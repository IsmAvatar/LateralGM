/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
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

public abstract class InstantiableResource<R extends InstantiableResource<R,P>, P extends Enum<P>>
		extends Resource<R,P>
	{
	private int id = -1; //indicates id not set

	public InstantiableResource()
		{
		super();
		}

	public InstantiableResource(ResourceReference<R> r)
		{
		super(r);
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

	protected void postCopy(R dest)
		{
		dest.setId(getId());
		}

	public abstract R makeInstance(ResourceReference<R> ref);

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null || !(obj instanceof InstantiableResource<?,?>)) return false;
		InstantiableResource<?,?> other = (InstantiableResource<?,?>) obj;
		if (id != other.id || !name.equals(other.name) || reference != other.reference) return false;
		return properties.equals(other.properties);
		}
	}
