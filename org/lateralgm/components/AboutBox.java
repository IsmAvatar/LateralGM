/*
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
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
import javax.swing.text.html.StyleSheet;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;

public class AboutBox extends JDialog implements PropertyChangeListener
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
		}

	public AboutBox(Frame owner)
		{
		super(owner,Messages.getString("AboutBox.TITLE"),true);
		setResizable(false);
		JEditorPane ep = new JEditorPane("text/html",Messages.format("AboutBox.ABOUT",LGM.version));
		addSSRules(((HTMLDocument) ep.getDocument()).getStyleSheet());
		ep.setOpaque(false);
		ep.setEditable(false);
		setLinkHandler(ep);
		lockWidth(ep,Math.max(ep.getMinimumSize().width,400));
		JOptionPane op = new JOptionPane(ep,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION,null,
				Option.values());
		op.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY,this);
		add(op);
		pack();
		setLocationRelativeTo(owner);
		}

	protected static void addSSRules(StyleSheet s)
		{
		s.addRule("body { font-size: 12pt; font-family: Dialog; }");
		}

	protected static void lockWidth(JTextComponent c, int width)
		{
		Dimension max = new Dimension(width,Integer.MAX_VALUE);
		c.setSize(max);
		Dimension min = new Dimension(max.width,c.getPreferredSize().height);
		c.setMaximumSize(max);
		c.setMinimumSize(min);
		c.setPreferredSize(min);
		}

	protected static void setLinkHandler(JEditorPane ep)
		{
		try
			{
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
				{
				ep.addHyperlinkListener(new HyperlinkListener()
					{
						public void hyperlinkUpdate(HyperlinkEvent e)
							{
							if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED
									&& Desktop.isDesktopSupported())
								{
								Desktop desktop = Desktop.getDesktop();
								try
									{
									desktop.browse(e.getURL().toURI());
									}
								catch (URISyntaxException use)
									{
									LGM.showDefaultExceptionHandler(use);
									}
								catch (IOException ioe)
									{
									LGM.showDefaultExceptionHandler(ioe);
									}
								}
							}
					});
				return;
				}
			}
		catch (NoClassDefFoundError e)
			{
			//Desktop not defined in Java 1.5
			LGM.showDefaultExceptionHandler(e);
			}
		}

	private void showLicense()
		{
		if (licenseDialog == null) licenseDialog = new LicenseDialog(this);
		licenseDialog.setVisible(true);
		}

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
			op.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY,new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent evt)
						{
						if (evt.getNewValue().equals(JOptionPane.OK_OPTION)) setVisible(false);
						((JOptionPane) evt.getSource()).setValue(JOptionPane.UNINITIALIZED_VALUE);
						}
				});
			add(op);
			pack();
			setLocationRelativeTo(owner);
			}
		}
	}
