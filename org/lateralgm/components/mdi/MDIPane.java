/*
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.Component;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import javax.swing.DesktopManager;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import org.lateralgm.main.WeakArrayList;
import org.lateralgm.subframes.InstantiableResourceFrame;

public class MDIPane extends JDesktopPane
	{
	private static final long serialVersionUID = 1L;
	private int offset = -1;
	private static final int OFFSET_WIDTH = 24;
	private static final int OFFSET_HEIGHT = 24;
	private static final int OFFSET_MAX = 9;
	private final MDIMenu menu;
	public static final String SELECTED_FRAME_PROPERTY = "selectedFrame";
	private final WeakHashMap<Component,WeakArrayList<Component>> zMap;

	public MDIMenu getMenu()
		{
		return menu;
		}

	public MDIPane()
		{
		setDesktopManager(new MDIManager(this));
		menu = new MDIMenu(this);
		zMap = new WeakHashMap<Component,WeakArrayList<Component>>();
		}

	public void setScrollPane(JScrollPane scroll)
		{
		MDIManager man = getMDIManager();
		if (man != null) man.setScrollPane(scroll);
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
			if (f instanceof InstantiableResourceFrame<?,?>)
				try
					{
					f.setClosed(true);
					}
				catch (PropertyVetoException e)
					{
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
			if (f != getSelectedFrame()) if (f instanceof InstantiableResourceFrame<?,?>)
				try
					{
					f.setClosed(true);
					}
				catch (PropertyVetoException e)
					{
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

	public MDIFrame add(MDIFrame f)
		{
		super.add(f);
		incrementOffset();
		Rectangle r = f.getBounds();
		r.x = offset * OFFSET_WIDTH;
		r.y = offset * OFFSET_HEIGHT;
		f.setBounds(r);
		return f;
		}

	public void remove(Component c)
		{
		super.remove(c);
		resizeDesktop();
		}

	public void resizeDesktop()
		{
		MDIManager mg = getMDIManager();
		if (mg != null) mg.resizeDesktop();
		}

	private void incrementOffset()
		{
		if (offset >= OFFSET_MAX)
			offset = 0;
		else
			offset++;
		}

	public boolean isMaximum()
		{
		for (JInternalFrame f : getAllFrames())
			if (f.isMaximum()) return true;
		return false;
		}

	public void bringMaximumToTop()
		{
		if (!isMaximum()) return;
		for (JInternalFrame f : getAllFrames())
			if (f.isMaximum() && f instanceof MDIFrame && f.isVisible()) ((MDIFrame) f).toTop();
		}

	private MDIManager getMDIManager()
		{
		DesktopManager man = getDesktopManager();
		if (man == null || !(man instanceof MDIManager)) return null;
		return (MDIManager) man;
		}

	public void setSelectedFrame(JInternalFrame frame)
		{
		JInternalFrame old = getSelectedFrame();
		super.setSelectedFrame(frame);
		this.firePropertyChange(SELECTED_FRAME_PROPERTY,old,frame);
		}

	private void updateZChildren(Component p)
		{
		if (zMap == null) return;
		int fi = getIndexOf(p);
		int fl = getLayer(p);
		int fp = getPosition(p);
		WeakArrayList<Component> l = zMap.get(p);
		if (l == null) return;
		for (WeakReference<Component> cr : l)
			{
			Component c = cr.get();
			int ci = c == null ? -1 : getIndexOf(c);
			if (ci < 0 || ci <= fi) continue;
			setLayer(c,fl,fp);
			}
		}

	/**
	 * Adds given child component to the z list of components such
	 * that it will stay above the given parent component.
	 * This method is particularly useful for allowing a child to
	 * appear on top of a parent component without it monopolizing
	 * the z-space of the rest of the MDI (e.g. on top of everything)
	 * @param p The parent component to appear underneath
	 * @param c The child component to appear on top
	 */
	public void addZChild(Component p, Component c)
		{
		WeakArrayList<Component> l = zMap.get(p);
		if (l == null)
			{
			l = new WeakArrayList<Component>();
			zMap.put(p,l);
			}
		l.add(new WeakReference<Component>(c));
		updateZChildren(p);
		}

	public void setLayer(Component c, int layer, int position)
		{
		super.setLayer(c,layer,position);
		updateZChildren(c);
		}
	}
