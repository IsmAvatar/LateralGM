/*
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameInformation;

public class PreferencesFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	protected JTabbedPane tabs;
	protected JSpinner sSizes;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	protected Color fgColor;
	
	JComboBox themeCombo, iconCombo;
	JCheckBox dndEnable;

	private JPanel makeGeneralPrefs()
	{
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		JLabel themeLabel = new JLabel(Messages.getString("PreferencesFrame.THEME") + ":");
    String[] themeOptions = { "Swing", "Native", "Motif", "GTK", "Custom"};
    themeCombo = new JComboBox(themeOptions);
    themeCombo.setSelectedItem(LGM.themename);
		JLabel iconLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS") + ":");
    String[] iconOptions = { "Swing", "Standard", "Custom" };
    iconCombo = new JComboBox(iconOptions);
    iconCombo.setSelectedItem(LGM.iconspack);
    dndEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_DND"));
    dndEnable.setSelected(Prefs.enableDragAndDrop);
    
    p.add(dndEnable);
    p.add(themeLabel);
		p.add(themeCombo);
		p.add(iconLabel);
		p.add(iconCombo);
		
		return p;
	}
	
	private JPanel makeMimePrefixPrefs()
	{
		JPanel p = new JPanel();
		
		return p;
	}
	
	private JPanel makeExternalEditorPrefs()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
    String[] defaultEditorOptions = { Messages.getString("PreferencesFrame.DEFAULT"), 
    		Messages.getString("PreferencesFrame.SYSTEM"), 
    		Messages.getString("PreferencesFrame.CUSTOM") };
		
		JLabel codeEditorLabel = new JLabel(Messages.getString("PreferencesFrame.CODE_EDITOR") + ":");
    JComboBox codeEditorCombo = new JComboBox(defaultEditorOptions);
    codeEditorCombo.setSelectedItem(0);
		JTextField codeEditorPath = new JTextField();
		JButton codeEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		JLabel spriteEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SPRITE_EDITOR") + ":");
    JComboBox spriteEditorCombo = new JComboBox(defaultEditorOptions);
    spriteEditorCombo.setSelectedItem(0);
		JTextField spriteEditorPath = new JTextField();
		JButton spriteEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		JLabel backgroundEditorLabel = new JLabel(Messages.getString("PreferencesFrame.BACKGROUND_EDITOR") + ":");
    JComboBox backgroundEditorCombo = new JComboBox(defaultEditorOptions);
    backgroundEditorCombo.setSelectedItem(0);
		JTextField backgroundEditorPath = new JTextField();
		JButton backgroundEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		JLabel soundEditorLabel = new JLabel(Messages.getString("PreferencesFrame.SOUND_EDITOR") + ":");
    JComboBox soundEditorCombo = new JComboBox(defaultEditorOptions);
    soundEditorCombo.setSelectedItem(0);
		JTextField soundEditorPath = new JTextField();
		JButton soundEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		p.add(codeEditorLabel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.5;
		p.add(codeEditorCombo, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 20;
		p.add(codeEditorPath, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.weightx = 1;
		p.add(codeEditorButton, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		p.add(spriteEditorLabel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.5;
		p.add(spriteEditorCombo, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 20;
		p.add(spriteEditorPath, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.weightx = 1;
		p.add(spriteEditorButton, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		p.add(backgroundEditorLabel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1.5;
		p.add(backgroundEditorCombo, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.weightx = 20;
		p.add(backgroundEditorPath, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.weightx = 1;
		p.add(backgroundEditorButton, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1;
		p.add(soundEditorLabel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 1.5;
		p.add(soundEditorCombo, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.weightx = 20;
		p.add(soundEditorPath, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 3;
		gbc.gridy = 3;
		gbc.weightx = 1;
		p.add(soundEditorButton, gbc);
		
		return p;
	}
	
	private JPanel makeCodeEditorPrefs()
	{
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		return p;
	}

	public PreferencesFrame()
	{
	  setAlwaysOnTop(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(600,400);
	  setLocationRelativeTo(LGM.frame);
		setTitle(Messages.getString("PreferencesFrame.TITLE"));
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
	  PrefsStore.setSwingTheme(LGM.themename);
	  PrefsStore.setDNDEnabled(dndEnable.isSelected());
	}
	
	public void ResetPreferences()
	{
		
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