/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.ButtonGroup;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lateralgm.subframes.ResourceFrame;

public class MDIMenu extends GmMenu implements ActionListener,ContainerListener,
		InternalFrameListener,ComponentListener
	{
	private static final long serialVersionUID = 1L;
	private MDIPane pane;
	public static ButtonGroup group = new ButtonGroup();

	public MDIMenu(MDIPane pane)
		{
		super("Window");
		//this.setMnemonic('W');
		this.pane = pane;

		addItem("MDIMenu.CASCADE",this);
		addItem("MDIMenu.ARRANGE_ICONS",this);
		addItem("MDIMenu.CLOSE_ALL",this);
		addItem("MDIMenu.MINIMIZE_ALL",this);
		addSeparator();
		addItem("MDIMenu.CLOSE",this);
		addItem("MDIMenu.CLOSE_OTHERS",this);
		addSeparator();
		JInternalFrame frames[] = pane.getAllFrames();
		for (JInternalFrame f : frames)
			{
			if (f.isVisible())
				{
				FrameItem item = new FrameItem(f);
				item.addActionListener(this);
				add(item);
				}
			f.addInternalFrameListener(this);
			f.addComponentListener(this);
			}
		pane.addContainerListener(this);
		}

	public JMenuItem addItem(String key, ActionListener listener)
		{
		return super.addItem(key,listener);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() instanceof FrameItem)
			{
			JInternalFrame f = ((FrameItem) e.getSource()).frame;
			try
				{
				f.setIcon(false);
				f.setSelected(true);
				}
			catch (PropertyVetoException e1)
				{
				e1.printStackTrace();
				}
			f.toFront();
			}
		else
			{
			if (e.getActionCommand().endsWith("CASCADE"))
				{
				pane.cascadeFrames();
				return;
				}
			if (e.getActionCommand().endsWith("ARRANGE_ICONS"))
				{
				pane.arrangeDesktopIcons();
				return;
				}
			if (e.getActionCommand().endsWith("CLOSE_ALL"))
				{
				pane.closeAll();
				return;
				}
			if (e.getActionCommand().endsWith("MINIMIZE_ALL"))
				{
				pane.iconizeAll();
				return;
				}
			if (e.getActionCommand().endsWith("CLOSE") && pane.getSelectedFrame() != null)
				{
				if (pane.getSelectedFrame() instanceof ResourceFrame)
					try
						{
						pane.getSelectedFrame().setClosed(true);
						}
					catch (PropertyVetoException e1)
						{
						e1.printStackTrace();
						}
				else
					pane.getSelectedFrame().setVisible(false);
				return;
				}
			if (e.getActionCommand().endsWith("CLOSE_OTHERS"))
				{
				pane.closeOthers();
				return;
				}
			}
		}

	class FrameItem extends JRadioButtonMenuItem implements PropertyChangeListener
		{
		private static final long serialVersionUID = 1L;
		public JInternalFrame frame;

		public FrameItem(JInternalFrame frame)
			{
			this.frame = frame;
			setText(frame.getTitle());
			setIcon(frame.getFrameIcon());
			frame.addPropertyChangeListener(this);
			group.add(this);
			}

		public void propertyChange(PropertyChangeEvent evt)
			{
			if (evt.getPropertyName().equals(JInternalFrame.TITLE_PROPERTY))
				setText(frame.getTitle());
			else if (evt.getPropertyName().equals(JInternalFrame.FRAME_ICON_PROPERTY))
				setIcon(frame.getFrameIcon());
			}
		}

	public void componentAdded(ContainerEvent e)
		{
		if (e.getChild() instanceof JInternalFrame)
			{
			JInternalFrame f = (JInternalFrame) e.getChild();
			f.addInternalFrameListener(this);
			f.addComponentListener(this);
			if (f.isVisible())
				{
				for (int i = 0; i < getItemCount(); i++)
					if (getItem(i) instanceof FrameItem)
						if (((FrameItem) getItem(i)).frame == e.getChild()) return;
				FrameItem item = new FrameItem(f);
				item.addActionListener(this);
				item.setSelected(true);
				add(item);
				}
			}
		}

	public void internalFrameClosed(InternalFrameEvent e)
		{
		for (int i = 0; i < getItemCount(); i++)
			if (getItem(i) instanceof FrameItem)
				if (((FrameItem) getItem(i)).frame == e.getInternalFrame())
					{
					remove(i);
					return;
					}
		}

	public void componentShown(ComponentEvent e)
		{
		componentAdded(new ContainerEvent(pane,ContainerEvent.COMPONENT_ADDED,e.getComponent()));
		}

	public void componentHidden(ComponentEvent e)
		{
		internalFrameClosed(new InternalFrameEvent((JInternalFrame) e.getSource(),
				InternalFrameEvent.INTERNAL_FRAME_CLOSED));
		}

	public void internalFrameActivated(InternalFrameEvent e)
		{
		for (int i = 0; i < getItemCount(); i++)
			if (getItem(i) instanceof FrameItem)
				if (((FrameItem) getItem(i)).frame == e.getInternalFrame())
					{
					((FrameItem) getItem(i)).setSelected(true);
					}
		}

	//unused
	public void componentRemoved(ContainerEvent e)
		{
		}

	public void internalFrameClosing(InternalFrameEvent e)
		{
		}

	public void internalFrameDeactivated(InternalFrameEvent e)
		{
		}

	public void internalFrameDeiconified(InternalFrameEvent e)
		{
		}

	public void internalFrameIconified(InternalFrameEvent e)
		{
		}

	public void internalFrameOpened(InternalFrameEvent e)
		{
		}

	public void componentMoved(ComponentEvent e)
		{
		}

	public void componentResized(ComponentEvent e)
		{
		}

	}
