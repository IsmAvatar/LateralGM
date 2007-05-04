package org.lateralgm.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NameDocument extends PlainDocument
	{
	private static final long serialVersionUID = 1L;

	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
		{
		if (str == null) return;
		// if (true) return;
		if (offs == 0)
			{
			if (str.matches("[a-zA-Z_]\\w*")) super.insertString(offs,str,a);
			return;
			}
		if (str.matches("\\w*"))
			{
			super.insertString(offs,str,a);
			}
		}
	}