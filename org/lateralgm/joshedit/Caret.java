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

public class Caret implements ActionListener
{
	public int col, row;
	/** The width to the position of the column, used when traversing lines of varying length. */
	public int colw;
	public boolean insert = true;

	private boolean visible = true;
	private Timer flasher;
	private JComponent painter;
	private JoshText joshText;
	private ArrayList<CaretListener> caretListeners = new ArrayList<CaretListener>();

	public Caret(JoshText jt)
	{
		setBlinkRate(getDefaultBlinkRate());
		joshText = jt;
		painter = jt;
		flasher.start();
	}

	public Caret(Caret caret) // Copy constructor solely for push/pop mechanisms. Copies only positions.
	{
		col = caret.col;
		row = caret.row;
		colw = caret.colw;
	}

	public static int getDefaultBlinkRate()
	{
		Object oblink = UIManager.get("TextArea.caretBlinkRate",null);
		int blink = 500;
		if (oblink != null && oblink instanceof Number) blink = ((Number) oblink).intValue();
		return blink;
	}

	public void setBlinkRate(int rate)
	{
		if (flasher == null)
			flasher = new Timer(rate,this);
		else
			flasher.setDelay(rate);
	}

	public void flashOn()
	{
		flasher.restart();
		if (visible != true)
		{
			visible = true;
			repaint();
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		visible = !visible;
		repaint();
	}

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
				painter.repaint(i.left + joshText.line_wid_at(row,col),i.top + row * gh,insert ? gw + 1 : 2,gh);
		}
	}

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
				g.fillRect(i.left + col * gw,     i.top + Math.min(row,sel.row) * gh,
									 insert ? 2 : gw + 1,   (Math.abs(row - sel.row) + 1) * gh);
			else
				g.fillRect(i.left + joshText.line_wid_at(row,col),i.top + row * gh, insert ? 2 : gw + 1,gh);
			g.setPaintMode();
		}
	}

	public void resetcoords(Caret scar)
	{
		col = scar.col;
		row = scar.row;
		colw = scar.colw;
	}

	public int getPositionRepresentation(Selection selection)
	{
		return selection.type == ST.RECT ? joshText.column_to_index(row,col) : col;
	}

	public void addCaretListener(CaretListener cl)
	{
		caretListeners.add(cl);
	}

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
