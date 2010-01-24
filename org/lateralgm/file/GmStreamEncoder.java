/*
 * Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import static org.lateralgm.main.Util.deRef;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.util.PropertyMap;

public class GmStreamEncoder extends StreamEncoder
	{
	public GmStreamEncoder(OutputStream o)
		{
		super(o);
		}

	public GmStreamEncoder(File f) throws FileNotFoundException
		{
		super(f);
		}

	public GmStreamEncoder(String filePath) throws FileNotFoundException
		{
		super(filePath);
		}

	public <P extends Enum<P>>void write4(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			write4((Integer) map.get(key));
		}

	public <P extends Enum<P>>void writeStr(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			writeStr((String) map.get(key));
		}

	public <P extends Enum<P>>void writeBool(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			writeBool((Boolean) map.get(key));
		}

	public <P extends Enum<P>>void writeD(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			writeD((Double) map.get(key));
		}

	public <R extends Resource<R,?>>void writeId(ResourceReference<R> id) throws IOException
		{
		writeId(id,-1);
		}

	public <R extends Resource<R,?>>void writeId(ResourceReference<R> id, int noneval)
			throws IOException
		{
		if (deRef(id) != null)
			write4(id.get().getId());
		else
			write4(noneval);
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

	public void writeZlibImage(BufferedImage image) throws IOException
		{
		//Drop any alpha channel and convert to 3-byte to ensure that the image is bmp-compatible
		ColorConvertOp conv = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB),null);
		final BufferedImage dest = new BufferedImage(image.getWidth(),image.getHeight(),
				BufferedImage.TYPE_3BYTE_BGR);
		conv.filter(image,dest);

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ImageIO.write(dest,"bmp",data);
		compress(data.toByteArray());
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
	}
