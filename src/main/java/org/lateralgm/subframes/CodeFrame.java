/*
 * Copyright (C) 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.lateralgm.components.CodeTextArea;
import org.lateralgm.components.MarkerCache;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.components.mdi.RevertableMDIFrame;
import org.lateralgm.main.LGM;

public class CodeFrame extends RevertableMDIFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public interface CodeHolder
		{
		String getCode();

		void setCode(String s);
		}

	public final CodeHolder codeHolder;
	public final JToolBar tool;
	public final CodeTextArea code;
	public final JPanel status;

	private final String titleFormat;
	private final JButton save;

	public CodeFrame(CodeHolder codeHolder, String titleFormat, Object titleArg)
		{
		super(MessageFormat.format(titleFormat,titleArg),true,true,true,true);
		this.setFrameIcon(LGM.getIconForKey("Resource.SCR"));
		this.codeHolder = codeHolder;
		this.titleFormat = titleFormat;
		setSize(700,430);

		tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		save = new JButton(LGM.getIconForKey("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.addActionListener(this);
		tool.add(save);
		tool.addSeparator();

		code = new CodeTextArea(codeHolder.getCode(),MarkerCache.getMarker("gml"));
		code.addEditorButtons(tool);

		status = new JPanel(new FlowLayout());
		status.setLayout(new BoxLayout(status,BoxLayout.X_AXIS));
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));
		final JLabel caretPos = new JLabel(" INS | UTF-8 | " + (code.getCaretLine() + 1) + " : "
				+ (code.getCaretColumn() + 1));
		status.add(caretPos);
		code.addCaretListener(new CaretListener()
			{
				public void caretUpdate(CaretEvent e)
					{
					caretPos.setText(" INS | UTF-8 | " + (code.getCaretLine() + 1) + ":"
							+ (code.getCaretColumn() + 1));
					}
			});

		add(tool,BorderLayout.NORTH);
		add(code,BorderLayout.CENTER);
		add(status,BorderLayout.SOUTH);

		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(code.text));

		SubframeInformer.fireSubframeAppear(this,false);
		}

	public void setTitleFormatArg(Object titleArg)
		{
		setTitle(MessageFormat.format(titleFormat,titleArg));
		}

	public void commitChanges()
		{
		codeHolder.setCode(code.getTextCompat());
		}

	public void actionPerformed(ActionEvent e)
		{
		//save button clicked, commit changes because our resourceChanged() method does not
		updateResource(true);
		close();
		}

	@Override
	public String getConfirmationName()
		{
		return getTitle();
		}

	//updatable only, no revert
	@Override
	public boolean resourceChanged()
		{
		return code.isChanged();
		}

	@Override
	public void revertResource()
		{
		//updatable only, no revert
		}

	@Override
	public void updateResource(boolean commit)
		{
		//NOTE: Ignore commit parameter, this is simply a flag to let us know if
		//resourceChanged() was called recently as some resources commit before
		//checking changes and we want to avoid resources committing twice.
		commitChanges();
		}

	@Override
	public void setResourceChanged()
		{
		// TODO: Discussion should be held about closing associated windows.

		}
	}
