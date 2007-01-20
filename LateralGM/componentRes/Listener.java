package componentRes;
//test

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mainRes.LGM;
import fileRes.Gm6File;
import fileRes.Gm6FormatException;

public class Listener extends TransferHandler implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e)
		{
		JTree tree = LGM.tree;
		String com = e.getActionCommand();
		if (com == "Exit") LGM.frame.dispose();
		if (com == "Expand") for (int m = 0; m < tree.getRowCount(); m++)
			tree.expandRow(m);
		if (com == "Collapse") for (int m = tree.getRowCount() - 1; m >= 0; m--)
			tree.collapseRow(m);
		if (com == "Rename") tree.startEditingAtPath(tree.getLeadSelectionPath());
		if (com == "Delete")
			{
			if (JOptionPane.showConfirmDialog(null,"Delete this resource?","Delete",JOptionPane.YES_NO_OPTION) == 0)
				{
				DefaultMutableTreeNode me = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				DefaultMutableTreeNode next = me.getNextSibling();
				if (next == null) next = (DefaultMutableTreeNode) me.getParent();
				if (next.isRoot()) next = (DefaultMutableTreeNode) next.getFirstChild();
				tree.setSelectionPath(new TreePath(next.getPath()));
				me.removeFromParent();
				tree.updateUI();
				}
			}

		if (com == "Group")
			{
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			ResNode b = (ResNode) n.getParent();
			int pos = b.getIndex(n);
			String name = JOptionPane.showInputDialog("Group Name?","Group");
			if (name == "") return;
			ResNode g = new ResNode(name,b.kind,ResNode.STATUS_GROUP);
			b.insert(g,pos);
			tree.expandPath(new TreePath(b.getPath()));
			TreePath path = new TreePath(g.getPath());
			tree.expandPath(path);
			tree.collapsePath(path);
			tree.setSelectionPath(path);
			tree.updateUI();
			}
		if (com == "Add Group")
			{
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			ResNode b;
			int pos;
			if (n.getAllowsChildren())
				{
				b = (ResNode) n;
				pos = b.getChildCount();
				}
			else
				{
				b = (ResNode) n.getParent();
				pos = b.getIndex(n) + 1;
				}
			String name = JOptionPane.showInputDialog("Group Name?","Group");
			if (name == "") return;
			ResNode g = new ResNode(name,b.kind,ResNode.STATUS_GROUP);
			b.insert(g,pos);
			tree.expandPath(new TreePath(b.getPath()));
			TreePath path = new TreePath(g.getPath());
			tree.expandPath(path);
			tree.collapsePath(path);
			tree.setSelectionPath(path);
			tree.updateUI();
			}
		if (com == "Open...")
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
						Gm6File newfile = new Gm6File();
						newfile.LoadGm6File(fc.getSelectedFile().getPath(),newroot);
						LGM.currentFile = newfile;
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
			}
		if (com == "Save As...")
			{
			if (true) return;
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new CustomFileFilter(".gm6","Game Maker 6 Files"));
			boolean done = false;
			while (!done)
				{
				fc.showSaveDialog(LGM.frame);
				if (fc.getSelectedFile() != null)
					{
					String alteredstr = fc.getSelectedFile().getPath();
					if (!alteredstr.endsWith(".gm6")) alteredstr += ".gm6";
					File altered = new File(alteredstr);
					if (!altered.exists())
						{
						LGM.currentFile.WriteGm6File(altered.getPath(),LGM.root);
						done = true;
						}
					else
						{
						int result = JOptionPane.showConfirmDialog(LGM.frame,altered.getPath()
								+ " already exists. Do you wish to replace it?","Save File",JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE);
						if (result == 0)
							{
							LGM.currentFile.WriteGm6File(altered.getPath(),LGM.root);
							done = true;
							}
						else if (result == 2) done = true;
						}
					}
				}
			}
		if (com == "Save")
			{
			return; // make a .gb1 file for backup, in case this corrupts the file.
			}
		if (com == "New")
			{
			LGM f = new LGM();
			f.createTree(true);
			f.createToolBar();
			f.setOpaque(true);
			LGM.frame.setContentPane(f);
			LGM.currentFile = new Gm6File();
			f.updateUI();
			}
		}

  protected Transferable createTransferable(JComponent c)
  	{
  	ResNode n = (ResNode)((JTree)c).getLastSelectedPathComponent();

  	//if (LGM.prefs.emulate)
  	if (n.status == 1 || n.kind == 10 || n.kind == 11) return null;
  	return n;
  	}

	public int getSourceActions(JComponent c)
		{
		return COPY_OR_MOVE;
		}

	public boolean canImport(TransferHandler.TransferSupport support)
		{
		if (!support.isDataFlavorSupported(ResNode.NODE_FLAVOR))
			return false;
		JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
		TreePath p = dl.getPath();
		if (p== null)
			return false;
		//if (LGM.prefs.emulate)
		return true;
		}

	public boolean canImport(JComponent c, DataFlavor[] flavors)
		{
		if (c instanceof JTree == false)
			return super.canImport(c,flavors);
		((JTree)c).getDropLocation().getPath();
		return true;
		}

	public boolean importData(TransferHandler.TransferSupport support)
		{
		if (!canImport(support))
			return false;
		
		return true;
		}
	}