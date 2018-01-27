/**
* @file  CustomJToolBar.java
* @brief JToolBar wrapper that stops look and feel changes from breaking custom layouts like for the
* search filter panel and the events toolbar,
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

package org.lateralgm.components;

import java.awt.LayoutManager;

import javax.swing.JToolBar;

public class CustomJToolBar extends JToolBar {
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = 625036878076896510L;

	public CustomJToolBar(int orientation)
		{
			super(orientation);
		}

	public CustomJToolBar()
		{
			super();
		}

	@Override
	public void updateUI() {
		LayoutManager layout = this.getLayout();
		super.updateUI();
		this.setLayout(layout);
	}
}
