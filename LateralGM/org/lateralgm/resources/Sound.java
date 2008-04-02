/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
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

	private Sound copy(boolean update, ResourceList<Sound> src)
		{
		Sound snd = new Sound();
		snd.kind = kind;
		snd.fileType = fileType;
		snd.fileName = fileName;
		snd.chorus = chorus;
		snd.echo = echo;
		snd.flanger = flanger;
		snd.gargle = gargle;
		snd.reverb = reverb;
		snd.volume = volume;
		snd.pan = pan;
		snd.preload = preload;
		snd.data = new byte[data.length];
		System.arraycopy(data,0,snd.data,0,data.length);
		if (update)
			{
			snd.setName(Prefs.prefixes[Resource.SOUND] + (src.lastId + 1));
			src.add(snd);
			}
		else
			{
			snd.setId(getId());
			snd.setName(getName());
			}
		return snd;
		}

	public Sound copy()
		{
		return copy(false,null);
		}

	public Sound copy(ResourceList<Sound> src)
		{
		return copy(true,src);
		}

	public byte getKind()
		{
		return SOUND;
		}
	}
