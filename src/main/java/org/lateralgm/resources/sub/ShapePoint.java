/**
* @file  ShapePoint.java
* @brief Class implementing a Physics Shape Point interface.
*
* @section License
*
* Copyright (C) 2014 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.resources.sub;

import java.util.EnumMap;

import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class ShapePoint implements PropertyValidator<ShapePoint.PShapePoint>
	{
	public enum PShapePoint
		{
		X,Y
		}

	private static final EnumMap<PShapePoint,Object> DEFS = PropertyMap.makeDefaultMap(
			PShapePoint.class,0,0);

	public final PropertyMap<PShapePoint> properties;

	public ShapePoint()
		{
		properties = new PropertyMap<PShapePoint>(PShapePoint.class,this,DEFS);
		}

	public ShapePoint(int x, int y)
		{
		properties = new PropertyMap<PShapePoint>(PShapePoint.class,this,PropertyMap.makeDefaultMap(
				PShapePoint.class,x,y));
		}

	public String toString()
		{
		String r = "(" + getX() + "," + getY() + ")";
		while (r.length() < 11)
			r += " ";
		return r;
		}

	public int getX()
		{
		return properties.get(PShapePoint.X);
		}

	public void setX(int x)
		{
		properties.put(PShapePoint.X,x);
		}

	public int getY()
		{
		return properties.get(PShapePoint.Y);
		}

	public void setY(int y)
		{
		properties.put(PShapePoint.Y,y);
		}

	public Object validate(PShapePoint k, Object v)
		{
		return v;
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
		if (!(obj instanceof ShapePoint)) return false;
		ShapePoint other = (ShapePoint) obj;
		if (properties == null)
			{
			if (other.properties != null) return false;
			}
		else if (!properties.equals(other.properties)) return false;
		return true;
		}
	}
