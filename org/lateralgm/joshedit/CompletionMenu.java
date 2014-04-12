/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014, Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.joshedit;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.UIManager;

/**
 * @author IsmAvatar
 * Class to handle code completion.
 */
// TODO: doCodeSize(true); <- call that when the auto completion is confirmed
// otherwise it doesnt update the client area, i propose making a system 
// wherein all insertions/deletions of text will invalidate a client area resizing
public class CompletionMenu
	{
	/** The text area in which to handle code completion. */
	protected JoshText area;
	/** The scroll pane to house completion results. */
	private JScrollPane scroll;
	/** Array of available completions. */
	private final Completion[] completions;
	/** Completion options from which the user can select. */
	private Completion[] options;
	private String word;
	private JList<Completion> completionList;
	private KeyHandler keyHandler;
	//	protected int wordOffset;
	//	protected int wordPos;
	//	protected int wordLength;

	protected int row, wordStart, wordEnd, caret;

	protected PopupHandler ph;
	protected Point loc;

	//FIXME: For some reason this completion menu can not accept focus allowing VK_TAB to not be
	// dispatched.
	
	public CompletionMenu(Frame owner, JoshText a, int y, int x1, int x2, int caret, Completion[] c)
		{
		area = a;
		row = y;
		wordStart = x1;
		wordEnd = x2;
		this.caret = caret;
		completions = c;

		keyHandler = new KeyHandler();
		completionList = new JList<Completion>();
		completionList.setFixedCellHeight(12);
		completionList.setFont(new Font("Monospace", Font.PLAIN, 10));
		completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		completionList.addKeyListener(keyHandler);
		completionList.addMouseListener(new MouseAdapter()
			{
				@Override
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
		ph = new PopupHandler(owner,scroll);
		ph.addHideListener(new PopupHandler.HideListener()
			{
		//r@Override
				public void hidePerformed(boolean wasVisible)
					{
					dispose();
					}
			});

		reset();
		}

	public void show()
		{
		ph.show(loc.x,loc.y);
		}

	public void dispose()
		{
		ph.dispose();
		area.requestFocusInWindow();
		}

	public void setLocation()
		{
		Point p = area.getLocationOnScreen();
		int y = (row + 1) * area.metrics.lineHeight();
		int x = area.metrics.lineWidth(row,wordEnd);
		// adding this breaks it, but without it shows at the correct position
		// ffs wtf?
		/*
		if (area.getParent() instanceof JViewport)
			{
			Point vp = ((JViewport) area.getParent()).getViewPosition();
			x -= vp.x;
			y -= vp.y;
			}
    */
		p.x += Math.min(area.getWidth(),Math.max(0,x));
		p.y += Math.min(area.getHeight(),Math.max(0,y)) + 3;
		loc = p;
		//		pm.setLocation(p);
		//		setLocation(p);
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
		//		pack();
		setLocation();
		select(0);

		show();
		//		requestFocus();
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

	/**
	 * The PopupHandler class maintains a popup container that can popup on demand,
	 * and hide at the expected time (e.g. loss of focus). Please be sure to dispose of
	 * it when you are done, as this will free up the global listeners it registers.
	 * A HideListener can be registered to listen for whenever the popup is hidden
	 * (e.g. loss of focus causes it to hide itself - as well as user-invoked hides).
	 * <p>
	 * A PopupHandler is intended for custom popup components where a JPopupMenu is insufficient.
	 */
	public static class PopupHandler implements AWTEventListener,WindowListener,ComponentListener
		{
		protected Popup pop;
		protected Window invoker;
		protected Component contents;
		protected int lastX, lastY;

		public PopupHandler(Window invoker, Component contents)
			{
			this.invoker = invoker;
			this.contents = contents;
			install();
			}

		public void setContents(Component contents)
			{
			this.contents = contents;
			show(lastX,lastY);
			}

		protected void install()
			{
			long mask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK;
			Toolkit.getDefaultToolkit().addAWTEventListener(this,mask);
			invoker.addWindowListener(this);
			invoker.addComponentListener(this);
			}

		public void dispose()
			{
			if (pop != null) pop.hide();
			pop = null;
			uninstall();
			}

		protected void uninstall()
			{
			Toolkit.getDefaultToolkit().removeAWTEventListener(this);
			invoker.removeWindowListener(this);
			invoker.removeComponentListener(this);
			}

		public void show(int x, int y)
			{
			lastX = x;
			lastY = y;
			if (pop != null) pop.hide();
			pop = PopupFactory.getSharedInstance().getPopup(invoker,contents,x,y);
			pop.show();
			}

		public void hide()
			{
			if (pop != null) pop.hide();
			fireHide(pop != null);
			pop = null;
			}

		public static interface HideListener extends EventListener
			{
			void hidePerformed(boolean wasVisible);
			}

		protected List<HideListener> hll = new ArrayList<HideListener>();

		public void addHideListener(HideListener e)
			{
			hll.add(e);
			}

		public void removeHideListener(HideListener e)
			{
			hll.remove(e);
			}

		protected void fireHide(boolean wasVisible)
			{
			for (HideListener hl : hll)
				hl.hidePerformed(wasVisible);
			}

		protected boolean isInPopup(Component src)
			{
			for (Component c = src; c != null; c = c.getParent())
				if (c == contents)
					return true;
				else if (c instanceof java.applet.Applet || c instanceof Window) return false;
			return false;
			}

		//events
	//r@Override
		public void eventDispatched(AWTEvent ev)
			{
			// We are interested in MouseEvents only
			if (!(ev instanceof MouseEvent)) return;
			MouseEvent me = (MouseEvent) ev;
			Component src = me.getComponent();
			switch (me.getID())
				{
				case MouseEvent.MOUSE_PRESSED:
					if (isInPopup(src)) return;
					hide();
					// Ask UIManager about should we consume event that closes
					// popup. This made to match native apps behaviour.
					// Consume the event so that normal processing stops.
					if (UIManager.getBoolean("PopupMenu.consumeEventOnClose")) me.consume();
					break;
				case MouseEvent.MOUSE_WHEEL:
					if (isInPopup(src)) return;
					hide();
					break;
				}
			}

		/** Just hide the window. */
	//r@Override
		public void componentResized(ComponentEvent e)
			{
			hide();
			}

		/** Just hide the window. */
	//r@Override
	 public void componentMoved(ComponentEvent e)
			{
			hide();
			}

		/** Just hide the window. */
	//r@Override
		public void componentShown(ComponentEvent e)
			{
			hide();
			}

		/** Just hide the window. */
	//r@Override
		public void componentHidden(ComponentEvent e)
			{
			hide();
			}

		/** Just hide the window. */
	//r@Override
		public void windowClosing(WindowEvent e)
			{
			hide();
			}

		/** Just hide the window. */
	//r@Override
		public void windowClosed(WindowEvent e)
			{
			hide();
			}

		/** Just hide the window. */
	//r@Override
		public void windowIconified(WindowEvent e)
			{
			hide();
			}

		/** Just hide the window. */
	//r@Override
		public void windowDeactivated(WindowEvent e)
			{
			hide();
			}

		/** Unused */
		//Unused
	//r@Override
		public void windowOpened(WindowEvent e)
			{ //Unused
			}

		/** Unused */
	//r@Override
		public void windowDeiconified(WindowEvent e)
			{ //Unused
			}

		/** Unused */
	//r@Override
		public void windowActivated(WindowEvent e)
			{ //Unused
			}
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

		@Override
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

		@Override
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

	/**
	 * @author IsmAvatar
	 * Class to handle key presses in the code completion pane.
	 */
	private class KeyHandler extends KeyAdapter
		{
		/** Invoke default super constructor.*/
		public KeyHandler()
			{
			super();
			}

		/** Handle key press. */
		@Override
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

		/** Handle key type. */
		@Override
		public void keyTyped(KeyEvent e)
			{
			if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) return;
			char c = e.getKeyChar();
			if (c == KeyEvent.VK_BACK_SPACE) return;
			String s = String.valueOf(c);
			//TODO: This statement used to check \\v and \\t as well, but it was causing VK_ENTER and VK_TAB not to be accepted
			// as completing the menu which resulted in a painting exception. VK_TAB and VK_ENTER are standard for completing
			// an autocompletion menu, see Eclipse and Scintilla/CodeBlock.
			if (s.matches("\\w")) 
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
