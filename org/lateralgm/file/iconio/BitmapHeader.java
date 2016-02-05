package org.lateralgm.file.iconio;

import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

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
	private long headerSize;
	private long width;
	// Weird, but this includes the height of the mask (so often header.height =
	// entry.height * 2
	private long height;
	private int planes;
	private int bpp;
	private TypeCompression compression;
	// Can be 0 when compression == 0 (b/c size can be calculated then ?!)
	private long imageSize;
	private long xPixelsPerM;
	private long yPixelsPerM;
	private long colorsUsed;
	private long colorsImportant;

	/**
	 * Create a header from decoded information.
	 *
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:26
	public BitmapHeader(final StreamDecoder pDec) throws IOException
		{
		pDec.mark(4);
		headerSize = pDec.read4();
		//0x89+PNG is the start of a png header
		if (headerSize == 0x474E5089)
			{
			headerSize = -1;
			compression = TypeCompression.BI_PNG;
			pDec.reset();
			}
		else
			{
			width = pDec.read4();
			height = pDec.read4();
			planes = pDec.read2();
			bpp = pDec.read2();

			compression = TypeCompression.getType(pDec.read4());

			imageSize = pDec.read4();
			xPixelsPerM = pDec.read4();
			yPixelsPerM = pDec.read4();
			colorsUsed = pDec.read4();
			colorsImportant = pDec.read4();
			}
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

	void write(StreamEncoder out) throws IOException
		{
		out.write4((int) headerSize);
		out.write4((int) width);
		out.write4((int) height);
		out.write2(planes);
		out.write2(bpp);

		out.write4(TypeCompression.BI_RGB.getValue());

		out.write4((int) imageSize);
		out.write4((int) xPixelsPerM);
		out.write4((int) yPixelsPerM);
		out.write4((int) colorsUsed);
		out.write4((int) colorsImportant);
		}
	}
