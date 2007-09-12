/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Script;

public class ScriptFrame extends ResourceFrame<Script>
	{
	private static final long serialVersionUID = 1L;
	public GMLTextArea code;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(600,400);
		// the code text area
		code = new GMLTextArea(res.scriptStr);
		// Setup the toolbar
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add("North",tool); //$NON-NLS-1$
		// Setup the buttons
		tool.add(save);
		tool.addSeparator();
		code.addEditorButtons(tool);
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(name);
		getContentPane().add(code);
		}

	public void revertResource()
		{
		LGM.currentFile.scripts.replace(res,resOriginal);
		}
	
	public void commitChanges()
		{
		res.scriptStr = code.getTextCompat();
		res.setName(name.getText());
		}

	public boolean resourceChanged()
		{
		return code.getUndoManager().isModified();
		//return !code.getTextCompat().equals(resOriginal.scriptStr)
		//		|| !resOriginal.getName().equals(name.getText());
		}

	public void fireInternalFrameEvent(int id)
		{
		switch (id)
			{
			case InternalFrameEvent.INTERNAL_FRAME_CLOSED:
				LGM.currentFile.removeChangeListener(code.rcl);
				break;
			case InternalFrameEvent.INTERNAL_FRAME_ACTIVATED:
				code.grabFocus();
				break;
			default:
				break;
			}
		super.fireInternalFrameEvent(id);
		}
	}
