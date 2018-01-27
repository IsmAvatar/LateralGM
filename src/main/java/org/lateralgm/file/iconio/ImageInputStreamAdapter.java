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

	@Override
	public int read(byte[] b) throws IOException
		{
		return in.read(b);
		}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
		{
		return in.read(b,off,len);
		}

	@Override
	public long skip(long n) throws IOException
		{
		return in.skipBytes(n);
		}

	@Override
	public int available() throws IOException
		{
		return 1;
		}

	@Override
	public void close() throws IOException
		{
		in.close();
		}

	@Override
	public synchronized void mark(int readlimit)
		{
		in.mark();
		}

	@Override
	public synchronized void reset() throws IOException
		{
		in.reset();
		}

	@Override
	public boolean markSupported()
		{
		return true;
		}
	}
