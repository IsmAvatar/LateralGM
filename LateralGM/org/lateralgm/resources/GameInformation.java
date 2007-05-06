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
	public Color BackgroundColor = new Color(0xFFFFE1);
	public boolean MimicGameWindow = false;
	public String FormCaption = "";
	public int Left = -1;
	public int Top = -1;
	public int Width = 600;
	public int Height = 400;
	public boolean ShowBorder = true;
	public boolean AllowResize = true;
	public boolean StayOnTop = false;
	public boolean PauseGame = true;
	public String GameInfoStr = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fnil Arial;}}"
			+ "{\\colortbl ;\\red0\\green0\\blue0;}\\viewkind4\\uc1\\pard\\cf1\\f0\\fs24}";
	// the default rtf string is almost exactly the same as in fresh gm files (no linebreaks in this one)
	}