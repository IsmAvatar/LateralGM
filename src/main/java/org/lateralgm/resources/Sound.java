/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.util.PropertyMap;

public class Sound extends InstantiableResource<Sound,Sound.PSound>
	{
	public byte[] data = new byte[0];

	public enum SoundKind
		{
		NORMAL,BACKGROUND,SPATIAL,MULTIMEDIA
		}

	public enum SoundType
		{
		MONO,STEREO,THREED
		}

	//NOTE: Kind is different than type in the GMX format
	public enum PSound
		{
		KIND,FILE_TYPE,FILE_NAME,CHORUS,ECHO,FLANGER,GARGLE,REVERB,VOLUME,PAN,PRELOAD,BIT_RATE,
		SAMPLE_RATE,TYPE,BIT_DEPTH,COMPRESSED,STREAMED,DECOMPRESS_ON_LOAD
		}

	private static final EnumMap<PSound,Object> DEFS = PropertyMap.makeDefaultMap(PSound.class,
			SoundKind.NORMAL,"","",false,false,false,false,false,1.0,0.0,true,192,44100,SoundType.MONO,
			16,false,false,false);

	public Sound()
		{
		this(null);
		}

	public Sound(ResourceReference<Sound> r)
		{
		super(r);
		}

	public Sound makeInstance(ResourceReference<Sound> r)
		{
		return new Sound(r);
		}

	@Override
	protected void postCopy(Sound dest)
		{
		super.postCopy(dest);
		dest.data = new byte[data.length];
		System.arraycopy(data,0,dest.data,0,data.length);
		}

	@Override
	protected PropertyMap<PSound> makePropertyMap()
		{
		return new PropertyMap<PSound>(PSound.class,this,DEFS);
		}
	}
