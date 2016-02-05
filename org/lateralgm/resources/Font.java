/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;

import org.lateralgm.main.LGM;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.sub.CharacterRange;
import org.lateralgm.resources.sub.CharacterRange.PCharacterRange;
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

	public CharacterRange addRange(int min, int max)
		{
		if (min < 0 || min > max) throw new IllegalArgumentException();
		CharacterRange cr = new CharacterRange(this,min,max);
		characterRanges.add(cr);
		return cr;
		}

	public void addRangesFromString(String s)
		{
		ArrayList<Integer> sorted = new ArrayList<Integer>();
		int cp = 0;
		for (int i = 0; i < s.codePointCount(0,s.length()); i++)
			{
			int cpa = s.codePointAt(cp);
			sorted.add(cpa);
			cp += Character.toChars(cpa).length;
			}

		Collections.sort(sorted);

		int last = sorted.get(0);
		CharacterRange cr = addRange(last,last);
		for (Integer charint : sorted)
			{
			int current = charint;
			if (current - last > 1) cr = addRange(current,current);
			last = current;
			cr.properties.put(PCharacterRange.RANGE_MAX,current);
			}
		}

	public void addRangesFromFile(File f)
		{
		try
			{
			addRangesFromString(new String(Files.readAllBytes(f.toPath()),"UTF-8"));
			}
		catch (IOException e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		}

	public static int makeStyle(boolean bold, boolean italic)
		{
		return (italic ? java.awt.Font.ITALIC : 0) | (bold ? java.awt.Font.BOLD : 0);
		}

	public java.awt.Font getAWTFont(int resolution)
		{
		int s = get(PFont.SIZE);
		String fn = get(PFont.FONT_NAME);
		boolean b = get(PFont.BOLD);
		boolean i = get(PFont.ITALIC);
		/* Java assumes 72 dpi, but we shouldn't depend on the native resolution either.
		 * For consistent pixel size across different systems, we should pick a common default.
		 * AFAIK, the default in Windows (and thus GM) is 96 dpi. */
		int fontSize = (int) Math.round(s * resolution / 72.0);

		return new java.awt.Font(fn,makeStyle(b,i),fontSize);
		}

	public java.awt.Font getAWTFont()
		{
		return getAWTFont(96);
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
		dest.characterRanges.clear();
		for (CharacterRange cr : characterRanges)
			{
			CharacterRange r2 = dest.addRange();
			r2.properties.putAll(cr.properties);
			}
		dest.glyphMetrics.clear();
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
