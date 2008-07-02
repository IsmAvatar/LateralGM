/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;

public class Background extends Resource<Background>
	{
	public int width = 0;
	public int height = 0;
	public boolean transparent = false;
	public boolean smoothEdges = false;
	public boolean preload = false;
	public boolean useAsTileSet = false;
	public int tileWidth = 16;
	public int tileHeight = 16;
	public int horizOffset = 0;
	public int vertOffset = 0;
	public int horizSep = 0;
	public int vertSep = 0;
	public BufferedImage backgroundImage = null;
	private SoftReference<BufferedImage> imageCache = null;

	public Background()
		{
		setName(Prefs.prefixes[Resource.BACKGROUND]);
		}

	public BufferedImage getDisplayImage()
		{
		if (backgroundImage == null) return null;
		BufferedImage bi;
		if (imageCache != null)
			{
			bi = imageCache.get();
			if (bi != null)
				{
				return bi;
				}
			}
		bi = backgroundImage;
		if (transparent) bi = Util.getTransparentIcon(bi);
		imageCache = new SoftReference<BufferedImage>(bi);
		return bi;
		}

	public BufferedImage copyBackgroundImage()
		{
		if (backgroundImage != null)
			{
			BufferedImage bf = backgroundImage;
			BufferedImage bf2 = new BufferedImage(bf.getWidth(),bf.getHeight(),bf.getType());
			bf2.setData(bf.getData());
			return bf2;
			}
		return null;
		}

	public Background copy()
		{
		return copy(false,null);
		}

	public Background copy(ResourceList<Background> src)
		{
		return copy(true,src);
		}

	private Background copy(boolean update, ResourceList<Background> src)
		{
		Background back = new Background();
		back.width = width;
		back.height = height;
		back.transparent = transparent;
		back.smoothEdges = smoothEdges;
		back.preload = preload;
		back.useAsTileSet = useAsTileSet;
		back.tileWidth = tileWidth;
		back.tileHeight = tileHeight;
		back.horizOffset = horizOffset;
		back.vertOffset = vertOffset;
		back.horizSep = horizSep;
		back.vertSep = vertSep;
		back.backgroundImage = copyBackgroundImage();
		if (update)
			{
			back.setName(Prefs.prefixes[Resource.BACKGROUND] + (src.lastId + 1));
			src.add(back);
			}
		else
			{
			back.setId(getId());
			back.setName(getName());
			}
		return back;
		}

	public byte getKind()
		{
		return BACKGROUND;
		}

	@Override
	protected void fireUpdate()
		{
		if (imageCache != null) imageCache.clear();
		super.fireUpdate();
		}
	}
