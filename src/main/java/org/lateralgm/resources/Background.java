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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.lang.ref.SoftReference;
import java.util.EnumMap;

import org.lateralgm.main.Util;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class Background extends InstantiableResource<Background,Background.PBackground> implements
		Resource.Viewable
	{
	private BufferedImage backgroundImage = null;
	private SoftReference<BufferedImage> imageCache = null;

	private final BackgroundPropertyListener bpl = new BackgroundPropertyListener();

	public enum PBackground
		{
		TRANSPARENT,SMOOTH_EDGES,PRELOAD,USE_AS_TILESET,TILE_WIDTH,TILE_HEIGHT,H_OFFSET,V_OFFSET,H_SEP,
		V_SEP,TILE_HORIZONTALLY,TILE_VERTICALLY,FOR3D
		}

	private static final EnumMap<PBackground,Object> DEFS = PropertyMap.makeDefaultMap(
			PBackground.class,false,false,false,false,16,16,0,0,0,0,false,false,false);

	public Background()
		{
		this(null);
		}

	public Background(ResourceReference<Background> r)
		{
		super(r);
		properties.getUpdateSource(PBackground.TRANSPARENT).addListener(bpl);
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
		if (get(PBackground.TRANSPARENT)) bi = Util.getTransparentImage(bi);
		imageCache = new SoftReference<BufferedImage>(bi);
		return bi;
		}

	protected void postCopy(Background dest)
		{
		super.postCopy(dest);
		dest.backgroundImage = Util.cloneImage(backgroundImage);
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

	/** Returns the byte length of a DataBuffer **/
	//TODO: This function reports astronomical values for some reason.
	public long getDataBytes(DataBuffer data)
		{
		int dataType = data.getDataType();
		switch (dataType)
			{
			case DataBuffer.TYPE_BYTE:
				byte[] bytes = ((DataBufferByte) data).getData();
				return bytes.length;
			case DataBuffer.TYPE_USHORT:
				short[] shorts = ((DataBufferShort) data).getData();
				return shorts.length * 2;
			case DataBuffer.TYPE_INT:
				int[] ints = ((DataBufferInt) data).getData();
				return ints.length * 4;
			case DataBuffer.TYPE_FLOAT:
				float[] floats = ((DataBufferFloat) data).getData();
				return floats.length * 4;
			case DataBuffer.TYPE_DOUBLE:
				double[] doubles = ((DataBufferDouble) data).getData();
				return doubles.length * 8;
			default:
				throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
			}
		}

	/** Returns the size of the background image in bytes */
	public long getSize()
		{
		if (backgroundImage != null)
			{
			return this.getWidth() * this.getHeight() * 4;//getDataBytes(backgroundImage.getRaster().getDataBuffer());
			}
		return 0;
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

	private class BackgroundPropertyListener extends PropertyUpdateListener<PBackground>
		{
		@Override
		public void updated(PropertyUpdateEvent<PBackground> e)
			{
			switch (e.key)
				{
				case TRANSPARENT:
					fireUpdate();
					break;
				default:
					//TODO: maybe put a failsafe here?
					break;
				}
			}
		}

	}
