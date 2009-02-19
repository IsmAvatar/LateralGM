/*
 * Copyright (C) 2006 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static org.lateralgm.components.GmMenuBar.setTextAndAlt;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;

public class GmMenu extends JMenu
	{
	private static final long serialVersionUID = 1L;

	public GmMenu(String s)
		{
		super();
		setTextAndAlt(this,s);
		}

	public JMenuItem addItem(String key)
		{
		return addItem(key,-1,-1,LGM.listener);
		}

	public JMenuItem addItem(String key, ActionListener listener)
		{
		return addItem(key,-1,-1,listener);
		}

	public JMenuItem addItem(String key, int shortcut, int control)
		{
		return addItem(key,shortcut,control,LGM.listener);
		}

	public JMenuItem addItem(String key, int shortcut, int control, ActionListener listener)
		{
		JMenuItem item = new JMenuItem();
		if (key != null)
			{
			setTextAndAlt(item,Messages.getString(key));
			item.setIcon(LGM.getIconForKey(key));
			item.setActionCommand(key);
			}
		if (shortcut >= 0) item.setAccelerator(KeyStroke.getKeyStroke(shortcut,control));
		item.addActionListener(listener);
		add(item);
		return item;
		}
	}
