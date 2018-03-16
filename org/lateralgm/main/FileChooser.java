/*
 * Copyright (C) 2007-2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 *
 * This file is part of LateralGM.
 *
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.main;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.tree.TreeNode;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.CustomFileChooser.FilterSet;
import org.lateralgm.components.ErrorDialog;
import org.lateralgm.components.GmMenuBar;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GMXFileReader;
import org.lateralgm.file.GMXFileWriter;
import org.lateralgm.file.GmFileReader;
import org.lateralgm.file.GmFileWriter;
import org.lateralgm.file.GmFormatException;
import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.ProjectFile.FormatFlavor;
import org.lateralgm.file.ProjectFormatException;
import org.lateralgm.file.ResourceList;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

import static org.lateralgm.file.ProjectFile.FormatFlavor.*;

public class FileChooser
	{
	public static List<FileReader> readers = new ArrayList<FileReader>();
	public static List<FileWriter> writers = new ArrayList<FileWriter>();
	public static List<FileView> fileViews = new ArrayList<FileView>();
	static ProjectReader projectReader;
	static GMXIO gmxIO;
	FileWriter selectedWriter;
	CustomFileChooser fc = new CustomFileChooser("/org/lateralgm","LAST_FILE_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
	FilterSet openFs = new FilterSet(), saveFs = new FilterSet();
	FilterUnion openAllFilter = new FilterUnion(), saveAllFilter = new FilterUnion();

	public static void addDefaultReadersAndWriters()
		{
		if (gmxIO == null)
			{
			readers.add(gmxIO = new GMXIO());
			writers.add(gmxIO);
			}

		if (projectReader != null) return;
		readers.add(projectReader = new ProjectReader());

		int[] gmvers = { 810,800,701,600 };
		for (int gmver : gmvers)
			writers.add(new ProjectWriter(gmver));
		}

	public static interface GroupFilter
		{
		FileFilter getGroupFilter();

		FileFilter[] getFilters();
		}

	public static interface FileReader
		{
		boolean canRead(URI uri);

		void read(InputStream is, ProjectFile file, URI pathname, ResNode root) throws ProjectFormatException;
		}

	public static interface FileWriter
		{
		void write(OutputStream out, ProjectFile f, ResNode root) throws ProjectFormatException,
				IOException;

		String getSelectionName();

		String getExtension();

		FormatFlavor getFlavor();
		}

	public void addOpenFilters(GroupFilter gf)
		{
		addFilters(openFs,openAllFilter,gf);
		}

	public void addSaveFilters(GroupFilter gf)
		{
		addFilters(saveFs,saveAllFilter,gf);
		}

	public static void addFilters(FilterSet fs, FilterUnion all, GroupFilter gf)
		{
		fs.add(gf.getGroupFilter());
		all.add(gf.getGroupFilter());
		for (FileFilter ff : gf.getFilters())
			fs.add(ff);
		if (all.size() == 2) fs.add(0,all);
		}

	/**
	 * Typically you construct a FileChooser when you want a graphical side of things.
	 * Headless applications should use the static methods and fields available.
	 */
	public FileChooser()
		{
		fc.setFileView(new FileViewUnion());

		addDefaultReadersAndWriters();
		addOpenFilters(gmxIO);
		addOpenFilters(projectReader);
		selectedWriter = writers.get(0); //TODO: need a better way to pick a default...

		addSaveFilters(gmxIO);
		addSaveFilters(new ProjectWriterFilter());
		}

	public class LGMDropHandler extends FileDropHandler
		{
		private static final long serialVersionUID = 1L;

		public boolean importData(TransferHandler.TransferSupport evt)
			{
			List<?> files = getDropList(evt);
			if (files == null || files.isEmpty()) return false;
			if (files.size() != 1) return false; //handle multiple files down the road
			Object o = files.get(0);
			if (o instanceof File)
				{
				open(((File) o).toURI());
				return true;
				}
			if (o instanceof URI)
				{
				open((URI) o);
				return true;
				}
			return false;
			}
		}

	public static abstract class FileDropHandler extends TransferHandler
		{
		private static final long serialVersionUID = 1L;

		public static final String MIME_URI_LIST = "uri-list"; //$NON-NLS-1$

		@SuppressWarnings("static-method")
		public boolean isDataFlavorSupported(DataFlavor df)
			{
			return df.isFlavorJavaFileListType()
					|| (df.isRepresentationClassReader() && MIME_URI_LIST.equals(df.getSubType()));
			}

		public boolean canImport(TransferHandler.TransferSupport evt)
			{
			//Mac won't let us grab the transferable.
			return getSupportedFlavor(evt.getDataFlavors()) != null;
			}

		protected DataFlavor getSupportedFlavor(DataFlavor...dfs)
			{
			for (DataFlavor df : dfs)
				if (isDataFlavorSupported(df)) return df;
			return null;
			}

		public List<?> getDropList(TransferHandler.TransferSupport evt)
			{
			Transferable tr = evt.getTransferable();
			DataFlavor df = getSupportedFlavor(evt.getDataFlavors());
			if (df == null) return null;
			try
				{
				if (df.isFlavorJavaFileListType())
					return (List<?>) tr.getTransferData(DataFlavor.javaFileListFlavor);

				//Linux support (uri-list reader)
				if (!df.isRepresentationClassReader() || !MIME_URI_LIST.equals(df.getSubType()))
					return null; //Or not? Let implementation handle it.

				BufferedReader br = new BufferedReader(df.getReaderForText(tr));
				List<URI> uriList = new LinkedList<URI>();
				String line;
				while ((line = br.readLine()) != null)
					{
					try
						{
						// kde seems to append a 0 char to the end of the reader
						if (line.isEmpty() || line.length() == 1 && line.charAt(0) == (char) 0) continue;
						uriList.add(new URI(line));
						}
					catch (URISyntaxException ex)
						{
						//Omit bad URI files from list.
						}
					catch (IllegalArgumentException ex)
						{
						//Omit unresolvable URLs from list.
						}
					}
				br.close();
				return uriList;
				}
			catch (UnsupportedFlavorException e)
				{
				//Looks like our flavor suddenly deserted us. Oh well.
				}
			catch (IOException e)
				{
				//The flavor or the reader is misbehaving. Oh well.
				}
			return null;
			}
		}

	private class FileViewUnion extends FileView
		{
		@Override
		public String getName(File f)
			{
			for (FileView fv : fileViews)
				{
				String val = fv.getName(f);
				if (val != null) return val;
				}
			return super.getName(f);
			}

		@Override
		public String getDescription(File f)
			{
			for (FileView fv : fileViews)
				{
				String val = fv.getDescription(f);
				if (val != null) return val;
				}
			return super.getDescription(f);
			}

		@Override
		public String getTypeDescription(File f)
			{
			for (FileView fv : fileViews)
				{
				String val = fv.getTypeDescription(f);
				if (val != null) return val;
				}
			return super.getTypeDescription(f);
			}

		@Override
		public Icon getIcon(File f)
			{
			for (FileView fv : fileViews)
				{
				Icon val = fv.getIcon(f);
				if (val != null) return val;
				}
			return super.getIcon(f);
			}

		@Override
		public Boolean isTraversable(File f)
			{
			for (FileView fv : fileViews)
				{
				Boolean val = fv.isTraversable(f);
				if (val != null) return val;
				}
			return super.isTraversable(f);
			}
		}

	public static class FilterUnion extends FileFilter
		{
		List<FileFilter> filters = new ArrayList<FileFilter>();

		public FilterUnion(FileFilter...filters)
			{
			add(filters);
			}

		public void add(FileFilter...filters)
			{
			for (FileFilter ff : filters)
				this.filters.add(ff);
			}

		public int size()
			{
			return filters.size();
			}

		@Override
		public boolean accept(File f)
			{
			for (FileFilter ff : filters)
				if (ff.accept(f)) return true;
			return false;
			}

		@Override
		public String getDescription()
			{
			return Messages.getString("FileChooser.ALL_SUPPORTED"); //$NON-NLS-1$
			}
		}

	protected static class ProjectReader implements FileReader,GroupFilter
		{
		protected CustomFileFilter[] filters;
		protected CustomFileFilter groupFilter;

		protected ProjectReader()
			{
			String[] exts = { ".gm81",".gmk",".gm6",".gmd", }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			String[] descs = { "GM81","GMK","GM6","GMD" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			groupFilter = new CustomFileFilter(Messages.getString("FileChooser.FORMAT_READERS_GM"),exts); //$NON-NLS-1$
			filters = new CustomFileFilter[exts.length];
			for (int i = 0; i < exts.length; i++)
				filters[i] = new CustomFileFilter(
						Messages.getString("FileChooser.FORMAT_" + descs[i]),exts[i]); //$NON-NLS-1$
			}

		public FileFilter getGroupFilter()
			{
			return groupFilter;
			}

		public FileFilter[] getFilters()
			{
			return filters;
			}

		public boolean canRead(URI f)
			{
			return groupFilter.accept(new File(f));
			}

		public void read(InputStream is, ProjectFile file, URI uri, ResNode root) throws ProjectFormatException
			{
			GmFileReader.readProjectFile(is,file,uri,root);
			}
		}

	protected static class ProjectWriter implements FileWriter
		{
		private int ver;

		public ProjectWriter(int ver)
			{
			this.ver = ver;
			}

		public void write(OutputStream out, ProjectFile f, ResNode root) throws ProjectFormatException
			{
				try
					{
					GmFileWriter.writeProjectFile(out,f,root,ver);
					}
				catch (IOException e)
					{
					throw new GmFormatException(f,e);
					}
			}

		public String getSelectionName()
			{
			//XXX: Externalize the version string?
			return Integer.toString(ver);
			}

		public FormatFlavor getFlavor()
			{
			return FormatFlavor.getVersionFlavor(ver);
			}

		public String getExtension()
			{
			switch (ver)
				{
				case 530:
					return ".gmd"; //$NON-NLS-1$
				case 600:
					return ".gm6"; //$NON-NLS-1$
				case 701:
				case 800:
					return ".gmk"; //$NON-NLS-1$
				case 810:
					return ".gm81"; //$NON-NLS-1$
				default:
					throw new IllegalArgumentException(Integer.toString(ver));
				}
			}
		}

	protected class ProjectWriterFilter implements GroupFilter
		{
		protected CustomFileFilter[] filters;
		protected CustomFileFilter groupFilter;

		protected ProjectWriterFilter()
			{
			final String exts[] = { ".gm81",".gmk",".gm6" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			final String[] descs = { "GM81","GMK","GM6" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			groupFilter = new CustomFileFilter(Messages.getString("FileChooser.FORMAT_WRITERS_GM"),exts); //$NON-NLS-1$
			filters = new CustomFileFilter[exts.length];
			for (int i = 0; i < exts.length; i++)
				filters[i] = new CustomFileFilter(
						Messages.getString("FileChooser.FORMAT_" + descs[i]),exts[i]); //$NON-NLS-1$
			}

		public FileFilter[] getFilters()
			{
			return filters;
			}

		public FileFilter getGroupFilter()
			{
			return groupFilter;
			}
		}

	protected static class GMXIO extends FileView implements FileReader,FileWriter,GroupFilter
	{
	static final String ext = ".project.gmx"; //$NON-NLS-1$
	CustomFileFilter filter = new CustomFileFilter(
			Messages.getString("FileChooser.FORMAT_GMX"),ext); //$NON-NLS-1$

	public FileFilter getGroupFilter()
		{
		return filter;
		}

	public FileFilter[] getFilters()
		{
		return new FileFilter[0];
		}

	public boolean canRead(URI uri)
		{
		return filter.accept(new File(uri));
		}

	public void read(InputStream in, ProjectFile file, URI uri, ResNode root) throws GmFormatException
		{
		GMXFileReader.readProjectFile(in,file,uri,root);
		}

	@Override
	public String getExtension()
		{
		return ext;
		}

	@Override
	public String getSelectionName()
		{
		return "GMX"; //$NON-NLS-1$
		}

	@Override
	public void write(OutputStream out, ProjectFile f, ResNode root) throws ProjectFormatException
		{
		try
			{
			GMXFileWriter.writeProjectFile(out,f,root);
			}
		catch (Exception e)
			{
			throw new GmFormatException(f,e);
			}
		}

	@Override
	public FormatFlavor getFlavor()
		{
		return FormatFlavor.GMX;
		}
	}

	public static void setTitleURI(URI uri)
		{
		LGM.frame.setTitle(Messages.format("LGM.TITLE",getTitleFromURI(uri))); //$NON-NLS-1$
		}

	private static String getTitleFromURI(URI uri)
		{
		if (uri == null) return Messages.getString("LGM.NEWGAME"); //$NON-NLS-1$
		try
			{
			return new File(uri).getName();
			}
		catch (IllegalArgumentException e)
			{
			return uri.toString();
			}
		}

	public void newFile()
		{
		setTitleURI(null);
		LGM.newRoot();
		LGM.currentFile = new ProjectFile();
		LGM.populateTree();
		fc.setSelectedFile(new File(new String()));
		selectedWriter = null;
		LGM.reload(true);
		OutputManager.append("\n" + Messages.getString("FileChooser.PROJECTCREATED") + ": " + new Date().toString());
		}

	public void openNewFile()
		{
		fc.setFilterSet(openFs);
		fc.setAccessory(null);
		if (fc.showOpenDialog(LGM.frame) != CustomFileChooser.APPROVE_OPTION) return;
		File f = fc.getSelectedFile();
		if (f == null) return;
		open(f.toURI());
		}

	/** Note that passing in null will cause an open dialog to display */
	public void open(File file)
		{
		if (file == null || !file.exists())
			{
			int result = JOptionPane.showConfirmDialog(LGM.frame,
					"Would you like to choose a different file?","File Not Found",JOptionPane.YES_NO_OPTION,
					JOptionPane.ERROR_MESSAGE);
			if (result == JOptionPane.YES_OPTION)
				openNewFile();
			return;
			}
		FileReader reader = findReader(file.toURI());
		if (reader == null)
			{
			String title = Messages.getString("FileChooser.UNRECOGNIZED_TITLE"); //$NON-NLS-1$
			String message = Messages.format("FileChooser.UNRECOGNIZED",file); //$NON-NLS-1$
			JOptionPane.showMessageDialog(LGM.frame,message,title,JOptionPane.WARNING_MESSAGE);
			return;
			}
		open(file.toURI(),reader);
		}

	/** Note that passing in null will cause an open dialog to display */
	public void open(URI uri)
		{
		if (uri == null)
			{
			openNewFile();
			return;
			}
		FileReader reader = findReader(uri);
		if (reader == null)
			{
			String title = Messages.getString("FileChooser.UNRECOGNIZED_TITLE"); //$NON-NLS-1$
			String message = Messages.format("FileChooser.UNRECOGNIZED",uri); //$NON-NLS-1$
			JOptionPane.showMessageDialog(LGM.frame,message,title,JOptionPane.WARNING_MESSAGE);
			return;
			}
		open(uri,reader);
		}

	/**
	 * Both open() methods are not headless. For a headless open:
	 * <code>findReader(uri).read(uriStream,uri,root)</code>
	 */
	public void open(final URI uri, final FileReader reader)
		{
		if (uri == null) return;
		LGM.getProgressDialog().setVisible(false);
		Thread t = new Thread(new Runnable()
			{
				public void run()
					{
					LGM.addDefaultExceptionHandler();
					try
						{
						ProjectFile f =  new ProjectFile();
						f.uri = uri;
						reader.read(uri.toURL().openStream(),f,uri,LGM.newRoot());
						LGM.currentFile = f;
						}
					catch (ProjectFormatException ex)
						{
						LGM.currentFile = ex.file;
						LGM.populateTree();
						rebuildTree();
						LGM.showDefaultExceptionHandler(ex);
						ErrorDialog.getInstance().setMessage(Messages.getString("FileChooser.ERROR_LOAD")); //$NON-NLS-1$
						ErrorDialog.getInstance().setTitle(Messages.getString("FileChooser.ERROR_LOAD_TITLE")); //$NON-NLS-1$
						}
					catch (Exception e)
						{
						// TODO: This catches exceptions in reading without freezing the program with the
						// progress bar or destroying the tree.
						LGM.populateTree();
						rebuildTree();
						LGM.showDefaultExceptionHandler(e);
						ErrorDialog.getInstance().setMessage(Messages.getString("FileChooser.ERROR_LOAD")); //$NON-NLS-1$
						ErrorDialog.getInstance().setTitle(Messages.getString("FileChooser.ERROR_LOAD_TITLE")); //$NON-NLS-1$
						}
					setTitleURI(uri);
					PrefsStore.addRecentFile(uri.toString());
					((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
					selectedWriter = null;
					LGM.setProgressDialogVisible(false);
					OutputManager.append("\n" + Messages.getString("FileChooser.PROJECTLOADED") + ": " +
							new Date().toString() + " " + uri.getPath());
					}
			});
		t.start();
		LGM.setProgressDialogVisible(true);
		LGM.reload(true);
		Listener.checkIdsInteractive(false);
		}

	public static FileReader findReader(URI uri)
		{
		for (FileReader fr : readers)
			if (fr.canRead(uri)) return fr;
		return null;
		}

	private static void rebuildTree()
		{
		for (int i = 0; i < LGM.root.getChildCount(); i++)
			{
			TreeNode n = LGM.root.getChildAt(i);
			if (!(n instanceof ResNode)) continue;
			ResNode rn = (ResNode) n;
			if (rn.status != ResNode.STATUS_PRIMARY || !rn.isInstantiable()) continue;
			ResourceList<?> rl = (ResourceList<?>) LGM.currentFile.resMap.get(rn.kind);
			for (Resource<?,?> r : rl)
				rn.add(new ResNode(r.getName(),ResNode.STATUS_SECONDARY,r.getClass(),r.reference));
			}
		}

	public boolean saveNewFile()
		{
		fc.setFilterSet(saveFs);
		//Populated fresh each time to ensure an up-to-date list of writers
		fc.setAccessory(makeSelectionAccessory());
		URI uri = LGM.currentFile.uri;
		File file = uri == null ? null : new File(uri);
		fc.setSelectedFile(file);
		uri = null;
		do //repeatedly display dialog until a valid response is given
			{
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return false;
			file = fc.getSelectedFile();
			if (forceExt.isSelected())
				{
				String ext = selectedWriter.getExtension();
				if (!file.getName().endsWith(ext)) file = new File(file.getPath() + ext);
				}
			// Create the folder for the user, otherwise people get confused.
			if (selectedWriter.getFlavor().equals(GMX) && file.getName().endsWith(".project.gmx")) //$NON-NLS-1$
				{
				file = new File(file.getAbsolutePath().replace(".project.gmx",".gmx") + '/' //$NON-NLS-1$ //$NON-NLS-2$
						+ file.getName());
				file.getParentFile().mkdir();
				}
			int result = JOptionPane.YES_OPTION;
			if (file.exists())
				result = JOptionPane.showConfirmDialog(
						LGM.frame,
						Messages.format("FileChooser.CONFIRM_REPLACE",file.getPath()), //$NON-NLS-1$
						Messages.getString("FileChooser.CONFIRM_REPLACE_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION) uri = file.toURI();
			if (result == JOptionPane.CANCEL_OPTION) return false;
			}
		while (uri == null);
		return save(uri,selectedWriter.getFlavor());
		}

	/**
	 * This method is not headless. For a headless save:
	 * <code>save(uri,findWriter(flavor))</code>
	 */
	public boolean save(URI uri, FormatFlavor flavor)
		{
		selectedWriter = findWriter(flavor);
		System.out.println(selectedWriter == null ? "null writer" : selectedWriter.getSelectionName());
		if (uri == null || selectedWriter == null) return saveNewFile();

		LGM.currentFile.format = flavor;

		if (uri != LGM.currentFile.uri)
			{
			LGM.currentFile.uri = uri;
			setTitleURI(uri);
			PrefsStore.addRecentFile(uri.toString());
			((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
			}

		LGM.commitAll();

		String ext = selectedWriter.getExtension();
		if (!uri.getPath().endsWith(ext))
			{
			int result = JOptionPane.showConfirmDialog(LGM.frame,
					Messages.format("FileChooser.CONFIRM_EXTENSION",ext,selectedWriter.getSelectionName()), //$NON-NLS-1$
					uri.toString(),JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.CANCEL_OPTION) return false;
			if (result == JOptionPane.NO_OPTION) return saveNewFile();
			//if result == yes then continue
			}

		attemptBackup();
		try
			{
			save(uri,selectedWriter);
			return true;
			}
		catch (IOException e)
			{
			e.printStackTrace();
			JOptionPane.showMessageDialog(LGM.frame,Messages.format("FileChooser.ERROR_SAVE", //$NON-NLS-1$
					uri,e.getClass().getName(),e.getMessage()),
					Messages.getString("FileChooser.ERROR_SAVE_TITLE"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			return false;
			}
		}

	/** This method is headless-safe. */
	public static void save(final URI uri, final FileWriter writer) throws IOException
		{
		LGM.resetChanges();
		System.out.println(uri);
		LGM.getProgressDialog().setVisible(false);
		Thread t = new Thread(new Runnable()
			{
				public void run()
					{
					LGM.addDefaultExceptionHandler();
					try
						{
						writer.write(new FileOutputStream(new File(uri)),LGM.currentFile,LGM.root);
						OutputManager.append("\n" + Messages.getString("FileChooser.PROJECTSAVED") + ": " +
								new Date().toString() + " " + uri.getPath());
						LGM.setProgressDialogVisible(false);
						return;
						}
					catch (ProjectFormatException e)
						{
						LGM.showDefaultExceptionHandler(e);
						}
					catch (Exception e)
						{
						LGM.showDefaultExceptionHandler(e);
						}
					URLConnection uc = null;
					try
						{
						uc = uri.toURL().openConnection();
						}
					catch (Exception e)
						{
						LGM.showDefaultExceptionHandler(e);
						}
					uc.setDoOutput(true);
					try
						{
						writer.write(uc.getOutputStream(),LGM.currentFile,LGM.root);
						}
					catch (ProjectFormatException e)
						{
						LGM.showDefaultExceptionHandler(e);
						}
					catch (Exception e)
						{
						LGM.showDefaultExceptionHandler(e);
						}
					LGM.setProgressDialogVisible(false);
					}
			});
		t.start();
		LGM.setProgressDialogVisible(true);
		}

	public FileWriter findWriter(FormatFlavor flavor)
		{
		if (flavor == null)
			{
			System.out.println("null flavor");
			return null;
			}
		// Already have a selected writer? Don't need to find one (or worry about ambiguity)
		if (selectedWriter != null && selectedWriter.getFlavor() == flavor) return selectedWriter;
		// Else, look for writers that support our flavor
		FileWriter first = null;
		for (FileWriter writer : writers)
			if (writer.getFlavor() == flavor)
				{
				if (first == null)
					first = writer; //found one
				else
					{
					System.out.println("two flavor writers");
					// we found another writer supporting our flavor, leading to ambiguity
					// usually, we resolve this by opening a Save As dialog and let the user pick one.
					return null;
					}
				}
		if (first == null) System.out.println("No registered writer for flavor");
		return first;
		}

	public static boolean attemptBackup()
		{
		if (pushBackups(new File(LGM.currentFile.uri))) return true;
		int result = JOptionPane.showOptionDialog(LGM.frame,Messages.format("FileChooser.ERROR_BACKUP", //$NON-NLS-1$
				LGM.currentFile.uri),Messages.getString("FileChooser.ERROR_BACKUP_TITLE"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
		return result == JOptionPane.YES_OPTION;
		}

	private static boolean pushBackups(File f)
		{
		String fn = f.getPath();
		int nb = PrefsStore.getNumberOfBackups();
		if (nb <= 0 || !new File(fn).exists()) return true;
		String bn;
		if (fn.endsWith(".gm6") || fn.endsWith(".gmk"))
			bn = fn.substring(0,fn.length() - 4);
		else if (fn.endsWith(".gm81"))
			bn = fn.substring(0,fn.length() - 5);
		else
			bn = fn;
		block:
			{
			String ff = "%s.gb%d";
			int i;
			for (i = 1; i <= nb; i++)
				{
				String bf = String.format(ff,bn,i);
				if (!new File(bf).exists()) break;
				}
			if (i > nb)
				{
				i = nb;
				if (!new File(String.format(ff,bn,i)).delete()) break block;
				}
			for (i--; i >= 0; i--)
				{
				File bf = new File(i > 0 ? String.format(ff,bn,i) : fn);
				if (!bf.renameTo(new File(String.format(ff,bn,i + 1)))) break block;
				}
			return true;
			}
		return false;
		}

	JCheckBox forceExt = new JCheckBox(Messages.getString("FileChooser.FORCE_EXT"),true); //$NON-NLS-1$

	JPanel makeSelectionAccessory()
		{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));
		ButtonGroup bg = new ButtonGroup();
		selectedWriter = findWriter(LGM.currentFile.format);
		// pick an arbitrary default
		if (selectedWriter == null) selectedWriter = writers.get(0);
		for (final FileWriter writer : writers)
			{
			JRadioButton b = new JRadioButton(writer.getSelectionName(),selectedWriter == writer);
			bg.add(b);
			p.add(b);
			b.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						selectedWriter = writer;
						}
				});
			}

		JPanel r = new JPanel();
		r.setLayout(new BoxLayout(r,BoxLayout.PAGE_AXIS));
		r.add(new JScrollPane(p));
		r.add(forceExt);
		return r;
		}
	}
