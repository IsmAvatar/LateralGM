/*
 * Copyright (C) 2007, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.messages.Messages;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public abstract class Resource<R extends Resource<R,P>, P extends Enum<P>> implements
		PropertyValidator<P>
	{
	public static final Map<String,Class<? extends Resource<?,?>>> kindsByName3 = new HashMap<String,Class<? extends Resource<?,?>>>();
	public static final Map<Class<? extends Resource<?,?>>,String> kindNames = new HashMap<Class<? extends Resource<?,?>>,String>();
	public static final Map<Class<? extends Resource<?,?>>,String> kindNamesPlural = new HashMap<Class<? extends Resource<?,?>>,String>();
	public static final ArrayList<Class<? extends Resource<?,?>>> kinds = new ArrayList<Class<? extends Resource<?,?>>>();

	static
		{
		String[] chr3 = { "SPR","SND","BKG","PTH", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"SCR","FNT","TML","OBJ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"RMM","GMI","GMS","EXT" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Class<?>[] ca = { Sprite.class,Sound.class,Background.class,Path.class,Script.class,Font.class,
				Timeline.class,GmObject.class,Room.class,GameInformation.class,GameSettings.class,
				Extensions.class, };
		for (int i = 0; i < chr3.length; i++)
			addKind(chr3[i],ca[i]);
		}

	@SuppressWarnings("unchecked")
	public static void addKind(String str3, Class<?> clz)
		{
		kinds.add((Class<? extends Resource<?,?>>) clz);
		kindsByName3.put(str3,(Class<? extends Resource<?,?>>) clz);

		kindNames.put((Class<? extends Resource<?,?>>) clz,Messages.getString("LGM." + str3)); //$NON-NLS-1$
		kindNamesPlural.put((Class<? extends Resource<?,?>>) clz,Messages.getString("LGM.PL_" + str3)); //$NON-NLS-1$
		}

	protected ResNode node;
	protected String name = "";
	public final ResourceReference<R> reference;
	public final PropertyMap<P> properties = makePropertyMap();

	public static interface Viewable
		{
		BufferedImage getDisplayImage();
		}

	public Resource()
		{
		this(null);
		}

	@SuppressWarnings("unchecked")
	public Resource(ResourceReference<R> r)
		{
		if (r == null)
			reference = new ResourceReference<R>((R) this);
		else
			reference = r;
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

	@SuppressWarnings("unchecked")
	public final void updateReference()
		{
		reference.set((R) this);
		}

	//Called when user wishes to duplicate a Resource
	public final void copy(R dest)
		{
		dest.properties.putAll(properties);
		postCopy(dest);
		}

	//Used for comparison
	public final R clone()
		{
		R dest = makeInstance(reference);
		dest.properties.putAll(properties);
		dest.node = node;
		dest.setName(getName());
		postCopy(dest);
		return dest;
		}

	public abstract R makeInstance(ResourceReference<R> ref);

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

	/** Copies over information not stored in the properties map. */
	protected abstract void postCopy(R dest);

	public Object validate(P k, Object v)
		{
		return v;
		}

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null || !(obj instanceof Resource<?,?>)) return false;
		Resource<?,?> other = (Resource<?,?>) obj;
		if (!name.equals(other.name) || reference != other.reference) return false;
		return properties.equals(other.properties);
		}

	public String toString()
		{
		return name;
		}
	}
