/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * 
 * Lateral GM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Lateral GM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.file;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;

public class Gm6File
	{
	private Map<Byte,ResourceList<?>> resMap = new HashMap<Byte,ResourceList<?>>();
	public ResourceList<Sprite> sprites = new ResourceList<Sprite>(Sprite.class,this);
	public ResourceList<Sound> sounds = new ResourceList<Sound>(Sound.class,this);
	public ResourceList<Background> backgrounds = new ResourceList<Background>(Background.class,this);
	public ResourceList<Path> paths = new ResourceList<Path>(Path.class,this);
	public ResourceList<Script> scripts = new ResourceList<Script>(Script.class,this);
	public ResourceList<Font> fonts = new ResourceList<Font>(Font.class,this);
	public ResourceList<Timeline> timelines = new ResourceList<Timeline>(Timeline.class,this);
	public ResourceList<GmObject> gmObjects = new ResourceList<GmObject>(GmObject.class,this);
	public ResourceList<Room> rooms = new ResourceList<Room>(Room.class,this);
	public ArrayList<Constant> constants = new ArrayList<Constant>();
	public ArrayList<Include> includes = new ArrayList<Include>();

	private final ResourceChangeListener rcl = new ResourceChangeListener();

	private EventListenerList listenerList = new EventListenerList();
	private ChangeEvent changeEvent = null;

	public Gm6File()
		{
		resMap.put(Resource.SPRITE,sprites);
		resMap.put(Resource.SOUND,sounds);
		resMap.put(Resource.BACKGROUND,backgrounds);
		resMap.put(Resource.PATH,paths);
		resMap.put(Resource.SCRIPT,scripts);
		resMap.put(Resource.FONT,fonts);
		resMap.put(Resource.TIMELINE,timelines);
		resMap.put(Resource.GMOBJECT,gmObjects);
		resMap.put(Resource.ROOM,rooms);
		for (ResourceList<?> rl : resMap.values())
			{
			rl.addChangeListener(rcl);
			}
		gameId = new Random().nextInt(100000001);
		gameIconData = new byte[0];
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
			gameIconData = dat.toByteArray();
			gameIcon = (BufferedImage) new ICOFile(new ByteArrayInputStream(gameIconData)).getDescriptor(
					0).getImageRGB();
			}
		catch (Exception ex)
			{
			gameIconData = new byte[0];
			System.err.println(Messages.getString("Gm6File.NOICON")); //$NON-NLS-1$
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

	// Constants For Resource Properties
	public static final byte COLOR_NOCHANGE = 0;
	public static final byte COLOR_16 = 1;
	public static final byte COLOR_32 = 2;
	public static final byte RES_NOCHANGE = 0;
	public static final byte RES_320X240 = 1;
	public static final byte RES_640X480 = 2;
	public static final byte RES_800X600 = 3;
	public static final byte RES_1024X768 = 4;
	public static final byte RES_1280X1024 = 5;
	public static final byte RES_1600X1200 = 6;
	public static final byte FREQ_NOCHANGE = 0;
	public static final byte FREQ_60 = 1;
	public static final byte FREQ_70 = 2;
	public static final byte FREQ_85 = 3;
	public static final byte FREQ_100 = 4;
	public static final byte FREQ_120 = 5;
	public static final byte PRIORITY_NORMAL = 0;
	public static final byte PRIORITY_HIGH = 1;
	public static final byte PRIORITY_HIGHEST = 2;
	public static final byte LOADBAR_NONE = 0;
	public static final byte LOADBAR_DEFAULT = 1;
	public static final byte LOADBAR_CUSTOM = 2;
	public static final byte INCLUDE_MAIN = 0;
	public static final byte INCLUDE_TEMP = 1;

	// File Properties
	public int gameId; // randomized in constructor
	public boolean startFullscreen = false;
	public boolean interpolate = false;
	public boolean dontDrawBorder = false;
	public boolean displayCursor = true;
	public int scaling = -1;
	public boolean allowWindowResize = false;
	public boolean alwaysOnTop = false;
	public Color colorOutsideRoom = Color.BLACK;
	public boolean setResolution = false;
	public byte colorDepth = COLOR_NOCHANGE;
	public byte resolution = RES_NOCHANGE;
	public byte frequency = FREQ_NOCHANGE;
	public boolean dontShowButtons = false;
	public boolean useSynchronization = false;
	public boolean letF4SwitchFullscreen = true;
	public boolean letF1ShowGameInfo = true;
	public boolean letEscEndGame = true;
	public boolean letF5SaveF6Load = true;
	public byte gamePriority = PRIORITY_NORMAL;
	public boolean freezeOnLoseFocus = false;
	public byte loadBarMode = LOADBAR_DEFAULT;
	public BufferedImage frontLoadBar = null;
	public BufferedImage backLoadBar = null;
	public boolean showCustomLoadImage = false;
	public BufferedImage loadingImage = null;
	public boolean imagePartiallyTransparent = false;
	public int loadImageAlpha = 255;
	public boolean scaleProgressBar = true;
	public boolean displayErrors = true;
	public boolean writeToLog = false;
	public boolean abortOnError = false;
	public boolean treatUninitializedAs0 = false;
	public String author = ""; //$NON-NLS-1$
	public int version = 100;
	public double lastChanged = longTimeToGmTime(System.currentTimeMillis());
	public String information = ""; //$NON-NLS-1$
	public int includeFolder = INCLUDE_MAIN;
	public boolean overwriteExisting = false;
	public boolean removeAtGameEnd = false;
	public int lastInstanceId = 100000;
	public int lastTileId = 10000000;

	// actual data is stored to be written on resave (no reason to re-encode)
	public byte[] gameIconData;
	public BufferedImage gameIcon; // icon as image for display purposes

	// Returns the ResourceList corresponding to given Resource constant
	public ResourceList<?> getList(int res)
		{
		return resMap.get((byte) res);
		}

	public void clearAll()
		{
		for (ResourceList<?> l : resMap.values())
			l.clear();
		constants.clear();
		includes.clear();
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
				j.instanceId = ++lastInstanceId;
			for (Tile j : r.tiles)
				j.tileId = ++lastTileId;
			}
		}

	public void addChangeListener(ChangeListener l)
		{
		listenerList.add(ChangeListener.class,l);
		}

	public void removeChangeListener(ChangeListener l)
		{
		listenerList.remove(ChangeListener.class,l);
		}

	protected void fireStateChanged(ChangeEvent e)
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ChangeListener.class)
				{
				((ChangeListener) listeners[i + 1]).stateChanged(e);
				}
			}
		}

	protected void fireStateChanged()
		{
		// Lazily create the event:
		if (changeEvent == null) changeEvent = new ChangeEvent(this);
		fireStateChanged(changeEvent);
		}

	private class ResourceChangeListener implements ChangeListener
		{
		public void stateChanged(ChangeEvent e)
			{
			fireStateChanged(e);
			}
		}
	}
