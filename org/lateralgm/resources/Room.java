/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
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
import java.util.EnumMap;
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
import org.lateralgm.util.PropertyMap;

public class Room extends Resource<Room,Room.PRoom>
	{
	public static final int TAB_OBJECTS = 0;
	public static final int TAB_SETTINGS = 1;
	public static final int TAB_TILES = 2;
	public static final int TAB_BACKGROUNDS = 3;
	public static final int TAB_VIEWS = 4;
	public BackgroundDef[] backgroundDefs = new BackgroundDef[8];
	public View[] views = new View[8];
	public ActiveArrayList<Instance> instances = new ActiveArrayList<Instance>();
	public ActiveArrayList<Tile> tiles = new ActiveArrayList<Tile>();
	private GmFile parent;

	public enum PRoom
		{
		CAPTION,WIDTH,HEIGHT,SNAP_X,SNAP_Y,ISOMETRIC,SPEED,PERSISTENT,BACKGROUND_COLOR,
		DRAW_BACKGROUND_COLOR,CREATION_CODE,REMEMBER_WINDOW_SIZE,EDITOR_WIDTH,EDITOR_HEIGHT,SHOW_GRID,
		SHOW_OBJECTS,SHOW_TILES,SHOW_BACKGROUNDS,SHOW_FOREGROUNDS,SHOW_VIEWS,DELETE_UNDERLYING_OBJECTS,
		DELETE_UNDERLYING_TILES,CURRENT_TAB,SCROLL_BAR_X,SCROLL_BAR_Y,ENABLE_VIEWS
		}

	private static final EnumMap<PRoom,Object> DEFS = PropertyMap.makeDefaultMap(PRoom.class,"",640,
			480,16,16,false,30,false,Color.BLACK,true,"",true,200,200,true,true,true,true,true,false,
			true,true,TAB_OBJECTS,0,0,false);

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
		setName(Prefs.prefixes.get(Kind.ROOM));
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
		copy(src,r);
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
		return r;
		}

	public Kind getKind()
		{
		return Kind.ROOM;
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

	@Override
	protected PropertyMap<PRoom> makePropertyMap()
		{
		return new PropertyMap<PRoom>(PRoom.class,this,DEFS);
		}

	}
