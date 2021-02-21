/**
* @file  TextureGroup.java
* @brief Class implementing a texture group interface.
*
* @section License
*
* Copyright (C) 2020 Robert B. Colton
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

import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class TextureGroup implements PropertyValidator<TextureGroup.PTextureGroup>
	{
	public enum PTextureGroup
		{
		NAME,SCALED,CROPPED,BORDER_WIDTH,PARENT
		}

	private static final EnumMap<PTextureGroup,Object> DEFS = PropertyMap.makeDefaultMap(
			PTextureGroup.class,"Default",false,false,2,null);

	public final PropertyMap<PTextureGroup> properties;

	public TextureGroup()
		{
		super();
		properties = new PropertyMap<PTextureGroup>(PTextureGroup.class,this,DEFS);
		}
	
	public Object validate(PTextureGroup k, Object v)
		{
		return v;
		}

	@Override
	public String toString()
		{
		return properties.get(PTextureGroup.NAME);
		}
	}
