/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.PlainDocument;

import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.file.ResourceList;
import org.lateralgm.jedit.DefaultInputHandler;
import org.lateralgm.jedit.GMLTokenMarker;
import org.lateralgm.jedit.InputHandler;
import org.lateralgm.jedit.JEditTextArea;
import org.lateralgm.jedit.KeywordMap;
import org.lateralgm.jedit.SyntaxDocument;
import org.lateralgm.jedit.Token;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

public class GMLTextArea extends JEditTextArea
	{
	private static final long serialVersionUID = 1L;
	private final GMLTokenMarker gmlTokenMarker = new GMLTokenMarker();
	public final ResourceChangeListener rcl = new ResourceChangeListener();
	private final DocumentUndoManager undoManager = new DocumentUndoManager();
	private static Timer timer;
	private Integer lastUpdateTaskID = 0;

	public GMLTextArea(String text)
		{
		super();
		setDocument(new SyntaxDocument());
		getDocument().getDocumentProperties().put(PlainDocument.tabSizeAttribute,Prefs.tabSize);
		updateTokenMarker();
		setTokenMarker(gmlTokenMarker);
		painter.setFont(Prefs.codeFont);
		painter.setStyles(PrefsStore.getSyntaxStyles());
		painter.setBracketHighlightColor(Color.gray);
		inputHandler = new DefaultInputHandler();
		inputHandler.addDefaultKeyBindings();
		putClientProperty(InputHandler.KEEP_INDENT_PROPERTY,Boolean.TRUE);
		putClientProperty(InputHandler.TAB_TO_INDENT_PROPERTY,Boolean.TRUE);
		putClientProperty(InputHandler.CONVERT_TABS_PROPERTY,Boolean.TRUE);
		putClientProperty(InputHandler.SMART_HOME_END_PROPERTY,Boolean.TRUE);
		text = text.replace("\r\n","\n"); //$NON-NLS-1$ //$NON-NLS-2$
		setText(text);
		setCaretPosition(0);
		LGM.currentFile.addChangeListener(rcl);
		addCaretListener(undoManager);
		document.addUndoableEditListener(undoManager);
		inputHandler.addKeyBinding("C+Z",undoManager.getUndoAction()); //$NON-NLS-1$
		inputHandler.addKeyBinding("C+Y",undoManager.getRedoAction()); //$NON-NLS-1$
		}

	private static JButton makeToolbarButton(Action a)
		{
		JButton b = new JButton(a);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setRequestFocusEnabled(false);
		return b;
		}

	private JButton makeInputHandlerToolbarButton(final ActionListener l, String key)
		{
		final GMLTextArea source = this;
		Action a = new AbstractAction(Messages.getString(key),LGM.getIconForKey(key))
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e)
					{
					getInputHandler().executeAction(l,source,null);
					}
			};
		return makeToolbarButton(a);
		}

	public void addEditorButtons(JToolBar tb)
		{
		tb.add(makeToolbarButton(getUndoManager().getUndoAction()));
		tb.add(makeToolbarButton(getUndoManager().getRedoAction()));
		tb.addSeparator();
		tb.add(makeInputHandlerToolbarButton(InputHandler.CUT,"GMLTextArea.CUT")); //$NON-NLS-1$
		tb.add(makeInputHandlerToolbarButton(InputHandler.COPY,"GMLTextArea.COPY")); //$NON-NLS-1$
		tb.add(makeInputHandlerToolbarButton(InputHandler.PASTE,"GMLTextArea.PASTE")); //$NON-NLS-1$
		}

	public DocumentUndoManager getUndoManager()
		{
		return undoManager;
		}

	public String getTextCompat()
		{
		String s = getText();
		s = s.replaceAll("\r?\n","\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return s;
		}

	public void updateTokenMarker()
		{
		KeywordMap km = new KeywordMap(false);
		int[] kmResources = { Resource.BACKGROUND,Resource.FONT,Resource.GMOBJECT,Resource.PATH,
				Resource.ROOM,Resource.SCRIPT,Resource.SOUND,Resource.SPRITE,Resource.TIMELINE };
		for (int j : kmResources)
			{
			ResourceList<?> rl = LGM.currentFile.getList(j);
			for (Resource<?> r : rl)
				{
				String n = r.getName();
				if (n.length() > 0) km.add(n,Token.KEYWORD3);
				}
			}
		gmlTokenMarker.setCustomKeywords(km);
		}

	private class ResourceChangeListener implements ChangeListener
		{
		public void stateChanged(ChangeEvent e)
			{
			if (timer == null) timer = new Timer();
			timer.schedule(new UpdateTask(),500);
			}
		}

	private class UpdateTask extends TimerTask
		{
		private int id;

		public UpdateTask()
			{
			synchronized (lastUpdateTaskID)
				{
				id = ++lastUpdateTaskID;
				}
			}

		public void run()
			{
			synchronized (lastUpdateTaskID)
				{
				if (id != lastUpdateTaskID) return;
				}
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
						{
						updateTokenMarker();
						int fl = getFirstLine();
						painter.invalidateLineRange(fl,fl + getVisibleLines());
						}
				});
			}
		}
	}
