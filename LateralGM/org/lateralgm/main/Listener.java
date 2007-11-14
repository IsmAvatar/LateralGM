/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.GmMenuBar;
import org.lateralgm.components.CustomFileChooser.FilterSet;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.GmFileReader;
import org.lateralgm.file.GmFileWriter;
import org.lateralgm.file.GmFormatException;
import org.lateralgm.file.ResourceList;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;

public class Listener extends TransferHandler implements ActionListener,CellEditorListener
	{
	private static final long serialVersionUID = 1L;
	MListener mListener = new MListener();
	private CustomFileChooser fc;
	private FilterSet openFs = new FilterSet();
	private FilterSet saveFs = new FilterSet();

	public Listener()
		{
		String exts[] = { ".gmk",".gm6",".gmd" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		fc = new CustomFileChooser("/org/lateralgm","LAST_FILE_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		openFs.addFilter("Listener.FORMAT_GM",exts); //$NON-NLS-1$
		openFs.addFilter("Listener.FORMAT_GMK",exts[0]); //$NON-NLS-1$
		openFs.addFilter("Listener.FORMAT_GM6",exts[1]); //$NON-NLS-1$
		openFs.addFilter("Listener.FORMAT_GMD",exts[2]); //$NON-NLS-1$

		saveFs.addFilter("Listener.FORMAT_GM6",exts[1]); //$NON-NLS-1$
		}

	public static byte stringToRes(String com)
		{
		if (com.equals("OBJECT")) //$NON-NLS-1$
			{
			return Resource.GMOBJECT;
			}
		if (com.equals("GROUP")) //$NON-NLS-1$
			{
			return -1;
			}
		try
			{
			return Resource.class.getDeclaredField(com).getByte(null);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		return -1;
		}

	private void openFile(JTree tree, String[] args)
		{
		File file;
		String filename;
		if (args.length > 1)
			{
			filename = Util.urlDecode(args[1]);
			file = new File(filename);
			}
		else
			{
			fc.setFilterSet(openFs);
			fc.showOpenDialog(LGM.frame);
			file = fc.getSelectedFile();
			if (file == null) return;
			filename = file.getPath();
			}
		if (!file.exists()) return;
		try
			{
			ResNode newroot = new ResNode("Root",(byte) 0,(byte) 0,null); //$NON-NLS-1$
			PrefsStore.addRecentFile(filename);
			LGM.frame.setTitle(String.format(Messages.getString("LGM.TITLE"),file.getName())); //$NON-NLS-1$
			((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
			LGM.currentFile = GmFileReader.readGmFile(filename,newroot);
			JPanel f = new JPanel(new BorderLayout());
			LGM.createToolBar(f);
			LGM.createTree(f,newroot,false);
			LGM.frame.setJMenuBar(new GmMenuBar());
			tree.setSelectionRow(0);
			f.setOpaque(true);
			LGM.frame.setContentPane(f);
			f.updateUI();
			}
		catch (GmFormatException ex)
			{
			JOptionPane.showMessageDialog(LGM.frame,String.format(
					Messages.getString("Listener.ERROR_MESSAGE"), //$NON-NLS-1$
					ex.stackAsString(),ex.getMessage()),
					Messages.getString("Listener.ERROR_TITLE"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			}
		LGM.gameInfo.dispose();
		LGM.gameInfo = new GameInformationFrame();
		LGM.mdi.add(LGM.gameInfo);
		LGM.gameSet.dispose();
		LGM.gameSet = new GameSettingFrame();
		LGM.mdi.add(LGM.gameSet);
		}

	public void newFile()
		{
		JPanel f = new JPanel(new BorderLayout());
		LGM.createToolBar(f);
		LGM.createTree(f,true);
		LGM.frame.setJMenuBar(new GmMenuBar());
		LGM.frame.setTitle(String.format(
				Messages.getString("LGM.TITLE"),Messages.getString("LGM.NEWGAME"))); //$NON-NLS-1$ //$NON-NLS-2$
		f.setOpaque(true);
		LGM.frame.setContentPane(f);
		LGM.currentFile = new GmFile();
		LGM.gameSet.dispose();
		LGM.gameSet = new GameSettingFrame();
		LGM.mdi.add(LGM.gameSet);
		LGM.gameInfo.dispose();
		LGM.gameInfo = new GameInformationFrame();
		LGM.mdi.add(LGM.gameInfo);
		f.updateUI();
		}

	public void saveFile()
		{
		if (LGM.currentFile.filename == null)
			{
			saveNewFile();
			return;
			}
		Enumeration<?> nodes = LGM.root.preorderEnumeration();
		while (nodes.hasMoreElements())
			{
			ResNode node = (ResNode) nodes.nextElement();
			if (node.frame != null) node.frame.updateResource(); // update open frames
			}
		LGM.gameSet.commitChanges();

		String fn = LGM.currentFile.filename;
		int p = fn.lastIndexOf("."); //$NON-NLS-1$
		if (p != -1) fn = fn.substring(0,p + 1);
		fn += ".gb1"; //$NON-NLS-1$

		//Copy file
		FileChannel inChannel = null, outChannel = null;
		try
			{
			inChannel = new FileInputStream(LGM.currentFile.filename).getChannel();
			outChannel = new FileOutputStream(new File(fn)).getChannel();
			inChannel.transferTo(0,inChannel.size(),outChannel);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		try
			{
			if (outChannel != null) outChannel.close();
			if (inChannel != null) inChannel.close();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		GmFileWriter.writeGmFile(LGM.currentFile,LGM.root);
		return;
		}

	public void saveNewFile()
		{
		fc.setFilterSet(saveFs);
		while (true) //repeatedly display dialog until a valid response is given
			{
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			String filename = fc.getSelectedFile().getPath();
			if (!filename.endsWith(".gm6")) filename += ".gm6"; //$NON-NLS-1$ //$NON-NLS-2$
			int result = JOptionPane.YES_OPTION;
			if (new File(filename).exists())
				result = JOptionPane.showConfirmDialog(LGM.frame,String.format(
						Messages.getString("Listener.CONFIRM_REPLACE"),filename), //$NON-NLS-1$
						Messages.getString("Listener.CONFIRM_REPLACE_TITLE"), //$NON-NLS-1$
						JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION)
				{
				Enumeration<?> nodes = LGM.root.preorderEnumeration();
				while (nodes.hasMoreElements())
					{
					ResNode node = (ResNode) nodes.nextElement();
					if (node.frame != null) node.frame.updateResource(); // update open frames
					}
				LGM.gameSet.commitChanges();
				LGM.currentFile.filename = filename;
				LGM.frame.setTitle(String.format(
						Messages.getString("LGM.TITLE"),new File(filename).getName())); //$NON-NLS-1$
				GmFileWriter.writeGmFile(LGM.currentFile,LGM.root);
				return;
				}
			if (result == JOptionPane.CANCEL_OPTION) return;
			}
		}

	private static void addResource(JTree tree, String com)
		{
		addResource(tree,stringToRes(com),null);
		}

	private static void addResource(JTree tree, byte r)
		{
		addResource(tree,r,null);
		}

	private static void addResource(JTree tree, byte r, Resource<?> res)
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

	private static void insertResource(JTree tree, String com)
		{
		insertResource(tree,stringToRes(com),null);
		}

	private static void insertResource(JTree tree, byte r)
		{
		insertResource(tree,r,null);
		}

	private static void insertResource(JTree tree, byte r, Resource<?> res)
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

	public static void putNode(JTree tree, ResNode node, ResNode parent, byte r, int pos,
			Resource<?> res)
		{
		if (r == -1)
			{
			String msg = Messages.getString("Listener.INPUT_GROUPNAME"); //$NON-NLS-1$
			String name = JOptionPane.showInputDialog(msg,"Group");
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

		Resource<?> resource = res == null ? LGM.currentFile.getList(parent.kind).add() : res;
		ResNode g = new ResNode(resource.getName(),ResNode.STATUS_SECONDARY,parent.kind,resource);
		parent.insert(g,pos);
		tree.expandPath(new TreePath(parent.getPath()));
		tree.setSelectionPath(new TreePath(g.getPath()));
		tree.updateUI();
		g.openFrame();
		}

	private static void deleteResource(JTree tree)
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
			if (me.frame != null) me.frame.dispose();
			me.removeFromParent();
			LGM.currentFile.getList(me.kind).remove(me.getRes());
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
			openFile(tree,args);
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
		if (com.endsWith(".TOGGLE_EVENT")) //$NON-NLS-1$
			{
			LGM.eventSelect.setVisible(LGM.eventSelect.toggle.isSelected());
			if (LGM.eventSelect.toggle.isSelected())
				{
				try
					{
					LGM.eventSelect.setIcon(false);
					LGM.eventSelect.setSelected(true);
					}
				catch (PropertyVetoException e1)
					{
					e1.printStackTrace();
					}
				}
			return;
			}
		if (com.endsWith(".EXIT")) //$NON-NLS-1$
			{
			LGM.frame.dispose();
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
			deleteResource(tree);
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
			JOptionPane.showMessageDialog(null,Messages.getString("Listener.ABOUT_MESSAGE"), //$NON-NLS-1$
					Messages.getString("Listener.ABOUT_TITLE"),JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
			}
		}

	public static ResNode getPrimaryParent(int kind)
		{
		for (int i = 0; i < LGM.root.getChildCount(); i++)
			if (((ResNode) LGM.root.getChildAt(i)).kind == kind) return (ResNode) LGM.root.getChildAt(i);
		return null;
		}

	protected Transferable createTransferable(JComponent c)
		{
		ResNode n = (ResNode) ((JTree) c).getLastSelectedPathComponent();

		if (n.status == ResNode.STATUS_PRIMARY || n.kind == Resource.GAMEINFO
				|| n.kind == Resource.GAMESETTINGS || n.kind == Resource.EXTENSIONS) return null;
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

	private JMenuItem makeMenuItem(String command, ActionListener al)
		{
		JMenuItem menuItem = new JMenuItem(Messages.getString(command));
		menuItem.setActionCommand(command);
		menuItem.addActionListener(al);
		return menuItem;
		}

	private void showNodeMenu(MouseEvent e)
		{
		ResNode node = (ResNode) LGM.tree.getPathForLocation(e.getX(),e.getY()).getLastPathComponent();
		JPopupMenu popup = new JPopupMenu();
		ActionListener al = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					JTree tree = LGM.tree;
					String com = e.getActionCommand().substring(e.getActionCommand().lastIndexOf('_') + 1);
					ResNode node = (ResNode) tree.getLastSelectedPathComponent();
					if (node == null) return;
					if (com.equals("EDIT")) //$NON-NLS-1$
						{
						if (node.kind == Resource.GAMEINFO)
							{
							LGM.gameInfo.setVisible(true);
							return;
							}
						if (node.kind == Resource.GAMESETTINGS)
							{
							LGM.gameSet.setVisible(true);
							return;
							}
						if (node.kind == Resource.EXTENSIONS)
							{
							return;
							}
						// kind must be a Resource kind
						if (node.status != ResNode.STATUS_SECONDARY) return;
						node.openFrame();
						return;
						}
					if (com.equals("DELETE")) //$NON-NLS-1$
						{
						deleteResource(tree);
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
					if (com.equals("COPY")) //$NON-NLS-1$
						{
						ResourceList<?> rl = LGM.currentFile.getList(node.kind);
						Resource<?> resource = null;
						try
							{
							if (node.frame != null) node.frame.commitChanges();
							// dodgy workaround to avoid warnings
							resource = (Resource<?>) rl.getClass().getMethod("duplicate",Resource.class).invoke(//$NON-NLS-1$
									rl,node.getRes());
							}
						catch (Exception e1)
							{
							e1.printStackTrace();
							}
						addResource(tree,node.kind,resource);
						return;
						}
					}
			};
		if (node.kind == Resource.GAMESETTINGS || node.kind == Resource.GAMEINFO
				|| node.kind == Resource.EXTENSIONS)
			{
			popup.add(makeMenuItem("Listener.TREE_EDIT",al)); //$NON-NLS-1$
			popup.show(e.getComponent(),e.getX(),e.getY());
			return;
			}
		if (node.status == ResNode.STATUS_SECONDARY)
			{
			popup.add(makeMenuItem("Listener.TREE_EDIT",al)); //$NON-NLS-1$
			popup.addSeparator();
			popup.add(makeMenuItem("Listener.TREE_INSERT",al)); //$NON-NLS-1$
			popup.add(makeMenuItem("Listener.TREE_COPY",al)); //$NON-NLS-1$
			}
		else
			popup.add(makeMenuItem("Listener.TREE_ADD",al)); //$NON-NLS-1$
		popup.addSeparator();
		popup.add(makeMenuItem("Listener.TREE_GROUP",al)); //$NON-NLS-1$
		if (node.status != ResNode.STATUS_SECONDARY) popup.add(makeMenuItem("Listener.TREE_SORT",al)); //$NON-NLS-1$
		if (node.status != ResNode.STATUS_PRIMARY)
			{
			popup.addSeparator();
			popup.add(makeMenuItem("Listener.TREE_DELETE",al)); //$NON-NLS-1$
			popup.add(makeMenuItem("Listener.TREE_RENAME",al)); //$NON-NLS-1$
			}
		popup.show(e.getComponent(),e.getX(),e.getY());
		}

	private class MListener extends MouseAdapter
		{
		public void mousePressed(MouseEvent e)
			{
			int selRow = LGM.tree.getRowForLocation(e.getX(),e.getY());
			TreePath selPath = LGM.tree.getPathForLocation(e.getX(),e.getY());
			if (selRow != -1)
				{
				if (e.getModifiers() == InputEvent.BUTTON3_MASK)
					{
					LGM.tree.setSelectionPath(selPath);
					showNodeMenu(e);
					}
				else
					{
					if (e.getClickCount() == 1)
						{
						//Isn't Java supposed to handle this for us? For some reason it doesn't.
						if (e.isControlDown())
							{
							LGM.tree.setSelectionPath(selPath);
							showNodeMenu(e);
							}
						return;
						}
					else if (e.getClickCount() == 2)
						{
						ResNode node = (ResNode) selPath.getLastPathComponent();
						if (node.kind == Resource.GAMEINFO)
							{
							LGM.gameInfo.setVisible(true);
							return;
							}
						if (node.kind == Resource.GAMESETTINGS)
							{
							LGM.gameSet.setVisible(true);
							return;
							}
						if (node.kind == Resource.EXTENSIONS)
							{
							return;
							}
						// kind must be a Resource kind
						if (node.status != ResNode.STATUS_SECONDARY) return;
						node.openFrame();
						return;
						}
					}
				}
			}
		}

	public void editingCanceled(ChangeEvent e)
		{
		}

	public void editingStopped(ChangeEvent e)
		{
		ResNode node = (ResNode) LGM.tree.getLastSelectedPathComponent();
		if (node.status == ResNode.STATUS_SECONDARY && node.kind != Resource.GAMEINFO
				&& node.kind != Resource.GAMESETTINGS && node.kind != Resource.EXTENSIONS)
			{
			String txt = ((String) node.getUserObject()).replaceAll("\\W","").replaceAll("^([0-9]+)",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			node.setUserObject(txt);
			node.updateFrame();
			}
		}
	}
