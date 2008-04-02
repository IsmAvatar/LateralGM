/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;

import org.lateralgm.main.PrefsStore;

public class FramePrefsHandler implements ComponentListener,WindowListener,WindowStateListener
	{
	private final JFrame frame;

	public FramePrefsHandler(JFrame frame)
		{
		this.frame = frame;
		frame.pack(); // makes the frame displayable, so that maximizing works
		frame.setMinimumSize(frame.getSize());
		frame.setBounds(PrefsStore.getWindowBounds());
		int state = frame.getExtendedState()
				| (PrefsStore.getWindowMaximized() ? JFrame.MAXIMIZED_BOTH : 0);
		frame.setExtendedState(state);
		frame.addComponentListener(this);
		frame.addWindowListener(this);
		frame.addWindowStateListener(this);
		}

	private boolean isMaximized()
		{
		return (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
		}

	public void componentMoved(ComponentEvent e)
		{
		if (!isMaximized()) PrefsStore.setWindowBounds(frame.getBounds());
		}

	public void componentResized(ComponentEvent e)
		{
		if (!isMaximized()) PrefsStore.setWindowBounds(frame.getBounds());
		}

	public void windowStateChanged(WindowEvent e)
		{
		PrefsStore.setWindowMaximized(isMaximized());
		}

	public void componentHidden(ComponentEvent e)
		{
		}

	public void componentShown(ComponentEvent e)
		{
		}

	public void windowActivated(WindowEvent e)
		{
		}

	public void windowClosed(WindowEvent e)
		{
		}

	public void windowClosing(WindowEvent e)
		{
		}

	public void windowDeactivated(WindowEvent e)
		{
		}

	public void windowDeiconified(WindowEvent e)
		{
		}

	public void windowIconified(WindowEvent e)
		{
		}

	public void windowOpened(WindowEvent e)
		{
		}

	}
