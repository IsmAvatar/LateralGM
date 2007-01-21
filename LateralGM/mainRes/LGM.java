package mainRes;

/*
 * Credit goes out to:
 * IsmAvatar from everywhere as project leader, lead programmer, and file format expert
 * TGMG from the G-Java forums for unlimited support and pointers,
 *  and finding ways to break things that I thought couldn't be broken
 *  and G-Java, which we hope will be incorporated as the primary compiler
 *  and finding CVS/SVN which helped teamwork and version control
 * Clam from the LGM forum for most of the Save and Load code
 *  which I then proceeded to break and refix; and starting the MDI desktop
 * DeathFinder from the GMC for providing graphics
 * Professor Mark Overmars for providing GM6 and its file format which inspired this,
 *  and then up and deciding to be an a-hole about it by the time GM7 came out.
 * Everyone else from the GMC forums, G-Java forums, LGM forum, etc. who helped out,
 *  gave tips, acknowledged LGM's presence, or have works that aided in its creation
 *  like (pardon any forgotten names):
 *  Porfirio, CAMD, pythonpoole, andrewmc, RhysAndrews, Yourself, GearGOD,
 *  roach, h0bbel, monkey dude, evilish, Natso, Appleman1234
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import resourcesRes.Resource;

import componentRes.GmMenuBar;
import componentRes.GmTreeGraphics;
import componentRes.Listener;
import componentRes.ResNode;

import fileRes.Gm6File;

public class LGM extends JPanel
	{
	private static final long serialVersionUID = 1L;
	public static final JFrame frame = new JFrame("Lateral GM 6.1");
	public static final Listener listener = new Listener(); 
	public static JTree tree;
	public static ResNode root;
	public static Gm6File currentFile = new Gm6File();

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
		JButton but = new JButton(LGM.findIcon("new.png"));
		but.setActionCommand("New");
		but.addActionListener(listener);
		tool.add(but);
		but = new JButton(LGM.findIcon("Open....png"));
		but.setActionCommand("Open...");
		but.addActionListener(listener);
		tool.add(but);
		but = new JButton(LGM.findIcon("save.png"));
		tool.add(but);
		tool.add(new JToolBar.Separator());
		but = new JButton(LGM.findIcon("save as....png"));
		tool.add(but);
		}

	public void createTree(boolean populate)
		{
		createTree(new ResNode("Root",(byte)0,(byte)0,null),populate);
		}

	public void createTree(ResNode newroot,boolean populate)
		{
		root = newroot;
		tree = new JTree(new DefaultTreeModel(root));
		tree.setEditable(true);
		tree.setScrollsOnExpand(true);
		tree.setTransferHandler(listener);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setCellRenderer(new GmTreeGraphics());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		if (populate)
			{
			ResNode r = root.addChild("Sprites",ResNode.STATUS_PRIMARY,Resource.SPRITE);
			r.addChild("sprite0",ResNode.STATUS_SECONDARY,Resource.SPRITE);
			ResNode g = r.addChild("group0",ResNode.STATUS_GROUP,Resource.SPRITE);
			g.addChild("sprite1",ResNode.STATUS_SECONDARY,Resource.SPRITE);
			g.addChild("group1",ResNode.STATUS_GROUP,Resource.SPRITE);
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
			}
		else
			{
			tree.setSelectionRow(0);
			}
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(200,100));
		JDesktopPane MDI = new JDesktopPane();
//		MDI.setPreferredSize(new Dimension(400,600));
		JScrollPane scroll2 = new JScrollPane(MDI);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,scroll,scroll2);
		split.setDividerLocation(170);
		add(split);
		JInternalFrame test = new JInternalFrame("test yay!!!",true,true,true,true);
		test.setVisible(true);
		test.setSize(300,300);
		MDI.add(test);
		}

	public static void main(String[] args)
		{
		frame.setSize(600,600);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(new GmMenuBar());
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
	}