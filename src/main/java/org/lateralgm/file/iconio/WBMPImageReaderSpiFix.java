/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package org.lateralgm.file.iconio;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi;

/**
 * A Minor hack of WBMPImageReaderSpi to prevent ICO files from being mistaken
 * for WBMP. Note that compatibility with files produced by the buggy Sony Ericson
 * encoder is lost.
 */
public class WBMPImageReaderSpiFix extends WBMPImageReaderSpi
	{
	public boolean canDecodeInput(Object source) throws IOException
		{
		if (!(source instanceof ImageInputStream)) return false;
		ImageInputStream stream = (ImageInputStream) source;

		stream.mark();
		boolean ret = stream.read() != 0 || stream.read() != 0 || readMultiInt(stream) <= 0
				|| readMultiInt(stream) <= 0;
		stream.reset();
		return !ret;
		}

	private static int readMultiInt(ImageInputStream s) throws IOException
		{
		int val = s.readByte();
		int ret = val & 0x7f;
		while ((val & 0x80) == 0x80 && ret <= 0x7fffff)
			{
			ret <<= 7;
			val = s.readByte();
			ret |= (val & 0x7f);
			}
		return ret;
		}
	}
