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

/**
 * Class representing the actual code body in our editor.
 */
public class Code extends ArrayList<Line>
{
	/** Stuff it, ECJ. */
	private static final long serialVersionUID = 1L;

	/**
	 * @param index Position at which to insert the row.
	 * @param sb The StringBuilder of the line to insert.
	 */
	public void add(int index, StringBuilder sb)
	{
		super.add(index,new Line(sb));
		fireLinesChanged();
	}

	/**
	 * @param index The index at which to insert the row.
	 * @param string The string from which to create a StringBuilder to insert.
	 */
	public void add(int index, String string)
	{
		add(index,new StringBuilder(string));
	}

	/**
	 * Append a line given by a StringBuilder to the code.
	 * The builder is not copied, but added by reference. Do not add the same builder multiple times.
	 * @param sb The StringBuilder represetning the line to append.
	 * @return True, as specified in Collection.add(E).
	 */
	public boolean add(StringBuilder sb)
	{
		boolean r = super.add(new Line(sb));
		fireLinesChanged();
		return r;
	}

	/**
	 * Append a line to the code.
	 * @param s The string to append to the end of the code.
	 * @return True, as specified in Collection.add(E).
	 */
	public boolean add(String s)
	{
		return add(new StringBuilder(s));
	}

	/** Remove the line with the given index. 
	 * @return Returns the Line that was removed. */
	@Override
	public Line remove(int index)
	{
		Line r = super.remove(index);
		fireLinesChanged();
		return r;
	}

	/**
	 * @param index The row index from which to retrieve the StringBuilder.
	 * @return The StringBuilder representing the row with the given index.
	 */
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
		/** @param e The event representing the code change. */
		public void codeChanged(Code.CodeEvent e);
	}

	/** List of listeners to inform on code modification. */
	public List<Code.CodeListener> listenerList = new LinkedList<Code.CodeListener>();

	/** @param l The code listener to add. */
	public void addCodeListener(Code.CodeListener l)
	{
		listenerList.add(l);
	}

	/** @param l The code listener to remove.	 */
	public void removeCodeListener(Code.CodeListener l)
	{
		listenerList.remove(l);
	}

	/** Fire code listener events. */
	protected void fireLinesChanged()
	{
		for (Code.CodeListener l : listenerList)
			l.codeChanged(new CodeEvent(this,CodeEvent.LINES_CHANGED));
	}

	/** Event related to this code. */
	public static class CodeEvent extends AWTEvent
	{
		/** Shut up, ECJ. */
		private static final long serialVersionUID = 1L;
		/** Constant given to the lines-changed event. */
		public static final int LINES_CHANGED = 0;

		/**
		 * @param source The Code that fired the event.
		 * @param id The ID of the event.
		 */
		public CodeEvent(Object source, int id)
		{
			super(source,id);
		}

	}

	/**
	 * Class containing info about a string search result.
	 * @author Josh Ventura
	 */
	class FindResults
	{
		/** The first line of the match. */
		int line;
		/** The starting position of the match. */
		int pos;
		/** The last line of the match. */
		int endLine;
		/** The ending position of the match. */
		int endPos;

		/**
		 * Construct for single-line result.
		 * @param l The line on which the result was found.
		 * @param p The position in the line at which the result was found.
		 * @param L The length of the match.
		 */
		FindResults(int l, int p, int L)
		{
			line = l;
			pos = p;
			endLine = l;
			endPos = p + L;
		}

		/**
		 * Construct for multi-line result.
		 * @param l The first line on which the result was found.
		 * @param p The position in the line at which the result was found.
		 * @param le The last line on which the result matched.
		 * @param pe The position on the final line the marks the end of the result.
		 */
		FindResults(int l, int p, int le, int pe)
		{
			line = l;
			pos = p;
			endLine = le;
			endPos = pe;
		}
	}

	/**
	 * @param findme The string to find, exploded at newlines.
	 * @param lineFrom The line at which to begin searching.
	 * @param posFrom The position in the line at which to begin searching.
	 * @return The results of the find.
	 */
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

	/**
	 * @param sb The StringBuilder to search in.
	 * @param findme The substring to find.
	 * @param from The position at which to begin the search.
	 * @return The index of the result, or -1 if not found.
	 */
	private static int find_prev_in(StringBuilder sb, String findme, int from)
	{
		if (from == 0)
			return -1;
		if (FindDialog.sens.isSelected())
		  return sb.lastIndexOf(findme, from-1);
	  return sb.toString().toLowerCase().lastIndexOf(findme.toLowerCase(),from-1);
	}

	/**
	 * @param p The regular expression pattern to find.
	 * @param lineFrom The line at which to begin searching.
	 * @param posFrom The position in the line at which to begin searching.
	 * @return The results of the find.
	 */
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

	/**
	 * @param findme The string to find, exploded at newlines.
	 * @param lineFrom The line at which to begin searching.
	 * @param posFrom The position in the line at which to begin searching.
	 * @return The results of the find.
	 */
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

	/**
	 * @param sb The StringBuilder to search in.
	 * @param findme The substring to find.
	 * @param from The position at which to begin the search.
	 * @return The index of the result, or -1 if not found.
	 */
	private static int find_next_in(StringBuilder sb, String findme, int from)
	{
		if (FindDialog.sens.isSelected()) return sb.indexOf(findme,from);
		return sb.toString().toLowerCase().indexOf(findme,from);
	}
}
