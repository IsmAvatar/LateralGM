package org.lateralgm.file.iconio;

import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

/**
 * <p>
 * Bitmap with 16 color palette (4 bits per sample).
 * </p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapIndexed4BPP extends AbstractBitmapIndexed
	{
	/**
	 * Create a 4 BPP bitmap.
	 *
	 * @param pDescriptor
	 */
	public BitmapIndexed4BPP(final BitmapDescriptor pDescriptor)
		{
		super(pDescriptor);
		}

	void readBitmap(final StreamDecoder pDec) throws IOException
		{
		// One byte contains 2 samples.
		final int lWt = getBytesPerScanLine(getWidth(),4);
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			final byte[] lRow = new byte[lWt];
			pDec.read(lRow);
			int lRowByte = 0;
			boolean lUpperNibbleP = true;
			int lOutputPos = (getHeight() - lRowNo - 1) * getWidth();
			for (int lColNo = 0; lColNo < getWidth(); lColNo++)
				{
				int lValue;
				if (lUpperNibbleP)
					{
					lValue = (lRow[lRowByte] & 0xF0) >> 4;
					}
				else
					{
					lValue = lRow[lRowByte] & 0x0F;
					lRowByte++;
					}
				pixels[lOutputPos++] = lValue & 0xFF;
				lUpperNibbleP = !lUpperNibbleP;
				}
			}
		}

	private void writeNibs(StreamEncoder out, int offset, int count) throws IOException
		{
		if (count == 2)
			out.write(pixels[offset] << 4 | pixels[offset + 1]);
		else if (count == 1)
			out.write(pixels[offset] << 4);
		else
			throw new IllegalArgumentException("Can't write anything other than 1 or 2 nibbles");
		}

	void writeBitmap(StreamEncoder out) throws IOException
		{
		int width = getWidth();
		int padding = getPaddingPerScanLine(width,4);
		for (int row = getHeight() - 1; row >= 0; row--)
			{
			int offset = row * width;
			for (int x = 0; x < width; x += 2)
				writeNibs(out,offset + x,Math.min(width - x,2));
			int i = padding;
			while (i-- > 0)
				out.write(0);
			}
		}
	}
