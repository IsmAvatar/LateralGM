/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

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