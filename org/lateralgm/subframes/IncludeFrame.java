/**
* @file  IncludeFrame.java
* @brief Class implementing the instantiable include frame
*
* @section License
*
* Copyright (C) 2014 Robert B. Colton
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

import java.awt.event.ActionEvent;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.resources.ExtensionPackages;
import org.lateralgm.resources.Include;

public class IncludeFrame extends InstantiableResourceFrame<Include,Include.PInclude>
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

	public IncludeFrame(Include res)
	{
		this(res,null);
	}

	public IncludeFrame(Include r, ResNode node)
	{
		//,Messages.getString("IncludeFrame.TITLE"),true
		super(r,node); //$NON-NLS-1$
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
			if (n.kind == ExtensionPackages.class) return n.getUserObject();
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

	public void setComponents(ExtensionPackages ext)
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
