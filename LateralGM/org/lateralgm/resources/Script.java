/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.util.PropertyMap;

public class Script extends Resource<Script,Script.PScript>
	{
	public enum PScript
		{
		CODE
		}

	private static final EnumMap<PScript,Object> DEFS = PropertyMap.makeDefaultMap(PScript.class,"");

	public Script()
		{
		this(null,true);
		}

	public Script(ResourceReference<Script> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes.get(Kind.SCRIPT));
		}

	@Override
	protected Script copy(ResourceList<Script> src, ResourceReference<Script> ref, boolean update)
		{
		Script s = new Script(ref,update);
		copy(src,s);
		return s;
		}

	public Kind getKind()
		{
		return Kind.SCRIPT;
		}

	@Override
	protected PropertyMap<PScript> makePropertyMap()
		{
		return new PropertyMap<PScript>(PScript.class,this,DEFS);
		}
	}
