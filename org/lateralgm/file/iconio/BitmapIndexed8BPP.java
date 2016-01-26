package org.lateralgm.file.iconio;

import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

/**
 * <p>
 * Bitmap with 256 color palette (8 bits per sample).
 * </p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapIndexed8BPP extends AbstractBitmapIndexed
	{
	/**
	 * Create a 8 BPP bitmap.
	 *
	 * @param pDescriptor
	 */
	public BitmapIndexed8BPP(final BitmapDescriptor pDescriptor)
		{
		super(pDescriptor);
		}

	void readBitmap(final StreamDecoder pDec) throws IOException
		{
		// One byte contains one sample.
		final int lWt = getBytesPerScanLine(getWidth(),8);
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			final byte[] lRow = new byte[lWt];
			pDec.read(lRow);
			int lRowByte = 0;
			int lOutputPos = (getHeight() - lRowNo - 1) * getWidth();
			for (int lColNo = 0; lColNo < getWidth(); lColNo++)
				{
				pixels[lOutputPos++] = lRow[lRowByte++] & 0xFF;
				}
			}
		}

	void writeBitmap(StreamEncoder out) throws IOException
		{
		int width = getWidth();
		int padding = getPaddingPerScanLine(width,8);
		for (int row = getHeight() - 1; row >= 0; row--)
			{
			int offset = row * width;
			for (int x = 0; x < width; x++)
				out.write(pixels[offset + x]);
			int i = padding;
			while (i-- > 0)
				out.write(0);
			}
		}
	}
