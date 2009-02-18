package org.lateralgm.file.iconio;

import java.io.IOException;

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

	void readBitmap(final AbstractDecoder pDec) throws IOException
		{
		// One byte contains 2 samples.
		final int lWt = getBytesPerScanLine(getWidth(),4);
		for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++)
			{
			final byte[] lRow = pDec.readBytes(lWt,null);
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
	}
