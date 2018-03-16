/*
* Copyright (C) 2006-2011 IsmAvatar <IsmAvatar@gmail.com>
* Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
* Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
* Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
* Copyright (C) 2013, 2014, 2015 Robert B. Colton
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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.imageio.spi.IIORegistry;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.ErrorDialog;
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
import org.lateralgm.file.ResourceList;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.file.iconio.ICOImageReaderSPI;
import org.lateralgm.file.iconio.WBMPImageReaderSpiFix;
import org.lateralgm.main.Search.InvisibleTreeModel;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.subframes.ConfigurationManager;
import org.lateralgm.subframes.ConstantsFrame;
import org.lateralgm.subframes.EventPanel;
import org.lateralgm.subframes.ExtensionPackagesFrame;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;
import org.lateralgm.subframes.PreferencesFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.ResourceFrame.ResourceFrameFactory;

import com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi;

public final class LGM
	{
	public static final String version = "1.8.41"; //$NON-NLS-1$

	// TODO: This list holds the class loader for any loaded plugins which should be
	// cleaned up and closed when the application closes.
	public final static ArrayList<URLClassLoader> classLoaders = new ArrayList<URLClassLoader>();

	public static boolean LOADING_PROJECT = false;
	public static JDialog progressDialog = null;
	public static JProgressBar progressDialogBar = null;

	public static String iconspath = "org/lateralgm/icons/"; //$NON-NLS-1$
	public static String iconspack = "Calico"; //$NON-NLS-1$
	public static String themename = "Swing"; //$NON-NLS-1$
	public static boolean themechanged = false;

	public static int javaVersion;
	public static File tempDir, workDir;
	static
		{
		//Get Java Version
		String jv = System.getProperty("java.version"); //$NON-NLS-1$
		Scanner s = new Scanner(jv);
		s.useDelimiter("[\\._-]"); //$NON-NLS-1$
		int major = s.hasNextInt() ? s.nextInt() * 10000 : 0;
		int minor = s.hasNextInt() ? s.nextInt() * 100 : 0;
		int patch = s.hasNextInt() ? s.nextInt() : 0;
		javaVersion = major + minor + patch;
		s.close();

		//Tweak service providers
		IIORegistry reg = IIORegistry.getDefaultInstance();
		reg.registerServiceProvider(new ICOImageReaderSPI());
		reg.deregisterServiceProvider(reg.getServiceProviderByClass(WBMPImageReaderSpi.class));
		reg.registerServiceProvider(new WBMPImageReaderSpiFix());

		//Setup workdir and tempdir
		try
			{
			workDir = new File(LGM.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			}
		catch (Exception e)
			{
			System.err.println(Messages.format("LGM.NO_WORKDIR",e.getClass(),e.getLocalizedMessage())); //$NON-NLS-1$
			}

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
		}
	public static JFrame frame;
	public static JPanel contents;
	public static JToolBar tool;
	public static JToolBar filterPanel;
	public static JTree tree, searchTree;
	public static ResNode root;
	public static ProjectFile currentFile = new ProjectFile();
	public static MDIPane mdi;
	private static ConstantsFrame constantsFrame;
	private static GameInformationFrame gameInfo;
	private static GameSettingFrame gameSet;
	private static ExtensionPackagesFrame extSet;
	public static EventPanel eventSelect;
	private static JDialog eventDialog;
	public static AbstractButton eventButton;
	public static PreferencesFrame prefFrame;
	public static Cursor zoomCursor;
	public static Cursor zoomInCursor;
	public static Cursor zoomOutCursor;
	private static String progressTitle;
	public static GmMenuBar menuBar;

	public static JComboBox<GameSettings> configsCombo;

	private LGM()
		{
		}

	public static JDialog getProgressDialog()
		{
		if (progressDialog == null)
			{
			progressDialog = new JDialog(LGM.frame,true);
			progressDialogBar = new JProgressBar(0,140);
			progressDialogBar.setStringPainted(true);
			progressDialogBar.setPreferredSize(new Dimension(240,20));
			progressDialog.add(BorderLayout.CENTER,progressDialogBar);
			progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			progressDialog.pack();
			progressDialog.setLocationRelativeTo(LGM.frame);
			progressDialog.setResizable(false);
			}
		return progressDialog;
		}

	public static JProgressBar getProgressDialogBar()
		{
		return progressDialogBar;
		}

	public static void setProgressDialogVisible(final boolean visible)
		{
		if (progressDialog != null)
			{
			progressDialogBar.setValue(0);
			progressDialog.setVisible(visible);
			return;
			}
		getProgressDialog().setVisible(visible);
		}

	public static void setProgressTitle(String title)
		{
		progressTitle = title;
		}

	public static void setProgress(final int value, final String message)
		{
		progressDialog.setTitle(progressTitle + " - " + message); //$NON-NLS-1$
		progressDialogBar.setValue(value);
		}

	private static void createMouseCursors()
		{
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		Image cimg = LGM.getIconForKey("CursorDisplay.ZOOM").getImage(); //$NON-NLS-1$
		BufferedImage img = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.createGraphics();
		g.drawImage(cimg,0,0,null);
		zoomCursor = toolkit.createCustomCursor(img,new Point(0,0),"Zoom");

		cimg = LGM.getIconForKey("CursorDisplay.ZOOM_IN").getImage(); //$NON-NLS-1$
		img = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.drawImage(cimg,0,0,null);
		zoomInCursor = toolkit.createCustomCursor(img,new Point(0,0),"ZoomIn");

		cimg = LGM.getIconForKey("CursorDisplay.ZOOM_OUT").getImage(); //$NON-NLS-1$
		img = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.drawImage(cimg,0,0,null);
		zoomOutCursor = toolkit.createCustomCursor(img,new Point(0,0),"ZoomOut");
		}

	public static void setLookAndFeel(String LOOKANDFEEL)
		{
		if (LOOKANDFEEL.equals(themename) && !LOOKANDFEEL.equals("Custom"))
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
				// This theme is also known as Metal - Ocean
				lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel"; //$NON-NLS-1$
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				}
			else if (LOOKANDFEEL.equals("Native"))
				{
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
				}
			else if (LOOKANDFEEL.equals("Nimbus"))
				{
				lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel"; //$NON-NLS-1$
				}
			else if (LOOKANDFEEL.equals("Windows"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; //$NON-NLS-1$
				}
			else if (LOOKANDFEEL.equals("Windows Classic"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"; //$NON-NLS-1$
				// Fixes UI bug in the JDK where the buttons look way too big and get cut off.
				// https://bugs.openjdk.java.net/browse/JDK-8140527
				UIManager.put("InternalFrame.titleButtonWidth", 22); //$NON-NLS-1$
				UIManager.put("InternalFrame.titleButtonHeight", 22); //$NON-NLS-1$
				}
			else if (LOOKANDFEEL.equals("CDE/Motif"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel"; //$NON-NLS-1$
				}
			else if (LOOKANDFEEL.equals("Metal"))
				{
				lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel"; //$NON-NLS-1$
				MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
				}
			else if (LOOKANDFEEL.equals("Ocean"))
				{
				lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel"; //$NON-NLS-1$
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				}
			else if (LOOKANDFEEL.equals("GTK+"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"; //$NON-NLS-1$
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
				for (int i = 0; i < lnfs.length; i++)
					{
					if (LOOKANDFEEL.equals(lnfs[i].getName()))
						{
						lookAndFeel = lnfs[i].getClassName();
						foundMatch = true;
						}
					}

				if (!foundMatch)
					{
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
	public static void updateLookAndFeel()
		{
		if (!themechanged)
			{
			return;
			}
		SwingUtilities.updateComponentTreeUI(tree);
		if (eventDialog == null)
			{
			SwingUtilities.updateComponentTreeUI(eventSelect);
			eventSelect.updateUI();
			}
		else
			{
			SwingUtilities.updateComponentTreeUI(eventDialog);
			}
		Window windows[] = Window.getWindows();
		for (Window window : windows)
			{
			SwingUtilities.updateComponentTreeUI(window);
			}
		}

	public static ConstantsFrame getConstantsFrame()
		{
		return constantsFrame;
		}

	public static GameInformationFrame getGameInfo()
		{
		return gameInfo;
		}

	public static GameSettingFrame getGameSettings()
		{
		return gameSet;
		}

	public static ExtensionPackagesFrame getExtensionPackages()
		{
		return extSet;
		}

	public static void showConstantsFrame(final Constants cnsts)
		{
		Runnable run = new Runnable() {

			@Override
			public void run()
				{
				if (constantsFrame.res != cnsts && constantsFrame.resOriginal != cnsts) {
					constantsFrame.res = cnsts;
					constantsFrame.resOriginal = cnsts.clone();
					constantsFrame.revertResource();
				}
				constantsFrame.updateTitle();
				getConstantsFrame().setVisible(true);
				getConstantsFrame().toTop();
				}

		};
		constantsFrame.doDefaultCloseAction(run);
		}

	public static void showGameInformation()
		{
		getGameInfo().setVisible(true);
		getGameInfo().toTop();
		}

	public static void showGameSettings(final GameSettings set)
		{
		Runnable run = new Runnable() {

			@Override
			public void run()
				{
				if (gameSet.res != set && gameSet.resOriginal != set) {
					gameSet.res = set;
					gameSet.resOriginal = set.clone();
					gameSet.revertResource();
				}
				gameSet.updateTitle();
				getGameSettings().setVisible(true);
				getGameSettings().toTop();
				}

		};
		gameSet.doDefaultCloseAction(run);
		}

	public static void showExtensionPackages()
		{
		getExtensionPackages().setVisible(true);
		getExtensionPackages().toTop();
		}

	public static ImageIcon findIcon(String filename)
		{
		String custompath = Prefs.iconPath + filename;
		String jarpath = iconspath + iconspack + '/' + filename;
		String location = ""; //$NON-NLS-1$
		if (Prefs.iconPack.equals("Custom"))
		{
			if (new File(custompath).exists()) {
				location = custompath;
			} else {
				location = jarpath;
			}
		} else {
			location = jarpath;
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
			System.err.println("Unable to read icons.properties");
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
				but.setToolTipText(Messages.format("Toolbar.ADD",Resource.kindNames.get(k))); //$NON-NLS-1$
				but.addActionListener(new Listener.ResourceAdder(false,k));
				tool.add(but);
				}
		tool.addSeparator();
		tool.add(makeButton("Toolbar.CST")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.GMI")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.PKG")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("Toolbar.CONFIGURATION"))); //$NON-NLS-1$
		configsCombo = new JComboBox<GameSettings>();
		configsCombo.setModel(new DefaultComboBoxModel<GameSettings>(LGM.currentFile.gameSettings));
		configsCombo.setMaximumSize(configsCombo.getPreferredSize());
		tool.add(configsCombo);
		tool.add(makeButton("Toolbar.CONFIG_MANAGE")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(makeButton("Toolbar.GMS")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(makeButton("Toolbar.PREFERENCES")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.DOCUMENTATION")); //$NON-NLS-1$
		tool.add(Box.createHorizontalGlue()); //right align after this
		tool.add(eventButton = makeButton(new JToggleButton(),"Toolbar.EVENT_BUTTON")); //$NON-NLS-1$
		return tool;
		}

	private static JTree createTree()
		{
		return createTree(newRoot());
		}

	private static JTree createTree(ResNode newroot)
		{
		InvisibleTreeModel ml = new InvisibleTreeModel(newroot);
		ml.activateFilter(false);
		tree = new JTree(ml);

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

		return tree;
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
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		mdi.setBackground(Color.GRAY);
		return scroll;
		}

	public static void addURL(URL url) throws Exception {
		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<?> clazz = URLClassLoader.class;

		// Use reflection
		Method method= clazz.getDeclaredMethod("addURL", new Class[] { URL.class }); //$NON-NLS-1$
		method.setAccessible(true);
		method.invoke(classLoader, new Object[] { url });
	}

	public static void loadLookAndFeels()
		{
		if (workDir == null) return;

		File dir = new File(workDir,"lookandfeels"); //$NON-NLS-1$
		if (!dir.exists()) {
			dir = new File(workDir.getParent(),"lookandfeels"); //$NON-NLS-1$
		}
		File[] ps = dir.listFiles(new CustomFileFilter(null,".jar")); //$NON-NLS-1$
		if (ps == null) return;

		for (File f : ps)
			{
			if (!f.exists()) continue;
			try
				{
				addURL(f.toURI().toURL());
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
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
				JarFile jar = new JarFile(f);
				Manifest mf = jar.getManifest();
				jar.close();
				String clastr = mf.getMainAttributes().getValue(pluginEntry);
				if (clastr == null)
					throw new Exception(Messages.format("LGM.PLUGIN_MISSING_ENTRY",pluginEntry)); //$NON-NLS-1$
				URLClassLoader ucl = new URLClassLoader(new URL[] { f.toURI().toURL() });
				ucl.loadClass(clastr).newInstance();
				classLoaders.add(ucl);
				}
			catch (Exception e)
				{
				String msgInd = "LGM.PLUGIN_LOAD_ERROR"; //$NON-NLS-1$
				LGM.showDefaultExceptionHandler(new Exception(Messages.format(msgInd,f.getName()), e));
				continue;
				}
			}
		}

	public static void populateTree()
		{
		/* TODO: This method here does not give the top level nodes for Game Info, Extensions, and
		 * Settings a proper resource reference, they get null. My commented code here will give them
		 * their proper references, but when a reload happens the references are lost again.
		 * I seriously do not believe there should be nodes with null references in the tree.
		for (Class<? extends Resource<?,?>> k : Resource.kinds)
			{
				String name = Resource.kindNamesPlural.get(k);
				byte status = InstantiableResource.class.isAssignableFrom(k) ? ResNode.STATUS_PRIMARY
						: ResNode.STATUS_SECONDARY;
				if (status == ResNode.STATUS_SECONDARY) {
					SingletonResourceHolder<?> rh = (SingletonResourceHolder<?>) LGM.currentFile.resMap.get(k);
					if (rh != null) {
						Resource<?,?> res = rh.getResource();
						root.add(new ResNode(name,status,k,res.reference));
					} else {
						root.addChild(name,status,k);
					}
				} else {
					root.addChild(name,status,k);
				}
			}

		tree.setSelectionPath(new TreePath(root).pathByAddingChild(root.getChildAt(0)));
		}
		 */
		for (Class<? extends Resource<?,?>> k : Resource.kinds)
			{
			boolean hasNode = true;
			// Check to see if the appropriate node already exists.
			Enumeration<?> children = root.depthFirstEnumeration();
			while (children.hasMoreElements()) {
				ResNode it = (ResNode) children.nextElement();
				if (it.kind == k) {
					hasNode = false;
					break;
				}
			}
			if (!hasNode) continue;
			try
				{
				//NOTE: Use reflection on the class to see if it has a variable telling us whether to create
				//a node in the tree for the resource type.
				hasNode = k.getField("hasNode").getBoolean(hasNode); //$NON-NLS-1$
				}
			catch (IllegalArgumentException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			catch (NoSuchFieldException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			catch (SecurityException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			catch (IllegalAccessException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			if (!hasNode) continue;
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
			if (node.frame != null) node.frame.commitChanges(); // update open frames
			}
		LGM.getExtensionPackages().commitChanges();
		LGM.getConstantsFrame().commitChanges();
		LGM.getGameInfo().commitChanges();
		LGM.getGameSettings().commitChanges();
		}

	public static void reload(boolean newRoot)
		{
		LGM.mdi.closeAll();

		//TODO: Swing code must be executed on the Swing thread,
		//without delaying this passing a GMX to the main method
		//when launching will sometimes, about once every 5 loads,
		//throw an exception when setModel is called.
		//After several hours and days of testing I still haven't figured out why
		//the GMK reader doesn't have this but the GMX reader does, I believe because the GMX
		//reader uses more postponed references, but I can't figure out where or how it correlates. - Robert
		SwingUtilities.invokeLater(new Runnable()
			{
			@Override
			public void run()
				{
				InvisibleTreeModel ml = new InvisibleTreeModel(LGM.root);
				LGM.tree.setModel(ml);
				ml.activateFilter(Search.pruneResultsCB.isSelected());
				if (ml.isActivatedFilter())
					Search.applyFilter(root.getChildren(),ml.isActivatedFilter(),Search.filterText.getText(),false,Search.wholeWordCB.isSelected(),true);
				LGM.tree.setSelectionRow(0);
				}
			});

		// Reload the search tree so that orphaned references can be dumped.
		DefaultMutableTreeNode searchRoot = (DefaultMutableTreeNode) searchTree.getModel().getRoot();
		searchRoot.removeAllChildren();
		// Reload because root is invisible.
		((DefaultTreeModel)searchTree.getModel()).reload();

		LGM.eventSelect.reload();

		ConfigurationManager.getInstance().setConfigList(LGM.currentFile.gameSettings);
		configsCombo.setModel(new DefaultComboBoxModel<GameSettings>(LGM.currentFile.gameSettings));

		// NOTE: We do this to update the reference to the one now loaded
		// since we never close these frames, then we simply revert their controls.
		constantsFrame.res = LGM.currentFile.gameSettings.firstElement().constants;
		constantsFrame.resOriginal = LGM.currentFile.gameSettings.firstElement().constants.clone();
		constantsFrame.revertResource();
		constantsFrame.setVisible(false);
		gameInfo.res = LGM.currentFile.gameInfo;
		gameInfo.resOriginal = LGM.currentFile.gameInfo.clone();
		gameInfo.revertResource();
		gameInfo.setVisible(false);
		gameSet.res = LGM.currentFile.gameSettings.firstElement();
		gameSet.resOriginal = LGM.currentFile.gameSettings.firstElement().clone();
		gameSet.revertResource();
		gameSet.setVisible(false);

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
	static JTabbedPane treeTabs;

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
		LGM.LOADING_PROJECT = false;
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

	public static void applyPreferences() {
		if (javaVersion >= 10700 && !Prefs.locale.toLanguageTag().equals("und")) {
			Locale.setDefault(Prefs.locale);
		}

		if (Prefs.direct3DAcceleration.equals("off")) { //$NON-NLS-1$
			//java6u10 regression causes graphical xor to be very slow
			System.setProperty("sun.java2d.d3d","false"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("sun.java2d.ddscale", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (Prefs.direct3DAcceleration.equals("on")) { //$NON-NLS-1$
			System.setProperty("sun.java2d.d3d","true"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("sun.java2d.ddscale", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (Prefs.openGLAcceleration.equals("off")) { //$NON-NLS-1$
			System.setProperty("sun.java2d.opengl","false"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (Prefs.openGLAcceleration.equals("on")) { //$NON-NLS-1$
			System.setProperty("sun.java2d.opengl","true"); //$NON-NLS-1$ //$NON-NLS-2$
			//TODO: Causes JFrame's other than the main JFrame to be white on Windows 8
			//under all Look and Feels with AMD graphics card, seems to be a known issue
			//with OpenGL for Java on Windows, as usual Oracle is unlikely to fix it.
			//https://community.oracle.com/thread/1263741?start=0&tstart=0
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
				//force directx completely off
				System.setProperty("sun.java2d.noddraw","true"); //$NON-NLS-1$ //$NON-NLS-2$
				System.setProperty("sun.java2d.opengl.fbobject","false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (!Prefs.antialiasControlFont.equals("default")) { //$NON-NLS-1$
			// Set antialiasing mode
			System.setProperty("awt.useSystemAAFontSettings",Prefs.antialiasControlFont); //$NON-NLS-1$
			// if the other antialiasing option is not off then assume this one is on as well
			if (Prefs.antialiasControlFont.equals("off")) { //$NON-NLS-1$
				System.setProperty("swing.aatext","false"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				System.setProperty("swing.aatext","true"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// this is necessary to make sure open/save dialogs get fixed
		JFrame.setDefaultLookAndFeelDecorated(Prefs.decorateWindowBorders);
		JDialog.setDefaultLookAndFeelDecorated(Prefs.decorateWindowBorders);

		Window[] windows = Window.getWindows();
		LookAndFeel laf = UIManager.getLookAndFeel();
		for (Window window : windows) {
			final boolean visible = window.isVisible();
			final boolean decorate = Prefs.decorateWindowBorders && laf.getSupportsWindowDecorations();
			if (window instanceof Frame) {
				final Frame decframe = (Frame) window;
				if (decorate != decframe.isUndecorated()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run()
							{
							decframe.dispose();
							decframe.setShape(null);
							decframe.setUndecorated(decorate);
							if (decframe instanceof RootPaneContainer)
								((RootPaneContainer) decframe).getRootPane().setWindowDecorationStyle(
										decorate ? JRootPane.FRAME : JRootPane.NONE);
							decframe.setVisible(visible);
							}
					});
				}
			} else if (window instanceof Dialog) {
				final Dialog decdialog = (Dialog) window;
				if (decorate != decdialog.isUndecorated()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run()
							{
							decdialog.dispose();
							decdialog.setShape(null);
							decdialog.setUndecorated(decorate);
							if (decdialog instanceof RootPaneContainer)
								((RootPaneContainer) decdialog).getRootPane().setWindowDecorationStyle(
										decorate ? JRootPane.PLAIN_DIALOG : JRootPane.NONE);
							decdialog.setVisible(visible);
							}
					});
				}
			}
		}
	}

	public static void main(final String[] args) throws InvocationTargetException, InterruptedException
		{
		// Set the default uncaught exception handler.
		LGM.setDefaultExceptionHandler();

		LGM.applyPreferences();
		Messages.updateLangPack();

		//TODO: Should probably make these preferences as well, but I don't have a Mac to test - Robert
		//Put the Mac menu bar where it belongs (ignored by other systems)
		System.setProperty("apple.laf.useScreenMenuBar","true"); //$NON-NLS-1$ //$NON-NLS-2$
		//Set the Mac menu bar title to the correct name (also adds a useless About entry, so disabled)
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name",Messages.getString("LGM.NAME")); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.format("Java Version: %d (%s)\n",javaVersion,System.getProperty("java.version")); //$NON-NLS-1$
		if (javaVersion < 10700)
			System.out.println("Some program functionality will be limited due to your outdated Java version"); //$NON-NLS-1$

		// Load external look and feels the user has plugged in
		loadLookAndFeels();

		iconspack = Prefs.iconPack;
		setLookAndFeel(Prefs.swingTheme);
		themechanged = false;
		// must be called after setting the look and feel
		JFrame.setDefaultLookAndFeelDecorated(Prefs.decorateWindowBorders);
		JDialog.setDefaultLookAndFeelDecorated(Prefs.decorateWindowBorders);

		SplashProgress splashProgress = new SplashProgress();
		splashProgress.start();
		frame = new JFrame(Messages.format("LGM.TITLE", //$NON-NLS-1$
				Messages.getString("LGM.NEWGAME"))); //$NON-NLS-1$

		splashProgress.progress(10,Messages.getString("LGM.SPLASH_LANG")); //$NON-NLS-1$
		Messages.updateLangPack();

		splashProgress.progress(15,Messages.getString("LGM.SPLASH_CURSOR")); //$NON-NLS-1$
		createMouseCursors();

		splashProgress.progress(20,Messages.getString("LGM.SPLASH_LIBS")); //$NON-NLS-1$
		LibManager.autoLoad();

		splashProgress.progress(30,Messages.getString("LGM.SPLASH_TOOLS")); //$NON-NLS-1$
		JToolBar toolbar = createToolBar();
		tree = createTree();
		searchTree = Search.createSearchTree();

		treeTabs = new JTabbedPane();
		treeTabs.addTab(Messages.getString("TreeFilter.TAB_RESOURCES"),new JScrollPane(tree));
		treeTabs.addTab(Messages.getString("TreeFilter.TAB_SEARCHRESULTS"),new JScrollPane(searchTree));

		contents = new JPanel(new BorderLayout());
		contents.add(BorderLayout.CENTER,createMDI());
		eventSelect = new EventPanel();
		if (Prefs.dockEventPanel) {
			treeTabs.addTab(Messages.getString("TreeFilter.TAB_EVENTS"),eventSelect);
		} else {
			eventSelect.setVisible(false); // must occur after adding split
		}

		filterPanel = Search.createSearchToolbar();

		JPanel hierarchyPanel = new JPanel();
		hierarchyPanel.setLayout(new BorderLayout(0, 0));
		hierarchyPanel.add(filterPanel, BorderLayout.NORTH);
		hierarchyPanel.add(treeTabs,BorderLayout.CENTER);
		hierarchyPanel.setPreferredSize(new Dimension(320, 320));

		// could possibly be used to force the toolbar with event panel to popout
		// reducing code, i can not get it to work right however
		//((BasicToolBarUI) eventSelect.getUI()).setFloating(true, new Point(500,50));

		splashProgress.progress(40,Messages.getString("LGM.SPLASH_THREAD")); //$NON-NLS-1$

		constantsFrame = new ConstantsFrame(currentFile.defaultConstants);
		mdi.add(constantsFrame);
		gameInfo = new GameInformationFrame(currentFile.gameInfo);
		mdi.add(gameInfo);
		gameSet = new GameSettingFrame(currentFile.gameSettings.firstElement());
		mdi.add(gameSet);
		extSet = new ExtensionPackagesFrame(currentFile.extPackages);
		mdi.add(extSet);

		splashProgress.progress(50,Messages.getString("LGM.SPLASH_MENU")); //$NON-NLS-1$
		menuBar = new GmMenuBar();
		frame.setJMenuBar(menuBar);
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

		//OutputManager.initialize();

		JSplitPane verSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,contents,OutputManager.outputTabs);
		final JSplitPane horSplit;
		if (Prefs.rightOrientation) {
			horSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,verSplit,hierarchyPanel);
			horSplit.setResizeWeight(1d);
		} else {
			horSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,hierarchyPanel,verSplit);
		}
		f.add(horSplit);

		frame.setContentPane(f);
		frame.setTransferHandler(Listener.getInstance().fc.new LGMDropHandler());
		f.add(BorderLayout.NORTH,toolbar);
		f.setOpaque(true);

		splashProgress.progress(65,Messages.getString("LGM.SPLASH_LOGO")); //$NON-NLS-1$
		try
			{
			ICOFile icoFile = new ICOFile(LGM.class.getClassLoader().getResource(
					"org/lateralgm/main/lgm-logo.ico")); //$NON-NLS-1$
			frame.setIconImages(icoFile.getImages());
			}
		catch (Exception e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		// let the user specify their own background
		if (new File("lookandfeels/lgmbackground.png").exists()) {
			applyBackground("lookandfeels/lgmbackground.png"); //$NON-NLS-1$
		} else {
			applyBackground("org/lateralgm/main/lgmbackground.png"); //$NON-NLS-1$
		}
		splashProgress.progress(70,Messages.getString("LGM.SPLASH_TREE")); //$NON-NLS-1$
		populateTree();
		splashProgress.progress(80,Messages.getString("LGM.SPLASH_PLUGINS")); //$NON-NLS-1$
		LOADING_PROJECT = true;
		loadPlugins();
		splashProgress.complete();

		// remembers our window bounds and state between sessions
		new FramePrefsHandler(frame);
		// finally, set the frame visible
		frame.setVisible(true);

		// Load any projects entered on the command line
		if (args.length > 0 && args[0].length() > 0)
			{
			Listener.getInstance().fc.open(new File(args[0]));
			}
		else
			{
			LOADING_PROJECT = false;
			}
		}

	public static int getTabIndex(JTabbedPane tabs, String title) {
		for (int i = 0; i < tabs.getTabCount(); i++) {
			if (tabs.getTitleAt(i).equals(title))
				return i;
		}
		return -1;
	}

	public static void setSelectedTab(JTabbedPane tabs, String title)
		{
			if (tabs.getTitleAt(tabs.getSelectedIndex()).equals(title)) return;
			int index = getTabIndex(tabs, title);
			if (index == -1) return;
			tabs.setSelectedIndex(index);
		}

	/*
	 * TODO: This checks for changes by iterating the tree, but there is one small caveat
	 * because of the todo comment above this will not work because the top level nodes
	 * for non-instantiable resources also have a null reference. The solution for now
	 * to also make ENIGMA settings work was to iterate the resmap instead.
	 */
	public static boolean checkForChangesInTree(DefaultMutableTreeNode node) {
		Enumeration<?> e = node.children();
		while (e.hasMoreElements()){
			ResNode rnode = (ResNode) e.nextElement();
			if (rnode.status != ResNode.STATUS_SECONDARY) {
				if (checkForChangesInTree(rnode))
					return true;
			}
			if (rnode.newRes) {
				return true;
			}
			ResourceReference<?> ref = rnode.getRes();
			if (ref != null) {
				Resource<?,?> res = ref.get();
				if (res != null && res.changed) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean checkForChanges() {
		for (JInternalFrame f : mdi.getAllFrames())
		{
			if (f instanceof ResourceFrame) {
				if (((ResourceFrame<?,?>) f).resourceChanged() && f.isVisible()) {
					return true;
				}
			}
		}

		//TODO: See comment above.
		//return checkForChangesInTree(LGM.root);

		Iterator<?> it = currentFile.resMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<?,?> pairs = (Map.Entry<?,?>)it.next();
			if (pairs.getValue() instanceof ResourceList) {
				ResourceList<?> list = (ResourceList<?>) pairs.getValue();
				for (Resource<?,?> res : list) {
					if (res.changed)
						return true;
				}
			} else if (pairs.getValue() instanceof SingletonResourceHolder) {
				SingletonResourceHolder<?> rh = (SingletonResourceHolder<?>) pairs.getValue();
				Resource<?,?> res = rh.getResource();
				if (res.changed) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * When the user saves, reset all the resources to their unsaved state. We do not check the frames
	 * because they commit their changes allowing them to be written, while still allowing the user to
	 * revert the frame if they so choose.
	 * If the user has an open frame with changes basically, the save button will save the changes to
	 * file and if the user saves the frame then they will still be asked to save when they close, if
	 * they revert the changes to the frame they will exit right out. This is the expected behavior of
	 * these functions.
	 */
	public static void resetChanges() {
		Iterator<?> it = currentFile.resMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<?,?> pairs = (Map.Entry<?,?>)it.next();
			if (pairs.getValue() instanceof ResourceList) {
				ResourceList<?> list = (ResourceList<?>) pairs.getValue();
				for (Resource<?,?> res : list) {
					res.changed = false;
				}
			} else if (pairs.getValue() instanceof SingletonResourceHolder) {
				SingletonResourceHolder<?> rh = (SingletonResourceHolder<?>) pairs.getValue();
				Resource<?,?> res = rh.getResource();
				res.changed = false;
			}
		}
	}

	public static void onMainFrameClosed()
		{
		int result = JOptionPane.CANCEL_OPTION;
		try
			{
			if (!checkForChanges()) { System.exit(0); }
			result = JOptionPane.showConfirmDialog(frame,Messages.getString("LGM.KEEPCHANGES"), //$NON-NLS-1$
					Messages.getString("LGM.KEEPCHANGES_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION); //$NON-NLS-1$
			}
		catch (Throwable t)
			{
			LGM.showDefaultExceptionHandler(t);
			result = JOptionPane.showConfirmDialog(frame,Messages.getString("LGM.KEEPCHANGES_ERROR"), //$NON-NLS-1$
					Messages.getString("LGM.KEEPCHANGES_ERROR_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			}

		switch (result)
			{
			case JOptionPane.YES_OPTION:
				Listener.getInstance().fc.save(LGM.currentFile.uri,LGM.currentFile.format);
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
		eventVisible = !eventVisible;
		if (Prefs.dockEventPanel)
			{
			eventSelect.setVisible(eventVisible);
			setSelectedTab(treeTabs, Messages.getString("TreeFilter.TAB_EVENTS"));
			}
		else
			{
			if (eventDialog == null)
				{
				eventDialog = new JDialog(LGM.frame);
				eventDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				eventDialog.setIconImage(LGM.getIconForKey("Toolbar.EVENT_BUTTON").getImage());
				eventDialog.setTitle(Messages.getString("Toolbar.EVENT_BUTTON"));
				eventDialog.add(eventSelect);
				eventSelect.setVisible(true);
				eventSelect.setFloatable(false);
				eventDialog.pack();
				eventDialog.setLocationRelativeTo(frame);
				}
			eventDialog.setVisible(eventVisible);
			}
		}

	public static void hideEventPanel()
		{
		if (eventDialog != null)
			{
			eventDialog.setVisible(false);
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
		prefFrame.setVisible(true);
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
		ImageIcon bg = new ImageIcon(bgloc);
		if (bg.getIconWidth() == -1)
			{
			URL url = LGM.class.getClassLoader().getResource(bgloc);
			if (url != null)
				{
				bg = new ImageIcon(url);
				}
			}
		mdi.add(new MDIBackground(bg),JLayeredPane.FRAME_CONTENT_LAYER);
		}

	// Sets the default uncaught exception handler in case any threads forget to add one.
	public static void setDefaultExceptionHandler()
		{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
			{
				public void uncaughtException(Thread t, Throwable e)
					{
					LGM.showDefaultExceptionHandler(e);
					}
			});
		}

	// Adds a default uncaught exception handler to the current thread. This allows LGM to catch most
	// exceptions and properly display a stack trace for the user to file a bug report.
	public static void addDefaultExceptionHandler()
		{
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
			{
				public void uncaughtException(Thread t, Throwable e)
					{
					LGM.showDefaultExceptionHandler(e);
					}
			});
		}

	// Show the default uncaught exception handler dialog to the user with a stack trace they can use
	// to submit a bug report.
	public static void showDefaultExceptionHandler(Throwable e)
		{
		System.err.println(Thread.currentThread().getName() + ": ");
		e.printStackTrace();
		ErrorDialog errorDialog = ErrorDialog.getInstance();
		if (!errorDialog.isVisible())
			{
			errorDialog.setVisible(true);
			errorDialog.setDebugInfo(ErrorDialog.generateAgnosticInformation());
			}
		errorDialog.appendDebugInfo(e);
		}

	public static GameSettings getSelectedConfig()
		{
		return (GameSettings) configsCombo.getSelectedItem();
		}

	}
