/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

public class Gm6FormatException extends Exception
	{
	private static final long serialVersionUID = 1L;

	public Gm6FormatException(String message)
		{
		super(message);
		}

	public String stackAsString()
		{
		StackTraceElement[] els = getStackTrace();
		String res = "";
		for (int i = 0; i < els.length; i++)
			{
			res += els[i].toString();
			if (i != els.length - 1) res += "\n";
			}
		return res;
		}
	}