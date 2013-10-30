/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static org.lateralgm.main.Util.deRef;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Sprite;

public class GmTreeGraphics extends DefaultTreeCellRenderer
	{
	private static final long serialVersionUID = 1L;

	private static ImageIcon blankIcon;
	private ResNode last;

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
		Component com = super.getTreeCellRendererComponent(tree,val,sel,exp,leaf,row,focus);
		//TODO: Make this a preference
		if (((ResNode)val).status == ResNode.STATUS_PRIMARY) {
			com.setFont(com.getFont().deriveFont(Font.BOLD));
		} else {
			com.setFont(com.getFont().deriveFont(Font.PLAIN));
		}
		return com;
		}

	public static ImageIcon getBlankIcon()
		{
		if (blankIcon == null)
			blankIcon = new ImageIcon(new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB));
		return blankIcon;
		}

	public static Icon getScaledIcon(Image i)
		{
		int w = i.getWidth(null);
		int h = i.getHeight(null);

		int m = Math.min(w,h); //Needs clipping
		if (m > 16) i = i.getScaledInstance(w * 16 / m,h * 16 / m,BufferedImage.SCALE_SMOOTH);
		// Crop and/or center the image
		Image i2 = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		int x = 0;
		int y = 0;
		if (w < 16) x = 8 - w / 2;
		if (h < 16) y = 8 - h / 2;
		i2.getGraphics().drawImage(i,x,y,null);
		i = i2;

		return new ImageIcon(i);
		}

	public static Icon getResourceIcon(ResourceReference<?> r)
		{
		Resource<?,?> res = deRef(r);
		if (res != null && res instanceof Resource.Viewable)
			{
			BufferedImage bi = ((Resource.Viewable) res).getDisplayImage();
			if (bi != null) return getScaledIcon(bi);
			}
		return getBlankIcon();
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
			if (last.kind == Sprite.class || last.kind == Background.class || last.kind == GmObject.class)
				return last.getIcon();
		return null;
		}

	public Icon getNodeIcon(Object val, boolean exp, boolean leaf)
		{
		last = (ResNode) val;
		if (leaf || last.status == ResNode.STATUS_SECONDARY) return getLeafIcon();
		if (exp) return getOpenIcon();
		return getClosedIcon();
		}
	}
