/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.Rectangle;

public abstract class AbstractVisual implements Visual
	{
	public final VisualContainer parent;

	public AbstractVisual(VisualContainer c)
		{
		parent = c;
		}

	protected void repaint(Rectangle r)
		{
		parent.repaint(r);
		}

	}
