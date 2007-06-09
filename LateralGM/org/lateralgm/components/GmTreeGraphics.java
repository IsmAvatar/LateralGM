/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Sprite;

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

	public static ImageIcon getBlankIcon()
		{
		return new ImageIcon(Util.getTransparentIcon(new BufferedImage(16,16,
				BufferedImage.TYPE_3BYTE_BGR)));
		}

	public static Icon getSpriteIcon(Sprite s)
		{
		if (s == null) return getBlankIcon();
		BufferedImage bi = s.getSubImage(0);
		if (bi == null) return getBlankIcon();
		Image i = bi;
		if (s.transparent) i = Util.getTransparentIcon(bi);

		if (true)
			{
			int w = bi.getWidth();
			int h = bi.getHeight();

			int m;
			if (false)
				m = Math.max(w,h); //GM's scaling - needs stretching
			else
				m = Math.min(w,h); //Needs clipping

			i = i.getScaledInstance((w * 16) / m,(h * 16) / m,BufferedImage.SCALE_DEFAULT);
			}
		else
			{
			i = i.getScaledInstance(16,16,Image.SCALE_DEFAULT); //scale to 16x16 only
			}

		return new ImageIcon(i);
		}

	public Icon getLeafIcon()
		{
		if (last.status == ResNode.STATUS_SECONDARY)
			{
			if (last.kind == Resource.SPRITE)
				{
				Sprite s = LGM.currentFile.sprites.get(last.resourceId);
				return getSpriteIcon(s);
				}
			if (last.kind == Resource.GMOBJECT)
				{
				GmObject o = LGM.currentFile.gmObjects.get(last.resourceId);
				if (o == null) return getBlankIcon();
				Sprite s = LGM.currentFile.sprites.get(o.sprite);
				return getSpriteIcon(s);
				}
			return Resource.ICON[last.kind];
			}
		return getClosedIcon();
		}

	public Icon getNodeIcon(JTree tree, Object val, boolean sel, boolean exp, boolean leaf, int row)
		{
		ResNode node = (ResNode) val;
		if (leaf || node.status == ResNode.STATUS_SECONDARY) return getLeafIcon();
		if (exp) return getOpenIcon();
		return getClosedIcon();
		}
	}
