/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.ActionList;
import org.lateralgm.components.ActionListEditor;
import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.IntegerField;
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
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.EventFrame.EventNodeRenderer;

public class GmObjectFrame extends ResourceFrame<GmObject> implements ActionListener,
		TreeSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon INFO_ICON = LGM.getIconForKey("GmObjectFrame.INFO"); //$NON-NLS-1$

	public ResourceMenu<Sprite> sprite;
	public JButton newSprite;
	public JButton editSprite;
	public JCheckBox visible;
	public JCheckBox solid;
	public IntegerField depth;
	public JCheckBox persistent;
	public ResourceMenu<GmObject> parent;
	public ResourceMenu<Sprite> mask;
	public JButton information;

	public EventTree events;
	public JButton eventDelete;
	public EventGroupNode rootEvent;
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
		side2.add(lab,"North"); //$NON-NLS-1$
		makeEventTree(res);
		JScrollPane scroll = new JScrollPane(events);
		scroll.setPreferredSize(new Dimension(140,260));
		side2.add(scroll,"Center"); //$NON-NLS-1$

		JPanel side2bottom = new JPanel(new BorderLayout());
		//I probably shouldn't refer to LGM.makeButton, but it suits my purpose well here
		side2bottom.add(LGM.makeButton("LGM.EVENT_BUTTON"),"West"); //$NON-NLS-1$ //$NON-NLS-2$
		eventDelete = new JButton(Messages.getString("GmObjectFrame.DELETE")); //$NON-NLS-1$
		eventDelete.addActionListener(this);
		side2bottom.add(eventDelete,"Center"); //$NON-NLS-1$
		side2.add(side2bottom,"South"); //$NON-NLS-1$

		//		side2.add(deleteEvent,"South"); //$NON-NLS-1$

		JComponent editor;
		if (false)
			{
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
		sprite = new ResourceMenu<Sprite>(Resource.SPRITE,t,144);
		sprite.setSelected(res.sprite);
		sprite.addActionListener(this);
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
		/**/.addGroup(oLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(newSprite)
		/*		*/.addComponent(editSprite))
		/**/.addContainerGap(4,4));

		visible = new JCheckBox(Messages.getString("GmObjectFrame.VISIBLE"),res.visible); //$NON-NLS-1$
		solid = new JCheckBox(Messages.getString("GmObjectFrame.SOLID"),res.solid); //$NON-NLS-1$
		JLabel dLabel = new JLabel(Messages.getString("GmObjectFrame.DEPTH")); //$NON-NLS-1$
		depth = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.depth);
		depth.setColumns(8);
		persistent = new JCheckBox(Messages.getString("GmObjectFrame.PERSISTENT")); //$NON-NLS-1$
		persistent.setSelected(res.persistent);
		JLabel pLabel = new JLabel(Messages.getString("GmObjectFrame.PARENT")); //$NON-NLS-1$
		t = Messages.getString("GmObjectFrame.NO_PARENT"); //$NON-NLS-1$
		parent = new ResourceMenu<GmObject>(Resource.GMOBJECT,t,110);
		parent.setSelected(res.parent);
		parent.addActionListener(this);
		JLabel mLabel = new JLabel(Messages.getString("GmObjectFrame.MASK")); //$NON-NLS-1$
		t = Messages.getString("GmObjectFrame.SAME_AS_SPRITE"); //$NON-NLS-1$
		mask = new ResourceMenu<Sprite>(Resource.SPRITE,t,110);
		mask.setSelected(res.mask);
		information = new JButton(Messages.getString("GmObjectFrame.INFO"),INFO_ICON); //$NON-NLS-1$
		information.addActionListener(this);
		save.setText(Messages.getString("GmObjectFrame.SAVE")); //$NON-NLS-1$

		s1Layout.setHorizontalGroup(s1Layout.createParallelGroup()
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(origin)
		/**/.addComponent(persistent)
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(visible)
		/*		*/.addComponent(solid))
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(dLabel)
		/*		*/.addComponent(depth))
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.LEADING)
		/*		*/.addComponent(pLabel)
		/*		*/.addComponent(mLabel))
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addGap(16)
		/*		*/.addGroup(s1Layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(parent,DEFAULT_SIZE,120,MAX_VALUE)
		/*				*/.addComponent(mask,DEFAULT_SIZE,120,MAX_VALUE)))
		/**/.addComponent(information,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		s1Layout.setVerticalGroup(s1Layout.createSequentialGroup()
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name))
		/**/.addComponent(origin)
		/**/.addComponent(persistent)
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(visible)
		/*		*/.addComponent(solid))
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(dLabel)
		/*		*/.addComponent(depth))
		/**/.addComponent(pLabel)
		/**/.addGap(2)
		/**/.addComponent(parent,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addComponent(mLabel)
		/**/.addGap(2)
		/**/.addComponent(mask,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
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
				Point p = support.getDropLocation().getDropPoint();
				TreePath path = events.getPathForLocation(p.x,p.y);
				int func = path == null ? EventFrame.FUNCTION_ADD : LGM.eventSelect.function.getValue();

				switch (func)
					{
					case EventFrame.FUNCTION_ADD:
						if (!t.isValid()) return false;
						addEvent(new Event(t.mainId,t.eventId,t.other));
						return true;
					case EventFrame.FUNCTION_REPLACE:
						DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
						if (!(dropNode instanceof EventInstanceNode) || !t.isValid()) return false;
						EventInstanceNode drop = (EventInstanceNode) dropNode;
						removeEvent(drop);
						Event ev = drop.getUserObject();
						ev.mainId = t.mainId;
						ev.id = t.eventId;
						ev.other = t.other;
						addEvent(ev);
						return true;
					case EventFrame.FUNCTION_DUPLICATE:
						dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
						if (!(dropNode instanceof EventInstanceNode) || !t.isValid()) return false;
						drop = (EventInstanceNode) dropNode;
						ev = drop.getUserObject();
						actions.save();
						Event ev2 = ev.copy();
						ev2.mainId = t.mainId;
						ev2.id = t.eventId;
						ev2.other = t.other;
						addEvent(ev2);
						return true;
					}
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
		if (n.getParent() == rootEvent)
			n.removeFromParent();
		else
			{
			if (n.getParent().getChildCount() < 3) //thunder
				{
				EventGroupNode p = (EventGroupNode) n.getParent();
				n.removeFromParent();
				rootEvent.insert((DefaultMutableTreeNode) p.getChildAt(0),rootEvent.getIndex(p));
				p.removeFromParent();
				}
			else
				n.removeFromParent();
			}
		if (rootEvent.getChildCount() == 0)
			actions.setActionContainer(null);
		else
			{
			DefaultMutableTreeNode first = (DefaultMutableTreeNode) rootEvent.getChildAt(0);
			TreePath path = new TreePath((first instanceof EventInstanceNode ? first
					: (DefaultMutableTreeNode) first.getChildAt(0)).getPath());
			events.setSelectionPath(path);
			events.scrollPathToVisible(path);
			}
		events.updateUI();
		}

	public void makeEventTree(GmObject res)
		{
		rootEvent = new EventGroupNode(-1);
		for (int m = 0; m < 11; m++)
			{
			MainEvent me = res.mainEvents[m];
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
		if (rootEvent.getChildCount() == 0)
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
					res.mainEvents[e.mainId].events.add(e);
					}
				}
			}
		}

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		ResourceComparator c = new ResourceComparator();
		c.addExclusions(Action.class,"updateTrigger","updateSource");
		c.addExclusions(Argument.class,"updateTrigger","updateSource");
		return !c.areEqual(res,resOriginal);
		}

	@Override
	public void revertResource()
		{
		resOriginal.updateReference();
		LGM.currentFile.gmObjects.replace(res,resOriginal);
		}

	public void commitChanges()
		{
		saveEvents();
		res.setName(name.getText());
		res.sprite = sprite.getSelected();
		res.visible = visible.isSelected();
		res.solid = solid.isSelected();
		res.depth = depth.getIntValue();
		res.persistent = persistent.isSelected();
		res.parent = parent.getSelected();
		res.mask = mask.getSelected();
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == newSprite)
			{
			ResNode n = Listener.getPrimaryParent(Resource.SPRITE);
			Sprite spr = LGM.currentFile.sprites.add();
			Listener.putNode(LGM.tree,n,n,Resource.SPRITE,n.getChildCount(),spr);
			sprite.setSelected(spr.reference);
			return;
			}
		if (e.getSource() == editSprite)
			{
			Sprite spr = deRef(sprite.getSelected());
			if (spr == null) return;
			spr.getNode().openFrame();
			return;
			}
		if (e.getSource() == sprite)
			{
			// TODO
			return;
			}
		if (e.getSource() == parent)
			{
			ResourceReference<GmObject> p = parent.getSelected();
			res.parent = p;
			if (deRef(p) != null) if (isCyclic(res))
				{
				String msg = Messages.getString("GmObjectFrame.LOOPING_PARENTS"); //$NON-NLS-1$
				String ttl = Messages.getString("GmObjectFrame.ERROR"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(this,msg,ttl,JOptionPane.ERROR_MESSAGE);
				parent.setSelected(null);
				res.parent = null;
				}
			return;
			}
		if (e.getSource() == eventDelete)
			{
			Object comp = events.getLastSelectedPathComponent();
			if (!(comp instanceof EventInstanceNode)) return;
			removeEvent((EventInstanceNode) comp);
			return;
			}
		super.actionPerformed(e);
		}

	private boolean isCyclic(GmObject inheritor)
		{
		ArrayList<GmObject> traversed = new ArrayList<GmObject>();
		traversed.add(inheritor);
		while (deRef(inheritor.parent) != null)
			{
			GmObject p = deRef(inheritor.parent);
			if (traversed.contains(p)) return true;
			inheritor = p;
			traversed.add(inheritor);
			}
		return false;
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
		eventDelete.removeActionListener(this);
		sprite.removeActionListener(this);
		mask.removeActionListener(this);
		parent.removeActionListener(this);
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
	public Dimension getMinimumSize()
		{
		Dimension p = getContentPane().getSize();
		Dimension l = getContentPane().getMinimumSize();
		Dimension s = getSize();
		l.width += s.width - p.width;
		l.height += s.height - p.height;
		return l;
		}
	}
