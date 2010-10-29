/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
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
		codeHolder.setCode(code.getTextCompat());
		}

	public void setTitleFormatArg(Object titleArg)
		{
		this.titleArg = titleArg;
		setTitle(MessageFormat.format(titleFormat,titleArg));
		}

	public boolean isChanged()
		{
		return code.getUndoManager().isModified();
		}

	public final CodeHolder codeHolder;
	public final JToolBar tool;
	public final GMLTextArea code;
	public final JPanel status;

	private final String titleFormat;
	private Object titleArg;
	private final JButton save;

	public CodeFrame(CodeHolder codeHolder, String titleFormat, Object titleArg)
		{
		super(MessageFormat.format(titleFormat,titleArg),true,true,true,true);
		this.codeHolder = codeHolder;
		this.titleFormat = titleFormat;
		this.titleArg = titleArg;
		setSize(600,400);
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

		tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		save = new JButton(LGM.getIconForKey("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.addActionListener(this);
		tool.add(save);
		tool.addSeparator();

		code = new GMLTextArea(codeHolder.getCode());
		code.addEditorButtons(tool);

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

		add(tool,BorderLayout.NORTH);
		add(code,BorderLayout.CENTER);
		add(status,BorderLayout.SOUTH);

		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(code));

		SubframeInformer.fireSubframeAppear(this);
		}

	public void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING)
			{
			if (isChanged())
				{
				int res = JOptionPane.showConfirmDialog(getParent(),Messages.format(
						"RoomFrame.CODE_CHANGED",titleArg,Messages.getString("RoomFrame.TITLE_CHANGES"), //$NON-NLS-1$ //$NON-NLS-2$
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
