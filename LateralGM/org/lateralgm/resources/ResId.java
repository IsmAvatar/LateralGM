/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

public class ResId
	{
	public int value;

	public ResId(int value)
		{
		this.value = value;
		}

	public String toString()
		{
		return Integer.toString(value);
		}
	}