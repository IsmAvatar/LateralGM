/**
* @file  OutputManager.java
* @brief Class implementing the output log and symbolic message feedback.
*
* @section License
*
* Copyright (C) 2008, 2009 IsmAvatar <IsmAvatar@gmail.com>
* Copyright (C) 2013, 2014 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.lateralgm.messages.Messages;

public class OutputManager
{

	public static JTabbedPane outputTabs;
	private static JTextPane logPane;
	private static JTable messageTable;

	private static final SimpleAttributeSet ORANGE = new SimpleAttributeSet();
	private static final SimpleAttributeSet RED = new SimpleAttributeSet();

	static
		{
		//because Color.ORANGE looks like it was done by Mark Rothko
		StyleConstants.setForeground(ORANGE,new Color(255,128,0));
		StyleConstants.setForeground(RED,Color.RED);
		}

	private static JMenuItem makeContextButton(Action a)
	{
		String key = "OutputManager." + a.getValue(Action.NAME);
		JMenuItem b = new JMenuItem();
		b.setIcon(LGM.getIconForKey(key));
		b.setText(Messages.getString(key));
		b.setRequestFocusEnabled(false);
		b.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString(key)));
		b.addActionListener(a);
		return b;
	}

	public static void initialize() {
		logPane = new JTextPane();
		logPane.setText(Messages.getString("OutputManager.SESSIONSTARTED") + ": " + (new Date().toString()));
		logPane.setEditable(false);
		logPane.getCaret().setVisible(true); // show the caret anyway
		logPane.setCaretPosition(0);
		logPane.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				return;
			}

			public void focusGained(FocusEvent e) {
				logPane.getCaret().setVisible(true); // show the caret anyway
			}
		});

		AbstractAction aCopy = new AbstractAction("COPY")
		{
			private static final long serialVersionUID = 1L;

			/** @see AbstractAction#actionPerformed(ActionEvent) */
			//r@Override
			public void actionPerformed(ActionEvent e)
			{
				logPane.copy();
			}
		};

		AbstractAction aSelAll = new AbstractAction("SELALL")
		{
			private static final long serialVersionUID = 1L;

			/** @see AbstractAction#actionPerformed(ActionEvent) */
			//r@Override
			public void actionPerformed(ActionEvent e)
			{
				logPane.selectAll();
			}
		};

		final JPopupMenu popup = new JPopupMenu();
		popup.add(makeContextButton(aCopy));
		popup.addSeparator();
		popup.add(makeContextButton(aSelAll));
		logPane.setComponentPopupMenu(popup);

		JScrollPane logScroll = new JScrollPane(logPane);

		outputTabs = new JTabbedPane();
		outputTabs.addTab("Log",logScroll);
		DefaultTableModel model = new DefaultTableModel() {
			/**
			 * NOTE: Default UID generated, change if necessary.
			 */
			private static final long serialVersionUID = -6256028089398003469L;

			@Override
			public boolean isCellEditable(int row, int column) {
				//all cells false
				return false;
			}
		};
		model.addColumn("Type");
		model.addColumn("Origin");
		model.addColumn("Description");

    Object[] data = {LGM.getIconForKey("OutputManager.NOTICE"), "obj_0", "Lorem ipsum dollor sit amet..."};
		model.addRow(data);
    Object[] data2 = {LGM.getIconForKey("OutputManager.WARNING"), "obj_0", "Lorem ipsum dollor sit amet..."};
		model.addRow(data2);
    Object[] data3 = {LGM.getIconForKey("OutputManager.ERROR"), "obj_0", "Lorem ipsum dollor sit amet..."};
		model.addRow(data3);

		messageTable = new JTable(model)
    {
			/**
			 * NOTE: Default UID generated, change if necessary.
			 */
			private static final long serialVersionUID = -1963784072451574899L;

			// Returning the Class of each column will allow different
			// renderers to be used based on Class
			public Class<?> getColumnClass(int column)
			{
				return getValueAt(0, column).getClass();
			}
    };
		outputTabs.addTab("Messages",new JScrollPane(messageTable));

		messageTable.setRowHeight(24);
		messageTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		messageTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		messageTable.getColumnModel().getColumn(2).setPreferredWidth(800);

		outputTabs.setPreferredSize(new Dimension(100, 250));
	}

	public static void append(String text)
		{
		if (logPane == null) return;
		StyledDocument doc = logPane.getStyledDocument();
		//assuming they actually pass us a full
		//warning/error string, this will highlight it
		AttributeSet style = null;
		String lower = text.toLowerCase();
		if (lower.startsWith("warning:"))
			{
			style = ORANGE;
			postWarning(text);
			}
		if (lower.startsWith("error:"))
			{
			style = RED;
			postError(text);
			}
		//do the actual append
		try
			{
			doc.insertString(doc.getLength(),text,style);
			}
		catch (BadLocationException e)
			{ //This can never happen (also, JTextArea does this)
			}
		logPane.setCaretPosition(doc.getLength());
		}

	public static void postWarning(String text)
		{
		Object[] data = {LGM.getIconForKey("OutputManager.WARNING"), "obj_0", text};
		DefaultTableModel model = (DefaultTableModel) messageTable.getModel();
		model.addRow(data);
		}

	public static void postError(String text)
		{
		Object[] data = {LGM.getIconForKey("OutputManager.ERROR"), "obj_0", text};
		DefaultTableModel model = (DefaultTableModel) messageTable.getModel();
		model.addRow(data);
		}

	public void clearLog()
		{
			logPane.setText(null);
		}

	public static void setVisible(boolean visible) {
		outputTabs.setVisible(visible);
	}

}
