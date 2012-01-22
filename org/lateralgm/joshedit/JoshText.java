/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.lateralgm.joshedit.FindDialog.FindNavigator;
import org.lateralgm.joshedit.Highlighter.HighlighterInfoEx;
import org.lateralgm.joshedit.Selection.ST;

import sun.awt.dnd.SunDragSourceContextPeer;

public class JoshText extends JComponent implements Scrollable,ComponentListener,ClipboardOwner,
		FocusListener
{
	private static final long serialVersionUID = 1L;

	// Settings
	public static class Settings
	{
		public static boolean indentUseTabs = false; // True if the tab character is used, false to use spaces
		public static int indentSizeInSpaces = 4; // The size with which tab characters are represented, in spaces (characters)
		public static String indentRepString = "    "; // The string which will be inserted into the code for indentation.
		public static boolean smartBackspace = true; // True if backspace should clear indentation to tab marks
		public static boolean highlight_line = true; // True if the caret's line is to be highlighted
	}

	// Components
	Code code;
	Selection sel;
	Caret caret;
	DragListener dragger;
	public Highlighter highlighter = new GMLHighlighter(this);
	public ArrayList<Marker> markers = new ArrayList<Marker>();

	// Dimensions
	private int monoAdvance, lineHeight, lineAscent, lineLeading;

	// Our longest row, and how many other rows are this long
	private int maxRowSize; // This is the size of the longest row, not the index.

	// Status bar messages
	Queue<String> infoMessages = new LinkedList<String>();

	// Find/Replace
	public FindNavigator finder;

	/**
	 * These get painted before the text
	 */
	public static interface Marker
	{
		void paint(Graphics g, Insets i, CodeMetrics gm, int line_start, int line_end);
	}

	public static interface CodeMetrics
	{
		int stringWidth(String str, int end);

		int lineWidth(int y, int end);

		int glyphWidth();

		int lineHeight();
	}

	CodeMetrics metrics = new CodeMetrics()
	{
		public int stringWidth(String l, int end)
		{
			end = Math.min(end,l.length());
			int w = 0;
			for (int i = 0; i < end; i++)
				if (l.charAt(i) == '\t')
				{
					final int wf = monoAdvance * Settings.indentSizeInSpaces;
					w = ((w + wf) / wf) * wf;
				}
				else
					w += monoAdvance;
			return w;
		}

		public int lineWidth(int y, int end)
		{
			return stringWidth(code.getsb(y).toString(),end);
		}

		public int glyphWidth()
		{
			return monoAdvance;
		}

		public int lineHeight()
		{
			return lineHeight;
		}
	};

	/**
	 * Character "type", such as letter (1), whitespace (2), symbol (0), etc.
	 * Used for word selection and backspacing.
	 */
	public static final class ChType
	{
		public static final int NONE = 0;
		public static final int WORD = 1;
		public static final int WHITE = 2;
	}

	private static final char chType[] = new char[256];
	static
	{
		for (int i = 0; i < 256; i++)
			chType[i] = ChType.NONE;

		for (int i = 'a'; i < 'z'; i++)
			chType[i] = ChType.WORD;
		for (int i = 'A'; i < 'Z'; i++)
			chType[i] = ChType.WORD;
		for (int i = '0'; i < '9'; i++)
			chType[i] = ChType.WORD;
		chType['_'] = ChType.WORD;

		chType[' '] = ChType.WHITE;
		chType['\t'] = ChType.WHITE;
		chType['\r'] = ChType.WHITE;
		chType['\n'] = ChType.WHITE;
	}

	SyntaxDesc myLang;

	public JoshText()
	{
		this(null);
	}

	public JoshText(String[] lines)
	{
		// Drawing stuff
		setPreferredSize(new Dimension(320,240));
		setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
		setBackground(Color.WHITE);
		setForeground(Color.BLACK);
		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		setOpaque(true);
		getInsets().left = 4;

		setFocusable(true);
		focusGained(null);
		setFocusTraversalKeysEnabled(false);
		setTransferHandler(new JoshTextTransferHandler());

		mapActions();

		// The mapping of keystrokes and action names
		Bindings.readMappings(getInputMap());

		// Events
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
				| AWTEvent.KEY_EVENT_MASK);

		// Managing our code
		code = new Code();
		if (lines == null || lines.length == 0)
			code.add(new StringBuilder());
		else
			for (String line : lines)
				code.add(line);

		FontMetrics fm = getFontMetrics(getFont());
		lineAscent = fm.getAscent();
		lineHeight = fm.getHeight();
		lineLeading = fm.getLeading();
		monoAdvance = fm.getWidths()['M'];

		caret = new Caret(this);
		sel = new Selection(code,this,caret);
		dragger = new DragListener();

		myLang = new SyntaxDesc();
		myLang.set_language("Shitbag");

		if (Settings.highlight_line) markers.add(new Marker()
		{
			public void paint(Graphics g, Insets i, CodeMetrics gm, int line_start, int line_end)
			{
				if (sel.row == caret.row)
				{
					Color rc = g.getColor();
					g.setColor(new Color(230,240,255));
					Rectangle clip = g.getClipBounds();
					g.fillRect(i.left + clip.x,i.top + caret.row * gm.lineHeight(),clip.width,gm.lineHeight());
					g.setColor(rc);
				}
			}
		});
		markers.add(sel);

		BracketMarker bm = new BracketMarker();
		markers.add(bm);
		caret.addCaretListener(bm);
		caret.addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				doShowCaret();
			}
		});

		addLineChangeListener((LineChangeListener) highlighter);
		fireLineChange(0,code.size());

		doCodeSize(true);
	}

	public void setText(String[] lines)
	{
		code.clear();
		if (lines == null || lines.length == 0)
			code.add(new StringBuilder());
		else
			for (String line : lines)
				code.add(line);
		fireLineChange(0,code.size());
	}

	/** Maps action names to their implementations */
	public static abstract class CustomAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public CustomAction(String name)
		{
			super(name);
		}
	}

	public AbstractAction aLineDel = new CustomAction("LINEDEL")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			//delete the line where the caret is
		}
	};
	public AbstractAction aLineDup = new CustomAction("LINEDUP")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			UndoPatch up = new UndoPatch();
			up.realize(up.startRow + sel.duplicate());
			storeUndo(up,OPT.DUPLICATE);
		}
	};
	public AbstractAction aLineSwap = new CustomAction("LINESWAP")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			if (caret.row == sel.row) {
				if (caret.row == 0) return;
				UndoPatch up = new UndoPatch(caret.row - 1, caret.row);
				StringBuilder swb = code.getsb(caret.row - 1);
				code.get(caret.row - 1).sbuild = code.get(caret.row).sbuild;
				code.get(caret.row).sbuild = swb;
				up.realize(caret.row);
				storeUndo(up,OPT.SWAP);
				if (sel.type != ST.RECT)
					caret.col = line_offset_from(caret.row,caret.colw);
			}
			else {
				UndoPatch up = new UndoPatch();
				int srow = Math.min(sel.row,caret.row), erow = Math.max(sel.row,caret.row);
				StringBuilder swb = code.getsb(srow);
				for (int i = srow; i < erow; i++)
				  code.get(i).sbuild = code.get(i+1).sbuild;
				code.get(erow).sbuild = swb;
				up.realize(erow);
				storeUndo(up,OPT.SWAP);
				if (sel.type != ST.RECT)
					caret.col = line_offset_from(caret.row,caret.colw);
			}
		}
	};
	public AbstractAction aLineUnSwap = new CustomAction("LINEUNSWAP")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
				if (caret.row == sel.row) {
					if (caret.row >= code.size() - 1) return;
					UndoPatch up = new UndoPatch(caret.row + 1, caret.row);
					StringBuilder swb = code.getsb(caret.row + 1);
					code.get(caret.row + 1).sbuild = code.get(caret.row).sbuild;
					code.get(caret.row).sbuild = swb;
					up.realize(caret.row);
					storeUndo(up,OPT.UNSWAP);
					if (sel.type != ST.RECT)
						caret.col = line_offset_from(caret.row,caret.colw);
				}
				else {
					UndoPatch up = new UndoPatch();
					int srow = Math.min(sel.row,caret.row), erow = Math.max(sel.row,caret.row);
					StringBuilder swb = code.getsb(erow);
					for (int i = erow; i > srow; i--)
					  code.get(i).sbuild = code.get(i-1).sbuild;
					code.get(srow).sbuild = swb;
					up.realize(erow);
					storeUndo(up,OPT.UNSWAP);
					if (sel.type != ST.RECT)
						caret.col = line_offset_from(caret.row,caret.colw);
				}
			}
	};
	public AbstractAction aSelAll = new CustomAction("SELALL")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			sel.row = sel.col = 0;
			caret.row = code.size() - 1;
			caret.col = code.getsb(caret.row).length();
			sel.type = ST.NORM;
			sel.selectionChanged();
		}
	};
	public AbstractAction aCopy = new CustomAction("COPY")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			sel.copy();
		}
	};
	public AbstractAction aCut = new CustomAction("CUT")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			if (sel.isEmpty()) return;
			sel.copy();
			UndoPatch up = new UndoPatch();
			sel.deleteSel();
			up.realize(Math.max(caret.row,sel.row));
			storeUndo(up,OPT.DELETE);
			repaint();
		}
	};
	public AbstractAction aPaste = new CustomAction("PASTE")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			UndoPatch up = new UndoPatch(Math.min(caret.row,sel.row),Math.min(code.size() - 1,
					Math.max(caret.row,sel.row) + sel.getPasteRipple()));
			up.realize(sel.paste());
			storeUndo(up,OPT.PASTE);
		}
	};
	public AbstractAction aUndo = new CustomAction("UNDO")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			undo();
		}
	};
	public AbstractAction aRedo = new CustomAction("REDO")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			redo();
		}
	};
	public AbstractAction aFind = new CustomAction("FIND")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			findDialog.setVisible(true);
		}
	};
	public AbstractAction aQuickFind = new CustomAction("QUICKFIND")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			finder.present();
		}
	};
	public AbstractAction aUnindent = new CustomAction("UNINDENT")
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			UndoPatch up = new UndoPatch();
			int erow;
			for (int row = erow = Math.min(sel.row,caret.row); row <= sel.row || row <= caret.row; row++)
			{
				unindent(row);
				erow = row;
			}
			up.realize(erow);
			storeUndo(up,OPT.INDENT);
		}
	};

	private void mapActions()
	{
		ActionMap am = getActionMap();
		Action acts[] = { aLineDel,aLineDup,aLineSwap,aLineUnSwap,aSelAll,aCopy,aCut,aPaste,aUndo,aRedo,aFind,aQuickFind,
				aUnindent };
		for (Action a : acts)
			am.put(a.getValue(Action.NAME),a);
	}

	FindDialog findDialog = FindDialog.getInstance();

	void unindent(int row)
	{
		StringBuilder sb = code.get(row).sbuild;
		int wc = 0, cc = 0;
		while (cc < sb.length() && Character.isWhitespace(sb.charAt(cc)))
		{
			if (sb.charAt(cc) == '\t')
			{
				wc = ((wc + Settings.indentSizeInSpaces) / Settings.indentSizeInSpaces)
						* Settings.indentSizeInSpaces;
			}
			else
				wc++;
			cc++;
		}
		if (wc > 0)
		{
			int kspaces = ((wc - 1) / Settings.indentSizeInSpaces) * Settings.indentSizeInSpaces;
			for (int atspaces = 0, lastspaces, i = 0; i < cc; i++)
			{
				lastspaces = atspaces;
				if (sb.charAt(i) == '\t')
					atspaces = ((atspaces + Settings.indentSizeInSpaces) / Settings.indentSizeInSpaces)
							* Settings.indentSizeInSpaces;
				else
					atspaces++;
				if (atspaces == kspaces)
				{
					if (sel.type != ST.RECT)
					{
						if (sel.row == row) sel.col -= cc - i - 1;
						if (caret.row == row) caret.col -= cc - i - 1;
					}
					for (i++; i < cc; cc--)
						sb.delete(i,i + 1);
					break;
				}
				if (atspaces > kspaces)
				{
					if (sel.type != ST.RECT)
					{
						if (sel.row == row) sel.col -= cc - i;
						if (caret.row == row) caret.col -= cc - i;
					}
					for (; i < cc; cc--)
						sb.delete(i,i + 1);
					for (i = lastspaces; i < kspaces; i++)
					{
						if (sel.row == row) sel.col++;
						if (caret.row == row) caret.col++;
						sb.insert(i," ");
					}
					break;
				}
			}
		}
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		getParent().addComponentListener(this);
	}

	/**
	 * Converts a string to a KeyStroke. The string should be of the
	 * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
	 * is any combination of A for Alt, C for Control, S for Shift,
	 * G for AltGr, or M for Meta, and <i>shortcut</i> is either a
	 * single character or a keycode name from the <code>KeyEvent</code>
	 * class, without the <code>VK_</code> prefix.
	 * @param s A string description of the key stroke.
	 * @return The resulting composed KeyStroke.
	 * @throws IllegalArgumentException if the key cannot be parsed for some reason.
	 */
	public static KeyStroke key(String s) throws IllegalArgumentException
	{
		if (s == null) return null;
		int index = s.indexOf('+');
		String key = s.substring(index + 1);
		if (key.length() == 0) throw new IllegalArgumentException("Invalid key stroke: " + s);

		// Parse modifiers
		int modifiers = 0;
		if (index != -1)
		{
			for (int i = 0; i < index; i++)
			{
				switch (Character.toUpperCase(s.charAt(i)))
				{
					case 'A':
						modifiers |= InputEvent.ALT_DOWN_MASK;
						break;
					case 'C':
						modifiers |= InputEvent.CTRL_DOWN_MASK;
						break;
					case 'G':
						modifiers |= InputEvent.ALT_GRAPH_MASK;
						break;
					case 'M':
						modifiers |= InputEvent.META_DOWN_MASK;
						break;
					case 'S':
						modifiers |= InputEvent.SHIFT_DOWN_MASK;
						break;
				}
			}
		}

		// Handle basic ordinal (single character) key
		if (key.length() == 1)
		{
			char ch = Character.toUpperCase(key.charAt(0));
			if (modifiers == 0) return KeyStroke.getKeyStroke(ch);
			return KeyStroke.getKeyStroke(ch,modifiers);
		}

		// Otherwise use Reflection to parse the key
		int ch;

		try
		{
			ch = KeyEvent.class.getField("VK_".concat(key)).getInt(null);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Invalid key stroke: " + s,e);
		}

		return KeyStroke.getKeyStroke(ch,modifiers);
	}

	void fitToCode()
	{
		int insetY = lineLeading;
		int w = (maxRowSize + 1) * monoAdvance + getInsets().left + 1; //extra char + pixel for overwrite caret
		int h = code.size() * lineHeight + insetY;
		setMinimumSize(new Dimension(w,h));
		setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		fireResize();
		repaint();
	}

	void doCodeSize(boolean rs)
	{
		maxRowSize = 0;
		for (int i = 0; i < code.size(); i++)
			if (code.getsb(i).length() > maxRowSize) maxRowSize = code.getsb(i).length();
		if (rs)
		{
			fitToCode();
		}
	}

	private void doShowCaret()
	{
		if (!(getParent() instanceof JViewport)) return;

		JViewport p = ((JViewport) getParent());
		Rectangle vr = p.getViewRect();

		Rectangle cr = new Rectangle(caret.col * monoAdvance,caret.row * lineHeight,monoAdvance,
				lineHeight);
		Point rp = new Point(vr.x,vr.y);

		if (cr.y + cr.height > vr.y + vr.height)
			rp.y = cr.y + cr.height - vr.height;
		else if (cr.y < vr.y) rp.y = Math.max(0,cr.y);

		if (cr.x + cr.width > vr.x + vr.width)
			rp.x = cr.x + cr.width - vr.width + 5 * monoAdvance;
		else if (cr.x < vr.x) rp.x = Math.max(0,cr.x - 5 * monoAdvance);

		if (rp.x != vr.x || rp.y != vr.y) p.setViewPosition(rp);
	}

	private void updateMouseAutoScroll(Point point)
	{
		// FIXME: This entire thing is some huge pile of fail.
		// It scrolls too slowly and causes viewport issues.
		if (!(getParent() instanceof JViewport)) return;

		JViewport p = ((JViewport) getParent());
		Rectangle vr = p.getViewRect();
		Point rp = new Point(0,0);

		if (point.x > vr.x + vr.width)
			rp.x += Math.pow(3,Math.max(0,Math.log10(point.x - vr.x - vr.width) - 1));
		if (point.x < vr.x) rp.x -= Math.pow(3,Math.max(0,Math.log10(vr.x - point.x) - 1));
		if (point.y > vr.y + vr.height)
			rp.y += Math.pow(5,Math.max(0,Math.log10(point.y - vr.y - vr.height) - 1));
		if (point.y < vr.y) rp.y -= Math.pow(5,Math.max(0,Math.log10(vr.y - point.y) - 1));

		rp.x = Math.max(0,Math.min(maxRowSize,rp.x));
		rp.y = Math.max(0,Math.min(code.size() * lineHeight - vr.height,rp.y));
		mas.p = p;
		mas.rp = rp;
		if (rp.x != 0 || rp.y != 0)
			mas.start();
		else
			mas.stop();
	}

	void removeMouseAutoScroll()
	{
		mas.stop();
	}

	class MouseAutoScroll
	{
		Point rp;
		JViewport p;
		private boolean running = false;

		MouseAutoScroll()
		{
			new Timer().scheduleAtFixedRate(doMouseAutoScroll,100,100);
		}

		void start()
		{
			running = true;
		}

		void stop()
		{
			running = false;
		}

		TimerTask doMouseAutoScroll = new TimerTask()
		{
			@Override
			public void run()
			{
				if (!running) return;
				Point po = p.getViewPosition();
				p.setViewPosition(new Point(po.x + rp.x,po.y + rp.y));
			}
		};
	}

	private MouseAutoScroll mas = new MouseAutoScroll();

	/** Returns the width encompassed by line with index 
	 *  @param l when read @param pos characters in. */
	int line_wid_at(int l, int pos)
	{
		return metrics.stringWidth(code.getsb(l).toString(),pos);
	}

	/** Returns the position in new line @param lTo when transitioned
	 * vertically from line @param lFrom at position @param pos */
	private int line_offset_from(int lTo, int wid)
	{
		int ret;
		String l = code.getsb(lTo).toString();
		int w = 0, lw = 0;
		for (ret = 0; ret < l.length() && w < wid; ret++)
		{
			lw = w;
			if (l.charAt(ret) == '\t')
			{
				final int wf = monoAdvance * Settings.indentSizeInSpaces;
				w = ((w + wf) / wf) * wf;
			}
			else
				w += monoAdvance;
		}
		if (Math.abs(lw - wid) < Math.abs(w - wid)) return Math.min(l.length(),ret - 1);
		return Math.min(l.length(),ret);
	}

	/** Returns the column which begins the character
	 *  in @param line at index @param ind */
	int index_to_column(int line, int ind)
	{
		int col = 0;
		StringBuilder l = code.getsb(line);
		ind = Math.min(ind,l.length());
		for (int i = 0; i < ind; i++)
			if (l.charAt(i) == '\t')
			{
				col += Settings.indentSizeInSpaces;
				col /= Settings.indentSizeInSpaces;
				col *= Settings.indentSizeInSpaces;
			}
			else
				++col;
		return col;
	}

	/** Returns the column which begins the character
	 *  in @param line at index @param ind */
	int column_to_index(int line, int col)
	{
		int ind = 0;
		StringBuilder l = code.getsb(line);
		for (int i = 0; i < col && ind < l.length(); ind++)
			if (l.charAt(ind) == '\t')
			{
				i += Settings.indentSizeInSpaces;
				i /= Settings.indentSizeInSpaces;
				i *= Settings.indentSizeInSpaces;
			}
			else
				++i;
		return ind;
	}

	/** Returns the column which begins the character
	 *  in @param line at index @param ind, ignoring bounds */
	int column_to_index_unsafe(int line, int col)
	{
		int ind = 0;
		StringBuilder l = code.getsb(line);
		int i;
		for (i = 0; i < col && ind < l.length(); ind++)
			if (l.charAt(ind) == '\t')
			{
				i += Settings.indentSizeInSpaces;
				i /= Settings.indentSizeInSpaces;
				i *= Settings.indentSizeInSpaces;
			}
			else
				++i;
		ind += col - i;
		return ind;
	}

	boolean column_in_tab(int line, int col)
	{
		int ind = 0;
		StringBuilder l = code.getsb(line);
		int i;
		for (i = 0; i < col && ind < l.length(); ind++)
		{
			if (l.charAt(ind) == '\t')
			{
				i += Settings.indentSizeInSpaces;
				i /= Settings.indentSizeInSpaces;
				i *= Settings.indentSizeInSpaces;
				if (i > col) return true;
			}
			else
				i++;
		}
		return false;
	}

	public static int selGetKind(CharSequence str, int pos)
	{
		if (!(pos >= 0 && pos < str.length()))
			return ChType.WHITE;
		int ohfukku = str.charAt(pos);
		if (ohfukku > 255)
			return ChType.WORD;
		return chType[ohfukku];
	}

	public static boolean selOfKind(CharSequence str, int pos, int otype)
	{
		if (!(pos >= 0 && pos < str.length()))
			return otype == ChType.WHITE;
		int ohfukku = str.charAt(pos);
		if (ohfukku > 255)
			return otype == ChType.WORD;
		return chType[ohfukku] == otype;
	}

	public FontMetrics getFontMetrics()
	{
		return getFontMetrics(getFont());
	}

	public Dimension getMaxGlyphSize()
	{
		return new Dimension(monoAdvance,lineHeight);
	}

	private int drawChars(Graphics g, char[] a, int sp, int ep, int xx, int ty)
	{
		Color c = g.getColor();
		for (int i = sp; i < ep; i++)
		{
			if (a[i] == '\t')
			{
				final int incby = Settings.indentSizeInSpaces * monoAdvance; //, xxp = xx;
				xx = ((xx + incby) / incby) * incby;
				g.setColor(new Color(255,0,0));
				/*g.drawLine(xxp + 2,ty - (lineHeight / 3),xx - 2,ty - (lineHeight / 3));
				g.drawLine(xxp + 2,ty - (lineHeight / 3) - (lineHeight / 5),xxp + 2,ty - (lineHeight / 3)
						+ (lineHeight / 5));
				g.drawLine(xx - 2,ty - (lineHeight / 3) - (lineHeight / 5),xx - 2,ty - (lineHeight / 3)
						+ (lineHeight / 5));*/
				g.setColor(c);
				continue;
			}
			g.drawChars(a,i,1,xx,getInsets().top + ty);
			xx += monoAdvance;
			g.setColor(c);
		}
		return xx;
	}

	private void drawLine(Graphics g, int lineNum, int ty)
	{
		g.setColor(getForeground());
		Font drawingFont = getFont();
		g.setFont(drawingFont);
		int fontFlags = 0;

		StringBuilder line = code.getsb(lineNum);
		int xx = getInsets().left;
		char[] a = line.toString().toCharArray();
		Color c = g.getColor();

		if (highlighter == null)
		{
			drawChars(g,a,0,a.length,xx,ty);
		}
		else
		{
			ArrayList<HighlighterInfoEx> hlall = highlighter.getStyles(lineNum);
			/*DEBUG SHIT: This is annoying to write, so I'm going to commit it once.
			if (lineNum == 10)
			{
				System.out.print("[ ");
				for (HighlighterInfoEx hl : hlall)
					System.out.print(hl.startPos + "-" + hl.endPos + " ");
				System.out.println("]");
			}*/
			int pos = 0;
			for (HighlighterInfoEx hl : hlall)
			{
				// Start by printing normal characters until we reach styleBlock.startPos
				xx = drawChars(g,a,pos,hl.startPos,xx,ty);
				// Print the remaining characters in the styleBlock range
				if (hl.color != null)
					g.setColor(hl.color);
				else
					g.setColor(c);
				if (hl.fontStyle != fontFlags)
				{
					fontFlags = hl.fontStyle;
					drawingFont = getFont().deriveFont(fontFlags);
					g.setFont(drawingFont);
				}
				xx = drawChars(g,a,hl.startPos,hl.endPos,xx,ty);
				pos = hl.endPos;
				g.setFont(getFont());
				g.setColor(c);
			}
		}
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Object map = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //$NON-NLS-1$
		if (map != null) ((Graphics2D) g).addRenderingHints((Map<?,?>) map);

		Rectangle clip = g.getClipBounds();

		// Fill background
		g.setColor(getBackground());
		g.fillRect(clip.x,clip.y,clip.width,clip.height);

		for (Marker a : markers)
			a.paint(g,getInsets(),metrics,0,code.size());

		// Draw each line
		final int insetY = lineLeading + lineAscent;
		int lineNum = clip.y / lineHeight;

		for (int ty = lineNum * lineHeight + insetY; ty < clip.y + clip.height + lineHeight
				&& lineNum < code.size(); ty += lineHeight)
			drawLine(g,lineNum++,ty);

		if (isFocusOwner()) caret.paint(g,sel);
	}

	/**
	 * A convenience repaint method which can convert a row/col pair into x/y space.
	 * @param convert True to convert row/col into x/y space. False to treat as x/y.
	 */
	public void repaint(int col, int row, int w, int h, boolean convert)
	{
		if (convert)
		{
			Dimension g = getMaxGlyphSize();
			col *= g.width;
			row *= g.height;
		}
		repaint(col,row,w,h);
	}

	//Input handling

	/**
	 * Translates a mouse coordinate to a text coordinate (y = row, x = col).
	 * @param bound Whether the coordinate should be trimmed to a valid column.
	 */
	public Point mouseToPoint(Point m, boolean bound)
	{
		Point p = m;
		int row = p.y / lineHeight;
		row = Math.max(Math.min(row,code.size() - 1),0);
		int col = bound ? line_offset_from(row,p.x) : (int) Math.round(p.x / (double) monoAdvance);
		if (!bound) return new Point(Math.max(col,0),row);
		col = Math.max(Math.min(col,code.getsb(row).length()),0);
		return new Point(col,row);
	}
	

	/**
	 * Whether or not mouseReleased should adjust the caret and focus.
	 * This flag is set by mousePressed if it wanted to adjust the caret
	 * and focus but couldn't because of a possible DnD operation.
	 */
	private boolean shouldHandleRelease = false;

	protected void handleMouseEvent(MouseEvent e)
	{
		/// FIXME: Double-click-and-drag is supposed to wrap the start and
		/// end caret positions to word endings.

		if (e.getID() == MouseEvent.MOUSE_PRESSED) requestFocusInWindow();
		//		if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0
		//				&& (e.getID() == MouseEvent.MOUSE_PRESSED)) dragger.query();

		if (SwingUtilities.isLeftMouseButton(e))
		{
			switch (e.getID())
			{
				case MouseEvent.MOUSE_ENTERED:
					return;
				case MouseEvent.MOUSE_PRESSED:
					dragger.mousePressed(e);
					if (e.isConsumed()) shouldHandleRelease = true;
					break;
				case MouseEvent.MOUSE_RELEASED:
					dragger.mouseReleased(e);
					removeMouseAutoScroll();
					break;
				case MouseEvent.MOUSE_DRAGGED:
					if (!hasFocus()) return;
					dragger.mouseDragged(e);
					break;
			}
			if (e.isConsumed() || e.getID() == MouseEvent.MOUSE_EXITED) return;

			if ((e.getModifiers() & (InputEvent.ALT_MASK | InputEvent.CTRL_MASK)) != 0)
				sel.changeType(ST.RECT);
			else
				sel.changeType(ST.NORM);

			Point sp = new Point(caret.col,caret.row);
			Point p = mouseToPoint(e.getPoint(),sel.type != ST.RECT);
			caret.col = p.x;
			caret.row = p.y;
			caret.colw = sel.type == ST.RECT ? caret.col * monoAdvance : line_wid_at(caret.row,caret.col);

			if (e.getClickCount() == 2)
			  sel.special.setHandler(sel.wordSelHandler);
			else if (e.getClickCount() == 3)
			  sel.special.setHandler(sel.lineSelHandler);
			else if (e.getID() == MouseEvent.MOUSE_PRESSED)
				sel.special.valid = false;

			updateMouseAutoScroll(e.getPoint());

			if (sel.special.valid)
				sel.special.adjust();
			

			//cleanup (deselect, flash, repaint)
			if (!sel.special.valid &&
					(e.getModifiers() & Event.SHIFT_MASK) == 0
					&& (e.getID() == MouseEvent.MOUSE_PRESSED || (e.getID() == MouseEvent.MOUSE_RELEASED && shouldHandleRelease)))
				sel.deselect(false);
			shouldHandleRelease = false;
			caret.flashOn();

			if (sp.x != caret.col || sp.y != caret.row)
			{
				caret.positionChanged();
				undoCanMerge = false;
			}
			if (e.getID() == MouseEvent.MOUSE_RELEASED) sel.selectionChanged();

			repaint();
			return;
		}
		else if (SwingUtilities.isMiddleMouseButton(e))
		{
			if (e.getID() != MouseEvent.MOUSE_PRESSED) return;
			Point p = mouseToPoint(e.getPoint(),true);
			ST sto = sel.type;
			sel.type = ST.NORM;
			UndoPatch up = new UndoPatch(p.y,Math.min(code.size() - 1,p.y + sel.getMiddlePasteRipple()));
			up.cbefore.selt = sto;
			sel.col = caret.col = p.x;
			sel.row = caret.row = p.y;
			caret.positionChanged();
			final int mcr = sel.middleClickPaste();
			doCodeSize(true);
			up.realize(mcr);
			storeUndo(up,OPT.PASTE);
			repaint();
		}
	}

	@Override
	protected void processMouseEvent(MouseEvent e)
	{
		super.processMouseEvent(e);
		handleMouseEvent(e);
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e)
	{
		super.processMouseMotionEvent(e);
		handleMouseEvent(e);
	}

	protected void processKeyTyped(KeyEvent e)
	{
		final Point sc = new Point(caret.col,caret.row);
		switch (e.getKeyChar())
		{
			case KeyEvent.VK_ENTER:
				switch (sel.type)
				{
					case NORM:
						UndoPatch up = new UndoPatch();
						sel.deleteSel();
						StringBuilder nr = code.getsb(caret.row);

						int offset = 0;
						StringBuilder ins = new StringBuilder();

						for (int i = 0; i < nr.length(); i++)
							if (Character.isWhitespace(nr.charAt(i)))
							{
								offset++;
								ins.append(nr.charAt(i));
							}
							else
								break;

						int iind = myLang.hasIndentAfter(nr.toString());
						if (iind != -1)
						{
							String ind = myLang.getIndent(iind);
							ins.append(ind);
							offset += ind.length();
						}
						code.add(++caret.row,ins + nr.substring(caret.col));
						nr.delete(caret.col,nr.length());
						sel.col = caret.col = offset;
						caret.colw = line_wid_at(caret.row,caret.col);
						up.realize(caret.row);
						storeUndo(up,OPT.ENTER);
						break;
					case RECT:
						sel.deselect(true);
						break;
				}
				sel.deselect(true);
				break;
			case KeyEvent.VK_BACK_SPACE:
				switch (sel.type)
				{
					case NORM:
					{
						UndoPatch up = new UndoPatch();
						int otype = selGetKind(code.getsb(caret.row),caret.col - 1);
						if (!sel.deleteSel())
						{
							if (Settings.smartBackspace && otype == ChType.WHITE && caret.col > 0
									&& all_white(code.getsb(caret.row).substring(0,caret.col)))
							{
								unindent(caret.row);
								caret.col = sel.col;
								up.realize(caret.row);
								storeUndo(up,OPT.BACKSPACE);
								break;
							}
							// 	 
							do
							{
								if (caret.col > 0)
								{
									code.getsb(caret.row).delete(caret.col - 1,caret.col);
									--caret.col;
								}
								else if (caret.row > 0)
								{
									StringBuilder s1 = code.getsb(caret.row - 1);
									StringBuilder s2 = code.getsb(caret.row);
									code.remove(caret.row--);
									up.prefix_row(code.get(caret.row));
									caret.col = s1.length();
									s1.append(s2);
								}
								else
									break;
							}
							while (e.isControlDown() && selOfKind(code.getsb(caret.row),caret.col - 1,otype));
						}
						caret.colw = line_wid_at(caret.row,caret.col);
						sel.deselect(false);
						up.realize(caret.row);
						storeUndo(up,OPT.BACKSPACE);
						break;
					}
					case RECT:
					{
						UndoPatch up = new UndoPatch();
						int otype = selGetKind(code.getsb(caret.row),caret.col - 1);
						if (!sel.deleteSel())
						{
							if (e.isControlDown())
							{
								// Control-Backspace on multiple lines.
								// We'll handle this by determining the smallest word/pattern we
								// can control-backspace over, and running with it on all lines.

								// Nab the smallest distance
								int mindist = -1;
								for (int y = Math.min(sel.row,caret.row); y <= Math.max(sel.row,caret.row); y++)
								{
									int actcol = column_to_index(y,caret.col);
									StringBuilder sb = code.getsb(y);
									otype = selGetKind(sb,actcol - 1);
									int mcol = actcol, mydist = 0;
									do
									{
										if (mcol > 0)
											if (mcol < sb.length())
											{
												if (sb.charAt(mcol--) == '\t')
												{
													mydist += Settings.indentSizeInSpaces;
													mydist /= Settings.indentSizeInSpaces;
													mydist *= Settings.indentSizeInSpaces;
												}
												else
													mydist += 1;
											}
											else
											{
												mydist++;
												mcol--;
											}
										else
											break;
									}
									while (selOfKind(code.getsb(y),mcol - 1,otype));
									if (mindist == -1 || mydist < mindist) mindist = mydist;
								}
								int cs = caret.col - mindist, ce = caret.col;
								sel.col = caret.col = cs;
								caret.colw = line_wid_at(caret.row,caret.col);
								for (int y = Math.min(sel.row,caret.row); y <= Math.max(sel.row,caret.row); y++)
									code.getsb(y).delete(column_to_index(y,cs),column_to_index(y,ce));
							}
							else if (caret.col > 0)
							{
								for (int y = Math.min(sel.row,caret.row); y <= Math.max(sel.row,caret.row); y++)
									code.getsb(y).delete(column_to_index(y,caret.col - 1),
											column_to_index(y,caret.col));
								sel.col = --caret.col;
								caret.colw = caret.col * monoAdvance;
							}
						}
						up.realize(Math.max(caret.row,sel.row));
						storeUndo(up,OPT.BACKSPACE);
						break;
					}
				}
				break;
			case KeyEvent.VK_DELETE:
				switch (sel.type)
				{
					case NORM:
					{
						UndoPatch up = new UndoPatch();
						int otype = selGetKind(code.getsb(caret.row),caret.col);
						if (!sel.deleteSel()) do
						{
							if (caret.col < code.getsb(caret.row).length())
								code.getsb(caret.row).delete(caret.col,caret.col + 1);
							else if (caret.row + 1 < code.size())
							{
								StringBuilder s1 = code.getsb(caret.row);
								StringBuilder s2 = code.getsb(caret.row + 1);
								code.remove(caret.row + 1);
								s1.append(s2);
							}
							else
								break;
						}
						while (e.isControlDown() && selOfKind(code.getsb(caret.row),caret.col,otype));
						up.realize(caret.row);
						storeUndo(up,OPT.DELETE);
						break;
					}
					case RECT:
					{
						UndoPatch up = new UndoPatch();
						int otype = selGetKind(code.getsb(caret.row),caret.col);
						if (!sel.deleteSel())
						{
							for (int y = Math.min(sel.row,caret.row); y <= Math.max(sel.row,caret.row); y++)
							{
								int dcol = column_to_index(y,caret.col);
								otype = selGetKind(code.getsb(y),dcol);
								do
								{
									if (dcol < code.getsb(y).length())
										code.getsb(y).delete(dcol,dcol + 1);
									else
										break;
								}
								while (e.isControlDown() && selOfKind(code.getsb(y),dcol,otype));
							}
						}
						up.realize(caret.row);
						storeUndo(up,OPT.DELETE);
						break;
					}
				}
				break;
			case KeyEvent.VK_TAB:
				if (!sel.isEmpty())
				{
					UndoPatch up = new UndoPatch();
					String tab = Settings.indentRepString;
					int yx = Math.max(sel.row,caret.row);
					for (int y = Math.min(sel.row,caret.row); y <= yx; y++)
						code.getsb(y).insert(0,tab);
					sel.col += tab.length();
					caret.col += tab.length();
					up.realize(Math.max(sel.row,caret.row));
					storeUndo(up,OPT.INDENT);
					break;
				}
				UndoPatch up = new UndoPatch();
				sel.insert(Settings.indentRepString);
				up.realize(caret.row);
				storeUndo(up,OPT.TYPED);
				break;
			default:
				if (e.isControlDown() || e.isAltDown())
				{
					switch (e.getKeyCode())
					{
					// Handle bindings. Usually this is handled by registering key bindings,
					// in which case it is usually consumed before it gets here.
						default:
							break;
					}
				}
				else
				{
					UndoPatch up2 = new UndoPatch();
					sel.insert(e.getKeyChar());
					up2.realize(caret.row);
					storeUndo(up2,e.getKeyChar() == ' ' ? OPT.SPACE : OPT.TYPED);
				}
				break;
		}
		if (sc.x != caret.col || sc.y != caret.row) caret.positionChanged();
		doCodeSize(true);
		//		doShowCursor();
	}

	private static boolean all_white(String str)
	{
		for (int i = 0; i < str.length(); i++)
			if (!Character.isWhitespace(str.charAt(i))) return false;
		return true;
	}

	protected void processKeyPressed(KeyEvent e)
	{
		//Note to developers: please consume keys that you use.
		//This way, containers don't still see them as usable
		//(e.g. arrow keys triggering the scrollbar)
		Point sc = new Point(caret.col,caret.row);
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_INSERT:
				caret.insert ^= true;
				e.consume();
				break;
			case KeyEvent.VK_LEFT:
				int otype = selGetKind(code.getsb(caret.row),caret.col - 1);
				do
				{
					if (caret.col > 0)
						--caret.col;
					else if (caret.row > 0 && sel.type != ST.RECT)
					{
						caret.row--;
						caret.col = code.getsb(caret.row).length();
					}
					else
						break;
				}
				while (e.isControlDown() && selOfKind(code.getsb(caret.row),caret.col - 1,otype));
				undoCanMerge = false;

				caret.colw = line_wid_at(caret.row,caret.col);
				if (e.isAltDown())
					sel.changeType(ST.RECT);
				else if (e.isShiftDown())
					sel.changeType(ST.NORM);
				else
					sel.deselect(true);
				doCodeSize(false);
				e.consume();
				break;
			case KeyEvent.VK_RIGHT:
				otype = selGetKind(code.getsb(caret.row),caret.col);
				do
				{
					if (sel.type == ST.RECT || caret.col < code.getsb(caret.row).length())
						++caret.col;
					else if (caret.row + 1 < code.size() && sel.type != ST.RECT)
					{
						caret.row++;
						caret.col = 0;
					}
					else
						break;
				}
				while (e.isControlDown() && selOfKind(code.getsb(caret.row),caret.col,otype));
				undoCanMerge = false;
				caret.colw = line_wid_at(caret.row,caret.col);
				if (e.isAltDown())
					sel.changeType(ST.RECT);
				else if (e.isShiftDown())
					sel.changeType(ST.NORM);
				else
					sel.deselect(true);
				e.consume();
				break;
			case KeyEvent.VK_UP:
				if (caret.row > 0)
				{
					if (sel.type == ST.RECT)
						--caret.row;
					else
					{
						caret.col = line_offset_from(--caret.row,caret.colw);
						if (caret.col > code.getsb(caret.row).length())
							caret.col = code.getsb(caret.row).length();
					}
				}
				else
					caret.colw = caret.col = 0;
				undoCanMerge = false;
				if (e.isAltDown())
					sel.changeType(ST.RECT);
				else if (e.isShiftDown())
					sel.changeType(ST.NORM);
				else
					sel.deselect(true);
				e.consume();
				break;
			case KeyEvent.VK_DOWN:
				if (caret.row + 1 < code.size())
				{
					if (sel.type == ST.RECT)
						++caret.row;
					else
					{
						caret.col = line_offset_from(++caret.row,caret.colw);
						if (caret.col > code.getsb(caret.row).length())
							caret.col = code.getsb(caret.row).length();
					}
				}
				else
					caret.colw = line_wid_at(caret.row,caret.col = code.getsb(caret.row).length());
				undoCanMerge = false;
				if (e.isAltDown())
					sel.changeType(ST.RECT);
				else if (e.isShiftDown())
					sel.changeType(ST.NORM);
				else
					sel.deselect(true);
				e.consume();
				break;
			case KeyEvent.VK_END:
				if (e.isControlDown()) caret.row = code.size() - 1;
				caret.colw = line_wid_at(caret.row,caret.col = code.getsb(caret.row).length());
				if (e.isAltDown())
					sel.changeType(ST.RECT);
				else if (!e.isShiftDown()) sel.deselect(true);
				e.consume();
				break;
			case KeyEvent.VK_HOME:
				if (e.isControlDown()) caret.row = 0;
				int P = 0;
				while (P < code.getsb(caret.row).length()
						&& Character.isWhitespace(code.getsb(caret.row).charAt(P)))
					P++;
				if (caret.col == P) P = 0;
				caret.colw = P; caret.col = P;
				if (e.isAltDown())
					sel.changeType(ST.RECT);
				else if (e.isShiftDown())
					sel.changeType(ST.NORM);
				else
					sel.deselect(true);
				e.consume();
				break;
			case KeyEvent.VK_PAGE_UP:
				int height = (getParent() instanceof JViewport) ? getParent().getHeight() : getHeight();
				caret.row = Math.max(0,caret.row - height / lineHeight);
				if (sel.type != ST.RECT) caret.col = Math.min(caret.col,code.getsb(caret.row).length());
				//FIXME: If parent is viewport, also scroll that a screenfull
				if (!e.isShiftDown()) sel.deselect(true);
				e.consume();
				break;
			case KeyEvent.VK_PAGE_DOWN:
				height = (getParent() instanceof JViewport) ? getParent().getHeight() : getHeight();
				caret.row = Math.min(code.size() - 1,caret.row + height / lineHeight);
				if (sel.type != ST.RECT) caret.col = Math.min(caret.col,code.getsb(caret.row).length());
				//FIXME: If parent is viewport, also scroll that a screenfull
				if (!e.isShiftDown()) sel.deselect(true);
				e.consume();
				break;
		}
		if (sc.x != caret.col || sc.y != caret.row) caret.positionChanged();
		fitToCode();
		//		doShowCursor();
	}

	@Override
	protected void processComponentKeyEvent(KeyEvent e)
	{
		caret.flashOn();
		switch (e.getID())
		{
			case KeyEvent.KEY_TYPED:
				processKeyTyped(e);
				break;
			case KeyEvent.KEY_PRESSED:
				processKeyPressed(e);
				break;
		}
	}

	//Line Change Listeners
	/**
	 * A LineChange is invoked whenever a characters are added/removed
	 * from lines, whether the line exists or is created. For only
	 * listening to whether lines are added/removed, use Code.CodeListener.
	 */
	public interface LineChangeListener extends EventListener
	{
		void linesChanged(int start, int end);
	}

	public void addLineChangeListener(LineChangeListener listener)
	{
		listenerList.add(LineChangeListener.class,listener);
	}

	public void removeLineChangeListener(LineChangeListener listener)
	{
		listenerList.remove(LineChangeListener.class,listener);
	}

	protected void fireLineChange(int start, int end)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			if (listeners[i] == LineChangeListener.class)
			{
				// Lazily create the event:
				// if (e == null)
				// e = new ListSelectionEvent(this, firstIndex, lastIndex);
				((LineChangeListener) listeners[i + 1]).linesChanged(start,end);
			}
	}

	//Inner classes (mostly drag and drop)

	/**
	 * Listens for mouse events for the purposes of detecting drag gestures.
	 * BasicTextUI will maintain one of these per AppContext.
	 */
	class DragListener
	{
		private boolean dragStarted;
		private int motionThreshold;
		private MouseEvent dndArmedEvent;

		public DragListener()
		{
			motionThreshold = getDefaultThreshold();
		}

		public int getDefaultThreshold()
		{
			Integer ti = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty(
					"DnD.gestureMotionThreshold");
			return ti == null ? 5 : ti.intValue();
		}

		public void mousePressed(MouseEvent e)
		{
			dragStarted = false;
			if (isDragPossible(e.getPoint()))
			{
				dndArmedEvent = e;
				e.consume();
			}
		}

		public void mouseReleased(MouseEvent e)
		{
			if (dragStarted) e.consume();
			dndArmedEvent = null;
		}

		public void mouseDragged(MouseEvent e)
		{
			if (dragStarted)
			{
				e.consume();
				return;
			}
			if (dndArmedEvent == null) return;

			int dx = Math.abs(e.getX() - dndArmedEvent.getX());
			int dy = Math.abs(e.getY() - dndArmedEvent.getY());
			if ((dx > motionThreshold) || (dy > motionThreshold))
			{
				TransferHandler th = JoshText.this.getTransferHandler();
				dragStarted = true;
				th.exportAsDrag(
						JoshText.this,
						dndArmedEvent,
						SunDragSourceContextPeer.convertModifiersToDropAction(e.getModifiersEx(),
								th.getSourceActions(JoshText.this)));
				dndArmedEvent = null;
			}
			e.consume();
		}

		/** Determines if the press event is located over a selection */
		protected boolean isDragPossible(Point mousePt)
		{
			Point p = mouseToPoint(mousePt,false);
			return sel.contains(p.y,p.x);
		}

		public void query()
		{
			System.out.println(dragStarted + "," + dndArmedEvent);
		}
	}

	//TransferHandler
	class JoshTextTransferHandler extends TransferHandler
	{
		private static final long serialVersionUID = 1L;

		public JoshTextTransferHandler()
		{
			addPropertyChangeListener("dropLocation",new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent pce)
				{
					repaintDropLocation(pce.getOldValue());
					repaintDropLocation(pce.getNewValue());
				}
			});
		}

		public void repaintDropLocation(Object drop)
		{
			if (drop == null || !(drop instanceof DropLocation)) repaint();
			DropLocation loc = (DropLocation) drop;
			loc.getDropPoint();
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			return COPY_OR_MOVE;
		}

		@Override
		protected Transferable createTransferable(JComponent c)
		{
			if (!(c instanceof JoshText)) return null;
			JoshText j = (JoshText) c;
			if (j.sel.isEmpty()) return null;
			return new StringSelection(j.sel.getSelectedTextForCopy());
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action)
		{
			UndoPatch up = new UndoPatch();
			if (action == MOVE && source instanceof JoshText)
			{
				((JoshText) source).sel.deleteSel();
				up.realize(Math.max(caret.row,sel.row));
				storeUndo(up,OPT.DELETE);
			}
			repaint();
		}

		@Override
		public boolean canImport(TransferSupport info)
		{
			return info.isDataFlavorSupported(DataFlavor.stringFlavor);
			//			if (info.isDrop()) dropPoint = mouseToPoint(info.getDropLocation().getDropPoint(),true);
			//			return true;
		}

		@Override
		public boolean importData(TransferSupport info)
		{
			Transferable t = info.getTransferable();
			String data;
			try
			{
				data = (String) t.getTransferData(DataFlavor.stringFlavor);
			}
			catch (Exception e)
			{
				return false;
			}
			UndoPatch up = new UndoPatch();
			if (info.isDrop())
			{
				Point p = mouseToPoint(info.getDropLocation().getDropPoint(),true);
				caret.row = p.y;
				caret.col = p.x;
				sel.deselect(false);
				sel.type = ST.NORM;
				up.startRow = caret.row;
				up.reconstruct(p.y,Math.min(code.size() - 1,p.y + sel.getInsertRipple(data)));
			}
			int er = 0;
			if (data.length() > 0 && data.charAt(data.length() - 1) == 0)
				er = Math.max(1,sel.insertRect(data.substring(0,data.length() - 1))) - 1;
			else
				sel.insert(data);
			up.realize(caret.row + er);
			storeUndo(up,OPT.PASTE);
			repaint();
			return true;
		}
	}

	//Scrollable

	public Dimension getPreferredScrollableViewportSize()
	{
		return new Dimension(320,240);
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		switch (orientation)
		{
			case SwingConstants.VERTICAL:
				return getFontMetrics(getFont()).getHeight();
			case SwingConstants.HORIZONTAL:
				return monoAdvance;
			default:
				throw new IllegalArgumentException("Invalid orientation: " + orientation);
		}
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		switch (orientation)
		{
			case SwingConstants.VERTICAL:
				return visibleRect.height;
			case SwingConstants.HORIZONTAL:
				return visibleRect.width;
			default:
				throw new IllegalArgumentException("Invalid orientation: " + orientation);
		}
	}

	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	void fireResize()
	{
		Container a = getParent();
		if (a == null) return;
		int w = a.getWidth(), h = a.getHeight();
		Dimension ps = getMinimumSize();
		ps.width = Math.max(ps.width,w);
		ps.height = Math.max(ps.height,h);
		setPreferredSize(ps);
		setSize(ps);
	}

	// Listen to parent component
	public void componentResized(ComponentEvent e)
	{
		fireResize();
	}

	public void componentHidden(ComponentEvent e)
	{
		repaint();
	}

	public void componentMoved(ComponentEvent e)
	{
		repaint();
	}

	public void componentShown(ComponentEvent e)
	{
		repaint();
	}

	// Be a clipboard owner
	public void lostOwnership(Clipboard arg0, Transferable arg1)
	{
		// WHOGIVESAFUCK.jpg
	}

	//-----------------------------------------------------------------
	//----- Mark Matching Brackets ------------------------------------
	//-----------------------------------------------------------------

	static enum MatchState
	{
		NOT_MATCHING,NO_MATCH,MATCHING
	}

	class BracketMarker implements Marker,CaretListener
	{
		MatchState matching;
		int matchLine, matchPos;

		public void paint(Graphics g, Insets i, CodeMetrics gm, int line_start, int line_end)
		{
			Color c = g.getColor();
			if (matching == MatchState.MATCHING)
			{
				g.setColor(new Color(100,100,100));
				g.drawRect(line_wid_at(matchLine,matchPos),matchLine * lineHeight,monoAdvance,lineHeight);
			}
			else if (matching == MatchState.NO_MATCH)
			{
				g.setColor(new Color(255,0,0));
				g.fillRect(line_wid_at(matchLine,matchPos),matchLine * lineHeight,monoAdvance,lineHeight);
			}
			g.setColor(c);
		}

		private boolean matchFound(BracketMatch m, int x, int y)
		{
			if (--m.count <= 0)
			{
				matchLine = y;
				matchPos = x;
				matching = MatchState.MATCHING;
				return true;
			}
			return false;
		}

		class BracketMatch
		{
			char match;
			char opposite;
			short count;

			public BracketMatch(char m, char o, short c)
			{
				match = m;
				opposite = o;
				count = c;
			}
		}

		private void findMatchForward(int row, int col, BracketMatch match)
		{
			int y = row;
			int blockType = 0;
			StringBuilder sb = code.getsb(y);

			// Figure out what kind of block we're in, if any.
			ArrayList<HighlighterInfoEx> hlall = highlighter.getStyles(y);

			int offset;
			for (offset = 0; offset < hlall.size(); offset++)
			{
				HighlighterInfoEx hl = hlall.get(offset);
				if (col < hl.startPos) break; // The blocks have skipped us.
				if (col >= hl.startPos && col < hl.endPos)
				{
					blockType = hl.blockHash;
					break;
				}
			}
			if (subFindMatchForward(match,sb,hlall,offset,col,blockType,y)) return;

			for (y++; y < code.size(); y++)
			{
				hlall = highlighter.getStyles(y);
				if (subFindMatchForward(match,code.getsb(y),hlall,0,0,blockType,y)) return;
			}
		}

		private boolean subFindMatchForward(BracketMatch match, StringBuilder sb,
				ArrayList<HighlighterInfoEx> hlall, int offset, int spos, int blockType, int y)
		{
			int pos = spos;
			for (int i = offset; i < hlall.size(); i++)
			{
				HighlighterInfoEx hl = hlall.get(i);
				if (blockType == 0) // If our start wasn't in a block
					for (; pos < hl.startPos; pos++)
						// Check outside this block's range
						if (sb.charAt(pos) == match.match)
						{
							if (matchFound(match,pos,y)) return true;
						}
						else if (sb.charAt(pos) == match.opposite) match.count++;
				if (blockType == hlall.get(i).blockHash) // If the block has the same type
					for (pos = Math.max(spos,hl.startPos); pos < hl.endPos; pos++)
						// Check inside it
						if (sb.charAt(pos) == match.match)
						{
							if (matchFound(match,pos,y)) return true;
						}
						else if (sb.charAt(pos) == match.opposite) match.count++;
				pos = hl.endPos;
			}
			return false;
		}

		private void findMatchBackward(int row, int col, BracketMatch match)
		{
			int y = row;
			int blockType = 0;
			StringBuilder sb = code.getsb(y);

			// Figure out what kind of block we're in, if any.
			ArrayList<HighlighterInfoEx> hlall = highlighter.getStyles(y);

			int offset;
			for (offset = 0; offset < hlall.size(); offset++)
			{
				HighlighterInfoEx hl = hlall.get(offset);
				if (col < hl.startPos) break; // The blocks have skipped us.
				if (col >= hl.startPos && col < hl.endPos)
				{
					blockType = hl.blockHash;
					break;
				}
			}
			if (subFindMatchBackward(match,sb,hlall,offset,caret.col,blockType,y)) return;

			for (y--; y >= 0; y--)
			{
				hlall = highlighter.getStyles(y);
				if (subFindMatchBackward(match,code.getsb(y),hlall,hlall.size() - 1,code.getsb(y).length(),
						blockType,y)) return;
			}
		}

		private boolean subFindMatchBackward(BracketMatch match, StringBuilder sb,
				ArrayList<HighlighterInfoEx> hlall, int offset, int spos, int blockType, int y)
		{
			int pos = spos;
			int i = offset;
			HighlighterInfoEx hl = hlall.get(i);
			for (;;)
			{
				if (blockType == hlall.get(i).blockHash) // If the block has the same type
					for (pos = Math.min(spos,hl.endPos - 1); pos >= hl.startPos; pos--)
						// Check inside it
						if (sb.charAt(pos) == match.match)
						{
							if (matchFound(match,pos,y)) return true;
						}
						else if (sb.charAt(pos) == match.opposite) match.count++;
				if (i > 0)
				{
					hl = hlall.get(--i);
					if (blockType == 0) // If our start wasn't in a block
						for (pos = hl.startPos - 1; pos >= hl.endPos; pos--)
							// Check outside this block's range
							if (sb.charAt(pos) == match.match)
							{
								if (matchFound(match,pos,y)) return true;
							}
							else if (sb.charAt(pos) == match.opposite) match.count++;
				}
				else
				{
					if (blockType == 0) // If our start wasn't in a block
						for (pos = hl.startPos - 1; pos >= 0; pos--)
							// Check outside this block's range
							if (sb.charAt(pos) == match.match)
							{
								if (matchFound(match,pos,y)) return true;
							}
							else if (sb.charAt(pos) == match.opposite) match.count++;
					break;
				}
			}
			return false;
		}

		public void caretUpdate(CaretEvent ce)
		{
			matching = MatchState.NOT_MATCHING;
			StringBuilder sb = code.getsb(caret.row);
			String start = "([{", end = ")]}";
			for (int x : new int[] { caret.col - 1,caret.col })
				if (x >= 0 && x < sb.length())
				{
					char c = sb.charAt(x);
					int p = start.indexOf(c);
					if (p != -1)
					{
						findMatchForward(caret.row,x,new BracketMatch(end.charAt(p),start.charAt(p),(short) 0));
						return;
					}
					p = end.indexOf(c);
					if (p != -1)
					{
						findMatchBackward(caret.row,x,new BracketMatch(start.charAt(p),end.charAt(p),(short) 0));
						return;
					}
				}
		}
	}

	//-----------------------------------------------------------------
	//----- Be Undoable -----------------------------------------------
	//-----------------------------------------------------------------

	static final class OPT
	{
		public static final int OTHER = 0;
		public static final int TYPED = 1;
		public static final int BACKSPACE = 2;
		public static final int DELETE = 3;
		public static final int SPACE = 4;
		public static final int ENTER = 5;
		public static final int PASTE = 6;
		public static final int INDENT = 7;
		public static final int DUPLICATE = 8;
		public static final int REPLACE = 9;
		public static final int SWAP = 10;
		public static final int UNSWAP = 11;
	}

	class UndoPatch
	{
		int opTag;
		Line[] oldtext;
		Line[] patchtext;
		int startRow;

		caretdata cbefore = new caretdata(), cafter = new caretdata();

		class caretdata
		{
			public int ccol, crow, scol, srow;
			ST selt;

			public void grab()
			{
				ccol = caret.col;
				crow = caret.row;
				scol = sel.col;
				srow = sel.row;
				selt = sel.type;
			}

			public void replace()
			{
				caret.col = ccol;
				caret.row = crow;
				sel.col = scol;
				sel.row = srow;
				sel.type = selt;
			}

			public void copy(caretdata cfrom)
			{
				ccol = cfrom.ccol;
				crow = cfrom.crow;
				scol = cfrom.scol;
				srow = cfrom.srow;
				selt = cfrom.selt;
			}
		}

		UndoPatch(Line[] t, Line[] ot, int sr)
		{
			oldtext = ot;
			patchtext = t;
			startRow = sr;
		}

		public void prefix_row(Line ln)
		{
			startRow--;
			Line[] ancient = oldtext;
			oldtext = new Line[oldtext.length + 1];
			oldtext[0] = ln;
			for (int i = 0; i < ancient.length; i++)
				oldtext[i + 1] = ancient[i];
		}

		UndoPatch()
		{
			this(Math.min(caret.row,sel.row),Math.max(caret.row,sel.row));
		}

		UndoPatch(int startRow, int endRow)
		{
			final int lc = endRow - startRow + 1;
			oldtext = new Line[lc];
			for (int i = 0; i < lc; i++)
				oldtext[i] = new Line(code.get(startRow + i));
			this.startRow = startRow;
			cbefore.grab();
		}

		public void reconstruct(int newStartRow, int newEndRow)
		{
			final int lc = newEndRow - newStartRow + 1;
			oldtext = new Line[lc];
			for (int i = 0; i < lc; i++)
				oldtext[i] = new Line(code.get(startRow + i));
			startRow = newStartRow;
		}

		public void realize(int endRow)
		{
			fireLineChange(startRow,endRow);
			final int lc = endRow - startRow + 1;
			patchtext = new Line[lc];
			for (int i = 0; i < lc; i++)
				patchtext[i] = new Line(code.get(startRow + i));
			cafter.grab();
		}
	}

	private ArrayList<UndoPatch> undoPatches = new ArrayList<UndoPatch>();
	private boolean undoCanMerge = true;
	private int patchIndex = 0;

	public void undo()
	{
		if (patchIndex == 0) return;
		UndoPatch p = undoPatches.get(--patchIndex);
		// Reverse patch
		int prow;
		for (prow = 0; prow < p.patchtext.length; prow++)
		{
			if (prow >= p.oldtext.length)
			{
				for (int da = p.patchtext.length - prow; da > 0; da--)
					code.remove(p.startRow + prow);
				break;
			}
			code.set(p.startRow + prow,new Line(p.oldtext[prow]));
		}
		while (prow < p.oldtext.length)
		{
			code.add(p.startRow + prow,new Line(p.oldtext[prow]));
			prow++;
		}
		p.cbefore.replace();
		fireLineChange(p.startRow,p.startRow + p.oldtext.length);
		repaint();
	}

	public void redo()
	{
		if (patchIndex >= undoPatches.size()) return;
		UndoPatch p = undoPatches.get(patchIndex++);
		// Perform patch
		int prow;
		for (prow = 0; prow < p.oldtext.length; prow++)
		{
			if (prow >= p.patchtext.length)
			{
				for (int da = p.oldtext.length - prow; da > 0; da--)
					code.remove(p.startRow + prow);
				break;
			}
			code.set(p.startRow + prow,new Line(p.patchtext[prow]));
		}
		while (prow < p.patchtext.length)
		{
			code.add(p.startRow + prow,new Line(p.patchtext[prow]));
			prow++;
		}
		p.cafter.replace();
		repaint();
	}

	public void storeUndo(UndoPatch undo, int patchType)
	{
		undo.opTag = patchType;
		while (patchIndex < undoPatches.size())
			undoPatches.remove(undoPatches.size() - 1);
		if (!undoCanMerge || patchIndex == 0 || !undoCompatible(undoPatches.get(patchIndex - 1),undo))
		{
			undoPatches.add(undo);
			undoCanMerge = true;
			patchIndex++;
		}
		else
			undoMerge(undo,undoPatches.get(patchIndex - 1));
	}

	private static void undoMerge(UndoPatch merge, UndoPatch into)
	{
		into.patchtext[0] = merge.patchtext[0];
		into.cafter.copy(merge.cafter);
		into.opTag = merge.opTag;
	}

	private static boolean undoCompatible(UndoPatch up1, UndoPatch up2)
	{
		if ((up1.opTag != up2.opTag && up2.opTag != OPT.SPACE) || up1.startRow != up2.startRow)
			return false;
		if (up1.oldtext.length != up2.oldtext.length || up2.patchtext.length != up2.patchtext.length)
			return false;
		return true;
	}

	public void focusGained(FocusEvent arg0)
	{
		FindDialog.getInstance().selectedJoshText = this;
	}

	public void focusLost(FocusEvent arg0)
	{ //Unused
	}

	public boolean isChanged()
	{
		return undoPatches.size() > 1;
	}
}