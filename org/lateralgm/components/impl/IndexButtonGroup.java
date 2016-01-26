/*
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.Container;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

public class IndexButtonGroup
	{
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
	private boolean bitwise;
	private ActionListener a;

	public IndexButtonGroup(int s, boolean exclusive, boolean bitwise, ActionListener a)
		{
		if (exclusive || !bitwise) g = new ButtonGroup();
		bm = new But[s];
		bs = 0;
		this.bitwise = bitwise;
		this.a = a;
		}

	public IndexButtonGroup(int s, boolean exclusive, boolean bitwise)
		{
		this(s,exclusive,bitwise,null);
		}

	public IndexButtonGroup(int s, boolean exclusive)
		{
		this(s,exclusive,true,null);
		}

	public IndexButtonGroup(int s)
		{
		this(s,true,true,null);
		}

	public void add(AbstractButton b, int value)
		{
		if (g != null) g.add(b);
		bm[bs++] = new But(b,value);
		if (a != null) b.addActionListener(a);
		}

	public void add(AbstractButton b)
		{
		if (bitwise)
			add(b,1 << bs);
		else
			add(b,bs);
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
			if (bitwise)
				{
				if ((b.i & value) != 0) b.b.setSelected(true);
				}
			else
				{
				if (b.i == value)
					{
					b.b.setSelected(true);
					return;
					}
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
