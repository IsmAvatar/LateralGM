package org.lateralgm.file.iconio;

import java.io.IOException;

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

	void readBitmap(final AbstractDecoder pDec) throws IOException
		{
		// One byte contains one sample.
		final int lWt = getBytesPerScanLine(getWidth(),8);
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			final byte[] lRow = pDec.readBytes(lWt,null);
			int lRowByte = 0;
			int lOutputPos = (getHeight() - lRowNo - 1) * getWidth();
			for (int lColNo = 0; lColNo < getWidth(); lColNo++)
				{
				pixels[lOutputPos++] = lRow[lRowByte++] & 0xFF;
				}
			}
		}
	}
