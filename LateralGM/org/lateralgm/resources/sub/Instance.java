/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.awt.Graphics;

import javax.swing.JComponent;

import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Ref;

public class Instance extends JComponent
	{
	private static final long serialVersionUID = 1L;

	public int x = 0;
	public int y = 0;
	public Ref<GmObject> gmObjectId = null;
	public int instanceId = 0;
	public String creationCode = "";
	public boolean locked = false;

	public void paintless()
		{

		}

	public void paintComponent(Graphics g)
		{
		//wtf... notice how I have to incrementally check against null...
		if (gmObjectId == null || gmObjectId.getRes() == null)
			{
			getParent().remove(this);
			paintless();
			return;
			}
		if (gmObjectId.getRes().sprite == null
				|| gmObjectId.getRes().sprite.getRes() == null
				|| gmObjectId.getRes().sprite.getRes().subImages.size() == 0)
			{
			paintless();
			return;
			}
		g.drawImage(gmObjectId.getRes().sprite.getRes().subImages.get(0),x,y,null);
		}
	}
