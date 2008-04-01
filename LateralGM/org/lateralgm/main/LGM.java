/*
 * Copyright (C) 2006, 2007, 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2006, 2007, 2008 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * 
 * Lateral GM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Lateral GM is distributed in the hope that it will be useful,
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

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
		String jv = System.getProperty("java.version"); //$NON-NLS-1$
		Scanner s = new Scanner(jv).useDelimiter("[\\._]"); //$NON-NLS-1$
		javaVersion = s.nextInt() * 10000 + s.nextInt() * 100 + s.nextInt();
		System.out.format("Java Version: %d (%s)\n",javaVersion,jv);
		if (javaVersion < 10600)
			System.out.println("Some program functionality will be limited due to your outdated version");
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
	public static GameInformationFrame gameInfo;
	public static GameSettingFrame gameSet;
	public static EventFrame eventSelect;
	public static File tempDir;

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
			System.err.println("Unable to read icons.properties");
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

	public static void createToolBar(JPanel f)
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

	public static void createTree(JPanel f, boolean populate)
		{
		createTree(f,new ResNode("Root",(byte) 0,(byte) 0,null),populate); //$NON-NLS-1$
		}

	public static void createTree(JPanel f, ResNode newroot, boolean populate)
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
		else
			{
			tree.setSelectionRow(0);
			}

		/*
		 * Setup the rest of the main window
		 */

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
		mdi.add(gameSet);
		mdi.add(gameInfo);
		eventSelect = new EventFrame();
		mdi.add(eventSelect);
		}

	static
		{
		Util.tweakIIORegistry();
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
		UIManager.put("swing.boldMetal",Boolean.FALSE);

		tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "lgm");
		if (!tempDir.exists())
			tempDir.mkdir();
		else
			for (File f : tempDir.listFiles())
				if (f.isFile()) f.delete();

		}

	public static void main(String[] args)
		{
		SplashProgress.progress(30,Messages.getString("LGM.SPLASH_LIBS"));
		LibManager.autoLoad();
		SplashProgress.progress(37,Messages.getString("LGM.SPLASH_UI"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameInfo = new GameInformationFrame();
		SplashProgress.progress(58);
		gameSet = new GameSettingFrame();
		SplashProgress.progress(70);
		JPanel f = new JPanel(new BorderLayout());
		createToolBar(f);
		createTree(f,true);
		SplashProgress.progress(85);
		frame.setJMenuBar(new GmMenuBar());
		SplashProgress.progress(90);
		f.setOpaque(true);
		frame.setContentPane(f);
		new FramePrefsHandler(frame);
		SplashProgress.progress(96);
		try
			{
			frame.setIconImage(ImageIO.read(LGM.class.getClassLoader().getResource(
					"org/lateralgm/main/lgm-logo.png")));
			}
		catch (Exception e)
			{
			e.printStackTrace();
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

		static final Boolean TIMER = System.getProperty("lgm.progresstimer") != null;
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
			progress(0,"");
			}

		static void complete()
			{
			if (TIMER)
				{
				completeTime = System.currentTimeMillis();
				long tt = completeTime - startTime;
				System.out.print("Progress/%       ");
				for (Integer v : progressValues)
					{
					System.out.print("\t" + v);
					}
				System.out.println();
				System.out.print("Time/ms          ");
				for (Long t : progressTimes)
					{
					System.out.print("\t" + t);
					}
				System.out.println();
				System.out.print("Actual progress/%");
				for (Long t : progressTimes)
					{
					System.out.print("\t" + Math.round(100.0 * t / tt));
					}
				System.out.println();
				}
			progress(100,"");
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
