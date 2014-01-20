/*
* Copyright (C) 2006-2011 IsmAvatar <IsmAvatar@gmail.com>
* Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
* Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
* Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
* Copyright (C) 2013, Robert B. Colton
*
* This file is part of LateralGM.
*
* LateralGM is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* LateralGM is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License (COPYING) for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.lateralgm.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.GmMenuBar;
import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.FramePrefsHandler;
import org.lateralgm.components.impl.GmTreeEditor;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIPane;
import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.ProjectFile.ResourceHolder;
import org.lateralgm.file.ProjectFile.SingletonResourceHolder;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Extensions;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.subframes.EventPanel;
import org.lateralgm.subframes.ExtensionsFrame;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;
import org.lateralgm.subframes.PreferencesFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.ResourceFrame.ResourceFrameFactory;


public final class LGM
	{
	public static String iconspath = "org/lateralgm/icons/";
	public static String iconspack = "Calico";
	public static String themename = "Swing";
	// this font is used on some controls in my Quantum theme
	// for the time being to fix the GTK bug with it not
	// using the UIManager resources for font and color on controls
	// in its look and feel classes, this is Java's fault not ours
	public static Font lnfFont = new Font("Ubuntu", Font.PLAIN, 14);
	public static boolean themechanged = false;

	public static int javaVersion;
	public static File tempDir, workDir;
	static
		{

		//Get Java Version
		String jv = System.getProperty("java.version"); //$NON-NLS-1$
		Scanner s = new Scanner(jv).useDelimiter("[\\._-]"); //$NON-NLS-1$
		javaVersion = s.nextInt() * 10000 + s.nextInt() * 100 + s.nextInt();

		try
			{
			workDir = new File(LGM.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			}
		catch (Exception e)
			{
			System.err.println(Messages.format("LGM.NO_WORKDIR",e.getClass(),e.getLocalizedMessage()));
			}
		}
	public static JFrame frame;
	public static JPanel content;
	public static JToolBar tool;
	public static JTree tree;
	public static ResNode root;
	public static ProjectFile currentFile = new ProjectFile();
	public static MDIPane mdi;
	private static GameInformationFrame gameInfo;
	private static GameSettingFrame gameSet;
	private static ExtensionsFrame extSet;
	public static EventPanel eventSelect;
	private static JFrame eventFrame;
	public static AbstractButton eventButton;
	public static PreferencesFrame prefFrame;
	public static Cursor zoomCursor;
	public static Cursor zoomInCursor;
	public static Cursor zoomOutCursor;

	private static void createMouseCursors() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		Image cimg = LGM.getIconForKey("CursorDisplay.ZOOM").getImage();
		BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.createGraphics();
		g.drawImage(cimg, 0, 0, null);
		zoomCursor = toolkit.createCustomCursor(img, 
				new Point(0,0), "Zoom");  
		
		cimg = LGM.getIconForKey("CursorDisplay.ZOOM_IN").getImage();
		img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.drawImage(cimg, 0, 0, null);
		zoomInCursor = toolkit.createCustomCursor(img, 
				new Point(0,0), "ZoomIn");  
		
		cimg = LGM.getIconForKey("CursorDisplay.ZOOM_OUT").getImage();
		img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.drawImage(cimg, 0, 0, null);
		zoomOutCursor = toolkit.createCustomCursor(img, 
				new Point(0,0), "ZoomOut");  
	}
	
	public static void SetLookAndFeel(String LOOKANDFEEL)
		{
		if (LOOKANDFEEL.equals(themename))
			{
			themechanged = false;
			return;
			}
		themechanged = true;
		themename = LOOKANDFEEL;
		String lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
		if (LOOKANDFEEL != null)
			{
			if (LOOKANDFEEL.equals("Swing"))
				{
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
				// an alternative way to set the Metal L&F is to replace the
				// previous line with:
				// lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
				}
			else if (LOOKANDFEEL.equals("Native"))
				{
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
				}
			else if (LOOKANDFEEL.equals("Nimbus"))
				{
				lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
				}
			else if (LOOKANDFEEL.equals("Windows"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
				}
			else if (LOOKANDFEEL.equals("CDE/Motif"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
				}
			else if (LOOKANDFEEL.equals("Metal"))
				{
				lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
				MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
				}
			else if (LOOKANDFEEL.equals("GTK+"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
				}
			else if (LOOKANDFEEL.equals("Quantum"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
				}
			else if (LOOKANDFEEL.equals("Custom"))
				{
				lookAndFeel = Prefs.swingThemePath;
				}
			else
				{
				// Perhaps we did not get the name right, see if the theme is installed
				// and attempt to use it.
				boolean foundMatch = false;
		    LookAndFeelInfo lnfs[] = UIManager.getInstalledLookAndFeels();
		    for (int i = 0; i < lnfs.length; i++) {
		      if (LOOKANDFEEL.equals(lnfs[i].getName())) {
		        lookAndFeel = lnfs[i].getClassName();
		        foundMatch = true;
		      }
		    }
				if (!foundMatch) {
				  System.err.println("Unexpected value of LOOKANDFEEL specified: " + LOOKANDFEEL);
				  lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
				}
				}

			try
				{
				UIManager.setLookAndFeel(lookAndFeel);

				}
			catch (ClassNotFoundException e)
				{
				System.err.println("Couldn't find class for specified look and feel:" + lookAndFeel);
				System.err.println("Did you include the L&F library in the class path?");
				System.err.println("Using the default look and feel.");
				}

			catch (UnsupportedLookAndFeelException e)
				{
				System.err.println("Can't use the specified look and feel (" + lookAndFeel
						+ ") on this platform.");
				System.err.println("Using the default look and feel.");
				}

			catch (Exception e)
				{
				System.err.println("Couldn't get specified look and feel (" + lookAndFeel
						+ "), for some reason.");
				System.err.println("Using the default look and feel.");
				e.printStackTrace();
				}
			}
		}

	// this function is for updating the look and feel after its
	// already been initialized and all controls created
	public static void UpdateLookAndFeel()
	{
		if (!themechanged)
			{
			return;
			}
		SwingUtilities.updateComponentTreeUI(tree);
		SwingUtilities.updateComponentTreeUI(mdi);
		if (eventFrame == null) {
		  SwingUtilities.updateComponentTreeUI(eventSelect);
		} else {
		  SwingUtilities.updateComponentTreeUI(eventFrame);
		}
		frame.pack();
		Window windows[] = frame.getWindows();
		for(Window i : windows) {
		  SwingUtilities.updateComponentTreeUI(i);
    }
	}

	public static GameInformationFrame getGameInfo()
	{
		return gameInfo;
	}

	public static GameSettingFrame getGameSettings()
	{
		return gameSet;
	}

	public static ExtensionsFrame getGameExtensions()
		{
    extSet.revertResource();
		return extSet;
		}

	public static void showGameInformation()
	{
    getGameInfo().setVisible(true);
	}
	
	public static void showGameSettings()
	{
    getGameSettings().setVisible(true);
	}
	
	public static void showGameExtensions()
	{
    getGameExtensions().setVisible(true);
	}
	
	private LGM()
		{

		}

	public static ImageIcon findIcon(String filename)
		{
		String fixedpath = iconspath + iconspack + "/" + filename;
		String custompath = Prefs.iconPath + filename;
		String location = ""; //$NON-NLS-1$
		File f = new File(custompath);
		if (Prefs.iconPack == "Custom")
			{
			location = custompath;
			}
		else
			{
			if (f.exists())
				{
				location = custompath;
				}
			else
				{
				location = fixedpath;
				}
			}

		ImageIcon ico = new ImageIcon(location);
		if (ico.getIconWidth() == -1)
			{
			URL url = LGM.class.getClassLoader().getResource(location);
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
		InputStream is = LGM.class.getClassLoader().getResourceAsStream(
				"org/lateralgm/main/icons.properties"); //$NON-NLS-1$
		try
			{
			if (is == null) throw new IOException();
			iconProps.load(is);
			}
		catch (IOException e)
			{
			System.err.println("Unable to read icons.properties"); //$NON-NLS-1$
			}
		String filename = iconProps.getProperty(key,""); //$NON-NLS-1$
		if (!filename.isEmpty()) return findIcon(filename);
		return null;
		}

	public static JButton makeButton(String key)
		{
		JButton but = new JButton();
		makeButton(but,key);
		return but;
		}

	public static AbstractButton makeButton(AbstractButton but, String key)
		{
		Icon ico = LGM.getIconForKey(key);
		if (ico != null)
			but.setIcon(ico);
		else
			but.setIcon(GmTreeGraphics.getBlankIcon());
		but.setActionCommand(key);
		but.setToolTipText(Messages.getString(key));
		but.addActionListener(Listener.getInstance());
	
		return but;
		}

	private static JToolBar createToolBar()
		{
		tool = new JToolBar();
		tool.setFloatable(true);
		tool.add(makeButton("Toolbar.NEW")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.OPEN")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.SAVE")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.SAVEAS")); //$NON-NLS-1$
		tool.addSeparator();
		for (Class<? extends Resource<?,?>> k : Resource.kinds)
			if (InstantiableResource.class.isAssignableFrom(k))
				{
				Icon ico = ResNode.ICON.get(k);
				if (ico == null) ico = GmTreeGraphics.getBlankIcon();
				JButton but = new JButton(ico);
				but.setToolTipText(Messages.format("Toolbar.ADD",Resource.kindNames.get(k)));
				but.addActionListener(new Listener.ResourceAdder(false,k));
				tool.add(but);
				}
		tool.addSeparator();
		tool.add(makeButton("Toolbar.PREFERENCES")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.GMI")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.GMS")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.EXT")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.DOCUMENTATION")); //$NON-NLS-1$
		tool.add(Box.createHorizontalGlue()); //right align after this
		tool.add(eventButton = makeButton(new JToggleButton(),"Toolbar.EVENT_BUTTON")); //$NON-NLS-1$
		if (LGM.themename.equals("Quantum")) {
		  tool.setFont(LGM.lnfFont);
		}
		return tool;
		}

	private static JComponent createTree()
		{
		return createTree(newRoot());
		}

	private static JComponent createTree(ResNode newroot)
		{
		tree = new JTree(new DefaultTreeModel(newroot));
		if (LGM.themename.equals("Quantum")) {
		  tree.setFont(lnfFont);
		}

		GmTreeGraphics renderer = new GmTreeGraphics();
		
		GmTreeEditor editor = new GmTreeEditor(tree,renderer);

		editor.addCellEditorListener(Listener.getInstance());
		tree.setEditable(true);

		tree.addMouseListener(Listener.getInstance().mListener);
		if (javaVersion >= 10600)
			{
			tree.setTransferHandler(Listener.getInstance());
			tree.setDragEnabled(true);
			tree.setDropMode(DropMode.ON_OR_INSERT);
			}
		tree.setCellRenderer(renderer);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellEditor(editor);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		//remove the cut, copy, and paste bindings
		InputMap im = tree.getInputMap();
		for (KeyStroke s : im.allKeys())
			{
			Object o = im.get(s);
			if (o.equals("cut") || o.equals("copy") || o.equals("paste")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				im.put(s,"none"); //null doesn't remove them //$NON-NLS-1$
			}

		// Setup the rest of the main window
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(250,100));
		scroll.setAlignmentX(JScrollPane.RIGHT_ALIGNMENT);

		return scroll;
		}

	public static ResNode newRoot()
		{
		return root = new ResNode("Root",(byte) 0,null,null); //$NON-NLS-1$
		}

	private static JComponent createMDI()
		{
		mdi = new MDIPane();
		JScrollPane scroll = new JScrollPane(mdi);
		mdi.setScrollPane(scroll);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mdi.setBackground(Color.GRAY);
		// eventSelect = new EventPanel();
		// mdi.add(eventSelect);
		return scroll;
		}

	public static void loadPlugins()
		{
		if (workDir == null) return;
		File dir = new File(workDir.getParent(),"plugins"); //$NON-NLS-1$
		if (!dir.exists()) dir = new File(workDir.getParent(),"Plugins"); //$NON-NLS-1$
		File[] ps = dir.listFiles(new CustomFileFilter(null,".jar")); //$NON-NLS-1$
		if (ps == null) return;
		for (File f : ps)
			{
			if (!f.exists()) continue;
			try
				{
				String pluginEntry = "LGM-Plugin"; //$NON-NLS-1$
				Manifest mf = new JarFile(f).getManifest();
				String clastr = mf.getMainAttributes().getValue(pluginEntry);
				if (clastr == null)
					throw new Exception(Messages.format("LGM.PLUGIN_MISSING_ENTRY",pluginEntry));
				URLClassLoader ucl = new URLClassLoader(new URL[] { f.toURI().toURL() });
				ucl.loadClass(clastr).newInstance();
				}
			catch (Exception e)
				{
				String msgInd = "LGM.PLUGIN_LOAD_ERROR"; //$NON-NLS-1$
				System.out.println(Messages.format(msgInd,f.getName(),e.getClass().getName(),e.getMessage()));
				continue;
				}
			}
		}

	public static void populateTree()
		{
		for (Class<? extends Resource<?,?>> k : Resource.kinds)
			{
			String name = Resource.kindNamesPlural.get(k);
			byte status = InstantiableResource.class.isAssignableFrom(k) ? ResNode.STATUS_PRIMARY
					: ResNode.STATUS_SECONDARY;
			root.addChild(name,status,k);
			}
		tree.setSelectionPath(new TreePath(root).pathByAddingChild(root.getChildAt(0)));
		}

	/**
	* Commits all front-end changes to the back-end.
	* Usually do this in preparation for writing the back-end to some output stream,
	* such as a file (e.g. saving) or a plugin (e.g. compiling).
	* Notice that LGM actually traverses the tree committing *all* ResNodes with frames,
	* rather than just the open MDI frames. Since GameSettings and GameInfo do not have
	* ResourceFrames associated with them, we commit them separately.
	* @see LGM#commitAll()
	*/
	public static void commitAll()
		{
		Enumeration<?> nodes = LGM.root.preorderEnumeration();
		while (nodes.hasMoreElements())
			{
			ResNode node = (ResNode) nodes.nextElement();
			if (node.frame != null) node.frame.updateResource(); // update open frames
			}
		LGM.getGameSettings().commitChanges();
		LGM.getGameInfo().updateResource();
		}

	public static void reload(boolean newRoot)
		{
		LGM.mdi.closeAll();

		LGM.tree.setModel(new DefaultTreeModel(LGM.root));
		LGM.tree.setSelectionRow(0);

		LGM.eventSelect.reload();

		gameSet.resOriginal = LGM.currentFile.gameSettings;
		gameSet.revertResource();
		gameSet.setVisible(false);
		gameInfo.resOriginal = LGM.currentFile.gameInfo;
		gameInfo.revertResource();
		gameInfo.setVisible(false);
		
		LGM.fireReloadPerformed(newRoot);
		}

	public static interface ReloadListener
		{
		/**
		* Called after LGM performs a reload, e.g. when a new file is created or loaded.
		* A reload causes the MDI to be flushed, the tree to refresh
		* (especially if a new root is provided), the EventPanel is recreated
		* (a hack to ensure that the events' link selectors know of the new root),
		* and Game Settings and Game Info are re-calibrated with their new settings
		* (but not recreated). Note that the Menu bar is left untouched and remains in tact.
		* @param newRoot indicates if a new root was provided to the tree
		* (e.g. the tree had to be re-populated)
		* @see LGM#reload(boolean newRoot)
		*/
		void reloadPerformed(boolean newRoot);
		}

	protected static ArrayList<ReloadListener> reloadListeners = new ArrayList<ReloadListener>();

	public static void addReloadListener(ReloadListener l)
		{
		reloadListeners.add(l);
		}

	public static void removeReloadListener(ReloadListener l)
		{
		reloadListeners.remove(l);
		}

	protected static void fireReloadPerformed(boolean newRoot)
		{
		for (ReloadListener rl : reloadListeners)
			rl.reloadPerformed(newRoot);
		}

	public static void addPluginResource(PluginResource pr)
		{
		ImageIcon i = pr.getIcon();
		if (i != null) ResNode.ICON.put(pr.getKind(),i);
		String p = pr.getPrefix();
		if (p != null) Prefs.prefixes.put(pr.getKind(),p);
		Resource.addKind(pr.getKind(),pr.getName3(),pr.getName(),pr.getPlural());
		LGM.currentFile.resMap.put(pr.getKind(),pr.getResourceHolder());
		ResourceFrame.factories.put(pr.getKind(),pr.getResourceFrameFactory());
		}

	public static interface PluginResource
		{
		Class<? extends Resource<?,?>> getKind();

		/** Can be null, in which case the default icon is used. */
		ImageIcon getIcon();

		String getName3();

		String getName();

		String getPlural();

		String getPrefix();

		ResourceHolder<?> getResourceHolder();

		ResourceFrameFactory getResourceFrameFactory();
		}

	public static abstract class SingletonPluginResource<T extends Resource<T,?>> implements
			PluginResource
		{
		public String getPlural()
			{
			return getName();
			}

		public String getPrefix()
			{
			return null;
			}

		public ResourceHolder<?> getResourceHolder()
			{
			return new SingletonResourceHolder<T>(getInstance());
			}

		public abstract T getInstance();
		}
  
	public static void main(final String[] args)
		{
		
		//java6u10 regression causes graphical xor to be very slow
		System.setProperty("sun.java2d.d3d","false"); //$NON-NLS-1$ //$NON-NLS-2$
		//Put the Mac menu bar where it belongs (ignored by other systems)
		System.setProperty("apple.laf.useScreenMenuBar","true"); //$NON-NLS-1$ //$NON-NLS-2$
		//Set the Mac menu bar title to the correct name (also adds a useless About entry, so disabled)
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name",Messages.getString("LGM.NAME")); //$NON-NLS-1$ //$NON-NLS-2$

		// Enable antialasing of fonts
		System.setProperty("awt.useSystemAAFontSettings",Prefs.antialiasControlFont);
		System.setProperty("swing.aatext", "true");

		
		System.out.format("Java Version: %d (%s)\n",javaVersion,System.getProperty("java.version")); //$NON-NLS-1$
		if (javaVersion < 10600)
			System.out.println("Some program functionality will be limited due to your outdated Java version"); //$NON-NLS-1$

		iconspack = Prefs.iconPack;
		SetLookAndFeel(Prefs.swingTheme);
		JFrame.setDefaultLookAndFeelDecorated(true);
		themechanged = false;
		
		SplashProgress splashProgress = new SplashProgress();
		splashProgress.start();

		//Set up temp dir and work dir
		Util.tweakIIORegistry();
		tempDir = new File(System.getProperty("java.io.tmpdir"),"lgm"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!tempDir.exists())
			{
			tempDir.mkdir();
			if (javaVersion >= 10600)
				{
				tempDir.setReadable(true,false);
				tempDir.setWritable(true,false);
				}
			}

		splashProgress.progress(10,Messages.getString("LGM.SPLASH_LANG"));
		Messages.updateLangPack();

		splashProgress.progress(15,Messages.getString("LGM.SPLASH_CURSOR"));
		createMouseCursors();
		
		splashProgress.progress(20,Messages.getString("LGM.SPLASH_LIBS")); //$NON-NLS-1$
		LibManager.autoLoad();

		splashProgress.progress(30,Messages.getString("LGM.SPLASH_TOOLS")); //$NON-NLS-1$
		JToolBar toolbar = createToolBar();
		JComponent tree = createTree();
		content = new JPanel(new BorderLayout());
		content.add(BorderLayout.CENTER,createMDI());
		eventSelect = new EventPanel();
		
		if (Prefs.dockEventPanel)
			{
			content.add(BorderLayout.EAST,eventSelect);
			}

		// could possibly be used to force the toolbar with event panel to popout
		// reducing code, i can not get it to work right however
		//((BasicToolBarUI) eventSelect.getUI()).setFloating(true, new Point(500,50));

		splashProgress.progress(40,Messages.getString("LGM.SPLASH_THREAD")); //$NON-NLS-1$
		
		gameInfo = new GameInformationFrame(currentFile.gameInfo);
		mdi.add(gameInfo);
		gameSet = new GameSettingFrame(currentFile.gameSettings,currentFile.constants,
				currentFile.includes);
		mdi.add(gameSet);
		extSet = new ExtensionsFrame(new Extensions());
		mdi.add(extSet);

		splashProgress.progress(50,Messages.getString("LGM.SPLASH_MENU")); //$NON-NLS-1$
		frame = new JFrame(Messages.format("LGM.TITLE", //$NON-NLS-1$
				Messages.getString("LGM.NEWGAME"))); //$NON-NLS-1$
		JMenuBar gmnb = new GmMenuBar();
		if (LGM.themename.equals("Quantum")) {
		  gmnb.setFont(lnfFont);
		}
		frame.setJMenuBar(gmnb);
		splashProgress.progress(60,Messages.getString("LGM.SPLASH_UI")); //$NON-NLS-1$
		JPanel f = new JPanel(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new java.awt.event.WindowAdapter()
			{
				public void windowClosing(WindowEvent winEvt)
					{
					LGM.onMainFrameClosed();
					}
			});

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,tree,content);
		split.setDividerLocation(250);
		split.setOneTouchExpandable(true);
		f.add(split);

		frame.setContentPane(f);
		frame.setTransferHandler(Listener.getInstance().fc.new LGMDropHandler());
		//f.add(BorderLayout.CENTER,content);
		eventSelect.setVisible(false); //must occur after adding split
		f.add(BorderLayout.NORTH,toolbar);
		f.setOpaque(true);
		new FramePrefsHandler(frame);
		splashProgress.progress(65,Messages.getString("LGM.SPLASH_LOGO")); //$NON-NLS-1$
		try
			{
			frame.setIconImage(ImageIO.read(LGM.class.getClassLoader().getResource(
					"org/lateralgm/main/lgm-logo.png"))); //$NON-NLS-1$
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
    splashProgress.progress(70,Messages.getString("LGM.SPLASH_TREE")); //$NON-NLS-1$
    populateTree();
		splashProgress.progress(75,Messages.getString("LGM.SPLASH_PLUGINS")); //$NON-NLS-1$
		loadPlugins();
		splashProgress.progress(90,Messages.getString("LGM.SPLASH_PRELOAD")); //$NON-NLS-1$
		if (args.length > 0 && args[0].length() > 1) {
			  String path = args[0].replace("\\","/");
			  URI uri = new File(path).toURI();
				Listener.getInstance().fc.open(uri); 
		}
		splashProgress.complete();
		applyBackground("org/lateralgm/main/lgm1.png"); //$NON-NLS-1$
		frame.setVisible(true);
		frame.pack();
		if (Prefs.frameMaximized) {
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}

		}

	public static void askToSaveProject()
		{
		FileChooser fc = new FileChooser();
		fc.save(LGM.currentFile.uri,LGM.currentFile.format);
		}

	public static void onMainFrameClosed()
		{
		int n = JOptionPane.showConfirmDialog(null,Messages.getString("LGM.KEEPCHANGES_MESSAGE"),
				Messages.getString("LGM.KEEPCHANGES_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,null);

		// java preferences do not seem to memorize maximized state
		if ((frame.getExtendedState() & frame.MAXIMIZED_BOTH) != 0) {
				PrefsStore.setFrameMaximized(true);
		} else {
				PrefsStore.setFrameMaximized(false);
		}

		switch (n)
			{
			case JOptionPane.YES_OPTION:
				askToSaveProject();
				System.exit(0);
				break;
			case JOptionPane.NO_OPTION:
				System.exit(0);
				break;
			case JOptionPane.CANCEL_OPTION:
				// do nothing
				break;
			}
		}

	static final class SplashProgress
		{
		final SplashScreen splash;
		final Graphics2D splashGraphics;
		final JProgressBar bar;
		final Graphics barGraphics;

		private String text = null;

		final boolean TIMER = System.getProperty("lgm.progresstimer") != null; //$NON-NLS-1$
		private long startTime, completeTime;
		private ArrayList<Integer> progressValues;
		private ArrayList<Long> progressTimes;

		SplashProgress()
			{
			splash = SplashScreen.getSplashScreen();
			if (splash != null)
				{
				splashGraphics = splash.createGraphics();
				Dimension sss = splash.getSize();
				Rectangle bb = new Rectangle(0,sss.height - 24,sss.width,24);
				bar = new JProgressBar();
				bar.setBounds(bb);
				barGraphics = splashGraphics.create(bb.x,bb.y,bb.width,bb.height);
				}
			else
				{
				splashGraphics = null;
				bar = null;
				barGraphics = null;
				}
			if (TIMER)
				{
				progressValues = new ArrayList<Integer>();
				progressTimes = new ArrayList<Long>();
				}
			}

		void start()
			{
			if (TIMER) startTime = System.currentTimeMillis();
			progress(0,Messages.getString("LGM.SPLASH_START")); //$NON-NLS-1$
			}

		void complete()
			{
			if (TIMER)
				{
				completeTime = System.currentTimeMillis();
				long tt = completeTime - startTime;
				System.out.print("Progress/% "); //$NON-NLS-1$
				for (Integer v : progressValues)
					{
					System.out.print("\t" + v); //$NON-NLS-1$
					}
				System.out.println();
				System.out.print("Time/ms "); //$NON-NLS-1$
				for (Long t : progressTimes)
					{
					System.out.print("\t" + t); //$NON-NLS-1$
					}
				System.out.println();
				System.out.print("Actual progress/%"); //$NON-NLS-1$
				for (Long t : progressTimes)
					{
					System.out.print("\t" + Math.round(100.0 * t / tt)); //$NON-NLS-1$
					}
				System.out.println();
				}
			}

		void progress(int p)
			{
			progress(p,text);
			}

		void progress(int p, String t)
			{
			if (TIMER)
				{
				progressValues.add(p);
				progressTimes.add(System.currentTimeMillis() - startTime);
				}
			text = t;
			if (splash != null)
				{
				bar.setValue(p);
				bar.setStringPainted(t != null);
				bar.setString(t);
				update();
				}
			}

		private void update()
			{
			bar.paint(barGraphics);
			splash.update();
			}
		}

	private static boolean eventVisible = false;

	public static void showEventPanel()
		{
		if (Prefs.dockEventPanel)
			{
			eventVisible = !eventVisible;
			eventSelect.setVisible(eventVisible);
			}
		else
			{
			if (eventFrame == null)
				{
				eventFrame = new JFrame();
				eventFrame.setAlwaysOnTop(true);
				eventFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				eventFrame.setSize(new Dimension(250,300));
				eventFrame.setIconImage(LGM.getIconForKey("Toolbar.EVENT_BUTTON").getImage());
				eventFrame.setTitle(Messages.getString("Toolbar.EVENT_BUTTON"));
				eventFrame.add(eventSelect);
				eventSelect.setVisible(true);
				eventSelect.setFloatable(false);
				eventFrame.setLocationRelativeTo(frame);
				}
			eventFrame.setVisible(true);
			}
		}

	public static void hideEventPanel()
		{
		if (eventFrame != null)
			{
			eventFrame.setVisible(false);
			}
		else
			{
			eventSelect.setVisible(false);
			}
		}

	public static void showPreferences()
		{
		if (prefFrame == null)
			{
			prefFrame = new PreferencesFrame();
			}
		prefFrame.show();
		}

	public static class MDIBackground extends JComponent
		{
		private static final long serialVersionUID = 1L;
		ImageIcon image;

		public MDIBackground(ImageIcon icon)
			{
			image = icon;
			if (image == null) return;
			if (image.getIconWidth() <= 0) image = null;
			}

		public int getWidth()
			{
			return LGM.mdi.getWidth();
			}

		public int getHeight()
			{
			return LGM.mdi.getHeight();
			}

		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			if (image == null) return;
			for (int y = 0; y < getHeight(); y += image.getIconHeight())
				for (int x = 0; x < getWidth(); x += image.getIconWidth())
					g.drawImage(image.getImage(),x,y,null);
			}
		}

	public static void applyBackground(String bgloc)
		{
		URL url = LGM.class.getClassLoader().getResource(bgloc);
		ImageIcon bg = new ImageIcon(url);
		mdi.add(new MDIBackground(bg),JLayeredPane.FRAME_CONTENT_LAYER);
		}

	}