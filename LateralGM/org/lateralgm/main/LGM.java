/*
 * Copyright (C) 2006, 2007, 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2006, 2007, 2008 Clam <ebordin@aapt.net.au>
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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
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
	static
		{
		try
			{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// TODO At some point, add LAF as an option
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		UIManager.put("swing.boldMetal",Boolean.FALSE); //$NON-NLS-1$
		String jv = System.getProperty("java.version"); //$NON-NLS-1$
		Scanner s = new Scanner(jv).useDelimiter("[\\._]"); //$NON-NLS-1$
		javaVersion = s.nextInt() * 10000 + s.nextInt() * 100 + s.nextInt();
		System.out.format("Java Version: %d (%s)\n",javaVersion,jv); //$NON-NLS-1$
		if (javaVersion < 10600)
			System.out.println("Some program functionality will be limited due to your outdated version"); //$NON-NLS-1$
		SplashProgress.start();
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
	public static File tempDir, workDir;

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
		if (filename != "") //$NON-NLS-1$
			return findIcon(filename);
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

	private static void createToolBar(JPanel f)
		{
		tool = new JToolBar();
		tool.setFloatable(false);
		f.add("North",tool); //$NON-NLS-1$
		tool.add(makeButton("LGM.NEW")); //$NON-NLS-1$
		tool.add(makeButton("LGM.OPEN")); //$NON-NLS-1$
		tool.add(makeButton("LGM.SAVE")); //$NON-NLS-1$
		tool.add(new JToolBar.Separator());
		tool.add(makeButton("LGM.SAVEAS")); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(makeButton("LGM.EVENT_BUTTON")); //$NON-NLS-1$
		}

	public static void populateTree()
		{
		root.addChild(Messages.getString("LGM.SPRITES"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.SPRITE);
		root.addChild(Messages.getString("LGM.SOUNDS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.SOUND);
		root.addChild(Messages.getString("LGM.BACKGROUNDS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.BACKGROUND);
		root.addChild(Messages.getString("LGM.PATHS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.PATH);
		root.addChild(Messages.getString("LGM.SCRIPTS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.SCRIPT);
		root.addChild(Messages.getString("LGM.FONTS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.FONT);
		root.addChild(Messages.getString("LGM.TIMELINES"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.TIMELINE);
		root.addChild(Messages.getString("LGM.OBJECTS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.GMOBJECT);
		root.addChild(Messages.getString("LGM.ROOMS"), //$NON-NLS-1$
				ResNode.STATUS_PRIMARY,Resource.ROOM);
		root.addChild(Messages.getString("LGM.GAMEINFO"), //$NON-NLS-1$
				ResNode.STATUS_SECONDARY,Resource.GAMEINFO);
		root.addChild(Messages.getString("LGM.GAMESETTINGS"), //$NON-NLS-1$
				ResNode.STATUS_SECONDARY,Resource.GAMESETTINGS);
		tree.setSelectionPath(new TreePath(root).pathByAddingChild(root.getChildAt(0)));
		}

	private static void createTree(JPanel f, boolean populate)
		{
		createTree(f,new ResNode("Root",(byte) 0,(byte) 0,null),populate); //$NON-NLS-1$
		}

	private static void createTree(JPanel f, ResNode newroot, boolean populate)
		{
		root = newroot;
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
		if (populate)
			populateTree();
		else
			tree.setSelectionRow(0);

		// Setup the rest of the main window
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(200,100));
		mdi = new MDIPane();
		JScrollPane scroll2 = new JScrollPane(mdi);
		mdi.setScrollPane(scroll2);
		scroll2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,scroll,scroll2);
		split.setDividerLocation(170);
		f.add(split);
		mdi.setBackground(Color.BLACK);
		eventSelect = new EventFrame();
		mdi.add(eventSelect);
		}

	public static void loadPlugins()
		{
		File dir = new File(workDir.getParent(),"plugins"); //$NON-NLS-1$
		if (!dir.exists()) dir = new File(workDir.getParent(),"Plugins"); //$NON-NLS-1$
		File[] ps = dir.listFiles(new CustomFileFilter(".jar",null)); //$NON-NLS-1$
		if (ps == null) return;
		for (File f : ps)
			{
			if (!f.exists()) continue;
			try
				{
				Manifest mf = new JarFile(f).getManifest();
				String clastr = mf.getMainAttributes().getValue("LGM-Plugin"); //$NON-NLS-1$
				URLClassLoader ucl = new URLClassLoader(new URL[] { f.toURI().toURL() });
				ucl.loadClass(clastr).newInstance();
				}
			catch (Exception e)
				{
				String err = "Unable to load plugin: %s: %s: %s"; //$NON-NLS-1$
				System.out.format(err,f.toString(),e.getCause(),e.getMessage());
				continue;
				}
			}
		}

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

	static
		{
		Util.tweakIIORegistry();
		tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "lgm"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!tempDir.exists())
			{
			tempDir.mkdir();
			tempDir.setReadable(true,false);
			tempDir.setWritable(true,false);
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

	public static void main(String[] args)
		{
		System.out.println(workDir.isDirectory());
		SplashProgress.progress(20,Messages.getString("LGM.SPLASH_LIBS")); //$NON-NLS-1$
		LibManager.autoLoad();
		SplashProgress.progress(30,Messages.getString("LGM.SPLASH_UI")); //$NON-NLS-1$
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SplashProgress.progress(40,Messages.getString("LGM.SPLASH_TOOLBAR")); //$NON-NLS-1$
		JPanel f = new JPanel(new BorderLayout());
		createToolBar(f);
		SplashProgress.progress(50,Messages.getString("LGM.SPLASH_TREE")); //$NON-NLS-1$
		createTree(f,true);
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
					gameSet = new GameSettingFrame();
					mdi.add(gameSet);
					}
			};
		gameInformationFrameBuilder.start(); //must occur after MDI created in createTree
		gameSettingFrameBuilder.start(); //must occur after MDI created in createTree
		SplashProgress.progress(60,Messages.getString("LGM.SPLASH_MENU")); //$NON-NLS-1$
		frame.setJMenuBar(new GmMenuBar());
		SplashProgress.progress(70,Messages.getString("LGM.SPLASH_FRAME")); //$NON-NLS-1$
		f.setOpaque(true);
		frame.setContentPane(f);
		new FramePrefsHandler(frame);
		SplashProgress.progress(80,Messages.getString("LGM.SPLASH_LOGO")); //$NON-NLS-1$
		try
			{
			frame.setIconImage(ImageIO.read(LGM.class.getClassLoader().getResource(
					"org/lateralgm/main/lgm-logo.png"))); //$NON-NLS-1$
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		SplashProgress.progress(90,Messages.getString("LGM.SPLASH_PLUGINS")); //$NON-NLS-1$
		loadPlugins();
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

		static final Boolean TIMER = System.getProperty("lgm.progresstimer") != null; //$NON-NLS-1$
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
			progress(100,""); //$NON-NLS-1$
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
