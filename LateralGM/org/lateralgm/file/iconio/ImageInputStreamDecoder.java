package org.lateralgm.file.iconio;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

/**
 * <p>
 * File decoder based on ImageInputStream (Chris' implementation).
 * </p>
 * &copy; 2001 Christian Treber, ct@ctreber.com
 * @author Christian Treber, ct@ctreber.com
 */
public class ImageInputStreamDecoder extends AbstractDecoder
	{
	private final ImageInputStream stream;

	/**
	 * Create a BIG_ENDIAN file decoder. See
	 * {@link AbstractDecoder#setEndianess}to change the default behavior.
	 * @param pStream
	 *            The image input stream to read from.
	 */
	public ImageInputStreamDecoder(final ImageInputStream pStream)
		{
		super();
		stream = pStream;
		}

	public void seek(final long pPos) throws IOException
		{
		stream.seek(pPos);
		}
	
	public void mark()
		{
		stream.mark();
		}
	
	public void reset() throws IOException
		{
		stream.reset();
		}

	public byte[] readBytes(final long pBytes, final byte[] pBuffer) throws IOException
		{
		byte[] lBuffer = pBuffer;
		if (lBuffer == null)
			{
			lBuffer = new byte[(int) pBytes];
			}
		else
			{
			if (lBuffer.length < pBytes)
				{
				throw new IllegalArgumentException("Insufficient space in buffer");
				}
			}

		final int lBytesRead = stream.read(lBuffer,0,(int) pBytes);
		if (lBytesRead != pBytes)
			{
			throw new IOException("Tried to read " + pBytes + " bytes, but obtained " + lBytesRead);
			}
		
		pos += pBytes;

		return lBuffer;
		}

	public void close() throws IOException
		{
		stream.close();
		}
	}
