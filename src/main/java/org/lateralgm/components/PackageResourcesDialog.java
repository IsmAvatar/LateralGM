/**
* @file ImportExportDialog.java
* @brief Class implementing the frame with selection box allowing you to mass import and export
* resources and essentially merge projects.
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

package org.lateralgm.components;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

public class PackageResourcesDialog extends JDialog
{
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = 3642639396173517907L;

	private static PackageResourcesDialog instance;

	public class TypeCheckBox extends JCheckBox {
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = -4523040009849657775L;

	public Class<?> kind;
		public TypeCheckBox(Class<?> k) {
			super(Resource.kindNamesPlural.get(k),true);
			kind = k;
		}
	}

	public JList<TypeCheckBox> typeList;

	public void setAllSelected(boolean selected) {
		ListModel<TypeCheckBox> model = typeList.getModel();

		for (int i = 0; i < model.getSize(); i++){
			TypeCheckBox cb =	model.getElementAt(i);
			cb.setSelected(selected);
		}

		typeList.repaint();
	}

	public HashSet<Class<?>> getSelectedTypes() {
		HashSet<Class<?>> ret = new HashSet<Class<?>>();
		ListModel<TypeCheckBox> model = typeList.getModel();

		for (int i = 0; i < model.getSize(); i++){
			TypeCheckBox cb =	model.getElementAt(i);
			if (cb.isSelected()) {
				ret.add(cb.kind);
			}
		}

		return ret;
	}

	public HashSet<Class<?>> getUnselectedTypes() {
		HashSet<Class<?>> ret = new HashSet<Class<?>>();
		ListModel<TypeCheckBox> model = typeList.getModel();

		for (int i = 0; i < model.getSize(); i++){
			TypeCheckBox cb =	model.getElementAt(i);
			if (!cb.isSelected()) {
				ret.add(cb.kind);
			}
		}

		return ret;
	}

	public void populateKindList() {
		DefaultListModel<TypeCheckBox> model = new DefaultListModel<TypeCheckBox>();

		for (Class<?> kind : Resource.kinds) {
			model.addElement(new TypeCheckBox(kind));
		}

		typeList.setModel(model);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			populateKindList();
		}
	}

	public PackageResourcesDialog(Frame parent) {
		super(parent);
		this.setTitle(Messages.getString("PackageResources.TITLE"));
		this.setIconImage(LGM.getIconForKey("PackageResources.ICON").getImage());
		this.setResizable(false);

		typeList = new JList<TypeCheckBox>();
		typeList.setCellRenderer(new CheckBoxListCellRenderer());
		typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		typeList.addMouseListener(new MouseAdapter()
			 {
					public void mousePressed(MouseEvent e)
					{
						 int index = typeList.locationToIndex(e.getPoint());

						 if (index != -1) {
								TypeCheckBox checkbox = (TypeCheckBox) typeList.getModel().getElementAt(index);
								checkbox.setSelected(!checkbox.isSelected());
								repaint();
						 }
					}
			 }
		);

		JScrollPane typeScroll = new JScrollPane(typeList);

		JButton selectAllButton = new JButton(Messages.getString("PackageResources.SELECTALL"));
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
				{
					setAllSelected(true);
				}
		});
		JButton selectNoneButton = new JButton(Messages.getString("PackageResources.SELECTNONE"));
		selectNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
				{
					setAllSelected(false);
				}
		});

		JButton importButton = new JButton(Messages.getString("PackageResources.IMPORT"));
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
				{
					//Listener.getInstance().fc.importProject();
				}
		});

		JButton exportButton = new JButton(Messages.getString("PackageResources.EXPORT"));
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
				{
					//Listener.getInstance().fc.exportProject();
				}
		});

		JButton closeButton = new JButton(Messages.getString("PackageResources.CLOSE"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
				{
					setVisible(false);
				}
		});

		GroupLayout gl = new GroupLayout(this.getContentPane());
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(selectAllButton)
		/*	*/.addComponent(selectNoneButton))
		/**/.addComponent(typeScroll)
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(importButton)
		/*	*/.addComponent(exportButton)
		/*	*/.addComponent(closeButton))
		);

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(selectAllButton)
		/*	*/.addComponent(selectNoneButton))
		/**/.addComponent(typeScroll)
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(importButton)
		/*	*/.addComponent(exportButton)
		/*	*/.addComponent(closeButton))
		);

		this.setLayout(gl);

		this.pack();
		this.setLocationRelativeTo(parent);
	}

	public static PackageResourcesDialog getInstance()
		{
			return instance == null ? instance = new PackageResourcesDialog(LGM.frame) : instance;
		}

	protected class CheckBoxListCellRenderer implements ListCellRenderer<JCheckBox>
	{
		public Component getListCellRendererComponent(
									 JList<? extends JCheckBox> list, JCheckBox checkbox, int index,
									 boolean isSelected, boolean cellHasFocus)
		{
			checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			return checkbox;
		}
	}

}
