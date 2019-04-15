/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.util.PropertyMap;

public class Include extends InstantiableResource<Include,Include.PInclude>
	{

	public byte[] data = new byte[0];

	public enum PInclude
		{
		FILENAME,FILEPATH,ORIGINAL,SIZE,EXPORTACTION,EXPORTFOLDER,OVERWRITE,FREEMEMORY,REMOVEATGAMEEND,
		STORE
		}

	private static final EnumMap<PInclude,Object> DEF = PropertyMap.makeDefaultMap(PInclude.class,"",
			"",true,0,2,"",false,true,true,false);

	public String filename = ""; //$NON-NLS-1$
	public String filepath = ""; //$NON-NLS-1$
	public boolean isOriginal;
	public int size = 0;
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

	@Override
	public Include makeInstance(ResourceReference<Include> ref)
		{
		return new Include();
		}

	@Override
	protected PropertyMap<PInclude> makePropertyMap()
		{
		return new PropertyMap<PInclude>(PInclude.class,this,DEF);
		}

	@Override
	protected void postCopy(Include dest)
		{ //Nothing else to copy

		}
	}
