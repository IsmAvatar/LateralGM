/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static org.lateralgm.main.Util.addDim;
import static org.lateralgm.subframes.ResourceFrame.addGap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.lateralgm.components.EventKeyInput;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.EventNode;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.components.mdi.MDIPane;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class EventFrame extends MDIFrame implements ActionListener,TreeSelectionListener,
		PropertyChangeListener
	{
	private static final long serialVersionUID = 1L;

	public JCheckBox replace;
	public EventKeyInput keySelect;
	public ResourceMenu<GmObject> collisionSelect;
	public ResourceMenu<GmObject> linkSelect;
	public GmObjectFrame linkedFrame;
	private MListener mListener = new MListener();
	public EventNode root;
	public JTree events;
	public JCheckBox onTop;
	public JToggleButton toggle;
	public EventNode selectedNode;

	public EventFrame(JToggleButton toggle)
		{
		super(Messages.getString("EventFrame.TITLE"),true,true,false,true); //$NON-NLS-1$

		this.toggle = toggle;
		setSize(300,310);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setFrameIcon(LGM.getIconForKey("LGM.TOGGLE_EVENT"));
		setMinimumSize(new Dimension(300,260));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		JPanel side1 = new JPanel(new BorderLayout());

		makeTree(side1);

		add(side1);

		JPanel side2Parent = new JPanel();
		JPanel side2 = new JPanel(new FlowLayout());
		side2Parent.add(side2);
		side2.setPreferredSize(new Dimension(150,250));
		side2.setMaximumSize(new Dimension(150,250));
		side2.setMinimumSize(new Dimension(150,250));

		replace = new JCheckBox("Replace Mode");
		addDim(side2,replace,120,16);

		addGap(side2,100,10);

		addDim(side2,new JLabel(Messages.getString("EventFrame.KEY_SELECTOR")),140,16); //$NON-NLS-1$
		keySelect = new EventKeyInput(this);
		addDim(side2,keySelect,140,20);
		keySelect.setEnabled(false);

		JLabel lab = new JLabel(Messages.getString("EventFrame.COLLISION_OBJECT"));
		addDim(side2,lab,140,16);
		collisionSelect = new ResourceMenu<GmObject>(Resource.GMOBJECT,
				Messages.getString("EventFrame.CHOOSE_OBJECT"),true,140); //$NON-NLS-1$
		side2.add(collisionSelect);
		collisionSelect.setEnabled(false);
		collisionSelect.addActionListener(this);

		addDim(side2,new JLabel(Messages.getString("EventFrame.FRAME_LINK")),140,16); //$NON-NLS-1$
		linkSelect = new ResourceMenu<GmObject>(Resource.GMOBJECT,"<no link>",true,140,true);
		linkSelect.addActionListener(this);
		side2.add(linkSelect);

		addGap(side2,50,15);

		onTop = new JCheckBox(Messages.getString("EventFrame.ALWAYS_ON_TOP")); //$NON-NLS-1$
		addDim(side2,onTop,120,16);
		onTop.addActionListener(this);
		if (onTop.isSelected()) setLayer(JLayeredPane.MODAL_LAYER);

		add(side2Parent);
		LGM.mdi.addPropertyChangeListener(MDIPane.SELECTED_FRAME_PROPERTY,this);
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

		String globMouseStr = Messages.getString("EventFrame.GLOBAL_MOUSE");
		EventNode global = new EventNode(globMouseStr,-1);
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
		events.setDragEnabled(true);
		events.setTransferHandler(new EventNodeTransferHandler());
		events.addTreeSelectionListener(this);
		events.setScrollsOnExpand(true);
		events.addMouseListener(mListener);
		JScrollPane scroll = new JScrollPane(events);
		scroll.setMinimumSize(new Dimension(120,260));
		side1.add(scroll,"Center"); //$NON-NLS-1$
		}

	private class EventNodeTransferHandler extends TransferHandler
		{
		private static final long serialVersionUID = 1L;

		protected Transferable createTransferable(JComponent c)
			{
			EventNode n = (EventNode) ((JTree) c).getLastSelectedPathComponent();
			if (n.eventId < 0 || n.mainId < 0) return null;
			return n;
			}

		public int getSourceActions(JComponent c)
			{
			return COPY;
			}

		public boolean canImport(TransferHandler.TransferSupport support)
			{
			return false;
			}
		}

	private class MListener extends MouseAdapter
		{
		public void mouseClicked(MouseEvent e)
			{
			if (e.getSource() == events && e.getClickCount() == 2)
				{
				TreePath path = events.getPathForLocation(e.getX(),e.getY());
				if (path == null) return;
				EventNode n = (EventNode) path.getLastPathComponent();
				if (n != null && n.isLeaf() && linkedFrame != null && n.isValid())
					{
					linkedFrame.addEvent(new Event(n.mainId,n.eventId,n.other));
					}
				}
			}
		}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == linkSelect)
			{
			GmObject obj = ((ResourceMenu<GmObject>) e.getSource()).getSelected();
			if (obj != null)
				{
				ResNode node = findNode(Listener.getPrimaryParent(Resource.GMOBJECT),obj);
				linkedFrame = (GmObjectFrame) node.frame;
				linkedFrame.toTop();
				if (isVisible()) this.toTop();
				}
			else
				linkedFrame = null;
			return;
			}
		if (e.getSource() == onTop)
			{
			setLayer(onTop.isSelected() ? JLayeredPane.MODAL_LAYER : JLayeredPane.DEFAULT_LAYER);
			return;
			}
		if (e.getSource() == collisionSelect)
			{
			if (selectedNode.mainId == MainEvent.EV_COLLISION && collisionSelect.getSelected() != null)
				selectedNode.other = collisionSelect.getSelected().getRef();
			}
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

	public void valueChanged(TreeSelectionEvent e)
		{
		selectedNode = (EventNode) e.getPath().getLastPathComponent();
		switch (selectedNode.mainId)
			{
			case MainEvent.EV_COLLISION:
				keySelect.setEnabled(false);
				collisionSelect.setEnabled(true);
				break;
			case MainEvent.EV_KEYBOARD:
			case MainEvent.EV_KEYPRESS:
			case MainEvent.EV_KEYRELEASE:
				keySelect.setEnabled(true);
				collisionSelect.setEnabled(false);
				selectedNode.eventId = keySelect.selectedKey;
				break;
			default:
				keySelect.setEnabled(false);
				collisionSelect.setEnabled(false);
				break;
			}
		}

	private ResNode findNode(ResNode root, Resource<?> res)
		{
		for (int i = 0; i < root.getChildCount(); i++)
			{
			ResNode node = (ResNode) root.getChildAt(i);
			if (!node.isLeaf())
				{
				ResNode found = findNode(node,res);
				if (found != null) return found;
				continue;
				}
			if (node.res == res) return node;
			}
		return null;
		}

	public void propertyChange(PropertyChangeEvent evt)
		{
		if (evt.getPropertyName().equals(MDIPane.SELECTED_FRAME_PROPERTY))
			{
			JInternalFrame newFrame = (JInternalFrame) evt.getNewValue();
			JInternalFrame oldFrame = (JInternalFrame) evt.getOldValue();
			if (newFrame instanceof GmObjectFrame)
				{
				linkedFrame = (GmObjectFrame) newFrame;
				linkSelect.setSelected((GmObject) linkedFrame.node.res);
				}
			else
				{
				if (newFrame == null && !oldFrame.isVisible()) linkSelect.setSelected(null);
				}
			}
		}
	}
