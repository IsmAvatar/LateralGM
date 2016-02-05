/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.lateralgm.file.ProjectFile.ResourceHolder;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;

public class ResourceList<R extends InstantiableResource<R,?>> extends TreeSet<R> implements
		UpdateListener,ResourceHolder<R>
	{
	private static final long serialVersionUID = 1L;

	private static final IdComparator COMPARATOR = new IdComparator();

	private final Class<R> type; // used as a workaround for add()
	private final HashMap<ResourceReference<R>,WeakReference<R>> refMap;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	ResourceList(Class<R> type)
		{
		super(COMPARATOR);
		this.type = type;
		refMap = new HashMap<ResourceReference<R>,WeakReference<R>>();
		}

	public int lastId = -1;

	private boolean doAdd(R res)
		{
		WeakReference<R> wr = refMap.get(res.reference);
		R r0 = wr == null ? null : wr.get();
		if (r0 != null)
			{
			if (r0 == res) return false;
			super.remove(r0);
			}
		res.reference.updateSource.addListener(this);
		refMap.put(res.reference,new WeakReference<R>(res));
		return super.add(res);
		}

	/**
	 * Notice: due to the nature of this list being sorted,
	 * changing the id of a resource after adding it may cause
	 * this list to become unsorted and result in unexpected behavior.
	 */
	public boolean add(R res)
		{
		//don't override id if it's already set
		if (res.getId() == -1) res.setId(++lastId);
		if (doAdd(res))
			{
			updateTrigger.fire();
			return true;
			}
		return false;
		}

	public boolean addAll(Collection<? extends R> c)
		{
		boolean r = false;
		for (R res : c)
			{
			res.setId(++lastId);
			r |= doAdd(res);
			}
		if (r) updateTrigger.fire();
		return r;
		}

	/**
	 * Notice: due to the nature of this list being sorted,
	 * changing the id of a resource after adding it may cause
	 * this list to become unsorted and result in unexpected behavior.
	 */
	public R add()
		{
		R res = null;
		try
			{
			res = type.newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		if (res != null)
			{
			res.setName(res.getName() + (lastId + 1));
			add(res);
			}
		return res;
		}

	public R getResource()
		{
		return add();
		}

	/**
	 * Duplicates the given resource as per user request. Adds the duplicate to this list.
	 */
	@SuppressWarnings("unchecked")
	public R duplicate(Resource<?,?> src)
		{
		if (!this.contains(src)) return null;
		R dest = add();
		((R) src).copy(dest);
		return dest;
		}

	public R getUnsafe(int id)
		{
		for (R res : this)
			{
			int ri = res.getId();
			if (ri == id)
				return res;
			else if (ri > id) break;
			}
		return null;
		}

	/** May return null */
	public R get(String name)
		{
		for (R res : this)
			if (res.getName().equals(name)) return res;
		return null;
		}

	private boolean doRemove(Resource<?,?> res)
		{
		if (super.remove(res))
			{
			res.reference.updateSource.removeListener(this);
			refMap.remove(res.reference);
			return true;
			}
		return false;
		}

	public boolean remove(Object o)
		{
		if (doRemove((Resource<?,?>) o))
			{
			updateTrigger.fire();
			return true;
			}
		return false;
		}

	public boolean removeAll(Collection<?> c)
		{
		boolean r = false;
		for (Object o : c)
			r |= doRemove((Resource<?,?>) o);
		if (r) updateTrigger.fire();
		return r;
		}

	public boolean retainAll(Collection<?> c)
		{
		boolean r = false;
		for (R res : this)
			if (!c.contains(res)) r |= doRemove(res);
		if (r) updateTrigger.fire();
		return r;
		}

	public void clear()
		{
		if (size() == 0) return;
		for (R r : this)
			r.reference.updateSource.removeListener(this);
		refMap.clear();
		super.clear();
		updateTrigger.fire();
		}

	public void defragIds()
		{
		int i = 0;
		for (R res : this)
			res.setId(i++);
		lastId = i - 1;
		}

	public void updated(UpdateEvent e)
		{
		assert size() == refMap.size();
		updateTrigger.fire(e);
		Object o = e.source.owner;
		if (o instanceof ResourceReference<?>)
			{
			ResourceReference<?> ref = (ResourceReference<?>) o;
			WeakReference<R> wr = refMap.get(ref);
			R r0 = wr == null ? null : wr.get();
			if (r0 != null)
				{
				Resource<?,?> r = ref.get();
				if (r0 != r)
					{
					remove(r0);
					if (r != null) add(type.cast(r));
					}
				else
					{
					// Ensure that the set stays sorted.
					boolean changed = false;
					try
						{
						Comparator<? super R> c = comparator();
						test:
							{
							R h = higher(r0);
							if (h != null && c.compare(r0,h) >= 0)
								{
								changed = true;
								break test;
								}
							R l = lower(r0);
							if (l != null && c.compare(r0,l) <= 0)
								{
								changed = true;
								break test;
								}
							}
						}
					catch (NoSuchMethodError nsme)
						{
						changed = true;
						}
					if (changed)
						{
						remove(r0);
						add(r0);
						}
					}
				}
			}
		}

	private static class IdComparator implements Comparator<InstantiableResource<?,?>>
		{
		public int compare(InstantiableResource<?,?> o1, InstantiableResource<?,?> o2)
			{
			if (o1.reference == o2.reference) return 0;
			int i1 = o1.getId();
			int i2 = o2.getId();
			if (i1 == i2) return Integer.signum(o1.hashCode() - o2.hashCode());
			return i1 < i2 ? -1 : 1;
			}
		}
	}
