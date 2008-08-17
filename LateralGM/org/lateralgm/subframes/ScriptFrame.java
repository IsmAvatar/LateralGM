/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Script;

public class ScriptFrame extends ResourceFrame<Script> implements ActionListener,ChangeListener
	{
	private static final long serialVersionUID = 1L;
	public JToolBar tool;
	public GMLTextArea code;
	public JButton edit;
	public File extFile;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(600,400);
		setLayout(new BorderLayout());

		// Setup the toolbar
		tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add(tool,BorderLayout.NORTH);

		tool.add(save);
		tool.addSeparator();

		code = new GMLTextArea(res.scriptStr);
		add(code,BorderLayout.CENTER);

		if (!Prefs.useExternalScriptEditor)
			code.addEditorButtons(tool);
		else
			{
			code.setEditable(false);
			edit = new JButton(Messages.getString("ScriptFrame.EDIT")); //$NON-NLS-1$
			edit.addActionListener(this);
			tool.add(edit);
			}

		tool.addSeparator();
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(code));
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
		return (Prefs.useExternalScriptEditor ? !res.scriptStr.equals(resOriginal.scriptStr)
				: code.getUndoManager().isModified())
				|| !resOriginal.getName().equals(name.getText());
		//return !code.getTextCompat().equals(resOriginal.scriptStr)
		//		|| !resOriginal.getName().equals(name.getText());
		}

	public void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSED)
			LGM.currentFile.updateSource.removeListener(code);
		super.fireInternalFrameEvent(id);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == edit)
			{
			if (Prefs.useExternalScriptEditor)
				{
				try
					{
					if (extFile == null)
						{
						extFile = File.createTempFile(res.getName(),".gml",LGM.tempDir);
						extFile.deleteOnExit();
						FileWriter out = new FileWriter(extFile);
						out.write(res.scriptStr);
						out.close();
						FileChangeMonitor fcm = new FileChangeMonitor(extFile);
						fcm.addChangeListener(this);
						fcm.start();
						}
					Runtime.getRuntime().exec(
							String.format(Prefs.externalScriptEditorCommand,extFile.getAbsolutePath()));
					}
				catch (Exception ex)
					{
					ex.printStackTrace();
					}
				}
			return;
			}
		super.actionPerformed(e);
		}

	public void stateChanged(ChangeEvent e)
		{
		if (e.getSource() instanceof FileChangeMonitor)
			{
			int flag = ((FileChangeMonitor) e.getSource()).getFlag();
			if (flag == FileChangeMonitor.FLAG_CHANGED)
				{
				try
					{
					StringBuffer sb = new StringBuffer(1024);
					BufferedReader reader = new BufferedReader(new FileReader(extFile));
					char[] chars = new char[1024];
					int len = 0;
					while ((len = reader.read(chars)) > -1)
						sb.append(String.valueOf(chars,0,len));
					reader.close();
					res.scriptStr = sb.toString();
					SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
								{
								code.setText(res.scriptStr);
								}
						});
					}
				catch (Exception ex)
					{
					ex.printStackTrace();
					}
				}
			else if (flag == FileChangeMonitor.FLAG_DELETED)
				{
				extFile = null;
				}
			}
		}

	public void dispose()
		{
		try
			{
			extFile.delete();
			}
		catch (Exception e)
			{
			}
		super.dispose();
		}
	}
