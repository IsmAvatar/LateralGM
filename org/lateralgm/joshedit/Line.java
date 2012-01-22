/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

class Line
{
	StringBuilder sbuild; // The StringBuilder we will use for line manipulation
	long attr; // A bit string indicating line attributes. A value less than zero marks invalidity.

	public static final class LINE_ATTRIBS {
		public static final long LA_NOTHING = 0x00,
				// Regular status markers
				LA_FOLDED = 0x01, LA_ERROR = 0x02, LA_WARNING = 0x04, LA_TODO = 0x08,
				LA_FIXME = 0x10,
				LA_DELETEME = 0x20,
				LA_INTEREST = 0x40,
				LA_BOOKMARK = 0x80,
				LA_STATUSED = 0xFF,
	
				/** Strings, Comments, Preprocessors--if it's a colored block of code, it's here.
				Most shit in code happens on one line, anymore, but some of it still tends to run
				onto additional lines. These are all block items, such as those named above. They
				are one subcategory of things we highlight, but they're the only one on which each
				line needs to store data. **/
				LA_SCHEMEBLOCK = 0xFFFF00,
				LA_SCHEMEBITOFFSET = 8,
	
				LA_INVALID = 0x80000000, LA_ANY = 0x7FFFFF;
	}
	
	public Line(StringBuilder sb)
	{
		sbuild = sb;
		attr = -1;
	}

	public Line(Line line)
	{
		sbuild = new StringBuilder(line.sbuild);
		attr = line.attr;
	}

	public boolean testAttr(long tAttr)
	{
		return (attr & tAttr) != 0;
	}

	public boolean testAttrAll(long tAttr)
	{
		return (attr & tAttr) == tAttr;
	}

	public void setAttr(long sAttr)
	{
		attr |= sAttr;
	}

	public void unsetAttr(long usAttr)
	{
		attr &= ~usAttr;
	}
}
