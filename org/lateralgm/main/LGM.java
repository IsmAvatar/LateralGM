/*
 * Copyright (C) 2006-2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
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
import org.lateralgm.file.GmFile;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.subframes.EventFrame;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;

public final class LGM
	{
	private static final long serialVersionUID = 1L;
	public static int javaVersion;
	public static File tempDir, workDir;
	static
		{
		//java6u10 regression causes graphical xor to be very slow
		System.setProperty("sun.java2d.d3d","false"); //$NON-NLS-1$ //$NON-NLS-2$
		//Put the Mac menu bar where it belongs (ignored by other systems)
		System.setProperty("apple.laf.useScreenMenuBar","true"); //$NON-NLS-1$ //$NON-NLS-2$
		//Set the Mac menu bar title to the correct name (also adds a useless About entry, so disabled)
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name",Messages.getString("LGM.NAME")); //$NON-NLS-1$ //$NON-NLS-2$
		//annoyingly, Metal bolds almost all components by default. This unbolds them.
		UIManager.put("swing.boldMetal",Boolean.FALSE); //$NON-NLS-1$

		//Get Java Version
		String jv = System.getProperty("java.version"); //$NON-NLS-1$
		Scanner s = new Scanner(jv).useDelimiter("[\\._-]"); //$NON-NLS-1$
		javaVersion = s.nextInt() * 10000 + s.nextInt() * 100 + s.nextInt();
		System.out.format("Java Version: %d (%s)\n",javaVersion,jv); //$NON-NLS-1$
		if (javaVersion < 10600)
			System.out.println("Some program functionality will be limited due to your outdated version"); //$NON-NLS-1$

		SplashProgress.start();

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

		try
			{
			workDir = new File(LGM.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			}
		catch (URISyntaxException e1)
			{
			e1.printStackTrace();
			}
		}
	public static JFrame frame = new JFrame(Messages.format("LGM.TITLE", //$NON-NLS-1$
			Messages.getString("LGM.NEWGAME"))); //$NON-NLS-1$
	public static Listener listener = new Listener();
	public static JToolBar tool;
	public static JTree tree;
	public static ResNode root;
	public static GmFile currentFile = new GmFile();
	public static MDIPane mdi;
	public static Thread gameInformationFrameBuilder;
	private static GameInformationFrame gameInfo;
	public static Thread gameSettingFrameBuilder;
	private static GameSettingFrame gameSet;
	public static EventFrame eventSelect;

	public static GameInformationFrame getGameInfo()
		{
		try
			{
			gameInformationFrameBuilder.join();
			}
		catch (InterruptedException e)
			{
			}
		return gameInfo;
		}

	public static GameSettingFrame getGameSettings()
		{
		try
			{
			gameSettingFrameBuilder.join();
			}
		catch (InterruptedException e)
			{
			}
		return gameSet;
		}

	private LGM()
		{
		}

	public static ImageIcon findIcon(String filename)
		{
		String location = "org/lateralgm/icons/" + filename; //$NON-NLS-1$
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
		but.addActionListener(listener);
		return but;
		}

	private static JToolBar createToolBar()
		{
		tool = new JToolBar();
		tool.setFloatable(false);
		tool.add(makeButton("LGM.NEW")); //$NON-NLS-1$
		tool.add(makeButton("LGM.OPEN")); //$NON-NLS-1$
		tool.add(makeButton("LGM.SAVE")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(makeButton("LGM.SAVEAS")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(makeButton("Toolbar.ADD_SPRITE")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_SOUND")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_BACKGROUND")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_PATH")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_SCRIPT")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_FONT")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_TIMELINE")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_OBJECT")); //$NON-NLS-1$
		tool.add(makeButton("Toolbar.ADD_ROOM")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(makeButton("LGM.EVENT_BUTTON")); //$NON-NLS-1$
		return tool;
		}

	private static JComponent createTree()
		{
		return createTree(newRoot());
		}

	private static JComponent createTree(ResNode newroot)
		{
		tree = new JTree(new DefaultTreeModel(root));
		GmTreeGraphics renderer = new GmTreeGraphics();
		GmTreeEditor editor = new GmTreeEditor(tree,renderer);
		editor.addCellEditorListener(listener);
		tree.setEditable(true);
		tree.addMouseListener(listener.mListener);
		if (javaVersion >= 10600)
			{
			tree.setTransferHandler(listener);
			tree.setDragEnabled(true);
			tree.setDropMode(DropMode.ON_OR_INSERT);
			}
		tree.setCellRenderer(renderer);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellEditor(editor);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

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
		scroll.setPreferredSize(new Dimension(200,100));
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
		mdi.setBackground(Color.BLACK);
		eventSelect = new EventFrame();
		mdi.add(eventSelect);
		return scroll;
		}

	public static void loadPlugins()
		{
		File dir = new File(workDir.getParent(),"plugins"); //$NON-NLS-1$
		if (!dir.exists()) dir = new File(workDir.getParent(),"Plugins"); //$NON-NLS-1$
		File[] ps = dir.listFiles(new CustomFileFilter(null,".jar")); //$NON-NLS-1$s
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
		root.addChild(Messages.getString("LGM.SPRITES"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.SPRITE);
		root.addChild(Messages.getString("LGM.SOUNDS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.SOUND);
		root.addChild(Messages.getString("LGM.BACKGROUNDS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.BACKGROUND);
		root.addChild(Messages.getString("LGM.PATHS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.PATH);
		root.addChild(Messages.getString("LGM.SCRIPTS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.SCRIPT);
		root.addChild(Messages.getString("LGM.FONTS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.FONT);
		root.addChild(Messages.getString("LGM.TIMELINES"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.TIMELINE);
		root.addChild(Messages.getString("LGM.OBJECTS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.OBJECT);
		root.addChild(Messages.getString("LGM.ROOMS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.Kind.ROOM);
		root.addChild(Messages.getString("LGM.GAMEINFO"), //$NON-NLS-1$
				ResNode.STATUS_SECONDARY,Resource.Kind.GAMEINFO);
		root.addChild(Messages.getString("LGM.GAMESETTINGS"), //$NON-NLS-1$
				ResNode.STATUS_SECONDARY,Resource.Kind.GAMESETTINGS);
		root.addChild(Messages.getString("LGM.EXTENSIONS"), //$NON-NLS-1$
				ResNode.STATUS_SECONDARY,Resource.Kind.EXTENSIONS);
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

		//This hack ensures EventSelector.linkSelect knows of the new root
		LGM.mdi.remove(LGM.eventSelect);
		LGM.eventSelect = new EventFrame();
		LGM.mdi.add(LGM.eventSelect);

		LGM.getGameSettings().setComponents(LGM.currentFile.gameSettings);
		LGM.getGameSettings().setVisible(false);
		LGM.getGameInfo().setComponents(LGM.currentFile.gameInfo);
		LGM.getGameInfo().setVisible(false);

		LGM.fireReloadPerformed(newRoot);
		}

	public static interface ReloadListener
		{
		/**
		 * Called after LGM performs a reload, e.g. when a new file is created or loaded.
		 * A reload causes the MDI to be flushed, the tree to refresh
		 * (especially if a new root is provided), the EventFrame is recreated
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

	public static void main(String[] args)
		{
		SplashProgress.progress(20,Messages.getString("LGM.SPLASH_LIBS")); //$NON-NLS-1$
		LibManager.autoLoad();
		SplashProgress.progress(30,Messages.getString("LGM.SPLASH_TOOLS")); //$NON-NLS-1$
		JComponent toolbar = createToolBar();
		JComponent left = createTree();
		JComponent right = createMDI();
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,left,right);
		split.setDividerLocation(170);
		split.setOneTouchExpandable(true);
		SplashProgress.progress(40,Messages.getString("LGM.SPLASH_THREAD")); //$NON-NLS-1$
		gameInformationFrameBuilder = new Thread()
			{
				public void run()
					{
					gameInfo = new GameInformationFrame(currentFile.gameInfo);
					mdi.add(gameInfo);
					}
			};
		gameSettingFrameBuilder = new Thread()
			{
				public void run()
					{
					gameSet = new GameSettingFrame(currentFile.gameSettings,currentFile.constants,
							currentFile.includes);
					mdi.add(gameSet);
					}
			};
		gameInformationFrameBuilder.start(); //must occur after createMDI
		gameSettingFrameBuilder.start(); //must occur after createMDI
		SplashProgress.progress(50,Messages.getString("LGM.SPLASH_MENU")); //$NON-NLS-1$
		frame.setJMenuBar(new GmMenuBar());
		SplashProgress.progress(60,Messages.getString("LGM.SPLASH_UI")); //$NON-NLS-1$
		JPanel f = new JPanel(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(f);
		f.add(BorderLayout.NORTH,toolbar);
		f.add(BorderLayout.CENTER,split);
		f.setOpaque(true);
		new FramePrefsHandler(frame);
		SplashProgress.progress(70,Messages.getString("LGM.SPLASH_LOGO")); //$NON-NLS-1$
		try
			{
			frame.setIconImage(ImageIO.read(LGM.class.getClassLoader().getResource(
					"org/lateralgm/main/lgm-logo.png"))); //$NON-NLS-1$
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		SplashProgress.progress(80,Messages.getString("LGM.SPLASH_TREE")); //$NON-NLS-1$
		populateTree();
		SplashProgress.progress(90,Messages.getString("LGM.SPLASH_PLUGINS")); //$NON-NLS-1$
		loadPlugins();
		if (args.length != 0)
			{
			SplashProgress.progress(95,Messages.getString("LGM.SPLASH_PRELOAD")); //$NON-NLS-1$
			listener.fc.openFile(new File(args[0]));
			}
		SplashProgress.complete();
		frame.setVisible(true);
		}

	static final class SplashProgress
		{
		static final SplashScreen SPLASH;
		static final Graphics2D SPLASH_GRAPHICS;
		static final JProgressBar BAR;
		static final Graphics BAR_GRAPHICS;

		private static String text = null;

		static final boolean TIMER = System.getProperty("lgm.progresstimer") != null; //$NON-NLS-1$
		private static long startTime, completeTime;
		private static ArrayList<Integer> progressValues;
		private static ArrayList<Long> progressTimes;

		static
			{
			SplashScreen ss = null;
			Graphics2D sg = null;
			JProgressBar b = null;
			Graphics bg = null;
			try
				{
				ss = SplashScreen.getSplashScreen();
				sg = ss.createGraphics();
				Dimension sss = ss.getSize();
				Rectangle bb = new Rectangle(0,sss.height - 24,sss.width,24);
				b = new JProgressBar();
				b.setBounds(bb);
				bg = sg.create(bb.x,bb.y,bb.width,bb.height);
				}
			catch (Throwable t)
				{
				ss = null;
				sg = null;
				b = null;
				bg = null;
				}
			finally
				{
				SPLASH = ss;
				SPLASH_GRAPHICS = sg;
				BAR = b;
				BAR_GRAPHICS = bg;
				}
			if (TIMER)
				{
				progressValues = new ArrayList<Integer>();
				progressTimes = new ArrayList<Long>();
				}
			}

		private SplashProgress()
			{
			}

		static void start()
			{
			if (TIMER) startTime = System.currentTimeMillis();
			progress(0,Messages.getString("LGM.SPLASH_START")); //$NON-NLS-1$
			}

		static void complete()
			{
			if (TIMER)
				{
				completeTime = System.currentTimeMillis();
				long tt = completeTime - startTime;
				System.out.print("Progress/%       "); //$NON-NLS-1$
				for (Integer v : progressValues)
					{
					System.out.print("\t" + v); //$NON-NLS-1$
					}
				System.out.println();
				System.out.print("Time/ms          "); //$NON-NLS-1$
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

		static void progress(int p)
			{
			progress(p,text);
			}

		static void progress(int p, String t)
			{
			if (TIMER)
				{
				progressValues.add(p);
				progressTimes.add(System.currentTimeMillis() - startTime);
				}
			text = t;
			if (SPLASH != null)
				{
				BAR.setValue(p);
				BAR.setStringPainted(t != null);
				BAR.setString(t);
				update();
				}
			}

		private static void update()
			{
			BAR.paint(BAR_GRAPHICS);
			SPLASH.update();
			}
		}
	}
