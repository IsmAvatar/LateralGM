/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
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
import org.lateralgm.messages.Messages;

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
	public int boundingBoxRight = 31;
	public int boundingBoxTop = 0;
	public int boundingBoxBottom = 31;
	private ArrayList<BufferedImage> subImages = new ArrayList<BufferedImage>();

	public Sprite()
		{
		setName(Prefs.prefixes[Resource.SPRITE]);
		}

	public int noSubImages()
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
			System.err.printf(Messages.getString("Sprite.ERROR_SUBIMAGE"),noSubImages(),
					getId().getValue());
			System.err.println();
			}
		return result;
		}

	public void addSubImage(BufferedImage image)
		{
		subImages.add(image);
		}

	public BufferedImage getSubImage(int listIndex)
		{
		if (listIndex >= 0 && listIndex < noSubImages()) return subImages.get(listIndex);
		return null;
		}

	public void removeSubImage(int listIndex)
		{
		if (listIndex >= 0 && listIndex < noSubImages()) subImages.remove(listIndex);
		}

	public void clearSubImages()
		{
		subImages.clear();
		}

	public BufferedImage copySubImage(int listIndex)// returns a copy of
	// subimage with given index (new subimage is not added to the sprite)
		{
		BufferedImage bf = getSubImage(listIndex);
		if (bf != null)
			{
			BufferedImage bf2 = new BufferedImage(bf.getWidth(),bf.getHeight(),bf.getType());
			bf2.setData(bf.getData());
			return bf2;
			}
		return null;
		}

	@SuppressWarnings("unchecked")
	private Sprite copy(boolean update, ResourceList src)
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
		for (int j = 0; j < noSubImages(); j++)
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
	
	public Sprite copy()
		{
		return copy(false,null);
		}

	@SuppressWarnings("unchecked")
	public Sprite copy(ResourceList src)
		{
		return copy(true,src);
		}

	public byte getKind()
		{
		return SPRITE;
		}
	}
