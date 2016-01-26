/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This class implements a stream filter for uncompressing data in the
 * "deflate" compression format, limited to a certain given number of compressed bytes.
 *
 * @see Inflater
 * @author IsmAvatar
 */
public class LimitedInflaterInputStream extends InflaterInputStream
	{
	protected long limit;
	private boolean closed = false;

	/**
	 * Creates a new input stream with a default decompressor and buffer size,
	 * limited to reading up to a given number of compressed bytes.
	 * @param in the input stream
	 * @param limit the maximum number of compressed bytes that may be read
	 */
	public LimitedInflaterInputStream(InputStream in, long limit)
		{
		super(in,new Inflater(),512);
		this.limit = limit;
		}

	/**
	 * Returns 0 if the end of file has been reached, otherwise returns
	 * the remaining compressed bytes left until the limit is reached.
	 * <p>
	 * Programs should not count on this method to return the number
	 * of bytes that could be read without blocking.
	 *
	 * @return     remaining compressed bytes left until limit reached
	 * @exception  IOException  if an I/O error occurs.
	 */
	public int available() throws IOException
		{
		ensureOpen();
		if (super.available() == 0) return 0;
		return (int) Math.min(limit,Integer.MAX_VALUE);
		}

	private void ensureOpen() throws IOException
		{
		if (closed) throw new IOException("Stream closed");
		}

	/**
	 * Skips the remaining compressed bytes in this stream until limit is reached,
	 * and releases any system resources associated with the inflater.
	 * Essentially closes this input stream without closing the underlying input stream.
	 * @throws IOException if an I/O error has occurred
	 */
	public void finish() throws IOException
		{
		if (!closed)
			{
			while (limit > 0 && in.available() > 0)
				limit -= in.skip(limit);
			inf.end(); //default inflater assumed
			closed = true;
			}
		}

	/**
	 * Closes this input stream, closes the underlying input stream,
	 * and releases any system resources associated with the stream.
	 * @exception IOException if an I/O error has occurred
	 */
	public void close() throws IOException
		{
		if (!closed)
			{
			inf.end(); //default inflater assumed
			in.close();
			closed = true;
			}
		}

	/**
	 * Fills input buffer with more data to decompress, not exceeding the limit.
	 * @exception IOException if an I/O error has occurred
	 */
	protected void fill() throws IOException
		{
		ensureOpen();
		int len = in.read(buf,0,(int) Math.min(limit,buf.length));
		if (len == -1) throw new EOFException("Unexpected end of ZLIB input stream");
		limit -= len;
		inf.setInput(buf,0,len);
		}

	public long getLimit()
		{
		return limit;
		}
	}
