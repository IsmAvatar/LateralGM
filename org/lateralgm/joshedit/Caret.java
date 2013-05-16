/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.lateralgm.joshedit.Selection.ST;

/**
 * Class representing our caret position.
 */
public class Caret implements ActionListener
{
	/** The column on which our caret is set. */
	public int col;
	/** The row on which our caret is set. */
	public int row;
	/** The width to the position of the column, used when traversing lines of varying length. */
	public int colw;
	/** True if we are in insert mode, false if we are in overwrite mode. */
	public boolean insert = true;

	/** True if we are to render. */
	private boolean visible = true;
	/** The timer that controls caret blinking/flashing. */
	private Timer flasher;
	/** The component to which we will paint. */
	private JComponent painter;
	/** The JoshText that contains us. */
	private JoshText joshText;
	/** List of caret listeners to notify on position update. */
	private ArrayList<CaretListener> caretListeners = new ArrayList<CaretListener>();

	/**
	 * @param jt The owning JoshText.
	 */
	public Caret(JoshText jt)
	{
		setBlinkRate(getDefaultBlinkRate());
		joshText = jt;
		painter = jt;
		flasher.start();
	}

	/**
	 * Copy coordinates on construct; do nothing else.
	 * @param caret Caret whose coordinates will be copied.
	 */
	public Caret(Caret caret) // Copy constructor solely for push/pop mechanisms. Copies only positions.
	{
		col = caret.col;
		row = caret.row;
		colw = caret.colw;
	}

	/**
	 * @return The default blink/flash rate.
	 */
	public static int getDefaultBlinkRate()
	{
		Object oblink = UIManager.get("TextArea.caretBlinkRate",null);
		int blink = 500;
		if (oblink != null && oblink instanceof Number) blink = ((Number) oblink).intValue();
		return blink;
	}

	/**
	 * @param rate The new default blink/flash rate.
	 */
	public void setBlinkRate(int rate)
	{
		if (flasher == null)
			flasher = new Timer(rate,this);
		else
			flasher.setDelay(rate);
	}

	/** Reset the flasher timer, showing the caret now. */
	public void flashOn()
	{
		flasher.restart();
		if (visible != true)
		{
			visible = true;
			repaint();
		}
	}

	/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
//r@Override
	public void actionPerformed(ActionEvent e)
	{
		visible = !visible;
		repaint();
	}

	/** Repaint the caret. */
	protected final synchronized void repaint()
	{
		if (painter != null)
		{
			FontMetrics fm = painter.getFontMetrics(painter.getFont());
			Insets i = painter.getInsets();
			int gw = fm.getMaxAdvance(), gh = fm.getHeight();
			if (joshText.sel.type == ST.RECT)
				painter.repaint(i.left + col * gw,i.top + row * gh,gw + 1,gh);
			else
				painter.repaint(i.left + joshText.line_wid_at(row,col),i.top + row * gh,
						insert ? gw + 1 : 2,gh);
		}
	}

	/**
	 * @param g The graphics object to which to paint.
	 * @param sel The selection object to paint along with the caret.
	 */
	public void paint(Graphics g, Selection sel)
	{
		// Draw caret
		if (visible)
		{
			FontMetrics fm = painter.getFontMetrics(painter.getFont());
			Insets i = painter.getInsets();
			int gw = fm.getMaxAdvance(), gh = fm.getHeight();

			g.setXORMode(Color.WHITE);
			if (sel.type == ST.RECT)
				g.fillRect(i.left + col * gw,i.top + Math.min(row,sel.row) * gh,insert ? 2 : gw + 1,
						(Math.abs(row - sel.row) + 1) * gh);
			else
				g.fillRect(i.left + joshText.line_wid_at(row,col),i.top + row * gh,insert ? 2 : gw + 1,gh);
			g.setPaintMode();
		}
	}

	/**
	 * Copy the coordinates from another Caret into this Caret.
	 * @param scar The caret from which to copy coordinates.
	 */
	public void resetcoords(Caret scar)
	{
		col = scar.col;
		row = scar.row;
		colw = scar.colw;
	}

	/**
	 * @param selection The current selection, for reference.
	 * @return The actual position index, in characters in the line, of the caret.
	 */
	public int getPositionRepresentation(Selection selection)
	{
		return selection.type == ST.RECT ? joshText.column_to_index(row,col) : col;
	}

	/**
	 * Add a caret listener to inform on position change.
	 * @param cl The new caret listener.
	 */
	public void addCaretListener(CaretListener cl)
	{
		caretListeners.add(cl);
	}

	/** Fire a position change to all listeners. */
	public void positionChanged()
	{
		for (int i = 0; i < caretListeners.size(); i++)
			caretListeners.get(i).caretUpdate(new CaretEvent(joshText)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public int getMark()
				{
					return col;
				}

				@Override
				public int getDot()
				{
					return joshText.sel.col;
				}
			});
	}
}
