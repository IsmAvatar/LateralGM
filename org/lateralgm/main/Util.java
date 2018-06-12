/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2013, 2014 Robert B. Colton
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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.bmp.BMPImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
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
import org.lateralgm.file.ApngIO;
import org.lateralgm.file.iconio.BitmapDescriptor;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Util
	{
	private static final InvokeOnceRunnable IOR = new InvokeOnceRunnable();

	private Util()
		{
		}

	public static CustomFileChooser imageReadFc = null;
	public static CustomFileChooser imageWriteFc = null;

	public static DataFlavor createJVMLocalDataFlavor(Class<?> dataClass) throws ClassNotFoundException
		{
		return new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + dataClass.getName()); //$NON-NLS-1$
		}

	public static DataFlavor createJVMLocalDataFlavor(String className) throws ClassNotFoundException
		{
		return new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + className); //$NON-NLS-1$
		}

	public static BufferedImage paintBackground(int width, int height, Color background,
			Color foreground)
		{
		if (width < 1) width = 1;
		if (height < 1) height = 1;
		BufferedImage dest = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

		for (int row = 0; row < height; row++)
			{
			for (int col = 0; col < width; col++)
				{
				if ((row + col) % 2 == 0)
					{
					dest.setRGB(col,row,foreground.getRGB());
					}
				else
					{
					dest.setRGB(col,row,background.getRGB());
					}
				}
			}
		return dest;
		}

	public static BufferedImage paintBackground(int width, int height)
		{
		return paintBackground(width,height,new Color(Prefs.imagePreviewBackgroundColor),new Color(
				Prefs.imagePreviewForegroundColor));
		}

	public static BufferedImage paintBackgroundScaled(int width, int height, int TILE,
			Color background, Color foreground)
		{
		BufferedImage dest = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dest.createGraphics();

		g.setClip(0,0,width,height);
		g.setColor(background);
		g.fillRect(0,0,width,height);

		g.setColor(foreground);
		int w = width / TILE + 1;
		int h = height / TILE + 1;
		for (int row = 0; row < h; row++)
			{
			for (int col = 0; col < w; col++)
				{
				if ((row + col) % 2 == 0)
					{
					g.fillRect(col * TILE,row * TILE,TILE,TILE);
					}
				}
			}
		return dest;
		}

	public static BufferedImage paintBackgroundScaled(int width, int height, int TILE)
		{
		return paintBackgroundScaled(width,height,TILE,new Color(Prefs.imagePreviewBackgroundColor),
				new Color(Prefs.imagePreviewForegroundColor));
		}

	public static String urlEncode(String s)
		{
		try
			{
			return URLEncoder.encode(s,"UTF-8"); //$NON-NLS-1$
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
			return URLDecoder.decode(s,"UTF-8"); //$NON-NLS-1$
			}
		catch (UnsupportedEncodingException e)
			{
			throw new Error(e);
			}
		}

	public static byte[] readFully(File file) throws FileNotFoundException, IOException
		{
		byte[] fileData = new byte[(int) file.length()];
		try (FileInputStream fis = new FileInputStream(file);
				DataInputStream dis = new DataInputStream(fis))
			{
			dis.readFully(fileData);
			}
		return fileData;
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

	public static byte[] readFully(String path) throws FileNotFoundException, IOException
		{
			return Util.readFully(new File(path));
		}

	public static void writeFully(File file, byte[] data) throws FileNotFoundException, IOException 
		{
		try (FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos))
			{
			bos.write(data);
			}
		}

	public static void writeFully(String path, byte[] data) throws FileNotFoundException, IOException
		{
			Util.writeFully(new File(path), data);
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

	// this is the size on disc in kibibytes
	public static String formatDataSize(long bytes)
		{
		if (bytes <= 0) return "0 B";
		final String[] units = new String[] { "B","KB","MB","GB","TB" };
		int digits = (int) (Math.log(bytes) / Math.log(1024));
		return new DecimalFormat("#,##0.##").format(bytes / Math.pow(1024,digits)) + " "
				+ units[digits];
		}

	// this is the size that Studio will report in kilobytes
	public static String formatDataSizeAlt(long bytes)
		{
		if (bytes <= 0) return "0 B";
		final String[] units = new String[] { "B","KB","MB","GB","TB" };
		int digits = (int) (Math.log(bytes) / Math.log(1000));
		return new DecimalFormat("#,##0.##").format(bytes / Math.pow(1000,digits)) + " "
				+ units[digits];
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

	public static BufferedImage getTransparentImage(BufferedImage i)
		{
		if (i == null) return null;
		final int t = i.getRGB(0,i.getHeight() - 1) & 0x00FFFFFF;
		ImageFilter filter = new RGBImageFilter()
			{
				@Override
				public int filterRGB(int x, int y, int rgb)
					{
					if ((rgb & 0x00FFFFFF) == t) return t;
					return rgb;
					}
			};
		ImageProducer ip = new FilteredImageSource(i.getSource(),filter);
		return toBufferedImage(Toolkit.getDefaultToolkit().createImage(ip));
		}

	private static void createImageReadChooser()
		{
		imageReadFc = new CustomFileChooser("/org/lateralgm","LAST_IMAGE_DIR");
		imageReadFc.setAccessory(new FileChooserImagePreview(imageReadFc));

		String[] readexts = { "apng","gif" };
		if (LGM.javaVersion >= 10600)
			{
			String[] internalexts = ImageIO.getReaderFileSuffixes();
			ArrayList<String> extensions = new ArrayList<String>();
			for (String ext : readexts)
				{
				extensions.add(ext);
				}
			for (String ext : internalexts)
				{
				if (!extensions.contains(ext))
					{
					extensions.add(ext);
					}
				}
			readexts = extensions.toArray(new String[extensions.size()]);
			}
		for (int i = 0; i < readexts.length; i++)
			readexts[i] = "." + readexts[i]; //$NON-NLS-1$
		String allSpiImages = Messages.getString("Util.ALL_SPI_IMAGES"); //$NON-NLS-1$

		CustomFileFilter allSpiFilter = new CustomFileFilter(allSpiImages,readexts);
		imageReadFc.addChoosableFileFilter(allSpiFilter);
		for (String element : readexts)
			{
			imageReadFc.addChoosableFileFilter(new CustomFileFilter(Messages.format("Util.FILES", //$NON-NLS-1$
					element),element));
			}

		imageReadFc.setFileFilter(allSpiFilter);
		}

	private static void createImageWriteChooser()
		{
		imageWriteFc = new CustomFileChooser("/org/lateralgm","LAST_IMAGE_DIR");

		String[] writeexts = { "apng" };
		if (LGM.javaVersion >= 10600)
			{
			String[] internalexts = ImageIO.getWriterFileSuffixes();
			ArrayList<String> extensions = new ArrayList<String>();
			for (String ext : writeexts)
				{
				extensions.add(ext);
				}
			for (String ext : internalexts)
				{
				if (!extensions.contains(ext))
					{
					extensions.add(ext);
					}
				}
			writeexts = extensions.toArray(new String[extensions.size()]);
			}
		for (int i = 0; i < writeexts.length; i++)
			writeexts[i] = "." + writeexts[i]; //$NON-NLS-1$
		String allSpiImages = Messages.getString("Util.ALL_SPI_IMAGES"); //$NON-NLS-1$

		CustomFileFilter allSpiFilter = new CustomFileFilter(allSpiImages,writeexts);
		imageWriteFc.addChoosableFileFilter(allSpiFilter);
		for (String element : writeexts)
			{
			imageWriteFc.addChoosableFileFilter(new CustomFileFilter(Messages.format("Util.FILES", //$NON-NLS-1$
					element),element));
			}

		imageWriteFc.setFileFilter(allSpiFilter);
		}

	/**
	 * Shows a JFileChooser with file filters for all currently registered instances of
	 * ImageReaderSpi with multiple file selection.
	 *
	 * @return The selected image, or null if one is not chosen
	 */
	public static File chooseImageFile()
		{
		if (imageReadFc == null)
			{
			createImageReadChooser();
			}
		imageReadFc.setMultiSelectionEnabled(false);
		if (imageReadFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			return imageReadFc.getSelectedFile();
		return null;
		}

	/**
	 * Shows a JFileChooser with file filters for all currently registered instances of
	 * ImageReaderSpi with multiple file selection.
	 *
	 * @return The selected image, or null if one is not chosen
	 */
	public static File[] chooseImageFiles()
		{
		if (imageReadFc == null)
			{
			createImageReadChooser();
			}
		imageReadFc.setMultiSelectionEnabled(true);
		if (imageReadFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			return imageReadFc.getSelectedFiles();
		return null;
		}

	/**
	 * Returns the selected file from a JFileChooser, including the extension from
	 * the file filter.
	 */
	public static File getSelectedFileWithExtension(JFileChooser c)
		{
		File file = c.getSelectedFile();
		if (c.getFileFilter() instanceof CustomFileFilter)
			{
			String[] exts = ((CustomFileFilter) c.getFileFilter()).getExtensions();
			String nameLower = file.getName().toLowerCase();
			for (String ext : exts)
				{ // check if it already has a valid extension
				if (ext.startsWith("."))
					{
					ext = ext.substring(1);
					}
				if (nameLower.endsWith('.' + ext.toLowerCase()))
					{
					return file; // if yes, return as-is
					}
				}
			// if not, append the first extension from the selected filter
			file = new File(file.toString() + (exts[0].startsWith(".") ? "" : ".") + exts[0]);
			}
		return file;
		}

	private static String getFileExtension(File file)
		{
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1)
			{
			return ""; // empty extension
			}
		return name.substring(lastIndexOf + 1);
		}

	//TODO: JPEG Writing is bugged in some newer JVM versions causing the RGB color channels to be mixed up.
	// This is a bug Oracle claims to have fixed but they actually haven't.
	// http://bugs.java.com/view_bug.do?bug_id=4712797
	// http://bugs.java.com/view_bug.do?bug_id=4776576
	// http://stackoverflow.com/questions/13072312/jpeg-image-color-gets-drastically-changed-after-just-imageio-read-and-imageio

	public static void writeImageQualityPrompt(BufferedImage img, String ext, File f)
			throws FileNotFoundException,IOException
		{
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(ext);
		ImageWriter writer = (ImageWriter) iter.next();
		BMPImageWriteParam iwp = (BMPImageWriteParam) writer.getDefaultWriteParam();
		if (iwp.canWriteCompressed())
			{
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

			for (String str : iwp.getCompressionTypes())
				{
				System.out.println(str);
				}
			if (ext.equals("jpg") || ext.equals("jpeg"))
				{

				iwp.setCompressionQuality(1.0f);
				}
			else if (ext.equals("bmp"))
				{
				iwp = (BMPImageWriteParam) iwp;
				//iwp.setCompressionType("BI_BITFIELDS");
				iwp.setCompressionType("BI_RLE4");
				//iwp.setCompressionType("BI_RLE8");
				//iwp.setCompressionType("BI_RGB");
				}

			}

		FileImageOutputStream output = new FileImageOutputStream(f);
		writer.setOutput(output);
		IIOImage image = new IIOImage(img,null,null);
		writer.write(null,image,iwp);
		writer.dispose();
		output.close();
		}

	public static BufferedImage convertImage(BufferedImage img, int format, Color col)
		{
		BufferedImage bi = new BufferedImage(img.getWidth(),img.getHeight(),format);
		Graphics gd = bi.getGraphics();
		if (col != null)
			{
			gd.setColor(col);
			gd.fillRect(0,0,img.getWidth(),img.getHeight());
			}
		gd.drawImage(img,0,0,img.getWidth(),img.getHeight(),null);
		gd.dispose();
		return bi;
		}

	public static BufferedImage convertImage(BufferedImage img, int format)
		{
		return convertImage(img,format,null);
		}

	public static BufferedImage clearBackground(BufferedImage img, Color col)
		{
		return convertImage(img,BufferedImage.TYPE_INT_ARGB,col);
		}

	public static BufferedImage makeOpaque(BufferedImage img, Color col)
		{
		return convertImage(img,BufferedImage.TYPE_BYTE_INDEXED,col);
		}

	public static void saveImages(ArrayList<BufferedImage> imgs)
		{
		if (imgs == null || imgs.size() <= 0)
			{
			JOptionPane.showMessageDialog(LGM.frame,Messages.getString("Util.NO_IMAGE_MESSAGE"),
					Messages.getString("Util.NO_IMAGE_TITLE"),JOptionPane.WARNING_MESSAGE);
			return;
			}
		if (imageWriteFc == null)
			{
			createImageWriteChooser();
			}
		imageWriteFc.setMultiSelectionEnabled(false);
		if (imageWriteFc.showSaveDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			try
				{
				File f = getSelectedFileWithExtension(imageWriteFc);
				String ext = getFileExtension(f);
				if (ext.equals("apng"))
					{
					FileOutputStream os = new FileOutputStream(f);
					ApngIO.imagesToApng(imgs,os);
					os.close();
					}
				else
					{
					// BMP and other formats can not write alpha transparent images, so if writing fails
					// try to remove the alpha layer and then write
					if (!ImageIO.write(imgs.get(0),ext,f))
						{
						BufferedImage bi = makeOpaque(imgs.get(0),Color.white);
						ImageIO.write(bi,ext,f);
						}
					}
				}
			catch (IOException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			}
		}

	public static void saveImage(BufferedImage img)
		{
		if (img == null)
			{
			JOptionPane.showMessageDialog(LGM.frame,Messages.getString("Util.NO_IMAGE_MESSAGE"),
					Messages.getString("Util.NO_IMAGE_TITLE"),JOptionPane.WARNING_MESSAGE);
			return;
			}
		if (imageWriteFc == null)
			{
			createImageWriteChooser();
			}
		imageWriteFc.setMultiSelectionEnabled(false);
		if (imageWriteFc.showSaveDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			try
				{
				File f = getSelectedFileWithExtension(imageWriteFc);
				String ext = getFileExtension(f);
				if (ext.equals("apng"))
					{
					ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>(1);
					imgs.add(img);
					FileOutputStream os = new FileOutputStream(f);
					ApngIO.imagesToApng(imgs,os);
					os.close();
					//} else if (ext.equalsIgnoreCase("ico")) {
					//new ICOFile(img.getRGB(startX,startY,w,h,rgbArray,offset,scansize)).write(new FileOutputStream(f));
					}
				else
					{
					// BMP and other formats can not write alpha transparent images, so if writing fails
					// try to remove the alpha layer and then write
					if (!ImageIO.write(img,ext,f))
						{
						BufferedImage bi = makeOpaque(img,Color.white);
						ImageIO.write(bi,ext,f);
						}
					}
				}
			catch (IOException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			}
		}

	public static String getPOSIXPath(String path)
		{
		return path.replace("\\","/"); //$NON-NLS-1$ //$NON-NLS-2$
		}

	private static ArrayList<BufferedImage> readGIF(File gif) throws IOException
		{
		ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>(0);
		ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
		;
		reader.setInput(ImageIO.createImageInputStream(gif));

		int width = -1;
		int height = -1;

		IIOMetadata metadata = reader.getStreamMetadata();
		if (metadata != null)
			{
			IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

			NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

			if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0)
				{
				IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

				if (screenDescriptor != null)
					{
					width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
					height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
					}
				}
			}

		BufferedImage master = null;
		Graphics2D masterGraphics = null;

		for (int frameIndex = 0;; frameIndex++)
			{
			BufferedImage image;
			try
				{
				image = reader.read(frameIndex);
				}
			catch (IndexOutOfBoundsException io)
				{
				break;
				}

			if (width == -1 || height == -1)
				{
				width = image.getWidth();
				height = image.getHeight();
				}

			IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree(
					"javax_imageio_gif_image_1.0");
			IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(
					0);
			//int delay = Integer.valueOf(gce.getAttribute("delayTime"));
			String disposal = gce.getAttribute("disposalMethod");

			int x = 0;
			int y = 0;

			if (master == null)
				{
				master = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
				masterGraphics = master.createGraphics();
				masterGraphics.setBackground(new Color(0,0,0,0));
				}
			else
				{
				NodeList children = root.getChildNodes();
				for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++)
					{
					Node nodeItem = children.item(nodeIndex);
					if (nodeItem.getNodeName().equals("ImageDescriptor"))
						{
						NamedNodeMap map = nodeItem.getAttributes();
						x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
						y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
						}
					}
				}
			masterGraphics.drawImage(image,x,y,null);

			BufferedImage copy = new BufferedImage(master.getColorModel(),master.copyData(null),
					master.isAlphaPremultiplied(),null);
			frames.add(copy);

			if (disposal.equals("restoreToPrevious"))
				{
				BufferedImage from = null;
				for (int i = frameIndex - 1; i >= 0; i--)
					{
					if (frameIndex == 0)
						{
						from = frames.get(i);
						break;
						}
					}

				master = new BufferedImage(from.getColorModel(),from.copyData(null),
						from.isAlphaPremultiplied(),null);
				masterGraphics = master.createGraphics();
				masterGraphics.setBackground(new Color(0,0,0,0));
				}
			else if (disposal.equals("restoreToBackgroundColor"))
				{
				masterGraphics.clearRect(x,y,image.getWidth(),image.getHeight());
				}
			}
		masterGraphics.dispose();
		reader.dispose();

		return frames;
		}

	public static BufferedImage getValidImage()
		{
		File f = chooseImageFile();
		if (f == null || !f.exists()) return null;
		try
			{
			if (f.getName().endsWith(".gif"))
				{
				return readGIF(f).get(0);
				}
			else if (f.getName().endsWith(".apng"))
				{
				FileInputStream is = new FileInputStream(f);
				List<BufferedImage> imgs = ApngIO.apngToBufferedImages(is);
				is.close();
				return imgs.get(0);
				}

			else
				{
				return ImageIO.read(f);
				}
			}
		catch (IOException e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		return null;
		}

	public static BufferedImage[] getValidImages()
		{
		File[] f = chooseImageFiles();
		if (f == null) return null;
		try
			{
			ArrayList<BufferedImage> subframes = new ArrayList<BufferedImage>(0);

			for (int i = 0; i < f.length; i++)
				{
				if (!f[i].exists()) continue;
				if (f[i].getName().endsWith(".gif")) //$NON-NLS-1$
					{
					subframes.addAll(readGIF(f[i]));
					}
				else if (f[i].getName().endsWith(".apng")) //$NON-NLS-1$
					{
					FileInputStream is = new FileInputStream(f[i]);
					subframes.addAll(ApngIO.apngToBufferedImages(is));
					is.close();
					}
				else if (f[i].getName().endsWith(".ico")) //$NON-NLS-1$
					{
					List<BufferedImage> imgs = new ICOFile(readFully(f[i])).getImages();
					return imgs.toArray(new BufferedImage[imgs.size()]);
					}
				else
					{
					subframes.add(ImageIO.read(f[i]));
					}
				}
			return subframes.toArray(new BufferedImage[0]);
			}
		catch (Exception e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		return null;
		}

	public static BufferedImage[] getValidImages(ImageInputStream in) throws IOException,
			IllegalArgumentException
		{
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

	public static BufferedImage cloneImage(BufferedImage bi)
		{
		if (bi == null) return null;
		//clone the raster
		WritableRaster or = bi.getRaster();
		WritableRaster nr = or.createCompatibleWritableRaster();
		nr.setRect(or);
		//construct with cloned raster, assume it has no special properties
		return new BufferedImage(bi.getColorModel(),nr,bi.isAlphaPremultiplied(),null);
		}

	public static Color convertGmColor(int col)
		{
		return new Color(col & 0xFF,(col & 0xFF00) >> 8,(col & 0xFF0000) >> 16);
		}

	public static Color convertGmColorWithAlpha(int col)
		{
		return new Color(col & 0xFF,(col & 0xFF00) >> 8,(col & 0xFF0000) >> 16,
				(col & 0xFF000000) >>> 24);
		}

	public static Color convertInstanceColorWithAlpha(int col)
		{
		return new Color((col & 0xFF0000) >> 16,(col & 0xFF00) >> 8,col & 0xFF,
				(col & 0xFF000000) >>> 24);
		}

	public static Color HSL2RGB(float h, float s, float l, int a)
		{
		float c = (1 - Math.abs(2.f * l - 1.f)) * s;
		float h_ = h / 60.f;
		float h_mod2 = h_;
		if (h_mod2 >= 4.f)
			h_mod2 -= 4.f;
		else if (h_mod2 >= 2.f) h_mod2 -= 2.f;

		float x = c * (1 - Math.abs(h_mod2 - 1));
		float r_, g_, b_;
		if (h_ < 1)
			{
			r_ = c;
			g_ = x;
			b_ = 0;
			}
		else if (h_ < 2)
			{
			r_ = x;
			g_ = c;
			b_ = 0;
			}
		else if (h_ < 3)
			{
			r_ = 0;
			g_ = c;
			b_ = x;
			}
		else if (h_ < 4)
			{
			r_ = 0;
			g_ = x;
			b_ = c;
			}
		else if (h_ < 5)
			{
			r_ = x;
			g_ = 0;
			b_ = c;
			}
		else
			{
			r_ = c;
			g_ = 0;
			b_ = x;
			}

		float m = l - (0.5f * c);
		int r = (int) ((r_ + m) * (255.f) + 0.5f);
		int g = (int) ((g_ + m) * (255.f) + 0.5f);
		int b = (int) ((b_ + m) * (255.f) + 0.5f);

		return new Color(r,g,b,a);
		}

	public static void RGB2HSL(int red, int green, int blue, float[] hslvals)
		{

		float r = red / 255.f;
		float g = green / 255.f;
		float b = blue / 255.f;
		float max = Math.max(Math.max(r,g),b);
		float min = Math.min(Math.min(r,g),b);
		float c = max - min;

		float h_ = 0.f;
		if (c == 0)
			{
			h_ = 0;
			}
		else if (max == r)
			{
			h_ = (float) (g - b) / c;
			if (h_ < 0) h_ += 6.f;
			}
		else if (max == g)
			{
			h_ = (float) (b - r) / c + 2.f;
			}
		else if (max == b)
			{
			h_ = (float) (r - g) / c + 4.f;
			}
		float h = 60.f * h_;

		float l = (max + min) * 0.5f;

		float s;
		if (c == 0)
			{
			s = 0.f;
			}
		else
			{
			s = c / (1 - Math.abs(2.f * l - 1.f));
			}

		hslvals[0] = h;
		hslvals[1] = s;
		hslvals[2] = l;
		}

	public static int getGmColor(Color col)
		{
		return col.getRed() | col.getGreen() << 8 | col.getBlue() << 16;
		}

	public static int getGmColorWithAlpha(Color col)
		{
		return col.getRed() | col.getGreen() << 8 | col.getBlue() << 16 | col.getAlpha() << 24;
		}

	public static int getGmColorWithAlpha(Color col, int alpha)
		{
		return col.getRed() | col.getGreen() << 8 | col.getBlue() << 16 | alpha << 24;
		}

	/** Turns an AWT Java Color into an rgba(r,g,b,a) CSS formatted string.
	 * NOTE: Java's 1.8 HTML implementation does not support opacity.
	 *
	 * @param col The integer value of the color.
	 * @param hasTransparency Whether the integer color value has transparency.
	 * @return The CSS encoded string representing the color value.
	 */
	public static String getHTMLColor(int col, boolean hasTransparency)
		{
		if (hasTransparency)
			{
			return String.format("rgba(%d,%d,%d,%d)",col >> 16 & 0xFF,col >> 8 & 0xFF,col & 0xFF,
					col >> 24 & 0xFF);
			}
		return String.format("rgb(%d,%d,%d)",col >> 16 & 0xFF,col >> 8 & 0xFF,col & 0xFF);
		}

	/** Turns an AWT Java Color into an rgba(r,g,b,a) CSS formatted string.
	 * NOTE: Java's 1.8 HTML implementation does not support opacity.
	 *
	 * @param col The integer value of the color.
	 * @param hasTransparency Whether the integer color value has transparency.
	 * @return The CSS encoded string representing the color value.
	 */
	public static String getHTMLColor(Color col, boolean hastransparency)
		{
		if (hastransparency)
			{
			return String.format("rgba(%d,%d,%d,%d)",col.getRed(),col.getGreen(),col.getBlue(),
					col.getAlpha());
			}
		return String.format("rgb(%d,%d,%d)",col.getRed(),col.getGreen(),col.getBlue());
		}

	/** Formats the given color to a hexadecimal encoded string.
	 *
	 * @param col The color value that should be formatted to a hexadecimal string.
	 * @param includeAlpha Whether the alpha of the color value should be formated.
	 * @return The hexadecimal encoded string for the color.
	 */
	public static String formatColortoHex(Color col, boolean includeAlpha) {
		if (includeAlpha) {
			return String.format("%02X%02X%02X%02X", col.getRed(), col.getGreen(), col.getBlue(),
				col.getAlpha());
		}
		return String.format("%02X%02X%02X", col.getRed(), col.getGreen(), col.getBlue());
	}

	/** Formats the given color to a hexadecimal encoded string.
	 *
	 * @param col The color value that should be formatted to a hexadecimal string.
	 * @return The hexadecimal encoded string for the color.
	 */
	public static String formatColortoHex(Color col) {
		return String.format("%02X%02X%02X%02X", col.getRed(), col.getGreen(), col.getBlue(),
			col.getAlpha());
	}

	public static long getInstanceColorWithAlpha(Color col, int alpha)
		{
		return (alpha << 24 | col.getRed() << 16 | col.getGreen() << 8 | col.getBlue()) & 0xFFFFFFFFL;
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

	/**
	 * Flags a class as inherently unique (when not cloned for modification),
	 * indicating that it has an isEqual(E) method for comparing fields,
	 * rather than the equals() method, which falls back to Object.equals.
	 * @param <E> The other class to compare fields with. Typically, this
	 * is the implementing class.
	 */
	public static interface InherentlyUnique<E extends InherentlyUnique<E>>
		{
		/**
		 * Objects which are inherently unique (when not cloned for modification)
		 * can't compare fields in their equals() method. As such, we instead
		 * use our own method, isEqual, which compares fields for an equality check.
		 * @return Whether the fields of these actions are equal.
		 */
		boolean isEqual(E other);
		}

	public static <V extends InherentlyUnique<V>>boolean areInherentlyUniquesEqual(List<V> a,
			List<V> b)
		{
		if (a == b) return true;
		if (a == null || b == null) return false;
		ListIterator<V> e1 = a.listIterator();
		ListIterator<V> e2 = b.listIterator();
		while (e1.hasNext() && e2.hasNext())
			{
			V o1 = e1.next();
			V o2 = e2.next();
			if (!(o1 == null ? o2 == null : o1.isEqual(o2))) return false;
			}
		return !(e1.hasNext() || e2.hasNext());
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
