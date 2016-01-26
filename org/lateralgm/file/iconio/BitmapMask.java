package org.lateralgm.file.iconio;

import java.io.IOException;

import org.lateralgm.file.StreamDecoder;

/**
 * <p>
 * Transparency mask, which is a 1 Bit per pixel information whether a pixel is transparent (1) or
 * opaque (0).
 * </p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapMask
	{
	protected final BitmapIndexed1BPP mask;

	/**
	 * @param pDescriptor
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:32
	public BitmapMask(final BitmapDescriptor pDescriptor)
		{
		mask = new BitmapIndexed1BPP(pDescriptor);
		}

	/**
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	void read(final StreamDecoder pDec) throws IOException
		{
		mask.readBitmap(pDec);
		}

	/**
	 * Fake a read operation, setting all pixels to 1 (full opacity).
	 */
	void fakeRead()
		{
		mask.fakeReadBitmap();
		}

	/**
	 * @param pXPos
	 * @param pYPos
	 * @return
	 */
	public int getPaletteIndex(final int pXPos, final int pYPos)
		{
		return mask.getPaletteIndex(pXPos,pYPos);
		}

	/**
	 * @param pDescriptor
	 */
	void setDescriptor(final BitmapDescriptor pDescriptor)
		{
		mask.setDescriptor(pDescriptor);
		}

	/**
	 * @param pXPos
	 * @param pYPos
	 * @return
	 */
	public boolean isOpaque(final int pXPos, final int pYPos)
		{
		return mask.getPaletteIndex(pXPos,pYPos) == 0;
		}
	}
