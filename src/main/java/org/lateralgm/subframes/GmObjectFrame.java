/*
 * Copyright (C) 2007, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.lateralgm.main.Util.deRef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.ActionList;
import org.lateralgm.components.ActionListEditor;
import org.lateralgm.components.CodeTextArea;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.ActionList.ActionListModel;
import org.lateralgm.components.impl.EventNode;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.GmObject.ParentLoopException;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class GmObjectFrame extends InstantiableResourceFrame<GmObject,PGmObject> implements
		TreeSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon INFO_ICON = LGM.getIconForKey("GmObjectFrame.INFO"); //$NON-NLS-1$

	public ResourceMenu<Sprite> sprite;
	public JButton newSprite;
	public JButton editSprite;
	public JCheckBox visible;
	public JCheckBox solid;
	public NumberField depth;
	public JCheckBox persistent;
	public ResourceMenu<GmObject> parent;
	public ResourceMenu<Sprite> mask;
	public JButton information;

	public EventTree events;
	public JButton eventModify;
	public JButton eventEdit;
	public JButton eventDelete;
	public JMenuItem eventModifyItem;
	public JMenuItem eventAddItem;
	public JMenuItem eventReplaceItem;
	public JMenuItem eventDuplicateItem;
	public JMenuItem eventEditItem;
	public JMenuItem eventDeleteItem;
	public EventGroupNode rootEvent;
	private MListener mListener = new MListener();
	public ActionList actions;
	public CodeTextArea code;
	private JComponent editor;

	private ResourceInfoFrame infoFrame;

	private DefaultMutableTreeNode lastValidEventSelection;
	private JCheckBox physics;
	private JPanel phyPane;
	private PropertyUpdateListener<PGmObject> propUpdateListener;

	// if drag and drop is not enabled the frame will not create or show
	// the action list editor but still create the action list, and add
	// an extra edit button for events, which when clicked checks the first
	// action of the action list to be a code window and if it is opens it
	// otherwise it creates one and opens it
	// this was the safest way I could think to do it, because it could get
	// tricky opening a DND game when you have it turned off, so I abstracted it
	// so that they are still there and you can see them if you just go to
	// the prefs panel and turn em back on, it will ensure the system doesnt
	// mess anything up or screw up somebodies game without giving them
	// a chance to fix it, very safe - Robert B. Colton

	public GmObjectFrame(GmObject res, ResNode node)
		{
		super(res,node);

		this.getRootPane().setDefaultButton(save);
		GroupLayout layout = new GroupLayout(getContentPane());
		setLayout(layout);

		JPanel propPane = makePropertiesPane();
		phyPane = makePhysicsPane();

		JPanel evtPane = new JPanel(new BorderLayout());
		JLabel lab = new JLabel(Messages.getString("GmObjectFrame.EVENTS")); //$NON-NLS-1$
		evtPane.add(lab,BorderLayout.NORTH);
		makeEventTree(res);
		JScrollPane scroll = new JScrollPane(events);
		//if (Prefs.enableDragAndDrop) {
		scroll.setPreferredSize(new Dimension(140,260));
		//} else {
		//scroll.setPreferredSize(new Dimension(300,260));
		//}
		evtPane.add(scroll,BorderLayout.CENTER);

		JPanel eventButtonPane = new JPanel(new BorderLayout());

		eventModify = new JButton(Messages.getString("GmObjectFrame.MODIFY")); //$NON-NLS-1
		eventModify.addActionListener(this);
		eventModify.setToolTipText(Messages.getString("GmObjectFrame.MODIFY_EVENT")); //$NON-NLS-1$

		eventEdit = new JButton(Messages.getString("GmObjectFrame.EDIT")); //$NON-NLS-1
		eventEdit.addActionListener(this);
		eventEdit.setToolTipText(Messages.getString("GmObjectFrame.EDIT_EVENT")); //$NON-NLS-1$

		eventDelete = new JButton(Messages.getString("GmObjectFrame.DELETE")); //$NON-NLS-1$
		eventDelete.addActionListener(this);
		eventDelete.setToolTipText(Messages.getString("GmObjectFrame.DELETE_EVENT")); //$NON-NLS-1$

		eventButtonPane.setLayout(new GridLayout());
		eventButtonPane.add(eventModify);
		eventButtonPane.add(eventEdit);
		eventButtonPane.add(eventDelete);

		evtPane.add(eventButtonPane,BorderLayout.SOUTH);

		actions = new ActionList(this);
		if (Prefs.enableDragAndDrop)
			{
			editor = new ActionListEditor(actions);
			}

		ParallelGroup pg = null;
		SequentialGroup sg = layout.createSequentialGroup();


		if (Prefs.rightOrientation) {
			if (Prefs.enableDragAndDrop)
			{
				sg.addComponent(editor);
			}
			sg.addComponent(evtPane)
				/**/.addComponent(phyPane,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
				/**/.addComponent(propPane,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
		} else {
			sg.addComponent(propPane,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
				/**/.addComponent(phyPane,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
				/**/.addComponent(evtPane);
			if (Prefs.enableDragAndDrop)
			{
				sg.addComponent(editor);
			}
		}
		layout.setHorizontalGroup(sg);

		pg = layout.createParallelGroup()
		/**/.addComponent(propPane)
		/**/.addComponent(phyPane)
		/**/.addComponent(evtPane);
		if (Prefs.enableDragAndDrop)
			{
			pg.addComponent(editor);
			}
		layout.setVerticalGroup(pg);

		pack();

		phyPane.setVisible((Boolean) res.properties.get(PGmObject.PHYSICS_OBJECT));

		propUpdateListener = new PropertyUpdateListener<PGmObject>()
			{
				public void updated(PropertyUpdateEvent<PGmObject> e)
					{
					if (e.key == PGmObject.PHYSICS_OBJECT)
						{
						phyPane.setVisible((Boolean) e.map.get(e.key));
						}
					}
			};
		res.properties.updateSource.addListener(propUpdateListener);

		// Select first event
		TreeNode event = (TreeNode) events.getModel().getRoot();
		while (event.getChildCount() > 0)
			event = event.getChildAt(0);
		if (event != events.getModel().getRoot())
			events.setSelectionPath(new TreePath(((DefaultMutableTreeNode) event).getPath()));
		}

	private JPanel makePhysicsPane()
		{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(
				Messages.getString("GmObjectFrame.PHYSICS_PROPERTIES")));

		JCheckBox awakeCB = new JCheckBox(Messages.getString("GmObjectFrame.AWAKE"));
		plf.make(awakeCB,PGmObject.PHYSICS_AWAKE);
		JCheckBox kinematicCB = new JCheckBox(Messages.getString("GmObjectFrame.KINEMATIC"));
		plf.make(kinematicCB,PGmObject.PHYSICS_KINEMATIC);
		JCheckBox sensorCB = new JCheckBox(Messages.getString("GmObjectFrame.SENSOR"));
		plf.make(sensorCB,PGmObject.PHYSICS_SENSOR);

		JLabel densityLabel = new JLabel(Messages.getString("GmObjectFrame.DENSITY"));
		NumberField densityField = new NumberField(0.0);
		plf.make(densityField,PGmObject.PHYSICS_DENSITY);
		JLabel restLabel = new JLabel(Messages.getString("GmObjectFrame.RESTITUTION"));
		NumberField restField = new NumberField(0.0);
		plf.make(restField,PGmObject.PHYSICS_RESTITUTION);
		JLabel groupLabel = new JLabel(Messages.getString("GmObjectFrame.COLLISION_GROUP"));
		NumberField groupField = new NumberField(0);
		plf.make(groupField,PGmObject.PHYSICS_GROUP);
		JLabel linearLabel = new JLabel(Messages.getString("GmObjectFrame.DAMPING_LINEAR"));
		NumberField linearField = new NumberField(0.0);
		plf.make(linearField,PGmObject.PHYSICS_DAMPING_LINEAR);
		JLabel angularLabel = new JLabel(Messages.getString("GmObjectFrame.DAMPING_ANGULAR"));
		NumberField angularField = new NumberField(0.0);
		plf.make(angularField,PGmObject.PHYSICS_DAMPING_ANGULAR);
		JLabel frictionLabel = new JLabel(Messages.getString("GmObjectFrame.FRICTION"));
		NumberField frictionField = new NumberField(0.0);
		plf.make(frictionField,PGmObject.PHYSICS_FRICTION);

		JButton shapeBT = new JButton(Messages.getString("GmObjectFrame.COLLISION_SHAPE"));

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(shapeBT)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(awakeCB)
		/*	*/.addComponent(kinematicCB))
		/**/.addComponent(sensorCB)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(densityLabel)
		/*		*/.addComponent(restLabel)
		/*		*/.addComponent(groupLabel)
		/*		*/.addComponent(linearLabel)
		/*		*/.addComponent(angularLabel)
		/*		*/.addComponent(frictionLabel))
		/*	*/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(densityField)
		/*		*/.addComponent(restField)
		/*		*/.addComponent(groupField)
		/*		*/.addComponent(linearField)
		/*		*/.addComponent(angularField)
		/*		*/.addComponent(frictionField))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(shapeBT)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(awakeCB)
		/*	*/.addComponent(kinematicCB))
		/**/.addComponent(sensorCB)
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(densityLabel)
		/*	*/.addComponent(densityField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(restLabel)
		/*	*/.addComponent(restField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(groupLabel)
		/*	*/.addComponent(groupField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(linearLabel)
		/*	*/.addComponent(linearField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(angularLabel)
		/*	*/.addComponent(angularField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(frictionLabel)
		/*	*/.addComponent(frictionField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)));

		return panel;
		}

	private JPanel makePropertiesPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		panel.setLayout(layout);

		JLabel nLabel = new JLabel(Messages.getString("GmObjectFrame.NAME")); //$NON-NLS-1$

		JPanel origin = new JPanel();
		GroupLayout oLayout = new GroupLayout(origin);
		origin.setLayout(oLayout);
		origin.setBorder(BorderFactory.createTitledBorder(Messages.getString("GmObjectFrame.SPRITE"))); //$NON-NLS-1$
		String t = Messages.getString("GmObjectFrame.NO_SPRITE"); //$NON-NLS-1$
		sprite = new ResourceMenu<Sprite>(Sprite.class,t,144);
		plf.make(sprite,PGmObject.SPRITE);
		newSprite = new JButton(Messages.getString("GmObjectFrame.NEW")); //$NON-NLS-1$
		newSprite.addActionListener(this);
		editSprite = new JButton(Messages.getString("GmObjectFrame.EDIT")); //$NON-NLS-1$
		editSprite.addActionListener(this);
		oLayout.setHorizontalGroup(oLayout.createSequentialGroup()
		/**/.addContainerGap(4,4)
		/**/.addGroup(oLayout.createParallelGroup()
		/*		*/.addComponent(sprite)
		/*		*/.addGroup(oLayout.createSequentialGroup()
		/*				*/.addComponent(newSprite,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addGap(4)
		/*				*/.addComponent(editSprite,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)))
		/**/.addContainerGap(4,4));
		oLayout.setVerticalGroup(oLayout.createSequentialGroup()
		/**/.addComponent(sprite,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addGap(2)
		/**/.addGroup(oLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(newSprite)
		/*		*/.addComponent(editSprite))
		/**/.addContainerGap(4,4));

		visible = new JCheckBox(Messages.getString("GmObjectFrame.VISIBLE")); //$NON-NLS-1$
		plf.make(visible,PGmObject.VISIBLE);
		solid = new JCheckBox(Messages.getString("GmObjectFrame.SOLID")); //$NON-NLS-1$
		plf.make(solid,PGmObject.SOLID);
		JLabel dLabel = new JLabel(Messages.getString("GmObjectFrame.DEPTH")); //$NON-NLS-1$
		depth = new NumberField(0);
		plf.make(depth,PGmObject.DEPTH);
		persistent = new JCheckBox(Messages.getString("GmObjectFrame.PERSISTENT")); //$NON-NLS-1$
		plf.make(persistent,PGmObject.PERSISTENT);
		physics = new JCheckBox(Messages.getString("GmObjectFrame.PHYSICS")); //$NON-NLS-1$
		plf.make(physics,PGmObject.PHYSICS_OBJECT);
		JLabel pLabel = new JLabel(Messages.getString("GmObjectFrame.PARENT")); //$NON-NLS-1$
		t = Messages.getString("GmObjectFrame.NO_PARENT"); //$NON-NLS-1$
		parent = new ResourceMenu<GmObject>(GmObject.class,t,110);
		plf.make(parent,PGmObject.PARENT);
		JLabel mLabel = new JLabel(Messages.getString("GmObjectFrame.MASK")); //$NON-NLS-1$
		t = Messages.getString("GmObjectFrame.SAME_AS_SPRITE"); //$NON-NLS-1$
		mask = new ResourceMenu<Sprite>(Sprite.class,t,110);
		plf.make(mask,PGmObject.MASK);
		information = new JButton(Messages.getString("GmObjectFrame.INFO"),INFO_ICON); //$NON-NLS-1$
		information.addActionListener(this);
		save.setText(Messages.getString("GmObjectFrame.SAVE")); //$NON-NLS-1$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(origin)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(dLabel)
		/*		*/.addComponent(depth))
		/**/.addGroup(layout.createParallelGroup(Alignment.LEADING)
		/*		*/.addComponent(pLabel)
		/*		*/.addComponent(mLabel))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(parent,DEFAULT_SIZE,120,MAX_VALUE)
		/*				*/.addComponent(mask,DEFAULT_SIZE,120,MAX_VALUE)))
		/**/.addGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(visible)
		/*		*/.addComponent(solid))
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(persistent)
		/*		*/.addComponent(physics)))
		/**/.addComponent(information,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name))
		/**/.addComponent(origin)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(dLabel)
		/*		*/.addComponent(depth))
		/**/.addComponent(pLabel)
		/**/.addGap(4)
		/**/.addComponent(parent,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addComponent(mLabel)
		/**/.addGap(4)
		/**/.addComponent(mask,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addGap(8)
		/**/.addGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(visible)
		/*		*/.addComponent(solid))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(persistent)
		/*		*/.addComponent(physics)))
		/**/.addGap(8,8,MAX_VALUE)
		/**/.addComponent(information)
		/**/.addComponent(save));

		return panel;
		}

	public static class EventTree extends JTree
		{
		private static final long serialVersionUID = 1L;

		public EventTree(TreeNode n)
			{
			super(n);
			//otherwise, getToolTipText won't be called
			setToolTipText(""); //$NON-NLS-1$
			}

		public String getToolTipText(MouseEvent e)
			{
			Point p = e.getPoint();
			TreePath path = getPathForLocation(p.x,p.y);
			if (path == null) return null;
			Object c = path.getLastPathComponent();
			if (c instanceof EventInstanceNode)
				{
				EventInstanceNode node = (EventInstanceNode) c;
				Event ev = node.getUserObject();
				return Messages.format("MainEvent.EVENT_HINT" + ev.mainId,ev.toString()); //$NON-NLS-1$
				}
			return Messages.format("MainEvent.EVENTS",c.toString()); //$NON-NLS-1$
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
			return null;
			}

		public int getSourceActions(JComponent c)
			{
			return NONE;
			}

		public boolean canImport(TransferHandler.TransferSupport support)
			{
			if (!support.isDataFlavorSupported(EventNode.DATA_FLAVOR)) return false;
			EventNode t = (EventNode) LGM.eventSelect.events.getLastSelectedPathComponent();
			if (t == null || !t.isValid()) return false;
			if (LGM.eventSelect.function.getValue() != EventPanel.FUNCTION_ADD
					&& !isValidEventInstanceNode(events.getLastSelectedPathComponent()))
				return false;
			if (rootEvent.contains(new Event(t.mainId,t.eventId,t.other))) return false;
			for (DataFlavor f : support.getDataFlavors())
				if (f == EventNode.DATA_FLAVOR) return true;
			return false;
			}

		public boolean importData(TransferHandler.TransferSupport support)
			{
			if (!canImport(support)) return false;
			try
				{
				EventNode t = (EventNode) support.getTransferable().getTransferData(
						EventNode.DATA_FLAVOR);
				if (!t.isValid()) return false;

				Point p = support.getDropLocation().getDropPoint();
				TreePath path = events.getPathForLocation(p.x,p.y);
				functionEvent(t.mainId,t.eventId,t.other,path);
				return true;
				}
			catch (Throwable e)
				{
				// This is just to stop the dnd system from silencing exceptions
				e.printStackTrace();
				}
			return false;
			}
		}

	public static class EventInstanceNode extends DefaultMutableTreeNode implements
			Comparable<EventInstanceNode>
		{
		private static final long serialVersionUID = 1L;

		public EventInstanceNode(Event e)
			{
			super(e);
			}

		public Event getUserObject()
			{
			return (Event) super.getUserObject();
			}

		public int compareTo(EventInstanceNode n)
			{
			return getUserObject().compareTo(n.getUserObject());
			}
		}

	public class EventGroupNode extends DefaultMutableTreeNode
		{
		private static final long serialVersionUID = 1L;
		public int mainId;

		public EventGroupNode(int mainId)
			{
			super(Messages.getString("MainEvent.EVENT" + mainId)); //$NON-NLS-1$
			this.mainId = mainId;
			}

		public boolean contains(Event e)
			{
			for (int i = 0; i < getChildCount(); i++)
				{
				if (getChildAt(i) instanceof EventInstanceNode)
					{
					if (((EventInstanceNode) getChildAt(i)).getUserObject().matchesType(e)) return true;
					}
				else if (((EventGroupNode) getChildAt(i)).contains(e)) return true;
				}
			return false;
			}

		@SuppressWarnings("unchecked")
		public void sortChildren()
			{
			// This doesn't seem to have any unwanted effects (directly sorting the protected field)
			Collections.sort((List)children);
			}

		public TreePath childPath(Event e)
			{
			for (int i = 0; i < getChildCount(); i++)
				if (getChildAt(i) instanceof EventInstanceNode
						&& ((EventInstanceNode) getChildAt(i)).getUserObject().matchesType(e))
					return new TreePath(((EventInstanceNode) getChildAt(i)).getPath());
			return null;
			}

		public void select(Event e)
			{
			TreePath p = childPath(e);
			events.setSelectionPath(p);
			events.updateUI();
			events.scrollPathToVisible(p);
			events.updateUI();
			}
		}

	public void addEvent(Event e)
		{
		for (int i = 0; i < rootEvent.getChildCount(); i++)
			{
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) rootEvent.getChildAt(i);
			if (n instanceof EventGroupNode)
				{
				EventGroupNode group = (EventGroupNode) n;
				if (group.mainId == e.mainId)
					{
					if (!group.contains(e))
						{
						group.add(new EventInstanceNode(e));
						group.sortChildren();
						}
					group.select(e);
					return;
					}
				}
			else
				{
				EventInstanceNode ein = (EventInstanceNode) n;
				if (ein.getUserObject().mainId == e.mainId)
					{
					if (!ein.getUserObject().matchesType(e))
						{
						EventGroupNode group = new EventGroupNode(e.mainId);
						int ind = rootEvent.getIndex(ein);
						rootEvent.remove(ind);
						rootEvent.insert(group,ind);
						group.add(ein);
						group.add(new EventInstanceNode(e));
						group.sortChildren();
						group.select(e);
						}
					else
						{
						rootEvent.select(e);
						}
					return;
					}
				}
			}
		for (int i = 0; i < rootEvent.getChildCount(); i++)
			{
			int mid;
			if (rootEvent.getChildAt(i) instanceof EventInstanceNode)
				mid = ((EventInstanceNode) rootEvent.getChildAt(i)).getUserObject().mainId;
			else
				mid = ((EventGroupNode) rootEvent.getChildAt(i)).mainId;
			if (mid > e.mainId)
				{
				EventInstanceNode ein = new EventInstanceNode(e);
				rootEvent.insert(ein,i);
				rootEvent.select(e);
				return;
				}
			}
		EventInstanceNode ein = new EventInstanceNode(e);
		rootEvent.add(ein);
		rootEvent.select(e);
		}

	public void removeEvent(EventInstanceNode n)
		{
		DefaultMutableTreeNode p = (DefaultMutableTreeNode) n.getParent();
		if (p == null) return;

		DefaultMutableTreeNode next = n.getNextSibling();
		if (next == null) next = n.getPreviousSibling();

		if (p == rootEvent)
			n.removeFromParent();
		else
			{
			if (p.getChildCount() < 3) //thunder
				{
				n.removeFromParent();
				rootEvent.insert((DefaultMutableTreeNode) p.getChildAt(0),rootEvent.getIndex(p));
				p.removeFromParent();
				}
			else
				n.removeFromParent();
			}

		if (next == null && rootEvent.getChildCount() != 0)
			next = (DefaultMutableTreeNode) rootEvent.getChildAt(0);
		if (next == null)
			actions.setActionContainer(null);
		else
			{
			TreePath path = new TreePath((next instanceof EventInstanceNode ? next
					: (DefaultMutableTreeNode) next.getChildAt(0)).getPath());
			events.setSelectionPath(path);
			events.scrollPathToVisible(path);
			}
		events.updateUI();
		}

	public void functionEvent(int mainId, int id, ResourceReference<GmObject> other, TreePath path)
		{
		if (path == null)
			{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) events.getLastSelectedPathComponent();
			path = node == null ? null : new TreePath(node.getPath());
			}
		int func = path == null ? EventPanel.FUNCTION_ADD : LGM.eventSelect.function.getValue();

		switch (func)
			{
			case EventPanel.FUNCTION_ADD:

				addEvent(new Event(mainId,id,other));
				break;
			case EventPanel.FUNCTION_REPLACE:
				DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (!(dropNode instanceof EventInstanceNode) || dropNode.getParent() == null) return;
				EventInstanceNode drop = (EventInstanceNode) dropNode;
				removeEvent(drop);
				Event ev = drop.getUserObject();
				ev.mainId = mainId;
				ev.id = id;
				ev.other = other;
				addEvent(ev);
				break;
			case EventPanel.FUNCTION_DUPLICATE:
				dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (!(dropNode instanceof EventInstanceNode) || dropNode.getParent() == null) return;
				drop = (EventInstanceNode) dropNode;
				ev = drop.getUserObject();
				actions.save();
				Event ev2 = ev.copy();
				ev2.mainId = mainId;
				ev2.id = id;
				ev2.other = other;
				addEvent(ev2);
				break;
			}
		}

	public void makeEventTree(GmObject res)
		{
		rootEvent = new EventGroupNode(-1);
		for (int m = 0; m < 12; m++)
			{
			MainEvent me = res.mainEvents.get(m);
			List<Event> ale = me.events;
			if (ale.size() == 1)
				{
				rootEvent.add(new EventInstanceNode(ale.get(0)));
				}
			if (ale.size() > 1)
				{
				EventGroupNode node = new EventGroupNode(m);
				rootEvent.add(node);
				for (Event e : ale)
					node.add(new EventInstanceNode(e));
				}
			}
		if (res.getNode().newRes && rootEvent.getChildCount() == 0)
			{
			rootEvent.add(new EventInstanceNode(new Event(MainEvent.EV_CREATE,0,null)));
			rootEvent.add(new EventInstanceNode(new Event(MainEvent.EV_STEP,Event.EV_STEP_NORMAL,null)));
			rootEvent.add(new EventInstanceNode(new Event(MainEvent.EV_DRAW,0,null)));
			}
		events = new EventTree(rootEvent);
		events.setScrollsOnExpand(true);
		events.setCellRenderer(new EventNodeRenderer());
		events.setRootVisible(false);
		events.setShowsRootHandles(true);
		events.setExpandsSelectedPaths(false);
		events.addMouseListener(mListener);
		events.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		events.addTreeSelectionListener(this);
		if (LGM.javaVersion >= 10600)
			{
			events.setDragEnabled(true);
			events.setDropMode(DropMode.ON);
			events.setTransferHandler(new EventNodeTransferHandler());
			}
		// This listener should be added to each node maybe
		// otherwise you can click on the whitespace and open it
		// but then again I suppose its fine like this because I have
		// ensured checks to make sure there are no NPE's trying to edit
		// an event that don't exist. Meh, this is fine as is.
		MouseListener ml = new MouseAdapter()
			{
				public void mousePressed(MouseEvent e)
					{
					if (e.getClickCount() == 2)
						{
						editSelectedEvent();
						}
					}
			};
		events.addMouseListener(ml);
		}

	public void showInfoFrame()
		{
		// NOTE: This does affect reverting the resource, just makes it so
		// the info frame can use the up to date version.
		saveEvents();

		if (infoFrame == null)
			{
			infoFrame = new ResourceInfoFrame();
			}
		infoFrame.updateObjectInfo(res.reference);
		infoFrame.setVisible(true);
		}

	public void saveEvents()
		{
		actions.save();
		Enumeration<?> dfe = rootEvent.depthFirstEnumeration();
		for (MainEvent me : res.mainEvents)
			me.events.clear();
		while (dfe.hasMoreElements())
			{
			Object o = dfe.nextElement();
			if (o instanceof EventInstanceNode)
				{
				EventInstanceNode ein = (EventInstanceNode) o;
				if (!ein.getUserObject().actions.isEmpty())
					{
					Event e = ein.getUserObject();
					res.mainEvents.get(e.mainId).events.add(e);
					}
				}
			}
		}

	protected boolean areResourceFieldsEqual()
		{
		return Util.areInherentlyUniquesEqual(res.mainEvents,resOriginal.mainEvents);
		}

	public void commitChanges()
		{
		saveEvents();
		res.setName(name.getText());
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == newSprite)
			{
			ResNode n = Listener.getPrimaryParent(Sprite.class);
			Sprite spr = LGM.currentFile.resMap.getList(Sprite.class).add();
			Listener.putNode(LGM.tree,n,n,Sprite.class,n.getChildCount(),spr);
			res.put(PGmObject.SPRITE,spr.reference);
			return;
			}
		if (e.getSource() == editSprite)
			{
			Sprite spr = deRef(sprite.getSelected());
			if (spr == null) return;
			spr.getNode().openFrame();
			return;
			}
		if (e.getSource() == information)
			{
			showInfoFrame();
			return;
			}
		if (e.getSource() == eventModify || e.getSource() == eventModifyItem)
			{
			LGM.showEventPanel();
			return;
			}
		if (e.getSource() == eventAddItem)
			{
			LGM.showEventPanel();
			LGM.eventSelect.function.setValue(EventPanel.FUNCTION_ADD);
			return;
			}
		if (e.getSource() == eventReplaceItem)
			{
			LGM.showEventPanel();
			LGM.eventSelect.function.setValue(EventPanel.FUNCTION_REPLACE);
			return;
			}
		if (e.getSource() == eventDuplicateItem)
			{
			LGM.showEventPanel();
			LGM.eventSelect.function.setValue(EventPanel.FUNCTION_DUPLICATE);
			return;
			}
		if (e.getSource() == eventEdit || e.getSource() == eventEditItem)
			{
			editSelectedEvent();
			return;
			}
		if (e.getSource() == eventDelete || e.getSource() == eventDeleteItem)
			{
			DefaultMutableTreeNode comp = (DefaultMutableTreeNode) events.getLastSelectedPathComponent();
			if (!isValidEventInstanceNode(comp)) return;
			removeEvent((EventInstanceNode) comp);
			return;
			}
		super.actionPerformed(e);
		}

	private TreeNode[] findEvent(DefaultMutableTreeNode node, int mainid, int id) {
		Enumeration<?> children = node.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			if (child instanceof EventInstanceNode) {
				EventInstanceNode evtNode = (EventInstanceNode) child;
				Event evt = evtNode.getUserObject();
				if (evt.mainId == mainid && evt.id == id) {
					return evtNode.getPath();
				}
			} else if (child instanceof EventGroupNode) {
				TreeNode[] ret = findEvent(child, mainid, id);
				if (ret != null) return ret;
			}
		}
		return null;
	}

	private TreeNode[] findEvent(int mainid, int id) {
		return findEvent((DefaultMutableTreeNode) events.getModel().getRoot(), mainid, id);
	}

	public void setSelectedEvent(int mainid, int id) {
		TreeNode[] nodes = findEvent(mainid, id);
		if (nodes != null) {
			TreePath path = new TreePath(nodes);
			// TODO: Why does this not work? I tried wrapping it in SwingUtilities as well as reloading
			// the tree model.
			//events.expandPath(path);
			// Using this temporarily.
			events.setExpandsSelectedPaths(true);
			events.setSelectionPath(path);
		}
	}

	private void editSelectedEvent()
		{
		if (events.getModel().getChildCount(events.getModel().getRoot()) == 0)
			{
			return;
			}
		Action a = null;
		LibAction la = null;
		Boolean prependNew = true;

		for (int i = 0; i < actions.model.list.size(); i++)
			{
			a = actions.model.list.get(i);
			la = a.getLibAction();
			if (la.actionKind == Action.ACT_CODE)
				{
				prependNew = false;
				break;
				}
			}

		if (prependNew)
			{
			a = new Action(LibManager.codeAction);
			((ActionListModel) actions.getModel()).add(0,a);
			actions.setSelectedValue(a,true);
			}

		MDIFrame af = ActionList.openActionFrame(actions.parent.get(),a);
		EventInstanceNode evnode = (EventInstanceNode) events.getLastSelectedPathComponent();
		af.setTitle(this.name.getText() + " : " + evnode.toString());
		af.setFrameIcon(LGM.getIconForKey("EventNode.EVENT" + evnode.getUserObject().mainId));
		return;
		}

	/**
	 * Check if a node is non-null, is an instance of EventInstanceNode, and that it has a parent and
	 * still exists in the events tree. This is useful with
	 * {@link javax.swing.JTree#getLastSelectedPathComponent() getLastSelectedPathComponent()} or
	 * {@link javax.swing.tree.TreePath#getLastPathComponent() getLastPathComponent()} because they
	 * can return nodes already removed from the tree.
	 *
	 * @param node The node to check for validity.
	 *
	 * @return Whether the node is valid.
	 */
	private static boolean isValidEventInstanceNode(Object node)
		{
		return (node != null && node instanceof EventInstanceNode
				&& ((EventInstanceNode) node).getParent() != null);
		}

	@Override
	public void dispose()
		{
		super.dispose();
		events.removeTreeSelectionListener(this);
		events.setModel(null);
		events.setTransferHandler(null);
		information.removeActionListener(this);
		newSprite.removeActionListener(this);
		editSprite.removeActionListener(this);
		eventModify.removeActionListener(this);
		eventEdit.removeActionListener(this);
		eventDelete.removeActionListener(this);
		if (infoFrame != null)
			{
			infoFrame.dispose();
			}
		res.properties.updateSource.removeListener(propUpdateListener);
		if (editor != null)
			{
			((ActionListEditor) editor).dispose();
			}
		}

	public void valueChanged(TreeSelectionEvent tse)
		{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) events.getLastSelectedPathComponent();
		if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof Event))
			{
			if (node != null && !node.isLeaf() && node.getParent() != null)
				{
				TreePath path = new TreePath(node.getPath());
				if (events.isExpanded(path))
					events.collapsePath(path);
				else
					events.expandPath(path);
				}
			if (lastValidEventSelection != null)
				events.setSelectionPath(new TreePath(lastValidEventSelection.getPath()));
			return;
			}
		lastValidEventSelection = node;
		actions.setActionContainer((Event) node.getUserObject());
		}

	@Override
	public void exceptionThrown(Exception e)
		{
		if (e instanceof ParentLoopException)
			{
			String msg = Messages.getString("GmObjectFrame.LOOPING_PARENTS"); //$NON-NLS-1$
			String ttl = Messages.getString("GmObjectFrame.WARNING"); //$NON-NLS-1$
			JOptionPane.showMessageDialog(this,msg,ttl,JOptionPane.WARNING_MESSAGE);
			return;
			}
		super.exceptionThrown(e);
		}

	@Override
	public Dimension getMinimumSize()
		{
		Dimension p = getContentPane().getSize();
		Dimension l = getContentPane().getMinimumSize();
		Dimension s = getSize();
		l.width += s.width - p.width;
		l.height += s.height - p.height;
		return l;
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

	private class MListener extends MouseAdapter
		{
		public void mouseReleased(MouseEvent e)
			{
			if (e.getSource() != events) return;
			int button = e.getButton();
			if (button == MouseEvent.BUTTON3)
				{
				TreePath path = events.getPathForLocation(e.getX(),e.getY());
				if (path == null) return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.isLeaf())
					{
					lastValidEventSelection = node;
					actions.setActionContainer((Event) node.getUserObject());
					events.setSelectionPath(path);

					JPopupMenu menu = new JPopupMenu();
					eventModifyItem = new JMenu(Messages.getString("GmObjectFrame.MODIFY")); //$NON-NLS-1
					menu.add(eventModifyItem);
					eventModifyItem.addActionListener(GmObjectFrame.this);

					eventAddItem = new JMenuItem(Messages.getString("GmObjectFrame.ADD")); //$NON-NLS-1
					eventModifyItem.add(eventAddItem);
					eventAddItem.addActionListener(GmObjectFrame.this);
					eventReplaceItem = new JMenuItem(Messages.getString("GmObjectFrame.REPLACE")); //$NON-NLS-1
					eventModifyItem.add(eventReplaceItem);
					eventReplaceItem.addActionListener(GmObjectFrame.this);
					eventDuplicateItem = new JMenuItem(Messages.getString("GmObjectFrame.DUPLICATE")); //$NON-NLS-1
					eventModifyItem.add(eventDuplicateItem);
					eventDuplicateItem.addActionListener(GmObjectFrame.this);

					eventEditItem = new JMenuItem(Messages.getString("GmObjectFrame.EDIT")); //$NON-NLS-1
					menu.add(eventEditItem);
					eventEditItem.addActionListener(GmObjectFrame.this);
					eventDeleteItem = new JMenuItem(Messages.getString("GmObjectFrame.DELETE")); //$NON-NLS-1
					menu.add(eventDeleteItem);
					eventDeleteItem.addActionListener(GmObjectFrame.this);
					menu.show(e.getComponent(),e.getX(),e.getY());
					}
				}
			}
		}
	}
