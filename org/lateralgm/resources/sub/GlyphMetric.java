/**
* @file  GlyphMetric.java
* @brief Class implementing a Font Glyph interface.
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

import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class GlyphMetric implements UpdateListener,PropertyValidator<GlyphMetric.PGlyphMetric>
	{
	public enum PGlyphMetric
		{
		CHARACTER,X,Y,W,H,SHIFT,OFFSET
		}

	private static final EnumMap<PGlyphMetric,Object> DEFS = PropertyMap.makeDefaultMap(
			PGlyphMetric.class,0,0,0,0,0,0,0);

	public final PropertyMap<PGlyphMetric> properties;

	public GlyphMetric()
		{
		super();
		properties = new PropertyMap<PGlyphMetric>(PGlyphMetric.class,this,DEFS);
		}

	public void updated(UpdateEvent e)
		{
		// TODO Auto-generated method stub

		}

	public Object validate(PGlyphMetric k, Object v)
		{
		// TODO Auto-generated method stub
		return v;
		}
	}
