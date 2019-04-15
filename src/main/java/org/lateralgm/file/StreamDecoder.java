/*
 * Copyright (C) 2007, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.lateralgm.messages.Messages;

public class StreamDecoder extends InputStream
	{
	protected InputStream in;
	protected int pos = 0;
	protected int markPos = 0;

	/**
	 * This allows extending classes to override the
	 * stream wrapping behaviour.
	 */
	protected StreamDecoder()
		{
		}

	public StreamDecoder(InputStream in)
		{
		if (in instanceof BufferedInputStream)
			this.in = in;
		else
			this.in = new BufferedInputStream(in);
		}

	public StreamDecoder(String path) throws FileNotFoundException
		{
		in = new BufferedInputStream(new FileInputStream(path));
		}

	public StreamDecoder(File f) throws FileNotFoundException
		{
		in = new BufferedInputStream(new FileInputStream(f));
		}

	public int read(byte b[]) throws IOException
		{
		return read(b,0,b.length);
		}

	public int read(byte b[], int off, int len) throws IOException
		{
		int read = in.read(b,off,len);
		if (read != len)
			{
			String error = Messages.format("StreamDecoder.UNEXPECTED_EOF",getPosString()); //$NON-NLS-1$
			throw new IOException(error);
			}
		pos += len;
		return read;
		}

	public int read() throws IOException
		{
		int t = in.read();
		if (t == -1)
			{
			String error = Messages.format("StreamDecoder.UNEXPECTED_EOF",getPosString()); //$NON-NLS-1$
			throw new IOException(error);
			}
		pos++;
		return t;
		}

	public int read2() throws IOException
		{
		int a = read();
		int b = read();
		return (a | (b << 8));
		}

	public int read3() throws IOException
		{
		int a = read();
		int b = read();
		int c = read();
		return (a | (b << 8) | (c << 16));
		}

	public int read4() throws IOException
		{
		int a = read();
		int b = read();
		int c = read();
		int d = read();
		return (a | (b << 8) | (c << 16) | (d << 24));
		}

	public double readD() throws IOException
		{
		byte[] b = new byte[8];
		read(b);
		long r = b[0] & 0xFF;
		for (int i = 1; i < 8; i++)
			r |= (b[i] & 0xFFL) << (8 * i);
		return Double.longBitsToDouble(r);
		}

	public void close() throws IOException
		{
		in.close();
		}

	public long skip(long length) throws IOException
		{
		long total = in.skip(length);
		while (total < length)
			{
			total += in.skip(length - total);
			}
		pos += (int) length;
		return total;
		}

	public InputStream getInputStream()
		{
		return in;
		}

	public boolean markSupported()
		{
		return in.markSupported();
		}

	public synchronized void mark(int readlimit)
		{
		in.mark(readlimit);
		markPos = pos;
		}

	public synchronized void reset() throws IOException
		{
		in.reset();
		pos = markPos;
		}

	public long getPos()
		{
		return this.pos;
		}

	public void seek(final long pBytes) throws IOException
		{
		final long toSkip = pBytes - getPos();
		if (toSkip >= 0)
			{
			final long lBytesSkipped = skip(toSkip);
			if (lBytesSkipped != toSkip)
				{
				throw new IOException(Messages.format("StreamDecoder.SEEK_SHORT",toSkip,lBytesSkipped)); //$NON-NLS-1$
				}
			}
		else
			{
			throw new IllegalArgumentException(Messages.format("StreamDecoder.SEEK_PASSED",pBytes, //$NON-NLS-1$
					getPosString(),toSkip));
			}
		}

	protected String getPosString()
		{
		return Integer.toString(pos);
		}
	}
