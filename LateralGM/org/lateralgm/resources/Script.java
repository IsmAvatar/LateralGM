/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;

public class Script extends Resource
	{
	public String scriptStr = "";

	public Script()
		{
		setName(Prefs.prefixes[Resource.SCRIPT]);
		}

	@SuppressWarnings("unchecked")
	private Script copy(boolean update, ResourceList src)
		{
		Script scr = new Script();
		scr.scriptStr = scriptStr;
		if (update)
			{
			scr.setId(new ResId(++src.lastId));
			scr.setName(Prefs.prefixes[Resource.SCRIPT] + src.lastId);
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

	@SuppressWarnings("unchecked")
	public Script copy(ResourceList src)
		{
		return copy(true,src);
		}

	public Script copy()
		{
		return copy(false,null);
		}
	}
