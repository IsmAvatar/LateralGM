/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;

public class CompletionMenu extends JWindow
	{
	private static final long serialVersionUID = 1L;
	protected JoshText area;
	private JScrollPane scroll;
	private final Completion[] completions;
	private Completion[] options;
	private String word;
	private JList completionList;
	private KeyHandler keyHandler;
	//	protected int wordOffset;
	//	protected int wordPos;
	//	protected int wordLength;

	protected int row, wordStart, wordEnd, caret;

	public CompletionMenu(Frame owner, JoshText a, int y, int x1, int x2, int caret, Completion[] c)
		{
		super(owner);
		area = a;
		row = y;
		wordStart = x1;
		wordEnd = x2;
		this.caret = caret;
		completions = c;

		keyHandler = new KeyHandler();
		completionList = new JList();
		completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		completionList.addKeyListener(keyHandler);
		completionList.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
					{
					System.out.println(e);
					if (apply())
						e.consume();
					else
						dispose();
					}
			});
		scroll = new JScrollPane(completionList);
		scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(scroll);
		getContentPane().setFocusTraversalKeysEnabled(false);
		addWindowFocusListener(new WindowFocusListener()
			{
				public void windowGainedFocus(WindowEvent e)
					{
					//					area.setCaretVisible(true);
					}

				public void windowLostFocus(WindowEvent e)
					{
					dispose();
					}
			});
		reset();
		}

	public void dispose()
		{
		super.dispose();
		area.requestFocusInWindow();
		}

	public void setLocation()
		{
		Point p = area.getLocationOnScreen();
		int y = (row + 1) * area.metrics.lineHeight();
		int x = area.metrics.lineWidth(row,wordStart);
		if (area.getParent() instanceof JViewport)
			{
			Point vp = ((JViewport) area.getParent()).getViewPosition();
			x -= vp.x;
			y -= vp.y;
			}

		p.x += Math.min(area.getWidth(),Math.max(0,x));
		p.y += Math.min(area.getHeight(),Math.max(0,y)) + 3;
		setLocation(p);
		}

	public void reset()
		{
		//		if (area.getSelectionStart() != wordOffset + wordPos)
		//			area.setCaretPosition(wordOffset + wordPos);
		//		String w = area.getText(wordOffset,wordPos);
		String w = area.code.getsb(row).toString().substring(wordStart,wordEnd);
		if (w.isEmpty())
			options = completions;
		else if ((options != null) && (word != null) && (w.startsWith(word)))
			{
			ArrayList<Completion> l = new ArrayList<Completion>();
			for (Completion c : options)
				if (c.match(w)) l.add(c);
			options = l.toArray(new Completion[l.size()]);
			}
		else
			{
			ArrayList<Completion> l = new ArrayList<Completion>();
			for (Completion c : completions)
				if (c.match(w)) l.add(c);
			options = l.toArray(new Completion[l.size()]);
			}
		if (options.length <= 0)
			{
			dispose();
			return;
			}
		word = w;
		completionList.setListData(options);
		completionList.setVisibleRowCount(Math.min(options.length,8));
		pack();
		setLocation();
		select(0);
		setVisible(true);
		requestFocus();
		completionList.requestFocusInWindow();
		}

	public void select(int n)
		{
		completionList.setSelectedIndex(n);
		completionList.ensureIndexIsVisible(n);
		}

	public void selectRelative(int n)
		{
		int s = completionList.getModel().getSize();
		if (s <= 1) return;
		int i = completionList.getSelectedIndex();
		select((s + ((i + n) % s)) % s);
		}

	public boolean apply()
		{
		return apply('\0');
		}

	public boolean apply(char input)
		{
		Object o = completionList.getSelectedValue();
		if (o instanceof Completion)
			{
			Completion c = (Completion) o;
			dispose();
			if (input == '\n') input = '\0';
			return c.apply(area,input,row,wordStart,wordEnd);
			}
		return false;
		}

	public void setSelectedText(String s)
		{
		area.sel.insert(s);
		}

	public abstract static class Completion
		{
		protected String name;

		public boolean match(String start)
			{
			return match(start,name) >= 0;
			}

		public abstract boolean apply(JoshText a, char input, int row, int wordStart, int wordEnd);

		public static boolean replace(JoshText d, int row, int start, int end, String text)
			{
			//			d.sel.insert(text);
			d.code.getsb(row).replace(start,end,text);
			d.code.fireLinesChanged();
			d.fireLineChange(row,row);
			d.repaint();

			//			try
			//				{
			//				d.replace(offset,length,text,null);
			//				}
			//			catch (BadLocationException ble)
			//				{
			return true;
			//				}
			//			return true;
			}

		public static int match(String input, String name)
			{
			if (input.equals(name)) return 0;
			if (name.startsWith(input)) return 1;
			String il = input.toLowerCase();
			String nl = name.toLowerCase();
			if (il.equals(nl)) return 2;
			if (nl.startsWith(il)) return 3;
			String re = "(?<!(^|_))" + (name.matches("[A-Z_]+") ? "." : "[a-z_]");
			String ns = name.replaceAll(re,"").toLowerCase();
			if (il.equals(ns)) return 4;
			if (ns.startsWith(il)) return 5;
			return -1;
			}

		public String toString()
			{
			return name;
			}
		}

	public static class WordCompletion extends Completion
		{
		public WordCompletion(String w)
			{
			name = w;
			}

		public boolean apply(JoshText a, char input, int row, int wordStart, int wordEnd)
			{
			String s = name + (input != '\0' ? String.valueOf(input) : new String());
			//			int l = input != '\0' ? pos : length;
			if (!replace(a,row,wordStart,wordEnd,s)) return false;
			a.caret.row = row;
			a.caret.col = wordStart + s.length();
			a.caret.positionChanged();
			//			a.setCaretPosition(offset + s.length());
			return true;
			}
		}

	private class KeyHandler extends KeyAdapter
		{
		public KeyHandler()
			{
			super();
			}

		public void keyPressed(KeyEvent e)
			{
			switch (e.getKeyCode())
				{
				case KeyEvent.VK_BACK_SPACE:
					if (area.sel.isEmpty())
						{
						if (caret <= 0)
							dispose();
						else
							//							try
							//								{
							//								area.getDocument().remove(wordOffset + wordPos - 1,1);
							caret -= 1;
						wordEnd -= 1;
						//								}
						//							catch (BadLocationException ble)
						//								{
						//								dispose();
						//								}
						}
					else
						setSelectedText(new String());
					e.consume();
					reset();
					break;
				case KeyEvent.VK_LEFT:
					if (caret <= 0)
						dispose();
					else
						caret -= 1;
					e.consume();
					reset();
					break;
				case KeyEvent.VK_RIGHT:
					if (caret >= wordEnd)
						dispose();
					else
						caret += 1;
					e.consume();
					reset();
					break;
				case KeyEvent.VK_ESCAPE:
					dispose();
					e.consume();
					break;
				case KeyEvent.VK_UP:
					selectRelative(-1);
					e.consume();
					break;
				case KeyEvent.VK_DOWN:
					selectRelative(1);
					e.consume();
					break;
				}
			}

		public void keyTyped(KeyEvent e)
			{
			if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) return;
			char c = e.getKeyChar();
			if (c == KeyEvent.VK_BACK_SPACE) return;
			String s = String.valueOf(c);
			if (s.matches("[^\\v\\t\\w]"))
				{
				apply(c);
				e.consume();
				dispose();
				return;
				}
			setSelectedText(s);
			e.consume();
			reset();
			}
		}
	}
