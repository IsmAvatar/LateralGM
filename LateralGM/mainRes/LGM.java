package mainRes;

/*
 * Credit goes out to:
 * IsmAvatar from everywhere as project leader, lead programmer, and file format expert
 * TGMG from the G-Java forums has been an invaluable asset and programmer for the team,
 *  and finding ways to break things that I thought couldn't be broken
 *  and G-Java, which we hope will be incorporated as the primary compiler
 *  and finding CVS/SVN which helped teamwork and version control
 * Clam from the LGM forum for most of the Save and Load code
 *  which I then proceeded to break and refix; and starting the MDI desktop
 * DeathFinder from the GMC for providing the default graphics
 * Professor Mark Overmars for providing GM6 and its file format which inspired this,
 *  and then up and deciding to be an a-hole about it by the time GM7 came out.
 * Everyone else from the GMC forums, G-Java forums, LGM forum, etc. who helped out,
 *  gave tips, acknowledged LGM's presence, or have works that aided in its creation,
 *  including but certainly not limited to (and pardon the many forgotten names):
 *  Porfirio, CAMD, pythonpoole, andrewmc, RhysAndrews, Yourself, GearGOD,
 *  roach, Bendodge, h0bbel, monkey dude, evilish, Natso, Appleman1234
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import resourcesRes.Resource;
import SubFrames.GameInformationFrame;
import SubFrames.GameSettingFrame;

import componentRes.GmMenuBar;
import componentRes.GmTreeEditor;
import componentRes.GmTreeGraphics;
import componentRes.Listener;
import componentRes.MDIPane;
import componentRes.ResNode;

import fileRes.Gm6File;

public class LGM extends JPanel
	{
	private static final long serialVersionUID = 1L;
	public static final JFrame frame = new JFrame("Lateral GM 6.1"); //$NON-NLS-1$
	public static final Listener listener = new Listener();
	public static JTree tree;
	public static ResNode root;
	public static Gm6File currentFile = new Gm6File();
	public static JDesktopPane MDI;
	public static GameInformationFrame gameInfo = new GameInformationFrame();
	public static GameSettingFrame gameSet = new GameSettingFrame();
	public static String[] kinds = { "","Object","Sprite","Sound","Room","","Background","Script","Path", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			"Font","Info","GM","Timeline" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public LGM()
		{
		super(new BorderLayout());
		}

	public static ImageIcon findIcon(String filename)
		{
		ImageIcon ico = new ImageIcon("icons/" + filename.toLowerCase()); //$NON-NLS-1$
		if (ico.getIconWidth() == -1)
			{
			URL url = LGM.class.getClassLoader().getResource("icons/" + filename.toLowerCase()); //$NON-NLS-1$
			if (url != null)
				{
				ico = new ImageIcon(url);
				}
			}
		return ico;
		}

	public static ImageIcon getIconForKey(String key)
		{
		Properties iconProps = new Properties();
		InputStream is = LGM.class.getClassLoader().getResourceAsStream("mainRes/icons.properties"); //$NON-NLS-1$
		try
			{
			iconProps.load(is);
			}
		catch (IOException e)
			{
			System.err.println("Unable to read icons.properties");
			}
		String filename = iconProps.getProperty(key,""); //$NON-NLS-1$
		if (filename != "") //$NON-NLS-1$
			return findIcon(filename);
		return null;
		}

	public JButton makeButton(String key)
		{
		JButton but = new JButton(LGM.getIconForKey(key));
		but.setActionCommand(key);
		but.setToolTipText(Messages.getString(key));
		but.addActionListener(listener);
		return but;
		}

	public void createToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		add("North",tool); //$NON-NLS-1$
		tool.add(makeButton("LGM.NEW")); //$NON-NLS-1$
		tool.add(makeButton("LGM.OPEN")); //$NON-NLS-1$
		tool.add(makeButton("LGM.SAVE")); //$NON-NLS-1$
		tool.add(new JToolBar.Separator());
		tool.add(makeButton("LGM.SAVEAS")); //$NON-NLS-1$
		}

	public void createTree(boolean populate)
		{
		createTree(new ResNode("Root",(byte) 0,(byte) 0,null),populate); //$NON-NLS-1$
		}

	public void createTree(ResNode newroot, boolean populate)
		{
		root = newroot;
		tree = new JTree(new DefaultTreeModel(root));
		GmTreeGraphics renderer = new GmTreeGraphics();
		GmTreeEditor editor = new GmTreeEditor(tree,renderer);
		tree.setEditable(true);
		tree.addMouseListener(listener);
		tree.setScrollsOnExpand(true);
		tree.setTransferHandler(listener);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setCellRenderer(renderer);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellEditor(editor);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		if (populate)
			{
			root.addChild(Messages.getString("LGM.SPRITES"),ResNode.STATUS_PRIMARY,Resource.SPRITE); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.SOUNDS"),ResNode.STATUS_PRIMARY,Resource.SOUND); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.BACKGROUNDS"),ResNode.STATUS_PRIMARY,Resource.BACKGROUND); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.PATHS"),ResNode.STATUS_PRIMARY,Resource.PATH); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.SCRIPTS"),ResNode.STATUS_PRIMARY,Resource.SCRIPT); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.FONTS"),ResNode.STATUS_PRIMARY,Resource.FONT); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.TIMELINES"),ResNode.STATUS_PRIMARY,Resource.TIMELINE); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.OBJECTS"),ResNode.STATUS_PRIMARY,Resource.GMOBJECT); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.ROOMS"),ResNode.STATUS_PRIMARY,Resource.ROOM); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.GAMEINFO"),ResNode.STATUS_SECONDARY,Resource.GAMEINFO); //$NON-NLS-1$
			root.addChild(Messages.getString("LGM.GAMESETTINGS"),ResNode.STATUS_SECONDARY,Resource.GAMESETTINGS); //$NON-NLS-1$
			tree.setSelectionPath(new TreePath(root).pathByAddingChild(root.getChildAt(0)));
			}
		else
			{
			tree.setSelectionRow(0);
			}

		/*
		 * Setup the rest of the main window
		 */

		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(200,100));
		MDI = new MDIPane();
		JScrollPane scroll2 = new JScrollPane(MDI);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,scroll,scroll2);
		split.setDividerLocation(170);
		add(split);
		// .setDefaultCloseOperation(GameInformationFrame.HIDE_ON_CLOSE);
		MDI.add(gameSet);
		MDI.add(gameInfo);
		// gameInfo.setVisible(true);
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
		}
	}