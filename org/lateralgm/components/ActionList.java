/*
 * Copyright (C) 2007, 2008, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
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
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.WeakHashMap;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
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
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.subframes.ActionFrame;

public class ActionList extends JList
	{
	private static final long serialVersionUID = 1L;
	private static final WeakHashMap<Action,WeakReference<ActionFrame>> FRAMES;
	private static final ActionListKeyListener ALKL = new ActionListKeyListener();
	protected ActionContainer actionContainer;
	private ActionListModel model;
	private final ActionRenderer renderer = new ActionRenderer();
	public final WeakReference<MDIFrame> parent;
	private final ActionListMouseListener alml;

	static
		{
		FRAMES = new WeakHashMap<Action,WeakReference<ActionFrame>>();
		}

	public ActionList(MDIFrame parent)
		{
		this.parent = new WeakReference<MDIFrame>(parent);
		setActionContainer(null);
		setBorder(BorderFactory.createEmptyBorder(0,0,24,0));
		if (LGM.javaVersion >= 10600)
			{
			setTransferHandler(new ActionTransferHandler(this.parent));
			setDragEnabled(true);
			setDropMode(DropMode.ON_OR_INSERT);
			}
		alml = new ActionListMouseListener(this.parent);
		addMouseListener(alml);
		addKeyListener(ALKL);
		setCellRenderer(renderer);
		}

	public void setActionContainer(ActionContainer ac)
		{
		save();
		actionContainer = ac;
		model = new ActionListModel();
		model.renderer = renderer;
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
		for (WeakReference<ActionFrame> a : FRAMES.values())
			if (a != null && a.get() != null) a.get().commitChanges();
		actionContainer.actions = model.list;
		}

	/**
	 * Opens an ActionFrame representing a given action.
	 * Actions like "else" etc. will not have a frame opened.
	 * @param a The action to open a frame for
	 * @return The frame opened or <code>null</code> if no
	 * frame was opened.
	 */
	public static MDIFrame openActionFrame(MDIFrame parent, Action a)
		{
		LibAction la = a.getLibAction();
		if ((la.libArguments == null || la.libArguments.length == 0) && !la.canApplyTo
				&& !la.allowRelative && !la.question) return null;
		WeakReference<ActionFrame> fr = FRAMES.get(a);
		ActionFrame af = fr == null ? null : fr.get();
		if (af == null || af.isClosed())
			{
			af = new ActionFrame(a);
			LGM.mdi.add(af);
			if (parent != null) LGM.mdi.addZChild(parent,af);
			FRAMES.put(a,new WeakReference<ActionFrame>(af));
			}
		af.setVisible(true);
		//FIXME: Find out why parent is sent to back. This is a workaround.
		if (parent != null) parent.toFront();
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
		public final WeakReference<MDIFrame> parent;

		public ActionListMouseListener(WeakReference<MDIFrame> parent)
			{
			super();
			this.parent = parent;
			}

		public void mouseClicked(MouseEvent e)
			{
			if (e.getClickCount() != 2 || !(e.getSource() instanceof JList)) return;
			JList l = (JList) e.getSource();
			Object o = l.getSelectedValue();

			if (o == null && l.getModel().getSize() == 0)
				{
				o = new Action(LibManager.codeAction);
				((ActionListModel) l.getModel()).add((Action) o);
				l.setSelectedValue(o,true);
				}

			if (o == null || !(o instanceof Action)) return;
			openActionFrame(parent.get(),(Action) o);
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
					if (indices.length != 0) l.setSelectedIndex(Math.min(alm.getSize() - 1,indices[0]));
					e.consume();
					break;
				}
			}
		}

	public static class ActionListModel extends AbstractListModel implements UpdateListener
		{
		private static final long serialVersionUID = 1L;
		protected ArrayList<Action> list;
		protected ArrayList<Integer> indents;
		private ActionRenderer renderer;

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
			a.updateSource.addListener(this);
			list.add(index,a);
			updateIndentation();
			fireIntervalAdded(this,index,index);
			}

		public void addAll(int index, Collection<? extends Action> c)
			{
			int s = c.size();
			if (s <= 0) return;
			for (Action a : c)
				{
				a.updateSource.addListener(this);
				}
			list.addAll(index,c);
			updateIndentation();
			fireIntervalAdded(this,index,index + s - 1);
			}

		public void remove(int index)
			{
			list.remove(index).updateSource.removeListener(this);
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
				LibAction la = a.getLibAction();
				int indent = nextIndent;
				switch (la.actionKind)
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
						if (la.question)
							{
							questions.peek().push(i);
							nextIndent++;
							}
						else if (la.execType != Action.EXEC_NONE) nextIndent = levelIndents.peek();
					}
				indents.add(indent);
				}
			}

		public void updated(UpdateEvent e)
			{
			if (renderer != null) renderer.clearCache();
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
		private int[] indices = null; //Location of dragged items (to be deleted)
		private int addIndex = -1; //Location where items were added
		private int addCount = 0; //Number of items added.
		private final WeakReference<MDIFrame> parent;

		public ActionTransferHandler(WeakReference<MDIFrame> parent)
			{
			super();
			this.parent = parent;
			}

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
			if (list.actionContainer == null) return false;
			if (info.isDrop() && ((JList.DropLocation) info.getDropLocation()).getIndex() == -1)
				return false;
			return true;
			}

		public boolean importData(TransferHandler.TransferSupport info)
			{
			if (!canImport(info)) return false;
			ActionList list = (ActionList) info.getComponent();
			ActionListModel alm = (ActionListModel) list.getModel();
			Transferable t = info.getTransferable();

			int index = alm.list.size();
			if (info.isDrop()) index = ((JList.DropLocation) info.getDropLocation()).getIndex();
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
				//clone properly for drag-copy or clipboard paste
				if (!info.isDrop() || info.getDropAction() == COPY) a = a.copy();
				//now add
				addIndex = index;
				addCount = 1;
				alm.add(index,a);
				list.setSelectedIndex(index);
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
				//clone properly for drag-copy or clipboard paste
				if (!info.isDrop() || info.getDropAction() == COPY) for (int i = 0; i < a.length; i++)
					a[i] = a[i].copy();
				//now add
				addIndex = index;
				addCount = a.length;
				alm.addAll(index,Arrays.asList(a));
				list.setSelectionInterval(index,index + a.length - 1);
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
					ActionList.openActionFrame(parent.get(),a);
					}
				catch (Exception e)
					{
					return false;
					}
				addIndex = index;
				addCount = 1;
				alm.add(index,a);
				list.setSelectedIndex(index);
				return true;
				}
			return false;
			}

		public int getSourceActions(JComponent c)
			{
			return COPY_OR_MOVE;
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
		private final WeakHashMap<Action,SoftReference<ActionRendererComponent>> lcrMap;

		public ActionRenderer()
			{
			super();
			lcrMap = new WeakHashMap<Action,SoftReference<ActionRendererComponent>>();
			}

		public void clearCache()
			{
			lcrMap.clear();
			}

		public static String parse(String s, Action a)
			{
			String escape = "FrNw01234567"; //$NON-NLS-1$
			StringBuilder ret = new StringBuilder();

			int k = 0;
			int p = s.indexOf('@');
			while (p != -1)
				{
				ret.append(s.substring(k,p));
				char c = s.charAt(p + 1);
				if (!escape.contains(String.valueOf(c)))
					{
					ret.append('@');
					k = p + 1;
					p = s.indexOf('@',k);
					continue;
					}
				if (c == 'F')
					{
					if (s.charAt(p + 2) == 'B' || s.charAt(p + 2) == 'I')
						p += 2;
					else
						ret.append('@');
					k = p + 1;
					p = s.indexOf('@',k);
					continue;
					}
				if (c == 'r' && a.isRelative()) ret.append(Messages.getString("Action.RELATIVE")); //$NON-NLS-1$
				if (c == 'N' && a.isNot()) ret.append(Messages.getString("Action.NOT")); //$NON-NLS-1$
				ResourceReference<GmObject> at = a.getAppliesTo();
				if (c == 'w' && !at.equals(GmObject.OBJECT_SELF))
					{
					if (at.equals(GmObject.OBJECT_OTHER))
						ret.append(Messages.getString("Action.APPLIES_OTHER")); //$NON-NLS-1$
					else
						{
						GmObject applies = deRef(at);
						ret.append(Messages.format("Action.APPLIES",applies == null ? at.toString() //$NON-NLS-1$
								: applies.getName()));
						}
					}
				if (c >= '0' && c < '8')
					{
					int arg = c - '0';
					List<Argument> args = a.getArguments();
					if (arg >= args.size())
						ret.append('0');
					else
						{
						Argument aa = args.get(arg);
						ret.append(aa.toString(a.getLibAction().libArguments[arg]));
						}
					}
				k = p + 2;
				p = s.indexOf('@',k);
				}

			return ret + s.substring(k);
			}

		public static String escape(String s)
			{
			s = s.replaceAll("&","&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("<","&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll(">","&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\n","<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\\\\#","\n"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("#","<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\n","&#35;"); //$NON-NLS-1$ //$NON-NLS-2$
			return s.replaceAll(" ","&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
			}

		private static class ActionRendererComponent extends JLabel
			{
			private static final long serialVersionUID = 1L;
			private int indent;
			private boolean selected;
			private final JList list;

			public ActionRendererComponent(Action a, JList list)
				{
				this.list = list;
				setOpaque(true);
				setBackground(selected ? list.getSelectionBackground() : list.getBackground());
				setForeground(selected ? list.getSelectionForeground() : list.getForeground());
				LibAction la = a.getLibAction();
				if (la.actImage == null)
					setText(Messages.getString("Action.UNKNOWN")); //$NON-NLS-1$
				else
					{
					StringBuilder sb = new StringBuilder("<html>");
					if (la.listText.contains("@FI")) //$NON-NLS-1$
						sb.append("<i>");
					if (la.listText.contains("@FB")) //$NON-NLS-1$
						sb.append("<b>");
					sb.append(escape(parse(la.listText,a)));
					setText(sb.toString());
					setIcon(new ImageIcon(la.actImage));

					if (Prefs.actionToolTipLines > 0 && Prefs.actionToolTipColumns > 0)
						{
						sb = new StringBuilder();
						String snip = parse(la.hintText.replaceAll("(?<!\\\\)#","\n"),a);
						int last, next = -1;
						for (int i = 0; i < Prefs.actionToolTipLines; i++)
							{
							last = next + 1;
							next = snip.indexOf('\n',last);
							if (next == -1)
								{
								sb.append(snip.substring(last));
								break;
								}
							if (next > last + Prefs.actionToolTipColumns)
								{
								sb.append(snip.substring(last,last + Prefs.actionToolTipColumns));
								sb.append("...");
								}
							else
								sb.append(snip.substring(last,next));
							sb.append("\n");
							}
						if (next != -1) sb.append(Messages.getString("Action.HINT_MORE"));
						setToolTipText("<html><font face=\"Courier\">" + escape(sb.toString()));
						}
					}
				}

			//			public JToolTip createToolTip()
			//				{
			//				JToolTip tip = new JToolTip();
			//				tip.setComponent(this);
			//				return tip;
			//				}

			/**
			 * Overridden to address java bug 6700748 by returning false.
			 * In WinXP, the cursor flickers between two states on drag & drop.
			 */
			public boolean isVisible()
				{
				return false;
				}

			public void setIndent(int indent)
				{
				if (this.indent == indent) return;
				this.indent = indent;
				setBorder(new EmptyBorder(1,2 + 8 * indent,1,2));
				}

			public void setSelected(boolean selected)
				{
				if (this.selected == selected) return;
				this.selected = selected;
				if (selected)
					{
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
					}
				else
					{
					setBackground(list.getBackground());
					setForeground(list.getForeground());
					}
				}
			}

		public Component getListCellRendererComponent(JList list, Object cell, int index,
				boolean isSelected, boolean hasFocus)
			{
			final Action cellAction = (Action) cell;

			SoftReference<ActionRendererComponent> arcref = lcrMap.get(cellAction);
			ActionRendererComponent arc = null;
			if (arcref != null) arc = arcref.get();
			if (arc == null)
				{
				arc = new ActionRendererComponent(cellAction,list);
				lcrMap.put(cellAction,new SoftReference<ActionRendererComponent>(arc));
				}
			ListModel lm = list.getModel();
			try
				{
				if (lm instanceof ActionListModel)
					arc.setIndent(((ActionListModel) lm).indents.get(index));
				}
			catch (IndexOutOfBoundsException e)
				{
				}
			arc.setSelected(isSelected);
			return arc;
			}
		}
	}
