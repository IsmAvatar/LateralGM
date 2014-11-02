/*
* Copyright (C) 2006-2011 IsmAvatar <IsmAvatar@gmail.com>
* Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
* Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
* Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
* Copyright (C) 2013, 2014, Robert B. Colton
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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.ActionList;
import org.lateralgm.components.CodeTextArea;
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
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.subframes.ActionFrame;
import org.lateralgm.subframes.CodeFrame;
import org.lateralgm.subframes.ConstantsFrame;
import org.lateralgm.subframes.EventPanel;
import org.lateralgm.subframes.ExtensionPackagesFrame;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;
import org.lateralgm.subframes.GmObjectFrame;
import org.lateralgm.subframes.PreferencesFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.ResourceFrame.ResourceFrameFactory;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.subframes.ScriptFrame;
import org.lateralgm.subframes.ShaderFrame;
import org.lateralgm.subframes.TimelineFrame;

public final class LGM
	{
	//TODO: This list holds the class loader for any loaded plugins which should be
	//cleaned up and closed when the application closes.
	public static ArrayList<URLClassLoader> classLoaders = new ArrayList<URLClassLoader>();
	public static boolean LOADING_PROJECT = false;
	public static JDialog progressDialog = null;
	public static JProgressBar progressDialogBar = null;
	public static String trackerURL = "https://github.com/IsmAvatar/LateralGM/issues";
	public static String iconspath = "org/lateralgm/icons/";
	public static String iconspack = "Calico";
	public static String themename = "Swing";
	// this font is used on some controls in my Quantum theme
	// for the time being to fix the GTK bug with it not
	// using the UIManager resources for font and color on controls
	// in its look and feel classes, this is Java's fault not ours
	public static Font lnfFont = new Font("Ubuntu",Font.PLAIN,14);
	public static boolean themechanged = false;

	public static int javaVersion;
	public static File tempDir, workDir;
	static
		{

		//Get Java Version
		String jv = System.getProperty("java.version"); //$NON-NLS-1$
		Scanner s = new Scanner(jv);
		s.useDelimiter("[\\._-]"); //$NON-NLS-1$
		javaVersion = s.nextInt() * 10000 + s.nextInt() * 100 + s.nextInt();
		s.close();

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
	private static ConstantsFrame constantsFrame;
	private static GameInformationFrame gameInfo;
	private static GameSettingFrame gameSet;
	private static ExtensionPackagesFrame extSet;
	public static EventPanel eventSelect;
	private static JFrame eventFrame;
	public static AbstractButton eventButton;
	public static PreferencesFrame prefFrame;
	// Window which displays the room controls
	public static JFrame roomControlsFrame;
	public static Cursor zoomCursor;
	public static Cursor zoomInCursor;
	public static Cursor zoomOutCursor;
	private static String progressTitle;
	public static GmMenuBar menuBar;
	
	public static JToolBar filterPanel;
	private static JTextField filterText;
	private static JCheckBox wholeWordCB;
	private static JCheckBox matchCaseCB;
	private static JCheckBox regexCB;
	private static JCheckBox pruneResultsCB;
	private static JButton closeButton;

	private static JTree searchTree;

	public static JDialog getProgressDialog()
		{
		if (progressDialog == null)
			{
			progressDialog = new JDialog(LGM.frame,"Progress Dialog",true);
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

	public static void setProgressDialogVisible(boolean visible)
		{
		if (!visible)
			{
			if (progressDialog != null)
				{
				progressDialog.setVisible(false);
				progressTitle = "Progress Dialog";
				progressDialogBar.setValue(0);
				}
			return;
			}
		getProgressDialog().setVisible(true);
		}

	public static void setProgressTitle(String title)
		{
		progressTitle = title;
		}

	public static void setProgress(int value, String message)
		{
		progressDialog.setTitle(progressTitle + " - " + message);
		progressDialogBar.setValue(value);
		}

	private static void createMouseCursors()
		{
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		Image cimg = LGM.getIconForKey("CursorDisplay.ZOOM").getImage();
		BufferedImage img = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.createGraphics();
		g.drawImage(cimg,0,0,null);
		zoomCursor = toolkit.createCustomCursor(img,new Point(0,0),"Zoom");

		cimg = LGM.getIconForKey("CursorDisplay.ZOOM_IN").getImage();
		img = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.drawImage(cimg,0,0,null);
		zoomInCursor = toolkit.createCustomCursor(img,new Point(0,0),"ZoomIn");

		cimg = LGM.getIconForKey("CursorDisplay.ZOOM_OUT").getImage();
		img = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.drawImage(cimg,0,0,null);
		zoomOutCursor = toolkit.createCustomCursor(img,new Point(0,0),"ZoomOut");
		}

	public static void SetLookAndFeel(String LOOKANDFEEL)
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
				lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
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
			else if (LOOKANDFEEL.equals("Windows Classic"))
				{
				lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
				//NOTE: Fixes UI bug in the JDK where the buttons look way too big and get cut off.
				UIManager.put("InternalFrame.titleButtonWidth", 20);
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
			else if (LOOKANDFEEL.equals("Ocean"))
				{
				lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
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
	public static void UpdateLookAndFeel()
		{
		if (!themechanged)
			{
			return;
			}
		SwingUtilities.updateComponentTreeUI(tree);
		if (eventFrame == null)
			{
			SwingUtilities.updateComponentTreeUI(eventSelect);
			eventSelect.updateUI();
			}
		else
			{
			SwingUtilities.updateComponentTreeUI(eventFrame);
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

	public static void showConstantsFrame()
		{
		getConstantsFrame().setVisible(true);
		getConstantsFrame().toTop();
		}

	public static void showGameInformation()
		{
		getGameInfo().setVisible(true);
		getGameInfo().toTop();
		}

	public static void showGameSettings()
		{
		getGameSettings().setVisible(true);
		getGameSettings().toTop();
		}

	public static void showExtensionPackages()
		{
		getExtensionPackages().setVisible(true);
		getExtensionPackages().toTop();
		}

	private LGM()
		{

		}
	public static ImageIcon findIcon(String filename)
		{
		String custompath = Prefs.iconPath + filename;
		String jarpath = iconspath + iconspack + "/" + filename;
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
		tool.add(makeButton("Toolbar.CST")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.GMI")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.PKG")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("Toolbar.Configurations") + ":"));
		String strs[] = { "Default" };
		JComboBox<String> configsCombo = new JComboBox<String>(strs);
		configsCombo.setMaximumSize(new Dimension(100,20));
		tool.add(configsCombo);
		tool.addSeparator();
		tool.add(makeButton("Toolbar.GMS")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(makeButton("Toolbar.PREFERENCES")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.DOCUMENTATION")); //$NON-NLS-1$
		tool.add(Box.createHorizontalGlue()); //right align after this
		tool.add(eventButton = makeButton(new JToggleButton(),"Toolbar.EVENT_BUTTON")); //$NON-NLS-1$
		if (LGM.themename.equals("Quantum"))
			{
			tool.setFont(LGM.lnfFont);
			}
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
		if (LGM.themename.equals("Quantum"))
			{
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
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mdi.setBackground(Color.GRAY);
		return scroll;
		}

	public static void addURL(URL url) throws Exception {
	  URLClassLoader classLoader
	         = (URLClassLoader) ClassLoader.getSystemClassLoader();
	  Class<?> clazz = URLClassLoader.class;
	
	  // Use reflection
	  Method method= clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
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
					throw new Exception(Messages.format("LGM.PLUGIN_MISSING_ENTRY",pluginEntry));
				URLClassLoader ucl = new URLClassLoader(new URL[] { f.toURI().toURL() });
				ucl.loadClass(clastr).newInstance();
				classLoaders.add(ucl);
				}
			catch (Exception e)
				{
				String msgInd = "LGM.PLUGIN_LOAD_ERROR"; //$NON-NLS-1$
				System.out.println(Messages.format(msgInd,f.getName(),e.getClass().getName(),e.getMessage()));
				//LGM.showDefaultExceptionHandler(e); // not sure about this one it helped me catch an error in the plugin don't know what to do really
				//TODO: lgm extensions need to be found another way perhaps with a local copy of some sort of digest or somthing like ENIGMA extensions
				continue;
				}
			}
		}

	public static void populateTree()
		{
		/* TODO: This method here does not give the top level nodes for Game Info, Extensions, and Settings
		 * a proper resource reference, they get null. My commented code here will give them there
		 * proper references, but when a reload happens the references are lost again.
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
				hasNode = k.getField("hasNode").getBoolean(hasNode);
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

		InvisibleTreeModel ml = new InvisibleTreeModel(LGM.root);
		LGM.tree.setModel(ml);
		
   	ml.activateFilter(pruneResultsCB.isSelected());
   	if (ml.isActivatedFilter()) {
 			applyFilter(root.getChildren(),ml.isActivatedFilter(),filterText.getText(),false,wholeWordCB.isSelected(),true);
   	}
		
		LGM.tree.setSelectionRow(0);
		
		// Reload the search tree so that orphaned references can be dumped.
		DefaultMutableTreeNode searchRoot = (DefaultMutableTreeNode) searchTree.getModel().getRoot();
		searchRoot.removeAllChildren();
		// Reload because root is invisible.
		((DefaultTreeModel)searchTree.getModel()).reload();

		LGM.eventSelect.reload();
		
		//NOTE: We do this to update the reference to the one now loaded
		//since we never close these frames, then we simply revert their controls.
		constantsFrame.res = LGM.currentFile.defaultConstants;
		constantsFrame.resOriginal = LGM.currentFile.defaultConstants.clone();
		constantsFrame.revertResource();
		constantsFrame.setVisible(false);
		gameInfo.res = LGM.currentFile.gameInfo;
		gameInfo.resOriginal = LGM.currentFile.gameInfo.clone();
		gameInfo.revertResource();
		gameInfo.setVisible(false);
		gameSet.res = LGM.currentFile.gameSettings;
		gameSet.resOriginal = LGM.currentFile.gameSettings.clone();
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
	
		public static class JSplitPaneExpandable extends JSplitPane {
			/**
			 * TODO: Change if needed.
			 */
			private static final long serialVersionUID = 1L;

			public boolean doubleClickExpandable = false;
			
			public MouseAdapter dividerAdapter = new MouseAdapter() {
				public void mouseReleased(MouseEvent me) {
					if (me.getClickCount() > 1) {
		      	if (getDividerLocation() <= 10) {
		      		setDividerLocation(getLastDividerLocation());
		      	} else {
		      		setLastDividerLocation(getDividerLocation());
		      		setDividerLocation(0);
		      	}
					}
				}
			};
			
			public JSplitPaneExpandable(int orientation, boolean b, JComponent first,
					JComponent second)
				{
					super(orientation, b, first, second);
				}

			public JSplitPaneExpandable(int orientation, JComponent first, JComponent second)
				{
					super(orientation, first, second);
				}

			// The purpose of this is an alternative and more standard feature found in most software to 
			// those tiny expand/collapse buttons with oneTouchExpandable.
			// * This looks much better than the trashy collapse icons.
			// * More user friendly, they can toggle the behavior much easier.
			// * Standard and found in more software applications.
			public void setDoubleClickExpandable(boolean enable) {
				if (enable && !doubleClickExpandable) {
					BasicSplitPaneUI basicSplitPaneUI = (BasicSplitPaneUI) this.getUI();
					BasicSplitPaneDivider basicSplitPaneDivider = basicSplitPaneUI.getDivider();
					basicSplitPaneDivider.addMouseListener(dividerAdapter);
				} else if (!enable && doubleClickExpandable) {
					BasicSplitPaneUI basicSplitPaneUI = (BasicSplitPaneUI) this.getUI();
					BasicSplitPaneDivider basicSplitPaneDivider = basicSplitPaneUI.getDivider();
					basicSplitPaneDivider.removeMouseListener(dividerAdapter);
				}
			}
		}

	protected static ArrayList<ReloadListener> reloadListeners = new ArrayList<ReloadListener>();
	static Action treeCopyAction = new AbstractAction("COPY") {

			 /**
				 * TODO: Change if needed.
				 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ev)
				{
				Object obj = ev.getSource();
				if (obj == null) return;
				JTree tree = null;
				if (!(obj instanceof JTree)) tree = LGM.searchTree;
				else tree = (JTree) obj;
				
				String text = "";
				int[] rows = tree.getSelectionRows();
				java.util.Arrays.sort(rows);
				for (int i = 0; i < rows.length; i++) {
					TreePath path = tree.getPathForRow(rows[i]);
					text += (i > 0 ? "\n" : "") + path.getLastPathComponent().toString().replaceAll("\\<[^>]*>","");
				}
				
			  StringSelection selection = new StringSelection(text);
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(selection, selection);
				}
		};
	private static JTabbedPane treeTabs;
	
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
	
	public static class HintTextField extends JTextField implements FocusListener {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private String hint;
	    private boolean hideOnFocus;
	    private Color color;

	    public Color getColor() {
	        return color;
	    }

	    public void setColor(Color color) {
	        this.color = color;
	        repaint();
	    }

	    public boolean isHideOnFocus() {
	        return hideOnFocus;
	    }

	    public void setHideOnFocus(boolean hideOnFocus) {
	        this.hideOnFocus = hideOnFocus;
	        repaint();
	    }

	    public String getHint() {
	        return hint;
	    }

	    public void setHint(String hint) {
	        this.hint = hint;
	        repaint();
	    }
	    
	    public HintTextField(String hint) {
	        this(hint,false);
	    }

	    public HintTextField(String hint, boolean hideOnFocus) {
	        this(hint,hideOnFocus, null);
	    }

	    public HintTextField(String hint, boolean hideOnFocus, Color color) {
	        this.hint = hint;
	        this.hideOnFocus = hideOnFocus;
	        this.color = color;
	       addFocusListener(this);
	    }

	    @Override
			public void paint(Graphics g) {
	        super.paint(g);
	        if (hint != null && getText().length() == 0 && (!(hideOnFocus && hasFocus()))){
	            if (color != null) {
	                g.setColor(color);
	            } else {
	                g.setColor(getForeground().brighter().brighter().brighter());              
	            }
	            int padding = (getHeight() - getFont().getSize())/2;
	            //g.setFont(g.getFont().deriveFont(Font.ITALIC));
	            g.drawString(hint, 2, getHeight()-padding-1);          
	        }
	    }

	    public void focusGained(FocusEvent e) {
	        if (hideOnFocus) repaint();
	    }

	    public void focusLost(FocusEvent e) {
	        if (hideOnFocus) repaint();
	    }
	}
	
	public static class InvisibleTreeModel extends DefaultTreeModel {

	  /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected boolean filterIsActive;
	
	  public InvisibleTreeModel(TreeNode root) {
	    this(root, false);
	  }
	
	  public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren) {
	    this(root, false, false);
	  }
	
	  public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren,
	      boolean filterIsActive) {
	    super(root, asksAllowsChildren);
	    this.filterIsActive = filterIsActive;
	  }
	
	  public void activateFilter(boolean newValue) {
	    filterIsActive = newValue;
	  }
	
	  public boolean isActivatedFilter() {
	    return filterIsActive;
	  }
	
	  public Object getChild(Object parent, int index) {
	    if (filterIsActive) {
	      if (parent instanceof ResNode) {
	        return ((ResNode) parent).getChildAt(index,
	            filterIsActive);
	      }
	    }
	    return ((TreeNode) parent).getChildAt(index);
	  }
	
	  public int getChildCount(Object parent) {
	    if (filterIsActive) {
	      if (parent instanceof ResNode) {
	        return ((ResNode) parent).getChildCount(filterIsActive);
	      }
	    }
	    return ((TreeNode) parent).getChildCount();
	  }
	
	}
	
  private static boolean expressionMatch(String token, String expression, boolean matchCase, boolean wholeWord) {
  	if (!matchCase) {
  		token = token.toLowerCase();
  		expression = expression.toLowerCase();
  	}
  	if (wholeWord) {
  		return token.equals(expression);
  	} else {
  		//if (expression.length() == 0) { return false; } // without this all of your folders will be open by default, we don't want to 
  		// check matches with an empty string - don't touch this as everything works so just leave it here in case I come back to it - Robert B. Colton
  		return token.contains(expression);
  	}
  }
  
  public static DefaultMutableTreeNode applyFilterRecursion(Vector<ResNode> children, boolean filter, String expression, boolean matchCase, boolean wholeWord) {
  	if (children == null) { return null; }
  	DefaultMutableTreeNode firstResult = null;
		for (ResNode child : children) {
			boolean match = expressionMatch(child.toString(), expression, matchCase, wholeWord);
			if (firstResult == null && match) {
				firstResult = child;
			}
			DefaultMutableTreeNode childResult = applyFilterRecursion(child.getChildren(), filter, expression, matchCase, wholeWord) ;
			if (firstResult == null && childResult != null) {
			//if (childResult != null) {
				firstResult = childResult;
			}
			if (childResult != null || match) {
				child.setVisible(true);
			} else {
				child.setVisible(false);
			}
		}
		return firstResult;
  }
  
	public static boolean applyFilter(Vector<ResNode> children, boolean filter, String expression, boolean matchCase, boolean wholeWord, boolean selectFirst) {
		if (children == null) { return false; }
		DefaultMutableTreeNode firstResult = applyFilterRecursion(children, filter, expression, matchCase, wholeWord);

  	if (firstResult != null && selectFirst) {
  		tree.setSelectionPath(new TreePath(firstResult.getPath()));
  		tree.updateUI();
  		return true;
  	}
  	tree.updateUI();
  	return false;
  }
	
	public static boolean searchFilter(ResNode child, String expression, boolean matchCase, boolean wholeWord, boolean backwards) {
		ResNode firstResult = null;
		while (child != null) {
			if (backwards) {
				child = (ResNode) child.getPreviousNode();
			} else {
				child = (ResNode) child.getNextNode();
			}
			if (child == null) break;
			boolean match = expressionMatch(child.toString(), expression, matchCase, wholeWord);
			if (firstResult == null && match) {
				firstResult = child;
				break;
			}
		}
		if (firstResult != null) {
			tree.setSelectionPath(new TreePath(firstResult.getPath()));
			tree.updateUI();
			//tree.expandPath(new TreePath(firstResult.getPath()));
			return true;
		}
		return false;
	}
	public static class MatchBlock {
	  public String content;
	  public boolean highlighted;
	  MatchBlock(String content, boolean highlighted) {
	    this.content = content;
	    this.highlighted = highlighted;
	  }
	}
	public static class LineMatch {
	  public int lineNum;
	  public List<MatchBlock> matchedText = new ArrayList<MatchBlock>();
		public String toHighlightableString()
			{
			boolean enablehtml = Prefs.highlightResultMatchBackground || Prefs.highlightResultMatchForeground;
			String text = (enablehtml ? "<html>" : "") + lineNum + ": ";
			for (MatchBlock block : matchedText) {
				if (block.highlighted && enablehtml) {
					text += "<span";
					if (Prefs.highlightResultMatchBackground) {
						text += " bgcolor='" + Util.getHTMLColor(Prefs.resultMatchBackgroundColor,false) + "'";
					}
					if (Prefs.highlightResultMatchForeground) {
						text += " color='" + Util.getHTMLColor(Prefs.resultMatchForegroundColor,false) + "'";
					}
					text += ">";
				}
				text += block.content;
				if (block.highlighted && enablehtml) {
					text += "</span>";
				}
			}
			if (enablehtml) {
				text += "</html>";
			}
			return text;
			}
	}
	
	private static String formatMatchCountText(String pretext, int matches) {
		boolean enablehtml = Prefs.highlightMatchCountBackground || Prefs.highlightMatchCountForeground;
		String text = (enablehtml ? "<html>" : "") + pretext + " " + (enablehtml ? "<font" : "");
		if (Prefs.highlightMatchCountBackground) {
			text += " bgcolor='" + Util.getHTMLColor(Prefs.matchCountBackgroundColor,false) + "'";
		}
		if (Prefs.highlightMatchCountForeground) {
			text += " color='" + Util.getHTMLColor(Prefs.matchCountForegroundColor,false) + "'";
		}
		text += (enablehtml ? ">" : "") + "(" + matches + " " + Messages.getString("TreeFilter.MATCHES") + ")" + (enablehtml ? "</font></html>" : "");
	
		return text;
	}
	
	private static final Pattern NEWLINE = Pattern.compile("\r\n|\r|\n");
	static List<LineMatch> getMatchingLines(String code, Pattern content) {
	  List<LineMatch> res = new ArrayList<LineMatch>();
	  Matcher m = content.matcher(code), nl = NEWLINE.matcher(code);
	  // code editor starts at line 0 so we need to here as well
	  int lineNum = 0, lineAt = 0, lastEnd = -1;
	  LineMatch lastMatch = null;
	  while (m.find()) {
	    nl.region(lineAt, m.start());
	    int firstSkippedLineAt = lineAt;
	    if (nl.find()) {
	      firstSkippedLineAt = nl.start();
	      lineAt = nl.end();
	      ++lineNum;
	      while (nl.find()) {
	        ++lineNum;
	        lineAt = nl.end();
	      }
	    }
	    if (lastMatch != null) {
	      // We have to add the rest of the line to the old match, either way.
	      // And if we're matching on the same line, we add that match, too.
	      if (lineNum == lastMatch.lineNum) {
	        lastMatch.matchedText.add(new MatchBlock(code.substring(lastEnd, m.start()), false));
	        lastMatch.matchedText.add(new MatchBlock(code.substring(m.start(), m.end()), true));
	      } else {
	        lastMatch.matchedText.add(
	            new MatchBlock(code.substring(lastEnd, firstSkippedLineAt), false));
	      }
	    }
	    if (lastMatch == null || lineNum != lastMatch.lineNum) {
	      lastMatch = new LineMatch();
	      lastMatch.lineNum = lineNum;
	      if (m.start() > lineAt) {
	        lastMatch.matchedText.add(new MatchBlock(code.substring(lineAt, m.start()), false));
	      }
	      lastMatch.matchedText.add(new MatchBlock(code.substring(m.start(), m.end()), true));
	      res.add(lastMatch);
	    }
	    lastEnd = m.end();
	  }
	  if (lastMatch != null) {
	    nl.region(lastEnd, code.length());
	    int indTo = (nl.find())? nl.start() : code.length();
	    lastMatch.matchedText.add(new MatchBlock(code.substring(lastEnd, indTo), false));
	  }
	  return res;
	}
	
	
	/*
	public static void assertEquals(Object obj1, Object obj2) {
		if (obj1.equals(obj2)) {
			Debug.println("assertEquals: ",obj1.toString() + "," + obj2.toString());
		} else {
			Debug.println("assertEquals: ","false");
		}
	}

  public static void testThing() {
          String CODE = "runatestinatestwith\nsomemoretestsandthen\nyou'redone";
          List<LineMatch> match  = getMatchingLines(CODE, Pattern.compile("test"));
          LineMatch[] matches = (LineMatch[]) match.toArray(new LineMatch[match.size()]);
          assertEquals(2, matches.length);
          assertEquals(5, matches[0].matchedText.size());
          assertEquals("runa",     matches[0].matchedText.get(0).content);
          assertEquals("test",     matches[0].matchedText.get(1).content);
          assertEquals("ina",      matches[0].matchedText.get(2).content);
          assertEquals("test",     matches[0].matchedText.get(3).content);
          assertEquals("with",     matches[0].matchedText.get(4).content);
          assertEquals(3, matches[1].matchedText.size());
          assertEquals("somemore", matches[1].matchedText.get(0).content);
          assertEquals("test",     matches[1].matchedText.get(1).content);
          assertEquals("sandthen", matches[1].matchedText.get(2).content);
  }
  */
	
	public static void buildSearchHierarchy(ResNode resNode, SearchResultNode resultRoot) {
		DefaultMutableTreeNode searchNode = (DefaultMutableTreeNode) LGM.searchTree.getModel().getRoot();
		if (resNode == null) { searchNode.add(resultRoot); return; }
		TreeNode[] paths = resNode.getPath();
		// start at 1 because we don't want to copy the root
		// subtract 1 so we don't consider the node itself
		for (int n = 1; n < paths.length - 1; n++) {
			ResNode pathNode = (ResNode) paths[n];
			boolean found = false;
			for (int y = 0; y < searchNode.getChildCount(); y++) {
			 DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) searchNode.getChildAt(y);
			 if (childNode.getUserObject() == pathNode.getUserObject()) {
			 		searchNode = childNode; found = true; break;
			 }
			}
			if (!found) {
				SearchResultNode newSearchNode = new SearchResultNode(pathNode.getUserObject());
				newSearchNode.status = pathNode.status;
				searchNode.add(newSearchNode);
				searchNode = newSearchNode;
			}
			if (pathNode == resNode.getParent()) {
				searchNode.insert(resultRoot, searchNode.getChildCount() + resNode.getDepth());
			}
		}
	}
	
	
	public static void searchInResourcesRecursion(DefaultMutableTreeNode node, Pattern pattern) {
		int numChildren = node.getChildCount();
	  for (int i = 0; i < numChildren; ++i) {
	  	DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			if (child instanceof ResNode) {
				ResNode resNode = (ResNode)child;
				if (resNode.status != ResNode.STATUS_SECONDARY) {
					searchInResourcesRecursion(child, pattern);
				} else {
					SearchResultNode resultRoot = null;
					ResourceReference<?> ref = resNode.getRes();
					
					if (ref != null) {
						Resource<?,?> resderef = ref.get();
						if (resNode.frame != null) {
							resNode.frame.commitChanges();
							resderef = resNode.frame.res;
						}
						
						if (resNode.kind == Script.class)	{
							Script res = (Script) resderef;
							String code = res.getCode();
							List<LineMatch> matches = getMatchingLines(code, pattern);
							if (matches.size() > 0) {
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(),matches.size()));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
								for (LineMatch match : matches) {
									if (match.matchedText.size() > 0) {
										String text = match.toHighlightableString();
										
										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.status = SearchResultNode.STATUS_RESULT;
										resultNode.data = new Object[]{match.lineNum};
										resultRoot.add(resultNode);
									}
								}
							}
						} else if (resNode.kind == Shader.class) {
							Shader res = (Shader) resderef;
							String vcode = res.getVertexCode();
							String fcode = res.getFragmentCode();
							List<LineMatch> vertexmatches = getMatchingLines(vcode, pattern);
							List<LineMatch> fragmentmatches = getMatchingLines(fcode, pattern);
							if (vertexmatches.size() + fragmentmatches.size() > 0) {
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(),vertexmatches.size() + fragmentmatches.size()));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
								
								SearchResultNode resultGroupNode = new SearchResultNode(formatMatchCountText(
										Messages.getString("TreeFilter.VERTEX_CODE") + ":",vertexmatches.size()));
								resultGroupNode.status = SearchResultNode.STATUS_VERTEX_CODE;
								resultRoot.add(resultGroupNode);
								for (LineMatch match : vertexmatches) {
									if (match.matchedText.size() > 0) {
										String text = match.toHighlightableString();
										
										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.status = SearchResultNode.STATUS_RESULT;
										resultNode.data = new Object[]{match.lineNum};
										resultGroupNode.add(resultNode);
									}
								}
								
								resultGroupNode = new SearchResultNode(formatMatchCountText(
										Messages.getString("TreeFilter.FRAGMENT_CODE") + ":",fragmentmatches.size()));
								resultGroupNode.status = SearchResultNode.STATUS_FRAGMENT_CODE;
								resultRoot.add(resultGroupNode);
								for (LineMatch match : fragmentmatches) {
									if (match.matchedText.size() > 0) {
										String text = match.toHighlightableString();
										
										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.status = SearchResultNode.STATUS_RESULT;
										resultNode.data = new Object[]{match.lineNum};
										resultGroupNode.add(resultNode);
									}
								}
							}
						} else if (resNode.kind == GmObject.class) {
							GmObject res = (GmObject) resderef;
							
							ArrayList<SearchResultNode> meNodes = new ArrayList<>();
							int matchCount = 0;
							for (MainEvent me : res.mainEvents) {
								ArrayList<SearchResultNode> evNodes = new ArrayList<>();
								int meMatches = 0;
								int mainid = 0;
								for (Event ev : me.events) {
									mainid = ev.mainId;
									ArrayList<SearchResultNode> actionNodes = new ArrayList<>();
									int evMatches = 0;
									List<org.lateralgm.resources.sub.Action> actions = ev.actions;
									for (int ii = 0; ii < actions.size(); ii++) {
										org.lateralgm.resources.sub.Action act = actions.get(ii);
										ArrayList<SearchResultNode> resultNodes = new ArrayList<>();
										int actMatches = 0;
										List<Argument> args = act.getArguments();
										for (int iii = 0; iii < args.size(); iii++) {
											Argument arg = args.get(iii);
											String code = arg.getVal();
											List<LineMatch> matches = getMatchingLines(code, pattern);
											actMatches += matches.size();
											for (LineMatch match : matches) {
												if (match.matchedText.size() > 0) {
													String text = match.toHighlightableString();

													SearchResultNode resultNode = null;
													if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE) {
														boolean enablehtml = Prefs.highlightResultMatchBackground || Prefs.highlightResultMatchForeground;
														text = ((enablehtml ? "<html>" : "") + Integer.toString(iii) + text.substring(text.indexOf(":"),text.length()));
														resultNode = new SearchResultNode(text);
														resultNode.data = new Object[]{iii};
													} else {
														resultNode = new SearchResultNode(text);
														resultNode.data = new Object[]{match.lineNum};
													}
													
													resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
													resultNode.status = SearchResultNode.STATUS_RESULT;
													resultNodes.add(resultNode);
												}
											}
										}
										evMatches += actMatches;
										if (resultNodes.size() > 0) {
											// Uses the same method of getting the Action name as ActionFrame
											SearchResultNode actRoot = new SearchResultNode(formatMatchCountText(act.getLibAction().name.replace("_"," "),actMatches));
											actRoot.status = SearchResultNode.STATUS_ACTION;
											actRoot.data = new Object[]{ii};
											actRoot.setIcon(new ImageIcon(act.getLibAction().actImage.getScaledInstance(16,16,0)));
											for (SearchResultNode actn : resultNodes) {
												actRoot.add(actn);
											}
											actionNodes.add(actRoot);
										}
									}
									meMatches += evMatches;
									if (actionNodes.size() > 0) {
										SearchResultNode evRoot = new SearchResultNode(formatMatchCountText(ev.toString().replaceAll("<","&lt;").replaceAll(">","&gt;"), evMatches));
										evRoot.status = SearchResultNode.STATUS_EVENT;
										evRoot.setIcon(LGM.getIconForKey("EventNode.EVENT" + ev.mainId));
										evRoot.data = new Object[]{ev.mainId,ev.id};
										for (SearchResultNode resn : actionNodes) {
											evRoot.add(resn);
										}
										evNodes.add(evRoot);
									}
								}
								matchCount += meMatches;
								if (evNodes.size() > 0) {
									if (evNodes.size() > 1) {
										SearchResultNode meRoot = new SearchResultNode(formatMatchCountText(Messages.getString("MainEvent.EVENT" + mainid),meMatches));
										meRoot.status = SearchResultNode.STATUS_MAIN_EVENT;
										meRoot.setIcon(LGM.getIconForKey("EventNode.GROUP" + mainid));
										for (SearchResultNode resn : evNodes) {
											meRoot.add(resn);
										}
										meNodes.add(meRoot);
									} else {
										for (SearchResultNode resn : evNodes) {
											meNodes.add(resn);
										}
									}
								}
							}
							
							if (meNodes.size() > 0) {
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(), matchCount));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
							}
							
							for (SearchResultNode resn : meNodes) {
								resultRoot.add(resn);
							}
	
						} else if (resNode.kind == Timeline.class) {
							Timeline res = (Timeline) resderef;
							
							ArrayList<SearchResultNode> momentNodes = new ArrayList<>();
							int matchCount = 0;
							for (Moment mom : res.moments) {
								ArrayList<SearchResultNode> actionNodes = new ArrayList<>();
								int momentMatches = 0;
								List<org.lateralgm.resources.sub.Action> actions = mom.actions;
								for (int ii = 0; ii < actions.size(); ii++) {
									org.lateralgm.resources.sub.Action act = actions.get(ii);
									ArrayList<SearchResultNode> resultNodes = new ArrayList<>();
									int actMatches = 0;
									List<Argument> args = act.getArguments();
									for (int iii = 0; iii < args.size(); iii++) {
										Argument arg = args.get(iii);
										String code = arg.getVal();
										List<LineMatch> matches = getMatchingLines(code, pattern);
										actMatches += matches.size();
										for (LineMatch match : matches) {
											if (match.matchedText.size() > 0) {
												String text = match.toHighlightableString();
	
												SearchResultNode resultNode = null;
												if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE) {
													boolean enablehtml = Prefs.highlightResultMatchBackground || Prefs.highlightResultMatchForeground;
													text = ((enablehtml ? "<html>" : "") + Integer.toString(iii) + text.substring(text.indexOf(":"),text.length()));
													resultNode = new SearchResultNode(text);
													resultNode.data = new Object[]{iii};
												} else {
													resultNode = new SearchResultNode(text);
													resultNode.data = new Object[]{match.lineNum};
												}
												
												resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
												resultNode.status = SearchResultNode.STATUS_RESULT;
												resultNodes.add(resultNode);
											}
										}
									}
								
									momentMatches += actMatches;
									if (resultNodes.size() > 0) {
										// Uses the same method of getting the Action name as ActionFrame
										SearchResultNode actRoot = new SearchResultNode(formatMatchCountText(act.getLibAction().name.replace("_"," "), actMatches));
										actRoot.status = SearchResultNode.STATUS_ACTION;
										actRoot.data = new Object[]{ii};
										actRoot.setIcon(new ImageIcon(act.getLibAction().actImage.getScaledInstance(16,16,0)));
										for (SearchResultNode actn : resultNodes) {
											actRoot.add(actn);
										}
										actionNodes.add(actRoot);
									}

								}
								matchCount += momentMatches;
								if (actionNodes.size() > 0) {
									SearchResultNode momentRoot = new SearchResultNode(formatMatchCountText(mom.toString(),momentMatches));
									momentRoot.status = SearchResultNode.STATUS_MOMENT;
									momentRoot.data = new Object[]{mom.stepNo};
									momentRoot.setIcon(null);
									for (SearchResultNode resn : actionNodes) {
										momentRoot.add(resn);
									}
									momentNodes.add(momentRoot);
								}
							}
							
							if (momentNodes.size() > 0) {
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(), matchCount));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
							}
							
							for (SearchResultNode momn : momentNodes) {
								resultRoot.add(momn);
							}
		
						} else if (resNode.kind == Room.class) {
							Room res = (Room) resderef;
							
							ArrayList<SearchResultNode> resultNodes = new ArrayList<>();
							int matchCount = 0;
							
							String code = res.getCode();
							List<LineMatch> matches = getMatchingLines(code, pattern);
							matchCount += matches.size();
							ArrayList<SearchResultNode> codeNodes = new ArrayList<>();
							for (LineMatch match : matches) {
								if (match.matchedText.size() > 0) {
									String text = match.toHighlightableString();
									
									SearchResultNode resultNode = new SearchResultNode(text);
									resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
									resultNode.status = SearchResultNode.STATUS_RESULT;
									resultNode.data = new Object[]{match.lineNum};
									codeNodes.add(resultNode);
								}
							}
							
							if (codeNodes.size() > 0) {
								SearchResultNode resultNode = new SearchResultNode(formatMatchCountText(Messages.getString("TreeFilter.CREATION_CODE"), matches.size()));
								resultNode.setIcon(null);
								resultNode.status = SearchResultNode.STATUS_ROOM_CREATION;
								resultNodes.add(resultNode);
								
								for (SearchResultNode codeNode : codeNodes) {
									resultNode.add(codeNode);
								}
							}
							
							for (Instance inst : res.instances) {
								code = inst.getCode();
								matches = getMatchingLines(code, pattern);
								matchCount += matches.size();
								codeNodes = new ArrayList<>();
								for (LineMatch match : matches) {
									if (match.matchedText.size() > 0) {
										String text = match.toHighlightableString();
										
										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.data = new Object[]{match.lineNum};
										resultNode.status = SearchResultNode.STATUS_RESULT;
										codeNodes.add(resultNode);
									}
								}
								
								if (codeNodes.size() > 0) {
									SearchResultNode resultNode = new SearchResultNode(formatMatchCountText(
											Messages.getString("TreeFilter.INSTANCE") + " " + inst.getID(), matches.size()));

									Resource<?,?> obj = ((ResourceReference<?>)inst.properties.get(PInstance.OBJECT)).get();
									resultNode.setIcon(obj == null ? null : obj.getNode().getIcon());
									resultNode.status = SearchResultNode.STATUS_INSTANCE_CREATION;
									resultNode.data = new Object[]{inst.getID()};
									resultNodes.add(resultNode);
									
									for (SearchResultNode codeNode : codeNodes) {
										resultNode.add(codeNode);
									}
								}
							}
							
							if (resultNodes.size() > 0) {
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(),matchCount));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
							}
							
							for (SearchResultNode resultNode : resultNodes) {
								resultRoot.add(resultNode);
							}
						}
						
						if (resultRoot != null) {
							TreeNode[] paths = resNode.getPath();
							DefaultMutableTreeNode searchNode = (DefaultMutableTreeNode) LGM.searchTree.getModel().getRoot();
							// start at 1 because we don't want to copy the root
							// subtract 1 so we don't consider the node itself
							for (int n = 1; n < paths.length - 1; n++) {
								ResNode pathNode = (ResNode) paths[n];
								boolean found = false;
								for (int y = 0; y < searchNode.getChildCount(); y++) {
								 DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) searchNode.getChildAt(y);
								 if (childNode.getUserObject() == pathNode.getUserObject()) {
								 		searchNode = childNode; found = true; break;
								 }
								}
								if (!found) {
									SearchResultNode newSearchNode = new SearchResultNode(pathNode.getUserObject());
									newSearchNode.status = pathNode.status;
									searchNode.add(newSearchNode);
									searchNode = newSearchNode;
								}
								if (pathNode == resNode.getParent()) {
									searchNode.add(resultRoot);
								}
							}
							
						}
					}
				}
			}
		}	
	}
	
	public static void searchInResources(DefaultMutableTreeNode node, String expression, boolean regex, boolean matchCase, boolean wholeWord) {
		DefaultMutableTreeNode searchRoot = (DefaultMutableTreeNode) searchTree.getModel().getRoot();
		searchRoot.removeAllChildren();
		Pattern pattern = Pattern.compile(wholeWord? "\\b" + Pattern.quote(expression) + "\\b" : regex? expression : Pattern.quote(expression), matchCase? 0 : Pattern.CASE_INSENSITIVE);
		searchInResourcesRecursion(node, pattern);
		// Reload because root is invisible.
		((DefaultTreeModel)searchTree.getModel()).reload();
	}
	
	static class SearchResultNode extends DefaultMutableTreeNode {
		/**
		 * TODO: Change if needed.
		 */
		private static final long serialVersionUID = 1L;
		public static final byte STATUS_RESULT = 4;
		public static final byte STATUS_MAIN_EVENT = 5;
		public static final byte STATUS_EVENT = 6;
		public static final byte STATUS_MOMENT = 7;
		public static final byte STATUS_VERTEX_CODE = 8;
		public static final byte STATUS_FRAGMENT_CODE = 9;
		public static final byte STATUS_ACTION = 10;
		public static final byte STATUS_ROOM_CREATION = 11;
		public static final byte STATUS_INSTANCE_CREATION = 12;
		
		public byte status;
		ResourceReference<?> ref;
		private Icon icon = null;
		
		Object[] data;
		Object[] parentdata;
		
		public SearchResultNode()
			{
				super();
			}
		
		public SearchResultNode(Object text)
			{
			super(text);
			}

		public void setIcon(Icon ico) {
			icon = ico;
		}
		
		public ResourceReference<?> getRef() {
			SearchResultNode par = (SearchResultNode) this.getParent();
			ResourceReference<?> ret = par.ref;
			while (ret == null) {
				par = (SearchResultNode) par.getParent();
				ret = par.ref;
			}
			return ret;
		}
		
		public void threadCaretUpdate(final CodeTextArea code, int row, int col) {
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run()
					{
						code.setCaretPosition((Integer)data[0],0);
						code.text.centerCaret();
						code.repaint();
					}
			});
		}
		
		public void openFrame() {
			if (status >= STATUS_RESULT) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getParent();
				if (node instanceof SearchResultNode) {
					SearchResultNode parNode = (SearchResultNode) node;
					parNode.openFrame();
					
					ResourceReference<?> parentRef = this.getRef();
					if (parentRef != null) {
						Resource<?,?> res = parentRef.get();
						if (res != null) {
							ResNode resNode = res.getNode();
							if (resNode != null) {
								ResourceFrame<?,?> frame = resNode.frame;
								if (frame != null) {
									if (resNode.kind == GmObject.class) {
										GmObjectFrame objframe = (GmObjectFrame) frame;
										if (status == STATUS_EVENT) {
											objframe.setSelectedEvent((Integer)data[0],(Integer)data[1]);
										} else if (status == STATUS_ACTION) {
											//TODO: There is a bug here where if the user deletes the action
											//and then selects it again from the search results tree it may open
											//and in fact scope a different action in the list because we have
											//no unique reference to the action we want to open, and we can't
											//because the frame may have been closed. May possibly lead to an NPE
											objframe.actions.setSelectedIndex((Integer)data[0]);
											org.lateralgm.resources.sub.Action act = objframe.actions.getSelectedValue();
											if (act != null) {
												ActionFrame af = (ActionFrame) ActionList.openActionFrame(objframe,act);
												parentdata = new Object[]{af};
											}
										} else if (status == STATUS_RESULT && parNode.status == STATUS_ACTION) {
											ActionFrame af = (ActionFrame) parNode.parentdata[0];
											org.lateralgm.resources.sub.Action act = af.getAction();
											if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE) {
												af.focusArgumentComponent((Integer)data[0]);
											} else {
												if ((Integer)data[0] < af.code.getLineCount()) {
													threadCaretUpdate(af.code, (Integer)data[0],0);
												}
											}
										}
									} else if (resNode.kind == Timeline.class) {
										TimelineFrame tmlframe = (TimelineFrame) frame;
										if (status == STATUS_MOMENT) {
											tmlframe.setSelectedMoment((Integer)data[0]);
										} else if (status == STATUS_ACTION) {
											tmlframe.actions.setSelectedIndex((Integer)data[0]);
											org.lateralgm.resources.sub.Action act = tmlframe.actions.getSelectedValue();
											if (act != null) {
												ActionFrame af = (ActionFrame) ActionList.openActionFrame(tmlframe,act);
												parentdata = new Object[]{af};
											}
										} else if (status == STATUS_RESULT && parNode.status == STATUS_ACTION) {
											ActionFrame af = (ActionFrame) parNode.parentdata[0];
											org.lateralgm.resources.sub.Action act = af.getAction();
											if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE) {
												af.focusArgumentComponent((Integer)data[0]);
											} else {
												if ((Integer)data[0] < af.code.getLineCount()) {
													threadCaretUpdate(af.code, (Integer)data[0],0);
												}
											}
										}
									} else if (resNode.kind == Room.class) {
										RoomFrame roomframe = (RoomFrame) frame;
										if (status == STATUS_INSTANCE_CREATION) {
											roomframe.tabs.setSelectedIndex(0);
											parentdata = new Object[]{roomframe.openInstanceCodeFrame((Integer)data[0], true)};
										} else if (status == STATUS_ROOM_CREATION) {
											roomframe.tabs.setSelectedIndex(1);
											parentdata = new Object[]{roomframe.openRoomCreationCode()};
										} else if (status == STATUS_RESULT) {
											if (parNode.status == STATUS_INSTANCE_CREATION || parNode.status == STATUS_ROOM_CREATION) {
												CodeFrame cf = (CodeFrame) parNode.parentdata[0];
												if ((Integer)data[0] < cf.code.getLineCount()) {
													threadCaretUpdate(cf.code, (Integer)data[0],0);
												}
											}
										}
									} else if (resNode.kind == Script.class) {
										final ScriptFrame scrframe = (ScriptFrame) frame;
										if (status == STATUS_RESULT) {
											if ((Integer)data[0] < scrframe.code.text.getLineCount()) {
												threadCaretUpdate(scrframe.code, (Integer)data[0],0);
											}
										}
									} else if (resNode.kind == Shader.class) {
										ShaderFrame shrframe = (ShaderFrame) frame;
										if (status == STATUS_VERTEX_CODE) {
											shrframe.editors.setSelectedIndex(0);
										} else if (status == STATUS_FRAGMENT_CODE) {
											shrframe.editors.setSelectedIndex(1);
										} else if (status == STATUS_RESULT) {
											if (parNode.status == STATUS_VERTEX_CODE) {
												shrframe.vcode.requestFocusInWindow();
												if ((Integer)data[0] < shrframe.vcode.text.getLineCount()) {
													threadCaretUpdate(shrframe.vcode, (Integer)data[0],0);
												}
											} else if (parNode.status == STATUS_FRAGMENT_CODE) {
												shrframe.fcode.requestFocusInWindow();
												if ((Integer)data[0] < shrframe.fcode.text.getLineCount()) {
													threadCaretUpdate(shrframe.fcode, (Integer)data[0],0);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} else if (status == ResNode.STATUS_SECONDARY) {
				if (ref != null) {
					Resource<?,?> res = ref.get();
					if (res != null) {
						ResNode node = res.getNode();
						if (node != null) {
							node.openFrame();
						}
					}
				}
			}
		}
	}
	
	public static class SearchResultsRenderer extends DefaultTreeCellRenderer
	{
		SearchResultNode last;
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (value instanceof SearchResultNode) {
				last = (SearchResultNode) value;
			}
			Component com = super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
			
			// Bold primary nodes
			if (value instanceof SearchResultNode && com instanceof JLabel) {
				SearchResultNode rn = (SearchResultNode) value;
				JLabel label = (JLabel) com;
				if (rn.status == ResNode.STATUS_PRIMARY) {
					label.setText("<html><b>" + label.getText() + "</b></html>");
				}
			}
	
			return com;
		}
		
		public Icon getLeafIcon()
		{
			if (last != null) {
				Icon icon = last.icon;
				if (icon != null) return icon;
			}
			return null;
		}

		public Icon getClosedIcon()
		{
			if (last != null) {
				if (last.status == ResNode.STATUS_PRIMARY || last.status == ResNode.STATUS_GROUP) {
					return LGM.getIconForKey("GmTreeGraphics.GROUP");
				} else {
					Icon icon = last.icon;
					if (icon != null) return icon;
				}
			}
			return null;
		}

		public Icon getOpenIcon()
		{
			if (last != null) {
				if (last.status == ResNode.STATUS_PRIMARY || last.status == ResNode.STATUS_GROUP) {
					return LGM.getIconForKey("GmTreeGraphics.GROUP_OPEN");
				} else {
					Icon icon = last.icon;
					if (icon != null) return icon;
				}
			}
			return null;
		}
	}

	public static void main(final String[] args)
		{

		// Set the default uncaught exception handler.
		LGM.setDefaultExceptionHandler();

	//TODO: I have a feeling some of these should just be preferences, especially Direct3D hardware acceleration.
		
		//java6u10 regression causes graphical xor to be very slow
		System.setProperty("sun.java2d.d3d","false"); //$NON-NLS-1$ //$NON-NLS-2$
		//Put the Mac menu bar where it belongs (ignored by other systems)
		System.setProperty("apple.laf.useScreenMenuBar","true"); //$NON-NLS-1$ //$NON-NLS-2$
		//Set the Mac menu bar title to the correct name (also adds a useless About entry, so disabled)
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name",Messages.getString("LGM.NAME")); //$NON-NLS-1$ //$NON-NLS-2$

		// Enable antialasing of fonts
		System.setProperty("awt.useSystemAAFontSettings",Prefs.antialiasControlFont);
		System.setProperty("swing.aatext","true");

		System.out.format("Java Version: %d (%s)\n",javaVersion,System.getProperty("java.version")); //$NON-NLS-1$
		if (javaVersion < 10600)
			System.out.println("Some program functionality will be limited due to your outdated Java version"); //$NON-NLS-1$

		// Load external look and feels the user has plugged in
		loadLookAndFeels();
		
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
		treeTabs = new JTabbedPane();
		tree = createTree();
		DefaultMutableTreeNode sroot = new DefaultMutableTreeNode("root");
		//sroot.add(new DefaultMutableTreeNode("cock"));
		
		searchTree = new JTree(sroot);

		// Create tree context menu
		final JPopupMenu searchMenu = new JPopupMenu();
		JMenuItem expandAllItem = new JMenuItem(Messages.getString("TreeFilter.EXPANDALL"));
		expandAllItem.setIcon(LGM.getIconForKey("TreeFilter.EXPANDALL"));
		expandAllItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.EXPANDALL")));
		expandAllItem.addActionListener(new ActionListener() {
			public void expandChildren(JTree tree, DefaultMutableTreeNode node) {
				Enumeration<?> children = node.children();
				DefaultMutableTreeNode it = null;
				while (children.hasMoreElements()) {
					it = (DefaultMutableTreeNode) children.nextElement();
					tree.expandPath(new TreePath(it.getPath()));
					if (it.getChildCount() > 0) {
						expandChildren(tree, it);
					}
				}
			}
			public void actionPerformed(ActionEvent ev)
				{
					expandChildren(LGM.searchTree,(DefaultMutableTreeNode) LGM.searchTree.getModel().getRoot());
				}
		});
		searchMenu.add(expandAllItem);
		JMenuItem collapseAllItem = new JMenuItem(Messages.getString("TreeFilter.COLLAPSEALL"));
		collapseAllItem.setIcon(LGM.getIconForKey("TreeFilter.COLLAPSEALL"));
		collapseAllItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.COLLAPSEALL")));
		collapseAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev)
				{
					//NOTE: The code for expanding all nodes does not work here because collapsing a child node
				  //will expand its parent, so you have to do it in reverse. For now I will just reload the tree.
					((DefaultTreeModel)searchTree.getModel()).reload();
				}
		});
		searchMenu.add(collapseAllItem);
		searchMenu.addSeparator();
		JMenuItem copyItem = new JMenuItem();
		copyItem.setAction(treeCopyAction);
		copyItem.setText(Messages.getString("TreeFilter.COPY"));
		copyItem.setIcon(LGM.getIconForKey("TreeFilter.COPY"));
		copyItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.COPY")));

    searchTree.getActionMap().put("COPY", treeCopyAction);
    searchTree.getInputMap().put(copyItem.getAccelerator(), "COPY");
    // Add it to the main tree as well to remove HTML formatting
    tree.getActionMap().put("COPY", treeCopyAction);
    tree.getInputMap().put(copyItem.getAccelerator(), "COPY");
    
		searchMenu.add(copyItem);
		searchMenu.addSeparator();
		JMenuItem selectAllItem = new JMenuItem(Messages.getString("TreeFilter.SELECTALL"));
		
		selectAllItem.setIcon(LGM.getIconForKey("TreeFilter.SELECTALL"));
		selectAllItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.SELECTALL")));
		//NOTE: It's possible to grab the trees built in Select All action.
		//selectAllItem.setAction(searchTree.getActionMap().get(searchTree.getInputMap().get(selectAllItem.getAccelerator())));
		
		selectAllItem.addActionListener(new ActionListener() {
		public void selectAllChildren(JTree tree, DefaultMutableTreeNode node) {
			Enumeration<?> children = node.children();
			DefaultMutableTreeNode it = null;
			while (children.hasMoreElements()) {
				it = (DefaultMutableTreeNode) children.nextElement();
				tree.addSelectionPath(new TreePath(it.getPath()));
				if (tree.isExpanded(new TreePath(it.getPath()))) {
					selectAllChildren(tree, it);
				}
			}
		}
		public void actionPerformed(ActionEvent ev)
			{
				selectAllChildren(LGM.searchTree,(DefaultMutableTreeNode) LGM.searchTree.getModel().getRoot());
			}
		});
		searchMenu.add(selectAllItem);

		searchTree.setToggleClickCount(0); // we only want to expand on double click with group nodes, not result nodes
		searchTree.setCellRenderer(new SearchResultsRenderer());
		searchTree.setRootVisible(false);
		searchTree.setShowsRootHandles(true);
		searchTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		searchTree.addMouseListener(new MouseAdapter() {
	    public void mouseReleased(MouseEvent me) {
	    	TreePath path = LGM.searchTree.getPathForLocation(me.getX(),me.getY());
	    	
	    	boolean inpath = false;
	    	if (path != null) {
					//Check to see if we have clicked on a different node then the one
					//currently selected.
					TreePath[] paths = LGM.searchTree.getSelectionPaths();
					
	
					if (paths != null)
						{
						for (int i = 0; i < paths.length; i++)
							{
							if (paths[i].equals(path))
								{
								inpath = true;
								}
							}
						}
					
					if (me.getModifiers() == InputEvent.BUTTON1_MASK && inpath)
						{
						LGM.searchTree.setSelectionPath(path);
						}
	    	}
				//Isn't Java supposed to handle ctrl+click for us? For some reason it doesn't.
				if (me.getModifiers() == InputEvent.BUTTON3_MASK && me.getClickCount() == 1)
					{
					// Yes the right click button does change the selection,
					// go ahead and experiment with Eclipse, CodeBlocks, Visual Studio
					// or Qt. Swing's default component popup listener does not do this
					// indicating it is an inconsistency with the framework compared to
					// other GUI libraries.
					if (!inpath && path != null)
						{
						LGM.searchTree.setSelectionPath(path);
						}
					searchMenu.show((Component)me.getSource(), me.getX(), me.getY());
					return;
					}
				
				if (path == null) return;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node == null) return;
				if (me.getModifiers() == InputEvent.BUTTON1_MASK && me.getClickCount() >= 2 && ((me.getClickCount() & 1) == 0)) {
					if (node instanceof SearchResultNode) {
						SearchResultNode srn = (SearchResultNode) node;
						
						if (srn.status >= ResNode.STATUS_SECONDARY) {
							srn.openFrame();
							return;
						} else {
							if (LGM.searchTree.isExpanded(path)) {
								LGM.searchTree.collapsePath(path);
							} else {
								LGM.searchTree.expandPath(path);
							}
						}
					} else {
						if (LGM.searchTree.isExpanded(path)) {
							LGM.searchTree.collapsePath(path);
						} else {
							LGM.searchTree.expandPath(path);
						}
					}
				}
	    }
	  });
		content = new JPanel(new BorderLayout());
		content.add(BorderLayout.CENTER,createMDI());
		content.setPreferredSize(new Dimension(640, 640));
		eventSelect = new EventPanel();

		// could possibly be used to force the toolbar with event panel to popout
		// reducing code, i can not get it to work right however
		//((BasicToolBarUI) eventSelect.getUI()).setFloating(true, new Point(500,50));

		splashProgress.progress(40,Messages.getString("LGM.SPLASH_THREAD")); //$NON-NLS-1$

		constantsFrame = new ConstantsFrame(currentFile.defaultConstants);
		mdi.add(constantsFrame);
		gameInfo = new GameInformationFrame(currentFile.gameInfo);
		mdi.add(gameInfo);
		gameSet = new GameSettingFrame(currentFile.gameSettings);
		mdi.add(gameSet);
		extSet = new ExtensionPackagesFrame(currentFile.extPackages);
		mdi.add(extSet);

		splashProgress.progress(50,Messages.getString("LGM.SPLASH_MENU")); //$NON-NLS-1$
		frame = new JFrame(Messages.format("LGM.TITLE", //$NON-NLS-1$
				Messages.getString("LGM.NEWGAME"))); //$NON-NLS-1$
		menuBar = new GmMenuBar();
		if (LGM.themename.equals("Quantum"))
			{
			menuBar.setFont(lnfFont);
			}
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

		final JFrame filterSettings = new JFrame();
		
		filterSettings.setIconImage(LGM.getIconForKey("TreeFilter.ICON").getImage());
		filterSettings.setTitle(Messages.getString("TreeFilter.TITLE"));
		filterSettings.setResizable(false);
		
    wholeWordCB = new JCheckBox(Messages.getString("TreeFilter.WHOLEWORD"));
		wholeWordCB.addItemListener(new ItemListener() {
		  public void itemStateChanged(ItemEvent e) {
		  	InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
		   	applyFilter(root.getChildren(),ml.isActivatedFilter(),filterText.getText(),matchCaseCB.isSelected(),wholeWordCB.isSelected(),false);
		  }
		});
  	regexCB = new JCheckBox(Messages.getString("TreeFilter.REGEX"));
		regexCB.addItemListener(new ItemListener() {
		  public void itemStateChanged(ItemEvent e) {
		  	InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
		   	applyFilter(root.getChildren(),ml.isActivatedFilter(),filterText.getText(),matchCaseCB.isSelected(),wholeWordCB.isSelected(),false);
		  }
		});
  	matchCaseCB = new JCheckBox(Messages.getString("TreeFilter.MATCHCASE"));
		matchCaseCB.addItemListener(new ItemListener() {
		  public void itemStateChanged(ItemEvent e) {
		  	InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
		   	applyFilter(root.getChildren(),ml.isActivatedFilter(),filterText.getText(),matchCaseCB.isSelected(),wholeWordCB.isSelected(),false);
		  }
		});
  	pruneResultsCB = new JCheckBox(Messages.getString("TreeFilter.PRUNERESULTS"));
		pruneResultsCB.addItemListener(new ItemListener() {
		  public void itemStateChanged(ItemEvent e) {
		  	InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
		   	ml.activateFilter(pruneResultsCB.isSelected());
	   		applyFilter(root.getChildren(),ml.isActivatedFilter(),filterText.getText(),false,wholeWordCB.isSelected(),false);
		  }
		});
  	closeButton = new JButton(Messages.getString("TreeFilter.CLOSE"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
				{
					filterSettings.setVisible(false);
				}
		});
		
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		panel.setLayout(gl);
		filterSettings.getContentPane().setLayout(new GridBagLayout());
		filterSettings.add(panel);
		
		gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
		/**/.addGroup(gl.createSequentialGroup()
		/* */.addGroup(gl.createParallelGroup()
		/*  */.addComponent(wholeWordCB)
		/*  */.addComponent(matchCaseCB))
		/* */.addGroup(gl.createParallelGroup()
		/*  */.addComponent(regexCB)
		/*  */.addComponent(pruneResultsCB)))
		/**/.addComponent(closeButton));
		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/* */.addGroup(gl.createSequentialGroup()
		/*  */.addComponent(wholeWordCB)
		/*  */.addComponent(matchCaseCB))
		/* */.addGroup(gl.createSequentialGroup()
		/*  */.addComponent(regexCB)
		/*  */.addComponent(pruneResultsCB)))
		/**/.addComponent(closeButton));
		
		
		filterSettings.pack();
		filterSettings.setSize(280, 140);
		filterSettings.setLocationRelativeTo(LGM.frame);

  	filterText = new HintTextField(Messages.getString("TreeFilter.SEARCHFOR"),true);
    
    JButton prevButton = new JButton(LGM.getIconForKey("TreeFilter.PREV"));
    prevButton.setToolTipText(Messages.getString("TreeFilter.PREV"));
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				searchFilter((ResNode)tree.getLastSelectedPathComponent(),filterText.getText(),
						matchCaseCB.isSelected(), wholeWordCB.isSelected(), true);
			}
		});
    
    JButton nextButton = new JButton(LGM.getIconForKey("TreeFilter.NEXT"));
    nextButton.setToolTipText(Messages.getString("TreeFilter.NEXT"));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				searchFilter((ResNode)tree.getLastSelectedPathComponent(), filterText.getText(),
						matchCaseCB.isSelected(), wholeWordCB.isSelected(), false);
			}
		});
		
    JButton searchInButton = new JButton(LGM.getIconForKey("TreeFilter.SEARCHIN"));
    searchInButton.setToolTipText(Messages.getString("TreeFilter.SEARCHIN"));
    searchInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
	  		if (filterText.getText().length() <= 0) return;
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				searchInResources((DefaultMutableTreeNode) ml.getRoot(), filterText.getText(), regexCB.isSelected(), 
						matchCaseCB.isSelected(), wholeWordCB.isSelected());
				setSelectedTab(treeTabs, Messages.getString("TreeFilter.TAB_SEARCHRESULTS"));
			}
		});
		
    JButton setButton = new JButton(LGM.getIconForKey("TreeFilter.SET"));
    setButton.setToolTipText(Messages.getString("TreeFilter.SET"));
		setButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				filterSettings.setVisible(true);
			}
		});
    
		filterText.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {

		  }
		  public void removeUpdate(DocumentEvent e) {
		  	InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
		   	if (ml.isActivatedFilter()) {
		   		applyFilter(root.getChildren(),ml.isActivatedFilter(),filterText.getText(),false,wholeWordCB.isSelected(),true);
		   	} else {
		   		searchFilter(root, filterText.getText(), matchCaseCB.isSelected(), wholeWordCB.isSelected(), false);
		   	}
		  }
		  
		  public void insertUpdate(DocumentEvent e) {
		  	InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
		   	if (ml.isActivatedFilter()) {
		   		applyFilter(root.getChildren(),ml.isActivatedFilter(),filterText.getText(),false,wholeWordCB.isSelected(),true);
		   	} else {
		   		searchFilter(root, filterText.getText(), matchCaseCB.isSelected(), wholeWordCB.isSelected(), false);
		   	}
		  }
		});
		
		filterText.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent evt) {
	  	if (filterText.getText().length() <= 0) return;
			InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
			searchInResources((DefaultMutableTreeNode) ml.getRoot(), filterText.getText(), regexCB.isSelected(), 
					matchCaseCB.isSelected(), wholeWordCB.isSelected());
			setSelectedTab(treeTabs, Messages.getString("TreeFilter.TAB_SEARCHRESULTS"));
	  }
		});
		
		// Use a toolbar so that the buttons render like tool buttons and smaller.
		filterPanel = new JToolBar();
		
		// Use a custom layout so that the filterText control will stretch horizontally under all Look and Feels.
		GroupLayout filterLayout = new GroupLayout(filterPanel);
		
		filterLayout.setHorizontalGroup(filterLayout.createSequentialGroup()
		/**/.addComponent(filterText)
		/**/.addComponent(prevButton)
		/**/.addComponent(nextButton)
		/**/.addComponent(searchInButton)
		/**/.addComponent(setButton));
		
		filterLayout.setVerticalGroup(filterLayout.createParallelGroup()
		/**/.addComponent(filterText)
		/**/.addComponent(prevButton)
		/**/.addComponent(nextButton)
		/**/.addComponent(searchInButton)
		/**/.addComponent(setButton));
		
		filterPanel.setLayout(filterLayout);
		filterPanel.setFloatable(true);
		filterPanel.setVisible(Prefs.showTreeFilter);

		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(250,100));
		scroll.setAlignmentX(JScrollPane.RIGHT_ALIGNMENT);
		
		//TODO: DO NOT remove this line, believe it or not it fixes a look and feel bug when you switch to
		//nimbus and back to native on Windows.
		treeTabs.setFont(treeTabs.getFont().deriveFont(Font.BOLD));
		treeTabs.addTab(Messages.getString("TreeFilter.TAB_RESOURCES"),scroll);
		scroll = new JScrollPane(searchTree);
		scroll.setPreferredSize(new Dimension(250,100));
		scroll.setAlignmentX(JScrollPane.RIGHT_ALIGNMENT);
		treeTabs.addTab(Messages.getString("TreeFilter.TAB_SEARCHRESULTS"),scroll);
		if (Prefs.dockEventPanel) {
			treeTabs.addTab(Messages.getString("TreeFilter.TAB_EVENTS"),eventSelect);
		} else {
			eventSelect.setVisible(false); //must occur after adding split
		}
		
    JPanel hierarchyPanel = new JPanel();
    hierarchyPanel.setLayout(new BorderLayout(0, 0));
    hierarchyPanel.add(filterPanel, BorderLayout.NORTH);
    hierarchyPanel.add(treeTabs,BorderLayout.CENTER);
    
    //OutputManager.initialize();
		
		JSplitPaneExpandable verSplit = new JSplitPaneExpandable(JSplitPane.VERTICAL_SPLIT,true,content,OutputManager.outputTabs);
		verSplit.setDoubleClickExpandable(true);
		final JSplitPaneExpandable horSplit = new JSplitPaneExpandable(JSplitPane.HORIZONTAL_SPLIT,true,hierarchyPanel,verSplit);
		horSplit.setDividerLocation(320);
		horSplit.setDoubleClickExpandable(true);
		f.add(horSplit);

		frame.setContentPane(f);
		frame.setTransferHandler(Listener.getInstance().fc.new LGMDropHandler());
		f.add(BorderLayout.NORTH,toolbar);
		f.setOpaque(true);

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

		frame.setVisible(true);
		frame.pack();
		// This needs to be here after the frame is set to visible for some reason,
		// it was causing the bug with the frame not memorizing its maximized state.
		new FramePrefsHandler(frame);

		// Load any projects entered on the command line
		if (args.length > 0 && args[0].length() > 1)
			{
			String path = args[0].replace("\\","/");
			URI uri = new File(path).toURI();
			Listener.getInstance().fc.open(uri);
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
	 * When the user saves reset all the resources to their unsaved state. We do not check the frames
	 * because they commit their changes allowing them to be written, while still allowing the user to revert
	 * the frame if they so choose.
	 * If the user has an open frame with changes basically, the save button will save the changes to file
	 * and if the user saves the frame then they will still be asked to save when they close, if they revert
	 * the changes to the frame they will exit right out. This is the expected behavior of these functions.
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
	
	public static void askToSaveProject()
		{
		FileChooser fc = new FileChooser();
		fc.save(LGM.currentFile.uri,LGM.currentFile.format);
		}

	public static void onMainFrameClosed()
		{
		if (!checkForChanges()) { System.exit(0); }
		int n = JOptionPane.showConfirmDialog(null,Messages.getString("LGM.KEEPCHANGES_MESSAGE"),
				Messages.getString("LGM.KEEPCHANGES_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,null);

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
			setSelectedTab(treeTabs, Messages.getString("TreeFilter.TAB_EVENTS"));
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

	// Adds a default uncaught exception handler to the current thread. This allows LGM to catch most exceptions
	// and properly display a stack trace for the user to file a bug report.
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

	// Show the default uncaught exception handler dialog to the user with a stack trace they can use to submit a bug report.
	public static void showDefaultExceptionHandler(Throwable e)
		{
		System.out.println(Thread.currentThread().getName() + ": " + e);
		e.printStackTrace();
		if (!ErrorDialog.getInstance().isVisible())
			{
			ErrorDialog.getInstance().setVisible(true);
			ErrorDialog.getInstance().setDebugInfo(ErrorDialog.generateAgnosticInformation());
			}
		ErrorDialog.getInstance().appendDebugInfo(e); //$NON-NLS-1$
		}

	}