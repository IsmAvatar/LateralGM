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

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.Random;

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
	private ResourceReference<?> object = null; //kept for listening purposes
	public final PropertyMap<PInstance> properties;
	private final ResourceReference<Room> room;

	private final InstancePropertyListener ipl = new InstancePropertyListener();

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public enum PInstance
		{
		X,Y,OBJECT,NAME,ID,CREATION_CODE,LOCKED,SCALE_X,SCALE_Y,COLOR,ROTATION,SELECTED,ALPHA
		}

	private static final EnumMap<PInstance,Object> DEFS = PropertyMap.makeDefaultMap(PInstance.class,
			0,0,null,"instance",0,"",false,1.0,1.0,new Color(255,255,255),0.0,false,255);

	public Instance(Room r)
		{
		room = r.reference;
		properties = new PropertyMap<PInstance>(PInstance.class,this,DEFS);
		properties.getUpdateSource(PInstance.OBJECT).addListener(ipl);
		properties.getUpdateSource(PInstance.NAME).addListener(ipl);
		properties.getUpdateSource(PInstance.SELECTED).addListener(ipl);
		properties.getUpdateSource(PInstance.SCALE_X).addListener(ipl);
		properties.getUpdateSource(PInstance.SCALE_Y).addListener(ipl);
		properties.getUpdateSource(PInstance.ROTATION).addListener(ipl);
		properties.getUpdateSource(PInstance.COLOR).addListener(ipl);
		properties.getUpdateSource(PInstance.ALPHA).addListener(ipl);
		properties.put(PInstance.NAME, "inst_" + String.format("%08X", new Random().nextInt()));
		}

	protected void fireUpdate(UpdateEvent e)
		{
		if (e == null) e = updateTrigger.getEvent();
		updateTrigger.fire(e);
		Room r = room == null ? null : room.get();
		if (r != null) r.instanceUpdated(e);
		}

	public int getID()
		{
		return properties.get(PInstance.ID);
		}

	@Override
	public void setName(String name)
		{
		properties.put(PInstance.NAME, name);
		}

	@Override
	public String getName()
		{
		return properties.get(PInstance.NAME);
		}

	public Point getPosition()
		{
		return new Point((Integer) properties.get(PInstance.X),(Integer) properties.get(PInstance.Y));
		}

	public int getAlpha()
		{
		return properties.get(PInstance.ALPHA);
		}

	public void setAlpha(int alpha)
		{
		properties.put(PInstance.ALPHA,alpha);
		}

	public Point2D getScale()
		{
		return new Point2D.Double((Double) properties.get(PInstance.SCALE_X),
				(Double) properties.get(PInstance.SCALE_Y));
		}

	public double getRotation()
		{
		return properties.get(PInstance.ROTATION);
		}

	public Color getColor()
		{
		return properties.get(PInstance.COLOR);
		}

	public Color getAWTColor()
		{
		//TODO: Write this properly
		return properties.get(PInstance.COLOR);
		}

	public void setPosition(Point pos)
		{
		properties.put(PInstance.X,pos.x);
		properties.put(PInstance.Y,pos.y);
		}

	public void setScale(Point2D scale)
		{
		properties.put(PInstance.SCALE_X,scale.getX());
		properties.put(PInstance.SCALE_Y,scale.getY());
		}

	public void setRotation(double degrees)
		{
		properties.put(PInstance.ROTATION,degrees);
		}

	public void setSelected(boolean selected)
		{
		properties.put(PInstance.SELECTED,selected);
		}

	public boolean isSelected()
		{
		return (Boolean) properties.get(PInstance.SELECTED);
		}

	public void setColor(Color color)
		{
		properties.put(PInstance.COLOR,color);
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
			if (e.key == PInstance.NAME) fireUpdate(null);
			if (e.key == PInstance.SELECTED) fireUpdate(null);
			if (e.key == PInstance.SCALE_X) fireUpdate(null);
			if (e.key == PInstance.SCALE_Y) fireUpdate(null);
			if (e.key == PInstance.ROTATION) fireUpdate(null);
			if (e.key == PInstance.COLOR)	fireUpdate(null);
			if (e.key == PInstance.ALPHA) fireUpdate(null);
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
		if (!(obj instanceof Instance)) return false;
		Instance other = (Instance) obj;
		if (properties == null)
			{
			if (other.properties != null) return false;
			}
		else if (!properties.equals(other.properties)) return false;
		return true;
		}
	}
