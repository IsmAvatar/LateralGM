/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static org.lateralgm.main.Util.deRef;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import org.lateralgm.components.ActionListEditor.LibActionButton;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.subframes.ActionFrame;

public class ActionList extends JList
	{

	private static final long serialVersionUID = 1L;
	private static final Hashtable<Action,MDIFrame> FRAMES = new Hashtable<Action,MDIFrame>();
	private static final ActionListMouseListener ALML = new ActionListMouseListener();
	private static final ActionListKeyListener ALKL = new ActionListKeyListener();
	protected ActionContainer actionContainer;
	private ActionListModel model;

	public ActionList()
		{
		setActionContainer(null);
		if (LGM.javaVersion >= 10600)
			{
			setTransferHandler(new ActionTransferHandler());
			setDragEnabled(true);
			setDropMode(DropMode.ON_OR_INSERT);
			}
		addMouseListener(ALML);
		addKeyListener(ALKL);
		setCellRenderer(new ActionRenderer());
		}

	public void setActionContainer(ActionContainer ac)
		{
		save();
		actionContainer = ac;
		model = new ActionListModel();
		setModel(model);
		if (ac == null) return;
		model.addAll(0,ac.actions);
		}

	public ActionContainer getActionContainer()
		{
		return actionContainer;
		}

	public void save()
		{
		if (actionContainer == null) return;
		actionContainer.actions = model.list;
		}

	/**
	 * Opens an ActionFrame representing a given action.
	 * Actions like "else" etc. will not have a frame opened.
	 * @param a The action to open a frame for
	 * @return The frame opened or <code>null</code> if no
	 * frame was opened.
	 */
	public static MDIFrame openActionFrame(Action a)
		{
		LibAction la = a.libAction;
		if ((la.libArguments == null || la.libArguments.length == 0) && !la.canApplyTo
				&& !la.allowRelative) return null;
		MDIFrame af = FRAMES.get(a);
		if (af == null || af.isClosed())
			{
			af = new ActionFrame(a);
			LGM.mdi.add(af);
			FRAMES.put(a,af);
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
		return af;
		}

	private static class ActionListMouseListener extends MouseAdapter
		{
		public ActionListMouseListener()
			{
			super();
			}

		public void mouseClicked(MouseEvent e)
			{
			if (e.getClickCount() != 2) return;
			JList l = (JList) e.getSource();
			Object o = l.getSelectedValue();
			if (o == null || !(o instanceof Action)) return;
			openActionFrame((Action) o);
			}
		}

	private static class ActionListKeyListener extends KeyAdapter
		{
		public ActionListKeyListener()
			{
			super();
			}

		@Override
		public void keyPressed(KeyEvent e)
			{
			JList l = (JList) e.getSource();
			switch (e.getKeyCode())
				{
				case KeyEvent.VK_DELETE:
					int[] indices = l.getSelectedIndices();
					ActionListModel alm = (ActionListModel) l.getModel();
					for (int i = indices.length - 1; i >= 0; i--)
						alm.remove(indices[i]);
					e.consume();
					break;
				}
			}
		}

	public static class ActionListModel extends AbstractListModel
		{
		private static final long serialVersionUID = 1L;
		protected ArrayList<Action> list;
		protected ArrayList<Integer> indents;

		public ActionListModel()
			{
			list = new ArrayList<Action>();
			indents = new ArrayList<Integer>();
			}

		public void add(Action a)
			{
			add(getSize(),a);
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
			Stack<Integer> levelIndents = new Stack<Integer>();
			Stack<Stack<Integer>> questions = new Stack<Stack<Integer>>();
			levelIndents.push(0);
			questions.push(new Stack<Integer>());
			int nextIndent = 0;
			for (int i = 0; i < lms; i++)
				{
				Action a = list.get(i);
				int indent = nextIndent;
				switch (a.libAction.actionKind)
					{
					case Action.ACT_BEGIN:
						levelIndents.push(indent);
						questions.push(new Stack<Integer>());
						break;
					case Action.ACT_END:
						indent = levelIndents.peek();
						if (levelIndents.size() > 1)
							{
							levelIndents.pop();
							questions.pop();
							}
						nextIndent = levelIndents.peek();
						break;
					case Action.ACT_ELSE:
						try
							{
							int j = questions.peek().pop();
							if (j >= 0) indent = indents.get(j);
							}
						catch (EmptyStackException e)
							{
							}
						nextIndent = indent + 1;
						break;
					case Action.ACT_REPEAT:
						nextIndent++;
						break;
					case Action.ACT_EXIT:
						nextIndent = levelIndents.peek();
						break;
					default:
						if (a.libAction.question)
							{
							questions.peek().push(i);
							nextIndent++;
							}
						else if (a.libAction.execType != Action.EXEC_NONE) nextIndent = levelIndents.peek();
					}
				indents.add(indent);
				}
			}
		}

	public static final DataFlavor ACTION_FLAVOR = new DataFlavor(Action.class,"Action"); //$NON-NLS-1$
	public static final DataFlavor ACTION_ARRAY_FLAVOR = new DataFlavor(List.class,"Action array"); //$NON-NLS-1$
	public static final DataFlavor LIB_ACTION_FLAVOR = new DataFlavor(LibAction.class,
			"Library action"); //$NON-NLS-1$

	public static class LibActionTransferable implements Transferable
		{
		private static final DataFlavor[] FLAVORS = { LIB_ACTION_FLAVOR };
		private final LibAction libAction;

		public LibActionTransferable(LibAction la)
			{
			libAction = la;
			}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
			{
			if (flavor == LIB_ACTION_FLAVOR)
				{
				return libAction;
				}
			throw new UnsupportedFlavorException(flavor);
			}

		public DataFlavor[] getTransferDataFlavors()
			{
			return FLAVORS;
			}

		public boolean isDataFlavorSupported(DataFlavor flavor)
			{
			return flavor == LIB_ACTION_FLAVOR;
			}
		}

	public static class LibActionTransferHandler extends TransferHandler
		{
		private static final long serialVersionUID = 1L;

		public boolean canImport(TransferHandler.TransferSupport info)
			{
			return false;
			}

		public boolean importData(TransferHandler.TransferSupport info)
			{
			return false;
			}

		public int getSourceActions(JComponent c)
			{
			return COPY;
			}

		protected Transferable createTransferable(JComponent c)
			{
			LibActionButton lab = (LibActionButton) c;
			LibAction la = lab.getLibAction();
			return new LibActionTransferable(la);
			}
		}

	public static class ActionTransferable implements Transferable
		{
		private final Action[] actions;
		private final DataFlavor[] flavors;

		public ActionTransferable(Action[] a)
			{
			actions = a;
			ArrayList<DataFlavor> fl = new ArrayList<DataFlavor>(2);
			fl.add(ACTION_ARRAY_FLAVOR);
			if (a.length == 1) fl.add(ACTION_FLAVOR);
			flavors = fl.toArray(new DataFlavor[2]);
			}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
			{
			if (flavor == ACTION_FLAVOR && actions.length == 1)
				{
				return actions[0];
				}
			if (flavor == ACTION_ARRAY_FLAVOR)
				{
				List<Action> l = Arrays.asList(actions);
				return l;
				}
			throw new UnsupportedFlavorException(flavor);
			}

		public DataFlavor[] getTransferDataFlavors()
			{
			return flavors;
			}

		public boolean isDataFlavorSupported(DataFlavor flavor)
			{
			for (DataFlavor f : flavors)
				{
				if (f == flavor) return true;
				}
			return false;
			}
		}

	public static class ActionTransferHandler extends TransferHandler
		{
		private static final long serialVersionUID = 1L;
		private int[] indices = null;
		private int addIndex = -1; //Location where items were added
		private int addCount = 0; //Number of items added.

		@Override
		protected void exportDone(JComponent source, Transferable data, int action)
			{
			if (action == MOVE && indices != null)
				{
				JList ls = (JList) source;
				ActionListModel model = (ActionListModel) ls.getModel();
				if (addCount > 0)
					{
					for (int i = 0; i < indices.length; i++)
						{
						if (indices[i] > addIndex)
							{
							indices[i] += addCount;
							}
						}
					}
				for (int i = indices.length - 1; i >= 0; i--)
					{
					model.remove(indices[i]);
					}
				}
			indices = null;
			addCount = 0;
			addIndex = -1;
			}

		public boolean canImport(TransferHandler.TransferSupport info)
			{
			DataFlavor[] f = info.getDataFlavors();
			boolean supported = false;
			for (DataFlavor flav : f)
				{
				if (flav == ACTION_FLAVOR || flav == ACTION_ARRAY_FLAVOR || flav == LIB_ACTION_FLAVOR)
					supported = true;
				}
			if (!supported) return false;
			ActionList list = (ActionList) info.getComponent();
			JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
			if (list.actionContainer == null || dl.getIndex() == -1 || !info.isDrop()) return false;
			return true;
			}

		public boolean importData(TransferHandler.TransferSupport info)
			{
			if (!canImport(info)) return false;
			ActionList list = (ActionList) info.getComponent();
			ActionListModel alm = (ActionListModel) list.getModel();
			JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
			Transferable t = info.getTransferable();
			int index = dl.getIndex();
			if (indices != null && index >= indices[0] && index <= indices[indices.length - 1])
				{
				indices = null;
				return false;
				}
			if (info.isDataFlavorSupported(ACTION_FLAVOR))
				{
				Action a;
				try
					{
					a = (Action) t.getTransferData(ACTION_FLAVOR);
					}
				catch (Exception e)
					{
					return false;
					}
				addIndex = index;
				addCount = 1;
				alm.add(index,a);
				return true;
				}
			if (info.isDataFlavorSupported(ACTION_ARRAY_FLAVOR))
				{
				Action[] a;
				try
					{
					a = ((List<?>) t.getTransferData(ACTION_ARRAY_FLAVOR)).toArray(new Action[0]);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					return false;
					}
				addIndex = index;
				addCount = a.length;
				alm.addAll(index,Arrays.asList(a));
				return true;
				}
			if (info.isDataFlavorSupported(LIB_ACTION_FLAVOR))
				{
				LibAction la;
				Action a;
				try
					{
					la = (LibAction) t.getTransferData(LIB_ACTION_FLAVOR);
					a = new Action(la);
					ActionList.openActionFrame(a);
					}
				catch (Exception e)
					{
					return false;
					}
				addIndex = index;
				addCount = 1;
				alm.add(index,a);
				return true;
				}
			return false;
			}

		public int getSourceActions(JComponent c)
			{
			return MOVE;
			}

		protected Transferable createTransferable(JComponent c)
			{
			JList list = (JList) c;
			indices = list.getSelectedIndices();
			Object[] o = list.getSelectedValues();
			Action[] a = new Action[o.length];
			a = Arrays.asList(o).toArray(a);
			return new ActionTransferable(a);
			}
		}

	private static class ActionRenderer implements ListCellRenderer
		{
		public ActionRenderer()
			{
			super();
			}

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
						GmObject applies = deRef(a.appliesTo);
						ret += Messages.format("Action.APPLIES",applies == null ? a.appliesTo.toString() //$NON-NLS-1$
								: applies.getName());
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
			s = s.replaceAll("&","&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("<","&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll(">","&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\n","<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\\\\#","\n"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("#","<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\n","&#35;"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll(" ","&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$

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
				l.setText("<b>" + l.getText()); //$NON-NLS-1$
			if (la.listText.contains("@FI")) //$NON-NLS-1$
				l.setText("<i>" + l.getText()); //$NON-NLS-1$
			l.setText("<html>" + l.getText()); //$NON-NLS-1$
			l.setIcon(new ImageIcon(Util.getTransparentIcon(la.actImage)));
			l.setToolTipText("<html>" + parse(la.hintText,(Action) cell)); //$NON-NLS-1$
			return l;
			}
		}
	}
