/*
 * Copyright (C) 2007, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
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
import org.lateralgm.util.PropertyMap;

public class GmStreamDecoder
	{
	private InputStream in;
	private int[] table = null;
	private int pos = 0;

	public GmStreamDecoder(InputStream in)
		{
		if (in instanceof BufferedInputStream)
			this.in = in;
		else
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

	public void read(byte b[]) throws IOException
		{
		read(b,0,b.length);
		}

	public void read(byte b[], int off, int len) throws IOException
		{
		if (in.read(b,off,len) != len)
			{
			String error = Messages.format("GmStreamDecoder.UNEXPECTED_EOF",pos); //$NON-NLS-1$
			throw new IOException(error);
			}
		if (table != null)
			{
			for (int i = 0; i < len; i++)
				{
				int t = b[off + i] & 0xFF;
				int x = (table[t] - pos - i) & 0xFF;
				b[off + i] = (byte) x;
				}
			}
		pos += len;
		}

	public int read() throws IOException
		{
		int t = in.read();
		if (t == -1)
			{
			String error = Messages.format("GmStreamDecoder.UNEXPECTED_EOF",pos); //$NON-NLS-1$
			throw new IOException(error);
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
		byte[] b = new byte[4];
		read(b);
		int r = b[0] & 0xFF;
		for (byte i = 1; i < 4; i++)
			r |= (b[i] & 0xFF) << (8 * i);
		return r;
		}

	public String readStr() throws IOException
		{
		byte data[] = new byte[read4()];
		read(data);
		return new String(data);
		}

	public String readStr1() throws IOException
		{
		byte data[] = new byte[read()];
		read(data);
		return new String(data);
		}

	public boolean readBool() throws IOException
		{
		int val = read4();
		if (val != 0 && val != 1)
			{
			String error = Messages.format("GmStreamDecoder.INVALID_BOOLEAN",val,pos); //$NON-NLS-1$
			throw new IOException(error);
			}
		return val == 0 ? false : true;
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

	public <P extends Enum<P>>void read4(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			map.put(key,read4());
		}

	public <P extends Enum<P>>void readStr(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			map.put(key,readStr());
		}

	public <P extends Enum<P>>void readBool(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			map.put(key,readBool());
		}

	public <P extends Enum<P>>void readD(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			map.put(key,readD());
		}

	public byte[] decompress(int length) throws IOException,DataFormatException
		{
		return decompress(length,length);
		}

	public byte[] decompress(int length, int initialCapacity) throws IOException,DataFormatException
		{
		Inflater decompresser = new Inflater();
		byte[] compressedData = new byte[length];
		read(compressedData,0,length);
		decompresser.setInput(compressedData);
		byte[] result = new byte[131072];
		ByteArrayOutputStream baos = new ByteArrayOutputStream(initialCapacity);
		while (!decompresser.finished())
			{
			int len = decompresser.inflate(result);
			baos.write(result,0,len);
			}
		decompresser.end();
		return baos.toByteArray();
		}

	public GmStreamDecoder spawnInflater() throws IOException
		{
		return new GmStreamDecoder(new LimitedInflaterInputStream(in,read4()));
		}

	/**
	 * Safely finishes this stream if it's an inflater, otherwise this call does nothing.
	 * This places the file reader after the end of the compressed data in the underlying input stream.
	 */
	public void finishInflater() throws IOException
		{
		if (in instanceof LimitedInflaterInputStream) ((LimitedInflaterInputStream) in).finish();
		}

	public BufferedImage readZlibImage(int width, int height) throws IOException,DataFormatException
		{
		int length = read4();
		int estimate = height * width * 4 + 100; //100 for generous header
		return ImageIO.read(new ByteArrayInputStream(decompress(length,estimate)));
		}

	public BufferedImage readZlibImage() throws IOException,DataFormatException
		{
		return readZlibImage(0,0);
		}

	public BufferedImage readBGRAImage(int w, int h) throws IOException
		{
		WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,w,h,w * 4,4,
				new int[] { 2,1,0,3 },null); //2103 = RGBA ordering

		byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();

		int s = read4();
		if (s != data.length)
			throw new IOException(
					Messages.format("GmStreamDecoder.IMAGE_SIZE_MISMATCH",s,data.length,pos)); //$NON-NLS-1$

		read(data);

		BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
		dst.getRaster().setRect(raster);
		return dst;
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

	public InputStream getInputStream()
		{
		return in;
		}

	/**
	 * GM7 Notice: since the first useful byte after the seed isn't encrypted,
	 * you may wish to delay setting the seed until that byte is retrieved,
	 * as implemented such functionality into these lower-level routines would add overhead
	 */
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
