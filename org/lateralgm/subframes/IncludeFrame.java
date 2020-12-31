/**
* @file  IncludeFrame.java
* @brief Class implementing the instantiable include frame
*
* @section License
*
* Copyright (C) 2014,2019 Robert B. Colton
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

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.ExtensionPackages;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Include.ExportAction;
import org.lateralgm.resources.Include.PInclude;

public class IncludeFrame extends InstantiableResourceFrame<Include,Include.PInclude>
	{
	/**
	 * NOTE: Default UID generated, changed if necessary.
	 */
	private static final long serialVersionUID = -2341007814035473389L;
	private CustomFileChooser fc = new CustomFileChooser("/org/lateralgm","LAST_INCLUDE_FILE_DIR");  //$NON-NLS-1$//$NON-NLS-2$
	private JButton saveDataBut, loadDataBut;
	private JLabel originalNameLabel, sizeLabel;

	public IncludeFrame(Include r, ResNode node)
		{
		super(r,node);
		this.getRootPane().setDefaultButton(save);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		saveDataBut = new JButton(LGM.getIconForKey("IncludeFrame.SAVE_DATA")); //$NON-NLS-1$
		saveDataBut.setText(Messages.getString("IncludeFrame.SAVE_DATA")); //$NON-NLS-1$
		saveDataBut.setToolTipText(Messages.getString("IncludeFrame.SAVE_TIP")); //$NON-NLS-1$
		saveDataBut.addActionListener(this);

		loadDataBut = new JButton(LGM.getIconForKey("IncludeFrame.LOAD_DATA")); //$NON-NLS-1$
		loadDataBut.setText(Messages.getString("IncludeFrame.LOAD_DATA")); //$NON-NLS-1$
		loadDataBut.setToolTipText(Messages.getString("IncludeFrame.LOAD_TIP")); //$NON-NLS-1$
		loadDataBut.addActionListener(this);

		JLabel nameLabel = new JLabel(Messages.getString("IncludeFrame.NAME")); //$NON-NLS-1$
		originalNameLabel = new JLabel();
		sizeLabel = new JLabel();
		updateStatusLabels();

		JLabel fileNameLabel = new JLabel(Messages.getString("IncludeFrame.FILE_NAME")); //$NON-NLS-1$
		JTextField fileNameField = new JTextField();
		plf.make(fileNameField.getDocument(),PInclude.FILENAME);

		JCheckBox store, removeEnd, freeMemory, overwrite;
		store = new JCheckBox(Messages.getString("IncludeFrame.STORE_EDITABLE")); //$NON-NLS-1$
		plf.make(store,PInclude.STORE);
		removeEnd = new JCheckBox(Messages.getString("IncludeFrame.REMOVE_FILES_AT_END")); //$NON-NLS-1$
		plf.make(removeEnd,PInclude.REMOVEATGAMEEND);
		freeMemory = new JCheckBox(Messages.getString("IncludeFrame.FREE_MEMORY")); //$NON-NLS-1$
		plf.make(freeMemory,PInclude.FREEMEMORY);
		overwrite = new JCheckBox(Messages.getString("IncludeFrame.OVERWRITE_EXISTING")); //$NON-NLS-1$
		plf.make(overwrite,PInclude.OVERWRITE);

		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		JPanel exportActionPanel = new JPanel();
		exportActionPanel.setLayout(new BoxLayout(exportActionPanel,BoxLayout.Y_AXIS));
		exportActionPanel.setBorder(BorderFactory.createTitledBorder(
				Messages.getString("IncludeFrame.EXPORT_ACTION"))); //$NON-NLS-1$

		final JTextField customFolderField = new JTextField();
		customFolderField.setEnabled(r.get(PInclude.EXPORTACTION) == ExportAction.CUSTOM_FOLDER);
		plf.make(customFolderField.getDocument(),PInclude.EXPORTFOLDER);

		JRadioButton dontExport, tempDirectory, sameFolder;
		final JRadioButton customFolder;
		dontExport = new JRadioButton(Messages.getString("IncludeFrame.DONT_EXPORT")); //$NON-NLS-1$
		tempDirectory = new JRadioButton(Messages.getString("IncludeFrame.TEMP_DIRECTORY")); //$NON-NLS-1$
		sameFolder = new JRadioButton(Messages.getString("IncludeFrame.SAME_FOLDER")); //$NON-NLS-1$
		customFolder = new JRadioButton(Messages.getString("IncludeFrame.CUSTOM_FOLDER")); //$NON-NLS-1$
		customFolder.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e)
				{
				customFolderField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				}
		});

		exportActionPanel.add(dontExport);
		exportActionPanel.add(tempDirectory);
		exportActionPanel.add(sameFolder);
		exportActionPanel.add(customFolder);
		exportActionPanel.add(customFolderField);

		ButtonGroup bg = new ButtonGroup();
		bg.add(dontExport);
		bg.add(tempDirectory);
		bg.add(sameFolder);
		bg.add(customFolder);
		plf.make(bg,PInclude.EXPORTACTION,Include.ExportAction.class);

		save.setText(Messages.getString("IncludeFrame.SAVE")); //$NON-NLS-1$

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(nameLabel)
		/*	*/.addComponent(name,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE))
		// we want these two labels to have an ellipsis if too long
		// and the tooltip can be used to see the full name/value
		/**/.addComponent(originalNameLabel,0,0,DEFAULT_SIZE)
		/**/.addComponent(sizeLabel,0,0,DEFAULT_SIZE)
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(fileNameLabel)
		/*	*/.addComponent(fileNameField,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE))
		/**/.addComponent(store)
		/**/.addComponent(removeEnd)
		/**/.addComponent(freeMemory)
		/**/.addComponent(overwrite)
		/**/.addComponent(exportActionPanel,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE)
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(save,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE)
		/*	*/.addComponent(loadDataBut,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE)
		/*	*/.addComponent(saveDataBut,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE)));

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(nameLabel)
		/*	*/.addComponent(name))
		/**/.addComponent(originalNameLabel)
		/**/.addComponent(sizeLabel)
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(fileNameLabel)
		/*	*/.addComponent(fileNameField))
		/**/.addComponent(store)
		/**/.addComponent(removeEnd)
		/**/.addComponent(freeMemory)
		/**/.addComponent(overwrite)
		/**/.addComponent(exportActionPanel,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(save)
		/*	*/.addComponent(loadDataBut)
		/*	*/.addComponent(saveDataBut)));

		this.add(p,BorderLayout.CENTER);
		this.pack();
		this.setMinimumSize(this.getSize());
		}

	private void updateStatusLabels()
		{
		saveDataBut.setEnabled(res.data != null && res.data.length > 0);

		String filePathText = Messages.format("IncludeFrame.ORIGINAL_FILE",res.get(PInclude.FILEPATH)); //$NON-NLS-1$
		originalNameLabel.setText(filePathText);
		String sizeText = Messages.format("IncludeFrame.SIZE",res.get(PInclude.SIZE)); //$NON-NLS-1$
		sizeLabel.setText(sizeText);
		// set the tooltip text too so the full path can be seen without resizing the editor
		originalNameLabel.setToolTipText(filePathText);
		sizeLabel.setToolTipText(sizeText);
		}

	public Object getUserObject()
		{
		if (node != null) return node.getUserObject();
		for (int m = 0; m < LGM.root.getChildCount(); m++)
			{
			ResNode n = (ResNode) LGM.root.getChildAt(m);
			if (n.kind == ExtensionPackages.class) return n.getUserObject();
			}
		return 0;//Messages.getString("LGM.EXT");
		}

	public void actionPerformed(ActionEvent ev)
		{
		super.actionPerformed(ev);
		Object source = ev.getSource();
		if (source == loadDataBut)
			{
			if (fc.showOpenDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			File f = fc.getSelectedFile();
			if (!f.exists()) return;
			res.put(PInclude.FILENAME,f.getName());
			res.put(PInclude.FILEPATH,f.getAbsolutePath());
			try
				{
				res.data = Files.readAllBytes(f.toPath());
				}
			catch (IOException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			res.put(PInclude.SIZE,res.data.length);
			updateStatusLabels();
			return;
			}
		if (source == saveDataBut)
			{
			fc.setSelectedFile(new File(res.get(PInclude.FILENAME).toString()));
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			File f = fc.getSelectedFile();
			try
				{
				Files.write(f.toPath(),res.data);
				}
			catch (IOException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			return;
			}
		}

	public void commitChanges()
		{

		}

	public void setComponents(Include inc)
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
		// NOTE: commit changes must be the first line because if we don't
		// the method will be flagged that we handled committing ourselves,
		// and the changes wont actually get committed.
		commitChanges();
		return !res.properties.equals(resOriginal.properties);
		}

	@Override
	public void revertResource()
		{
		res.properties.putAll(resOriginal.properties);
		//setComponents(res);
		}
	}
