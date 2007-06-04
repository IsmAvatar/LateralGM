package org.lateralgm.file.iconio;

import java.io.IOException;

/**
 * <p>
 * Byte stream decoder for 1, 2, and 4 byte values in big or little endian format.
 * </p>
 * 
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public abstract class AbstractDecoder
	{
	/** Highest order byte comes first. */
	public static final int BIG_ENDIAN = 0;

	/** Lowest order byte comes first. */
	public static final int LITTLE_ENDIAN = 1;

	/** Determines the byte order in multi byte values. */
	private int endianness = BIG_ENDIAN;

	protected long pos;

	/** Static buffer to read values w/o allocating an array every time. */
	private final byte[] readBuf = new byte[4];

	/**
	 * @return A one byte value (aka BYTE, unsigned char)
	 * @throws java.io.IOException
	 */
	public short readUInt1() throws IOException
		{
		return (short) readValue(1);
		}

	/**
	 * @return A two byte value (aka WORD, unsigned short)
	 * @throws java.io.IOException
	 */
	public int readUInt2() throws IOException
		{
		return (int) readValue(2);
		}

	/**
	 * @return A four byte value (aka DWORD, unsigned long).
	 * @throws java.io.IOException
	 */
	public long readUInt4() throws IOException
		{
		return readValue(4);
		}

	/**
	 * @param pEndianess The byte order
	 * @see #BIG_ENDIAN
	 * @see #LITTLE_ENDIAN
	 */
	public void setEndianess(final int pEndianess)
		{
		endianness = pEndianess;
		}

	/**
	 * @return Current position in file
	 */
	public long getPos()
		{
		return pos;
		}

	/**
	 * @param pPos Position to advance to. Nothing will happen if the position has already been
	 *          passed.
	 * @throws java.io.IOException
	 */
	public abstract void seek(long pPos) throws IOException;

	/**
	 * Implemented by a specific decoder.
	 * 
	 * @param pBytes Bytes to read
	 * @param pBuffer The buffer to write the read bytes to. If null, a buffer is reserved.
	 * @return Array with the bytes read.
	 * @throws java.io.IOException
	 */
	public abstract byte[] readBytes(long pBytes, byte[] pBuffer) throws IOException;

	protected long readValue(final int pBytes) throws IOException
		{
		readBytes(pBytes,readBuf);
		if (pBytes == 1)
			{
			// Shortcut: endianness plays no role here.
			return readBuf[0] & 0xFF;
			}

		long lValue = 0;
		if (endianness == BIG_ENDIAN)
			{
			for (int lByteNo = 0; lByteNo < pBytes; lByteNo++)
				{
				lValue <<= 8;
				lValue += readBuf[lByteNo] & 0xff;
				}
			}
		else
			{
			for (int lByteNo = pBytes - 1; lByteNo >= 0; lByteNo--)
				{
				lValue <<= 8;
				lValue += readBuf[lByteNo] & 0xff;
				}
			}

		return lValue;
		}

	/**
	 * Call when done with decoder.
	 * 
	 * @throws IOException
	 */
	public abstract void close() throws IOException;
	}
