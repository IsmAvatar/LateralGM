/*
 * Copyright (C) 2006 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.lateralgm.main.LGM;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.plugin.EGMC;
import org.lateralgm.plugin.Plugin;

public class GmMenuBar extends JMenuBar
	{
	private static final long serialVersionUID = 1L;
	private JMenuItem[] recentFiles = new JMenuItem[0];
	private int recentFilesPos;
	public static GmMenu menu;
	public static GmMenu fileMenu;

	public static final void setTextAndAlt(JMenuItem item, String input)
		{
		Matcher m = Pattern.compile("\t+([^\\s])$").matcher(input); //$NON-NLS-1$
		if (m.find())
			{
			int alt = m.group(1).toUpperCase(Locale.ENGLISH).charAt(0);
			item.setMnemonic(alt);
			item.setText(input.substring(0,m.start()));
			}
		else
			{
			item.setMnemonic(-1);
			item.setText(input);
			}
		}

	public void updateRecentFiles()
		{
		String[] recentList = PrefsStore.getRecentFiles().toArray(new String[0]);
		for (JMenuItem item : recentFiles)
			{
			fileMenu.remove(item);
			}
		recentFiles = new JMenuItem[recentList.length];
		for (int i = 0; i < recentFiles.length; i++)
			{
			File file = new File(recentList[i]).getAbsoluteFile();
			String number = Integer.toString(i + 1);
			JMenuItem item = new JMenuItem(String.format("%s %s  [%s]",number,file.getName(),
					file.getParent()),number.codePointAt(0));
			recentFiles[i] = item;
			item.setActionCommand("GmMenuBar.OPEN " + Util.urlEncode(file.toString())); //$NON-NLS-1$
			item.addActionListener(LGM.listener);
			fileMenu.insert(item,recentFilesPos + i);
			}
		}

	public GmMenu makePluginMenu()
		{
		GmMenu sub = new GmMenu("Plugins");
		File dir = new File("./plugins");
		if (!dir.exists()) return sub;
		File[] ps = dir.listFiles();
		if (ps.length == 0) return sub;
		for (File f : ps)
			{
			if (!f.exists()) continue;
			JMenuItem mi = new JMenuItem(f.getName());
			try
				{
				//				Manifest mf = new JarFile(f).getManifest();
				URLClassLoader ucl = new URLClassLoader(new URL[] { f.toURI().toURL() });
				Class<?> c = ucl.loadClass("Main");
				boolean b = c.isAssignableFrom(Plugin.class);
				System.out.println(b);
				if (!b) continue;
				final Plugin p = (Plugin) ucl.loadClass("Main").newInstance();
				mi.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
							{
							p.execute();
							}
					});
				}
			catch (Exception e)
				{
				System.out.println("Unable to load plugin: " + f.toString() + ": " + e.getCause() + ": "
						+ e.getMessage());
				continue;
				}
			//			if (!(o instanceof Plugin))
			//				{
			//				System.out.println(o.toString());
			//				continue;
			//				}
			//			final Plugin p = (Plugin) o;
			sub.add(mi);
			}
		return sub;
		}

	public GmMenuBar()
		{
		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_FILE")); //$NON-NLS-1$
		fileMenu = menu;
		add(menu);

		menu.addItem("GmMenuBar.NEW",KeyEvent.VK_N,ActionEvent.CTRL_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.OPEN",KeyEvent.VK_O,ActionEvent.CTRL_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.SAVE",KeyEvent.VK_S,ActionEvent.CTRL_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.SAVEAS"); //$NON-NLS-1$
		menu.add(new JSeparator());
		JMenuItem mi = new JMenuItem("Compile with Enigma");
		mi.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					new EGMC().execute();
					}
			});
		menu.add(mi);
		// JCheckBoxMenuItem check = new JCheckBoxMenuItem();
		// setTextAndAlt(check,Messages.getString("GmMenuBar.ADVANCED")); //$NON-NLS-1$
		// menu.add(check);
		// menu.addItem("GmMenuBar.PREFERENCES"); //$NON-NLS-1$
		// menu.add(new JSeparator());
		recentFilesPos = menu.getMenuComponentCount();
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.EXIT",KeyEvent.VK_F4,ActionEvent.ALT_MASK); //$NON-NLS-1$
		updateRecentFiles();

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_EDIT")); //$NON-NLS-1$
		add(menu);

		GmMenu sub = new GmMenu(Messages.getString("GmMenuBar.MENU_INSERT")); //$NON-NLS-1$
		menu.add(sub);
		sub.addItem("GmMenuBar.INSERT_GROUP"); //$NON-NLS-1$
		sub.add(new JSeparator());
		sub.addItem("GmMenuBar.INSERT_SPRITE"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_SOUND"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_BACKGROUND"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_PATH"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_SCRIPT"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_FONT"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_TIMELINE"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_OBJECT"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_ROOM"); //$NON-NLS-1$

		sub = new GmMenu(Messages.getString("GmMenuBar.MENU_ADD")); //$NON-NLS-1$
		menu.add(sub);
		sub.addItem("GmMenuBar.ADD_GROUP"); //$NON-NLS-1$
		sub.add(new JSeparator());
		sub.addItem("GmMenuBar.ADD_SPRITE"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_SOUND"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_BACKGROUND"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_PATH"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_SCRIPT"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_FONT"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_TIMELINE"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_OBJECT"); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_ROOM"); //$NON-NLS-1$

		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.RENAME",KeyEvent.VK_F2,0); //$NON-NLS-1$
		menu.addItem("GmMenuBar.DELETE",KeyEvent.VK_DELETE,ActionEvent.SHIFT_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.COPY",KeyEvent.VK_INSERT,ActionEvent.ALT_MASK); //$NON-NLS-1$
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.PROPERTIES",KeyEvent.VK_ENTER,ActionEvent.ALT_MASK); //$NON-NLS-1$

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_RESOURCES")); //$NON-NLS-1$
		add(menu);

		menu.addItem("GmMenuBar.DEFRAGIDS"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.VERIFYNAMES"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.SYNTAXCHECK"); //$NON-NLS-1$
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.FIND", //$NON-NLS-1$
				KeyEvent.VK_F,ActionEvent.ALT_MASK + ActionEvent.CTRL_MASK);
		menu.addItem("GmMenuBar.ANNOTATE"); //$NON-NLS-1$
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.EXPAND"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.COLLAPSE"); //$NON-NLS-1$

		add(LGM.mdi.getMenu());

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_HELP")); //$NON-NLS-1$
		add(menu);

		menu.addItem("GmMenuBar.MANUAL",KeyEvent.VK_F1,0); //$NON-NLS-1$
		menu.addItem("GmMenuBar.ABOUT"); //$NON-NLS-1$
		}
	}
