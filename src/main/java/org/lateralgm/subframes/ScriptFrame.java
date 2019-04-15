/*
 * Copyright (C) 2007, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2006, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
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

import org.lateralgm.components.CodeTextArea;
import org.lateralgm.components.MarkerCache;
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

public class ScriptFrame extends InstantiableResourceFrame<Script,PScript>
	{
	private static final long serialVersionUID = 1L;
	public JToolBar tool;
	public CodeTextArea code;
	public JButton edit;
	public JPanel status;

	private ScriptEditor editor;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(700,430);
		setLayout(new BorderLayout());

		code = new CodeTextArea((String) res.get(PScript.CODE),MarkerCache.getMarker("gml"));
		add(code,BorderLayout.CENTER);

		// Setup the toolbar
		tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add(tool,BorderLayout.NORTH);

		tool.add(save);
		tool.addSeparator();

		if (Prefs.useExternalScriptEditor) {
			edit = new JButton(LGM.getIconForKey("ScriptFrame.EDIT")); //$NON-NLS-1$
			edit.setToolTipText(Messages.getString("ScriptFrame.EDIT"));
			edit.addActionListener(this);
			tool.add(edit);

			tool.addSeparator();
		}

		code.addEditorButtons(tool);

		tool.addSeparator();
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		status = new JPanel(new FlowLayout());
		BoxLayout layout = new BoxLayout(status,BoxLayout.X_AXIS);
		status.setLayout(layout);
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));
		final JLabel caretPos = new JLabel(" INS | UTF-8 | " + (code.getCaretLine() + 1) + " : "
				+ (code.getCaretColumn() + 1));
		status.add(caretPos);
		code.addCaretListener(new CaretListener()
			{
				public void caretUpdate(CaretEvent e)
					{
					caretPos.setText(" INS | UTF-8 | " + (code.getCaretLine() + 1) + " : "
							+ (code.getCaretColumn() + 1));
					}
			});
		add(status,BorderLayout.SOUTH);

		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(code.text));
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
				LGM.showDefaultExceptionHandler(ex);
				}
			}
		else
			{
			super.actionPerformed(e);
			}
		}

	private class ScriptEditor implements UpdateListener
		{
		private FileChangeMonitor monitor;
		private File f;

		public ScriptEditor() throws IOException
			{
			editor = this;
			start();
			}

		public void start() throws IOException
			{
			if (monitor != null)
				monitor.stop();

			if (f == null || !f.exists())
				{
				f = File.createTempFile(res.getName(),'.' + Prefs.externalScriptExtension,LGM.tempDir);
				f.deleteOnExit();
				}

			try (FileWriter out = new FileWriter(f))
				{
				out.write(code.getTextCompat());
				}

			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this,true);

			if (!Prefs.useExternalScriptEditor || Prefs.externalScriptEditorCommand == null)
				try
					{
					Desktop.getDesktop().edit(monitor.file);
					}
				catch (UnsupportedOperationException e)
					{
					LGM.showDefaultExceptionHandler(e);
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
					try (BufferedReader reader = new BufferedReader(new FileReader(monitor.file)))
						{
						char[] chars = new char[1024];
						int len = 0;
						while ((len = reader.read(chars)) > -1)
							sb.append(chars,0,len);
						}
					catch (IOException ioe)
						{
						LGM.showDefaultExceptionHandler(ioe);
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
