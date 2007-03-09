package componentRes;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import mainRes.LGM;
import mainRes.Prefs;
import resourcesRes.Resource;
import SubFrames.GameInformationFrame;
import SubFrames.GameSettingFrame;
import SubFrames.ScriptFrame;
import fileRes.Gm6File;
import fileRes.Gm6FormatException;

public class Listener extends TransferHandler implements ActionListener,MouseListener
	{
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e)
		{
		JTree tree = LGM.tree;
		String com = e.getActionCommand();
		if (com.endsWith(".NEW")) //$NON-NLS-1$
			{
			LGM f = new LGM();
			f.createTree(true);
			f.createToolBar();
			f.setOpaque(true);
			LGM.frame.setContentPane(f);
			LGM.currentFile = new Gm6File();
			f.updateUI();
			return;
			}
		if (com.endsWith(".OPEN")) //$NON-NLS-1$
			{
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new CustomFileFilter(".gm6",Messages.getString("Listener.FORMAT_GM6"))); //$NON-NLS-2$
			fc.showOpenDialog(LGM.frame);

			if (fc.getSelectedFile() != null)
				{
				if (fc.getSelectedFile().exists())
					{
					try
						{
						ResNode newroot = new ResNode("Root",0,0,null);
						LGM.currentFile = new Gm6File();
						LGM.currentFile.ReadGm6File(fc.getSelectedFile().getPath(),newroot);
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
						JOptionPane.showMessageDialog(LGM.frame,String.format(Messages.getString("Listener.ERROR_MESSAGE"), ex.stackAsString(), ex.getMessage()), Messages.getString("Listener.ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
						}
					}
				}
			LGM.gameInfo.setVisible(false);
			LGM.gameInfo = new GameInformationFrame();
			LGM.MDI.add(LGM.gameInfo);
			LGM.gameSet.setVisible(false);
			LGM.gameSet = new GameSettingFrame();
			LGM.MDI.add(LGM.gameSet);
			return;
			}
		if (com.endsWith(".SAVE")) //$NON-NLS-1$
			{
			return; // make a .gb1 file for backup, in case this corrupts the file.
			}
		if (com.endsWith(".SAVEAS")) //$NON-NLS-1$
			{
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new CustomFileFilter(".gm6",Messages.getString("Listener.FORMAT_GM6"))); //$NON-NLS-2$
			while (true)
				{
				if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
				String filename = fc.getSelectedFile().getPath();
				if (!filename.endsWith(".gm6")) filename += ".gm6";
				int result = 0;
				if (new File(filename).exists())
					result = JOptionPane.showConfirmDialog(LGM.frame,
							String.format(Messages.getString("Listener.CONFIRM_REPLACE"), filename), //$NON-NLS-1$
							Messages.getString("Listener.CONFIRM_REPLACE_TITLE"), //$NON-NLS-1$
							JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
				if (result == 0)
					{
					LGM.currentFile.WriteGm6File(filename,LGM.root);
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
			ResNode node = (ResNode)tree.getLastSelectedPathComponent();
			if (node == null) return;
			ResNode parent = (ResNode)node.getParent();
			int pos = parent.getIndex(node);
			com = com.substring(com.lastIndexOf('_') + 1);
			if (com.equals("GROUP")) //$NON-NLS-1$
				{
				String name = JOptionPane.showInputDialog(Messages.getString("Listener.INPUT_GROUPNAME"),"group"); //$NON-NLS-1$
				if (name == "") return;
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
				parent = (ResNode)node;
				pos = parent.getChildCount();
				}
			else
				{
				parent = (ResNode)node.getParent();
				pos = parent.getIndex(node) + 1;
				}
			com = com.substring(com.lastIndexOf('_') + 1);
			if (com.equals("GROUP")) //$NON-NLS-1$
				{
				String name = JOptionPane.showInputDialog(Messages.getString("Listener.INPUT_GROUPNAME"),"Group"); //$NON-NLS-1$
				if (name == "") return;
				ResNode g = new ResNode(name,parent.kind,ResNode.STATUS_GROUP);
				parent.insert(g,pos);
				tree.expandPath(new TreePath(parent.getPath()));
				tree.setSelectionPath(new TreePath(g.getPath()));
				tree.updateUI();
				return;
				}
			}
		if (com.endsWith(".RENAME")) //$NON-NLS-1$
			{
			tree.startEditingAtPath(tree.getLeadSelectionPath());
			return;
			}
		if (com.endsWith(".DELETE")) //$NON-NLS-1$
			{
			ResNode me = (ResNode) tree.getLastSelectedPathComponent();
			if (me == null) return;
			if (Prefs.protectRoot && me.status == ResNode.STATUS_PRIMARY) return;
			if (JOptionPane.showConfirmDialog(null,Messages.getString("Listener.CONFIRM_DELETERESOURCE"),Messages.getString("Listener.CONFIRM_DELETERESOURCE_TITLE"),JOptionPane.YES_NO_OPTION) == 0) //$NON-NLS-1$ //$NON-NLS-2$
				{
				ResNode next = (ResNode) me.getNextSibling();
				if (next == null) next = (ResNode) me.getParent();
				if (next.isRoot()) next = (ResNode) next.getFirstChild();
				tree.setSelectionPath(new TreePath(next.getPath()));
				me.removeFromParent();
				tree.updateUI();
				}
			return;
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
		if (dropNode == dragNode.getParent() && dropIndex > dragNode.getParent().getIndex(dragNode)) dropIndex--;
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
//				ResNode node = (ResNode)selPath.getLastPathComponent();
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
						//JInternalFrame gameinfo = new GameInformationFrame();
						LGM.gameInfo.setVisible(true);
						return;
//						LGM.MDI.add(LGM.gameInfo);
						}

					if (node.kind == Resource.GAMESETTINGS)
						{
						LGM.gameSet.setVisible(true);
						}

					if (node.kind == Resource.SCRIPT)
						{
						if (node.status == ResNode.STATUS_PRIMARY) return;
						if (Prefs.protectLeaf && node.status != ResNode.STATUS_SECONDARY) return;
						ScriptFrame sf = new ScriptFrame(node.resourceId);
//						sf.setScript();
						LGM.MDI.add(sf);
						sf.setVisible(true);
						return;
						}

					}
				}
			}
		}

	//Unused
	public void mouseReleased(MouseEvent arg0) { }
	public void mouseClicked(MouseEvent arg0) { }
	public void mouseEntered(MouseEvent arg0) { }
	public void mouseExited(MouseEvent arg0) { }
	}