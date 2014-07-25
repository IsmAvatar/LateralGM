/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

/**
 * Class representing a line of code in our editor.
 */
public class Line
	{
	/** The StringBuilder we will use for line manipulation */
	public StringBuilder sbuild;
	/** A bit string indicating line attributes. A value less than zero marks invalidity. */
	public long attr;

	/**
	 * @author Josh Ventura
	 * Set of attributes for which to test.
	 */
	public static final class LINE_ATTRIBS
		{
		/** Zero mask; equal if the line has no status markers. */
		public static final long LA_NOTHING = 0x00;
		// Regular status markers
		/** True if the line is hidden by a fold. */
		public static final long LA_FOLDED = 0x01;
		/** Set if the line contains an error. */
		public static final long LA_ERROR = 0x02;
		/** Set if the line contains a warning. */
		public static final long LA_WARNING = 0x04;
		/** Set if the line contains a to-do point. */
		public static final long LA_TODO = 0x08;
		/** Set if the line contains a fix-me point. */
		public static final long LA_FIXME = 0x10;
		/** Set if the line contains a delete-me point. */
		public static final long LA_DELETEME = 0x20;
		/** Set if the line contains a point of interest. */
		public static final long LA_INTEREST = 0x40;
		/** Set if the line contains a bookmark. */
		public static final long LA_BOOKMARK = 0x80;
		/** Mask to test if the line contains any status marking(s). */
		public static final long LA_STATUSED = 0xFF;

		/** Strings, Comments, Preprocessors--if it's a colored block of code, it's here.
		Most shit in code happens on one line, anymore, but some of it still tends to run
		onto additional lines. These are all block items, such as those named above. They
		are one subcategory of things we highlight, but they're the only one on which each
		line needs to store data. **/
		public static final long LA_SCHEMEBLOCK = 0xFFFF00;
		/** The index of the first scheme bit. */
		public static final int LA_SCHEMEBITOFFSET = 8;

		/** Bit indicating the line has not been recently parsed and is not valid. */
		public static final long LA_INVALID = 0x80000000;
		/** Mask to test if the line has any attributes at all. */
		public static final long LA_ANY = 0x7FFFFF;
		}

	/**
	 * @param sb Construct a line from a string builder; set attributes to invalid.
	 */
	public Line(StringBuilder sb)
		{
		sbuild = sb;
		attr = -1;
		}

	/**
	 * @param line The line to copy.
	 */
	public Line(Line line)
		{
		sbuild = new StringBuilder(line.sbuild);
		attr = line.attr;
		}

	/**
	 * Check if this line has a given attribute.
	 * @param tAttr The attribute for which to check.
	 * @return Whether this line has the given attribute.
	 */
	public boolean testAttr(long tAttr)
		{
		return (attr & tAttr) != 0;
		}

	/**
	 * Check if this line has a certain set of attributes, ORed together.
	 * @param tAttr The OR of the attributes for which to check.
	 * @return Whether this line has all of the given attributes.
	 */
	public boolean testAttrAll(long tAttr)
		{
		return (attr & tAttr) == tAttr;
		}

	/**
	 * @param sAttr Set an attribute on this line.
	 */
	public void setAttr(long sAttr)
		{
		attr |= sAttr;
		}

	/**
	 * @param usAttr Unset an attribute on this line.
	 */
	public void unsetAttr(long usAttr)
		{
		attr &= ~usAttr;
		}
	}
