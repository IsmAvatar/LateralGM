/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.lateralgm.joshedit.Code.FindResults;
import org.lateralgm.joshedit.FindDialog.FindNavigator;
import org.lateralgm.joshedit.JoshText.OPT;
import org.lateralgm.joshedit.JoshText.UndoPatch;

public class QuickFind extends JToolBar implements FindNavigator
{
	private static final long serialVersionUID = 1L;
	private static final Font FONT = new Font(Font.SANS_SERIF,Font.PLAIN,12);
	private static final String S_FIND, S_REPL, B_HIGHL, B_REPL;
	private static final ImageIcon CLOSE, PREV, NEXT, MARK, REPL, SET;
	static
	{
		S_FIND = Runner.editorInterface.getString("QuickFind.FIND") + ": "; //$NON-NLS-1$
		S_REPL = Runner.editorInterface.getString("QuickFind.REPLACE") + ": "; //$NON-NLS-1$
		B_HIGHL = Runner.editorInterface.getString("QuickFind.HIGHLIGHT"); //$NON-NLS-1$
		B_REPL = Runner.editorInterface.getString("QuickFind.REPLACE"); //$NON-NLS-1$

		CLOSE = Runner.editorInterface.getIconForKey("QuickFind.CLOSE"); //$NON-NLS-1$
		PREV = Runner.editorInterface.getIconForKey("QuickFind.PREV"); //$NON-NLS-1$
		NEXT = Runner.editorInterface.getIconForKey("QuickFind.NEXT"); //$NON-NLS-1$
		MARK = Runner.editorInterface.getIconForKey("QuickFind.MARK"); //$NON-NLS-1$
		REPL = Runner.editorInterface.getIconForKey("QuickFind.REPL"); //$NON-NLS-1$
		SET = Runner.editorInterface.getIconForKey("QuickFind.SET"); //$NON-NLS-1$
	}

	public JButton close, prev, next, settings;
	public JLabel swapFnR;
	public JToggleButton highlight;
	public JButton bReplace;
	public JTextField tFind, tReplace;
	public JoshText joshText;

	enum Mode
	{
		mode_find,mode_replace
	}

	Mode mode = Mode.mode_find;
	protected FindResults lastResult = null;

	public QuickFind(JoshText text)
	{
		super();
		setFloatable(false);
		add(close = new JButton(CLOSE));
		//		close.setMaximumSize(new Dimension(12,12));
		//		close.setPreferredSize(new Dimension(12,12));
		add(swapFnR = new JLabel(S_FIND));
		add(tFind = new JTextField());
		add(tReplace = new JTextField());
		add(prev = new JButton(PREV));
		add(next = new JButton(NEXT));
		add(highlight = new JToggleButton(B_HIGHL,MARK));
		add(bReplace = new JButton(B_REPL,REPL));
		add(settings = new JButton(SET));
		highlight.setFont(FONT);
		swapFnR.setFont(FONT);

		setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		setPreferredSize(new Dimension(320,24));
		setBorder(null);

		tReplace.setVisible(false);
		bReplace.setVisible(false);
		joshText = text;

		settings.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				FindDialog.tFind.setSelectedItem(tFind.getText());
				FindDialog.getInstance().selectedJoshText = joshText;
				FindDialog.getInstance().setVisible(true);
			}
		});
		close.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		prev.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				findPrevious();
			}
		});
		next.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				findNext();
			}
		});
		tFind.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if ((e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0 ^ FindDialog.back.isSelected())
					findPrevious();
				else
					findNext();
			}
		});
		bReplace.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (lastResult == null || !isSelected(lastResult))
				{
					if (joshText.sel.isEmpty()) if (!FindDialog.back.isSelected())
						findNext();
					else
						findPrevious();
				}
				else
					doReplace();
			}
		});
		swapFnR.addMouseListener(new MouseAdapter()
		{
			final Color HIGHLIGHT = new Color(0,128,255);

			@Override
			public void mouseExited(MouseEvent arg0)
			{
				JComponent but = (JComponent) arg0.getSource();
				//				Font mf = but.getFont();
				//				if ((mf.getStyle() & Font.BOLD) != 0)
				//					but.setFont(mf.deriveFont(mf.getStyle() & ~Font.BOLD));
				but.setForeground(Color.BLACK);
			}

			@Override
			public void mouseEntered(MouseEvent arg0)
			{
				JComponent but = (JComponent) arg0.getSource();
				//				Font mf = but.getFont();
				//				if ((mf.getStyle() & Font.BOLD) == 0)
				//				{
				//					orig = mf;
				//					but.setFont(mf.deriveFont(mf.getStyle() | Font.BOLD));
				//				}
				but.setForeground(HIGHLIGHT);
			}

			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				toggleMode();
			}
		});

		setVisible(false);
	}

	protected void doReplace()
	{
		UndoPatch up = joshText.new UndoPatch();
		joshText.sel.insert(tReplace.getText());
		up.realize(Math.max(joshText.caret.row,joshText.sel.row));
		joshText.storeUndo(up,OPT.REPLACE);
		joshText.repaint();
	}

	protected void toggleMode()
	{
		if (mode != Mode.mode_find)
			toggleModeFind();
		else
			toggleModeReplace();
	}

	protected void toggleModeFind()
	{
		swapFnR.setText(S_FIND);
		tFind.setVisible(true);
		tReplace.setVisible(false);
		highlight.setVisible(true);
		bReplace.setVisible(false);
		mode = Mode.mode_find;
	}

	protected void toggleModeReplace()
	{
		swapFnR.setText(S_REPL);
		tFind.setVisible(false);
		tReplace.setVisible(true);
		highlight.setVisible(false);
		bReplace.setVisible(true);
		mode = Mode.mode_replace;
	}

	private void selectFind(FindResults fr)
	{
		joshText.caret.row = fr.line;
		joshText.caret.col = fr.pos;
		joshText.sel.row = fr.endLine;
		joshText.sel.col = fr.endPos;
		joshText.repaint();
	}

	protected boolean isSelected(FindResults fr)
	{
		boolean res = (joshText.caret.row == fr.line && joshText.caret.col == fr.pos
				&& joshText.sel.row == fr.endLine && joshText.sel.col == fr.endPos);
		System.out.println("SELECTED: " + res);
		return res;
	}

	public void findNext()
	{
		// TODO: I have no idea how multiline regexp search will be handled.
		String ftext = tFind.getText();
		if (ftext.length() == 0) return;
		if (FindDialog.regex.isSelected())
		{
			Pattern p;
			try
			{
				p = Pattern.compile(ftext,Pattern.CASE_INSENSITIVE);
			}
			catch (PatternSyntaxException pse)
			{
				System.out.println("Shit man, your expression sucks");
				return;
			}
			lastResult = joshText.code.findNext(p,joshText.caret.row,joshText.caret.col
					+ (joshText.sel.isEmpty() ? 0 : 1));
			if (lastResult != null) selectFind(lastResult);
			return;
		}
		String[] findme = ftext.split("\r?\n");

		lastResult = joshText.code.findNext(findme,joshText.caret.row,joshText.caret.col
				+ (joshText.sel.isEmpty() ? 0 : 1));
		if (lastResult != null) selectFind(lastResult);
		return;
	}

	public void findPrevious()
	{
		String ftext = tFind.getText();
		if (ftext.length() == 0) return;
		if (FindDialog.regex.isSelected()) return;
		String[] findme = ftext.split("\r?\n");
		lastResult = joshText.code.findPrevious(findme,joshText.caret.row,joshText.caret.col);
		if (lastResult != null) selectFind(lastResult);
	}

	public void updateParameters(String find, String replace)
	{
		tFind.setText(find);
		tReplace.setText(replace);
	}

	public void present()
	{
		setVisible(true);
		tFind.selectAll();
		tFind.grabFocus();
	}

	public void replaceNext()
	{
		findNext();
		toggleModeReplace();
	}

	public void replacePrevious()
	{
		findPrevious();
		toggleModeReplace();
	}
}