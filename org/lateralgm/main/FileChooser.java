/*
 * Copyright (C) 2007-2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.tree.TreeNode;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.ErrorDialog;
import org.lateralgm.components.GmMenuBar;
import org.lateralgm.components.CustomFileChooser.FilterSet;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.GmFileReader;
import org.lateralgm.file.GmFileWriter;
import org.lateralgm.file.GmFormatException;
import org.lateralgm.file.ResourceList;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

public class FileChooser
	{
	List<FileReader> readers = new ArrayList<FileReader>();
	CustomFileChooser fc = new CustomFileChooser("/org/lateralgm","LAST_FILE_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
	FilterSet openFs = new FilterSet();
	FilterUnion openAllFilter = new FilterUnion();
	FilterSet saveFs = new FilterSet();
	public static List<FileView> fileViews = new ArrayList<FileView>();

	public static interface FileReader
		{
		public FileFilter getGroupFilter();

		public FileFilter[] getFilters();

		public boolean canRead(File f);

		public GmFile readFile(File f, ResNode root) throws GmFormatException;
		}

	public static interface FileWriter
		{
		}

	public void addReader(FileReader fr)
		{
		readers.add(fr);
		openFs.add(fr.getGroupFilter());
		for (FileFilter ff : fr.getFilters())
			openFs.add(ff);
		openAllFilter.add(fr.getGroupFilter());
		if (readers.size() == 2) openFs.add(0,openAllFilter);
		}

	public FileChooser()
		{
		fc.setFileView(new FileViewUnion());
		addReader(new GmReader());

		String exts[] = { ".gm81",".gmk",".gm6" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		saveFs.addFilter("FileChooser.FORMAT_WRITERS_GM",exts); //$NON-NLS-1$
		saveFs.addFilter("FileChooser.FORMAT_GM81",exts[0]); //$NON-NLS-1$
		saveFs.addFilter("FileChooser.FORMAT_GMK",exts[1]); //$NON-NLS-1$
		saveFs.addFilter("FileChooser.FORMAT_GM6",exts[2]); //$NON-NLS-1$
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

	private class FilterUnion extends FileFilter
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

	protected class GmReader implements FileReader
		{
		CustomFileFilter[] filters;
		CustomFileFilter groupFilter;

		GmReader()
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

		public boolean canRead(File f)
			{
			return groupFilter.accept(f);
			}

		public GmFile readFile(File f, ResNode root) throws GmFormatException
			{
			return GmFileReader.readGmFile(f,root);
			}
		}

	private void setTitleFile(String titleFile)
		{
		LGM.frame.setTitle(Messages.format("LGM.TITLE",titleFile)); //$NON-NLS-1$
		}

	public void newFile()
		{
		setTitleFile(Messages.getString("LGM.NEWGAME")); //$NON-NLS-1$
		LGM.newRoot();
		LGM.currentFile = new GmFile();
		LGM.populateTree();
		fc.setSelectedFile(new File(new String()));
		LGM.reload(true);
		}

	public void openNewFile()
		{
		fc.setFilterSet(openFs);
		fc.setAccessory(null);
		if (fc.showOpenDialog(LGM.frame) != CustomFileChooser.APPROVE_OPTION) return;
		File f = fc.getSelectedFile();
		if (f == null) return;
		openFile(f);
		}

	/** Note that passing in null will cause an open dialog to display */
	public void openFile(File file)
		{
		if (file == null)
			{
			openNewFile();
			return;
			}
		if (!file.exists()) return;

		FileReader reader = findReader(file);
		if (reader == null)
			{
			String title = Messages.getString("FileChooser.UNRECOGNIZED_TITLE"); //$NON-NLS-1$
			String message = Messages.format("FileChooser.UNRECOGNIZED",file.getName()); //$NON-NLS-1$
			JOptionPane.showMessageDialog(LGM.frame,message,title,JOptionPane.WARNING_MESSAGE);
			return;
			}

		try
			{
			LGM.currentFile = reader.readFile(file,LGM.newRoot());
			}
		catch (GmFormatException ex)
			{
			new ErrorDialog(LGM.frame,Messages.getString("FileChooser.ERROR_LOAD_TITLE"), //$NON-NLS-1$
					Messages.getString("FileChooser.ERROR_LOAD"),Messages.format("FileChooser.DEBUG_INFO", //$NON-NLS-1$ //$NON-NLS-2$
							ex.getClass().getName(),ex.getMessage(),ex.stackAsString())).setVisible(true);
			LGM.currentFile = ex.file;
			LGM.populateTree();
			rebuildTree();
			}
		setTitleFile(file.getName());
		PrefsStore.addRecentFile(file.getPath());
		((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
		LGM.reload(true);
		}

	private FileReader findReader(File file)
		{
		for (FileReader fr : readers)
			if (fr.canRead(file)) return fr;
		return null;
		}

	private void rebuildTree()
		{
		for (int i = 0; i < LGM.root.getChildCount(); i++)
			{
			TreeNode n = LGM.root.getChildAt(i);
			if (!(n instanceof ResNode)) continue;
			ResNode rn = (ResNode) n;
			if (rn.status != ResNode.STATUS_PRIMARY) continue;
			ResourceList<?> rl = LGM.currentFile.getList(rn.kind);
			for (Resource<?,?> r : rl)
				rn.add(new ResNode(r.getName(),ResNode.STATUS_SECONDARY,r.getKind(),r.reference));
			}
		}

	public boolean saveFile()
		{
		if (LGM.currentFile.filename == null) return saveNewFile();
		LGM.commitAll();
		String ext = getVersionExtension(LGM.currentFile.fileVersion);
		if (!LGM.currentFile.filename.endsWith(ext))
			{
			int result = JOptionPane.showConfirmDialog(LGM.frame,Messages.format(
					"FileChooser.CONFIRM_EXTENSION",ext,LGM.currentFile.fileVersion), //$NON-NLS-1$
					LGM.currentFile.filename,JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.CANCEL_OPTION) return false;
			if (result == JOptionPane.NO_OPTION) return saveNewFile();
			//if result == yes then continue
			}
		attemptBackup();
		try
			{
			writeFile(LGM.currentFile,LGM.root);
			return true;
			}
		catch (IOException e)
			{
			e.printStackTrace();
			JOptionPane.showMessageDialog(LGM.frame,Messages.format("FileChooser.ERROR_SAVE", //$NON-NLS-1$
					LGM.currentFile.filename,e.getClass().getName(),e.getMessage()),
					Messages.getString("FileChooser.ERROR_SAVE_TITLE"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			return false;
			}
		}

	public boolean saveNewFile()
		{
		fc.setFilterSet(saveFs);
		fc.setAccessory(makeVersionRadio());
		String filename = LGM.currentFile.filename;
		fc.setSelectedFile(filename == null ? null : new File(filename));
		while (true) //repeatedly display dialog until a valid response is given
			{
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return false;
			filename = fc.getSelectedFile().getPath();
			if (forceExt.isSelected())
				{
				String ext = getVersionExtension(LGM.currentFile.fileVersion);
				if (!filename.endsWith(ext)) filename += ext;
				}
			int result = JOptionPane.YES_OPTION;
			if (new File(filename).exists())
				result = JOptionPane.showConfirmDialog(
						LGM.frame,
						Messages.format("FileChooser.CONFIRM_REPLACE",filename), //$NON-NLS-1$
						Messages.getString("FileChooser.CONFIRM_REPLACE_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION)
				{
				LGM.currentFile.filename = filename;
				LGM.frame.setTitle(Messages.format("LGM.TITLE",new File(filename).getName())); //$NON-NLS-1$
				if (!saveFile()) return false;
				PrefsStore.addRecentFile(filename);
				((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
				return true;
				}
			if (result == JOptionPane.CANCEL_OPTION) return false;
			}
		}

	//Backups
	public static boolean attemptBackup()
		{
		if (pushBackups(LGM.currentFile.filename)) return true;
		int result = JOptionPane.showOptionDialog(LGM.frame,Messages.format("FileChooser.ERROR_BACKUP", //$NON-NLS-1$
				LGM.currentFile.filename),Messages.getString("FileChooser.ERROR_BACKUP_TITLE"), //$NON-NLS-1$
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

	//Version Radio
	public static String getVersionExtension(int version)
		{
		switch (version)
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
				throw new IllegalArgumentException(Integer.toString(version));
			}
		}

	JCheckBox forceExt = new JCheckBox(Messages.getString("FileChooser.FORCE_EXT"),true); //$NON-NLS-1$

	public JPanel makeVersionRadio()
		{
		final int versions[] = { 810,800,701,600 };
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));
		ButtonGroup bg = new ButtonGroup();
		for (final int v : versions)
			{
			//XXX: Externalize the version string?
			JRadioButton b = new JRadioButton(Integer.toString(v),LGM.currentFile.fileVersion == v);
			bg.add(b);
			p.add(b);
			b.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						LGM.currentFile.fileVersion = v;
						}
				});
			}
		p.add(forceExt);
		return p;
		}

	//TODO: Remove
	void writeFile(GmFile f, ResNode root) throws IOException
		{
		GmFileWriter.writeGmFile(f,root);
		}
	}
