/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;

public class Sprite extends Resource
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
	public int boundingBoxRight = 0;
	public int boundingBoxTop = 0;
	public int boundingBoxBottom = 0;
	private ArrayList<BufferedImage> subImages = new ArrayList<BufferedImage>();

	public Sprite()
		{
		setName(Prefs.prefixes[Resource.SPRITE]);
		}

	public int NoSubImages()
		{
		return subImages.size();
		}

	public BufferedImage addSubImage()
		{
		BufferedImage sub = null;
		try
			{
			sub = ImageIO.read(LGM.class.getResource("icons/default_sprite.png"));
			subImages.add(sub);
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
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
			System.err.printf(Messages.getString("Sprite.ERROR_SUBIMAGE"),NoSubImages(),getId().getValue());
			System.err.println();
			}
		return result;
		}

	public void addSubImage(BufferedImage image)
		{
		subImages.add(image);
		}

	public BufferedImage getSubImage(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoSubImages()) return subImages.get(ListIndex);
		return null;
		}

	public void removeSubImage(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoSubImages()) subImages.remove(ListIndex);
		}

	public void clearSubImages()
		{
		subImages.clear();
		}

	public BufferedImage copySubImage(int ListIndex)// returns a copy of
	// subimage with given index (new subimage is not added to the sprite)
		{
		BufferedImage bf = getSubImage(ListIndex);
		if (bf != null)
			{
			BufferedImage bf2 = new BufferedImage(bf.getWidth(),bf.getHeight(),bf.getType());
			bf2.setData(bf.getData());
			return bf2;
			}
		return null;
		}

	@SuppressWarnings("unchecked")
	public Sprite copy(boolean update, ResourceList src)
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
		for (int j = 0; j < NoSubImages(); j++)
			{
			spr.addSubImage(copySubImage(j));
			}
		if (update)
			{
			spr.setId(new ResId(++src.lastId));
			spr.setName(Prefs.prefixes[Resource.SPRITE] + src.lastId);
			src.add(spr);
			}
		else
			{
			spr.setId(getId());
			spr.setName(getName());
			}
		return spr;
		}
	}