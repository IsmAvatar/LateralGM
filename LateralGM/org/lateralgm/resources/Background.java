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
	private BufferedImage backgroundImage = null;
	private SoftReference<BufferedImage> imageCache = null;

	public Background()
		{
		this(null,true);
		}

	public Background(ResourceReference<Background> r, boolean update)
		{
		super(r,update);
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

	protected Background copy(ResourceList<Background> src, ResourceReference<Background> ref,
			boolean update)
		{
		Background b = new Background(ref,update);
		b.width = width;
		b.height = height;
		b.transparent = transparent;
		b.smoothEdges = smoothEdges;
		b.preload = preload;
		b.useAsTileSet = useAsTileSet;
		b.tileWidth = tileWidth;
		b.tileHeight = tileHeight;
		b.horizOffset = horizOffset;
		b.vertOffset = vertOffset;
		b.horizSep = horizSep;
		b.vertSep = vertSep;
		b.backgroundImage = copyBackgroundImage();
		if (src != null)
			{
			b.setName(Prefs.prefixes[Resource.BACKGROUND] + (src.lastId + 1));
			src.add(b);
			}
		else
			{
			b.setId(getId());
			b.setName(getName());
			}
		return b;
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

	public BufferedImage getBackgroundImage()
		{
		return backgroundImage;
		}

	public void setBackgroundImage(BufferedImage backgroundImage)
		{
		this.backgroundImage = backgroundImage;
		}
	}
