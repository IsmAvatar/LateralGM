/**
* @file  ShaderFrame.java
* @brief Class implementing the shader frame and a tabbed CodeTextArea
*
* @section License
*
* Copyright (C) 2013-2014 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.CodeTextArea;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.joshedit.FindDialog;
import org.lateralgm.joshedit.TokenMarker;
import org.lateralgm.joshedit.lexers.DefaultTokenMarker;
import org.lateralgm.joshedit.lexers.GLESTokenMarker;
import org.lateralgm.joshedit.lexers.GLSLTokenMarker;
import org.lateralgm.joshedit.lexers.HLSLTokenMarker;
import org.lateralgm.joshedit.lexers.MarkerCache;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Shader.PShader;
import org.lateralgm.ui.swing.util.SwingExecutor;

public class ShaderFrame extends InstantiableResourceFrame<Shader,PShader>
	{
	private static final long serialVersionUID = 1L;
	public JToolBar tool;
	JTabbedPane editors;
	public CodeTextArea vcode;
	public CodeTextArea fcode;
	public JButton edit;
	public JPanel status;
	public JCheckBox precompileCB;
	public JComboBox typeCombo;
	public String currentLang = "";

	private ScriptEditor editor;

	public ShaderFrame(Shader res, ResNode node)
		{
		super(res,node);
		setSize(700,430);
		setLayout(new BorderLayout());

		// Setup the toolbar
		tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add(tool,BorderLayout.NORTH);

		tool.add(save);
		tool.addSeparator();

		vcode = new CodeTextArea((String) res.get(PShader.VERTEX), MarkerCache.getMarker("glsles"));
		fcode = new CodeTextArea((String) res.get(PShader.FRAGMENT), MarkerCache.getMarker("glsles"));
		
		editors = new JTabbedPane();
		editors.add(vcode, "Vertex");
		editors.add(fcode, "Fragment");
		add(editors,BorderLayout.CENTER);

		if (!Prefs.useExternalScriptEditor)
			this.addEditorButtons(tool);
		else
			{
			//vcode.editable = false;
			//fcode.editable = false;
			edit = new JButton(Messages.getString("ShaderFrame.EDIT")); //$NON-NLS-1$
			edit.addActionListener(this);
			tool.add(edit);
			}

		tool.addSeparator();
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("ShaderFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("ShaderFrame.TYPE")));
		String[] typeOptions = { "GLSLES", "GLSL", "HLSL9", "HLSL11" };
		typeCombo = new JComboBox(typeOptions);
		typeCombo.setMaximumSize(new Dimension(100, 20));
		typeCombo.addItemListener (new ItemListener() {
			public void itemStateChanged(ItemEvent arg0)
				{
					updateLexer();
				}
			});
		
		tool.add(typeCombo);
		precompileCB = new JCheckBox(Messages.getString("ShaderFrame.PRECOMPILE"));
		precompileCB.setSelected(res.getPrecompile());
		tool.addSeparator();
		tool.add(precompileCB);
		
		status = new JPanel(new FlowLayout());
		BoxLayout layout = new BoxLayout(status,BoxLayout.X_AXIS);
		status.setLayout(layout);
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));
		final JLabel caretPos = new JLabel(" INS | UTF-8 | " + (vcode.getCaretLine() + 1) + " : "
				+ (vcode.getCaretColumn() + 1));
		status.add(caretPos);
		vcode.addCaretListener(new CaretListener()
			{
				public void caretUpdate(CaretEvent e)
					{
					caretPos.setText(" INS | UTF-8 | " + (vcode.getCaretLine() + 1) + " : " + (vcode.getCaretColumn() + 1));
					}
			});
		fcode.addCaretListener(new CaretListener()
			{
				public void caretUpdate(CaretEvent e)
					{
					caretPos.setText(" INS | UTF-8 | " + (fcode.getCaretLine() + 1) + " : " + (fcode.getCaretColumn() + 1));
					}
			});
		add(status,BorderLayout.SOUTH);

		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(vcode.text));
		
		typeCombo.setSelectedItem(res.getType());
		updateLexer();
		}

	private void updateLexer()
		{
		//TODO: This should be moved into the base CodeTextArea, as a feature of JoshEdit
		String val = typeCombo.getSelectedItem().toString();
		if (val.equals(currentLang)) { return; }
		DefaultTokenMarker marker = null;
		if (val.equals("GLSLES")) {
			marker = new GLESTokenMarker();
		} else if (val.equals("GLSL")) {
			marker = new GLSLTokenMarker();
		} else if (val.equals("HLSL9")) {
		  marker = new HLSLTokenMarker();
		} else if (val.equals("HLSL11")) {
		  marker = new HLSLTokenMarker();
		} else {
		
		}
		//TODO: Both of these calls will utilize the same lexer, but they both
		//will recompose the list of completions. Should possibly add an abstract
		//GetCompletions() to the DefaultTokenMarker class, so they all code editors
		//can utilize the same completions list to save memory.
		vcode.setTokenMarker(marker);
		fcode.setTokenMarker(marker);
		currentLang = val;
		repaint();
		}

	public void commitChanges()
		{
		res.put(PShader.VERTEX,vcode.getTextCompat());
		res.put(PShader.FRAGMENT,fcode.getTextCompat());
		res.setName(name.getText());
		res.put(PShader.TYPE, typeCombo.getSelectedItem());
		res.put(PShader.PRECOMPILE, precompileCB.isSelected());
		}

	public void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSED)
			LGM.currentFile.updateSource.removeListener(vcode);
		  LGM.currentFile.updateSource.removeListener(fcode);
		super.fireInternalFrameEvent(id);
		}

	private class ScriptEditor implements UpdateListener
		{
		public final FileChangeMonitor monitor;

		public ScriptEditor() throws IOException
			{
			File f = File.createTempFile(res.getName(),"." + Prefs.externalScriptExtension,LGM.tempDir); //$NON-NLS-1$
			f.deleteOnExit();
			FileWriter out = new FileWriter(f);
			out.write((String) res.get(PShader.VERTEX));
			out.write((String) res.get(PShader.FRAGMENT));
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
					res.put(PShader.VERTEX,s);
					vcode.setText(s);
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
	
	private JButton makeToolbarButton(String name)
		{
		String key = "JoshText." + name;
		JButton b = new JButton(LGM.getIconForKey(key));
		b.setToolTipText(Messages.getString(key));
		b.setRequestFocusEnabled(false);
		b.setActionCommand(key);
		b.addActionListener(this);
		return b;
	}
	
	public void addEditorButtons(JToolBar tb)
	{
	  
		tb.add(makeToolbarButton("SAVE"));
		tb.add(makeToolbarButton("LOAD"));
		tb.add(makeToolbarButton("PRINT"));
		tb.addSeparator();
		tb.add(makeToolbarButton("UNDO"));
		tb.add(makeToolbarButton("REDO"));
		tb.addSeparator();
		tb.add(makeToolbarButton("FIND"));
		tb.add(makeToolbarButton("GOTO"));
		tb.addSeparator();
		tb.add(makeToolbarButton("CUT"));
		tb.add(makeToolbarButton("COPY"));
		tb.add(makeToolbarButton("PASTE"));
	}
	
	public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			if (ev.getSource() == edit)
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
		
			String com = ev.getActionCommand();
			
			CodeTextArea tcode = null;
			int stab = editors.getSelectedIndex(); 
			
			if (stab == 0) {
				tcode = vcode;
			} else if (stab == 1) {
			  tcode = fcode;
			} else {
				// dun know what fucking tab u have selected
			}
			
			if (com.equals("JoshText.LOAD")) {
				tcode.text.aLoad();
			} else if (com.equals("JoshText.SAVE")) {
				tcode.text.aSave();
			} else if (com.equals("JoshText.UNDO")) {
				tcode.text.aUndo();
			} else if (com.equals("JoshText.REDO")) {
				tcode.text.aRedo();
			} else if (com.equals("JoshText.CUT")) {
				tcode.text.aCut();
			} else if (com.equals("JoshText.COPY")) {
				tcode.text.aCopy();
			} else if (com.equals("JoshText.PASTE")) {
				tcode.text.aPaste();
			} else if (com.equals("JoshText.FIND")) {
				tcode.text.aFind();
			} else if (com.equals("JoshText.GOTO")) {
				tcode.aGoto();
			} else if (com.equals("JoshText.SELALL")) {
				tcode.text.aSelAll();
			}
		}
	}
