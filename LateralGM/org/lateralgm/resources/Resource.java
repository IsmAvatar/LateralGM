/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * 
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.resources;

import java.awt.image.BufferedImage;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public abstract class Resource<R extends Resource<R,P>, P extends Enum<P>> implements
		Comparable<Resource<R,P>>,PropertyValidator<P>
	{
	public enum Kind
		{
		SPRITE,SOUND,BACKGROUND,PATH,SCRIPT,FONT,TIMELINE,OBJECT,ROOM,GAMEINFO,GAMESETTINGS,EXTENSIONS
		}

	private ResNode node;
	private String name = "";
	private int id = -1; //indicates id not set
	public final ResourceReference<R> reference;
	public final PropertyMap<P> properties = makePropertyMap();

	public Resource()
		{
		this(null,true);
		}

	@SuppressWarnings("unchecked")
	public Resource(ResourceReference<R> r, boolean update)
		{
		if (r == null)
			reference = new ResourceReference<R>((R) this);
		else
			{
			reference = r;
			if (update) updateReference();
			}
		}

	public void setId(int id)
		{
		this.id = id;
		fireUpdate();
		}

	public int getId()
		{
		return id;
		}

	public int compareTo(Resource<R,P> res)
		{
		return res.id == id ? 0 : (res.id < id ? -1 : 1);
		}

	protected void fireUpdate()
		{
		reference.updateTrigger.fire();
		}

	public String getName()
		{
		return name;
		}

	public void setName(String name)
		{
		this.name = name;
		fireUpdate();
		}

	public ResNode getNode()
		{
		return node;
		}

	public void setNode(ResNode node)
		{
		this.node = node;
		}

	public BufferedImage getDisplayImage()
		{
		return null;
		}

	@SuppressWarnings("unchecked")
	public final void updateReference()
		{
		reference.set((R) this);
		}

	public final R copy(ResourceList<R> src)
		{
		return copy(src,null,true);
		}

	public final R clone()
		{
		return copy(null,reference,false);
		}

	public void dispose()
		{
		reference.set(null);
		}

	public void put(P key, Object value)
		{
		properties.put(key,value);
		}

	public <V>V get(P key)
		{
		return properties.get(key);
		}

	protected abstract PropertyMap<P> makePropertyMap();

	protected abstract R copy(ResourceList<R> src, ResourceReference<R> ref, boolean update);

	protected void copy(ResourceList<R> src, R dest)
		{
		dest.properties.putAll(properties);
		if (src != null)
			{
			dest.setName(Prefs.prefixes.get(getKind()) + (src.lastId + 1));
			src.add(dest);
			}
		else
			{
			dest.setId(getId());
			dest.setName(getName());
			}
		}

	public abstract Kind getKind();

	public Object validate(P k, Object v)
		{
		return v;
		}

	public String toString()
		{
		return name;
		}
	}
