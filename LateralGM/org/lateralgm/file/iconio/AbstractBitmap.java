package org.lateralgm.file.iconio;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * <p>
 * Parent class for all icon bitmaps, indexed or RGB.
 * </p>
 * <p>
 * Why is creation and read() not one thing? Because we might want to create the object, fill it
 * step by step, and then write it.
 * </p>
 * 
 * @see com.ctreber.aclib.image.ico.AbstractBitmapIndexed
 * @see com.ctreber.aclib.image.ico.AbstractBitmapRGB
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public abstract class AbstractBitmap
	{
	/** Describes the bitmap. */
	protected BitmapDescriptor descriptor;

	/**
	 * @param pDescriptor The image descriptor.
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:27
	public AbstractBitmap(final BitmapDescriptor pDescriptor)
		{
		descriptor = pDescriptor;
		}

	/**
	 * Create an RGB image rendition of this bitmap.
	 * 
	 * @return A BufferedImage rendition (RGB) of this bitmap.
	 */
	public abstract BufferedImage createImageRGB();

	/**
	 * Read bitmap from the decoder into class internal data structures. Implemented by specific
	 * Bitmap classes.
	 * 
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	abstract void read(AbstractDecoder pDec) throws IOException;

	/**
	 * The (manipulated height - read on). I found that a) mostly (but not always) the height reported
	 * by the header is double the real size, and b) that the descriptor is usually correct.
	 * 
	 * @return Returns the height.
	 */
	protected int getHeight()
		{
		if (descriptor.getWidth() == descriptor.getHeight() / 2)
			{
			return descriptor.getWidth();
			}

		return descriptor.getHeight();
		}

	/**
	 * Get the bitmap width.
	 * 
	 * @return Returns the width.
	 */
	protected int getWidth()
		{
		return descriptor.getWidth();
		}

	protected int getColorCount()
		{
		return descriptor.getHeader().getColorCount();
		}

	/**
	 * Get the bitmap descriptor.
	 * 
	 * @return Returns the descriptor.
	 */
	public BitmapDescriptor getDescriptor()
		{
		return descriptor;
		}

	/**
	 * @param pDescriptor The descriptor to set.
	 */
	void setDescriptor(final BitmapDescriptor pDescriptor)
		{
		descriptor = pDescriptor;
		}

	/**
	 * Simply returns the class name which reflects the bitmap type.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
		{
		return getClass().toString();
		}
	}
