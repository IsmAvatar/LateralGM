package org.lateralgm.file.iconio;

import java.awt.Image;
import java.io.IOException;


/**
 * <p>
 * ICO file entry descriptor. Describes an embedded bitmap, and points to the
 * header/bitmap pair. I found that the descriptor often "lies" about size,
 * number of colors etc., hence the bitmap header should be used for reference.
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapDescriptor
	{
	private final int _width;

	private final int _height;

	private final int _colorCount;

	private final int _reserved;

	private final int _planes;

	private final int _bpp;

	private final long _size;

	private final long _offset;

	/** For convenience, not part of an entry: The header the entry refers to. */
	private BitmapHeader _header;

	/** For convenience, not part of an entry: The bitmap the entry refers to. */
	private AbstractBitmap _bitmap;

	/**
	 * For convenience, not part of an entry: The mask the entry refers to. Note
	 * that RGB images have no mask
	 */
	private BitmapMask _mask;

	/**
	 * Read the descriptor with the decoder (16 Bytes in total).
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:32
	public BitmapDescriptor(final AbstractDecoder pDec) throws IOException
		{
		_width = pDec.readUInt1();
		_height = pDec.readUInt1();
		_colorCount = pDec.readUInt1();

		_reserved = pDec.readUInt1();
		_planes = pDec.readUInt2();
		_bpp = pDec.readUInt2();
		_size = pDec.readUInt4();
		_offset = pDec.readUInt4();
		}

	/**
	 * @return Provides some information on the descriptor.
	 */
	public String toString()
		{
		return "width: " + _width + ", height: " + _height + ", colorCount: " + _colorCount + " ("
				+ getColorCount() + ")" + ", planes: " + _planes + ", BPP: " + _bpp + ", size: " + _size
				+ ", offset: " + _offset;
		}

	/**
	 * Image with indexed colors. Returns null if an indexed image can't be
	 * created (like, from an RGB icon - color mapping and dithering is a bit
	 * much for the time being). Transparency information that might be present
	 * in the ICO file is lost. See {@link #getImageRGB}.
	 * @return Image.
	 */
	public Image getImageIndexed()
		{
		if (!(_bitmap instanceof AbstractBitmapIndexed))
			{
			// Can't create indexed image from RGB icon.
			return null;
			}
		return ((AbstractBitmapIndexed) _bitmap).createImageIndexed();
		}

	/**
	 * Bits per pixel. If the bit count of the entry is 0, the bit count of the
	 * header is returned. See {@link #getBPPRaw}.
	 * @return Bits per pixel (fudged).
	 */
	public int getBPP()
		{
		if (_bpp != 0)
			{
			return _bpp;
			}
		return _header.getBPP();
		}

	/**
	 * The original bits per pixel count. See {@link #getBPP()}.
	 * @return Bits per pixel (raw).
	 */
	public int getBPPRaw()
		{
		return _bpp;
		}

	/**
	 * Image with ARGB colors. This method works for indexed color and RGB ICO
	 * files. Transparency information that might be present in the ICO is used.
	 * See {@link #getImageIndexed}.
	 * @return Image created from the bitmap.
	 */
	public Image getImageRGB()
		{
		return _bitmap.createImageRGB();
		}

	/**
	 * The original color count (note "0" means "256"). See
	 * {@link #getColorCount}.
	 * @return Color count (raw).
	 */
	public int getColorCountRaw()
		{
		return _colorCount;
		}

	/**
	 * The actual color count. See {@link #getColorCountRaw}.
	 * @return Color count (cooked).
	 */
	public int getColorCount()
		{
		return _colorCount == 0 ? 256 : _colorCount;
		}

	/**
	 * Bitmap height.
	 * @return Height.
	 */
	public int getHeight()
		{
		return _height;
		}

	/**
	 * Offset of header in ICO file.
	 * @return Offset.
	 */
	public long getOffset()
		{
		return _offset;
		}

	/**
	 * Number of planes ("1" for bitmaps, as far as I know).
	 * @return Planes.
	 */
	public int getPlanes()
		{
		return _planes;
		}

	/**
	 * Reserved value in the descriptor.
	 * @return Reserved value.
	 */
	public int getReserved()
		{
		return _reserved;
		}

	/**
	 * Hm - the size of the header and bitmap maybe?
	 * @return Size.
	 */
	public long getSize()
		{
		return _size;
		}

	/**
	 * Bitmap width.
	 * @return Width.
	 */
	public int getWidth()
		{
		return _width;
		}

	/**
	 * The header of the bitmap this descriptor refers to.
	 * @return Header.
	 */
	public BitmapHeader getHeader()
		{
		return _header;
		}

	/**
	 * @param pHeader
	 */
	void setHeader(final BitmapHeader pHeader)
		{
		_header = pHeader;
		}

	/**
	 * The mask of the bitmap this descriptor refers to. Null for RGB bitmaps.
	 * @return Mask.
	 */
	public BitmapMask getMask()
		{
		return _mask;
		}

	/**
	 * Bitmap this descriptor refers to.
	 * @return Bitmap.
	 */
	public AbstractBitmap getBitmap()
		{
		return _bitmap;
		}

	void setBitmap(final AbstractBitmap pBitmap)
		{
		_bitmap = pBitmap;
		}
	}
