/*
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static org.lateralgm.main.Util.deRef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.EventNode;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.components.mdi.MDIPane;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.GmObjectFrame.EventGroupNode;
import org.lateralgm.subframes.GmObjectFrame.EventInstanceNode;

public class EventFrame extends MDIFrame implements ActionListener,TreeSelectionListener,
		PropertyChangeListener,UpdateListener
	{
	private static final long serialVersionUID = 1L;

	public static final int FUNCTION_ADD = 0;
	public static final int FUNCTION_REPLACE = 1;
	public static final int FUNCTION_DUPLICATE = 2;

	public IndexButtonGroup function;
	public JPanel keySelectPanel;
	public ResourceMenu<GmObject> collisionSelect;
	public JPanel collisionSelectPanel;
	public JPanel emptyPanel;
	public ResourceMenu<GmObject> linkSelect;
	public WeakReference<GmObjectFrame> linkedFrame;
	public JButton okButton;
	private MListener mListener = new MListener();
	public EventNode root;
	public JTree events;
	public EventNode selectedNode;
	public JPopupMenu eventMenu;
	public EventNode collision;
	protected static final ImageIcon GROUP_ICO = LGM.getIconForKey("GmTreeGraphics.GROUP"); //$NON-NLS-1$;

	public EventFrame()
		{
		super(Messages.getString("EventFrame.TITLE"),true,true,true,true); //$NON-NLS-1$

		setSize(250,355);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setFrameIcon(LGM.getIconForKey("LGM.TOGGLE_EVENT")); //$NON-NLS-1$
		setMinimumSize(new Dimension(250,355));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JPanel side1 = new JPanel(new BorderLayout());
		makeTree(side1);

		JPanel side2Parent = new JPanel();
		FlowLayout side2Layout = new FlowLayout(FlowLayout.LEADING);
		side2Layout.setHgap(12);
		JPanel side2 = new JPanel(side2Layout);
		side2Parent.add(side2);
		side2.setPreferredSize(new Dimension(250,50));
		side2.setMaximumSize(new Dimension(250,50));
		side2.setMinimumSize(new Dimension(250,50));

		JPanel objectFramePanel = new JPanel(new BorderLayout(0,0));
		side2.add(objectFramePanel);
		JLabel windowLabel = new JLabel(Messages.getString("EventFrame.OBJECT_WINDOW"));
		objectFramePanel.add(windowLabel,BorderLayout.CENTER); //$NON-NLS-1$
		linkSelect = new ResourceMenu<GmObject>(Resource.Kind.OBJECT,
				Messages.getString("EventFrame.NO_LINK"),false,140,true,true); //$NON-NLS-1$
		linkSelect.addActionListener(this);
		objectFramePanel.add(linkSelect,BorderLayout.EAST);

		JPanel functionPanel = new JPanel(new BorderLayout(8,0));
		side2.add(functionPanel);
		function = new IndexButtonGroup(3,true,false);
		JRadioButton rad = new JRadioButton(Messages.getString("EventFrame.ADD")); //$NON-NLS-1$
		function.add(rad);
		functionPanel.add(rad,BorderLayout.WEST);
		rad = new JRadioButton(Messages.getString("EventFrame.REPLACE")); //$NON-NLS-1$
		function.add(rad);
		functionPanel.add(rad,BorderLayout.CENTER);
		rad = new JRadioButton(Messages.getString("EventFrame.DUPLICATE")); //$NON-NLS-1$
		function.add(rad);
		functionPanel.add(rad,BorderLayout.EAST);
		function.setValue(FUNCTION_ADD);

		add(side2Parent);
		add(side1);
		LGM.mdi.addPropertyChangeListener(MDIPane.SELECTED_FRAME_PROPERTY,this);
		LGM.root.updateSource.addListener(this);
		}

	private void makeTree(JPanel side1)
		{
		root = new EventNode("Root"); //$NON-NLS-1$

		//CREATE
		root.add(MainEvent.EV_CREATE);

		//DESTROY
		root.add(MainEvent.EV_DESTROY);

		//ALARM
		EventNode alarm = new EventNode(MainEvent.EV_ALARM);
		root.add(alarm);
		for (int i = 0; i <= 11; i++)
			alarm.add(new EventNode(
					Messages.format("Event.EVENT2_X",i),MainEvent.EV_ALARM,Event.EV_ALARM0 + i)); //$NON-NLS-1$

		//STEP
		EventNode step = new EventNode(MainEvent.EV_STEP);
		root.add(step);
		for (int i = Event.EV_STEP_NORMAL; i <= Event.EV_STEP_END; i++)
			step.add(MainEvent.EV_STEP,i);

		//COLLISION
		collision = new EventNode(MainEvent.EV_COLLISION);
		root.add(collision);
		populate_collision_node();

		//KEYBOARD
		EventNode keyboard = new EventNode(MainEvent.EV_KEYBOARD);
		root.add(keyboard);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_LEFT);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_RIGHT);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_UP);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_DOWN);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_CONTROL);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_ALT);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_SHIFT);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_SPACE);
		keyboard.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_ENTER);

		EventNode subkey;
		subkey = new EventNode(Messages.getString("EventFrame.KEYPAD"),MainEvent.EV_KEYBOARD,0); //$NON-NLS-1$
		keyboard.add(subkey);
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; i++)
			subkey.add(MainEvent.EV_KEYBOARD,i);

		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_DIVIDE);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_MULTIPLY);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_SUBTRACT);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_ADD);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_DECIMAL);

		subkey = new EventNode(Messages.getString("EventFrame.DIGITS"),MainEvent.EV_KEYBOARD,0); //$NON-NLS-1$
		keyboard.add(subkey);
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++)
			subkey.add(MainEvent.EV_KEYBOARD,i);

		subkey = new EventNode(Messages.getString("EventFrame.LETTERS"),MainEvent.EV_KEYBOARD,0); //$NON-NLS-1$
		keyboard.add(subkey);
		for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++)
			subkey.add(MainEvent.EV_KEYBOARD,i);

		subkey = new EventNode(Messages.getString("EventFrame.FUNCTION_KEYS"),MainEvent.EV_KEYBOARD,0); //$NON-NLS-1$
		keyboard.add(subkey);
		for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++)
			subkey.add(MainEvent.EV_KEYBOARD,i);

		subkey = new EventNode(Messages.getString("EventFrame.OTHERS"),MainEvent.EV_KEYBOARD,0); //$NON-NLS-1$
		keyboard.add(subkey);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_BACK_SPACE);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_ESCAPE);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_HOME);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_END);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_PAGE_UP);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_PAGE_DOWN);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_DELETE);
		subkey.add(MainEvent.EV_KEYBOARD,KeyEvent.VK_INSERT);

		//MOUSE
		EventNode mouse = new EventNode(MainEvent.EV_MOUSE);
		root.add(mouse);
		for (int i = Event.EV_LEFT_BUTTON; i <= Event.EV_MOUSE_LEAVE; i++)
			mouse.add(MainEvent.EV_MOUSE,i);

		for (int i = Event.EV_MOUSE_WHEEL_UP; i <= Event.EV_MOUSE_WHEEL_DOWN; i++)
			mouse.add(MainEvent.EV_MOUSE,i);

		EventNode submouse;
		submouse = new EventNode(Messages.getString("EventFrame.GLOBAL_MOUSE"),MainEvent.EV_MOUSE,0); //$NON-NLS-1$
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

		//OTHER
		EventNode other = new EventNode(MainEvent.EV_OTHER);
		root.add(other);
		for (int i = Event.EV_OUTSIDE; i <= Event.EV_NO_MORE_HEALTH; i++)
			other.add(MainEvent.EV_OTHER,i);

		EventNode user = new EventNode(
				Messages.getString("EventFrame.USER_DEFINED"),MainEvent.EV_OTHER,0); //$NON-NLS-1$
		other.add(user);
		for (int i = 0; i <= 14; i++)
			{
			user.add(new EventNode(
					Messages.format("Event.EVENT7_X",i),MainEvent.EV_OTHER,Event.EV_USER0 + i)); //$NON-NLS-1$
			}

		//DRAW
		root.add(MainEvent.EV_DRAW);

		//KEYPRESS
		EventNode keypress = new EventNode(MainEvent.EV_KEYPRESS);
		root.add(keypress);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_LEFT);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_RIGHT);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_UP);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_DOWN);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_CONTROL);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_ALT);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_SHIFT);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_SPACE);
		keypress.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_ENTER);

		subkey = new EventNode(Messages.getString("EventFrame.KEYPAD"),MainEvent.EV_KEYPRESS,0); //$NON-NLS-1$
		keypress.add(subkey);
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; i++)
			subkey.add(MainEvent.EV_KEYPRESS,i);

		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_DIVIDE);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_MULTIPLY);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_SUBTRACT);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_ADD);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_DECIMAL);

		subkey = new EventNode(Messages.getString("EventFrame.DIGITS"),MainEvent.EV_KEYPRESS,0); //$NON-NLS-1$
		keypress.add(subkey);
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++)
			subkey.add(MainEvent.EV_KEYPRESS,i);

		subkey = new EventNode(Messages.getString("EventFrame.LETTERS"),MainEvent.EV_KEYPRESS,0); //$NON-NLS-1$
		keypress.add(subkey);
		for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++)
			subkey.add(MainEvent.EV_KEYPRESS,i);

		subkey = new EventNode(Messages.getString("EventFrame.FUNCTION_KEYS"),MainEvent.EV_KEYPRESS,0); //$NON-NLS-1$
		keypress.add(subkey);
		for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++)
			subkey.add(MainEvent.EV_KEYPRESS,i);

		subkey = new EventNode(Messages.getString("EventFrame.OTHERS"),MainEvent.EV_KEYPRESS,0); //$NON-NLS-1$
		keypress.add(subkey);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_BACK_SPACE);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_ESCAPE);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_HOME);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_END);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_PAGE_UP);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_PAGE_DOWN);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_DELETE);
		subkey.add(MainEvent.EV_KEYPRESS,KeyEvent.VK_INSERT);

		//KEYRELEASE
		EventNode keyrelase = new EventNode(MainEvent.EV_KEYRELEASE);
		root.add(keyrelase);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_LEFT);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_RIGHT);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_UP);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_DOWN);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_CONTROL);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_ALT);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_SHIFT);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_SPACE);
		keyrelase.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_ENTER);

		subkey = new EventNode(Messages.getString("EventFrame.KEYPAD"),MainEvent.EV_KEYRELEASE,0); //$NON-NLS-1$
		keyrelase.add(subkey);
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; i++)
			subkey.add(MainEvent.EV_KEYRELEASE,i);

		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_DIVIDE);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_MULTIPLY);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_SUBTRACT);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_ADD);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_DECIMAL);

		subkey = new EventNode(Messages.getString("EventFrame.DIGITS"),MainEvent.EV_KEYRELEASE,0); //$NON-NLS-1$
		keyrelase.add(subkey);
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++)
			subkey.add(MainEvent.EV_KEYRELEASE,i);

		subkey = new EventNode(Messages.getString("EventFrame.LETTERS"),MainEvent.EV_KEYRELEASE,0); //$NON-NLS-1$
		keyrelase.add(subkey);
		for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++)
			subkey.add(MainEvent.EV_KEYRELEASE,i);

		subkey = new EventNode(Messages.getString("EventFrame.FUNCTION_KEYS"),MainEvent.EV_KEYRELEASE,0); //$NON-NLS-1$
		keyrelase.add(subkey);
		for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++)
			subkey.add(MainEvent.EV_KEYRELEASE,i);

		subkey = new EventNode(Messages.getString("EventFrame.OTHERS"),MainEvent.EV_KEYRELEASE,0); //$NON-NLS-1$
		keyrelase.add(subkey);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_BACK_SPACE);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_ESCAPE);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_HOME);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_END);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_PAGE_UP);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_PAGE_DOWN);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_DELETE);
		subkey.add(MainEvent.EV_KEYRELEASE,KeyEvent.VK_INSERT);

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
		side1.add(scroll,BorderLayout.CENTER);
		}

	public void populate_collision_node()
		{
		collision.removeAllChildren();
		if (Prefs.groupKind)
			{
			for (int i = 0; i < LGM.root.getChildCount(); i++)
				{
				ResNode group = (ResNode) LGM.root.getChildAt(i);
				if (group.kind != Resource.Kind.OBJECT) continue;
				populate_object_nodes(collision,group);
				return;
				}
			}
		populate_object_nodes(collision,LGM.root);
		return;
		}

	@SuppressWarnings("unchecked")
	protected void populate_object_nodes(EventNode parent, ResNode group)
		{
		for (int i = 0; i < group.getChildCount(); i++)
			{
			ResNode child = (ResNode) group.getChildAt(i);
			if (child.kind != Resource.Kind.OBJECT) continue;
			if (child.status == ResNode.STATUS_SECONDARY)
				parent.add(MainEvent.EV_COLLISION,(ResourceReference<GmObject>) child.getRes());
			else if (child.status == ResNode.STATUS_GROUP)
				{
				EventNode subnode = new EventNode(child.getUserObject().toString(),MainEvent.EV_COLLISION,0);
				parent.add(subnode);
				if (child.getChildCount() > 0) populate_object_nodes(subnode,child);
				}
			}
		}

	public static class EventNodeRenderer extends DefaultTreeCellRenderer
		{
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
				boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
			super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
			int mid = -1;
			if (value instanceof EventNode)
				{
				EventNode en = (EventNode) value;
				mid = en.mainId;
				if (mid == MainEvent.EV_COLLISION && ((DefaultMutableTreeNode) en.getParent()).isRoot())
					leaf = false;
				}
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

				boolean added = (button == MouseEvent.BUTTON1 && clicks == 2)
						|| (button == MouseEvent.BUTTON3 && clicks == 1);
				GmObjectFrame f = linkedFrame == null ? null : linkedFrame.get();
				if (added && n.isLeaf() && f != null && n.isValid())
					{
					f.functionEvent(n.mainId,n.eventId,n.other,null);
					f.toTop();
					}
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
		//		populate_collision_node();
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

	public void updated(UpdateEvent e)
		{
		populate_collision_node();
		events.updateUI();
		}
	}
