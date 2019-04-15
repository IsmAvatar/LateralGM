/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.Rectangle;

public abstract class VisualBox extends AbstractVisual
	{
	final BinVisual bv;
	private final Rectangle bounds = new Rectangle();
	private boolean removed;

	public VisualBox(BinVisual v)
		{
		super(v);
		bv = v;
		v.add(this,null,0);
		}

	protected void setBounds(Rectangle b)
		{
		if (removed) return;
		bounds.setBounds(b);
		bv.setBounds(this,bounds);
		}

	protected void repaint()
		{
		repaint(bounds);
		}

	public void remove()
		{
		removed = true;
		bv.remove(this);
		}
	}
