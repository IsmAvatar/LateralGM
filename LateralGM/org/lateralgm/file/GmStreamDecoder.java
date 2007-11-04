/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import org.lateralgm.messages.Messages;

public class GmStreamDecoder
	{
	private BufferedInputStream in;
	private int[] table = null;
	private int pos = 0;

	public GmStreamDecoder(InputStream in)
		{
		this.in = new BufferedInputStream(in);
		}

	public GmStreamDecoder(String path) throws FileNotFoundException
		{
		in = new BufferedInputStream(new FileInputStream(path));
		}

	public GmStreamDecoder(File f) throws FileNotFoundException
		{
		in = new BufferedInputStream(new FileInputStream(f));
		}

	public int read(byte b[]) throws IOException
		{
		return read(b,0,b.length);
		}

	public int read(byte b[], int off, int len) throws IOException
		{
		int ret = in.read(b,off,len);
		if (ret != len)
			{
			String error = Messages.getString("GmStreamDecoder.UNEXPECTED_EOF"); //$NON-NLS-1$
			throw new IOException(String.format(error,pos));
			}
		if (table != null)
			{
			for (int i = 0; i < ret; i++)
				{
				int t = b[off + i] & 0xFF;
				int x = (table[t] - pos - i) & 0xFF;
				b[off + i] = (byte) x;
				}
			}
		pos += ret;
		return ret;
		}

	public int read() throws IOException
		{
		int t = in.read();
		if (t == -1)
			{
			String error = Messages.getString("GmStreamDecoder.UNEXPECTED_EOF"); //$NON-NLS-1$
			throw new IOException(String.format(error,pos));
			}
		if (table != null) t = (table[t] - pos) & 0xFF;
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

	public String readStr() throws IOException
		{
		byte data[] = new byte[read4()];
		long check = read(data);
		if (check < data.length)
			{
			String error = Messages.getString("GmStreamDecoder.UNEXPECTED_EOF"); //$NON-NLS-1$
			throw new IOException(String.format(error,pos));
			}
		return new String(data);
		}

	public String readStr1() throws IOException
		{
		byte data[] = new byte[read()];
		long check = read(data);
		if (check < data.length)
			{
			String error = Messages.getString("GmStreamDecoder.UNEXPECTED_EOF"); //$NON-NLS-1$
			throw new IOException(String.format(error,pos));
			}
		return new String(data);
		}

	public boolean readBool() throws IOException
		{
		int val = read4();
		if (val != 0 && val != 1)
			{
			String error = Messages.getString("GmStreamDecoder.INVALID_BOOLEAN"); //$NON-NLS-1$
			throw new IOException(String.format(error,val,pos));
			}
		return val == 0 ? false : true;
		}

	public double readD() throws IOException
		{
		int a = read();
		int b = read();
		int c = read();
		int d = read();
		int e = read();
		int f = read();
		int g = read();
		int h = read();
		long result = a | (long) b << 8 | (long) c << 16 | (long) d << 24 | (long) e << 32
				| (long) f << 40 | (long) g << 48 | (long) h << 56;
		return Double.longBitsToDouble(result);
		}

	public byte[] decompress(int length) throws IOException,DataFormatException
		{
		//BAOS default buffer size is 32
		return decompress(length,32);
		}

	public byte[] decompress(int length, int initialCapacity) throws IOException,DataFormatException
		{
		Inflater decompresser = new Inflater();
		byte[] compressedData = new byte[length];
		read(compressedData,0,length);
		decompresser.setInput(compressedData);
		byte[] result = new byte[1000];
		ByteArrayOutputStream baos = new ByteArrayOutputStream(initialCapacity);
		while (!decompresser.finished())
			{
			int len = decompresser.inflate(result);
			baos.write(result,0,len);
			}
		decompresser.end();
		return baos.toByteArray();
		}

	public BufferedImage readImage(int width, int height) throws IOException,DataFormatException
		{
		int length = read4();
		int estimate = height * width * 4 + 100; //100 for generous header
		return ImageIO.read(new ByteArrayInputStream(decompress(length,estimate)));
		}

	public BufferedImage readImage() throws IOException,DataFormatException
		{
		return readImage(0,0);
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

	/**
	 * Convenience method to retrieve whether the given bit is masked in bits,
	 * That is, if given flag is set.
	 * E.g.: to find out if the 3rd flag from right is set in 00011*0*10, use mask(26,4);
	 * @param bits - A cluster of flags/bits
	 * @param bit - The desired (and already shifted) bit or bits to mask
	 * @return Whether bit is masked in bits
	 */
	public static boolean mask(int bits, int bit)
		{
		return (bits & bit) == bit;
		}

	public BufferedInputStream getInputStream()
		{
		return in;
		}

	public void setSeed(int s)
		{
		if (s >= 0)
			table = makeSwapTable(s)[1];
		else
			table = null;
		}

	private static int[][] makeSwapTable(int seed)
		{
		int[][] table = new int[2][256];
		int a = 6 + (seed % 250);
		int b = seed / 250;
		for (int i = 0; i < 256; i++)
			table[0][i] = i;
		for (int i = 1; i < 10001; i++)
			{
			int j = 1 + ((i * a + b) % 254);
			int t = table[0][j];
			table[0][j] = table[0][j + 1];
			table[0][j + 1] = t;
			}
		for (int i = 1; i < 256; i++)
			table[1][table[0][i]] = i;
		return table;
		}
	}
