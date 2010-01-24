package org.lateralgm.file.iconio;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * <p>
 * ICO image service provider plugin. Supports only the most basic ImageIO
 * options (i.e., fires no events etc.).
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class ICOReader extends ImageReader
	{
	private static final int[] ONE = new int[1];
	protected ICOFile icoFile;
	protected ImageInputStream stream;

	/**
	 * @param pProvider
	 *            Handle back to the provider.
	 */
	public ICOReader(final ImageReaderSpi pProvider)
		{
		super(pProvider);
		}

	public int getHeight(final int pImageIndex)
		{
		return getICOEntry(pImageIndex).getHeight();
		}

	public IIOMetadata getImageMetadata(final int pImageIndex)
		{
		return null;
		}

	public Iterator<ImageTypeSpecifier> getImageTypes(final int pImageIndex)
		{
		final List<ImageTypeSpecifier> lTypes = new ArrayList<ImageTypeSpecifier>();
		for (int lImageNo = 0; lImageNo < getNumImages(false); lImageNo++)
			{
			final ImageTypeSpecifier lSpecifier = ImageTypeSpecifier.createInterleaved(
					ColorSpace.getInstance(ColorSpace.CS_sRGB),ONE,DataBuffer.TYPE_BYTE,false,false);
			lTypes.add(lSpecifier);

			}
		return lTypes.iterator();
		}

	public int getNumImages(final boolean pAllowSearch)
		{
		return getICOFile().getImageCount();
		}

	public IIOMetadata getStreamMetadata()
		{
		return null;
		}

	public int getWidth(final int pImageIndex)
		{
		return getICOEntry(pImageIndex).getWidth();
		}

	public BufferedImage read(final int pImageIndex, final ImageReadParam pParam)
		{
		return getICOEntry(pImageIndex).getBitmap().createImageRGB();
		}

	public void setInput(final Object pInput, final boolean pSeekForwardOnly,
			final boolean pIgnoreMetadata)
		{
		if (!(pInput instanceof ImageInputStream))
			{
			throw new IllegalArgumentException("Only ImageInputStream supported as input source");
			}

		stream = (ImageInputStream) pInput;
		}

	/**
	 * Get ICOFile object (cached).
	 * @return The ICOFile object
	 */
	private ICOFile getICOFile()
		{
		if (icoFile == null)
			{
			try
				{
				icoFile = new ICOFile(new ImageInputStreamAdapter(stream));
				}
			catch (IOException e)
				{
				System.err.println("Can't create ICOFile: " + e.getMessage());
				}
			}

		return icoFile;
		}

	private BitmapDescriptor getICOEntry(final int pImageIndex)
		{
		return getICOFile().getDescriptors().get(pImageIndex);
		}
	}
