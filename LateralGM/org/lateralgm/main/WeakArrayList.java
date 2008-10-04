package org.lateralgm.main;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

public class WeakArrayList<E> extends ArrayList<WeakReference<E>>
	{
	private static final long serialVersionUID = 1L;
	private final ReferenceQueue<E> queue = new ReferenceQueue<E>();

	public WeakArrayList()
		{
		super();
		}

	@Override
	public int size()
		{
		expungeStaleEntries();
		return super.size();
		}

	@Override
	public boolean isEmpty()
		{
		expungeStaleEntries();
		return size() == 0;
		}

	@Override
	public boolean contains(Object o)
		{
		expungeStaleEntries();
		return super.contains(o);
		}

	@Override
	public boolean containsAll(Collection<?> c)
		{
		expungeStaleEntries();
		return super.containsAll(c);
		}

	@Override
	public int indexOf(Object o)
		{
		expungeStaleEntries();
		return super.indexOf(o);
		}

	@Override
	public int lastIndexOf(Object o)
		{
		expungeStaleEntries();
		return super.lastIndexOf(o);
		}

	@Override
	public Iterator<WeakReference<E>> iterator()
		{
		expungeStaleEntries();
		return super.iterator();
		}

	@Override
	public ListIterator<WeakReference<E>> listIterator()
		{
		expungeStaleEntries();
		return super.listIterator();
		}

	@Override
	public void trimToSize()
		{
		expungeStaleEntries();
		super.trimToSize();
		}

	@Override
	public Object[] toArray()
		{
		expungeStaleEntries();
		return super.toArray();
		}

	@Override
	public <T>T[] toArray(T[] a)
		{
		expungeStaleEntries();
		return super.toArray(a);
		}

	private void expungeStaleEntries()
		{
		Reference<? extends E> r;
		while ((r = queue.poll()) != null)
			remove(r);
		}
	}
