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

import org.lateralgm.util.PropertyMap;

public class View
	{
	public final PropertyMap<PView> properties;

	public enum PView
		{
		VISIBLE,VIEW_X,VIEW_Y,VIEW_W,VIEW_H,PORT_X,PORT_Y,PORT_W,PORT_H,BORDER_H,BORDER_V,SPEED_H,
		SPEED_V,OBJECT,OBJECT_FOLLOWING_X,OBJECT_FOLLOWING_Y
		}

	private static final EnumMap<PView,Object> DEFS = PropertyMap.makeDefaultMap(PView.class,false,0,
			0,640,480,0,0,640,480,32,32,-1,-1,null,-1,-1);

	public View()
		{
		properties = new PropertyMap<PView>(PView.class,null,DEFS);
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
		if (!(obj instanceof View)) return false;
		View other = (View) obj;
		if (properties == null)
			{
			if (other.properties != null) return false;
			}
		else if (!properties.equals(other.properties)) return false;
		return true;
		}
	}
