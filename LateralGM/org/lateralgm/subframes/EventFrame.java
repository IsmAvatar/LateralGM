/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static org.lateralgm.subframes.ResourceFrame.addDim;
import static org.lateralgm.subframes.ResourceFrame.addGap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lateralgm.components.EventKeyInput;
import org.lateralgm.components.EventNode;
import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class EventFrame extends JInternalFrame implements MouseListener,ActionListener,
		ContainerListener,InternalFrameListener,DocumentListener
	{
	private static final long serialVersionUID = 1L;

	public EventKeyInput keySelect;
	public ResourceMenu collisionSelect;
	public JTextField frameName;
	public JButton frameChoose;
	public GmObjectFrame linkedFrame;
	private JPopupMenu menu = new JPopupMenu();
	public EventNode root;
	public JTree events;
	public JCheckBox onTop;
	public JToggleButton toggle;

	public EventFrame(JToggleButton toggle)
		{
		super(Messages.getString("EventFrame.TITLE"),true,true,false,true); //$NON-NLS-1$

		this.toggle = toggle;
		setSize(290,310);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(290,260));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		JPanel side1 = new JPanel(new BorderLayout());

		makeTree(side1);

		add(side1);

		JPanel side2Parent = new JPanel();
		JPanel side2 = new JPanel(new FlowLayout());
		side2Parent.add(side2);
		side2.setPreferredSize(new Dimension(150,200));
		side2.setMaximumSize(new Dimension(150,200));
		side2.setMinimumSize(new Dimension(150,200));

		addDim(side2,new JLabel(Messages.getString("EventFrame.KEY_SELECTOR")),140,16); //$NON-NLS-1$
		keySelect = new EventKeyInput();
		addDim(keySelect,140,20);

		JLabel lab = new JLabel(Messages.getString("EventFrame.COLLISION_OBJECT"));
		addDim(side2,lab,140,16); //$NON-NLS-1$
		collisionSelect = new ResourceMenu(Resource.GMOBJECT,Messages
				.getString("EventFrame.CHOOSE_OBJECT"),true,140); //$NON-NLS-1$
		side2.add(collisionSelect);

		addDim(side2,new JLabel(Messages.getString("EventFrame.FRAME_LINK")),140,16); //$NON-NLS-1$
		frameName = new JTextField();
		frameName.setEditable(false);
		addDim(frameName,110,20);
		frameChoose = new JButton(Resource.ICON[Resource.GMOBJECT]);
		frameChoose.addMouseListener(this);
		addDim(frameChoose,20,20);
		addGap(side2,50,30);

		onTop = new JCheckBox(Messages.getString("EventFrame.ALWAYS_ON_TOP")); //$NON-NLS-1$
		addDim(side2,onTop,140,16);
		onTop.addActionListener(this);
		if (onTop.isSelected()) setLayer(JLayeredPane.MODAL_LAYER);

		JInternalFrame frames[] = LGM.mdi.getAllFrames();
		for (JInternalFrame frame : frames)
			if (frame instanceof GmObjectFrame)
				{
				GmObjectFrame f = (GmObjectFrame) frame;
				GmObjectFrameItem item = new GmObjectFrameItem(f);
				f.addInternalFrameListener(this);
				item.addActionListener(this);
				menu.add(item);
				}
		LGM.mdi.addContainerListener(this);

		add(side2Parent);
		}

	private void makeTree(JPanel side1)
		{
		root = new EventNode("Root",-1); //$NON-NLS-1$

		root.add(MainEvent.EV_CREATE);

		root.add(MainEvent.EV_DESTROY);

		EventNode alarm = new EventNode(Messages.getString("MainEvent.EVENT2"),-1); //$NON-NLS-1$
		root.add(alarm);
		for (int i = 0; i <= 11; i++)
			alarm.add(new EventNode(String.format(Messages.getString("Event.EVENT2_X"),i), //$NON-NLS-1$
					MainEvent.EV_ALARM,i));

		EventNode step = new EventNode(Messages.getString("MainEvent.EVENT3"),-1); //$NON-NLS-1$
		root.add(step);
		for (int i = Event.EV_STEP_NORMAL; i <= Event.EV_STEP_END; i++)
			step.add(MainEvent.EV_STEP,i);

		root.add(MainEvent.EV_COLLISION);

		root.add(MainEvent.EV_KEYBOARD);

		EventNode mouse = new EventNode(Messages.getString("MainEvent.EVENT6"),-1); //$NON-NLS-1$
		root.add(mouse);
		for (int i = Event.EV_LEFT_BUTTON; i <= Event.EV_MOUSE_LEAVE; i++)
			mouse.add(MainEvent.EV_MOUSE,i);
		mouse.add(MainEvent.EV_MOUSE,Event.EV_MOUSE_WHEEL_UP);
		mouse.add(MainEvent.EV_MOUSE,Event.EV_MOUSE_WHEEL_DOWN);

		EventNode global = new EventNode(Messages.getString("EventFrame.GLOBAL_MOUSE"),-1); //$NON-NLS-1$
		mouse.add(global);
		for (int i = Event.EV_GLOBAL_LEFT_BUTTON; i <= Event.EV_GLOBAL_MIDDLE_RELEASE; i++)
			global.add(MainEvent.EV_MOUSE,i);

		EventNode joy = new EventNode(Messages.getString("EventFrame.JOYSTICK_1"),-1); //$NON-NLS-1$
		mouse.add(joy);
		for (int i = Event.EV_JOYSTICK1_LEFT; i <= Event.EV_JOYSTICK1_BUTTON8; i++)
			if (i != 20) joy.add(MainEvent.EV_MOUSE,i);

		joy = new EventNode(Messages.getString("EventFrame.JOYSTICK_2"),-1); //$NON-NLS-1$
		mouse.add(joy);
		for (int i = Event.EV_JOYSTICK2_LEFT; i <= Event.EV_JOYSTICK2_BUTTON8; i++)
			if (i != 35) joy.add(MainEvent.EV_MOUSE,i);

		EventNode other = new EventNode(Messages.getString("MainEvent.EVENT7"),-1); //$NON-NLS-1$
		root.add(other);
		for (int i = 0; i <= 8; i++)
			other.add(MainEvent.EV_OTHER,i);

		EventNode user = new EventNode(Messages.getString("EventFrame.USER_DEFINED"),-1); //$NON-NLS-1$
		other.add(user);
		for (int i = 0; i <= 14; i++)
			user.add(new EventNode(String.format(Messages.getString("Event.EVENT7_X"),i), //$NON-NLS-1$
					MainEvent.EV_OTHER,Event.EV_USER0 + i));

		root.add(MainEvent.EV_DRAW);
		root.add(MainEvent.EV_KEYPRESS);
		root.add(MainEvent.EV_KEYRELEASE);

		events = new JTree(root);
		events.setRootVisible(false);
		events.setShowsRootHandles(true);
		JScrollPane scroll = new JScrollPane(events);
		scroll.setMinimumSize(new Dimension(130,260));
		side1.add(scroll,"Center"); //$NON-NLS-1$
		}

	private class GmObjectFrameItem extends JMenuItem implements DocumentListener
		{
		private static final long serialVersionUID = 1L;
		GmObjectFrame frame;

		GmObjectFrameItem(GmObjectFrame frame)
			{
			this.frame = frame;
			setIcon(GmTreeGraphics.getSpriteIcon(LGM.currentFile.sprites.get(frame.res.sprite)));
			setText(frame.name.getText());
			frame.name.getDocument().addDocumentListener(this);
			}

		public void changedUpdate(DocumentEvent e)
			{
			}

		public void insertUpdate(DocumentEvent e)
			{
			setText(frame.name.getText());
			}

		public void removeUpdate(DocumentEvent e)
			{
			setText(frame.name.getText());
			}
		}

	public void mouseClicked(MouseEvent e)
		{
		if (e.getSource() == frameChoose && menu.getComponentCount() > 0)
			{
			menu.show(e.getComponent(),e.getX(),e.getY());
			}
		}

	public void internalFrameActivated(InternalFrameEvent e)
		{
		if (e.getInternalFrame() instanceof GmObjectFrame && !e.getInternalFrame().isIcon())
			{
			if (linkedFrame != null) linkedFrame.name.getDocument().removeDocumentListener(this);
			linkedFrame = (GmObjectFrame) e.getInternalFrame();
			linkedFrame.name.getDocument().addDocumentListener(this);
			frameName.setText(linkedFrame.name.getText());
			}
		}

	public void internalFrameDeiconified(InternalFrameEvent e)
		{
		internalFrameActivated(e);
		}

	public void componentAdded(ContainerEvent e)
		{
		if (e.getChild() instanceof GmObjectFrame)
			{
			for (int i = 0; i < menu.getComponentCount(); i++)
				if (((GmObjectFrameItem) menu.getComponent(i)).frame == e.getChild()) return;
			GmObjectFrame frame = (GmObjectFrame) e.getChild();
			GmObjectFrameItem item = new GmObjectFrameItem(frame);
			item.addActionListener(this);
			frame.addInternalFrameListener(this);
			menu.add(item);
			}
		}

	public void internalFrameClosed(InternalFrameEvent e)
		{
		if (e.getInternalFrame() instanceof GmObjectFrame)
			{
			for (int i = 0; i < menu.getComponentCount(); i++)
				if (((GmObjectFrameItem) menu.getComponent(i)).frame == e.getInternalFrame())
					{
					if (linkedFrame == e.getInternalFrame())
						{
						linkedFrame.name.getDocument().removeDocumentListener(this);
						linkedFrame = null;
						frameName.setText(""); //$NON-NLS-1$
						}
					menu.remove(i);
					break;
					}
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() instanceof GmObjectFrameItem)
			{
			GmObjectFrameItem item = (GmObjectFrameItem) e.getSource();
			linkedFrame = item.frame;
			linkedFrame.toFront();
			frameName.setText(item.getText());
			return;
			}
		if (e.getSource() == onTop)
			{
			setLayer(onTop.isSelected() ? JLayeredPane.MODAL_LAYER : JLayeredPane.DEFAULT_LAYER);
			}

		}

	public void insertUpdate(DocumentEvent e)
		{
		frameName.setText(linkedFrame.name.getText());
		}

	public void removeUpdate(DocumentEvent e)
		{
		frameName.setText(linkedFrame.name.getText());
		}

	public void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_ICONIFIED)
			LGM.mdi.setLayer(getDesktopIcon(),0);
		else if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING) toggle.setSelected(false);
		super.fireInternalFrameEvent(id);
		}

	public void setVisible(boolean b)
		{
		super.setVisible(b);
		if (!b && toggle != null) fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSING);
		}

	//unused
	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mousePressed(MouseEvent e)
		{
		}

	public void mouseReleased(MouseEvent e)
		{
		}

	public void componentRemoved(ContainerEvent e)
		{
		}

	public void internalFrameClosing(InternalFrameEvent e)
		{
		}

	public void internalFrameDeactivated(InternalFrameEvent e)
		{
		}

	public void internalFrameIconified(InternalFrameEvent e)
		{
		}

	public void internalFrameOpened(InternalFrameEvent e)
		{
		}

	public void changedUpdate(DocumentEvent e)
		{
		}
	}
