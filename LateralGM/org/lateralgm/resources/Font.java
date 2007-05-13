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
	public String FontName = "Arial";
	public int Size = 12;
	public boolean Bold = false;
	public boolean Italic = false;
	public int CharRangeMin = 32;
	public int CharRangeMax = 127;

	public Font()
		{
		setName(Prefs.prefixes[Resource.FONT]);
		}

	@SuppressWarnings("unchecked")
	public Font copy(boolean update, ResourceList src)
		{
		Font font = new Font();
		font.FontName = FontName;
		font.Size = Size;
		font.Bold = Bold;
		font.Italic = Italic;
		font.CharRangeMin = CharRangeMin;
		font.CharRangeMax = CharRangeMax;
		if (update)
			{
			font.setId(new ResId(++src.LastId));
			font.setName(Prefs.prefixes[Resource.FONT] + src.LastId);
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