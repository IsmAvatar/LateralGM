/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.Container;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

public class MDIFrame extends JInternalFrame
	{
	private static final long serialVersionUID = 1L;

	public MDIFrame()
		{
		this("",false,false,false,false);
		}

	public MDIFrame(String title)
		{
		this(title,false,false,false,false);
		}

	public MDIFrame(String title, boolean resizable)
		{
		this(title,resizable,false,false,false);
		}

	public MDIFrame(String title, boolean resizable, boolean closable)
		{
		this(title,resizable,closable,false,false);
		}

	public MDIFrame(String title, boolean resizable, boolean closable, boolean maximizable)
		{
		this(title,resizable,closable,maximizable,false);
		}

	public MDIFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable)
		{
		super(title,resizable,closable,maximizable,iconifiable);
		}

	private MDIPane getMDIPane()
		{
		Container c = getParent();
		if (c != null && c instanceof MDIPane) return (MDIPane) c;
		return null;
		}

	public void toTop()
		{
		try
			{
			setVisible(true);
			setIcon(false);
			setSelected(true);
			MDIPane pane = getMDIPane();
			if (pane != null)
				{
				if (pane.isMaximum())
					{
					if (isMaximizable())
						{
						toFront();
						setMaximum(true);
						}
					else
						pane.bringMaximumToTop();
					}
				else
					toFront();
				}
			}
		catch (PropertyVetoException e1)
			{
			e1.printStackTrace();
			}
		}

	public void setMaximum(boolean b) throws PropertyVetoException
		{
		super.setMaximum(b);
		MDIPane pane = getMDIPane();
		if (pane != null) pane.resizeDesktop();
		}

	public void setVisible(boolean visible)
		{
		super.setVisible(visible);
		MDIPane pane = getMDIPane();
		if (pane != null)
			{
			if (visible)
				{
				if (pane.isMaximum() && isMaximizable())
					try
						{
						setMaximum(true);
						}
					catch (PropertyVetoException e)
						{
						e.printStackTrace();
						}
				else
					pane.bringMaximumToTop();
				}
			}
		}
	}
