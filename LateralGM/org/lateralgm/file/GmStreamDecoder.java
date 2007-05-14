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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import org.lateralgm.components.ResNode;
import org.lateralgm.resources.Resource;

public class GmStreamDecoder
	{
	private BufferedInputStream in;

	public GmStreamDecoder(String path) throws FileNotFoundException
		{
		in = new BufferedInputStream(new FileInputStream(path));
		}

	public int read() throws IOException
		{
		return in.read();
		}

	public int read(byte b[]) throws IOException
		{
		return read(b,0,b.length);
		}

	public int read(byte b[], int off, int len) throws IOException
		{
		return in.read(b,off,len);
		}

	public int readi() throws IOException
		{
		int a = in.read();
		int b = in.read();
		int c = in.read();
		int d = in.read();
		if (a == -1 || b == -1 || c == -1 || d == -1)
			throw new IOException(Messages.getString("GmStreamDecoder.UNEXPECTED_EOF")); //$NON-NLS-1$
		long result = (a | (b << 8) | (c << 16) | (d << 24));
		return (int) result;
		}

	public String readStr() throws IOException
		{
		byte data[] = new byte[readi()];
		long check = in.read(data);
		if (check < data.length) throw new IOException(Messages.getString("GmStreamDecoder.UNEXPECTED_EOF")); //$NON-NLS-1$
		return new String(data);
		}

	public boolean readBool() throws IOException
		{
		int val = readi();
		if (val != 0 && val != 1)
			throw new IOException(String.format(Messages.getString("GmStreamDecoder.INVALID_BOOLEAN"),val)); //$NON-NLS-1$
		if (val == 0) return false;
		return true;
		}

	public double readD() throws IOException
		{
		int a = in.read();
		int b = in.read();
		int c = in.read();
		int d = in.read();
		int e = in.read();
		int f = in.read();
		int g = in.read();
		int h = in.read();
		long result = (long) a | (long) b << 8 | (long) c << 16 | (long) d << 24 | (long) e << 32
				| (long) f << 40 | (long) g << 48 | (long) h << 56;
		return Double.longBitsToDouble(result);
		}

	public byte[] decompress(int length) throws IOException,DataFormatException
		{
		Inflater decompresser = new Inflater();
		byte[] compressedData = new byte[length];
		in.read(compressedData,0,length);
		decompresser.setInput(compressedData,0,compressedData.length);
		byte[] result = new byte[100];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!decompresser.finished())
			{
			int len = decompresser.inflate(result);
			baos.write(result,0,len);
			}
		decompresser.end();
		return baos.toByteArray();
		}

	public BufferedImage readImage() throws IOException,DataFormatException
		{
		int length = readi();
		return ImageIO.read(new ByteArrayInputStream(decompress(length)));
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
		return total;
		}

	public void readTree(ResNode root, Gm6File src) throws IOException
		{
		Stack<ResNode> path = new Stack<ResNode>();
		Stack<Integer> left = new Stack<Integer>();
		path.push(root);
		int cur = 11;
		while (cur-- > 0)
			{
			byte status = (byte) readi();
			byte type = (byte) readi();
			int ind = readi();
			String name = readStr();
			ResNode node = path.peek().addChild(name,status,type);
			if (status == ResNode.STATUS_SECONDARY && type != Resource.GAMEINFO && type != Resource.GAMESETTINGS)
				{
				node.resourceId = src.getList(node.kind).getUnsafe(ind).getId();

				// GM actually ignores the name given
				node.setUserObject(src.getList(node.kind).getUnsafe(ind).getName());
				// in the tree data
				}
			int contents = readi();
			if (contents > 0)
				{
				left.push(new Integer(cur));
				cur = contents;
				path.push(node);
				}
			while (cur == 0 && !left.isEmpty())
				{
				cur = left.pop().intValue();
				path.pop();
				}
			}
		}
	}
