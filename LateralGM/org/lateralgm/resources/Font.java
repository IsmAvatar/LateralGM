/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
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
		this(null,true);
		}

	public Font(ResourceReference<Font> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes[Resource.FONT]);
		}

	protected Font copy(ResourceList<Font> src, ResourceReference<Font> ref, boolean update)
		{
		Font f = new Font(ref,update);
		f.fontName = fontName;
		f.size = size;
		f.bold = bold;
		f.italic = italic;
		f.charRangeMin = charRangeMin;
		f.charRangeMax = charRangeMax;
		if (src != null)
			{
			f.setName(Prefs.prefixes[Resource.FONT] + (src.lastId + 1));
			src.add(f);
			}
		else
			{
			f.setId(getId());
			f.setName(getName());
			}
		return f;
		}

	public byte getKind()
		{
		return FONT;
		}
	}
