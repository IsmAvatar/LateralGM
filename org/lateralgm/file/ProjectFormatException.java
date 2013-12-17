/*
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * 
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.file;

public class ProjectFormatException extends Exception
	{
	private static final long serialVersionUID = 1L;
	public ProjectFile file;
	public Exception e;

	public ProjectFormatException(ProjectFile file, String message)
		{
		super(message);
		this.file = file;
		}

	public ProjectFormatException(ProjectFile file, Exception e)
		{
		super(e.getClass().getName() + ": " + e.getMessage());
		this.e = e;
		this.file = file;
		}

	public String stackAsString()
		{
		StackTraceElement[] els = (e == null ? getStackTrace() : e.getStackTrace());
		String res = ""; //$NON-NLS-1$
		for (int i = 0; i < els.length; i++)
			{
			res += els[i].toString();
			if (i != els.length - 1) res += "\n"; //$NON-NLS-1$
			}
		return res;
		}
	}
