/**
* @file  AutoComboBox.java
* @brief Class implementing a JComboBox class with auto-completion features.
*
* @section License
*
* Copyright (C) 2014 Robert B. Colton
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
package org.lateralgm.util;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

public class AutoComboBox<T> extends JComboBox<T>
	{
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = -2358279864039989215L;

	JTextComponent editor;
	DocumentFilter document;
	// flag to indicate if setSelectedItem has been called
	// subsequent calls to remove/insertString should be ignored
	boolean selecting = false;

	public AutoComboBox(T[] items)
		{
		super(items);
		this.setEditable(true);
		editor = (JTextComponent) getEditor().getEditorComponent();
		// change the editor's document
		document = new DocumentFilter();
		editor.setDocument(document);

		addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					if (!selecting) document.highlightCompletedText(0);
					}
			});
		editor.addKeyListener(new KeyAdapter()
			{
				public void keyPressed(KeyEvent e)
					{
					if (isDisplayable()) setPopupVisible(true);
					}
			});
		}

	private Object lookupItem(String pattern)
		{
		Object selectedItem = getSelectedItem();
		Object ret = null;
		// only search for a different item if the currently selected does not match
		if (selectedItem != null)
			{
			if (selectedItem.toString().startsWith(pattern))
				{
				return selectedItem;
				}
			else if (selectedItem.toString().toUpperCase().startsWith(pattern.toUpperCase()))
				{
				ret = selectedItem;
				}
			}

		// iterate over all items
		ComboBoxModel<T> model = this.getModel();

		for (int i = 0, n = model.getSize(); i < n; i++)
			{
			Object currentItem = model.getElementAt(i);
			// current item starts with the pattern?
			if (currentItem.toString().startsWith(pattern))
				{
				return currentItem;
				}
			else if (currentItem.toString().toUpperCase().startsWith(pattern.toUpperCase()))
				{
				ret = currentItem;
				}
			}

		// no item starts with the pattern => return null
		return ret;
		}

	public void setSelectedItem(Object item)
		{
		selecting = true;
		super.setSelectedItem(item);
		selecting = false;
		}

	public class DocumentFilter extends PlainDocument
		{

		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = 7860466445717335935L;

		public void remove(int offs, int len) throws BadLocationException
			{
			// return immediately when selecting an item
			//if (selecting) return;
			super.remove(offs,len);
			}

		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
			{
			// insert the string into the document
			super.insertString(offs,str,a);
			// return immediately when selecting an item
			if (selecting) return;
			// lookup and select a matching item
			Object item = lookupItem(getText(0,getLength()));
			if (item != null)
				{
				setSelectedItem(item);
				}
			else
				{
				// keep old item selected if there is no match
				item = getSelectedItem();
				// imitate no insert (later on offs will be incremented by str.length(): selection won't move forward)
				offs = offs - str.length();
				// provide feedback to the user that his input has been received but can not be accepted
				getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
				}
			setText(item.toString());
			// select the completed part
			highlightCompletedText(offs + str.length());
			}

		private void setText(String text) throws BadLocationException
			{
			// remove all text and insert the completed string
			super.remove(0,getLength());
			super.insertString(0,text,null);
			}

		private void highlightCompletedText(int start)
			{
			editor.setSelectionStart(start);
			editor.setSelectionEnd(getLength());
			}

		}
	}
