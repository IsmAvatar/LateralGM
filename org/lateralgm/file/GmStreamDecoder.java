/*
 * Copyright (C) 2007, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import org.lateralgm.messages.Messages;
import org.lateralgm.util.PropertyMap;

public class GmStreamDecoder extends StreamDecoder
	{
	protected int originalPos = -1;
	protected InputStream originalStream;
	private int[] table = null;

	public GmStreamDecoder(InputStream in)
		{
		super(in);
		}

	public GmStreamDecoder(String path) throws FileNotFoundException
		{
		super(path);
		}

	public GmStreamDecoder(File f) throws FileNotFoundException
		{
		super(f);
		}

	public int read(byte b[]) throws IOException
		{
		return read(b,0,b.length);
		}

	public int read(byte b[], int off, int len) throws IOException
		{
		int total = 0;
		while (true)
			{
			int n = in.read(b,off + total,len - total);
			if (n <= 0)
				{
				if (total == 0) total = n;
				break;
				}
			total += n;
			if (total == len) break;
			}

		if (total != len)
			{
			String error = Messages.format("StreamDecoder.UNEXPECTED_EOF",getPosString()); //$NON-NLS-1$
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
		return total;
		}

	public int read() throws IOException
		{
		int t = in.read();
		if (t == -1)
			{
			String error = Messages.format("StreamDecoder.UNEXPECTED_EOF",getPosString()); //$NON-NLS-1$
			throw new IOException(error);
			}
		if (table != null)
			{
			t = (table[t] - pos) & 0xFF;
			}
		pos++;
		return t;
		}

	/**
	 * ISO-8859-1 was the fixed charset in earlier LGM versions, so those parts of the code which
	 * have not been updated to set the charset explicitly should continue to use it to avoid
	 * regressions.
	 */
	private Charset charset = Charset.forName("ISO-8859-1");

	public Charset getCharset()
		{
		return charset;
		}

	public void setCharset(Charset charset)
		{
		this.charset = charset;
		}

	public String readStr() throws IOException
		{
		byte data[] = new byte[read4()];
		read(data);
		return new String(data,charset);
		}

	public String readStr1() throws IOException
		{
		byte data[] = new byte[read()];
		read(data);
		return new String(data,charset);
		}

	public boolean readBool() throws IOException
		{
		int val = read4();
		if (val != 0 && val != 1)
			{
			String error = Messages.format("GmStreamDecoder.INVALID_BOOLEAN",val,getPosString()); //$NON-NLS-1$
			throw new IOException(error);
			}
		return val == 0 ? false : true;
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

	public void beginInflate() throws IOException
		{
		int limit = read4();
		originalStream = in;
		in = new LimitedInflaterInputStream(originalStream,limit);
		originalPos = pos;
		pos = 0;
		}

	/**
	 * Safely finishes this stream if it's an inflater, otherwise this call does nothing.
	 * This places the file reader after the end of the compressed data in the underlying stream.
	 */
	public void endInflate() throws IOException
		{
		if (originalStream != null)
			{
			LimitedInflaterInputStream inf = (LimitedInflaterInputStream) in;
			inf.finish();
			pos = originalPos + (int) inf.getLimit();
			originalPos = -1;
			in = originalStream;
			originalStream = null;
			}
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
		//meta and setup
		final int datatype = DataBuffer.TYPE_BYTE, trans = Transparency.TRANSLUCENT;
		final int[] bitSizes = { 8,8,8,8 }, bitOrder = { 2,1,0,3 }; //2103 = RGBA ordering

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs,bitSizes,true,false,trans,datatype);
		WritableRaster raster = Raster.createInterleavedRaster(datatype,w,h,w * 4,4,bitOrder,null);

		//populate raster
		byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();

		int s = read4();
		if (s != data.length)
			throw new IOException(Messages.format(
					"GmStreamDecoder.IMAGE_SIZE_MISMATCH",s,data.length,getPosString())); //$NON-NLS-1$

		read(data);

		//combine and return
		return new BufferedImage(cm,raster,false,null);
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

	/**
	 * GM7 Notice: since the first useful byte after the seed isn't encrypted,
	 * you may wish to delay setting the seed until that byte is retrieved,
	 * as implementing such functionality into these lower-level routines would add overhead
	 */
	public void setSeed(int s)
		{
		if (s >= 0)
			table = makeDecodeTable(s);
		else
			table = null;
		}

	protected static int[] makeDecodeTable(int seed)
		{
		int[] encTable = GmStreamEncoder.makeEncodeTable(seed);
		return makeDecodeTable(encTable);
		}

	protected static int[] makeDecodeTable(int[] encTable)
		{
		int[] table = new int[256];
		for (int i = 1; i < 256; i++)
			table[encTable[i]] = i;
		return table;
		}

	/**
	 * If the stream is currently reading zlib data,
	 * this returns a string in the format:
	 * <code>&lt;file offset&gt;[&lt;decompressed data offset&gt;]</code><br/>
	 * Otherwise just the file offset is returned.
	 */
	protected String getPosString()
		{
		if (originalPos != -1) return originalPos + "[" + pos + "]";
		return Integer.toString(pos);
		}
	}
