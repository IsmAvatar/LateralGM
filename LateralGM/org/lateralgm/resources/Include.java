/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

public class Include
	{
	public String filePath = "";

	public Include()
		{
		}

	public Include(String filePath)
		{
		this.filePath = filePath;
		}

	public Include copy()
		{
		return new Include(filePath);
		}

	public String toString()
		{
		return filePath;
		}
	}
