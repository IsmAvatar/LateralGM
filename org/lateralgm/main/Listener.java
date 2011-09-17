/*
 * Copyright (C) 2007, 2008, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import static org.lateralgm.main.Util.deRef;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.lateralgm.components.AboutBox;
import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.CustomFileChooser.FilterSet;
import org.lateralgm.components.ErrorDialog;
import org.lateralgm.components.GmMenuBar;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.GmFileReader;
import org.lateralgm.file.GmFileWriter;
import org.lateralgm.file.GmFormatException;
import org.lateralgm.file.ResourceList;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;

public class Listener extends TransferHandler implements ActionListener,CellEditorListener
	{
	private static final long serialVersionUID = 1L;
	MListener mListener = new MListener();
	private CustomFileChooser fc;
	private FilterSet openFs = new FilterSet();
	private FilterSet saveFs = new FilterSet();

	public Listener()
		{
		String exts[] = { ".gm81",".gmk",".gm6",".gmd" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		fc = new CustomFileChooser("/org/lateralgm","LAST_FILE_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		openFs.addFilter("Listener.FORMAT_GM",exts); //$NON-NLS-1$
		openFs.addFilter("Listener.FORMAT_GM81",exts[0]); //$NON-NLS-1$
		openFs.addFilter("Listener.FORMAT_GMK",exts[1]); //$NON-NLS-1$
		openFs.addFilter("Listener.FORMAT_GM6",exts[2]); //$NON-NLS-1$
		openFs.addFilter("Listener.FORMAT_GMD",exts[3]); //$NON-NLS-1$

		saveFs.addFilter("Listener.FORMAT_GM",exts); //$NON-NLS-1$
		saveFs.addFilter("Listener.FORMAT_GM81",exts[0]); //$NON-NLS-1$
		saveFs.addFilter("Listener.FORMAT_GMK",exts[1]); //$NON-NLS-1$
		saveFs.addFilter("Listener.FORMAT_GM6",exts[2]); //$NON-NLS-1$
		}

	public static Resource.Kind stringToRes(String com)
		{
		try
			{
			return Resource.Kind.valueOf(com);
			}
		catch (IllegalArgumentException e)
			{
			return null;
			}
		}

	/** Note that passing in null will cause an open dialog to display */
	public void openFile(String filename)
		{
		File file;
		if (filename != null)
			file = new File(filename);
		else
			{
			fc.setFilterSet(openFs);
			fc.setAccessory(null);
			if (fc.showOpenDialog(LGM.frame) != CustomFileChooser.APPROVE_OPTION) return;
			file = fc.getSelectedFile();
			if (file == null) return;
			filename = file.getPath();
			}
		if (!file.exists()) return;

		PrefsStore.addRecentFile(filename);
		LGM.frame.setTitle(Messages.format("LGM.TITLE",file.getName())); //$NON-NLS-1$
		((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
		LGM.root = new ResNode("Root",(byte) 0,null,null); //$NON-NLS-1$;
		try
			{
			LGM.currentFile = GmFileReader.readGmFile(filename,LGM.root);
			}
		catch (GmFormatException ex)
			{
			new ErrorDialog(LGM.frame,Messages.getString("Listener.ERROR_TITLE"), //$NON-NLS-1$
					Messages.getString("Listener.ERROR_MESSAGE"),Messages.format("Listener.DEBUG_INFO", //$NON-NLS-1$ //$NON-NLS-2$
							ex.getClass().getName(),ex.getMessage(),ex.stackAsString())).setVisible(true);
			LGM.currentFile = ex.file;
			LGM.populateTree();
			//Rebuild the tree based on what we have
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
		LGM.reload(true);
		}

	public void newFile()
		{
		LGM.frame.setTitle(Messages.format("LGM.TITLE",Messages.getString("LGM.NEWGAME"))); //$NON-NLS-1$ //$NON-NLS-2$
		LGM.root = new ResNode("Root",(byte) 0,null,null); //$NON-NLS-1$
		LGM.currentFile = new GmFile();
		LGM.populateTree();
		fc.setSelectedFile(new File(new String()));
		LGM.reload(true);
		}

	public static class BackupException extends Exception
		{
		private static final long serialVersionUID = 1L;

		public BackupException(String message)
			{
			super(message);
			}

		public BackupException(Throwable cause)
			{
			super(cause);
			}
		}

	private static void pushBackups(String fn) throws BackupException
		{
		int nb = PrefsStore.getNumberOfBackups();
		if (nb <= 0 || !new File(fn).exists()) return;
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
			return;
			}
		throw new BackupException(fn);
		}

	public boolean saveFile()
		{
		if (LGM.currentFile.filename == null)
			{
			return saveNewFile();
			}
		LGM.commitAll();
		String ext = getVersionExtension(LGM.currentFile.fileVersion);
		if (!LGM.currentFile.filename.endsWith(ext))
			{
			int result = JOptionPane.showConfirmDialog(LGM.frame,Messages.format(
					"Listener.CONFIRM_EXTENSION",ext,LGM.currentFile.fileVersion), //$NON-NLS-1$
					LGM.currentFile.filename,JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.CANCEL_OPTION) return false;
			if (result == JOptionPane.NO_OPTION) return saveNewFile();
			//if result == yes then continue
			}
		try
			{
			pushBackups(LGM.currentFile.filename);
			}
		catch (BackupException e)
			{
			int result = JOptionPane.showOptionDialog(LGM.frame,Messages.format("Listener.ERROR_BACKUP", //$NON-NLS-1$
					LGM.currentFile.filename),Messages.getString("Listener.ERROR_BACKUP_TITLE"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
			if (result != JOptionPane.YES_OPTION) return false;
			}
		try
			{
			GmFileWriter.writeGmFile(LGM.currentFile,LGM.root);
			return true;
			}
		catch (IOException e)
			{
			e.printStackTrace();
			JOptionPane.showMessageDialog(LGM.frame,Messages.format("Listener.ERROR_SAVE", //$NON-NLS-1$
					LGM.currentFile.filename,e.getClass().getName(),e.getMessage()),
					Messages.getString("Listener.ERROR_SAVE_TITLE"),JOptionPane.ERROR_MESSAGE);
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
				result = JOptionPane.showConfirmDialog(LGM.frame,Messages.format(
						"Listener.CONFIRM_REPLACE",filename), //$NON-NLS-1$
						Messages.getString("Listener.CONFIRM_REPLACE_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
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

	// vvv These should probably be moved to somewhere else vvv //
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

	JCheckBox forceExt = new JCheckBox("Force extension",true);

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

	// ^^^ These should probably be moved to somewhere else ^^^ //

	protected static void addResource(JTree tree, String com)
		{
		addResource(tree,stringToRes(com),null);
		}

	protected static void addResource(JTree tree, Resource.Kind r)
		{
		addResource(tree,r,null);
		}

	protected static void addResource(JTree tree, Resource.Kind r, Resource<?,?> res)
		{
		ResNode node = (ResNode) tree.getLastSelectedPathComponent();
		if (node == null) return;
		ResNode parent;
		int pos;
		if (node.getAllowsChildren())
			{
			parent = node;
			pos = parent.getChildCount();
			}
		else
			{
			parent = (ResNode) node.getParent();
			pos = parent.getIndex(node) + 1;
			}
		putNode(tree,node,parent,r,pos,res);
		}

	protected static void insertResource(JTree tree, String com)
		{
		insertResource(tree,stringToRes(com),null);
		}

	protected static void insertResource(JTree tree, Resource.Kind r)
		{
		insertResource(tree,r,null);
		}

	private static void insertResource(JTree tree, Resource.Kind r, Resource<?,?> res)
		{
		ResNode node = (ResNode) tree.getLastSelectedPathComponent();
		if (node == null) return;
		ResNode parent = (ResNode) node.getParent();
		if (parent.isRoot())
			{
			addResource(tree,r,res);
			return;
			}
		int pos = parent.getIndex(node);
		putNode(tree,node,parent,r,pos,res);
		}

	public static void putNode(JTree tree, ResNode node, ResNode parent, Resource.Kind r, int pos,
			Resource<?,?> res)
		{
		if (r == null)
			{
			String msg = Messages.getString("Listener.INPUT_GROUPNAME"); //$NON-NLS-1$
			String name = JOptionPane.showInputDialog(msg,
					Messages.getString("Listener.DEFAULT_GROUPNAME")); //$NON-NLS-1$
			if (name == "" || name == null) return; //$NON-NLS-1$
			ResNode g = new ResNode(name,ResNode.STATUS_GROUP,parent.kind);
			parent.insert(g,pos);
			tree.expandPath(new TreePath(parent.getPath()));
			tree.setSelectionPath(new TreePath(g.getPath()));
			tree.updateUI();
			return;
			}

		if (node.kind != r)
			{
			parent = getPrimaryParent(r);
			pos = parent.getChildCount();
			}

		Resource<?,?> resource = res == null ? LGM.currentFile.getList(parent.kind).add() : res;
		ResNode g = new ResNode(resource.getName(),ResNode.STATUS_SECONDARY,parent.kind,
				resource.reference);
		parent.insert(g,pos);
		tree.expandPath(new TreePath(parent.getPath()));
		tree.setSelectionPath(new TreePath(g.getPath()));
		tree.updateUI();
		g.openFrame(true);
		}

	protected static void deleteSelectedResource(JTree tree)
		{
		ResNode me = (ResNode) tree.getLastSelectedPathComponent();
		if (me == null) return;
		if (me.status == ResNode.STATUS_PRIMARY) return;
		String msg = Messages.getString("Listener.CONFIRM_DELETERESOURCE"); //$NON-NLS-1$
		if (JOptionPane.showConfirmDialog(null,msg,
				Messages.getString("Listener.CONFIRM_DELETERESOURCE_TITLE"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION) == 0)
			{
			ResNode next = (ResNode) me.getNextSibling();
			if (next == null) next = (ResNode) me.getParent();
			if (next.isRoot()) next = (ResNode) next.getFirstChild();
			tree.setSelectionPath(new TreePath(next.getPath()));
			Enumeration<?> nodes = me.depthFirstEnumeration();
			// Calling dispose() on a resource modifies the tree and invalidates
			// the enumeration, so we need to wait until after traversal.
			HashSet<Resource<?,?>> rs = new HashSet<Resource<?,?>>();
			while (nodes.hasMoreElements())
				{
				ResNode node = (ResNode) nodes.nextElement();
				if (node.frame != null) node.frame.dispose();
				if (node.status == ResNode.STATUS_SECONDARY)
					{
					Resource<?,?> res = deRef((ResourceReference<?>) node.getRes());
					if (res != null) rs.add(res);
					LGM.currentFile.getList(node.kind).remove(res);
					}
				}
			for (Resource<?,?> r : rs)
				r.dispose();
			me.removeFromParent();
			tree.updateUI();
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		JTree tree = LGM.tree;
		String[] args = e.getActionCommand().split(" "); //$NON-NLS-1$
		String com = args[0];
		if (com.endsWith(".NEW")) //$NON-NLS-1$
			{
			newFile();
			return;
			}
		if (com.endsWith(".OPEN")) //$NON-NLS-1$
			{
			openFile(args.length > 1 ? Util.urlDecode(args[1]) : null);
			return;
			}
		if (com.endsWith(".SAVE")) //$NON-NLS-1$
			{
			saveFile();
			return;
			}
		if (com.endsWith(".SAVEAS")) //$NON-NLS-1$
			{
			saveNewFile();
			return;
			}
		if (com.endsWith(".EVENT_BUTTON")) //$NON-NLS-1$
			{
			LGM.eventSelect.setVisible(true);
			LGM.eventSelect.toTop();
			return;
			}
		if (com.endsWith(".EXIT")) //$NON-NLS-1$
			{
			System.exit(0);
			return;
			}
		if (com.contains(".INSERT_")) //$NON-NLS-1$
			{
			insertResource(tree,com.substring(com.lastIndexOf('_') + 1));
			return;
			}
		if (com.contains(".ADD_")) //$NON-NLS-1$
			{
			addResource(tree,com.substring(com.lastIndexOf('_') + 1));
			return;
			}
		if (com.endsWith(".RENAME")) //$NON-NLS-1$
			{
			if (tree.getCellEditor().isCellEditable(null))
				tree.startEditingAtPath(tree.getLeadSelectionPath());
			return;
			}
		if (com.endsWith(".DELETE")) //$NON-NLS-1$
			{
			deleteSelectedResource(tree);
			return;
			}
		if (com.endsWith(".DEFRAGIDS")) //$NON-NLS-1$
			{
			String msg = Messages.getString("Listener.CONFIRM_DEFRAGIDS"); //$NON-NLS-1$
			if (JOptionPane.showConfirmDialog(LGM.frame,msg,
					Messages.getString("Listener.CONFIRM_DEFRAGIDS_TITLE"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION) == 0) LGM.currentFile.defragIds();
			}
		if (com.endsWith(".EXPAND")) //$NON-NLS-1$
			{
			for (int m = 0; m < tree.getRowCount(); m++)
				tree.expandRow(m);
			return;
			}
		if (com.endsWith(".COLLAPSE")) //$NON-NLS-1$
			{
			for (int m = tree.getRowCount() - 1; m >= 0; m--)
				tree.collapseRow(m);
			return;
			}
		if (com.endsWith(".ABOUT")) //$NON-NLS-1$
			{
			new AboutBox(LGM.frame).setVisible(true);
			return;
			}
		}

	public static ResNode getPrimaryParent(Resource.Kind kind)
		{
		for (int i = 0; i < LGM.root.getChildCount(); i++)
			if (((ResNode) LGM.root.getChildAt(i)).kind == kind) return (ResNode) LGM.root.getChildAt(i);
		return null;
		}

	protected Transferable createTransferable(JComponent c)
		{
		ResNode n = (ResNode) ((JTree) c).getLastSelectedPathComponent();

		if (n.status == ResNode.STATUS_PRIMARY) return null;
		switch (n.kind)
			{
			case GAMEINFO:
			case GAMESETTINGS:
			case EXTENSIONS:
				return null;
			}
		return n;
		}

	public int getSourceActions(JComponent c)
		{
		return MOVE;
		}

	public boolean canImport(TransferHandler.TransferSupport support)
		{
		if (!support.isDataFlavorSupported(ResNode.NODE_FLAVOR)) return false;
		// the above method uses equals(), which does not work as expected
		for (DataFlavor f : support.getDataFlavors())
			if (f != ResNode.NODE_FLAVOR) return false;
		TreePath drop = ((JTree.DropLocation) support.getDropLocation()).getPath();
		if (drop == null) return false;
		ResNode dropNode = (ResNode) drop.getLastPathComponent();
		ResNode dragNode = (ResNode) ((JTree) support.getComponent()).getLastSelectedPathComponent();
		if (dragNode == dropNode) return false;
		if (dragNode.isNodeDescendant(dropNode)) return false;
		if (Prefs.groupKind && dropNode.kind != dragNode.kind) return false;
		if (dropNode.status == ResNode.STATUS_SECONDARY) return false;
		return true;
		}

	public boolean importData(TransferHandler.TransferSupport support)
		{
		if (!canImport(support)) return false;
		JTree.DropLocation drop = (JTree.DropLocation) support.getDropLocation();
		int dropIndex = drop.getChildIndex();
		ResNode dropNode = (ResNode) drop.getPath().getLastPathComponent();
		ResNode dragNode = (ResNode) ((JTree) support.getComponent()).getLastSelectedPathComponent();
		if (dropIndex == -1)
			{
			dropIndex = dropNode.getChildCount();
			}
		if (dropNode == dragNode.getParent() && dropIndex > dragNode.getParent().getIndex(dragNode))
			dropIndex--;
		dropNode.insert(dragNode,dropIndex);
		LGM.tree.expandPath(new TreePath(dropNode.getPath()));
		LGM.tree.updateUI();
		return true;
		}

	public static class NodeMenuListener implements ActionListener
		{
		ResNode node;

		public NodeMenuListener(ResNode node)
			{
			this.node = node;
			}

		public void actionPerformed(ActionEvent e)
			{
			JTree tree = LGM.tree;
			String com = e.getActionCommand().substring(e.getActionCommand().lastIndexOf('_') + 1);
			if (com.equals("EDIT")) //$NON-NLS-1$
				{
				if (node.status == ResNode.STATUS_SECONDARY) node.openFrame();
				return;
				}
			if (com.equals("DELETE")) //$NON-NLS-1$
				{
				deleteSelectedResource(tree);
				return;
				}
			if (com.equals("RENAME")) //$NON-NLS-1$
				{
				if (tree.getCellEditor().isCellEditable(null))
					tree.startEditingAtPath(tree.getLeadSelectionPath());
				return;
				}
			if (com.equals("GROUP")) //$NON-NLS-1$
				{
				if (node.status == ResNode.STATUS_SECONDARY)
					insertResource(tree,"GROUP"); //$NON-NLS-1$
				else
					addResource(tree,"GROUP"); //$NON-NLS-1$
				return;
				}
			if (com.equals("INSERT")) //$NON-NLS-1$
				{
				insertResource(tree,node.kind);
				return;
				}
			if (com.equals("ADD")) //$NON-NLS-1$
				{
				addResource(tree,node.kind);
				return;
				}
			if (com.equals("DUPLICATE")) //$NON-NLS-1$
				{
				ResourceList<?> rl = LGM.currentFile.getList(node.kind);
				Resource<?,?> resource = null;
				try
					{
					if (node.frame != null) node.frame.commitChanges();
					//FIXME: dodgy workaround to avoid warnings
					resource = (Resource<?,?>) rl.getClass().getMethod("duplicate",Resource.class).invoke(//$NON-NLS-1$
							rl,node.getRes().get());
					}
				catch (Exception e1)
					{
					e1.printStackTrace();
					}
				Listener.addResource(tree,node.kind,resource);
				return;
				}
			}
		}

	private class MListener extends MouseAdapter
		{
		public MListener()
			{
			super();
			}

		public void mousePressed(MouseEvent e)
			{
			int selRow = LGM.tree.getRowForLocation(e.getX(),e.getY());
			TreePath selPath = LGM.tree.getPathForLocation(e.getX(),e.getY());
			if (selRow != -1 && e.getModifiers() == InputEvent.BUTTON3_MASK)
				LGM.tree.setSelectionPath(selPath);
			}

		public void mouseReleased(MouseEvent e)
			{
			ResNode node = (ResNode) LGM.tree.getLastSelectedPathComponent();
			if (e.getX() >= LGM.tree.getWidth() && e.getY() >= LGM.tree.getHeight() || node == null)
				return;
			if (e.getModifiers() == InputEvent.BUTTON3_MASK
			//Isn't Java supposed to handle ctrl+click for us? For some reason it doesn't.
					|| (e.getClickCount() == 1 && e.isControlDown()))
				{
				node.showMenu(e);
				return;
				}
			if (e.getClickCount() == 2)
				{
				// kind must be a Resource kind
				if (node.status != ResNode.STATUS_SECONDARY) return;
				node.openFrame();
				return;
				}
			}
		}

	public void editingCanceled(ChangeEvent e)
		{
		}

	public void editingStopped(ChangeEvent e)
		{
		ResNode node = (ResNode) LGM.tree.getLastSelectedPathComponent();
		if (node.status == ResNode.STATUS_SECONDARY)
			switch (node.kind)
				{
				case GAMEINFO:
				case GAMESETTINGS:
				case EXTENSIONS:
					break;
				default:
					String txt = ((String) node.getUserObject()).replaceAll("\\W","").replaceAll("^([0-9]+)",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					Resource<?,?> r = deRef((ResourceReference<?>) node.getRes());
					if (r != null) r.setName(txt);
				}
		}
	}
