/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.propertylink;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.lateralgm.main.LGM;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class DocumentLink<K extends Enum<K>> extends PropertyLink<K,String> implements
		DocumentListener
	{
	public final Document document;

	public DocumentLink(Document d, PropertyMap<K> m, K k)
		{
		super(m,k);
		document = d;
		reset();
		d.addDocumentListener(this);
		}

	protected void setComponent(final String t)
		{
			try {
				document.remove(0,document.getLength());
				document.insertString(0,t,null);
			} catch (BadLocationException e) {
				LGM.showDefaultExceptionHandler(e);
			}
		}

	@Override
	public void remove()
		{
		super.remove();
		document.removeDocumentListener(this);
		}

	public void changedUpdate(DocumentEvent e)
		{
		update();
		}

	public void insertUpdate(DocumentEvent e)
		{
		update();
		}

	public void removeUpdate(DocumentEvent e)
		{
		update();
		}

	private void update()
		{
		try
			{
			editProperty(document.getText(0,document.getLength()));
			}
		catch (BadLocationException e)
			{
			e.printStackTrace();
			}
		}

	@Override
	public void updated(PropertyUpdateEvent<K> e)
		{
		String t = (String) map.get(key);
		if (t == null) t = ""; //$NON-NLS-1$
		int l = t.length();
		try
			{
			if (l == document.getLength() && t.equals(document.getText(0,l))) return;
			}
		catch (BadLocationException e1)
			{
			e1.printStackTrace();
			}
		editComponent(t);
		}
	}
