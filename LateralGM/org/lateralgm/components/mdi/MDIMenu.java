/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.WeakHashMap;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GmMenu;
import org.lateralgm.messages.Messages;
import org.lateralgm.subframes.ResourceFrame;

public class MDIMenu extends GmMenu implements ActionListener,ContainerListener
	{
	private static final long serialVersionUID = 1L;
	private MDIPane pane;
	private final ButtonGroup group = new ButtonGroup();
	protected final WeakHashMap<MDIFrame,FrameButton> frameButtons;

	public MDIMenu(MDIPane pane)
		{
		super(Messages.getString("MDIMenu.WINDOW"));
		this.pane = pane;
		frameButtons = new WeakHashMap<MDIFrame,FrameButton>();
		pane.addContainerListener(this);
		addItem("MDIMenu.CASCADE",this); //$NON-NLS-1$
		addItem("MDIMenu.ARRANGE_ICONS",this); //$NON-NLS-1$
		addItem("MDIMenu.CLOSE_ALL",this); //$NON-NLS-1$
		addItem("MDIMenu.MINIMIZE_ALL",this); //$NON-NLS-1$
		addSeparator();
		addItem("MDIMenu.CLOSE",this); //$NON-NLS-1$
		addItem("MDIMenu.CLOSE_OTHERS",this); //$NON-NLS-1$
		addSeparator();
		}

	public void actionPerformed(ActionEvent e)
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

	protected void addRadio(FrameButton item)
		{
		group.add(item);
		add(item);
		}

	protected void removeRadio(FrameButton item)
		{
		group.remove(item);
		remove(item);
		}

	public void componentAdded(ContainerEvent e)
		{
		Component c = e.getChild();
		if (c instanceof MDIFrame) new FrameButton((MDIFrame) c);
		}

	public void componentRemoved(ContainerEvent e)
		{
		Component c = e.getChild();
		FrameButton b = frameButtons.get(c);
		if (b != null) b.dispose();
		}

	private class FrameButton extends JRadioButtonMenuItem implements PropertyChangeListener
		{
		private static final long serialVersionUID = 1L;
		private IFListener ifl = new IFListener();
		private CListener cl = new CListener();
		protected MDIFrame mdif;

		public FrameButton(MDIFrame f)
			{
			mdif = f;
			frameButtons.put(f,this);
			addRadio(this);
			f.addInternalFrameListener(ifl);
			f.addPropertyChangeListener(this);
			f.addComponentListener(cl);
			update();
			setVisible(mdif.isVisible());
			addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						mdif.toTop();
						}
				});
			}

		public void dispose()
			{
			frameButtons.remove(mdif);
			removeRadio(this);
			mdif.removeInternalFrameListener(ifl);
			mdif.removePropertyChangeListener(this);
			mdif.removeComponentListener(cl);
			}

		private void update()
			{
			setText(mdif.getTitle());
			setIcon(mdif.getFrameIcon());
			}

		public void propertyChange(PropertyChangeEvent evt)
			{
			update();
			}

		private class IFListener extends InternalFrameAdapter
			{
			public IFListener()
				{
				super();
				}

			public void internalFrameActivated(InternalFrameEvent e)
				{
				setSelected(true);
				}
			}

		private class CListener extends ComponentAdapter
			{
			public CListener()
				{
				super();
				}

			public void componentHidden(ComponentEvent e)
				{
				setVisible(false);
				}

			public void componentShown(ComponentEvent e)
				{
				setVisible(true);
				}
			}
		}
	}
