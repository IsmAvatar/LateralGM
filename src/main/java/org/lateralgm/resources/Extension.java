/**
* @file  Extension.java
* @brief Class implementing the instiable Extension resource
*
* @section License
*
* Copyright (C) 2013-2014 Robert B. Colton
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

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.resources.sub.ExtensionFile;
import org.lateralgm.resources.sub.ExtensionFunction;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.PropertyMap;

public class Extension extends InstantiableResource<Extension,Extension.PExtension>
	{

	public final ActiveArrayList<ExtensionFile> files = new ActiveArrayList<ExtensionFile>();
	public final ActiveArrayList<ExtensionFunction> functions = new ActiveArrayList<ExtensionFunction>();

	public enum PExtension
		{
		//TODO: Extensions
		NAME,VERSION,AUTHOR,DATE,LICENSE,DESCRIPTION,HELPFILE,INSTALLDIR
		}

	private static final EnumMap<PExtension,Object> DEF = null;

	@Override
	public Extension makeInstance(ResourceReference<Extension> ref)
		{
		return new Extension();
		}

	@Override
	protected PropertyMap<PExtension> makePropertyMap()
		{
		return new PropertyMap<PExtension>(PExtension.class,this,DEF);
		}

	@Override
	protected void postCopy(Extension dest)
		{ //Nothing else to copy
		}
	}
