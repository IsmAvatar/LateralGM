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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.jedit.InputHandler;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Script;

public class ScriptFrame extends ResourceFrame<Script>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon FRAME_ICON = Script.ICON[Script.SCRIPT];
	public GMLTextArea code;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(600,400);
		setFrameIcon(FRAME_ICON);
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
		tool.add(makeToolbarButton(code.getUndoManager().getUndoAction()));
		tool.add(makeToolbarButton(code.getUndoManager().getRedoAction()));
		tool.addSeparator();
		tool.add(makeInputHandlerToolbarButton(InputHandler.CUT,"ScriptFrame.CUT"));
		tool.add(makeInputHandlerToolbarButton(InputHandler.COPY,"ScriptFrame.COPY"));
		tool.add(makeInputHandlerToolbarButton(InputHandler.PASTE,"ScriptFrame.PASTE"));
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(name);
		getContentPane().add(code);
		}

	public static JButton makeToolbarButton(Action a)
		{
		JButton b = new JButton(a);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setRequestFocusEnabled(false);
		return b;
		}

	@SuppressWarnings("serial")
	private JButton makeInputHandlerToolbarButton(final ActionListener l, String key)
		{
		Action a = new AbstractAction(Messages.getString(key),LGM.getIconForKey(key))
			{
				public void actionPerformed(ActionEvent e)
					{
					code.getInputHandler().executeAction(l,code,null);
					}
			};
		return makeToolbarButton(a);
		}

	public void revertResource()
		{
		LGM.currentFile.scripts.replace(res.getId(),resOriginal);
		}

	public void updateResource()
		{
		res.scriptStr = code.getTextCompat();
		res.setName(name.getText());
		resOriginal = res.copy();
		}

	public boolean resourceChanged()
		{
		return !code.getTextCompat().equals(resOriginal.scriptStr)
				|| !resOriginal.getName().equals(name.getText());
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
