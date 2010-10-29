/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

public class Include
	{
	public String filename = ""; //$NON-NLS-1$
	public String filepath = ""; //$NON-NLS-1$
	public boolean isOriginal;
	public int size = 0;
	public byte[] data =  null;
	public int export = 2;
	public String exportFolder = ""; //$NON-NLS-1$
	public boolean overwriteExisting = false;
	public boolean freeMemAfterExport = true;
	public boolean removeAtGameEnd = true;

	public Include copy()
		{
		Include inc = new Include();
		inc.filename = filename;
		inc.filepath = filepath;
		inc.isOriginal = isOriginal;
		inc.size = size;
		inc.data = data;
		inc.export = export;
		inc.exportFolder = exportFolder;
		inc.overwriteExisting = overwriteExisting;
		inc.freeMemAfterExport = freeMemAfterExport;
		inc.removeAtGameEnd = removeAtGameEnd;
		return inc;
		}

	public String toString()
		{
		return filepath;
		}
	}
