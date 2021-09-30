/*
 * Copyright (C) 2021, Robert Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.util;

import javax.swing.ComboBoxModel;

import org.lateralgm.util.ActiveArrayList;

public class ArrayComboBoxModel<E> extends ArrayListModel<E> implements ComboBoxModel<E>
	{
	private Object selectedItem = null;

	public ArrayComboBoxModel(ActiveArrayList<E> l)
		{
		super(l);
		}

	@Override
	public void setSelectedItem(Object anItem)
		{
		selectedItem = anItem;
		}

	@Override
	public Object getSelectedItem()
		{
		return selectedItem;
		}

	}
