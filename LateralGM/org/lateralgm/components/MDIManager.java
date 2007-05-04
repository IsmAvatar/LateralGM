package org.lateralgm.components;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;

public class MDIManager extends DefaultDesktopManager
	{
	private static final long serialVersionUID = 1L;
	MDIPane pane;

	public MDIManager(MDIPane pane)
		{
		super();
		this.pane = pane;
		}

	public void endResizingFrame(JComponent f)
		{
		super.endResizingFrame(f);
		resizeDesktop();
		}

	public void endDraggingFrame(JComponent f)
		{
		super.endDraggingFrame(f);
		resizeDesktop();
		}

	public void resizeDesktop()
		{

		}
	}