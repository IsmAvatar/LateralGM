/**
* @file  JSplitPaneExpandable.java
* @brief Extension to JSplitPane that provides double click expansion as an alternative to tiny expand/collapse buttons.
*
* @section License
*
* Copyright (C) 2013-2014 Robert B. Colton
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class JSplitPaneExpandable extends JSplitPane {
	/**
	 * TODO: Change if needed.
	 */
	private static final long serialVersionUID = 1L;
	
	public boolean doubleClickExpandable = false;
	
	public MouseAdapter dividerAdapter = new MouseAdapter() {
		public void mouseReleased(MouseEvent me) {
			if (me.getClickCount() > 1) {
	    	if (getDividerLocation() <= 10) {
	    		setDividerLocation(getLastDividerLocation());
	    	} else {
	    		setLastDividerLocation(getDividerLocation());
	    		setDividerLocation(0);
	    	}
			}
		}
	};
	
	public JSplitPaneExpandable(int orientation, boolean b, JComponent first,
			JComponent second)
		{
			super(orientation, b, first, second);
		}
	
	public JSplitPaneExpandable(int orientation, JComponent first, JComponent second)
		{
			super(orientation, first, second);
		}
	
	// The purpose of this is an alternative and more standard feature found in most software to 
	// those tiny expand/collapse buttons with oneTouchExpandable.
	// * This looks much better than the trashy collapse icons.
	// * More user friendly, they can toggle the behavior much easier.
	// * Standard and found in more software applications.
	public void setDoubleClickExpandable(boolean enable) {
		if (enable && !doubleClickExpandable) {
			BasicSplitPaneUI basicSplitPaneUI = (BasicSplitPaneUI) this.getUI();
			BasicSplitPaneDivider basicSplitPaneDivider = basicSplitPaneUI.getDivider();
			basicSplitPaneDivider.addMouseListener(dividerAdapter);
		} else if (!enable && doubleClickExpandable) {
			BasicSplitPaneUI basicSplitPaneUI = (BasicSplitPaneUI) this.getUI();
			BasicSplitPaneDivider basicSplitPaneDivider = basicSplitPaneUI.getDivider();
			basicSplitPaneDivider.removeMouseListener(dividerAdapter);
		}
	}
}