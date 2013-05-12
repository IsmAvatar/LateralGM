/*
 * Copyright (C) 2013, Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

/*
 * Stolen from Font Family listener. Not sure what m_monitor was... 
 * 	String m_fontName = m_cbFonts.getSelectedItem().toString();
 * 	MutableAttributeSet attr = new SimpleAttributeSet();
 * 	StyleConstants.setFontFamily(attr,m_fontName);
 * 	// setAttributeSet(attr);
 * 	// m_monitor.grabFocus();
 * 
 * TODO: Add left, right, center text alignment
 */

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Extensions;
import org.lateralgm.resources.Extensions.PExtensions;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.util.PropertyMap;

public class ExtensionsFrame extends ResourceFrame<Extensions,Extensions.PExtensions>
{
	private static final long serialVersionUID = 1L;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	private CustomFileChooser fc;

	private JPanel makeSettings()
	{
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		return p;
	}

	public ExtensionsFrame(Extensions res)
	{
		this(res,null);
	}

	public ExtensionsFrame(Extensions res, ResNode node)
	{
		super(res,node,Messages.getString("ExtensionsFrame.TITLE"),true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(600,400);

	}

	private void addDocumentListeners()
	{

	}
	

	public Object getUserObject()
	{
		if (node != null) return node.getUserObject();
		for (int m = 0; m < LGM.root.getChildCount(); m++)
			{
			ResNode n = (ResNode) LGM.root.getChildAt(m);
			if (n.kind == Extensions.class) return n.getUserObject();
			}
		return 0;//Messages.getString("LGM.EXT"); //$NON-NLS-1$
	}
	
	public void actionPerformed(ActionEvent ev)
	{
		super.actionPerformed(ev);
		String com = ev.getActionCommand();
		if (com.equals("ExtensionsFrame.INSTALL")) //$NON-NLS-1$
		{
      return;
		}
		if (com.equals("ExtensionsFrame.SAVE")) //$NON-NLS-1$
		{
      return;
		}
	}

	public void commitChanges()
	{

	}

	public void setComponents(Extensions ext)
	{

	}

	@Override
	public String getConfirmationName()
	{
	  return (String) getUserObject();
	}

	@Override
	public boolean resourceChanged()
	{
		commitChanges();
		return !res.properties.equals(resOriginal.properties);
	}

	@Override
	public void revertResource()
	{
		res.properties.putAll(resOriginal.properties);
		//setComponents(res);
	}
}
