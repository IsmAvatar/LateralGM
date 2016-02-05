package org.lateralgm.file.iconio;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;

import org.lateralgm.file.StreamDecoder;
import org.lateralgm.file.StreamEncoder;

/**
 * <p>
 * Parent class for indexed bitmaps (1, 4, and 8 bits per pixel). The value of a pixel refers to an
 * entry in the color palette. The bitmap has a mask which is a 1 BPP bitmap specifiying whether a
 * pixel is transparent or opaque.
 * </p>
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public abstract class AbstractBitmapIndexed extends AbstractBitmap
	{

	private static final int OPAQUE = 255;

	/**
	 * The color palette. Refered to by the pixel values. The size is expected to be 2^BPP, but see
	 * getVerifiedColorCount() for a discussion of this.
	 */
	private Color[] colorPalette;

	/** The pixel values. The value refers to an entry in the color palette. */
	protected int[] pixels;

	/**
	 * Create a bitmap with a color table and a mask.
	 *
	 * @param pDescriptor The descriptor.
	 */
	public AbstractBitmapIndexed(final BitmapDescriptor pDescriptor)
		{
		super(pDescriptor);

		pixels = new int[getWidth() * getHeight()];
		}

	/**
	 * Needed to be replaced for indexed images because they contain a color palette and a mask which
	 * needs to be read as well.
	 *
	 * @param in The decoder.
	 * @throws IOException
	 */
	void read(final StreamDecoder in) throws IOException
		{
		readColorPalette(in);
		readBitmap(in);
		readMask(in);
		}

	/**
	 * This functions is needed b/c all classes read the bitmap, but not always a color table and a
	 * mask.
	 *
	 * @param pDec The decoder.
	 * @throws IOException
	 */
	abstract void readBitmap(final StreamDecoder pDec) throws IOException;

	/**
	 * @param in The decoder.
	 * @throws IOException
	 */
	private void readColorPalette(final StreamDecoder in) throws IOException
		{
		final int lColorCount = getVerifiedColorCount();
		colorPalette = new Color[lColorCount];
		for (int lColorNo = 0; lColorNo < lColorCount; lColorNo++)
			{
			setColor(lColorNo,readColor(in));
			}
		}

	private static Color readColor(final StreamDecoder in) throws IOException
		{
		final int lBlue = in.read();
		final int lGreen = in.read();
		final int lRed = in.read();
		// "Reserved"
		in.read();

		return new Color(lRed,lGreen,lBlue);
		}

	/**
	 * Thanks to eml@ill.com for pointing out that official color count might not be what it should:
	 * 2^BPP specifies the miminum size for the color palette!
	 *
	 * @return The verified color count.
	 */
	private int getVerifiedColorCount()
		{
		int lColorCount = getColorCount();
		final int lColorCount2 = 1 << descriptor.getBPP();
		if (lColorCount < lColorCount2)
			{
			lColorCount = lColorCount2;
			}
		return lColorCount;
		}

	/**
	 * @return BufferedImage (palette) created from the indexed bitmap.
	 */
	public BufferedImage createImageIndexed()
		{
		final IndexColorModel lModel = createColorModel();
		final BufferedImage lImage = new BufferedImage(getWidth(),getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED,lModel);
		lImage.getRaster().setSamples(0,0,getWidth(),getHeight(),0,pixels);
		return lImage;
		}

	/**
	 * @return Color model created from color palette in entry.
	 */
	private IndexColorModel createColorModel()
		{
		final int lColorCount = getVerifiedColorCount();

		final byte[] lRed = new byte[lColorCount];
		final byte[] lGreen = new byte[lColorCount];
		final byte[] lBlue = new byte[lColorCount];
		final byte[] lAlpha = new byte[lColorCount];

		for (int lColorNo = 0; lColorNo < lColorCount; lColorNo++)
			{
			final Color lColor = getColor(lColorNo);
			lRed[lColorNo] = (byte) lColor.getRed();
			lGreen[lColorNo] = (byte) lColor.getGreen();
			lBlue[lColorNo] = (byte) lColor.getBlue();
			lAlpha[lColorNo] = (byte) OPAQUE;
			}

		final IndexColorModel lModel = new IndexColorModel(8,lColorCount,lRed,lGreen,lBlue,lAlpha);
		return lModel;
		}

	/**
	 * @return BufferedImage (ARGB) from the indexed bitmap.
	 */
	public BufferedImage createImageRGB()
		{
		final BufferedImage lImage = new BufferedImage(getWidth(),getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		// For each pixel, copy the color information.
		for (int lYPos = 0; lYPos < getHeight(); lYPos++)
			{
			for (int lXPos = 0; lXPos < getWidth(); lXPos++)
				{
				int lRGB = getColor(lXPos,lYPos).getRGB();
				if (transparencyMask.isOpaque(lXPos,lYPos))
					{
					// Visible (sic), set alpha to opaque
					lRGB |= 0xFF000000;
					}
				else
					{
					// Invisible, set alpha to transparent.
					lRGB &= 0x00FFFFFF;
					}
				lImage.setRGB(lXPos,lYPos,lRGB);
				}
			}

		return lImage;
		}

	/**
	 * Get the color for the specified point.
	 *
	 * @param pXPos The x position.
	 * @param pYPos The y position.
	 * @return Color of the selected point.
	 */
	public Color getColor(final int pXPos, final int pYPos)
		{
		return getColor(getPaletteIndex(pXPos,pYPos));
		}

	/**
	 * Index into the color palette for the specified point.
	 *
	 * @param pXPos The x position.
	 * @param pYPos The y position.
	 * @return Palette index for pixel x, y
	 */
	public int getPaletteIndex(final int pXPos, final int pYPos)
		{
		return pixels[pYPos * getWidth() + pXPos];
		}

	/**
	 * Get the color for the specified color palette index.
	 *
	 * @param pIndex of the color requested.
	 * @return Requested color.
	 */
	public Color getColor(final int pIndex)
		{
		if (pIndex >= getVerifiedColorCount())
			{
			throw new IllegalArgumentException("Color index out of range: is " + pIndex + ", max. "
					+ getVerifiedColorCount());
			}
		return colorPalette[pIndex];
		}

	/**
	 * @param pIndex Color index.
	 * @param pColor Color to set.
	 */
	private void setColor(final int pIndex, final Color pColor)
		{
		colorPalette[pIndex] = pColor;
		}

	void write(StreamEncoder out) throws IOException
		{
		writeColorPalette(out);
		writeBitmap(out);
		writeMask(out);
		}

	private void writeColorPalette(StreamEncoder out) throws IOException
		{
		for (Color c : colorPalette)
			{
			//bgr res
			out.write(c.getBlue());
			out.write(c.getGreen());
			out.write(c.getRed());
			out.write(0); //reserved
			}
		}

	abstract void writeBitmap(StreamEncoder out) throws IOException;
	}
