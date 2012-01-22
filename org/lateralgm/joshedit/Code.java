/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Code extends ArrayList<Line>
{
	private static final long serialVersionUID = 1L;

	public void add(int index, StringBuilder sb)
	{
		super.add(index,new Line(sb));
		fireLinesChanged();
	}

	public void add(int index, String substring)
	{
		add(index,new StringBuilder(substring));
	}

	public boolean add(StringBuilder sb)
	{
		boolean r = super.add(new Line(sb));
		fireLinesChanged();
		return r;
	}

	public boolean add(String s)
	{
		return add(new StringBuilder(s));
	}

	@Override
	public Line remove(int index)
	{
		Line r = super.remove(index);
		fireLinesChanged();
		return r;
	}

	public StringBuilder getsb(int index)
	{
		return super.get(index).sbuild;
	}

	/**
	 * A CodeListener listens for lines being added/removed.
	 * Use a JoshText.LineListener for individual characters.
	 */
	public static interface CodeListener extends EventListener
	{
		public void codeChanged(Code.CodeEvent e);
	}

	public List<Code.CodeListener> listenerList = new LinkedList<Code.CodeListener>();

	public void addCodeListener(Code.CodeListener l)
	{
		listenerList.add(l);
	}

	public void removeCodeListener(Code.CodeListener l)
	{
		listenerList.remove(l);
	}

	protected void fireLinesChanged()
	{
		for (Code.CodeListener l : listenerList)
			l.codeChanged(new CodeEvent(this,CodeEvent.LINES_CHANGED));
	}

	public static class CodeEvent extends AWTEvent
	{
		private static final long serialVersionUID = 1L;
		public static final int LINES_CHANGED = 0;

		public CodeEvent(Object source, int id)
		{
			super(source,id);
		}

	}

	class FindResults
	{
		int line, pos;
		int endLine, endPos;

		FindResults(int l, int p, int L)
		{
			line = l;
			pos = p;
			endLine = l;
			endPos = p + L;
		}

		FindResults(int l, int p, int le, int pe)
		{
			line = l;
			pos = p;
			endLine = le;
			endPos = pe;
		}
	}

	public FindResults findPrevious(String[] findme, int lineFrom, int posFrom)
	{
		/* FIXME: I'm not sure what to do with this, yet. This method only works right
		          for one instance per line, and regexps can't be traversed backward. */

		for (int y = lineFrom; y >= 0; y--)
		{
			if (findme.length == 1)
			{
				int io = find_prev_in(getsb(y),findme[0].toLowerCase(),y == lineFrom ? posFrom : getsb(y).length());
				if (io != -1)
					return new FindResults(y,io,findme[0].length());
			}
		}
		return null;
	}

	private static int find_prev_in(StringBuilder sb, String findme, int from)
	{
		if (from == 0)
			return -1;
		if (FindDialog.sens.isSelected())
		  return sb.lastIndexOf(findme, from-1);
	  return sb.toString().toLowerCase().lastIndexOf(findme.toLowerCase(),from-1);
	}

	public FindResults findNext(Pattern p, int lineFrom, int posFrom)
	{
		for (int y = lineFrom; y < size(); y++)
		{
			Matcher m = p.matcher(getsb(y).toString());
			int si = y == lineFrom ? posFrom : 0;
			if (m.find(si)) return new FindResults(y,m.start(),y,m.end());
		}
		return null;
	}

	public FindResults findNext(String[] findme, int lineFrom, int posFrom)
	{
		findMain: for (int y = lineFrom; y < size(); y++)
		{
			int io = find_next_in(getsb(y),findme[0],y == lineFrom ? posFrom : 0);
			if (io == -1) continue;

			if (findme.length == 1) return new FindResults(y,io,findme[0].length());

			int intermediate;
			for (intermediate = 1; intermediate < findme.length - 1; intermediate++)
			{
				if (!findme[intermediate].equals(getsb(y + intermediate))) continue findMain;
			}
			if (getsb(y + intermediate).length() >= findme[intermediate].length()
					&& getsb(y + intermediate).substring(0,findme[intermediate].length()).equals(
							findme[intermediate]))
				return new FindResults(io,y,findme[intermediate].length(),y + intermediate);
		}
		return null;
	}

	private static int find_next_in(StringBuilder sb, String findme, int from)
	{
		if (FindDialog.sens.isSelected()) return sb.indexOf(findme,from);
		return sb.toString().toLowerCase().indexOf(findme,from);
	}
}
