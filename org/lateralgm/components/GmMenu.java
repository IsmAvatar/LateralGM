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

import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.messages.Messages;

public class GmMenu extends JMenu
	{
	private static final long serialVersionUID = 1L;

	public GmMenu(String s)
		{
		super();
		setTextAndAlt(this,s);
		if (LGM.themename.equals("Quantum"))
			{
			this.setFont(LGM.lnfFont.deriveFont(Font.ROMAN_BASELINE));
			}
		}

	public GmMenu()
		{
		// TODO Auto-generated constructor stub
		}

	public JMenuItem addItem(String key)
		{
		return addItem(key,-1,-1,Listener.getInstance());
		}

	public JMenuItem addItem(String key, ActionListener listener)
		{
		return addItem(key,-1,-1,listener);
		}

	public JMenuItem addItem(String key, int shortcut, int control)
		{
		return addItem(key,shortcut,control,Listener.getInstance());
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
		if (LGM.themename.equals("Quantum"))
			{
			item.setFont(LGM.lnfFont);
			}
		add(item);
		return item;
		}

	public GmMenu addMenu(String key)
		{
		return addMenu(key,-1,-1,Listener.getInstance());
		}

	public GmMenu addMenu(String key, ActionListener listener)
		{
		return addMenu(key,-1,-1,listener);
		}

	public GmMenu addMenu(String key, int shortcut, int control)
		{
		return addMenu(key,shortcut,control,Listener.getInstance());
		}

	public GmMenu addMenu(String key, int shortcut, int control, ActionListener listener)
		{
		GmMenu item = new GmMenu();
		if (key != null)
			{
			setTextAndAlt(item,Messages.getString(key));
			item.setIcon(LGM.getIconForKey(key));
			item.setActionCommand(key);
			}
		//if (shortcut >= 0) item.setAccelerator(KeyStroke.getKeyStroke(shortcut,control));
		item.addActionListener(listener);
		if (LGM.themename.equals("Quantum"))
			{
			item.setFont(LGM.lnfFont);
			}
		add(item);
		return item;
		}
	}
