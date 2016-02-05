/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.Caret;
import javax.swing.text.NumberFormatter;

public class NumberField extends JFormattedTextField
	{
	private static final long serialVersionUID = 1L;
	protected final NumberFormatter formatter;

	public NumberField(int value)
		{
		this(Integer.MIN_VALUE,Integer.MAX_VALUE,value);
		}

	public NumberField(int min, int max)
		{
		this(min,max,0);
		}

	public NumberField(int min, int max, int value)
		{
		this(min,max,value,getFormatter(getIntegerFormat()));
		setColumns(1 + Math.max(numDigits(min),numDigits(max)));
		}

	public NumberField(double value)
		{
		this(Double.MIN_VALUE,Double.MAX_VALUE,value,getFormatter(getNumberFormat(0)));
		}

	public NumberField(double min, double max, double value)
		{
		this(min,max,value,getFormatter(getNumberFormat(0)));
		}

	public NumberField(double min, double max, double value, int fractionDigits)
		{
		this(min,max,value,getFormatter(getNumberFormat(fractionDigits)));
		}

	public <T extends Number & Comparable<T>> NumberField(T min, T max, T value, NumberFormatter f)
		{
		super(f);
		formatter = f;
		f.setMinimum(min);
		f.setMaximum(max);
		if (value != null)
			setValue(value.compareTo(min) < 0 ? min : value.compareTo(max) > 0 ? max : value);
		}

	public <T extends Number & Comparable<T>>void setRange(T min, T max)
		{
		formatter.setMinimum(min);
		formatter.setMaximum(max);
		commitOrRevert();
		}

	public Integer getIntValue()
		{
		return (Integer) getValue();
		}

	public void revertEdit()
		{
		setValue(getValue());
		}

	public void setCommitsOnValidEdit(boolean val)
		{
		formatter.setCommitsOnValidEdit(val);
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

	private PropertyChangeListener valListener = null;

	/**
	 * equivalent to registering a <code>PropertyChangeListener</code> on <code>value</code>.
	 * @param l
	 */
	public void addValueChangeListener(ValueChangeListener listener)
		{
		if (valListener == null)
			{
			valListener = new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent evt)
						{
						fireValueChange(evt.getOldValue(),evt.getNewValue());
						}
				};
			super.addPropertyChangeListener("value",valListener); //$NON-NLS-1$
			}
		listenerList.add(ValueChangeListener.class,listener);
		//		listener.valueChange(new ValueChangeEvent(this,getValue(),getValue()));
		}

	public void removeValueChangeListener(ValueChangeListener listener)
		{
		listenerList.remove(ValueChangeListener.class,listener);
		}

	protected void fireValueChange(Object oldValue, Object newValue)
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			if (listeners[i] == ValueChangeListener.class)
				((ValueChangeListener) listeners[i + 1]).valueChange(new ValueChangeEvent(this,oldValue,
						newValue));
		}

	//This is a workaround for a java bug causing the caret to jump to 0
	//on focus gain since the value is recalculated. bug #4740914 (rejected)
	protected void processFocusEvent(FocusEvent e)
		{
		if (e.getID() == FocusEvent.FOCUS_GAINED)
			{
			Caret c = getCaret();
			int cd = getCaret().getDot();
			int cm = getCaret().getMark();
			super.processFocusEvent(e);
			//Assumes this won't go out of bounds (e.g. text didn't change).
			//This is normally a safe assumption, since it seems like the value can't change.
			c.setDot(cm);
			c.moveDot(cd);
			}
		else
			super.processFocusEvent(e);
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

	private static NumberFormat getNumberFormat(int fractionDigits)
		{
		NumberFormat f = NumberFormat.getNumberInstance();

		if (fractionDigits !=0)
			{
			f.setMinimumFractionDigits(fractionDigits);
			f.setMaximumFractionDigits(fractionDigits);
			}

		f.setGroupingUsed(false);
		return f;
		}

	private static int numDigits(int n)
		{
		return n == 0 ? 1 : 1 + (int) Math.log10(Math.abs(n));
		}

	public static interface ValueChangeListener extends java.util.EventListener
		{
		void valueChange(ValueChangeEvent evt);
		}

	public static class ValueChangeEvent extends java.util.EventObject
		{
		private static final long serialVersionUID = 1L;
		private Object oldValue, newValue;

		public ValueChangeEvent(Object source, Object oldValue, Object newValue)
			{
			super(source);
			this.oldValue = oldValue;
			this.newValue = newValue;
			}

		public Object getOldValue()
			{
			return oldValue;
			}

		public Object getNewValue()
			{
			return newValue;
			}
		}
	}
