/**
* @file  ExtensionPackagesFrame.java
* @brief Class implementing the Extension Packages frame
*
* @section License
*
* Copyright (C) 2013-2014 Robert B. Colton
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.ExtensionPackages;

public class ExtensionPackagesFrame extends
		ResourceFrame<ExtensionPackages,ExtensionPackages.PExtensionPackages> implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	private CustomFileChooser fc;

	JList<JCheckBox> packageList;
	private JButton closeButton;
	private JButton installButton;
	private JButton uninstallButton;

	public ExtensionPackagesFrame(ExtensionPackages res)
		{
		this(res,null);
		}

	public ExtensionPackagesFrame(ExtensionPackages res, ResNode node)
		{
		super(res,node,Messages.getString("ExtensionPackagesFrame.TITLE"),true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		closeButton = new JButton(Messages.getString("ExtensionPackagesFrame.CLOSE"));
		closeButton.addActionListener(this);
		installButton = new JButton(Messages.getString("ExtensionPackagesFrame.INSTALL"));
		installButton.addActionListener(this);
		uninstallButton = new JButton(Messages.getString("ExtensionPackagesFrame.UNINSTALL"));
		uninstallButton.addActionListener(this);

		JCheckBox cbArray[] = new JCheckBox[1];
		cbArray[0] = new JCheckBox(Messages.getString("ExtensionPackagesFrame.NONE_INSTALLED"));
		packageList = new JList<JCheckBox>(cbArray);
		packageList.setCellRenderer(new CheckBoxListRenderer());
		JScrollPane listScroll = new JScrollPane(packageList);

		JPanel descPanel = new JPanel();
		descPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("ExtensionPackagesFrame.ABOUT")));

		JPanel centerPanel = new JPanel();
		GroupLayout gl = new GroupLayout(centerPanel);
		centerPanel.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*  */.addComponent(closeButton)
		/*  */.addComponent(installButton)
		/*  */.addComponent(uninstallButton))
		/**/.addComponent(listScroll)
		/**/.addComponent(descPanel));
		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/*  */.addComponent(closeButton)
		/*  */.addComponent(installButton)
		/*  */.addComponent(uninstallButton))
		/**/.addComponent(listScroll)
		/**/.addComponent(descPanel));

		this.add(centerPanel,BorderLayout.CENTER);
		this.pack();
		setSize(this.getWidth(),400);
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
		if (ev.getSource() == closeButton) //$NON-NLS-1$
			{
			this.setVisible(false);
			return;
			}
		if (ev.getSource() == installButton) //$NON-NLS-1$
			{
			return;
			}
		if (ev.getSource() == uninstallButton)
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

	private class CheckBoxListRenderer implements ListCellRenderer<JCheckBox>
		{
		public Component getListCellRendererComponent(JList<? extends JCheckBox> comp, JCheckBox value,
				int index, boolean isSelected, boolean hasFocus)
			{
			JCheckBox item = new JCheckBox(value.getText());
			item.setEnabled(comp.isEnabled());
			item.setSelected(value.isSelected());
			item.setFont(comp.getFont());

			if (isSelected)
				{
				item.setBackground(comp.getSelectionBackground());
				item.setForeground(comp.getSelectionForeground());
				}
			else
				{
				item.setBackground(comp.getBackground());
				item.setForeground(comp.getForeground());
				}

			return item;
			}
		}
	}
