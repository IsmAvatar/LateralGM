/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class IntegerField extends JTextField implements DocumentListener
	{
	private static final long serialVersionUID = 1L;
	private int min;
	private int max;
	private int lastGoodVal;
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
		try
			{
			int val = Integer.parseInt(getText());
			lastGoodVal = Math.max(min,Math.min(val,max));
			}
		catch (NumberFormatException ex)
			{
			}
		return lastGoodVal;
		}

	public void setIntValue(int val)
		{
		lastGoodVal = Math.max(min,Math.min(val,max));
		setText(Integer.toString(lastGoodVal));
		}

	protected void processFocusEvent(FocusEvent e)
		{
		if (e.getID() == FocusEvent.FOCUS_LOST)
			{
			setText(Integer.toString(getIntValue()));
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
