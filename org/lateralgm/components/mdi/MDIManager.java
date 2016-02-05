/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2014 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

public class MDIManager extends DefaultDesktopManager
	{
	private static final long serialVersionUID = 1L;
	public MDIPane pane;
	public JScrollPane scroll;

	public void setScrollPane(final JScrollPane scroll)
		{
		this.scroll = scroll;
		this.scroll.setAutoscrolls(false);
		this.scroll.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent arg0)
				{
				}

			@Override
			public void componentMoved(ComponentEvent arg0)
				{
				}

			@Override
			public void componentResized(ComponentEvent arg0)
				{
				//resizeDesktop();
				}

			@Override
			public void componentShown(ComponentEvent arg0)
				{
				}

		});
		resizeDesktop();
		}

	public MDIManager(MDIPane pane)
		{
		super();
		this.pane = pane;
		}

	@Override
	public void endResizingFrame(JComponent f)
		{
		super.endResizingFrame(f);
		resizeDesktop();
		}

	@Override
	public void endDraggingFrame(JComponent f)
		{
		super.endDraggingFrame(f);
		resizeDesktop();
		}

	/**
	 * Resizes the desktop to reflect the frame (and icon)
	 * positions. Also repositions the frames so that empty
	 * spaces are removed while preserving the current view
	 * (similar behavior to the windows MDI).
	 */
	public void resizeDesktop()
		{
		Rectangle viewrect = scroll.getViewport().getViewRect();
		int xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE, xmax = 0, ymax = 0;
		for (JInternalFrame f : pane.getAllFrames())
			{
			if (f.isVisible())
				{
				if (!f.isMaximum())
					{
					JComponent comp;
					if (!f.isIcon())
						comp = f;
					else
						comp = f.getDesktopIcon();
					xmin = Math.min(comp.getX(),xmin);
					ymin = Math.min(comp.getY(),ymin);
					xmax = Math.max(comp.getX() + comp.getWidth(),xmax);
					ymax = Math.max(comp.getY() + comp.getHeight(),ymax);
					}
				else
					{
					//
					//scroll.getViewport().setViewSize(scroll.getViewport().getExtentSize());
					//pane.setPreferredSize(new Dimension((int)viewrect.getWidth(),(int)viewrect.getHeight()-200));
					f.setSize(pane.getSize());

					return;
					}
				}
			}
			int xcorrect = 0, ycorrect = 0;
			if (viewrect.x < xmin)
				xcorrect = -viewrect.x;
			else
				xcorrect = -xmin;

			if (viewrect.y < ymin)
				ycorrect = -viewrect.y;
			else
				ycorrect = -ymin;

			Point newviewpos = new Point(viewrect.x + xcorrect,viewrect.y + ycorrect);

			Dimension newPaneSize = new Dimension(
					Math.max(xmax + xcorrect,newviewpos.x + Math.min(viewrect.width,pane.getPreferredSize().width)),
					Math.max(ymax + ycorrect,newviewpos.y + Math.min(viewrect.height,pane.getPreferredSize().height)));

			for (JInternalFrame f1 : pane.getAllFrames())
				{
				if (!f1.isIcon())
					{
					Point p = f1.getLocation();
					f1.setLocation(new Point(p.x + xcorrect,p.y + ycorrect));
					}
				Point p = f1.getDesktopIcon().getLocation();
				f1.getDesktopIcon().setLocation(new Point(p.x + xcorrect,p.y + ycorrect));
				f1.repaint();
				}

			scroll.getViewport().setViewPosition(newviewpos);
			pane.setPreferredSize(newPaneSize);

			pane.getParent().invalidate();
			pane.getParent().validate();
			pane.repaint();
		}
	}
