/*
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2019 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static org.lateralgm.main.Util.deRef;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIPane;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class EventPanel extends JPanel implements ActionListener,PropertyChangeListener,UpdateListener
	{
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = 4801776050696461727L;

	public static final int FUNCTION_ADD = 0;
	public static final int FUNCTION_REPLACE = 1;
	public static final int FUNCTION_DUPLICATE = 2;

	public IndexButtonGroup function;
	public ResourceMenu<GmObject> linkSelect;
	public WeakReference<GmObjectFrame> linkedFrame;
	public JCheckBox stayOpen;
	private EventAction collisionBt;
	private JMenu collisionMenu;

	private class EventAction extends AbstractAction
		{
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = -3612891952734251981L;

		private int mid = 0, sid = 0;
		private ResourceReference<GmObject> other;

		private JMenu subevtMenu = null;

		public EventAction(int id, int sid, ResourceReference<GmObject> other)
			{
			super();
			this.mid = id;
			this.sid = sid;
			this.other = other;
			this.putValue(Action.SMALL_ICON,LGM.getIconForKey("EventNode.EVENT" + mid)); //$NON-NLS-1$
			this.putValue(Action.LARGE_ICON_KEY,LGM.getIconForKey("EventNode.EVENT" + mid + "_32px")); //$NON-NLS-1$ //$NON-NLS-2$
			this.putValue(Action.NAME,(other == null)?Event.eventName(mid,sid) : other.get().getName());
			}

		public EventAction(int id)
			{
			this(id, 0, null);
			this.putValue(Action.SHORT_DESCRIPTION,Messages.getString("MainEvent.EVENT" + mid)); //$NON-NLS-1$
			}

		public EventAction(int id, int sid)
			{
			this(id, sid, null);
			}

		public EventAction(ResourceReference<GmObject> other)
			{
			this(MainEvent.EV_COLLISION, 0, other);
			}

		public void setEventMenu(JMenu subevtMenu)
			{
			this.subevtMenu = subevtMenu;
			}

		@Override
		public void actionPerformed(ActionEvent e)
			{
			if (subevtMenu != null)
				{
				if (!(e.getSource() instanceof JComponent)) return;
				JComponent src = (JComponent)e.getSource();
				JPopupMenu popup = subevtMenu.getPopupMenu();
				popup.show(src,src.getWidth(),0);
				}
			else
				{
				GmObjectFrame f = linkedFrame == null ? null : linkedFrame.get();
				if (f != null)
					{
					f.functionEvent(function.getValue(),mid,sid,other,null);
					f.toTop();
					boolean ctrlDown = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
					if (!stayOpen.isSelected() ^ ctrlDown) LGM.hideEventPanel();
					}
				}
			}
		}

	public EventPanel()
		{
		super();
		GroupLayout layout = new GroupLayout(this);

		JPanel settingsLabelPane = Util.makeLabelPane(Messages.getString("EventPanel.SETTINGS")); //$NON-NLS-1$
		function = new IndexButtonGroup(3,true,false);
		JRadioButton ra = new JRadioButton(Messages.getString("EventPanel.ADD")); //$NON-NLS-1$
		JRadioButton rr = new JRadioButton(Messages.getString("EventPanel.REPLACE")); //$NON-NLS-1$
		JRadioButton rd = new JRadioButton(Messages.getString("EventPanel.DUPLICATE")); //$NON-NLS-1$
		function.add(ra);
		function.add(rr);
		function.add(rd);
		function.setValue(FUNCTION_ADD);

		JLabel contextLabel = new JLabel(Messages.getString("EventPanel.CONTEXT")); //$NON-NLS-1$
		linkSelect = new ResourceMenu<GmObject>(GmObject.class,
				Messages.getString("EventPanel.NO_LINK"),false,120,true,true); //$NON-NLS-1$
		linkSelect.addActionListener(this);

		stayOpen = new JCheckBox(Messages.getString("EventPanel.STAY_OPEN")); //$NON-NLS-1$

		JPanel basicLabelPane = Util.makeLabelPane(Messages.getString("EventPanel.BASIC")); //$NON-NLS-1$
		JToolBar basicTB = makeBasicToolBar();

		JPanel specialLabelPane = Util.makeLabelPane(Messages.getString("EventPanel.SPECIAL")); //$NON-NLS-1$
		JToolBar specialTB = makeSpecialToolBar();

		JPanel inputLabelPane = Util.makeLabelPane(Messages.getString("EventPanel.INPUT")); //$NON-NLS-1$
		JToolBar inputTB = makeInputToolBar();

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(4)
		/**/.addComponent(basicLabelPane)
		/**/.addComponent(basicTB)
		/**/.addComponent(specialLabelPane)
		/**/.addComponent(specialTB)
		/**/.addComponent(inputLabelPane)
		/**/.addComponent(inputTB)
		/**/.addComponent(settingsLabelPane)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(ra).addComponent(rr).addComponent(rd))
		/**/.addComponent(stayOpen)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(contextLabel).addComponent(linkSelect)).addGap(4));

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(4)
		/**/.addGroup(layout.createParallelGroup()
		/**/.addComponent(basicLabelPane)
		/**/.addComponent(basicTB)
		/**/.addComponent(specialLabelPane)
		/**/.addComponent(specialTB)
		/**/.addComponent(inputLabelPane)
		/**/.addComponent(inputTB)
		/**/.addComponent(settingsLabelPane)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(ra).addComponent(rr).addComponent(rd))
		/**/.addComponent(stayOpen)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(contextLabel).addComponent(linkSelect))).addGap(4));

		// set the layout after you actually create the layout, otherwise it won't work for certain look and feels
		setLayout(layout);
		reload();
		}

	private JToolBar makeBasicToolBar()
		{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);

		//CREATE
		EventAction createBt = new EventAction(MainEvent.EV_CREATE);

		//STEP
		EventAction stepBt = new EventAction(MainEvent.EV_STEP);
		JMenu stepMenu = new JMenu();
		for (int i = Event.EV_STEP_NORMAL; i <= Event.EV_STEP_END; i++)
			stepMenu.add(new EventAction(MainEvent.EV_STEP,i));
		stepBt.setEventMenu(stepMenu);

		//DRAW
		EventAction drawBt = new EventAction(MainEvent.EV_DRAW);
		JMenu drawMenu = new JMenu();
		drawMenu.add(new EventAction(MainEvent.EV_DRAW,Event.EV_DRAW_NORMAL));
		drawMenu.add(new EventAction(MainEvent.EV_DRAW,Event.EV_DRAW_GUI));
		drawMenu.addSeparator();

		for (int i = Event.EV_DRAW_BEGIN; i <= Event.EV_DRAW_POST; i++)
			drawMenu.add(new EventAction(MainEvent.EV_DRAW,i));
		drawMenu.insertSeparator(5); // after "Draw Begin"/"Draw End"
		drawMenu.insertSeparator(8); // after "Draw GUI Begin"/"Draw GUI End"
		drawMenu.addSeparator(); // after "Pre Draw"/"Post Draw"

		drawMenu.add(new EventAction(MainEvent.EV_DRAW,Event.EV_DRAW_RESIZE));
		drawBt.setEventMenu(drawMenu);

		//DESTROY
		EventAction destroyBt = new EventAction(MainEvent.EV_DESTROY);

		tb.add(createBt);
		tb.add(stepBt);
		tb.add(drawBt);
		tb.add(destroyBt);
		return tb;
		}

	private JToolBar makeSpecialToolBar()
		{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);

		//COLLISION
		collisionBt = new EventAction(MainEvent.EV_COLLISION);
		collisionMenu = new JMenu();
		collisionBt.setEventMenu(collisionMenu);

		//ALARM
		EventAction alarmBt = new EventAction(MainEvent.EV_ALARM);
		JMenu alarmMenu = new JMenu();
		for (int i = 0; i <= 11; i++)
			alarmMenu.add(new EventAction(MainEvent.EV_ALARM,i));
		alarmBt.setEventMenu(alarmMenu);

		//OTHER
		ImageIcon otherGroupIcon = LGM.getIconForKey("EventNode.GROUP" + MainEvent.EV_OTHER); //$NON-NLS-1$
		EventAction otherBt = new EventAction(MainEvent.EV_OTHER);
		JMenu otherMenu = new JMenu();
		for (int i = Event.EV_OUTSIDE; i <= Event.EV_ROOM_END; i++)
			otherMenu.add(new EventAction(MainEvent.EV_OTHER,i));
		otherMenu.insertSeparator(2); // after "Outside Room"/"Intersect Boundary"

		JMenu viewsMenu = new JMenu(Messages.getString("EventPanel.VIEWS")); //$NON-NLS-1$
		viewsMenu.setIcon(otherGroupIcon);
		for (int i = 0; i <= 7; i++)
			viewsMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_OUTSIDEVIEW0 + i));
		viewsMenu.addSeparator();
		for (int i = 0; i <= 7; i++)
			viewsMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_BOUNDARYVIEW0 + i));
		otherMenu.insert(viewsMenu,3);

		otherMenu.insertSeparator(4); // after "Views" menu
		otherMenu.insertSeparator(7); // after "Game Start"/"Game End"
		otherMenu.insertSeparator(10); // after "Room Start"/"Room End"
		otherMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_NO_MORE_LIVES));
		otherMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_NO_MORE_HEALTH));
		otherMenu.addSeparator();
		otherMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_ANIMATION_END));
		otherMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_END_OF_PATH));
		otherMenu.addSeparator();
		otherMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_CLOSEWINDOW));
		otherMenu.addSeparator();

		JMenu userMenu = new JMenu(Messages.getString("EventPanel.USER_DEFINED")); //$NON-NLS-1$
		userMenu.setIcon(otherGroupIcon);
		for (int i = 0; i <= 15; i++)
			userMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_USER0 + i));
		otherMenu.add(userMenu);
		otherMenu.addSeparator();

		JMenu asynchronousMenu = new JMenu(Messages.getString("EventPanel.ASYNCHRONOUS")); //$NON-NLS-1$
		asynchronousMenu.setIcon(otherGroupIcon);
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_IMAGELOADED));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_SOUNDLOADED));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_HTTP));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_DIALOG));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_IAP));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_CLOUD));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_NETWORKING));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_STEAM));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_SOCIAL));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_PUSHNOTIFICATION));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_SAVELOAD));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_AUDIORECORDING));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_AUDIOPLAYBACK));
		asynchronousMenu.add(new EventAction(MainEvent.EV_OTHER,Event.EV_SYSTEM));
		otherMenu.add(asynchronousMenu);

		otherBt.setEventMenu(otherMenu);

		tb.add(collisionBt);
		tb.add(alarmBt);
		tb.add(otherBt);
		return tb;
		}

	private EventAction makeKeyboardAction(int mid)
		{
		EventAction keyBt = new EventAction(mid);
		JMenu keyMenu = new JMenu();
		ImageIcon keyGroupIcon = LGM.getIconForKey("EventNode.GROUP" + mid); //$NON-NLS-1$

		keyMenu.add(new EventAction(mid,Event.EV_NO_KEY));
		keyMenu.add(new EventAction(mid,Event.EV_ANY_KEY));
		keyMenu.addSeparator();
		keyMenu.add(new EventAction(mid,KeyEvent.VK_LEFT));
		keyMenu.add(new EventAction(mid,KeyEvent.VK_RIGHT));
		keyMenu.add(new EventAction(mid,KeyEvent.VK_UP));
		keyMenu.add(new EventAction(mid,KeyEvent.VK_DOWN));
		keyMenu.addSeparator();
		keyMenu.add(new EventAction(mid,KeyEvent.VK_CONTROL));
		keyMenu.add(new EventAction(mid,KeyEvent.VK_ALT));
		keyMenu.add(new EventAction(mid,KeyEvent.VK_SHIFT));
		keyMenu.add(new EventAction(mid,KeyEvent.VK_SPACE));
		keyMenu.add(new EventAction(mid,Event.EV_ENTER_KEY));
		keyMenu.addSeparator();

		JMenu keypadMenu = new JMenu(Messages.getString("EventPanel.KEYPAD")); //$NON-NLS-1$
		keypadMenu.setIcon(keyGroupIcon);
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; i++)
			keypadMenu.add(new EventAction(mid,i));

		keypadMenu.add(new EventAction(mid,KeyEvent.VK_DIVIDE));
		keypadMenu.add(new EventAction(mid,KeyEvent.VK_MULTIPLY));
		keypadMenu.add(new EventAction(mid,KeyEvent.VK_SUBTRACT));
		keypadMenu.add(new EventAction(mid,KeyEvent.VK_ADD));
		keypadMenu.add(new EventAction(mid,KeyEvent.VK_DECIMAL));
		keyMenu.add(keypadMenu);

		JMenu digitsMenu = new JMenu(Messages.getString("EventPanel.DIGITS")); //$NON-NLS-1$
		digitsMenu.setIcon(keyGroupIcon);
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++)
			digitsMenu.add(new EventAction(mid,i));
		keyMenu.add(digitsMenu);

		JMenu lettersMenu = new JMenu(Messages.getString("EventPanel.LETTERS")); //$NON-NLS-1$
		lettersMenu.setIcon(keyGroupIcon);
		for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++)
			lettersMenu.add(new EventAction(mid,i));
		keyMenu.add(lettersMenu);

		JMenu funcMenu = new JMenu(Messages.getString("EventPanel.FUNCTION_KEYS")); //$NON-NLS-1$
		funcMenu.setIcon(keyGroupIcon);
		for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++)
			funcMenu.add(new EventAction(mid,i));
		keyMenu.add(funcMenu);

		JMenu othersMenu = new JMenu(Messages.getString("EventPanel.OTHERS")); //$NON-NLS-1$
		othersMenu.setIcon(keyGroupIcon);
		othersMenu.add(new EventAction(mid,KeyEvent.VK_BACK_SPACE));
		othersMenu.add(new EventAction(mid,KeyEvent.VK_ESCAPE));
		othersMenu.add(new EventAction(mid,KeyEvent.VK_HOME));
		othersMenu.add(new EventAction(mid,KeyEvent.VK_END));
		othersMenu.add(new EventAction(mid,KeyEvent.VK_PAGE_UP));
		othersMenu.add(new EventAction(mid,KeyEvent.VK_PAGE_DOWN));
		othersMenu.add(new EventAction(mid,Event.EV_DELETE_KEY));
		othersMenu.add(new EventAction(mid,Event.EV_INSERT_KEY));
		keyMenu.add(othersMenu);

		keyBt.setEventMenu(keyMenu);
		return keyBt;
		}

	private JToolBar makeInputToolBar()
		{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);

		//KEYBOARD
		EventAction keyBt = makeKeyboardAction(MainEvent.EV_KEYBOARD);

		//KEYPRESS
		EventAction keyPressBt = makeKeyboardAction(MainEvent.EV_KEYPRESS);

		//KEYRELEASE
		EventAction keyUpBt = makeKeyboardAction(MainEvent.EV_KEYRELEASE);

		//MOUSE
		EventAction mouseBt = new EventAction(MainEvent.EV_MOUSE);
		JMenu mouseMenu = new JMenu();
		ImageIcon mouseGroupIcon = LGM.getIconForKey("EventNode.GROUP" + MainEvent.EV_MOUSE); //$NON-NLS-1$
		for (int i = Event.EV_LEFT_BUTTON; i <= Event.EV_MOUSE_LEAVE; i++)
			mouseMenu.add(new EventAction(MainEvent.EV_MOUSE,i));
		mouseMenu.insertSeparator(3); // after "Left Button"/"Right Button"/"Middle Button"
		mouseMenu.insertSeparator(5); // after "No Button"
		mouseMenu.insertSeparator(9); // after "Left Pressed"/"Right Pressed"/"Middle Pressed"
		mouseMenu.insertSeparator(13); // after "Left Released"/"Right Released"/"Middle Released"
		mouseMenu.addSeparator(); // after "Mouse Enter"/"Mouse Leave"
		for (int i = Event.EV_MOUSE_WHEEL_UP; i <= Event.EV_MOUSE_WHEEL_DOWN; i++)
			mouseMenu.add(new EventAction(MainEvent.EV_MOUSE,i));
		mouseMenu.addSeparator();

		JMenu globalMenu = new JMenu(Messages.getString("EventPanel.GLOBAL_MOUSE")); //$NON-NLS-1$
		globalMenu.setIcon(mouseGroupIcon);
		for (int i = Event.EV_GLOBAL_LEFT_BUTTON; i <= Event.EV_GLOBAL_MIDDLE_RELEASE; i++)
			globalMenu.add(new EventAction(MainEvent.EV_MOUSE,i));
		globalMenu.insertSeparator(3); // after "Global Left Button"/"Global Right Button"/"Global Middle Button"
		globalMenu.insertSeparator(7); // after "Global Left Pressed"/"Global Right Pressed"/"Global Middle Pressed"
		mouseMenu.add(globalMenu);
		mouseMenu.addSeparator();

		JMenu joy1Menu = new JMenu(Messages.getString("EventPanel.JOYSTICK_1")); //$NON-NLS-1$
		joy1Menu.setIcon(mouseGroupIcon);
		for (int i = Event.EV_JOYSTICK1_LEFT; i <= Event.EV_JOYSTICK1_BUTTON8; i++)
			if (i != 20) joy1Menu.add(new EventAction(MainEvent.EV_MOUSE,i));
		joy1Menu.insertSeparator(4); // after Joystick 1 "Left"/"Right"/"Up"/"Down"
		mouseMenu.add(joy1Menu);

		JMenu joy2Menu = new JMenu(Messages.getString("EventPanel.JOYSTICK_2")); //$NON-NLS-1$
		joy2Menu.setIcon(mouseGroupIcon);
		for (int i = Event.EV_JOYSTICK2_LEFT; i <= Event.EV_JOYSTICK2_BUTTON8; i++)
			if (i != 35) joy2Menu.add(new EventAction(MainEvent.EV_MOUSE,i));
		joy2Menu.insertSeparator(4); // after Joystick 2 "Left"/"Right"/"Up"/"Down"
		mouseMenu.add(joy2Menu);

		mouseBt.setEventMenu(mouseMenu);

		tb.add(keyBt);
		tb.add(keyPressBt);
		tb.add(keyUpBt);
		tb.add(mouseBt);
		return tb;
		}

	public void populate_collision_menu()
		{
		collisionMenu.removeAll();
		collisionBt.setEnabled(false);
		if (Prefs.groupKind)
			{
			for (int i = 0; i < LGM.root.getChildCount(); i++)
				{
				ResNode group = (ResNode) LGM.root.getChildAt(i);
				if (group.kind != GmObject.class) continue;
				populate_object_nodes(collisionMenu,group);
				}
			}
		else
			populate_object_nodes(collisionMenu,LGM.root);
		}

	@SuppressWarnings("unchecked")
	protected void populate_object_nodes(JMenu parent, ResNode group)
		{
		for (int i = 0; i < group.getChildCount(); i++)
			{
			ResNode child = (ResNode) group.getChildAt(i);
			if (child.kind != GmObject.class) continue;
			if (child.status == ResNode.STATUS_SECONDARY)
				{
				parent.add(new EventAction((ResourceReference<GmObject>) child.getRes()));
				collisionBt.setEnabled(true);
				}
			else if (child.status == ResNode.STATUS_GROUP)
				{
				JMenu groupMenu = new JMenu(child.getUserObject().toString());
				ImageIcon groupIcon = LGM.getIconForKey("EventNode.GROUP" + MainEvent.EV_COLLISION); //$NON-NLS-1$
				groupMenu.setIcon(groupIcon);
				if (child.getChildCount() > 0) populate_object_nodes(groupMenu,child);
				else groupMenu.add(new JMenuItem(Messages.getString("EventPanel.EMPTY_GROUP"))); //$NON-NLS-1$
				parent.add(groupMenu);
				}
			}
		}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == linkSelect)
			{
			GmObject obj = deRef(((ResourceMenu<GmObject>) e.getSource()).getSelected());
			if (obj != null)
				{
				ResNode node = obj.getNode();
				GmObjectFrame f = (GmObjectFrame) node.frame;
				linkedFrame = new WeakReference<GmObjectFrame>(f);
				f.toTop();
				}
			else
				linkedFrame = null;
			return;
			}
		}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt)
		{
		if (evt.getPropertyName().equals(MDIPane.SELECTED_FRAME_PROPERTY))
			{
			JInternalFrame newFrame = (JInternalFrame) evt.getNewValue();
			JInternalFrame oldFrame = (JInternalFrame) evt.getOldValue();
			if (newFrame instanceof GmObjectFrame)
				{
				GmObjectFrame f = (GmObjectFrame) newFrame;
				linkedFrame = new WeakReference<GmObjectFrame>(f);
				linkSelect.setSelected((ResourceReference<GmObject>) f.node.getRes());
				}
			else
				{
				if (newFrame == null && !oldFrame.isVisible()) linkSelect.setSelected(null);
				}
			}
		}

	@Override
	public void updated(UpdateEvent e)
		{
		populate_collision_menu();
		}

	public void reload()
		{
		LGM.mdi.addPropertyChangeListener(MDIPane.SELECTED_FRAME_PROPERTY,this);
		LGM.root.updateSource.addListener(this);
		populate_collision_menu();
		}
	}
