/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;

public class Sound extends Resource
	{
	public static final byte NORMAL = 0;
	public static final byte BACKGROUND = 1;
	public static final byte THREE = 2;
	public static final byte MULTIMEDIA = 3;

	public int kind = NORMAL;
	public String FileType = "";
	public String FileName = "";
	public boolean chorus = false;
	public boolean echo = false;
	public boolean flanger = false;
	public boolean gargle = false;
	public boolean reverb = false;
	public double volume = 1;
	public double pan = 0;
	public boolean preload = true;
	public byte[] Data;

	public Sound()
		{
		name = Prefs.prefixes[Resource.SOUND];
		}

	public static boolean getChorus(int effects)
		{
		return ((effects & 1) != 0);
		}

	public static boolean getEcho(int effects)
		{
		return ((effects & 2) != 0);
		}

	public static boolean getFlanger(int effects)
		{
		return ((effects & 4) != 0);
		}

	public static boolean getGargle(int effects)
		{
		return ((effects & 8) != 0);
		}

	public static boolean getReverb(int effects)
		{
		return ((effects & 16) != 0);
		}

	public static int makeEffects(boolean chorus, boolean echo, boolean flanger, boolean gargle, boolean reverb)
		{
		return (chorus ? 1 : 0) | (echo ? 2 : 0) | (flanger ? 4 : 0) | (gargle ? 8 : 0) | (reverb ? 16 : 0);
		}

	public void setEffects(int val)
		{
		chorus = getChorus(val);
		echo = getEcho(val);
		flanger = getFlanger(val);
		gargle = getGargle(val);
		reverb = getReverb(val);
		}

	public int getEffects()
		{
		return makeEffects(chorus,echo,flanger,gargle,reverb);
		}

	public Sound copy(boolean update, ResourceList src)
		{
		Sound snd = new Sound();
		snd.kind = kind;
		snd.FileType = FileType;
		snd.FileName = FileName;
		snd.chorus = chorus;
		snd.echo = echo;
		snd.flanger = flanger;
		snd.gargle = gargle;
		snd.reverb = reverb;
		snd.volume = volume;
		snd.pan = pan;
		snd.preload = preload;
		snd.Data = new byte[Data.length];
		System.arraycopy(Data,0,snd.Data,0,Data.length);
		if (update)
			{
			snd.Id.value = ++src.LastId;
			snd.name = Prefs.prefixes[Resource.SOUND] + src.LastId;
			src.add(snd);
			}
		else
			{
			snd.Id = Id;
			snd.name = name;
			}
		return snd;
		}
	}