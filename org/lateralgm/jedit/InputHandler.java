/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 * 
 * This file incorporates work covered by the following copyright and
 * permission notice: 
 * 
 *     InputHandler.java - Manages key bindings and executes actions
 *     Copyright (C) 1999 Slava Pestov
 *     
 *     You may use and modify this package for any purpose. Redistribution is
 *     permitted, in both source and binary form, provided that this notice
 *     remains intact in all source distributions of this package.
 */
package org.lateralgm.jedit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;

import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * An input handler converts the user's key strokes into concrete actions.
 * It also takes care of macro recording and action repetition.<p>
 *
 * This class provides all the necessary support code for an input
 * handler, but doesn't actually do any key binding logic. It is up
 * to the implementations of this class to do so.
 *
 * @author Slava Pestov
 */
public abstract class InputHandler extends KeyAdapter
	{
	public static final String KEEP_INDENT_PROPERTY = "InputHandler.keepIndent";
	public static final String TAB_TO_INDENT_PROPERTY = "InputHandler.tabToIndent";
	public static final String CONVERT_TABS_PROPERTY = "InputHandler.convertTabs";

	public static final ActionListener BACKSPACE = new Backspace();
	public static final ActionListener BACKSPACE_WORD = new BackspaceWord();
	public static final ActionListener DELETE = new Delete();
	public static final ActionListener DELETE_WORD = new DeleteWord();
	public static final ActionListener END = new End(false);
	public static final ActionListener DOCUMENT_END = new DocumentEnd(false);
	public static final ActionListener SELECT_END = new End(true);
	public static final ActionListener SELECT_DOC_END = new DocumentEnd(true);
	public static final ActionListener SELECT_ALL = new SelectAll();
	public static final ActionListener INSERT_BREAK = new InsertBreak();
	public static final ActionListener INSERT_TAB = new InsertTab();
	public static final ActionListener HOME = new Home(false);
	public static final ActionListener DOCUMENT_HOME = new DocumentHome(false);
	public static final ActionListener SELECT_HOME = new Home(true);
	public static final ActionListener SELECT_DOC_HOME = new DocumentHome(true);
	public static final ActionListener NEXT_CHAR = new NextChar(false);
	public static final ActionListener NEXT_LINE = new NextLine(false);
	public static final ActionListener NEXT_PAGE = new NextPage(false);
	public static final ActionListener NEXT_WORD = new NextWord(false);
	public static final ActionListener SELECT_NEXT_CHAR = new NextChar(true);
	public static final ActionListener SELECT_NEXT_LINE = new NextLine(true);
	public static final ActionListener SELECT_NEXT_PAGE = new NextPage(true);
	public static final ActionListener SELECT_NEXT_WORD = new NextWord(true);
	public static final ActionListener OVERWRITE = new Overwrite();
	public static final ActionListener PREV_CHAR = new PrevChar(false);
	public static final ActionListener PREV_LINE = new PrevLine(false);
	public static final ActionListener PREV_PAGE = new PrevPage(false);
	public static final ActionListener PREV_WORD = new PrevWord(false);
	public static final ActionListener SELECT_PREV_CHAR = new PrevChar(true);
	public static final ActionListener SELECT_PREV_LINE = new PrevLine(true);
	public static final ActionListener SELECT_PREV_PAGE = new PrevPage(true);
	public static final ActionListener SELECT_PREV_WORD = new PrevWord(true);
	public static final ActionListener REPEAT = new Repeat();
	public static final ActionListener TOGGLE_RECT = new ToggleRect();

	public static final ActionListener CUT = new Cut();
	public static final ActionListener COPY = new Copy();
	public static final ActionListener PASTE = new Paste();

	// Default action
	public static final ActionListener INSERT_CHAR = new InsertChar();

	private static Hashtable<String,ActionListener> actions;

	static
		{
		actions = new Hashtable<String,ActionListener>();
		actions.put("backspace",BACKSPACE);
		actions.put("backspace-word",BACKSPACE_WORD);
		actions.put("delete",DELETE);
		actions.put("delete-word",DELETE_WORD);
		actions.put("select-all",SELECT_ALL);
		actions.put("end",END);
		actions.put("select-end",SELECT_END);
		actions.put("document-end",DOCUMENT_END);
		actions.put("select-doc-end",SELECT_DOC_END);
		actions.put("insert-break",INSERT_BREAK);
		actions.put("insert-tab",INSERT_TAB);
		actions.put("home",HOME);
		actions.put("select-home",SELECT_HOME);
		actions.put("document-home",DOCUMENT_HOME);
		actions.put("select-doc-home",SELECT_DOC_HOME);
		actions.put("next-char",NEXT_CHAR);
		actions.put("next-line",NEXT_LINE);
		actions.put("next-page",NEXT_PAGE);
		actions.put("next-word",NEXT_WORD);
		actions.put("select-next-char",SELECT_NEXT_CHAR);
		actions.put("select-next-line",SELECT_NEXT_LINE);
		actions.put("select-next-page",SELECT_NEXT_PAGE);
		actions.put("select-next-word",SELECT_NEXT_WORD);
		actions.put("overwrite",OVERWRITE);
		actions.put("prev-char",PREV_CHAR);
		actions.put("prev-line",PREV_LINE);
		actions.put("prev-page",PREV_PAGE);
		actions.put("prev-word",PREV_WORD);
		actions.put("select-prev-char",SELECT_PREV_CHAR);
		actions.put("select-prev-line",SELECT_PREV_LINE);
		actions.put("select-prev-page",SELECT_PREV_PAGE);
		actions.put("select-prev-word",SELECT_PREV_WORD);
		actions.put("repeat",REPEAT);
		actions.put("toggle-rect",TOGGLE_RECT);
		actions.put("insert-char",INSERT_CHAR);
		//
		actions.put("cut",CUT);
		actions.put("copy",COPY);
		actions.put("paste",PASTE);
		}

	/**
	 * Returns a named text area action.
	 * @param name The action name
	 */
	public static ActionListener getAction(String name)
		{
		return actions.get(name);
		}

	/**
	 * Returns the name of the specified text area action.
	 * @param listener The action
	 */
	public static String getOperationName(ActionListener listener)
		{
		Enumeration<String> en = getActions();
		while (en.hasMoreElements())
			{
			String name = en.nextElement();
			ActionListener l = getAction(name);
			if (l == listener) return name;
			}
		return null;
		}

	/** Returns an enumeration of all available actions. */
	public static Enumeration<String> getActions()
		{
		return actions.keys();
		}

	/**
	 * Adds the default key bindings to this input handler.
	 * This should not be called in the constructor of this
	 * input handler, because applications might load the
	 * key bindings from a file, etc.
	 */
	public abstract void addDefaultKeyBindings();

	/**
	 * Adds a key binding to this input handler.
	 * @param keyBinding The key binding (the format of this is
	 * input-handler specific)
	 * @param action The action
	 */
	public abstract void addKeyBinding(String keyBinding, ActionListener action);

	/**
	 * Removes a key binding from this input handler.
	 * @param keyBinding The key binding
	 */
	public abstract void removeKeyBinding(String keyBinding);

	/** Removes all key bindings from this input handler. */
	public abstract void removeAllKeyBindings();

	/**
	 * Grabs the next key typed event and invokes the specified
	 * action with the key as a the action command.
	 * @param action The action
	 */
	public void grabNextKeyStroke(ActionListener listener)
		{
		grabAction = listener;
		}

	/**
	 * Returns if repeating is enabled. When repeating is enabled,
	 * actions will be executed multiple times. This is usually
	 * invoked with a special key stroke in the input handler.
	 */
	public boolean isRepeatEnabled()
		{
		return repeat;
		}

	/**
	 * Enables repeating. When repeating is enabled, actions will be
	 * executed multiple times. Once repeating is enabled, the input
	 * handler should read a number from the keyboard.
	 */
	public void setRepeatEnabled(boolean repeat)
		{
		this.repeat = repeat;
		}

	/** Returns the number of times the next action will be repeated. */
	public int getRepeatCount()
		{
		return (repeat ? Math.max(1,repeatCount) : 1);
		}

	/**
	 * Sets the number of times the next action will be repeated.
	 * @param repeatCount The repeat count
	 */
	public void setRepeatCount(int repeatCount)
		{
		this.repeatCount = repeatCount;
		}

	/**
	 * Returns the macro recorder. If this is non-null, all executed
	 * actions should be forwarded to the recorder.
	 */
	public InputHandler.MacroRecorder getMacroRecorder()
		{
		return recorder;
		}

	/**
	 * Sets the macro recorder. If this is non-null, all executed
	 * actions should be forwarded to the recorder.
	 * @param recorder The macro recorder
	 */
	public void setMacroRecorder(InputHandler.MacroRecorder recorder)
		{
		this.recorder = recorder;
		}

	/**
	 * Executes the specified action, repeating and recording it as
	 * necessary.
	 * @param listener The action listener
	 * @param source The event source
	 * @param actionCommand The action command
	 */
	public void executeAction(ActionListener listener, Object source, String actionCommand)
		{
		// create event
		ActionEvent evt = new ActionEvent(source,ActionEvent.ACTION_PERFORMED,actionCommand);

		// don't do anything if the action is a wrapper
		// (like EditAction.Wrapper)
		if (listener instanceof Wrapper)
			{
			listener.actionPerformed(evt);
			return;
			}

		// remember old values, in case action changes them
		boolean r = repeat;
		int rc = getRepeatCount();

		// execute the action
		if (listener instanceof InputHandler.NonRepeatable)
			listener.actionPerformed(evt);
		else
			{
			for (int i = 0; i < Math.max(1,repeatCount); i++)
				listener.actionPerformed(evt);
			}

		// do recording. Notice that we do no recording whatsoever
		// for actions that grab keys
		if (grabAction == null)
			{
			if (recorder != null)
				{
				if (!(listener instanceof InputHandler.NonRecordable))
					{
					if (rc != 1) recorder.actionPerformed(REPEAT,String.valueOf(rc));

					recorder.actionPerformed(listener,actionCommand);
					}
				}

			// If repeat was true originally, clear it
			// Otherwise it might have been set by the action, etc
			if (r)
				{
				repeat = false;
				repeatCount = 0;
				}
			}
		}

	/**
	 * Returns the text area that fired the specified event.
	 * @param evt The event
	 */
	public static JEditTextArea getTextArea(EventObject evt)
		{
		if (evt != null)
			{
			Object o = evt.getSource();
			if (o instanceof Component)
				{
				// find the parent text area
				Component c = (Component) o;
				for (;;)
					{
					if (c instanceof JEditTextArea)
						return (JEditTextArea) c;
					else if (c == null) break;
					if (c instanceof JPopupMenu)
						c = ((JPopupMenu) c).getInvoker();
					else
						c = c.getParent();
					}
				}
			}

		// this shouldn't happen
		System.err.println("BUG: getTextArea() returning null");
		return null;
		}

	// protected members

	/**
	 * If a key is being grabbed, this method should be called with
	 * the appropriate key event. It executes the grab action with
	 * the typed character as the parameter.
	 */
	protected void handleGrabAction(KeyEvent evt)
		{
		// Clear it *before* it is executed so that executeAction()
		// resets the repeat count
		ActionListener ga = grabAction;
		grabAction = null;
		executeAction(ga,evt.getSource(),String.valueOf(evt.getKeyChar()));
		}

	// protected members
	protected ActionListener grabAction;
	protected boolean repeat;
	protected int repeatCount;
	protected InputHandler.MacroRecorder recorder;

	/**
	 * If an action implements this interface, it should not be repeated.
	 * Instead, it will handle the repetition itself.
	 */
	public interface NonRepeatable
		{ //Just an interface for identification
		}

	/**
	 * If an action implements this interface, it should not be recorded
	 * by the macro recorder. Instead, it will do its own recording.
	 */
	public interface NonRecordable
		{ //Just an interface for identification
		}

	/**
	 * For use by EditAction.Wrapper only.
	 * @since jEdit 2.2final
	 */
	public interface Wrapper
		{ //Just an interface for identification
		}

	/**
	 * Macro recorder.
	 */
	public interface MacroRecorder
		{
		void actionPerformed(ActionListener listener, String actionCommand);
		}

	public static class Backspace implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);

			if (!textArea.editable)
				{
				textArea.getToolkit().beep();
				return;
				}

			if (textArea.getSelectionStart() != textArea.getSelectionEnd())
				{
				textArea.setSelectedText("");
				}
			else
				{
				int caret = textArea.getCaretPosition();
				if (caret == 0)
					{
					textArea.getToolkit().beep();
					return;
					}
				try
					{
					textArea.getDocument().remove(caret - 1,1);
					}
				catch (BadLocationException bl)
					{
					bl.printStackTrace();
					}
				}
			}
		}

	public static class BackspaceWord implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int start = textArea.getSelectionStart();
			if (start != textArea.getSelectionEnd())
				{
				textArea.setSelectedText("");
				}

			int line = textArea.getCaretLine();
			int lineStart = textArea.getLineStartOffset(line);
			int caret = start - lineStart;

			String lineText = textArea.getLineText(textArea.getCaretLine());

			if (caret == 0)
				{
				if (lineStart == 0)
					{
					textArea.getToolkit().beep();
					return;
					}
				caret--;
				}
			else
				{
				String noWordSep = (String) textArea.getDocument().getProperty("noWordSep");
				caret = TextUtilities.findWordStart(lineText,caret,noWordSep);
				}

			try
				{
				textArea.getDocument().remove(caret + lineStart,start - (caret + lineStart));
				}
			catch (BadLocationException bl)
				{
				bl.printStackTrace();
				}
			}
		}

	public static class Delete implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);

			if (!textArea.editable)
				{
				textArea.getToolkit().beep();
				return;
				}

			if (textArea.getSelectionStart() != textArea.getSelectionEnd())
				{
				textArea.setSelectedText("");
				}
			else
				{
				int caret = textArea.getCaretPosition();
				if (caret == textArea.getDocumentLength())
					{
					textArea.getToolkit().beep();
					return;
					}
				try
					{
					textArea.getDocument().remove(caret,1);
					}
				catch (BadLocationException bl)
					{
					bl.printStackTrace();
					}
				}
			}
		}

	public static class DeleteWord implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int start = textArea.getSelectionStart();
			if (start != textArea.getSelectionEnd())
				{
				textArea.setSelectedText("");
				}

			int line = textArea.getCaretLine();
			int lineStart = textArea.getLineStartOffset(line);
			int caret = start - lineStart;

			String lineText = textArea.getLineText(textArea.getCaretLine());

			if (caret == lineText.length())
				{
				if (lineStart + caret == textArea.getDocumentLength())
					{
					textArea.getToolkit().beep();
					return;
					}
				caret++;
				}
			else
				{
				String noWordSep = (String) textArea.getDocument().getProperty("noWordSep");
				caret = TextUtilities.findWordEnd(lineText,caret,noWordSep);
				}

			try
				{
				textArea.getDocument().remove(start,(caret + lineStart) - start);
				}
			catch (BadLocationException bl)
				{
				bl.printStackTrace();
				}
			}
		}

	public static class SelectAll implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea eta = getTextArea(evt);
			eta.select(0,eta.getDocumentLength());
			}
		}

	public static class End implements ActionListener
		{
		private boolean select;

		public End(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int lastOfLine = textArea.getLineEndOffset(textArea.getCaretLine()) - 1;
			if (select)
				textArea.select(textArea.getMarkPosition(),lastOfLine);
			else
				textArea.setCaretPosition(lastOfLine);
			}
		}

	public static class DocumentEnd implements ActionListener
		{
		private boolean select;

		public DocumentEnd(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			if (select)
				textArea.select(textArea.getMarkPosition(),textArea.getDocumentLength());
			else
				textArea.setCaretPosition(textArea.getDocumentLength());
			}
		}

	public static class Home implements ActionListener
		{
		private boolean select;

		public Home(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int firstOfLine = textArea.getLineStartOffset(textArea.getCaretLine());
			char[] line = textArea.getLineText(textArea.getCaretLine()).toCharArray();
			int i;
			for (i = 0; i < line.length; i++)
				if (!Character.isWhitespace(line[i])) break;
			i += firstOfLine;

			if (caret != i)
				caret = i;
			else
				caret = firstOfLine;
			if (select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
			}
		}

	public static class DocumentHome implements ActionListener
		{
		private boolean select;

		public DocumentHome(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			if (select)
				textArea.select(textArea.getMarkPosition(),0);
			else
				textArea.setCaretPosition(0);
			}
		}

	public static class InsertBreak implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);

			if (!textArea.editable)
				{
				textArea.getToolkit().beep();
				return;
				}

			if (Boolean.TRUE.equals(textArea.getClientProperty(KEEP_INDENT_PROPERTY))
					&& textArea.getSelectedText() == null)
				{
				int caretLine = textArea.getCaretLine();
				int caretPos = textArea.getCaretPosition() - textArea.getLineStartOffset(caretLine);
				String indent = textArea.getLineText(caretLine).split("\\S",2)[0];
				if (indent.length() > caretPos) indent = indent.substring(0,caretPos);
				textArea.setSelectedText("\n" + indent);
				}
			else
				textArea.setSelectedText("\n");
			}
		}

	public static class InsertTab implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);

			if (!textArea.editable)
				{
				textArea.getToolkit().beep();
				return;
				}

			if (Boolean.TRUE.equals(textArea.getClientProperty(TAB_TO_INDENT_PROPERTY)))
				{
				int caretLine = textArea.getCaretLine();
				int caretLineStartOffset = textArea.getLineStartOffset(caretLine);
				int caretPos = textArea.getCaretPosition() - caretLineStartOffset;
				if (textArea.getSelectionEnd() != textArea.getSelectionStart())
					insertTab(textArea);
				else if (caretLine > 0)
					{
					String i1 = textArea.getLineText(caretLine - 1).split("\\S",2)[0];
					String i2 = textArea.getLineText(caretLine).split("\\S",2)[0];
					int i1w = textArea.offsetToX(caretLine - 1,i1.length());
					int i2w = textArea.offsetToX(caretLine,i2.length());
					int cx = textArea.offsetToX(caretLine,caretPos);
					if (caretPos <= i2.length() && cx < i1w)
						{
						String s = i1w > i2w ? i1 : i2;
						textArea.setSelectionStart(caretLineStartOffset);
						textArea.setSelectionEnd(caretLineStartOffset + i2.length());
						textArea.setSelectedText(s);
						}
					else
						insertTab(textArea);
					}
				else
					insertTab(textArea);
				}
			else
				insertTab(textArea);
			}

		private static void insertTab(JEditTextArea textArea)
			{
			if (Boolean.TRUE.equals(textArea.getClientProperty(CONVERT_TABS_PROPERTY)))
				{
				String key = PlainDocument.tabSizeAttribute;
				int tabSize = ((Integer) textArea.getDocument().getProperty(key)).intValue();
				String tab = "";
				for (int i = 0; i < tabSize; i++)
					tab += " ";
				int p = textArea.getCaretPosition() - textArea.getLineStartOffset(textArea.getCaretLine());
				textArea.setSelectedText(tab.substring(p % tab.length()));
				}
			else
				textArea.setSelectedText("\t");
			}
		}

	public static class NextChar implements ActionListener
		{
		private boolean select;

		public NextChar(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			if (caret == textArea.getDocumentLength())
				{
				textArea.getToolkit().beep();
				return;
				}

			if (select)
				textArea.select(textArea.getMarkPosition(),caret + 1);
			else
				textArea.setCaretPosition(caret + 1);
			}
		}

	public static class NextLine implements ActionListener
		{
		private boolean select;

		public NextLine(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();

			if (line == textArea.getLineCount() - 1)
				{
				textArea.getToolkit().beep();
				return;
				}

			if (textArea.magicCaret == -1)
				{
				textArea.magicCaret = textArea.offsetToX(line,caret - textArea.getLineStartOffset(line));
				}

			caret = textArea.getLineStartOffset(line + 1)
					+ textArea.xToOffset(line + 1,textArea.magicCaret);
			if (select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
			}
		}

	public static class NextPage implements ActionListener
		{
		private boolean select;

		public NextPage(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int lineCount = textArea.getLineCount();
			int firstLine = textArea.getFirstLine();
			int visibleLines = textArea.getVisibleLines();
			int line = textArea.getCaretLine();

			firstLine += visibleLines;

			if (firstLine + visibleLines >= lineCount - 1) firstLine = lineCount - visibleLines;

			textArea.setFirstLine(firstLine);

			int l = Math.min(textArea.getLineCount() - 1,line + visibleLines);
			int caret = textArea.getLineStartOffset(l);
			if (select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
			}
		}

	public static class NextWord implements ActionListener
		{
		private boolean select;

		public NextWord(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();
			int lineStart = textArea.getLineStartOffset(line);
			caret -= lineStart;

			String lineText = textArea.getLineText(textArea.getCaretLine());

			if (caret == lineText.length())
				{
				if (lineStart + caret == textArea.getDocumentLength())
					{
					textArea.getToolkit().beep();
					return;
					}
				caret++;
				}
			else
				{
				String noWordSep = (String) textArea.getDocument().getProperty("noWordSep");
				caret = TextUtilities.findWordEnd(lineText,caret,noWordSep);
				}

			if (select)
				textArea.select(textArea.getMarkPosition(),lineStart + caret);
			else
				textArea.setCaretPosition(lineStart + caret);
			}
		}

	public static class Overwrite implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			textArea.setOverwriteEnabled(!textArea.isOverwriteEnabled());
			}
		}

	public static class PrevChar implements ActionListener
		{
		private boolean select;

		public PrevChar(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			if (caret == 0)
				{
				textArea.getToolkit().beep();
				return;
				}

			if (select)
				textArea.select(textArea.getMarkPosition(),caret - 1);
			else
				textArea.setCaretPosition(caret - 1);
			}
		}

	public static class PrevLine implements ActionListener
		{
		private boolean select;

		public PrevLine(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();

			if (line == 0)
				{
				textArea.getToolkit().beep();
				return;
				}

			if (textArea.magicCaret == -1)
				{
				textArea.magicCaret = textArea.offsetToX(line,caret - textArea.getLineStartOffset(line));
				}

			caret = textArea.getLineStartOffset(line - 1)
					+ textArea.xToOffset(line - 1,textArea.magicCaret);
			if (select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
			}
		}

	public static class PrevPage implements ActionListener
		{
		private boolean select;

		public PrevPage(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int firstLine = textArea.getFirstLine();
			int visibleLines = textArea.getVisibleLines();
			int line = textArea.getCaretLine();

			if (firstLine < visibleLines) firstLine = visibleLines;

			textArea.setFirstLine(firstLine - visibleLines);

			int caret = textArea.getLineStartOffset(Math.max(0,line - visibleLines));
			if (select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
			}
		}

	public static class PrevWord implements ActionListener
		{
		private boolean select;

		public PrevWord(boolean select)
			{
			this.select = select;
			}

		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();
			int lineStart = textArea.getLineStartOffset(line);
			caret -= lineStart;

			String lineText = textArea.getLineText(textArea.getCaretLine());

			if (caret == 0)
				{
				if (lineStart == 0)
					{
					textArea.getToolkit().beep();
					return;
					}
				caret--;
				}
			else
				{
				String noWordSep = (String) textArea.getDocument().getProperty("noWordSep");
				caret = TextUtilities.findWordStart(lineText,caret,noWordSep);
				}

			if (select)
				textArea.select(textArea.getMarkPosition(),lineStart + caret);
			else
				textArea.setCaretPosition(lineStart + caret);
			}
		}

	public static class Repeat implements ActionListener,InputHandler.NonRecordable
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			textArea.inputHandler.setRepeatEnabled(true);
			String actionCommand = evt.getActionCommand();
			if (actionCommand != null)
				{
				textArea.inputHandler.setRepeatCount(Integer.parseInt(actionCommand));
				}
			}
		}

	public static class ToggleRect implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			textArea.setSelectionRectangular(!textArea.isSelectionRectangular());
			}
		}

	public static class InsertChar implements ActionListener,InputHandler.NonRepeatable
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			String str = evt.getActionCommand();
			int repeatCount = textArea.inputHandler.getRepeatCount();

			if (textArea.editable)
				{
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < repeatCount; i++)
					buf.append(str);
				textArea.overwriteSetSelectedText(buf.toString());
				}
			else
				{
				textArea.getToolkit().beep();
				}
			}
		}

	public static class Cut implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			textArea.cut();
			}
		}

	public static class Copy implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			textArea.copy();
			}
		}

	public static class Paste implements ActionListener
		{
		public void actionPerformed(ActionEvent evt)
			{
			JEditTextArea textArea = getTextArea(evt);
			textArea.paste();
			}
		}
	}
