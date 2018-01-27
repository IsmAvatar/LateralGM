package org.lateralgm.file.iconio;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

public class BitmapPNG extends AbstractBitmap
	{
	public BitmapPNG(BitmapDescriptor descriptor)
		{
		super(descriptor);
		}

	private BufferedImage image;

	public BufferedImage createImageRGB()
		{
		return image;
		}

	void read(StreamDecoder dec) throws IOException
		{
		image = ImageIO.read(dec);
		}

	@Override
	void write(StreamEncoder out) throws IOException
		{
		ImageIO.write(image,"png",out);
		}
	}
