/*
 * Copyright (C) 2007 Clam
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class IntegerDocument extends PlainDocument
	{
	private static final long serialVersionUID = 1L;
	private boolean allowNegative;

	public IntegerDocument(boolean allowNegative)
		{
		this.allowNegative = allowNegative;
		}

	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
		{
		if (str == null) return;
		if (allowNegative && offs == 0)
			super.insertString(offs,str.replaceAll("[^0-9-]",""),a);
		else
			super.insertString(offs,str.replaceAll("\\D",""),a);
		}
	}