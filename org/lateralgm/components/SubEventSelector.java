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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.lateralgm.components.impl.EventNode;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.messages.Messages;
import org.lateralgm.subframes.EventFrame;
import org.lateralgm.subframes.GmObjectFrame;

public class SubEventSelector extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public JButton button;
	public JPopupMenu menu;
	public JLabel label;
	public MListener mListener = new MListener();
	public EventFrame par;
	public int parMethod;
	public int parEvent;
	public GmObjectFrame parFrame;
	public EventNode parNode;
	public JMenu submenu;

	public SubEventSelector(EventFrame parent, int event, String def, int width)
		{
		super();
		GroupLayout l = new GroupLayout(this);
		setLayout(l);

		par = parent;
		parMethod = 0;
		parEvent = event;
		label = new JLabel(def);
		label.setBorder(BorderFactory.createEtchedBorder());
		label.addMouseListener(mListener);
		button = new JButton();
		button.addMouseListener(mListener);
		button.setMaximumSize(button.getPreferredSize());
		menu = new JPopupMenu();
		populateSubEventMenu(menu,event);

		l.setHorizontalGroup(l.createSequentialGroup()
		/**/.addContainerGap(20,20)
		/**/.addComponent(label,PREFERRED_SIZE,width - 40,Integer.MAX_VALUE)
		/**/.addComponent(button,PREFERRED_SIZE,20,PREFERRED_SIZE));
		l.setVerticalGroup(l.createParallelGroup()
		/**/.addComponent(label,PREFERRED_SIZE,20,PREFERRED_SIZE)
		/**/.addComponent(button,PREFERRED_SIZE,19,PREFERRED_SIZE));
		}

	public SubEventSelector(EventFrame parent, int event, MouseEvent e, GmObjectFrame f, EventNode n)
		{
		par = parent;
		parMethod = 1;
		parEvent = event;
		parFrame = f;
		parNode = n;
		JPopupMenu rightmenu = new JPopupMenu();
		populateSubEventMenu(rightmenu,event);
		if (!isEnabled()) return;
		rightmenu.show(e.getComponent(),e.getX(),e.getY());
		}

	public void populateSubEventMenu(JPopupMenu menu, int event)
		{
		switch (event)
			{
			case MainEvent.EV_STEP:
				for (int i = Event.EV_STEP_NORMAL; i <= Event.EV_STEP_END; i++)
					add(menu,i);
				break;
			case MainEvent.EV_ALARM:
				for (int i = Event.EV_ALARM0; i <= Event.EV_ALARM11; i++)
					add(menu,i);
				break;
			case MainEvent.EV_MOUSE:
				for (int i = Event.EV_LEFT_BUTTON; i <= Event.EV_MOUSE_LEAVE; i++)
					add(menu,i);
				for (int i = Event.EV_MOUSE_WHEEL_UP; i <= Event.EV_MOUSE_WHEEL_DOWN; i++)
					add(menu,i);
				submenu = new JMenu(Messages.getString("SubEventSelector.GLOBAL_MOUSE")); //$NON-NLS-1$
				for (int i = Event.EV_GLOBAL_LEFT_BUTTON; i <= Event.EV_GLOBAL_MIDDLE_RELEASE; i++)
					addSub(submenu,i);
				menu.add(submenu);
				submenu = new JMenu(Messages.getString("SubEventSelector.JOYSTICK_1")); //$NON-NLS-1$
				for (int i = Event.EV_JOYSTICK1_LEFT; i <= Event.EV_JOYSTICK1_BUTTON8; i++)
					addSub(submenu,i);
				menu.add(submenu);
				submenu = new JMenu(Messages.getString("SubEventSelector.JOYSTICK_2")); //$NON-NLS-1$
				for (int i = Event.EV_JOYSTICK2_LEFT; i <= Event.EV_JOYSTICK2_BUTTON8; i++)
					addSub(submenu,i);
				menu.add(submenu);
				break;
			case MainEvent.EV_OTHER:
				for (int i = Event.EV_OUTSIDE; i <= Event.EV_NO_MORE_HEALTH; i++)
					add(menu,i);
				submenu = new JMenu(Messages.getString("SubEventSelector.USER_DEFINED")); //$NON-NLS-1$
				for (int i = Event.EV_USER0; i <= Event.EV_USER15; i++)
					addSub(submenu,i);
				menu.add(submenu);
				break;
			}
		}

	public void add(JPopupMenu menu, int event)
		{
		JMenuItem mi = new SubEventMenuItem(event);
		menu.add(mi);
		mi.addActionListener(this);
		}

	public void addSub(JMenu menu, int event)
		{
		JMenuItem mi = new SubEventMenuItem(event);
		menu.add(mi);
		mi.addActionListener(this);
		}

	public void actionPerformed(ActionEvent e)
		{
		Object o = e.getSource();
		if (!(o instanceof SubEventMenuItem)) return;
		switch (parMethod)
			{
			case 0:
				if (par.selectedNode != null)
					{
					par.selectedNode.eventId = ((SubEventMenuItem) o).event;
					label.setText(e.getActionCommand());
					}
				break;
			case 1:
				if (parFrame != null)
					{
					parFrame.functionEvent(parEvent,((SubEventMenuItem) o).event,parNode.other,null);
					parFrame.toTop();
					}
				break;
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

	public class SubEventMenuItem extends JMenuItem
		{
		private static final long serialVersionUID = 1L;
		public final int event;

		public SubEventMenuItem(int event)
			{
			super(Event.eventName(parEvent,event));
			this.event = event;
			}
		}
	}
