/*
 * Copyright (C) 2007, 2008, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014, 2016 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static org.lateralgm.main.Util.deRef;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

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

public class ActionList extends JList<Action> implements ActionListener,ClipboardOwner
	{
	private static final long serialVersionUID = 1L;
	private static final Map<Action,WeakReference<ActionFrame>> FRAMES;
	private static final ActionListKeyListener ALKL = new ActionListKeyListener();
	protected ActionContainer actionContainer;
	public ActionListModel model;
	private final ActionRenderer renderer = new ActionRenderer(this);
	public final WeakReference<MDIFrame> parent;
	private final ActionListMouseListener alml;
	public UndoManager undomanager;

	static
		{
		FRAMES = new WeakHashMap<Action,WeakReference<ActionFrame>>();
		}

	private JMenuItem makeContextButton(String key)
		{
		JMenuItem b = new JMenuItem(Messages.getString(key));
		b.setActionCommand(key);
		b.setText(b.getText());
		b.setIcon(LGM.getIconForKey(key));
		b.setRequestFocusEnabled(false);
		b.addActionListener(this);
		return b;
		}

	public ActionList(MDIFrame parent)
		{
		// build popup menu
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem item;

		item = makeContextButton("ActionList.EDIT");
		popup.add(item);
		popup.addSeparator();

		item = makeContextButton("ActionList.CUT");
		popup.add(item);
		item.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.CUT")));
		item = makeContextButton("ActionList.COPY");
		popup.add(item);
		item.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.COPY")));
		item = makeContextButton("ActionList.PASTE");
		popup.add(item);
		item.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.PASTE")));

		popup.addSeparator();

		undomanager = new UndoManager();
		final JMenuItem undoitem = makeContextButton("ActionList.UNDO");
		popup.add(undoitem);
		undoitem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.UNDO")));
		final JMenuItem redoitem = makeContextButton("ActionList.REDO");
		popup.add(redoitem);
		redoitem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.REDO")));
		popup.addSeparator();

		item = makeContextButton("ActionList.SELECTALL");
		popup.add(item);
		item.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.SELECTALL")));

		popup.addSeparator();
		item = makeContextButton("ActionList.DELETE");
		popup.add(item);
		item.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.DELETE")));

		item = makeContextButton("ActionList.CLEAR");
		popup.add(item);

		this.setComponentPopupMenu(popup);
		popup.addPopupMenuListener(new PopupMenuListener()
			{

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0)
				{
				}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0)
				{
				}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0)
				{
				undoitem.setEnabled(undomanager.canUndo());
				redoitem.setEnabled(undomanager.canRedo());
				}

			});

		this.parent = new WeakReference<MDIFrame>(parent);
		setActionContainer(null);
		setBorder(BorderFactory.createEmptyBorder(0,0,24,0));
		setTransferHandler(new ActionTransferHandler(this.parent,this));
		setDragEnabled(true);
		setDropMode(DropMode.ON_OR_INSERT);
		alml = new ActionListMouseListener(this.parent);
		addMouseListener(alml);
		addKeyListener(ALKL);
		setCellRenderer(renderer);
		}

	public void setActionContainer(ActionContainer ac)
		{
		save();
		actionContainer = ac;

		model = new ActionListModel(undomanager);
		model.renderer = renderer;
		setModel(model);
		if (ac == null) return;
		model.addAll(ac.actions,false);
		}

	public ActionContainer getActionContainer()
		{
		return actionContainer;
		}

	public void save()
		{
		if (actionContainer == null) return;
		for (WeakReference<ActionFrame> a : FRAMES.values())
			{
			if (a != null)
				{
				ActionFrame af = a.get();
				if (af != null && !af.isClosed()) af.commitChanges();
				}
			}
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
			//Guess it doesn't want us doing that. Oh well.
			LGM.showDefaultExceptionHandler(pve);
			}
		return af;
		}

	public static void closeFrames()
		{
		for (Map.Entry<Action,WeakReference<ActionFrame>> entry : FRAMES.entrySet())
			{
				ActionFrame frame = entry.getValue().get();

				if (frame != null) {
					frame.dispose();
				}
			}
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
			if (e.getClickCount() != 2 || !(e.getSource() instanceof ActionList)) return;
			ActionList l = (ActionList) e.getSource();
			Object o = l.getSelectedValue();

			if (o == null && l.getModel().getSize() == 0 && l.getActionContainer() != null)
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
			if (!(e.getSource() instanceof ActionList))
				return;

			@SuppressWarnings("unchecked")
			JList<Action> l = (JList<Action>) e.getSource();

			KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
			if (stroke != null)
				{
				if (stroke.equals(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.UNDO"))))
					{
					ActionsUndo(l);
					}
				else if (stroke.equals(KeyStroke.getKeyStroke(Messages.getKeyboardString("ActionList.REDO"))))
					{
					ActionsRedo(l);
					}
				}

			switch (e.getKeyCode())
				{
				case KeyEvent.VK_DELETE:
					ActionsDelete(l);
					e.consume();
					break;
				}
			}
		}

	public class UndoableActionEdit extends AbstractUndoableEdit {
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = 3005489569659632528L;

		public static final byte ACTION_ADD = 0;
		public static final byte ACTION_REMOVE = 1;
		public static final byte ACTION_MOVE = 2;
		public static final byte ACTION_EDIT = 3;

		public int type;

		List<Action> actions = null;
		List<Integer> indices = null;
		List<Integer> indicesmoved = null;

		public UndoableActionEdit(int t, List<Action> acts) {
			super();
			type = t;
			actions = acts;
		}

		public UndoableActionEdit(int t, List<Integer> inds, List<Action> acts) {
			super();
			type = t;
			actions = acts;
			indices = inds;
		}

		public UndoableActionEdit(int t, List<Integer> inds, List<Integer> moved, List<Action> acts) {
			super();
			type = t;
			indices = inds;
			indicesmoved = moved;
		}

		// Return a reasonable name for this edit.
		@Override
		public String getPresentationName() {
			return "Action " + type;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (type == ACTION_ADD) {
				if (indices != null) {
					model.addAll(indices,actions,false);
				} else {
					model.addAll(actions,false);
				}
			} else if (type == ACTION_REMOVE) {
				if (indices != null) {
					model.removeAll(indices,false);
				} else {
					model.clear(false);
				}
			} else if (type == ACTION_MOVE) {
				model.moveAll(indices, indicesmoved, false);
			} else if (type == ACTION_EDIT) {
				//TODO: Implement
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (type == ACTION_ADD) {
				if (indices != null) {
					model.removeAll(indices,false);
				} else {
					model.clear(false);
				}
			} else if (type == ACTION_REMOVE) {
				if (indices != null) {
					model.addAll(indices,actions,false);
				} else {
					model.addAll(actions,false);
				}
			} else if (type == ACTION_MOVE) {
				model.moveAll(indicesmoved, indices, false);
			} else if (type == ACTION_EDIT) {
				//TODO: Implement
			}
		}
	}

	//TODO: Make sure a change actually happened before you store it, i.e. when you just
	//drag and drop an action to the same location it shouldn't create an unecessary undo
	public class ActionListModel extends AbstractListModel<Action> implements UpdateListener
		{
		private static final long serialVersionUID = 1L;
		public ArrayList<Action> list;
		protected ArrayList<Integer> indents;
		private ActionRenderer renderer;
		private UndoManager undoManager;

		public ActionListModel(UndoManager um)
		{
			list = new ArrayList<Action>();
			indents = new ArrayList<Integer>();
			undoManager = um;
		}

		public void add(Action a)
		{
			add(a, true);
		}

		public void add(Action a, boolean updateundo)
		{
			add(getSize(),a,updateundo);
		}

		public void add(int index, Action a)
		{
			add(index, a, true);
		}

		public void add(int index, Action a, boolean updateundo)
		{
			if (updateundo) {
				List<Integer> indices = new ArrayList<Integer>();
				List<Action> actions = new ArrayList<Action>();
				indices.add(index);
				actions.add(a);
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_ADD, indices, actions));
			}
			a.updateSource.addListener(this);
			list.add(index,a);
			updateIndentation();
			fireIntervalAdded(this,index,index);
		}

		public void addAll(int index, List<Action> c, boolean updateundo)
		{
			int s = c.size();
			if (s <= 0) return;
			List<Integer> indices = new ArrayList<Integer>();
			int i = index;
			for (Action a : c)
			{
				indices.add(i++);
				a.updateSource.addListener(this);
			}
			list.addAll(index,c);
			updateIndentation();
			fireIntervalAdded(this,index,index + s - 1);
			if (updateundo) {
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_ADD, indices, c));
			}
		}

		public void addAll(int index, List<Action> c)
		{
			addAll(index, c, true);
		}

		public void addAll(List<Action> c, boolean updateundo)
		{
			int s = c.size();
			if (s <= 0) return;
			for (Action a : c)
			{
				a.updateSource.addListener(this);
			}
			list.addAll(c);
			updateIndentation();
			fireIntervalAdded(this,0,list.size());
			if (updateundo) {
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_ADD, c));
			}
		}

		public void addAll(List<Action> c) {
			addAll(c, true);
		}

		public void addAll(List<Integer> indices, List<Action> c, boolean updateundo)
		{
			int s = c.size();
			if (s <= 0) return;

			// sort small to large to avoid oob
			TreeMap<Integer,Action> map = new TreeMap<Integer,Action>();
			for (int i = 0; i < indices.size(); i++) {
				Integer ind = indices.get(i);
				Action act = c.get(i);
				map.put(ind,act);
			}
			for (Entry<Integer,Action> entry : map.entrySet()) {
				Action a = entry.getValue();
				Integer ind = entry.getKey();
				a.updateSource.addListener(this);
				list.add(ind, a);
				fireIntervalAdded(this,ind,ind);
			}

			updateIndentation();

			if (updateundo) {
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_ADD, indices, c));
			}
		}

		public void addAll(List<Integer> indices, List<Action> c)
		{
			addAll(indices, c, true);
		}

		public void remove(int index, boolean updateundo)
		{
			if (updateundo) {
				ArrayList<Integer> indices = new ArrayList<Integer>();
				ArrayList<Action> actions = new ArrayList<Action>();
				indices.add(index);
				actions.add(list.get(index));
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_REMOVE, indices, actions));
			}
			list.remove(index).updateSource.removeListener(this);
			updateIndentation();
			fireIntervalRemoved(this,index,index);
		}

		public void remove(int index)
		{
			remove(index, true);
		}

		public void removeAll(List<Integer> indices, boolean updateundo)
		{
			List<Action> removed = new ArrayList<Action>();
			// sort large to small to avoid oob
			List<Integer> copy = new ArrayList<Integer>(indices);
			Collections.sort(copy, new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				//TODO: handle null
				return b.compareTo(a);
			}
			});
			// collect the removed ones in order
			for (int i = 0; i < indices.size(); i++) {
				int ind = indices.get(i);
				removed.add(list.get(ind));
			}
			// now remove them in sorted order
			for (int i = 0; i < copy.size(); i++) {
				int ind = copy.get(i);
				list.remove(ind).updateSource.removeListener(this);
				fireIntervalRemoved(this,ind,ind);
			}

			if (updateundo) {
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_REMOVE, indices, removed));
			}

			updateIndentation();
		}

		public void removeAll(List<Integer> indices)
		{
			removeAll(indices, true);
		}

		public void clear(boolean updateundo)
		{
			ArrayList<Action> removed = new ArrayList<Action>(list);
			list.clear();
			fireIntervalRemoved(this,0,removed.size());
			if (updateundo) {
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_REMOVE, removed));
			}
		}

		public void clear() {
			clear(true);
		}

		public int move(int prev, int next, ArrayList<Action> unchanged, boolean updateundo) {
			Action a = unchanged.get(prev);

			list.remove(prev).updateSource.removeListener(this);
			fireIntervalRemoved(this,prev,prev);

			if (next > list.size())
				{
				next = list.size();
				}
			a.updateSource.addListener(this);
			list.add(next,a);
			fireIntervalAdded(this,next,next);

			if (updateundo)
				{
				ArrayList<Integer> indices = new ArrayList<Integer>(1);
				ArrayList<Integer> indicesmoved = new ArrayList<Integer>(1);
				indices.add(prev);
				indicesmoved.add(next);
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_MOVE, indices, indicesmoved, null));
				}
			return next;
		}

		public void move(int prev, int next) {
			move(prev, next, new ArrayList<Action>(list), true);
		}

		public void moveAll(List<Integer> indices, List<Integer> indicesmoved, boolean updateundo)
			{
			ArrayList<Action> unchanged = new ArrayList<Action>(list);
			removeAll(indices, false);
			for (int i = 0; i < indices.size(); i++)
				{
				Integer prev = indices.get(i);
				Integer next = indicesmoved.get(i);
				add(next,unchanged.get(prev),false);
				}
			fireContentsChanged(this, 0, list.size());

			if (updateundo)
				{
				undoManager.addEdit(new UndoableActionEdit(UndoableActionEdit.ACTION_MOVE, indices,
						indicesmoved, null));
				}
			}

		public void moveAll(List<Integer> indices, List<Integer> indicesmoved)
		{
			moveAll(indices, indicesmoved, true);
		}

		public void moveAll(List<Integer> indices, int index)
		{
			List<Integer> indicesmoved = new ArrayList<Integer>();
			for (int i = 0; i < indices.size(); i++) {
				indicesmoved.add(index + i);
			}
			moveAll(indices, indicesmoved, true);
		}

		public Action getElementAt(int index)
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
							{ //Silly user put a standalone Else
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
	private final ArrayList<Action> actions;
	private final DataFlavor[] flavors;

	public ActionTransferable(ArrayList<Action> a)
		{
		actions = a;
		ArrayList<DataFlavor> fl = new ArrayList<DataFlavor>(2);
		fl.add(ACTION_ARRAY_FLAVOR);
		if (a.size() == 1) fl.add(ACTION_FLAVOR);
		flavors = fl.toArray(new DataFlavor[fl.size()]);
		}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
		if (flavor == ACTION_FLAVOR && actions.size() == 1)
			{
			return actions.get(0);
			}
		if (flavor == ACTION_ARRAY_FLAVOR)
			{
			return actions;
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
	private final WeakReference<MDIFrame> parent;
	private ActionList list = null;

	public ActionTransferHandler(WeakReference<MDIFrame> parent, ActionList l)
		{
		super();
		this.parent = parent;
		this.list = l;
		}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
		{
		if (indices != null)
		{
			ActionListModel model = (ActionListModel) list.getModel();
			List<Integer> inds = new ArrayList<Integer>(indices.length);
			int index = addIndex;
			for (int i = 0; i < indices.length; i++)
				{
				inds.add(indices[i]);
				if (indices[i] < addIndex) index--;
				}
			if (action == MOVE)
				{
				if (addIndex != -1)
					{
					model.moveAll(inds, index);
					list.setSelectionInterval(index, index + inds.size() - 1);
					}
				else
					{
					model.removeAll(inds);
					}
				}
		}
		indices = null;
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

		int index = list.getSelectedIndex();
		index = index < 0 ? alm.getSize() : index;
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
				LGM.showDefaultExceptionHandler(e);
				return false;
				}
			//clone properly for drag-copy or clipboard paste
			if (!info.isDrop() || info.getDropAction() == COPY) a = a.copy();
			if (info.isDrop() && info.getDropAction() == MOVE && indices != null)
				{
				addIndex = index;
				}
			else
				{
				alm.add(index, a);
				list.setSelectionInterval(index,index);
				}
			return true;
			}
		if (info.isDataFlavorSupported(ACTION_ARRAY_FLAVOR))
			{
			List<Action> a;
			try
				{
				a = ((List<Action>) t.getTransferData(ACTION_ARRAY_FLAVOR));
				}
			catch (Exception e)
				{
				LGM.showDefaultExceptionHandler(e);
				return false;
				}
			//clone properly for drag-copy or clipboard paste
			if (!info.isDrop() || info.getDropAction() == COPY) for (int i = 0; i < a.size(); i++)
				a.set(i,a.get(i).copy());
			if (info.isDrop() && info.getDropAction() == MOVE && indices != null)
				{
				addIndex = index;
				}
			else
				{
				alm.addAll(index, a);
				list.setSelectionInterval(index,index + a.size() - 1);
				}
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
				LGM.showDefaultExceptionHandler(e);
				return false;
				}

			alm.add(index, a);
			list.setSelectionInterval(index,index);
			return true;
			}
		return false;
		}

	@Override
	public int getSourceActions(JComponent c)
		{
		return COPY_OR_MOVE;
		}

	@Override
	protected Transferable createTransferable(JComponent c)
		{
		indices = list.getSelectedIndices();
		return new ActionTransferable((ArrayList<Action>) list.getSelectedValuesList());
		}
	}

	private static class ActionRenderer implements ListCellRenderer<Action>
		{
		private final WeakHashMap<Action,SoftReference<ActionRendererComponent>> lcrMap;
		private final ActionList list;

		public ActionRenderer(ActionList l)
			{
			super();
			lcrMap = new WeakHashMap<Action,SoftReference<ActionRendererComponent>>();
			this.list = l;
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

		private static class ActionLineComponent extends JLabel {
			/**
			 * NOTE: Default UID generated, change if necessary.
			 */
			private static final long serialVersionUID = 4152567649770789101L;

			private JList<Action> list = null;
			private int index = 0;
			private int maxwidth = 0;

			public ActionLineComponent(int ind, JList<Action> l)
				{
					super();
					this.setText(Integer.toString(ind));
					//setBackground(Color.red);
					index = ind;
					list = l;
				}

			@Override
			public void paintComponent(Graphics g) {
				int width = g.getFontMetrics().stringWidth(this.getText());
				int height = g.getFontMetrics().getHeight();
				g.setColor(this.getBackground());
				g.fillRect(0,0,this.getWidth(),this.getHeight());
				g.setColor(this.getForeground());
				g.drawString(this.getText(),5 + maxwidth - width,(int)((this.getPreferredSize().getHeight() - height)/2 + getFontMetrics(getFont()).getAscent()));
				g.fillRect(5 + maxwidth + 5,0,2,this.getPreferredSize().height);
			}

			public void setIndex(int ind)
				{
				index = ind;
				this.setText(Integer.toString(index));
				}

			public void updatePreferredSize()
				{
					maxwidth = getFontMetrics(getFont()).stringWidth(Integer.toString(list.getModel().getSize() - 1));
					setPreferredSize(new Dimension(5 + maxwidth + 7, this.getPreferredSize().height));
				}
		}

		private static class ActionRendererComponent extends JPanel
			{
			private static final long serialVersionUID = 1L;
			//private int indent;
			private boolean selected;
			private final JList<Action> list;
			JLabel actlabel = null;
			ActionLineComponent linelabel = null;

			public ActionRendererComponent(int index, JList<Action> l)
				{
				this.list = l;
				this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));

				actlabel = new JLabel();
				linelabel = new ActionLineComponent(index, l);

				setOpaque(true);
				setBackground(selected ? list.getSelectionBackground() : list.getBackground());
				actlabel.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
				linelabel.setBackground(list.getBackground());
				linelabel.setForeground(list.getForeground());
				Action a = l.getModel().getElementAt(index);
				LibAction la = a.getLibAction();
				if (la.actImage == null)
					actlabel.setText(Messages.getString("Action.UNKNOWN")); //$NON-NLS-1$
				else
					{
					StringBuilder sb = null;
					// let the user supply their own description using
					// a special comment like in GM8.1 and GMS
					if (a.getLibAction().actionKind == Action.ACT_CODE)
						{
						Pattern r = Pattern.compile("^\\s*//[/!]+\\s*(.+)([\r\n]|$)"); //$NON-NLS-1$
						Matcher m = r.matcher(a.getArguments().get(0).getVal());
						if (m.find())
							{
							sb = new StringBuilder(m.group(1));
							}
						}

					if (sb == null)
						{
						sb = new StringBuilder("<html>"); //$NON-NLS-1$
						if (la.listText.contains("@FI")) //$NON-NLS-1$
							sb.append("<i>"); //$NON-NLS-1$
						if (la.listText.contains("@FB")) //$NON-NLS-1$
							sb.append("<b>"); //$NON-NLS-1$
						sb.append(escape(parse(la.listText,a)));
						}
					actlabel.setText(sb.toString());
					actlabel.setIcon(new ImageIcon(la.actImage));

					if (Prefs.actionToolTipLines > 0 && Prefs.actionToolTipColumns > 0)
						{
						sb = new StringBuilder();
						String snip = parse(la.hintText.replaceAll("(?<!\\\\)#","\n"),a); //$NON-NLS-1$
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
							sb.append('\n');
							}
						if (next != -1) sb.append(Messages.getString("Action.HINT_MORE"));
						setToolTipText("<html><font face=\"Courier\">" + escape(sb.toString()));
						}
					}

				linelabel.setPreferredSize(new Dimension(linelabel.getPreferredSize().width, actlabel.getPreferredSize().height + 4));

				this.add(linelabel);
				this.add(actlabel);

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
				//if (this.indent == indent) return;
				//this.indent = indent;
				actlabel.setBorder(new EmptyBorder(2,2 + 8 * indent,2,2));
				linelabel.updatePreferredSize();
				}

			public void setSelected(boolean selected)
				{
				if (this.selected == selected) return;
				this.selected = selected;
				if (selected)
					{
					setBackground(list.getSelectionBackground());
					actlabel.setForeground(list.getSelectionForeground());
					}
				else
					{
					setBackground(list.getBackground());
					actlabel.setForeground(list.getForeground());
					}
				}

			public void setIndex(int index)
				{
					linelabel.setIndex(index);
				}
			}

		public Component getListCellRendererComponent(JList<? extends Action> l, Action cell,
				int index, boolean isSelected, boolean hasFocus)
			{
			final Action cellAction = (Action) cell;

			SoftReference<ActionRendererComponent> arcref = lcrMap.get(cellAction);
			ActionRendererComponent arc = null;
			if (arcref != null) arc = arcref.get();
			if (arc == null)
				{
				arc = new ActionRendererComponent(index,(JList<Action>) list);
				lcrMap.put(cellAction,new SoftReference<ActionRendererComponent>(arc));
				}
			ListModel<Action> lm = (ListModel<Action>) list.getModel();
			try
				{
				if (lm instanceof ActionListModel)
					arc.setIndent(((ActionListModel) lm).indents.get(index));
					arc.setIndex(index);
				}
			catch (IndexOutOfBoundsException e)
				{
				//Lazy way of dealing with an invalid index value passed in.
				}
			arc.setSelected(isSelected);
			return arc;
			}
		}

	public void ActionsEdit(JList<Action> list)
		{
		int index = list.getSelectedIndex();
		if (index == -1) return;
		ActionListModel alm = (ActionListModel) list.getModel();
		ActionList.openActionFrame(parent.get(),(Action) alm.getElementAt(index));
		}

	public void ActionsCut(JList<Action> list)
		{
		ActionsCopy(list);
		ActionsDelete(list);
		}

	public void ActionsCopy(JList<Action> list)
		{
		int[] indices = list.getSelectedIndices();
		ArrayList<Action> actions = (ArrayList<Action>) list.getSelectedValuesList();
		if (indices.length <= 0) return;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		ActionTransferable at = new ActionTransferable(actions);

		clipboard.setContents(at,this);
		}

	public void ActionsPaste(JList<Action> list)
		{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable clipboardContents = clipboard.getContents(this);

		for (DataFlavor flavor : clipboardContents.getTransferDataFlavors())
			{
			Object content = null;
			try
				{
				content = clipboardContents.getTransferData(flavor);
				}
			catch (UnsupportedFlavorException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			catch (IOException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			if (flavor.equals(ACTION_ARRAY_FLAVOR))
				{
				ActionListModel alm = (ActionListModel) list.getModel();
				@SuppressWarnings("unchecked")
				ArrayList<Action> actions = (ArrayList<Action>) content;
				int ind = list.getSelectedIndex();
				if (ind < 0) {
					ind = alm.getSize();
				}
				alm.addAll(ind, (List<Action>) actions);
				list.setSelectionInterval(ind,ind += actions.size() - 1);
				}
			// throw unsupported flavor exception?
			}
		}

	public static void ActionsUndo(JList<Action> list)
		{
			if (!(list instanceof ActionList)) {
				return;
			}
			ActionList l = (ActionList) list;
			if (l.undomanager.canUndo())
				l.undomanager.undo();
		}

	public static void ActionsRedo(JList<Action> list)
		{
			if (!(list instanceof ActionList)) {
				return;
			}
			ActionList l = (ActionList) list;
			if (l.undomanager.canRedo())
				l.undomanager.redo();
		}

	public static void ActionsDelete(JList<Action> list)
		{
		int[] indices = list.getSelectedIndices();
		ActionListModel alm = (ActionListModel) list.getModel();
		List<Integer> inds = new ArrayList<Integer>();
		for (int i : indices) {
			inds.add(i);
		}
		alm.removeAll(inds);
		if (indices.length != 0) list.setSelectedIndex(Math.min(alm.getSize() - 1,indices[0]));
		}

	public static void ActionsSelectAll(JList<Action> list)
		{
		int start = 0;
		int end = list.getModel().getSize() - 1;
		if (end >= 0)
			{
			list.setSelectionInterval(start,end);
			}
		}

	public static void ActionsClear(JList<Action> list)
		{
		ActionListModel alm = (ActionListModel) list.getModel();
		alm.clear();
		}

	public void actionPerformed(ActionEvent ev)
		{
		String com = ev.getActionCommand();
		if (com.endsWith("EDIT"))
			{
			ActionsEdit(this);
			}
		else if (com.endsWith("CUT"))
			{
			ActionsCut(this);
			}
		else if (com.endsWith("COPY"))
			{
			ActionsCopy(this);
			}
		else if (com.endsWith("PASTE"))
			{
			ActionsPaste(this);
			}
		else if (com.endsWith("UNDO"))
			{
			ActionsUndo(this);
			}
		else if (com.endsWith("REDO"))
			{
			ActionsRedo(this);
			}
		else if (com.endsWith("SELECTALL"))
			{
			ActionsSelectAll(this);
			}
		else if (com.endsWith("DELETE"))
			{
			ActionsDelete(this);
			}
		else if (com.endsWith("CLEAR"))
			{
			ActionsClear(this);
			}

		}

	public void lostOwnership(Clipboard arg0, Transferable arg1)
		{
		// TODO Auto-generated method stub
		// You could hold the transferable in something like a lastTransferable
		// field so if the user hits paste it uses the last transferable instead
		// of doing nothing, assuming this is the purpose of this lost ownership
		// method in Java.
		}

	}
