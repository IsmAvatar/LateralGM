/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.PlainDocument;

import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.file.ResourceList;
import org.lateralgm.jedit.GMLTokenMarker;
import org.lateralgm.jedit.InputHandler;
import org.lateralgm.jedit.JEditTextArea;
import org.lateralgm.jedit.KeywordMap;
import org.lateralgm.jedit.SyntaxDocument;
import org.lateralgm.jedit.Token;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Resource;

public class GMLTextArea extends JEditTextArea
	{
	private static final long serialVersionUID = 1L;
	private final GMLTokenMarker gmlTokenMarker = new GMLTokenMarker();
	public final ResourceChangeListener rcl = new ResourceChangeListener();
	private final DocumentUndoManager undoManager = new DocumentUndoManager();
	private Timer timer;
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
		putClientProperty(InputHandler.KEEP_INDENT_PROPERTY,Boolean.TRUE);
		putClientProperty(InputHandler.SMART_HOME_END_PROPERTY,Boolean.TRUE);
		text = text.replace("\r\n","\n");
		text = Util.convertIndents(text);
		setText(text);
		setCaretPosition(0);
		LGM.currentFile.addChangeListener(rcl);
		addCaretListener(undoManager);
		document.addUndoableEditListener(undoManager);
		inputHandler.addKeyBinding("C+Z",undoManager.getUndoAction());
		inputHandler.addKeyBinding("C+Y",undoManager.getRedoAction());
		}

	public DocumentUndoManager getUndoManager()
		{
		return undoManager;
		}

	public String getTextCompat()
		{
		String s = getText();
		s = s.replaceAll("\r?\n","\r\n");
		String tab = "";
		for (int i = 0; i < Prefs.tabSize; i++)
			tab += " ";
		s = s.replaceAll("^\t*\t",tab);
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
			for (int i = 0; i < rl.count(); i++)
				{
				String n = rl.getList(i).getName();
				if (n.length() > 0) km.add(n,Token.KEYWORD3);
				}
			}
		gmlTokenMarker.setCustomKeywords(km);
		}

	private class ResourceChangeListener implements ChangeListener
		{
		public void stateChanged(ChangeEvent e)
			{
			if (timer != null) timer.cancel();
			timer = new Timer();
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
