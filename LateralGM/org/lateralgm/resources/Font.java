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

public class Font extends Resource<Font>
	{
	public String fontName = "Arial"; //$NON-NLS-1$
	public int size = 12;
	public boolean bold = false;
	public boolean italic = false;
	public int charRangeMin = 32;
	public int charRangeMax = 127;

	public Font()
		{
		setName(Prefs.prefixes[Resource.FONT]);
		}

	public Font copy()
		{
		return copy(false,null);
		}

	public Font copy(ResourceList<Font> src)
		{
		return copy(true,src);
		}

	private Font copy(boolean update, ResourceList<Font> src)
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
			font.setName(Prefs.prefixes[Resource.FONT] + (src.lastId + 1));
			src.add(font);
			}
		else
			{
			font.setId(getId());
			font.setName(getName());
			}
		return font;
		}

	public byte getKind()
		{
		return FONT;
		}
	}
