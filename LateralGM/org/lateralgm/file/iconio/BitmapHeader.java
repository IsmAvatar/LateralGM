package org.lateralgm.file.iconio;

import java.io.IOException;


/**
 * <p>
 * Icon header. Describes the dimensions and properties of the icon.
 * </p>
 * <p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapHeader
	{
	// Always 40
	private final long _headerSize;

	private final long _width;

	// Weird, but this includes the height of the mask (so often header.height =
	// entry.height * 2
	private final long _height;

	private final int _planes;

	private final int _bpp;

	private final TypeCompression _compression;

	// Can be 0 when compression == 0 (b/c size can be calculated then ?!)
	private final long _imageSize;

	private final long _xPixelsPerM;

	private final long _yPixelsPerM;

	private final long _colorsUsed;

	private final long _colorsImportant;

	/**
	 * Create a header from decoded information.
	 * @param pDec
	 *            The decoder.
	 * @throws IOException
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:26
	public BitmapHeader(final AbstractDecoder pDec) throws IOException
		{
		_headerSize = pDec.readUInt4();
		_width = pDec.readUInt4();
		_height = pDec.readUInt4();
		_planes = pDec.readUInt2();
		_bpp = pDec.readUInt2();

		_compression = TypeCompression.getType(pDec.readUInt4());

		_imageSize = pDec.readUInt4();
		_xPixelsPerM = pDec.readUInt4();
		_yPixelsPerM = pDec.readUInt4();
		_colorsUsed = pDec.readUInt4();
		_colorsImportant = pDec.readUInt4();
		}

	public String toString()
		{
		return "size: " + _headerSize + ", width: " + _width + ", height: " + _height + ", planes: "
				+ _planes + ", BPP: " + _bpp + /*", compression: " + _compression +*/", imageSize: "
				+ _imageSize + ", XPixelsPerM: " + _xPixelsPerM + ", YPixelsPerM: " + _yPixelsPerM
				+ ", colorsUsed: " + _colorsUsed + ", colorsImportant: " + _colorsImportant
				+ (_colorsImportant == 0 ? " (all)" : "");
		}

	/**
	 * Bits per pixel.
	 * @return Bits per pixel.
	 */
	public int getBPP()
		{
		return _bpp;
		}

	/**
	 * Number of important colors (0: All).
	 * @return Important colors.
	 */
	public long getColorsImportant()
		{
		return _colorsImportant;
		}

	/**
	 * Number of colors used (often not set properly).
	 * @return Colors used.
	 */
	public long getColorsUsed()
		{
		return _colorsUsed;
		}

	/**
	 * The bitmap compression type.
	 * @return Compression type.
	 * @see TypeCompression
	 */
	public TypeCompression getCompression()
	 {
	 return _compression;
	 }

	/**
	 * Bitmap height. Note: It seems the mask gets reported as well, so divide
	 * this number by two.
	 * @return Height.
	 */
	public long getHeight()
		{
		return _height;
		}

	/**
	 * Bitmap size in bytes.
	 * @return Bitmap size.
	 */
	public long getBitmapSize()
		{
		return _imageSize;
		}

	/**
	 * Number of planes (always 1 in bitmaps, right?).
	 * @return Planes.
	 */
	public int getPlanes()
		{
		return _planes;
		}

	/**
	 * Header size (40 + 4 * color count, right?).
	 * @return Hease size.
	 */
	public long getHeaderSize()
		{
		return _headerSize;
		}

	/**
	 * Bitmap width.
	 * @return Width.
	 */
	public long getWidth()
		{
		return _width;
		}

	/**
	 * I'm not sure what this is.
	 * @return ???
	 */
	public long getXPixelsPerM()
		{
		return _xPixelsPerM;
		}

	/**
	 * I'm not sure what this is.
	 * @return ???
	 */
	public long getYPixelsPerM()
		{
		return _yPixelsPerM;
		}

	/**
	 * The number of colors (based on BPP).
	 * @return Colors.
	 */
	public int getColorCount()
		{
		return 1 << _bpp;
		}
	}