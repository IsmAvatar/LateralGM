/**
* @file  PreferencesFrame.java
* @brief Class implementing a frame for the user to edit the Java preferences for the application including styles and the look and feel.
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.joshedit.TokenMarker;
import org.lateralgm.joshedit.lexers.GLESTokenMarker;
import org.lateralgm.joshedit.lexers.GLSLTokenMarker;
import org.lateralgm.joshedit.lexers.GMLTokenMarker;
import org.lateralgm.joshedit.lexers.HLSLTokenMarker;
import org.lateralgm.main.LGM;
import org.lateralgm.main.LGM.JSplitPaneExpandable;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;

public class PreferencesFrame extends JFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	protected JPanel cardPane;
	protected JSpinner sSizes;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	protected Color fgColor;
	protected JTree tree;

	JComboBox<String> themeCombo, iconCombo, langCombo, actionsCombo;
	JCheckBox dndEnable, restrictTreeEnable, extraNodesEnable, showTreeFilter, dockEvent, backupsEnable;
	JTextField iconPath, themePath, manualPath, actionsPath;

	JTextField soundEditorPath, backgroundEditorPath, spriteEditorPath, codeEditorPath,
			numberBackupsField;
	// Sounds use their own stored filename/extension, which may vary from sound to sound. 
	JTextField backgroundMIME, spriteMIME, scriptMIME;

	// Room editor fields
	NumberField undoHistorySize;
	JCheckBox useFilledRectangleForViews, useInvertedColorForViews, useFilledRectangleForSelection,
			useInvertedColorForSelection;
	ColorSelect viewInsideColor, viewOutsideColor, selectionInsideColor, selectionOutsideColor;
	private ColorSelect imagePreviewBackgroundColor;
	private ColorSelect imagePreviewForegroundColor;
	private ColorSelect matchCountBackgroundColor;
	private ColorSelect matchCountForegroundColor;
	private ColorSelect resultMatchBackgroundColor;
	private ColorSelect resultMatchForegroundColor;
	private JCheckBox matchCountBackgroundCheckBox;
	private JCheckBox matchCountForegroundCheckBox;
	private JCheckBox resultMatchBackgroundCheckBox;
	private JCheckBox resultMatchForegroundCheckBox;
	private JComboBox<String> direct3DCombo;
	private JComboBox<String> openGLCombo;
	private JComboBox<String> antialiasCombo;

	private JPanel makeAppearancePrefs() {
		JPanel panel = new JPanel();
		
		String[] systemComboItems = { 
				Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_DEFAULT"),
				Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_OFF"),
				Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_ON")
		};
		
		JLabel themeLabel = new JLabel(Messages.getString("PreferencesFrame.THEME") + ":");
		Vector<String> themeComboItems = new Vector<String>();
		themeComboItems.add("Swing");
		themeComboItems.add("Native");
		LookAndFeelInfo lnfs[] = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < lnfs.length; i++)
			{
			themeComboItems.add(lnfs[i].getName());
			if (lnfs[i].getName().toLowerCase().contains("gtk"))
				{
				themeComboItems.add("Quantum");
				}
			}
		themeComboItems.add("Custom");
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(themeComboItems);

		themeCombo = new JComboBox<String>(model);
		themeCombo.setSelectedItem(LGM.themename);
		JLabel iconLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS") + ":");
		iconCombo = new JComboBox<String>();
		//TODO: Do not reactivate, we no longer maintain the Swing icon pack.
		//iconCombo.addItem("Swing");
		iconCombo.addItem("Calico");
		iconCombo.addItem("Contrast");
		iconCombo.addItem("Custom");
		/*  TODO: This is a failed experiment, the code for inside 
		 *  Eclipse works, but outside the IDE we can't properly
		 *  get directories from the Jar.
		 *  This needs figured out so that users can edit the icon packs and add new ones.
		 */
		/*
		File dir = new File("org/lateralgm/icons");
		JOptionPane.showMessageDialog(null,LGM.workDir);
		if (!dir.exists() && LGM.workDir != null)
		{
			dir = new File(LGM.workDir,"org/lateralgm/icons");
			if (!dir.exists()) dir = LGM.workDir;
		}
		String[] directories = null;
		if (!dir.exists()) {
			directories = dir.list(new FilenameFilter() {
				public boolean accept(File current, String name) {
					JOptionPane.showMessageDialog(null,name);
			  	return new File(current, name).isDirectory();
			  }
			});
		} else {
			List<String> res = new ArrayList<String>();
			JarInputStream jar = null;
			try
				{
				jar = new JarInputStream(new FileInputStream(dir));
				}
			catch (FileNotFoundException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			catch (IOException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
	    JarEntry jarEntry = null;
			try
				{
				jarEntry = jar.getNextJarEntry();
				}
			catch (IOException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
	    while (jarEntry != null) {
				if (jarEntry.isDirectory()) {
					String str = jarEntry.getName();
				  if (str.replace("\\","/").contains("org/lateralgm/icons")) {
						JOptionPane.showMessageDialog(null,str);
						res.add(str);
				  }
				 }
				 try
					{
					jarEntry = jar.getNextJarEntry();
					}
				catch (IOException e)
					{
						LGM.showDefaultExceptionHandler(e);
					}
	    }  
				
			directories = res.toArray(new String[res.size()]);
		}
		if (directories != null) {
			for (String name : directories) {
				iconCombo.addItem(name);
			}
		}
		*/
		iconCombo.setSelectedItem(LGM.iconspack);
		
		JLabel antialiasLabel = new JLabel(Messages.getString("PreferencesFrame.ANTIALIASING") + ":");
		antialiasCombo = new JComboBox<String>(new DefaultComboBoxModel<String>(systemComboItems));
		antialiasCombo.setSelectedItem(Prefs.antialiasControlFont);
		
		JLabel iconPathLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS_PATH") + ":");
		iconPath = new JTextField(Prefs.iconPath);
		
		JLabel themePathLabel = new JLabel(Messages.getString("PreferencesFrame.THEME_PATH") + ":");
		themePath = new JTextField(Prefs.swingThemePath);
		
		JPanel imagePreviewPanel = new JPanel();
		GroupLayout imagePreviewLayout = new GroupLayout(imagePreviewPanel);
		imagePreviewLayout.setAutoCreateGaps(true);
		imagePreviewLayout.setAutoCreateContainerGaps(true);
		imagePreviewPanel.setLayout(imagePreviewLayout);

		imagePreviewPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PreferencesFrame.IMAGE_PREVIEW")));

		JLabel imagePreviewBackgroundLabel = new JLabel(Messages.getString("PreferencesFrame.IMAGE_PREVIEW_BACKGROUND_COLOR") + ":");
		imagePreviewBackgroundColor = new ColorSelect(new Color(Prefs.imagePreviewBackgroundColor));
		JLabel imagePreviewForegroundLabel = new JLabel(Messages.getString("PreferencesFrame.IMAGE_PREVIEW_FOREGROUND_COLOR") + ":");
		imagePreviewForegroundColor = new ColorSelect(new Color(Prefs.imagePreviewForegroundColor));
		
		imagePreviewLayout.setHorizontalGroup(imagePreviewLayout.createParallelGroup()
		/*	*/.addGroup(imagePreviewLayout.createSequentialGroup()
		/*		*/.addGroup(imagePreviewLayout.createParallelGroup()
		/*			*/.addComponent(imagePreviewBackgroundLabel)
		/*			*/.addComponent(imagePreviewForegroundLabel))
		/*		*/.addGroup(imagePreviewLayout.createParallelGroup()
		/*			*/.addComponent(imagePreviewBackgroundColor,120,120,120)
		/*			*/.addComponent(imagePreviewForegroundColor,120,120,120))));

		imagePreviewLayout.setVerticalGroup(imagePreviewLayout.createSequentialGroup()
		/*	*/.addGroup(imagePreviewLayout.createParallelGroup()
		/*		*/.addComponent(imagePreviewBackgroundLabel)
		/*		*/.addComponent(imagePreviewBackgroundColor,18,18,18))
		/*	*/.addGroup(imagePreviewLayout.createParallelGroup()
		/*		*/.addComponent(imagePreviewForegroundLabel)
		/*		*/.addComponent(imagePreviewForegroundColor,18,18,18)));
		
		JPanel hardwareAccelerationPanel = new JPanel();
		GroupLayout hardwareAccelerationLayout = new GroupLayout(hardwareAccelerationPanel);
		hardwareAccelerationLayout.setAutoCreateGaps(true);
		hardwareAccelerationLayout.setAutoCreateContainerGaps(true);
		hardwareAccelerationPanel.setLayout(hardwareAccelerationLayout);

		hardwareAccelerationPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PreferencesFrame.HARDWARE_ACCELERATION")));

		JLabel direct3DLabel = new JLabel(Messages.getString("PreferencesFrame.DIRECT3D") + ":");
		direct3DCombo = new JComboBox<String>(new DefaultComboBoxModel<String>(systemComboItems));
		direct3DCombo.setSelectedItem(Prefs.direct3DAcceleration);
		JLabel openGLLabel = new JLabel(Messages.getString("PreferencesFrame.OPENGL") + ":");
		openGLCombo = new JComboBox<String>(new DefaultComboBoxModel<String>(systemComboItems));
		openGLCombo.setSelectedItem(Prefs.openGLAcceleration);
		
		hardwareAccelerationLayout.setHorizontalGroup(hardwareAccelerationLayout.createParallelGroup()
		/*	*/.addGroup(hardwareAccelerationLayout.createSequentialGroup()
		/*		*/.addGroup(hardwareAccelerationLayout.createParallelGroup(Alignment.TRAILING)
		/*			*/.addComponent(direct3DLabel)
		/*			*/.addComponent(openGLLabel))
		/*		*/.addGroup(hardwareAccelerationLayout.createParallelGroup(Alignment.TRAILING)
		/*			*/.addComponent(direct3DCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(openGLCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))));

		hardwareAccelerationLayout.setVerticalGroup(hardwareAccelerationLayout.createSequentialGroup()
		/*	*/.addGroup(hardwareAccelerationLayout.createParallelGroup(Alignment.CENTER)
		/*			*/.addComponent(direct3DLabel)
		/*			*/.addComponent(direct3DCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/*	*/.addGroup(hardwareAccelerationLayout.createParallelGroup(Alignment.CENTER)
		/*			*/.addComponent(openGLLabel)
		/*			*/.addComponent(openGLCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)));
		
		JPanel searchResultsPanel = new JPanel();
		GroupLayout searchResultsLayout = new GroupLayout(searchResultsPanel);
		searchResultsLayout.setAutoCreateGaps(true);
		searchResultsLayout.setAutoCreateContainerGaps(true);
		searchResultsPanel.setLayout(searchResultsLayout);

		searchResultsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PreferencesFrame.SEARCH_RESULTS")));

		JLabel matchCountLable = new JLabel(Messages.getString("PreferencesFrame.MATCH_COUNT"));
		matchCountBackgroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.MATCH_COUNT_BACKGROUND_COLOR") + ":", Prefs.highlightMatchCountBackground);
		matchCountBackgroundColor = new ColorSelect(new Color(Prefs.matchCountBackgroundColor));
		matchCountForegroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.MATCH_COUNT_FOREGROUND_COLOR") + ":", Prefs.highlightMatchCountForeground);
		matchCountForegroundColor = new ColorSelect(new Color(Prefs.matchCountForegroundColor));
		
		JLabel resultMatchLabel = new JLabel(Messages.getString("PreferencesFrame.RESULT_MATCH"));
		resultMatchBackgroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.RESULT_MATCH_BACKGROUND_COLOR") + ":", Prefs.highlightResultMatchBackground);
		resultMatchBackgroundColor = new ColorSelect(new Color(Prefs.resultMatchBackgroundColor));
		resultMatchForegroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.RESULT_MATCH_FOREGROUND_COLOR") + ":", Prefs.highlightResultMatchForeground);
		resultMatchForegroundColor = new ColorSelect(new Color(Prefs.resultMatchForegroundColor));
		
		searchResultsLayout.setHorizontalGroup(searchResultsLayout.createParallelGroup()
		/**/.addComponent(matchCountLable)
		/*	*/.addGroup(searchResultsLayout.createSequentialGroup()
		/*		*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.LEADING)
		/*			*/.addComponent(matchCountBackgroundCheckBox)
		/*			*/.addComponent(matchCountForegroundCheckBox))
		/*		*/.addGroup(searchResultsLayout.createParallelGroup()
		/*			*/.addComponent(matchCountBackgroundColor,120,120,120)
		/*			*/.addComponent(matchCountForegroundColor,120,120,120)))
		/**/.addComponent(resultMatchLabel)
		/*	*/.addGroup(searchResultsLayout.createSequentialGroup()
		/*		*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.LEADING)
		/*			*/.addComponent(resultMatchBackgroundCheckBox)
		/*			*/.addComponent(resultMatchForegroundCheckBox))
		/*		*/.addGroup(searchResultsLayout.createParallelGroup()
		/*			*/.addComponent(resultMatchBackgroundColor,120,120,120)
		/*			*/.addComponent(resultMatchForegroundColor,120,120,120))));

		searchResultsLayout.setVerticalGroup(searchResultsLayout.createSequentialGroup()
		/**/.addComponent(matchCountLable)
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.CENTER)
		/*		*/.addComponent(matchCountBackgroundCheckBox)
		/*		*/.addComponent(matchCountBackgroundColor,18,18,18))
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.CENTER)
		/*		*/.addComponent(matchCountForegroundCheckBox)
		/*		*/.addComponent(matchCountForegroundColor,18,18,18))
		/**/.addComponent(resultMatchLabel)
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.CENTER)
		/*		*/.addComponent(resultMatchBackgroundCheckBox)
		/*		*/.addComponent(resultMatchBackgroundColor,18,18,18))
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.CENTER)
		/*		*/.addComponent(resultMatchForegroundCheckBox)
		/*		*/.addComponent(resultMatchForegroundColor,18,18,18)));

		
		GroupLayout gl = new GroupLayout(panel);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/* */.addComponent(themeLabel)
		/* */.addComponent(themeCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/* */.addComponent(iconLabel)
		/* */.addComponent(iconCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/* */.addComponent(antialiasLabel)
		/* */.addComponent(antialiasCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createSequentialGroup()
		/* */.addGroup(gl.createParallelGroup(Alignment.TRAILING)
		/*  */.addComponent(themePathLabel)
		/*  */.addComponent(iconPathLabel))
		/* */.addGroup(gl.createParallelGroup()
		/*  */.addComponent(themePath)
		/*  */.addComponent(iconPath)))
		/**/.addGroup(gl.createSequentialGroup()
		/* */.addComponent(imagePreviewPanel)
		/* */.addComponent(hardwareAccelerationPanel))
		/**/.addComponent(searchResultsPanel));

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/* */.addComponent(themeLabel)
		/* */.addComponent(themeCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/* */.addComponent(iconLabel)
		/* */.addComponent(iconCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/* */.addComponent(antialiasLabel)
		/* */.addComponent(antialiasCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/* */.addComponent(themePathLabel)
		/* */.addComponent(themePath,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/* */.addComponent(iconPathLabel)
		/* */.addComponent(iconPath,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/* */.addComponent(imagePreviewPanel)
		/* */.addComponent(hardwareAccelerationPanel))
		/**/.addComponent(searchResultsPanel));

		panel.setLayout(gl);
		
		return panel;
	}
	
	private JPanel makeGeneralPrefs()
		{
		JPanel p = new JPanel();

		JLabel langLabel = new JLabel(Messages.getString("PreferencesFrame.LANGUAGE") + ":");
		String[] langOptions = { "English","French","Turkish","Danish" };
		langCombo = new JComboBox<String>(langOptions);
		langCombo.setSelectedItem(Prefs.languageName);
		dndEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_DND"));
		dndEnable.setSelected(Prefs.enableDragAndDrop);
		restrictTreeEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_TREE_RESTRICT"));
		restrictTreeEnable.setSelected(Prefs.restrictHierarchy);
		extraNodesEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_EXTRA_NODES"));
		extraNodesEnable.setSelected(Prefs.extraNodes);
		showTreeFilter = new JCheckBox(Messages.getString("PreferencesFrame.SHOW_TREE_FILTER"));
		showTreeFilter.setSelected(Prefs.showTreeFilter);
		dockEvent = new JCheckBox(Messages.getString("PreferencesFrame.DOCK_EVENT_PANEL"));
		dockEvent.setSelected(Prefs.dockEventPanel);
		JLabel manualPathLabel = new JLabel(Messages.getString("PreferencesFrame.MANUAL_PATH") + ":");
		manualPath = new JTextField(Prefs.manualPath);
		//JLabel actionsLabel = new JLabel(Messages.getString("PreferencesFrame.ACTIONLIBRARY") + ":");
		String[] actionsOptions = { "Standard","Logic","Custom" };
		actionsCombo = new JComboBox<String>(actionsOptions);
		//actionsCombo.setSelectedItem(Prefs.actionLibrary);
		actionsPath = new JTextField();
		actionsPath.setText(Prefs.userLibraryPath);
		backupsEnable = new JCheckBox(Messages.getString("PreferencesFrame.CREATE_BACKUPS"));
		backupsEnable.setSelected(Prefs.enableBackupSave);
		JLabel backupsLabel = new JLabel(Messages.getString("PreferencesFrame.NUMBER_BACKUPS") + ":");
		numberBackupsField = new JTextField(Prefs.numberofBackups);

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(langLabel)
		/*	*/.addComponent(langCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(dndEnable)
		/*	*/.addComponent(dockEvent)
		/*  */.addComponent(showTreeFilter))
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(backupsEnable))
		/**/.addGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
		/*	*/.addComponent(backupsLabel)
		/*	*/.addComponent(manualPathLabel))
		/* */.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(numberBackupsField)
		/*	*/.addComponent(manualPath))));

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(langLabel)
		/*	*/.addComponent(langCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(dndEnable)
		/*	*/.addComponent(dockEvent)
		/*   */.addComponent(showTreeFilter))
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(backupsEnable,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(backupsLabel)
		/*	*/.addComponent(numberBackupsField,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(manualPathLabel)
		/*	*/.addComponent(manualPath,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)));

		p.setLayout(gl);

		return p;
		}

	private JPanel makeMimePrefixPrefs()
		{
		JPanel p = new JPanel();

		JLabel spriteLabel = new JLabel("Sprite:");
		JTextField spritePrefix = new JTextField("spr_");
		JLabel soundLabel = new JLabel("Sound:");
		JTextField soundPrefix = new JTextField("snd_");
		JLabel backgroundLabel = new JLabel("Background:");
		JTextField backgroundPrefix = new JTextField("bg_");
		JLabel pathLabel = new JLabel("Path:");
		JTextField pathPrefix = new JTextField("pth_");
		JLabel scriptLabel = new JLabel("Script:");
		JTextField scriptPrefix = new JTextField("scr_");
		JLabel fontLabel = new JLabel("Font:");
		JTextField fontPrefix = new JTextField("fnt_");
		JLabel timelineLabel = new JLabel("Timeline:");
		JTextField timelinePrefix = new JTextField("tl_");
		JLabel objectLabel = new JLabel("Object:");
		JTextField objectPrefix = new JTextField("obj_");
		JLabel roomLabel = new JLabel("Room:");
		JTextField roomPrefix = new JTextField("rm_");

		JLabel backgroundMIMELabel = new JLabel("Background MIME:");
		backgroundMIME = new JTextField(Prefs.externalBackgroundExtension);
		JLabel spriteMIMELabel = new JLabel("Sprite MIME:");
		spriteMIME = new JTextField(Prefs.externalSpriteExtension);
		JLabel scriptMIMELabel = new JLabel("Script MIME:");
		scriptMIME = new JTextField(Prefs.externalScriptExtension);

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(
		/**/gl.createParallelGroup(Alignment.LEADING)
		/*	*/.addGroup(gl.createSequentialGroup()
		/*		*/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
		/*			*/.addComponent(timelineLabel)
		/*			*/.addComponent(pathLabel)
		/*			*/.addComponent(spriteLabel))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.LEADING)
		/*			*/.addComponent(spritePrefix)
		/*			*/.addComponent(timelinePrefix)
		/*			*/.addComponent(pathPrefix))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.LEADING,false)
		/*			*/.addComponent(soundLabel)
		/*			*/.addComponent(objectLabel)
		/*			*/.addComponent(scriptLabel))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.LEADING)
		/*			*/.addComponent(soundPrefix)
		/*			*/.addComponent(scriptPrefix)
		/*			*/.addComponent(objectPrefix))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.LEADING,false)
		/*			*/.addComponent(backgroundLabel)
		/*			*/.addComponent(fontLabel)
		/*			*/.addComponent(roomLabel))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.LEADING)
		/*			*/.addComponent(roomPrefix)
		/*			*/.addComponent(fontPrefix)
		/*			*/.addComponent(backgroundPrefix)))
		/*		*/.addGroup(gl.createSequentialGroup()
		/*			*/.addComponent(backgroundMIMELabel)
		/*			*/.addComponent(backgroundMIME)
		/*			*/.addComponent(spriteMIMELabel)
		/*			*/.addComponent(spriteMIME)
		/*			*/.addComponent(scriptMIMELabel)
		/*			*/.addComponent(scriptMIME)));

		gl.setVerticalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addGroup(gl.createSequentialGroup()
		/*		*/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*			*/.addComponent(soundPrefix)
		/*			*/.addComponent(backgroundLabel)
		/*			*/.addComponent(backgroundPrefix)
		/*			*/.addComponent(spritePrefix,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(spriteLabel)
		/*			*/.addComponent(soundLabel))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*			*/.addComponent(pathLabel)
		/*			*/.addComponent(pathPrefix,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(scriptPrefix,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(fontPrefix,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(scriptLabel)
		/*			*/.addComponent(fontLabel))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*			*/.addComponent(timelinePrefix,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(objectPrefix,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(roomPrefix,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(timelineLabel)
		/*			*/.addComponent(objectLabel)
		/*			*/.addComponent(roomLabel,PREFERRED_SIZE,18,PREFERRED_SIZE)))
		/*		*/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*			*/.addComponent(backgroundMIMELabel)
		/*			*/.addComponent(backgroundMIME,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(spriteMIMELabel)
		/*			*/.addComponent(spriteMIME,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(scriptMIMELabel)
		/*			*/.addComponent(scriptMIME,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)));

		p.setLayout(gl);

		return p;
		}

	private JPanel makeExternalEditorPrefs()
		{
		JPanel p = new JPanel();

		JLabel codeEditorLabel = new JLabel(Messages.getString("PreferencesFrame.CODE_EDITOR") + ":");
		codeEditorPath = new JTextField(Prefs.externalScriptEditorCommand);

		JLabel spriteEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SPRITE_EDITOR")
				+ ":");
		spriteEditorPath = new JTextField(Prefs.externalSpriteEditorCommand);

		JLabel backgroundEditorLabel = new JLabel(
				Messages.getString("PreferencesFrame.BACKGROUND_EDITOR") + ":");
		backgroundEditorPath = new JTextField(Prefs.externalBackgroundEditorCommand);

		JLabel soundEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SOUND_EDITOR") + ":");
		soundEditorPath = new JTextField(Prefs.externalSoundEditorCommand);

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		// TODO: Fix this layout so that it resizes the text fields properly
		gl.setHorizontalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(spriteEditorLabel)
		/*		*/.addComponent(soundEditorLabel)
		/*		*/.addComponent(backgroundEditorLabel)
		/*		*/.addComponent(codeEditorLabel))
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(spriteEditorPath)
		/*		*/.addComponent(soundEditorPath)
		/*		*/.addComponent(backgroundEditorPath)
		/*		*/.addComponent(codeEditorPath)));

		gl.setVerticalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(spriteEditorLabel)
		/*		*/.addComponent(spriteEditorPath))
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(soundEditorLabel)
		/*		*/.addComponent(soundEditorPath))
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(backgroundEditorLabel)
		/*		*/.addComponent(backgroundEditorPath))
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(codeEditorLabel)
		/*		*/.addComponent(codeEditorPath)));

		p.setLayout(gl);

		return p;
		}

	private JPanel makeCodeEditorPrefs()
		{
		JPanel p = new JPanel();

		//TODO: Make components

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		//TODO: Add components to layout

		return p;
		}

	// Create the room editor panel
	private Component makeRoomEditorPrefs()
		{
		JPanel roomEditorPanel = new JPanel();

		// Undo settings
		JLabel undoHistorySizeLabel = new JLabel(
				Messages.getString("PreferencesFrame.UNDO_HISTORY_SIZE") + " : ");
		undoHistorySize = new NumberField(-1,999999,Prefs.undoHistorySize);

		// Views settings
		JPanel viewsPanel = new JPanel();
		GroupLayout viewsLayout = new GroupLayout(viewsPanel);
		viewsLayout.setAutoCreateGaps(true);
		viewsLayout.setAutoCreateContainerGaps(true);
		viewsPanel.setLayout(viewsLayout);

		String title = Messages.getString("PreferencesFrame.VIEWS_TITLE");
		viewsPanel.setBorder(BorderFactory.createTitledBorder(title));

		useFilledRectangleForViews = new JCheckBox(
				Messages.getString("PreferencesFrame.FILLED_RECTANGLE"));
		useFilledRectangleForViews.setSelected(Prefs.useFilledRectangleForViews);

		useInvertedColorForViews = new JCheckBox(Messages.getString("PreferencesFrame.INVERTED_COLOR"));
		useInvertedColorForViews.setSelected(Prefs.useInvertedColorForViews);

		JLabel insideColorLabel = new JLabel(Messages.getString("PreferencesFrame.INSIDE_COLOR")
				+ " : ");
		viewInsideColor = new ColorSelect(Util.convertGmColorWithAlpha(Prefs.viewInsideColor));

		JLabel outsideColorLabel = new JLabel(Messages.getString("PreferencesFrame.OUTSIDE_COLOR")
				+ " : ");
		viewOutsideColor = new ColorSelect(Util.convertGmColorWithAlpha(Prefs.viewOutsideColor));

		// Set the layout for the views
		viewsLayout.setHorizontalGroup(
		/**/viewsLayout.createParallelGroup()
		/*	*/.addGroup(viewsLayout.createSequentialGroup()
		/*		*/.addComponent(useFilledRectangleForViews))
		/*	*/.addGroup(viewsLayout.createSequentialGroup()
		/*		*/.addGroup(viewsLayout.createParallelGroup()
		/*			*/.addComponent(useInvertedColorForViews)
		/*			*/.addComponent(insideColorLabel)
		/*			*/.addComponent(outsideColorLabel))
		/*		*/.addGroup(viewsLayout.createParallelGroup()
		/*			*/.addComponent(viewInsideColor,120,120,120)
		/*			*/.addComponent(viewOutsideColor,120,120,120))));

		viewsLayout.setVerticalGroup(
		/**/viewsLayout.createSequentialGroup()
		/*	*/.addComponent(useFilledRectangleForViews)
		/*	*/.addComponent(useInvertedColorForViews).addGap(10)
		/*	*/.addGroup(viewsLayout.createParallelGroup()
		/*		*/.addComponent(insideColorLabel)
		/*		*/.addComponent(viewInsideColor,18,18,18))
		/*	*/.addGroup(viewsLayout.createParallelGroup()
		/*		*/.addComponent(outsideColorLabel)
		/*		*/.addComponent(viewOutsideColor,18,18,18)));

		// Selection settings
		JPanel selectionPanel = new JPanel();
		GroupLayout selectionLayout = new GroupLayout(selectionPanel);
		selectionLayout.setAutoCreateGaps(true);
		selectionLayout.setAutoCreateContainerGaps(true);
		selectionPanel.setLayout(selectionLayout);

		String selectionTitle = Messages.getString("PreferencesFrame.SELECTION");
		selectionPanel.setBorder(BorderFactory.createTitledBorder(selectionTitle));

		useFilledRectangleForSelection = new JCheckBox(
				Messages.getString("PreferencesFrame.FILLED_RECTANGLE"));
		useFilledRectangleForSelection.setSelected(Prefs.useFilledRectangleForSelection);

		useInvertedColorForSelection = new JCheckBox(
				Messages.getString("PreferencesFrame.INVERTED_COLOR"));
		useInvertedColorForSelection.setSelected(Prefs.useInvertedColorForSelection);

		JLabel insideColorLabelForSelection = new JLabel(
				Messages.getString("PreferencesFrame.INSIDE_COLOR") + " : ");
		selectionInsideColor = new ColorSelect(Util.convertGmColorWithAlpha(Prefs.selectionInsideColor));

		JLabel outsideColorLabelForSelection = new JLabel(
				Messages.getString("PreferencesFrame.OUTSIDE_COLOR") + " : ");
		selectionOutsideColor = new ColorSelect(
				Util.convertGmColorWithAlpha(Prefs.selectionOutsideColor));

		// Set the layout for the views
		selectionLayout.setHorizontalGroup(
		/**/selectionLayout.createParallelGroup()
		/*	*/.addGroup(selectionLayout.createSequentialGroup()
		/*		*/.addComponent(useFilledRectangleForSelection))
		/*	*/.addGroup(selectionLayout.createSequentialGroup()
		/*		*/.addGroup(selectionLayout.createParallelGroup()
		/*			*/.addComponent(useInvertedColorForSelection)
		/*			*/.addComponent(insideColorLabelForSelection)
		/*			*/.addComponent(outsideColorLabelForSelection))
		/*		*/.addGroup(selectionLayout.createParallelGroup()
		/*			*/.addComponent(selectionInsideColor,120,120,120)
		/*			*/.addComponent(selectionOutsideColor,120,120,120))));

		selectionLayout.setVerticalGroup(
		/**/selectionLayout.createSequentialGroup()
		/*	*/.addComponent(useFilledRectangleForSelection)
		/*	*/.addComponent(useInvertedColorForSelection).addGap(10)
		/*	*/.addGroup(selectionLayout.createParallelGroup()
		/*		*/.addComponent(insideColorLabelForSelection)
		/*		*/.addComponent(selectionInsideColor,18,18,18))
		/*	*/.addGroup(selectionLayout.createParallelGroup()
		/*		*/.addComponent(outsideColorLabelForSelection)
		/*		*/.addComponent(selectionOutsideColor,18,18,18)));

		// Set the layout for the main panel
		GroupLayout gl = new GroupLayout(roomEditorPanel);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(
		/**/gl.createParallelGroup()
		/*	*/.addGroup(gl.createSequentialGroup()
		/*		*/.addComponent(undoHistorySizeLabel)
		/*		*/.addComponent(undoHistorySize,100,100,100))
		/*	*/.addGroup(gl.createSequentialGroup()
		/*		*/.addComponent(viewsPanel,320,320,320))
		/*		*/.addComponent(selectionPanel,320,320,320));

		gl.setVerticalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(undoHistorySizeLabel)
		/*		*/.addComponent(undoHistorySize,18,18,18))
		/*		*/.addComponent(viewsPanel,150,150,150)
		/*		*/.addComponent(selectionPanel,150,150,150));

		roomEditorPanel.setLayout(gl);

		return roomEditorPanel;
		}

	public PreferencesFrame()
		{
		setAlwaysOnTop(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		setTitle(Messages.getString("PreferencesFrame.TITLE"));
		setIconImage(LGM.getIconForKey("Toolbar.PREFERENCES").getImage());
		setResizable(true);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");
		
		tree = new JTree(new DefaultTreeModel(root));
		tree.setEditable(false);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
    renderer.setLeafIcon(null);
    renderer.setClosedIcon(null);
    renderer.setOpenIcon(null);

		cardPane = new JPanel(new CardLayout());

		DefaultMutableTreeNode  node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_GENERAL"));
		root.add(node);
		cardPane.add(makeGeneralPrefs(),Messages.getString("PreferencesFrame.TAB_GENERAL"));
		
		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_APPEARANCE"));
		root.add(node);
		cardPane.add(makeAppearancePrefs(),Messages.getString("PreferencesFrame.TAB_APPEARANCE"));
		
		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_EXTERNAL_EDITOR"));
		root.add(node);
		cardPane.add(makeExternalEditorPrefs(),Messages.getString("PreferencesFrame.TAB_EXTERNAL_EDITOR"));
		
		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_MIME_PREFIX"));
		root.add(node);
		cardPane.add(makeMimePrefixPrefs(),Messages.getString("PreferencesFrame.TAB_MIME_PREFIX"));
		
		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_CODE_EDITOR"));
		root.add(node);
		DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_KEYBINDINGS"));
		node.add(cnode);
		cnode = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_SYNTAX_HIGHLIGHTING"));
		node.add(cnode);
		cardPane.add(new org.lateralgm.joshedit.preferences.KeybindingsPanel(),Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_KEYBINDINGS"));
		cardPane.add(new org.lateralgm.joshedit.preferences.HighlightPreferences(
				new TokenMarker.LanguageDescription[][] { GMLTokenMarker.getLanguageDescriptions(), GLSLTokenMarker.getLanguageDescriptions(), GLESTokenMarker.getLanguageDescriptions(), HLSLTokenMarker.getLanguageDescriptions() }, 
				Preferences.userRoot().node("org/lateralgm/joshedit")),Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_SYNTAX_HIGHLIGHTING"));
		
		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_ROOM_EDITOR"));
		root.add(node);
		cardPane.add(makeRoomEditorPrefs(),Messages.getString("PreferencesFrame.TAB_ROOM_EDITOR"));
		
		// reload after adding all root children to make sure its children are visible
		((DefaultTreeModel)tree.getModel()).reload();
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
  	public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				                   tree.getLastSelectedPathComponent();
				
				/* if nothing is selected */ 
				if (node == null) return;
				
				/* retrieve the node that was selected */ 
				String nodeInfo = node.getUserObject().toString();
				
				CardLayout cl = (CardLayout)(cardPane.getLayout());
		    cl.show(cardPane, nodeInfo);
			}
		});
		
		JSplitPaneExpandable split = new JSplitPaneExpandable(JSplitPane.HORIZONTAL_SPLIT,true,tree,cardPane);
		split.setDividerLocation(200);
		split.setDoubleClickExpandable(true);
		add(split);
		
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		String key;

		key = "PreferencesFrame.APPLY_CHANGES";
		JButton applyBut = new JButton(Messages.getString(key));
		applyBut.addActionListener(this);
		applyBut.setActionCommand(key);

		key = "PreferencesFrame.CLOSE";
		JButton closeBut = new JButton(Messages.getString(key));
		closeBut.addActionListener(this);
		closeBut.setActionCommand(key);

		gl.setHorizontalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(applyBut)
		/*		*/.addComponent(closeBut)));


		add(p,BorderLayout.SOUTH);
		pack();
		setSize(800,500);
		setLocationRelativeTo(LGM.frame);
		}

	public void savePreferences()
		{
		LGM.iconspack = (String) iconCombo.getSelectedItem();
		PrefsStore.setIconPack(LGM.iconspack);
		PrefsStore.setIconPath(iconPath.getText());
		PrefsStore.setSwingThemePath(themePath.getText());
		PrefsStore.setSwingTheme((String) themeCombo.getSelectedItem());
		PrefsStore.setManualPath(manualPath.getText());
		PrefsStore.setDNDEnabled(dndEnable.isSelected());
		PrefsStore.setExtraNodes(extraNodesEnable.isSelected());
		PrefsStore.setShowTreeFilter(showTreeFilter.isSelected());
		PrefsStore.setLanguageName((String) langCombo.getSelectedItem());
		PrefsStore.setUserLibraryPath(actionsPath.getText());
		PrefsStore.setSpriteExt(spriteMIME.getText());
		PrefsStore.setBackgroundExt(backgroundMIME.getText());
		PrefsStore.setScriptExt(scriptMIME.getText());
		PrefsStore.setBackgroundEditorCommand(backgroundEditorPath.getText());
		PrefsStore.setSpriteEditorCommand(spriteEditorPath.getText());
		PrefsStore.setSoundEditorCommand(soundEditorPath.getText());
		PrefsStore.setScriptEditorCommand(codeEditorPath.getText());
		PrefsStore.setDockEventPanel(dockEvent.isSelected());
		PrefsStore.setUndoHistorySize(undoHistorySize.getIntValue());
		PrefsStore.setFilledRectangleForViews(useFilledRectangleForViews.isSelected());
		PrefsStore.setInvertedColorForViews(useInvertedColorForViews.isSelected());
		PrefsStore.setViewInsideColor(Util.getGmColorWithAlpha(viewInsideColor.getSelectedColor()));
		PrefsStore.setViewOutsideColor(Util.getGmColorWithAlpha(viewOutsideColor.getSelectedColor()));
		PrefsStore.setFilledRectangleForSelection(useFilledRectangleForSelection.isSelected());
		PrefsStore.setInvertedColorForSelection(useInvertedColorForSelection.isSelected());
		PrefsStore.setSelectionInsideColor(Util.getGmColorWithAlpha(selectionInsideColor.getSelectedColor()));
		PrefsStore.setSelectionOutsideColor(Util.getGmColorWithAlpha(selectionOutsideColor.getSelectedColor()));
		
		PrefsStore.setAntialiasControlFont(antialiasCombo.getSelectedItem().toString().toLowerCase());
		PrefsStore.setDirect3DAcceleration(direct3DCombo.getSelectedItem().toString().toLowerCase());
		PrefsStore.setOpenGLAcceleration(openGLCombo.getSelectedItem().toString().toLowerCase());
		PrefsStore.setImagePreviewBackgroundColor(imagePreviewBackgroundColor.getSelectedColor().getRGB());
		PrefsStore.setImagePreviewForegroundColor(imagePreviewForegroundColor.getSelectedColor().getRGB());
		PrefsStore.setHighlightMatchCountBackground(matchCountBackgroundCheckBox.isSelected());
		PrefsStore.setHighlightMatchCountForeground(matchCountForegroundCheckBox.isSelected());
		PrefsStore.setMatchCountBackgroundColor(matchCountBackgroundColor.getSelectedColor().getRGB());
		PrefsStore.setMatchCountForegroundColor(matchCountForegroundColor.getSelectedColor().getRGB());
		PrefsStore.setHighlightResultMatchBackground(resultMatchBackgroundCheckBox.isSelected());
		PrefsStore.setHighlightResultMatchForeground(resultMatchForegroundCheckBox.isSelected());
		PrefsStore.setResultMatchBackgroundColor(resultMatchBackgroundColor.getSelectedColor().getRGB());
		PrefsStore.setResultMatchForegroundColor(resultMatchForegroundColor.getSelectedColor().getRGB());
		}

	public void resetPreferences()
		{
		//TODO: Reset preferences to their active state when this frame was first opened
		// For this one just copy the shit above where I give the controls their default
		// values froms Prefs.
		}

	public void resetPreferencesToDefault()
		{
		//TODO: Reset the preferences to their settings when LGM was first installed
		}

	public void actionPerformed(ActionEvent ev)
		{
		String com = ev.getActionCommand();
		if (com.equals("PreferencesFrame.APPLY_CHANGES")) //$NON-NLS-1$
			{
			JOptionPane.showMessageDialog(this,
					Messages.getString("PreferencesFrame.APPLY_CHANGES_NOTICE"));
			LGM.filterPanel.setVisible(showTreeFilter.isSelected());
			savePreferences();
			LGM.applyPreferences();
			LGM.setLookAndFeel((String) themeCombo.getSelectedItem());
			LGM.updateLookAndFeel();
			}
		if (com.equals("PreferencesFrame.CLOSE")) //$NON-NLS-1$
			{
			this.setVisible(false);
			}
		}
	}