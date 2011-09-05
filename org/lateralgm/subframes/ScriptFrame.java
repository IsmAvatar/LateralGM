/*
 * Copyright (C) 2007, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2006, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Script.PScript;
import org.lateralgm.ui.swing.util.SwingExecutor;

public class ScriptFrame extends ResourceFrame<Script,PScript> implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	public JToolBar tool;
	public GMLTextArea code;
	public JButton edit;
	public JPanel status;

	private ScriptEditor editor;

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

		code = new GMLTextArea((String) res.get(PScript.CODE));
		add(code,BorderLayout.CENTER);

		if (!Prefs.useExternalScriptEditor)
			code.addEditorButtons(tool);
		else
			{
			code.editable = false;
			edit = new JButton(Messages.getString("ScriptFrame.EDIT")); //$NON-NLS-1$
			edit.addActionListener(this);
			tool.add(edit);
			}

		tool.addSeparator();
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		status = new JPanel(new FlowLayout());
		status.setLayout(new BoxLayout(status,BoxLayout.X_AXIS));
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));
		final JLabel caretPos = new JLabel((code.getCaretLine() + 1) + ":"
				+ (code.getCaretColumn() + 1));
		status.add(caretPos);
		code.addCaretListener(new CaretListener()
			{
				public void caretUpdate(CaretEvent e)
					{
					caretPos.setText((code.getCaretLine() + 1) + ":" + (code.getCaretColumn() + 1));
					}
			});
		add(status,BorderLayout.SOUTH);

		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(code));
		}

	public void commitChanges()
		{
		res.put(PScript.CODE,code.getTextCompat());
		res.setName(name.getText());
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
			try
				{
				if (editor == null)
					new ScriptEditor();
				else
					editor.start();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			return;
			}
		super.actionPerformed(e);
		}

	private class ScriptEditor implements UpdateListener
		{
		public final FileChangeMonitor monitor;

		public ScriptEditor() throws IOException
			{
			File f = File.createTempFile(res.getName(),"." + Prefs.externalScriptExtension,LGM.tempDir); //$NON-NLS-1$
			f.deleteOnExit();
			FileWriter out = new FileWriter(f);
			out.write((String) res.get(PScript.CODE));
			out.close();
			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this,true);
			editor = this;
			start();
			}

		public void start() throws IOException
			{
			if (!Prefs.useExternalScriptEditor || Prefs.externalScriptEditorCommand == null)
				try
					{
					System.out.println(Desktop.getDesktop());
					//					Desktop d = Desktop.getDesktop();
					//					Desktop.Action.EDIT;
					//					Toolkit.getDefaultToolkit().createDesktopPeer(d);
					Desktop.getDesktop().edit(monitor.file);
					}
				catch (UnsupportedOperationException e)
					{
					throw new UnsupportedOperationException("no internal or system script editor",e);
					}
			else
				Runtime.getRuntime().exec(
						String.format(Prefs.externalScriptEditorCommand,monitor.file.getAbsolutePath()));
			}

		public void stop()
			{
			monitor.stop();
			monitor.file.delete();
			editor = null;
			}

		public void updated(UpdateEvent e)
			{
			if (!(e instanceof FileUpdateEvent)) return;
			switch (((FileUpdateEvent) e).flag)
				{
				case CHANGED:
					StringBuffer sb = new StringBuffer(1024);
					try
						{
						BufferedReader reader = new BufferedReader(new FileReader(monitor.file));
						char[] chars = new char[1024];
						int len = 0;
						while ((len = reader.read(chars)) > -1)
							sb.append(chars,0,len);
						reader.close();
						}
					catch (IOException ioe)
						{
						ioe.printStackTrace();
						return;
						}
					String s = sb.toString();
					res.put(PScript.CODE,s);
					code.setText(s);
					break;
				case DELETED:
					editor = null;
				}
			}
		}

	public void dispose()
		{
		if (editor != null) editor.stop();
		super.dispose();
		}
	}
