/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam
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

public class GameSettingFrame extends JInternalFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public static JCheckBox startFullscreen;

	JTabbedPane tabbedPane = new JTabbedPane();

	public GameSettingFrame()
		{
		super(Messages.getString("GameSettingFrame.TITLE"),true,true,true,true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(540,470);
		setFrameIcon(LGM.findIcon("gm.png"));
		setLayout(new FlowLayout());
		tabbedPane.setPreferredSize(new Dimension(530,400));
		setResizable(false);
		getContentPane().add(tabbedPane);

			// Graphics tab
			{
			JComponent panel1 = new JPanel(false);
			tabbedPane
					.addTab(
							Messages.getString("GameSettingFrame.TAB_GRAPHICS"),null,panel1,Messages.getString("GameSettingFrame.HINT_GRAPHICS")); //$NON-NLS-1$ //$NON-NLS-2$
			tabbedPane.setMnemonicAt(0,KeyEvent.VK_1);
			panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
			startFullscreen = new JCheckBox(
					Messages.getString("GameSettingFrame.FULLSCREEN"),LGM.currentFile.StartFullscreen); //$NON-NLS-1$

			JPanel scaling = new JPanel();
			scaling.setBorder(BorderFactory
					.createTitledBorder(Messages.getString("GameSettingFrame.SCALING_TITLE"))); //$NON-NLS-1$
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

			JCheckBox Interpolatecolors = new JCheckBox(Messages.getString("GameSettingFrame.INTERPOLATE"), //$NON-NLS-1$
					LGM.currentFile.Interpolate);
			JLabel backcolor = new JLabel(Messages.getString("GameSettingFrame.BACKCOLOR")); //$NON-NLS-1$
			JButton colorbutton = new JButton(Messages.getString("GameSettingFrame.SETCOLOR")); //$NON-NLS-1$
			colorbutton.setBackground(new Color(LGM.currentFile.ColorOutsideRoom));
			colorbutton.setHideActionText(true);

			JCheckBox ResizeWindow = new JCheckBox(Messages.getString("GameSettingFrame.RESIZE"), //$NON-NLS-1$
					LGM.currentFile.AllowWindowResize);
			JCheckBox StayOnTop = new JCheckBox(Messages.getString("GameSettingFrame.STAYONTOP"), //$NON-NLS-1$
					LGM.currentFile.AlwaysOnTop);
			JCheckBox DrawBorderedWindow = new JCheckBox(Messages.getString("GameSettingFrame.NOBORDER"), //$NON-NLS-1$
					LGM.currentFile.DontDrawBorder);
			JCheckBox DrawButtonsCaption = new JCheckBox(Messages.getString("GameSettingFrame.NOBUTTONS"), //$NON-NLS-1$
					LGM.currentFile.DontShowButtons);
			JCheckBox DisplayMouse = new JCheckBox(
					Messages.getString("GameSettingFrame.DISPLAYCURSOR"),LGM.currentFile.DisplayCursor); //$NON-NLS-1$
			JCheckBox FreezeGame = new JCheckBox(Messages.getString("GameSettingFrame.FREEZE"), //$NON-NLS-1$
					LGM.currentFile.FreezeOnLoseFocus);
			panel1.add(startFullscreen);
			panel1.add(scaling);
			panel1.add(Interpolatecolors);
			panel1.add(backcolor);
			panel1.add(colorbutton);
			panel1.add(ResizeWindow);
			panel1.add(StayOnTop);
			panel1.add(DrawBorderedWindow);
			panel1.add(DrawButtonsCaption);
			panel1.add(DisplayMouse);
			panel1.add(FreezeGame);
			}

		JComponent panel2 = new JPanel(false);
		tabbedPane
				.addTab(
						Messages.getString("GameSettingFrame.TAB_RESOLUTION"),null,panel2,Messages.getString("GameSettingFrame.HINT_RESOLUTION")); //$NON-NLS-1$ //$NON-NLS-2$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

			// other tab
			{
			JComponent panel3 = new JPanel(false);
			tabbedPane
					.addTab(
							Messages.getString("GameSettingFrame.TAB_OTHER"),null,panel3,Messages.getString("GameSettingFrame.HINT_OTHER")); //$NON-NLS-1$ //$NON-NLS-2$
			tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);
			panel3.setLayout(new BoxLayout(panel3,BoxLayout.PAGE_AXIS));
			JPanel Dkeys = new JPanel();
			Dkeys.setBorder(BorderFactory.createTitledBorder(Messages.getString("GameSettingFrame.TITLE_KEYS"))); //$NON-NLS-1$
			Dkeys.setLayout(new BoxLayout(Dkeys,BoxLayout.PAGE_AXIS));
			panel3.add(Dkeys);
			JCheckBox ESC = new JCheckBox(
					Messages.getString("GameSettingFrame.KEY_ENDGAME"),LGM.currentFile.LetEscEndGame); //$NON-NLS-1$
			JCheckBox F1 = new JCheckBox(
					Messages.getString("GameSettingFrame.KEY_INFO"),LGM.currentFile.LetF1ShowGameInfo); //$NON-NLS-1$
			JCheckBox F4 = new JCheckBox(Messages.getString("GameSettingFrame.KEY_SWITCHFULLSCREEN"), //$NON-NLS-1$
					LGM.currentFile.LetF4SwitchFullscreen);
			JCheckBox F5 = new JCheckBox(Messages.getString("GameSettingFrame.SAVELOAD"), //$NON-NLS-1$
					LGM.currentFile.LetF5SaveF6Load);
			Dkeys.add(ESC);
			Dkeys.add(F1);
			Dkeys.add(F4);
			Dkeys.add(F5);
			JPanel GPP = new JPanel();
			GPP.setBorder(BorderFactory.createTitledBorder(Messages.getString("GameSettingFrame.TITLE_PRIORITY"))); //$NON-NLS-1$
			GPP.setLayout(new BoxLayout(GPP,BoxLayout.PAGE_AXIS));
			panel3.add(GPP);

			ButtonGroup group = new ButtonGroup();
			JRadioButton option;
			option = new JRadioButton(Messages.getString("GameSettingFrame.PRIORITY_NORMAL")); //$NON-NLS-1$
			group.add(option);
			GPP.add(option);
			option = new JRadioButton(Messages.getString("GameSettingFrame.PRIORITY_HIGH")); //$NON-NLS-1$
			group.add(option);
			GPP.add(option);
			option = new JRadioButton(Messages.getString("GameSettingFrame.PRIORITY_HIHGEST")); //$NON-NLS-1$
			group.add(option);
			GPP.add(option);
			}

		JComponent panel4 = new JPanel(false);
		tabbedPane
				.addTab(
						Messages.getString("GameSettingFrame.TAB_LOADING"),null,panel4,Messages.getString("GameSettingFrame.HINT_LOADING")); //$NON-NLS-1$ //$NON-NLS-2$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		JComponent panel5 = new JPanel(false);
		tabbedPane
				.addTab(
						Messages.getString("GameSettingFrame.TAB_CONSTANTS"),null,panel5,Messages.getString("GameSettingFrame.HINT_CONSTANTS")); //$NON-NLS-1$ //$NON-NLS-2$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		JComponent panel6 = new JPanel(false);
		tabbedPane
				.addTab(
						Messages.getString("GameSettingFrame.TAB_INCLUDE"),null,panel6,Messages.getString("GameSettingFrame.HINT_INCLUDE")); //$NON-NLS-1$ //$NON-NLS-2$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

			// error tab
			{
			JComponent panel7 = new JPanel(false);
			tabbedPane
					.addTab(
							Messages.getString("GameSettingFrame.TAB_ERRORS"),null,panel7,Messages.getString("GameSettingFrame.HINT_ERRORS")); //$NON-NLS-1$ //$NON-NLS-2$
			tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);
			panel7.setLayout(new BoxLayout(panel7,BoxLayout.PAGE_AXIS));
			JCheckBox DEM = new JCheckBox(
					Messages.getString("GameSettingFrame.ERRORS_DISPLAY"),LGM.currentFile.DisplayErrors); //$NON-NLS-1$
			JCheckBox WGE = new JCheckBox(
					Messages.getString("GameSettingFrame.ERRORS_LOG"),LGM.currentFile.WriteToLog); //$NON-NLS-1$
			JCheckBox Abort = new JCheckBox(
					Messages.getString("GameSettingFrame.ERRORS_ABORT"),LGM.currentFile.AbortOnError); //$NON-NLS-1$
			JCheckBox TUV0 = new JCheckBox(
					Messages.getString("GameSettingFrame.UNINITZERO"),LGM.currentFile.TreatUninitializedAs0); //$NON-NLS-1$
			panel7.add(DEM);
			panel7.add(WGE);
			panel7.add(Abort);
			panel7.add(TUV0);
			}

			{
			JComponent panel8 = new JPanel(false);
			tabbedPane
					.addTab(
							Messages.getString("GameSettingFrame.TAB_INFO"),null,panel8,Messages.getString("GameSettingFrame.HINT_INFO")); //$NON-NLS-1$ //$NON-NLS-2$
			tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);
			panel8.setLayout(new FlowLayout());
			JLabel label = new JLabel(Messages.getString("GameSettingFrame.AUTHOR")); //$NON-NLS-1$
			label.setPreferredSize(new Dimension(80,25));
			panel8.add(label);
			JTextField box = new JTextField(LGM.currentFile.Author);
			box.setPreferredSize(new Dimension(390,25));
			panel8.add(box);
			label = new JLabel(Messages.getString("GameSettingFrame.VERSION")); //$NON-NLS-1$
			label.setPreferredSize(new Dimension(80,25));
			panel8.add(label);
			box = new JTextField("" + LGM.currentFile.Version);
			box.setPreferredSize(new Dimension(390,25));
			panel8.add(box);
			label = new JLabel(Messages.getString("GameSettingFrame.LASTCHANGED")); //$NON-NLS-1$
			label.setPreferredSize(new Dimension(80,25));
			panel8.add(label);
			box = new JTextField(Gm6File.gmTimeToString(LGM.currentFile.LastChanged));
			box.setPreferredSize(new Dimension(390,25));
			box.setEditable(false);
			panel8.add(box);
			label = new JLabel(Messages.getString("GameSettingFrame.INFORMATION")); //$NON-NLS-1$
			label.setPreferredSize(new Dimension(70,25));
			panel8.add(label);
			JTextArea boxa = new JTextArea(LGM.currentFile.Information);
			boxa.setPreferredSize(new Dimension(500,200));
			boxa.setLineWrap(true);
			panel8.add(boxa);
			}

		JButton okButton = new JButton(Messages.getString("GameSettingFrame.BUTTON_SAVE")); //$NON-NLS-1$
		getContentPane().add(okButton);
		JButton cancelButton = new JButton(Messages.getString("GameSettingFrame.BUTTON_DISCARD")); //$NON-NLS-1$
		add(cancelButton);

		}

	public void actionPerformed(ActionEvent arg0)
		{
		// unused
		}
	}