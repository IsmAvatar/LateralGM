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
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.border.Border;
import javax.swing.plaf.InternalFrameUI;
import org.lateralgm.main.LGM;
import com.sun.java.swing.plaf.windows.WindowsInternalFrameTitlePane;
import com.sun.java.swing.plaf.windows.WindowsInternalFrameUI;

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

	@Override
	public void setUI(InternalFrameUI ui) {
		if (LGM.themename.equals("Windows")) {
			//TODO: This is a fix for UI bugs mentioned below which have been reported.
			super.setUI(new WinInternalFrameUI(this));
		} else {
			super.setUI(ui);
		}
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

	/*
	private Area calculateRectOutside(Rectangle2D r)
		{
		Area outside = new Area(this.getVisibleRect());
		outside.subtract(new Area(r));
		return outside;
		}
*/
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

	public class WinInternalFrameUI extends WindowsInternalFrameUI
		{

		public WinInternalFrameUI(JInternalFrame b)
			{
				super(b);
			}

		protected JComponent createNorthPane(JInternalFrame w)
			{
				return new WinInternalFrameTitlePane(w);
			}

		}

	private class WinInternalFrameTitlePane extends WindowsInternalFrameTitlePane
	{

		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = -8196008182338058385L;

		public WinInternalFrameTitlePane(JInternalFrame f)
			{
			super(f);
			}

		public JButton duplicateButton(JButton button) {
			JButton ret = new JButton() {
				/**
				 * NOTE: Default UID generated, change if necessary.
				 */
				private static final long serialVersionUID = 5193418971949557823L;

				@Override
				public void setBounds(int x, int y, int w, int h) {
					// NOTE: This corrects the buttons from being cut off under the Windows Look and Feel.
					// It makes them both the correct size and moves them to the left close to where
					// they are in native Windows Forms.
					// https://bugs.openjdk.java.net/browse/JDK-8139392
					super.setBounds(x - 6,y,getIcon().getIconWidth(),getIcon().getIconHeight());
				}
			};
			for (ActionListener al : button.getActionListeners()) {
				ret.addActionListener(al);
			}

			ret.setText(button.getText());
			ret.setIcon(button.getIcon());
			return ret;
		}

		@Override
		protected void createButtons() {
			super.createButtons();
			closeButton = duplicateButton(closeButton);
			maxButton = duplicateButton(maxButton);
			iconButton = duplicateButton(iconButton);
		}
	}

	}
