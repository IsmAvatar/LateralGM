/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.ShapePoint;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.PropertyMap.PropertyValidationException;

public class GmObject extends InstantiableResource<GmObject,GmObject.PGmObject> implements
		Resource.Viewable,UpdateListener
	{
	public static final ResourceReference<GmObject> OBJECT_SELF = new ResourceReference<GmObject>(
			null);
	public static final ResourceReference<GmObject> OBJECT_OTHER = new ResourceReference<GmObject>(
			null);

	public static int refAsInt(ResourceReference<GmObject> ref)
		{
		if (ref == OBJECT_SELF) return -1;
		if (ref == OBJECT_OTHER) return -2;
		GmObject obj = Util.deRef(ref);
		return obj == null ? -100 : obj.getId();
		}

	private final ObjectPropertyListener opl = new ObjectPropertyListener();

	private ResourceReference<?> sprite = null; //kept for listening purposes
	public final List<MainEvent> mainEvents;
	public final ActiveArrayList<ShapePoint> shapePoints = new ActiveArrayList<ShapePoint>();

	public enum PhysicsShape
		{
		CIRCLE,BOX,SHAPE
		}

	public enum PGmObject
		{
		SPRITE,SOLID,VISIBLE,DEPTH,PERSISTENT,PARENT,MASK,PHYSICS_OBJECT,PHYSICS_SENSOR,PHYSICS_SHAPE,
		PHYSICS_DENSITY,PHYSICS_RESTITUTION,PHYSICS_GROUP,PHYSICS_DAMPING_LINEAR,
		PHYSICS_DAMPING_ANGULAR,PHYSICS_FRICTION,PHYSICS_AWAKE,PHYSICS_KINEMATIC
		}

	private static final EnumMap<PGmObject,Object> DEFS = PropertyMap.makeDefaultMap(PGmObject.class,
			null,false,true,0,false,null,null,false,false,PhysicsShape.CIRCLE,0.5,0.1,0,0.1,0.1,0.2,true,
			false);

	public GmObject()
		{
		this(null);
		}

	public GmObject(ResourceReference<GmObject> r)
		{
		super(r);
		MainEvent[] e = new MainEvent[12];
		for (int j = 0; j < 12; j++)
			e[j] = new MainEvent();
		mainEvents = Collections.unmodifiableList(Arrays.asList(e));
		properties.getUpdateSource(PGmObject.SPRITE).addListener(opl);
		}

	public GmObject makeInstance(ResourceReference<GmObject> r)
		{
		return new GmObject(r);
		}

	@Override
	protected void postCopy(GmObject dest)
		{
		super.postCopy(dest);
		for (int i = 0; i < 12; i++)
			{
			MainEvent mev = mainEvents.get(i);
			MainEvent mev2 = dest.mainEvents.get(i);
			for (Event ev : mev.events)
				{
				mev2.events.add(ev.copy());
				}
			}
		for (ShapePoint point : shapePoints)
			{
			ShapePoint point2 = new ShapePoint(point.getX(),point.getY());
			dest.shapePoints.add(point2);
			}
		}

	public BufferedImage getDisplayImage()
		{
		ResourceReference<Sprite> r = get(PGmObject.SPRITE);
		Sprite s = Util.deRef(r);
		return s == null ? null : s.getDisplayImage();
		}

	public void updated(UpdateEvent e)
		{
		reference.updateTrigger.fire(e);
		}

	@Override
	protected PropertyMap<PGmObject> makePropertyMap()
		{
		return new PropertyMap<PGmObject>(PGmObject.class,this,DEFS);
		}

	private boolean isValidParent(GmObject p)
		{
		if (p == this) return false;
		if (p == null) return true;
		HashSet<GmObject> traversed = new HashSet<GmObject>();
		traversed.add(p);
		while (true)
			{
			ResourceReference<GmObject> r = p.get(PGmObject.PARENT);
			p = Util.deRef(r);
			if (p == null) return true;
			if (p == this || !traversed.add(p)) return false;
			}
		}

	@Override
	public Object validate(PGmObject k, Object v)
		{
		switch (k)
			{
			case SPRITE:
				ResourceReference<?> r = (ResourceReference<?>) v;
				if (r != null)
					{
					Object o = r.get();
					if (o == null)
						r = null;
					else if (!(o instanceof Sprite)) throw new PropertyValidationException();
					}
				if (sprite != null) sprite.updateSource.removeListener(this);
				sprite = r;
				if (sprite != null) sprite.updateSource.addListener(this);
				break;
			case PARENT:
				if (v == null) break;
				GmObject p = (GmObject) ((ResourceReference<?>) v).get();
				if (!isValidParent(p)) throw new ParentLoopException();
			default:
				//TODO: maybe put a failsafe here?
				break;
			}
		return v;
		}

	public static class ParentLoopException extends PropertyValidationException
		{
		private static final long serialVersionUID = 1L;
		}

	private class ObjectPropertyListener extends PropertyUpdateListener<PGmObject>
		{
		@Override
		public void updated(PropertyUpdateEvent<PGmObject> e)
			{
			if (e.key == PGmObject.SPRITE) fireUpdate();
			}
		}
	}
