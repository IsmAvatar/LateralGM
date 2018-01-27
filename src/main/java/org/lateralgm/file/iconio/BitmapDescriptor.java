package org.lateralgm.file.iconio;

import java.awt.Image;
import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

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
	//See note in constructor: the ICO format's width/height are broken and ignored.
	//private int width;
	//private int height;
	private int colorCount;
	private int reserved;
	private int planes;
	private int bpp;
	private long size;
	private long offset;

	/** For convenience, not part of an entry: The header the entry refers to. */
	private BitmapHeader header;

	/** For convenience, not part of an entry: The bitmap the entry refers to. */
	private AbstractBitmap bitmap;

	/**
	 * Read the descriptor with the decoder (16 Bytes in total).
	 *
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:32
	public BitmapDescriptor(final StreamDecoder pDec) throws IOException
		{
		// The ICO format's width/height fields are universally ignored
		// (Windows and Linux will both ignore them even if they are clearly set wrong).
		// Instead, you have to use the internal bitmap/png's width/height.
		/*int ignoredWidth =*/ pDec.read();
		/*int ignoredHeight =*/ pDec.read();

		colorCount = pDec.read();

		reserved = pDec.read();
		planes = pDec.read2();
		bpp = pDec.read2();
		size = pDec.read4();
		offset = pDec.read4();
		}

	/**
	 * @return Provides some information on the descriptor.
	 */
	public String toString()
		{
		return "width: " + getWidth() + ", height: " + getHeight() + ", colorCount: " + colorCount + " ("
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
		return (header==null || header.getHeight()>Integer.MAX_VALUE) ? 0 : ((int)header.getHeight());
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

	public void setOffset(long offset)
		{
		this.offset = offset;
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

	public void setSize(long size)
		{
		this.size = size;
		}

	/**
	 * Bitmap width.
	 *
	 * @return Width.
	 */
	public int getWidth()
		{
		return (header==null || header.getWidth()>Integer.MAX_VALUE) ? 0 : ((int)header.getWidth());
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

	void write(StreamEncoder out) throws IOException
		{
		out.write(getWidth());
		out.write(getHeight());
		out.write(colorCount);

		out.write(reserved);
		out.write2(planes);
		out.write2(bpp);
		out.write4((int) size);
		out.write4((int) offset);
		}
	}
