package org.lateralgm.file.iconio;

import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

/**
 * <p>
 * Bitmap with 2 color palette (black and white icon). Not tested, but seems to work.
 * </p>
 * <p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapIndexed1BPP extends AbstractBitmapIndexed
	{
	/**
	 * Create a 1BPP bitmap.
	 *
	 * @param pDescriptor
	 */
	public BitmapIndexed1BPP(final BitmapDescriptor pDescriptor)
		{
		super(pDescriptor);
		}

	void readBitmap(final StreamDecoder pDec) throws IOException
		{
		// One byte contains 8 samples.
		final int lBytesPerScanLine = getBytesPerScanLine(getWidth(),1);
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			final byte[] lBitmapBytes = new byte[lBytesPerScanLine];
			pDec.read(lBitmapBytes);
			int lBitmapByteNo = 0;
			int lTestBitMask = 0x80;
			int lPixelNo = (getHeight() - 1 - lRowNo) * getWidth();
			for (int lColNo = 0; lColNo < getWidth(); lColNo++)
				{
				pixels[lPixelNo++] = ((lBitmapBytes[lBitmapByteNo] & lTestBitMask) / lTestBitMask) & 0xFF;

				if (lTestBitMask == 0x01)
					{
					// When the last bit (bit 0, mask 0x01) has been processed,
					// advance to the next bitmap byte, and set the test bit to
					// bit 7, mask 0x80.
					lTestBitMask = 0x80;
					lBitmapByteNo++;
					}
				else
					{
					lTestBitMask >>= 1;
					}
				}
			}
		}

	void fakeReadBitmap()
		{
		int lPixelNo = 0;
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			for (int lColNo = 0; lColNo < getWidth(); lColNo++)
				{
				pixels[lPixelNo++] = 1;
				}
			}
		}

	private void writeBits(StreamEncoder out, int offset, int count) throws IOException
		{
		int b = 0;
		for (int i = count - 1; i >= 0; i--)
			{
			b |= pixels[offset + i] << (7 - i);
			}
		out.write(b);
		}

	void writeBitmap(StreamEncoder out) throws IOException
		{
		int width = getWidth();
		int padding = getPaddingPerScanLine(width,1);
		for (int row = getHeight() - 1; row >= 0; row--)
			{
			int offset = row * width;
			for (int x = 0; x < width; x += 8)
				writeBits(out,offset + x,Math.min(width - x,8));
			int i = padding;
			while (i-- > 0)
				out.write(0);
			}
		}
	}
