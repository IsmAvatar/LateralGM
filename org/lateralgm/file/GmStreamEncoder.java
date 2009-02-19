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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

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
	}
