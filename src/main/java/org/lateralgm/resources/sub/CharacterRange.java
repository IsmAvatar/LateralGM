/**
* @file  CharacterRange.java
* @brief Class implementing a Font Character Range interface.
*
* @section License
*
* Copyright (C) 2014 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.resources.sub;

import java.util.EnumMap;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class CharacterRange implements UpdateListener,
		PropertyValidator<CharacterRange.PCharacterRange>
	{

	public enum PCharacterRange
		{
		RANGE_MIN,RANGE_MAX
		}

	private static final EnumMap<PCharacterRange,Object> DEFS = PropertyMap.makeDefaultMap(
			PCharacterRange.class,32,127);

	public final PropertyMap<PCharacterRange> properties;
	private final ResourceReference<Font> font;

	private final RangePropertyListener rpl = new RangePropertyListener();

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public CharacterRange(Font fnt)
		{
		super();
		font = fnt.reference;
		properties = new PropertyMap<PCharacterRange>(PCharacterRange.class,this,DEFS);
		properties.getUpdateSource(PCharacterRange.RANGE_MAX).addListener(rpl);
		properties.getUpdateSource(PCharacterRange.RANGE_MIN).addListener(rpl);
		}

	public CharacterRange(Font fnt, int min, int max)
		{
		this(fnt);
		properties.put(PCharacterRange.RANGE_MIN,min);
		properties.put(PCharacterRange.RANGE_MAX,max);
		}

	public void fireUpdate(UpdateEvent e)
		{
		if (e == null) e = updateTrigger.getEvent();
		updateTrigger.fire(e);
		Font f = font == null ? null : font.get();
		if (f != null) f.rangeUpdated(e);
		}

	public void updated(UpdateEvent e)
		{
		fireUpdate(e);
		}

	private class RangePropertyListener extends PropertyUpdateListener<PCharacterRange>
		{
		@Override
		public void updated(PropertyUpdateEvent<PCharacterRange> e)
			{
			fireUpdate(null);
			}
		}

	public Object validate(PCharacterRange k, Object v)
		{
		switch (k)
			{
			case RANGE_MIN:
				int min = (Integer) v;
				if (min < 0) min = 0;
				//TODO: No limit since Unicode supported
				//else if (min > 65536) min = 65536;
				if (min > (Integer) v)
					{
					properties.put(PCharacterRange.RANGE_MIN,min);
					}
				if (min != (Integer) v) return min;
				break;
			case RANGE_MAX:
				int max = (Integer) v;
				if (max < 0) max = 0;
				//TODO: No limit since Unicode supported
				//else if (max > 65536) max = 65536;
				if (max < (Integer) v)
					{
					properties.put(PCharacterRange.RANGE_MAX,max);
					}
				if (max != (Integer) v) return max;
				break;
			default:
				//TODO: maybe put a failsafe here?
				break;
			}
		return v;
		}

	@Override
	public int hashCode()
		{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		return result;
		}

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof CharacterRange)) return false;
		CharacterRange other = (CharacterRange) obj;
		if (properties == null)
			{
			if (other.properties != null) return false;
			}
		else if (!properties.equals(other.properties)) return false;
		return true;
		}
	}
