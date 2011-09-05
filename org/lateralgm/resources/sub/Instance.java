/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.awt.Point;
import java.util.EnumMap;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.subframes.CodeFrame.CodeHolder;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.PropertyMap.PropertyValidationException;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class Instance implements Room.Piece,UpdateListener,CodeHolder,
		PropertyValidator<Instance.PInstance>
	{
	private static final long serialVersionUID = 1L;

	private ResourceReference<?> object = null; //kept for listening purposes
	public final PropertyMap<PInstance> properties;
	private final ResourceReference<Room> room;

	private final InstancePropertyListener ipl = new InstancePropertyListener();

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public enum PInstance
		{
		X,Y,OBJECT,ID,CREATION_CODE,LOCKED
		}

	private static final EnumMap<PInstance,Object> DEFS = PropertyMap.makeDefaultMap(PInstance.class,
			0,0,null,0,"",false);

	public Instance(Room r)
		{
		room = r.reference;
		properties = new PropertyMap<PInstance>(PInstance.class,this,DEFS);
		properties.getUpdateSource(PInstance.OBJECT).addListener(ipl);
		}

	protected void fireUpdate(UpdateEvent e)
		{
		if (e == null) e = updateTrigger.getEvent();
		updateTrigger.fire(e);
		Room r = room == null ? null : room.get();
		if (r != null) r.instanceUpdated(e);
		}

	public Point getPosition()
		{
		return new Point((Integer) properties.get(PInstance.X),(Integer) properties.get(PInstance.Y));
		}

	public void setPosition(Point pos)
		{
		properties.put(PInstance.X,pos.x);
		properties.put(PInstance.Y,pos.y);
		}

	public String getCreationCode()
		{
		return (String) properties.get(PInstance.CREATION_CODE);
		}

	public String getCode()
		{
		return getCreationCode();
		}

	public void setCreationCode(String creationCode)
		{
		properties.put(PInstance.CREATION_CODE,creationCode);
		}

	public void setCode(String s)
		{
		setCreationCode(s);
		}

	public void updated(UpdateEvent e)
		{
		fireUpdate(e);
		}

	public boolean isLocked()
		{
		return (Boolean) properties.get(PInstance.LOCKED);
		}

	public void setLocked(boolean l)
		{
		properties.put(PInstance.LOCKED,l);
		}

	public Object validate(PInstance k, Object v)
		{
		if (k == PInstance.OBJECT)
			{
			ResourceReference<?> r = (ResourceReference<?>) v;
			if (r != null)
				{
				Object o = r.get();
				if (o == null)
					r = null;
				else if (!(o instanceof GmObject)) throw new PropertyValidationException();
				}
			if (object != null) object.updateSource.removeListener(this);
			object = r;
			if (object != null) object.updateSource.addListener(this);
			}
		return v;
		}

	private class InstancePropertyListener extends PropertyUpdateListener<PInstance>
		{
		@Override
		public void updated(PropertyUpdateEvent<PInstance> e)
			{
			if (e.key == PInstance.OBJECT) fireUpdate(null);
			}
		}

	public boolean equals(Object o)
		{
		if (o == this) return true;
		if (o == null || !(o instanceof Instance)) return false;
		//room?
		return properties.equals(((Instance) o).properties);
		}
	}
