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

public class GameInformation extends Resource<GameInformation,GameInformation.PGameInformation>
	{
	public static final Color DEFAULT_COLOR = new Color(0xFFFFE1);
	/*
	 * the default rtf string is almost exactly the same as in fresh gm files
	 * (no linebreaks in this one)
	 */
	public static final String DEFAULT_TEXT = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033"
			+ "{\\fonttbl{\\f0\\fnil Arial;}}{\\colortbl ;\\red0\\green0\\blue0;}"
			+ "\\viewkind4\\uc1\\pard\\cf1\\f0\\fs24}";
	/*
	 * the default rtf text set by Java
	 */
	public static final String RTF_TEXT =
		"{\\rtf1\\ansi\n{\\fonttbl\\f0\\fnil Monospaced;}\n\n\\par\n}\n";

	public enum PGameInformation
		{
		BACKGROUND_COLOR,EMBED_GAME_WINDOW,FORM_CAPTION,LEFT,TOP,WIDTH,HEIGHT,SHOW_BORDER,ALLOW_RESIZE,
		STAY_ON_TOP,PAUSE_GAME,TEXT
		}

	private static final EnumMap<PGameInformation,Object> DEFS = PropertyMap.makeDefaultMap(
			PGameInformation.class,DEFAULT_COLOR,false,"",-1,-1,600,400,true,true,false,true,RTF_TEXT);

	@Override
	public GameInformation makeInstance(ResourceReference<GameInformation> ref)
		{
		return new GameInformation();
		}

	@Override
	protected PropertyMap<PGameInformation> makePropertyMap()
		{
		return new PropertyMap<PGameInformation>(PGameInformation.class,this,DEFS);
		}

	@Override
	protected void postCopy(GameInformation dest)
		{ //Nothing else to copy
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
