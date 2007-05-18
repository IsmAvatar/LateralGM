/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * IsmAvatar (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.lateralgm.jedit.SyntaxStyle;

public final class Util
	{
	private Util()
		{
		}

	public static String urlEncode(String s)
		{
		try
			{
			return URLEncoder.encode(s,"UTF-8");
			}
		catch (UnsupportedEncodingException e)
			{
			throw new Error(e);
			}
		}

	public static String urlDecode(String s)
		{
		try
			{
			return URLDecoder.decode(s,"UTF-8");
			}
		catch (UnsupportedEncodingException e)
			{
			throw new Error(e);
			}
		}

	public static Rectangle stringToRectangle(String s, Rectangle defaultValue)
		{
		if (s == null) return defaultValue;
		String[] sa = s.split(" +");
		if (sa.length != 4) return defaultValue;
		int[] ia = new int[4];
		for (int i = 0; i < 4; i++)
			try
				{
				ia[i] = Integer.parseInt(sa[i]);
				}
			catch (NumberFormatException e)
				{
				return defaultValue;
				}
		return new Rectangle(ia[0],ia[1],ia[2],ia[3]);
		}

	public static String rectangleToString(Rectangle r)
		{
		return String.format("%d %d %d %d",r.x,r.y,r.width,r.height);
		}

	public static SyntaxStyle stringToSyntaxStyle(String s, SyntaxStyle defaultValue)
		{
		String[] a;
		Color c;
		try
			{
			a = s.split(" ",2);
			c = new Color(Integer.valueOf(a[0],16));
			}
		catch (NullPointerException npe)
			{
			return defaultValue;
			}
		catch (NumberFormatException nfe)
			{
			return defaultValue;
			}
		boolean i = false, b = false;
		if (a.length > 1)
			{
			i = a[1].matches("(?i).*\\bitalic\\b.*");
			b = a[1].matches("(?i).*\\bbold\\b.*");
			}
		return new SyntaxStyle(c,i,b);
		}

	public static Image getTransparentIcon(BufferedImage i)
		{
		final BufferedImage fi = i;
		ImageFilter filter = new RGBImageFilter()
			{
				public int filterRGB(int x, int y, int rgb)
					{
					if ((rgb | 0xFF000000) == fi.getRGB(0,fi.getHeight() - 1))
						return 0x00FFFFFF & rgb;
					return rgb;
					}
			};
		ImageProducer ip = new FilteredImageSource(i.getSource(),filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
		}
	}
