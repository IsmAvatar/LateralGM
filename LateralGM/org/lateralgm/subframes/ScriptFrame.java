/*
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lateralgm.components.DocumentUndoManager;
import org.lateralgm.components.ResNode;
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
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Script;

public class ScriptFrame extends ResourceFrame<Script>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon frameIcon = LGM.getIconForKey("ScriptFrame.SCRIPT"); //$NON-NLS-1$
	private static final ImageIcon saveIcon = LGM.getIconForKey("ScriptFrame.SAVE"); //$NON-NLS-1$
	public GMLTextArea code;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(600,400);
		setFrameIcon(frameIcon);
		// Setup the toolbar
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add("North",tool); //$NON-NLS-1$
		// Setup the buttons
		save.setIcon(saveIcon);
		tool.add(save);
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(name);
		// the code text area
		code = new GMLTextArea();
		getContentPane().add(code);
		addInternalFrameListener(new ScriptFrameListener());
		}

	public void revertResource()
		{
		LGM.currentFile.scripts.replace(res.getId(),resOriginal);
		}

	public void updateResource()
		{
		res.scriptStr = code.getText().replaceAll("\r?\n","\r\n");
		res.setName(name.getText());
		resOriginal = (Script) res.copy(false,null);
		}

	public boolean resourceChanged()
		{
		return (!code.getText().equals(res.scriptStr.replace("\r\n","\n")))
				|| (!res.getName().equals(resOriginal.getName()));
		}

	public class GMLTextArea extends JEditTextArea
		{
		private static final long serialVersionUID = 1L;
		private final GMLTokenMarker gmlTokenMarker = new GMLTokenMarker();
		private final ResourceChangeListener rcl = new ResourceChangeListener();
		private final DocumentUndoManager undoManager = new DocumentUndoManager();
		private Timer timer;
		private Integer lastUpdateTaskID = 0;
		private int caretUpdates = 0;

		public GMLTextArea()
			{
			super();
			setDocument(new SyntaxDocument());
			updateTokenMarker();
			setTokenMarker(gmlTokenMarker);
			painter.setFont(Prefs.codeFont);
			painter.setStyles(PrefsStore.getSyntaxStyles());
			painter.setBracketHighlightColor(Color.gray);
			putClientProperty(InputHandler.KEEP_INDENT_PROPERTY,Boolean.TRUE);
			putClientProperty(InputHandler.SMART_HOME_END_PROPERTY,Boolean.TRUE);
			setText(res.scriptStr.replace("\r\n","\n"));
			setCaretPosition(0);
			LGM.currentFile.addChangeListener(rcl);
			addCaretListener(undoManager);
			document.addUndoableEditListener(undoManager);
			inputHandler.addKeyBinding("C+Z",undoManager.getUndoAction());
			inputHandler.addKeyBinding("C+Y",undoManager.getRedoAction());
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

	private class ScriptFrameListener implements InternalFrameListener
		{
		public void internalFrameActivated(InternalFrameEvent e)
			{
			code.grabFocus();
			}

		public void internalFrameClosed(InternalFrameEvent e)
			{
			LGM.currentFile.removeChangeListener(code.rcl);
			}

		public void internalFrameClosing(InternalFrameEvent e)
			{
			}

		public void internalFrameDeactivated(InternalFrameEvent e)
			{
			}

		public void internalFrameDeiconified(InternalFrameEvent e)
			{
			}

		public void internalFrameIconified(InternalFrameEvent e)
			{
			}

		public void internalFrameOpened(InternalFrameEvent e)
			{
			}
		}
	}
