/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2006, 2007, 2008 IsmAvatar <IsmAvatar@gmail.com>
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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
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
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sound.SoundKind;
import org.lateralgm.resources.Sprite.BBMode;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile.PTile;

public class GmFile implements UpdateListener
	{
	protected static final Resource.Kind[] RESOURCE_KIND = { null,Resource.Kind.OBJECT,
			Resource.Kind.SPRITE,Resource.Kind.SOUND,Resource.Kind.ROOM,null,Resource.Kind.BACKGROUND,
			Resource.Kind.SCRIPT,Resource.Kind.PATH,Resource.Kind.FONT,Resource.Kind.GAMEINFO,
			Resource.Kind.GAMESETTINGS,Resource.Kind.TIMELINE,Resource.Kind.EXTENSIONS };
	protected static final Map<Resource.Kind,Integer> RESOURCE_CODE;
	static
		{
		EnumMap<Resource.Kind,Integer> m = new EnumMap<Resource.Kind,Integer>(Resource.Kind.class);
		for (int i = 0; i < RESOURCE_KIND.length; i++)
			if (RESOURCE_KIND[i] != null) m.put(RESOURCE_KIND[i],i);
		RESOURCE_CODE = Collections.unmodifiableMap(m);
		}
	protected static final PSound[] SOUND_FX_FLAGS = { PSound.CHORUS,PSound.ECHO,PSound.FLANGER,
			PSound.GARGLE,PSound.REVERB };
	protected static final SoundKind[] SOUND_KIND = { SoundKind.NORMAL,SoundKind.BACKGROUND,
			SoundKind.SPATIAL,SoundKind.MULTIMEDIA };
	protected static final Map<SoundKind,Integer> SOUND_CODE;
	static
		{
		EnumMap<SoundKind,Integer> m = new EnumMap<SoundKind,Integer>(SoundKind.class);
		for (int i = 0; i < SOUND_KIND.length; i++)
			m.put(SOUND_KIND[i],i);
		SOUND_CODE = Collections.unmodifiableMap(m);
		}
	protected static final BBMode[] SPRITE_BB_MODE = { BBMode.AUTO,BBMode.FULL,BBMode.MANUAL };
	protected static final Map<BBMode,Integer> SPRITE_BB_CODE;
	static
		{
		EnumMap<BBMode,Integer> m = new EnumMap<BBMode,Integer>(BBMode.class);
		for (int i = 0; i < SPRITE_BB_MODE.length; i++)
			m.put(SPRITE_BB_MODE[i],i);
		SPRITE_BB_CODE = Collections.unmodifiableMap(m);
		}

	private final EnumMap<Resource.Kind,ResourceList<?>> resMap;
	public final ResourceList<Sprite> sprites = new ResourceList<Sprite>(Sprite.class,this);
	public final ResourceList<Sound> sounds = new ResourceList<Sound>(Sound.class,this);
	public final ResourceList<Background> backgrounds = new ResourceList<Background>(
			Background.class,this);
	public final ResourceList<Path> paths = new ResourceList<Path>(Path.class,this);
	public final ResourceList<Script> scripts = new ResourceList<Script>(Script.class,this);
	public final ResourceList<Font> fonts = new ResourceList<Font>(Font.class,this);
	public final ResourceList<Timeline> timelines = new ResourceList<Timeline>(Timeline.class,this);
	public final ResourceList<GmObject> gmObjects = new ResourceList<GmObject>(GmObject.class,this);
	public final ResourceList<Room> rooms = new ResourceList<Room>(Room.class,this);

	public String filename = null;

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
		gameSettings.gameId = new Random().nextInt(100000001);
		gameSettings.gameIconData = new byte[0];
		try
			{
			String loc = "org/lateralgm/file/default.ico";
			InputStream filein;
			File file = new File(loc);
			if (!file.exists())
				filein = LGM.class.getClassLoader().getResourceAsStream(loc);
			else
				filein = new FileInputStream(file);

			BufferedInputStream in = new BufferedInputStream(filein);
			ByteArrayOutputStream dat = new ByteArrayOutputStream();

			int val = in.read();
			while (val != -1)
				{
				dat.write(val);
				val = in.read();
				}
			gameSettings.gameIconData = dat.toByteArray();
			gameSettings.gameIcon = (BufferedImage) new ICOFile(new ByteArrayInputStream(
					gameSettings.gameIconData)).getDescriptor(0).getImageRGB();
			}
		catch (Exception ex)
			{
			gameSettings.gameIconData = new byte[0];
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

	public GameSettings gameSettings = new GameSettings();
	public int lastInstanceId = 100000;
	public int lastTileId = 10000000;

	// Returns the ResourceList corresponding to given Resource constant
	public ResourceList<?> getList(Resource.Kind res)
		{
		return resMap.get(res);
		}

	public GameInformation gameInfo = new GameInformation();

	public void defragIds()
		{
		Iterator<ResourceList<?>> iter = resMap.values().iterator();
		while (iter.hasNext())
			iter.next().defragIds();
		lastInstanceId = 100000;
		lastTileId = 100000;
		for (Room r : rooms)
			{
			for (Instance j : r.instances)
				j.properties.put(PInstance.ID,++lastInstanceId);
			for (Tile j : r.tiles)
				j.properties.put(PTile.ID,++lastTileId);
			}
		}

	public void updated(UpdateEvent e)
		{
		updateTrigger.fire(e);
		}
	}
