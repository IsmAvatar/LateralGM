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

	public enum ExportAction
		{
		DONT_EXPORT,TEMP_DIRECTORY,SAME_FOLDER,CUSTOM_FOLDER
		}

	public enum PInclude
		{
		FILENAME,FILEPATH,ORIGINAL,SIZE,EXPORTACTION,EXPORTFOLDER,OVERWRITE,FREEMEMORY,REMOVEATGAMEEND,
		STORE
		}

	private static final EnumMap<PInclude,Object> DEFS = PropertyMap.makeDefaultMap(PInclude.class,"", //$NON-NLS-1$
			"",true,0,ExportAction.SAME_FOLDER,"",false,true,true,false);  //$NON-NLS-1$//$NON-NLS-2$

	public Include()
		{
		this(null);
		}

	public Include(ResourceReference<Include> ref)
		{
		super(ref);
		}

	@Override
	public Include makeInstance(ResourceReference<Include> ref)
		{
		return new Include(ref);
		}

	@Override
	protected PropertyMap<PInclude> makePropertyMap()
		{
		return new PropertyMap<PInclude>(PInclude.class,this,DEFS);
		}

	@Override
	protected void postCopy(Include dest)
		{
		super.postCopy(dest);
		dest.data = data.clone();
		}
	}
