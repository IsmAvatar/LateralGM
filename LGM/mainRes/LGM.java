package mainRes;

/*
 * Special thanks to the following people:
 * GMGuru from the GJava forums for unlimited support and pointers,
 *  and finding ways to break things that I thought couldn't be brokent
 * Deudeu from the SDN Java Forums for his Drag and Drop tree
 * DeathFinder from the GMC for providing graphics
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import resourcesRes.Resource;
import resourcesRes.Sprite;

import componentRes.ResNode;

import fileRes.Gm6File;
import fileRes.Gm6FormatException;

public class LGM extends JPanel implements MouseListener
	{

	private static final long serialVersionUID = 1L;

	static JFrame frame;
	static myMenu menu;
	static JTree tree;
	static ResNode root;
	static Gm6File currentFile;

	private static class myFileFilter extends FileFilter
		{
		private String extension;
		private String desc;

		public myFileFilter(String extension,String desc)
			{
			this.extension = extension;
			this.desc = desc;
			}

		public boolean accept(File f)
			{
			if (f.isDirectory()) return true;
			return f.getPath().endsWith(extension);
			}

		public String getDescription()
			{
			return desc;
			}
		}

	public static String[] kinds = { "","Object","Sprite","Sound","Room","","Background","Script","Path","Font",
			"Info","GM","Timeline" };

	public LGM()
		{
		super(new BorderLayout());
		}

  public static ImageIcon findIcon(String filename)  
    {  
    ImageIcon ico = new ImageIcon("icons/" + filename.toLowerCase());  
    if (ico.getIconWidth() == -1)  
         {  
         URL url = LGM.class.getClassLoader().getResource("icons/" + filename.toLowerCase());  
         if (url != null)  
              {  
              ico = new ImageIcon(url);  
              }  
         }  
    return ico;  
    }

	public void createToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		add("North",tool);
		JButton but = new JButton(findIcon("new.png"));
		but.setActionCommand("New");
		but.addActionListener(menu);
		tool.add(but);
		but = new JButton(findIcon("Open....png"));
		but.setActionCommand("Open...");
		but.addActionListener(menu);
		tool.add(but);
		but = new JButton(findIcon("save.png"));
		tool.add(but);
		tool.add(new JToolBar.Separator());
		but = new JButton(findIcon("save as....png"));
		tool.add(but);
		}

	public void createTree(boolean populate)
		{
		createTree(new ResNode("Root",(byte)0,(byte)0,null),populate);
		}

	public void createTree(ResNode newroot,boolean populate)
		{
		root = newroot;
		tree = new DnDJTree();
		DefaultTreeModel mod = new DefaultTreeModel (root); 
		tree.setModel (mod);
		tree.setEditable(true);
		tree.addMouseListener(this);
		tree.setScrollsOnExpand(true);
		tree.setDragEnabled(true);
		tree.setCellRenderer(new myTreeGraphics());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		if (populate)
			{
			ResNode sprites = root.addChild("Sprites",ResNode.STATUS_PRIMARY,Resource.SPRITE);
			root.addChild("Sounds",ResNode.STATUS_PRIMARY,Resource.SOUND);
			root.addChild("Backgrounds",ResNode.STATUS_PRIMARY,Resource.BACKGROUND);
			root.addChild("Paths",ResNode.STATUS_PRIMARY,Resource.PATH);
			root.addChild("Scripts",ResNode.STATUS_PRIMARY,Resource.SCRIPT);
			root.addChild("Fonts",ResNode.STATUS_PRIMARY,Resource.FONT);
			root.addChild("Time Lines",ResNode.STATUS_PRIMARY,Resource.TIMELINE);
			root.addChild("Objects",ResNode.STATUS_PRIMARY,Resource.GMOBJECT);
			root.addChild("Rooms",ResNode.STATUS_PRIMARY,Resource.ROOM);
			root.addChild("Game Information",ResNode.STATUS_SECONDARY,Resource.GAMEINFO);
			root.addChild("Global Game Settings",ResNode.STATUS_SECONDARY,Resource.GAMESETTINGS);
			tree.setSelectionPath(new TreePath(root).pathByAddingChild(root.getChildAt(0)));
			sprites.addChild("Spr_1",ResNode.STATUS_SECONDARY,Resource.SPRITE);
			sprites.addChild("Spr_2",ResNode.STATUS_SECONDARY,Resource.SPRITE);
			sprites.addChild("Spr_3",ResNode.STATUS_SECONDARY,Resource.SPRITE);
			sprites.addChild("Spr_4",ResNode.STATUS_SECONDARY,Resource.SPRITE);
			
			}
		else
			{
			tree.setSelectionRow(0);
			}
		tree.updateUI();
		for (int m = 0; m < tree.getRowCount(); m++)
			tree.expandRow(m);
		for (int m = tree.getRowCount() - 1; m >= 0; m--)
			tree.collapseRow(m);
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(200,400));
		JDesktopPane MDI = new JDesktopPane();
		//MDI.setPreferredSize(new Dimension(400,600));
		JScrollPane scroll2 = new JScrollPane(MDI);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,scroll,scroll2);
		split.setDividerLocation(170);  
		add(split);
		JInternalFrame test = new JInternalFrame("test yay!!!",true,true,true,true);
		test.setVisible(true);
		test.setSize(300,300);
		MDI.add(test);
		}
	
	
 


	
	public static void sprite_form(int sprite)
	{
	Sprite spr = fileRes.Gm6File.Sprites.get(sprite);
	JInternalFrame test = new JInternalFrame(spr.Name,true,true,true,true);
	}

	static class myTreeGraphics extends DefaultTreeCellRenderer
		{
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree,Object val,boolean sel,boolean exp,boolean leaf,
				int row,boolean focus)
			{
			super.getTreeCellRendererComponent(tree,val,sel,exp,leaf,row,focus);
			ResNode node = (ResNode)val;
			if (node.status == ResNode.STATUS_SECONDARY)
				{
				setIcon(findIcon(kinds[node.kind] + ".png"));
				}
			else
				{
				if (exp && !node.isLeaf())
					setIcon(findIcon("group_open.png"));
				else
					setIcon(findIcon("group.png"));
				}
			return this;
			}
		}

	static class myMenuBar extends JMenuBar
		{
		private static final long serialVersionUID = 1L;

		public myMenuBar()
			{
			menu = new myMenu("File");
			menu.setMnemonic(KeyEvent.VK_F);
			add(menu);

			menu.addItem("New",KeyEvent.VK_N,KeyEvent.VK_N,ActionEvent.CTRL_MASK);
			menu.addItem("Open...",KeyEvent.VK_O,KeyEvent.VK_O,ActionEvent.CTRL_MASK);
			menu.addItem("Save",KeyEvent.VK_S,KeyEvent.VK_S,ActionEvent.CTRL_MASK);
			menu.addItem("Save As...",KeyEvent.VK_A);
			menu.add(new JSeparator());
			JCheckBoxMenuItem check = new JCheckBoxMenuItem("Advanced Mode");
			check.setMnemonic(KeyEvent.VK_V);
			menu.add(check);
			menu.addItem("Preferences...",KeyEvent.VK_P);
			menu.add(new JSeparator());
			menu.addItem("Exit",KeyEvent.VK_X,KeyEvent.VK_F4,ActionEvent.ALT_MASK);

			menu = new myMenu("Edit");
			menu.setMnemonic(KeyEvent.VK_E);
			add(menu);

			myMenu sub = new myMenu("Insert");
			sub.setMnemonic(KeyEvent.VK_I);
			menu.add(sub);
			sub.addItem("Group",KeyEvent.VK_G);
			sub.add(new JSeparator());
			sub.addItem("Sprite",KeyEvent.VK_I);
			sub.addItem("Sound",KeyEvent.VK_M);
			sub.addItem("Background",KeyEvent.VK_B);
			sub.addItem("Path",KeyEvent.VK_P);
			sub.addItem("Script",KeyEvent.VK_S);
			sub.addItem("Font",KeyEvent.VK_F);
			sub.addItem("Timeline",KeyEvent.VK_T);
			sub.addItem("Object",KeyEvent.VK_O);
			sub.addItem("Room",KeyEvent.VK_R);

			sub = new myMenu("Add");
			sub.setMnemonic(KeyEvent.VK_I);
			menu.add(sub);
			sub.addItem("Add Group",KeyEvent.VK_G);
			sub.add(new JSeparator());
			sub.addItem("Add Sprite",KeyEvent.VK_I);
			sub.addItem("Add Sound",KeyEvent.VK_M);
			sub.addItem("Add Background",KeyEvent.VK_B);
			sub.addItem("Add Path",KeyEvent.VK_P);
			sub.addItem("Add Script",KeyEvent.VK_S);
			sub.addItem("Add Font",KeyEvent.VK_F);
			sub.addItem("Add Timeline",KeyEvent.VK_T);
			sub.addItem("Add Object",KeyEvent.VK_O);
			sub.addItem("Add Room",KeyEvent.VK_R);

			menu.add(new JSeparator());
			menu.addItem("Rename",KeyEvent.VK_R,KeyEvent.VK_F2,0);
			menu.addItem("Delete",KeyEvent.VK_D,KeyEvent.VK_DELETE,ActionEvent.SHIFT_MASK);
			menu.addItem("Copy",KeyEvent.VK_C,KeyEvent.VK_INSERT,ActionEvent.ALT_MASK);
			menu.add(new JSeparator());
			menu.addItem("Properties",KeyEvent.VK_P,KeyEvent.VK_ENTER,ActionEvent.ALT_MASK);

			menu = new myMenu("Resources");
			menu.setMnemonic(KeyEvent.VK_R);
			add(menu);

			menu.addItem("Verify Names",KeyEvent.VK_V);
			menu.addItem("Syntax Check",KeyEvent.VK_S);
			menu.add(new JSeparator());
			menu.addItem("Find...",KeyEvent.VK_F,KeyEvent.VK_F,ActionEvent.ALT_MASK + ActionEvent.CTRL_MASK);
			menu.addItem("Annotate",KeyEvent.VK_A);
			menu.add(new JSeparator());
			menu.addItem("Expand",KeyEvent.VK_E);
			menu.addItem("Collapse",KeyEvent.VK_C);

			menu = new myMenu("Help");
			menu.setMnemonic(KeyEvent.VK_H);
			add(menu);

			menu.addItem("Manual",KeyEvent.VK_M,KeyEvent.VK_F1,0);
			menu.addItem("About",KeyEvent.VK_A);
			}
		}

	static class myMenu extends JMenu implements ActionListener
		{
		private static final long serialVersionUID = 1L;

		public myMenu(String s)
			{
			super(s);
			}

		public JMenuItem addItem(String name,int alt)
			{
			JMenuItem item = new JMenuItem(name.replaceAll("Add ",""),alt);
			item.setIcon(findIcon(name.replaceAll("Add ","") + ".png"));
			item.setActionCommand(name);
			item.addActionListener(this);
			add(item);
			return item;
			}

		public JMenuItem addItem(String name,int alt,int shortcut,int control)
			{
			JMenuItem item = new JMenuItem(name,alt);
			item.setIcon(findIcon(name + ".png"));
			item.setActionCommand(name);
			item.setAccelerator(KeyStroke.getKeyStroke(shortcut,control));
			item.addActionListener(this);
			add(item);
			return item;
			}

		public void actionPerformed(ActionEvent e)
			{
			String com = e.getActionCommand();
			if (com == "Exit") frame.dispose();
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
				fc.setFileFilter(new myFileFilter(".gm6","Game Maker 6 Files"));
				fc.showOpenDialog(frame);

				if (fc.getSelectedFile() != null)
					{
					if (fc.getSelectedFile().exists())
						{
						try
							{
							ResNode newroot = new ResNode("Root",0,0,null);
							Gm6File newfile = new Gm6File();
							newfile.LoadGm6File(fc.getSelectedFile().getPath(),newroot);
							currentFile = newfile;
							LGM f = new LGM();
							f.createTree(newroot,false);
							tree.setSelectionPath(new TreePath(root).pathByAddingChild(root.getChildAt(0)));
							f.createToolBar();
							f.setOpaque(true);
							frame.setContentPane(f);
							f.updateUI();
							}
						catch (Gm6FormatException ex)
							{
							JOptionPane.showMessageDialog(frame,"error occured in:\n" + ex.stackAsString() + "\nmessage: "
									+ ex.getMessage(),"Error Loading File",JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			if (com == "Save As...")
				{
				if (true) return;
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new myFileFilter(".gm6","Game Maker 6 Files"));
				boolean done = false;
				while (!done)
					{
					fc.showSaveDialog(frame);
					if (fc.getSelectedFile() != null)
						{
						String alteredstr = fc.getSelectedFile().getPath();
						if (!alteredstr.endsWith(".gm6")) alteredstr += ".gm6";
						File altered = new File(alteredstr);
						if (!altered.exists())
							{
							currentFile.WriteGm6File(altered.getPath(),root);
							done = true;
							}
						else
							{
							int result = JOptionPane.showConfirmDialog(frame,altered.getPath()
									+ " already exists. Do you wish to replace it?","Save File",
									JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
							if (result == 0)
								{
								currentFile.WriteGm6File(altered.getPath(),root);
								done = true;
								}
							else if (result == 2) done = true;
							}
						}
					}
				}
			if (com == "Save")
				{
				return; //make a .gb1 file for backup, in case this corrupts the file.
				}
			if (com == "New")
				{
				LGM f = new LGM();
				f.createTree(true);
				f.createToolBar();
				f.setOpaque(true);
				frame.setContentPane(f);
				currentFile = new Gm6File();
				f.updateUI();
				}
			}
		}

	public static void main(String[] args)
		{
		try
			{
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		catch (Exception e)
			{
			}
		currentFile = new Gm6File();
		frame = new JFrame("Lateral GM 6.1");
		frame.setSize(600,600);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(new myMenuBar());
		LGM f = new LGM();
		f.createTree(true);
		f.createToolBar();
		f.setOpaque(true);
		frame.setContentPane(f);
		frame.setVisible(true);
		for (int m = 0; m < tree.getRowCount(); m++)
			tree.expandRow(m);
		for (int m = tree.getRowCount() - 1; m >= 0; m--)
			tree.collapseRow(m);
		tree.updateUI();
		}

	public void mouseClicked(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseEntered(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseExited(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mousePressed(MouseEvent e)
		{
    int selRow = tree.getRowForLocation(e.getX(), e.getY());
    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if(selRow != -1) {
        if(e.getClickCount() == 1) {
           //do nothing
        Object[] path = selPath.getPath();
        ResNode r = (ResNode)path[0];
       int a = r.id;
       sprite_form(a);
        }
        else if(e.getClickCount() == 2) {
            {
            //myDoubleClick(selRow, selPath);
            Object[] path = selPath.getPath();
            ResNode r = (ResNode)path[0];
           int a = r.id;
           sprite_form(a);
            }
        }
    }
		
		}

	public void mouseReleased(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}
	}