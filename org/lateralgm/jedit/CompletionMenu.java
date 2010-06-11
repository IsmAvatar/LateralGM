/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.jedit;

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
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.text.BadLocationException;

public class CompletionMenu extends JWindow
	{
	private static final long serialVersionUID = 1L;
	protected JEditTextArea area;
	private JScrollPane scroll;
	private final Completion[] completions;
	private Completion[] options;
	private String word;
	private JList completionList;
	private KeyHandler keyHandler;
	protected int wordOffset;
	protected int wordPos;
	protected int wordLength;

	public CompletionMenu(Frame f, JEditTextArea a, int offset, int pos, int length, Completion[] c)
		{
		super(f);
		area = a;
		wordOffset = offset;
		wordPos = pos;
		wordLength = length;
		completions = c;
		keyHandler = new KeyHandler();
		completionList = new JList();
		completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		completionList.addKeyListener(keyHandler);
		completionList.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
					{
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
					area.setCaretVisible(true);
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
		area.requestFocus();
		}

	public void setLocation()
		{
		int wl = area.getLineOfOffset(wordOffset);
		int lwo = wordOffset - area.getLineStartOffset(wl);
		Point p = area.getLocationOnScreen();
		p.x += Math.min(area.getWidth(),Math.max(0,area.offsetToX(wl,lwo)));
		p.y += Math.min(area.getHeight(),Math.max(0,area.lineToY(wl + 1))) + 3;
		setLocation(p);
		}

	public void reset()
		{
		if (area.getSelectionStart() != wordOffset + wordPos)
			area.setCaretPosition(wordOffset + wordPos);
		String w = area.getText(wordOffset,wordPos);
		if (w == "")
			{
			options = completions;
			}
		else if ((options != null) && (word != null) && (w.startsWith(word)))
			{
			ArrayList<Completion> l = new ArrayList<Completion>();
			for (Completion c : options)
				{
				if (c.match(w)) l.add(c);
				}
			options = l.toArray(new Completion[l.size()]);
			}
		else
			{
			ArrayList<Completion> l = new ArrayList<Completion>();
			for (Completion c : completions)
				{
				if (c.match(w)) l.add(c);
				}
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
			return c.apply(area,input,wordOffset,wordPos,wordLength);
			}
		return false;
		}

	public void setSelectedText(String s)
		{
		int ss = area.getSelectionStart();
		int se = area.getSelectionEnd();
		int sl = se - ss;
		int nl = s.length();
		int ne = ss + nl;
		int[] w = { wordOffset,wordOffset + wordPos,wordOffset + wordLength };
		for (int i = 1; i < 3; i++)
			{
			if (w[i] >= (i == 0 ? se + 1 : se))
				w[i] += nl - sl;
			else if (w[i] >= ss) w[i] = i == 0 ? ss : ne;
			}
		area.setSelectedText(s);
		wordOffset = w[0];
		wordPos = w[1] - w[0];
		wordLength = w[2] - w[0];
		}

	public abstract static class Completion
		{
		protected String name;

		public boolean match(String start)
			{
			return match(start,name) >= 0;
			}

		public abstract boolean apply(JEditTextArea a, char input, int offset, int pos, int length);

		public static boolean replace(SyntaxDocument d, int offset, int length, String text)
			{
			try
				{
				d.replace(offset,length,text,null);
				}
			catch (BadLocationException ble)
				{
				return false;
				}
			return true;
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

		public boolean apply(JEditTextArea a, char input, int offset, int pos, int length)
			{
			String s = name + (input != '\0' ? String.valueOf(input) : "");
			int l = input != '\0' ? pos : length;
			SyntaxDocument d = a.getDocument();
			if (!replace(d,offset,l,s)) return false;
			a.setCaretPosition(offset + s.length());
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
					if (area.getSelectionStart() == area.getSelectionEnd())
						{
						if (wordPos <= 0)
							dispose();
						else
							try
								{
								area.getDocument().remove(wordOffset + wordPos - 1,1);
								wordPos -= 1;
								wordLength -= 1;
								}
							catch (BadLocationException ble)
								{
								dispose();
								}
						}
					else
						setSelectedText("");
					e.consume();
					reset();
					break;
				case KeyEvent.VK_LEFT:
					if (wordPos <= 0)
						dispose();
					else
						wordPos -= 1;
					e.consume();
					reset();
					break;
				case KeyEvent.VK_RIGHT:
					if (wordPos >= wordLength)
						dispose();
					else
						wordPos += 1;
					e.consume();
					reset();
					break;
				case KeyEvent.VK_ENTER:
					if (apply())
						e.consume();
					else
						dispose();
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
