/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.util;

import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class BinPlane
	{
	public final int binShift;
	private final ConcurrentHashMap<Integer,WeakReference<Bin>> bins;

	public BinPlane(int s, int w, int h)
		{
		binShift = 32 - Integer.numberOfLeadingZeros(s - 1);
		int c = (1 + (w - 1 >> binShift)) * (1 + (h - 1 >> binShift)) * 2;
		bins = new ConcurrentHashMap<Integer,WeakReference<Bin>>(c,0.5f,2);
		}

	public static enum Edge
		{
		LEFT(null)
			{
			public int compareBin(int i0, int i1)
				{
				return Integer.valueOf(i1 << 16 >> 16).compareTo(i0 << 16 >> 16);
				}

			public int getValue(int left, int right, int top, int bottom)
				{
				return -left;
				}
			},
		RIGHT(LEFT),TOP(null)
			{
			public int compareBin(int i0, int i1)
				{
				return Integer.valueOf(i1 >> 16).compareTo(i0 >> 16);
				}

			public int getValue(int left, int right, int top, int bottom)
				{
				return -top;
				}
			},
		BOTTOM(TOP);
		private Edge opposite;

		private Edge(Edge o)
			{
			opposite = o;
			if (o != null) o.opposite = this;
			}

		public int compareBin(int i0, int i1)
			{
			return opposite.compareBin(i1,i0);
			}

		public int getValue(int left, int right, int top, int bottom)
			{
			return opposite.getValue(-right,-left,-bottom,-top);
			}

		public int compareBounds(Rectangle b0, Rectangle b1)
			{
			int v0 = getValue(b0.x,b0.x + b0.width,b0.y,b0.y + b0.height);
			int v1 = getValue(b1.x,b1.x + b1.width,b1.y,b1.y + b1.height);
			return v0 > v1 ? 1 : v0 < v1 ? -1 : 0;
			}
		}

	public Bin[] getEdgeBins(Edge edge)
		{
		ArrayList<Bin> l = new ArrayList<Bin>();
		int s = 0;
		for (Entry<Integer,WeakReference<Bin>> e : bins.entrySet())
			{
			Bin b = e.getValue().get();
			if (b == null || b.candidates.size() == 0) continue;
			if (s == 0)
				{
				s = 1;
				l.add(b);
				continue;
				}
			switch (edge.compareBin(b.index,l.get(s - 1).index))
				{
				case 0:
					if (l.size() > s++)
						l.set(s - 1,b);
					else
						l.add(b);
					break;
				case 1:
					s = 1;
					if (l.size() > 0)
						l.set(0,b);
					else
						l.add(b);
					break;
				default:
					continue;
				}
			}
		return l.subList(0,s).toArray(new Bin[s]);
		}

	public Candidate getEdgeCandidate(Edge edge)
		{
		Candidate ec = null;
		for (Bin b : getEdgeBins(edge))
			for (Candidate c : b.candidates)
				if (ec == null || edge.compareBounds(c.bounds,ec.bounds) > 0) ec = c;
		return ec;
		}

	public Iterator<Candidate> getBin(final int bx, final int by)
		{
		int bi = binindex(bx,by);
		WeakReference<Bin> r = bins.get(bi);
		if (r == null) return null;
		Bin b = r.get();
		if (b == null) return null;
		return b.candidates.iterator();
		}

	public Iterator<Candidate> getBin(final int bx, final int by, final boolean cutLeft,
			final boolean cutAbove)
		{
		final Iterator<Candidate> b = getBin(bx,by);
		if (b == null) return null;
		return new Iterator<Candidate>()
			{
				private Candidate c = findNext();

				public boolean hasNext()
					{
					return c != null;
					}

				public Candidate next()
					{
					if (c == null) throw new NoSuchElementException();
					Candidate r = c;
					c = findNext();
					return r;
					}

				private Candidate findNext()
					{
					while (true)
						{
						if (!b.hasNext()) return null;
						Candidate bc = b.next();
						if ((cutLeft && bc.bounds.x >> binShift != bx)
								|| (cutAbove && bc.bounds.y >> binShift != by)) continue;
						return bc;
						}
					}

				public void remove()
					{
					throw new UnsupportedOperationException();
					}
			};
		}

	public Iterator<Candidate> intersect(final int bx, final int by, final Rectangle r,
			final boolean cut)
		{
		final Iterator<Candidate> b = getBin(bx,by,cut && r.x >> binShift != bx,cut
				&& r.y >> binShift != by);
		if (b == null) return null;
		return new Iterator<Candidate>()
			{
				private Candidate c = findNext();

				public boolean hasNext()
					{
					return c != null;
					}

				public Candidate next()
					{
					if (c == null) throw new NoSuchElementException();
					Candidate r = c;
					c = findNext();
					return r;
					}

				private Candidate findNext()
					{
					while (true)
						{
						if (!b.hasNext()) return null;
						Candidate bc = b.next();
						if (bc.bounds.intersects(r)) return bc;
						}
					}

				public void remove()
					{
					throw new UnsupportedOperationException();
					}
			};
		}

	public Iterator<CandidateBin> intersect(final Rectangle r, final boolean cut)
		{
		return new Iterator<CandidateBin>()
			{
				private boolean inside;
				private Iterator<Candidate> b;
				private boolean end = false;
				private int x0 = r.x >> binShift;
				private int x1 = r.x + r.width - 1 >> binShift;
				private int y0 = r.y >> binShift;
				private int y1 = r.y + r.height - 1 >> binShift;
				private int bx = x0 - 1;
				private int by = y0;
				private CandidateBin c = findNext();

				public boolean hasNext()
					{
					return c != null;
					}

				public CandidateBin next()
					{
					if (c == null) throw new NoSuchElementException();
					CandidateBin r = c;
					c = findNext();
					return r;
					}

				private void nextBin()
					{
					bx++;
					if (bx > x1)
						{
						by++;
						if (by > y1)
							{
							b = null;
							end = true;
							return;
							}
						bx = x0;
						}
					inside = (bx << binShift >= r.x && by << binShift >= r.y && bx < x1 - 1 && by < y1 - 1);
					b = inside ? (cut ? getBin(bx,by,true,true) : getBin(bx,by)) : intersect(bx,by,r,cut);
					}

				private CandidateBin findNext()
					{
					while (true)
						{
						nextBin();
						if (b == null)
							{
							if (end) return null;
							continue;
							}
						return new CandidateBin(bx << binShift,by << binShift,1 << binShift,1 << binShift,b);
						}
					}

				public void remove()
					{
					throw new UnsupportedOperationException();
					}
			};
		}

	public Iterator<CandidateBin> all(final boolean cut)
		{
		final Iterator<Entry<Integer,WeakReference<Bin>>> es = bins.entrySet().iterator();
		return new Iterator<CandidateBin>()
			{
				private CandidateBin cb = findNext();

				public boolean hasNext()
					{
					return cb != null;
					}

				public CandidateBin next()
					{
					if (cb == null) throw new NoSuchElementException();
					CandidateBin r = cb;
					cb = findNext();
					return r;
					}

				private CandidateBin findNext()
					{
					if (!es.hasNext()) return null;
					Entry<Integer,WeakReference<Bin>> e = es.next();
					int i = e.getKey();
					int bx = i << 16 >> 16;
					int by = i >> 16;
					Bin b = e.getValue().get();
					if (b == null) return null;
					return new CandidateBin(bx << binShift,by << binShift,1 << binShift,1 << binShift,
							cut ? getBin(bx,by,true,true) : b.iterator());
					}

				public void remove()
					{
					throw new UnsupportedOperationException();
					}
			};

		}

	private static int binindex(int x, int y)
		{
		if (x << 16 >> 16 != x || y << 16 >> 16 != y) throw new IllegalArgumentException();
		return x & -1 >>> 16 | y << 16;
		}

	public class Candidate implements Comparable<Candidate>
		{
		public Object data;
		private int binx, biny, binw, binh;
		private final Rectangle bounds = new Rectangle(-1,-1);
		private int depth;
		// If the candidate is selected, it should be always visible
		private boolean isSelected = false;
		private Bin[] cBins;

		public void setDepth(int d)
			{
			setDepth(d,false);
			}

		public void setDepth(int d, boolean selected)
			{
			//if (depth == d) return;
			if (cBins == null)
				{
				depth = d;
				isSelected = selected;
				return;
				}
			for (Bin b : cBins)
				b.candidates.remove(this);
			depth = d;
			isSelected = selected;
			for (Bin b : cBins)
				b.candidates.add(this);
			}

		public void setBounds(Rectangle b)
			{
			bounds.setBounds(b);
			int obx = binx;
			int oby = biny;
			int obw = binw;
			int obh = binh;
			binx = b.x >> binShift;
			biny = b.y >> binShift;
			binw = 1 + (b.x + b.width - 1 >> binShift) - binx;
			binh = 1 + (b.y + b.height - 1 >> binShift) - biny;
			if (binx == obx && biny == oby && binw == obw && binh == obh) return;
			Bin[] ob = cBins;
			cBins = new Bin[binw * binh];
			int i = 0;
			for (int y = 0; y < obh; y++)
				for (int x = 0; x < obw; x++)
					{
					int xo = obx + x - binx;
					if (xo < 0 || xo >= binw)
						{
						ob[i++].candidates.remove(this);
						continue;
						}
					int yo = oby + y - biny;
					if (yo < 0 || yo >= binh)
						{
						ob[i++].candidates.remove(this);
						continue;
						}
					cBins[xo + binw * yo] = ob[i++];
					}
			i = 0;
			for (int y = 0; y < binh; y++)
				for (int x = 0; x < binw; x++)
					{
					if (cBins[i] == null)
						{
						int idx = binindex(binx + x,biny + y);
						WeakReference<Bin> r = bins.get(idx);
						Bin bin = r == null ? null : r.get();
						if (bin == null)
							{
							bin = new Bin(idx);
							}
						cBins[i] = bin;
						bin.candidates.add(this);
						}
					i++;
					}
			}

		public Rectangle getBounds(Rectangle b)
			{
			if (b == null) return bounds.getBounds();
			b.setBounds(bounds);
			return b;
			}

		public void remove()
			{
			for (Bin b : cBins)
				b.candidates.remove(this);
			cBins = null;
			bounds.setSize(-1,-1);
			binw = 0;
			binh = 0;
			}

		public int compareTo(Candidate c)
			{
			if (this == c) return 0;
			// If the candidate is selected, it should be always visible
			if (isSelected) return 1;

			return c.depth > depth ? 1 : c.depth < depth ? -1
					: new Integer(c.hashCode()).compareTo(hashCode());
			}
		}

	public static abstract class LateralIterator<T> implements Iterator<T>
		{
		protected Iterator<T> iter;

		public boolean hasNext()
			{
			if (iter == null || !iter.hasNext())
				{
				iter = getNextIterator();
				if (iter == null) return false;
				}
			return true;
			}

		public T next()
			{
			return iter.next();
			}

		public void remove()
			{
			iter.remove();
			}

		protected abstract Iterator<T> getNextIterator();
		}

	public static final class CandidateIterator extends LateralIterator<Candidate>
		{
		final Iterator<CandidateBin> cbi;

		public CandidateIterator(Iterator<CandidateBin> i)
			{
			cbi = i;
			}

		@Override
		protected Iterator<Candidate> getNextIterator()
			{
			while (cbi.hasNext())
				{
				Iterator<Candidate> r = cbi.next().iterator;
				if (r != null && r.hasNext()) return r;
				}
			return null;
			}
		}

	/**
	 * Abstract Iterator wrapper/implementation which allows the individual elements
	 * to be converted or even bypassed as needed (by having convert() return null).
	 * This implementation will not return null elements.
	 * <p>
	 * Note that due to the way this iterator is implemented,
	 * dynamic removal of elements is not possible.
	 * @param <T1> The type of the wrapped iterator.
	 * @param <T2> The output (converted) type of the implementation iterator.
	 */
	public static abstract class ConversionIterator<T1, T2> implements Iterator<T2>
		{
		/** The wrapped iterator */
		protected final Iterator<T1> iter;
		/**
		 * Temporarily stores the next element that was able
		 * to convert, between calls to hasNext() and next()
		 */
		private T2 next;

		/**
		 * Wraps the given iterator.
		 * @param t1 The iterator to wrap
		 */
		public ConversionIterator(Iterator<T1> t1)
			{
			iter = t1;
			}

		public boolean hasNext()
			{
			if (next == null) next = findNext();
			return next != null;
			}

		public T2 next()
			{
			T2 n = next == null ? findNext() : next;
			next = null;
			return n;
			}

		@Deprecated
		public void remove()
			{
			// Simply doing ci.remove() here wouldn't work if hasNext has been called.
			throw new UnsupportedOperationException();
			}

		/**
		 * Prepares the next available converted element for iteration.
		 * @return The converted element.
		 */
		private T2 findNext()
			{
			while (iter.hasNext())
				{
				T1 c = iter.next();
				T2 r = convert(c);
				if (r != null) return r;
				}
			return null;
			}

		/**
		 * Converts a given element into the implementation type.
		 * Implementations may return null to entirely skip the element.
		 * The implementation iterator will skip to the next non-null conversion.
		 * @param c An element from the wrapped iterator.
		 * @return A converted element for the implementation iterator, or null.
		 */
		protected abstract T2 convert(T1 c);
		}

	public static final class CandidateDataIterator<T> extends ConversionIterator<Candidate,T>
		{
		private final Class<T> ct;

		public CandidateDataIterator(Iterator<CandidateBin> i, Class<T> t)
			{
			super(new CandidateIterator(i));
			ct = t;
			}

		@Override
		protected T convert(Candidate c)
			{
			return ct.isInstance(c.data) ? ct.cast(c.data) : null;
			}
		}

	public static final class CandidateDepthDataIterator<T> extends ConversionIterator<Candidate,T>
		{
		private final Class<T> ct;
		private final int depth;

		public CandidateDepthDataIterator(Iterator<CandidateBin> i, Class<T> t, int depth)
			{
			super(new CandidateIterator(i));
			ct = t;
			this.depth = depth;
			}

		@Override
		protected T convert(Candidate c)
			{
			return ct.isInstance(c.data) && c.depth == depth ? ct.cast(c.data) : null;
			}
		}

	public static final class CandidateBin
		{
		public final int x, y, w, h;
		public final Iterator<Candidate> iterator;

		public CandidateBin(int x, int y, int w, int h, Iterator<Candidate> i)
			{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			iterator = i;
			}
		}

	public final class Bin
		{
		private TreeSet<Candidate> candidates = new TreeSet<Candidate>();
		public final int index;
		public final WeakReference<Bin> reference;

		public Bin(int idx)
			{
			index = idx;
			reference = new WeakReference<Bin>(this);
			bins.put(idx,reference);
			}

		public Iterator<Candidate> iterator()
			{
			return candidates.iterator();
			}

		@Override
		protected void finalize()
			{
			bins.remove(index,reference);
			}
		}
	}
