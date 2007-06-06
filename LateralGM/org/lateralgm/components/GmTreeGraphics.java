/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.lateralgm.main.LGM;
import org.lateralgm.resources.Resource;

public class GmTreeGraphics extends DefaultTreeCellRenderer
	{
	private ResNode last;
	private static final long serialVersionUID = 1L;

	public GmTreeGraphics()
		{
		super();
		setOpenIcon(LGM.getIconForKey("GmTreeGraphics.GROUP_OPEN")); //$NON-NLS-1$
		setClosedIcon(LGM.getIconForKey("GmTreeGraphics.GROUP")); //$NON-NLS-1$
		setLeafIcon(getClosedIcon());
		}

	public Component getTreeCellRendererComponent(JTree tree, Object val, boolean sel, boolean exp,
			boolean leaf, int row, boolean focus)
		{
		last = (ResNode) val;
		super.getTreeCellRendererComponent(tree,val,sel,exp,leaf,row,focus);
		return this;
		}

	public Icon getLeafIcon()
		{
		if (last.status == ResNode.STATUS_SECONDARY) return Resource.ICON[last.kind];
		return getClosedIcon();
		}

	public Icon getNodeIcon(JTree tree, Object val, boolean sel, boolean exp, boolean leaf, int row)
		{
		ResNode node = (ResNode) val;
		if (leaf) if (node.status == ResNode.STATUS_SECONDARY) return Resource.ICON[node.kind];
		if (exp) return getOpenIcon();
		return getClosedIcon();
		}
	}
