/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

public class GmFormatException extends ProjectFormatException
	{
	private static final long serialVersionUID = 1L;
	public ProjectFile file;
	public Exception e;
	public Throwable cause;

	public GmFormatException(ProjectFile file, String message)
		{
		super(file,message);
		this.file = file;
		}

	public GmFormatException(ProjectFile file, String message, Throwable cause)
		{
		super(file,message,cause);
		this.file = file;
		this.cause = cause;
		}

	public GmFormatException(ProjectFile file, Exception e)
		{
		super(file,e);
		this.e = e;
		this.file = file;
		}
	}
