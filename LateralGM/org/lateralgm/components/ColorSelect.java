/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

public class ColorSelect extends JPanel
	{
	private static final long serialVersionUID = 1L;
	private Color selectedColor;
	private Component parent;

	public ColorSelect(Color col, Component parent)
		{
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackground(col);		
		selectedColor = col;
		this.parent = parent;
		enableEvents(MouseEvent.MOUSE_FIRST);
		}

	public void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_CLICKED)
			{
			Color newcol = JColorChooser.showDialog(parent,"Choose a Color",selectedColor);
			if (newcol != null)
				{
				selectedColor = newcol;
				setBackground(newcol);
				setBorder(BorderFactory.createLineBorder(Color.BLACK));
				}
			}
		super.processMouseEvent(e);
		}
	}
