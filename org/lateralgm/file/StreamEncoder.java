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

	public void write(int b) throws IOException
		{
		out.write(b);
		}

	public void write(byte[] b) throws IOException
		{
		out.write(b);
		}

	public void write(byte[] b, int off, int len) throws IOException
		{
		out.write(b,off,len);
		}

	public void write2(int val) throws IOException
		{
		short i = (short) val;
		write(i & 255);
		write((i >> 8) & 255);
		}

	public void write4(int val) throws IOException
		{
		out.write(val & 255);
		out.write((val >> 8) & 255);
		out.write((val >> 16) & 255);
		out.write((val >> 24) & 255);
		}

	public void writeD(double val) throws IOException
		{
		long num = Double.doubleToLongBits(val);
		out.write((int) ((num) & 255));
		out.write((int) ((num >> 8) & 255));
		out.write((int) ((num >> 16) & 255));
		out.write((int) ((num >> 24) & 255));
		out.write((int) ((num >> 32) & 255));
		out.write((int) ((num >> 40) & 255));
		out.write((int) ((num >> 48) & 255));
		out.write((int) ((num >> 56) & 255));
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
