/**
* @file  VisibleNode.java
* @brief Class that implements show/hide capabilities for generic tree nodes.
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

package org.lateralgm.components.impl;

import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class DefaultNode extends DefaultMutableTreeNode
	{
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = -6294943318574589195L;

	protected boolean isVisible = true;
	protected Icon icon;

	public Icon getIcon() {
		return icon;
	}

	public Icon getIconisedGroup() {
		return null;
	}

	public Icon getLeafIcon() {
		return getIcon();
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	public TreeNode getChildAt(int index, boolean filterIsActive) {
		if (!filterIsActive) {
			return super.getChildAt(index);
		}
		if (children == null) {
			throw new ArrayIndexOutOfBoundsException("node has no children");
		}

		int realIndex = -1;
		int visibleIndex = -1;
		//NOTE: If you really wanted to be safe you could check the cast of e.nextElement()
		//and count every node that is not DefaultNode based as visible.
		//For now we know we are safe though.
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			DefaultNode node = (DefaultNode) e.nextElement();
			if (node.isVisible()) {
				visibleIndex++;
			}
			realIndex++;
			if (visibleIndex == index) {
				return (TreeNode) children.elementAt(realIndex);
			}
		}

		throw new ArrayIndexOutOfBoundsException("index unmatched");
		//return (TreeNode)children.elementAt(index);
	}

	public int getChildCount(boolean filterIsActive) {
		if (!filterIsActive) {
			return super.getChildCount();
		}
		if (children == null) {
			return 0;
		}

		int count = 0;
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			DefaultNode node = (DefaultNode) e.nextElement();
			if (node.isVisible()) {
				count++;
			}
		}

		return count;
	}

	public DefaultNode(String name)
		{
		super(name);
		}

	public DefaultNode addChild(String name)
		{
		DefaultNode b = new DefaultNode(name);
		add(b);
		return b;
		}

	public boolean getAllowsChildren()
		{
		if (isRoot()) return false;
		return true;
		}

	public void openFrame()
		{
		// TODO Auto-generated method stub
		}

	public class EventNode extends DefaultNode {
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = 6408866430274328299L;
	int emainid, eid;

		public EventNode(String name, int mid, int id)
			{
			super(name);
			emainid = mid;
			eid = id;
			}
	}
}
