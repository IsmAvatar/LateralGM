/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;

public class ActionFrame extends MDIFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	private IndexButtonGroup applies;
	protected ResourceMenu<GmObject> appliesObject;
	private JPanel appliesPanel;
	private Action act;
	private ArgumentComponent argComp[];
	private JCheckBox relativeBox;
	private JCheckBox notBox;
	private JButton save;
	private JButton discard;
	private GMLTextArea code;

	public ActionFrame(Action a)
		{
		this(a,a.libAction);
		}

	//Must be delegated through ActionFrame(Action)
	private ActionFrame(Action a, LibAction la)
		{
		super(la.description);
		if (la.parent == null) setTitle(Messages.getString("Action.UNKNOWN")); //$NON-NLS-1$
		setLayer(JLayeredPane.MODAL_LAYER);
		if (la.actImage != null)
			setFrameIcon(new ImageIcon(la.actImage.getScaledInstance(16,16,Image.SCALE_SMOOTH)));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		String s;
		if (a.appliesTo == GmObject.OBJECT_SELF)
			s = Messages.getString("ActionFrame.SELF"); //$NON-NLS-1$
		else
			s = Messages.getString("ActionFrame.OTHER"); //$NON-NLS-1$
		appliesObject = new ResourceMenu<GmObject>(Resource.GMOBJECT,s,false,100);
		appliesObject.setEnabled(GmObject.refAsInt(a.appliesTo) >= 0);
		appliesObject.setOpaque(false);
		appliesObject.setSelected(a.appliesTo);
		act = a;

		appliesPanel = new JPanel();
		appliesPanel.setOpaque(false);

		applies = new IndexButtonGroup(3,true,false);
		JRadioButton button = new JRadioButton(Messages.getString("ActionFrame.SELF")); //$NON-NLS-1$
		button.setOpaque(false);
		applies.add(button,-1);
		appliesPanel.add(button);
		button = new JRadioButton(Messages.getString("ActionFrame.OTHER")); //$NON-NLS-1$
		button.setOpaque(false);
		applies.add(button,-2);
		appliesPanel.add(button);
		button = new JRadioButton(Messages.getString("ActionFrame.OBJECT")); //$NON-NLS-1$
		button.setHorizontalAlignment(JRadioButton.LEFT);
		button.setOpaque(false);
		button.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
					{
					boolean sel = ((JRadioButton) e.getSource()).isSelected();
					appliesObject.setEnabled(sel);
					}
			});
		applies.add(button,0);
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder());
		p.setLayout(new GridBagLayout());
		p.setOpaque(false);
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		p.add(button,gbc);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0,4,0,6);
		p.add(appliesObject,gbc);
		appliesPanel.add(p);
		applies.setValue(Math.min(GmObject.refAsInt(a.appliesTo),0));

		if (la.interfaceKind == LibAction.INTERFACE_CODE)
			{
			setSize(600,400);
			setClosable(true);
			setMaximizable(true);
			setResizable(true);
			setIconifiable(true);
			// the code text area
			code = new GMLTextArea(a.arguments[0].val);
			// Setup the toolbar
			JToolBar tool = new JToolBar();
			tool.setFloatable(false);
			tool.setAlignmentX(0);
			add("North",tool); //$NON-NLS-1$
			// Setup the buttons
			save = new JButton(LGM.getIconForKey("ActionFrame.SAVE")); //$NON-NLS-1$
			save.addActionListener(this);
			add(save);
			tool.add(save);
			tool.addSeparator();
			code.addEditorButtons(tool);
			tool.addSeparator();
			tool.add(new JLabel(Messages.getString("ActionFrame.APPLIES"))); //$NON-NLS-1$
			tool.add(appliesPanel);
			appliesPanel.setLayout(new BoxLayout(appliesPanel,BoxLayout.LINE_AXIS));
			getContentPane().add(code);
			}
		else
			makeArgumentPane(a,la);
		pack();
		repaint();
		}

	private void makeArgumentPane(Action a, LibAction la)
		{
		setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JLabel lab;
		JPanel pane;
		pane = new JPanel();
		pane.setBorder(new EmptyBorder(6,6,0,6));
		pane.setLayout(new BorderLayout());
		add(pane);
		if (la.actImage != null)
			{
			lab = new JLabel(new ImageIcon(la.actImage));
			lab.setBorder(new EmptyBorder(16,16,16,20));
			pane.add(lab,BorderLayout.LINE_START);
			}

		appliesPanel.setLayout(new GridLayout(0,1));
		String s = Messages.getString("ActionFrame.APPLIES"); //$NON-NLS-1$
		appliesPanel.setBorder(BorderFactory.createTitledBorder(s));
		pane.add(appliesPanel);
		if (!la.canApplyTo) appliesPanel.setVisible(false);

		argComp = new ArgumentComponent[a.arguments.length];
		if (a.arguments.length > 0)
			{
			pane = new JPanel();
			pane.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6,8,0,8),
					BorderFactory.createTitledBorder("")));
			GroupLayout kvLayout = new GroupLayout(pane);
			GroupLayout.SequentialGroup hGroup, vGroup;
			GroupLayout.ParallelGroup keyGroup, valueGroup;
			hGroup = kvLayout.createSequentialGroup();
			vGroup = kvLayout.createSequentialGroup();
			keyGroup = kvLayout.createParallelGroup();
			valueGroup = kvLayout.createParallelGroup();

			hGroup.addGap(4);
			hGroup.addGroup(keyGroup);
			hGroup.addGap(6);
			hGroup.addGroup(valueGroup);
			hGroup.addGap(4);

			kvLayout.setHorizontalGroup(hGroup);
			kvLayout.setVerticalGroup(vGroup);

			pane.setLayout(kvLayout);
			add(pane);

			vGroup.addGap(4);
			for (int n = 0; n < a.arguments.length; n++)
				{
				argComp[n] = new ArgumentComponent(a.arguments[n],a.libAction.libArguments[n]);
				if (la.parent == null)
					lab = new JLabel(Messages.format("ActionFrame.UNKNOWN",n)); //$NON-NLS-1$
				else
					{
					LibArgument larg = la.libArguments[n];
					lab = new JLabel(larg.caption);
					}
				if (n == 0 && act.libAction.interfaceKind == LibAction.INTERFACE_ARROWS)
					{
					argComp[n].setEditor(new ArrowsEditor(argComp[n].getArgument().val));
					}
				else
					{
					Component c = argComp[n].getEditor();
					c.setMaximumSize(new Dimension(240,20));
					c.setPreferredSize(new Dimension(200,20));
					c.setMinimumSize(new Dimension(160,20));
					}
				keyGroup.addComponent(lab);
				valueGroup.addComponent(argComp[n].getEditor());
				if (n > 0) vGroup.addGap(6);
				GroupLayout.ParallelGroup argGroup = kvLayout.createParallelGroup(Alignment.BASELINE);
				argGroup.addComponent(lab).addComponent(argComp[n].getEditor());
				vGroup.addGroup(argGroup);
				}
			vGroup.addGap(4);
			}
		pane = new JPanel();
		pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
		add(pane);
		if (act.libAction.allowRelative)
			{
			relativeBox = new JCheckBox(Messages.getString("ActionFrame.RELATIVE")); //$NON-NLS-1$
			relativeBox.setSelected(act.relative);
			pane.add(relativeBox);
			}
		if (act.libAction.question)
			{
			notBox = new JCheckBox(Messages.getString("ActionFrame.NOT")); //$NON-NLS-1$
			notBox.setSelected(act.not);
			pane.add(notBox);
			}

		pane = new JPanel();
		pane.setLayout(new GridLayout(1,2,8,0));
		pane.setBorder(new EmptyBorder(0,8,8,8));
		add(pane);
		s = Messages.getString("ActionFrame.SAVE"); //$NON-NLS-1$
		save = new JButton(s,LGM.getIconForKey("ActionFrame.SAVE")); //$NON-NLS-1$
		save.addActionListener(this);
		pane.add(save);
		s = Messages.getString("ActionFrame.DISCARD"); //$NON-NLS-1$
		discard = new JButton(s,LGM.getIconForKey("ActionFrame.DISCARD")); //$NON-NLS-1$
		discard.addActionListener(this);
		pane.add(discard);
		}

	public WeakReference<GmObject> getApplies()
		{
		if (applies.getValue() >= 0)
			{
			WeakReference<GmObject> sel = appliesObject.getSelected();
			if (sel != null) return sel;
			return act.appliesTo;
			}
		if (applies.getValue() == -1) return GmObject.OBJECT_SELF;
		if (applies.getValue() == -2) return GmObject.OBJECT_OTHER;
		return null;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == discard)
			{
			for (ArgumentComponent a : argComp)
				{
				a.discard();
				}
			dispose();
			}
		//TODO: Update The List on save
		else if (e.getSource() == save)
			{
			act.appliesTo = getApplies();
			if (relativeBox != null) act.relative = relativeBox.isSelected();
			if (notBox != null) act.not = notBox.isSelected();
			switch (act.libAction.interfaceKind)
				{
				case LibAction.INTERFACE_CODE:
					act.arguments[0].val = code.getTextCompat();
					break;
				default:
					for (ArgumentComponent a : argComp)
						{
						a.commit();
						}
				}
			dispose();
			}
		}

	public class ArrowsEditor extends JPanel
		{
		private static final long serialVersionUID = 1L;
		private JToggleButton[] arrows;
		private final Dimension btnSize = new Dimension(32,32);
		private final Dimension panelSize = new Dimension(96,96);

		public ArrowsEditor(String val)
			{
			setLayout(new GridLayout(3,3));
			arrows = new JToggleButton[9];
			for (int i = 0; i < 9; i++)
				{
				arrows[i] = new JToggleButton();
				arrows[i].setPreferredSize(btnSize);
				if (val.length() > i) arrows[i].setSelected(val.charAt(i) == '1');
				add(arrows[i]);
				}
			setMaximumSize(panelSize);
			setPreferredSize(panelSize);
			}

		public String getStringValue()
			{
			char[] res = new char[9];
			for (int i = 0; i < 9; i++)
				res[i] = arrows[i].isSelected() ? '1' : '0';
			return new String(res);
			}
		}

	public void fireInternalFrameEvent(int id)
		{
		switch (id)
			{
			case InternalFrameEvent.INTERNAL_FRAME_CLOSING:
				if (act.libAction.interfaceKind == LibAction.INTERFACE_CODE)
					if (code.getUndoManager().isModified() || !act.appliesTo.equals(getApplies()))
						{
						int ret = JOptionPane.showConfirmDialog(LGM.frame,Messages.format(
								"ActionFrame.KEEPCHANGES",getTitle()), //$NON-NLS-1$
								Messages.getString("ActionFrame.KEEPCHANGES_TITLE"), //$NON-NLS-1$
								JOptionPane.YES_NO_CANCEL_OPTION);
						if (ret == JOptionPane.CANCEL_OPTION) break;
						if (ret == JOptionPane.YES_OPTION) save.doClick();
						}
				dispose();
				break;
			default:
			}
		super.fireInternalFrameEvent(id);
		}

	private class ArgumentComponent
		{
		private Argument arg;
		private Component editor;

		public ArgumentComponent(Argument arg, LibArgument libArg)
			{
			this.arg = arg;
			editor = makeEditor(libArg);
			discard();
			}

		public Argument getArgument()
			{
			return arg;
			}

		private String getNoSelectionString(byte resourceKind)
			{
			String key;
			switch (resourceKind)
				{
				case Resource.SPRITE:
					key = "ArgumentComponent.SPRITE"; //$NON-NLS-1$
					break;
				case Resource.SOUND:
					key = "ArgumentComponent.SOUND"; //$NON-NLS-1$
					break;
				case Resource.BACKGROUND:
					key = "ArgumentComponent.BACKGROUND"; //$NON-NLS-1$
					break;
				case Resource.PATH:
					key = "ArgumentComponent.PATH"; //$NON-NLS-1$
					break;
				case Resource.SCRIPT:
					key = "ArgumentComponent.SCRIPT"; //$NON-NLS-1$
					break;
				case Resource.FONT:
					key = "ArgumentComponent.FONT"; //$NON-NLS-1$
					break;
				case Resource.GMOBJECT:
					key = "ArgumentComponent.OBJECT"; //$NON-NLS-1$
					break;
				case Resource.ROOM:
					key = "ArgumentComponent.ROOM"; //$NON-NLS-1$
					break;
				case Resource.TIMELINE:
					key = "ArgumentComponent.TIMELINE"; //$NON-NLS-1$
					break;
				default:
					key = "";
				}
			return Messages.format("ArgumentComponent.NO_SELECTION",Messages.getString(key)); //$NON-NLS-1$
			}

		@SuppressWarnings("unchecked")
		private JComponent makeEditor(LibArgument la)
			{
			switch (arg.kind)
				{
				case Argument.ARG_BOOLEAN:
					final String[] sab = { "false","true" };
					return new JComboBox(sab);
				case Argument.ARG_MENU:
					if (la == null) return new JTextField(arg.val);
					final String[] sam = la.menu.split("\\|"); //$NON-NLS-1$
					return new JComboBox(sam);
				case Argument.ARG_COLOR:
					return new ColorSelect(Util.convertGmColor(Integer.parseInt(arg.val)));
				case Argument.ARG_SPRITE:
				case Argument.ARG_SOUND:
				case Argument.ARG_BACKGROUND:
				case Argument.ARG_PATH:
				case Argument.ARG_SCRIPT:
				case Argument.ARG_GMOBJECT:
				case Argument.ARG_ROOM:
				case Argument.ARG_FONT:
				case Argument.ARG_TIMELINE:
					byte rk = Argument.getResourceKind(arg.kind);
					return new ResourceMenu(rk,getNoSelectionString(rk),120);
				default:
					return new JTextField(arg.val);
				}
			}

		/**
		 * Gets a Component editor for this Argument. Defaults to raw JTextField.
		 * @param la - The corresponding LibArgument, used for Menus.
		 * May be null, but then a menu will default to JTextField.
		 * @return One of ColorSelect, JComboBox, ResourceMenu, or JTextField
		 */
		public Component getEditor()
			{
			return editor;
			}

		public void setEditor(Component editor)
			{
			this.editor = editor;
			}

		/** Commits any changes in the Component editor to update this Argument. */
		public void commit()
			{
			if (editor instanceof JTextField)
				{
				arg.val = ((JTextField) editor).getText();
				return;
				}
			if (editor instanceof JComboBox)
				{
				arg.val = Integer.toString(((JComboBox) editor).getSelectedIndex());
				return;
				}
			if (editor instanceof ColorSelect)
				{
				arg.val = Integer.toString(Util.getGmColor(((ColorSelect) editor).getSelectedColor()));
				}
			if (editor instanceof ArrowsEditor)
				{
				arg.val = ((ArrowsEditor) editor).getStringValue();
				}
			if (editor instanceof ResourceMenu)
				{
				arg.res = ((ResourceMenu<?>) editor).getSelected();
				return;
				}
			}

		@SuppressWarnings("unchecked")
		public void discard()
			{
			if (editor instanceof JTextField)
				{
				((JTextField) editor).setText(arg.val);
				}
			else if (editor instanceof JComboBox)
				{
				((JComboBox) editor).setSelectedIndex(Integer.parseInt(arg.val));
				}
			else if (editor instanceof ColorSelect)
				{
				((ColorSelect) editor).setSelectedColor(Util.convertGmColor(Integer.parseInt(arg.val)));
				}
			else if (editor instanceof ResourceMenu)
				{
				try
					{
					((ResourceMenu) editor).setSelected(arg.res);
					}
				catch (NumberFormatException nfe)
					{
					}
				}
			}
		}
	}
