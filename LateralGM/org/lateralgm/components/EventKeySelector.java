/*
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.EventFrame;
import org.lateralgm.subframes.GmObjectFrame;

public class EventKeySelector extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public EventKeyInput text;
	public JButton button;
	public JPopupMenu menu;

	public EventKeySelector(EventFrame parent, int width)
		{
		super();
		GroupLayout l = new GroupLayout(this);
		setLayout(l);

		text = new EventKeyInput(parent);
		button = new JButton();
		button.addMouseListener(new MListener());
		menu = new JPopupMenu();
		populateMenu();

		l.setHorizontalGroup(l.createSequentialGroup()
		/**/.addComponent(text,PREFERRED_SIZE,width - 20,Integer.MAX_VALUE)
		/**/.addComponent(button,PREFERRED_SIZE,20,PREFERRED_SIZE));
		l.setVerticalGroup(l.createParallelGroup()
		/**/.addComponent(text,PREFERRED_SIZE,20,PREFERRED_SIZE)
		/**/.addComponent(button,PREFERRED_SIZE,19,PREFERRED_SIZE));
		}

	protected void populateMenu()
		{
		add(KeyEvent.VK_LEFT);
		add(KeyEvent.VK_RIGHT);
		add(KeyEvent.VK_UP);
		add(KeyEvent.VK_DOWN);
		menu.addSeparator();
		add(KeyEvent.VK_CONTROL);
		add(KeyEvent.VK_ALT);
		add(KeyEvent.VK_SHIFT);
		add(KeyEvent.VK_SPACE);
		add(KeyEvent.VK_ENTER);
		menu.addSeparator();
		//TODO: Add key groups (Keypad, Digits, Letters, Function Keys, Other)
		menu.addSeparator();
		add(0);
		add(1);
		}

	private void add(int key)
		{
		JMenuItem mi = new KeyMenuItem(Event.getGmKeyCode(key));
		menu.add(mi);
		mi.addActionListener(EventKeySelector.this);
		}

	public int getSelectedKey()
		{
		return text.getSelectedKey();
		}

	public void setSelectedGmKey(int key)
		{
		text.setSelectedGmKey(key);
		}

	public void actionPerformed(ActionEvent e)
		{
		Object o = e.getSource();
		if (!(o instanceof KeyMenuItem)) return;
		setSelectedGmKey(((KeyMenuItem) o).gmKey);
		}

	public static class EventKeyInput extends JTextField
		{
		private static final long serialVersionUID = 1L;
		public EventFrame parent;
		private int selectedKey;

		public EventKeyInput(EventFrame parent)
			{
			this.parent = parent;
			setFocusTraversalKeysEnabled(false);
			setDragEnabled(false);
			}

		public int getSelectedKey()
			{
			return selectedKey;
			}

		public void setSelectedGmKey(int key)
			{
			selectedKey = key;
			setText(Event.getGmKeyName(key));
			if (parent.selectedNode != null) switch (parent.selectedNode.mainId)
				{
				case MainEvent.EV_KEYBOARD:
				case MainEvent.EV_KEYPRESS:
				case MainEvent.EV_KEYRELEASE:
					parent.selectedNode.eventId = selectedKey;
					break;
				default:
				}
			}

		public void processKeyEvent(KeyEvent e)
			{
			if (e.getID() == KeyEvent.KEY_PRESSED)
				{
				int key = Event.getGmKeyCode(e.getKeyCode());
				if (Event.KEYS.contains(key))
					setSelectedGmKey(key);
				else if (key == Prefs.eventKeyInputAddKey)
					{
					GmObjectFrame f = parent.linkedFrame == null ? null : parent.linkedFrame.get();
					if (f != null && Event.KEYS.contains(selectedKey))
						{
						f.addEvent(new Event(parent.selectedNode.mainId,selectedKey));
						}
					}
				}
			}
		}

	private class MListener extends MouseAdapter
		{
		public void mouseClicked(MouseEvent e)
			{
			if (!isEnabled()) return;
			menu.show(e.getComponent(),e.getX(),e.getY());
			}
		}

	public static class KeyMenuItem extends JMenuItem
		{
		private static final long serialVersionUID = 1L;
		public final int gmKey;

		public KeyMenuItem(int gmKey)
			{
			super(Event.getGmKeyName(gmKey));
			this.gmKey = gmKey;
			}
		}
	}
