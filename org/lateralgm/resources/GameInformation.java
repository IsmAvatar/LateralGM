/*
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2008 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;
import java.util.EnumMap;

import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class GameInformation implements PropertyValidator<GameInformation.PGameInformation>
	{
	public static final Color DEFAULT_COLOR = new Color(0xFFFFE1);
	/*
	 * the default rtf string is almost exactly the same as in fresh gm files
	 * (no linebreaks in this one)
	 */
	public static final String DEFAULT_TEXT = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033"
			+ "{\\fonttbl{\\f0\\fnil Arial;}}{\\colortbl ;\\red0\\green0\\blue0;}"
			+ "\\viewkind4\\uc1\\pard\\cf1\\f0\\fs24}";

	public enum PGameInformation
		{
		BACKGROUND_COLOR,MIMIC_GAME_WINDOW,FORM_CAPTION,LEFT,TOP,WIDTH,HEIGHT,SHOW_BORDER,ALLOW_RESIZE,
		STAY_ON_TOP,PAUSE_GAME,TEXT
		}

	private static final EnumMap<PGameInformation,Object> DEFS = PropertyMap.makeDefaultMap(
			PGameInformation.class,DEFAULT_COLOR,false,"",-1,-1,600,400,true,true,false,true,DEFAULT_TEXT);

	public final PropertyMap<PGameInformation> properties = new PropertyMap<PGameInformation>(
			PGameInformation.class,this,DEFS);

	public GameInformation clone()
		{
		GameInformation dest = new GameInformation();
		dest.properties.putAll(properties);
		return dest;
		}

	public Object validate(PGameInformation k, Object v)
		{
		return v;
		}

	public void put(PGameInformation key, Object value)
		{
		properties.put(key,value);
		}

	public <V>V get(PGameInformation key)
		{
		return properties.get(key);
		}
	}
