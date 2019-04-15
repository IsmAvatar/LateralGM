/*
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

public class CustomFileFilter extends FileFilter implements FilenameFilter
	{
	private ArrayList<String> ext = new ArrayList<String>();
	private String desc;

	/**
	 * Gets the extension part of the given filename, including the period
	 * @param filename
	 * @return the extension, including period
	 */
	public static String getExtension(String filename)
		{
		int p = filename.lastIndexOf(".");
		if (p == -1) return null;
		return filename.substring(p).toLowerCase(Locale.ENGLISH);
		}

	public CustomFileFilter(String desc, String...ext)
		{
		this.desc = desc;
		for (String element : ext)
			this.ext.add(element);
		}

	public boolean accept(File f)
		{
		if (f.isDirectory()) return true;
		return accept(f,f.getPath());
		}

	public boolean accept(File dir, String name)
		{
		if (ext.size() == 0) return true;
		//if (f.isDirectory()) return true;
		for (String e : ext)
			if (name.endsWith(e))
				return true;
		return false;
		}

	public String getDescription()
		{
		return desc;
		}

	public String[] getExtensions()
		{
		return ext.toArray(new String[0]);
		}
	}
