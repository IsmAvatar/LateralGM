/*
 * Copyright (C) 2007, 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013 Robert B.Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.lateralgm.resources.Resource;

public final class Prefs
	{
	private static String BUNDLE_NAME = "org.lateralgm.main.preferences"; //$NON-NLS-1$
	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	private static final Preferences PREFS = Preferences.userRoot().node("/org/lateralgm");

	static
		{
		loadPrefs();
		}

	private Prefs()
		{
		}

	public static String getString(String key, String def)
		{
		String r;
		try
			{
			  r = RESOURCE_BUNDLE.getString(key);
			}
		catch (MissingResourceException e)
			{
			r = def == null ? '!' + key + '!' : def;
			}
		return PREFS.get(key,r);
		}

	public static int getInt(String key, int def)
		{
		try
			{
			return Integer.parseInt(getString(key,null));
			}
		catch (NumberFormatException e)
			{
			return def;
			}
		}

	public static boolean getBoolean(String key, boolean def)
		{
		String ret = getString(key,null).trim().toLowerCase();
		if (ret.startsWith("true")) return true;
		if (ret.startsWith("false")) return false;
		return def;
		}

	public static void loadPrefs()
		{
		if (new File("preferences.properties").exists()) {
		  BUNDLE_NAME = "preferences.properties";
		  RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		}
		
		extraNodes = getBoolean("extraNodes",true);
		restrictHierarchy = getBoolean("restrictHierarchy",true);
		renamableRoots = getBoolean("renamableRoots",false);
		swingTheme = getString("swingTheme", "Native");
		swingThemePath = getString("swingThemePath", "");
		iconPack = getString("iconPack", "Standard");
		iconPath = getString("iconPath", "/icons/");
		groupKind = getBoolean("groupKind",true);
		iconizeGroup = getBoolean("iconizeGroup",false);
		String fontName = getString("codeFontName","Monospaced");
		codeFont = new Font(fontName,Font.PLAIN,getInt("codeFontSize",12));
		tabSize = getInt("tabSize",4);
		String d = "OBJ>obj_	SPR>spr_	SND>snd_	RMM>rm_	 BKG>bkg_  PTH>path_	SCR>scr_"
				+ "  SHR>shr_	 FNT>font_	TML>time_";
		String[] p = getString("prefixes",d).split("\\t+");
		prefixes = new HashMap<Class<? extends Resource<?,?>>,String>();
		for (int i = 0; i < p.length; i++)
			{
			String[] kv = p[i].split(">",2);
			try
				{
				Class<? extends Resource<?,?>> k = Resource.kindsByName3.get(kv[0]);
				if (k != null) prefixes.put(k,kv[1]);
				}
			catch (IllegalArgumentException e)
				{
				e.printStackTrace();
				}
			}
		languageName = getString("languageName", "English");
		manualPath = getString("manualPath", "http://enigma-dev.org/docs/Wiki/Main_Page");
		enableDragAndDrop = getBoolean("enableDragAndDrop", true);
		frameMaximized = getBoolean("frameMaximized", true);
		dockEventPanel = getBoolean("dockEventPanel", false);
		actionLibraryPath = getString("actionLibraryPath","org/lateralgm/resources/library/default");
		userLibraryPath = getString("userLibraryPath","./lib");
		antialiasContolFont = getString("antialiasContolFont", "on");
		
		eventKeyInputAddKey = KeyEvent.VK_BACK_SLASH;
		actionToolTipColumns = getInt("actionToolTipColumns",30);
		actionToolTipLines = getInt("actionToolTipLines",10);

		externalSpriteExtension = getString("externalSpriteExtension","png");
		externalBackgroundExtension = getString("externalBackgroundExtension","png");
		externalScriptExtension = getString("externalScriptExtension","gml");

		String str = getString("externalBackgroundEditorCommand","null");
		useExternalBackgroundEditor = !str.isEmpty() && !str.toLowerCase().equals("null");
		externalBackgroundEditorCommand = str.toLowerCase().equals("system") ? null : str;
		str = getString("externalSpriteEditorCommand","null");
		useExternalSpriteEditor = !str.isEmpty() && !str.toLowerCase().equals("null");
		externalSpriteEditorCommand = str.toLowerCase().equals("system") ? null : str;
		str = getString("externalScriptEditorCommand","null");
		useExternalScriptEditor = !str.isEmpty() && !str.toLowerCase().equals("null");
		externalScriptEditorCommand = str.toLowerCase().equals("system") ? null : str;
		str = getString("externalSoundEditorCommand","null");
		useExternalSoundEditor = !str.isEmpty() && !str.toLowerCase().equals("null");
		externalSoundEditorCommand = str.toLowerCase().equals("system") ? null : str;
		}

	public static String iconPack;
	public static String iconPath;
	public static String swingTheme;
	public static String swingThemePath;
	public static String manualPath;
	public static String languageName;
	public static String antialiasContolFont;
	
	public static boolean frameMaximized;
	public static boolean extraNodes;
	public static boolean restrictHierarchy;
	public static boolean renamableRoots;
	public static boolean groupKind;
	public static boolean iconizeGroup;
	public static Map<Class<? extends Resource<?,?>>,String> prefixes;

	public static Font codeFont;
	public static int tabSize;
	public static int eventKeyInputAddKey = KeyEvent.VK_BACK_SLASH;

	public static boolean dockEventPanel;
	public static boolean enableDragAndDrop;
	public static String actionLibraryPath;
	public static String userLibraryPath;
	public static int actionToolTipColumns;
	public static int actionToolTipLines;

	public static boolean useExternalBackgroundEditor;
	public static String externalBackgroundEditorCommand;
	public static String externalBackgroundExtension;
	public static boolean useExternalSpriteEditor;
	public static String externalSpriteEditorCommand;
	public static String externalSpriteExtension;
	public static boolean useExternalScriptEditor;
	public static String externalScriptEditorCommand;
	public static String externalScriptExtension;
	public static boolean useExternalSoundEditor;
	public static String externalSoundEditorCommand;

	}
