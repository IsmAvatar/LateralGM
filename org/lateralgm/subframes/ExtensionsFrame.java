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

import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Extensions;

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
