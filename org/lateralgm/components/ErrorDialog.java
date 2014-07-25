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
import java.io.File;
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
	private static ErrorDialog myInstance = new ErrorDialog(
			LGM.frame,
			Messages.getString("ErrorDialog.UNCAUGHT_TITLE"), //$NON-NLS-1$
			Messages.getString("ErrorDialog.UNCAUGHT_MESSAGE"),
			"https://github.com/IsmAvatar/LateralGM/issues");

	private String submiturl = LGM.trackerURL;
	protected JTextArea debugInfo;
	protected JButton copy;
	protected JButton submit;
	protected JButton cancel;
	protected JScrollPane scroll;

	public static ErrorDialog getInstance()
		{
		return myInstance;
		}

	public String generateAgnosticInformation()
		{
		String ret = "Operating System: " + System.getProperty("os.name");
		ret += "\nVersion: " + System.getProperty("os.version");
		ret += "\nArchitecture: " + System.getProperty("os.arch");

		ret += "\n\nJava Vendor: " + System.getProperty("java.vendor");
		ret += "\nVersion: " + System.getProperty("java.version");

		ret += "\n\nAvailable processors (cores): " + Runtime.getRuntime().availableProcessors();
		ret += "\nFree memory (bytes): " + Runtime.getRuntime().freeMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();
		ret += "\nMaximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory);
		ret += "\nTotal memory available to JVM (bytes): " + Runtime.getRuntime().totalMemory();

		/* Get a list of all filesystem roots on this system */
		File[] roots = File.listRoots();

		/* For each filesystem root, print some info */
		for (File root : roots)
			{
			ret += "\n\nFile system root: " + root.getAbsolutePath();
			ret += "\nTotal space (bytes): " + root.getTotalSpace();
			ret += "\nFree space (bytes): " + root.getFreeSpace();
			ret += "\nUsable space (bytes): " + root.getUsableSpace();
			}

		ret += "\n\nStack trace:";
		return ret;
		}

	public void appendDebugInfo(String text)
		{
		debugInfo.append("\n" + text);
		debugInfo.setCaretPosition(0);
		}

	public void appendDebugInfo(Throwable e)
		{
		debugInfo.append("\n" + throwableToString(e));
		debugInfo.setCaretPosition(0);
		}

	private static JButton makeButton(String key, ActionListener listener)
		{
		JButton but = new JButton(Messages.getString(key),LGM.getIconForKey(key));
		but.addActionListener(listener);
		return but;
		}

	public ErrorDialog(Frame parent, String title, String message, String url)
		{
		this(parent,title,message,"",url);
		}

	public ErrorDialog(Frame parent, String title, String message, Throwable e, String url)
		{
		this(parent,title,message,throwableToString(e),url);
		}

	public ErrorDialog(Frame parent, String title, String message, Throwable e)
		{
		this(parent,title,message,throwableToString(e),LGM.trackerURL);
		}

	public ErrorDialog(Frame parent, String title, String message, String debugText, String url)
		{
		super(parent,title);
		setResizable(false);
		submiturl = url;

		this.debugInfo = new JTextArea(debugText);
		scroll = new JScrollPane(this.debugInfo);

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
				JOptionPane.DEFAULT_OPTION,null,new JButton[] { copy,submit,cancel });
		add(wtfwjd);
		pack();
		setLocationRelativeTo(parent);
		this.addWindowListener(new java.awt.event.WindowAdapter()
			{
				@Override
				public void windowClosing(java.awt.event.WindowEvent windowEvent)
					{
					debugInfo.setText("");
					}
			});
		}

	@Override
	public void setVisible(boolean visible)
		{
		super.setVisible(visible);
		debugInfo.setText(generateAgnosticInformation());
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
			}
		else if (e.getSource() == copy)
			{
			debugInfo.selectAll();
			debugInfo.copy();
			}
		else if (e.getSource() == cancel)
			{
			debugInfo.setText("");
			dispose();
			}

		}
	}
