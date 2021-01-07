/**
* @file  Shader.java
* @brief Class implementing a shader resource with vertex and fragment code for multiple shading languages.
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

package org.lateralgm.resources;

import java.io.InputStream;
import java.util.EnumMap;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.util.PropertyMap;

public class Shader extends InstantiableResource<Shader,Shader.PShader>
	{
	// plugins may override these to change contents of newly created shaders
	public static String TEMPLATE_VERTEX = "";
	public static String TEMPLATE_FRAGMENT = "";

	static
		{
		ClassLoader cl = Shader.class.getClassLoader();
		try (InputStream vis = cl.getResourceAsStream("org/lateralgm/resources/shader_template.vert");
				 InputStream fis = cl.getResourceAsStream("org/lateralgm/resources/shader_template.frag"))
			{
			TEMPLATE_VERTEX = new String(Util.readFully(vis).toByteArray());
			TEMPLATE_FRAGMENT = new String(Util.readFully(fis).toByteArray());
			}
		catch (Exception e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		}

	public enum PShader
		{
		VERTEX,FRAGMENT,TYPE,PRECOMPILE
		}

	private static final EnumMap<PShader,Object> DEFS = PropertyMap.makeDefaultMap(PShader.class,TEMPLATE_VERTEX,
			TEMPLATE_FRAGMENT,"GLSLES",true);

	public Shader()
		{
		this(null);
		}

	public Shader(ResourceReference<Shader> r)
		{
		super(r);
		}

	public Shader makeInstance(ResourceReference<Shader> r)
		{
		return new Shader(r);
		}

	@Override
	protected PropertyMap<PShader> makePropertyMap()
		{
		return new PropertyMap<PShader>(PShader.class,this,DEFS);
		}

	public String getVertexCode()
		{
		return properties.get(PShader.VERTEX);
		}

	public boolean getPrecompile()
		{
		return properties.get(PShader.PRECOMPILE);
		}

	public String getType()
		{
		return properties.get(PShader.TYPE);
		}

	public void setVertexCode(String s)
		{
		properties.put(PShader.VERTEX,s);
		}

	public String getFragmentCode()
		{
		return properties.get(PShader.FRAGMENT);
		}

	public void setFragmentCode(String s)
		{
		properties.put(PShader.FRAGMENT,s);
		}
	}
