package org.lateralgm.file.iconio;

import java.awt.Image;
import java.io.IOException;

/**
 * <p>
 * ICO file entry descriptor. Describes an embedded bitmap, and points to the header/bitmap pair. I
 * found that the descriptor often "lies" about size, number of colors etc., hence the bitmap header
 * should be used for reference.
 * </p>
 * 
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapDescriptor
	{
	private final int width;

	private final int height;

	private final int colorCount;

	private final int reserved;

	private final int planes;

	private final int bpp;

	private final long size;

	private final long offset;

	/** For convenience, not part of an entry: The header the entry refers to. */
	private BitmapHeader header;

	/** For convenience, not part of an entry: The bitmap the entry refers to. */
	private AbstractBitmap bitmap;

	/**
	 * For convenience, not part of an entry: The mask the entry refers to. Note that RGB images have
	 * no mask
	 */
	private BitmapMask mask;

	/**
	 * Read the descriptor with the decoder (16 Bytes in total).
	 * 
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:32
	public BitmapDescriptor(final AbstractDecoder pDec) throws IOException
		{
		width = pDec.readUInt1();
		height = pDec.readUInt1();
		colorCount = pDec.readUInt1();

		reserved = pDec.readUInt1();
		planes = pDec.readUInt2();
		bpp = pDec.readUInt2();
		size = pDec.readUInt4();
		offset = pDec.readUInt4();
		}

	/**
	 * @return Provides some information on the descriptor.
	 */
	public String toString()
		{
		return "width: " + width + ", height: " + height + ", colorCount: " + colorCount + " ("
				+ getColorCount() + ")" + ", planes: " + planes + ", BPP: " + bpp + ", size: " + size
				+ ", offset: " + offset;
		}

	/**
	 * Image with indexed colors. Returns null if an indexed image can't be created (like, from an RGB
	 * icon - color mapping and dithering is a bit much for the time being). Transparency information
	 * that might be present in the ICO file is lost. See {@link #getImageRGB}.
	 * 
	 * @return Image.
	 */
	public Image getImageIndexed()
		{
		if (!(bitmap instanceof AbstractBitmapIndexed))
			{
			// Can't create indexed image from RGB icon.
			return null;
			}
		return ((AbstractBitmapIndexed) bitmap).createImageIndexed();
		}

	/**
	 * Bits per pixel. If the bit count of the entry is 0, the bit count of the header is returned.
	 * See {@link #getBPPRaw}.
	 * 
	 * @return Bits per pixel (fudged).
	 */
	public int getBPP()
		{
		if (bpp != 0)
			{
			return bpp;
			}
		return header.getBPP();
		}

	/**
	 * The original bits per pixel count. See {@link #getBPP()}.
	 * 
	 * @return Bits per pixel (raw).
	 */
	public int getBPPRaw()
		{
		return bpp;
		}

	/**
	 * Image with ARGB colors. This method works for indexed color and RGB ICO files. Transparency
	 * information that might be present in the ICO is used. See {@link #getImageIndexed}.
	 * 
	 * @return Image created from the bitmap.
	 */
	public Image getImageRGB()
		{
		return bitmap.createImageRGB();
		}

	/**
	 * The original color count (note "0" means "256"). See {@link #getColorCount}.
	 * 
	 * @return Color count (raw).
	 */
	public int getColorCountRaw()
		{
		return colorCount;
		}

	/**
	 * The actual color count. See {@link #getColorCountRaw}.
	 * 
	 * @return Color count (cooked).
	 */
	public int getColorCount()
		{
		return colorCount == 0 ? 256 : colorCount;
		}

	/**
	 * Bitmap height.
	 * 
	 * @return Height.
	 */
	public int getHeight()
		{
		return height;
		}

	/**
	 * Offset of header in ICO file.
	 * 
	 * @return Offset.
	 */
	public long getOffset()
		{
		return offset;
		}

	/**
	 * Number of planes ("1" for bitmaps, as far as I know).
	 * 
	 * @return Planes.
	 */
	public int getPlanes()
		{
		return planes;
		}

	/**
	 * Reserved value in the descriptor.
	 * 
	 * @return Reserved value.
	 */
	public int getReserved()
		{
		return reserved;
		}

	/**
	 * Hm - the size of the header and bitmap maybe?
	 * 
	 * @return Size.
	 */
	public long getSize()
		{
		return size;
		}

	/**
	 * Bitmap width.
	 * 
	 * @return Width.
	 */
	public int getWidth()
		{
		return width;
		}

	/**
	 * The header of the bitmap this descriptor refers to.
	 * 
	 * @return Header.
	 */
	public BitmapHeader getHeader()
		{
		return header;
		}

	/**
	 * @param pHeader
	 */
	void setHeader(final BitmapHeader pHeader)
		{
		header = pHeader;
		}

	/**
	 * The mask of the bitmap this descriptor refers to. Null for RGB bitmaps.
	 * 
	 * @return Mask.
	 */
	public BitmapMask getMask()
		{
		return mask;
		}

	/**
	 * Bitmap this descriptor refers to.
	 * 
	 * @return Bitmap.
	 */
	public AbstractBitmap getBitmap()
		{
		return bitmap;
		}

	void setBitmap(final AbstractBitmap pBitmap)
		{
		bitmap = pBitmap;
		}
	}
