/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2013, 2014 Robert B Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.Container;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.border.Border;

public class MDIFrame extends JInternalFrame
	{
	private static final long serialVersionUID = 1L;
	private Border border;

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

		// real multiple document interfaces hide the window border, it gives us a little extra room
		// and makes it feel not only more native, but resemble DWM's better
/*
		this.addPropertyChangeListener(IS_MAXIMUM_PROPERTY,new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent ev)
				{
					if ((boolean) ev.getNewValue()) {
						System.out.println("wtf");
						border = getBorder();

						setBorder(null);
						((BasicInternalFrameUI) getUI()).setNorthPane(null);;
					} else {
						System.out.println("ass");
						if (border != null) {
							setBorder(border);
						}
					}
				}

		});*/
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

	@Override
	public void setMaximum(boolean b) throws PropertyVetoException
		{
		if (b) {
			//border = getBorder();
			//setBorder(null);
		}
		super.setMaximum(b);
		MDIPane pane = getMDIPane();
		if (pane != null) pane.resizeDesktop();
		}

	@Override
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
