/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
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

/**
 * @author Josh Ventura
 * A find navigator designed to be small and unobtrusive.
 */
public class QuickFind extends JToolBar implements FindNavigator
{
	/** Blow it out your ears, ECJ. */
	private static final long serialVersionUID = 1L;

	/** Font for quick find widgets. */
	private static final Font FONT = new Font(Font.SANS_SERIF,Font.PLAIN,12);
	/** The string to display on the find label. */
	private static final String S_FIND;
	/** The string to display on the replace label. */
	private static final String S_REPL;
	/** The string to display on the highlight button. */
	private static final String B_HIGHL;
	/** The string to display on the replace button. */
	private static final String B_REPL;

	/** The icon to display on the close button. */
	private static final ImageIcon I_CLOSE;
	/** The string to display on the Find Previous button. */
	private static final ImageIcon I_PREV;
	/** The string to display on the Find Next button. */
	private static final ImageIcon I_NEXT;
	/** The string to display on the Mark button. */
	private static final ImageIcon I_MARK;
	/** The string to display on the Replace button. */
	private static final ImageIcon I_REPL;
	/** The string to display on the Set button. */
	private static final ImageIcon I_SET;
	static
	{
		S_FIND = Runner.editorInterface.getString("QuickFind.FIND") + ": "; //$NON-NLS-1$
		S_REPL = Runner.editorInterface.getString("QuickFind.REPLACE") + ": "; //$NON-NLS-1$
		B_HIGHL = Runner.editorInterface.getString("QuickFind.HIGHLIGHT"); //$NON-NLS-1$
		B_REPL = Runner.editorInterface.getString("QuickFind.REPLACE"); //$NON-NLS-1$

		I_CLOSE = Runner.editorInterface.getIconForKey("QuickFind.CLOSE"); //$NON-NLS-1$
		I_PREV = Runner.editorInterface.getIconForKey("QuickFind.PREV"); //$NON-NLS-1$
		I_NEXT = Runner.editorInterface.getIconForKey("QuickFind.NEXT"); //$NON-NLS-1$
		I_MARK = Runner.editorInterface.getIconForKey("QuickFind.MARK"); //$NON-NLS-1$
		I_REPL = Runner.editorInterface.getIconForKey("QuickFind.REPL"); //$NON-NLS-1$
		I_SET = Runner.editorInterface.getIconForKey("QuickFind.SET"); //$NON-NLS-1$
	}

	/** The Close button. */
	public JButton close;
	/** The Find Previous button. */
	public JButton prev;
	/** The Find Next button. */
	public JButton next;
	/** The Find Settings button. */
	public JButton settings;
	/** The Find/Replace label/switcher. */
	public JLabel swapFnR;
	/** The Highlight All button. */
	public JToggleButton highlight;
	/** The Replace button. */
	public JButton bReplace;
	/** The Find field. */
	public JTextField tFind;
	/** The Replace field. */
	public JTextField tReplace;
	/** The JoshText field. */
	public JoshText joshText;

	/**
	 * Mode constants for find and replace.
	 * @author Josh Ventura
	 */
	enum Mode
	{
		/** Find mode. */
		mode_find,
		/** Find and replace mode. */
		mode_replace
	}

	/** The current find/replace mode. */
	Mode mode = Mode.mode_find;
	/** The most recent find result. */
	protected FindResults lastResult = null;

	/**
	 * @param text The owning JoshText.
	 */
	public QuickFind(JoshText text)
	{
		super();
		setFloatable(false);
		add(close = new JButton(I_CLOSE));
		//		close.setMaximumSize(new Dimension(12,12));
		//		close.setPreferredSize(new Dimension(12,12));
		add(swapFnR = new JLabel(S_FIND));
		add(tFind = new JTextField());
		add(tReplace = new JTextField());
		add(prev = new JButton(I_PREV));
		add(next = new JButton(I_NEXT));
		add(highlight = new JToggleButton(B_HIGHL,I_MARK));
		add(bReplace = new JButton(B_REPL,I_REPL));
		add(settings = new JButton(I_SET));
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
	//r@Override
			public void actionPerformed(ActionEvent e)
			{
				FindDialog.tFind.setSelectedItem(tFind.getText());
				FindDialog.getInstance().selectedJoshText = joshText;
				FindDialog.getInstance().setVisible(true);
			}
		});
		close.addActionListener(new ActionListener()
		{
	//r@Override
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		prev.addActionListener(new ActionListener()
		{
	//r@Override
			public void actionPerformed(ActionEvent arg0)
			{
				findPrevious();
			}
		});
		next.addActionListener(new ActionListener()
		{
	//r@Override
			public void actionPerformed(ActionEvent arg0)
			{
				findNext();
			}
		});
		tFind.addActionListener(new ActionListener()
		{
	//r@Override
			public void actionPerformed(ActionEvent e)
			{
				if ((e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0 ^ FindDialog.backward.isSelected())
					findPrevious();
				else
					findNext();
			}
		});
		bReplace.addActionListener(new ActionListener()
		{
	//r@Override
			public void actionPerformed(ActionEvent e)
			{
				if (lastResult == null || !isSelected(lastResult))
				{
					if (joshText.sel.isEmpty()) if (!FindDialog.backward.isSelected())
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

		//r@Override
			public void mouseExited(MouseEvent arg0)
			{
				JComponent but = (JComponent) arg0.getSource();
				//				Font mf = but.getFont();
				//				if ((mf.getStyle() & Font.BOLD) != 0)
				//					but.setFont(mf.deriveFont(mf.getStyle() & ~Font.BOLD));
				but.setForeground(Color.BLACK);
			}

		//r@Override
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

	/** Replace the current selection in the text editor. */
	public void doReplace()
	{
		UndoPatch up = joshText.new UndoPatch();
		joshText.sel.insert(tReplace.getText());
		up.realize(Math.max(joshText.caret.row,joshText.sel.row));
		joshText.storeUndo(up,OPT.REPLACE);
		joshText.repaint();
	}

	/** Changes our mode between find and find and replace. */
	protected void toggleMode()
	{
		if (mode != Mode.mode_find)
			toggleModeFind();
		else
			toggleModeReplace();
	}

	/** Change the mode to find. */
	protected void toggleModeFind()
	{
		swapFnR.setText(S_FIND);
		tFind.setVisible(true);
		tReplace.setVisible(false);
		highlight.setVisible(true);
		bReplace.setVisible(false);
		mode = Mode.mode_find;
	}

	/** Change the mode to find and replace. */
	protected void toggleModeReplace()
	{
		swapFnR.setText(S_REPL);
		tFind.setVisible(false);
		tReplace.setVisible(true);
		highlight.setVisible(false);
		bReplace.setVisible(true);
		mode = Mode.mode_replace;
	}

	/** Select find results in the editor.
	 * @param fr The find results to highlight. */
	private void selectFind(FindResults fr)
	{
		joshText.caret.row = fr.line;
		joshText.caret.col = fr.pos;
		joshText.sel.row = fr.endLine;
		joshText.sel.col = fr.endPos;
		joshText.caret.positionChanged();
		joshText.repaint();
	}

	/** Check if a set of find results is currently selected in the editor.
	 * @param fr The find results to check.
	 * @return True if the editor's selection mirrors the given find results, false otherwise. */
	protected boolean isSelected(FindResults fr)
	{
		boolean res = (joshText.caret.row == fr.line && joshText.caret.col == fr.pos
				&& joshText.sel.row == fr.endLine && joshText.sel.col == fr.endPos);
		System.out.println("SELECTED: " + res);
		return res;
	}

	/** @see org.lateralgm.joshedit.FindDialog.FindNavigator#findNext()	 */
//r@Override
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
			lastResult = joshText.code.findNext(p,joshText.caret.row,
					joshText.caret.col + (joshText.sel.isEmpty() ? 0 : 1));
			if (lastResult != null) selectFind(lastResult);
			return;
		}
		String[] findme = ftext.split("\r?\n");

		lastResult = joshText.code.findNext(findme,joshText.caret.row,joshText.caret.col
				+ (joshText.sel.isEmpty() ? 0 : 1));
		if (lastResult != null) selectFind(lastResult);
		return;
	}
	
	/** @see org.lateralgm.joshedit.FindDialog.FindNavigator#findPrevious()	 */
//r@Override
	public void findPrevious()
	{
		String ftext = tFind.getText();
		if (ftext.length() == 0) return;
		if (FindDialog.regex.isSelected()) return;
		String[] findme = ftext.split("\r?\n");
		lastResult = joshText.code.findPrevious(findme,joshText.caret.row,joshText.caret.col);
		if (lastResult != null) selectFind(lastResult);
	}

	/** @see org.lateralgm.joshedit.FindDialog.FindNavigator#updateParameters(java.lang.String, java.lang.String)	 */
//r@Override
	public void updateParameters(String find, String replace)
	{
		tFind.setText(find);
		tReplace.setText(replace);
	}

	/** @see org.lateralgm.joshedit.FindDialog.FindNavigator#present() */
//r@Override
	public void present()
	{
		setVisible(true);
		tFind.selectAll();
		tFind.grabFocus();
	}

	/** @see org.lateralgm.joshedit.FindDialog.FindNavigator#replaceNext() */
//r@Override
	public void replaceNext()
	{
		findNext();
		toggleModeReplace();
	}
	
	public int replaceAll()
	{
	  int count = 0;
		toggleModeReplace();
		
		String ftext = tFind.getText();
		if (ftext.length() == 0) return count;
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
				return count;
			}
			
			Boolean resultsExist = true;
			while (resultsExist) {
			  lastResult = joshText.code.findNext(p,joshText.caret.row,
					  joshText.caret.col + (joshText.sel.isEmpty() ? 0 : 1));
			  if (lastResult != null) 
			  {
			    selectFind(lastResult);
			    doReplace();
			    count += 1;
		  	} else {
		  	  resultsExist = false;
		  	}
			}

			return count;
		}
		String[] findme = ftext.split("\r?\n");
		
		Boolean resultsExist = true;
		while (resultsExist) {
		  lastResult = joshText.code.findNext(findme,joshText.caret.row,joshText.caret.col
				+ (joshText.sel.isEmpty() ? 0 : 1));
		  if (lastResult != null) 
		  {
		    selectFind(lastResult);
		    doReplace();
		    count += 1;
	  	} else {
	  	  resultsExist = false;
	  	}
		}
		
		return count;
	}

	/** @see org.lateralgm.joshedit.FindDialog.FindNavigator#replacePrevious() */
//r@Override
	public void replacePrevious()
	{
		findPrevious();
		toggleModeReplace();
	}
}