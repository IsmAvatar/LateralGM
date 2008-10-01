/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;

public class Sound extends Resource<Sound>
	{
	public static final byte SOUND_NORMAL = 0;
	public static final byte SOUND_BACKGROUND = 1;
	public static final byte SOUND_3D = 2;
	public static final byte SOUND_MULTIMEDIA = 3;

	public static final byte FX_CHORUS = 1;
	public static final byte FX_ECHO = 2;
	public static final byte FX_FLANGER = 4;
	public static final byte FX_GARGLE = 8;
	public static final byte FX_REVERB = 16;

	public int kind = SOUND_NORMAL;
	public String fileType = "";
	public String fileName = "";
	public boolean chorus = false;
	public boolean echo = false;
	public boolean flanger = false;
	public boolean gargle = false;
	public boolean reverb = false;
	public double volume = 1;
	public double pan = 0;
	public boolean preload = true;
	public byte[] data = new byte[0];

	public Sound()
		{
		this(null,true);
		}

	public Sound(ResourceReference<Sound> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes[Resource.SOUND]);
		}

	public static boolean hasEffect(int effects, int type)
		{
		return (effects & type) != 0;
		}

	public static int makeEffects(boolean chorus, boolean echo, boolean flanger, boolean gargle,
			boolean reverb)
		{
		return (chorus ? 1 : 0) | (echo ? 2 : 0) | (flanger ? 4 : 0) | (gargle ? 8 : 0)
				| (reverb ? 16 : 0);
		}

	public void setEffects(int val)
		{
		chorus = hasEffect(val,FX_CHORUS);
		echo = hasEffect(val,FX_ECHO);
		flanger = hasEffect(val,FX_FLANGER);
		gargle = hasEffect(val,FX_GARGLE);
		reverb = hasEffect(val,FX_REVERB);
		}

	public int getEffects()
		{
		return makeEffects(chorus,echo,flanger,gargle,reverb);
		}

	@Override
	protected Sound copy(ResourceList<Sound> src, ResourceReference<Sound> ref, boolean update)
		{
		Sound s = new Sound(ref,update);
		s.kind = kind;
		s.fileType = fileType;
		s.fileName = fileName;
		s.chorus = chorus;
		s.echo = echo;
		s.flanger = flanger;
		s.gargle = gargle;
		s.reverb = reverb;
		s.volume = volume;
		s.pan = pan;
		s.preload = preload;
		s.data = new byte[data.length];
		System.arraycopy(data,0,s.data,0,data.length);
		if (src != null)
			{
			s.setName(Prefs.prefixes[Resource.SOUND] + (src.lastId + 1));
			src.add(s);
			}
		else
			{
			s.setId(getId());
			s.setName(getName());
			}
		return s;
		}

	public byte getKind()
		{
		return SOUND;
		}
	}
