/*
 * Copyright (C) 2006, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
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
	/*
	 * the default rtf string is almost exactly the same as in fresh gm files
	 * (no linebreaks in this one)
	 */
	public String gameInfoStr = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl"
			+ "{\\f0\\fnil Arial;}}{\\colortbl ;\\red0\\green0\\blue0;}\\viewkind4\\uc1\\pard"
			+ "\\cf1\\f0\\fs24}";

	public GameInformation copy()
		{
		GameInformation gi = new GameInformation();
		gi.backgroundColor = backgroundColor;
		gi.mimicGameWindow = mimicGameWindow;
		gi.formCaption = formCaption;
		gi.left = left;
		gi.top = top;
		gi.width = width;
		gi.height = height;
		gi.showBorder = showBorder;
		gi.allowResize = allowResize;
		gi.stayOnTop = stayOnTop;
		gi.pauseGame = pauseGame;
		gi.gameInfoStr = gameInfoStr;
		return gi;
		}
	}
