/*
 * Copyright (C) 2007, 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.lateralgm.main.Util.deRef;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.AbstractImagePreview;
import org.lateralgm.components.visual.ImageToolTip;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;

public class ResourceMenu<R extends Resource<R>> extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JButton button;
	protected ResourceReference<R> selected;
	protected JPopupMenu pm;
	protected JMenuItem noResource;
	protected boolean onlyOpen;
	private ActionEvent actionEvent;
	protected byte kind;
	private MListener mListener = new MListener();
	private final RMUpdatable updatable = new RMUpdatable();
	protected static final ImageIcon GROUP_ICO = LGM.getIconForKey("GmTreeGraphics.GROUP"); //$NON-NLS-1$;
	private final Preview rPreview;

	public static interface Updatable
		{
		void update();
		}

	public class ResourceJMenu extends JMenu implements Updatable
		{
		private static final long serialVersionUID = 1L;
		public ResNode node;

		public ResourceJMenu(ResNode node)
			{
			super(node.getUserObject().toString());
			this.node = node;
			new NodeListener(node,this,true);
			}

		public void update()
			{
			setText(node.getUserObject().toString());
			ResourceMenu.this.setSelected(selected); //update text
			}

		public boolean isVisible()
			{
			return !onlyOpen || hasVisibleChildren();
			}

		private boolean hasVisibleChildren()
			{
			for (int i = 0; i < getPopupMenu().getComponentCount(); i++)
				if (getPopupMenu().getComponent(i).isVisible()) return true;
			return false;
			}
		}

	public class ResourceMenuItem extends JMenuItem implements Updatable
		{
		private static final long serialVersionUID = 1L;
		public ResNode node;

		public ResourceMenuItem(ResNode node)
			{
			super(node.getUserObject().toString());
			this.node = node;
			new NodeListener(node,this,true);
			setIcon(node.getIcon());
			}

		public void setIcon(Icon ico)
			{
			super.setIcon(ico == null ? GROUP_ICO : ico);
			}

		public void update()
			{
			setIcon(node.getIcon());
			setText(node.getUserObject().toString());
			if (selected == node.getRes()) ResourceMenu.this.setSelected(selected); //update text
			}

		public boolean isVisible()
			{
			return !onlyOpen || node.frame != null;
			}
		}

	private static class NodeListener implements ChangeListener
		{
		private static ReferenceQueue<Updatable> refQueue;
		private static Cleaner cleaner;
		protected WeakReference<Updatable> updatable;
		private WeakReference<ResNode> node;
		private boolean onlyNull;

		public NodeListener(ResNode n, Updatable u, boolean onlyNull)
			{
			this.onlyNull = onlyNull;
			if (refQueue == null) refQueue = new ReferenceQueue<Updatable>();
			if (cleaner == null) cleaner = new Cleaner(refQueue);
			node = new WeakReference<ResNode>(n);
			updatable = new WeakReference<Updatable>(u,refQueue);
			n.addChangeListener(this);
			cleaner.add(this);
			}

		public void stateChanged(ChangeEvent e)
			{
			Updatable u = updatable.get();
			if (u == null)
				dispose();
			else if (onlyNull == (e == null)) u.update();
			}

		public void dispose()
			{
			if (node == null) return;
			ResNode n = node.get();
			if (n == null) return;
			synchronized (n)
				{
				n.removeChangeListener(this);
				}
			node = null;
			}

		private static class Cleaner
			{
			protected ReferenceQueue<Updatable> rq;
			protected Hashtable<WeakReference<Updatable>,NodeListener> listeners;
			private Timer timer;
			private CleanerTask task;

			public Cleaner(ReferenceQueue<Updatable> q)
				{
				rq = q;
				}

			public void add(NodeListener l)
				{
				if (listeners == null)
					{
					listeners = new Hashtable<WeakReference<Updatable>,NodeListener>();
					task = new CleanerTask();
					if (timer == null)
						timer = new Timer();
					else
						timer.purge();
					timer.schedule(task,60000,60000);
					}
				listeners.put(l.updatable,l);
				}

			private class CleanerTask extends TimerTask
				{
				public CleanerTask()
					{
					super();
					}

				public void run()
					{
					Reference<? extends Updatable> r;
					while ((r = rq.poll()) != null)
						{
						NodeListener l = listeners.remove(r);
						if (l != null) l.dispose();
						}
					if (listeners.size() == 0)
						{
						cancel();
						listeners = null;
						}
					}
				}
			}
		}

	public static class Preview extends JLabel
		{
		private static final long serialVersionUID = 1L;

		private BufferedImage displayImage;

		public Preview()
			{
			//Must be set or else toolTip won't show
			setToolTipText(""); //$NON-NLS-1$
			}

		public <R extends Resource<R>>void setResource(ResourceReference<R> r)
			{
			Resource<R> res = Util.deRef(r);
			displayImage = res == null ? null : res.getDisplayImage();
			setIcon(displayImage == null ? null : GmTreeGraphics.getScaledIcon(displayImage));
			}

		public JToolTip createToolTip()
			{
			return new ImageToolTip(new AbstractImagePreview()
				{
					private static final long serialVersionUID = 1L;

					public BufferedImage getImage()
						{
						return displayImage;
						}
				});
			}
		}

	/**
	 * Creates a Resource Menu of given Resource kind.
	 * @param kind - One of the kind constants defined in Resource (eg Resource.SPRITE)
	 * @param def - The default value to display if no resource is selected
	 * @param showDef - Whether to display the default value as a selectable menu option
	 * @param width - The component width desired
	 */
	public ResourceMenu(byte kind, String def, boolean showDef, int width)
		{
		this(kind,def,showDef,width,false,canPreview(kind));
		}

	/**
	 * Creates a Resource Menu of given Resource kind.
	 * @param kind - One of the kind constants defined in Resource (eg Resource.SPRITE)
	 * @param def - The default value to display if no resource is selected
	 * @param showDef - Whether to display the default value as a selectable menu option
	 * @param width - The component width desired
	 * @param onlyOpen  - Whether to only show open frames on the menu
	 * @param preview - Whether to display a preview icon
	 */
	public ResourceMenu(byte kind, String def, boolean showDef, int width, boolean onlyOpen,
			boolean preview)
		{
		this.kind = kind;
		this.onlyOpen = onlyOpen;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		label = new JLabel(def);
		label.setBorder(BorderFactory.createEtchedBorder());
		label.addMouseListener(mListener);
		button = new JButton(Resource.ICON[kind]);
		button.addMouseListener(mListener);
		button.setMaximumSize(button.getPreferredSize());
		int freeWidth = width - (preview ? 40 : 20);
		rPreview = preview ? new Preview() : null;
		SequentialGroup hg = layout.createSequentialGroup();
		ParallelGroup vg = layout.createParallelGroup(Alignment.LEADING,false);
		if (preview)
			{
			hg.addComponent(rPreview,PREFERRED_SIZE,20,PREFERRED_SIZE);
			vg.addComponent(rPreview,PREFERRED_SIZE,20,PREFERRED_SIZE);
			}
		layout.setHorizontalGroup(hg
		/**/.addComponent(label,PREFERRED_SIZE,freeWidth,Integer.MAX_VALUE)
		/**/.addComponent(button,PREFERRED_SIZE,20,PREFERRED_SIZE));
		layout.setVerticalGroup(vg
		/**/.addComponent(label,PREFERRED_SIZE,20,PREFERRED_SIZE)
		/**/.addComponent(button,PREFERRED_SIZE,19,PREFERRED_SIZE));

		pm = new JPopupMenu();
		if (showDef)
			{
			noResource = pm.add(new JMenuItem(def));
			noResource.addActionListener(this);
			}
		populate(kind);
		new NodeListener(LGM.root,updatable,false);
		}

	/**
	 * Convenience method for creating a Resource Menu that does display the default value
	 * as a selectable menu option.
	 * @param kind - One of the kind constants defined in Resource (eg Resource.SPRITE)
	 * @param def - The default value to display if no resource is selected (selectable in menu)
	 * @param width - The component width desired
	 */
	public ResourceMenu(byte kind, String def, int width)
		{
		this(kind,def,true,width,false,canPreview(kind));
		}

	public static boolean canPreview(byte kind)
		{
		switch (kind)
			{
			case Resource.SPRITE:
			case Resource.BACKGROUND:
			case Resource.GMOBJECT:
				return true;
			default:
				return false;
			}
		}

	protected void populate(byte kind)
		{
		if (Prefs.groupKind)
			{
			for (int m = 0; m < LGM.root.getChildCount(); m++)
				{
				ResNode group = (ResNode) LGM.root.getChildAt(m);
				if (group.kind == kind)
					{
					populate(pm,group,kind);
					return;
					} //found group
				} //root loop
			} //group kind
		populate(pm,LGM.root,kind);
		return;
		}

	private void populate(JComponent parent, ResNode group, int kind)
		{
		for (int i = 0; i < group.getChildCount(); i++)
			{
			ResNode child = (ResNode) group.getChildAt(i);
			if (child.status != ResNode.STATUS_SECONDARY)
				{

				JMenuItem newParent;
				if (child.getChildCount() == 0)
					newParent = new ResourceMenuItem(child);
				else
					newParent = new ResourceJMenu(child);
				newParent.setIcon(GROUP_ICO);
				parent.add(newParent);
				populate(newParent,child,kind);
				continue;
				}
			if (child.kind != kind) continue;
			ResourceMenuItem newParent = new ResourceMenuItem(child);
			newParent.addActionListener(this);
			parent.add(newParent);
			}
		}

	public void addActionListener(ActionListener il)
		{
		listenerList.add(ActionListener.class,il);
		}

	public void removeActionListener(ActionListener il)
		{
		listenerList.remove(ActionListener.class,il);
		}

	protected void fireActionPerformed()
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ActionListener.class)
				{
				if (actionEvent == null)
					actionEvent = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""); //$NON-NLS-1$
				((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
				}
			}
		}

	public void showPopup(Component c, int x, int y)
		{
		if (pm.getComponentCount() == 0) return;
		pm.show(c,x,y);
		}

	public ResourceReference<R> getSelected()
		{
		return selected;
		}

	public void setSelected(ResourceReference<R> res)
		{
		selected = res;
		Resource<R> r = deRef(res);
		label.setText(r == null ? (noResource != null ? noResource.getText() : "") : r.getName()); //$NON-NLS-1$
		if (rPreview != null) rPreview.setResource(res);
		}

	public void setEnabled(boolean enabled)
		{
		label.setEnabled(enabled);
		button.setEnabled(enabled);
		super.setEnabled(enabled);
		}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e)
		{
		JMenuItem source = (JMenuItem) e.getSource();
		if (source instanceof ResourceMenu.ResourceMenuItem)
			setSelected((ResourceReference<R>) ((ResourceMenuItem) source).node.getRes());
		else
			setSelected(null);
		fireActionPerformed();
		}

	private class MListener extends MouseAdapter
		{
		public MListener()
			{
			super();
			}

		public void mouseClicked(MouseEvent e)
			{
			if (!isEnabled()) return;
			if (pm.getComponentCount() == 0) return;
			showPopup(e.getComponent(),e.getX(),e.getY());
			}
		}

	private class RMUpdatable implements Updatable
		{
		public RMUpdatable()
			{
			super();
			}

		public void update()
			{
			pm.removeAll();
			if (noResource != null) pm.add(noResource);
			populate(kind);
			if (selected == null || !Listener.getPrimaryParent(kind).contains(selected))
				setSelected(null);
			setSelected(selected);
			}
		}
	}
