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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lateralgm.components.ResNode;
import org.lateralgm.file.ResourceList;
import org.lateralgm.jedit.GMLTokenMarker;
import org.lateralgm.jedit.JEditTextArea;
import org.lateralgm.jedit.KeywordMap;
import org.lateralgm.jedit.SyntaxDocument;
import org.lateralgm.jedit.SyntaxStyle;
import org.lateralgm.jedit.Token;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Script;

public class ScriptFrame extends ResourceFrame<Script>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon frameIcon = LGM.getIconForKey("ScriptFrame.SCRIPT"); //$NON-NLS-1$
	private static final ImageIcon saveIcon = LGM.getIconForKey("ScriptFrame.SAVE"); //$NON-NLS-1$
	private final GMLTokenMarker gmlTokenMarker = new GMLTokenMarker();
	public JEditTextArea code;

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
		updateTokenMarker();
		code = new JEditTextArea();
		code.setDocument(new SyntaxDocument());
		code.setTokenMarker(gmlTokenMarker);
		code.getPainter().setFont(Prefs.codeFont);
		code.setText(res.ScriptStr.replace("\r\n","\n"));
		code.setCaretPosition(0);
		code.getPainter().setStyles(PrefsStore.getSyntaxStyles());
		code.getPainter().setBracketHighlightColor(Color.gray);
		getContentPane().add(code);
		addInternalFrameListener(new ScriptFrameListener());
		}

	public void updateTokenMarker()
		{
		KeywordMap km = new KeywordMap(false);
		int[] kmResources = { Resource.BACKGROUND,Resource.FONT,Resource.GMOBJECT,Resource.PATH,Resource.ROOM,
				Resource.SCRIPT,Resource.SOUND,Resource.SPRITE,Resource.TIMELINE };
		for (int j : kmResources)
			{
			ResourceList rl = LGM.currentFile.getList(j);
			for (int i = 0; i < rl.count(); i++)
				{
				km.add(rl.getList(i).name,Token.KEYWORD3);
				}
			}
		gmlTokenMarker.setCustomKeywords(km);
		}

	public void revertResource()
		{
		LGM.currentFile.Scripts.replace(res.Id,resOriginal);
		}

	public void updateResource()
		{
		res.ScriptStr = code.getText().replaceAll("\r?\n","\r\n");
		res.name = name.getText();
		resOriginal = (Script) res.copy(false,null);
		}

	public boolean resourceChanged()
		{
		return (!code.getText().equals(res.ScriptStr.replace("\r\n","\n")))
				|| (!res.name.equals(resOriginal.name));
		}

	private class ScriptFrameListener implements InternalFrameListener
		{
		public void internalFrameActivated(InternalFrameEvent e)
			{
			code.grabFocus();
			}

		public void internalFrameClosed(InternalFrameEvent e)
			{
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