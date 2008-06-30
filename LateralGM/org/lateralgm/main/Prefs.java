/*
 * Copyright (C) 2007, 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Font;
import java.awt.event.KeyEvent;

public final class Prefs
	{
	private Prefs()
		{
		}

	public static boolean renamableRoots = false;
	public static boolean groupKind = true;
	public static boolean iconizeGroup = false;
	public static Font codeFont = new Font("Monospaced",Font.PLAIN,12);
	public static int tabSize = 4;
	public static String[] prefixes = { "","obj_","spr_","snd_","rm_","","bk_","scr_","path_",
			"font_","","","time_" };
	public static String defaultLibraryPath = "org/lateralgm/resources/library/lib/";
	public static String userLibraryPath = "./lib";
	public static int eventKeyInputAddKey = KeyEvent.VK_BACK_SLASH;

	public static boolean useExternalBackgroundEditor = true;
	public static String externalBackgroundEditorCommand = "gimp %s";
	public static boolean useExternalSpriteEditor = true;
	public static String externalSpriteEditorCommand = "gimp %s";
	public static boolean useExternalScriptEditor = false;
	public static String externalScriptEditorCommand = "gedit %s";
	}
