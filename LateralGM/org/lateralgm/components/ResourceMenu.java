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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Sprite;

public class ResourceMenu extends JPanel implements MouseListener,ActionListener
	{
	private static final long serialVersionUID = 1L;
	private final ResourceChangeListener rcl = new ResourceChangeListener();
	private JLabel label;
	private JButton button;
	private Resource selected;
	private JPopupMenu pm;
	private JMenuItem noResource;
	private ActionEvent actionEvent;

	public class ResourceMenuItem extends JMenuItem
		{
		public Resource resource;
		private static final long serialVersionUID = 1L;

		//Must be constructed with a Resource argument
		@Deprecated
		private ResourceMenuItem()
			{
			throw new UnsupportedOperationException();
			}

		public ResourceMenuItem(Resource res)
			{
			super(res.getName());
			resource = res;
			if (res.getKind() == Resource.SPRITE)
				{
				setIcon(GmTreeGraphics.getSpriteIcon((Sprite) res));
				}
			if (res.getKind() == Resource.GMOBJECT)
				{
				Sprite s = LGM.currentFile.sprites.get(((GmObject) res).sprite);
				setIcon(GmTreeGraphics.getSpriteIcon(s));
				}
			}
		}

	public ResourceMenu(byte kind, String def, boolean showDef, int width)
		{
		setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		LGM.currentFile.addChangeListener(rcl);
		label = new JLabel();
		label.setBorder(BorderFactory.createEtchedBorder());
		label.addMouseListener(this);
		label.setPreferredSize(new Dimension(width - 20,20));
		add(label);
		button = new JButton(Resource.ICON[kind]);
		button.addMouseListener(this);
		button.setPreferredSize(new Dimension(20,19));
		add(button);

		pm = new JPopupMenu();
		if (showDef)
			{
			noResource = pm.add(new JMenuItem(def));
			noResource.addActionListener(this);
			}
		populate(kind);
		}

	public ResourceMenu(byte kind, String def, int width)
		{
		this(kind,def,true,width);
		}

	private void populate(byte kind)
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
				ImageIcon groupIco = LGM.getIconForKey("GmTreeGraphics.GROUP"); //$NON-NLS-1$
				JMenuItem newParent;
				if (child.getChildCount() == 0)
					{
					newParent = new JMenuItem((String) child.getUserObject(), groupIco);
					parent.add(newParent);
					continue;
					}
				newParent = new JMenu((String) child.getUserObject());
				newParent.setIcon(groupIco);
				parent.add(newParent);
				populate(newParent,child,kind);
				continue;
				}
			if (child.kind != kind) continue;
			Resource r = LGM.currentFile.getList(kind).get(child.resourceId);
			ResourceMenuItem newParent = new ResourceMenuItem(r);
			newParent.addActionListener(this);
			parent.add(newParent);
			if (!Prefs.protectLeaf) populate(newParent,child,kind);
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

	public void setSelected(Resource res)
		{
		selected = res;
		label.setText((res == null) ? noResource.getText() : res.getName());
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == noResource)
			{
			label.setText(noResource.getText());
			selected = null;
			fireActionPerformed();
			return;
			}
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
