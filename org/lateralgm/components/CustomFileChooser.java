/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.messages.Messages;

public class CustomFileChooser extends JFileChooser
	{
	private static final long serialVersionUID = 1L;
	private Preferences prefs;
	private String propertyName;

	public CustomFileChooser(String node, String propertyName)
		{
		this.propertyName = propertyName;
		prefs = Preferences.userRoot().node(node);
		setCurrentDirectory(new File(prefs.get(propertyName,getCurrentDirectory().getAbsolutePath())));
		}

	public void approveSelection()
		{
		super.approveSelection();
		saveDir();
		}

	public void cancelSelection()
		{
		super.cancelSelection();
		saveDir();
		}

	private void saveDir()
		{
		prefs.put(propertyName,getCurrentDirectory().getAbsolutePath());
		}

	/**
	 * Sets the given <code>FilterSet</code> to be the current set
	 * of chooseable file filters. The first item in the list will be set as
	 * the currently selected filter.
	 * @param filters The list of filters to use
	 */
	public void setFilterSet(FilterSet fs)
		{
		if (fs == null) throw new IllegalArgumentException("null FilterSet");
		resetChoosableFileFilters();
		for (FileFilter filt : fs)
			addChoosableFileFilter(filt);
		if (fs.size() > 0) setFileFilter(fs.get(0));
		}

	public static class FilterSet extends ArrayList<FileFilter>
		{
		private static final long serialVersionUID = 1L;

		public void addFilter(String descKey, String...exts)
			{
			add(new CustomFileFilter(Messages.getString(descKey),exts));
			}
		}
	}
