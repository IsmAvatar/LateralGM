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

import javax.swing.JOptionPane;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.sub.CharacterRange;
import org.lateralgm.resources.sub.GlyphMetric;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.PropertyMap;

public class Font extends InstantiableResource<Font,Font.PFont>
{

	public enum PFont
	{
		FONT_NAME,SIZE,BOLD,ITALIC,ANTIALIAS,CHARSET
	}
	
	private static final EnumMap<PFont,Object> DEFS = PropertyMap.makeDefaultMap(PFont.class,"Arial",
			12,false,false,3,0);
	
	public final ActiveArrayList<CharacterRange> characterRanges = new ActiveArrayList<CharacterRange>();
	public final ActiveArrayList<GlyphMetric> glyphMetrics = new ActiveArrayList<GlyphMetric>();
	
	private final UpdateTrigger rangeUpdateTrigger = new UpdateTrigger();
	public final UpdateSource rangeUpdateSource = new UpdateSource(this,rangeUpdateTrigger);
	
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
	
	public GlyphMetric addGlyph()
		{
		GlyphMetric gm = new GlyphMetric();
		glyphMetrics.add(gm);
		return gm;
		}

	public CharacterRange addRange()
		{
		CharacterRange cr = new CharacterRange(this);
		characterRanges.add(cr);
		return cr;
		}
	
	public void addRange(int min, int max)
		{
		if (min < 0 || max > 255 || min > max) throw new IllegalArgumentException();
		characterRanges.add(new CharacterRange(this, min, max));
		}

	@Override
	protected PropertyMap<PFont> makePropertyMap()
		{
		return new PropertyMap<PFont>(PFont.class,this,DEFS);
		}
	
	@Override
	protected void postCopy(Font dest)
		{
		super.postCopy(dest);
		for (CharacterRange cr : characterRanges)
			{
			CharacterRange r2 = dest.addRange();
			r2.properties.putAll(cr.properties);
			}
		for (GlyphMetric gm : glyphMetrics)
			{
			GlyphMetric g2 = dest.addGlyph();
			g2.properties.putAll(gm.properties);
			}
		}

	public void rangeUpdated(UpdateEvent e)
		{
		rangeUpdateTrigger.fire(new UpdateEvent(rangeUpdateSource,e));
		}
	}
