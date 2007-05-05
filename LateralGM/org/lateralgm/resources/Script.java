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
	public String ScriptStr = "";

	public Script()
		{
		name = Prefs.prefixes[Resource.SCRIPT];
		}

	public Script copy(boolean update, ResourceList src)
		{
		Script scr = new Script();
		scr.ScriptStr = ScriptStr;
		if (update)
			{
			scr.Id.value = ++src.LastId;
			scr.name = Prefs.prefixes[Resource.SCRIPT] + src.LastId;
			src.add(scr);
			}
		else
			{
			scr.Id = Id;
			scr.name = name;
			}
		return scr;
		}
	}