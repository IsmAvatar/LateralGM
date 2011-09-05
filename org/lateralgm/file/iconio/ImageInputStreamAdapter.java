package org.lateralgm.file.iconio;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

public class ImageInputStreamAdapter extends InputStream
	{
	private ImageInputStream in;

	public ImageInputStreamAdapter(ImageInputStream iis)
		{
		in = iis;
		}

	public int read() throws IOException
		{
		return in.read();
		}

	public int read(byte[] b) throws IOException
		{
		return in.read(b);
		}

	public int read(byte[] b, int off, int len) throws IOException
		{
		return in.read(b,off,len);
		}

	public long skip(long n) throws IOException
		{
		return in.skipBytes(n);
		}

	public int available() throws IOException
		{
		return 1;
		}

	public void close() throws IOException
		{
		in.close();
		}

	public synchronized void mark(int readlimit)
		{
		in.mark();
		}

	public synchronized void reset() throws IOException
		{
		in.reset();
		}

	public boolean markSupported()
		{
		return true;
		}
	}
