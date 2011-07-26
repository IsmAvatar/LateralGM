/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.EnumMap;

import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.util.PropertyMap;

public class Background extends Resource<Background,Background.PBackground>
	{
	private BufferedImage backgroundImage = null;
	private SoftReference<BufferedImage> imageCache = null;

	public enum PBackground
		{
		TRANSPARENT,SMOOTH_EDGES,PRELOAD,USE_AS_TILESET,TILE_WIDTH,TILE_HEIGHT,H_OFFSET,V_OFFSET,H_SEP,
		V_SEP
		}

	private static final EnumMap<PBackground,Object> DEFS = PropertyMap.makeDefaultMap(
			PBackground.class,false,false,false,false,16,16,0,0,0,0);

	public Background()
		{
		this(null);
		}

	public Background(ResourceReference<Background> r)
		{
		super(r);
		setName(Prefs.prefixes.get(Kind.BACKGROUND));
		}

	public Background makeInstance(ResourceReference<Background> r)
		{
		return new Background(r);
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
		if (get(PBackground.TRANSPARENT)) bi = Util.getTransparentIcon(bi);
		imageCache = new SoftReference<BufferedImage>(bi);
		return bi;
		}

	protected void postCopy(Background dest)
		{
		dest.backgroundImage = Util.cloneImage(backgroundImage);
		}

	public Kind getKind()
		{
		return Kind.BACKGROUND;
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
		fireUpdate();
		}

	public int getWidth()
		{
		return backgroundImage == null ? 0 : backgroundImage.getWidth();
		}

	public int getHeight()
		{
		return backgroundImage == null ? 0 : backgroundImage.getHeight();
		}

	@Override
	protected PropertyMap<PBackground> makePropertyMap()
		{
		return new PropertyMap<PBackground>(PBackground.class,this,DEFS);
		}
	}
