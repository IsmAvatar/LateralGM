package org.lateralgm.file.iconio;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

/**
 * <p>
 * RGB bitmap with 8 bits per color (24 bits per sample).
 * </p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapRGB24BPP extends AbstractBitmapRGB
	{
	/**
	 * @param pDescriptor
	 */
	public BitmapRGB24BPP(final BitmapDescriptor pDescriptor)
		{
		super(pDescriptor);
		}

	void readBitmap(final StreamDecoder pDec) throws IOException
		{
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			final byte[] lRow = new byte[getWidth() * 3];
			pDec.read(lRow);
			int lRowByte = 0;
			int lOutputPos = (getHeight() - lRowNo - 1) * getWidth();
			for (int lColNo = 0; lColNo < getWidth(); lColNo++)
				{
				// BGR -> RGB, 8 bits per component.
				samples[lOutputPos++] = (lRow[lRowByte++] & 0xff) + ((lRow[lRowByte++] & 0xff) << 8)
						+ ((lRow[lRowByte++] & 0xff) << 16);
				}
			}
		}

	/**
	 * @return Create an RGB image.
	 */
	public BufferedImage createImageRGB()
		{
		final BufferedImage lImage = new BufferedImage(getWidth(),getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		lImage.setRGB(0,0,getWidth(),getHeight(),samples,0,getWidth());
		int width = getWidth();
		int height = getHeight();

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				{
				int p = lImage.getRGB(x,y);
				int mask = transparencyMask.mask.pixels[y * width + x];
				p = (mask == 0 ? 0xFF000000 : 0) | (p & 0xFFFFFF);
				lImage.setRGB(x,y,p);
				}
		return lImage;
		}

	void writeBitmap(StreamEncoder out) throws IOException
		{
		int width = getWidth();
		int padding = getPaddingPerScanLine(width,24);
		for (int row = getHeight() - 1; row >= 0; row--)
			{
			int offset = row * width;
			for (int x = 0; x < width; x++)
				{
				int sample = samples[offset + x];
				out.write(sample & 0xFF);
				out.write((sample >> 8) & 0xFF);
				out.write((sample >> 16) & 0xFF);
				}
			int i = padding;
			while (i-- > 0)
				out.write(0);
			}
		}
	}
