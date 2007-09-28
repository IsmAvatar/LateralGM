/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package org.lateralgm.resources;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.lateralgm.file.Gm6File;
import org.lateralgm.resources.sub.Constant;

public class GameSettings
	{
	//Constants
	public static final byte COLOR_NOCHANGE = 0;
	public static final byte COLOR_16 = 1;
	public static final byte COLOR_32 = 2;
	public static final byte RES_NOCHANGE = 0;
	public static final byte RES_320X240 = 1;
	public static final byte RES_640X480 = 2;
	public static final byte RES_800X600 = 3;
	public static final byte RES_1024X768 = 4;
	public static final byte RES_1280X1024 = 5;
	public static final byte RES_1600X1200 = 6;
	public static final byte FREQ_NOCHANGE = 0;
	public static final byte FREQ_60 = 1;
	public static final byte FREQ_70 = 2;
	public static final byte FREQ_85 = 3;
	public static final byte FREQ_100 = 4;
	public static final byte FREQ_120 = 5;
	public static final byte PRIORITY_NORMAL = 0;
	public static final byte PRIORITY_HIGH = 1;
	public static final byte PRIORITY_HIGHEST = 2;
	public static final byte LOADBAR_NONE = 0;
	public static final byte LOADBAR_DEFAULT = 1;
	public static final byte LOADBAR_CUSTOM = 2;
	public static final byte INCLUDE_MAIN = 0;
	public static final byte INCLUDE_TEMP = 1;

	//Properties
	public int gameId; // randomized in Gm6File constructor
	public boolean startFullscreen = false;
	public boolean interpolate = false;
	public boolean dontDrawBorder = false;
	public boolean displayCursor = true;
	public int scaling = -1;
	public boolean allowWindowResize = false;
	public boolean alwaysOnTop = false;
	public Color colorOutsideRoom = Color.BLACK;
	public boolean setResolution = false;
	public byte colorDepth = GameSettings.COLOR_NOCHANGE;
	public byte resolution = GameSettings.RES_NOCHANGE;
	public byte frequency = GameSettings.FREQ_NOCHANGE;
	public boolean dontShowButtons = false;
	public boolean useSynchronization = false;
	public boolean letF4SwitchFullscreen = true;
	public boolean letF1ShowGameInfo = true;
	public boolean letEscEndGame = true;
	public boolean letF5SaveF6Load = true;
	public byte gamePriority = GameSettings.PRIORITY_NORMAL;
	public boolean freezeOnLoseFocus = false;
	public byte loadBarMode = GameSettings.LOADBAR_DEFAULT;
	public BufferedImage frontLoadBar = null;
	public BufferedImage backLoadBar = null;
	public boolean showCustomLoadImage = false;
	public BufferedImage loadingImage = null;
	public boolean imagePartiallyTransparent = false;
	public int loadImageAlpha = 255;
	public boolean scaleProgressBar = true;
	public boolean displayErrors = true;
	public boolean writeToLog = false;
	public boolean abortOnError = false;
	public boolean treatUninitializedAs0 = false;
	public String author = ""; //$NON-NLS-1$
	public String version = "100";
	public double lastChanged = Gm6File.longTimeToGmTime(System.currentTimeMillis());
	public String information = ""; //$NON-NLS-1$
	public int includeFolder = GameSettings.INCLUDE_MAIN;
	public boolean overwriteExisting = false;
	public boolean removeAtGameEnd = false;

	public ArrayList<Constant> constants = new ArrayList<Constant>();
	public ArrayList<Include> includes = new ArrayList<Include>();

	//actual data is stored to be written on resave (no reason to re-encode)
	public byte[] gameIconData;
	public BufferedImage gameIcon; // icon as image for display purposes
	}
