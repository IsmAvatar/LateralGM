/*
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.subframes.CodeFrame.CodeHolder;
import org.lateralgm.util.PropertyMap;

public class Shader extends InstantiableResource<Shader,Shader.PShader>
	{
	public enum PShader
		{
		VERTEX,FRAGMENT,TYPE,PRECOMPILE
		}

	private static final EnumMap<PShader,Object> DEFS = PropertyMap.makeDefaultMap(PShader.class,"","","GLSLES",false);

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
