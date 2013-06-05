/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public final class PrefsStore
	{
	private PrefsStore()
		{
		}

	private static final Preferences PREFS = Preferences.userRoot().node("/org/lateralgm");

	public static ArrayList<String> getRecentFiles()
		{
		String value = PREFS.get("FILE_RECENT",null);
		if (value == null) return new ArrayList<String>(0);
		String[] array = value.split(" ");
		ArrayList<String> list = new ArrayList<String>(array.length);
		for (String name : array)
			list.add(Util.urlDecode(name));
		return list;
		}

	public static void addRecentFile(String name)
		{
		int maxcount = PREFS.getInt("FILE_RECENT_COUNT",4);
		ArrayList<String> oldList = getRecentFiles();
		oldList.remove(name);
		String newList;
		newList = Util.urlEncode(name);
		for (int i = 0; i + 1 < maxcount && i < oldList.size(); i++)
			newList += " " + Util.urlEncode(oldList.get(i));
		PREFS.put("FILE_RECENT",newList);
		}

	public static Rectangle getWindowBounds()
		{
		return Util.stringToRectangle(PREFS.get("WINDOW_BOUNDS",null),new Rectangle(800,600));
		}

	public static void setWindowBounds(Rectangle r)
		{
		PREFS.put("WINDOW_BOUNDS",Util.rectangleToString(r));
		}

	public static boolean getWindowMaximized()
	{
		return PREFS.getBoolean("WINDOW_MAXIMIZED",true);
	}

	public static void setWindowMaximized(boolean b)
	{
		PREFS.putBoolean("WINDOW_MAXIMIZED",b);
	}
	
	public static void setIconPack(String s)
	{
		PREFS.put("iconPack",s);
		Prefs.iconPack = s;
	}
	
	public static void setIconPath(String s)
	{
		PREFS.put("iconPath",s);
		Prefs.iconPath = s;
	}
	
	public static void setSwingTheme(String s)
	{
		PREFS.put("swingTheme",s);
		Prefs.swingTheme = s;
	}
	
	public static void setSwingThemePath(String s)
	{
		PREFS.put("swingThemePath",s);
		Prefs.swingThemePath = s;
	}
	
	public static void setLanguageName(String s)
	{
		PREFS.put("languageName",s);
		Prefs.languageName = s;
	}

	public static void setDNDEnabled(boolean selected)
	{
		PREFS.putBoolean("enableDragAndDrop",selected);
		Prefs.enableDragAndDrop = selected;
	}
	
	public static void setManualPath(String path)
	{
		PREFS.put("manualPath", path);
		Prefs.manualPath = path;
	}
	
	public static void setExtraNodes(boolean selected)
	{
	  PREFS.putBoolean("extraNodes",selected);
	  Prefs.extraNodes = selected;
	}
	
	public static int getNumberOfBackups()
		{
		return PREFS.getInt("FILE_BACKUP_COUNT",1);
		}
	}
