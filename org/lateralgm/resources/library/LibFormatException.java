/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.library;

public class LibFormatException extends Exception
	{
	private static final long serialVersionUID = 1L;

	public LibFormatException(String message)
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
