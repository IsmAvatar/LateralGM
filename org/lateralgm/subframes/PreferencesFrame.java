/**
* @file  PreferencesFrame.java
* @brief Class implementing a frame for the user to edit the Java preferences for the application
* including styles and the look and feel.
*
* @section License
*
* Copyright (C) 2013-2015,2019 Robert B. Colton
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
	public static PreferencesFrame instance = null;
	protected JPanel cardPane;
	protected JTree tree;
	protected JLabel applyChangesLabel;

	public PreferencesFrame()
		{
		super(LGM.frame);
		instance = this;
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		setTitle(Messages.getString("PreferencesFrame.TITLE")); //$NON-NLS-1$
		setIconImage(LGM.getIconForKey("Toolbar.PREFERENCES").getImage()); //$NON-NLS-1$
		setResizable(true);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		cardPane = new JPanel(new CardLayout());

		for (PreferencesGroup group : groups)
			{
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(group.name);
			root.add(node);
			cardPane.add(group.makePanel(), group.name);
			}

		//TODO: Fix UI bugs in JoshEdit repo and then use the serialize feature to save them.
		//root.add(node);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(Messages.getString("PreferencesFrame.TAB_CODE_EDITOR")); //$NON-NLS-1$
		DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(
				Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_KEYBINDINGS")); //$NON-NLS-1$
		node.add(cnode);
		cnode = new DefaultMutableTreeNode(
				Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_SYNTAX_HIGHLIGHTING")); //$NON-NLS-1$
		node.add(cnode);
		cardPane.add(new org.lateralgm.joshedit.preferences.KeybindingsPanel(),
				Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_KEYBINDINGS")); //$NON-NLS-1$
		cardPane.add(
			new org.lateralgm.joshedit.preferences.HighlightPreferences(
					new TokenMarker.LanguageDescription[][] { GMLTokenMarker.getLanguageDescriptions(),
							GLSLTokenMarker.getLanguageDescriptions(),
							GLESTokenMarker.getLanguageDescriptions(),HLSLTokenMarker.getLanguageDescriptions() },
					Preferences.userRoot().node("org/lateralgm/joshedit")), //$NON-NLS-1$
			Messages.getString("PreferencesFrame.TAB_CODE_EDITOR_SYNTAX_HIGHLIGHTING")); //$NON-NLS-1$

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

		key = "PreferencesFrame.APPLY_CHANGES"; //$NON-NLS-1$
		JButton applyBut = new JButton(Messages.getString(key));
		applyBut.addActionListener(this);
		applyBut.setActionCommand(key);

		key = "PreferencesFrame.RESET_DEFAULTS"; //$NON-NLS-1$
		JButton resetDefaultsBut = new JButton(Messages.getString(key));
		resetDefaultsBut.addActionListener(this);
		resetDefaultsBut.setActionCommand(key);

		key = "PreferencesFrame.CLOSE"; //$NON-NLS-1$
		JButton closeBut = new JButton(Messages.getString(key));
		closeBut.addActionListener(this);
		closeBut.setActionCommand(key);

		applyChangesLabel = new JLabel(Messages.getString(
			"PreferencesFrame.APPLY_NOTICE")); //$NON-NLS-1$
		applyChangesLabel.setIcon(LGM.getIconForKey("PreferencesFrame.APPLY_NOTICE")); //$NON-NLS-1$
		applyChangesLabel.setVisible(false);

		p.add(applyChangesLabel);
		p.add(applyBut);
		p.add(resetDefaultsBut);
		p.add(closeBut);

		add(p,BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(LGM.frame);
		}

	private void savePreferences()
		{
		for (PreferencesGroup group : groups)
			{
			group.save();
			}
		}

	private void resetDefaults()
		{
		PrefsStore.resetToDefaults();
		Prefs.loadPrefs();
		for (PreferencesGroup group : groups)
			{
			group.load();
			}
		}

	@Override
	public void setVisible(boolean visible)
		{
		if (visible)
			for (PreferencesGroup group : groups)
				group.load();

		super.setVisible(visible);
		}

	private Timer blinkTimer;

	private void startBlink()
		{
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

	public void actionPerformed(ActionEvent ev)
		{
		String com = ev.getActionCommand();
		if (com.equals("PreferencesFrame.APPLY_CHANGES")) //$NON-NLS-1$
			{
			this.savePreferences();
			this.startBlink();
			}
		else if (com.equals("PreferencesFrame.RESET_DEFAULTS")) //$NON-NLS-1$
			{
			this.resetDefaults();
			this.startBlink();
			}
		else if (com.equals("PreferencesFrame.CLOSE")) //$NON-NLS-1$
			{
			this.setVisible(false);
			}
		}

	public static abstract class PreferencesGroup
		{
		public final String name;

		protected PreferencesGroup(String name)
			{
			this.name = name;
			}

		public abstract JPanel makePanel();
		public abstract void load();
		public abstract void save();
		}

	public static final List<PreferencesGroup> groups = new ArrayList<>();
	static
		{
		groups.add(new GeneralGroup());
		groups.add(new AppearanceGroup());
		groups.add(new ExternalGroup());
		groups.add(new MediaGroup());
		groups.add(new RoomEditorGroup());
		}

	private static class GeneralGroup extends PreferencesGroup
		{
		JCheckBox dndEnable, expandEventsEnable, restrictTreeEnable, extraNodesEnable, showTreeFilter,
			rightOrientation;
		JComboBox<Locale> localeCombo;
		JComboBox<String> actionsCombo;
		JTextField documentationURI, websiteURI, communityURI, issueURI, actionsPath;

		protected GeneralGroup()
			{
			super(Messages.getString("PreferencesFrame.TAB_GENERAL")); //$NON-NLS-1$
			}

		private JButton getURIBrowseButton(final JTextField textField)
			{
			JButton button = new JButton(Messages.getString("PreferencesFrame.BROWSE")); //$NON-NLS-1$
			button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (fc.showOpenDialog(PreferencesFrame.instance) != JFileChooser.APPROVE_OPTION) return;
				File file = fc.getSelectedFile();
				if (file != null) {
					textField.setText(file.toURI().toString());
				}
			}
			});
			return button;
			}

		@Override
		public JPanel makePanel()
			{
			JPanel p = new JPanel();

			dndEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_DND")); //$NON-NLS-1$
			expandEventsEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_EXPAND_EVENTS")); //$NON-NLS-1$
			restrictTreeEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_TREE_RESTRICT")); //$NON-NLS-1$
			extraNodesEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_EXTRA_NODES")); //$NON-NLS-1$
			showTreeFilter = new JCheckBox(Messages.getString("PreferencesFrame.SHOW_TREE_FILTER")); //$NON-NLS-1$
			rightOrientation = new JCheckBox(Messages.getString("PreferencesFrame.RIGHT_ORIENTATION")); //$NON-NLS-1$

			JLabel documentationLabel = new JLabel(
				Messages.getString("PreferencesFrame.DOCUMENTATION_URI")); //$NON-NLS-1$
			documentationURI = new JTextField();
			JButton documentationBrowse = getURIBrowseButton(documentationURI);
			JLabel websiteLabel = new JLabel(
				Messages.getString("PreferencesFrame.WEBSITE_URI")); //$NON-NLS-1$
			websiteURI = new JTextField();
			JButton websiteBrowse = getURIBrowseButton(websiteURI);
			JLabel communityLabel = new JLabel(
				Messages.getString("PreferencesFrame.COMMUNITY_URI")); //$NON-NLS-1$
			communityURI = new JTextField();
			JButton communityBrowse = getURIBrowseButton(communityURI);
			JLabel issueLabel = new JLabel(
				Messages.getString("PreferencesFrame.ISSUE_URI")); //$NON-NLS-1$
			issueURI = new JTextField();
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

			JLabel localeLabel = new JLabel(Messages.getString("PreferencesFrame.LOCALE")); //$NON-NLS-1$
			JLabel localeWarningLabel = new JLabel(Messages.getString("PreferencesFrame.LOCALE_WARNING")); //$NON-NLS-1$

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
			/*		*/.addComponent(expandEventsEnable)
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
			/*		*/.addComponent(expandEventsEnable)
			/*		*/.addComponent(rightOrientation)
			/*		*/.addComponent(showTreeFilter))));

			//TODO: Finish backup preferences.
			Util.setComponentTreeEnabled(backupsPanel,false);

			return p;
			}

		@Override
		public void load()
			{
			localeCombo.setSelectedItem(Prefs.locale);
			dndEnable.setSelected(Prefs.enableDragAndDrop);
			expandEventsEnable.setSelected(Prefs.expandEventTree);
			restrictTreeEnable.setSelected(Prefs.restrictHierarchy);
			extraNodesEnable.setSelected(Prefs.extraNodes);
			showTreeFilter.setSelected(Prefs.showTreeFilter);
			rightOrientation.setSelected(Prefs.rightOrientation);

			documentationURI.setText(Prefs.documentationURI);
			websiteURI.setText(Prefs.websiteURI);
			communityURI.setText(Prefs.communityURI);
			issueURI.setText(Prefs.issueURI);
			}

		@Override
		public void save()
			{
			LGM.filterPanel.setVisible(showTreeFilter.isSelected());
			PrefsStore.setLocale((Locale) localeCombo.getSelectedItem());
			PrefsStore.setIconPack(LGM.iconspack);
			PrefsStore.setDocumentationURI(documentationURI.getText());
			PrefsStore.setWebsiteURI(websiteURI.getText());
			PrefsStore.setCommunityURI(communityURI.getText());
			PrefsStore.setIssueURI(issueURI.getText());
			PrefsStore.setDNDEnabled(dndEnable.isSelected());
			PrefsStore.setExpandEventTree(expandEventsEnable.isSelected());
			PrefsStore.setExtraNodes(extraNodesEnable.isSelected());
			PrefsStore.setShowTreeFilter(showTreeFilter.isSelected());
			PrefsStore.setRightOrientation(rightOrientation.isSelected());
			PrefsStore.setUserLibraryPath(actionsPath.getText());
			}
		}

	private static class AppearanceGroup extends PreferencesGroup
		{
		JComboBox<String> themeCombo, iconCombo;
		JTextField iconPath, themePath;
		private ColorSelect imagePreviewBackgroundColor, imagePreviewForegroundColor,
			matchCountBackgroundColor, matchCountForegroundColor, resultMatchBackgroundColor,
			resultMatchForegroundColor;
		private JCheckBox matchCountBackgroundCheckBox,matchCountForegroundCheckBox,
			resultMatchBackgroundCheckBox, resultMatchForegroundCheckBox;
		private JComboBox<String> direct3DCombo, openGLCombo, antialiasCombo;
		private JCheckBox decorateWindowBordersCheckBox;

		protected AppearanceGroup()
			{
			super(Messages.getString("PreferencesFrame.TAB_APPEARANCE")); //$NON-NLS-1$
			}

		@Override
		public JPanel makePanel()
			{
			JPanel panel = new JPanel();

			String[] systemItems = { "default", "off", "on" };
			String[] systemItemsLocalized = {
				Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_DEFAULT"),
				Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_OFF"),
				Messages.getString("PreferencesFrame.SYSTEM_PROPERTY_ON") };

			decorateWindowBordersCheckBox = new JCheckBox(
					Messages.getString("PreferencesFrame.DECORATE_WINDOW_BORDERS")); //$NON-NLS-1$

			JLabel themeLabel = new JLabel(Messages.getString("PreferencesFrame.THEME")); //$NON-NLS-1$
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
			JLabel iconLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS")); //$NON-NLS-1$
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

			JLabel iconPathLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS_PATH")); //$NON-NLS-1$
			iconPath = new JTextField();

			JLabel themePathLabel = new JLabel(Messages.getString("PreferencesFrame.THEME_PATH")); //$NON-NLS-1$
			themePath = new JTextField();

			JPanel imagePreviewPanel = new JPanel();
			GroupLayout imagePreviewLayout = new GroupLayout(imagePreviewPanel);
			imagePreviewLayout.setAutoCreateGaps(true);
			imagePreviewLayout.setAutoCreateContainerGaps(true);
			imagePreviewPanel.setLayout(imagePreviewLayout);

			imagePreviewPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString(
				"PreferencesFrame.IMAGE_PREVIEW"))); //$NON-NLS-1$

			JLabel imagePreviewBackgroundLabel = new JLabel(
					Messages.getString("PreferencesFrame.IMAGE_PREVIEW_BACKGROUND_COLOR")); //$NON-NLS-1$
			imagePreviewBackgroundColor = new ColorSelect();
			JLabel imagePreviewForegroundLabel = new JLabel(
					Messages.getString("PreferencesFrame.IMAGE_PREVIEW_FOREGROUND_COLOR")); //$NON-NLS-1$
			imagePreviewForegroundColor = new ColorSelect();

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
				"PreferencesFrame.HARDWARE_ACCELERATION"))); //$NON-NLS-1$

			JLabel direct3DLabel = new JLabel(Messages.getString("PreferencesFrame.DIRECT3D")); //$NON-NLS-1$
			direct3DCombo = new JComboBox<String>(systemItems);
			JLabel openGLLabel = new JLabel(Messages.getString("PreferencesFrame.OPENGL")); //$NON-NLS-1$
			openGLCombo = new JComboBox<String>(systemItems);

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
				"PreferencesFrame.SEARCH_RESULTS"))); //$NON-NLS-1$

			JLabel matchCountLable = new JLabel(Messages.getString("PreferencesFrame.MATCH_COUNT")); //$NON-NLS-1$
			matchCountBackgroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.MATCH_COUNT_BACKGROUND_COLOR")); //$NON-NLS-1$
			matchCountBackgroundColor = new ColorSelect();
			matchCountForegroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.MATCH_COUNT_FOREGROUND_COLOR")); //$NON-NLS-1$
			matchCountForegroundColor = new ColorSelect();

			JLabel resultMatchLabel = new JLabel(Messages.getString("PreferencesFrame.RESULT_MATCH")); //$NON-NLS-1$
			resultMatchBackgroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.RESULT_MATCH_BACKGROUND_COLOR")); //$NON-NLS-1$
			resultMatchBackgroundColor = new ColorSelect();
			resultMatchForegroundCheckBox = new JCheckBox(Messages.getString("PreferencesFrame.RESULT_MATCH_FOREGROUND_COLOR")); //$NON-NLS-1$
			resultMatchForegroundColor = new ColorSelect();

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
			/*	*/.addComponent(themeCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
			/*	*/.addComponent(iconLabel)
			/*	*/.addComponent(iconCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
			/*	*/.addComponent(antialiasLabel)
			/*	*/.addComponent(antialiasCombo,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
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

		@Override
		public void load()
			{
			iconPath.setText(Prefs.iconPath);
			themePath.setText(Prefs.swingThemePath);
			themeCombo.setSelectedItem(LGM.themename);
			iconCombo.setSelectedItem(LGM.iconspack);
			antialiasCombo.setSelectedItem(Prefs.antialiasControlFont);
			imagePreviewBackgroundColor.setSelectedColor(new Color(Prefs.imagePreviewBackgroundColor));
			imagePreviewForegroundColor.setSelectedColor(new Color(Prefs.imagePreviewForegroundColor));
			direct3DCombo.setSelectedItem(Prefs.direct3DAcceleration);
			openGLCombo.setSelectedItem(Prefs.openGLAcceleration);
			decorateWindowBordersCheckBox.setSelected(Prefs.decorateWindowBorders);
			matchCountBackgroundCheckBox.setSelected(Prefs.highlightMatchCountBackground);
			matchCountForegroundCheckBox.setSelected(Prefs.highlightMatchCountForeground);
			resultMatchBackgroundCheckBox.setSelected(Prefs.highlightResultMatchBackground);
			resultMatchForegroundCheckBox.setSelected(Prefs.highlightResultMatchForeground);
			matchCountBackgroundColor.setSelectedColor(new Color(Prefs.matchCountBackgroundColor));
			matchCountForegroundColor.setSelectedColor(new Color(Prefs.matchCountForegroundColor));
			resultMatchBackgroundColor.setSelectedColor(new Color(Prefs.resultMatchBackgroundColor));
			resultMatchForegroundColor.setSelectedColor(new Color(Prefs.resultMatchForegroundColor));
			}

		@Override
		public void save()
			{
			LGM.iconspack = (String) iconCombo.getSelectedItem();
			PrefsStore.setIconPack(LGM.iconspack);
			PrefsStore.setIconPath(iconPath.getText());
			PrefsStore.setSwingThemePath(themePath.getText());
			PrefsStore.setSwingTheme((String) themeCombo.getSelectedItem());
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

			LGM.setLookAndFeel((String) themeCombo.getSelectedItem());
			LGM.updateLookAndFeel();
			LGM.reloadIcons();
			// must be called after updating the look and feel so that laf can be asked if window borders
			// should be decorated
			LGM.applyPreferences();
			// refocus the window in case a LAF change occurred
			PreferencesFrame.instance.requestFocus();
			}
		}

	private static class ExternalGroup extends PreferencesGroup
		{
		JTextField soundEditorPath, backgroundEditorPath, spriteEditorPath, codeEditorPath;

		protected ExternalGroup()
			{
			super(Messages.getString("PreferencesFrame.TAB_EXTERNAL_EDITOR")); //$NON-NLS-1$
			}

		private JButton makeEditorBrowseButton(final JTextField textField)
			{
			JButton button = new JButton(Messages.getString("PreferencesFrame.BROWSE")); //$NON-NLS-1$
			button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				if (fc.showOpenDialog(PreferencesFrame.instance) != JFileChooser.APPROVE_OPTION) return;
				File file = fc.getSelectedFile();
				if (file != null) {
					String commandText = '\"' + file.getAbsolutePath() + "\" %s"; //$NON-NLS-1$
					if (System.getProperty("os.name").equalsIgnoreCase("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
						// NOTE: Running this through the Windows Command Interpreter enables the use of *.lnk
						// for external editors by letting the OS resolve the shortcut. - Robert
						textField.setText("cmd /c " + commandText); //$NON-NLS-1$
					} else {
						textField.setText(commandText);
					}
				}
			}
			});
			return button;
			}

		@Override
		public JPanel makePanel()
			{
			JPanel p = new JPanel();

			JLabel codeEditorLabel = new JLabel(Messages.getString("PreferencesFrame.CODE_EDITOR")); //$NON-NLS-1$
			codeEditorPath = new JTextField();
			JButton codeEditorBrowse = makeEditorBrowseButton(codeEditorPath);

			JLabel spriteEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SPRITE_EDITOR")); //$NON-NLS-1$
			spriteEditorPath = new JTextField();
			JButton spriteEditorBrowse = makeEditorBrowseButton(spriteEditorPath);

			JLabel backgroundEditorLabel = new JLabel(
					Messages.getString("PreferencesFrame.BACKGROUND_EDITOR")); //$NON-NLS-1$
			backgroundEditorPath = new JTextField();
			JButton backgroundEditorBrowse = makeEditorBrowseButton(backgroundEditorPath);

			JLabel soundEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SOUND_EDITOR")); //$NON-NLS-1$
			soundEditorPath = new JTextField();
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

		@Override
		public void load()
			{
			codeEditorPath.setText(Prefs.externalScriptEditorCommand);
			spriteEditorPath.setText(Prefs.externalSpriteEditorCommand);
			backgroundEditorPath.setText(Prefs.externalBackgroundEditorCommand);
			soundEditorPath.setText(Prefs.externalSoundEditorCommand);
			}

		@Override
		public void save()
			{
			PrefsStore.setBackgroundEditorCommand(backgroundEditorPath.getText());
			PrefsStore.setSpriteEditorCommand(spriteEditorPath.getText());
			PrefsStore.setSoundEditorCommand(soundEditorPath.getText());
			PrefsStore.setScriptEditorCommand(codeEditorPath.getText());
			}
		}

	private static class MediaGroup extends PreferencesGroup
		{
		// Sounds use their own stored filename/extension, which may vary from sound to sound.
		JTextField backgroundExtension, spriteExtension, scriptExtension;

		protected class PrefixList extends JPanel
			{
			/**
			 * NOTE: Default UID generated, change if necessary.
			 */
			private static final long serialVersionUID = 5270374014574194314L;
			private Map<Class<? extends Resource<?,?>>,JTextField> prefixMap = new HashMap<>();

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
							continue;

						JLabel label = new JLabel(Messages.format("PreferencesFrame.PREFIX_FORMAT",ent.getValue())); //$NON-NLS-1$

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

			public void load()
				{
				for (Entry<Class<? extends Resource<?,?>>,JTextField> entry : prefixMap.entrySet())
					entry.getValue().setText(Prefs.prefixes.get(entry.getKey()));
				}

			public String getSerializedPrefixes()
				{
					String ret = ""; //$NON-NLS-1$
					for (Entry<String,Class<? extends Resource<?,?>>> ent : Resource.kindsByName3.entrySet())
						{
						if (!InstantiableResource.class.isAssignableFrom(ent.getValue()))
							continue;

						ret += ent.getKey() + ">" + prefixMap.get(ent.getValue()).getText() + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
						}
					return ret;
				}
			}

		private PrefixList prefixList;

		protected MediaGroup()
			{
			super(Messages.getString("PreferencesFrame.TAB_MEDIA_PREFIX")); //$NON-NLS-1$
			}

		@Override
		public JPanel makePanel()
			{
			JPanel p = new JPanel();

			prefixList = new PrefixList();
			prefixList.setSize(new Dimension(100,100));

			JScrollPane prefixScroll = new JScrollPane(prefixList);

			prefixScroll.setBorder(BorderFactory.createTitledBorder(
					Messages.getString("PreferencesFrame.PREFIXES"))); //$NON-NLS-1$

			JLabel backgroundExtensionLabel = new JLabel(
				Messages.format("PreferencesFrame.EXTENSION_FORMAT", //$NON-NLS-1$
				Resource.kindNames.get(Background.class)));
			backgroundExtension = new JTextField();
			JLabel spriteExtensionLabel = new JLabel(
				Messages.format("PreferencesFrame.EXTENSION_FORMAT",Resource.kindNames.get(Sprite.class))); //$NON-NLS-1$
			spriteExtension = new JTextField();
			JLabel scriptExtensionLabel = new JLabel(
				Messages.format("PreferencesFrame.EXTENSION_FORMAT",Resource.kindNames.get(Script.class))); //$NON-NLS-1$
			scriptExtension = new JTextField();

			JPanel extensionsPanel = new JPanel();
			extensionsPanel.setBorder(BorderFactory.createTitledBorder(
				Messages.getString("PreferencesFrame.EXTENSIONS"))); //$NON-NLS-1$

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
			/*	*/.addComponent(prefixScroll)
			/*	*/.addComponent(extensionsPanel));

			gl.setVerticalGroup(
			/**/gl.createParallelGroup()
			/*	*/.addComponent(prefixScroll, DEFAULT_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			/*	*/.addComponent(extensionsPanel));

			return p;
			}

		@Override
		public void load()
			{
			prefixList.load();
			backgroundExtension.setText(Prefs.externalBackgroundExtension);
			spriteExtension.setText(Prefs.externalSpriteExtension);
			scriptExtension.setText(Prefs.externalScriptExtension);
			}

		@Override
		public void save()
			{
			PrefsStore.setPrefixes(prefixList.getSerializedPrefixes());
			PrefsStore.setSpriteExt(spriteExtension.getText());
			PrefsStore.setBackgroundExt(backgroundExtension.getText());
			PrefsStore.setScriptExt(scriptExtension.getText());
			}
		}

	private static class RoomEditorGroup extends PreferencesGroup
		{
		NumberField undoHistorySize;
		JCheckBox useFilledRectangleForViews, useInvertedColorForViews, useFilledRectangleForSelection,
			useInvertedColorForSelection, useFilledRectangleForMultipleSelection,
			useInvertedColorForMultipleSelection;
		ColorSelect viewInsideColor, viewOutsideColor, selectionInsideColor, selectionOutsideColor,
			multipleSelectionInsideColor, multipleSelectionOutsideColor;

		protected RoomEditorGroup()
			{
			super(Messages.getString("PreferencesFrame.TAB_ROOM_EDITOR")); //$NON-NLS-1$
			}

		@Override
		public JPanel makePanel()
			{
			JPanel roomEditorPanel = new JPanel();

			// Undo settings
			JLabel undoHistorySizeLabel = new JLabel(
					Messages.getString("PreferencesFrame.UNDO_HISTORY_SIZE")); //$NON-NLS-1$
			undoHistorySize = new NumberField(-1,999999);

			// Views settings
			JPanel viewsPanel = new JPanel();
			GroupLayout viewsLayout = new GroupLayout(viewsPanel);
			viewsLayout.setAutoCreateGaps(true);
			viewsLayout.setAutoCreateContainerGaps(true);
			viewsPanel.setLayout(viewsLayout);

			String title = Messages.getString("PreferencesFrame.VIEWS_TITLE"); //$NON-NLS-1$
			viewsPanel.setBorder(BorderFactory.createTitledBorder(title));

			useFilledRectangleForViews = new JCheckBox(
					Messages.getString("PreferencesFrame.FILLED_RECTANGLE")); //$NON-NLS-1$

			useInvertedColorForViews = new JCheckBox(Messages.getString("PreferencesFrame.INVERTED_COLOR")); //$NON-NLS-1$

			JLabel insideColorLabel = new JLabel(Messages.getString("PreferencesFrame.INSIDE_COLOR")); //$NON-NLS-1$
			viewInsideColor = new ColorSelect();
			JLabel outsideColorLabel = new JLabel(Messages.getString("PreferencesFrame.OUTSIDE_COLOR")); //$NON-NLS-1$
			viewOutsideColor = new ColorSelect();

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

			String selectionTitle = Messages.getString("PreferencesFrame.SELECTION"); //$NON-NLS-1$
			selectionPanel.setBorder(BorderFactory.createTitledBorder(selectionTitle));

			useFilledRectangleForSelection = new JCheckBox(
					Messages.getString("PreferencesFrame.FILLED_RECTANGLE")); //$NON-NLS-1$
			useInvertedColorForSelection = new JCheckBox(
					Messages.getString("PreferencesFrame.INVERTED_COLOR")); //$NON-NLS-1$
			JLabel insideColorLabelForSelection = new JLabel(
					Messages.getString("PreferencesFrame.INSIDE_COLOR")); //$NON-NLS-1$
			selectionInsideColor = new ColorSelect();
			JLabel outsideColorLabelForSelection = new JLabel(
					Messages.getString("PreferencesFrame.OUTSIDE_COLOR")); //$NON-NLS-1$
			selectionOutsideColor = new ColorSelect();

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

			String multipleSelectionTitle = Messages.getString("PreferencesFrame.MULTIPLE_SELECTION"); //$NON-NLS-1$
			multipleSelectionPanel.setBorder(BorderFactory.createTitledBorder(multipleSelectionTitle));

			useFilledRectangleForMultipleSelection = new JCheckBox(
					Messages.getString("PreferencesFrame.FILLED_RECTANGLE")); //$NON-NLS-1$

			useInvertedColorForMultipleSelection = new JCheckBox(
					Messages.getString("PreferencesFrame.INVERTED_COLOR")); //$NON-NLS-1$

			JLabel insideColorLabelForMultipleSelection = new JLabel(
					Messages.getString("PreferencesFrame.INSIDE_COLOR")); //$NON-NLS-1$
			multipleSelectionInsideColor = new ColorSelect();

			JLabel outsideColorLabelForMultipleSelection = new JLabel(
					Messages.getString("PreferencesFrame.OUTSIDE_COLOR")); //$NON-NLS-1$
			multipleSelectionOutsideColor = new ColorSelect();

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

		@Override
		public void load()
			{
			undoHistorySize.setValue(Prefs.undoHistorySize);
			useFilledRectangleForViews.setSelected(Prefs.useFilledRectangleForViews);
			useInvertedColorForViews.setSelected(Prefs.useInvertedColorForViews);
			viewInsideColor.setSelectedColor(Util.convertGmColorWithAlpha(Prefs.viewInsideColor));
			viewInsideColor.setSelectedColor(Util.convertGmColorWithAlpha(Prefs.viewOutsideColor));
			useFilledRectangleForSelection.setSelected(Prefs.useFilledRectangleForSelection);
			useInvertedColorForSelection.setSelected(Prefs.useInvertedColorForSelection);
			selectionInsideColor.setSelectedColor(Util.convertGmColorWithAlpha(Prefs.selectionInsideColor));
			selectionOutsideColor.setSelectedColor(Util.convertGmColorWithAlpha(Prefs.selectionOutsideColor));
			useFilledRectangleForMultipleSelection.setSelected(Prefs.useFilledRectangleForMultipleSelection);
			useInvertedColorForMultipleSelection.setSelected(Prefs.useInvertedColorForMultipleSelection);
			multipleSelectionInsideColor.setSelectedColor(Util.convertGmColorWithAlpha(Prefs.multipleSelectionInsideColor));
			multipleSelectionOutsideColor.setSelectedColor(Util.convertGmColorWithAlpha(Prefs.multipleSelectionOutsideColor));
			}

		@Override
		public void save()
			{
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
			}
		}
	}
