/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

public class GmFormatException extends Exception
	{
	private static final long serialVersionUID = 1L;

	public GmFormatException(String message)
		{
		super(message);
		}

	public String stackAsString()
		{
		StackTraceElement[] els = getStackTrace();
		String res = ""; //$NON-NLS-1$
		for (int i = 0; i < els.length; i++)
			{
			res += els[i].toString();
			if (i != els.length - 1) res += "\n"; //$NON-NLS-1$
			}
		return res;
		}
	}
