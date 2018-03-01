/*
 * Copyright (C) 2007, 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.lateralgm.main.Util.deRef;

import java.awt.Component;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
import javax.swing.TransferHandler;
import javax.swing.border.EtchedBorder;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.AbstractImagePreview;
import org.lateralgm.components.visual.ImageToolTip;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.util.PropertyEditor;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ResourceMenu<R extends Resource<R,?>> extends JPanel implements ActionListener,
		UpdateListener,PropertyEditor<ResourceReference<R>>
	{
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JButton button;
	protected ResourceReference<R> selected;
	protected JPopupMenu pm;
	public JMenuItem noResource;
	protected String defStr;
	protected boolean onlyOpen;
	private ActionEvent actionEvent;
	protected Class<? extends Resource<?,?>> kind;
	private MListener mListener = new MListener();
	protected static final ImageIcon GROUP_ICO = LGM.getIconForKey("GmTreeGraphics.GROUP"); //$NON-NLS-1$;
	private final Preview rPreview;
	private final MenuBuilder builder = new MenuBuilder();

	public class ResourceJMenu extends JMenu
		{
		private static final long serialVersionUID = 1L;
		public ResNode node;

		public ResourceJMenu(ResNode node)
			{
			super(node.getUserObject().toString());
			this.node = node;
			node.updateSource.addListener(ResourceMenu.this);
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

	public class ResourceMenuItem extends JMenuItem
		{
		private static final long serialVersionUID = 1L;
		public ResNode node;

		public ResourceMenuItem(ResNode node)
			{
			super(node.getUserObject().toString());
			this.node = node;
			node.updateSource.addListener(ResourceMenu.this);
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

	public static class Preview extends JLabel
		{
		private static final long serialVersionUID = 1L;

		private BufferedImage displayImage;

		public Preview()
			{
			//Must be set or else toolTip won't show
			setToolTipText(new String());
			}

		public <R extends Resource<R,?>>void setResource(ResourceReference<R> r)
			{
			Resource<R,?> res = Util.deRef(r);
			if (res == null || !(res instanceof Resource.Viewable))
				{
				displayImage = null;
				setIcon(null);
				return;
				}
			displayImage = ((Resource.Viewable) res).getDisplayImage();
			ResNode rn = res.getNode();
			setIcon(rn == null ? null : rn.getIcon());
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

	class ResourceTransferHandler<K extends Resource<K,?>> extends TransferHandler
		{
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = 1716244867900780500L;

		private ResourceMenu<K> menu;
		public ResourceTransferHandler(ResourceMenu<K> menu)
			{
			this.menu = menu;
			}

		public int getSourceActions(JComponent c)
			{
			return COPY;
			}

		public boolean canImport(TransferSupport ts)
			{
			if (!ts.isDataFlavorSupported(ResNode.DATA_FLAVOR))
				{
				return false;
				}
			try
				{
				ResNode data = (ResNode) ts.getTransferable().getTransferData(ResNode.DATA_FLAVOR);
				if (data.kind.equals(menu.kind) && data.status == ResNode.STATUS_SECONDARY)
					{
					return true;
					}
				}
			catch (UnsupportedFlavorException | IOException e)
				{
				// Should never occur.
				LGM.showDefaultExceptionHandler(e);
				}
			return false;
		}

		@SuppressWarnings("unchecked")
		public boolean importData(TransferSupport ts)
			{
				try
					{
					ResNode data = (ResNode) ts.getTransferable().getTransferData(ResNode.DATA_FLAVOR);
					menu.setSelected((ResourceReference<K>) data.getRes());
					fireActionPerformed();
					return true;
					}
				catch (UnsupportedFlavorException | IOException e)
					{
					// Should never occur.
					LGM.showDefaultExceptionHandler(e);
					}
				return false;
			}
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
	public ResourceMenu(Class<? extends Resource<?,?>> kind, String def, boolean showDef, int width,
			boolean onlyOpen, boolean preview)
		{
		this.setTransferHandler(new ResourceTransferHandler<R>(this));

		this.kind = kind;
		this.onlyOpen = onlyOpen;
		this.defStr = def;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		label = new JLabel(def);
		label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		label.addMouseListener(mListener);
		button = new JButton(ResNode.ICON.get(kind));
		button.addMouseListener(mListener);
		button.setMaximumSize(button.getPreferredSize());
		int freeWidth = width - (preview ? 40 : 20);
		rPreview = preview ? new Preview() : null;
		SequentialGroup hg = layout.createSequentialGroup();
		ParallelGroup vg = layout.createParallelGroup(Alignment.CENTER,false);
		if (preview)
			{
			hg.addComponent(rPreview,PREFERRED_SIZE,20,PREFERRED_SIZE);
			vg.addComponent(rPreview,PREFERRED_SIZE,20,PREFERRED_SIZE);
			}
		layout.setHorizontalGroup(hg
		/**/.addComponent(label,PREFERRED_SIZE,freeWidth,Integer.MAX_VALUE)
		/**/.addComponent(button,PREFERRED_SIZE,22,PREFERRED_SIZE));
		layout.setVerticalGroup(vg
		/**/.addComponent(label,PREFERRED_SIZE,20,PREFERRED_SIZE)
		/**/.addComponent(button,PREFERRED_SIZE,22,PREFERRED_SIZE));

		pm = new JPopupMenu();
		if (showDef)
			{
			noResource = pm.add(new JMenuItem(def));
			noResource.addActionListener(this);
			}
		populate(kind);
		LGM.root.updateSource.addListener(ResourceMenu.this);
		}

	/**
	 * Creates a Resource Menu of given Resource kind.
	 * @param kind - One of the kind constants defined in Resource (eg Resource.SPRITE)
	 * @param def - The default value to display if no resource is selected
	 * @param showDef - Whether to display the default value as a selectable menu option
	 * @param width - The component width desired
	 */
	public ResourceMenu(Class<? extends Resource<?,?>> kind, String def, boolean showDef, int width)
		{
		this(kind,def,showDef,width,false,canPreview(kind));
		}

	/**
	 * Convenience method for creating a Resource Menu that does display the default value
	 * as a selectable menu option.
	 * @param kind - One of the kind constants defined in Resource (eg Resource.SPRITE)
	 * @param def - The default value to display if no resource is selected (selectable in menu)
	 * @param width - The component width desired
	 */
	public ResourceMenu(Class<? extends Resource<?,?>> kind, String def, int width)
		{
		this(kind,def,true,width,false,canPreview(kind));
		}

	@Override
	public int getBaseline(int width, int height)
		{
		return label.getBaseline(width,height);
		}

	public static boolean canPreview(Class<? extends Resource<?,?>> kind)
		{
		for (Class<?> i : kind.getInterfaces())
			if (i == Resource.Viewable.class) return true;
		return false;
		}

	protected void populate(Class<? extends Resource<?,?>> kind)
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

	private void populate(JComponent parent, ResNode group, Class<? extends Resource<?,?>> kind)
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
		Resource<R,?> r = deRef(res);
		label.setText(r == null ? defStr : r.getName());
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

	//TODO: Possibly replace with addComponentPopupListener?
	//Though maybe not since this is used on a label.
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

	public void updated(UpdateEvent e)
		{
		Util.invokeOnceLater(builder);
		}

	private class MenuBuilder implements Runnable
		{
		public void run()
			{
			pm.removeAll();
			if (noResource != null) pm.add(noResource);
			populate(kind);
			if (selected == null || !Listener.getPrimaryParent(kind).contains(selected))
				setSelected(null);
			setSelected(selected);
			}
		}

	public <K extends Enum<K>>PropertyLink<K,ResourceReference<R>> getLink(PropertyMap<K> m, K k)
		{
		return new ResourceMenuLink<K>(m,k);
		}

	private class ResourceMenuLink<K extends Enum<K>> extends PropertyLink<K,ResourceReference<R>>
			implements ActionListener
		{
		public ResourceMenuLink(PropertyMap<K> m, K k)
			{
			super(m,k);
			reset();
			addActionListener(this);
			}

		protected void setComponent(ResourceReference<R> r)
			{
			setSelected(r);
			}

		@Override
		public void remove()
			{
			super.remove();
			removeActionListener(this);
			}

		@Override
		public void updated(PropertyUpdateEvent<K> e)
			{
			editComponentIfChanged(getSelected());
			}

		public void actionPerformed(ActionEvent e)
			{
			if (selected == null ? map.get(key) == null : selected.equals(map.get(key))) return;
			editProperty(selected);
			}

		}
	}
