package org.lateralgm.file.iconio;

import java.io.IOException;


/**
 * <p>
 * Transparency mask, which is a 1 Bit per pixel information whether a pixel is
 * transparent (1) or opaque (0).
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapMask
	{
	private final BitmapIndexed1BPP _mask;

	/**
	 * @param pDescriptor
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:32
	public BitmapMask(final BitmapDescriptor pDescriptor)
		{
		_mask = new BitmapIndexed1BPP(pDescriptor);
		}

	/**
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	void read(final AbstractDecoder pDec) throws IOException
		{
		_mask.readBitmap(pDec);
		}

	/**
	 * @param pXPos
	 * @param pYPos
	 * @return
	 */
	public int getPaletteIndex(final int pXPos, final int pYPos)
		{
		return _mask.getPaletteIndex(pXPos,pYPos);
		}

	/**
	 * @param pDescriptor
	 */
	void setDescriptor(final BitmapDescriptor pDescriptor)
		{
		_mask.setDescriptor(pDescriptor);
		}

	/**
	 * @param pXPos
	 * @param pYPos
	 * @return
	 */
	public boolean isOpaque(final int pXPos, final int pYPos)
		{
		return _mask.getPaletteIndex(pXPos,pYPos) == 0;
		}
	}
