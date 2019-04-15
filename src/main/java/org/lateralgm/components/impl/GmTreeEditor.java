/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreePath;

import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.main.Prefs;

public class GmTreeEditor extends DefaultTreeCellEditor
	{
	public GmTreeEditor(JTree tree, GmTreeGraphics renderer)
		{
		super(tree,renderer);
		}

	public boolean isCellEditable(EventObject event)
		{
		if (event != null && event.getSource() instanceof JTree && event instanceof MouseEvent)
			{
			TreePath path = tree.getPathForLocation(((MouseEvent) event).getX(),
					((MouseEvent) event).getY());
			if (path != null && path.getPathCount() <= 2 && !Prefs.renamableRoots) return false;
			}
		else if (event == null)
			{
			ResNode node = ((ResNode) tree.getLastSelectedPathComponent());
			if (node != null)
				{
				if (Prefs.renamableRoots) return true;
				if (node.status != ResNode.STATUS_PRIMARY) return node.isEditable();
				return false;
				}
			}
		return super.isCellEditable(event);
		}

	protected void determineOffset(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row)
		{
		if (renderer != null)
			{
			GmTreeGraphics g = (GmTreeGraphics) renderer;
			editingIcon = g.getNodeIcon(value,expanded,leaf);
			offset = renderer.getIconTextGap();
			if (editingIcon != null) offset += editingIcon.getIconWidth();
			}
		else
			{
			editingIcon = null;
			offset = 0;
			}
		}
	}
