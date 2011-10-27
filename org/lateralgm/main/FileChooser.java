/*
 * Copyright (C) 2007-2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
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
import org.lateralgm.file.GmFile;
import org.lateralgm.file.GmFile.FormatFlavor;
import org.lateralgm.file.GmFileReader;
import org.lateralgm.file.GmFileWriter;
import org.lateralgm.file.GmFormatException;
import org.lateralgm.file.ResourceList;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

public class FileChooser
	{
	public static List<FileReader> readers = new ArrayList<FileReader>();
	public static List<FileWriter> writers = new ArrayList<FileWriter>();
	public static List<FileView> fileViews = new ArrayList<FileView>();
	FileWriter selectedWriter;
	CustomFileChooser fc = new CustomFileChooser("/org/lateralgm","LAST_FILE_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
	FilterSet openFs = new FilterSet(), saveFs = new FilterSet();
	FilterUnion openAllFilter = new FilterUnion(), saveAllFilter = new FilterUnion();

	public static interface GroupFilter
		{
		FileFilter getGroupFilter();

		FileFilter[] getFilters();
		}

	public static interface FileReader
		{
		boolean canRead(URI uri);

		GmFile read(InputStream is, URI pathname, ResNode root) throws GmFormatException;
		}

	public static interface FileWriter
		{
		void write(OutputStream out, GmFile f, ResNode root) throws IOException;

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

	public FileChooser()
		{
		fc.setFileView(new FileViewUnion());

		//Add GM default readers, writers, and filters
		GmReader r = new GmReader();
		readers.add(r);
		addOpenFilters(r);

		int[] gmvers = { 810,800,701,600 };
		for (int gmver : gmvers)
			writers.add(new GmWriter(gmver));

		selectedWriter = writers.get(0); //TODO: need a better way to pick a default...

		addSaveFilters(new GmWriterFilter());
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

		public static final String MIME_URI_LIST = "uri-list";

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
		public String getName(File f)
			{
			for (FileView fv : fileViews)
				{
				String val = fv.getName(f);
				if (val != null) return val;
				}
			return super.getName(f);
			}

		public String getDescription(File f)
			{
			for (FileView fv : fileViews)
				{
				String val = fv.getDescription(f);
				if (val != null) return val;
				}
			return super.getDescription(f);
			}

		public String getTypeDescription(File f)
			{
			for (FileView fv : fileViews)
				{
				String val = fv.getTypeDescription(f);
				if (val != null) return val;
				}
			return super.getTypeDescription(f);
			}

		public Icon getIcon(File f)
			{
			for (FileView fv : fileViews)
				{
				Icon val = fv.getIcon(f);
				if (val != null) return val;
				}
			return super.getIcon(f);
			}

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

	protected class GmReader implements FileReader,GroupFilter
		{
		protected CustomFileFilter[] filters;
		protected CustomFileFilter groupFilter;

		protected GmReader()
			{
			String[] exts = { ".gm81",".gmk",".gm6",".gmd" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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

		public GmFile read(InputStream is, URI uri, ResNode root) throws GmFormatException
			{
			return GmFileReader.readGmFile(is,uri,root);
			}
		}

	protected class GmWriter implements FileWriter
		{
		int ver;

		public GmWriter(int ver)
			{
			this.ver = ver;
			}

		public void write(OutputStream out, GmFile f, ResNode root) throws IOException
			{
			GmFileWriter.writeGmFile(out,f,root,ver);
			}

		public String getSelectionName()
			{
			//XXX: Externalize the version string?
			return Integer.toString(ver);
			}

		public String getExtension()
			{
			switch (ver)
				{
				case 530:
					return ".gmd";
				case 600:
					return ".gm6";
				case 701:
				case 800:
					return ".gmk";
				case 810:
					return ".gm81";
				default:
					throw new IllegalArgumentException(Integer.toString(ver));
				}
			}

		public FormatFlavor getFlavor()
			{
			return FormatFlavor.getVersionFlavor(ver);
			}
		}

	protected class GmWriterFilter implements GroupFilter
		{
		protected CustomFileFilter[] filters;
		protected CustomFileFilter groupFilter;

		protected GmWriterFilter()
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
		LGM.currentFile = new GmFile();
		LGM.populateTree();
		fc.setSelectedFile(new File(new String()));
		selectedWriter = null;
		LGM.reload(true);
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

	public void open(URI uri, FileReader reader)
		{
		if (uri == null) return;
		try
			{
			try
				{
				LGM.currentFile = reader.read(uri.toURL().openStream(),uri,LGM.newRoot());
				}
			catch (MalformedURLException e)
				{
				return;
				}
			catch (IOException e)
				{
				return;
				}
			}
		catch (GmFormatException ex)
			{
			new ErrorDialog(LGM.frame,Messages.getString("FileChooser.ERROR_LOAD_TITLE"), //$NON-NLS-1$
					Messages.getString("FileChooser.ERROR_LOAD"),ex).setVisible(true); //$NON-NLS-1$
			LGM.currentFile = ex.file;
			LGM.populateTree();
			rebuildTree();
			}
		setTitleURI(uri);
		PrefsStore.addRecentFile(uri.toString());
		((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
		selectedWriter = null;
		LGM.reload(true);
		}

	private static FileReader findReader(URI uri)
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

	public static void save(URI uri, FileWriter writer) throws IOException
		{
		System.out.println(uri);
		try
			{
			writer.write(new FileOutputStream(new File(uri)),LGM.currentFile,LGM.root);
			return;
			}
		catch (IllegalArgumentException e)
			{ //Do the stuff below
			}
		URLConnection uc = uri.toURL().openConnection();
		uc.setDoOutput(true);
		writer.write(uc.getOutputStream(),LGM.currentFile,LGM.root);
		}

	private FileWriter findWriter(FormatFlavor flavor)
		{
		if (flavor == null)
			{
			System.out.println("null flavor");
			return null;
			}
		//Already have a selected writer? Don't need to find one (or worry about ambiguity)
		if (selectedWriter != null && selectedWriter.getFlavor() == flavor) return selectedWriter;
		//Else, look for writers that support our flavor
		FileWriter first = null;
		for (FileWriter writer : writers)
			if (writer.getFlavor() == flavor)
				{
				if (first == null)
					first = writer; //found one
				else
					{
					System.out.println("two flavor writers");
					//we found another writer supporting our flavor, leading to ambiguity
					//usually, we resolve this by opening a Save As dialog and let the user pick one.
					return null;
					}
				}
		if (first == null) System.out.println("No registered writer for flavor");
		return first;
		}

	public static boolean attemptBackup()
		{
		if (pushBackups(LGM.currentFile.uri.toString())) return true;
		int result = JOptionPane.showOptionDialog(LGM.frame,Messages.format("FileChooser.ERROR_BACKUP", //$NON-NLS-1$
				LGM.currentFile.uri),Messages.getString("FileChooser.ERROR_BACKUP_TITLE"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
		return result == JOptionPane.YES_OPTION;
		}

	private static boolean pushBackups(String fn)
		{
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
				String f = String.format(ff,bn,i);
				if (!new File(f).exists()) break;
				}
			if (i > nb)
				{
				i = nb;
				if (!new File(String.format(ff,bn,i)).delete()) break block;
				}
			for (i--; i >= 0; i--)
				{
				File f = new File(i > 0 ? String.format(ff,bn,i) : fn);
				if (!f.renameTo(new File(String.format(ff,bn,i + 1)))) break block;
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
		//pick an arbitrary default
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
