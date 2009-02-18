package org.lateralgm.file.iconio;

import java.io.IOException;

/**
 * <p>
 * Icon header. Describes the dimensions and properties of the icon.
 * </p>
 * <p>
 * 
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapHeader
	{
	// Always 40
	private final long headerSize;
	private final long width;
	// Weird, but this includes the height of the mask (so often header.height =
	// entry.height * 2
	private final long height;
	private final int planes;
	private final int bpp;
	private final TypeCompression compression;
	// Can be 0 when compression == 0 (b/c size can be calculated then ?!)
	private final long imageSize;
	private final long xPixelsPerM;
	private final long yPixelsPerM;
	private final long colorsUsed;
	private final long colorsImportant;

	/**
	 * Create a header from decoded information.
	 * 
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:26
	public BitmapHeader(final AbstractDecoder pDec) throws IOException
		{
		headerSize = pDec.readUInt4();
		width = pDec.readUInt4();
		height = pDec.readUInt4();
		planes = pDec.readUInt2();
		bpp = pDec.readUInt2();

		compression = TypeCompression.getType(pDec.readUInt4());

		imageSize = pDec.readUInt4();
		xPixelsPerM = pDec.readUInt4();
		yPixelsPerM = pDec.readUInt4();
		colorsUsed = pDec.readUInt4();
		colorsImportant = pDec.readUInt4();
		}

	public String toString()
		{
		return "size: " + headerSize + ", width: " + width + ", height: " + height + ", planes: "
				+ planes + ", BPP: " + bpp + /* ", compression: " + _compression + */", imageSize: "
				+ imageSize + ", XPixelsPerM: " + xPixelsPerM + ", YPixelsPerM: " + yPixelsPerM
				+ ", colorsUsed: " + colorsUsed + ", colorsImportant: " + colorsImportant
				+ (colorsImportant == 0 ? " (all)" : "");
		}

	/**
	 * Bits per pixel.
	 * 
	 * @return Bits per pixel.
	 */
	public int getBPP()
		{
		return bpp;
		}

	/**
	 * Number of important colors (0: All).
	 * 
	 * @return Important colors.
	 */
	public long getColorsImportant()
		{
		return colorsImportant;
		}

	/**
	 * Number of colors used (often not set properly).
	 * 
	 * @return Colors used.
	 */
	public long getColorsUsed()
		{
		return colorsUsed;
		}

	/**
	 * The bitmap compression type.
	 * 
	 * @return Compression type.
	 * @see TypeCompression
	 */
	public TypeCompression getCompression()
		{
		return compression;
		}

	/**
	 * Bitmap height. Note: It seems the mask gets reported as well, so divide this number by two.
	 * 
	 * @return Height.
	 */
	public long getHeight()
		{
		return height;
		}

	/**
	 * Bitmap size in bytes.
	 * 
	 * @return Bitmap size.
	 */
	public long getBitmapSize()
		{
		return imageSize;
		}

	/**
	 * Number of planes (always 1 in bitmaps, right?).
	 * 
	 * @return Planes.
	 */
	public int getPlanes()
		{
		return planes;
		}

	/**
	 * Header size (40 + 4 * color count, right?).
	 * 
	 * @return Hease size.
	 */
	public long getHeaderSize()
		{
		return headerSize;
		}

	/**
	 * Bitmap width.
	 * 
	 * @return Width.
	 */
	public long getWidth()
		{
		return width;
		}

	/**
	 * I'm not sure what this is.
	 * 
	 * @return ???
	 */
	public long getXPixelsPerM()
		{
		return xPixelsPerM;
		}

	/**
	 * I'm not sure what this is.
	 * 
	 * @return ???
	 */
	public long getYPixelsPerM()
		{
		return yPixelsPerM;
		}

	/**
	 * The number of colors (based on BPP).
	 * 
	 * @return Colors.
	 */
	public int getColorCount()
		{
		return 1 << bpp;
		}
	}
