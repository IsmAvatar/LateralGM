/*
 * Copyright (C) 2008 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;

public class ErrorDialog extends JDialog implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private static final int DEBUG_HEIGHT = 200;

	protected JTextArea debugInfo;
	protected JButton copy;
	protected JButton ok;

	private static JButton makeButton(String key, ActionListener listener)
		{
		JButton but = new JButton(Messages.getString(key),LGM.getIconForKey(key));
		but.addActionListener(listener);
		return but;
		}

	public ErrorDialog(Frame parent, String title, String message, String debugInfo)
		{
		super(parent,title,true);
		setResizable(false);

		this.debugInfo = new JTextArea(debugInfo);
		JScrollPane scroll = new JScrollPane(this.debugInfo);

		Dimension dim = new Dimension(scroll.getWidth(),DEBUG_HEIGHT);
		scroll.setPreferredSize(dim);
		copy = makeButton("ErrorDialog.COPY",this); //$NON-NLS-1$
		ok = makeButton("ErrorDialog.OK",this); //$NON-NLS-1$
		dim = new Dimension(Math.max(copy.getPreferredSize().width,ok.getPreferredSize().width),
				copy.getPreferredSize().height);
		copy.setPreferredSize(dim);
		ok.setPreferredSize(dim);
		JOptionPane wtfwjd = new JOptionPane(new Object[] { message,scroll },JOptionPane.ERROR_MESSAGE,
				JOptionPane.DEFAULT_OPTION,null,new JButton[] { copy,ok });
		add(wtfwjd);
		pack();
		setLocationRelativeTo(parent);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == copy)
			{
			debugInfo.selectAll();
			debugInfo.copy();
			}
		else if (e.getSource() == ok) dispose();
		}
	}
