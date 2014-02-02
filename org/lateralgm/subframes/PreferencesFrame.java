/*
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.messages.Messages;

public class PreferencesFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	protected JTabbedPane tabs;
	protected JSpinner sSizes;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	protected Color fgColor;
	
	JComboBox themeCombo, iconCombo, langCombo, actionsCombo;
	JCheckBox dndEnable, restrictTreeEnable, extraNodesEnable, dockEvent, backupsEnable;
  JTextField iconPath, themePath, manualPath, actionsPath;
  
	JTextField soundEditorPath, backgroundEditorPath, spriteEditorPath, codeEditorPath, numberBackupsField;
  // Sounds use their own stored filename/extension, which may vary from sound to sound. 
  JTextField backgroundMIME, spriteMIME, scriptMIME;
	
	private JPanel makeGeneralPrefs()
	{
		JPanel p = new JPanel();

		JLabel themeLabel = new JLabel(Messages.getString("PreferencesFrame.THEME") + ":");
    Vector<String> comboBoxItems = new Vector<String>();
    comboBoxItems.add("Swing");
    comboBoxItems.add("Native");
    LookAndFeelInfo lnfs[] = UIManager.getInstalledLookAndFeels();
    for (int i = 0; i < lnfs.length; i++) {
      comboBoxItems.add(lnfs[i].getName());
      if (lnfs[i].getName().toLowerCase().contains("gtk")) {
        comboBoxItems.add("Quantum");
      }
    }
    comboBoxItems.add("Custom");
    final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(comboBoxItems);
    
    themeCombo = new JComboBox(model);
    themeCombo.setSelectedItem(LGM.themename);
		JLabel iconLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS") + ":");
    String[] iconOptions = { "Swing", "Calico", "Custom" };
    iconCombo = new JComboBox(iconOptions);
    iconCombo.setSelectedItem(LGM.iconspack);
		JLabel langLabel = new JLabel(Messages.getString("PreferencesFrame.LANGUAGE") + ":");
    String[] langOptions = { "English", "French", "Turkish", "Danish" };
    langCombo = new JComboBox(langOptions);
    langCombo.setSelectedItem(Prefs.languageName);
    dndEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_DND"));
    dndEnable.setSelected(Prefs.enableDragAndDrop);
    restrictTreeEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_TREE_RESTRICT"));
    restrictTreeEnable.setSelected(Prefs.restrictHierarchy);
    extraNodesEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_EXTRA_NODES"));
    extraNodesEnable.setSelected(Prefs.extraNodes);
    dockEvent = new JCheckBox(Messages.getString("PreferencesFrame.DOCK_EVENT_PANEL"));
    dockEvent.setSelected(Prefs.dockEventPanel);
		JLabel themePathLabel = new JLabel(Messages.getString("PreferencesFrame.THEME_PATH") + ":");
		themePath = new JTextField(Prefs.swingThemePath);
		JLabel iconPathLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS_PATH") + ":");
		iconPath = new JTextField(Prefs.iconPath);
		JLabel manualPathLabel = new JLabel(Messages.getString("PreferencesFrame.MANUAL_PATH") + ":");
		manualPath = new JTextField(Prefs.manualPath);
		JLabel actionsLabel = new JLabel(Messages.getString("PreferencesFrame.ACTIONLIBRARY") + ":");
    String[] actionsOptions = { "Standard", "Logic", "Custom" };
    actionsCombo = new JComboBox(actionsOptions);
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
		
		gl.setHorizontalGroup(
			   gl.createParallelGroup()
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(dndEnable)
			           .addComponent(restrictTreeEnable)
			           .addComponent(extraNodesEnable))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(themeLabel)
			           .addComponent(themeCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           .addComponent(iconLabel)
			           .addComponent(iconCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			  				 .addComponent(langLabel)
			           .addComponent(langCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createParallelGroup()
			           .addComponent(themePathLabel)
			           .addComponent(iconPathLabel)
			           .addComponent(manualPathLabel))
			      .addGroup(gl.createParallelGroup()
			      		 .addComponent(themePath)
			           .addComponent(iconPath)
			           .addComponent(manualPath))
			      .addGroup(gl.createParallelGroup()
			      		 .addComponent(dockEvent))
			      .addGroup(gl.createParallelGroup()
			      		 .addComponent(backupsEnable))
			      .addGroup(gl.createParallelGroup()
			           .addComponent(backupsLabel)
			      		 .addComponent(numberBackupsField))
			           /*
			      .addGroup(gl.createSequentialGroup()
			      		 .addComponent(actionsLabel)
			           .addComponent(actionsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           .addComponent(actionsPath))
				*/
				);
		gl.setVerticalGroup(
			   gl.createSequentialGroup()
			      .addGroup(gl.createParallelGroup()
			           .addComponent(dndEnable)
			           .addComponent(restrictTreeEnable)
			           .addComponent(extraNodesEnable))
			      .addGroup(gl.createParallelGroup(Alignment.BASELINE)
			           .addComponent(themeLabel)
			           .addComponent(themeCombo)
			           .addComponent(iconLabel)
			           .addComponent(iconCombo)
			           .addComponent(langLabel)
			           .addComponent(langCombo))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(themePathLabel)
			           .addComponent(themePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(iconPathLabel)
			           .addComponent(iconPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(manualPathLabel)
			           .addComponent(manualPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(dockEvent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(backupsEnable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(backupsLabel))
			      .addGroup(gl.createSequentialGroup()
			           .addComponent(numberBackupsField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))   
			           /*
			  		.addGroup(gl.createParallelGroup(Alignment.BASELINE)
			      		 .addComponent(actionsLabel)
			           .addComponent(actionsCombo)
			           .addComponent(actionsPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			           */
				);
		
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
				gl.createParallelGroup(Alignment.LEADING)
					.addGroup(gl.createSequentialGroup()
						.addGroup(gl.createParallelGroup(Alignment.TRAILING)
							.addComponent(timelineLabel)
							.addComponent(pathLabel)
							.addComponent(spriteLabel))
						.addGroup(gl.createParallelGroup(Alignment.LEADING)
								.addComponent(spritePrefix)
								.addComponent(timelinePrefix)
								.addComponent(pathPrefix))
						.addGroup(gl.createParallelGroup(Alignment.LEADING, false)
								.addComponent(soundLabel)
								.addComponent(objectLabel)
								.addComponent(scriptLabel))
						.addGroup(gl.createParallelGroup(Alignment.LEADING)
								.addComponent(soundPrefix)
								.addComponent(scriptPrefix)
								.addComponent(objectPrefix))
						.addGroup(gl.createParallelGroup(Alignment.LEADING, false)
								.addComponent(backgroundLabel)
								.addComponent(fontLabel)
								.addComponent(roomLabel))
						.addGroup(gl.createParallelGroup(Alignment.LEADING)
							.addComponent(roomPrefix)
							.addComponent(fontPrefix)
							.addComponent(backgroundPrefix)))
						.addGroup(gl.createSequentialGroup()
								  .addComponent(backgroundMIMELabel)
									.addComponent(backgroundMIME)
									.addComponent(spriteMIMELabel)
									.addComponent(spriteMIME)
									.addComponent(scriptMIMELabel)
									.addComponent(scriptMIME))
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
					.addGroup(gl.createSequentialGroup()
						.addGroup(gl.createParallelGroup(Alignment.BASELINE)
							.addComponent(soundPrefix)
							.addComponent(backgroundLabel)
							.addComponent(backgroundPrefix)
							.addComponent(spritePrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(spriteLabel)
							.addComponent(soundLabel))
						//.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl.createParallelGroup(Alignment.BASELINE)
							.addComponent(pathLabel)
							.addComponent(pathPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(scriptPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(fontPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(scriptLabel)
							.addComponent(fontLabel))
						//.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl.createParallelGroup(Alignment.BASELINE)
							.addComponent(timelinePrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(objectPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(roomPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(timelineLabel)
							.addComponent(objectLabel)
							.addComponent(roomLabel, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl.createParallelGroup(Alignment.BASELINE)
								.addComponent(backgroundMIMELabel)
								.addComponent(backgroundMIME, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(spriteMIMELabel)
								.addComponent(spriteMIME, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(scriptMIMELabel)
								.addComponent(scriptMIME, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			);
		
		p.setLayout(gl);
		
		return p;
	}
	
	private JPanel makeExternalEditorPrefs()
	{
		JPanel p = new JPanel();
		
    String[] defaultEditorOptions = { Messages.getString("PreferencesFrame.DEFAULT"), 
    		Messages.getString("PreferencesFrame.SYSTEM"), 
    		Messages.getString("PreferencesFrame.CUSTOM") };
		
    // Uncomment the shit for this panel if you dun want people messin with commands
    // too lazy to code it.
    
		JLabel codeEditorLabel = new JLabel(Messages.getString("PreferencesFrame.CODE_EDITOR") + ":");
    //JComboBox codeEditorCombo = new JComboBox(defaultEditorOptions);
    //codeEditorCombo.setSelectedItem(0);
		codeEditorPath = new JTextField(Prefs.externalScriptEditorCommand);
		//JButton codeEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		JLabel spriteEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SPRITE_EDITOR") + ":");
    //JComboBox spriteEditorCombo = new JComboBox(defaultEditorOptions);
    //spriteEditorCombo.setSelectedItem(0);
		spriteEditorPath = new JTextField(Prefs.externalSpriteEditorCommand);
		//JButton spriteEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		JLabel backgroundEditorLabel = new JLabel(Messages.getString("PreferencesFrame.BACKGROUND_EDITOR") + ":");
    //JComboBox backgroundEditorCombo = new JComboBox(defaultEditorOptions);
    //backgroundEditorCombo.setSelectedItem(0);
		backgroundEditorPath = new JTextField(Prefs.externalBackgroundEditorCommand);
		//JButton backgroundEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		JLabel soundEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SOUND_EDITOR") + ":");
    //JComboBox soundEditorCombo = new JComboBox(defaultEditorOptions);
    //soundEditorCombo.setSelectedItem(0);
		soundEditorPath = new JTextField(Prefs.externalSoundEditorCommand);
		//JButton soundEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		// TODO: Fix this layout so that it resizes the text fields properly
		gl.setHorizontalGroup(
			   gl.createSequentialGroup()
			      .addGroup(gl.createParallelGroup(Alignment.TRAILING)
			           .addComponent(spriteEditorLabel)
			           .addComponent(soundEditorLabel)
			           .addComponent(backgroundEditorLabel)
			           .addComponent(codeEditorLabel))
			      //.addGroup(gl.createParallelGroup()
			           //.addComponent(spriteEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           //.addComponent(soundEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           //.addComponent(backgroundEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           //.addComponent(codeEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createParallelGroup()
			           .addComponent(spriteEditorPath)
			           .addComponent(soundEditorPath)
			           .addComponent(backgroundEditorPath)
			           .addComponent(codeEditorPath))
			      //.addGroup(gl.createParallelGroup()
			           //.addComponent(spriteEditorButton)
			           //.addComponent(soundEditorButton)
			           //.addComponent(backgroundEditorButton)
			           //.addComponent(codeEditorButton))
			  );
		gl.setVerticalGroup(
			   gl.createSequentialGroup()
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(spriteEditorLabel)
			           //.addComponent(spriteEditorCombo)
			           .addComponent(spriteEditorPath))
			           //.addComponent(spriteEditorButton))
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(soundEditorLabel)
			           //.addComponent(soundEditorCombo)
			           .addComponent(soundEditorPath))
			           //.addComponent(soundEditorButton))
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(backgroundEditorLabel)
			           //.addComponent(backgroundEditorCombo)
			           .addComponent(backgroundEditorPath))
			           //.addComponent(backgroundEditorButton))
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(codeEditorLabel)
			           //.addComponent(codeEditorCombo)
			           .addComponent(codeEditorPath))
			           //.addComponent(codeEditorButton))
			  );
		
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

	public PreferencesFrame()
	{
	  setAlwaysOnTop(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(600,400);
	  setLocationRelativeTo(LGM.frame);
		setTitle(Messages.getString("PreferencesFrame.TITLE"));
		setIconImage(LGM.getIconForKey("Toolbar.PREFERENCES").getImage());
		setResizable(true);

		tabs = new JTabbedPane();
		add(tabs,BorderLayout.CENTER);

		tabs.addTab(Messages.getString("PreferencesFrame.TAB_GENERAL"), //$NON-NLS-1$
				/**/null,makeGeneralPrefs(),Messages.getString("PreferencesFrame.HINT_GENERAL")); //$NON-NLS-1$ 
		JPanel pan = makeExternalEditorPrefs();
		tabs.addTab(Messages.getString("PreferencesFrame.TAB_EXTERNAL_EDITOR"), //$NON-NLS-1$
				/**/null,pan,Messages.getString("PreferencesFrame.HINT_EXTERNAL_EDITOR")); //$NON-NLS-1$ 
		tabs.addTab(Messages.getString("PreferencesFrame.TAB_MIME_PREFIX"), //$NON-NLS-1$
				/**/null,makeMimePrefixPrefs(),Messages.getString("PreferencesFrame.HINT_MIME_PREFIX")); //$NON-NLS-1$
		tabs.addTab(Messages.getString("PreferencesFrame.TAB_CODE_EDITOR"), //$NON-NLS-1$
				/**/null,makeCodeEditorPrefs(),Messages.getString("PreferencesFrame.HINT_CODE_EDITOR")); //$NON-NLS-1$ 
		
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
	   gl.createSequentialGroup()
	      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
	           .addComponent(applyBut)
	           .addComponent(closeBut))
	  );
		
    add(p,BorderLayout.SOUTH);
	}
	
	public void SavePreferences()
  {
    LGM.iconspack = (String)iconCombo.getSelectedItem();
    PrefsStore.setIconPack(LGM.iconspack);
    PrefsStore.setIconPath(iconPath.getText());
    PrefsStore.setSwingThemePath(themePath.getText());
	  PrefsStore.setSwingTheme(LGM.themename);
	  PrefsStore.setManualPath(manualPath.getText());
	  PrefsStore.setDNDEnabled(dndEnable.isSelected());
	  PrefsStore.setExtraNodes(extraNodesEnable.isSelected());
	  PrefsStore.setLanguageName((String)langCombo.getSelectedItem());
	  PrefsStore.setUserLibraryPath(actionsPath.getText());
	  PrefsStore.setSpriteExt(spriteMIME.getText());
	  PrefsStore.setBackgroundExt(backgroundMIME.getText());
	  PrefsStore.setScriptExt(scriptMIME.getText());
	  PrefsStore.setBackgroundEditorCommand(backgroundEditorPath.getText());
	  PrefsStore.setSpriteEditorCommand(spriteEditorPath.getText());
	  PrefsStore.setSoundEditorCommand(soundEditorPath.getText());
	  PrefsStore.setScriptEditorCommand(codeEditorPath.getText());
	  PrefsStore.setDockEventPanel(dockEvent.isSelected());
	}
	
	public void ResetPreferences()
	{
		//TODO: Reset preferences to their active state when this frame was first opened
	  // For this one just copy the shit above where I give the controls their default
	  // values froms Prefs.
	}
	
	public void ResetPreferencesToDefault()
	{
		//TODO: Reset the preferences to their settings when LGM was first installed
	}
	
	public void actionPerformed(ActionEvent ev)
	{
			String com = ev.getActionCommand();
			if (com.equals("PreferencesFrame.APPLY_CHANGES")) //$NON-NLS-1$
			{
			  JOptionPane.showMessageDialog(this, Messages.getString("PreferencesFrame.APPLY_CHANGES_NOTICE"));
			  LGM.SetLookAndFeel((String)themeCombo.getSelectedItem());
			  LGM.UpdateLookAndFeel();
			  SavePreferences();
			}
			if (com.equals("PreferencesFrame.CLOSE")) //$NON-NLS-1$
			{
				this.setVisible(false);
			}
	}
}