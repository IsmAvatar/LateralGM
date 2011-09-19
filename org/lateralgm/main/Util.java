/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
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
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import javax.swing.SwingUtilities;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.visual.FileChooserImagePreview;
import org.lateralgm.file.iconio.BitmapDescriptor;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.file.iconio.ICOImageReaderSPI;
import org.lateralgm.file.iconio.WBMPImageReaderSpiFix;
import org.lateralgm.jedit.SyntaxStyle;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;

import com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi;

public final class Util
	{
	private static final InvokeOnceRunnable IOR = new InvokeOnceRunnable();

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

	public static ByteArrayOutputStream readFully(InputStream in) throws IOException
		{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[4096];

		// Read in the bytes
		int numRead = 0;
		while ((numRead = in.read(buffer)) >= 0)
			baos.write(buffer,0,numRead);

		// Close the input stream and return bytes
		return baos;
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
		final int t = i.getRGB(0,i.getHeight() - 1) & 0x00FFFFFF;
		ImageFilter filter = new RGBImageFilter()
			{
				public int filterRGB(int x, int y, int rgb)
					{
					if ((rgb & 0x00FFFFFF) == t) return t;
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
			String[] exts = { "jpg","bmp","tif","jpeg","wbmp","png","ico","TIF","TIFF","gif","tiff" };
			if (LGM.javaVersion >= 10600) exts = ImageIO.getReaderFileSuffixes();
			for (int i = 0; i < exts.length; i++)
				exts[i] = "." + exts[i]; //$NON-NLS-1$
			String allSpiImages = Messages.getString("Util.ALL_SPI_IMAGES"); //$NON-NLS-1$
			CustomFileFilter filt = new CustomFileFilter(allSpiImages,exts);
			imageFc.addChoosableFileFilter(filt);
			for (String element : exts)
				{
				imageFc.addChoosableFileFilter(new CustomFileFilter(Messages.format("Util.FILES", //$NON-NLS-1$
						element),element));
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
				for (int i = 0; i < count; i++)
					img[i] = reader.read(i);
				//TODO: Gif overlay support (as GM already does)
				return img;
				}
			catch (Throwable t)
				{
				String msg = Messages.format("Util.ERROR_LOADING",imageFc.getSelectedFile()); //$NON-NLS-1$
				String title = Messages.getString("Util.ERROR_TITLE"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(LGM.frame,msg,title,JOptionPane.ERROR_MESSAGE);
				t.printStackTrace();
				}
			}
		return null;
		}

	public static BufferedImage cloneImage(BufferedImage bi)
		{
		if (bi == null) return null;
		//clone the raster
		WritableRaster or = bi.getRaster();
		WritableRaster nr = or.createCompatibleWritableRaster();
		nr.setRect(or);
		//construct with cloned raster, assume it has no special properties
		return new BufferedImage(bi.getColorModel(),or,bi.isAlphaPremultiplied(),null);
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

	public static <R extends Resource<R,?>>R deRef(ResourceReference<R> ref)
		{
		return ref == null ? null : ref.get();
		}

	public static int gcd(int a, int b)
		{
		while (b != 0)
			{
			int c = a % b;
			a = b;
			b = c;
			}
		return a;
		}

	/**
	 * Integer division with rounding towards negative infinity.
	 */
	public static int negDiv(int a, int b)
		{
		return a >= 0 ? a / b : ~(~a / b);
		}

	public static void invokeOnceLater(Runnable r)
		{
		IOR.add(r);
		}

	private static class InvokeOnceRunnable implements Runnable
		{
		private final ArrayList<Runnable> queue = new ArrayList<Runnable>();
		private boolean inDispatcher = false;

		public synchronized void add(Runnable r)
			{
			if (queue.contains(r)) return;
			queue.add(r);
			if (!inDispatcher)
				{
				SwingUtilities.invokeLater(this);
				inDispatcher = true;
				}
			}

		public void run()
			{
			Runnable[] q;
			synchronized (this)
				{
				inDispatcher = false;
				q = new Runnable[queue.size()];
				q = queue.toArray(q);
				queue.clear();
				}
			for (Runnable r : q)
				r.run();
			}
		}

	/**
	 * Makes an icon suitable for embedding into a GM runner
	 * of the given version. Before the compilation process was improved,
	 * the icon was written by overwriting a placeholder of fixed size.
	 * Any icon greater than this size (32x32@32bpp) will usually overflow
	 * onto the resource table of the exe, causing a crash. If required,
	 * this function will do its best to choose the image with the best resolution
	 * and colour depth possible, discarding all the other images.
	 * 
	 * @param ico the icon to (possibly) modify
	 * @param ver the version to make the icon suitable for
	 */
	public static void fixIcon(ICOFile ico, int ver)
		{
		//Preference weighting:
		//32x32 = 3, 16x16 = 1, anything else = -9
		//32bpp = 3, 24bpp = 2, 8bpp = 1, >0 bpp = 0, anything else = -9
		if (ver < 800)
			{
			byte[] weights = new byte[ico.getImageCount()];
			int i = 0;
			for (BitmapDescriptor bmd : ico.getDescriptors())
				{
				int width = bmd.getWidth();
				if (width == 32)
					weights[i] += 3;
				else if (width == 16)
					weights[i]++;
				else
					weights[i] -= 9;

				int bpp = bmd.getBPP();
				if (bpp == 32) weights[i] += 3;
				if (bpp == 24) weights[i] += 2;
				if (bpp == 8)
					weights[i]++;
				else if (bpp <= 0) weights[i] -= 9;

				i++;
				}

			int maxind = 0;
			int maxweight = 0;
			for (i = 0; i < weights.length; i++)
				if (weights[i] > maxweight)
					{
					maxweight = weights[i];
					maxind = i;
					}
			BitmapDescriptor bmd = ico.getDescriptor(maxind);
			ico.getDescriptors().clear();
			ico.getDescriptors().add(bmd);
			}
		}
	}
