/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CaretListener;

import org.lateralgm.joshedit.Code.CodeEvent;
import org.lateralgm.joshedit.Code.CodeListener;
import org.lateralgm.main.LGM;

public class JoshTextPanel extends JPanel
	{
	private static final long serialVersionUID = 1L;

	public JScrollPane scroller;
	public JoshText text;
	public LineNumberPanel lines;
	public QuickFind find;

	public JoshTextPanel()
		{
		this((String[]) null);
		}

	public JoshTextPanel(String code)
		{
		this(Runner.splitLines(code));
		}

	public JoshTextPanel(String[] codeLines)
		{
		this(codeLines,true);
		}

	public JoshTextPanel(String[] codeLines, boolean startZero)
		{
		super(new BorderLayout());

		text = new JoshText(codeLines);
		lines = new LineNumberPanel(text,text.code.size(),startZero);
		text.code.addCodeListener(new CodeListener()
			{
				public void codeChanged(CodeEvent e)
					{
					lines.setLines(text.code.size());
					}
			});

		find = new QuickFind(text);
		text.finder = find;

		if (LGM.themename.equals("Quantum"))
			{
			scroller = new CustomJScrollPane(text);
			}
		else
			{
			scroller = new JScrollPane(text);
			}
		scroller.setRowHeaderView(lines);
		add(scroller,BorderLayout.CENTER);
		add(find,BorderLayout.SOUTH);
		}

	private class CustomJScrollPane extends JScrollPane
		{

		/**
			 * 
			 */
		private static final long serialVersionUID = -4960571003195771376L;

		public CustomJScrollPane(JComponent c)
			{
			super(c);
			// TODO Auto-generated constructor stub
			}

		@Override
		public void paintComponent(Graphics g)
			{
			// gtk does not outline the scroll component like the other look and feels
			// nimbus and the default and all the other ones put a border around the line
			// numbering as well as code area, gtk tries to put a border right between the
			// the two, this class gets around that by masking its back color to that of the 
			// line number area
			//super.paint(g);
			g.setColor(lines.bgColor);
			g.fillRect(-1,-1,getWidth() + 11,getHeight() + 1);
			// paint children, the line number panel and code area
			this.paintChildren(g);
			return;
			}

		}

	public int getCaretLine()
		{
		return text.caret.row;
		}

	public int getCaretColumn()
		{
		return text.caret.col;
		}

	public void setCaretPosition(int row, int col)
		{
		text.caret.row = row;
		text.caret.col = col;
		text.caret.colw = text.line_wid_at(row,col);
		text.sel.deselect(false);
		text.caret.positionChanged();
		}

	public void addCaretListener(CaretListener cl)
		{
		text.caret.addCaretListener(cl);
		}

	/** Convenience method that replaces newlines with \r\n for GM compatibility */
	public String getTextCompat()
		{
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < text.code.size(); i++)
			{
			if (i != 0) res.append("\r\n");
			res.append(text.code.getsb(i));
			}
		return res.toString();
		}

	public boolean isChanged()
		{
		return text.isChanged();
		}

	public void setText(String s)
		{
		text.setText(s == null ? null : s.split("\r?\n"));
		}

	public String getLineText(int line)
		{
		return text.code.getsb(line).toString();
		}

	public int getLineCount()
		{
		return text.code.size();
		}

	//TODO: Does not appear to actually be any warning hear to suppress.
	@SuppressWarnings("static-method")
	public void setTabSize(int spaces)
		{
		JoshText.Settings.indentSizeInSpaces = spaces;
		}

	//TODO: This method is icky because CodeTextArea takes DefaultTokenMarker
	//and it is very easy to mix these two up when you are not aware that CodeTextArea
	//derives from this class JoshTextPanel.
	public void setTokenMarker(TokenMarker tm)
		{
		text.setTokenMarker(tm);
		}

	public void setSelection(int row, int col, int row2, int col2)
		{
		text.sel.row = row;
		text.sel.col = col;
		text.caret.row = row2;
		text.caret.col = col2;
		text.caret.colw = text.line_wid_at(row,col);
		text.caret.positionChanged();
		text.sel.selectionChanged();
		}
	}
