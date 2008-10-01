/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;

public class Script extends Resource<Script>
	{
	public String scriptStr = "";

	public Script()
		{
		this(null,true);
		}

	public Script(ResourceReference<Script> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes[Resource.SCRIPT]);
		}

	@Override
	protected Script copy(ResourceList<Script> src, ResourceReference<Script> ref, boolean update)
		{
		Script s = new Script(ref,update);
		s.scriptStr = scriptStr;
		if (src != null)
			{
			s.setName(Prefs.prefixes[Resource.SCRIPT] + (src.lastId + 1));
			src.add(s);
			}
		else
			{
			s.setId(getId());
			s.setName(getName());
			}
		return s;
		}

	public byte getKind()
		{
		return SCRIPT;
		}
	}
