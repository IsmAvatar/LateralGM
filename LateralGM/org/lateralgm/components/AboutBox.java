/*
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.lateralgm.messages.Messages;

public class AboutBox extends JDialog
	{
	private static final long serialVersionUID = 1L;

	private LicenseDialog licenseDialog;

	private enum Option
		{
		LICENSE,CLOSE;
		public String toString()
			{
			return Messages.getString("AboutBox.OPTION_" + name());
			}
		};

	public AboutBox(Frame owner)
		{
		super(owner,Messages.getString("AboutBox.TITLE"),true);
		setResizable(false);
		JEditorPane ep = new JEditorPane("text/html",Messages.getString("AboutBox.ABOUT"));
		addSSRules(((HTMLDocument) ep.getDocument()).getStyleSheet());
		ep.setOpaque(false);
		ep.setEditable(false);
		setLinkHandler(ep);
		lockWidth(ep,Math.max(ep.getMinimumSize().width,400));
		JOptionPane op = new JOptionPane(ep,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION,null,
				Option.values());
		op.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY,new OptionHandler());
		add(op);
		pack();
		setLocationRelativeTo(owner);
		}

	private static void addSSRules(StyleSheet s)
		{
		s.addRule("body { font-size: 12pt; font-family: Dialog; }");
		}

	private static void lockWidth(JTextComponent c, int width)
		{
		Dimension max = new Dimension(width,Integer.MAX_VALUE);
		c.setSize(max);
		Dimension min = new Dimension(max.width,c.getPreferredSize().height);
		c.setMaximumSize(max);
		c.setMinimumSize(min);
		c.setPreferredSize(min);
		}

	private static void setLinkHandler(JEditorPane ep)
		{
		try
			{
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
				{
				ep.addHyperlinkListener(new LinkHandler());
				return;
				}
			}
		catch (NoClassDefFoundError e)
			{
			}
		((HTMLEditorKit) ep.getEditorKit()).setLinkCursor(null);
		}

	private void showLicense()
		{
		if (licenseDialog == null) licenseDialog = new LicenseDialog(this);
		licenseDialog.setVisible(true);
		}

	private class OptionHandler implements PropertyChangeListener
		{
		public void propertyChange(PropertyChangeEvent e)
			{
			if (isVisible())
				{
				Object v = e.getNewValue();
				if (!(v instanceof Option)) return;
				switch ((Option) v)
					{
					case CLOSE:
						dispose();
						break;
					case LICENSE:
						showLicense();
						break;
					}
				Object s = e.getSource();
				if (s instanceof JOptionPane) ((JOptionPane) s).setValue(JOptionPane.UNINITIALIZED_VALUE);
				}
			}
		}

	private static class LicenseDialog extends JDialog
		{
		private static final long serialVersionUID = 1L;

		public LicenseDialog(JDialog owner)
			{
			super(owner,Messages.getString("AboutBox.LICENSE_TITLE"),true);
			setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			setResizable(false);
			JEditorPane ep = new JEditorPane("text/html",Messages.getString("AboutBox.LICENSE"));
			addSSRules(((HTMLDocument) ep.getDocument()).getStyleSheet());
			ep.setOpaque(false);
			ep.setEditable(false);
			setLinkHandler(ep);
			lockWidth(ep,400);
			JOptionPane op = new JOptionPane(ep,JOptionPane.PLAIN_MESSAGE);
			op.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY,new OptionHandler());
			add(op);
			pack();
			setLocationRelativeTo(owner);
			}

		private class OptionHandler implements PropertyChangeListener
			{
			public void propertyChange(PropertyChangeEvent evt)
				{
				if (evt.getNewValue().equals(JOptionPane.OK_OPTION)) setVisible(false);
				((JOptionPane) evt.getSource()).setValue(JOptionPane.UNINITIALIZED_VALUE);
				}
			}
		}

	private static class LinkHandler implements HyperlinkListener
		{
		public void hyperlinkUpdate(HyperlinkEvent e)
			{
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported())
				{
				Desktop desktop = Desktop.getDesktop();
				try
					{
					desktop.browse(e.getURL().toURI());
					}
				catch (URISyntaxException use)
					{
					use.printStackTrace();
					}
				catch (IOException ioe)
					{
					ioe.printStackTrace();
					}
				}
			}
		}
	}
