/*
 * Copyright (C) 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EnumMap;

import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.util.PropertyMap;

public class GameSettings extends Resource<GameSettings,GameSettings.PGameSettings>
	{
	public static ICOFile DEFAULT_ICON = null;
	static
		{
		try
			{
			String loc = "org/lateralgm/file/default.ico"; //$NON-NLS-1$
			InputStream filein;
			File file = new File(loc);
			if (!file.exists())
				filein = LGM.class.getClassLoader().getResourceAsStream(loc);
			else
				filein = new FileInputStream(file);
			DEFAULT_ICON = new ICOFile(filein);
			}
		catch (Exception ex)
			{
			System.err.println(Messages.getString("GmFile.NOICON")); //$NON-NLS-1$
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			}
		}

	public Constants constants = new Constants();

	public enum ColorDepth
		{
		NO_CHANGE,BIT_16,BIT_32
		}

	public enum Resolution
		{
		NO_CHANGE,RES_320X240,RES_640X480,RES_800X600,RES_1024X768,RES_1280X1024,RES_1600X1200
		}

	public enum Frequency
		{
		NO_CHANGE,FREQ_60,FREQ_70,FREQ_85,FREQ_100,FREQ_120
		}

	public enum Priority
		{
		NORMAL,HIGH,HIGHEST
		}

	public enum ProgressBar
		{
		NONE,DEFAULT,CUSTOM
		}

	//FIXME: Includes information moved
	public enum IncludeFolder
		{
		MAIN,TEMP
		}

	public enum PGameSettings
		{
		GAME_ID,GAME_GUID,START_FULLSCREEN,INTERPOLATE,FORCE_SOFTWARE_VERTEX_PROCESSING,
		DONT_DRAW_BORDER,DISPLAY_CURSOR,SCALING,ALLOW_WINDOW_RESIZE,ALWAYS_ON_TOP,COLOR_OUTSIDE_ROOM,/**/
		SET_RESOLUTION,COLOR_DEPTH,RESOLUTION,FREQUENCY,/**/
		DONT_SHOW_BUTTONS,USE_SYNCHRONIZATION,DISABLE_SCREENSAVERS,LET_F4_SWITCH_FULLSCREEN,
		LET_F1_SHOW_GAME_INFO,LET_ESC_END_GAME,LET_F5_SAVE_F6_LOAD,LET_F9_SCREENSHOT,
		TREAT_CLOSE_AS_ESCAPE,GAME_PRIORITY,/**/
		FREEZE_ON_LOSE_FOCUS,LOAD_BAR_MODE,FRONT_LOAD_BAR,BACK_LOAD_BAR,SHOW_CUSTOM_LOAD_IMAGE,
		LOADING_IMAGE,IMAGE_PARTIALLY_TRANSPARENTY,LOAD_IMAGE_ALPHA,SCALE_PROGRESS_BAR,/**/
		DISPLAY_ERRORS,WRITE_TO_LOG,ABORT_ON_ERROR,TREAT_UNINIT_AS_0,ERROR_ON_ARGS,AUTHOR,VERSION,
		LAST_CHANGED,INFORMATION,/**/
		INCLUDE_FOLDER,OVERWRITE_EXISTING,REMOVE_AT_GAME_END,VERSION_MAJOR,VERSION_MINOR,
		VERSION_RELEASE,VERSION_BUILD,COMPANY,PRODUCT,COPYRIGHT,DESCRIPTION,GAME_ICON
		}

	//GAME_ID and  DPLAY_GUID randomized in GmFile constructor
	private static final EnumMap<PGameSettings,Object> DEFS = PropertyMap.makeDefaultMap(
			PGameSettings.class,-1,new byte[16],false,false,false,false,true,-1,false,false,Color.BLACK,/**/
			false,ColorDepth.NO_CHANGE,Resolution.NO_CHANGE,Frequency.NO_CHANGE,/**/
			false,false,true,true,true,true,false,false,true,Priority.NORMAL,/**/
			true,ProgressBar.DEFAULT,null,null,false,null,false,255,true,/**/
			true,false,false,false,true,
			"","100",ProjectFile.longTimeToGmTime(System.currentTimeMillis()),"",/**///$NON-NLS-1$ //$NON-NLS-3$
			IncludeFolder.MAIN,false,false,1,0,0,0,"","","","",DEFAULT_ICON); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	@Override
	public GameSettings makeInstance(ResourceReference<GameSettings> ref)
		{
		return new GameSettings();
		}

	@Override
	protected PropertyMap<PGameSettings> makePropertyMap()
		{
		return new PropertyMap<PGameSettings>(PGameSettings.class,this,DEFS);
		}

	@Override
	protected void postCopy(GameSettings dest)
		{ //Nothing else to copy
		}

	public Object validate(PGameSettings k, Object v)
		{
		return v;
		}

	public void put(PGameSettings key, Object value)
		{
		properties.put(key,value);
		}

	public <V>V get(PGameSettings key)
		{
		return properties.get(key);
		}

	public Double getLastChanged()
		{
		return (Double) properties.get(PGameSettings.LAST_CHANGED);
		}
	}
