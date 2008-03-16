/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;

public class Sprite extends Resource<Sprite>
	{
	public static final byte BBOX_AUTO = 0;
	public static final byte BBOX_FULL = 1;
	public static final byte BBOX_MANUAL = 2;

	public int width = 32;
	public int height = 32;
	public boolean transparent = true;
	public boolean preciseCC = true;
	public boolean smoothEdges = false;
	public boolean preload = true;
	public int originX = 0;
	public int originY = 0;
	public byte boundingBoxMode = BBOX_AUTO;
	public int boundingBoxLeft = 0;
	public int boundingBoxRight = 31;
	public int boundingBoxTop = 0;
	public int boundingBoxBottom = 31;
	public ArrayList<BufferedImage> subImages = new ArrayList<BufferedImage>();

	private SoftReference<BufferedImage> imageCache = null;

	public Sprite()
		{
		setName(Prefs.prefixes[Resource.SPRITE]);
		}

	public BufferedImage addSubImage()
		{
		BufferedImage sub = new BufferedImage(32,32,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = sub.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,32,32);
		return sub;
		}

	public BufferedImage addSubImage(byte[] imagedata)
		{
		BufferedImage result = null;
		try
			{
			ByteArrayInputStream imagestr = new ByteArrayInputStream(imagedata);
			result = ImageIO.read(imagestr);
			subImages.add(result);
			}
		catch (IOException ex)
			{
			System.err.println(Messages.format("Sprite.ERROR_SUBIMAGE",subImages.size(),getId())); //$NON-NLS-1$
			}
		return result;
		}

	public void addSubImage(BufferedImage image)
		{
		subImages.add(image);
		}

	public BufferedImage copySubImage(int listIndex)// returns a copy of
	// subimage with given index (new subimage is not added to the sprite)
		{
		BufferedImage bf = subImages.get(listIndex);
		if (bf != null)
			{
			BufferedImage bf2 = new BufferedImage(bf.getWidth(),bf.getHeight(),bf.getType());
			bf2.setData(bf.getData());
			return bf2;
			}
		return null;
		}

	public BufferedImage getDisplayImage()
		{
		BufferedImage bi;
		if (imageCache != null)
			{
			bi = imageCache.get();
			if (bi != null)
				{
				return bi;
				}
			}
		if (subImages.size() < 1)
			{
			return null;
			}
		bi = subImages.get(0);
		if (transparent) bi = Util.getTransparentIcon(bi);
		imageCache = new SoftReference<BufferedImage>(bi);
		return bi;
		}

	private Sprite copy(boolean update, ResourceList<Sprite> src)
		{
		Sprite spr = new Sprite();
		spr.width = width;
		spr.height = height;
		spr.transparent = transparent;
		spr.preciseCC = preciseCC;
		spr.smoothEdges = smoothEdges;
		spr.preload = preload;
		spr.originX = originX;
		spr.originY = originY;
		spr.boundingBoxMode = boundingBoxMode;
		spr.boundingBoxLeft = boundingBoxLeft;
		spr.boundingBoxRight = boundingBoxRight;
		spr.boundingBoxTop = boundingBoxTop;
		spr.boundingBoxBottom = boundingBoxBottom;
		for (int j = 0; j < subImages.size(); j++)
			{
			spr.addSubImage(copySubImage(j));
			}
		if (update)
			{
			spr.setName(Prefs.prefixes[Resource.SPRITE] + (src.lastId + 1));
			src.add(spr);
			}
		else
			{
			spr.setId(getId());
			spr.setName(getName());
			}
		return spr;
		}

	public Sprite copy()
		{
		return copy(false,null);
		}

	public Sprite copy(ResourceList<Sprite> src)
		{
		return copy(true,src);
		}

	public byte getKind()
		{
		return SPRITE;
		}

	@Override
	protected void fireStateChanged()
		{
		if (imageCache != null) imageCache.clear();
		super.fireStateChanged();
		}
	}
