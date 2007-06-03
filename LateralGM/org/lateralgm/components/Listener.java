/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * 
 * Lateral GM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Lateral GM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lateral GM; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.lateralgm.components;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import org.lateralgm.file.Gm6File;
import org.lateralgm.file.Gm6FileReader;
import org.lateralgm.file.Gm6FileWriter;
import org.lateralgm.file.Gm6FormatException;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;

public class Listener extends TransferHandler implements ActionListener,MouseListener,
		CellEditorListener
	{
	private static final long serialVersionUID = 1L;
	JFileChooser fc = new JFileChooser();

	public void actionPerformed(ActionEvent e)
		{
		JTree tree = LGM.tree;
		String[] args = e.getActionCommand().split(" ");
		String com = args[0];
		if (com.endsWith(".NEW")) //$NON-NLS-1$
			{
			LGM f = new LGM();
			f.createTree(true);
			f.createToolBar();
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
			return;
			}
		if (com.endsWith(".OPEN")) //$NON-NLS-1$
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
				LGM.frame.setTitle("Lateral GM 6.1: " + file.getName());
				((GmMenuBar) LGM.frame.getJMenuBar()).updateRecentFiles();
				LGM.currentFile = Gm6FileReader.readGm6File(filename,newroot);
				LGM f = new LGM();
				f.createTree(newroot,false);
				tree.setSelectionPath(new TreePath(LGM.root).pathByAddingChild(LGM.root.getChildAt(0)));
				f.createToolBar();
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
			return;
			}
		if (com.endsWith(".SAVE")) //$NON-NLS-1$
			{
			return; // make a .gb1 file for backup, in case this corrupts the file.
			}
		if (com.endsWith(".SAVEAS")) //$NON-NLS-1$
			{
			JFileChooser fc = new JFileChooser();
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
					Gm6FileWriter.writeGm6File(LGM.currentFile,filename,LGM.root);
					return;
					}
				if (result == 2) return;
				}
			}
		if (com.endsWith(".EXIT")) //$NON-NLS-1$
			{
			LGM.frame.dispose();
			return;
			}
		if (com.contains(".INSERT_")) //$NON-NLS-1$
			{
			ResNode node = (ResNode) tree.getLastSelectedPathComponent();
			if (node == null) return;
			ResNode parent = (ResNode) node.getParent();
			int pos = parent.getIndex(node);
			com = com.substring(com.lastIndexOf('_') + 1);
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
				return;
				}
			}
		if (com.contains(".ADD_")) //$NON-NLS-1$
			{
			ResNode node = (ResNode) tree.getLastSelectedPathComponent();
			if (node == null) return;
			ResNode parent;
			int pos;
			if (node.getAllowsChildren())
				{
				parent = (ResNode) node;
				pos = parent.getChildCount();
				}
			else
				{
				parent = (ResNode) node.getParent();
				pos = parent.getIndex(node) + 1;
				}
			com = com.substring(com.lastIndexOf('_') + 1);
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
			if (com.equals("SPRITE")) //$NON-NLS-1$
				{
				if (node.kind != Resource.SPRITE)
					{
					parent = getPrimaryParent(Resource.SPRITE);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("SOUND")) //$NON-NLS-1$
				{
				if (node.kind != Resource.SOUND)
					{
					parent = getPrimaryParent(Resource.SOUND);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("BACKGROUND")) //$NON-NLS-1$
				{
				if (node.kind != Resource.BACKGROUND)
					{
					parent = getPrimaryParent(Resource.BACKGROUND);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("PATH")) //$NON-NLS-1$
				{
				if (node.kind != Resource.PATH)
					{
					parent = getPrimaryParent(Resource.PATH);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("SCRIPT")) //$NON-NLS-1$
				{
				if (node.kind != Resource.SCRIPT)
					{
					parent = getPrimaryParent(Resource.SCRIPT);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("FONT")) //$NON-NLS-1$
				{
				if (node.kind != Resource.FONT)
					{
					parent = getPrimaryParent(Resource.FONT);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("TIMELINE")) //$NON-NLS-1$
				{
				if (node.kind != Resource.TIMELINE)
					{
					parent = getPrimaryParent(Resource.TIMELINE);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("OBJECT")) //$NON-NLS-1$
				{
				if (node.kind != Resource.GMOBJECT)
					{
					parent = getPrimaryParent(Resource.GMOBJECT);
					pos = parent.getChildCount();
					}
				}
			if (com.equals("ROOM")) //$NON-NLS-1$
				{
				if (node.kind != Resource.ROOM)
					{
					parent = getPrimaryParent(Resource.ROOM);
					pos = parent.getChildCount();
					}
				}
			Resource res = LGM.currentFile.getList(parent.kind).add();
			ResNode g = new ResNode(res.getName(),ResNode.STATUS_SECONDARY,parent.kind,res.getId());
			parent.insert(g,pos);
			tree.expandPath(new TreePath(parent.getPath()));
			tree.setSelectionPath(new TreePath(g.getPath()));
			tree.updateUI();
			g.openFrame();
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
				LGM.currentFile.getList(me.kind).remove(me.resourceId);
				tree.updateUI();
				}
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

	private ResNode getPrimaryParent(int kind)
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
		TreePath drop = ((JTree.DropLocation) support.getDropLocation()).getPath();
		if (drop == null) return false;
		ResNode dropNode = (ResNode) drop.getLastPathComponent();
		ResNode dragNode = (ResNode) ((JTree) support.getComponent()).getLastSelectedPathComponent();
		if (dragNode == dropNode) return false;
		if (dragNode.isNodeDescendant(dropNode)) return false;
		if (Prefs.groupKind && dropNode.kind != dragNode.kind) return false;
		if (Prefs.protectLeaf && dropNode.status == ResNode.STATUS_SECONDARY) return false;
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

	public void mousePressed(MouseEvent e)
		{
		int selRow = LGM.tree.getRowForLocation(e.getX(),e.getY());
		TreePath selPath = LGM.tree.getPathForLocation(e.getX(),e.getY());
		if (selRow != -1)
			{
			if (e.getModifiers() == InputEvent.BUTTON3_MASK)
				{
				// ResNode node = (ResNode)selPath.getLastPathComponent();
				LGM.tree.setSelectionPath(selPath);
				JPopupMenu popup = new JPopupMenu();
				JMenuItem menuItem = new JMenuItem("A popup menu item");
				menuItem.addActionListener(this);
				popup.add(menuItem);
				menuItem = new JMenuItem("Another popup menu item");
				menuItem.addActionListener(this);
				popup.add(menuItem);
				popup.show(e.getComponent(),e.getX(),e.getY());
				}
			else
				{
				if (e.getClickCount() == 1)
					{
					// unused for now
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
					// kind must be a Resource kind
					if (node.status == ResNode.STATUS_PRIMARY) return;
					if (Prefs.protectLeaf && node.status != ResNode.STATUS_SECONDARY) return;
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
