/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * 
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.util.PropertyMap;

public class Extensions extends Resource<Extensions,Extensions.PExtensions>
	{
	public enum PExtensions
		{
		//TODO: Extensions
		}

	private static final EnumMap<PExtensions,Object> DEF = null;

	@Override
	public Extensions makeInstance(ResourceReference<Extensions> ref)
		{
		return new Extensions();
		}

	@Override
	protected PropertyMap<PExtensions> makePropertyMap()
		{
		return new PropertyMap<PExtensions>(PExtensions.class,this,DEF);
		}

	@Override
	protected void postCopy(Extensions dest)
		{ //Nothing else to copy
		}
	}
