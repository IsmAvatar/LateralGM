/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

import org.lateralgm.messages.Messages;
import org.lateralgm.subframes.ResourceFrame;

public class MDIMenu extends GmMenu implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private MDIPane pane;
	public static ButtonGroup group = new ButtonGroup();

	public MDIMenu(MDIPane pane)
		{
		super(Messages.getString("MDIMenu.WINDOW"));
		this.pane = pane;

		addItem("MDIMenu.CASCADE",this);
		addItem("MDIMenu.ARRANGE_ICONS",this);
		addItem("MDIMenu.CLOSE_ALL",this);
		addItem("MDIMenu.MINIMIZE_ALL",this);
		addSeparator();
		addItem("MDIMenu.CLOSE",this);
		addItem("MDIMenu.CLOSE_OTHERS",this);
		addSeparator();
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getActionCommand().endsWith("CASCADE"))
			{
			pane.cascadeFrames();
			return;
			}
		if (e.getActionCommand().endsWith("ARRANGE_ICONS"))
			{
			pane.arrangeDesktopIcons();
			return;
			}
		if (e.getActionCommand().endsWith("CLOSE_ALL"))
			{
			pane.closeAll();
			return;
			}
		if (e.getActionCommand().endsWith("MINIMIZE_ALL"))
			{
			pane.iconizeAll();
			return;
			}
		if (e.getActionCommand().endsWith("CLOSE") && pane.getSelectedFrame() != null)
			{
			if (pane.getSelectedFrame() instanceof ResourceFrame)
				try
					{
					pane.getSelectedFrame().setClosed(true);
					}
				catch (PropertyVetoException e1)
					{
					e1.printStackTrace();
					}
			else
				pane.getSelectedFrame().setVisible(false);
			return;
			}
		if (e.getActionCommand().endsWith("CLOSE_OTHERS"))
			{
			pane.closeOthers();
			return;
			}
		}

	public void addRadio(JRadioButtonMenuItem item)
		{
		group.add(item);
		add(item);
		}

	public void removeRadio(JRadioButtonMenuItem item)
		{
		group.remove(item);
		remove(item);
		}
	}
