package org.lateralgm.file.iconio;

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
	}
