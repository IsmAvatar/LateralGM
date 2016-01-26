/**
* @file  ConfigurationManager.java
* @brief Class implementing a frame for managing multiple configurations.
*
* @section License
*
* Copyright (C) 2015 Robert B. Colton
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.components.impl.NameDocument;
import org.lateralgm.file.ProjectFile;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameSettings;

public class ConfigurationManager extends JDialog implements ActionListener
	{
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = 683154513336137335L;
	JList<GameSettings> configList = null;
	private VectorListModel<GameSettings> vlm;
	private JButton deleteButton;
	private static ConfigurationManager INSTANCE = null;

	public static ConfigurationManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ConfigurationManager();
			INSTANCE.setConfigList(LGM.currentFile.gameSettings);
		}
		return INSTANCE;
	}

	// The purpose of this internal class is because Vectors and ArrayLists are handled by reference for Java
	// so instead of converting to an array and doing all kinds of crazy updating, we can just make a list model
	// that handles the array by reference.
	public class VectorListModel<T> extends AbstractListModel<T> {
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = 8672982566605074780L;
		private Vector<T> vector = null;

		public VectorListModel(Vector<T> vec) {
			vector = vec;
		}

		public void add(int index, T element) {
			vector.add(index,element);
			super.fireIntervalAdded(this,index,index);
		}

		public void addElement(T element) {
			vector.addElement(element);
			super.fireIntervalAdded(this,vector.size()-1,vector.size()-1);
		}

		public boolean remove(T element) {
			int index = vector.indexOf(element);
			boolean ret = vector.remove(element);
			super.fireIntervalRemoved(this,index,index);
			return ret;
		}

		public boolean removeAll(List<T> elements) {
			boolean ret = vector.removeAll(elements);
			super.fireIntervalRemoved(this,0,elements.size());
			return ret;
		}

		public T getElementAt(int index) {
			return vector.get(index);
		}

		public T get(int index) {
			return vector.get(index);
		}

		public Enumeration<T> elements() {
			return vector.elements();
		}

		public Object[] toArray() {
			return vector.toArray();
		}

		public int size() {
			return vector.size();
		}

		public boolean isEmpty() {
			return vector.isEmpty();
		}

		@Override
		public int getSize()
			{
			return vector.size();
			}

	}

	public ConfigurationManager() {
		super(LGM.frame);
		setResizable(false);
		setAlwaysOnTop(true);
		setTitle(Messages.getString("ConfigurationManager.TITLE"));
		setIconImage(LGM.getIconForKey("ConfigurationManager.ICON").getImage());

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		deleteButton = makeToolbarButton("DELETE");

		configList = new JList<GameSettings>();

		toolbar.add(makeToolbarButton("ADD"));
		toolbar.add(makeToolbarButton("COPY"));
		toolbar.add(deleteButton);
		toolbar.addSeparator();
		toolbar.add(makeToolbarButton("EDIT_SETTINGS"));
		toolbar.add(makeToolbarButton("EDIT_CONSTANTS"));
		toolbar.addSeparator();
		toolbar.add(new JLabel(Messages.getString("ConfigurationManager.NAME")));
		final JTextField nameField = new JTextField();
		nameField.setDocument(new NameDocument());
		nameField.setColumns(10);
		nameField.setMaximumSize(nameField.getPreferredSize());
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateNameField(e);
			}

			public void removeUpdate(DocumentEvent e) {
				updateNameField(e);
			}

			public void insertUpdate(DocumentEvent e) {
				updateNameField(e);
			}

			public void updateNameField(DocumentEvent e) {
				GameSettings sel = configList.getSelectedValue();
				if (sel == null) return;
				sel.setName(nameField.getText());
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run()
						{
						configList.updateUI();
						}

				});
				sel.constants.setName(nameField.getText());
				LGM.getConstantsFrame().updateTitle();
				LGM.getGameSettings().updateTitle();
			}
		});
		configList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent ev)
				{
					GameSettings sel = configList.getSelectedValue();
					if (sel == null) return;
					nameField.setText(sel.getName());
				}

		});
		toolbar.add(nameField);

		this.add(toolbar, BorderLayout.NORTH);

		JScrollPane scroll = new JScrollPane(configList);
		this.add(scroll, BorderLayout.CENTER);

		this.pack();
		this.setSize(this.getWidth(),320);
		setLocationRelativeTo(LGM.frame);
	}

	JButton makeToolbarButton(String key) {
		key = "ConfigurationManager." + key;
		JButton jb = new JButton();
		jb.setToolTipText(Messages.getString(key));
		jb.setIcon(LGM.getIconForKey(key));
		jb.setActionCommand(key);
		jb.addActionListener(this);
		return jb;
	}

	@Override
	public void actionPerformed(ActionEvent ev)
		{
		String cmd = ev.getActionCommand();
		VectorListModel<GameSettings> model = (VectorListModel<GameSettings>) configList.getModel();
		if (cmd.endsWith("ADD")) {
			GameSettings config = ProjectFile.createDefaultConfig();
			int id = 0;
			for (GameSettings cfg : LGM.currentFile.gameSettings) {
				if (cfg.getName().startsWith("Configuration")) {
					id++;
				}
			}
			config.setName("Configuration" + id);
			model.addElement(config);
			configList.setSelectedValue(config,true);
			LGM.configsCombo.updateUI();
		} else if (cmd.endsWith("COPY")) {
			GameSettings sel = configList.getSelectedValue();
			if (sel == null) return;
			GameSettings config = sel.clone();
			int id = 0;
			for (GameSettings cfg : LGM.currentFile.gameSettings) {
				if (cfg.getName().startsWith("Configuration")) {
					id++;
				}
			}
			config.setName("Configuration" + id);
			model.addElement(config);
			configList.setSelectedValue(config,true);
			LGM.configsCombo.updateUI();
		} else if (cmd.endsWith("DELETE")) {
			//Stop the user from deleting all configs, they must keep at least 1
			List<GameSettings> selList = configList.getSelectedValuesList();
			if (selList.size() >= model.getSize()) {
				selList.remove(0);
			}
			model.removeAll(selList);
			// Make sure the JCombo on the main toolbar wasn't selecting what we just deleted
			if (LGM.configsCombo.getSelectedIndex() >= LGM.configsCombo.getItemCount() || LGM.configsCombo.getSelectedIndex() < 0) {
				LGM.configsCombo.setSelectedIndex(0);
			}
			LGM.configsCombo.updateUI();
		} else if (cmd.endsWith("EDIT_SETTINGS")) {
			GameSettings sel = configList.getSelectedValue();
			if (sel == null) return;
			LGM.showGameSettings(sel);
		} else if (cmd.endsWith("EDIT_CONSTANTS")) {
			GameSettings sel = configList.getSelectedValue();
			if (sel == null) return;
			sel.constants.setName(sel.getName());
			LGM.showConstantsFrame(sel.constants);
		}
		}

	public void setConfigList(Vector<GameSettings> gameSettings)
		{
		vlm = new VectorListModel<GameSettings>(LGM.currentFile.gameSettings);
		vlm.addListDataListener(new ListDataListener() {

			@Override
			public void contentsChanged(ListDataEvent arg0)
				{
					deleteButton.setEnabled(vlm.getSize() > 1);
				}

			@Override
			public void intervalAdded(ListDataEvent arg0)
				{
					deleteButton.setEnabled(vlm.getSize() > 1);
				}

			@Override
			public void intervalRemoved(ListDataEvent arg0)
				{
					deleteButton.setEnabled(vlm.getSize() > 1);
				}

		});
		configList.setModel(vlm);
		// check at least once
		deleteButton.setEnabled(vlm.getSize() > 1);
		}

	}
