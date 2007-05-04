/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

public class CustomFileFilter extends FileFilter
	{
	private ArrayList<String> ext = new ArrayList<String>();
	private String desc;
	
	public static String getExtension(String filename)
		{
		int p = filename.lastIndexOf(".");
		if (p == -1) return null;
		return filename.substring(p).toLowerCase(Locale.ENGLISH);
		}

	public CustomFileFilter(String ext, String desc)
		{
		this.ext.add(ext);
		this.desc = desc;
		}

	public CustomFileFilter(String[] ext, String desc)
		{
		for (int m = 0; m < ext.length; m++)
			{
			this.ext.add(ext[m]);
			}
		this.desc = desc;
		}

	public boolean accept(File f)
		{
		if (ext.size() == 0) return true;
		if (f.isDirectory()) return true;
		String s = getExtension(f.getPath());
		if (s == null)
			return false;
		return ext.contains(s);
		}

	public String getDescription()
		{
		return desc;
		}
	}