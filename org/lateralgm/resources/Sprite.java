/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;

import javax.imageio.ImageIO;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class Sprite extends Resource<Sprite,Sprite.PSprite>
	{
	public enum BBMode
		{
		AUTO,FULL,MANUAL
		}

	public enum MaskShape
		{
		PRECISE,RECTANGLE,DISK,DIAMOND
		}

	public final ImageList subImages = new ImageList();

	public enum PSprite
		{
		TRANSPARENT,SHAPE,ALPHA_TOLERANCE,SEPARATE_MASK,SMOOTH_EDGES,PRELOAD,ORIGIN_X,ORIGIN_Y,BB_MODE,
		BB_LEFT,BB_RIGHT,BB_TOP,BB_BOTTOM
		}

	private static final EnumMap<PSprite,Object> DEFS = PropertyMap.makeDefaultMap(PSprite.class,
			false,MaskShape.PRECISE,0,false,false,true,0,0,BBMode.AUTO,0,31,0,31);

	private SoftReference<BufferedImage> imageCache = null;

	private final SpritePropertyListener spl = new SpritePropertyListener();

	public Sprite()
		{
		this(null);
		}

	public Sprite(ResourceReference<Sprite> r)
		{
		super(r);
		properties.getUpdateSource(PSprite.TRANSPARENT).addListener(spl);
		properties.getUpdateSource(PSprite.BB_MODE).addListener(spl);
		setName(Prefs.prefixes.get(Kind.SPRITE));
		}

	public Sprite makeInstance(ResourceReference<Sprite> r)
		{
		return new Sprite(r);
		}

	public BufferedImage addSubImage()
		{
		int w = subImages.getWidth();
		int h = subImages.getHeight();
		if (w == 0 || h == 0)
			{
			w = 32;
			h = 32;
			}
		BufferedImage sub = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = sub.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,w,h);
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

	private void updateBoundingBox()
		{
		BBMode mode = get(PSprite.BB_MODE);
		switch (mode)
			{
			case AUTO:
				Rectangle r = get(PSprite.TRANSPARENT) ? getOverallBounds(subImages) : new Rectangle(0,0,
						subImages.getWidth() - 1,subImages.getHeight() - 1);
				put(PSprite.BB_LEFT,r.x);
				put(PSprite.BB_RIGHT,r.x + r.width);
				put(PSprite.BB_TOP,r.y);
				put(PSprite.BB_BOTTOM,r.y + r.height);
				break;
			case FULL:
				put(PSprite.BB_LEFT,0);
				put(PSprite.BB_RIGHT,subImages.getWidth() - 1);
				put(PSprite.BB_TOP,0);
				put(PSprite.BB_BOTTOM,subImages.getHeight() - 1);
				break;
			default:
				break;
			}
		}

	public static Rectangle getOverallBounds(ImageList l)
		{
		Rectangle r = new Rectangle();
		for (BufferedImage bi : l)
			getCropBounds(bi,r);
		if (r.width > 0 && r.height > 0)
			{
			r.width--;
			r.height--;
			}
		return r;
		}

	public static void getCropBounds(BufferedImage img, Rectangle u)
		{
		int transparent = img.getRGB(0,img.getHeight() - 1);
		int width = img.getWidth();
		int height = img.getHeight();
		boolean unz = u.width > 0 && u.height > 0;

		int uy2 = unz ? u.y + u.height - 1 : -1;
		int y2 = height - 1;
		y2loop: for (; y2 > uy2; y2--)
			for (int i = 0; i < width; i++)
				if (img.getRGB(i,y2) != transparent) break y2loop;

		int ux2 = unz ? u.x + u.width - 1 : -1;
		int x2 = width - 1;
		x2loop: for (; x2 > ux2; x2--)
			for (int j = 0; j <= y2; j++)
				if (img.getRGB(x2,j) != transparent) break x2loop;

		int uy1 = unz ? u.y : y2;
		int y1 = 0;
		y1loop: for (; y1 < uy1; y1++)
			for (int i = 0; i < x2; i++)
				if (img.getRGB(i,y1) != transparent) break y1loop;

		int ux1 = unz ? u.x : x2;
		int x1 = 0;
		x1loop: for (; x1 < ux1; x1++)
			for (int j = y1; j < y2; j++)
				if (img.getRGB(x1,j) != transparent) break x1loop;

		u.x = x1;
		u.y = y1;
		u.width = 1 + x2 - x1;
		u.height = 1 + y2 - y1;
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
		if (get(PSprite.TRANSPARENT)) bi = Util.getTransparentIcon(bi);
		imageCache = new SoftReference<BufferedImage>(bi);
		return bi;
		}

	@Override
	protected void postCopy(Sprite dest)
		{
		for (int j = 0; j < subImages.size(); j++)
			dest.subImages.add(Util.cloneImage(subImages.get(j)));
		}

	public Kind getKind()
		{
		return Kind.SPRITE;
		}

	@Override
	protected void fireUpdate()
		{
		if (imageCache != null) imageCache.clear();
		updateBoundingBox();
		super.fireUpdate();
		}

	public final class ImageList extends ArrayList<BufferedImage>
		{
		private static final long serialVersionUID = 1L;

		private ImageList()
			{
			}

		public int getWidth()
			{
			if (size() > 0) return get(0).getWidth();
			return 0;
			}

		public int getHeight()
			{
			if (size() > 0) return get(0).getHeight();
			return 0;
			}

		@Override
		public boolean add(BufferedImage e)
			{
			super.add(e);
			fireUpdate();
			return true;
			}

		@Override
		public void add(int index, BufferedImage element)
			{
			super.add(index,element);
			fireUpdate();
			}

		@Override
		public boolean addAll(Collection<? extends BufferedImage> c)
			{
			boolean u = super.addAll(c);
			if (u) fireUpdate();
			return u;
			}

		@Override
		public boolean addAll(int index, Collection<? extends BufferedImage> c)
			{
			boolean u = super.addAll(index,c);
			if (u) fireUpdate();
			return u;
			}

		public boolean replace(BufferedImage obi, BufferedImage nbi)
			{
			int i = indexOf(obi);
			if (i < 0) return false;
			set(i,nbi);
			return true;
			}

		@Override
		public void clear()
			{
			super.clear();
			fireUpdate();
			}

		@Override
		public BufferedImage remove(int index)
			{
			BufferedImage i = super.remove(index);
			fireUpdate();
			return i;
			}

		@Override
		public boolean remove(Object o)
			{
			boolean u = super.remove(o);
			if (u) fireUpdate();
			return u;
			}

		@Override
		public boolean removeAll(Collection<?> c)
			{
			boolean u = super.removeAll(c);
			if (u) fireUpdate();
			return u;
			}

		@Override
		protected void removeRange(int fromIndex, int toIndex)
			{
			super.removeRange(fromIndex,toIndex);
			fireUpdate();
			}

		@Override
		public boolean retainAll(Collection<?> c)
			{
			boolean u = super.retainAll(c);
			if (u) fireUpdate();
			return u;
			}

		@Override
		public BufferedImage set(int index, BufferedImage element)
			{
			BufferedImage i = super.set(index,element);
			fireUpdate();
			return i;
			}
		}

	@Override
	protected PropertyMap<PSprite> makePropertyMap()
		{
		DEFS.put(PSprite.TRANSPARENT,LGM.currentFile.fileVersion <= 600);
		return new PropertyMap<PSprite>(PSprite.class,this,DEFS);
		}

	private class SpritePropertyListener extends PropertyUpdateListener<PSprite>
		{
		@Override
		public void updated(PropertyUpdateEvent<PSprite> e)
			{
			switch (e.key)
				{
				case TRANSPARENT:
					fireUpdate();
					break;
				case BB_MODE:
					updateBoundingBox();
					break;
				}
			}
		}
	}
