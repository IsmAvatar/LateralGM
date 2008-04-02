/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static org.lateralgm.main.Util.deRef;
import static org.lateralgm.resources.Resource.BACKGROUND;
import static org.lateralgm.resources.Resource.GMOBJECT;
import static org.lateralgm.resources.Resource.SPRITE;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Background;
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
		setBorder(BorderFactory.createEmptyBorder(1,0,0,0));
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

	public static Icon getScaledIcon(Image i)
		{
		if (true)
			{
			int w = i.getWidth(null);
			int h = i.getHeight(null);

			int m;
			if (false)
				m = Math.max(w,h); //GM's scaling - needs stretching
			else
				m = Math.min(w,h); //Needs clipping
			if (m > 16) i = i.getScaledInstance(w * 16 / m,h * 16 / m,BufferedImage.SCALE_SMOOTH);
			// Crop and/or center the image
			Image i2 = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
			int x = 0;
			int y = 0;
			if (w < 16) x = 8 - w / 2;
			if (h < 16) y = 8 - h / 2;
			i2.getGraphics().drawImage(i,x,y,null);
			i = i2;
			}
		else
			{
			i = i.getScaledInstance(16,16,Image.SCALE_DEFAULT); //scale to 16x16 only
			}

		return new ImageIcon(i);
		}

	public static Icon getSpriteIcon(WeakReference<Sprite> s)
		{
		if (s == null || s.get() == null || s.get().subImages.size() == 0) return getBlankIcon();
		Sprite spr = s.get();
		BufferedImage bi = spr.getDisplayImage();
		if (bi == null) return getBlankIcon();
		return getScaledIcon(bi);
		}

	public static Icon getBackgroundIcon(WeakReference<Background> b)
		{
		Background back = deRef(b);
		if (back == null) return getBlankIcon();
		if (back.backgroundImage == null) return getBlankIcon();
		Image i = back.backgroundImage;
		if (back.transparent) i = Util.getTransparentIcon(back.backgroundImage);
		return getScaledIcon(i);
		}

	public Icon getLeafIcon()
		{
		if (last.status == ResNode.STATUS_SECONDARY) return last.getIcon();
		return getClosedIcon();
		}

	public Icon getClosedIcon()
		{
		Icon ico = getIconisedGroup();
		if (ico != null) return ico;
		return super.getClosedIcon();
		}

	public Icon getOpenIcon()
		{
		Icon ico = getIconisedGroup();
		if (ico != null) return ico;
		return super.getOpenIcon();
		}

	private Icon getIconisedGroup()
		{
		if (Prefs.iconizeGroup && last != null && last.status != ResNode.STATUS_PRIMARY)
			if ((last.kind == SPRITE || last.kind == BACKGROUND || last.kind == GMOBJECT))
				{
				return last.getIcon();
				}
		return null;
		}

	public Icon getNodeIcon(Object val, boolean exp, boolean leaf)
		{
		ResNode node = (ResNode) val;
		if (leaf || node.status == ResNode.STATUS_SECONDARY) return getLeafIcon();
		if (exp) return getOpenIcon();
		return getClosedIcon();
		}
	}
