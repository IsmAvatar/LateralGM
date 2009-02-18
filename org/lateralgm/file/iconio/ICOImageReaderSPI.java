package org.lateralgm.file.iconio;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * <p>
 * Information for the ICO image service provider plugin.
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class ICOImageReaderSPI extends ImageReaderSpi
	{
	/**
	 * Define the capabilities of this provider service.
	 */
	public ICOImageReaderSPI()
		{
		super("Christian Treber, www.ctreber.com, ct@ctreber.com","1.0 December 2003",new String[] {
				"ico","ICO" },new String[] { "ico" },new String[] { "image/x-ico" },
				"org.lateralgm.file.iconio.ICOReader",STANDARD_INPUT_TYPE,null,false,null,null,null,null,
				false,null,null,null,null);
		}

	public boolean canDecodeInput(final Object pSource)
		{
		if (pSource instanceof ImageInputStream)
			{
			ImageInputStreamDecoder in = new ImageInputStreamDecoder((ImageInputStream) pSource);
			in.setEndianess(AbstractDecoder.LITTLE_ENDIAN);
			try
				{
				in.mark();
				int res = in.readUInt2();
				int type = in.readUInt2();
				in.reset();
				if (res == 0 && type == 1) return true;
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			}
		return false;
		}

	/**
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 * @param pExtension
	 *            Not used by our reader.
	 */
	public ImageReader createReaderInstance(final Object pExtension)
		{
		return new ICOReader(this);
		}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 * @param pLocale
	 *            Ignored - one locale fits all.
	 */
	public String getDescription(final Locale pLocale)
		{
		return "Microsoft Icon Format (ICO)";
		}
	}
