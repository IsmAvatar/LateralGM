/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.awt.Dimension;
import java.awt.Point;
import java.util.EnumMap;

import org.lateralgm.file.GmFile;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.PropertyMap.PropertyValidationException;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class Tile implements Room.Piece,UpdateListener,PropertyValidator<Tile.PTile>
	{
	private ResourceReference<?> background = null; //kept for listening purposes
	public final PropertyMap<PTile> properties;
	private final ResourceReference<Room> room;

	private final TilePropertyListener tpl = new TilePropertyListener();

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public enum PTile
		{
		BG_X,BG_Y,ROOM_X,ROOM_Y,WIDTH,HEIGHT,DEPTH,BACKGROUND,ID,LOCKED
		}

	private static final EnumMap<PTile,Object> DEFS = PropertyMap.makeDefaultMap(PTile.class,0,0,0,0,
			0,0,0,null,0,false);

	/**
	 * Do not call this constructor unless you intend
	 * to handle your own tile ID. See Tile(Room r, GmFile f).
	 */
	public Tile(Room r)
		{
		room = r.reference;
		properties = new PropertyMap<PTile>(PTile.class,this,DEFS);
		properties.getUpdateSource(PTile.BACKGROUND).addListener(tpl);
		}

	public Tile(Room r, int id)
		{
		this(r);
		properties.put(PTile.ID,id);
		}

	/**
	 * Constructs a tile for this GmFile, and determines ID via the last tile id.
	 * Notice that a tile initializes with no settings.
	 */
	public Tile(Room r, GmFile f)
		{
		this(r,++f.lastTileId);
		}

	protected void fireUpdate(UpdateEvent e)
		{
		if (e == null) e = updateTrigger.getEvent();
		updateTrigger.fire(e);
		Room r = room == null ? null : room.get();
		if (r != null) r.tileUpdated(e);
		}

	public Point getBackgroundPosition()
		{
		int x = properties.get(PTile.BG_X);
		int y = properties.get(PTile.BG_Y);
		return new Point(x,y);
		}

	public void setBackgroundPosition(Point p)
		{
		properties.put(PTile.BG_X,p.x);
		properties.put(PTile.BG_Y,p.y);
		fireUpdate(null);
		}

	public Point getRoomPosition()
		{
		int x = properties.get(PTile.ROOM_X);
		int y = properties.get(PTile.ROOM_Y);
		return new Point(x,y);
		}

	public void setRoomPosition(Point p)
		{
		properties.put(PTile.ROOM_X,p.x);
		properties.put(PTile.ROOM_Y,p.y);
		}

	public Dimension getSize()
		{
		int w = properties.get(PTile.WIDTH);
		int h = properties.get(PTile.HEIGHT);
		return new Dimension(w,h);
		}

	public void setSize(Dimension s)
		{
		properties.put(PTile.WIDTH,s.width);
		properties.put(PTile.HEIGHT,s.height);
		fireUpdate(null);
		}

	public int getDepth()
		{
		return properties.get(PTile.DEPTH);
		}

	public void setDepth(int d)
		{
		properties.put(PTile.DEPTH,d);
		}

	public void updated(UpdateEvent e)
		{
		fireUpdate(e);
		}

	public boolean isLocked()
		{
		return properties.get(PTile.LOCKED);
		}

	public void setLocked(boolean l)
		{
		properties.put(PTile.LOCKED,l);
		}

	public Object validate(PTile k, Object v)
		{
		if (k == PTile.BACKGROUND)
			{
			ResourceReference<?> r = (ResourceReference<?>) v;
			if (r != null)
				{
				Object o = r.get();
				if (o == null)
					r = null;
				else if (!(o instanceof Background)) throw new PropertyValidationException();
				}
			if (background != null) background.updateSource.removeListener(this);
			background = r;
			if (background != null) background.updateSource.addListener(this);
			}
		return v;
		}

	private class TilePropertyListener extends PropertyUpdateListener<PTile>
		{
		@Override
		public void updated(PropertyUpdateEvent<PTile> e)
			{
			if (e.key == PTile.BACKGROUND) fireUpdate(null);
			}
		}

	public boolean equals(Object o)
		{
		if (o == this) return true;
		if (o == null || !(o instanceof Tile)) return false;
		//room?
		return properties.equals(((Tile) o).properties);
		}
	}
