/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lateralgm.components.impl.IntegerDocument;

public class IntegerField extends JTextField implements DocumentListener
	{
	private static final long serialVersionUID = 1L;
	private int min;
	private int max;
	private int lastGoodVal;
	private int lastUnBoundedVal;
	private boolean setting = false; // prevents recursive calls of setText

	public IntegerField(int min, int max)
		{
		this(min,max,min);
		}

	public IntegerField(int min, int max, int val)
		{
		this.min = min;
		this.max = max;
		lastGoodVal = Math.max(min,Math.min(val,max));
		setDocument(new IntegerDocument(min < 0));
		setText(Integer.toString(lastGoodVal));
		getDocument().addDocumentListener(this);
		}

	public void changedUpdate(DocumentEvent e)
		{
		fireActionPerformed();
		}

	public void insertUpdate(DocumentEvent e)
		{
		fireActionPerformed();
		}

	public void removeUpdate(DocumentEvent e)
		{
		fireActionPerformed();
		}

	public int getIntValue()
		{
		validateInt();
		return lastGoodVal;
		}

	private boolean validateInt()
		{
		try
			{
			lastUnBoundedVal = Integer.parseInt(getText());
			lastGoodVal = Math.max(min,Math.min(lastUnBoundedVal,max));
			}
		catch (NumberFormatException ex)
			{
			return false;
			}
		return true;
		}

	public void setIntValue(int val)
		{
		lastGoodVal = Math.max(min,Math.min(val,max));
		setText(Integer.toString(lastGoodVal));
		}

	public void setRange(int min, int max)
		{
		this.min = min;
		this.max = max;
		validateInt();
		}

	protected void processFocusEvent(FocusEvent e)
		{
		if (e.getID() == FocusEvent.FOCUS_LOST)
			{
			int a = lastGoodVal;
			boolean valid = validateInt();
			int b = lastUnBoundedVal;
			int c = lastGoodVal;
			if (a != b || !valid) setText(Integer.toString(c));
			}
		super.processFocusEvent(e);
		}

	public void setText(String s)
		{
		if (!setting)
			{
			setting = true;
			super.setText(s);
			setting = false;
			}
		}
	}
