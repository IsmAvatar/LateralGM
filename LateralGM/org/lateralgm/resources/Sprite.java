/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
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
		this(null,true);
		}

	public Sprite(ResourceReference<Sprite> r, boolean update)
		{
		super(r,update);
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

	/**
	 * returns a copy of subimage with given index (new subimage is not added to the sprite)
	 */
	public BufferedImage copySubImage(int listIndex)
		{
		BufferedImage bf = subImages.get(listIndex);
		if (bf == null) return null;
		BufferedImage bf2 = new BufferedImage(bf.getWidth(),bf.getHeight(),bf.getType());
		bf2.setData(bf.getData());
		return bf2;
		}

	public BufferedImage getDisplayImage()
		{
		BufferedImage bi;
		if (imageCache != null)
			{
			bi = imageCache.get();
			if (bi != null) return bi;
			}
		if (subImages.size() < 1) return null;
		bi = subImages.get(0);
		if (transparent) bi = Util.getTransparentIcon(bi);
		imageCache = new SoftReference<BufferedImage>(bi);
		return bi;
		}

	@Override
	protected Sprite copy(ResourceList<Sprite> src, ResourceReference<Sprite> ref, boolean update)
		{
		Sprite s = new Sprite(ref,update);
		s.width = width;
		s.height = height;
		s.transparent = transparent;
		s.preciseCC = preciseCC;
		s.smoothEdges = smoothEdges;
		s.preload = preload;
		s.originX = originX;
		s.originY = originY;
		s.boundingBoxMode = boundingBoxMode;
		s.boundingBoxLeft = boundingBoxLeft;
		s.boundingBoxRight = boundingBoxRight;
		s.boundingBoxTop = boundingBoxTop;
		s.boundingBoxBottom = boundingBoxBottom;
		for (int j = 0; j < subImages.size(); j++)
			s.addSubImage(copySubImage(j));
		if (src != null)
			{
			s.setName(Prefs.prefixes[Resource.SPRITE] + (src.lastId + 1));
			src.add(s);
			}
		else
			{
			s.setId(getId());
			s.setName(getName());
			}
		return s;
		}

	public byte getKind()
		{
		return SPRITE;
		}

	@Override
	protected void fireUpdate()
		{
		if (imageCache != null) imageCache.clear();
		super.fireUpdate();
		}
	}
