/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.lateralgm.file.Gm6File;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;

public class GameSettingFrame extends JInternalFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public static JCheckBox startFullscreen;

	JTabbedPane tabbedPane = new JTabbedPane();

	private JPanel makeGraphicsPane()
		{
		JPanel panel = new JPanel(false);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		String t = Messages.getString("GameSettingFrame.FULLSCREEN"); //$NON-NLS-1$
		startFullscreen = new JCheckBox(t,LGM.currentFile.startFullscreen);

		JPanel scaling = new JPanel();
		String scaleTitle = Messages.getString("GameSettingFrame.SCALING_TITLE"); //$NON-NLS-1$
		scaling.setBorder(BorderFactory.createTitledBorder(scaleTitle));
		scaling.setLayout(new BoxLayout(scaling,BoxLayout.PAGE_AXIS));
		ButtonGroup group = new ButtonGroup();
		JRadioButton option;
		option = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_FIXED")); //$NON-NLS-1$
		group.add(option);
		scaling.add(option);
		option = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_RATIO")); //$NON-NLS-1$
		group.add(option);
		scaling.add(option);
		option = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_FULL")); //$NON-NLS-1$
		group.add(option);
		scaling.add(option);

		t = Messages.getString("GameSettingFrame.INTERPOLATE"); //$NON-NLS-1$
		JCheckBox interpolatecolors = new JCheckBox(t,LGM.currentFile.interpolate);
		JLabel backcolor = new JLabel(Messages.getString("GameSettingFrame.BACKCOLOR")); //$NON-NLS-1$
		t = Messages.getString("GameSettingFrame.SETCOLOR"); //$NON-NLS-1$
		JButton colorbutton = new JButton(t);
		colorbutton.setBackground(new Color(LGM.currentFile.colorOutsideRoom));
		colorbutton.setHideActionText(true);

		t = Messages.getString("GameSettingFrame.RESIZE"); //$NON-NLS-1$
		JCheckBox resizeWindow = new JCheckBox(t,LGM.currentFile.allowWindowResize);
		t = Messages.getString("GameSettingFrame.STAYONTOP"); //$NON-NLS-1$
		JCheckBox stayOnTop = new JCheckBox(t,LGM.currentFile.alwaysOnTop);
		t = Messages.getString("GameSettingFrame.NOBORDER"); //$NON-NLS-1$
		JCheckBox drawBorderedWindow = new JCheckBox(t,LGM.currentFile.dontDrawBorder);
		t = Messages.getString("GameSettingFrame.NOBUTTONS"); //$NON-NLS-1$
		JCheckBox drawButtonsCaption = new JCheckBox(t,LGM.currentFile.dontShowButtons);
		t = Messages.getString("GameSettingFrame.DISPLAYCURSOR"); //$NON-NLS-1$
		JCheckBox displayMouse = new JCheckBox(t,LGM.currentFile.displayCursor);
		t = Messages.getString("GameSettingFrame.FREEZE"); //$NON-NLS-1$
		JCheckBox freezeGame = new JCheckBox(t,LGM.currentFile.freezeOnLoseFocus);
		panel.add(startFullscreen);
		panel.add(scaling);
		panel.add(interpolatecolors);
		panel.add(backcolor);
		panel.add(colorbutton);
		panel.add(resizeWindow);
		panel.add(stayOnTop);
		panel.add(drawBorderedWindow);
		panel.add(drawButtonsCaption);
		panel.add(displayMouse);
		panel.add(freezeGame);
		return panel;
		}

	//TODO:
	private JPanel makeResolutionPane()
		{
		JPanel panel = new JPanel(false);

		return panel;
		}

	private JPanel makeOtherPane()
		{
		JPanel panel = new JPanel(false);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		JPanel dKeys = new JPanel();
		String t = Messages.getString("GameSettingFrame.TITLE_KEYS"); //$NON-NLS-1$
		dKeys.setBorder(BorderFactory.createTitledBorder(t));
		dKeys.setLayout(new BoxLayout(dKeys,BoxLayout.PAGE_AXIS));
		panel.add(dKeys);
		t = Messages.getString("GameSettingFrame.KEY_ENDGAME"); //$NON-NLS-1$
		JCheckBox esc = new JCheckBox(t,LGM.currentFile.letEscEndGame);
		t = Messages.getString("GameSettingFrame.KEY_INFO"); //$NON-NLS-1$
		JCheckBox f1 = new JCheckBox(t,LGM.currentFile.letF1ShowGameInfo);
		t = Messages.getString("GameSettingFrame.KEY_SWITCHFULLSCREEN"); //$NON-NLS-1$
		JCheckBox f4 = new JCheckBox(t,LGM.currentFile.letF4SwitchFullscreen);
		t = Messages.getString("GameSettingFrame.SAVELOAD"); //$NON-NLS-1$
		JCheckBox f5 = new JCheckBox(t,LGM.currentFile.letF5SaveF6Load);
		dKeys.add(esc);
		dKeys.add(f1);
		dKeys.add(f4);
		dKeys.add(f5);
		JPanel gpp = new JPanel();
		t = Messages.getString("GameSettingFrame.TITLE_PRIORITY"); //$NON-NLS-1$
		gpp.setBorder(BorderFactory.createTitledBorder(t));
		gpp.setLayout(new BoxLayout(gpp,BoxLayout.PAGE_AXIS));
		panel.add(gpp);

		ButtonGroup group = new ButtonGroup();
		JRadioButton option;
		t = Messages.getString("GameSettingFrame.PRIORITY_NORMAL"); //$NON-NLS-1$
		option = new JRadioButton(t);
		group.add(option);
		gpp.add(option);
		t = Messages.getString("GameSettingFrame.PRIORITY_HIGH"); //$NON-NLS-1$
		option = new JRadioButton(t);
		group.add(option);
		gpp.add(option);
		t = Messages.getString("GameSettingFrame.PRIORITY_HIHGEST"); //$NON-NLS-1$
		option = new JRadioButton(t);
		group.add(option);
		gpp.add(option);
		return panel;
		}

	//TODO:
	private JPanel makeLoadingPane()
		{
		JPanel panel = new JPanel(false);

		return panel;
		}

	//TODO: (I love this name)
	private JPanel makeConstantsPane()
		{
		JPanel panel = new JPanel(false);

		return panel;
		}

	//TODO:
	private JPanel makeIncludePane()
		{
		JPanel panel = new JPanel(false);

		return panel;
		}

	private JPanel makeErrorPane()
		{
		JPanel panel = new JPanel(false);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		String t = Messages.getString("GameSettingFrame.ERRORS_DISPLAY"); //$NON-NLS-1$
		JCheckBox dem = new JCheckBox(t,LGM.currentFile.displayErrors);
		t = Messages.getString("GameSettingFrame.ERRORS_LOG"); //$NON-NLS-1$
		JCheckBox wge = new JCheckBox(t,LGM.currentFile.writeToLog);
		t = Messages.getString("GameSettingFrame.ERRORS_ABORT"); //$NON-NLS-1$
		JCheckBox abort = new JCheckBox(t,LGM.currentFile.abortOnError);
		t = Messages.getString("GameSettingFrame.UNINITZERO"); //$NON-NLS-1$
		JCheckBox tuv0 = new JCheckBox(t,LGM.currentFile.treatUninitializedAs0);
		panel.add(dem);
		panel.add(wge);
		panel.add(abort);
		panel.add(tuv0);
		return panel;
		}

	private JPanel makeInfoPane()
		{
		JPanel panel = new JPanel(false);
		panel.setLayout(new FlowLayout());
		JLabel label = new JLabel(Messages.getString("GameSettingFrame.AUTHOR")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(80,25));
		panel.add(label);
		JTextField box = new JTextField(LGM.currentFile.author);
		box.setPreferredSize(new Dimension(390,25));
		panel.add(box);
		label = new JLabel(Messages.getString("GameSettingFrame.VERSION")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(80,25));
		panel.add(label);
		box = new JTextField("" + LGM.currentFile.version);
		box.setPreferredSize(new Dimension(390,25));
		panel.add(box);
		label = new JLabel(Messages.getString("GameSettingFrame.LASTCHANGED")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(80,25));
		panel.add(label);
		box = new JTextField(Gm6File.gmTimeToString(LGM.currentFile.lastChanged));
		box.setPreferredSize(new Dimension(390,25));
		box.setEditable(false);
		panel.add(box);
		label = new JLabel(Messages.getString("GameSettingFrame.INFORMATION")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(70,25));
		panel.add(label);
		JTextArea boxa = new JTextArea(LGM.currentFile.information);
		boxa.setPreferredSize(new Dimension(500,200));
		boxa.setLineWrap(true);
		panel.add(boxa);
		return panel;
		}

	public GameSettingFrame()
		{
		super(Messages.getString("GameSettingFrame.TITLE"),true,true,true,true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(540,470);
		setFrameIcon(LGM.findIcon("restree/gm.png"));
		setLayout(new FlowLayout());
		tabbedPane.setPreferredSize(new Dimension(530,400));
		setResizable(false);
		getContentPane().add(tabbedPane);

		JComponent pane = makeGraphicsPane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_GRAPHICS"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_GRAPHICS")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(0,KeyEvent.VK_1);

		pane = makeResolutionPane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_RESOLUTION"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_RESOLUTION")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		pane = makeOtherPane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_OTHER"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_OTHER")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		pane = makeLoadingPane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_LOADING"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_LOADING")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		pane = makeConstantsPane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_CONSTANTS"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_CONSTANTS")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		pane = makeIncludePane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_INCLUDE"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_INCLUDE")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		pane = makeErrorPane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_ERRORS"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_ERRORS")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		pane = makeInfoPane();
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_INFO"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_INFO")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		String t = Messages.getString("GameSettingFrame.BUTTON_SAVE"); //$NON-NLS-1$
		JButton okButton = new JButton(t);
		getContentPane().add(okButton);
		t = Messages.getString("GameSettingFrame.BUTTON_DISCARD"); //$NON-NLS-1$
		JButton cancelButton = new JButton(t);
		add(cancelButton);
		}

	//TODO:
	public void actionPerformed(ActionEvent arg0)
		{
		// unused
		}
	}
