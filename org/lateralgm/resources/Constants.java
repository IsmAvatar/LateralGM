/*
 * Copyright (C) 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.lateralgm.resources.sub.Constant;
import org.lateralgm.util.PropertyMap;

public class Constants extends Resource<Constants,Constants.PConstants>
	{

	public List<Constant> constants = new ArrayList<Constant>();

	public enum PConstants
		{
		//TODO:
		}

	private static final EnumMap<PConstants,Object> DEF = null;

	@Override
	public Constants makeInstance(ResourceReference<Constants> ref)
		{
		return new Constants();
		}

	@Override
	protected void postCopy(Constants dest)
		{
		dest.constants.clear();
		for (Constant cnst : constants)
			{
			Constant r2 = cnst.copy();
			dest.constants.add(r2);
			}
		}

	public Object validate(PConstants k, Object v)
		{
		return v;
		}

	public void put(PConstants key, Object value)
		{
		properties.put(key,value);
		}

	public <V>V get(PConstants key)
		{
		return properties.get(key);
		}

	@Override
	protected PropertyMap<PConstants> makePropertyMap()
		{
		return new PropertyMap<PConstants>(PConstants.class,this,DEF);
		}

	}
