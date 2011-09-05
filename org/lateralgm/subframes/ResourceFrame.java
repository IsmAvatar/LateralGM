/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.ExceptionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lateralgm.components.impl.NameDocument;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.RevertableMDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;

/** Provides common functionality and structure to Resource editing frames */
public abstract class ResourceFrame<R extends Resource<R,P>, P extends Enum<P>> extends
		RevertableMDIFrame implements DocumentListener,ActionListener,ExceptionListener
	{
	private static final long serialVersionUID = 1L;
	/**
	 * The Resource's name - setup automatically to update the title of the frame and
	 * the ResNode's text
	 */
	public final JTextField name = new JTextField();
	/** Automatically set up to save and close the frame */
	public final JButton save = new JButton();
	/** The resource this frame is editing (feel free to change it as you wish) */
	public R res;
	/** Backup of res as it was before changes were made */
	public R resOriginal;
	/** The ResNode this frame is linked to */
	public final ResNode node;

	public String titlePrefix = ""; //$NON-NLS-1$
	public String titleSuffix = ""; //$NON-NLS-1$

	protected final PropertyLinkFactory<P> plf;

	/**
	 * Note for inheriting classes. Be sure to call this parent instantiation for proper setup.
	 * The res and node parameters are only needed in the instantiation to assign globals;
	 * That is, once you call this, they will immediately gain global scope and may be treated thusly.
	 */
	public ResourceFrame(R res, ResNode node)
		{
		super(res.getName(),true);
		plf = new PropertyLinkFactory<P>(res.properties,this);
		this.res = res;
		this.node = node;
		resOriginal = res.clone();
		setFrameIcon(ResNode.ICON.get(res.getKind()));
		name.setDocument(new NameDocument());
		name.setText(res.getName());
		name.getDocument().addDocumentListener(this);
		name.setCaretPosition(0);
		save.setToolTipText(Messages.getString("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(LGM.getIconForKey("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.addActionListener(this);
		}

	public String getConfirmationName()
		{
		return res.getName();
		}

	public boolean resourceChanged()
		{
		commitChanges();
		if (!areResourceFieldsEqual()) return true;
		return !res.equals(resOriginal);
		}

	/** Override to check additional fields other than the Resource<> defaults. */
	protected boolean areResourceFieldsEqual()
		{
		return true;
		}

	public void updateResource()
		{
		commitChanges();
		resOriginal = res.clone();
		}

	public void revertResource()
		{
		resOriginal.updateReference();
		}

	public abstract void commitChanges();

	public static void addGap(Container c, int w, int h)
		{
		JLabel l = new JLabel();
		l.setPreferredSize(new Dimension(w,h));
		c.add(l);
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

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == save)
			{
			updateResource();
			dispose();
			}
		}

	public void exceptionThrown(Exception e)
		{
		e.printStackTrace();
		}

	public void setTitle(String title)
		{
		super.setTitle(titlePrefix + title + titleSuffix);
		}

	public void dispose()
		{
		super.dispose();
		node.frame = null; // allows a new frame to open
		name.getDocument().removeDocumentListener(this);
		save.removeActionListener(this);
		removeAll();
		}
	}
