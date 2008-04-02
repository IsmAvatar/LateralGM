/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

import org.lateralgm.messages.Messages;

public class ColorSelect extends JPanel
	{
	private static final long serialVersionUID = 1L;
	private Color selectedColor;
	private ActionEvent actionEvent;

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
				fireActionPerformed();
				}
			}
		super.processMouseEvent(e);
		}

	/**
	 * Adds a listener for purpose of finding out when a new color is chosen.
	 * Notice that the listener will not be notified until the dialog box resolves.
	 * For your convenience, setSelectedColor will not notify the listener.
	 * ActionListener was an arbitrary choice to match with common swing components.
	 */
	public void addActionListener(ActionListener al)
		{
		listenerList.add(ActionListener.class,al);
		}

	public void removeActionListener(ActionListener il)
		{
		listenerList.remove(ActionListener.class,il);
		}

	protected void fireActionPerformed()
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ActionListener.class)
				{
				if (actionEvent == null)
					actionEvent = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""); //$NON-NLS-1$
				((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
				}
			}
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
