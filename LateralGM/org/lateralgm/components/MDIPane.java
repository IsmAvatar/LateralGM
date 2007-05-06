/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Component;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

public class MDIPane extends JDesktopPane
	{
	private static final long serialVersionUID = 1L;
	MDIManager manager = new MDIManager(this);

	public void setBounds(int x, int y, int w, int h)
		{
		super.setBounds(x,y,w,h);
		checkDesktopSize();
		}

	public Component add(JInternalFrame f)
		{
		// JInternalFrame[] frames = getAllFrames();
		Component ret = super.add(f);
		return ret;
		}

	public void remove(Component c)
		{
		super.remove(c);
		checkDesktopSize();
		}

	private void checkDesktopSize()
		{

		}
	}