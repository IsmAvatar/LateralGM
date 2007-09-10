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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;

import org.lateralgm.components.GmMenuBar;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.Gm6File;
import org.lateralgm.file.Gm6FileReader;
import org.lateralgm.file.Gm6FileWriter;
import org.lateralgm.file.Gm6FormatException;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;

public class Listener extends TransferHandler implements ActionListener,MouseListener,
		CellEditorListener
	{
	private static final long serialVersionUID = 1L;
	JFileChooser fc = new JFileChooser();

	public static byte stringToRes(String com)
		{
		if (com.equals("OBJECT")) //$NON-NLS-1$
			{
			return Resource.GMOBJECT;
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
			fc.setFileFilter(new CustomFileFilter(".gm6", //$NON-NLS-1$
					Messages.getString("Listener.FORMAT_GM6"))); //$NON-NLS-1$
			fc.showOpenDialog(LGM.frame);
			file = fc.getSelectedFile();
			if (file == null) return;
			filename = file.getPath();
			}
		if (!file.exists()) return;
		try
			{
			ResNode newroot = new ResNode("Root",0,0,null); //$NON-NLS-1$
			PrefsStore.addRecentFile(filename);
			LGM.frame.setTitle(String.format(Messages.getString("LGM.TITLE"),file.getName()));
			((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
			LGM.currentFile = Gm6FileReader.readGm6File(filename,newroot);
			LGM f = new LGM();
			f.createToolBar();
			f.createTree(newroot,false);
			LGM.frame.setJMenuBar(new GmMenuBar());
			tree.setSelectionRow(0);
			f.setOpaque(true);
			LGM.frame.setContentPane(f);
			f.updateUI();
			}
		catch (Gm6FormatException ex)
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
		LGM f = new LGM();
		f.createToolBar();
		f.createTree(true);
		LGM.frame.setJMenuBar(new GmMenuBar());
		LGM.frame.setTitle(String.format(
				Messages.getString("LGM.TITLE"),Messages.getString("LGM.NEWGAME"))); //$NON-NLS-1$ $NON-NLS-2$)
		f.setOpaque(true);
		LGM.frame.setContentPane(f);
		LGM.currentFile = new Gm6File();
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
		//TODO: Save (make a .gb1 file for backup, in case this corrupts the file)
		}

	public void saveNewFile()
		{
		fc.setFileFilter(new CustomFileFilter(".gm6", //$NON-NLS-1$
				Messages.getString("Listener.FORMAT_GM6"))); //$NON-NLS-1$
		while (true)
			{
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			String filename = fc.getSelectedFile().getPath();
			if (!filename.endsWith(".gm6")) filename += ".gm6"; //$NON-NLS-1$ //$NON-NLS-2$
			int result = 0;
			if (new File(filename).exists())
				result = JOptionPane.showConfirmDialog(LGM.frame,String.format(
						Messages.getString("Listener.CONFIRM_REPLACE"),filename), //$NON-NLS-1$
						Messages.getString("Listener.CONFIRM_REPLACE_TITLE"), //$NON-NLS-1$
						JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
			if (result == 0)
				{
				Enumeration<?> nodes = LGM.root.preorderEnumeration();
				while (nodes.hasMoreElements())
					{
					ResNode node = (ResNode) nodes.nextElement();
					if (node.frame != null) node.frame.updateResource(); // update open frames
					}
				LGM.gameSet.commitChanges();
				Gm6FileWriter.writeGm6File(LGM.currentFile,filename,LGM.root);
				return;
				}
			if (result == 2) return;
			}
		}

	private void addResource(JTree tree, String com)
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
		if (com.equals("GROUP")) //$NON-NLS-1$
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

		byte r = stringToRes(com);
		if (node.kind != r)
			{
			parent = getPrimaryParent(r);
			pos = parent.getChildCount();
			}

		Resource<?> res = LGM.currentFile.getList(parent.kind).add();
		ResNode g = new ResNode(res.getName(),ResNode.STATUS_SECONDARY,parent.kind,res);
		parent.insert(g,pos);
		tree.expandPath(new TreePath(parent.getPath()));
		tree.setSelectionPath(new TreePath(g.getPath()));
		tree.updateUI();
		g.openFrame();
		}

	//TODO: insertResource (works for group, but not other resources)
	private void insertResource(JTree tree, String com)
		{
		ResNode node = (ResNode) tree.getLastSelectedPathComponent();
		if (node == null) return;
		ResNode parent = (ResNode) node.getParent();
		int pos = parent.getIndex(node);
		if (com.equals("GROUP")) //$NON-NLS-1$
			{
			String msg = Messages.getString("Listener.INPUT_GROUPNAME"); //$NON-NLS-1$
			String name = JOptionPane.showInputDialog(msg,"group");
			if (name == "" || name == null) return; //$NON-NLS-1$
			ResNode g = new ResNode(name,parent.kind,ResNode.STATUS_GROUP);
			parent.insert(g,pos);
			tree.expandPath(new TreePath(parent.getPath()));
			tree.setSelectionPath(new TreePath(g.getPath()));
			tree.updateUI();
			}
		}

	private void deleteResource(JTree tree)
		{
		ResNode me = (ResNode) tree.getLastSelectedPathComponent();
		if (me == null) return;
		if (Prefs.protectRoot && me.status == ResNode.STATUS_PRIMARY) return;
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
			LGM.currentFile.getList(me.kind).remove(me.res);
			tree.updateUI();
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		JTree tree = LGM.tree;
		String[] args = e.getActionCommand().split(" ");
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
			JOptionPane.showMessageDialog(null,Messages.getString("Listener.ABOUT_MESSAGE"),
					Messages.getString("Listener.ABOUT_TITLE"),JOptionPane.INFORMATION_MESSAGE);
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

		if (Prefs.protectRoot) if (n.status == 1 || n.kind == 10 || n.kind == 11) return null;
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
					//TODO: Tree Menu: Insert Resource, Add Resource, Copy Resource
					if (com.equals("INSERT")) //$NON-NLS-1$
						{
						//insertResource(tree,"");
						return;
						}
					if (com.equals("ADD")) //$NON-NLS-1$
						{
						//addResource(tree,"");
						return;
						}
					if (com.equals("COPY")) //$NON-NLS-1$
						{
						return;
						}
					}
			};
		if (node.kind == Resource.GAMESETTINGS || node.kind == Resource.GAMEINFO
				|| node.kind == Resource.EXTENSIONS)
			{
			popup.add(makeMenuItem("Listener.TREE_EDIT",al));
			popup.show(e.getComponent(),e.getX(),e.getY());
			return;
			}
		if (node.status == ResNode.STATUS_SECONDARY)
			{
			popup.add(makeMenuItem("Listener.TREE_EDIT",al));
			popup.addSeparator();
			popup.add(makeMenuItem("Listener.TREE_INSERT",al));
			popup.add(makeMenuItem("Listener.TREE_COPY",al));
			}
		else
			popup.add(makeMenuItem("Listener.TREE_ADD",al));
		popup.addSeparator();
		popup.add(makeMenuItem("Listener.TREE_GROUP",al));
		if (node.status != ResNode.STATUS_SECONDARY) popup.add(makeMenuItem("Listener.TREE_SORT",al));
		if (node.status != ResNode.STATUS_PRIMARY)
			{
			popup.addSeparator();
			popup.add(makeMenuItem("Listener.TREE_DELETE",al));
			popup.add(makeMenuItem("Listener.TREE_RENAME",al));
			}
		popup.show(e.getComponent(),e.getX(),e.getY());
		}

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

	// Unused
	public void mouseReleased(MouseEvent e)
		{
		}

	public void mouseClicked(MouseEvent e)
		{
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void editingCanceled(ChangeEvent e)
		{
		}

	public void editingStopped(ChangeEvent e)
		{
		ResNode node = (ResNode) LGM.tree.getLastSelectedPathComponent();
		if (node.status == ResNode.STATUS_SECONDARY && node.kind != Resource.GAMEINFO
				&& node.kind != Resource.GAMESETTINGS)
			{
			String txt = ((String) node.getUserObject()).replaceAll("\\W","").replaceAll("^([0-9]+)","");
			node.setUserObject(txt);
			node.updateFrame();
			}
		}
	}
