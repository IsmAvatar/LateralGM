/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

public class NumberField extends JFormattedTextField
	{
	private static final long serialVersionUID = 1L;
	public final NumberFormatter formatter;

	public NumberField(int min, int max)
		{
		this(min,max,getFormatter(getIntegerFormat()));
		setColumns(1 + Math.max(numDigits(min),numDigits(max)));
		}

	public NumberField(double min, double max)
		{
		this(min,max,getFormatter(getNumberFormat()));
		}

	public NumberField(Comparable<? extends Number> min, Comparable<? extends Number> max,
			NumberFormatter f)
		{
		super(f);
		formatter = f;
		f.setMinimum(min);
		f.setMaximum(max);
		}

	public void revertEdit()
		{
		setValue(getValue());
		}

	public void commitOrRevert()
		{
		try
			{
			commitEdit();
			}
		catch (ParseException e)
			{
			revertEdit();
			}
		}

	private static NumberFormatter getFormatter(NumberFormat f)
		{
		NumberFormatter nf = new NumberFormatter(f);
		nf.setCommitsOnValidEdit(true);
		return nf;
		}

	private static NumberFormat getIntegerFormat()
		{
		NumberFormat f = NumberFormat.getIntegerInstance();
		f.setGroupingUsed(false);
		return f;
		}

	private static NumberFormat getNumberFormat()
		{
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setGroupingUsed(false);
		return f;
		}

	private static int numDigits(int n)
		{
		return n == 0 ? 1 : 1 + (int) Math.log10(Math.abs(n));
		}
	}
