/*
 * Copyright (C) 2006, 2007, 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2009 Quadduc <quadduc@gmail.com>
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.GameSettings.ColorDepth;
import org.lateralgm.resources.GameSettings.Frequency;
import org.lateralgm.resources.GameSettings.IncludeFolder;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GameSettings.Priority;
import org.lateralgm.resources.GameSettings.ProgressBar;
import org.lateralgm.resources.GameSettings.Resolution;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sound.SoundKind;
import org.lateralgm.resources.Sprite.BBMode;
import org.lateralgm.resources.Sprite.MaskShape;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Trigger;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile.PTile;

public class GmFile implements UpdateListener
	{
	//Game Settings Enums
	public static final ColorDepth[] GS_DEPTHS = { ColorDepth.NO_CHANGE,ColorDepth.BIT_16,
			ColorDepth.BIT_32 };
	public static final Map<ColorDepth,Integer> GS_DEPTH_CODE;
	public static final Resolution[] GS5_RESOLS = { Resolution.RES_640X480,Resolution.RES_800X600,
			Resolution.RES_1024X768,Resolution.RES_1280X1024,Resolution.NO_CHANGE,Resolution.RES_320X240,
			Resolution.RES_1600X1200 };
	public static final Resolution[] GS_RESOLS = { Resolution.NO_CHANGE,Resolution.RES_320X240,
			Resolution.RES_640X480,Resolution.RES_800X600,Resolution.RES_1024X768,
			Resolution.RES_1280X1024,Resolution.RES_1600X1200 };
	public static final Map<Resolution,Integer> GS_RESOL_CODE;
	public static final Frequency[] GS_FREQS = { Frequency.NO_CHANGE,Frequency.FREQ_60,
			Frequency.FREQ_70,Frequency.FREQ_85,Frequency.FREQ_100,Frequency.FREQ_120 };
	public static final Map<Frequency,Integer> GS_FREQ_CODE;
	public static final Priority[] GS_PRIORITIES = { Priority.NORMAL,Priority.HIGH,Priority.HIGHEST };
	public static final Map<Priority,Integer> GS_PRIORITY_CODE;
	public static final ProgressBar[] GS_PROGBARS = { ProgressBar.NONE,ProgressBar.DEFAULT,
			ProgressBar.CUSTOM };
	public static final Map<ProgressBar,Integer> GS_PROGBAR_CODE;
	public static final IncludeFolder[] GS_INCFOLDERS = { IncludeFolder.MAIN,IncludeFolder.TEMP };
	public static final Map<IncludeFolder,Integer> GS_INCFOLDER_CODE;
	static
		{
		EnumMap<ColorDepth,Integer> m = new EnumMap<ColorDepth,Integer>(ColorDepth.class);
		for (int i = 0; i < GS_DEPTHS.length; i++)
			m.put(GS_DEPTHS[i],i);
		GS_DEPTH_CODE = Collections.unmodifiableMap(m);

		EnumMap<Resolution,Integer> m2 = new EnumMap<Resolution,Integer>(Resolution.class);
		for (int i = 0; i < GS_RESOLS.length; i++)
			m2.put(GS_RESOLS[i],i);
		GS_RESOL_CODE = Collections.unmodifiableMap(m2);

		EnumMap<Frequency,Integer> m3 = new EnumMap<Frequency,Integer>(Frequency.class);
		for (int i = 0; i < GS_FREQS.length; i++)
			m3.put(GS_FREQS[i],i);
		GS_FREQ_CODE = Collections.unmodifiableMap(m3);

		EnumMap<Priority,Integer> m4 = new EnumMap<Priority,Integer>(Priority.class);
		for (int i = 0; i < GS_PRIORITIES.length; i++)
			m4.put(GS_PRIORITIES[i],i);
		GS_PRIORITY_CODE = Collections.unmodifiableMap(m4);

		EnumMap<ProgressBar,Integer> m5 = new EnumMap<ProgressBar,Integer>(ProgressBar.class);
		for (int i = 0; i < GS_PROGBARS.length; i++)
			m5.put(GS_PROGBARS[i],i);
		GS_PROGBAR_CODE = Collections.unmodifiableMap(m5);

		EnumMap<IncludeFolder,Integer> m6 = new EnumMap<IncludeFolder,Integer>(IncludeFolder.class);
		for (int i = 0; i < GS_INCFOLDERS.length; i++)
			m6.put(GS_INCFOLDERS[i],i);
		GS_INCFOLDER_CODE = Collections.unmodifiableMap(m6);
		}

	public static final Resource.Kind[] RESOURCE_KIND = { null,Resource.Kind.OBJECT,
			Resource.Kind.SPRITE,Resource.Kind.SOUND,Resource.Kind.ROOM,null,Resource.Kind.BACKGROUND,
			Resource.Kind.SCRIPT,Resource.Kind.PATH,Resource.Kind.FONT,Resource.Kind.GAMEINFO,
			Resource.Kind.GAMESETTINGS,Resource.Kind.TIMELINE,Resource.Kind.EXTENSIONS };
	public static final Map<Resource.Kind,Integer> RESOURCE_CODE;
	static
		{
		EnumMap<Resource.Kind,Integer> m = new EnumMap<Resource.Kind,Integer>(Resource.Kind.class);
		for (int i = 0; i < RESOURCE_KIND.length; i++)
			if (RESOURCE_KIND[i] != null) m.put(RESOURCE_KIND[i],i);
		RESOURCE_CODE = Collections.unmodifiableMap(m);
		}
	public static final PSound[] SOUND_FX_FLAGS = { PSound.CHORUS,PSound.ECHO,PSound.FLANGER,
			PSound.GARGLE,PSound.REVERB };
	public static final SoundKind[] SOUND_KIND = { SoundKind.NORMAL,SoundKind.BACKGROUND,
			SoundKind.SPATIAL,SoundKind.MULTIMEDIA };
	public static final Map<SoundKind,Integer> SOUND_CODE;
	static
		{
		EnumMap<SoundKind,Integer> m = new EnumMap<SoundKind,Integer>(SoundKind.class);
		for (int i = 0; i < SOUND_KIND.length; i++)
			m.put(SOUND_KIND[i],i);
		SOUND_CODE = Collections.unmodifiableMap(m);
		}
	public static final BBMode[] SPRITE_BB_MODE = { BBMode.AUTO,BBMode.FULL,BBMode.MANUAL };
	public static final Map<BBMode,Integer> SPRITE_BB_CODE;
	static
		{
		EnumMap<BBMode,Integer> m = new EnumMap<BBMode,Integer>(BBMode.class);
		for (int i = 0; i < SPRITE_BB_MODE.length; i++)
			m.put(SPRITE_BB_MODE[i],i);
		SPRITE_BB_CODE = Collections.unmodifiableMap(m);
		}
	public static final MaskShape[] SPRITE_MASK_SHAPE = { MaskShape.PRECISE,MaskShape.RECTANGLE,
			MaskShape.DISK,MaskShape.DIAMOND };
	public static final Map<MaskShape,Integer> SPRITE_MASK_CODE;
	static
		{
		EnumMap<MaskShape,Integer> m = new EnumMap<MaskShape,Integer>(MaskShape.class);
		for (int i = 0; i < SPRITE_MASK_SHAPE.length; i++)
			m.put(SPRITE_MASK_SHAPE[i],i);
		SPRITE_MASK_CODE = Collections.unmodifiableMap(m);
		}

	/** One of 530, 600, 701, 800, 810 */
	public int fileVersion = 810;
	public String filename = null;

	private final EnumMap<Resource.Kind,ResourceList<?>> resMap;
	public final ResourceList<Sprite> sprites = new ResourceList<Sprite>(Sprite.class);
	public final ResourceList<Sound> sounds = new ResourceList<Sound>(Sound.class);
	public final ResourceList<Background> backgrounds = new ResourceList<Background>(//force newline
			Background.class);
	public final ResourceList<Path> paths = new ResourceList<Path>(Path.class);
	public final ResourceList<Script> scripts = new ResourceList<Script>(Script.class);
	public final ResourceList<Font> fonts = new ResourceList<Font>(Font.class);
	public final ResourceList<Timeline> timelines = new ResourceList<Timeline>(Timeline.class);
	public final ResourceList<GmObject> gmObjects = new ResourceList<GmObject>(GmObject.class);
	public final ResourceList<Room> rooms = new ResourceList<Room>(Room.class);

	public List<Trigger> triggers = new ArrayList<Trigger>();
	public List<Constant> constants = new ArrayList<Constant>();
	public List<Include> includes = new ArrayList<Include>();
	public List<String> packages = new ArrayList<String>();

	public GameInformation gameInfo = new GameInformation();
	public GameSettings gameSettings = new GameSettings();
	public int lastInstanceId = 100000;
	public int lastTileId = 10000000;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public GmFile()
		{
		resMap = new EnumMap<Resource.Kind,ResourceList<?>>(Resource.Kind.class);
		resMap.put(Resource.Kind.SPRITE,sprites);
		resMap.put(Resource.Kind.SOUND,sounds);
		resMap.put(Resource.Kind.BACKGROUND,backgrounds);
		resMap.put(Resource.Kind.PATH,paths);
		resMap.put(Resource.Kind.SCRIPT,scripts);
		resMap.put(Resource.Kind.FONT,fonts);
		resMap.put(Resource.Kind.TIMELINE,timelines);
		resMap.put(Resource.Kind.OBJECT,gmObjects);
		resMap.put(Resource.Kind.ROOM,rooms);
		for (ResourceList<?> rl : resMap.values())
			{
			rl.updateSource.addListener(this);
			}
		Random random = new Random();
		gameSettings.put(PGameSettings.GAME_ID,random.nextInt(100000001));
		random.nextBytes((byte[]) gameSettings.get(PGameSettings.DPLAY_GUID));
		try
			{
			String loc = "org/lateralgm/file/default.ico";
			InputStream filein;
			File file = new File(loc);
			if (!file.exists())
				filein = LGM.class.getClassLoader().getResourceAsStream(loc);
			else
				filein = new FileInputStream(file);
			gameSettings.put(PGameSettings.GAME_ICON,new ICOFile(filein));
			}
		catch (Exception ex)
			{
			System.err.println(Messages.getString("GmFile.NOICON")); //$NON-NLS-1$
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			}
		}

	public static Calendar gmBaseTime()
		{
		Calendar res = Calendar.getInstance();
		res.set(1899,11,29,23,59,59);
		return res;
		}

	public static double longTimeToGmTime(long time)
		{
		return (time - gmBaseTime().getTimeInMillis()) / 86400000d;
		}

	public static String gmTimeToString(double time)
		{
		Calendar base = gmBaseTime();
		base.setTimeInMillis(base.getTimeInMillis() + ((long) (time * 86400000)));
		return DateFormat.getDateTimeInstance().format(base.getTime());
		}

	// Returns the ResourceList corresponding to given Resource constant
	public ResourceList<?> getList(Resource.Kind res)
		{
		return resMap.get(res);
		}

	public void defragIds()
		{
		Iterator<ResourceList<?>> iter = resMap.values().iterator();
		while (iter.hasNext())
			iter.next().defragIds();
		lastInstanceId = 100000;
		lastTileId = 10000000;
		for (Room r : rooms)
			{
			for (Instance j : r.instances)
				j.properties.put(PInstance.ID,++lastInstanceId);
			for (Tile j : r.tiles)
				j.properties.put(PTile.ID,++lastTileId);
			}
		}

	public static List<Constant> copyConstants(List<Constant> source)
		{
		List<Constant> dest = new ArrayList<Constant>();
		for (Constant c : source)
			dest.add(c.copy());
		return dest;
		}

	public void updated(UpdateEvent e)
		{
		updateTrigger.fire(e);
		}
	}
