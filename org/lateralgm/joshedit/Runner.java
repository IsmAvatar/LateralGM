/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.lateralgm.joshedit.lexers.GMLTokenMarker;

public class Runner
{
	public static final ResourceBundle MESSAGES = ResourceBundle.getBundle("org.lateralgm.joshedit.translate"); //$NON-NLS-1$
	public static final Preferences PREFS = Preferences.userRoot().node("/org/lateralgm/joshedit"); //$NON-NLS-1$

	public static EditorInterface editorInterface = new EditorInterface()
	{
		public ImageIcon getIconForKey(String key)
		{
			return Runner.getIconForKey(key);
		}

		public String getString(String key)
		{
			return Runner.getString(key,null);
		}

		public String getString(String key, String def)
		{
			return Runner.getString(key,def);
		}
	};

	public static interface EditorInterface
	{
		ImageIcon getIconForKey(String key);

		String getString(String key);

		String getString(String key, String def);
	}

	public static void createAndShowGUI()
	{
		showCodeWindow(true);
		//showBindingsWindow(false);
	}

	public static void showCodeWindow(boolean closeExit)
	{
		JFrame f = new JFrame("Title");
		JoshTextPanel p = new JoshTextPanel(getDefaultCode());
		p.setTokenMarker(new GMLTokenMarker());
		f.setContentPane(p);
		f.pack();
		f.setLocationRelativeTo(null);
		if (closeExit) f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	public static String[] getDefaultCode()
	{
		ArrayList<String> code = new ArrayList<String>();
		code.add("Hello, world");
		code.add("Bracket's' { test }");
		code.add("\tTab test line 1");
		code.add("\tTab test line 2");
		code.add("\tTab test line 3");
		code.add("End tab test line");
		code.add("derp\tCascading tab test");
		code.add("der\tCascading tab test");
		code.add("de\tCascading tab test");
		code.add("d\tCascading tab test");
		code.add("if (a = \"It's hard to think\")");
		code.add("  then b = c + 10; // That after all this time");
		code.add("else /* Everything is finally working.");
		code.add("        *wipes tear* */");
		code.add("  d = e * 20;");
		code.add("/** Okay, so there are probably");
		code.add("    some bugs to work out, but you");
		code.add("    have to admit... It looks nice. */");
		return code.toArray(new String[0]);
	}

	public static String[] splitLines(String text)
	{
		if (text == null) return null;
		LinkedList<String> list = new LinkedList<String>();
		Scanner sc = new Scanner(text);
		while (sc.hasNext())
			list.add(sc.nextLine());
		sc.close();
		return list.toArray(new String[0]);
	}

	public static void showBindingsWindow(boolean closeExit)
	{
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(),BoxLayout.PAGE_AXIS));

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Bindings",new Bindings());
		f.add(tabs);

		JPanel repanel = new JPanel();
		repanel.setLayout(new BoxLayout(repanel,BoxLayout.LINE_AXIS));
		repanel.add(new JButton("OK"));
		repanel.add(new JButton("Cancel"));
		f.add(repanel);

		f.pack();
		f.setLocationRelativeTo(null);
		if (closeExit) f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	public static void showFindWindow(boolean closeExit)
	{
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(),BoxLayout.PAGE_AXIS));

	}

	public static ImageIcon findIcon(String filename)
	{
		String location = "org/lateralgm/joshedit/icons/" + filename; //$NON-NLS-1$
		ImageIcon ico = new ImageIcon(location);
		if (ico.getIconWidth() == -1)
		{
			URL url = Runner.class.getClassLoader().getResource(location);
			if (url != null) ico = new ImageIcon(url);
		}
		return ico;
	}

	public static ImageIcon getIconForKey(String key)
	{
		Properties iconProps = new Properties();
		InputStream is = Runner.class.getClassLoader().getResourceAsStream(
				"org/lateralgm/joshedit/icons.properties"); //$NON-NLS-1$
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

	public static String getString(String key, String def)
	{
		String r;
		try
		{
			r = MESSAGES.getString(key);
		}
		catch (MissingResourceException e)
		{
			r = def == null ? '!' + key + '!' : def;
		}
		return PREFS.get(key,r);
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				createAndShowGUI();
			}
		});
	}
}
