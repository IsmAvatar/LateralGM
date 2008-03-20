/*
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static org.lateralgm.main.Util.addDim;
import static org.lateralgm.main.Util.deRef;
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
import java.lang.ref.WeakReference;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.lateralgm.components.EventKeyInput;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.EventNode;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.components.mdi.MDIPane;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class EventFrame extends MDIFrame implements ActionListener,TreeSelectionListener,
		PropertyChangeListener
	{
	private static final long serialVersionUID = 1L;

	public static final int FUNCTION_ADD = 0;
	public static final int FUNCTION_REPLACE = 1;
	public static final int FUNCTION_DUPLICATE = 2;

	public IndexButtonGroup function;
	public EventKeyInput keySelect;
	public JPanel keySelectPanel;
	public ResourceMenu<GmObject> collisionSelect;
	public JPanel collisionSelectPanel;
	public JPanel emptyPanel;
	public ResourceMenu<GmObject> linkSelect;
	public GmObjectFrame linkedFrame;
	private MListener mListener = new MListener();
	public EventNode root;
	public JTree events;
	public JCheckBox onTop;
	public EventNode selectedNode;

	public EventFrame()
		{
		super(Messages.getString("EventFrame.TITLE"),true,true,true,true); //$NON-NLS-1$

		setSize(300,335);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setFrameIcon(LGM.getIconForKey("LGM.TOGGLE_EVENT")); //$NON-NLS-1$
		setMinimumSize(new Dimension(300,335));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		JPanel side1 = new JPanel(new BorderLayout());

		makeTree(side1);

		add(side1);

		JPanel side2Parent = new JPanel();
		JPanel side2 = new JPanel(new FlowLayout());
		side2Parent.add(side2);
		side2.setPreferredSize(new Dimension(150,315));
		side2.setMaximumSize(new Dimension(150,315));
		side2.setMinimumSize(new Dimension(150,315));

		side2.add(new JLabel(Messages.getString("EventFrame.DOUBLE_CLICK"))); //$NON-NLS-1$
		addGap(side2,100,1);

		function = new IndexButtonGroup(3,true,false);
		JRadioButton rad = new JRadioButton(Messages.getString("EventFrame.ADD")); //$NON-NLS-1$
		function.add(rad);
		rad = new JRadioButton(Messages.getString("EventFrame.REPLACE")); //$NON-NLS-1$
		function.add(rad);
		rad = new JRadioButton(Messages.getString("EventFrame.DUPLICATE")); //$NON-NLS-1$
		function.add(rad);
		JPanel panel = Util.makeRadioPanel(Messages.getString("EventFrame.FUNCTION"),120,100); //$NON-NLS-1$
		function.populate(panel);
		function.setValue(FUNCTION_ADD);
		side2.add(panel);

		keySelectPanel = new JPanel();
		addDim(keySelectPanel,new JLabel(Messages.getString("EventFrame.KEY_SELECTOR")),140,16); //$NON-NLS-1$
		keySelect = new EventKeyInput(this);
		addDim(keySelectPanel,keySelect,140,20);
		addDim(side2,keySelectPanel,140,46);
		keySelectPanel.setVisible(false);

		collisionSelectPanel = new JPanel();
		JLabel lab = new JLabel(Messages.getString("EventFrame.COLLISION_OBJECT")); //$NON-NLS-1$
		addDim(collisionSelectPanel,lab,140,16);
		collisionSelect = new ResourceMenu<GmObject>(Resource.GMOBJECT,
				Messages.getString("EventFrame.CHOOSE_OBJECT"),true,140); //$NON-NLS-1$
		collisionSelectPanel.add(collisionSelect);
		collisionSelect.addActionListener(this);
		addDim(side2,collisionSelectPanel,140,46);
		collisionSelectPanel.setVisible(false);

		emptyPanel = new JPanel();
		addDim(side2,emptyPanel,140,46);

		addGap(side2,140,5);

		addDim(side2,new JLabel(Messages.getString("EventFrame.FRAME_LINK")),140,16); //$NON-NLS-1$
		linkSelect = new ResourceMenu<GmObject>(Resource.GMOBJECT,
				Messages.getString("EventFrame.NO_LINK"),true,140,true); //$NON-NLS-1$
		linkSelect.addActionListener(this);
		side2.add(linkSelect);

		addGap(side2,50,10);

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
			alarm.add(new EventNode(Messages.format("Event.EVENT2_X",i),MainEvent.EV_ALARM,i)); //$NON-NLS-1$

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

		String globMouseStr = Messages.getString("EventFrame.GLOBAL_MOUSE"); //$NON-NLS-1$
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
			user.add(new EventNode(Messages.format("Event.EVENT7_X",i),MainEvent.EV_OTHER,Event.EV_USER0 //$NON-NLS-1$
					+ i));

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

		public EventNodeTransferHandler()
			{
			super();
			}

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
		public MListener()
			{
			super();
			}

		public void mouseClicked(MouseEvent e)
			{
			if (e.getSource() != events) return;
			int button = e.getButton();
			int clicks = e.getClickCount();
			if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3)
				{
				TreePath path = events.getPathForLocation(e.getX(),e.getY());
				if (path == null) return;
				events.setSelectionPath(path);
				if (events.isExpanded(path))
					events.collapsePath(path);
				else
					events.expandPath(path);
				EventNode n = (EventNode) path.getLastPathComponent();
				boolean added = (button == MouseEvent.BUTTON1 && clicks == 2)
						|| (button == MouseEvent.BUTTON3 && clicks == 1);
				if (added && n != null && n.isLeaf() && linkedFrame != null && n.isValid())
					linkedFrame.addEvent(new Event(n.mainId,n.eventId,n.other));
				}
			}
		}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == linkSelect)
			{
			GmObject obj = deRef(((ResourceMenu<GmObject>) e.getSource()).getSelected());
			if (obj != null)
				{
				ResNode node = obj.getNode();
				linkedFrame = (GmObjectFrame) node.frame;
				linkedFrame.toTop();
				if (isVisible()) toTop();
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
				selectedNode.other = collisionSelect.getSelected();
			}
		}

	public void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_ICONIFIED) LGM.mdi.setLayer(getDesktopIcon(),0);
		super.fireInternalFrameEvent(id);
		}

	public void valueChanged(TreeSelectionEvent e)
		{
		selectedNode = (EventNode) e.getPath().getLastPathComponent();
		switch (selectedNode.mainId)
			{
			case MainEvent.EV_COLLISION:
				keySelectPanel.setVisible(false);
				collisionSelectPanel.setVisible(true);
				emptyPanel.setVisible(false);
				break;
			case MainEvent.EV_KEYBOARD:
			case MainEvent.EV_KEYPRESS:
			case MainEvent.EV_KEYRELEASE:
				keySelectPanel.setVisible(true);
				collisionSelectPanel.setVisible(false);
				emptyPanel.setVisible(false);
				selectedNode.eventId = keySelect.selectedKey;
				break;
			default:
				keySelectPanel.setVisible(false);
				collisionSelectPanel.setVisible(false);
				emptyPanel.setVisible(true);
				break;
			}
		}

	@SuppressWarnings("unchecked")
	public void propertyChange(PropertyChangeEvent evt)
		{
		if (evt.getPropertyName().equals(MDIPane.SELECTED_FRAME_PROPERTY))
			{
			JInternalFrame newFrame = (JInternalFrame) evt.getNewValue();
			JInternalFrame oldFrame = (JInternalFrame) evt.getOldValue();
			if (newFrame instanceof GmObjectFrame)
				{
				linkedFrame = (GmObjectFrame) newFrame;
				linkSelect.setSelected((WeakReference<GmObject>) linkedFrame.node.getRes());
				}
			else
				{
				if (newFrame == null && !oldFrame.isVisible()) linkSelect.setSelected(null);
				}
			}
		}
	}
