/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;

public class GameInformation
	{
	public Color backgroundColor = new Color(0xFFFFE1);
	public boolean mimicGameWindow = false;
	public String formCaption = "";
	public int left = -1;
	public int top = -1;
	public int width = 600;
	public int height = 400;
	public boolean showBorder = true;
	public boolean allowResize = true;
	public boolean stayOnTop = false;
	public boolean pauseGame = true;
	public String gameInfoStr = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fnil Arial;}}"
			+ "{\\colortbl ;\\red0\\green0\\blue0;}\\viewkind4\\uc1\\pard\\cf1\\f0\\fs24}";
	// the default rtf string is almost exactly the same as in fresh gm files (no linebreaks in this one)
	}