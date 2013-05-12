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
import java.awt.Dimension;
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
import javax.swing.LayoutStyle.ComponentPlacement;

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
	JCheckBox dndEnable, restrictTreeEnable, extraNodesEnable;

	private JPanel makeGeneralPrefs()
	{
		JPanel p = new JPanel();

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
    restrictTreeEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_TREE_RESTRICT"));
    restrictTreeEnable.setSelected(Prefs.restrictHierarchy);
    extraNodesEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_EXTRA_NODES"));
    extraNodesEnable.setSelected(Prefs.extraNodes);
		
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
			           .addComponent(iconCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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
			           .addComponent(iconCombo))
			  );
		
		p.setLayout(gl);
		
		return p;
	}
	
	private JPanel makeMimePrefixPrefs()
	{
		JPanel p = new JPanel();
		
		Dimension preferredSize = new Dimension(100, 30);
		
		JLabel spriteLabel = new JLabel("Sprite:");
		JTextField spritePrefix = new JTextField("spr_");
		spritePrefix.setPreferredSize(preferredSize);
		JLabel soundLabel = new JLabel("Sound:");
		JTextField soundPrefix = new JTextField("snd_");
		soundPrefix.setPreferredSize(preferredSize);
		JLabel backgroundLabel = new JLabel("Background:");
		JTextField backgroundPrefix = new JTextField("bg_");
		backgroundPrefix.setPreferredSize(preferredSize);
		JLabel pathLabel = new JLabel("Path:");
		JTextField pathPrefix = new JTextField("pth_");
		pathPrefix.setPreferredSize(preferredSize);
		JLabel scriptLabel = new JLabel("Script:");
		JTextField scriptPrefix = new JTextField("scr_");
		scriptPrefix.setPreferredSize(preferredSize);
		JLabel fontLabel = new JLabel("Font:");
		JTextField fontPrefix = new JTextField("fnt_");
		fontPrefix.setPreferredSize(preferredSize);
		JLabel timelineLabel = new JLabel("Timeline:");
		JTextField timelinePrefix = new JTextField("tl_");
		timelinePrefix.setPreferredSize(preferredSize);
		JLabel objectLabel = new JLabel("Object:");
		JTextField objectPrefix = new JTextField("obj_");
		objectPrefix.setPreferredSize(preferredSize);
		JLabel roomLabel = new JLabel("Room:");
		JTextField roomPrefix = new JTextField("rm_");
		roomPrefix.setPreferredSize(preferredSize);
		
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
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl.createParallelGroup(Alignment.LEADING, false)
							.addGroup(gl.createSequentialGroup()
								.addComponent(spritePrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(soundLabel))
							.addGroup(gl.createSequentialGroup()
								.addComponent(timelinePrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(objectLabel))
							.addGroup(gl.createSequentialGroup()
								.addComponent(pathPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(scriptLabel)))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl.createParallelGroup(Alignment.LEADING, false)
							.addGroup(gl.createSequentialGroup()
								.addComponent(soundPrefix)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(backgroundLabel))
							.addGroup(gl.createSequentialGroup()
								.addComponent(scriptPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(fontLabel))
							.addGroup(gl.createSequentialGroup()
								.addComponent(objectPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(roomLabel)))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl.createParallelGroup(Alignment.LEADING)
							.addComponent(roomPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(fontPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(backgroundPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
			);
			gl.setVerticalGroup(
				gl.createParallelGroup(Alignment.LEADING)
					.addGroup(gl.createSequentialGroup()
						.addGroup(gl.createParallelGroup(Alignment.BASELINE)
							.addComponent(soundPrefix)
							.addComponent(backgroundLabel)
							.addComponent(backgroundPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(spritePrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(spriteLabel)
							.addComponent(soundLabel))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl.createParallelGroup(Alignment.BASELINE)
							.addComponent(pathLabel)
							.addComponent(pathPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(scriptPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(fontPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(scriptLabel)
							.addComponent(fontLabel))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl.createParallelGroup(Alignment.BASELINE)
							.addComponent(timelinePrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(objectPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(roomPrefix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(timelineLabel)
							.addComponent(objectLabel)
							.addComponent(roomLabel, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))
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
		
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		// TODO: Fix this layout so that it rezies the text fields properly
		gl.setHorizontalGroup(
			   gl.createSequentialGroup()
			      .addGroup(gl.createParallelGroup(Alignment.TRAILING)
			           .addComponent(spriteEditorLabel)
			           .addComponent(soundEditorLabel)
			           .addComponent(backgroundEditorLabel)
			           .addComponent(codeEditorLabel))
			      .addGroup(gl.createParallelGroup()
			           .addComponent(spriteEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           .addComponent(soundEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           .addComponent(backgroundEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			           .addComponent(codeEditorCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(gl.createParallelGroup()
			           .addComponent(spriteEditorPath)
			           .addComponent(soundEditorPath)
			           .addComponent(backgroundEditorPath)
			           .addComponent(codeEditorPath))
			      .addGroup(gl.createParallelGroup()
			           .addComponent(spriteEditorButton)
			           .addComponent(soundEditorButton)
			           .addComponent(backgroundEditorButton)
			           .addComponent(codeEditorButton))
			  );
		gl.setVerticalGroup(
			   gl.createSequentialGroup()
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(spriteEditorLabel)
			           .addComponent(spriteEditorCombo)
			           .addComponent(spriteEditorPath)
			           .addComponent(spriteEditorButton))
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(soundEditorLabel)
			           .addComponent(soundEditorCombo)
			           .addComponent(soundEditorPath)
			           .addComponent(soundEditorButton))
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(backgroundEditorLabel)
			           .addComponent(backgroundEditorCombo)
			           .addComponent(backgroundEditorPath)
			           .addComponent(backgroundEditorButton))
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(codeEditorLabel)
			           .addComponent(codeEditorCombo)
			           .addComponent(codeEditorPath)
			           .addComponent(codeEditorButton))
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
	  PrefsStore.setExtraNodes(extraNodesEnable.isSelected());
	}
	
	public void ResetPreferences()
	{
		//TODO: Reset preferences to their active state when this frame was first opened
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