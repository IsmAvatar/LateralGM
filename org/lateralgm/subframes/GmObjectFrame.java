/*
 * Copyright (C) 2007, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008, 2009 Quadduc <quadduc@gmail.com>
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
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.ActionList;
import org.lateralgm.components.ActionListEditor;
import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.EventNode;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.GmObject.ParentLoopException;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

import com.sun.corba.se.spi.orbutil.fsm.Action;

public class GmObjectFrame extends ResourceFrame<GmObject,PGmObject> implements ActionListener,
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
	public JButton eventAdd;
	public JButton eventReplace;
	public JButton eventDuplicate;
	public JButton eventDelete;
	public JMenuItem eventAddItem;
	public JMenuItem eventReplaceItem;
	public JMenuItem eventDuplicateItem;
	public JMenuItem eventDeleteItem;
	public EventGroupNode rootEvent;
	private MListener mListener = new MListener();
	public ActionList actions;
	public GMLTextArea code;

	private DefaultMutableTreeNode lastValidEventSelection;

	public GmObjectFrame(GmObject res, ResNode node)
		{
		super(res,node);

		GroupLayout layout = new GroupLayout(getContentPane());
		setLayout(layout);

		JPanel side1 = new JPanel();
		makeSide1(side1);

		JPanel side2 = new JPanel(new BorderLayout());
		JLabel lab = new JLabel(Messages.getString("GmObjectFrame.EVENTS")); //$NON-NLS-1$
		side2.add(lab,BorderLayout.NORTH);
		makeEventTree(res);
		JScrollPane scroll = new JScrollPane(events);
		scroll.setPreferredSize(new Dimension(140,260));
		side2.add(scroll,BorderLayout.CENTER);

		JPanel side2bottom = new JPanel(new BorderLayout());
		eventAdd = new JButton(Messages.getString("GmObjectFrame.ADD_EVENT")); //$NON-NLS-1
		eventAdd.addActionListener(this);
		eventReplace = new JButton(Messages.getString("GmObjectFrame.REP")); //$NON-NLS-1
		eventReplace.addActionListener(this);
		eventReplace.setToolTipText(Messages.getString("GmObjectFrame.REPLACE_EVENT")); //$NON-NLS-1$
		eventReplace.setMargin(new Insets(1,7,1,7));
		eventDuplicate = new JButton(Messages.getString("GmObjectFrame.DUP")); //$NON-NLS-1
		eventDuplicate.addActionListener(this);
		eventDuplicate.setToolTipText(Messages.getString("GmObjectFrame.DUPLICATE_EVENT")); //$NON-NLS-1$
		eventDuplicate.setMargin(new Insets(1,7,1,7));
		eventDelete = new JButton(Messages.getString("GmObjectFrame.DELETE")); //$NON-NLS-1$
		eventDelete.addActionListener(this);
		eventDelete.setToolTipText(Messages.getString("GmObjectFrame.DELETE_EVENT")); //$NON-NLS-1$
		eventDelete.setMargin(new Insets(2,0,2,0));
		side2bottom.add(eventAdd,BorderLayout.NORTH);
		side2bottom.add(eventReplace,BorderLayout.WEST);
		side2bottom.add(eventDuplicate,BorderLayout.EAST);
		side2bottom.add(eventDelete,BorderLayout.CENTER);
		side2.add(side2bottom,BorderLayout.SOUTH);

		JComponent editor;
		if (false)
			{
			//TODO
			code = new GMLTextArea(""); //$NON-NLS-1$
			editor = new JScrollPane(code);
			}
		else
			{
			actions = new ActionList(this);
			editor = new ActionListEditor(actions);
			}

		layout.setHorizontalGroup(layout.createSequentialGroup()
		/**/.addComponent(side1,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addComponent(side2)
		/**/.addComponent(editor));
		layout.setVerticalGroup(layout.createParallelGroup()
		/**/.addComponent(side1)
		/**/.addComponent(side2)
		/**/.addComponent(editor));

		pack();

		// Select first event
		TreeNode event = (TreeNode) events.getModel().getRoot();
		while (event.getChildCount() > 0)
			event = event.getChildAt(0);
		if (event != events.getModel().getRoot())
			events.setSelectionPath(new TreePath(((DefaultMutableTreeNode) event).getPath()));
		}

	private void makeSide1(JPanel side1)
		{
		GroupLayout s1Layout = new GroupLayout(side1);
		s1Layout.setAutoCreateContainerGaps(true);
		s1Layout.setAutoCreateGaps(true);
		side1.setLayout(s1Layout);

		JLabel nLabel = new JLabel(Messages.getString("GmObjectFrame.NAME")); //$NON-NLS-1$

		JPanel origin = new JPanel();
		GroupLayout oLayout = new GroupLayout(origin);
		origin.setLayout(oLayout);
		origin.setBorder(BorderFactory.createTitledBorder(Messages.getString("GmObjectFrame.SPRITE"))); //$NON-NLS-1$
		String t = Messages.getString("GmObjectFrame.NO_SPRITE"); //$NON-NLS-1$
		sprite = new ResourceMenu<Sprite>(Resource.Kind.SPRITE,t,144);
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
		JLabel pLabel = new JLabel(Messages.getString("GmObjectFrame.PARENT")); //$NON-NLS-1$
		t = Messages.getString("GmObjectFrame.NO_PARENT"); //$NON-NLS-1$
		parent = new ResourceMenu<GmObject>(Resource.Kind.OBJECT,t,110);
		plf.make(parent,PGmObject.PARENT);
		JLabel mLabel = new JLabel(Messages.getString("GmObjectFrame.MASK")); //$NON-NLS-1$
		t = Messages.getString("GmObjectFrame.SAME_AS_SPRITE"); //$NON-NLS-1$
		mask = new ResourceMenu<Sprite>(Resource.Kind.SPRITE,t,110);
		plf.make(mask,PGmObject.MASK);
		information = new JButton(Messages.getString("GmObjectFrame.INFO"),INFO_ICON); //$NON-NLS-1$
		information.addActionListener(this);
		save.setText(Messages.getString("GmObjectFrame.SAVE")); //$NON-NLS-1$

		s1Layout.setHorizontalGroup(s1Layout.createParallelGroup()
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(origin)
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(dLabel)
		/*		*/.addComponent(depth))
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.LEADING)
		/*		*/.addComponent(pLabel)
		/*		*/.addComponent(mLabel))
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addGroup(s1Layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(parent,DEFAULT_SIZE,120,MAX_VALUE)
		/*				*/.addComponent(mask,DEFAULT_SIZE,120,MAX_VALUE)))
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(visible)
		/*		*/.addComponent(solid))
		/**/.addComponent(persistent)
		/**/.addComponent(information,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));

		s1Layout.setVerticalGroup(s1Layout.createSequentialGroup()
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name))
		/**/.addComponent(origin)
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(dLabel)
		/*		*/.addComponent(depth))
		/**/.addComponent(pLabel)
		/**/.addGap(4)
		/**/.addComponent(parent,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addComponent(mLabel)
		/**/.addGap(4)
		/**/.addComponent(mask,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addGap(8)
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(visible)
		/*		*/.addComponent(solid))
		/**/.addComponent(persistent)
		/**/.addGap(8,8,MAX_VALUE)
		/**/.addComponent(information)
		/**/.addComponent(save));
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
			if (!support.isDataFlavorSupported(EventNode.EVENTNODE_FLAVOR)) return false;
			EventNode t = (EventNode) LGM.eventSelect.events.getLastSelectedPathComponent();
			if (t == null || !t.isValid()) return false;
			if (rootEvent.contains(new Event(t.mainId,t.eventId,t.other))) return false;
			for (DataFlavor f : support.getDataFlavors())
				if (f == EventNode.EVENTNODE_FLAVOR) return true;
			return false;
			}

		public boolean importData(TransferHandler.TransferSupport support)
			{
			if (!canImport(support)) return false;
			try
				{
				EventNode t = (EventNode) support.getTransferable().getTransferData(
						EventNode.EVENTNODE_FLAVOR);
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
			Collections.sort((java.util.Vector<EventInstanceNode>) children);
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
		int func = path == null ? EventFrame.FUNCTION_ADD : LGM.eventSelect.function.getValue();

		switch (func)
			{
			case EventFrame.FUNCTION_ADD:
				addEvent(new Event(mainId,id,other));
				break;
			case EventFrame.FUNCTION_REPLACE:
				DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (!(dropNode instanceof EventInstanceNode)) return;
				EventInstanceNode drop = (EventInstanceNode) dropNode;
				removeEvent(drop);
				Event ev = drop.getUserObject();
				ev.mainId = mainId;
				ev.id = id;
				ev.other = other;
				addEvent(ev);
				break;
			case EventFrame.FUNCTION_DUPLICATE:
				dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (!(dropNode instanceof EventInstanceNode)) return;
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
			ArrayList<Event> ale = me.events;
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
		if (res.getNode().newRes)
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
		events.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		events.addTreeSelectionListener(this);
		if (LGM.javaVersion >= 10600)
			{
			events.setDragEnabled(true);
			events.setDropMode(DropMode.ON);
			events.setTransferHandler(new EventNodeTransferHandler());
			}
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
				if (ein.getUserObject().actions.size() > 0)
					{
					Event e = ein.getUserObject();
					res.mainEvents.get(e.mainId).events.add(e);
					}
				}
			}
		}

	protected boolean areResourceFieldsEqual()
		{
		ResourceComparator c = new ResourceComparator();
		c.addExclusions(Action.class,"updateTrigger","updateSource");
		c.addExclusions(Argument.class,"updateTrigger","updateSource");
		return c.areEqual(res.mainEvents,resOriginal.mainEvents);
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
			ResNode n = Listener.getPrimaryParent(Resource.Kind.SPRITE);
			Sprite spr = LGM.currentFile.sprites.add();
			Listener.putNode(LGM.tree,n,n,Resource.Kind.SPRITE,n.getChildCount(),spr);
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
			//TODO: Object Information
			return;
			}
		if (e.getSource() == eventAdd || e.getSource() == eventAddItem)
			{
			LGM.eventSelect.show();
			LGM.eventSelect.toTop();
			LGM.eventSelect.function.setValue(EventFrame.FUNCTION_ADD);
			return;
			}
		if (e.getSource() == eventReplace || e.getSource() == eventReplaceItem)
			{
			LGM.eventSelect.show();
			LGM.eventSelect.toTop();
			LGM.eventSelect.function.setValue(EventFrame.FUNCTION_REPLACE);
			return;
			}
		if (e.getSource() == eventDuplicate || e.getSource() == eventDuplicateItem)
			{
			LGM.eventSelect.show();
			LGM.eventSelect.toTop();
			LGM.eventSelect.function.setValue(EventFrame.FUNCTION_DUPLICATE);
			return;
			}
		if (e.getSource() == eventDelete || e.getSource() == eventDeleteItem)
			{
			Object comp = events.getLastSelectedPathComponent();
			if (!(comp instanceof EventInstanceNode)) return;
			removeEvent((EventInstanceNode) comp);
			return;
			}
		super.actionPerformed(e);
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
		eventAdd.removeActionListener(this);
		eventReplace.removeActionListener(this);
		eventDuplicate.removeActionListener(this);
		eventDelete.removeActionListener(this);
		}

	public void valueChanged(TreeSelectionEvent tse)
		{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) events.getLastSelectedPathComponent();
		if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof Event))
			{
			if (node != null && !node.isLeaf())
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
			String ttl = Messages.getString("GmObjectFrame.ERROR"); //$NON-NLS-1$
			JOptionPane.showMessageDialog(this,msg,ttl,JOptionPane.ERROR_MESSAGE);
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
					eventAddItem = new JMenuItem(Messages.getString("GmObjectFrame.ADD_EVENT")); //$NON-NLS-1
					menu.add(eventAddItem);
					eventAddItem.addActionListener(GmObjectFrame.this);
					eventReplaceItem = new JMenuItem(Messages.getString("GmObjectFrame.REPLACE_EVENT")); //$NON-NLS-1
					menu.add(eventReplaceItem);
					eventReplaceItem.addActionListener(GmObjectFrame.this);
					eventDuplicateItem = new JMenuItem(Messages.getString("GmObjectFrame.DUPLICATE_EVENT")); //$NON-NLS-1
					menu.add(eventDuplicateItem);
					eventDuplicateItem.addActionListener(GmObjectFrame.this);
					eventDeleteItem = new JMenuItem(Messages.getString("GmObjectFrame.DELETE_EVENT")); //$NON-NLS-1
					menu.add(eventDeleteItem);
					eventDeleteItem.addActionListener(GmObjectFrame.this);
					menu.show(e.getComponent(),e.getX(),e.getY());
					}
				}
			}
		}
	}
