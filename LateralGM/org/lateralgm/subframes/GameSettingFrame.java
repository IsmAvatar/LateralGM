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

import static org.lateralgm.main.Util.addDim;
import static org.lateralgm.subframes.ResourceFrame.addGap;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.CustomFileFilter;
import org.lateralgm.components.IndexButtonGroup;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.MDIFrame;
import org.lateralgm.file.Gm6File;
import org.lateralgm.file.GmStreamDecoder;
import org.lateralgm.file.GmStreamEncoder;
import org.lateralgm.file.iconio.BitmapHeader;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.sub.Constant;

public class GameSettingFrame extends MDIFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public JTabbedPane tabbedPane = new JTabbedPane();

	public JCheckBox startFullscreen;
	public IndexButtonGroup scaling;
	public IntegerField scale;
	public JCheckBox interpolatecolors;
	public ColorSelect colorbutton;
	public JCheckBox resizeWindow;
	public JCheckBox stayOnTop;
	public JCheckBox drawBorderedWindow;
	public JCheckBox drawButtonsCaption;
	public JCheckBox displayMouse;
	public JCheckBox freezeGame;

	private JPanel makeGraphicsPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		String t = Messages.getString("GameSettingFrame.FULLSCREEN"); //$NON-NLS-1$
		startFullscreen = new JCheckBox(t,LGM.currentFile.startFullscreen);

		JPanel scalegroup = makeTitledPanel(Messages.getString("GameSettingFrame.SCALING_TITLE"),250, //$NON-NLS-1$
				120);
		scalegroup.setAlignmentX(0f);
		scaling = new IndexButtonGroup(3,true,false,this);
		JRadioButton option;

		JPanel fixed = new JPanel();
		fixed.setPreferredSize(new Dimension(240,26));
		option = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_FIXED")); //$NON-NLS-1$
		option.setPreferredSize(new Dimension(142,16));
		scaling.add(option,1);
		fixed.add(option,"CENTER"); //$NON-NLS-1$
		scale = new IntegerField(1,999,100);
		scale.setPreferredSize(new Dimension(50,20));
		fixed.add(scale,"EAST"); //$NON-NLS-1$
		scalegroup.add(fixed);

		option = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_RATIO")); //$NON-NLS-1$
		option.setPreferredSize(new Dimension(200,16));
		scaling.add(option,-1);
		scalegroup.add(option);

		option = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_FULL")); //$NON-NLS-1$
		option.setPreferredSize(new Dimension(200,16));
		scaling.add(option,0);
		scalegroup.add(option);

		int s = LGM.currentFile.scaling;
		scaling.setValue(s > 1 ? 1 : s);
		if (s > 1) scale.setIntValue(s);
		scale.setEnabled(s > 0);

		t = Messages.getString("GameSettingFrame.INTERPOLATE"); //$NON-NLS-1$
		interpolatecolors = new JCheckBox(t,LGM.currentFile.interpolate);
		JLabel backcolor = new JLabel(Messages.getString("GameSettingFrame.BACKCOLOR")); //$NON-NLS-1$
		t = Messages.getString("GameSettingFrame.SETCOLOR"); //$NON-NLS-1$
		colorbutton = new ColorSelect(LGM.currentFile.colorOutsideRoom,LGM.frame);
		colorbutton.setMaximumSize(new Dimension(100,20));
		colorbutton.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		colorbutton.setAlignmentX(0f);

		t = Messages.getString("GameSettingFrame.RESIZE"); //$NON-NLS-1$
		resizeWindow = new JCheckBox(t,LGM.currentFile.allowWindowResize);
		t = Messages.getString("GameSettingFrame.STAYONTOP"); //$NON-NLS-1$
		stayOnTop = new JCheckBox(t,LGM.currentFile.alwaysOnTop);
		t = Messages.getString("GameSettingFrame.NOBORDER"); //$NON-NLS-1$
		drawBorderedWindow = new JCheckBox(t,LGM.currentFile.dontDrawBorder);
		t = Messages.getString("GameSettingFrame.NOBUTTONS"); //$NON-NLS-1$
		drawButtonsCaption = new JCheckBox(t,LGM.currentFile.dontShowButtons);
		t = Messages.getString("GameSettingFrame.DISPLAYCURSOR"); //$NON-NLS-1$
		displayMouse = new JCheckBox(t,LGM.currentFile.displayCursor);
		t = Messages.getString("GameSettingFrame.FREEZE"); //$NON-NLS-1$
		freezeGame = new JCheckBox(t,LGM.currentFile.freezeOnLoseFocus);
		panel.add(startFullscreen);
		panel.add(scalegroup);
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

	public JCheckBox synchronised;
	public JCheckBox setResolution;
	public IndexButtonGroup colourDepth;
	public IndexButtonGroup resolution;
	public IndexButtonGroup frequency;
	public JPanel resolutionPane;

	private JPanel makeResolutionPane()
		{
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		synchronised = new JCheckBox(Messages.getString("GameSettingFrame.USE_SYNC"), //$NON-NLS-1$
				LGM.currentFile.useSynchronization);
		addDim(panel,synchronised,516,16);
		addGap(panel,450,20);
		setResolution = new JCheckBox(
				Messages.getString("GameSettingFrame.SET_RESOLUTION"),LGM.currentFile.setResolution); //$NON-NLS-1$
		setResolution.addActionListener(this);
		addDim(panel,setResolution,516,16);
		addGap(panel,450,10);

		resolutionPane = new JPanel();
		resolutionPane.setPreferredSize(new Dimension(480,700));

		JPanel depth = makeRadioPanel(Messages.getString("GameSettingFrame.TITLE_COLOR_DEPTH"),150,200); //$NON-NLS-1$
		IndexButtonGroup group = new IndexButtonGroup(3,true,false);
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.NO_CHANGE"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.16_BIT"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.32_BIT"))); //$NON-NLS-1$
		group.setValue(LGM.currentFile.colorDepth);
		group.populate(depth);
		resolutionPane.add(depth);

		JPanel res = makeRadioPanel(Messages.getString("GameSettingFrame.TITLE_RESOLUTION"),150,200); //$NON-NLS-1$
		group = new IndexButtonGroup(7,true,false);
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.NO_CHANGE"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.320X240"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.640X480"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.800X600"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.1024X768"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.1280X1024"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.1600X1200"))); //$NON-NLS-1$
		group.setValue(LGM.currentFile.resolution);
		group.populate(res);
		resolutionPane.add(res);

		JPanel freq = makeRadioPanel(Messages.getString("GameSettingFrame.TITLE_FREQUENCY"),150,200); //$NON-NLS-1$
		group = new IndexButtonGroup(6,true,false);
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.NO_CHANGE"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.60HZ"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.70HZ"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.85HZ"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.100HZ"))); //$NON-NLS-1$
		group.add(new JRadioButton(Messages.getString("GameSettingFrame.120HZ"))); //$NON-NLS-1$
		group.setValue(LGM.currentFile.frequency);
		group.populate(freq);
		resolutionPane.add(freq);

		panel.add(resolutionPane);
		resolutionPane.setVisible(setResolution.isSelected());

		return panel;
		}

	public JCheckBox esc;
	public JCheckBox f1;
	public JCheckBox f4;
	public JCheckBox f5;
	public IndexButtonGroup gamePriority;

	private JPanel makeOtherPane()
		{
		JPanel panel = new JPanel(new FlowLayout());
		String t = Messages.getString("GameSettingFrame.TITLE_KEYS"); //$NON-NLS-1$
		addGap(panel,450,10);
		JPanel dKeys = makeRadioPanel(t,480,150);
		panel.add(dKeys);

		t = Messages.getString("GameSettingFrame.KEY_ENDGAME"); //$NON-NLS-1$
		esc = new JCheckBox(t,LGM.currentFile.letEscEndGame);
		t = Messages.getString("GameSettingFrame.KEY_INFO"); //$NON-NLS-1$
		f1 = new JCheckBox(t,LGM.currentFile.letF1ShowGameInfo);
		t = Messages.getString("GameSettingFrame.KEY_SWITCHFULLSCREEN"); //$NON-NLS-1$
		f4 = new JCheckBox(t,LGM.currentFile.letF4SwitchFullscreen);
		t = Messages.getString("GameSettingFrame.SAVELOAD"); //$NON-NLS-1$
		f5 = new JCheckBox(t,LGM.currentFile.letF5SaveF6Load);
		dKeys.add(esc);
		dKeys.add(f1);
		dKeys.add(f4);
		dKeys.add(f5);

		t = Messages.getString("GameSettingFrame.TITLE_PRIORITY"); //$NON-NLS-1$
		JPanel gpp = makeRadioPanel(t,480,120);
		panel.add(gpp);

		gamePriority = new IndexButtonGroup(3,true,false);
		JRadioButton option;
		t = Messages.getString("GameSettingFrame.PRIORITY_NORMAL"); //$NON-NLS-1$
		option = new JRadioButton(t);
		gamePriority.add(option);
		t = Messages.getString("GameSettingFrame.PRIORITY_HIGH"); //$NON-NLS-1$
		option = new JRadioButton(t);
		gamePriority.add(option);
		t = Messages.getString("GameSettingFrame.PRIORITY_HIHGEST"); //$NON-NLS-1$
		option = new JRadioButton(t);
		gamePriority.add(option);
		gamePriority.populate(gpp);
		gamePriority.setValue(LGM.currentFile.gamePriority);

		return panel;
		}

	public JCheckBox useCustomLoad;
	public BufferedImage customLoad;
	public JButton changeCustomLoad;
	public JCheckBox transparent;
	public IntegerField loadAlpha;
	public IndexButtonGroup progBarMode;
	public JButton backLoad;
	public JButton frontLoad;
	public BufferedImage backLoadImage;
	public BufferedImage frontLoadImage;
	public JCheckBox scaleProgBar;
	public JLabel iconPreview;
	public BufferedImage icon;
	public byte[] iconData;
	public JButton changeIcon;
	public IntegerField gameId;
	public JButton randomise;

	private JPanel makeLoadingPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		JPanel loadImage = new JPanel(new FlowLayout());
		loadImage.setPreferredSize(new Dimension(480,120));
		loadImage.setBorder(BorderFactory.createTitledBorder(Messages
				.getString("GameSettingFrame.TITLE_LOADING_IMAGE"))); //$NON-NLS-1$

		useCustomLoad = new JCheckBox(
				Messages.getString("GameSettingFrame.CUSTOM_LOAD_IMAGE"),LGM.currentFile.showCustomLoadImage); //$NON-NLS-1$
		useCustomLoad.addActionListener(this);
		addDim(loadImage,useCustomLoad,200,16);
		customLoad = LGM.currentFile.loadingImage;

		changeCustomLoad = new JButton(Messages.getString("GameSettingFrame.CHANGE_IMAGE")); //$NON-NLS-1$
		changeCustomLoad.setEnabled(useCustomLoad.isSelected());
		changeCustomLoad.addActionListener(this);
		addDim(loadImage,changeCustomLoad,120,24);

		addGap(loadImage,130,16);

		transparent = new JCheckBox(Messages.getString("GameSettingFrame.MAKE_TRANSPARENT"), //$NON-NLS-1$
				LGM.currentFile.imagePartiallyTransparent);
		addDim(loadImage,transparent,460,16);
		JLabel lab = new JLabel(Messages.getString("GameSettingFrame.ALPHA_TRANSPARENCY")); //$NON-NLS-1$
		addDim(loadImage,lab,120,16);
		loadAlpha = new IntegerField(0,255,LGM.currentFile.loadImageAlpha);
		addDim(loadImage,loadAlpha,50,20);
		addGap(loadImage,270,16);
		panel.add(loadImage);

		JPanel progBar = makeTitledPanel(Messages
				.getString("GameSettingFrame.TITLE_LOADING_PROGRESS_BAR"),480,150); //$NON-NLS-1$
		progBarMode = new IndexButtonGroup(3,true,false,this);
		JRadioButton but = new JRadioButton(Messages.getString("GameSettingFrame.NO_PROGRESS_BAR")); //$NON-NLS-1$
		progBarMode.add(but);
		addDim(progBar,but,460,16);
		but = new JRadioButton(Messages.getString("GameSettingFrame.DEF_PROGRESS_BAR")); //$NON-NLS-1$
		progBarMode.add(but);
		addDim(progBar,but,460,16);
		but = new JRadioButton(Messages.getString("GameSettingFrame.CUSTOM_PROGRESS_BAR")); //$NON-NLS-1$
		progBarMode.add(but);
		addDim(progBar,but,460,16);
		progBarMode.setValue(LGM.currentFile.loadBarMode);

		backLoad = new JButton(Messages.getString("GameSettingFrame.BACK_IMAGE")); //$NON-NLS-1$
		backLoad.addActionListener(this);
		backLoadImage = LGM.currentFile.backLoadBar;
		addDim(progBar,backLoad,120,24);
		frontLoad = new JButton(Messages.getString("GameSettingFrame.FRONT_IMAGE")); //$NON-NLS-1$
		frontLoad.addActionListener(this);
		frontLoadImage = LGM.currentFile.frontLoadBar;
		addDim(progBar,frontLoad,120,24);
		addGap(progBar,180,20);
		backLoad.setEnabled(progBarMode.getValue() == Gm6File.LOADBAR_CUSTOM);
		frontLoad.setEnabled(backLoad.isEnabled());

		scaleProgBar = new JCheckBox(
				Messages.getString("GameSettingFrame.SCALE_IMAGE"),LGM.currentFile.scaleProgressBar); //$NON-NLS-1$
		addDim(progBar,scaleProgBar,460,16);
		panel.add(progBar);

		icon = LGM.currentFile.gameIcon;
		iconData = LGM.currentFile.gameIconData;
		iconPreview = new JLabel(Messages.getString("GameSettingFrame.GAME_ICON")); //$NON-NLS-1$
		if (LGM.currentFile.gameIcon != null) iconPreview.setIcon(new ImageIcon(icon));
		iconPreview.setHorizontalTextPosition(SwingConstants.LEFT);
		addDim(panel,iconPreview,140,40);
		changeIcon = new JButton(Messages.getString("GameSettingFrame.CHANGE_ICON")); //$NON-NLS-1$
		changeIcon.addActionListener(this);
		addDim(panel,changeIcon,120,24);

		addGap(panel,200,16);

		lab = new JLabel(Messages.getString("GameSettingFrame.GAME_ID")); //$NON-NLS-1$
		addDim(panel,lab,50,16);

		gameId = new IntegerField(0,100000000,LGM.currentFile.gameId);
		addDim(panel,gameId,70,20);
		addGap(panel,5,16);
		randomise = new JButton(Messages.getString("GameSettingFrame.RANDOMIZE")); //$NON-NLS-1$
		randomise.addActionListener(this);
		addDim(panel,randomise,120,24);
		addGap(panel,195,16);

		return panel;
		}

	public JButton importBut;
	public JButton exportBut;
	public JTable constants;
	public ConstantsTableModel cModel;
	public JButton add;
	public JButton insert;
	public JButton delete;
	public JButton clear;
	public JButton up;
	public JButton down;
	public JButton sort;

	//TODO: Prevent duplicate constants
	private JPanel makeConstantsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());
		importBut = new JButton(Messages.getString("GameSettingFrame.IMPORT")); //$NON-NLS-1$
		importBut.addActionListener(this);
		addDim(panel,importBut,80,24);
		exportBut = new JButton(Messages.getString("GameSettingFrame.EXPORT")); //$NON-NLS-1$
		exportBut.addActionListener(this);
		addDim(panel,exportBut,80,24);
		addGap(panel,450,5);

		cModel = new ConstantsTableModel(LGM.currentFile);
		constants = new JTable(cModel);
		JScrollPane scroll = new JScrollPane(constants);
		addDim(panel,scroll,450,260);
		constants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add = new JButton(Messages.getString("GameSettingFrame.ADD")); //$NON-NLS-1$
		add.addActionListener(this);
		addDim(panel,add,100,24);
		delete = new JButton(Messages.getString("GameSettingFrame.DELETE")); //$NON-NLS-1$
		delete.addActionListener(this);
		addDim(panel,delete,100,24);
		addGap(panel,60,20);
		up = new JButton(Messages.getString("GameSettingFrame.UP")); //$NON-NLS-1$
		up.addActionListener(this);
		addDim(panel,up,100,24);
		sort = new JButton(Messages.getString("GameSettingFrame.SORT")); //$NON-NLS-1$
		sort.addActionListener(this);
		addDim(panel,sort,100,24);
		insert = new JButton(Messages.getString("GameSettingFrame.INSERT")); //$NON-NLS-1$
		insert.addActionListener(this);
		addDim(panel,insert,100,24);
		clear = new JButton(Messages.getString("GameSettingFrame.CLEAR")); //$NON-NLS-1$
		clear.addActionListener(this);
		addDim(panel,clear,100,24);
		addGap(panel,60,20);
		down = new JButton(Messages.getString("GameSettingFrame.DOWN")); //$NON-NLS-1$
		down.addActionListener(this);
		addDim(panel,down,100,24);
		addGap(panel,100,24);

		return panel;
		}

	private class ConstantsTableModel extends AbstractTableModel
		{
		private static final long serialVersionUID = 1L;
		ArrayList<Constant> constants;

		ConstantsTableModel(Gm6File file)
			{
			constants = new ArrayList<Constant>();
			for (Constant c : file.constants)
				constants.add(c.copy());
			}

		public int getColumnCount()
			{
			return 2;
			}

		public int getRowCount()
			{
			return constants.size();
			}

		public Object getValueAt(int rowIndex, int columnIndex)
			{
			Constant c = constants.get(rowIndex);
			if (columnIndex == 0)
				return c.name;
			else
				return c.value;
			}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
			Constant c = constants.get(rowIndex);
			if (columnIndex == 0)
				c.name = aValue.toString();
			else
				c.value = aValue.toString();
			}

		public boolean isCellEditable(int row, int col)
			{
			return true;
			}

		public String getColumnName(int column)
			{
			if (column == 0)
				return Messages.getString("GameSettingFrame.NAME"); //$NON-NLS-1$
			else
				return Messages.getString("GameSettingFrame.VALUE"); //$NON-NLS-1$
			}
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
		box = new JTextField("" + LGM.currentFile.version); //$NON-NLS-1$
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

	public JButton saveButton;
	public JButton discardButton;

	public GameSettingFrame()
		{
		super(Messages.getString("GameSettingFrame.TITLE"),true,true,true,true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(540,470);
		setFrameIcon(LGM.findIcon("restree/gm.png")); //$NON-NLS-1$
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
		saveButton = new JButton(t);
		saveButton.addActionListener(this);
		//getContentPane().add(saveButton);
		add(saveButton);
		t = Messages.getString("GameSettingFrame.BUTTON_DISCARD"); //$NON-NLS-1$
		discardButton = new JButton(t);
		discardButton.addActionListener(this);
		add(discardButton);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == saveButton)
			{
			commitChanges();
			setVisible(false);
			return;
			}

		if (e.getSource() == discardButton)
			{
			setVisible(false);
			return;
			}

		switch (tabbedPane.getSelectedIndex())
			{
			case 0:
				if (e.getSource() instanceof JRadioButton)
					{
					scale.setEnabled(scaling.getValue() > 0);
					return;
					}
			case 1:
				resolutionPane.setVisible(setResolution.isSelected());
				return;
			case 3:
				loadActionPerformed(e);
				return;
			case 4:
				constantsActionPerformed(e);
				return;
			default:
				return;
			}
		}

	private void loadActionPerformed(ActionEvent e)
		{
		if (e.getSource() == useCustomLoad)
			{
			changeCustomLoad.setEnabled(useCustomLoad.isSelected());
			}
		else if (e.getSource() == changeCustomLoad)
			{
			try
				{
				customLoad = Util.getValidImage();
				}
			catch (Throwable ex)
				{
				JOptionPane.showMessageDialog(LGM.frame,Messages
						.getString("GameSettingFrame.ERROR_LOADING_IMAGE")); //$NON-NLS-1$
				}
			}
		else if (e.getSource() instanceof JRadioButton)
			{
			backLoad.setEnabled(progBarMode.getValue() == Gm6File.LOADBAR_CUSTOM);
			frontLoad.setEnabled(backLoad.isEnabled());
			}
		else if (e.getSource() == backLoad)
			{
			BufferedImage img = Util.getValidImage();
			if (img != null) backLoadImage = img;
			}
		else if (e.getSource() == frontLoad)
			{
			BufferedImage img = Util.getValidImage();
			if (img != null) frontLoadImage = img;
			}
		else if (e.getSource() == changeIcon)
			{
			JFileChooser fc = new JFileChooser();
			fc
					.setFileFilter(new CustomFileFilter(
							".ico",Messages.getString("GameSettingFrame.ICO_FILES"))); //$NON-NLS-1$ //$NON-NLS-2$
			if (fc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
				{
				File f = fc.getSelectedFile();
				if (f.exists())
					try
						{
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));

						ICOFile i = new ICOFile(in);
						if (i.getImageCount() != 1)
							{
							JOptionPane
									.showMessageDialog(
											LGM.frame,
											Messages.getString("GameSettingFrame.INVALID_ICON"),Messages.getString("GameSettingFrame.TITLE_ERROR"), //$NON-NLS-1$ //$NON-NLS-2$
											JOptionPane.ERROR_MESSAGE);
							return;
							}
						BitmapHeader d = i.getDescriptor(0).getHeader();
						if (d.getWidth() != 32 || d.getHeight() != 64)
							{
							JOptionPane
									.showMessageDialog(
											LGM.frame,
											Messages.getString("GameSettingFrame.INVALID_ICON"),Messages.getString("GameSettingFrame.TITLE_ERROR"), //$NON-NLS-1$ //$NON-NLS-2$
											JOptionPane.ERROR_MESSAGE);
							return;
							}

						icon = i.getDescriptor(0).getBitmap().createImageRGB();
						iconPreview.setIcon(new ImageIcon(icon));

						//ICOFile closes the stream when it's done
						in = new BufferedInputStream(new FileInputStream(f));
						ByteArrayOutputStream dat = new ByteArrayOutputStream();

						int val = in.read();
						while (val != -1)
							{
							dat.write(val);
							val = in.read();
							}
						iconData = dat.toByteArray();
						}
					catch (FileNotFoundException e1)
						{
						e1.printStackTrace();
						}
					catch (IOException ex)
						{
						ex.printStackTrace();
						}
				}
			}
		else if (e.getSource() == randomise)
			{
			gameId.setIntValue(new Random().nextInt(100000001));
			}
		}

	private void constantsActionPerformed(ActionEvent e)
		{
		if (e.getSource() == importBut)
			{
			importConstants();
			return;
			}
		if (e.getSource() == exportBut)
			{
			exportConstants();
			return;
			}
		if (e.getSource() == add)
			{
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			cModel.constants.add(new Constant());
			int row = cModel.constants.size() - 1;
			cModel.fireTableRowsInserted(row,row);
			constants.getSelectionModel().setSelectionInterval(row,row);
			return;
			}
		if (e.getSource() == insert)
			{
			if (constants.getSelectedRow() == -1) return;
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			cModel.constants.add(constants.getSelectedRow(),new Constant());
			cModel.fireTableRowsInserted(constants.getSelectedRow(),constants.getSelectedRow());
			constants.getSelectionModel().setSelectionInterval(0,constants.getSelectedRow() - 1);
			return;
			}
		if (e.getSource() == delete)
			{
			if (constants.getSelectedRow() == -1) return;
			int row = constants.getSelectedRow();
			cModel.constants.remove(row);
			cModel.fireTableRowsDeleted(row,row);
			if (cModel.constants.size() > 0)
				constants.getSelectionModel().setSelectionInterval(0,
						Math.min(row,cModel.constants.size() - 1));
			return;
			}
		if (e.getSource() == clear)
			{
			if (cModel.constants.size() == 0) return;
			int last = cModel.constants.size() - 1;
			cModel.constants.clear();
			cModel.fireTableRowsDeleted(0,last);
			return;
			}
		if (e.getSource() == up)
			{
			int row = constants.getSelectedRow();
			if (row <= 0) return;
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();

			Constant c = cModel.constants.get(row - 1);
			cModel.constants.set(row - 1,cModel.constants.get(row));
			cModel.constants.set(row,c);
			cModel.fireTableDataChanged();
			constants.getSelectionModel().setSelectionInterval(0,row - 1);
			return;
			}
		if (e.getSource() == down)
			{
			int row = constants.getSelectedRow();
			if (row == -1 || row >= cModel.constants.size() - 1) return;
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			Constant c = cModel.constants.get(row + 1);
			cModel.constants.set(row + 1,cModel.constants.get(row));
			cModel.constants.set(row,c);
			cModel.fireTableDataChanged();
			constants.getSelectionModel().setSelectionInterval(0,row + 1);
			return;
			}
		if (e.getSource() == sort)
			{
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			Collections.sort(cModel.constants);
			cModel.fireTableDataChanged();
			if (cModel.constants.size() > 0) constants.getSelectionModel().setSelectionInterval(0,0);
			return;
			}
		}

	private void importConstants()
		{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(".lgc",Messages.getString("GameSettingFrame.LGC_FILES"))); //$NON-NLS-1$ //$NON-NLS-2$
		if (fc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			GmStreamDecoder in = null;
			try
				{
				File f = fc.getSelectedFile();
				if (f == null || !f.exists()) throw new Exception();

				in = new GmStreamDecoder(f);
				if (in.read3() != ('L' | ('G' << 8) | ('C' << 16))) throw new Exception();
				int count = in.read2();
				for (int i = 0; i < count; i++)
					{
					Constant c = new Constant();
					c.name = in.readStr1();
					c.value = in.readStr1();
					if (!cModel.constants.contains(c)) cModel.constants.add(c);
					}
				cModel.fireTableDataChanged();
				if (cModel.constants.size() > 0) constants.getSelectionModel().setSelectionInterval(0,0);
				}
			catch (Exception ex)
				{
				JOptionPane
						.showMessageDialog(
								LGM.frame,
								Messages.getString("GameSettingFrame.ERROR_IMPORTING_CONSTANTS"),Messages.getString("GameSettingFrame.TITLE_ERROR"), //$NON-NLS-1$ //$NON-NLS-2$
								JOptionPane.ERROR_MESSAGE);
				}
			finally
				{
				if (in != null) try
					{
					in.close();
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				}
			}
		}

	private void exportConstants()
		{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(".lgc",Messages.getString("GameSettingFrame.LGC_FILES"))); //$NON-NLS-1$ //$NON-NLS-2$

		while (fc.showSaveDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			File f = fc.getSelectedFile();
			if (f == null) return;
			if (!f.getPath().endsWith(".lgc")) f = new File(f.getPath() + ".lgc"); //$NON-NLS-1$ //$NON-NLS-2$
			int result = 0;
			if (f.exists())
				{
				result = JOptionPane
						.showConfirmDialog(
								LGM.frame,
								Messages.getString("GameSettingFrame.REPLACE_FILE"),Messages.getString("GameSettingFrame.TITLE_REPLACE_FILE"), //$NON-NLS-1$ //$NON-NLS-2$
								JOptionPane.YES_NO_CANCEL_OPTION);
				}
			if (result == 2) return;
			if (result == 1) continue;

			GmStreamEncoder out = null;
			try
				{
				out = new GmStreamEncoder(f);
				out.write('L');
				out.write('G');
				out.write('C');
				out.write2(cModel.constants.size());
				for (Constant c : cModel.constants)
					{
					out.writeStr1(c.name);
					out.writeStr1(c.value);
					}
				}
			catch (FileNotFoundException e1)
				{
				e1.printStackTrace();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			finally
				{
				if (out != null) try
					{
					out.close();
					}
				catch (IOException ex)
					{
					ex.printStackTrace();
					}
				}
			return;
			}
		}

	//TODO:
	public void commitChanges()
		{
		}

	private JPanel makeRadioPanel(String paneTitle, int width, int height)
		{
		JPanel panel = makeTitledPanel(paneTitle,width,height);
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		return panel;
		}

	private JPanel makeTitledPanel(String paneTitle, int width, int height)
		{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(paneTitle));
		Dimension newSize = new Dimension(width,height);
		panel.setPreferredSize(newSize);
		panel.setMaximumSize(newSize);
		panel.setMinimumSize(newSize);
		return panel;
		}
	}
