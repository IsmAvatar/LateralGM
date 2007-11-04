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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
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
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;

public class GmFile
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

	private final ResourceChangeListener rcl = new ResourceChangeListener();

	private EventListenerList listenerList = new EventListenerList();
	private ChangeEvent changeEvent = null;

	public GmFile()
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
	public ResourceList<?> getList(int res)
		{
		return resMap.get((byte) res);
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
