/*
 * Copyright (C) 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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

	private static String submiturl = "https://github.com/IsmAvatar/LateralGM/issues";
	protected JTextArea debugInfo;
	protected JButton copy;
	protected JButton submit;
	protected JButton cancel;

	private static JButton makeButton(String key, ActionListener listener)
		{
		JButton but = new JButton(Messages.getString(key),LGM.getIconForKey(key));
		but.addActionListener(listener);
		return but;
		}

	public ErrorDialog(Frame parent, String title, String message, Throwable e, String url)
		{
		this(parent,title,message,throwableToString(e), url);
		}
	
	public ErrorDialog(Frame parent, String title, String message, Throwable e)
		{
		this(parent,title,message,throwableToString(e),submiturl);
		}

	public ErrorDialog(Frame parent, String title, String message, String debugInfo, String url)
		{
		super(parent,title,true);
		setResizable(false);

		this.debugInfo = new JTextArea(debugInfo);
		JScrollPane scroll = new JScrollPane(this.debugInfo);

		Dimension dim = new Dimension(scroll.getWidth(),DEBUG_HEIGHT);
		scroll.setPreferredSize(dim);
		copy = makeButton("ErrorDialog.COPY",this); //$NON-NLS-1$
		submit = makeButton("ErrorDialog.SUBMIT",this); //$NON-NLS-1$
		cancel = makeButton("ErrorDialog.CANCEL",this); //$NON-NLS-1$
		
		dim = new Dimension(Math.max(copy.getPreferredSize().width,cancel.getPreferredSize().width),
				copy.getPreferredSize().height);
		submit.setPreferredSize(dim);
		copy.setPreferredSize(dim);
		cancel.setPreferredSize(dim);
		JOptionPane wtfwjd = new JOptionPane(new Object[] { message,scroll },JOptionPane.ERROR_MESSAGE,
				JOptionPane.DEFAULT_OPTION,null,new JButton[] { copy, submit,cancel });
		add(wtfwjd);
		pack();
		setLocationRelativeTo(parent);
		}

	protected static String throwableToString(Throwable e)
		{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == submit)
		{
			try
				{
				Desktop.getDesktop().browse(java.net.URI.create(submiturl));
				}
			catch (IOException e1)
				{
				//TODO: Fail silently I guess?
				e1.printStackTrace();
				}
		} else if (e.getSource() == copy) {
			debugInfo.selectAll();
			debugInfo.copy();
		} else if (e.getSource() == cancel) dispose();
		}
	}
