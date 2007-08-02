/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.sub.Argument;

public class GmStreamEncoder
	{
	private BufferedOutputStream out;

	public GmStreamEncoder(File f) throws FileNotFoundException
		{
		out = new BufferedOutputStream(new FileOutputStream(f));
		}

	public GmStreamEncoder(String filePath) throws FileNotFoundException
		{
		out = new BufferedOutputStream(new FileOutputStream(filePath));
		}

	public void write(int b) throws IOException
		{
		out.write(b);
		}

	public void write(byte[] b) throws IOException
		{
		write(b,0,b.length);
		}

	public void write(byte[] b, int off, int len) throws IOException
		{
		write4(len);
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

	public void writeStr(String str) throws IOException
		{
		write4(str.length());
		out.write(str.getBytes("ascii"));
		}

	public void writeStr1(String str) throws IOException
		{
		write(Math.min(str.length(),255));
		out.write(str.getBytes("ascii"),0,Math.min(str.length(),255));
		}

	public void writeBool(boolean val) throws IOException
		{
		write4(val ? 1 : 0);
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

	public void compress(byte[] data) throws IOException
		{
		Deflater compresser = new Deflater();
		compresser.setInput(data);
		compresser.finish();
		byte[] buffer = new byte[100];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!compresser.finished())
			{
			int len = compresser.deflate(buffer);
			baos.write(buffer,0,len);
			}
		write4(baos.size());
		out.write(baos.toByteArray());
		}

	public void writeImage(Image image) throws IOException
		{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ImageIO.write((RenderedImage) image,"bmp",data);
		compress(data.toByteArray());
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

	public void writeId(ResId id, byte type, Gm6File src) throws IOException
		{
		writeId(id,type,-1,src);
		}

	public void writeId(ResId id, byte type, int noneval, Gm6File src) throws IOException
		{
		Resource res = src.getList(type).get(id);
		if (id != null && res != null)
			{
			write4(id.getValue());
			}
		else
			{
			write4(noneval);
			}
		}

	public void writeIdStr(ResId id, byte type, Gm6File src) throws IOException
		{
		// We can guarantee that "type" corresponds to one of the preset Resource types
		byte kind = Argument.getResourceKind(type);
		Resource res = src.getList(kind).get(id);
		if (id != null && res != null)
			{
			writeStr(Integer.toString(id.getValue()));
			}
		else
			{
			writeStr("-1");
			}
		}

	public void writeTree(ResNode root) throws IOException
		{
		Enumeration<?> e = root.preorderEnumeration();
		e.nextElement();
		while (e.hasMoreElements())
			{
			ResNode node = (ResNode) e.nextElement();
			write4(node.status);
			write4(node.kind);
			if (node.resourceId != null)
				write4(node.resourceId.getValue());
			else
				write4(0);
			writeStr((String) node.getUserObject());
			write4(node.getChildCount());
			}
		}
	}
