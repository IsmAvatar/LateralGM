/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com> *
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Component;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import org.lateralgm.subframes.ResourceFrame;

public class MDIPane extends JDesktopPane
	{
	private static final long serialVersionUID = 1L;
	private int offset = -1;
	private static final int OFFSET_WIDTH = 24;
	private static final int OFFSET_HEIGHT = 24;
	private static final int OFFSET_MAX = 9;

	public MDIPane()
		{
		setDesktopManager(new MDIManager(this));
		}

	public void setScrollPane(JScrollPane scroll)
		{
		((MDIManager) getDesktopManager()).setScrollPane(scroll);
		}

	public void cascadeFrames()
		{
		offset = -1;
		for (JInternalFrame f : getAllFrames())
			{
			if (f.isVisible() && !f.isIcon())
				{
				incrementOffset();
				Rectangle r = f.getBounds();
				r.x = offset * OFFSET_WIDTH;
				r.y = offset * OFFSET_HEIGHT;
				f.setBounds(r);
				f.toFront();
				try
					{
					f.setSelected(true);
					}
				catch (PropertyVetoException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
				}
			}
		resizeDesktop();
		}

	public void arrangeDesktopIcons()
		{
		int x = Integer.MAX_VALUE - 9000, y = getPreferredSize().height;
		for (JInternalFrame f : getAllFrames())
			{
			if (f.isIcon())
				{
				Rectangle r = f.getDesktopIcon().getBounds();
				if (x + r.width * 2 <= getPreferredSize().width)
					x += r.width;
				else
					{
					x = 0;
					y -= r.height;
					}
				r.x = x;
				r.y = y;
				f.getDesktopIcon().setBounds(r);
				}
			}
		resizeDesktop();
		}

	public void closeAll()
		{
		for (JInternalFrame f : getAllFrames())
			{
			if (f instanceof ResourceFrame)
				try
					{
					f.setClosed(true);
					}
				catch (PropertyVetoException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			else
				{
				f.setVisible(false);
				}
			}
		}

	public void closeOthers()
		{
		if (getSelectedFrame() != null) for (JInternalFrame f : getAllFrames())
			{
			if (f != getSelectedFrame()) if (f instanceof ResourceFrame)
				try
					{
					f.setClosed(true);
					}
				catch (PropertyVetoException e)
					{
					// TODO Auto-generated catch block
				e.printStackTrace();
				}
		else
			{
			f.setVisible(false);
			}
			}
		}

	public void iconizeAll()
		{
		for (JInternalFrame f : getAllFrames())
			{
			if (f.isVisible()) try
				{
				f.setIcon(true);
				}
			catch (PropertyVetoException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}
		arrangeDesktopIcons();
		}

	public void setBounds(int x, int y, int w, int h)
		{
		super.setBounds(x,y,w,h);
		resizeDesktop();
		}

	public JInternalFrame add(JInternalFrame f)
		{
		JInternalFrame fix = null;
		if (getSelectedFrame() != null && getSelectedFrame().isMaximum()) fix = getSelectedFrame();
		super.add(f);
		incrementOffset();
		Rectangle r = f.getBounds();
		r.x = offset * OFFSET_WIDTH;
		r.y = offset * OFFSET_HEIGHT;
		f.setBounds(r);
		if (fix != null) fix.toFront();
		return f;
		}

	public void remove(Component c)
		{
		super.remove(c);
		resizeDesktop();
		}

	private void resizeDesktop()
		{
		((MDIManager) getDesktopManager()).resizeDesktop();
		}

	private void incrementOffset()
		{
		if (offset >= OFFSET_MAX)
			offset = 0;
		else
			offset++;
		}
	}
