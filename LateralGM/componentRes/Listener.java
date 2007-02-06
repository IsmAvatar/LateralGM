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
		if (com.equals("New"))
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
		if (com.equals("Open..."))
			{
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new CustomFileFilter(".gm6","Game Maker 6 Files"));
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
						JOptionPane.showMessageDialog(LGM.frame,"error occured in:\n" + ex.stackAsString()
								+ "\nmessage: " + ex.getMessage(),"Error Loading File",JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			return;
			}
		if (com.equals("Save"))
			{
			return; // make a .gb1 file for backup, in case this corrupts the file.
			}
		if (com.equals("Save As..."))
			{
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new CustomFileFilter(".gm6","Game Maker 6 Files"));
			while (true)
				{
				if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
				String filename = fc.getSelectedFile().getPath();
				if (!filename.endsWith(".gm6")) filename += ".gm6";
				int result = 0;
				if (new File(filename).exists())
					result = JOptionPane.showConfirmDialog(LGM.frame,filename
							+ " already exists. Do you wish to replace it?","Save File",
							JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
				if (result == 0)
					{
					LGM.currentFile.WriteGm6File(filename,LGM.root);
					return;
					}
				if (result == 2) return;
				}
			}
		if (com.equals("Exit"))
			{
			LGM.frame.dispose();
			return;
			}
		if (com.startsWith("Insert "))
			{
			ResNode node = (ResNode)tree.getLastSelectedPathComponent();
			if (node == null) return;
			ResNode parent = (ResNode)node.getParent();
			int pos = parent.getIndex(node);
			com = com.replaceAll("Insert ","");
			if (com.equals("Group"))
				{
				String name = JOptionPane.showInputDialog("Group Name?","group");
				if (name == "") return;
				ResNode g = new ResNode(name,parent.kind,ResNode.STATUS_GROUP);
				parent.insert(g,pos);
				tree.expandPath(new TreePath(parent.getPath()));
				tree.setSelectionPath(new TreePath(g.getPath()));
				tree.updateUI();
				return;
				}
			}
		if (com.startsWith("Add "))
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
			com = com.replaceAll("Add ","");
			if (com.equals("Group"))
				{
				String name = JOptionPane.showInputDialog("Group Name?","Group");
				if (name == "") return;
				ResNode g = new ResNode(name,parent.kind,ResNode.STATUS_GROUP);
				parent.insert(g,pos);
				tree.expandPath(new TreePath(parent.getPath()));
				tree.setSelectionPath(new TreePath(g.getPath()));
				tree.updateUI();
				return;
				}
			}
		if (com.equals("Rename"))
			{
			tree.startEditingAtPath(tree.getLeadSelectionPath());
			return;
			}
		if (com.equals("Delete"))
			{
			ResNode me = (ResNode) tree.getLastSelectedPathComponent();
			if (me == null) return;
			if (Prefs.protectRoot && me.status == ResNode.STATUS_PRIMARY) return;
			if (JOptionPane.showConfirmDialog(null,"Delete this resource?","Delete",JOptionPane.YES_NO_OPTION) == 0)
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
		if (com.equals("Expand"))
			{
			for (int m = 0; m < tree.getRowCount(); m++)
				tree.expandRow(m);
			return;
			}
		if (com.equals("Collapse"))
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
					if (node.kind == Resource.SCRIPT)
						{
						if (Prefs.protectLeaf && node.status != ResNode.STATUS_SECONDARY) return;
						ScriptFrame sf = new ScriptFrame();
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