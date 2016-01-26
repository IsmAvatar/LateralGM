/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 *
 * This file is part of LateralGM.
 *
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.subframes;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lateralgm.components.impl.NameDocument;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.resources.InstantiableResource;

/** Provides common functionality and structure to Resource editing frames */
public abstract class InstantiableResourceFrame<R extends InstantiableResource<R,P>, P extends Enum<P>>
		extends ResourceFrame<R,P> implements DocumentListener
	{
	private static final long serialVersionUID = 1L;
	/**
	 * The Resource's name - setup automatically to update the title of the frame and
	 * the ResNode's text
	 */
	public final JTextField name = new JTextField();

	/**
	 * Note for inheriting classes. Be sure to call this parent instantiation for proper setup.
	 * The res and node parameters are only needed in the instantiation to assign globals;
	 * That is, once you call this, they will immediately gain global scope and may be treated thusly.
	 */
	public InstantiableResourceFrame(R res, ResNode node)
		{
		super(res,node);
		name.setDocument(new NameDocument());
		name.setText(res.getName());
		name.getDocument().addDocumentListener(this);
		name.setCaretPosition(0);
		}

	public void changedUpdate(DocumentEvent e)
		{
		// Not used
		}

	public void insertUpdate(DocumentEvent e)
		{
		if (e.getDocument() == name.getDocument()) res.setName(name.getText());
		}

	public void removeUpdate(DocumentEvent e)
		{
		if (e.getDocument() == name.getDocument()) res.setName(name.getText());
		}

	public void dispose()
		{
		super.dispose();
		name.getDocument().removeDocumentListener(this);
		}
	}
