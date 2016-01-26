/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
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

public class Script extends InstantiableResource<Script,Script.PScript> implements CodeHolder
	{
	public enum PScript
		{
		CODE
		}

	private static final EnumMap<PScript,Object> DEFS = PropertyMap.makeDefaultMap(PScript.class,"");

	public Script()
		{
		this(null);
		}

	public Script(ResourceReference<Script> r)
		{
		super(r);
		}

	public Script makeInstance(ResourceReference<Script> r)
		{
		return new Script(r);
		}

	@Override
	protected PropertyMap<PScript> makePropertyMap()
		{
		return new PropertyMap<PScript>(PScript.class,this,DEFS);
		}

	public String getCode()
		{
		return properties.get(PScript.CODE);
		}

	public void setCode(String s)
		{
		properties.put(PScript.CODE,s);
		}
	}
