/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
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
		setName(Prefs.prefixes[Resource.SCRIPT]);
		}

	private Script copy(boolean update, ResourceList<Script> src)
		{
		Script scr = new Script();
		scr.scriptStr = scriptStr;
		if (update)
			{
			scr.setName(Prefs.prefixes[Resource.SCRIPT] + (src.lastId + 1));
			src.add(scr);
			}
		else
			{
			scr.setId(getId());
			scr.setName(getName());
			}
		return scr;
		}

	public byte getKind()
		{
		return SCRIPT;
		}

	public Script copy(ResourceList<Script> src)
		{
		return copy(true,src);
		}

	public Script copy()
		{
		return copy(false,null);
		}
	}
