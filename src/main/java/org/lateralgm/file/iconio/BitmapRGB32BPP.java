package org.lateralgm.file.iconio;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

/**
 * <p>
 * ARGB bitmap with 8 bits per color (32 bits per sample).
 * </p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapRGB32BPP extends AbstractBitmapRGB
	{
	///How far can we read before the next image? If <=0, read as far as necessary.
	protected long readStreamLimit;

	/**
	 * @param pDescriptor The image descriptor.
	 */
	public BitmapRGB32BPP(final BitmapDescriptor pDescriptor)
		{
		super(pDescriptor);
		readStreamLimit = pDescriptor.getOffset() + pDescriptor.getSize();
		}

	/**
	 * According to Microsoft, the topmost byte simply is not used, but I found the fourth byte seems
	 * to be the alpha channel.
	 *
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	void readBitmap(final StreamDecoder pDec) throws IOException
		{
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			final byte[] lRow = new byte[getWidth() * 4];
			pDec.read(lRow);
			int lRowByte = 0;
			int lOutputPos = (getHeight() - lRowNo - 1) * getWidth();
			for (int lColNo = 0; lColNo < getWidth(); lColNo++)
				{
				// BGRA -> ARGB, 8 bits per component.
				samples[lOutputPos++] = (lRow[lRowByte++] & 0xFF) | ((lRow[lRowByte++] & 0xFF) << 8)
						+ ((lRow[lRowByte++] & 0xFF) << 16) | ((lRow[lRowByte++] & 0xFF) << 24);
				}
			}

		}

	/**
	 * 32BPP Bitmaps can optionally have NO mask.
	 */
	protected void readMask(final StreamDecoder pDec) throws IOException
	{
	if (readStreamLimit<=0 || pDec.getPos()<readStreamLimit)
		{
		super.readMask(pDec);
		} else {
			transparencyMask = new BitmapMask(descriptor);
			transparencyMask.fakeRead();
		}
	}

	/**
	 * @return Create an ARGB image.
	 */
	public BufferedImage createImageRGB()
		{
		final BufferedImage lImage = new BufferedImage(getWidth(),getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		lImage.setRGB(0,0,getWidth(),getHeight(),samples,0,getWidth());

		return lImage;
		}

	void writeBitmap(StreamEncoder out) throws IOException
		{
		int width = getWidth();
		int padding = getPaddingPerScanLine(width,32);
		for (int row = getHeight() - 1; row >= 0; row--)
			{
			int offset = row * width;
			for (int x = 0; x < width; x++)
				{
				int sample = samples[offset + x];
				out.write(sample & 0xFF);
				out.write((sample >> 8) & 0xFF);
				out.write((sample >> 16) & 0xFF);
				out.write((sample >> 24) & 0xFF);
				}
			int i = padding;
			while (i-- > 0)
				out.write(0);
			}
		}
	}
