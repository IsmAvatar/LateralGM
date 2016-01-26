package org.lateralgm.file.iconio;

import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

/**
 * <p>
 * Parent class for RGB (16, 24, and 32 bits per pixel) bitmaps.
 * </p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public abstract class AbstractBitmapRGB extends AbstractBitmap
	{
	protected int[] samples;

	/**
	 * Create a RGB bitmap.
	 *
	 * @param pDescriptor
	 */
	public AbstractBitmapRGB(final BitmapDescriptor pDescriptor)
		{
		super(pDescriptor);

		samples = new int[getWidth() * getHeight()];
		}

	void read(final StreamDecoder in) throws IOException
		{
		readBitmap(in);
		readMask(in);
		}

	void write(StreamEncoder out) throws IOException
		{
		writeBitmap(out);
		writeMask(out);
		}

	/**
	 * This functions is needed b/c all classes read the bitmap, but not always a color table and a
	 * mask.
	 *
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	abstract void readBitmap(final StreamDecoder pDec) throws IOException;

	abstract void writeBitmap(final StreamEncoder pDec) throws IOException;
	}
