/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.Resource;

public class ResourceMenu extends JPanel implements MouseListener,ActionListener
	{
	private static final long serialVersionUID = 1L;
	private final ResourceChangeListener rcl = new ResourceChangeListener();
	private JLabel label;
	private JButton button;
	private Resource selected;
	private JPopupMenu pm;
	private ResourceMenuItem noResource;
	private ActionEvent actionEvent;

	public class ResourceMenuItem extends JMenuItem
		{
		public Resource resource;
		private static final long serialVersionUID = 1L;

		//Must be constructed with at least 1 argument
		@Deprecated
		private ResourceMenuItem()
			{
			}

		public ResourceMenuItem(Resource res)
			{
			super(res.getName());
			resource = res;
			}

		public ResourceMenuItem(String name)
			{
			super(name);
			}

		public ResourceMenuItem(Icon ico, String name)
			{
			super(name,ico);
			}
		}

	public ResourceMenu(Resource selected, String def, boolean showDef, int width)
		{
		setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		LGM.currentFile.addChangeListener(rcl);
		label = new JLabel((selected == null) ? def : selected.getName());
		label.setBorder(BorderFactory.createEtchedBorder());
		label.addMouseListener(this);
		label.setPreferredSize(new Dimension(width - 20,20));
		add(label);
		button = new JButton(Resource.ICON[selected.getKind()]);
		button.addMouseListener(this);
		button.setPreferredSize(new Dimension(20,19));
		add(button);

		pm = new JPopupMenu();
		if (showDef)
			{
			noResource = (ResourceMenuItem) pm.add(new ResourceMenuItem(def));
			noResource.addActionListener(this);
			}
		populate(selected);
		}

	public ResourceMenu(Resource selected, String def, int width)
		{
		this(selected,def,true,width);
		}

	private void populate(Resource selected)
		{
		if (!Prefs.groupKind)
			{
			populate(null,LGM.root,selected.getKind());
			return;
			}
		}

	private void populate(JComponent parent, ResNode group, int kind)
		{
		for (int i = 0; i < group.getChildCount(); i++)
			{
			ResNode child = (ResNode) group.getChildAt(i);
			if (child.status != ResNode.STATUS_SECONDARY)
				{
				ImageIcon groupIco = LGM.getIconForKey("GmTreeGraphics.GROUP"); //$NON-NLS-1$
				ResourceMenuItem newParent = new ResourceMenuItem(groupIco, (String) child.getUserObject());
				parent.add(newParent);
				populate(newParent,child,kind);
				continue;
				}
			if (child.kind != kind) continue;
			Resource r = LGM.currentFile.sprites.get(group.resourceId);
			ResourceMenuItem newParent = new ResourceMenuItem(r);
			newParent.addActionListener(this);
			parent.add(newParent);
			if (Prefs.protectLeaf) populate(newParent,child,kind);
			}
		}

	private class ResourceChangeListener implements ChangeListener
		{
		public void stateChanged(ChangeEvent e)
			{
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

	public Resource getSelected()
		{
		return selected;
		}

	public void actionPerformed(ActionEvent e)
		{
		ResourceMenuItem source = (ResourceMenuItem) e.getSource();
		label.setText(source.getText());
		selected = source.resource;
		fireActionPerformed();
		}

	public void mouseClicked(MouseEvent e)
		{
		if (pm.getComponentCount() == 0) return;
		pm.show(e.getComponent(),e.getX(),e.getY());
		pm.setVisible(true);
		}

	//Unused
	public void mouseEntered(MouseEvent arg0)
		{
		}

	public void mouseExited(MouseEvent arg0)
		{
		}

	public void mousePressed(MouseEvent arg0)
		{
		}

	public void mouseReleased(MouseEvent arg0)
		{
		}
	}
