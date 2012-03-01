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
import org.lateralgm.joshedit.JoshText.Marker;

public class Selection implements Marker
{
	enum ST
	{
		NORM,RECT
	}

	ST type = ST.NORM;

	Code code;
	JoshText joshText;
	Caret caret;

	/**
	 * A trailing selection position indicator.
	 * Call deselect() to update this to the caret position.
	 * If this is the same as the caret position, there is no selection.
	 */
	int col, row;

	public Selection(Code txt, JoshText joshTxt, Caret pt)
	{
		code = txt;
		joshText = joshTxt;
		caret = pt;
	}

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
	 */
	public void deselect(boolean reset)
	{
		row = caret.row;
		col = caret.col;
		if (reset) changeType(ST.NORM);
	}

	public void copy()
	{
		if (isEmpty()) return;
		StringSelection stringSelection = new StringSelection(getSelectedTextForCopy());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection,joshText);
	}

	/// Returns the number of lines affected by a potential paste in this selection.
	public int getInsertRipple(String str)
	{
		if (type == ST.RECT) return Math.abs(caret.row - row) + 1;
		if (str.length() > 0 && str.charAt(str.length() - 1) == 0)
			return Math.max(str.split("(\r?\n|\r)",-1).length,0);
		return 1;
	}

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

	static Clipboard fallbackMCClipboard;

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

	public void selectionChanged()
	{
		if (isEmpty()) return;
		Clipboard a = Toolkit.getDefaultToolkit().getSystemSelection();
		if (a == null) a = fallbackMCClipboard;
		if (a == null) a = fallbackMCClipboard = new Clipboard("Improvised Middle-Click Clipboard");
		StringSelection stringSelection = new StringSelection(getSelectedTextForCopy());
		a.setContents(stringSelection,joshText);
	}

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

	/** Deletes the selection if there is one, returns false on no selection */
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

	private void resetcoords(Selection ssel)
	{
		col = ssel.col;
		row = ssel.row;
	}

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

	public static abstract class SortedRegion
	{
		protected Point minPt, maxPt;

		public SortedRegion(Point p1, Point p2)
		{
			setSorted(p1,p2);
		}

		protected abstract void setSorted(Point p1, Point p2);

		public Point getMinPoint()
		{
			return minPt;
		}

		public Point getMaxPoint()
		{
			return maxPt;
		}

		public int getMinX()
		{
			return minPt.x;
		}

		public int getMinY()
		{
			return minPt.y;
		}

		public int getMaxX()
		{
			return maxPt.x;
		}

		public int getMaxY()
		{
			return maxPt.y;
		}
	}

	public static class RowFirstRegion extends SortedRegion
	{
		public RowFirstRegion(Point p1, Point p2)
		{
			super(p1,p2);
		}

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

	public static class RectangleRegion extends SortedRegion
	{
		public RectangleRegion(Point p1, Point p2)
		{
			super(p1,p2);
		}

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

	public static class SimpleMarker implements Marker
	{
		protected static final Color DEF_COL = new Color(200,200,220);
		protected int y, x1, x2;

		public SimpleMarker(int y, int x1, int x2)
		{
			this.y = y;
			this.x1 = x1;
			this.x2 = x2;
		}

		public void paint(Graphics g, Insets i, CodeMetrics cm, int line_start, int line_end)
		{
			g.setColor(DEF_COL);
			int gh = cm.lineHeight();
			int xx = cm.lineWidth(y,x1);
			g.fillRect(i.left + xx,i.top + y * gh,cm.lineWidth(y,x2) - xx,gh);
		}
	}

	public static class SortedRegionMarker implements Marker
	{
		SortedRegion r;
		ST type;
		static final Color DEF_COL = new Color(200,200,220);

		public SortedRegionMarker(SortedRegion r, ST type)
		{
			this.r = r;
			this.type = type;
		}

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
				new SimpleMarker(r.getMinY(),r.getMinX(),r.getMaxX()).paint(g,i,cm,line_start,line_end);
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

	public void paint(Graphics g, Insets i, CodeMetrics cm, int line_start, int line_end)
	{
		if (isEmpty()) return;
		new SortedRegionMarker(getSortedRegion(),type).paint(g,i,cm,line_start,line_end);
	}

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

	public String getSelectedTextForCopy()
	{
		String ret = getSelectedText();
		if (type == ST.RECT) ret += '\0';
		return ret;
	}

	/**
	 * Handle special mouse selections.
	 */

	interface SpecialSelectionHandler
	{
		void adjustSelection(SpecialSel ss);
	}

	class WordSelHandler implements SpecialSelectionHandler
	{
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

	WordSelHandler wordSelHandler = new WordSelHandler();

	class LineSelHandler implements SpecialSelectionHandler
	{
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

	LineSelHandler lineSelHandler = new LineSelHandler();

	class SpecialSel
	{
		boolean valid = false;
		int spos = 0, epos = 0;
		public int row;
		SpecialSelectionHandler ssh;
		int irow, icol;

		/**< Initial column and row **/

		public void toNorm()
		{
			if (valid)
			{
				spos = joshText.column_to_index(row,spos);
				epos = joshText.column_to_index(row,epos);
			}
		}

		public void toRect()
		{
			if (valid)
			{
				spos = joshText.index_to_column(row,spos);
				epos = joshText.index_to_column(row,epos);
			}
		}

		public void setHandler(SpecialSelectionHandler specSelHandler)
		{
			valid = true;
			ssh = specSelHandler;
			icol = caret.col;
			irow = caret.row;
		}

		public void adjust()
		{
			ssh.adjustSelection(this);
		}
	}

	SpecialSel special = new SpecialSel();

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
		special.row = caret.row;
		special.valid = true;
		if (type == ST.RECT) special.toRect();
	}
}
