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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
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
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.visual.FileChooserImagePreview;
import org.lateralgm.file.iconio.ICOImageReaderSPI;
import org.lateralgm.file.iconio.WBMPImageReaderSpiFix;
import org.lateralgm.jedit.SyntaxStyle;
import org.lateralgm.messages.Messages;

import com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi;

public final class Util
	{
	private Util()
		{
		}

	public static CustomFileChooser imageFc = null;

	public static void tweakIIORegistry()
		{
		IIORegistry reg = IIORegistry.getDefaultInstance();
		reg.registerServiceProvider(new ICOImageReaderSPI());
		reg.deregisterServiceProvider(reg.getServiceProviderByClass(WBMPImageReaderSpi.class));
		reg.registerServiceProvider(new WBMPImageReaderSpiFix());
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

	public static BufferedImage toBufferedImage(Image image)
		{
		if (image instanceof BufferedImage) return (BufferedImage) image;

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		BufferedImage bimage = new BufferedImage(image.getWidth(null),image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = bimage.createGraphics();
		g.drawImage(image,0,0,null);
		g.dispose();
		return bimage;
		}

	public static BufferedImage getTransparentIcon(BufferedImage i)
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
		return toBufferedImage(Toolkit.getDefaultToolkit().createImage(ip));
		}

	/**
	 * Shows a JFileChooser with file filters for all currently registered instances of
	 * ImageReaderSpi.
	 * 
	 * @return The selected image, or null if one is not chosen
	 */
	public static BufferedImage getValidImage()
		{
		BufferedImage[] img = getValidImages();
		if (img == null || img.length == 0) return null;
		return img[0];
		}

	public static BufferedImage[] getValidImages()
		{
		if (imageFc == null)
			{
			imageFc = new CustomFileChooser("/org/lateralgm","LAST_IMAGE_DIR");
			imageFc.setAccessory(new FileChooserImagePreview(imageFc));
			String exts[] = ImageIO.getReaderFileSuffixes();
			for (int i = 0; i < exts.length; i++)
				exts[i] = "." + exts[i]; //$NON-NLS-1$
			String allSpiImages = Messages.getString("Util.ALL_SPI_IMAGES"); //$NON-NLS-1$
			CustomFileFilter filt = new CustomFileFilter(exts,allSpiImages);
			imageFc.addChoosableFileFilter(filt);
			for (String element : exts)
				{
				imageFc.addChoosableFileFilter(new CustomFileFilter(element,element
						+ Messages.getString("Util.FILES"))); //$NON-NLS-1$
				}
			imageFc.setFileFilter(filt);
			}
		if (imageFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			try
				{
				ImageInputStream in = ImageIO.createImageInputStream(imageFc.getSelectedFile());
				Iterator<ImageReader> it = ImageIO.getImageReaders(in);
				ImageReader reader = it.next();
				reader.setInput(in);
				int count = reader.getNumImages(true);
				BufferedImage[] img = new BufferedImage[count];
				ColorConvertOp conv = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB),null);
				for (int i = 0; i < count; i++)
					{
					img[i] = reader.read(i);
					if (img[i] != null)
						{
						BufferedImage dest = new BufferedImage(img[i].getWidth(),img[i].getHeight(),
								BufferedImage.TYPE_3BYTE_BGR);
						conv.filter(img[i],dest);
						img[i] = dest;
						}
					else
						throw new Exception();
					}
				return img;
				}
			catch (Throwable t)
				{
				String msg = String.format(Messages.getString("Util.ERROR_LOADING"), //$NON-NLS-1$
						imageFc.getSelectedFile());
				String title = Messages.getString("Util.ERROR_TITLE"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(LGM.frame,msg,title,JOptionPane.ERROR_MESSAGE);
				t.printStackTrace();
				}
			}
		return null;
		}

	public static Color convertGmColor(int col)
		{
		return new Color(col & 0xFF,(col & 0xFF00) >> 8,(col & 0xFF0000) >> 16);
		}

	public static int getGmColor(Color col)
		{
		return col.getRed() | col.getGreen() << 8 | col.getBlue() << 16;
		}

	public static Component addDim(Container container, Component comp, int width, int height)
		{
		comp.setPreferredSize(new Dimension(width,height));
		return container.add(comp);
		}

	public static JPanel makeRadioPanel(String paneTitle, int width, int height)
		{
		JPanel panel = makeTitledPanel(paneTitle,width,height);
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		return panel;
		}

	public static JPanel makeTitledPanel(String paneTitle, int width, int height)
		{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(paneTitle));
		Dimension newSize = new Dimension(width,height);
		panel.setPreferredSize(newSize);
		panel.setMaximumSize(newSize);
		panel.setMinimumSize(newSize);
		return panel;
		}

	public static <R>R deRef(WeakReference<R> ref)
		{
		return ref == null ? null : ref.get();
		}

	public static boolean refsAreEqual(WeakReference<?> r1, WeakReference<?> r2)
		{
		if (r1 == null && r2 == null) return true;
		if (r1 == null || r2 == null) return false;
		return r1.get() == r2.get();
		}
	}
