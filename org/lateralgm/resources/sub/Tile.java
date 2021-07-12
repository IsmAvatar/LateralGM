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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.Random;

import org.lateralgm.file.ProjectFile;
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
		BG_X,BG_Y,ROOM_X,ROOM_Y,WIDTH,HEIGHT,DEPTH,BACKGROUND,NAME,ID,LOCKED,COLOR,ALPHA,SCALE_X,SCALE_Y,
		ROTATION,SELECTED
		}

	/**
	 * Do not call this constructor unless you intend
	 * to handle your own tile ID. See Tile(Room r, GmFile f).
	 */
	public Tile(Room r)
		{
		room = r.reference;
		properties = new PropertyMap<PTile>(PTile.class,this)
			{
			int bgX = 0;
			int bgY = 0;
			int roomX = 0;
			int roomY = 0;
			int width = 0;
			int height = 0;
			int depth = 0;
			Object background = null;
			String name = "tile";
			int id = 0;
			boolean locked = false;
			long color = 4294967295L;
			int alpha = 255;
			double scaleX = 1.0f;
			double scaleY = 1.0f;
			double rotation = 0.0f;
			boolean selected = false;

			@Override
			public Object put(PTile key, Object value)
				{
				Object vv = validator == null ? value : validator.validate(key,value);
				Object o = this.get(key);
				switch (key)
					{
					case ALPHA: alpha = (int) value; break;
					case BACKGROUND: background = value; break;
					case BG_X: bgX = (int) value; break;
					case BG_Y: bgY = (int) value; break;
					case COLOR: color = (long) value; break;
					case DEPTH: depth = (int) value; break;
					case HEIGHT: height = (int) value; break;
					case ID: id = (int) value; break;
					case LOCKED: locked= (boolean) value; break;
					case NAME: name = (String) value; break;
					case ROOM_X: roomX = (int) value; break;
					case ROOM_Y: roomY = (int) value; break;
					case ROTATION: rotation = (double) value; break;
					case SCALE_X: scaleX = (double) value; break;
					case SCALE_Y: scaleY = (double) value; break;
					case SELECTED: selected = (boolean) value; break;
					case WIDTH: width = (int) value; break;
					default: return value;
					}
				if (vv != value || o != value) fireUpdate(key);
				return value;
				}

			@Override
			public Object get(PTile key)
				{
				switch (key)
				{
				case ALPHA: return alpha;
				case BACKGROUND: return background;
				case BG_X: return bgX;
				case BG_Y: return bgY;
				case COLOR: return color;
				case DEPTH: return depth;
				case HEIGHT: return height;
				case ID: return id;
				case LOCKED: return locked;
				case NAME: return name;
				case ROOM_X: return roomX;
				case ROOM_Y: return roomY;
				case ROTATION: return rotation;
				case SCALE_X: return scaleX;
				case SCALE_Y: return scaleY;
				case SELECTED: return selected;
				case WIDTH: return width;
				default: break;
				}
				return null;
				}
			};
		//properties.put(PTile.NAME, "tile_" + String.format("%08X", new Random().nextInt()));
		properties.updateSource.addListener(tpl);
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
	public Tile(Room r, ProjectFile f)
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

	public int getID()
		{
		return properties.get(PTile.ID);
		}

	@Override
	public void setName(String name)
		{
		properties.put(PTile.NAME, name);
		}

	@Override
	public String getName()
		{
		return properties.get(PTile.NAME);
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

	public double getRotation()
		{
		return properties.get(PTile.ROTATION);
		}

	public void setRotation(double degrees)
		{
		properties.put(PTile.ROTATION,degrees);
		}

	public Point getPosition()
		{
		int x = properties.get(PTile.ROOM_X);
		int y = properties.get(PTile.ROOM_Y);
		return new Point(x,y);
		}

	public void setPosition(Point p)
		{
		properties.put(PTile.ROOM_X,p.x);
		properties.put(PTile.ROOM_Y,p.y);
		}

	public void setColor(Color color)
		{
		properties.put(PTile.COLOR,color);
		}

	public int getAlpha()
		{
		return properties.get(PTile.ALPHA);
		}

	public void setAlpha(int alpha)
		{
		properties.put(PTile.ALPHA,alpha);
		}

	public Dimension getSize()
		{
		int w = properties.get(PTile.WIDTH);
		int h = properties.get(PTile.HEIGHT);
		return new Dimension(w,h);
		}

	public void setSelected(boolean selected)
		{
		properties.put(PTile.SELECTED,selected);
		}

	public boolean isSelected()
		{
		return (Boolean) properties.get(PTile.SELECTED);
		}

	public void setSize(Dimension s)
		{
		properties.put(PTile.WIDTH,s.width);
		properties.put(PTile.HEIGHT,s.height);
		fireUpdate(null);
		}

	public void setScale(Point2D scale)
		{
		properties.put(PTile.SCALE_X,scale.getX());
		properties.put(PTile.SCALE_Y,scale.getY());
		}

	public void setColor(long color)
		{
		properties.put(PTile.COLOR,color);
		}

	public Point2D getScale()
		{
		return new Point2D.Double((Double) properties.get(PTile.SCALE_X),
				(Double) properties.get(PTile.SCALE_Y));
		}

	public long getColor()
		{
		return properties.get(PTile.COLOR);
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
			if (e.key == PTile.SELECTED) fireUpdate(null);
			}
		}

	@Override
	public int hashCode()
		{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		return result;
		}

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Tile)) return false;
		Tile other = (Tile) obj;
		if (properties == null)
			{
			if (other.properties != null) return false;
			}
		else if (!properties.equals(other.properties)) return false;
		return true;
		}
	}
