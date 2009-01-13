/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lateralgm.file.GmFile;
import org.lateralgm.file.ResourceList;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Room.ActiveArrayList.ListUpdateEvent.Type;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

public class Room extends Resource<Room>
	{
	public static final byte TAB_OBJECTS = 0;
	public static final byte TAB_SETTINGS = 1;
	public static final byte TAB_TILES = 2;
	public static final byte TAB_BACKGROUNDS = 3;
	public static final byte TAB_VIEWS = 4;
	public String caption = "";
	public int width = 640;
	public int height = 480;
	public int snapX = 16;
	public int snapY = 16;
	public boolean isometricGrid = false;
	public int speed = 30;
	public boolean persistent = false;
	public Color backgroundColor = Color.BLACK;
	public boolean drawBackgroundColor = true;
	public String creationCode = "";
	public boolean rememberWindowSize = true;
	// ** may not be relevant to swing, or may not produce the same effect in the LGM GUI
	public int editorWidth = 200; // **
	public int editorHeight = 200; // **
	public boolean showGrid = true;
	public boolean showObjects = true;
	public boolean showTiles = true;
	public boolean showBackgrounds = true;
	public boolean showForegrounds = true;
	public boolean showViews = false;
	public boolean deleteUnderlyingObjects = true;
	public boolean deleteUnderlyingTiles = true;
	public int currentTab = TAB_OBJECTS;
	public int scrollBarX = 0; // **
	public int scrollBarY = 0; // **
	public BackgroundDef[] backgroundDefs = new BackgroundDef[8];
	public View[] views = new View[8];
	public boolean enableViews = false;
	public ActiveArrayList<Instance> instances = new ActiveArrayList<Instance>();
	public ActiveArrayList<Tile> tiles = new ActiveArrayList<Tile>();
	private GmFile parent;

	public Room()
		{
		this(LGM.currentFile);
		}

	public Room(GmFile parent) // Rooms are special - they need to know what file they belong to
		{
		this(parent,null,true);
		}

	public Room(GmFile parent, ResourceReference<Room> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes[Resource.ROOM]);
		this.parent = parent;
		for (int j = 0; j < 8; j++)
			{
			views[j] = new View();
			backgroundDefs[j] = new BackgroundDef();
			}
		}

	public Instance addInstance()
		{
		Instance inst = new Instance();
		inst.instanceId = ++parent.lastInstanceId;
		instances.add(inst);
		return inst;
		}

	@Override
	protected Room copy(ResourceList<Room> src, ResourceReference<Room> ref, boolean update)
		{
		Room r = new Room(parent,ref,update);
		r.caption = caption;
		r.width = width;
		r.height = height;
		r.snapX = snapX;
		r.snapY = snapY;
		r.isometricGrid = isometricGrid;
		r.speed = speed;
		r.persistent = persistent;
		r.backgroundColor = backgroundColor;
		r.drawBackgroundColor = drawBackgroundColor;
		r.creationCode = creationCode;
		r.rememberWindowSize = rememberWindowSize;
		r.editorWidth = editorWidth;
		r.editorHeight = editorHeight;
		r.showGrid = showGrid;
		r.showObjects = showObjects;
		r.showTiles = showTiles;
		r.showBackgrounds = showBackgrounds;
		r.showForegrounds = showForegrounds;
		r.showViews = showViews;
		r.deleteUnderlyingObjects = deleteUnderlyingObjects;
		r.deleteUnderlyingTiles = deleteUnderlyingTiles;
		r.currentTab = currentTab;
		r.scrollBarX = scrollBarX;
		r.scrollBarY = scrollBarY;
		r.enableViews = enableViews;
		for (Instance inst : instances)
			{
			Instance inst2 = r.addInstance();
			inst2.setCreationCode(inst.getCreationCode());
			inst2.locked = inst.locked;
			inst2.setObject(inst.getObject());
			inst2.instanceId = inst.instanceId;
			inst2.setPosition(inst.getPosition());
			}
		for (Tile tile : tiles)
			{
			Tile tile2 = new Tile();
			tile2.setBackground(tile.getBackground());
			tile2.setBackgroundPosition(tile.getBackgroundPosition());
			tile2.setDepth(tile.getDepth());
			tile2.setRoomPosition(tile.getRoomPosition());
			tile2.setSize(tile.getSize());
			tile2.tileId = tile.tileId;
			tile2.locked = tile.locked;
			r.tiles.add(tile2);
			tile2.setAutoUpdate(true);
			}
		for (int i = 0; i < 8; i++)
			{
			View view = views[i];
			View view2 = r.views[i];
			view2.visible = view.visible;
			view2.viewX = view.viewX;
			view2.viewY = view.viewY;
			view2.viewW = view.viewW;
			view2.viewH = view.viewH;
			view2.portX = view.portX;
			view2.portY = view.portY;
			view2.portW = view.portW;
			view2.portH = view.portH;
			view2.hbor = view.hbor;
			view2.vbor = view.vbor;
			view2.hspeed = view.hspeed;
			view2.vspeed = view.vspeed;
			view2.objectFollowing = view.objectFollowing;
			}
		for (int i = 0; i < 8; i++)
			{
			BackgroundDef back = backgroundDefs[i];
			BackgroundDef back2 = r.backgroundDefs[i];
			back2.visible = back.visible;
			back2.foreground = back.foreground;
			back2.backgroundId = back.backgroundId;
			back2.x = back.x;
			back2.y = back.y;
			back2.tileHoriz = back.tileHoriz;
			back2.tileVert = back.tileVert;
			back2.horizSpeed = back.horizSpeed;
			back2.vertSpeed = back.vertSpeed;
			back2.stretch = back.stretch;
			}
		if (src != null)
			{
			r.setName(Prefs.prefixes[Resource.ROOM] + (src.lastId + 1));
			src.add(r);
			}
		else
			{
			r.setId(getId());
			r.setName(getName());
			}
		return r;
		}

	public byte getKind()
		{
		return ROOM;
		}

	public static class ActiveArrayList<E> extends ArrayList<E>
		{
		private static final long serialVersionUID = 1L;
		public final UpdateSource updateSource;
		private final UpdateTrigger trigger;

		public ActiveArrayList()
			{
			trigger = new UpdateTrigger();
			updateSource = new UpdateSource(this,trigger);
			}

		public boolean add(E e)
			{
			int i = size();
			super.add(e);
			trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,i,i));
			return true;
			}

		public void add(int index, E element)
			{
			super.add(index,element);
			trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,index,index));
			}

		@Override
		public boolean addAll(Collection<? extends E> c)
			{
			int s = size();
			if (super.addAll(c))
				{
				trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,s,size() - 1));
				return true;
				}
			return false;
			}

		@Override
		public boolean addAll(int index, Collection<? extends E> c)
			{
			int s = size();
			if (super.addAll(index,c))
				{
				trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,index,index + size() - s - 1));
				return true;
				}
			return false;
			}

		@Override
		public void clear()
			{
			int s = size();
			super.clear();
			trigger.fire(new ListUpdateEvent(updateSource,Type.REMOVED,0,s - 1));
			}

		@Override
		public E remove(int index)
			{
			E e = super.remove(index);
			trigger.fire(new ListUpdateEvent(updateSource,Type.REMOVED,index,index));
			return e;
			}

		@Override
		public boolean remove(Object o)
			{
			int i = indexOf(o);
			if (i >= 0)
				{
				super.remove(i);
				trigger.fire(new ListUpdateEvent(updateSource,Type.REMOVED,i,i));
				return true;
				}
			return false;
			}

		@Override
		public boolean removeAll(Collection<?> c)
			{
			if (super.removeAll(c))
				{
				trigger.fire(new ListUpdateEvent(updateSource,Type.CHANGED,0,Integer.MAX_VALUE));
				return true;
				}
			return false;
			}

		@Override
		public boolean retainAll(Collection<?> c)
			{
			if (super.retainAll(c))
				{
				trigger.fire(new ListUpdateEvent(updateSource,Type.CHANGED,0,Integer.MAX_VALUE));
				return true;
				}
			return false;
			}

		public E set(int index, E element)
			{
			E e = super.set(index,element);
			trigger.fire(new ListUpdateEvent(updateSource,Type.CHANGED,index,index));
			return e;
			}

		@Override
		public List<E> subList(int fromIndex, int toIndex)
			{
			// FIXME Sub list's 'set' method needs overriding
			return super.subList(fromIndex,toIndex);
			}

		public static class ListUpdateEvent extends UpdateEvent
			{
			public enum Type
				{
				ADDED,REMOVED,CHANGED
				}

			public final Type type;
			public final int fromIndex, toIndex;

			public ListUpdateEvent(UpdateSource s, Type t, int from, int to)
				{
				super(s);
				type = t;
				fromIndex = from;
				toIndex = to;
				}

			}
		}

	}
