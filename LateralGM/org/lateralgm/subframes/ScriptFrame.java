/*
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.lateralgm.components.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.Script;

public class ScriptFrame extends ResourceFrame<Script>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon frameIcon = LGM.getIconForKey("ScriptFrame.SCRIPT"); //$NON-NLS-1$
	private static final ImageIcon saveIcon = LGM.getIconForKey("ScriptFrame.SAVE"); //$NON-NLS-1$
	public JTextArea code;

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
		code = new JTextArea();
		code.setFont(Prefs.codeFont);
		code.setText(res.ScriptStr);
		JScrollPane codePane = new JScrollPane(code);
		getContentPane().add(codePane,BorderLayout.CENTER);
		}

	public void revertResource()
		{
		LGM.currentFile.Scripts.replace(res.Id,resOriginal);
		}

	public void updateResource()
		{
		res.ScriptStr = code.getText();
		res.name = name.getText();
		resOriginal = (Script) res.copy(false,null);
		}

	public boolean resourceChanged()
		{
		return (!code.getText().equals(res.ScriptStr)) || (!res.name.equals(resOriginal.name));
		}
	}