/**
* @file  MarkerCache.java
* @brief Class implementing token marker caching for efficiency.
*
* @section License
*
* Copyright (C) 2013 Robert B. Colton
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

package org.lateralgm.components;

import java.util.HashMap;
import java.util.Map;

import org.lateralgm.joshedit.DefaultTokenMarker;
import org.lateralgm.joshedit.lexers.GLESTokenMarker;
import org.lateralgm.joshedit.lexers.GLSLTokenMarker;
import org.lateralgm.joshedit.lexers.GMLTokenMarker;
import org.lateralgm.joshedit.lexers.HLSLTokenMarker;

/**
 * A non-instantiable token marker cache that avoids instantiating more
 * markers than is necessary.
 *
 * @author Robert B. Colton
 */
public final class MarkerCache
	{
	/** The cached token markers keyed with the language **/
	private static Map<String,DefaultTokenMarker> markers = new HashMap<String,DefaultTokenMarker>();

	/**
	 * Get one of the cached markers or cache it if it doesn't exist.
	 *
	 * @param language
	 *            One of available token markers, eg. "glsles", "glsl", "gml", "hlsl9", "hlsl11"
	 **/
	public static DefaultTokenMarker getMarker(String language)
		{
		language = language.toLowerCase();
		DefaultTokenMarker marker = markers.get(language);
		if (marker == null)
			{
			switch (language)
				{
				case "glsles": marker = new GLESTokenMarker(); break;
				case "glsl": marker = new GLSLTokenMarker(); break;
				case "hlsl9": marker = new HLSLTokenMarker(); break;
				case "hlsl11": marker = new HLSLTokenMarker(); break;
				case "gml": marker = new GMLTokenMarker(); break;
				}
			markers.put(language,marker);
			}
		return marker;
		}

	}
