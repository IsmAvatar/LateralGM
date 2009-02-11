/*
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static org.lateralgm.main.Util.addDim;
import static org.lateralgm.main.Util.deRef;
import static org.lateralgm.subframes.ResourceFrame.addGap;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.Icon;
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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.lateralgm.components.EventKeySelector;
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
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.GmObjectFrame.EventGroupNode;
import org.lateralgm.subframes.GmObjectFrame.EventInstanceNode;

public class EventFrame extends MDIFrame implements ActionListener,TreeSelectionListener,
		PropertyChangeListener
	{
	private static final long serialVersionUID = 1L;

	public static final int FUNCTION_ADD = 0;
	public static final int FUNCTION_REPLACE = 1;
	public static final int FUNCTION_DUPLICATE = 2;

	public IndexButtonGroup function;
	public EventKeySelector keySelect;
	public JPanel keySelectPanel;
	public ResourceMenu<GmObject> collisionSelect;
	public JPanel collisionSelectPanel;
	public JPanel emptyPanel;
	public ResourceMenu<GmObject> linkSelect;
	public WeakReference<GmObjectFrame> linkedFrame;
	private MListener mListener = new MListener();
	public EventNode root;
	public JTree events;
	public JCheckBox onTop;
	public EventNode selectedNode;

	public EventFrame()
		{
		super(Messages.getString("EventFrame.TITLE"),true,true,true,true); //$NON-NLS-1$

		setSize(400,335);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setFrameIcon(LGM.getIconForKey("LGM.TOGGLE_EVENT")); //$NON-NLS-1$
		setMinimumSize(new Dimension(400,335));
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
		keySelect = new EventKeySelector(this,140);
		addDim(keySelectPanel,keySelect,140,20);
		addDim(side2,keySelectPanel,140,46);
		keySelectPanel.setVisible(false);

		collisionSelectPanel = new JPanel();
		JLabel lab = new JLabel(Messages.getString("EventFrame.COLLISION_OBJECT")); //$NON-NLS-1$
		addDim(collisionSelectPanel,lab,140,16);
		collisionSelect = new ResourceMenu<GmObject>(Resource.Kind.OBJECT,
				Messages.getString("EventFrame.CHOOSE_OBJECT"),true,140); //$NON-NLS-1$
		collisionSelectPanel.add(collisionSelect);
		collisionSelect.addActionListener(this);
		addDim(side2,collisionSelectPanel,140,46);
		collisionSelectPanel.setVisible(false);

		emptyPanel = new JPanel();
		addDim(side2,emptyPanel,140,46);

		addGap(side2,140,5);

		addDim(side2,new JLabel(Messages.getString("EventFrame.FRAME_LINK")),140,16); //$NON-NLS-1$
		linkSelect = new ResourceMenu<GmObject>(Resource.Kind.OBJECT,
				Messages.getString("EventFrame.NO_LINK"),true,140,true,true); //$NON-NLS-1$
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
		root = new EventNode("Root"); //$NON-NLS-1$

		root.add(MainEvent.EV_CREATE);
		root.add(MainEvent.EV_DESTROY);

		EventNode alarm = new EventNode(MainEvent.EV_ALARM);
		root.add(alarm);
		for (int i = 0; i <= 11; i++)
			alarm.add(new EventNode(Messages.format("Event.EVENT2_X",i),MainEvent.EV_ALARM,i)); //$NON-NLS-1$

		EventNode step = new EventNode(MainEvent.EV_STEP);
		root.add(step);
		for (int i = Event.EV_STEP_NORMAL; i <= Event.EV_STEP_END; i++)
			step.add(MainEvent.EV_STEP,i);

		root.add(MainEvent.EV_COLLISION);
		root.add(MainEvent.EV_KEYBOARD);

		EventNode mouse = new EventNode(MainEvent.EV_MOUSE);
		root.add(mouse);
		for (int i = Event.EV_LEFT_BUTTON; i <= Event.EV_MOUSE_LEAVE; i++)
			mouse.add(MainEvent.EV_MOUSE,i);
		mouse.add(MainEvent.EV_MOUSE,Event.EV_MOUSE_WHEEL_UP);
		mouse.add(MainEvent.EV_MOUSE,Event.EV_MOUSE_WHEEL_DOWN);

		String name = Messages.getString("EventFrame.GLOBAL_MOUSE"); //$NON-NLS-1$
		EventNode submouse = new EventNode(name,MainEvent.EV_MOUSE,0);
		mouse.add(submouse);
		for (int i = Event.EV_GLOBAL_LEFT_BUTTON; i <= Event.EV_GLOBAL_MIDDLE_RELEASE; i++)
			submouse.add(MainEvent.EV_MOUSE,i);

		submouse = new EventNode(Messages.getString("EventFrame.JOYSTICK_1"),MainEvent.EV_MOUSE,0); //$NON-NLS-1$
		mouse.add(submouse);
		for (int i = Event.EV_JOYSTICK1_LEFT; i <= Event.EV_JOYSTICK1_BUTTON8; i++)
			if (i != 20) submouse.add(MainEvent.EV_MOUSE,i);

		submouse = new EventNode(Messages.getString("EventFrame.JOYSTICK_2"),MainEvent.EV_MOUSE,0); //$NON-NLS-1$
		mouse.add(submouse);
		for (int i = Event.EV_JOYSTICK2_LEFT; i <= Event.EV_JOYSTICK2_BUTTON8; i++)
			if (i != 35) submouse.add(MainEvent.EV_MOUSE,i);

		EventNode other = new EventNode(MainEvent.EV_OTHER);
		root.add(other);
		for (int i = 0; i <= 8; i++)
			other.add(MainEvent.EV_OTHER,i);

		EventNode user = new EventNode(
				Messages.getString("EventFrame.USER_DEFINED"),MainEvent.EV_OTHER,0); //$NON-NLS-1$
		other.add(user);
		for (int i = 0; i <= 14; i++)
			{
			name = Messages.format("Event.EVENT7_X",i); //$NON-NLS-1$
			user.add(new EventNode(name,MainEvent.EV_OTHER,Event.EV_USER0 + i));
			}

		root.add(MainEvent.EV_DRAW);
		root.add(MainEvent.EV_KEYPRESS);
		root.add(MainEvent.EV_KEYRELEASE);

		events = new JTree(root);
		events.setCellRenderer(new EventNodeRenderer());
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

	public static class EventNodeRenderer extends DefaultTreeCellRenderer
		{
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
				boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
			super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
			int mid = -1;
			if (value instanceof EventNode) mid = ((EventNode) value).mainId;
			if (value instanceof EventInstanceNode)
				mid = ((EventInstanceNode) value).getUserObject().mainId;
			if (value instanceof EventGroupNode) mid = ((EventGroupNode) value).mainId;
			Icon i = LGM.getIconForKey("EventNode." + (leaf ? "EVENT" : "GROUP") + mid);
			if (i != null && i.getIconWidth() != -1) setIcon(i);
			return this;
			}
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
			if (!n.isLeaf()) return null;
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

		public void mouseReleased(MouseEvent e)
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
				if (n == null) return;
				if (n.mainId == MainEvent.EV_KEYBOARD || n.mainId == MainEvent.EV_KEYPRESS
						|| n.mainId == MainEvent.EV_KEYRELEASE) keySelect.text.requestFocusInWindow();

				boolean added = (button == MouseEvent.BUTTON1 && clicks == 2)
						|| (button == MouseEvent.BUTTON3 && clicks == 1);
				GmObjectFrame f = linkedFrame == null ? null : linkedFrame.get();
				if (added && n.isLeaf() && f != null && n.isValid())
					f.addEvent(new Event(n.mainId,n.eventId,n.other));
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
				GmObjectFrame f = (GmObjectFrame) node.frame;
				linkedFrame = new WeakReference<GmObjectFrame>(f);
				f.toTop();
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

	public void setVisible(boolean b)
		{
		super.setVisible(b);
		if (!b && events != null) for (int m = events.getRowCount() - 1; m >= 0; m--)
			events.collapseRow(m);
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
				selectedNode.eventId = keySelect.getSelectedKey();
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
	}
