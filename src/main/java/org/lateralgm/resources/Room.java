/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.lateralgm.main.LGM;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;
import org.lateralgm.subframes.CodeFrame.CodeHolder;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.PropertyMap;

public class Room extends InstantiableResource<Room,Room.PRoom> implements CodeHolder
	{
	public static final int TAB_OBJECTS = 0;
	public static final int TAB_SETTINGS = 1;
	public static final int TAB_TILES = 2;
	public static final int TAB_BACKGROUNDS = 3;
	public static final int TAB_VIEWS = 4;
	public final List<BackgroundDef> backgroundDefs;
	public final List<View> views;
	public final ActiveArrayList<Instance> instances = new ActiveArrayList<Instance>();
	public final ActiveArrayList<Tile> tiles = new ActiveArrayList<Tile>();

	private final UpdateTrigger instanceUpdateTrigger = new UpdateTrigger();
	public final UpdateSource instanceUpdateSource = new UpdateSource(this,instanceUpdateTrigger);
	private final UpdateTrigger tileUpdateTrigger = new UpdateTrigger();
	public final UpdateSource tileUpdateSource = new UpdateSource(this,tileUpdateTrigger);

	public enum PRoom
		{
		CAPTION,WIDTH,HEIGHT,SNAP_X,SNAP_Y,ISOMETRIC,SPEED,PERSISTENT,BACKGROUND_COLOR,
		DRAW_BACKGROUND_COLOR,CREATION_CODE,REMEMBER_WINDOW_SIZE,EDITOR_WIDTH,EDITOR_HEIGHT,SHOW_GRID,
		SHOW_OBJECTS,SHOW_TILES,SHOW_BACKGROUNDS,SHOW_FOREGROUNDS,SHOW_VIEWS,DELETE_UNDERLYING_OBJECTS,
		DELETE_UNDERLYING_TILES,CURRENT_TAB,SCROLL_BAR_X,SCROLL_BAR_Y,VIEWS_ENABLED,VIEWS_CLEAR,
		PHYSICS_WORLD,PHYSICS_TOP,PHYSICS_LEFT,PHYSICS_RIGHT,PHYSICS_BOTTOM,PHYSICS_GRAVITY_X,
		PHYSICS_GRAVITY_Y,PHYSICS_PIXTOMETERS
		}

	private static final EnumMap<PRoom,Object> DEFS = PropertyMap.makeDefaultMap(PRoom.class,"",640,
			480,16,16,false,30,false,new Color(102,204,255),true,"",true,1024,640,true,true,true,true,true,
			false,false,false,TAB_OBJECTS,0,0,false,true,false,0,0,640,480,0.0,10.0,0.1);

	public Room()
		{
		this(null);
		}

	public Room(ResourceReference<Room> r)
		{
		super(r);
		BackgroundDef[] b = new BackgroundDef[8];
		for (int j = 0; j < b.length; j++)
			b[j] = new BackgroundDef();
		backgroundDefs = Collections.unmodifiableList(Arrays.asList(b));
		View[] v = new View[8];
		for (int j = 0; j < v.length; j++)
			v[j] = new View();
		views = Collections.unmodifiableList(Arrays.asList(v));
		}

	@Override
	public Room makeInstance(ResourceReference<Room> r)
		{
		return new Room(r);
		}

	public Instance addInstance()
		{
		Instance inst = new Instance(this);
		inst.properties.put(PInstance.ID,++LGM.currentFile.lastInstanceId);
		instances.add(inst);
		return inst;
		}

	public int getWidth()
		{
		return properties.get(PRoom.WIDTH);
		}

	public int getHeight()
		{
		return properties.get(PRoom.HEIGHT);
		}

	public String getCode()
		{
		return properties.get(PRoom.CREATION_CODE);
		}

	public void setCode(String s)
		{
		properties.put(PRoom.CREATION_CODE,s);
		}

	@Override
	protected void postCopy(Room dest)
		{
		super.postCopy(dest);
		for (Instance inst : instances)
			{
			Instance inst2 = dest.addInstance();
			inst2.properties.putAll(inst.properties);
			}
		for (Tile tile : tiles)
			{
			Tile tile2 = new Tile(this);
			tile2.properties.putAll(tile.properties);
			dest.tiles.add(tile2);
			}
		int s = views.size();
		for (int i = 0; i < s; i++)
			{
			View view = views.get(i);
			View view2 = dest.views.get(i);
			view2.properties.putAll(view.properties);
			}
		s = backgroundDefs.size();
		for (int i = 0; i < s; i++)
			{
			BackgroundDef back = backgroundDefs.get(i);
			BackgroundDef back2 = dest.backgroundDefs.get(i);
			back2.properties.putAll(back.properties);
			}
		}

	@Override
	protected PropertyMap<PRoom> makePropertyMap()
		{
		return new PropertyMap<PRoom>(PRoom.class,this,DEFS);
		}

	public void instanceUpdated(UpdateEvent e)
		{
		instanceUpdateTrigger.fire(new UpdateEvent(instanceUpdateSource,e));
		}

	public void tileUpdated(UpdateEvent e)
		{
		tileUpdateTrigger.fire(new UpdateEvent(tileUpdateSource,e));
		}

	public interface Piece
		{
		boolean isLocked();

		void setLocked(boolean l);

		void setName(String name);

		String getName();

		void setPosition(Point pos);

		void setScale(Point2D scale);

		Point2D getScale();

		double getRotation();

		void setRotation(double rotation);

		void setAlpha(int alpha);

		void setColor(Color color);

		int getAlpha();

		void setSelected(boolean l);

		boolean isSelected();

		Point getPosition();
		}
	}
