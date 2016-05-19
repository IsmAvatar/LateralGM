/**
* @file  PreferencesFrame.java
* @brief Class implementing a frame for the user to edit the Java preferences for the application
* including styles and the look and feel.
*
* @section License
*
* Copyright (C) 2013-2015 Robert B. Colton
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
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
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sprite;

public class PreferencesFrame extends JDialog implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	protected JPanel cardPane;
	protected JSpinner sSizes;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	protected Color fgColor;
	protected JTree tree;
	protected JLabel applyChangesLabel;

	JComboBox<Locale> localeCombo;
	JComboBox<String> themeCombo, iconCombo, actionsCombo;
	JCheckBox dndEnable, restrictTreeEnable, extraNodesEnable, showTreeFilter, dockEvent,
		rightOrientation, backupsEnable;
	JTextField iconPath, themePath, documentationURI, websiteURI, communityURI, issueURI, actionsPath;

	JTextField soundEditorPath, backgroundEditorPath, spriteEditorPath, codeEditorPath,
		numberBackupsField;
	// Sounds use their own stored filename/extension, which may vary from sound to sound.
	JTextField backgroundExtension, spriteExtension, scriptExtension;

	// Room editor fields
	NumberField undoHistorySize;
	JCheckBox useFilledRectangleForViews, useInvertedColorForViews, useFilledRectangleForSelection,
		useInvertedColorForSelection, useFilledRectangleForMultipleSelection,
		useInvertedColorForMultipleSelection;
	ColorSelect viewInsideColor, viewOutsideColor, selectionInsideColor, selectionOutsideColor,
		multipleSelectionInsideColor, multipleSelectionOutsideColor;
	private ColorSelect imagePreviewBackgroundColor, imagePreviewForegroundColor,
		matchCountBackgroundColor, matchCountForegroundColor, resultMatchBackgroundColor,
		resultMatchForegroundColor;
	private JCheckBox matchCountBackgroundCheckBox, matchCountForegroundCheckBox,
		resultMatchBackgroundCheckBox, resultMatchForegroundCheckBox;
	private JComboBox<String> direct3DCombo, openGLCombo, antialiasCombo;
	private JCheckBox decorateWindowBordersCheckBox;

	private JButton getURIBrowseButton(final JTextField textField) {
		JButton button = new JButton(Messages.getString("PreferencesFrame.BROWSE"));
		button.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.showOpenDialog(PreferencesFrame.this);
			File file = fc.getSelectedFile();
			if (file != null) {
				documentationURI.setText(file.toURI().toString());
			}
		}
		});
		return button;
	}

	private JPanel makeGeneralPrefs()
		{
		JPanel p = new JPanel();

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
		rightOrientation = new JCheckBox(Messages.getString("PreferencesFrame.RIGHT_ORIENTATION"));
		rightOrientation.setSelected(Prefs.rightOrientation);

		JLabel documentationLabel = new JLabel(
			Messages.getString("PreferencesFrame.DOCUMENTATION_URI"));
		documentationURI = new JTextField(Prefs.documentationURI);
		JButton documentationBrowse = getURIBrowseButton(documentationURI);
		JLabel websiteLabel = new JLabel(
			Messages.getString("PreferencesFrame.WEBSITE_URI"));
		websiteURI = new JTextField(Prefs.websiteURI);
		JButton websiteBrowse = getURIBrowseButton(websiteURI);
		JLabel communityLabel = new JLabel(
			Messages.getString("PreferencesFrame.COMMUNITY_URI"));
		communityURI = new JTextField(Prefs.communityURI);
		JButton communityBrowse = getURIBrowseButton(communityURI);
		JLabel issueLabel = new JLabel(
			Messages.getString("PreferencesFrame.ISSUE_URI"));
		issueURI = new JTextField(Prefs.issueURI);
		JButton issueBrowse = getURIBrowseButton(issueURI);

		//JLabel actionsLabel = new JLabel(Messages.getString("PreferencesFrame.ACTIONLIBRARY"));
		String[] actionsOptions = { "Standard","Logic","Custom" };
		actionsCombo = new JComboBox<String>(actionsOptions);
		//actionsCombo.setSelectedItem(Prefs.actionLibrary);
		actionsPath = new JTextField();
		actionsPath.setText(Prefs.userLibraryPath);

		JPanel backupsPanel = new JPanel();
		backupsPanel.setBorder(BorderFactory.createTitledBorder("Backups"));

		JCheckBox backupSave = new JCheckBox("On save", Prefs.backupSave);
		JCheckBox backupExit = new JCheckBox("On exit", Prefs.backupExit);
		JCheckBox backupFrequently = new JCheckBox("On interval", Prefs.backupInterval);

		JLabel maxCopiesLabel = new JLabel("Copies:");
		JSpinner maxCopiesField = new JSpinner(new SpinnerNumberModel(Prefs.backupCopies, 0, 5, 1));
		JLabel hoursLabel = new JLabel("Hours:");
		JSpinner hoursField = new JSpinner(new SpinnerNumberModel(Prefs.backupHours, 0, 12, 1));
		JLabel minutesLabel = new JLabel("Minutes:");
		JSpinner minutesField = new JSpinner(new SpinnerNumberModel(Prefs.backupMinutes, 0, 59, 1));

		GroupLayout backupsLayout = new GroupLayout(backupsPanel);
		backupsLayout.setAutoCreateGaps(true);
		backupsLayout.setAutoCreateContainerGaps(true);
		backupsPanel.setLayout(backupsLayout);

		backupsLayout.setHorizontalGroup(backupsLayout.createSequentialGroup()
		/**/.addGroup(backupsLayout.createParallelGroup(Alignment.TRAILING)
		/*	*/.addComponent(maxCopiesLabel)
		/*	*/.addComponent(hoursLabel)
		/*	*/.addComponent(minutesLabel))
		/**/.addGroup(backupsLayout.createParallelGroup()
		/*	*/.addComponent(maxCopiesField)
		/*	*/.addComponent(hoursField)
		/*	*/.addComponent(minutesField))
		/**/.addGroup(backupsLayout.createParallelGroup()
		/*	*/.addComponent(backupSave)
		/*	*/.addComponent(backupExit)
		/*	*/.addComponent(backupFrequently)));

		backupsLayout.setVerticalGroup(backupsLayout.createSequentialGroup()
		/**/.addGroup(backupsLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(maxCopiesLabel)
		/*	*/.addComponent(maxCopiesField)
		/*	*/.addComponent(backupSave))
		/**/.addGroup(backupsLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(hoursLabel)
		/*	*/.addComponent(hoursField)
		/*	*/.addComponent(backupExit))
		/**/.addGroup(backupsLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(minutesLabel)
		/*	*/.addComponent(minutesField)
		/*	*/.addComponent(backupFrequently)));

		JLabel localeLabel = new JLabel(Messages.getString("PreferencesFrame.LOCALE"));
		JLabel localeWarningLabel = new JLabel(Messages.getString("PreferencesFrame.LOCALE_WARNING"));

		Locale[] locales = Locale.getAvailableLocales();
		// sort our list of locales by display name
		Arrays.sort(locales, new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2)
				{
				return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
		});
		localeCombo = new JComboBox<Locale>(locales);
		localeCombo.setSelectedItem(Prefs.locale);

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		p.setLayout(gl);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(localeLabel)
		/*		*/.addComponent(documentationLabel)
		/*		*/.addComponent(websiteLabel)
		/*		*/.addComponent(communityLabel)
		/*		*/.addComponent(issueLabel))
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addGroup(gl.createSequentialGroup()
		/*			*/.addComponent(localeCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(localeWarningLabel))
		/*		*/.addGroup(gl.createSequentialGroup()
		/*			*/.addGroup(gl.createParallelGroup()
		/*				*/.addComponent(documentationURI)
		/*				*/.addComponent(websiteURI)
		/*				*/.addComponent(communityURI)
		/*				*/.addComponent(issueURI))
		/*			*/.addGroup(gl.createParallelGroup()
		/*				*/.addComponent(documentationBrowse)
		/*				*/.addComponent(websiteBrowse)
		/*				*/.addComponent(communityBrowse)
		/*				*/.addComponent(issueBrowse)))))
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(backupsPanel,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(dndEnable)
		/*		*/.addComponent(dockEvent)
		/*		*/.addComponent(rightOrientation)
		/*		*/.addComponent(showTreeFilter))));

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(localeLabel)
		/*	*/.addComponent(localeCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*	*/.addComponent(localeWarningLabel))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(documentationLabel)
		/*	*/.addComponent(documentationURI)
		/*	*/.addComponent(documentationBrowse))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(websiteLabel)
		/*	*/.addComponent(websiteURI)
		/*	*/.addComponent(websiteBrowse))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(communityLabel)
		/*	*/.addComponent(communityURI)
		/*	*/.addComponent(communityBrowse))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(issueLabel)
		/*	*/.addComponent(issueURI)
		/*	*/.addComponent(issueBrowse))
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(backupsPanel)
		/*	*/.addGroup(gl.createSequentialGroup()
		/*		*/.addComponent(dndEnable)
		/*		*/.addComponent(dockEvent)
		/*		*/.addComponent(rightOrientation)
		/*		*/.addComponent(showTreeFilter))));

		//TODO: Finish backup preferences.
		backupsPanel.setEnabled(false);
		Component[] coms = backupsPanel.getComponents();
		for (int i = 0; i < coms.length; i++) {
			coms[i].setEnabled(false);
		}

		return p;
		}

	private class ComboBoxItem {

	}

	private JPanel makeAppearancePrefs()
		{
		JPanel panel = new JPanel();

		String[] systemItems = { "default", "off", "on" };
		String[] systemItemsLocalized = {
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_DEFAULT"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_OFF"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_ON") };

		decorateWindowBordersCheckBox = new JCheckBox(
				Messages.getString("PreferencesFrame.DECORATE_WINDOW_BORDERS"));
		decorateWindowBordersCheckBox.setSelected(Prefs.decorateWindowBorders);

		JLabel themeLabel = new JLabel(Messages.getString("PreferencesFrame.THEME"));
		Vector<String> themeComboItems = new Vector<String>();
		themeComboItems.add("Swing");
		themeComboItems.add("Native");
		LookAndFeelInfo lnfs[] = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < lnfs.length; ++i)
			{
			themeComboItems.add(lnfs[i].getName());
			}
		themeComboItems.add("Custom");
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(themeComboItems);

		themeCombo = new JComboBox<String>(model);
		themeCombo.setSelectedItem(LGM.themename);
		JLabel iconLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS"));
		String[] iconItems = new String[] { "Calico", "Contrast", "Custom" };
		iconCombo = new JComboBox<String>(iconItems);
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

		JLabel antialiasLabel = new JLabel(Messages.getString("PreferencesFrame.ANTIALIASING"));
		String[] antialiasItems = { "default", "off", "on", "gasp", "lcd_hrgb", "lcd_hbgr",
				"lcd_vrgb", "lcd_vbgr" };
		String[] antialiasItemsLocalized = {
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_DEFAULT"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_OFF"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_ON"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_GASP"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_LCD_HBGR"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_LCD_HRGB"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_LCD_VBGR"),
			Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_LCD_VRGB")
		};
		antialiasCombo = new JComboBox<String>(antialiasItems);
		antialiasCombo.setSelectedItem(Prefs.antialiasControlFont);

		JLabel iconPathLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS_PATH"));
		iconPath = new JTextField(Prefs.iconPath);

		JLabel themePathLabel = new JLabel(Messages.getString("PreferencesFrame.THEME_PATH"));
		themePath = new JTextField(Prefs.swingThemePath);

		JPanel imagePreviewPanel = new JPanel();
		GroupLayout imagePreviewLayout = new GroupLayout(imagePreviewPanel);
		imagePreviewLayout.setAutoCreateGaps(true);
		imagePreviewLayout.setAutoCreateContainerGaps(true);
		imagePreviewPanel.setLayout(imagePreviewLayout);

		imagePreviewPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString(
			"PreferencesFrame.IMAGE_PREVIEW")));

		JLabel imagePreviewBackgroundLabel = new JLabel(
				Messages.getString("PreferencesFrame.IMAGE_PREVIEW_BACKGROUND_COLOR"));
		imagePreviewBackgroundColor = new ColorSelect(new Color(Prefs.imagePreviewBackgroundColor));
		JLabel imagePreviewForegroundLabel = new JLabel(
				Messages.getString("PreferencesFrame.IMAGE_PREVIEW_FOREGROUND_COLOR"));
		imagePreviewForegroundColor = new ColorSelect(new Color(Prefs.imagePreviewForegroundColor));

		imagePreviewLayout.setHorizontalGroup(imagePreviewLayout.createSequentialGroup()
		/**/.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, PREFERRED_SIZE, Short.MAX_VALUE)
		/**/.addGroup(imagePreviewLayout.createParallelGroup(Alignment.TRAILING)
		/*	*/.addComponent(imagePreviewBackgroundLabel)
		/*	*/.addComponent(imagePreviewForegroundLabel))
		/**/.addGroup(imagePreviewLayout.createParallelGroup()
		/*	*/.addComponent(imagePreviewBackgroundColor)
		/*	*/.addComponent(imagePreviewForegroundColor)));

		imagePreviewLayout.setVerticalGroup(imagePreviewLayout.createSequentialGroup()
		/**/.addGroup(imagePreviewLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(imagePreviewBackgroundLabel)
		/*	*/.addComponent(imagePreviewBackgroundColor))
		/**/.addGroup(imagePreviewLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(imagePreviewForegroundLabel)
		/*	*/.addComponent(imagePreviewForegroundColor)));

		JPanel hardwareAccelerationPanel = new JPanel();
		GroupLayout hardwareAccelerationLayout = new GroupLayout(hardwareAccelerationPanel);
		hardwareAccelerationLayout.setAutoCreateGaps(true);
		hardwareAccelerationLayout.setAutoCreateContainerGaps(true);

		hardwareAccelerationPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString(
			"PreferencesFrame.HARDWARE_ACCELERATION")));

		JLabel direct3DLabel = new JLabel(Messages.getString("PreferencesFrame.DIRECT3D"));
		direct3DCombo = new JComboBox<String>(systemItems);
		direct3DCombo.setSelectedItem(Prefs.direct3DAcceleration);
		JLabel openGLLabel = new JLabel(Messages.getString("PreferencesFrame.OPENGL"));
		openGLCombo = new JComboBox<String>(systemItems);
		openGLCombo.setSelectedItem(Prefs.openGLAcceleration);

		hardwareAccelerationLayout.setHorizontalGroup(hardwareAccelerationLayout.createParallelGroup()
		/*	*/.addGroup(hardwareAccelerationLayout.createSequentialGroup()
		/*		*/.addGroup(hardwareAccelerationLayout.createParallelGroup(Alignment.TRAILING)
		/*			*/.addComponent(direct3DLabel)
		/*			*/.addComponent(openGLLabel))
		/*		*/.addGroup(hardwareAccelerationLayout.createParallelGroup()
		/*			*/.addComponent(direct3DCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(openGLCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE))));

		hardwareAccelerationLayout.setVerticalGroup(hardwareAccelerationLayout.createSequentialGroup()
		/*	*/.addGroup(hardwareAccelerationLayout.createParallelGroup(Alignment.BASELINE)
		/*			*/.addComponent(direct3DLabel)
		/*			*/.addComponent(direct3DCombo))
		/*	*/.addGroup(hardwareAccelerationLayout.createParallelGroup(Alignment.BASELINE)
		/*			*/.addComponent(openGLLabel)
		/*			*/.addComponent(openGLCombo)));

		hardwareAccelerationPanel.setLayout(hardwareAccelerationLayout);

		JPanel searchResultsPanel = new JPanel();
		GroupLayout searchResultsLayout = new GroupLayout(searchResultsPanel);
		searchResultsLayout.setAutoCreateGaps(true);
		searchResultsLayout.setAutoCreateContainerGaps(true);
		searchResultsPanel.setLayout(searchResultsLayout);

		searchResultsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString(
			"PreferencesFrame.SEARCH_RESULTS")));

		JLabel matchCountLable = new JLabel(Messages.getString("PreferencesFrame.MATCH_COUNT"));
		matchCountBackgroundCheckBox = new JCheckBox(
				Messages.getString("PreferencesFrame.MATCH_COUNT_BACKGROUND_COLOR"),
				Prefs.highlightMatchCountBackground);
		matchCountBackgroundColor = new ColorSelect(new Color(Prefs.matchCountBackgroundColor));
		matchCountForegroundCheckBox = new JCheckBox(
				Messages.getString("PreferencesFrame.MATCH_COUNT_FOREGROUND_COLOR"),
				Prefs.highlightMatchCountForeground);
		matchCountForegroundColor = new ColorSelect(new Color(Prefs.matchCountForegroundColor));

		JLabel resultMatchLabel = new JLabel(Messages.getString("PreferencesFrame.RESULT_MATCH"));
		resultMatchBackgroundCheckBox = new JCheckBox(
				Messages.getString("PreferencesFrame.RESULT_MATCH_BACKGROUND_COLOR"),
				Prefs.highlightResultMatchBackground);
		resultMatchBackgroundColor = new ColorSelect(new Color(Prefs.resultMatchBackgroundColor));
		resultMatchForegroundCheckBox = new JCheckBox(
				Messages.getString("PreferencesFrame.RESULT_MATCH_FOREGROUND_COLOR"),
				Prefs.highlightResultMatchForeground);
		resultMatchForegroundColor = new ColorSelect(new Color(Prefs.resultMatchForegroundColor));

		searchResultsLayout.setHorizontalGroup(searchResultsLayout.createParallelGroup()
		/**/.addComponent(matchCountLable)
		/*	*/.addGroup(searchResultsLayout.createSequentialGroup()
		/*		*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.LEADING)
		/*			*/.addComponent(matchCountBackgroundCheckBox)
		/*			*/.addComponent(matchCountForegroundCheckBox))
		/*		*/.addGroup(searchResultsLayout.createParallelGroup()
		/*			*/.addComponent(matchCountBackgroundColor,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(matchCountForegroundColor,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		))
		/**/.addComponent(resultMatchLabel)
		/*	*/.addGroup(searchResultsLayout.createSequentialGroup()
		/*		*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.LEADING)
		/*			*/.addComponent(resultMatchBackgroundCheckBox)
		/*			*/.addComponent(resultMatchForegroundCheckBox))
		/*		*/.addGroup(searchResultsLayout.createParallelGroup()
		/*			*/.addComponent(resultMatchBackgroundColor,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(resultMatchForegroundColor,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		)));

		searchResultsLayout.setVerticalGroup(searchResultsLayout.createSequentialGroup()
		/**/.addComponent(matchCountLable)
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(matchCountBackgroundCheckBox)
		/*		*/.addComponent(matchCountBackgroundColor))
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(matchCountForegroundCheckBox)
		/*		*/.addComponent(matchCountForegroundColor))
		/**/.addComponent(resultMatchLabel)
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(resultMatchBackgroundCheckBox)
		/*		*/.addComponent(resultMatchBackgroundColor))
		/*	*/.addGroup(searchResultsLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(resultMatchForegroundCheckBox)
		/*		*/.addComponent(resultMatchForegroundColor)));

		GroupLayout gl = new GroupLayout(panel);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		panel.setLayout(gl);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(themeLabel)
		/*		*/.addComponent(themePathLabel)
		/*		*/.addComponent(iconPathLabel))
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(themePath)
		/*		*/.addComponent(iconPath)
		/*		*/.addGroup(gl.createSequentialGroup()
		/*			*/.addComponent(themeCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(iconLabel)
		/*			*/.addComponent(iconCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(antialiasLabel)
		/*			*/.addComponent(antialiasCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*			*/.addComponent(decorateWindowBordersCheckBox))))
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(imagePreviewPanel)
		/*		*/.addComponent(searchResultsPanel))
		/*	*/.addComponent(hardwareAccelerationPanel)));

		gl.linkSize(SwingConstants.HORIZONTAL, imagePreviewPanel, searchResultsPanel);

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(themeLabel)
		/*	*/.addComponent(themeCombo)
		/*	*/.addComponent(iconLabel)
		/*	*/.addComponent(iconCombo)
		/*	*/.addComponent(antialiasLabel)
		/*	*/.addComponent(antialiasCombo)
		/*	*/.addComponent(decorateWindowBordersCheckBox))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(themePathLabel)
		/*	*/.addComponent(themePath))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(iconPathLabel)
		/*	*/.addComponent(iconPath))
		/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(imagePreviewPanel)
		/*	*/.addComponent(hardwareAccelerationPanel))
		/**/.addComponent(searchResultsPanel));

		gl.linkSize(SwingConstants.VERTICAL, imagePreviewPanel, hardwareAccelerationPanel);

		return panel;
		}

	protected class PrefixList extends JPanel
	{
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = 5270374014574194314L;
		private Map<Class<? extends Resource<?,?>>,JTextField> prefixMap =
				new HashMap<Class<? extends Resource<?,?>>,JTextField>();

		public PrefixList()
		{
			GroupLayout gl = new GroupLayout(this);
			gl.setAutoCreateGaps(true);
			gl.setAutoCreateContainerGaps(true);

			ParallelGroup labelGroup = gl.createParallelGroup(Alignment.TRAILING);
			ParallelGroup textfieldGroup = gl.createParallelGroup();
			SequentialGroup verticalGroup = gl.createSequentialGroup();

			for (Entry<Class<? extends Resource<?,?>>,String> ent : Resource.kindNames.entrySet())
				{
				if (!InstantiableResource.class.isAssignableFrom(ent.getKey()))
					{
					continue;
					}

				JLabel label = new JLabel(Messages.format("PreferencesFrame.PREFIX_FORMAT",ent.getValue()));

				JTextField textfield = new JTextField(Prefs.prefixes.get(ent.getKey()));
				prefixMap.put(ent.getKey(),textfield);

				ParallelGroup vg = gl.createParallelGroup(Alignment.BASELINE);

				labelGroup.addComponent(label);
				textfieldGroup.addComponent(textfield);
				vg.addComponent(label);
				vg.addComponent(textfield);

				verticalGroup.addGroup(vg);
				}

			gl.setHorizontalGroup(
				gl.createSequentialGroup().addGroup(labelGroup).addGroup(textfieldGroup));
			gl.setVerticalGroup(verticalGroup);

			this.setLayout(gl);
		}

		public String getFormattedPrefixes()
		{
			String ret = "";
			for (Entry<String,Class<? extends Resource<?,?>>> ent : Resource.kindsByName3.entrySet())
				{
				if (!InstantiableResource.class.isAssignableFrom(ent.getValue()))
					{
					continue;
					}
				ret += ent.getKey() + ">" + prefixMap.get(ent.getValue()).getText() + "\t";
				}
			return ret;
		}
	}

	private PrefixList prefixList;

	private JPanel makeExtensionPrefixPrefs()
		{
		JPanel p = new JPanel();

		prefixList = new PrefixList();
		prefixList.setSize(new Dimension(100,100));

		JScrollPane prefixScroll = new JScrollPane(prefixList);

		prefixScroll.setBorder(BorderFactory.createTitledBorder(
				Messages.getString("PreferencesFrame.PREFIXES")));

		JLabel backgroundExtensionLabel = new JLabel(
			Messages.format("PreferencesFrame.EXTENSION_FORMAT",
			Resource.kindNames.get(Background.class)));
		backgroundExtension = new JTextField(Prefs.externalBackgroundExtension);
		JLabel spriteExtensionLabel = new JLabel(
			Messages.format("PreferencesFrame.EXTENSION_FORMAT",Resource.kindNames.get(Sprite.class)));
		spriteExtension = new JTextField(Prefs.externalSpriteExtension);
		JLabel scriptExtensionLabel = new JLabel(
			Messages.format("PreferencesFrame.EXTENSION_FORMAT",Resource.kindNames.get(Script.class)));
		scriptExtension = new JTextField(Prefs.externalScriptExtension);

		JPanel extensionsPanel = new JPanel();
		extensionsPanel.setBorder(BorderFactory.createTitledBorder(
			Messages.getString("PreferencesFrame.EXTENSIONS")));

		GroupLayout el = new GroupLayout(extensionsPanel);
		el.setAutoCreateGaps(true);
		el.setAutoCreateContainerGaps(true);
		extensionsPanel.setLayout(el);

		el.setHorizontalGroup(el.createSequentialGroup()
		/*	*/.addGroup(el.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(backgroundExtensionLabel)
		/*		*/.addComponent(spriteExtensionLabel)
		/*		*/.addComponent(scriptExtensionLabel))
		/*	*/.addGroup(el.createParallelGroup()
		/*		*/.addComponent(backgroundExtension)
		/*		*/.addComponent(spriteExtension)
		/*		*/.addComponent(scriptExtension)));

		el.setVerticalGroup(el.createSequentialGroup()
		/*	*/.addGroup(el.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(backgroundExtensionLabel)
		/*		*/.addComponent(backgroundExtension))
		/*	*/.addGroup(el.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(spriteExtensionLabel)
		/*		*/.addComponent(spriteExtension))
		/*	*/.addGroup(el.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(scriptExtensionLabel)
		/*		*/.addComponent(scriptExtension)));

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		p.setLayout(gl);

		gl.setHorizontalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addComponent(extensionsPanel)
		/*	*/.addComponent(prefixScroll));

		gl.setVerticalGroup(
		/**/gl.createParallelGroup()
		/*	*/.addComponent(extensionsPanel)
		/*	*/.addComponent(prefixScroll));

		return p;
		}

	private JButton makeEditorBrowseButton(final JTextField textField) {
		JButton button = new JButton(Messages.getString("PreferencesFrame.BROWSE"));
		button.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
			if (fc.showOpenDialog(PreferencesFrame.this) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file != null) {
					String commandText = '\"' + file.getAbsolutePath() + "\" %s";
					if (System.getProperty("os.name").toLowerCase().contains("windows")) {
						// NOTE: Running this through the Windows Command Interpreter enables the use of *.lnk
						// for external editors by letting the OS resolve the shortcut. - Robert
						textField.setText("cmd /c " + commandText);
					} else {
						textField.setText(commandText);
					}
				}
			}
		}
		});
		return button;
	}

	private JPanel makeExternalEditorPrefs()
		{
		JPanel p = new JPanel();

		JLabel codeEditorLabel = new JLabel(Messages.getString("PreferencesFrame.CODE_EDITOR"));
		codeEditorPath = new JTextField(Prefs.externalScriptEditorCommand);
		JButton codeEditorBrowse = makeEditorBrowseButton(codeEditorPath);

		JLabel spriteEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SPRITE_EDITOR"));
		spriteEditorPath = new JTextField(Prefs.externalSpriteEditorCommand);
		JButton spriteEditorBrowse = makeEditorBrowseButton(spriteEditorPath);

		JLabel backgroundEditorLabel = new JLabel(
				Messages.getString("PreferencesFrame.BACKGROUND_EDITOR"));
		backgroundEditorPath = new JTextField(Prefs.externalBackgroundEditorCommand);
		JButton backgroundEditorBrowse = makeEditorBrowseButton(backgroundEditorPath);

		JLabel soundEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SOUND_EDITOR"));
		soundEditorPath = new JTextField(Prefs.externalSoundEditorCommand);
		JButton soundEditorBrowse = makeEditorBrowseButton(soundEditorPath);

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		p.setLayout(gl);

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
		/*		*/.addComponent(codeEditorPath))
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(spriteEditorBrowse)
		/*		*/.addComponent(soundEditorBrowse)
		/*		*/.addComponent(backgroundEditorBrowse)
		/*		*/.addComponent(codeEditorBrowse)));

		gl.setVerticalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(spriteEditorLabel)
		/*		*/.addComponent(spriteEditorPath)
		/*		*/.addComponent(spriteEditorBrowse))
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(soundEditorLabel)
		/*		*/.addComponent(soundEditorPath)
		/*		*/.addComponent(soundEditorBrowse))
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(backgroundEditorLabel)
		/*		*/.addComponent(backgroundEditorPath)
		/*		*/.addComponent(backgroundEditorBrowse))
		/*	*/.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
		/*		*/.addComponent(codeEditorLabel)
		/*		*/.addComponent(codeEditorPath)
		/*		*/.addComponent(codeEditorBrowse)));

		return p;
		}

	// Create the room editor panel
	private Component makeRoomEditorPrefs()
		{
		JPanel roomEditorPanel = new JPanel();

		// Undo settings
		JLabel undoHistorySizeLabel = new JLabel(
				Messages.getString("PreferencesFrame.UNDO_HISTORY_SIZE"));
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

		JLabel insideColorLabel = new JLabel(Messages.getString("PreferencesFrame.INSIDE_COLOR"));
		viewInsideColor = new ColorSelect(Util.convertGmColorWithAlpha(Prefs.viewInsideColor));

		JLabel outsideColorLabel = new JLabel(Messages.getString("PreferencesFrame.OUTSIDE_COLOR"));
		viewOutsideColor = new ColorSelect(Util.convertGmColorWithAlpha(Prefs.viewOutsideColor));

		// Set the layout for the views
		viewsLayout.setHorizontalGroup(
		/**/viewsLayout.createParallelGroup()
		/*	*/.addGroup(viewsLayout.createSequentialGroup()
		/*		*/.addComponent(useFilledRectangleForViews))
		/*	*/.addGroup(viewsLayout.createSequentialGroup()
		/*		*/.addGroup(viewsLayout.createParallelGroup(Alignment.TRAILING)
		/*			*/.addComponent(useInvertedColorForViews)
		/*			*/.addComponent(insideColorLabel)
		/*			*/.addComponent(outsideColorLabel))
		/*		*/.addGroup(viewsLayout.createParallelGroup()
		/*			*/.addComponent(viewInsideColor)
		/*			*/.addComponent(viewOutsideColor))));

		viewsLayout.setVerticalGroup(
		/**/viewsLayout.createSequentialGroup()
		/*	*/.addComponent(useFilledRectangleForViews)
		/*	*/.addComponent(useInvertedColorForViews).addGap(10)
		/*	*/.addGroup(viewsLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(insideColorLabel)
		/*		*/.addComponent(viewInsideColor))
		/*	*/.addGroup(viewsLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(outsideColorLabel)
		/*		*/.addComponent(viewOutsideColor)));

		// Single selection settings
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
				Messages.getString("PreferencesFrame.INSIDE_COLOR"));
		selectionInsideColor = new ColorSelect(Util.convertGmColorWithAlpha(
			Prefs.selectionInsideColor));

		JLabel outsideColorLabelForSelection = new JLabel(
				Messages.getString("PreferencesFrame.OUTSIDE_COLOR"));
		selectionOutsideColor = new ColorSelect(
				Util.convertGmColorWithAlpha(Prefs.selectionOutsideColor));

		// Set the layout for the single selection
		selectionLayout.setHorizontalGroup(
		/**/selectionLayout.createParallelGroup()
		/*	*/.addGroup(selectionLayout.createSequentialGroup()
		/*		*/.addComponent(useFilledRectangleForSelection))
		/*	*/.addGroup(selectionLayout.createSequentialGroup()
		/*		*/.addGroup(selectionLayout.createParallelGroup(Alignment.TRAILING)
		/*			*/.addComponent(useInvertedColorForSelection)
		/*			*/.addComponent(insideColorLabelForSelection)
		/*			*/.addComponent(outsideColorLabelForSelection))
		/*		*/.addGroup(selectionLayout.createParallelGroup()
		/*			*/.addComponent(selectionInsideColor)
		/*			*/.addComponent(selectionOutsideColor))));

		selectionLayout.setVerticalGroup(
		/**/selectionLayout.createSequentialGroup()
		/*	*/.addComponent(useFilledRectangleForSelection)
		/*	*/.addComponent(useInvertedColorForSelection).addGap(10)
		/*	*/.addGroup(selectionLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(insideColorLabelForSelection)
		/*		*/.addComponent(selectionInsideColor))
		/*	*/.addGroup(selectionLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(outsideColorLabelForSelection)
		/*		*/.addComponent(selectionOutsideColor)));

		// Multiple selection settings
		JPanel multipleSelectionPanel = new JPanel();
		GroupLayout multipleSelectionLayout = new GroupLayout(multipleSelectionPanel);
		multipleSelectionLayout.setAutoCreateGaps(true);
		multipleSelectionLayout.setAutoCreateContainerGaps(true);
		multipleSelectionPanel.setLayout(multipleSelectionLayout);

		String multipleSelectionTitle = Messages.getString("PreferencesFrame.MULTIPLE_SELECTION");
		multipleSelectionPanel.setBorder(BorderFactory.createTitledBorder(multipleSelectionTitle));

		useFilledRectangleForMultipleSelection = new JCheckBox(
				Messages.getString("PreferencesFrame.FILLED_RECTANGLE"));
		useFilledRectangleForMultipleSelection.setSelected(
			Prefs.useFilledRectangleForMultipleSelection);

		useInvertedColorForMultipleSelection = new JCheckBox(
				Messages.getString("PreferencesFrame.INVERTED_COLOR"));
		useInvertedColorForMultipleSelection.setSelected(Prefs.useInvertedColorForMultipleSelection);

		JLabel insideColorLabelForMultipleSelection = new JLabel(
				Messages.getString("PreferencesFrame.INSIDE_COLOR"));
		multipleSelectionInsideColor = new ColorSelect(
				Util.convertGmColorWithAlpha(Prefs.multipleSelectionInsideColor));

		JLabel outsideColorLabelForMultipleSelection = new JLabel(
				Messages.getString("PreferencesFrame.OUTSIDE_COLOR"));
		multipleSelectionOutsideColor = new ColorSelect(
				Util.convertGmColorWithAlpha(Prefs.multipleSelectionOutsideColor));

		// Set the layout for the single selection
		multipleSelectionLayout.setHorizontalGroup(
		/**/multipleSelectionLayout.createParallelGroup()
		/*	*/.addGroup(multipleSelectionLayout.createSequentialGroup()
		/*		*/.addComponent(useFilledRectangleForMultipleSelection))
		/*	*/.addGroup(multipleSelectionLayout.createSequentialGroup()
		/*		*/.addGroup(multipleSelectionLayout.createParallelGroup(Alignment.TRAILING)
		/*			*/.addComponent(useInvertedColorForMultipleSelection)
		/*			*/.addComponent(insideColorLabelForMultipleSelection)
		/*			*/.addComponent(outsideColorLabelForMultipleSelection))
		/*		*/.addGroup(multipleSelectionLayout.createParallelGroup()
		/*			*/.addComponent(multipleSelectionInsideColor)
		/*			*/.addComponent(multipleSelectionOutsideColor))));

		multipleSelectionLayout.setVerticalGroup(
		/**/multipleSelectionLayout.createSequentialGroup()
		/*	*/.addComponent(useFilledRectangleForMultipleSelection)
		/*	*/.addComponent(useInvertedColorForMultipleSelection).addGap(10)
		/*	*/.addGroup(multipleSelectionLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(insideColorLabelForMultipleSelection)
		/*		*/.addComponent(multipleSelectionInsideColor))
		/*	*/.addGroup(multipleSelectionLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(outsideColorLabelForMultipleSelection)
		/*		*/.addComponent(multipleSelectionOutsideColor)));

		// Set the layout for the main panel
		GroupLayout gl = new GroupLayout(roomEditorPanel);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		roomEditorPanel.setLayout(gl);

		gl.setHorizontalGroup(
		/**/gl.createParallelGroup()
		/*	*/.addGroup(gl.createSequentialGroup()
		/*		*/.addComponent(undoHistorySizeLabel)
		/*		*/.addComponent(undoHistorySize,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/*	*/.addGroup(gl.createSequentialGroup()
		/*		*/.addComponent(selectionPanel,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/*		*/.addComponent(multipleSelectionPanel,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/*	*/.addComponent(viewsPanel,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE));

		gl.setVerticalGroup(
		/**/gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(undoHistorySizeLabel)
		/*		*/.addComponent(undoHistorySize))
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(selectionPanel)
		/*		*/.addComponent(multipleSelectionPanel))
		/*	*/.addComponent(viewsPanel));

		return roomEditorPanel;
		}

	public PreferencesFrame()
		{
		super(LGM.frame);
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		setTitle(Messages.getString("PreferencesFrame.TITLE"));
		setIconImage(LGM.getIconForKey("Toolbar.PREFERENCES").getImage());
		setResizable(true);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");

		cardPane = new JPanel(new CardLayout());

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(
				Messages.getString("PreferencesFrame.TAB_GENERAL"));
		root.add(node);
		cardPane.add(makeGeneralPrefs(),Messages.getString("PreferencesFrame.TAB_GENERAL"));

		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_APPEARANCE"));
		root.add(node);
		cardPane.add(makeAppearancePrefs(),Messages.getString("PreferencesFrame.TAB_APPEARANCE"));

		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_EXTERNAL_EDITOR"));
		root.add(node);
		cardPane.add(makeExternalEditorPrefs(),
				Messages.getString("PreferencesFrame.TAB_EXTERNAL_EDITOR"));

		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_MEDIA_PREFIX"));
		root.add(node);
		cardPane.add(makeExtensionPrefixPrefs(),Messages.getString(
			"PreferencesFrame.TAB_MEDIA_PREFIX"));

		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_CODE_EDITOR"));
		//TODO: Fix UI bugs in JoshEdit repo and then use the serialize feature to save them.
		//root.add(node);
		DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(
				Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_KEYBINDINGS"));
		node.add(cnode);
		cnode = new DefaultMutableTreeNode(
				Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_SYNTAX_HIGHLIGHTING"));
		node.add(cnode);
		cardPane.add(new org.lateralgm.joshedit.preferences.KeybindingsPanel(),
				Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_KEYBINDINGS"));
		cardPane.add(
			new org.lateralgm.joshedit.preferences.HighlightPreferences(
					new TokenMarker.LanguageDescription[][] { GMLTokenMarker.getLanguageDescriptions(),
							GLSLTokenMarker.getLanguageDescriptions(),
							GLESTokenMarker.getLanguageDescriptions(),HLSLTokenMarker.getLanguageDescriptions() },
					Preferences.userRoot().node("org/lateralgm/joshedit")),
			Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_SYNTAX_HIGHLIGHTING"));

		node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_ROOM_EDITOR"));
		root.add(node);
		cardPane.add(makeRoomEditorPrefs(),Messages.getString("PreferencesFrame.TAB_ROOM_EDITOR"));

		tree = new JTree(new DefaultTreeModel(root));
		tree.setEditable(false);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Simplest way to stop updateUI/setUI calls for changing the look and feel from reverting the
		// tree icons.
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		tree.setCellRenderer(renderer);

		// reload after adding all root children to make sure its children are visible
		((DefaultTreeModel) tree.getModel()).reload();

		tree.addTreeSelectionListener(new TreeSelectionListener()
			{
				public void valueChanged(TreeSelectionEvent e)
					{
					DefaultMutableTreeNode node =
						(DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

					/* if nothing is selected */
					if (node == null) return;

					/* retrieve the node that was selected */
					String nodeInfo = node.getUserObject().toString();

					CardLayout cl = (CardLayout) (cardPane.getLayout());
					cl.show(cardPane,nodeInfo);
					}
			});

		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(160, 0));
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scroll, cardPane);
		add(split);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.TRAILING));

		String key;

		key = "PreferencesFrame.APPLY_CHANGES";
		JButton applyBut = new JButton(Messages.getString(key));
		applyBut.addActionListener(this);
		applyBut.setActionCommand(key);

		key = "PreferencesFrame.RESET_DEFAULTS";
		JButton resetDefaultsBut = new JButton(Messages.getString(key));
		resetDefaultsBut.addActionListener(this);
		resetDefaultsBut.setActionCommand(key);

		key = "PreferencesFrame.CLOSE";
		JButton closeBut = new JButton(Messages.getString(key));
		closeBut.addActionListener(this);
		closeBut.setActionCommand(key);

		applyChangesLabel = new JLabel(Messages.getString(
			"PreferencesFrame.APPLY_NOTICE"));
		applyChangesLabel.setIcon(LGM.getIconForKey("PreferencesFrame.APPLY_NOTICE"));
		applyChangesLabel.setVisible(false);

		p.add(applyChangesLabel);
		p.add(applyBut);
		p.add(resetDefaultsBut);
		p.add(closeBut);

		add(p,BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(LGM.frame);
		}

	public void savePreferences()
		{
		LGM.iconspack = (String) iconCombo.getSelectedItem();
		PrefsStore.setLocale((Locale) localeCombo.getSelectedItem());
		PrefsStore.setIconPack(LGM.iconspack);
		PrefsStore.setIconPath(iconPath.getText());
		PrefsStore.setSwingThemePath(themePath.getText());
		PrefsStore.setSwingTheme((String) themeCombo.getSelectedItem());
		PrefsStore.setDocumentationURI(documentationURI.getText());
		PrefsStore.setWebsiteURI(websiteURI.getText());
		PrefsStore.setCommunityURI(communityURI.getText());
		PrefsStore.setIssueURI(issueURI.getText());
		PrefsStore.setDNDEnabled(dndEnable.isSelected());
		PrefsStore.setExtraNodes(extraNodesEnable.isSelected());
		PrefsStore.setShowTreeFilter(showTreeFilter.isSelected());
		PrefsStore.setRightOrientation(rightOrientation.isSelected());
		PrefsStore.setUserLibraryPath(actionsPath.getText());
		PrefsStore.setSpriteExt(spriteExtension.getText());
		PrefsStore.setBackgroundExt(backgroundExtension.getText());
		PrefsStore.setScriptExt(scriptExtension.getText());
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
		PrefsStore.setSelectionInsideColor(Util.getGmColorWithAlpha(
			selectionInsideColor.getSelectedColor()));
		PrefsStore.setSelectionOutsideColor(Util.getGmColorWithAlpha(
			selectionOutsideColor.getSelectedColor()));
		PrefsStore.setFilledRectangleForMultipleSelection(
			useFilledRectangleForMultipleSelection.isSelected());
		PrefsStore.setInvertedColorForMultipleSelection(
			useInvertedColorForMultipleSelection.isSelected());
		PrefsStore.setMultipleSelectionInsideColor(Util.getGmColorWithAlpha(
			multipleSelectionInsideColor.getSelectedColor()));
		PrefsStore.setMultipleSelectionOutsideColor(Util.getGmColorWithAlpha(
			multipleSelectionOutsideColor.getSelectedColor()));

		PrefsStore.setPrefixes(prefixList.getFormattedPrefixes());

		PrefsStore.setDecorateWindowBorders(decorateWindowBordersCheckBox.isSelected());
		PrefsStore.setAntialiasControlFont(antialiasCombo.getSelectedItem().toString());
		PrefsStore.setDirect3DAcceleration(direct3DCombo.getSelectedItem().toString());
		PrefsStore.setOpenGLAcceleration(openGLCombo.getSelectedItem().toString());
		PrefsStore.setImagePreviewBackgroundColor(
			imagePreviewBackgroundColor.getSelectedColor().getRGB());
		PrefsStore.setImagePreviewForegroundColor(
			imagePreviewForegroundColor.getSelectedColor().getRGB());
		PrefsStore.setHighlightMatchCountBackground(matchCountBackgroundCheckBox.isSelected());
		PrefsStore.setHighlightMatchCountForeground(matchCountForegroundCheckBox.isSelected());
		PrefsStore.setMatchCountBackgroundColor(matchCountBackgroundColor.getSelectedColor().getRGB());
		PrefsStore.setMatchCountForegroundColor(matchCountForegroundColor.getSelectedColor().getRGB());
		PrefsStore.setHighlightResultMatchBackground(resultMatchBackgroundCheckBox.isSelected());
		PrefsStore.setHighlightResultMatchForeground(resultMatchForegroundCheckBox.isSelected());
		PrefsStore.setResultMatchBackgroundColor(
			resultMatchBackgroundColor.getSelectedColor().getRGB());
		PrefsStore.setResultMatchForegroundColor(
			resultMatchForegroundColor.getSelectedColor().getRGB());
		}

	private Timer blinkTimer;

	public void actionPerformed(ActionEvent ev)
		{
		String com = ev.getActionCommand();
		if (com.equals("PreferencesFrame.APPLY_CHANGES")) //$NON-NLS-1$
			{
			LGM.filterPanel.setVisible(showTreeFilter.isSelected());

			savePreferences();
			LGM.setLookAndFeel((String) themeCombo.getSelectedItem());
			LGM.updateLookAndFeel();
			// must be called after updating the look and feel so that laf can be asked if window borders
			// should be decorated
			LGM.applyPreferences();
			// refocus the window in case a LAF change occurred
			this.requestFocus();

			// show the restart notice once we've applied the changes to indicate they have been saved
			applyChangesLabel.setVisible(true);
			if (blinkTimer == null) {
				blinkTimer = new Timer(300, new ActionListener() {
					int count = 0;
					Icon icon = applyChangesLabel.getIcon();
					@Override
					public void actionPerformed(ActionEvent e)
						{
							if (count % 2 > 0) {
								applyChangesLabel.setIcon(icon);
							} else {
								applyChangesLabel.setIcon(null);
							}
							count++;
							if (count > 7) {
								count = 0;
								blinkTimer.stop();
							}
						}
				});
			}
			blinkTimer.start();
			}
		else if (com.equals("PreferencesFrame.RESET_DEFAULTS")) //$NON-NLS-1$
			{
			PrefsStore.resetToDefaults();
			}
		else if (com.equals("PreferencesFrame.CLOSE")) //$NON-NLS-1$
			{
			this.setVisible(false);
			}
		}
	}
