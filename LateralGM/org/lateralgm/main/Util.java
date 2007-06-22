/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO
 * WARRANTY. See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.lateralgm.components.CustomFileFilter;
import org.lateralgm.components.ImagePreview;
import org.lateralgm.jedit.SyntaxStyle;
import org.lateralgm.messages.Messages;

public final class Util
	{
	private Util()
		{
		}

	public static JFileChooser imageFc;

	static
		{
		imageFc = new JFileChooser();
		imageFc.setAccessory(new ImagePreview(imageFc));
		String exts[] = ImageIO.getReaderFileSuffixes();
		for (int i = 0; i < exts.length; i++)
			exts[i] = "." + exts[i]; //$NON-NLS-1$
		String allSpiImages = Messages.getString("Util.ALL_SPI_IMAGES"); //$NON-NLS-1$
		CustomFileFilter filt = new CustomFileFilter(exts,allSpiImages);
		imageFc.addChoosableFileFilter(filt);
		for (int i = 0; i < exts.length; i++)
			{
			imageFc.addChoosableFileFilter(new CustomFileFilter(exts[i],exts[i]
					+ Messages.getString("Util.FILES"))); //$NON-NLS-1$
			}
		imageFc.setFileFilter(filt);
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
		if (i == null) return null;
		final BufferedImage fi = i;
		ImageFilter filter = new RGBImageFilter()
			{
				public int filterRGB(int x, int y, int rgb)
					{
					if ((rgb | 0xFF000000) == fi.getRGB(0,fi.getHeight() - 1)) return 0x00FFFFFF & rgb;
					return rgb;
					}
			};
		ImageProducer ip = new FilteredImageSource(i.getSource(),filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
		}

	/**
	 * Shows a JFileChooser with file filters for all currently registered instances of
	 * ImageReaderSpi.
	 * 
	 * @return The selected image, or null if one is not chosen
	 * @throws IOException In the case of an invalid file or IO error
	 */
	public static BufferedImage getValidImage() throws IOException
		{
		if (imageFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			BufferedImage img = ImageIO.read(imageFc.getSelectedFile());
			// TODO support animated formats
			if (img != null)
				{
				ColorConvertOp conv = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB),null);
				BufferedImage dest = new BufferedImage(img.getWidth(),img.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
				conv.filter(img,dest);
				return dest;
				}
			else
				throw new IOException("Invalid image file");
			}
		return null;
		}

	/**
	 * Converts as many leading spaces as possible in each line of the given string to "\t".
	 * Every group of spaces of length <code>Prefs.tabWidth</code> will be converted.
	 * Any existing tabs will be preserved.
	 * @param s The String to convert.
	 * @return The converted String.
	 */

	public static String convertIndents(String s)
		{
		//TODO: correct the line splitting behaviour (try for example "\n\n\n" as an input)
		String lines[] = s.split("\n");
		StringBuffer buf = new StringBuffer();
		for (String str : lines)
			{
			char[] chr = str.toCharArray();
			int i;
			int realtabs = 0;
			for (i = 0; i < chr.length; i++)
				if (chr[i] == ' ')
					continue;
				else if (chr[i] == '\t')
					{
					realtabs++;
					continue;
					}
				else
					break;

			int count = (i - realtabs) / Prefs.tabSize + realtabs;
			for (int j = 0; j < count; j++)
				buf.append('\t');
			buf.append(str.substring((count - realtabs) * Prefs.tabSize + realtabs));
			buf.append('\n');
			}
		if (!s.endsWith("\n")) buf.deleteCharAt(buf.length() - 1);
		return buf.toString();
		}
	}
