/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.util.EnumMap;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.PropertyMap.PropertyValidationException;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class BackgroundDef implements UpdateListener,
		PropertyValidator<BackgroundDef.PBackgroundDef>
	{
	private ResourceReference<?> background = null; //kept for listening purposes
	public final PropertyMap<PBackgroundDef> properties;

	private final BgDefPropertyListener bdpl = new BgDefPropertyListener();

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public enum PBackgroundDef
		{
		VISIBLE,FOREGROUND,BACKGROUND,X,Y,TILE_HORIZ,TILE_VERT,H_SPEED,V_SPEED,STRETCH
		}

	private static final EnumMap<PBackgroundDef,Object> DEFS = PropertyMap.makeDefaultMap(
			PBackgroundDef.class,false,false,null,0,0,true,true,0,0,false);

	public BackgroundDef()
		{
		properties = new PropertyMap<PBackgroundDef>(PBackgroundDef.class,this,DEFS);
		properties.getUpdateSource(PBackgroundDef.BACKGROUND).addListener(bdpl);
		}

	protected void fireUpdate(UpdateEvent e)
		{
		if (e == null)
			updateTrigger.fire();
		else
			updateTrigger.fire(e);
		}

	public void updated(UpdateEvent e)
		{
		fireUpdate(new UpdateEvent(updateSource,e));
		}

	public Object validate(PBackgroundDef k, Object v)
		{
		if (k == PBackgroundDef.BACKGROUND)
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

	private class BgDefPropertyListener extends PropertyUpdateListener<PBackgroundDef>
		{
		@Override
		public void updated(PropertyUpdateEvent<PBackgroundDef> e)
			{
			if (e.key == PBackgroundDef.BACKGROUND) fireUpdate(null);
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
		if (!(obj instanceof BackgroundDef)) return false;
		BackgroundDef other = (BackgroundDef) obj;
		if (properties == null)
			{
			if (other.properties != null) return false;
			}
		else if (!properties.equals(other.properties)) return false;
		return true;
		}
	}
