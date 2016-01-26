/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.lateralgm.util.BinPlane;
import org.lateralgm.util.BinPlane.Candidate;
import org.lateralgm.util.BinPlane.CandidateBin;
import org.lateralgm.util.BinPlane.Edge;

public class BinVisual extends AbstractVisual implements VisualContainer,BoundedVisual
	{
	private static final Rectangle ZERO_RECTANGLE = new Rectangle();

	private final BinPlane binPlane;
	private Visual vLeft, vRight, vTop, vBottom;
	private final Rectangle boxBounds = new Rectangle();
	private Map<Visual,Candidate> candidates;

	public BinVisual(VisualContainer c, int s, int x, int y)
		{
		super(c);
		binPlane = new BinPlane(s,x,y);
		}

	@Override
	public void repaint(Rectangle r)
		{
		super.repaint(r);
		}

	void add(Visual v, Rectangle b, int d)
		{
		BinPlane.Candidate c = binPlane.new Candidate();
		c.data = v;
		c.setDepth(d);
		if (candidates == null) candidates = new HashMap<Visual,Candidate>();
		candidates.put(v,c);
		if (b != null) setBounds(c,b);
		}

	boolean remove(Visual v)
		{
		if (candidates == null) return false;
		Candidate c = candidates.remove(v);
		if (c == null) return false;
		repaint(c.getBounds(null));
		c.remove();
		Rectangle obb = boxBounds.getBounds();
		if (v == vLeft) vLeft = null;
		if (v == vRight) vRight = null;
		if (v == vTop) vTop = null;
		if (v == vBottom) vBottom = null;
		fixBounds();
		if (!obb.equals(boxBounds)) parent.updateBounds();
		return true;
		}

	public void setDepth(Visual v, int d)
		{
		setDepth(v,d,false);
		}

	public void setDepth(Visual v, int d, boolean selected)
		{
		Candidate c = getCandidate(v);
		if (c == null) return;
		c.setDepth(d,selected);
		Rectangle ob = c.getBounds(null);
		if (ob != null && !ob.isEmpty()) repaint(ob);
		}

	public void setBounds(Visual v, Rectangle b)
		{
		setBounds(getCandidate(v),b);
		}

	private Candidate getCandidate(Visual v)
		{
		return candidates == null ? null : candidates.get(v);
		}

	private void setBounds(Candidate c, Rectangle b)
		{
		Rectangle ob = c.getBounds(null);
		if (!ob.isEmpty()) repaint(ob);
		Visual v = (Visual) c.data;
		c.setBounds(b);
		Rectangle obb = boxBounds.getBounds();
		if (b.x <= boxBounds.x)
			{
			vLeft = v;
			boxBounds.width += boxBounds.x - b.x;
			boxBounds.x = b.x;
			}
		else if (v == vLeft) vLeft = null;
		int w = b.x + b.width - boxBounds.x;
		if (w >= boxBounds.width)
			{
			vRight = v;
			boxBounds.width = w;
			}
		else if (v == vRight) vRight = null;
		if (b.y <= boxBounds.y)
			{
			vTop = v;
			boxBounds.height += boxBounds.y - b.y;
			boxBounds.y = b.y;
			}
		else if (v == vTop) vTop = null;
		int h = b.y + b.height - boxBounds.y;
		if (h >= boxBounds.height)
			{
			vBottom = v;
			boxBounds.height = h;
			}
		else if (v == vBottom) vBottom = null;
		fixBounds();
		if (!obb.equals(boxBounds)) parent.updateBounds();
		repaint(b);
		}

	private void fixBounds()
		{
		if (vLeft == null)
			{
			Candidate ec = binPlane.getEdgeCandidate(Edge.LEFT);
			int l = ec == null ? 0 : ec.getBounds(null).x;
			if (ec != null) vLeft = (VisualBox) ec.data;
			boxBounds.width += boxBounds.x - l;
			boxBounds.x = l;
			}
		if (vRight == null)
			{
			Candidate ec = binPlane.getEdgeCandidate(Edge.RIGHT);
			Rectangle cb = ec == null ? ZERO_RECTANGLE : ec.getBounds(null);
			if (ec != null) vRight = (VisualBox) ec.data;
			boxBounds.width = cb.x + cb.width - boxBounds.x;
			}
		if (vTop == null)
			{
			Candidate ec = binPlane.getEdgeCandidate(Edge.TOP);
			int t = ec == null ? 0 : ec.getBounds(null).y;
			if (ec != null) vTop = (VisualBox) ec.data;
			boxBounds.height += boxBounds.y - t;
			boxBounds.y = t;
			}
		if (vBottom == null)
			{
			Candidate ec = binPlane.getEdgeCandidate(Edge.BOTTOM);
			Rectangle cb = ec == null ? ZERO_RECTANGLE : ec.getBounds(null);
			if (ec != null) vBottom = (VisualBox) ec.data;
			boxBounds.height = cb.y + cb.height - boxBounds.y;
			}
		}

	public Iterator<Visual> intersect(Rectangle r)
		{
		return intersect(r,Visual.class);
		}

	public <V extends Visual>Iterator<V> intersect(Rectangle r, Class<V> v)
		{
		return new BinPlane.CandidateDataIterator<V>(binPlane.intersect(r,true),v);
		}

	public <V extends Visual>Iterator<V> intersect(Rectangle r, Class<V> v, int depth)
		{
		return new BinPlane.CandidateDepthDataIterator<V>(binPlane.intersect(r,true),v,depth);
		}

	public void paint(Graphics g)
		{
		Rectangle clip = g.getClipBounds();
		Iterator<CandidateBin> cbi = clip == null ? binPlane.all(false)
				: binPlane.intersect(clip,false);
		Rectangle b = null;
		while (cbi.hasNext())
			{
			CandidateBin cb = cbi.next();
			g.clipRect(cb.x,cb.y,cb.w,cb.h);
			while (cb.iterator.hasNext())
				{
				Candidate c = cb.iterator.next();
				Visual v = (Visual) c.data;
				b = c.getBounds(b);
				Graphics g2 = g.create(b.x,b.y,b.width,b.height);
				v.paint(g2);
				g2.dispose();
				}
			g.setClip(clip);
			}
		}

	public void updateBounds()
		{
		//Unused
		}

	public void extendBounds(Rectangle b)
		{
		b.add(boxBounds);
		}
	}
