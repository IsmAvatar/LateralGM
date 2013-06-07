/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.util.PropertyMap;

public class Font extends InstantiableResource<Font,Font.PFont>
	{
	public enum PFont
		{
		FONT_NAME,SIZE,BOLD,ITALIC,ANTIALIAS,CHARSET,RANGE_MIN,RANGE_MAX
		}

	private static final EnumMap<PFont,Object> DEFS = PropertyMap.makeDefaultMap(PFont.class,"Arial",
			12,false,false,3,0,32,127);

	public Font()
		{
		this(null);
		}

	public Font(ResourceReference<Font> r)
		{
		super(r);
		}

	public Font makeInstance(ResourceReference<Font> r)
		{
		return new Font(r);
		}

	public void setRange(int min, int max)
		{
		if (min < 0 || max > 255 || min > max) throw new IllegalArgumentException();
		if (min > (Integer) get(PFont.RANGE_MAX))
			{
			put(PFont.RANGE_MAX,max);
			put(PFont.RANGE_MIN,min);
			}
		else
			{
			put(PFont.RANGE_MIN,min);
			put(PFont.RANGE_MAX,max);
			}
		}

	@Override
	protected PropertyMap<PFont> makePropertyMap()
		{
		return new PropertyMap<PFont>(PFont.class,this,DEFS);
		}

	@Override
	public Object validate(PFont k, Object v)
		{
		switch (k)
			{
			case RANGE_MIN:
				int min = (Integer) v;
				if (min < 0)
					min = 0;
				else if (min > 255) min = 255;
				if (min > (Integer) get(PFont.RANGE_MAX)) put(PFont.RANGE_MAX,min);
				if (min != (Integer) v) return min;
				break;
			case RANGE_MAX:
				int max = (Integer) v;
				if (max < 0)
					max = 0;
				else if (max > 255) min = 255;
				if (max < (Integer) get(PFont.RANGE_MIN)) put(PFont.RANGE_MIN,max);
				if (max != (Integer) v) return max;
		default:
		  //TODO: maybe put a failsafe here?
			break;
			}
		return v;
		}
	}
