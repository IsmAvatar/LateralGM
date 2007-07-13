package org.lateralgm.components;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.InternalFrameEvent;

public class MDIFrame extends JInternalFrame
	{
	private static final long serialVersionUID = 1L;
	private JRadioButtonMenuItem item;

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
		item = new JRadioButtonMenuItem();
		item.setText(getTitle());
		item.setIcon(getFrameIcon());
		item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					MDIFrame.this.toTop();
					}
			});
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
				if (pane.isMaximum() && isMaximizable())
					{
					toFront();
					setMaximum(true);
					}
				else
					{
					pane.bringMaximumToTop();
					}
				}
			}
		catch (PropertyVetoException e1)
			{
			e1.printStackTrace();
			}
		}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
		{
		if (item != null)
			{
			if (propertyName.equals(JInternalFrame.TITLE_PROPERTY))
				item.setText(getTitle());
			else if (propertyName.equals(JInternalFrame.FRAME_ICON_PROPERTY))
				item.setIcon((Icon) newValue);
			}
		super.firePropertyChange(propertyName,oldValue,newValue);
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
				pane.getMenu().addRadio(item);
				item.setSelected(true);
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
			else
				pane.getMenu().removeRadio(item);
			}
		}

	protected void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_ACTIVATED)
			{
			item.setSelected(true);
			}
		}
	}
