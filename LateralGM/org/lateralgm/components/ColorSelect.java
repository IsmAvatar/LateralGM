/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

import org.lateralgm.messages.Messages;

public class ColorSelect extends JPanel
	{
	private static final long serialVersionUID = 1L;
	private Color selectedColor;

	public ColorSelect(Color col)
		{
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackground(col);
		selectedColor = col;
		enableEvents(MouseEvent.MOUSE_FIRST);
		}

	public void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_CLICKED)
			{
			Color newcol = JColorChooser.showDialog(getParent(),
					Messages.getString("ColorSelect.CHOOSE_TITLE"),selectedColor); //$NON-NLS-1$
			if (newcol != null)
				{
				selectedColor = newcol;
				setBackground(newcol);
				setBorder(BorderFactory.createLineBorder(Color.BLACK));
				}
			}
		super.processMouseEvent(e);
		}

	public void setSelectedColor(Color selectedColor)
		{
		this.selectedColor = selectedColor;
		setBackground(selectedColor);
		}

	public Color getSelectedColor()
		{
		return selectedColor;
		}
	}
