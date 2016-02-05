/*
 * Copyright (C) 2007, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StreamEncoder extends OutputStream
	{
	protected OutputStream out;
	protected int pos = 0;

	/**
	 * This allows extending classes to override the
	 * stream wrapping behaviour.
	 */
	protected StreamEncoder()
		{
		}

	public StreamEncoder(OutputStream o)
		{
		if (o instanceof BufferedOutputStream)
			out = o;
		else
			out = new BufferedOutputStream(o);
		}

	public StreamEncoder(File f) throws FileNotFoundException
		{
		out = new BufferedOutputStream(new FileOutputStream(f));
		}

	public StreamEncoder(String filePath) throws FileNotFoundException
		{
		out = new BufferedOutputStream(new FileOutputStream(filePath));
		}

	public void write(byte[] b) throws IOException
		{
		out.write(b);
		}

	public void write(byte[] b, int off, int len) throws IOException
		{
		out.write(b,off,len);
		pos += len;
		}

	public void write(int b) throws IOException
		{
		out.write(b);
		pos++;
		}

	public void write2(int val) throws IOException
		{
		short i = (short) val;
		write(i & 255);
		write((i >>> 8) & 255);
		}

	public void write3(int val) throws IOException
		{
		write(val & 255);
		write((val >>> 8) & 255);
		write((val >>> 16) & 255);
		}

	public void write4(int val) throws IOException
		{
		write(val & 255);
		write((val >>> 8) & 255);
		write((val >>> 16) & 255);
		write((val >>> 24) & 255);
		}

	public void writeD(double val) throws IOException
		{
		long num = Double.doubleToLongBits(val);
		byte[] b = new byte[8];
		b[0] = (byte) (num & 0xFF);
		for (int i = 1; i < 8; i++)
			b[i] = (byte) ((num >>> (8 * i)) & 0xFF);
		write(b);
		}

	public void close() throws IOException
		{
		out.close();
		}

	public void fill(int count) throws IOException
		{
		for (int i = 0; i < count; i++)
			{
			write4(0);
			}
		}

	public void flush() throws IOException
		{
		out.flush();
		}
	}
