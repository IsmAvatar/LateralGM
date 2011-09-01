/*
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.lateralgm.components.impl.EventNode;
import org.lateralgm.main.Prefs;
import org.lateralgm.messages.Messages;
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
	public EventFrame par;
	public int parMethod;
	public int parEvent;
	public GmObjectFrame parFrame;
	public EventNode parNode;
	public JMenu submenu;

	public EventKeySelector(EventFrame parent, int width)
		{
		super();
		GroupLayout l = new GroupLayout(this);
		setLayout(l);

		par = parent;
		parMethod = 0;
		text = new EventKeyInput(parent);
		button = new JButton();
		button.addMouseListener(new MListener());
		menu = new JPopupMenu();
		populateMenu(menu);

		l.setHorizontalGroup(l.createSequentialGroup()
		/**/.addContainerGap(20,20)
		/**/.addComponent(text,PREFERRED_SIZE,width - 40,Integer.MAX_VALUE)
		/**/.addComponent(button,PREFERRED_SIZE,20,PREFERRED_SIZE));
		l.setVerticalGroup(l.createParallelGroup()
		/**/.addComponent(text,PREFERRED_SIZE,20,PREFERRED_SIZE)
		/**/.addComponent(button,PREFERRED_SIZE,19,PREFERRED_SIZE));
		}

	public EventKeySelector(EventFrame parent, int event, MouseEvent e, GmObjectFrame f, EventNode n)
		{
		par = parent;
		parMethod = 1;
		parEvent = event;
		parFrame = f;
		parNode = n;
		JPopupMenu rightmenu = new JPopupMenu();
		populateMenu(rightmenu);
		if (!isEnabled()) return;
		rightmenu.show(e.getComponent(),e.getX(),e.getY());
		}

	public void populateMenu(JPopupMenu menu)
		{
		add(menu,KeyEvent.VK_LEFT);
		add(menu,KeyEvent.VK_RIGHT);
		add(menu,KeyEvent.VK_UP);
		add(menu,KeyEvent.VK_DOWN);
		menu.addSeparator();
		add(menu,KeyEvent.VK_CONTROL);
		add(menu,KeyEvent.VK_ALT);
		add(menu,KeyEvent.VK_SHIFT);
		add(menu,KeyEvent.VK_SPACE);
		add(menu,KeyEvent.VK_ENTER);
		menu.addSeparator();
		submenu = new JMenu(Messages.getString("EventKeySelector.KEYPAD")); //$NON-NLS-1$
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; i++)
			addSub(submenu,i);
		submenu.addSeparator();
		addSub(submenu,KeyEvent.VK_DIVIDE);
		addSub(submenu,KeyEvent.VK_MULTIPLY);
		addSub(submenu,KeyEvent.VK_MINUS);
		addSub(submenu,KeyEvent.VK_ADD);
		addSub(submenu,KeyEvent.VK_DECIMAL);
		menu.add(submenu);
		submenu = new JMenu(Messages.getString("EventKeySelector.DIGITS")); //$NON-NLS-1$
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++)
			addSub(submenu,i);
		menu.add(submenu);
		submenu = new JMenu(Messages.getString("EventKeySelector.LETTERS")); //$NON-NLS-1$
		for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++)
			addSub(submenu,i);
		menu.add(submenu);
		submenu = new JMenu(Messages.getString("EventKeySelector.FUNCTION_KEYS")); //$NON-NLS-1$
		for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++)
			addSub(submenu,i);
		menu.add(submenu);
		submenu = new JMenu(Messages.getString("EventKeySelector.OTHERS")); //$NON-NLS-1$
		addSub(submenu,KeyEvent.VK_BACK_SPACE);
		addSub(submenu,KeyEvent.VK_ESCAPE);
		addSub(submenu,KeyEvent.VK_HOME);
		addSub(submenu,KeyEvent.VK_END);
		addSub(submenu,KeyEvent.VK_PAGE_UP);
		addSub(submenu,KeyEvent.VK_PAGE_DOWN);
		addSub(submenu,KeyEvent.VK_DELETE);
		addSub(submenu,KeyEvent.VK_INSERT);
		menu.add(submenu);
		menu.addSeparator();
		add(menu,0);
		add(menu,1);
		}

	public void add(JPopupMenu menu, int key)
		{
		JMenuItem mi = new KeyMenuItem(Event.getGmKeyCode(key));
		menu.add(mi);
		mi.addActionListener(this);
		}

	public void addSub(JMenu menu, int key)
		{
		JMenuItem mi = new KeyMenuItem(Event.getGmKeyCode(key));
		menu.add(mi);
		mi.addActionListener(this);
		}

	public void setSelectedGmKey(int key)
		{
		text.setSelectedGmKey(key);
		}

	public void actionPerformed(ActionEvent e)
		{
		Object o = e.getSource();
		if (!(o instanceof KeyMenuItem)) return;
		switch (parMethod)
			{
			case 0:
				setSelectedGmKey(((KeyMenuItem) o).gmKey);
				break;
			case 1:
				if (parFrame != null)
					{
					parFrame.functionEvent(parEvent,((KeyMenuItem) o).gmKey,parNode.other,null);
					parFrame.toTop();
					}
				break;
			}
		}

	public static class EventKeyInput extends JTextField
		{
		private static final long serialVersionUID = 1L;
		public EventFrame parent;
		private int selectedKey = -1;

		public EventKeyInput(EventFrame parent)
			{
			this.parent = parent;
			setFocusTraversalKeysEnabled(false);
			setDragEnabled(false);
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
