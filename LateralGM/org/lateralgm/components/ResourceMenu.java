/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.main.LGM;

public class ResourceMenu extends JPanel implements MouseListener
	{
	private static final long serialVersionUID = 1L;
	private final ResourceChangeListener rcl = new ResourceChangeListener();
	private JLabel label;
	private JButton button;
	private JPopupMenu pm;

	public ResourceMenu(byte kind, int width)
		{
		setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		LGM.currentFile.addChangeListener(rcl);
		label = new JLabel("Resource");
		label.setBorder(BorderFactory.createEtchedBorder());
		label.addMouseListener(this);
		label.setPreferredSize(new Dimension(width - 50,20));
		add(label);
		button = new JButton("B");
		button.addMouseListener(this);
		button.setPreferredSize(new Dimension(20,20));
		add(button);
		setPreferredSize(new Dimension(width,20));

		pm = new JPopupMenu();
		pm.add(new JMenuItem("Item"));
		}

	private class ResourceChangeListener implements ChangeListener
		{
		public void stateChanged(ChangeEvent e)
			{
			}
		}

	public void mouseClicked(MouseEvent arg0)
		{
		pm.setVisible(true);
		}

	//Unused
	public void mouseEntered(MouseEvent arg0)
		{
		}

	public void mouseExited(MouseEvent arg0)
		{
		}

	public void mousePressed(MouseEvent arg0)
		{
		}

	public void mouseReleased(MouseEvent arg0)
		{
		}
	}
