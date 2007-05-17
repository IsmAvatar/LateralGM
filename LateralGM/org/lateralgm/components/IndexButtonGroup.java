/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Container;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

public class IndexButtonGroup
	{
	private static final long serialVersionUID = 1L;

	private class But
		{
		AbstractButton b;
		int i;

		But(AbstractButton b, int i)
			{
			this.b = b;
			this.i = i;
			}
		}

	private But bm[];
	private int bs;
	private ButtonGroup g = null;

	public IndexButtonGroup(int s, boolean exclusive)
		{
		if (exclusive) g = new ButtonGroup();
		bm = new But[s];
		bs = 0;
		}

	public IndexButtonGroup(int s)
		{
		this(s,true);
		}
	
	public void add(AbstractButton b, int value)
		{
		if (g != null) g.add(b);
		bm[bs++] = new But(b,value);
		}

	public int getValue()
		{
		int value = 0;
		for (But b : bm)
			{
			if (b.b.isSelected()) value |= b.i;
			}
		return value;
		}

	public void setValue(int value)
		{
		for (But b : bm)
			{
			if (((b.i & value) != 0) || (b.i == value)) b.b.setSelected(true);
			}
		}

	public void populate(Container c)
		{
		for (But b : bm)
			{
			c.add(b.b);
			}
		}
	}
