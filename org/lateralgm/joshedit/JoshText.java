/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011, 2012 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. */

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
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.joshedit.FindDialog.FindNavigator;
import org.lateralgm.joshedit.Selection.ST;
import org.lateralgm.joshedit.TokenMarker.TokenMarkerInfo;
import org.lateralgm.messages.Messages;

/**
 * The main component class; instantiate this, and you're good to go.
 */
public class JoshText extends JComponent implements Scrollable,ComponentListener,ClipboardOwner,
		FocusListener
{
	/** Make the compiler shut up. */
	private static final long serialVersionUID = 1L;

	/** Any settings which affect the behavior of this JoshText. */
	public static class Settings
	{
		/** True if the tab character is used, false to use spaces */
		public static boolean indentUseTabs = true;
		/**
		 * The size with which tab characters are represented, in spaces
		 * (characters)
		 */
		public static int indentSizeInSpaces = 8;
		/** The string which will be inserted into the code for indentation. */
		public static String indentRepString = "\t";
		/** True if backspace should clear indentation to tab marks */
		public static boolean smartBackspace = true;
		/** True if the caret's line is to be highlighted */
		public static boolean highlight_line = true;
		/** True if tabs should be represented visually. */
		public static boolean renderTabs;
	}

	// Components
	/** The code contained in this JoshText; an array of lines. */
	Code code;
	/** Information about what is selected. */
	Selection sel;
	/** Information about our caret position */
	Caret caret;
	/** The listener that will handle text drag-and-drop. */
	DragListener dragger;

	/** File chooser used for save/load dialogue */
	public CustomFileChooser fc;
	
	/** The TokenMarker that will be polled for character formatting. */
	private TokenMarker marker;
	/**
	 * All Highlighters which will be called to highlight their lines or
	 * characters.
	 */
	public ArrayList<Highlighter> highlighters = new ArrayList<Highlighter>();

	// Dimensions
	/** The width of the largest UTF-8 character our font contains. */
	private int monoAdvance;
	/** The height of the largest UTF-8 character our font contains. */
	private int lineHeight;
	/**
	 * The largest height above the base line of any UTF-8 character our font
	 * contains.
	 */
	private int lineAscent;
	/** The distance we need to keep between the baselines of each line of text. */
	private int lineLeading;

	/** Our longest row, and how many other rows are this long */
	private int maxRowSize; // This is the size of the longest row, not the
	// index.

	/**
	 * A queue of all messages that need displayed in our status bar.
	 * There will probably only be one item on this queue at a time.
	 */
	Queue<String> infoMessages = new LinkedList<String>();

	/** Find and Replace Navigator; eg, QuickFind. */
	public FindNavigator finder;

	/**
	 * A Highlighter is a class that gets painted before the text
	 * so as to appear in the background of the characters.
	 */
	public static interface Highlighter
	{
		/**
		 * Called when it is time to render any and all backgrounds for this Highlighter.
		 * 
		 * @param g
		 *            The graphics object to paint to.
		 * @param i
		 *            The insets of the canvas.
		 * @param gm
		 *            The string and glyph metrics for this code.
		 * @param line_start
		 *            The index of the first visible line.
		 * @param line_end
		 *            The index of the last visible line.
		 */
		void paint(Graphics g, Insets i, CodeMetrics gm, int line_start, int line_end);
	}

	/**
	 * An interface for passing glyph and string metrics associated with this
	 * editor.
	 */
	public static interface CodeMetrics
	{
		/**
		 * Get the width of a particular string from its start to a given end
		 * position.
		 * 
		 * @param str
		 *            The string whose width will be returned.
		 * @param end
		 *            The index of the last character to consider in calculating
		 *            the width.
		 * @return The width of the given range of characters in the given
		 *         string.
		 */
		int stringWidth(String str, int end);

		/**
		 * Get the width of the line of code with a given index from its start
		 * to a given end position.
		 * 
		 * @param line
		 *            The index of the line whose width will be returned.
		 * @param end
		 *            The index of the last character to consider in calculating
		 *            the width.
		 * @return The width of the given range of characters in the given
		 *         string.
		 */
		int lineWidth(int line, int end);

		/**
		 * Get the width of each glyph rendered, which will be the width of the
		 * largest glyph in the UTF-8 character set.
		 * 
		 * @return Returns the width each glyph is given.
		 */
		int glyphWidth();

		/**
		 * @return Returns the height given to each line.
		 */
		int lineHeight();
	}

	/** Our own code metric information. */
	CodeMetrics metrics = new CodeMetrics()
	{
//r@Override
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

	//r@Override
		public int lineWidth(int y, int end)
		{
			return stringWidth(code.getsb(y).toString(),end);
		}

	//r@Override
		public int glyphWidth()
		{
			return monoAdvance;
		}

	//r@Override
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
		/** This character is some generic symbol. */
		public static final int NONE = 0;
		/**
		 * This character is a word char, which includes underscores and
		 * numerals.
		 */
		public static final int WORD = 1;
		/** This character is whitespace. */
		public static final int WHITE = 2;
	}

	/** An array of character types by their ordinal. */
	private static final char chType[] = new char[256];
	static
	{
		for (int i = 0; i < 256; i++)
			chType[i] = ChType.NONE;

		for (int i = 'a'; i <= 'z'; i++)
			chType[i] = ChType.WORD;
		for (int i = 'A'; i <= 'Z'; i++)
			chType[i] = ChType.WORD;
		for (int i = '0'; i <= '9'; i++)
			chType[i] = ChType.WORD;
		chType['_'] = ChType.WORD;

		chType[' '] = ChType.WHITE;
		chType['\t'] = ChType.WHITE;
		chType['\r'] = ChType.WHITE;
		chType['\n'] = ChType.WHITE;
	}

	/** Code completion syntax descriptor. */
	SyntaxDesc myLang;

	/** Default constructor; delegates to JoshText(String[]). */
	public JoshText()
	{
		this(null);
	}

	/**
	 * Construct a new JoshText with some code given as a String[] of lines.
	 * 
	 * @param lines
	 *            An array of Strings; one String for each line.
	 */
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

		// The mapping of keystrokes and action names
		Bindings.readMappings(getInputMap());
		
		mapActions();

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

		if (Settings.highlight_line) highlighters.add(new Highlighter()
		{
	//r@Override
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
		highlighters.add(sel);

		BracketHighlighter bm = new BracketHighlighter();
		highlighters.add(bm);
		caret.addCaretListener(bm);
		caret.addCaretListener(new CaretListener()
		{
	//r@Override
			public void caretUpdate(CaretEvent e)
			{
				if (!mas.isRunning()) doShowCaret();
			}
		});

		fc = new CustomFileChooser("/org/lateralgm","LAST_GAMEINFO_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setFileFilter(new CustomFileFilter(
				Messages.getString("JoshText.TYPE_TXT"),".txt")); //$NON-NLS-1$ //$NON-NLS-2$
		
		doCodeSize(true);
	}

	/**
	 * Set the contents of this editor from an array of strings.
	 * 
	 * @param lines
	 *            An array of Strings making up the code, with one String per
	 *            line.
	 */
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

	/**
	 * Get the text in this editor as a String[].
	 * 
	 * @return Return the text in this editor as an array of strings,
	 *         with one element in the array for each line.
	 */
	public String[] getLines()
	{
		String res[] = new String[code.size()];
		for (int i = 0; i < code.size(); i++)
			res[i] = code.get(i).sbuild.toString();
		return res;
	}

	/**
	 * Get the number of lines in the current code..
	 * 
	 * @return The number of lines in the current code.
	 */
	public int getLineCount()
	{
		return code.size();
	}

	/**
	 * Get the text in this editor as a String.
	 * 
	 * @return Returns the text in this editor as a single string.
	 */
	public String getText()
	{
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < code.size(); i++)
			res.append(code.get(i).sbuild.toString() + "\n");
		return res.toString();
	}

	/**
	 * Basically like htmlSpecialChars in PHP.
	 * 
	 * @param x The not HTML-ready string.
	 * @return The HTML-ready string.
	 */
	public static String htmlSpecialChars(String x)
	{
		if (x == null) return "";
		x = x.replace("/","&#47;").replace("\\","&#92;");
		x = x.replace("&","&amp;").replace("\"","&quot;");
		x = x.replace("<","&lt;").replace(">","&gt;");
		return x;
	}

	/**
	 * Export the current code as HTML.
	 * 
	 * @return The contents of the editor, with tokens marked up in HTML.
	 */
	public String getHTML()
	{
		StringBuilder res = new StringBuilder(code.size() * 100);

		for (int i = 0; i < code.size(); i++)
		{
			Line l = code.get(i);
			StringBuilder lsb = l.sbuild;
			int from = 0;
			ArrayList<TokenMarkerInfo> tmall = marker.getStyles(l);
			for (TokenMarkerInfo ti : tmall)
			{
				if (ti.startPos > from) res.append(htmlSpecialChars(lsb.substring(from,ti.startPos)));
				if (ti.startPos < ti.endPos)
				{
					res.append("<span style=\"");
					if ((ti.fontStyle & Font.BOLD) != 0) res.append("font-weight:bold;");
					if ((ti.fontStyle & Font.ITALIC) != 0) res.append("font-style:italic;");
					if (ti.color != null)
						res.append("color:#" + Integer.toHexString(ti.color.getRGB()).substring(2) + ";");
					res.append("\">");
					res.append(htmlSpecialChars(lsb.substring(ti.startPos,ti.endPos)));
					res.append("</span>");
				}
				from = ti.endPos;
			}
			res.append("\n");
		}

		return res.toString();
	}

	/**
	 * Applies a TokenMarker that will be polled for character formatting
	 * 
	 * @param tm
	 *            the TokenMarker to apply
	 */
	public void setTokenMarker(TokenMarker tm)
	{
		if (marker != null) removeLineChangeListener(marker);
		marker = tm;
		addLineChangeListener(marker);
		fireLineChange(0,code.size());
	}

	public void loadFromFile()
	{
		fc.setDialogTitle(Messages.getString("JoshText.LOAD_TITLE")); //$NON-NLS-1$
		while (true)
		{
			if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
			if (fc.getSelectedFile().exists()) break;
			JOptionPane.showMessageDialog(null,
					fc.getSelectedFile().getName() + Messages.getString("JoshText.FILE_MISSING"), //$NON-NLS-1$
					Messages.getString("JoshText.LOAD_TITLE"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
		}
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));
			code.clear();
			
	    String line = br.readLine();
	    try {
	      while (line != null){
          code.add(line);
          line = br.readLine();
	      }
	    }
	    finally {
	      br.close();
	    }
			fireLineChange(0,0);
			doCodeSize(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void saveToFile()
	{
		fc.setDialogTitle(Messages.getString("JoshText.SAVE_TITLE")); //$NON-NLS-1$
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		String name = fc.getSelectedFile().getPath();
		if (CustomFileFilter.getExtension(name) == null) name += ".txt"; //$NON-NLS-1$
		try
		{
		  BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()));

      try {
        for (int i = 0; i < code.size(); i++) {
           bw.write(code.getsb(i).toString() + "\n");
        }
      }
      finally {
        bw.close();
      }

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
		// ==========================================================
		// == Map action names to their implementations =============
		// ==========================================================
		/** Delete the current line, including the newline character. */
		public void LineDel()
		{
			// delete the line where the caret is
		}
	
		/** Duplicate the current line, placing the copy beneath this one. */
		public void LineDup()
		{
			UndoPatch up = new UndoPatch();
			up.realize(up.startRow + sel.duplicate());
			storeUndo(up,OPT.DUPLICATE);
		}
		
		/** Swap the currently selected lines, or this line and the line above it. */
		public void LineSwap()
		{
			if (caret.row == sel.row)
			{
				if (caret.row == 0) return;
				UndoPatch up = new UndoPatch(caret.row - 1,caret.row);
				StringBuilder swb = code.getsb(caret.row - 1);
				code.get(caret.row - 1).sbuild = code.get(caret.row).sbuild;
				code.get(caret.row).sbuild = swb;
				up.realize(caret.row);
				storeUndo(up,OPT.SWAP);
				if (sel.type != ST.RECT) sel.col = caret.col = line_offset_from(caret.row,caret.colw);
			}
			else
			{
				UndoPatch up = new UndoPatch();
				int srow = Math.min(sel.row,caret.row), erow = Math.max(sel.row,caret.row);
				StringBuilder swb = code.getsb(srow);
				for (int i = srow; i < erow; i++)
					code.get(i).sbuild = code.get(i + 1).sbuild;
				code.get(erow).sbuild = swb;
				up.realize(erow);
				if (sel.type != ST.RECT) sel.col = caret.col = line_offset_from(caret.row,caret.colw);
				storeUndo(up,OPT.SWAP);
			}
		}
		
		/** Un-swap the selected lines, or this line and the line below it. */
		public void LineUnSwap()
		{
			if (caret.row == sel.row)
			{
				if (caret.row >= code.size() - 1) return;
				UndoPatch up = new UndoPatch(caret.row,caret.row + 1);
				StringBuilder swb = code.getsb(caret.row + 1);
				code.get(caret.row + 1).sbuild = code.get(caret.row).sbuild;
				code.get(caret.row).sbuild = swb;
				up.realize(caret.row);
				storeUndo(up,OPT.UNSWAP);
				if (sel.type != ST.RECT) sel.col = caret.col = line_offset_from(caret.row,caret.colw);
			}
			else
			{
				UndoPatch up = new UndoPatch();
				int srow = Math.min(sel.row,caret.row), erow = Math.max(sel.row,caret.row);
				StringBuilder swb = code.getsb(erow);
				for (int i = erow; i > srow; i--)
					code.get(i).sbuild = code.get(i - 1).sbuild;
				code.get(srow).sbuild = swb;
				up.realize(erow);
				storeUndo(up,OPT.UNSWAP);
				if (sel.type != ST.RECT) sel.col = caret.col = line_offset_from(caret.row,caret.colw);
			}
		}

		/**
	 	* Select all: Place the caret at the end of the code,
	 	* and start the selection from the beginning.
	 	*/
		public void SelectAll()
		{
			sel.row = sel.col = 0;
			caret.row = code.size() - 1;
			caret.col = code.getsb(caret.row).length();
			sel.type = ST.NORM;
			sel.selectionChanged();
			repaint();
		}
		
		/** Open a file chooser to save the contents of the editor. */
		public void Save()
		{
			saveToFile();
		}
		
		/** Open a dialog to load the contents of the editor from a file. */
		public void Load()
		{
			loadFromFile();
			repaint();
		}

		/** Open a print dialogue to print the contents of the editor. */
		public void Print()
		{
		  //TODO: Make the fucker actually prints
		  PrinterJob pj = PrinterJob.getPrinterJob();
	    if (pj.printDialog()) {
	        try {
	          pj.print();
	        }
	        catch (PrinterException exc) {
	          System.out.println(exc);
	        }
	     }   
		}
		
		/** Copy the contents of the selection to the clipboard. */
		public void Copy()
		{
			sel.copy();
		}

		/**
	 	* Cut the contents of the selection, removing them from
	 	* the code and storing them in the clipboard.
	 	*/
		public void Cut()
		{
			if (sel.isEmpty()) return;
			sel.copy();
			UndoPatch up = new UndoPatch();
			sel.deleteSel();
			up.realize(Math.max(caret.row,sel.row));
			storeUndo(up,OPT.DELETE);
			doCodeSize(true);
			repaint();
		}
		
		/**
		* Paste the clipboard into the code, overwriting the selection if there is
	 	* one.
	 	*/
		public void Paste()
		{
			UndoPatch up = new UndoPatch(Math.min(caret.row,sel.row),Math.max(
					Math.max(caret.row,sel.row),
					Math.min(code.size() - 1,Math.min(caret.row,sel.row) + sel.getPasteRipple() - 1)));
			up.realize(sel.paste());
			storeUndo(up,OPT.PASTE);
			doCodeSize(true);
			repaint();
		}

		/** Undo the most recent action. */
		public void Undo()
		{
			undo();
			doCodeSize(true);
			repaint();
		}
	
		/** Redo the most recently undone action. */
		public void Redo()
		{
			redo();
			doCodeSize(true);
			repaint();
		}

		/** Display the find dialog. */
		public void ShowFind()
		{
			FindDialog.getInstance().selectedJoshText = this;
			findDialog.setVisible(true);
		}

		/** Display the quick find dialog. */
		public void ShowQuickFind()
		{
			finder.present();
		}

		/** Decrease the indent for all selected lines. */
		/*
		public void aUnindent(ActionEvent e)
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
		}*/
		
		public AbstractAction actCut = new AbstractAction("CUT")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			Cut();
		}
		};
		
		public AbstractAction actCopy = new AbstractAction("COPY")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			Copy();
		}
		};
		
		public AbstractAction actPaste = new AbstractAction("PASTE")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			Paste();
		}
		};
		
		public AbstractAction actUndo = new AbstractAction("UNDO")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			Undo();
		}
		};
		
		public AbstractAction actRedo = new AbstractAction("REDO")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			Redo();
		}
		};
		
		public AbstractAction actFind = new AbstractAction("FIND")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			ShowFind();
		}
		};
		
		public AbstractAction actQuickFind = new AbstractAction("QUICKFIND")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			ShowQuickFind();
		}
		};
		
		public AbstractAction actLineDel = new AbstractAction("LINEDEL")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			LineDel();
		}
		};
		
		public AbstractAction actLineDup = new AbstractAction("LINEDUP")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			LineDup();
		}
		};
		
		public AbstractAction actLineSwap = new AbstractAction("LINESWAP")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			LineSwap();
		}
		};
		
		public AbstractAction actLineUnSwap = new AbstractAction("LINEUNSWAP")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			LineUnSwap();
		}
		};
		
		public AbstractAction actSelAll = new AbstractAction("SELALL")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			SelectAll();
		}
		};
		
		public AbstractAction actPrint = new AbstractAction("PRINT")
		{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e)
		{
			Print();
		}
		};

		private void mapActions()
			{
			ActionMap am = getActionMap();
			Action acts[] = { actLineDel,actLineDup,actLineSwap,actLineUnSwap,actSelAll,actCopy,actCut,
					actPaste,actUndo,actRedo,actFind,actQuickFind,actPrint };
			InputMap map = getInputMap();
			KeyStroke[] strokes = map.allKeys();
			for (Action a : acts) {
				for (KeyStroke ks : strokes) {
					if (map.get(ks).equals(a.getValue(Action.NAME))) {
						a.putValue(Action.ACCELERATOR_KEY, ks);
					}
				}
				am.put(a.getValue(Action.NAME),a);
			}
			
			}
		
	/** The global find dialog. */
	FindDialog findDialog = FindDialog.getInstance();
	
	/**
	 * Adds a unit of indentation to the beginning of a given row.
	 * 
	 * @param row
	 *            The index of the row to indent.
	 */
	void indent(int row) {		
		code.getsb(row).insert(0,Settings.indentRepString);
	}
	
	/**
	 * Removes a unit of indentation from the beginning of a given row.
	 * 
	 * @param row
	 *            The index of the row to unindent.
	 */
	void unindent(int row)
	{
		StringBuilder sb = code.get(row).sbuild;
		int wc = 0, cc = 0;
		// Calculate the number of cells by which this line appears to be indented
		while (cc < sb.length() && Character.isWhitespace(sb.charAt(cc)))
		{
			if (sb.charAt(cc++) == '\t')
				wc = ((wc + Settings.indentSizeInSpaces) / Settings.indentSizeInSpaces) * Settings.indentSizeInSpaces;
			else
				wc++;
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

	/** @see javax.swing.JComponent#addNotify() */
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
	 * single character or a keycode name from the <code>KeyEvent</code> class,
	 * without the <code>VK_</code> prefix.
	 * 
	 * @param s
	 *            A string description of the key stroke.
	 * @return The resulting composed KeyStroke.
	 * @throws IllegalArgumentException
	 *             if the key cannot be parsed for some reason.
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

	/**
	 * Fit this component so that it can contain all code, but is not sized
	 * excessively.
	 */
	void fitToCode()
	{
		int insetY = lineLeading;
		int w = (maxRowSize + 1) * monoAdvance + getInsets().left + 1; // extra
		// char
		// +
		// pixel
		// for
		// overwrite
		// caret
		int h = code.size() * lineHeight + insetY;
		setMinimumSize(new Dimension(w,h));
		setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		fireResize();
		repaint();
	}

	/**
	 * Calculate the size of the code, and optionally fit the window to it.
	 * 
	 * @param rs
	 *            Whether or not to resize the window to these new dimensions.
	 */
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

	/** Adjust the view such that it contains the caret. */
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

	/**
	 * @param point The new mouse point.
	 */
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

	/** Stop the auto-scroll mechanism. */
	void removeMouseAutoScroll()
	{
		mas.stop();
	}

	/** Class for handling the auto scroll. */
	class MouseAutoScroll
	{
		/** Rate Point. The velocity of the auto scroll. */
		Point rp;
		/** The viewport. */
		JViewport p;
		/** True if the autoscroll is active, false otherwise. */
		private boolean running = false;

		/** Default constructor. Creates a timer. */
		MouseAutoScroll()
		{
			new Timer().scheduleAtFixedRate(doMouseAutoScroll,100,100);
		}

		/** Start the scroll mechanism. */
		void start()
		{
			running = true;
		}

		/** Stop the scroll mechanism. */
		void stop()
		{
			running = false;
		}

		/**
		 * Test if the scroll mechanism is active.
		 * 
		 * @return Whether the scroll mechanism is active.
		 */
		boolean isRunning()
		{
			return running;
		}

		/** Our timer task callback */
		TimerTask doMouseAutoScroll = new TimerTask()
		{
			@Override
			public void run()
			{
				if (!running) return;
				Point po = p.getViewPosition();
				p.setViewPosition(new Point(po.x + rp.x * monoAdvance,po.y + rp.y * lineHeight));
				// doShowCaret();
				updateUI();
			}
		};
	}

	/** Our local auto scroll mechanism. */
	private MouseAutoScroll mas = new MouseAutoScroll();

	/**
	 * Get the width of a particular line up to a given position.
	 * 
	 * @param l
	 *            The index of the line in question.
	 * @param pos
	 *            The number of characters from the start of the line to
	 *            consider in the width.
	 * @return The width of the specified portion of the line with the given
	 *         index.
	 */
	public int line_wid_at(int l, int pos)
	{
		return metrics.stringWidth(code.getsb(l).toString(),pos);
	}

	/**
	 * Returns the index of the last character in the line with the given index
	 * which fits in the given width.
	 * 
	 * @param line
	 *            The line to get the offset in.
	 * @param wid
	 *            The width to be translated to a character index.
	 * @return Returns the given width, translated to a character index.
	 */
	public int line_offset_from(int line, int wid)
	{
		int ret;
		String l = code.getsb(line).toString();
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

	/**
	 * Takes the nth character in the line with the given line index
	 * and computes the column at which that character will be rendered.
	 * 
	 * @param line
	 *            The index of the line in question.
	 * @param n
	 *            The index of the character whose column will be returned.
	 * @return The column number of the nth character on the line.
	 */
	public int index_to_column(int line, int n)
	{
		int col = 0;
		StringBuilder l = code.getsb(line);
		n = Math.min(n,l.length());
		for (int i = 0; i < n; i++)
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

	/**
	 * Takes a column number and a line with the given index, and returns the
	 * index of the character in that line which will render at that column.
	 * If the line does not reach the given column, then the length of the
	 * line is returned.
	 * 
	 * @param line
	 *            The line index in question.
	 * @param col
	 *            The column the index of character at which will be returned.
	 * @return The index of the character in the given line that renders at the
	 *         given column.
	 */
	public int column_to_index(int line, int col)
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

	/**
	 * Takes a column number and a line with the given index, and returns the
	 * index of the character in that line which will render at that column.
	 * If the line does not reach the given column, the result is the sum of
	 * the length of the line and the number of columns between the column
	 * index of the end of the line and the given column. This means that the
	 * behavior is the same as it would be if the line were suffixed with an
	 * infinite number of spaces.
	 * 
	 * @param line
	 *            The line index in question.
	 * @param col
	 *            The column the index of character at which will be returned.
	 * @return The index of the character in the given line that renders at the
	 *         given column.
	 */
	public int column_to_index_unsafe(int line, int col)
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

	/**
	 * @param line
	 *            The index of the line to check in.
	 * @param col
	 *            The column to look at inside the line.
	 * @return Returns whether the given column in the line with the given
	 *         index lies within a tab character.
	 */
	public boolean column_in_tab(int line, int col)
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

	/**
	 * Get the type of the character at the given position in the given
	 * character sequence.
	 * 
	 * @param str
	 *            The character sequence in question.
	 * @param pos
	 *            The position of the character whose type is returned
	 * @return Returns the type of the character.
	 */
	public static int selGetKind(CharSequence str, int pos)
	{
		if (!(pos >= 0 && pos < str.length())) return ChType.WHITE;
		int ohfukku = str.charAt(pos);
		if (ohfukku > 255) return ChType.WORD;
		return chType[ohfukku];
	}

	/**
	 * Basically, returns whether selGetKind(str, pos) == otype.
	 * 
	 * @param str
	 *            The character sequence in question.
	 * @param pos
	 *            The position of the character whose type is checked.
	 * @param otype
	 *            The original type against which this character will be
	 *            matched.
	 * @return Returns whether the type of the described character matches the
	 *         given original type.
	 */
	public static boolean selOfKind(CharSequence str, int pos, int otype)
	{
		if (!(pos >= 0 && pos < str.length())) return otype == ChType.WHITE;
		int ohfukku = str.charAt(pos);
		if (ohfukku > 255) return otype == ChType.WORD;
		return chType[ohfukku] == otype;
	}

	/**
	 * Get font metric information for the active font.
	 * 
	 * @return Returns the FontMetrics of the current font.
	 */
	public FontMetrics getFontMetrics()
	{
		return getFontMetrics(getFont());
	}

	/**
	 * Get the largest dimensions of any glyph that will be rendered with the
	 * current font.
	 * 
	 * @return Returns a Dimension representing the largest sizes glyphs in this
	 *         font attain.
	 */
	public Dimension getMaxGlyphSize()
	{
		return new Dimension(monoAdvance,lineHeight);
	}

	/**
	 * Draw the given characters with full highlighting.
	 * 
	 * @param g
	 *            The graphics object to render to.
	 * @param a
	 *            The characters to render.
	 * @param sp
	 *            The starting position in the given array.
	 * @param ep
	 *            The ending position in the given array.
	 * @param xx
	 *            The x coordinate at which to start render.
	 * @param ty
	 *            The y coordinate at which to start render
	 * @return The new x coordinate from which to render.
	 */
	private int drawChars(Graphics g, char[] a, int sp, int ep, int xx, int ty)
	{
		Color c = g.getColor();
		for (int i = sp; i < ep; i++)
		{
			if (a[i] == '\t')
			{
				final int incby = Settings.indentSizeInSpaces * monoAdvance, xxp = xx;
				xx = ((xx + incby) / incby) * incby;
				if (Settings.renderTabs)
				{
					g.setColor(new Color(255,0,0));
					g.drawLine(xxp + 2,ty - (lineHeight / 3),xx - 2,ty - (lineHeight / 3));
					g.drawLine(xxp + 2,ty - (lineHeight / 3) - (lineHeight / 5),xxp + 2,ty - (lineHeight / 3)
							+ (lineHeight / 5));
					g.drawLine(xx - 2,ty - (lineHeight / 3) - (lineHeight / 5),xx - 2,ty - (lineHeight / 3)
							+ (lineHeight / 5));
					g.setColor(c);
				}
				continue;
			}
			g.drawChars(a,i,1,xx,getInsets().top + ty);
			xx += monoAdvance;
			g.setColor(c);
		}
		return xx;
	}

	/** This is the font for which we have cached derived fonts. This prevents drawing with stale fonts. */
	private Font baseFont = null;
	/** This is a map of our derived fonts by the flags with which they were derived from the base font. */
	private HashMap<Integer,Font> specialFonts = new HashMap<Integer,Font>();

	/**
	 * Paint the line with the given index to the given Graphics object,
	 * starting
	 * at the given y-coordinate.
	 * 
	 * @param g
	 *            The graphics object to paint to.
	 * @param lineNum
	 *            The index of the line to render.
	 * @param ty
	 *            The y-coordinate at which to render the baseline.
	 */
	private void drawLine(Graphics g, int lineNum, int ty)
	{
		if (baseFont != getFont())
		{
			baseFont = getFont();
			specialFonts.clear();
		}
		g.setColor(getForeground());
		Font drawingFont = baseFont;
		g.setFont(drawingFont);
		int fontFlags = 0;

		StringBuilder line = code.getsb(lineNum);
		int xx = 1 + getInsets().left;
		char[] a = line.toString().toCharArray();
		Color c = g.getColor();

		if (marker == null)
		{
			drawChars(g,a,0,a.length,xx,ty);
		}
		else
		{
			ArrayList<TokenMarkerInfo> tmall = marker.getStyles(code.get(lineNum));
			int pos = 0;
			for (TokenMarkerInfo tm : tmall)
			{
				// Start by printing normal characters until we reach
				// styleBlock.startPos
				xx = drawChars(g,a,pos,tm.startPos,xx,ty);
				// Print the remaining characters in the styleBlock range
				g.setColor(tm.color != null ? tm.color : c);

				fontFlags = tm.fontStyle;
				if (specialFonts.containsKey(fontFlags))
					drawingFont = specialFonts.get(fontFlags);
				else
				{
					drawingFont = baseFont.deriveFont(fontFlags);
					specialFonts.put(fontFlags,drawingFont);
				}
				g.setFont(drawingFont);

				xx = drawChars(g,a,tm.startPos,tm.endPos,xx,ty);
				pos = tm.endPos;
				g.setFont(baseFont);
				g.setColor(c);
			}
		}
	}

	/** @param g The graphics object to which to paint. */
	@Override
	public void paintComponent(Graphics g)
	{
		Object map = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //$NON-NLS-1$
		if (map != null) ((Graphics2D) g).addRenderingHints((Map<?,?>) map);
		
		Rectangle clip = g.getClipBounds();

		// Fill background
		g.setColor(getBackground());
		g.fillRect(clip.x,clip.y,clip.width,clip.height);

		for (Highlighter a : highlighters)
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
	 * A convenience repaint method which can convert a row/col pair into x/y
	 * space.
	 * 
	 * @param col
	 *            The column at which to start the repaint.
	 * @param row
	 *            The row at which to start the repaint.
	 * @param w
	 *            The width of the region to repaint.
	 * @param h
	 *            The height of the region to repaint.
	 * @param convert
	 *            True to convert row/col into x/y space. False to treat as x/y.
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

	// Input handling

	/**
	 * Translates a mouse coordinate to a text coordinate (y = row, x = col).
	 * 
	 * @param m
	 *            The mouse coordinates to translate.
	 * @param bound
	 *            Whether the coordinate should be trimmed to a valid column.
	 * @return A point representing the text coordinate, where Point.x is the
	 *         column,
	 *         and point.y is the row in the code.
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

	/**
	 * Handle a mouse event, such as clicks, double clicks, drags, for
	 * selection,
	 * scrolling, and general mouse-related code manipulation.
	 * 
	 * @param e
	 *            The mouse event to handle.
	 */
	protected void handleMouseEvent(MouseEvent e)
	{
		// / FIXME: Double-click-and-drag is supposed to wrap the start and
		// / end caret positions to word endings.

		if (e.getID() == MouseEvent.MOUSE_PRESSED) requestFocusInWindow();
		// if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0
		// && (e.getID() == MouseEvent.MOUSE_PRESSED)) dragger.query();

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
			else if (e.getID() == MouseEvent.MOUSE_PRESSED) sel.special.valid = false;

			updateMouseAutoScroll(e.getPoint());

			if (sel.special.valid) sel.special.adjust();

			// cleanup (deselect, flash, repaint)
			if (!sel.special.valid
					&& (e.getModifiers() & Event.SHIFT_MASK) == 0
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
			UndoPatch up = new UndoPatch(p.y,Math.min(code.size() - 1,p.y + sel.getMiddlePasteRipple()
					- 1));
			up.cbefore.stype = sto;
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

	/** Handle most mouse events by delegating to super. */
	@Override
	protected void processMouseEvent(MouseEvent e)
	{
		super.processMouseEvent(e);
		handleMouseEvent(e);
	}

	/** Handle mouse movement by delegating to super. */
	@Override
	protected void processMouseMotionEvent(MouseEvent e)
	{
		super.processMouseMotionEvent(e);
		handleMouseEvent(e);
	}

	/**
	 * Handle when the user types a key.
	 * @param e The key type event.
	 */
	protected void processKeyTyped(KeyEvent e)
	{
		final Point sc = new Point(caret.col,caret.row);
		switch (e.getKeyChar())
		{
			case KeyEvent.VK_ENTER:
				if (sel.type == Selection.ST.NORM)
				{
					UndoPatch up = new UndoPatch();
					sel.deleteSel();
					StringBuilder nr = code.getsb(caret.row);

					int offset = 0;
					StringBuilder ins = new StringBuilder();

					for (int i = 0; i < nr.length() && i < caret.col; i++)
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
				}
				// RECT falls to here
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
								// We'll handle this by determining the smallest
								// word/pattern we
								// can control-backspace over, and running with it on
								// all lines.

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
				System.out.println("Tab");
				if (!sel.isEmpty())
				{
					UndoPatch up = new UndoPatch();
					String tab = Settings.indentRepString;
					int yx = Math.max(sel.row,caret.row);
					if (!e.isShiftDown()) for (int y = Math.min(sel.row,caret.row); y <= yx; y++)
						indent(y);
					else for (int y = Math.min(sel.row,caret.row); y <= yx; y++)
						unindent(y);
					sel.col += tab.length();
					caret.col += tab.length();
					up.realize(Math.max(sel.row,caret.row));
					storeUndo(up,OPT.INDENT);
					break;
				}
				UndoPatch up = new UndoPatch();
				if (!e.isShiftDown())
					sel.insert(Settings.indentRepString);
				else {
					unindent(caret.row);
					int P = 0;
					while (P < code.getsb(caret.row).length()
							&& Character.isWhitespace(code.getsb(caret.row).charAt(P)))
						P++;
					caret.colw = caret.col = sel.col = P;
				}
				up.realize(caret.row);
				storeUndo(up,OPT.TYPED);
				break;
			case '\u0018': // cancel (not sure why it's VK_FINAL instead of
				// VK_CANCEL)
			case KeyEvent.VK_ESCAPE: // escape (in paramString, this is \u001B,
				// which is VK_ESCAPE
			case KeyEvent.CHAR_UNDEFINED:
				// these cases are taken from KeyEvent.paramString
				break;
			default:
				if (e.isControlDown() || e.isAltDown())
				{
					switch (e.getKeyCode())
					{
					// Handle bindings. Usually this is handled by registering key
					// bindings,
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
		// doShowCursor();
	}

	/**
	 * Test if a string comprises only whitespace characters.
	 * @param str String to test.
	 * @return Whether the string is entirely whitespace.
	 */
	private static boolean all_white(String str)
	{
		for (int i = 0; i < str.length(); i++)
			if (!Character.isWhitespace(str.charAt(i))) return false;
		return true;
	}

	/**
	 * Process a generic key press.
	 * @param e The key press event.
	 */
	protected void processKeyPressed(KeyEvent e)
	{
		// Note to developers: please consume keys that you use.
		// This way, containers don't still see them as usable
		// (e.g. arrow keys triggering the scrollbar)
		Point sc = new Point(caret.col,caret.row);
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_INSERT:
				caret.insert ^= true;
				e.consume();
				break;
			case KeyEvent.VK_LEFT:
				int otype = selGetKind(code.getsb(caret.row),caret.col - 1);
				int moved = 0;
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
					if (moved == 0 && !selOfKind(code.getsb(caret.row),caret.col - 1,otype))
						otype = selGetKind(code.getsb(caret.row),caret.col - 1);
					++moved;
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
				moved = 0;
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
					if (moved == 0 && !selOfKind(code.getsb(caret.row),caret.col,otype))
						otype = selGetKind(code.getsb(caret.row),caret.col);
					++moved;
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
				caret.colw = P;
				caret.col = P;
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
				// FIXME: If parent is viewport, also scroll that a screenfull
				if (!e.isShiftDown()) sel.deselect(true);
				e.consume();
				break;
			case KeyEvent.VK_PAGE_DOWN:
				height = (getParent() instanceof JViewport) ? getParent().getHeight() : getHeight();
				caret.row = Math.min(code.size() - 1,caret.row + height / lineHeight);
				if (sel.type != ST.RECT) caret.col = Math.min(caret.col,code.getsb(caret.row).length());
				// FIXME: If parent is viewport, also scroll that a screenfull
				if (!e.isShiftDown()) sel.deselect(true);
				e.consume();
				break;
		}
		if (sc.x != caret.col || sc.y != caret.row)
		{
			caret.positionChanged();
			sel.selectionChanged();
		}
		fitToCode();
	}

	/** Handle a component key event; a type or a generic press. */
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

	// Line Change Listeners
	/**
	 * A LineChange is invoked whenever characters are added/removed
	 * from lines, whether the line exists or is created. For only
	 * listening to whether lines are added/removed, use Code.CodeListener.
	 */
	public interface LineChangeListener extends EventListener
	{
		/**
		 * @param code The code that was changed.
		 * @param start The index of the first line changed.
		 * @param end The index of the last line changed.
		 */
		void linesChanged(Code code, int start, int end);
	}

	/** @param listener The listener to add. */
	public void addLineChangeListener(LineChangeListener listener)
	{
		listenerList.add(LineChangeListener.class,listener);
	}

	/** @param listener The listener to remove. */
	public void removeLineChangeListener(LineChangeListener listener)
	{
		listenerList.remove(LineChangeListener.class,listener);
	}

	/** Fires a line change event to all listeners.
	 * @param start The first line changed.
	 * @param end   The last line changed.  */
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
				((LineChangeListener) listeners[i + 1]).linesChanged(code,start,end);
			}
	}

	// Inner classes (mostly drag and drop)

	/**
	 * Listens for mouse events for the purposes of detecting drag gestures.
	 * BasicTextUI will maintain one of these per AppContext.
	 */
	class DragListener
	{
		/** True if a drag has been initiated. */
		private boolean dragStarted;
		/** Motion threshold before a drag is initiated. */
		private int motionThreshold;
		/** The event that sparked the drag. */
		private MouseEvent dndArmedEvent;

		/** Construct with default threshold. */
		public DragListener()
		{
			motionThreshold = getDefaultThreshold();
		}

		/**
		 * @return The system default motion threshold for beginning a drag.
		 */
		public int getDefaultThreshold()
		{
			Integer ti = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty(
					"DnD.gestureMotionThreshold");
			return ti == null ? 5 : ti.intValue();
		}

		/**
		 * Handle a mouse press.
		 * @param e The mouse event.
		 */
		public void mousePressed(MouseEvent e)
		{
			dragStarted = false;
			if (isDragPossible(e.getPoint()))
			{
				dndArmedEvent = e;
				e.consume();
			}
		}

		/**
		 * Handle a mouse release.
		 * @param e The mouse event.
		 */
		public void mouseReleased(MouseEvent e)
		{
			if (dragStarted) e.consume();
			dndArmedEvent = null;
		}

		/**
		 * Handle a mouse drag.
		 * @param e The mouse event.
		 */
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
				int act = e.isControlDown() ? TransferHandler.COPY : TransferHandler.MOVE;
				dragStarted = true;
				th.exportAsDrag(JoshText.this,dndArmedEvent,act);
				dndArmedEvent = null;
			}
			e.consume();
		}

		/** Determines if the press event is located over a selection.
		 * @param mousePt The Point representing the mouse.
		 * @return Whether the drag is possible; if the press event is located over a selection. */
		protected boolean isDragPossible(Point mousePt)
		{
			Point p = mouseToPoint(mousePt,false);
			return sel.contains(p.y,p.x);
		}

		/** Print debug info. */
		public void query()
		{
			System.out.println(dragStarted + "," + dndArmedEvent);
		}
	}

	/**
	 * @author IsmAvatar
	 * Class to handle drag and drop text in JoshEdit.
	 */
	class JoshTextTransferHandler extends TransferHandler
	{
		/** Shut up, ECJ. */
		private static final long serialVersionUID = 1L;

		/** Construct, establishing listeners. */
		public JoshTextTransferHandler()
		{
			addPropertyChangeListener("dropLocation",new PropertyChangeListener()
			{
		//r@Override
				public void propertyChange(PropertyChangeEvent pce)
				{
					repaintDropLocation(pce.getOldValue());
					repaintDropLocation(pce.getNewValue());
				}
			});
		}

		/**
		 * Repaint to reflect text drop.
		 * @param drop The object being dropped.
		 */
		public void repaintDropLocation(Object drop)
		{
			if (drop == null || !(drop instanceof DropLocation)) repaint();
			DropLocation loc = (DropLocation) drop;
			loc.getDropPoint();
		}

		/** @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent) */
		@Override
		public int getSourceActions(JComponent c)
		{
			return COPY_OR_MOVE;
		}

		/** @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent) */
		@Override
		protected Transferable createTransferable(JComponent c)
		{
			if (!(c instanceof JoshText)) return null;
			JoshText j = (JoshText) c;
			if (j.sel.isEmpty()) return null;
			return new StringSelection(j.sel.getSelectedTextForCopy());
		}

		/** Upon drag completion, if drag was MOVE, not COPY, delete the moved text. */
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

		/** Test if we can import a given draggable; ie, test if it's a string. */
		@Override
		public boolean canImport(TransferSupport info)
		{
			return info.isDataFlavorSupported(DataFlavor.stringFlavor);
			// if (info.isDrop()) dropPoint =
			// mouseToPoint(info.getDropLocation().getDropPoint(),true);
			// return true;
		}

		/** @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport) */
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
				up.reconstruct(p.y,Math.min(code.size() - 1,p.y + sel.getInsertRipple(data) - 1));
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

	// Scrollable

	/** @see javax.swing.Scrollable#getPreferredScrollableViewportSize() */
//r@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return new Dimension(320,240);
	}

	/** @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int) */
//r@Override
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

	/** @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int) */
//r@Override
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

	/** @see javax.swing.Scrollable#getScrollableTracksViewportHeight() */
//r@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	/** The scrollable should not track the viewport width. */
//r@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	/** Handle a resize. */
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

	/** Listen to parent component */
//r@Override
	public void componentResized(ComponentEvent e)
	{
		fireResize();
	}

	/** Repaint when covered partially by another window. */
//r@Override
	public void componentHidden(ComponentEvent e)
	{
		repaint();
	}

	/** @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent) */
//r@Override
	public void componentMoved(ComponentEvent e)
	{
		repaint();
	}

	/** @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent) */
//r@Override
	public void componentShown(ComponentEvent e)
	{
		repaint();
	}

	/** Be a clipboard owner and do not give a fuck. */
//r@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1)
	{
		// WHOGIVESAFUCK.jpg
	}

	// -----------------------------------------------------------------
	// ----- Mark Matching Brackets ------------------------------------
	// -----------------------------------------------------------------

	/**
	 * @author Josh Ventura
	 * The status of a match; there are three match states.
	 */
	static enum MatchState
	{
		/** No attempt is presently being made to match anything. */
		NOT_MATCHING,
		/** A match was attempted, but none was found. */
		NO_MATCH,
		/** A match was successfully located. */
		MATCHING
	}

	/**
	 * @author Josh Ventura
	 * A highlighter to mark pairs of matching brackets.
	 */
	class BracketHighlighter implements Highlighter,CaretListener
	{
		/** The state of our match. */
		MatchState matching;
		/** The line on which the match was found. */
		int matchLine;
		/** The column at which the match was found. */
		int matchPos;

		/** Paint a box around each bracket in the match. */
	//r@Override
		public void paint(Graphics g, Insets i, CodeMetrics gm, int line_start, int line_end)
		{
			Color c = g.getColor();

			//It should be assumed that line_start is smaller than line end
			//if (line_start > line_end) {
				//JOptionPane.showMessageDialog(null,"line start larger than line end!");
			//}
			
			//TODO: Make sure we haven't deleted a selection of code that fires a bracket repaint on a line
			//that was deleted. Current check suffices to fix the exception. - Robert
			if (matchLine < line_end) {
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
			}
			g.setColor(c);
		}

		/**
		 * @param m The current BracketMatch instance.
		 * @param x The column of the most recent match candidate.
		 * @param y The row of the most recent match candidate.
		 * @return True if the match candidate is indeed the match to the original bracket.
		 */
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

		/**
		 * @author Josh Ventura
		 * Class to match various brackets; parentheses, square brackets, braces.
		 */
		class BracketMatch
		{
			/** The character of the bracket to be matched. */
			char match;
			/** The character of the matching bracket; the opposite-side bracket. */
			char opposite;
			/** The number of nested brackets during this matching process. */
			short count;

			/**
			 * @param m The character of the bracket to be matched.
			 * @param o The character of the matching bracket; the opposite-side bracket.
			 * @param c The number of nested brackets during this matching process.
			 */
			public BracketMatch(char m, char o, short c)
			{
				match = m;
				opposite = o;
				count = c;
			}
		}

		/**
		 * @param row The row of the bracket to match.
		 * @param col The column of the bracket to match.
		 * @param match The BracketMatch instance for this match attempt.
		 */
		private void findMatchForward(int row, int col, BracketMatch match)
		{
			int y = row;
			int blockType = 0;
			StringBuilder sb = code.getsb(y);

			// Figure out what kind of block we're in, if any.
			ArrayList<TokenMarkerInfo> tmall = marker.getStyles(code.get(y));

			int offset;
			for (offset = 0; offset < tmall.size(); offset++)
			{
				TokenMarkerInfo tm = tmall.get(offset);
				if (col < tm.startPos) break; // The blocks have skipped us.
				if (col >= tm.startPos && col < tm.endPos)
				{
					blockType = tm.blockHash;
					break;
				}
			}
			if (subFindMatchForward(match,sb,tmall,offset,col,blockType,y)) return;

			for (y++; y < code.size(); y++)
			{
				tmall = marker.getStyles(code.get(y));
				if (subFindMatchForward(match,code.getsb(y),tmall,0,0,blockType,y)) return;
			}
		}

		/**
		 * @param match The BracketMatch class representing this search.
		 * @param sb The StringBuilder of the line to search.The position from which to continue searching.
		 * @param tmall All marker tokens for this line.
		 * @param offset The marker token index from which to continue searching.
		 * @param spos The position from which to continue searching.
		 * @param blockType The type of block in which we are interested.
		 * @param y Our row number.
		 * @return True if the match was found in this line, false otherwise.
		 *          Info about the match will be contained in the match parameter.
		 */
		private boolean subFindMatchForward(BracketMatch match, StringBuilder sb,
				ArrayList<TokenMarkerInfo> tmall, int offset, int spos, int blockType, int y)
		{
			int pos = spos;
			for (int i = offset; i < tmall.size(); i++)
			{
				TokenMarkerInfo tm = tmall.get(i);
				if (blockType == 0) // If our start wasn't in a block
					for (; pos < tm.startPos; pos++)
						// Check outside this block's range
						if (sb.charAt(pos) == match.match)
						{
							if (matchFound(match,pos,y)) return true;
						}
						else if (sb.charAt(pos) == match.opposite) match.count++;
				if (blockType == tmall.get(i).blockHash) // If the block has the
					// same type
					for (pos = Math.max(spos,tm.startPos); pos < tm.endPos; pos++)
						// Check inside it
						if (sb.charAt(pos) == match.match)
						{
							if (matchFound(match,pos,y)) return true;
						}
						else if (sb.charAt(pos) == match.opposite) match.count++;
				pos = tm.endPos;
			}
			return false;
		}

		/**
		 * @param row The row of the bracket to match.
		 * @param col The column of the bracket to match.
		 * @param match The BracketMatch instance for this match attempt.
		 */
		private void findMatchBackward(int row, int col, BracketMatch match)
		{
			int y = row;
			int blockType = 0;
			StringBuilder sb = code.getsb(y);

			// Figure out what kind of block we're in, if any.
			ArrayList<TokenMarkerInfo> tmall = marker.getStyles(code.get(y));

			int offset;
			for (offset = 0; offset < tmall.size(); offset++)
			{
				TokenMarkerInfo tm = tmall.get(offset);
				if (col < tm.startPos) break; // The blocks have skipped us.
				if (col >= tm.startPos && col < tm.endPos)
				{
					blockType = tm.blockHash;
					break;
				}
			}
			if (subFindMatchBackward(match,sb,tmall,offset,caret.col,blockType,y)) return;

			for (y--; y >= 0; y--)
			{
				tmall = marker.getStyles(code.get(y));
				if (subFindMatchBackward(match,code.getsb(y),tmall,tmall.size() - 1,code.getsb(y).length(),
						blockType,y)) return;
			}
		}

		/**
		 * @param match The BracketMatch class representing this search.
		 * @param sb The StringBuilder of the line to search.The position from which to continue searching.
		 * @param tmall All marker tokens for this line.
		 * @param offset The marker token index from which to continue searching.
		 * @param spos The position from which to continue searching.
		 * @param blockType The type of block in which we are interested.
		 * @param y Our row number.
		 * @return True if the match was found in this line, false otherwise.
		 *          Info about the match will be contained in the match parameter.
		 */
		private boolean subFindMatchBackward(BracketMatch match, StringBuilder sb,
				ArrayList<TokenMarkerInfo> tmall, int offset, int spos, int blockType, int y)
		{
			int pos = spos;
			int i = offset;
			TokenMarkerInfo tm = tmall.get(i);
			for (;;)
			{
				if (blockType == tmall.get(i).blockHash) // If the block has the
					// same type
					for (pos = Math.min(spos,tm.endPos - 1); pos >= tm.startPos; pos--)
						// Check inside it
						if (sb.charAt(pos) == match.match)
						{
							if (matchFound(match,pos,y)) return true;
						}
						else if (sb.charAt(pos) == match.opposite) match.count++;
				if (i > 0)
				{
					tm = tmall.get(--i);
					if (blockType == 0) // If our start wasn't in a block
						for (pos = tm.startPos - 1; pos >= tm.endPos; pos--)
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
						for (pos = tm.startPos - 1; pos >= 0; pos--)
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

		/** Callback for when the caret position changes. */
	//r@Override
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

	// -----------------------------------------------------------------
	// ----- Be Undoable -----------------------------------------------
	// -----------------------------------------------------------------

	/**
	 * A class of a dozen types an UndoPatch can have.
	 * 
	 * @author josh
	 */
	static final class OPT
	{
		/** The patch is a one-of-a-kind that isn't worth its own constant. */
		public static final int OTHER = 0;
		/** The patch contains something the user typed. */
		public static final int TYPED = 1;
		/** The patch is from backspacing over something. */
		public static final int BACKSPACE = 2;
		/** The patch is from deleting something. */
		public static final int DELETE = 3;
		/**
		 * The patch is from typing a space; 'space' gets its
		 * own type to allow changing merge behavior around it
		 */
		public static final int SPACE = 4;
		/** The patch is from a newline insertion. */
		public static final int ENTER = 5;
		/** The patch is from the 'paste' function. */
		public static final int PASTE = 6;
		/** The patch is from changing line indentation. */
		public static final int INDENT = 7;
		/** The patch is from the 'duplicate line' function. */
		public static final int DUPLICATE = 8;
		/** The patch is from a find-replace replace. */
		public static final int REPLACE = 9;
		/** The patch is from the 'swap lines' function. */
		public static final int SWAP = 10;
		/** The patch is from the 'unswap lines' function. */
		public static final int UNSWAP = 11;
	}

	/**
	 * A class for storing two patches of code used in the
	 * Undo and Redo mechanisms. An UndoPatch contains sufficient
	 * information to be undone and applied any number of times
	 * during its lifecycle, with perfect accuracy assuming each
	 * patch is applied in the same order it was created and reverted
	 * in the opposite. (And was, of course, constructed properly.)
	 * 
	 * @author josh
	 */
	class UndoPatch
	{
		/** The type of this UndoPatch; a member of {@link OPT} */
		int opTag;
		/**
		 * The text stored before the code was modified, hereafter
		 * referred to as the "pre-patch text."
		 */
		Line[] oldText;
		/**
		 * The text as it stood after the modifications that sparked
		 * the creation of this UndoPatch, hereafter just "patch text."
		 */
		Line[] patchText;
		/**
		 * The index of the row that begins both segments of text.
		 * In other words, both oldText and patchText must start at the same
		 * line.
		 */
		int startRow;

		/** Caret/selection information from before the action in this undo item. */
		CaretData cbefore = new CaretData();
		/** Caret/selection information for immediately after the action in this undo item. */
		CaretData cafter = new CaretData();

		/**
		 * Storage class for grabbing caret data and later replacing it.
		 */
		class CaretData
		{
			/** The stored caret.column. */
			public int ccol;
			/** The stored caret.row. */
			public int crow;
			/** The stored sel.column. */
			public int scol;
			/** The stored sel.row. */
			public int srow;
			/** The stored sel.type*/
			ST stype;

			/**
			 * Grab the current caret indexes for use later.
			 */
			public void grab()
			{
				ccol = caret.col;
				crow = caret.row;
				scol = sel.col;
				srow = sel.row;
				stype = sel.type;
			}

			/**
			 * Replace previously grabbed caret data, modifying this.caret.
			 */
			public void replace()
			{
				caret.col = ccol;
				caret.row = crow;
				sel.col = scol;
				sel.row = srow;
				sel.type = stype;
			}

			/**
			 * Copy data from another instance.
			 * @param cfrom The CaretData class to copy from.
			 */
			public void copy(CaretData cfrom)
			{
				ccol = cfrom.ccol;
				crow = cfrom.crow;
				scol = cfrom.scol;
				srow = cfrom.srow;
				stype = cfrom.stype;
			}
		}

		/**
		 * Construct a complete, finalized UndoPatch manually.
		 * 
		 * @param t
		 *            The pre-patch code.
		 * @param ot
		 *            The patch code.
		 * @param sr
		 *            The index of the starting row of the two codes.
		 */
		UndoPatch(Line[] t, Line[] ot, int sr)
		{
			oldText = ot;
			patchText = t;
			startRow = sr;
		}

		/**
		 * Prefixes a line to the stored pre-patch code, decrementing
		 * the recorded startRow to account for the change.
		 * 
		 * @param ln
		 *            The line to prefix to our stored pre-patch code.
		 */
		public void prefix_row(Line ln)
		{
			startRow--;
			Line[] ancient = oldText;
			oldText = new Line[oldText.length + 1];
			oldText[0] = ln;
			for (int i = 0; i < ancient.length; i++)
				oldText[i + 1] = ancient[i];
		}

		/**
		 * A convenience constructor; same as calling the other overload as
		 * UndoPatch(<caret.row,sel.row>.sort).
		 */
		UndoPatch()
		{
			this(Math.min(caret.row,sel.row),Math.max(caret.row,sel.row));
		}

		/**
		 * Construct a new UndoPatch, copying in pre-patch code from
		 * {@link code} in the given row interval.
		 * 
		 * @param startRow
		 * @param endRow
		 */
		UndoPatch(int startRow, int endRow)
		{
			final int lc = endRow - startRow + 1;
			oldText = new Line[lc];
			for (int i = 0; i < lc; i++)
				oldText[i] = new Line(code.get(startRow + i));
			this.startRow = startRow;
			cbefore.grab();
		}

		/**
		 * Reconstruct the stored pre-patch text by copying from JoshText.code in
		 * the given row range.
		 * 
		 * @param newStartRow
		 *            The index of the first row to copy.
		 * @param newEndRow
		 *            The index of the last row to copy.
		 */
		public void reconstruct(int newStartRow, int newEndRow)
		{
			final int lc = newEndRow - newStartRow + 1;
			oldText = new Line[lc];
			for (int i = 0; i < lc; i++)
				oldText[i] = new Line(code.get(startRow + i));
			startRow = newStartRow;
		}

		/**
		 * Populates the patchText member by copying data stored in this.code
		 * from the previously given starting line this.startRow to the newly
		 * specified end row.
		 * 
		 * @param endRow
		 *            The row at which to stop copying the patch text.
		 */
		public void realize(int endRow)
		{
			fireLineChange(startRow,endRow);
			final int lc = endRow - startRow + 1;
			patchText = new Line[lc];
			for (int i = 0; i < lc; i++)
				patchText[i] = new Line(code.get(startRow + i));
			cafter.grab();
		}
	}

	/**
	 * An array of all available UndoPatches to be reverted (as in Undo) or
	 * re-applied (as in Redo).
	 */
	private ArrayList<UndoPatch> undoPatches = new ArrayList<UndoPatch>();
	/**
	 * A control variable that determines whether a new
	 * UndoPatch can be merged with an old if it is compatible.
	 */
	private boolean undoCanMerge = true;
	/**
	 * Our position in {@link undoPatches}.
	 */
	private int patchIndex = 0;

	/**
	 * Undo the most recently stored UndoPatch.
	 * The patch itself is not moved; instead, patchIndex is decremented.
	 */
	public void undo()
	{
		if (patchIndex == 0) return;
		UndoPatch p = undoPatches.get(--patchIndex);
		// Reverse patch
		int prow;
		for (prow = 0; prow < p.patchText.length; prow++)
		{
			if (prow >= p.oldText.length)
			{
				for (int da = p.patchText.length - prow; da > 0; da--)
					code.remove(p.startRow + prow);
				break;
			}
			code.set(p.startRow + prow,new Line(p.oldText[prow]));
		}
		while (prow < p.oldText.length)
		{
			code.add(p.startRow + prow,new Line(p.oldText[prow]));
			prow++;
		}
		p.cbefore.replace();
		fireLineChange(p.startRow,p.startRow + p.oldText.length);
		repaint();
	}

	/**
	 * Re-apply the UndoPatch that has most recently been undone.
	 * If no previous UndoPatch has been reverted, return without error.
	 */
	public void redo()
	{
		if (patchIndex >= undoPatches.size()) return;
		UndoPatch p = undoPatches.get(patchIndex++);
		// Perform patch
		int prow;
		for (prow = 0; prow < p.oldText.length; prow++)
		{
			if (prow >= p.patchText.length)
			{
				for (int da = p.oldText.length - prow; da > 0; da--)
					code.remove(p.startRow + prow);
				break;
			}
			code.set(p.startRow + prow,new Line(p.patchText[prow]));
		}
		while (prow < p.patchText.length)
		{
			code.add(p.startRow + prow,new Line(p.patchText[prow]));
			prow++;
		}
		p.cafter.replace();
		fireLineChange(p.startRow,p.startRow + p.oldText.length);
		repaint();
	}

	/**
	 * Store an UndoPatch so we can undo it later.
	 * 
	 * @param undo
	 *            The UndoPatch to store.
	 * @param patchType
	 *            The type of the patch, as a constant from OPT.
	 */
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

	/**
	 * Merge two undo patches into one, assuming the patches have the
	 * same starting line index and line count.
	 * 
	 * @param merge
	 *            The new UndoPatch to merge in.
	 * @param into
	 *            The old UndoPatch into which we will merge the new one.
	 */
	private static void undoMerge(UndoPatch merge, UndoPatch into)
	{
		into.patchText = merge.patchText;
		into.cafter.copy(merge.cafter);
		into.opTag = merge.opTag;
	}

	/**
	 * Utility function to check if two UndoPatches are ideal candidates for
	 * merging.
	 * 
	 * @param up1
	 *            Some UndoPatch, which will be tested for merge sanity.
	 * @param up2
	 *            Some other UndoPatch against which to test the first for
	 *            compatibility.
	 * @return Whether, given the two undo patches, they should be merged
	 *         instead of pushing the new one.
	 * @note It is immaterial which UndoPatch is newer.
	 */
	private static boolean undoCompatible(UndoPatch up1, UndoPatch up2)
	{
		if ((up1.opTag != up2.opTag && up2.opTag != OPT.SPACE) || up1.startRow != up2.startRow)
			return false;
		if (up1.oldText.length != up1.patchText.length || up1.oldText.length != up2.oldText.length
				|| up2.patchText.length != up2.patchText.length) return false;
		return true;
	}

	/** @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent) */
//r@Override
	public void focusGained(FocusEvent arg0)
	{
		FindDialog.getInstance().selectedJoshText = this;
	}

	/** @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent) */
//r@Override
	public void focusLost(FocusEvent arg0)
	{ // Unused
	}

	/**
	 * Check if the text has been modified since open.
	 * @return Returns true if the code has been changed, false otherwise.
	 */
	public boolean isChanged()
	{
		return !undoPatches.isEmpty();
	}
}
