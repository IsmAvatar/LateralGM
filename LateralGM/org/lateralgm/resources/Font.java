/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;

public class Font extends Resource
	{
	public String fontName = "Arial";
	public int size = 12;
	public boolean bold = false;
	public boolean italic = false;
	public int charRangeMin = 32;
	public int charRangeMax = 127;

	public Font()
		{
		setName(Prefs.prefixes[Resource.FONT]);
		}

	@SuppressWarnings("unchecked")
	public Font copy(boolean update, ResourceList src)
		{
		Font font = new Font();
		font.fontName = fontName;
		font.size = size;
		font.bold = bold;
		font.italic = italic;
		font.charRangeMin = charRangeMin;
		font.charRangeMax = charRangeMax;
		if (update)
			{
			font.setId(new ResId(++src.lastId));
			font.setName(Prefs.prefixes[Resource.FONT] + src.lastId);
			src.add(font);
			}
		else
			{
			font.setId(getId());
			font.setName(getName());
			}
		return font;
		}
	}
