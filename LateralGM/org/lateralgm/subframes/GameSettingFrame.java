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
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
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
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.file.Gm6File;
import org.lateralgm.file.GmStreamDecoder;
import org.lateralgm.file.GmStreamEncoder;
import org.lateralgm.file.iconio.BitmapHeader;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.Include;
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
	public JCheckBox noWindowBorder;
	public JCheckBox noWindowButtons;
	public JCheckBox displayMouse;
	public JCheckBox freezeGame;

	private JPanel makeGraphicsPane()
		{
		GameSettings g = LGM.currentFile.gameSettings;
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		String t = Messages.getString("GameSettingFrame.FULLSCREEN"); //$NON-NLS-1$
		startFullscreen = new JCheckBox(t,g.startFullscreen);

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

		int s = g.scaling;
		scaling.setValue(s > 1 ? 1 : s);
		if (s > 1) scale.setIntValue(s);
		scale.setEnabled(s > 0);

		t = Messages.getString("GameSettingFrame.INTERPOLATE"); //$NON-NLS-1$
		interpolatecolors = new JCheckBox(t,g.interpolate);

		JLabel backcolor = new JLabel(Messages.getString("GameSettingFrame.BACKCOLOR")); //$NON-NLS-1$
		/*
		 * XXX: should this be on the same line as its JLabel?
		 */
		colorbutton = new ColorSelect(g.colorOutsideRoom);
		colorbutton.setMaximumSize(new Dimension(100,20));
		colorbutton.setAlignmentX(0f);

		t = Messages.getString("GameSettingFrame.RESIZE"); //$NON-NLS-1$
		resizeWindow = new JCheckBox(t,g.allowWindowResize);
		t = Messages.getString("GameSettingFrame.STAYONTOP"); //$NON-NLS-1$
		stayOnTop = new JCheckBox(t,g.alwaysOnTop);
		t = Messages.getString("GameSettingFrame.NOBORDER"); //$NON-NLS-1$
		noWindowBorder = new JCheckBox(t,g.dontDrawBorder);
		t = Messages.getString("GameSettingFrame.NOBUTTONS"); //$NON-NLS-1$
		noWindowButtons = new JCheckBox(t,g.dontShowButtons);
		t = Messages.getString("GameSettingFrame.DISPLAYCURSOR"); //$NON-NLS-1$
		displayMouse = new JCheckBox(t,g.displayCursor);
		t = Messages.getString("GameSettingFrame.FREEZE"); //$NON-NLS-1$
		freezeGame = new JCheckBox(t,g.freezeOnLoseFocus);
		panel.add(startFullscreen);
		panel.add(scalegroup);
		panel.add(interpolatecolors);
		panel.add(backcolor);
		panel.add(colorbutton);
		panel.add(resizeWindow);
		panel.add(stayOnTop);
		panel.add(noWindowBorder);
		panel.add(noWindowButtons);
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
		GameSettings g = LGM.currentFile.gameSettings;
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		synchronised = new JCheckBox(Messages.getString("GameSettingFrame.USE_SYNC"), //$NON-NLS-1$
				g.useSynchronization);
		addDim(panel,synchronised,516,16);
		addGap(panel,450,20);
		setResolution = new JCheckBox(
				Messages.getString("GameSettingFrame.SET_RESOLUTION"),g.setResolution); //$NON-NLS-1$
		setResolution.addActionListener(this);
		addDim(panel,setResolution,516,16);
		addGap(panel,450,10);

		resolutionPane = new JPanel();
		resolutionPane.setPreferredSize(new Dimension(480,700));

		JPanel depth = makeRadioPanel(Messages.getString("GameSettingFrame.TITLE_COLOR_DEPTH"),150,200); //$NON-NLS-1$
		colourDepth = new IndexButtonGroup(3,true,false);
		colourDepth.add(new JRadioButton(Messages.getString("GameSettingFrame.NO_CHANGE"))); //$NON-NLS-1$
		colourDepth.add(new JRadioButton(Messages.getString("GameSettingFrame.16_BIT"))); //$NON-NLS-1$
		colourDepth.add(new JRadioButton(Messages.getString("GameSettingFrame.32_BIT"))); //$NON-NLS-1$
		colourDepth.setValue(g.colorDepth);
		colourDepth.populate(depth);
		resolutionPane.add(depth);

		JPanel res = makeRadioPanel(Messages.getString("GameSettingFrame.TITLE_RESOLUTION"),150,200); //$NON-NLS-1$
		resolution = new IndexButtonGroup(7,true,false);
		resolution.add(new JRadioButton(Messages.getString("GameSettingFrame.NO_CHANGE"))); //$NON-NLS-1$
		resolution.add(new JRadioButton(Messages.getString("GameSettingFrame.320X240"))); //$NON-NLS-1$
		resolution.add(new JRadioButton(Messages.getString("GameSettingFrame.640X480"))); //$NON-NLS-1$
		resolution.add(new JRadioButton(Messages.getString("GameSettingFrame.800X600"))); //$NON-NLS-1$
		resolution.add(new JRadioButton(Messages.getString("GameSettingFrame.1024X768"))); //$NON-NLS-1$
		resolution.add(new JRadioButton(Messages.getString("GameSettingFrame.1280X1024"))); //$NON-NLS-1$
		resolution.add(new JRadioButton(Messages.getString("GameSettingFrame.1600X1200"))); //$NON-NLS-1$
		resolution.setValue(g.resolution);
		resolution.populate(res);
		resolutionPane.add(res);

		JPanel freq = makeRadioPanel(Messages.getString("GameSettingFrame.TITLE_FREQUENCY"),150,200); //$NON-NLS-1$
		frequency = new IndexButtonGroup(6,true,false);
		frequency.add(new JRadioButton(Messages.getString("GameSettingFrame.NO_CHANGE"))); //$NON-NLS-1$
		frequency.add(new JRadioButton(Messages.getString("GameSettingFrame.60HZ"))); //$NON-NLS-1$
		frequency.add(new JRadioButton(Messages.getString("GameSettingFrame.70HZ"))); //$NON-NLS-1$
		frequency.add(new JRadioButton(Messages.getString("GameSettingFrame.85HZ"))); //$NON-NLS-1$
		frequency.add(new JRadioButton(Messages.getString("GameSettingFrame.100HZ"))); //$NON-NLS-1$
		frequency.add(new JRadioButton(Messages.getString("GameSettingFrame.120HZ"))); //$NON-NLS-1$
		frequency.setValue(g.frequency);
		frequency.populate(freq);
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
		GameSettings g = LGM.currentFile.gameSettings;
		JPanel panel = new JPanel(new FlowLayout());
		String t = Messages.getString("GameSettingFrame.TITLE_KEYS"); //$NON-NLS-1$
		addGap(panel,450,10);
		JPanel dKeys = makeRadioPanel(t,480,150);
		panel.add(dKeys);

		t = Messages.getString("GameSettingFrame.KEY_ENDGAME"); //$NON-NLS-1$
		esc = new JCheckBox(t,g.letEscEndGame);
		t = Messages.getString("GameSettingFrame.KEY_INFO"); //$NON-NLS-1$
		f1 = new JCheckBox(t,g.letF1ShowGameInfo);
		t = Messages.getString("GameSettingFrame.KEY_SWITCHFULLSCREEN"); //$NON-NLS-1$
		f4 = new JCheckBox(t,g.letF4SwitchFullscreen);
		t = Messages.getString("GameSettingFrame.SAVELOAD"); //$NON-NLS-1$
		f5 = new JCheckBox(t,g.letF5SaveF6Load);
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
		gamePriority.setValue(g.gamePriority);

		return panel;
		}

	public JCheckBox showCustomLoadImage;
	public BufferedImage customLoadingImage;
	public JButton changeCustomLoad;
	public JCheckBox imagePartiallyTransparent;
	public IntegerField loadImageAlpha;
	public IndexButtonGroup loadBarMode;
	public JButton backLoad;
	public JButton frontLoad;
	public BufferedImage backLoadImage;
	public BufferedImage frontLoadImage;
	public JCheckBox scaleProgressBar;
	public JLabel iconPreview;
	public BufferedImage gameIcon;
	public byte[] gameIconData;
	public JButton changeIcon;
	public IntegerField gameId;
	public JButton randomise;

	private JPanel makeLoadingPane()
		{
		GameSettings g = LGM.currentFile.gameSettings;
		JPanel panel = new JPanel(new FlowLayout());

		JPanel loadImage = new JPanel(new FlowLayout());
		loadImage.setPreferredSize(new Dimension(480,120));
		loadImage.setBorder(BorderFactory.createTitledBorder(Messages.getString("GameSettingFrame.TITLE_LOADING_IMAGE"))); //$NON-NLS-1$

		showCustomLoadImage = new JCheckBox(
				Messages.getString("GameSettingFrame.CUSTOM_LOAD_IMAGE"),g.showCustomLoadImage); //$NON-NLS-1$
		showCustomLoadImage.addActionListener(this);
		addDim(loadImage,showCustomLoadImage,200,16);
		customLoadingImage = g.loadingImage;

		changeCustomLoad = new JButton(Messages.getString("GameSettingFrame.CHANGE_IMAGE")); //$NON-NLS-1$
		changeCustomLoad.setEnabled(showCustomLoadImage.isSelected());
		changeCustomLoad.addActionListener(this);
		addDim(loadImage,changeCustomLoad,120,24);

		addGap(loadImage,130,16);

		imagePartiallyTransparent = new JCheckBox(
				Messages.getString("GameSettingFrame.MAKE_TRANSPARENT"), //$NON-NLS-1$
				g.imagePartiallyTransparent);
		addDim(loadImage,imagePartiallyTransparent,460,16);
		JLabel lab = new JLabel(Messages.getString("GameSettingFrame.ALPHA_TRANSPARENCY")); //$NON-NLS-1$
		addDim(loadImage,lab,120,16);
		loadImageAlpha = new IntegerField(0,255,g.loadImageAlpha);
		addDim(loadImage,loadImageAlpha,50,20);
		addGap(loadImage,270,16);
		panel.add(loadImage);

		JPanel progBar = makeTitledPanel(
				Messages.getString("GameSettingFrame.TITLE_LOADING_PROGRESS_BAR"),480,150); //$NON-NLS-1$
		loadBarMode = new IndexButtonGroup(3,true,false,this);
		JRadioButton but = new JRadioButton(Messages.getString("GameSettingFrame.NO_PROGRESS_BAR")); //$NON-NLS-1$
		loadBarMode.add(but);
		addDim(progBar,but,460,16);
		but = new JRadioButton(Messages.getString("GameSettingFrame.DEF_PROGRESS_BAR")); //$NON-NLS-1$
		loadBarMode.add(but);
		addDim(progBar,but,460,16);
		but = new JRadioButton(Messages.getString("GameSettingFrame.CUSTOM_PROGRESS_BAR")); //$NON-NLS-1$
		loadBarMode.add(but);
		addDim(progBar,but,460,16);
		loadBarMode.setValue(g.loadBarMode);

		backLoad = new JButton(Messages.getString("GameSettingFrame.BACK_IMAGE")); //$NON-NLS-1$
		backLoad.addActionListener(this);
		backLoadImage = g.backLoadBar;
		addDim(progBar,backLoad,120,24);
		frontLoad = new JButton(Messages.getString("GameSettingFrame.FRONT_IMAGE")); //$NON-NLS-1$
		frontLoad.addActionListener(this);
		frontLoadImage = g.frontLoadBar;
		addDim(progBar,frontLoad,120,24);
		addGap(progBar,180,20);
		backLoad.setEnabled(loadBarMode.getValue() == GameSettings.LOADBAR_CUSTOM);
		frontLoad.setEnabled(backLoad.isEnabled());

		scaleProgressBar = new JCheckBox(
				Messages.getString("GameSettingFrame.SCALE_IMAGE"),g.scaleProgressBar); //$NON-NLS-1$
		addDim(progBar,scaleProgressBar,460,16);
		panel.add(progBar);

		gameIcon = g.gameIcon;
		gameIconData = g.gameIconData;
		iconPreview = new JLabel(Messages.getString("GameSettingFrame.GAME_ICON")); //$NON-NLS-1$
		if (g.gameIcon != null) iconPreview.setIcon(new ImageIcon(gameIcon));
		iconPreview.setHorizontalTextPosition(SwingConstants.LEFT);
		addDim(panel,iconPreview,140,40);
		changeIcon = new JButton(Messages.getString("GameSettingFrame.CHANGE_ICON")); //$NON-NLS-1$
		changeIcon.addActionListener(this);
		addDim(panel,changeIcon,120,24);

		addGap(panel,200,16);

		lab = new JLabel(Messages.getString("GameSettingFrame.GAME_ID")); //$NON-NLS-1$
		addDim(panel,lab,50,16);

		gameId = new IntegerField(0,100000000,g.gameId);
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

		cModel = new ConstantsTableModel(LGM.currentFile.gameSettings.constants);
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

		ConstantsTableModel(ArrayList<Constant> list)
			{
			constants = new ArrayList<Constant>();
			for (Constant c : list)
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

	JList includes;
	IncludesListModel iModel;
	JButton iAdd;
	JButton iDelete;
	JButton iClear;
	IndexButtonGroup exportFolder;
	JCheckBox overwriteExisting;
	JCheckBox removeAtGameEnd;

	private JPanel makeIncludePane()
		{
		GameSettings g = LGM.currentFile.gameSettings;
		JPanel panel = new JPanel(new FlowLayout());
		JLabel lab = new JLabel(Messages.getString("GameSettingFrame.FILES_TO_INCLUDE")); //$NON-NLS-1$
		addDim(panel,lab,450,16);

		iModel = new IncludesListModel(g.includes);
		includes = new JList(iModel);
		addDim(panel,includes,450,200);
		iAdd = new JButton(Messages.getString("GameSettingFrame.ADD_INCLUDE")); //$NON-NLS-1$
		iAdd.addActionListener(this);
		addDim(panel,iAdd,80,24);
		addGap(panel,80,24);
		iDelete = new JButton(Messages.getString("GameSettingFrame.DELETE_INCLUDE")); //$NON-NLS-1$
		iDelete.addActionListener(this);
		addDim(panel,iDelete,80,24);
		addGap(panel,80,24);
		iClear = new JButton(Messages.getString("GameSettingFrame.CLEAR_INCLUDES")); //$NON-NLS-1$
		iClear.addActionListener(this);
		addDim(panel,iClear,80,24);

		JPanel folderPanel = makeRadioPanel(Messages.getString("GameSettingFrame.EXPORT_TO"),200,80); //$NON-NLS-1$
		exportFolder = new IndexButtonGroup(2,true,false);
		exportFolder.add(new JRadioButton(Messages.getString("GameSettingFrame.SAME_FOLDER"))); //$NON-NLS-1$
		exportFolder.add(new JRadioButton(Messages.getString("GameSettingFrame.TEMP_DIRECTORY"))); //$NON-NLS-1$
		exportFolder.setValue(g.includeFolder);
		exportFolder.populate(folderPanel);
		panel.add(folderPanel);

		JPanel checkPanel = new JPanel(new FlowLayout());
		addDim(panel,checkPanel,200,50);
		overwriteExisting = new JCheckBox(Messages.getString("GameSettingFrame.OVERWRITE_EXISTING")); //$NON-NLS-1$
		addDim(checkPanel,overwriteExisting,200,16);
		removeAtGameEnd = new JCheckBox(Messages.getString("GameSettingFrame.REMOVE_FILES_AT_END")); //$NON-NLS-1$
		addDim(checkPanel,removeAtGameEnd,200,16);
		return panel;
		}

	private class IncludesListModel extends DefaultListModel
		{
		private static final long serialVersionUID = 1L;

		IncludesListModel(ArrayList<Include> list)
			{
			for (Include i : list)
				addElement(i.copy());
			}

		public ArrayList<Include> toArrayList()
			{
			ArrayList<Include> list = new ArrayList<Include>();
			for (Object o : toArray())
				list.add((Include) o);
			return list;
			}
		}

	JCheckBox displayErrors;
	JCheckBox writeToLog;
	JCheckBox abortOnError;
	JCheckBox treatUninitialisedAs0;

	private JPanel makeErrorPane()
		{
		GameSettings g = LGM.currentFile.gameSettings;
		JPanel panel = new JPanel(false);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		String t = Messages.getString("GameSettingFrame.ERRORS_DISPLAY"); //$NON-NLS-1$
		displayErrors = new JCheckBox(t,g.displayErrors);
		t = Messages.getString("GameSettingFrame.ERRORS_LOG"); //$NON-NLS-1$
		writeToLog = new JCheckBox(t,g.writeToLog);
		t = Messages.getString("GameSettingFrame.ERRORS_ABORT"); //$NON-NLS-1$
		abortOnError = new JCheckBox(t,g.abortOnError);
		t = Messages.getString("GameSettingFrame.UNINITZERO"); //$NON-NLS-1$
		treatUninitialisedAs0 = new JCheckBox(t,g.treatUninitializedAs0);
		panel.add(displayErrors);
		panel.add(writeToLog);
		panel.add(abortOnError);
		panel.add(treatUninitialisedAs0);
		return panel;
		}

	JTextField author;
	IntegerField version;

	private JPanel makeInfoPane()
		{
		GameSettings g = LGM.currentFile.gameSettings;
		JPanel panel = new JPanel(false);
		panel.setLayout(new FlowLayout());
		JLabel label = new JLabel(Messages.getString("GameSettingFrame.AUTHOR")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(80,25));
		panel.add(label);
		author = new JTextField(g.author);
		author.setPreferredSize(new Dimension(390,25));
		panel.add(author);
		label = new JLabel(Messages.getString("GameSettingFrame.VERSION")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(80,25));
		panel.add(label);
		version = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,g.version); 
		version.setPreferredSize(new Dimension(390,25));
		panel.add(version);
		label = new JLabel(Messages.getString("GameSettingFrame.LASTCHANGED")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(80,25));
		panel.add(label);
		JTextField lastChanged = new JTextField(Gm6File.gmTimeToString(g.lastChanged));
		lastChanged.setPreferredSize(new Dimension(390,25));
		lastChanged.setEditable(false);
		panel.add(lastChanged);
		label = new JLabel(Messages.getString("GameSettingFrame.INFORMATION")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(70,25));
		panel.add(label);
		JTextArea boxa = new JTextArea(g.information);
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

		rebuildTabs();

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

	private void rebuildTabs()
		{
		tabbedPane.removeAll();
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
			rebuildTabs();
			setVisible(false);
			return;
			}

		switch (tabbedPane.getSelectedIndex())
			{
			case 0:
				if (e.getSource() instanceof JRadioButton) scale.setEnabled(scaling.getValue() > 0);
				break;
			case 1:
				resolutionPane.setVisible(setResolution.isSelected());
				break;
			case 3:
				loadActionPerformed(e);
				break;
			case 4:
				constantsActionPerformed(e);
				break;
			case 5:
				includesActionPerformed(e);
				break;
			}
		}

	private void loadActionPerformed(ActionEvent e)
		{
		if (e.getSource() == showCustomLoadImage)
			{
			changeCustomLoad.setEnabled(showCustomLoadImage.isSelected());
			}
		else if (e.getSource() == changeCustomLoad)
			{
			try
				{
				customLoadingImage = Util.getValidImage();
				}
			catch (Throwable ex)
				{
				JOptionPane.showMessageDialog(LGM.frame,
						Messages.getString("GameSettingFrame.ERROR_LOADING_IMAGE")); //$NON-NLS-1$
				}
			}
		else if (e.getSource() instanceof JRadioButton)
			{
			backLoad.setEnabled(loadBarMode.getValue() == GameSettings.LOADBAR_CUSTOM);
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
			fc.setFileFilter(new CustomFileFilter(".ico",Messages.getString("GameSettingFrame.ICO_FILES"))); //$NON-NLS-1$ //$NON-NLS-2$
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
							JOptionPane.showMessageDialog(
									LGM.frame,
									Messages.getString("GameSettingFrame.INVALID_ICON"),Messages.getString("GameSettingFrame.TITLE_ERROR"), //$NON-NLS-1$ //$NON-NLS-2$
									JOptionPane.ERROR_MESSAGE);
							return;
							}
						BitmapHeader d = i.getDescriptor(0).getHeader();
						if (d.getWidth() != 32 || d.getHeight() != 64)
							{
							JOptionPane.showMessageDialog(
									LGM.frame,
									Messages.getString("GameSettingFrame.INVALID_ICON"),Messages.getString("GameSettingFrame.TITLE_ERROR"), //$NON-NLS-1$ //$NON-NLS-2$
									JOptionPane.ERROR_MESSAGE);
							return;
							}

						gameIcon = i.getDescriptor(0).getBitmap().createImageRGB();
						iconPreview.setIcon(new ImageIcon(gameIcon));

						//ICOFile closes the stream when it's done
						in = new BufferedInputStream(new FileInputStream(f));
						ByteArrayOutputStream dat = new ByteArrayOutputStream();

						int val = in.read();
						while (val != -1)
							{
							dat.write(val);
							val = in.read();
							}
						gameIconData = dat.toByteArray();
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

	private void includesActionPerformed(ActionEvent e)
		{
		if (e.getSource() == iAdd)
			{
			JFileChooser choose = new JFileChooser();
			if (choose.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION)
				{
				File f = choose.getSelectedFile();
				if (f != null) iModel.addElement(new Include(f.getAbsolutePath()));
				}
			return;
			}
		if (e.getSource() == iDelete)
			{
			int ind = includes.getSelectedIndex();
			if (ind != -1) iModel.removeElementAt(ind);
			return;
			}
		if (e.getSource() == iClear)
			{
			iModel.clear();
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
				JOptionPane.showMessageDialog(
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
				result = JOptionPane.showConfirmDialog(
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

	public void commitChanges()
		{
		GameSettings g = LGM.currentFile.gameSettings;
		//Graphics
		g.startFullscreen = startFullscreen.isSelected();
		g.scaling = scaling.getValue() > 0 ? scale.getIntValue() : scaling.getValue();
		g.interpolate = interpolatecolors.isSelected();
		g.colorOutsideRoom = colorbutton.getSelectedColor();
		g.allowWindowResize = resizeWindow.isSelected();
		g.alwaysOnTop = stayOnTop.isSelected();
		g.dontDrawBorder = noWindowBorder.isSelected();
		g.dontShowButtons = noWindowButtons.isSelected();
		g.displayCursor = displayMouse.isSelected();
		g.freezeOnLoseFocus = freezeGame.isSelected();

		//Resolution
		g.useSynchronization = synchronised.isSelected();
		g.setResolution = setResolution.isSelected();
		g.colorDepth = (byte) colourDepth.getValue();
		g.resolution = (byte) resolution.getValue();
		g.frequency = (byte) frequency.getValue();

		//Other
		g.letEscEndGame = esc.isSelected();
		g.letF1ShowGameInfo = f1.isSelected();
		g.letF4SwitchFullscreen = f4.isSelected();
		g.letF5SaveF6Load = f5.isSelected();
		g.gamePriority = (byte) gamePriority.getValue();

		//Loading
		g.showCustomLoadImage = showCustomLoadImage.isSelected();
		g.loadingImage = customLoadingImage;
		g.imagePartiallyTransparent = imagePartiallyTransparent.isSelected();
		g.loadImageAlpha = loadImageAlpha.getIntValue();
		g.loadBarMode = (byte) loadBarMode.getValue();
		g.backLoadBar = backLoadImage;
		g.frontLoadBar = frontLoadImage;
		g.scaleProgressBar = scaleProgressBar.isSelected();
		g.gameIcon = gameIcon;
		g.gameIconData = gameIconData;
		g.gameId = gameId.getIntValue();

		//Constants
		g.constants = cModel.constants;

		//Includes
		g.includes = iModel.toArrayList();
		g.includeFolder = exportFolder.getValue();
		g.overwriteExisting = overwriteExisting.isSelected();
		g.removeAtGameEnd = removeAtGameEnd.isSelected();

		//Errors
		g.displayErrors = displayErrors.isSelected();
		g.writeToLog = writeToLog.isSelected();
		g.abortOnError = abortOnError.isSelected();
		g.treatUninitializedAs0 = treatUninitialisedAs0.isSelected();

		//Info
		g.author = author.getText();
		g.version = version.getIntValue();
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
