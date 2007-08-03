/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;

public class ActionFrame extends MDIFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	private IndexButtonGroup applies;
	private ResourceMenu appliesObject;
	private Action act;
	private JComponent argEdit[];
	private JButton save;
	private JButton discard;

	public ActionFrame(Action a)
		{
		this(a,a.libAction);
		}

	//TODO: Add support for Arrows, Relative, and Not, and consider using JDialog
	//Must be delegated through ActionFrame(Action)
	private ActionFrame(Action a, LibAction la)
		{
		super(la.description);
		if (la.parent == null) setTitle(Messages.getString("Action.UNKNOWN")); //$NON-NLS-1$
		setSize(260,470);
		setLayout(null); // Components are placed at absolute coordinates
		setFrameIcon(new ImageIcon(la.actImage.getScaledInstance(16,16,Image.SCALE_SMOOTH)));

		act = a;
		JLabel lab;
		JPanel pane;
		String s;

		if (la.actImage != null)
			{
			lab = new JLabel(new ImageIcon(Util.getTransparentIcon(la.actImage)));
			add(lab);
			lab.setBounds(5,15,35,35);
			}

		pane = new JPanel(null);
		s = Messages.getString("ActionFrame.APPLIES"); //$NON-NLS-1$
		pane.setBorder(BorderFactory.createTitledBorder(s));
		pane.setBounds(45,5,200,90);
		add(pane);

		if (a.appliesTo.getValue() == -1)
			s = Messages.getString("ActionFrame.SELF"); //$NON-NLS-1$
		else
			s = Messages.getString("ActionFrame.OTHER"); //$NON-NLS-1$
		appliesObject = new ResourceMenu(Resource.GMOBJECT,s,false,100);
		appliesObject.setBounds(75,60,100,20);
		pane.add(appliesObject);
		if (a.appliesTo.getValue() >= 0)
			appliesObject.setSelected(LGM.currentFile.gmObjects.get(a.appliesTo));
		else
			appliesObject.setVisible(false);
		// Added after the radio buttons, but created before for the third button's listener 

		applies = new IndexButtonGroup(3,true,false);
		JRadioButton button = new JRadioButton(Messages.getString("ActionFrame.SELF")); //$NON-NLS-1$
		button.setBounds(5,20,70,20);
		applies.add(button,-1);
		button = new JRadioButton(Messages.getString("ActionFrame.OTHER")); //$NON-NLS-1$
		button.setBounds(5,40,70,20);
		applies.add(button,-2);

		button = new JRadioButton(Messages.getString("ActionFrame.OBJECT")); //$NON-NLS-1$
		button.setHorizontalAlignment(JRadioButton.LEFT);
		button.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		button.setBounds(5,60,70,20);
		button.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
					{
					boolean sel = ((JRadioButton) e.getSource()).isSelected();
					appliesObject.setVisible(sel);
					}
			});
		applies.add(button,0);
		applies.setValue(Math.min(a.appliesTo.getValue(),0));
		applies.populate(pane);
		if (!la.canApplyTo) pane.setVisible(false);

		pane = new JPanel();
		pane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		pane.setBounds(5,100,240,150);
		add(pane);

		argEdit = new JComponent[a.arguments.length];
		for (int n = 0; n < a.arguments.length; n++)
			{
			Argument arg = a.arguments[n];
			if (la.parent == null)
				lab = new JLabel(String.format(Messages.getString("ActionFrame.UNKNOWN"),n)); //$NON-NLS-1$
			else
				{
				LibArgument larg = la.libArguments[n];
				lab = new JLabel(larg.caption);
				}
			lab.setPreferredSize(new Dimension(100,20));
			lab.setHorizontalAlignment(JLabel.RIGHT);
			pane.add(lab);
			if (la.libArguments == null || la.libArguments.length <= n)
				argEdit[n] = arg.getEditor(null);
			else
				argEdit[n] = arg.getEditor(la.libArguments[n]);
			argEdit[n].setPreferredSize(new Dimension(120,20));
			pane.add(argEdit[n]);
			}

		s = Messages.getString("ActionFrame.SAVE"); //$NON-NLS-1$
		save = new JButton(s,LGM.getIconForKey("ActionFrame.SAVE"));
		save.addActionListener(this);
		save.setBounds(5,250,112,20);
		add(save);
		s = Messages.getString("ActionFrame.DISCARD"); //$NON-NLS-1$
		discard = new JButton(s,LGM.getIconForKey("ActionFrame.DISCARD"));
		discard.addActionListener(this);
		discard.setBounds(132,250,112,20);
		add(discard);

		repaint();
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == discard)
			{
			for (Argument a : act.arguments)
				{
				a.discard();
				}
			dispose();
			}
		else if (e.getSource() == save)
			{
			if (applies.getValue() >= 0)
				{
				Resource sel = appliesObject.getSelected();
				if (sel != null) act.appliesTo = sel.getId();
				}
			else
				act.appliesTo = new ResId(applies.getValue());
			for (Argument a : act.arguments)
				{
				a.commit();
				}
			dispose();
			}
		}
	}
