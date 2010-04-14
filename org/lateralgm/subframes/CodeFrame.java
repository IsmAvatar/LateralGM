/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;

public class CodeFrame extends MDIFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public interface CodeHolder
		{
		String getCode();

		void setCode(String s);
		}

	public void commit()
		{
		code.setCode(gta.getTextCompat());
		}

	public void setTitleFormatArg(Object arg)
		{
		this.arg = arg;
		setTitle(Messages.format(format,arg));
		}

	public boolean isChanged()
		{
		return gta.getUndoManager().isModified();
		}

	public final CodeHolder code;
	final GMLTextArea gta;
	private final String format;
	private Object arg;
	private final JButton save;

	public CodeFrame(CodeHolder code, String format, Object arg)
		{
		super(Messages.format(format,arg),true,true,true,true);
		this.code = code;
		this.format = format;
		this.arg = arg;
		setSize(600,400);
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		// the code text area
		gta = new GMLTextArea(code.getCode());
		// Setup the toolbar
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add("North",tool); //$NON-NLS-1$
		// Setup the buttons
		save = new JButton(LGM.getIconForKey("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.addActionListener(this);
		tool.add(save);
		tool.addSeparator();
		gta.addEditorButtons(tool);
		getContentPane().add(gta);
		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(gta));
		}

	public void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING)
			{
			if (isChanged())
				{
				int res = JOptionPane.showConfirmDialog(getParent(),Messages.format(
						"RoomFrame.CODE_CHANGED",arg,Messages.getString("RoomFrame.TITLE_CHANGES"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE));
				if (res == JOptionPane.YES_OPTION)
					commit();
				else if (res == JOptionPane.CANCEL_OPTION)
					{
					super.fireInternalFrameEvent(id);
					return;
					}
				}
			dispose();
			}
		super.fireInternalFrameEvent(id);
		}

	public void actionPerformed(ActionEvent e)
		{
		//save button clicked
		commit();
		dispose();
		}
	}
