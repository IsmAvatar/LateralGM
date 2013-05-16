/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.lateralgm.joshedit.JoshText.CodeMetrics;
import org.lateralgm.joshedit.JoshText.Highlighter;

/**
 *	Class describing a selection in 
 */
public class Selection implements Highlighter
{
	/** Selection type enumeration; JoshEdit allows multiple kinds of selection. */
	enum ST
	{
		/** Normal selection, as seen in most traditional editors. */
		NORM,
		/** Rectangle selection, as in some terminal interfaces. */
		RECT
	}

	/** The type of the current selection.  */
	ST type = ST.NORM;

	/** The code from which our selections are made. */
	Code code;
	/** The JoshText class that owns our code. */
	JoshText joshText;
	/** The caret component of our selection. */
	Caret caret;

	/**
	 * A trailing selection position indicator. Call deselect() to update this to the caret position.
	 * If this and row are the same as the caret position, there is no selection.
	 */
	int col;
	/**
	 * A trailing selection position indicator. Call deselect() to update this to the caret position.
	 * If this and col are the same as the caret position, there is no selection.
	 */
	int row;

	/**
	 * @param txt The Code from which we will make selections.
	 * @param joshTxt The JoshText that owns the code.
	 * @param pt The caret component of the selection.
	 */
	public Selection(Code txt, JoshText joshTxt, Caret pt)
	{
		code = txt;
		joshText = joshTxt;
		caret = pt;
	}

	/**
	 * @param selection Another selection to copy.
	 */
	public Selection(Selection selection)
	{
		col = selection.col;
		row = selection.row;
		code = selection.code;
		caret = selection.caret;
	}

	/**
	 * Updates the trailing selection position indicator to the caret position,
	 * essentially removing the selection.
	 * This is invoked e.g. when an arrow key is pressed without Shift held down.
	 * @param reset Whether to also reset the selection type back to Normal.
	 * This is mostly just a convenience for when we know to switch to Normal.
	 */
	public void deselect(boolean reset)
	{
		row = caret.row;
		col = caret.col;
		if (reset) changeType(ST.NORM);
	}

	/**
	 * Copy the contents of the current selection to the clipboard,
	 * if anything is selected. Otherwise, do nothing.
	 */
	public void copy()
	{
		if (isEmpty()) return;
		StringSelection stringSelection = new StringSelection(getSelectedTextForCopy());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection,joshText);
	}

	/** Returns the number of lines affected by a potential paste in this selection.
	 * @param str The text which would be pasted.
	 * @return The number of lines affected; ie, max(lines in paste, lines selected).
	 **/
	public int getInsertRipple(String str)
	{
		if (type == ST.RECT) return Math.abs(caret.row - row) + 1;
		if (str.length() > 0 && str.charAt(str.length() - 1) == 0)
			return Math.max(str.split("(\r?\n|\r)",-1).length,1);
		return 1;
	}

	/**
	 * Returns the number of lines affected by a potential paste in this selection.
	 * @return Same as getInsertRipple(clipboard text).
	 */
	public int getPasteRipple()
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		try
		{
			String str = (String) contents.getTransferData(DataFlavor.stringFlavor);
			return getInsertRipple(str);
		}
		catch (UnsupportedFlavorException e)
		{
			// Clipboard format unsupported; no sense erroring.
		}
		catch (IOException e)
		{
			// Something broke reading the clipboard. Maybe it was too big.
			// Who knows? The user probably does, and probably isn't surprised.
		}
		return 0;
	}

	/**
	 * Paste the contents of the clipboard into the current selection.
	 * @return The last affected row index.
	 */
	public int paste()
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		try
		{
			String ins = (String) contents.getTransferData(DataFlavor.stringFlavor);
			if (ins.length() > 0 && ins.charAt(ins.length() - 1) == 0)
				return Math.min(row,caret.row)
						+ Math.max(0,insertRect(ins.substring(0,ins.length() - 1)) - 1);
			insert(ins);
			return caret.row;
		}
		catch (UnsupportedFlavorException e)
		{
			joshText.infoMessages.add("Clipboard contains no text.");
		}
		catch (IOException e)
		{
			joshText.infoMessages.add("Clipboard I/O error: Maybe it's massive?");
		}
		return caret.row;
	}

	/** A fallback Clipboard to facilitate middle-click paste on Mac and Windows. */
	static Clipboard fallbackMCClipboard;

	/** Do a paste from the middle-click clipboard into the code.
	 * @return The last row affected by the paste. */
	public int middleClickPaste()
	{
		Clipboard a = Toolkit.getDefaultToolkit().getSystemSelection();
		if (a == null) a = fallbackMCClipboard;
		if (a == null) return caret.row;
		Transferable contents = a.getContents(null);
		try
		{
			String str = (String) contents.getTransferData(DataFlavor.stringFlavor);
			if (str.length() > 0 && str.charAt(str.length() - 1) == 0)
				return caret.row + Math.max(0,insertRect(str.substring(0,str.length() - 1)) - 1);
			insert(str);
			return caret.row;
		}
		catch (UnsupportedFlavorException e)
		{
			joshText.infoMessages.add("Middle click paste: No data.");
			System.err.println("Middle click paste: No data.");
		}
		catch (IOException e)
		{
			joshText.infoMessages.add("Middle click paste: I/O Exception.");
			System.err.println("Middle click paste: I/O Exception.");
		}
		caret.positionChanged();
		return caret.row;
	}

	/**
	 * @return The number of lines affected by a middle click paste.
	 */
	public int getMiddlePasteRipple()
	{
		Clipboard a = Toolkit.getDefaultToolkit().getSystemSelection();
		if (a == null) a = fallbackMCClipboard;
		if (a == null) return 0;
		Transferable contents = a.getContents(null);
		try
		{
			String str = (String) contents.getTransferData(DataFlavor.stringFlavor);
			return getInsertRipple(str);
		}
		catch (UnsupportedFlavorException e)
		{
			// Clipboard format unsupported; no sense erroring.
		}
		catch (IOException e)
		{
			// Something broke reading the clipboard. Maybe it was too big.
			// Who knows? The user probably does, and probably isn't surprised.
		}
		return 0;
	}

	/** Stores the selection in the SystemSelection, e.g. for middle-click. */
	public void selectionChanged()
	{
		if (isEmpty()) return;
		Clipboard a = Toolkit.getDefaultToolkit().getSystemSelection();
		if (a == null) a = fallbackMCClipboard;
		if (a == null) a = fallbackMCClipboard = new Clipboard("Improvised Middle-Click Clipboard");
		StringSelection stringSelection = new StringSelection(getSelectedTextForCopy());
		a.setContents(stringSelection,joshText);
	}

	/** Move the caret to the beginning of the selection and deselect. */
	public void moveBegin()
	{
		if (row > caret.row)
		{
			row = caret.row;
			col = caret.col;
		}
		else
		{
			caret.row = row;
			caret.col = col;
		}
		type = ST.NORM;
	}

	/** Move the caret to the end of the selection and deselect. */
	public void moveEnd()
	{
		if (row > caret.row)
		{
			caret.row = row;
			caret.col = col;
		}
		else
		{
			row = caret.row;
			col = caret.col;
		}
		type = ST.NORM;
	}

	/** Deletes the selection if there is one, returns false on no selection.
	 * @return Returns whether any selection was deleted. */
	public boolean deleteSel()
	{
		if (isEmpty()) return false;
		switch (type)
		{
			case NORM:
				if (row == caret.row)
				{
					code.getsb(row).delete(Math.min(col,caret.col),Math.max(col,caret.col));
					caret.col = col = Math.min(col,caret.col);
				}
				else if (row > caret.row)
				{
					code.getsb(row).replace(0,col,code.getsb(caret.row).substring(0,caret.col));
					while (row > caret.row)
						code.remove(--row);
					col = caret.col;
				}
				else
				{
					code.getsb(caret.row).replace(0,caret.col,code.getsb(row).substring(0,col));
					while (caret.row > row)
						code.remove(--caret.row);
					caret.col = col;
				}
				break;
			case RECT:
				int x1 = Math.min(col,caret.col),
				x2 = Math.max(col,caret.col);
				for (int y = Math.min(row,caret.row); y <= Math.max(row,caret.row); y++)
				{
					StringBuilder s = code.getsb(y);
					int ax1 = joshText.column_to_index(y,x1), ax2 = joshText.column_to_index(y,x2);
					if (ax1 < s.length()) s.delete(ax1,Math.min(ax2,s.length()));
				}
				col = caret.col = x1;
				caret.colw = joshText.line_wid_at(caret.row,caret.col);
				return true;
		}
		deselect(false);
		return true;
	}

	/**
	 * Test if the selection is empty (contains no text).
	 * @return Whether the selection is empty.
	 */
	public boolean isEmpty()
	{
		switch (type)
		{
			case NORM:
				return col == caret.col && row == caret.row;
			case RECT:
				return col == caret.col;
		}
		return true;
	}

	/**
	 * @return The text in this selection.
	 */
	public String getText()
	{
		if (isEmpty()) return "";
		if (row == caret.row)
			return code.getsb(row).substring(Math.min(col,caret.col),Math.max(col,caret.col));
		final int f = Math.min(row,caret.row), l = Math.max(row,caret.row);
		StringBuilder ret = new StringBuilder(code.getsb(f).substring(f == row ? col : caret.col));
		for (int y = f + 1; y < l; y++)
			ret.append(System.getProperty("line.separator") + code.getsb(y));
		ret.append(System.getProperty("line.separator")
				+ code.getsb(l).substring(0,l == row ? col : caret.col));
		return new String(ret);
	}

	/** Duplicates selection, returning number of new lines created
	 * @return The number of new lines created.
	 */
	public int duplicate()
	{
		switch (type)
		{
			case NORM:
				if (isEmpty())
				{
					code.add(row + 1,new StringBuilder(code.getsb(row)));
					return 1;
				}
				String n = getText();
				int res = Math.abs(caret.row - row) + 1;
				Selection ssel = new Selection(this);
				Caret scar = new Caret(caret);
				moveEnd();
				insert(n);
				resetcoords(ssel);
				caret.resetcoords(scar);
				return (res - 1) * 2;
			case RECT:

				break;
		}
		return 0;
	}

	/**
	 * Copy coordinates from a prior selection.
	 * @param ssel The Selection from which coordinates will be copied.
	 */
	private void resetcoords(Selection ssel)
	{
		col = ssel.col;
		row = ssel.row;
	}

	/**
	 * @param c Insert a character at the cursor, erasing any selected text.
	 */
	public void insert(char c)
	{
		switch (type)
		{
			case NORM:
				deleteSel();
				code.getsb(caret.row).insert(caret.col++,c);
				col = caret.col;
				caret.colw = joshText.line_wid_at(caret.row,caret.col);
				break;
			case RECT:
				deleteSel();
				for (int y = Math.min(row,caret.row); y <= Math.max(row,caret.row); y++)
				{
					if (joshText.column_in_tab(y,caret.col)) continue;
					final int ipos = joshText.column_to_index_unsafe(y,caret.col);
					StringBuilder s = code.getsb(y);
					if (ipos > s.length())
					{
						StringBuilder spaces = new StringBuilder(ipos - s.length());
						for (int i = 0; i < ipos - s.length(); i++)
							spaces.append(' ');
						s.append(spaces);
					}
					s.insert(ipos,c);
				}
				col = ++caret.col;
				caret.colw = joshText.line_wid_at(caret.row,caret.col);
				break;
		}
	}

	/**
	 * Insert text into the current selection, replacing the current selected text if the selection is not empty.
	 * @param str The text to insert.
	 */
	public void insert(String str)
	{
		String[] lines = str.split("(\r?\n|\r)",-1);
		if (lines.length > 0) switch (type)
		{
			case NORM:
				deleteSel();
				StringBuilder l1 = code.getsb(caret.row);
				String resub = l1.substring(col);
				l1.replace(col,l1.length(),lines[0]);
				caret.col += lines[0].length();
				for (int y = 1; y < lines.length; y++)
				{
					code.add(++caret.row,lines[y]);
					caret.col = lines[y].length();
				}
				code.getsb(caret.row).append(resub);
				col = caret.col;
				row = caret.row;
				caret.colw = joshText.line_wid_at(caret.row,caret.col);
				break;
			case RECT:
				deleteSel();
				final int sr = Math.min(caret.row,row);
				for (int i = 0; i <= Math.abs(caret.row - row); i++)
				{
					final int ipos = joshText.column_to_index_unsafe(sr + i,caret.col);
					StringBuilder s = code.getsb(sr + i);
					if (ipos > s.length())
					{
						StringBuilder spaces = new StringBuilder(ipos - s.length());
						for (int si = 0; si < ipos - s.length(); si++)
							spaces.append(' ');
						s.append(spaces);
					}
					code.getsb(sr + i).insert(ipos,lines[i % lines.length]);
				}
				break;
		}
	}

	/**
	 * @param str The text to insert.
	 * @return The number of lines inserted.
	 */
	public int insertRect(String str)
	{
		System.out.println("Half-ass Rectpaste");
		String[] lines = str.split("(\r?\n|\r)",-1);
		if (lines.length > 0) switch (type)
		{
			case NORM:
				deleteSel();
				int dcol = joshText.index_to_column(caret.row,caret.col);
				while (caret.row + lines.length > code.size())
					code.add(new Line(new StringBuilder("")));
				for (int i = 0; i < lines.length; i++)
				{
					int ipos = joshText.column_to_index_unsafe(caret.row + i,dcol);
					StringBuilder sb = code.getsb(caret.row + i);
					while (sb.length() < ipos)
						sb.append(' ');
					sb.insert(ipos,lines[i]);
				}
				return lines.length;
			case RECT:
				if (caret.col == col)
				{
					int i;
					final int sr = Math.min(row,caret.row), ld = Math.abs(caret.row - row);
					for (i = 0; i < lines.length && i < ld + 1; i++)
						code.getsb(sr + i).insert(joshText.column_to_index(sr + i,col),lines[i]);
					return i;
				}
				System.out.println("Rectpaste");
				final int maxw = Math.abs(caret.col - col),
				ld = Math.abs(caret.row - row);
				final int sr = Math.min(caret.row,row);
				deleteSel();
				int i;
				for (i = 0; i < lines.length && i < ld + 1; i++)
				{
					String itxt = lines[i];
					int ipos = joshText.column_to_index_unsafe(sr + i,col);
					StringBuilder sb = code.getsb(sr + i);
					while (ipos > sb.length())
						sb.append(' ');
					if (itxt.length() > maxw)
						itxt = itxt.substring(0,maxw);
					else if (ipos < sb.length()) while (itxt.length() < maxw)
						itxt += ' ';
					sb.insert(ipos,itxt);
				}
				return i;
		}
		return 0;
	}

	/**
	 * An abstract class to sort two line-colum points in a particular fashion.
	 * @author IsmAvatar
	 */
	public static abstract class SortedRegion
	{
		/** The point which appears first in the code. */
		protected Point minPt;
		/** The point which appears last in the code. */
		protected Point maxPt;

		/**
		 * @param p1 The first line-column point to sort.
		 * @param p2 The second line-column point to sort.
		 */
		public SortedRegion(Point p1, Point p2)
		{
			setSorted(p1,p2);
		}

		/**
		 * @param p1 The first line-column point to sort.
		 * @param p2 The second line-column point to sort.
		 */
		protected abstract void setSorted(Point p1, Point p2);

		/** @return The point that appears first in the code. */
		public Point getMinPoint()
		{
			return minPt;
		}

		/** @return The point that appears last in the code. */
		public Point getMaxPoint()
		{
			return maxPt;
		}

		/** @return The lowest x-coordinate between the two points. */
		public int getMinX()
		{
			return minPt.x;
		}

		/** @return The lowest y-coordinate between the two points. */
		public int getMinY()
		{
			return minPt.y;
		}

		/** @return The highest x-coordinate between the two points. */
		public int getMaxX()
		{
			return maxPt.x;
		}

		/** @return The highest y-coordinate between the two points. */
		public int getMaxY()
		{
			return maxPt.y;
		}
	}

	/**
	 * Class to sort by row, then by column - The order in which we read.
	 * @author IsmAvatar
	 */
	public static class RowFirstRegion extends SortedRegion
	{
		/**
		 * @param p1 The first point to sort.
		 * @param p2 The second point to sort.
		 */
		public RowFirstRegion(Point p1, Point p2)
		{
			super(p1,p2);
		}

		/**
		 * Sort by row, then by column - The order in which we read.
		 * @param p1 The first point to sort.
		 * @param p2 The second point to sort.
		 */
		@Override
		protected void setSorted(Point p1, Point p2)
		{
			minPt = p1;
			maxPt = p2;
			if (p1.y == p2.y)
			{
				if (p2.x < p1.x)
				{
					minPt = p2;
					maxPt = p1;
				}
			}
			else if (p2.y < p1.y)
			{
				minPt = p2;
				maxPt = p1;
			}
		}
	}

	/**
	 * Class to sort two points by rectangular coordinates.
	 * @author IsmAvatar
	 */
	public static class RectangleRegion extends SortedRegion
	{
		/**
		 * @param p1 The first point to sort.
		 * @param p2 The second point to sort.
		 */
		public RectangleRegion(Point p1, Point p2)
		{
			super(p1,p2);
		}

		/**
		 * Sort the points by rectangular coordinates.
		 * @param p1 The first point to sort.
		 * @param p2 The second point to sort.
		 */
		@Override
		protected void setSorted(Point p1, Point p2)
		{
			minPt = p1;
			maxPt = p2;
			if (p2.y < p1.y)
			{
				minPt = p2;
				maxPt = p1;
			}
			if (maxPt.x < minPt.x)
			{
				int minx = maxPt.x;
				maxPt.x = minPt.x;
				minPt.x = minx;
			}
		}
	}

	/**
	 * Returns the selection region, such that you may conveniently fetch:
	 * <li>the selection end closest to the start of the document (getMin) and
	 * <li>the selection end closest to the end of the document (getMax)
	 * @return The selected region.
	 */
	public SortedRegion getSortedRegion()
	{
		Point p1 = new Point(caret.col,caret.row), p2 = new Point(col,row);
		switch (type)
		{
			case RECT:
				return new RectangleRegion(p1,p2);
			case NORM:
				return new RowFirstRegion(p1,p2);
		}
		return null;
	}

	/**
	 * @param y The row to check for being within the selection.
	 * @param x The column to check for being within the selection.
	 * @return Whether the given column-row point is within the selection. 
	 */
	public boolean contains(int y, int x)
	{
		if (isEmpty()) return false;

		SortedRegion r = getSortedRegion();
		switch (type)
		{
			case RECT:
				return !(y < r.getMinY() || y > r.getMaxY() || x < r.getMinX() || x > r.getMaxX());
			case NORM:
				if (y < r.getMinY() || y > r.getMaxY()) return false;
				if (y == r.getMinY() && x < r.getMinX()) return false;
				if (y == r.getMaxY() && x > r.getMaxX()) return false;
				return true;
		}
		return false;
	}

	/**
	 * @author Josh Ventura
	 *
	 */
	public static class SimpleHighlighter implements Highlighter
	{
		/** The default highlight color. */
		protected static final Color DEF_COL = new Color(200,200,220);
		/** The row on which highlighting will occur */
		protected int y;
		/** The first column to be highlighted. */
		protected int x1;
		/** The last column to be highlighted. */
		protected int x2;

		/**
		 * @param y The row on which to highlight.
		 * @param x1 The first column to highlight.
		 * @param x2 The last column to highlight.
		 */
		public SimpleHighlighter(int y, int x1, int x2)
		{
			this.y = y;
			this.x1 = x1;
			this.x2 = x2;
		}

		/** Draw the highlight mark. */
	//r@Override
		public void paint(Graphics g, Insets i, CodeMetrics cm, int line_start, int line_end)
		{
			g.setColor(DEF_COL);
			int gh = cm.lineHeight();
			int xx = cm.lineWidth(y,x1);
			g.fillRect(i.left + xx,i.top + y * gh,cm.lineWidth(y,x2) - xx,gh);
		}
	}

	/** Class to highlight a sorted region. */
	public static class SortedRegionHighlighter implements Highlighter
	{
		/** The region to highlight. */
		SortedRegion r;
		/** The selection type to highlight. */
		ST type;
		/** The default color in which to highlight. */
		static final Color DEF_COL = new Color(200,200,220);

		/**
		 * @param r The SortedRegion to highlight.
		 * @param type The type of the selection given by the SortedRegion.
		 */
		public SortedRegionHighlighter(SortedRegion r, ST type)
		{
			this.r = r;
			this.type = type;
		}

		/** Paint the highlighter mark.
		 * @see org.lateralgm.joshedit.JoshText.Highlighter#paint(java.awt.Graphics, java.awt.Insets, org.lateralgm.joshedit.JoshText.CodeMetrics, int, int)
		 */
	//r@Override
		public void paint(Graphics g, Insets i, CodeMetrics cm, int line_start, int line_end)
		{
			//g.setXORMode(Color.WHITE);

			Color rc = g.getColor();
			g.setColor(DEF_COL);

			int gw = cm.glyphWidth();
			int gh = cm.lineHeight();

			// This section is fine without tab consideration because selected rectangles
			// are assumed to be completely column-based.
			if (type == ST.RECT)
				g.fillRect(i.left + r.getMinX() * gw,i.top + r.getMinY() * gh,(r.getMaxX() - r.getMinX())
						* gw,(r.getMaxY() - r.getMinY() + 1) * gh);
			else if (r.getMaxY() == r.getMinY())
			{
				new SimpleHighlighter(r.getMinY(),r.getMinX(),r.getMaxX()).paint(g,i,cm,line_start,line_end);
				/*				int xx = line_wid_at(r.getMinY(),r.getMinX());
								g.fillRect(i.left + xx,i.top + r.getMinY() * gh,line_wid_at(r.getMaxY(),r.getMaxX()) - xx,
										gh);*/
			}
			else if (type == ST.NORM)
			{
				Rectangle clip = g.getClipBounds();

				// First line
				g.fillRect(i.left + cm.lineWidth(r.getMinY(),r.getMinX()),i.top + r.getMinY() * gh,
						clip.width - cm.lineWidth(r.getMinY(),r.getMinX()) - i.left + clip.x,gh);
				// Middle lines
				g.fillRect(i.left + clip.x,i.top + (r.getMinY() + 1) * gh,clip.width,
						(r.getMaxY() - r.getMinY() - 1) * gh);
				// Last line
				g.fillRect(i.left,i.top + r.getMaxY() * gh,cm.lineWidth(r.getMaxY(),r.getMaxX()),gh);
			}

			g.setColor(rc);
			//g.setPaintMode();
		}
	}

	/** Paint the selection highlighting.
	 * @see org.lateralgm.joshedit.JoshText.Highlighter#paint(java.awt.Graphics, java.awt.Insets, org.lateralgm.joshedit.JoshText.CodeMetrics, int, int)
	 */
//r@Override
	public void paint(Graphics g, Insets i, CodeMetrics cm, int line_start, int line_end)
	{
		if (isEmpty()) return;
		new SortedRegionHighlighter(getSortedRegion(),type).paint(g,i,cm,line_start,line_end);
	}

	/**
	 * Change the type of the current selection without affecting the start and end position on screen (eg, due to tabs).
	 * @param t The selection type to change to.
	 */
	public void changeType(ST t)
	{
		if (type == t) return;
		type = t;
		if (t == ST.NORM)
		{
			col = joshText.column_to_index(row,col);
			caret.col = joshText.column_to_index(caret.row,caret.col);
			special.toNorm();
		}
		else
		{
			col = joshText.index_to_column(row,col);
			caret.col = joshText.index_to_column(caret.row,caret.col);
			special.toRect();
		}
	}

	/**
	 * @return The text encompassed by this selection.
	 */
	public String getSelectedText()
	{
		SortedRegion r = getSortedRegion();
		int maxY = Math.min(r.getMaxY(),joshText.code.size());
		if (r.getMinY() == maxY)
		{
			StringBuilder line = joshText.code.getsb(r.getMinY());
			if (r.getMinX() >= line.length()) return "";
			return line.substring(r.getMinX(),Math.min(r.getMaxX(),line.length()));
		}

		switch (type)
		{
			case RECT:
				StringBuilder sbr = new StringBuilder();
				for (int y = r.getMinY(); y <= maxY; y++)
				{
					StringBuilder liner = joshText.code.getsb(y);
					final int bpos = joshText.column_to_index(y,r.getMinX());
					if (bpos < liner.length())
						sbr.append(liner.substring(bpos,
								Math.min(joshText.column_to_index(y,r.getMaxX()),liner.length())));
					if (y != maxY) sbr.append('\n');
				}
				return sbr.toString();
			case NORM:
				StringBuilder sbn = new StringBuilder(joshText.code.getsb(r.getMinY()).substring(
						r.getMinX()));
				for (int y = r.getMinY() + 1; y < maxY; y++)
					sbn.append('\n').append(joshText.code.getsb(y));
				StringBuilder linen = joshText.code.getsb(maxY);
				sbn.append('\n').append(linen.substring(0,Math.min(r.getMaxX(),linen.length())));
				return sbn.toString();
		}
		return null;
	}

	/**
	 * @return The selected text as it should be placed on the clipboard.
	 */
	public String getSelectedTextForCopy()
	{
		String ret = getSelectedText();
		if (type == ST.RECT) ret += '\0';
		return ret;
	}

	/**
	 * Class to handle special mouse selections, such as whole-word and full-line selection.
	 * @author Josh Ventura
	 */
	interface SpecialSelectionHandler
	{
		/**
		 * Called when the cursor position changes while selecting to allow adjustment of final selection position.
		 * @param ss The selection metrics to adjust.
		 */
		void adjustSelection(SpecialSel ss);
	}

	/**
	 * A class designed to wrap the selection around whole words.
	 * @author Josh Ventura
	 */
	class WordSelHandler implements SpecialSelectionHandler
	{
		/** Adjust the selection to encompass the entire word at the caret.
		 * @see org.lateralgm.joshedit.Selection.SpecialSelectionHandler#adjustSelection(org.lateralgm.joshedit.Selection.SpecialSel)
		 */
//r@Override
		public void adjustSelection(SpecialSel ss)
		{
			if (type == ST.RECT ? caret.col >= joshText.index_to_column(ss.irow,ss.icol)
					: caret.row > ss.irow || (caret.row == ss.irow && caret.col >= ss.icol))
			{
				// Handle first selected word (us)
				col = ss.icol;
				if (col > 0) col--;
				StringBuilder sb = code.getsb(row);
				int st = JoshText.selGetKind(sb,col);
				while (col >= 0 && JoshText.selOfKind(sb,col,st))
					col--;
				col++;

				// Handle cursor
				sb = code.getsb(caret.row);
				if (type == ST.RECT) caret.col = joshText.column_to_index(caret.row,caret.col);
				st = JoshText.selGetKind(sb,caret.col);
				int ep = caret.col, sp = caret.col;
				while (sp >= 0 && JoshText.selOfKind(sb,sp,st))
					sp--;
				sp++;
				while (ep < sb.length() && JoshText.selOfKind(sb,ep,st))
					ep++;
				if (type != ST.RECT)
					caret.col = caret.col - sp > 0 || (caret.col == ss.icol && caret.row == ss.irow) ? ep
							: sp;
				else
					caret.col = caret.col - sp > 0 || (sp == joshText.column_to_index(ss.irow,ss.icol)) ? ep
							: sp;
			}
			else
			{
				// Handle cursor
				StringBuilder sb = code.getsb(caret.row);
				if (type == ST.RECT) caret.col = joshText.column_to_index(caret.row,caret.col);
				int st = JoshText.selGetKind(sb,caret.col);
				while (caret.col >= 0 && JoshText.selOfKind(sb,caret.col,st))
					caret.col--;
				caret.col++;

				// Handle first selected word (us)
				col = ss.icol;
				sb = code.getsb(row);
				st = JoshText.selGetKind(sb,col);
				while (col < sb.length() && JoshText.selOfKind(sb,col,st))
					col++;
			}
			if (type == ST.RECT)
			{
				caret.col = joshText.index_to_column(caret.row,caret.col);
				col = joshText.index_to_column(row,col);
			}
		}
	}

	/** Our {@link SpecialSelectionHandler} that wraps the selection around whole words. */
	WordSelHandler wordSelHandler = new WordSelHandler();

	/** A class designed to wrap the selection to entire lines.
	 * @author Josh Ventura */
	class LineSelHandler implements SpecialSelectionHandler
	{
		/** Upon selection change, wrap the selection to contain the entirety of the start and end line(s). */
//r@Override
		public void adjustSelection(SpecialSel ss)
		{
			if (row > caret.row)
			{
				caret.col = 0;
				col = code.getsb(row).length();
			}
			else if (row < caret.row)
			{
				col = 0;
				caret.col = code.getsb(caret.row).length();
			}
			else if (col < caret.col)
			{
				col = 0;
				caret.col = code.getsb(caret.row).length();
			}
			else
			{
				caret.col = 0;
				col = code.getsb(row).length();
			}

			if (type == ST.RECT)
			{
				if (caret.col == 0)
					for (int i = caret.row; i <= row; i++)
						col = Math.max(col,joshText.index_to_column(i,code.getsb(i).length()));
				else
					for (int i = row; i <= caret.row; i++)
						caret.col = Math.max(caret.col,joshText.index_to_column(i,code.getsb(i).length()));
			}
		}
	}

	/** Our {@link SpecialSelectionHandler} that wraps the selection around entire lines. */
	LineSelHandler lineSelHandler = new LineSelHandler();

	/**
	 * @author Josh Ventura
	 * A class to communicate adjustable coordinates.
	 */
	class SpecialSel
	{
		/** Whether the selection coordinates depicted by this class have been set. */
		boolean valid = false;
		/** The row on which this special selection is . */
		public int ssrow;
		/** The starting column. */
		int spos = 0;
		/** The ending column. */
		int epos = 0;
		/** The handler for this special selection. */
		SpecialSelectionHandler ssh;
		/** Initial column. */
		int irow;
		/** Initial row. */
		int icol;

		/** Change to standard coordinates. */
		public void toNorm()
		{
			if (valid)
			{
				spos = joshText.column_to_index(ssrow,spos);
				epos = joshText.column_to_index(ssrow,epos);
			}
		}

		/** Change to rectangular coordinates. */
		public void toRect()
		{
			if (valid)
			{
				spos = joshText.index_to_column(ssrow,spos);
				epos = joshText.index_to_column(ssrow,epos);
			}
		}

		/**
		 * Change our special selection handler.
		 * @param specSelHandler The {@link SpecialSelectionHandler} to use.
		 */
		public void setHandler(SpecialSelectionHandler specSelHandler)
		{
			valid = true;
			ssh = specSelHandler;
			icol = caret.col;
			irow = caret.row;
		}

		/** Invoke the adjustor for our current special selection handler. */
		public void adjust()
		{
			ssh.adjustSelection(this);
		}
	}

	/** The SpecialSel class we will use to allow special selection types. */
	SpecialSel special = new SpecialSel();

	/** Move the selection to encompass the word in which the caret has rested. */
	public void selectCaretWord()
	{
		final StringBuilder sb = joshText.code.getsb(caret.row);
		special.spos = caret.getPositionRepresentation(this);
		special.epos = caret.getPositionRepresentation(this);
		int skind = JoshText.selGetKind(sb,special.spos);
		while (special.spos > 0 && JoshText.selOfKind(sb,special.spos - 1,skind))
			special.spos--;
		while (++special.epos < sb.length() && JoshText.selOfKind(sb,special.epos,skind))
		{ /* Move to end of selection kind */
		}
		row = caret.row;
		col = special.spos;
		caret.col = special.epos;
		special.ssrow = caret.row;
		special.valid = true;
		if (type == ST.RECT) special.toRect();
	}
}
