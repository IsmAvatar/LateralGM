/*
 * Copyright (C) 2006, 2007, 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
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
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.ExtensionPackages;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GameSettings.ColorDepth;
import org.lateralgm.resources.GameSettings.Frequency;
import org.lateralgm.resources.GameSettings.IncludeFolder;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GameSettings.Priority;
import org.lateralgm.resources.GameSettings.ProgressBar;
import org.lateralgm.resources.GameSettings.Resolution;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PhysicsShape;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sound.SoundKind;
import org.lateralgm.resources.Sound.SoundType;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.BBMode;
import org.lateralgm.resources.Sprite.MaskShape;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.Trigger;

public class ProjectFile implements UpdateListener
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

	public static final Class<?>[] RESOURCE_KIND = { null,GmObject.class,Sprite.class,Sound.class,
			Room.class,null,Background.class,Script.class,Path.class,Font.class,GameInformation.class,
			GameSettings.class,Timeline.class,ExtensionPackages.class,Shader.class };
	public static final Map<Class<?>,Integer> RESOURCE_CODE;
	static
		{
		Map<Class<?>,Integer> m = new HashMap<Class<?>,Integer>();
		for (int i = 0; i < RESOURCE_KIND.length; i++)
			if (RESOURCE_KIND[i] != null) m.put(RESOURCE_KIND[i],i);
		RESOURCE_CODE = Collections.unmodifiableMap(m);
		}
	public static final PSound[] SOUND_FX_FLAGS = { PSound.CHORUS,PSound.ECHO,PSound.FLANGER,
			PSound.GARGLE,PSound.REVERB };
	public static final SoundKind[] SOUND_KIND = { SoundKind.NORMAL,SoundKind.BACKGROUND,
			SoundKind.SPATIAL,SoundKind.MULTIMEDIA };
	public static final SoundType[] SOUND_TYPE = { SoundType.MONO, SoundType.STEREO,
			SoundType.THREED };
	public static final Map<SoundKind,Integer> SOUND_KIND_CODE;
	static
		{
		EnumMap<SoundKind,Integer> m = new EnumMap<SoundKind,Integer>(SoundKind.class);
		for (int i = 0; i < SOUND_KIND.length; i++)
			m.put(SOUND_KIND[i],i);
		SOUND_KIND_CODE = Collections.unmodifiableMap(m);
		}
	public static final Map<SoundType,Integer> SOUND_TYPE_CODE;
	static
		{
		EnumMap<SoundType,Integer> m = new EnumMap<SoundType,Integer>(SoundType.class);
		for (int i = 0; i < SOUND_TYPE.length; i++)
			m.put(SOUND_TYPE[i],i);
		SOUND_TYPE_CODE = Collections.unmodifiableMap(m);
		}
	public static final PhysicsShape[] PHYSICS_SHAPE = { PhysicsShape.CIRCLE,PhysicsShape.BOX,
			PhysicsShape.SHAPE };
	public static final Map<PhysicsShape,Integer> SHAPE_CODE;
	static
		{
		EnumMap<PhysicsShape,Integer> m = new EnumMap<PhysicsShape,Integer>(PhysicsShape.class);
		for (int i = 0; i < PHYSICS_SHAPE.length; i++)
			m.put(PHYSICS_SHAPE[i],i);
		SHAPE_CODE = Collections.unmodifiableMap(m);
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
		m.put(MaskShape.POLYGON,m.get(MaskShape.RECTANGLE));
		SPRITE_MASK_CODE = Collections.unmodifiableMap(m);
		}

	public String getPath()
		{
		return uri.getPath().replace("\\","/");
		}

	// This will return the top level folder path with name that the
	// main project file is in.
	public String getDirectory()
		{
		String path = "";
		if (uri != null)
			{
			path = uri.getPath().replace("\\","/");
			}
		File f = new File(path);
		if (f.exists())
			{
			return f.getParent();
			}
		return path;
		}

	public FormatFlavor format;
	public URI uri;

	public static interface ResourceHolder<T extends Resource<T,?>>
		{
		T getResource();
		}

	public static class SingletonResourceHolder<T extends Resource<T,?>> implements ResourceHolder<T>
		{
		T r;

		public SingletonResourceHolder(T r)
			{
			this.r = r;
			}

		public T getResource()
			{
			return r;
			}
		}

	@SuppressWarnings("unchecked")
	public class ResourceMap extends HashMap<Class<?>,ResourceHolder<?>>
		{
		private static final long serialVersionUID = 1L;

		public <R extends Resource<R,?>>ResourceHolder<R> put(Class<R> key, ResourceHolder<R> value)
			{
			return (ResourceHolder<R>) super.put(key,value);
			}

		public <R extends Resource<R,?>>ResourceHolder<R> get(Class<R> key)
			{
			return (ResourceHolder<R>) super.get(key);
			}

		public <R extends InstantiableResource<R,?>>ResourceList<R> getList(Class<R> key)
			{
			return (ResourceList<R>) super.get(key);
			}

		@SuppressWarnings("rawtypes")
		public void addList(Class<?> kind)
			{
			put(kind,new ResourceList(kind));
			}
		}

	public final ResourceMap resMap;

	public SortedMap<Integer,Trigger> triggers = new TreeMap<Integer,Trigger>();
	public List<String> packages = new ArrayList<String>();

	public Constants defaultConstants = new Constants();
	public GameInformation gameInfo = new GameInformation();
	public Vector<GameSettings> gameSettings = new Vector<GameSettings>();
	public ExtensionPackages extPackages = new ExtensionPackages();
	public int lastInstanceId = 100000;
	public int lastTileId = 10000000;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public static class FormatFlavor
		{
		public static final String GM_OWNER = "GM";
		public static final String GMX_OWNER = "GMX";
		public static final FormatFlavor GM_530 = new FormatFlavor(GM_OWNER,530);
		public static final FormatFlavor GM_600 = new FormatFlavor(GM_OWNER,600);
		public static final FormatFlavor GM_701 = new FormatFlavor(GM_OWNER,701);
		public static final FormatFlavor GM_800 = new FormatFlavor(GM_OWNER,800);
		public static final FormatFlavor GM_810 = new FormatFlavor(GM_OWNER,810);
		public static final FormatFlavor GMX = new FormatFlavor(GMX_OWNER,1);

		protected Object owner;
		protected int version;

		public FormatFlavor(Object owner, int version)
			{
			this.owner = owner;
			this.version = version;
			}

		public static FormatFlavor getVersionFlavor(int ver)
			{
			switch (ver)
				{
				case 530:
					return FormatFlavor.GM_530;
				case 600:
					return FormatFlavor.GM_600;
				case 701:
					return FormatFlavor.GM_701;
				case 800:
					return FormatFlavor.GM_800;
				case 810:
					return FormatFlavor.GM_810;
				default:
					return null;
				}
			}

		public Object getOwner()
			{
			return owner;
			}

		public int getVersion()
			{
			return version;
			}
		}

	public ProjectFile()
		{
		resMap = new ResourceMap();
		for (Class<?> kind : Resource.kinds)
			if (InstantiableResource.class.isAssignableFrom(kind)) resMap.addList(kind);


		// Default initial configuration
		GameSettings gs = createDefaultConfig();
		gs.setName("Default");
		gameSettings.add(gs);

		resMap.put(Constants.class,new SingletonResourceHolder<Constants>(defaultConstants));
		resMap.put(GameInformation.class,new SingletonResourceHolder<GameInformation>(gameInfo));
		// TODO: We don't need this anymore. It should however still be iterable, perhaps we should
		// make a Config resource to manage all game configurations? - Robert
		//resMap.put(GameSettings.class,new SingletonResourceHolder<GameSettings>(gs));
		resMap.put(ExtensionPackages.class,new SingletonResourceHolder<ExtensionPackages>(extPackages));
		for (ResourceHolder<?> rl : resMap.values())
			if (rl instanceof ResourceList<?>) ((ResourceList<?>) rl).updateSource.addListener(this);
		}

	public static GameSettings createDefaultConfig() {
		GameSettings gs = new GameSettings();
		Random random = new Random();
		gs.put(PGameSettings.GAME_ID,random.nextInt(100000001));
		random.nextBytes((byte[]) gs.get(PGameSettings.GAME_GUID));
		return gs;
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

	public boolean checkIds()
		{
		Iterator<ResourceHolder<?>> iter = resMap.values().iterator();
		while (iter.hasNext())
			{
			ResourceHolder<?> rh = iter.next();
			if (!(rh instanceof ResourceList<?>)) continue;
			Set<Integer> set = new HashSet<Integer>();
			ResourceList<?> rl = (ResourceList<?>) rh;
			for (Object o : rl)
				{
				if (!(o instanceof InstantiableResource<?,?>)) continue;
				InstantiableResource<?,?> r = (InstantiableResource<?,?>) o;
				if (set.contains(r.getId())) return true;
				set.add(r.getId());
				}
			}
		Set<Integer> instancesSet = new HashSet<Integer>();
		Set<Integer> tilesSet = new HashSet<Integer>();
		for (Room r : resMap.getList(Room.class))
			{
			for (Instance j : r.instances)
				if (instancesSet.contains(j.getID())) return true;
				else instancesSet.add(j.getID());
			for (Tile j : r.tiles)
				if (tilesSet.contains(j.getID())) return true;
				else tilesSet.add(j.getID());
			}
		return false;
		}

	public void defragIds()
		{
		Iterator<ResourceHolder<?>> iter = resMap.values().iterator();
		while (iter.hasNext())
			{
			ResourceHolder<?> rh = iter.next();
			if (rh instanceof ResourceList<?>) ((ResourceList<?>) rh).defragIds();
			}
		lastInstanceId = 100000;
		lastTileId = 10000000;
		for (Room r : resMap.getList(Room.class))
			{
			for (Instance j : r.instances)
				j.properties.put(PInstance.ID,++lastInstanceId);
			for (Tile j : r.tiles)
				j.properties.put(PTile.ID,++lastTileId);
			}
		}

	public static List<Constant> copyConstants(List<Constant> source)
		{
		List<Constant> dest = new ArrayList<Constant>(source.size());
		for (Constant c : source)
			dest.add(c.copy());
		return dest;
		}

	public void updated(UpdateEvent e)
		{
		updateTrigger.fire(e);
		}
	}
