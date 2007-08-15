/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.EventNode;
import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.components.visual.VTextIcon;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.library.Library;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class GmObjectFrame extends ResourceFrame<GmObject> implements ActionListener,
		TreeSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon INFO_ICON = LGM.getIconForKey("GmObjectFrame.INFO"); //$NON-NLS-1$

	public JLabel preview;
	public ResourceMenu sprite;
	public JComboBox sp2;
	public JButton newsprite;
	public JButton edit;
	public JCheckBox visible;
	public JCheckBox solid;
	public IntegerField depth;
	public JCheckBox persistent;
	public ResourceMenu parent;
	public ResourceMenu mask;
	public JButton information;

	public JTree events;
	public EventGroupNode rootEvent;
	public JList actions;
	public GMLTextArea code;

	private DefaultMutableTreeNode lastValidEventSelection;

	public GmObjectFrame(GmObject res, ResNode node)
		{
		super(res,node);

		setSize(560,400);
		setMinimumSize(new Dimension(560,400));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setFrameIcon(Resource.ICON[Resource.GMOBJECT]);

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setPreferredSize(new Dimension(180,280));

		JLabel lab = new JLabel(Messages.getString("GmObjectFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(50,14));
		side1.add(lab);
		name.setPreferredSize(new Dimension(110,20));
		side1.add(name);

		JPanel origin = new JPanel(new FlowLayout());
		String t = Messages.getString("GmObjectFrame.SPRITE"); //$NON-NLS-1$
		origin.setBorder(BorderFactory.createTitledBorder(t));
		origin.setPreferredSize(new Dimension(180,80));
		Sprite s = LGM.currentFile.sprites.get(res.sprite);
		preview = new JLabel(GmTreeGraphics.getSpriteIcon(s));
		preview.setPreferredSize(new Dimension(16,16));
		origin.add(preview);

		sprite = new ResourceMenu(Resource.SPRITE,"<no sprite>",144);
		sprite.setSelected(LGM.currentFile.sprites.get(res.sprite));
		origin.add(sprite);
		newsprite = new JButton(Messages.getString("GmObjectFrame.NEW")); //$NON-NLS-1$
		newsprite.setPreferredSize(new Dimension(80,20));
		newsprite.addActionListener(this);
		origin.add(newsprite);
		edit = new JButton(Messages.getString("GmObjectFrame.EDIT")); //$NON-NLS-1$
		edit.setPreferredSize(new Dimension(80,20));
		edit.addActionListener(this);
		origin.add(edit);
		side1.add(origin);

		visible = new JCheckBox(Messages.getString("GmObjectFrame.VISIBLE"),res.visible); //$NON-NLS-1$
		visible.setPreferredSize(new Dimension(80,20));
		side1.add(visible);
		solid = new JCheckBox(Messages.getString("GmObjectFrame.SOLID"),res.solid); //$NON-NLS-1$
		solid.setPreferredSize(new Dimension(80,20));
		side1.add(solid);

		lab = new JLabel(Messages.getString("GmObjectFrame.DEPTH")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(50,14));
		side1.add(lab);
		depth = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.depth);
		depth.setPreferredSize(new Dimension(110,20));
		side1.add(depth);

		addGap(side1,30,1);
		persistent = new JCheckBox(Messages.getString("GmObjectFrame.PERSISTENT")); //$NON-NLS-1$
		persistent.setSelected(res.persistent);
		persistent.setPreferredSize(new Dimension(100,20));
		side1.add(persistent);
		addGap(side1,30,1);

		lab = new JLabel(Messages.getString("GmObjectFrame.PARENT")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(50,14));
		side1.add(lab);
		parent = new ResourceMenu(Resource.GMOBJECT,"<no parent>",110);
		parent.setSelected(LGM.currentFile.gmObjects.get(res.parent));
		side1.add(parent);

		lab = new JLabel(Messages.getString("GmObjectFrame.MASK")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(50,14));
		side1.add(lab);
		mask = new ResourceMenu(Resource.SPRITE,"<same as sprite>",110);
		mask.setSelected(LGM.currentFile.sprites.get(res.mask));
		side1.add(mask);

		addGap(side1,160,4);

		information = new JButton(Messages.getString("GmObjectFrame.INFO"),INFO_ICON); //$NON-NLS-1$
		information.setPreferredSize(new Dimension(160,20));
		information.addActionListener(this);
		side1.add(information);

		addGap(side1,160,16);

		save.setPreferredSize(new Dimension(130,24));
		save.setText(Messages.getString("GmObjectFrame.SAVE")); //$NON-NLS-1$
		side1.add(save);

		JPanel side2 = new JPanel(new BorderLayout());
		side2.setMaximumSize(new Dimension(90,Integer.MAX_VALUE));
		lab = new JLabel(Messages.getString("GmObjectFrame.EVENTS")); //$NON-NLS-1$
		side2.add(lab,"North"); //$NON-NLS-1$
		makeEventTree(res);
		JScrollPane scroll = new JScrollPane(events);
		scroll.setPreferredSize(new Dimension(140,260));
		side2.add(scroll,"Center"); //$NON-NLS-1$

		add(side1);
		add(side2);

		if (false)
			{
			code = new GMLTextArea("");
			JScrollPane codePane = new JScrollPane(code);
			add(codePane);
			}
		else
			{
			actions = addActionPane(this);
			}

		// Select first event
		TreeNode event = (TreeNode) events.getModel().getRoot();
		while (event.getChildCount() > 0)
			event = event.getChildAt(0);
		if (event != events.getModel().getRoot())
			events.setSelectionPath(new TreePath(((DefaultMutableTreeNode) event).getPath()));
		}

	private class EventNodeTransferHandler extends TransferHandler
		{
		private static final long serialVersionUID = 1L;

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
			// This is a bad way to do it, but support.getTransferable() doesn't work...
			try
				{
				EventNode t = (EventNode) LGM.eventSelect.events.getLastSelectedPathComponent();
				Point p = support.getDropLocation().getDropPoint();
				TreePath path = events.getPathForLocation(p.x,p.y);
				if (!LGM.eventSelect.replace.isSelected() || path == null)
					{
					if (!t.isValid()) return false;
					addEvent(new Event(t.mainId,t.eventId,t.other));
					return true;
					}
				else
					{
					DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					if (!(dropNode instanceof EventInstanceNode) || !t.isValid()) return false;
					EventInstanceNode drop = (EventInstanceNode) dropNode;
					Event ev = drop.getUserObject();
					removeEvent(ev);
					ev.mainId = t.mainId;
					ev.id = t.eventId;
					ev.other = t.other;
					addEvent(ev);
					}
				}
			catch (Throwable e)
				{
				// This is just to stop the dnd system from silencing exceptions
				e.printStackTrace();
				}
			return true;
			}
		}

	private class EventInstanceNode extends DefaultMutableTreeNode implements
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

	private class EventGroupNode extends DefaultMutableTreeNode
		{
		private static final long serialVersionUID = 1L;
		public int mainId;

		public EventGroupNode(int mainId)
			{
			super(Messages.getString("MainEvent.EVENT" + mainId));
			this.mainId = mainId;
			}

		public boolean contains(Event e)
			{
			for (int i = 0; i < getChildCount(); i++)
				{
				if (getChildAt(i) instanceof EventInstanceNode)
					{
					if (((EventInstanceNode) getChildAt(i)).getUserObject().equals(e)) return true;
					}
				else if (((EventGroupNode) getChildAt(i)).contains(e)) return true;
				}
			return false;
			}

		public boolean checkAndRemove(Event e)
			{
			for (int i = 0; i < getChildCount(); i++)
				{
				if (((EventInstanceNode) getChildAt(i)).getUserObject().equals(e))
					{
					remove(i);
					return true;
					}
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
						&& ((EventInstanceNode) getChildAt(i)).getUserObject().equals(e))
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
					if (!ein.getUserObject().equals(e))
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

	public void removeEvent(Event e)
		{
		for (int i = 0; i < rootEvent.getChildCount(); i++)
			{
			if (rootEvent.getChildAt(i) instanceof EventInstanceNode)
				{
				if (((EventInstanceNode) rootEvent.getChildAt(i)).getUserObject().equals(e))
					{
					rootEvent.remove(i);
					return;
					}
				}
			if (rootEvent.getChildAt(i) instanceof EventGroupNode)
				{
				EventGroupNode group = (EventGroupNode) rootEvent.getChildAt(i);
				if (group.checkAndRemove(e))
					{
					if (group.getChildCount() == 1)
						{
						rootEvent.remove(i);
						rootEvent.insert((EventInstanceNode) group.getChildAt(0),i);
						}
					return;
					}
				}
			}
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
				EventGroupNode node = new EventGroupNode(m); //$NON-NLS-1$
				rootEvent.add(node);
				for (Event e : ale)
					node.add(new EventInstanceNode(e));
				}
			}
		events = new JTree(rootEvent);
		events.setScrollsOnExpand(true);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
			{
				private static final long serialVersionUID = 1L;

				public Component getTreeCellRendererComponent(JTree tree, Object val, boolean sel,
						boolean exp, boolean leaf, int row, boolean focus)
					{
					super.getTreeCellRendererComponent(tree,val,sel,exp,leaf,row,focus);
					return this;
					}
			};
		events.setCellRenderer(renderer);
		events.setRootVisible(false);
		events.setShowsRootHandles(true);
		events.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		events.addTreeSelectionListener(this);
		events.setDragEnabled(true);
		events.setDropMode(DropMode.ON);
		events.setTransferHandler(new EventNodeTransferHandler());
		}

	@SuppressWarnings("serial")
	public static JList addActionPane(JComponent container)
		{
		JPanel side3 = new JPanel(new BorderLayout());
		side3.setPreferredSize(new Dimension(50,319));
		JLabel lab = new JLabel(Messages.getString("TimelineFrame.ACTIONS")); //$NON-NLS-1$
		side3.add(lab,"North"); //$NON-NLS-1$
		JList list = new JList();
		final JList list2 = list;
		list.addMouseListener(new MouseAdapter()
			{
				private Hashtable<Action,MDIFrame> actionFrames = new Hashtable<Action,MDIFrame>();

				public void mouseClicked(MouseEvent e)
					{
					if (e.getClickCount() != 2) return;
					Object o = list2.getSelectedValue();
					if (o == null || !(o instanceof Action)) return;
					Action a = (Action) o;
					MDIFrame af = actionFrames.get(a);
					if (af == null || af.isClosed())
						{
						af = new ActionFrame(a);
						LGM.mdi.add(af);
						actionFrames.put(a,af);
						}
					af.setVisible(true);
					af.toFront();
					try
						{
						af.setIcon(false);
						af.setSelected(true);
						}
					catch (PropertyVetoException pve)
						{
						}
					}
			});
		list.setCellRenderer(new ListCellRenderer()
			{
				public String parse(String s, Action a)
					{
					String escape = "FrNw01234567"; //$NON-NLS-1$
					String ret = ""; //$NON-NLS-1$
					//s = s.replaceAll("\n","<br>");

					int k = 0;
					int p = s.indexOf("@"); //$NON-NLS-1$
					while (p != -1)
						{
						ret += s.substring(k,p);
						char c = s.charAt(p + 1);
						if (!escape.contains(String.valueOf(c)))
							{
							ret += "@"; //$NON-NLS-1$
							k = p + 1;
							p = s.indexOf("@",k); //$NON-NLS-1$
							continue;
							}
						if (c == 'F')
							{
							if (s.charAt(p + 2) == 'B' || s.charAt(p + 2) == 'I')
								p += 2;
							else
								ret += "@"; //$NON-NLS-1$
							k = p + 1;
							p = s.indexOf("@",k); //$NON-NLS-1$
							continue;
							}
						if (c == 'r' && a.relative) ret += Messages.getString("Action.RELATIVE"); //$NON-NLS-1$
						if (c == 'N' && a.not) ret += Messages.getString("Action.NOT"); //$NON-NLS-1$
						if (c == 'w' && !a.appliesTo.equals(GmObject.OBJECT_SELF))
							{
							if (a.appliesTo.equals(GmObject.OBJECT_OTHER))
								ret += Messages.getString("Action.APPLIES_OTHER"); //$NON-NLS-1$
							else
								{
								GmObject applies = LGM.currentFile.gmObjects.get(a.appliesTo);
								ret += String.format(Messages.getString("Action.APPLIES"), //$NON-NLS-1$
										applies == null ? a.appliesTo.toString() : applies.getName());
								}
							}
						if (c >= '0' && c < '8')
							{
							int arg = c - '0';
							if (arg >= a.arguments.length)
								ret += "0"; //$NON-NLS-1$
							else
								{
								Argument aa = a.arguments[arg];
								ret += aa.toString(a.libAction.libArguments[arg]);
								}
							}
						k = p + 2;
						p = s.indexOf("@",k); //$NON-NLS-1$
						}

					s = ret + s.substring(k);
					s = s.replaceAll("&","&amp;");
					s = s.replaceAll("<","&lt;");
					s = s.replaceAll(">","&gt;");
					s = s.replaceAll("\n","<br>");
					s = s.replaceAll("\\\\#","\n");
					s = s.replaceAll("#","<br>");
					s = s.replaceAll("\n","&#35;");
					s = s.replaceAll(" ","&nbsp;");

					return s;
					}

				public Component getListCellRendererComponent(JList list, Object cell, int index,
						boolean isSelected, boolean hasFocus)
					{
					final Action cellAction = (Action) cell;
					LibAction la = cellAction.libAction;
					JLabel l = new JLabel();
					ListModel lm = list.getModel();
					try
						{
						if (lm instanceof ActionListModel)
							l.setBorder(new EmptyBorder(1,2 + 8 * ((ActionListModel) lm).indents.get(index),1,2));
						}
					catch (IndexOutOfBoundsException e)
						{
						}
					if (isSelected)
						{
						l.setBackground(list.getSelectionBackground());
						l.setForeground(list.getSelectionForeground());
						}
					else
						{
						l.setBackground(list.getBackground());
						l.setForeground(list.getForeground());
						}
					l.setOpaque(true);
					if (la.actImage == null)
						{
						l.setText(Messages.getString("Action.UNKNOWN")); //$NON-NLS-1$
						return l;
						}
					l.setText(parse(la.listText,(Action) cell));
					if (la.listText.contains("@FB")) //$NON-NLS-1$
						l.setText("<b>" + l.getText());
					if (la.listText.contains("@FI")) //$NON-NLS-1$
						l.setText("<i>" + l.getText());
					l.setText("<html>" + l.getText());
					l.setIcon(new ImageIcon(Util.getTransparentIcon(la.actImage)));
					l.setToolTipText("<html>" + parse(la.hintText,(Action) cell));
					return l;
					}
			});
		JScrollPane scroll = new JScrollPane(list);
		side3.add(scroll,"Center"); //$NON-NLS-1$

		JTabbedPane side4 = GmObjectFrame.makeLibraryTabs();
		side4.setPreferredSize(new Dimension(140,319));
		container.add(side3);
		container.add(side4);
		return list;
		}

	private static JPanel makeLabelPane(String name)
		{
		JPanel lp = new JPanel(new GridLayout(0,3,0,0));
		Border mb = BorderFactory.createMatteBorder(1,0,0,0,new Color(184,207,229));
		Border tb = BorderFactory.createTitledBorder(mb,name);
		lp.setBorder(tb);
		return lp;
		}

	//possibly extract to some place like resources.library.LibManager
	public static JTabbedPane makeLibraryTabs()
		{
		JTabbedPane tp = new JTabbedPane(JTabbedPane.RIGHT);

		tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		JPanel lp = null;
		for (Library l : LibManager.libs)
			{
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
			for (LibAction la : l.libActions)
				{
				if (la.hidden || la.actionKind == Action.ACT_SEPARATOR) continue;
				if (la.advanced && !la.advanced) continue;
				JLabel b;
				if (la.actionKind == Action.ACT_LABEL)
					{
					lp = makeLabelPane(la.name);
					p.add(lp);
					continue;
					}
				if (la.actionKind == Action.ACT_PLACEHOLDER)
					b = new JLabel();
				else
					b = new JLabel(new ImageIcon(Util.getTransparentIcon(la.actImage)));
				b.setHorizontalAlignment(JLabel.LEFT);
				b.setVerticalAlignment(JLabel.TOP);
				b.setPreferredSize(new Dimension(30,30));
				if (lp == null)
					{
					lp = makeLabelPane(null);
					p.add(lp);
					}
				b.setToolTipText(la.description);
				lp.add(b);
				}
			tp.addTab(l.tabCaption,p);
			tp.setTabComponentAt(tp.getTabCount() - 1,new JLabel(new VTextIcon(tp,l.tabCaption)));
			}
		return tp;
		}

	//TODO:
	@Override
	public boolean resourceChanged()
		{
		return true;
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.gmObjects.replace(res.getId(),resOriginal);
		}

	@Override
	public void updateResource()
		{
		res.setName(name.getText());

		resOriginal = res.copy();
		}

	//TODO:
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == newsprite)
			{
			return;
			}
		super.actionPerformed(e);
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
		ActionListModel alm = new ActionListModel();
		alm.addAll(0,((Event) node.getUserObject()).actions);
		actions.setModel(alm);
		}

	@SuppressWarnings("serial")
	public class ActionListModel extends AbstractListModel
		{
		private ArrayList<Action> list;
		private ArrayList<Integer> indents;

		public ActionListModel()
			{
			list = new ArrayList<Action>();
			indents = new ArrayList<Integer>();
			}

		public void add(int index, Action a)
			{
			list.add(index,a);
			updateIndentation();
			fireIntervalAdded(this,index,index);
			}

		public void addAll(int index, Collection<? extends Action> c)
			{
			int s = c.size();
			if (s <= 0) return;
			list.addAll(index,c);
			updateIndentation();
			fireIntervalAdded(this,index,index + s - 1);
			}

		public void remove(int index)
			{
			list.remove(index);
			updateIndentation();
			fireIntervalRemoved(this,index,index);
			}

		public Object getElementAt(int index)
			{
			return list.get(index);
			}

		public int getSize()
			{
			return list.size();
			}

		private void updateIndentation()
			{
			int lms = list.size();
			indents.clear();
			indents.ensureCapacity(lms);
			ArrayList<Integer> levelIndents = new ArrayList<Integer>();
			levelIndents.add(0);
			int level = 0;
			boolean indentNext = false;
			for (int i = 0; i < lms; i++)
				{
				Action a = list.get(i);
				int indent;
				if (indentNext)
					{
					indent = indents.get(i - 1) + 1;
					indentNext = false;
					}
				else
					indent = levelIndents.get(level);
				indents.add(indent);
				switch (a.libAction.actionKind)
					{
					case Action.ACT_BEGIN:
						level += 1;
						if (levelIndents.size() > level)
							{
							levelIndents.set(level,indent);
							}
						else
							{
							levelIndents.add(indent);
							}
						break;
					case Action.ACT_END:
						level -= 1;
						if (level < 0) level = 0;
						break;
					case Action.ACT_ELSE:
					case Action.ACT_REPEAT:
						indentNext = true;
						break;
					default:
						if (a.libAction.question) indentNext = true;
					}
				}

			}
		}
	}
