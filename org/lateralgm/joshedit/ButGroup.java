/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * @author IsmAvatar
 */
public class ButGroup extends ButtonGroup
	{
	/** Stow it, ECJ. */
	private static final long serialVersionUID = 1L;

	/**
	 * @param buts Buttons to add to this group.
	 */
	public ButGroup(AbstractButton...buts)
		{
		super();
		for (AbstractButton b : buts)
			add(b);
		}

	/**
	 * @see javax.swing.ButtonGroup#setSelected(javax.swing.ButtonModel, boolean)
	 */
	@Override
	public void setSelected(ButtonModel m, boolean b)
		{
		if (m == getSelection() && b == false) clearSelection();
		//		else
		super.setSelected(m,b);
		}
	}
