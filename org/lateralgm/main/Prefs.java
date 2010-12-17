/*
 * Copyright (C) 2007, 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.lateralgm.resources.Resource;

public final class Prefs
	{
	private static final String BUNDLE_NAME = "org.lateralgm.main.preferences"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
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
		renamableRoots = getBoolean("renamableRoots",false);
		groupKind = getBoolean("groupKind",true);
		iconizeGroup = getBoolean("iconizeGroup",false);
		String fontName = getString("codeFontName","Monospaced");
		codeFont = new Font(fontName,Font.PLAIN,getInt("codeFontSize",12));
		tabSize = getInt("tabSize",4);
		String d = "OBJECT>obj_	SPRITE>spr_	SOUND>snd_	ROOM>rm_	BACKGROUND>bkg_	SCRIPT>scr_	"
				+ "PATH>path_	FONT>font_	TIMELINE>time_";
		String[] p = getString("prefixes",d).split("\\t+");
		prefixes = new EnumMap<Resource.Kind,String>(Resource.Kind.class);
		for (int i = 0; i < p.length; i++)
			{
			String[] kv = p[i].split(">",2);
			try
				{
				prefixes.put(Resource.Kind.valueOf(kv[0]),kv[1]);
				}
			catch (IllegalArgumentException e)
				{
				e.printStackTrace();
				}
			}
		defaultLibraryPath = getString("defaultLibraryPath","org/lateralgm/resources/library/lib/");
		userLibraryPath = getString("userLibraryPath","./lib");
		eventKeyInputAddKey = KeyEvent.VK_BACK_SLASH;
		actionToolTipColumns = getInt("actionToolTipColumns",30);
		actionToolTipLines = getInt("actionToolTipLines",10);

		String str = getString("externalBackgroundEditorCommand","gimp %s");
		useExternalBackgroundEditor = !str.toLowerCase().equals("null");
		externalBackgroundEditorCommand = str;
		str = getString("externalSpriteEditorCommand","gimp %s");
		useExternalSpriteEditor = !str.toLowerCase().equals("null");
		externalSpriteEditorCommand = str;
		str = getString("externalScriptEditorCommand","null");
		useExternalScriptEditor = !str.toLowerCase().equals("null");
		externalScriptEditorCommand = str;
		str = getString("externalSoundEditorCommand","null");
		useExternalSoundEditor = !str.toLowerCase().equals("null");
		externalSoundEditorCommand = str;
		}

	public static boolean renamableRoots;
	public static boolean groupKind;
	public static boolean iconizeGroup;
	public static Map<Resource.Kind,String> prefixes;

	public static Font codeFont;
	public static int tabSize;
	public static int eventKeyInputAddKey = KeyEvent.VK_BACK_SLASH;

	public static String defaultLibraryPath;
	public static String userLibraryPath;
	public static int actionToolTipColumns;
	public static int actionToolTipLines;

	public static boolean useExternalBackgroundEditor;
	public static String externalBackgroundEditorCommand;
	public static boolean useExternalSpriteEditor;
	public static String externalSpriteEditorCommand;
	public static boolean useExternalScriptEditor;
	public static String externalScriptEditorCommand;
	public static boolean useExternalSoundEditor;
	public static String externalSoundEditorCommand;

	}
