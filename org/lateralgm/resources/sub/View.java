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
		SPEED_V,OBJECT
		}

	private static final EnumMap<PView,Object> DEFS = PropertyMap.makeDefaultMap(PView.class,false,0,
			0,640,480,0,0,640,480,32,32,-1,-1,null);

	public View()
		{
		properties = new PropertyMap<PView>(PView.class,null,DEFS);
		}

	public boolean equals(Object o)
		{
		if (o == this) return true;
		if (o == null || !(o instanceof View)) return false;
		return properties.equals(((View) o).properties);
		}
	}
