/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.ExceptionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
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
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.AbstractTableModel;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.mdi.RevertableMDIFrame;
import org.lateralgm.file.GmFile;
import org.lateralgm.file.GmStreamDecoder;
import org.lateralgm.file.GmStreamEncoder;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.GameSettings.ColorDepth;
import org.lateralgm.resources.GameSettings.Frequency;
import org.lateralgm.resources.GameSettings.IncludeFolder;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GameSettings.Priority;
import org.lateralgm.resources.GameSettings.ProgressBar;
import org.lateralgm.resources.GameSettings.Resolution;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;

public class GameSettingFrame extends RevertableMDIFrame implements ActionListener,
		ExceptionListener
	{
	private static final long serialVersionUID = 1L;

	public GameSettings res, resOriginal;
	protected final PropertyLinkFactory<PGameSettings> plf;
	boolean imagesChanged = false;
	public JTabbedPane tabbedPane = new JTabbedPane();

	public JCheckBox startFullscreen;
	public IndexButtonGroup scaling;
	public NumberField scale;
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
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		startFullscreen = new JCheckBox(Messages.getString("GameSettingFrame.FULLSCREEN")); //$NON-NLS-1$
		plf.make(startFullscreen,PGameSettings.START_FULLSCREEN);

		JPanel scalegroup = new JPanel();
		GroupLayout sLayout = new GroupLayout(scalegroup);
		scalegroup.setLayout(sLayout);
		String t = Messages.getString("GameSettingFrame.SCALING_TITLE"); //$NON-NLS-1$
		scalegroup.setBorder(BorderFactory.createTitledBorder(t));
		scaling = new IndexButtonGroup(3,true,false,this);
		JRadioButton osFixed = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_FIXED")); //$NON-NLS-1$
		scaling.add(osFixed,1);
		scale = new NumberField(1,999,100);
		JRadioButton osRatio = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_RATIO")); //$NON-NLS-1$
		scaling.add(osRatio,-1);
		JRadioButton osFull = new JRadioButton(Messages.getString("GameSettingFrame.SCALING_FULL")); //$NON-NLS-1$
		scaling.add(osFull,0);
		//due to the complexity of this setup resolving to 1 property, we handle this in commitChanges.

		sLayout.setHorizontalGroup(sLayout.createParallelGroup()
		/**/.addGroup(sLayout.createSequentialGroup()
		/*		*/.addComponent(osFixed).addPreferredGap(ComponentPlacement.RELATED)
		/*		*/.addComponent(scale,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE).addContainerGap())
		/**/.addComponent(osRatio)
		/**/.addComponent(osFull));
		sLayout.setVerticalGroup(sLayout.createSequentialGroup()
		/**/.addGroup(sLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(osFixed)
		/*		*/.addComponent(scale))
		/**/.addComponent(osRatio)
		/**/.addComponent(osFull));

		int s = res.properties.get(PGameSettings.SCALING);
		scaling.setValue(s > 1 ? 1 : s);
		if (s > 1) scale.setValue(s);
		scale.setEnabled(s > 0);

		t = Messages.getString("GameSettingFrame.INTERPOLATE"); //$NON-NLS-1$
		plf.make(interpolatecolors = new JCheckBox(t),PGameSettings.INTERPOLATE);

		JLabel backcolor = new JLabel(Messages.getString("GameSettingFrame.BACKCOLOR")); //$NON-NLS-1$
		plf.make(colorbutton = new ColorSelect(),PGameSettings.COLOR_OUTSIDE_ROOM);

		resizeWindow = new JCheckBox(Messages.getString("GameSettingFrame.RESIZE")); //$NON-NLS-1$
		stayOnTop = new JCheckBox(Messages.getString("GameSettingFrame.STAYONTOP")); //$NON-NLS-1$
		noWindowBorder = new JCheckBox(Messages.getString("GameSettingFrame.NOBORDER")); //$NON-NLS-1$
		noWindowButtons = new JCheckBox(Messages.getString("GameSettingFrame.NOBUTTONS")); //$NON-NLS-1$
		displayMouse = new JCheckBox(Messages.getString("GameSettingFrame.DISPLAYCURSOR")); //$NON-NLS-1$
		freezeGame = new JCheckBox(Messages.getString("GameSettingFrame.FREEZE")); //$NON-NLS-1$

		plf.make(resizeWindow,PGameSettings.ALLOW_WINDOW_RESIZE);
		plf.make(stayOnTop,PGameSettings.ALWAYS_ON_TOP);
		plf.make(noWindowBorder,PGameSettings.DONT_DRAW_BORDER);
		plf.make(noWindowButtons,PGameSettings.DONT_SHOW_BUTTONS);
		plf.make(displayMouse,PGameSettings.DISPLAY_CURSOR);
		plf.make(freezeGame,PGameSettings.FREEZE_ON_LOSE_FOCUS);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(startFullscreen)
		/**/.addComponent(scalegroup)
		/**/.addComponent(interpolatecolors)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(backcolor)
		/*	*/.addComponent(colorbutton,DEFAULT_SIZE,DEFAULT_SIZE,120))
		/**/.addComponent(resizeWindow)
		/**/.addComponent(stayOnTop)
		/**/.addComponent(noWindowBorder)
		/**/.addComponent(noWindowButtons)
		/**/.addComponent(displayMouse)
		/**/.addComponent(freezeGame));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(startFullscreen)
		/**/.addComponent(scalegroup)
		/**/.addComponent(interpolatecolors)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE,false)
		/*	*/.addComponent(backcolor)
		/*	*/.addComponent(colorbutton))
		/**/.addComponent(resizeWindow)
		/**/.addComponent(stayOnTop)
		/**/.addComponent(noWindowBorder)
		/**/.addComponent(noWindowButtons)
		/**/.addComponent(displayMouse)
		/**/.addComponent(freezeGame)
		/**/.addGap(4,4,MAX_VALUE));
		return panel;
		}

	public JCheckBox synchronised;
	public JCheckBox setResolution;
	public ButtonGroup colorDepth;
	public ButtonGroup resolution;
	public ButtonGroup frequency;
	public JPanel resolutionPane;

	private <V extends Enum<V>>JPanel makeRadioPane(String title, ButtonGroup bg, PGameSettings prop,
			Class<V> optsClass, String[] vals)
		{
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder(title));
		p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));

		for (String s : vals)
			{
			JRadioButton but = new JRadioButton(Messages.getString(s));
			bg.add(but);
			p.add(but);
			}
		plf.make(bg,prop,optsClass);

		return p;
		}

	private JPanel makeResolutionPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHonorsVisibility(false);
		panel.setLayout(layout);

		synchronised = new JCheckBox(Messages.getString("GameSettingFrame.USE_SYNC")); //$NON-NLS-1$
		plf.make(synchronised,PGameSettings.USE_SYNCHRONIZATION);
		setResolution = new JCheckBox(Messages.getString("GameSettingFrame.SET_RESOLUTION")); //$NON-NLS-1$
		plf.make(setResolution,PGameSettings.SET_RESOLUTION);
		setResolution.addActionListener(this);

		resolutionPane = new JPanel();
		GroupLayout rpLayout = new GroupLayout(resolutionPane);
		rpLayout.setAutoCreateGaps(true);
		resolutionPane.setLayout(rpLayout);

		String colDepths[] = { "GameSettingFrame.NO_CHANGE", //$NON-NLS-1$
				"GameSettingFrame.16_BIT","GameSettingFrame.32_BIT" }; //$NON-NLS-1$ //$NON-NLS-2$

		String resolutions[] = { "GameSettingFrame.NO_CHANGE","GameSettingFrame.320X240", //$NON-NLS-1$ //$NON-NLS-2$
				"GameSettingFrame.640X480","GameSettingFrame.800X600","GameSettingFrame.1024X768", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"GameSettingFrame.1280X1024","GameSettingFrame.1600X1200" }; //$NON-NLS-1$ //$NON-NLS-2$

		String freqs[] = { "GameSettingFrame.NO_CHANGE","GameSettingFrame.60HZ", //$NON-NLS-1$ //$NON-NLS-2$
				"GameSettingFrame.70HZ","GameSettingFrame.85HZ", //$NON-NLS-1$ //$NON-NLS-2$
				"GameSettingFrame.100HZ","GameSettingFrame.120HZ", }; //$NON-NLS-1$ //$NON-NLS-2$

		JPanel depth = makeRadioPane(Messages.getString("GameSettingFrame.TITLE_COLOR_DEPTH"), //$NON-NLS-1$
				colorDepth = new ButtonGroup(),PGameSettings.COLOR_DEPTH,ColorDepth.class,colDepths);
		JPanel resol = makeRadioPane(Messages.getString("GameSettingFrame.TITLE_RESOLUTION"), //$NON-NLS-1$
				resolution = new ButtonGroup(),PGameSettings.RESOLUTION,Resolution.class,resolutions);
		JPanel freq = makeRadioPane(Messages.getString("GameSettingFrame.TITLE_FREQUENCY"), //$NON-NLS-1$
				frequency = new ButtonGroup(),PGameSettings.FREQUENCY,Frequency.class,freqs);

		rpLayout.setHorizontalGroup(rpLayout.createSequentialGroup()
		/**/.addComponent(depth,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(resol,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(freq,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		rpLayout.setVerticalGroup(rpLayout.createParallelGroup(Alignment.LEADING,false)
		/**/.addComponent(depth,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(resol,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(freq,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		resolutionPane.setVisible(setResolution.isSelected());

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(synchronised).addComponent(setResolution).addComponent(resolutionPane));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(synchronised).addComponent(setResolution).addComponent(resolutionPane));
		return panel;
		}

	public JCheckBox esc, f1, f4, f5, f9;
	public ButtonGroup gamePriority;

	private JPanel makeOtherPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		String t = Messages.getString("GameSettingFrame.TITLE_KEYS"); //$NON-NLS-1$
		JPanel dKeys = new JPanel();
		dKeys.setBorder(BorderFactory.createTitledBorder(t));
		dKeys.setLayout(new BoxLayout(dKeys,BoxLayout.PAGE_AXIS));

		esc = new JCheckBox(Messages.getString("GameSettingFrame.KEY_ENDGAME")); //$NON-NLS-1$
		f1 = new JCheckBox(Messages.getString("GameSettingFrame.KEY_INFO")); //$NON-NLS-1$
		f4 = new JCheckBox(Messages.getString("GameSettingFrame.KEY_SWITCHFULLSCREEN")); //$NON-NLS-1$
		f5 = new JCheckBox(Messages.getString("GameSettingFrame.SAVELOAD")); //$NON-NLS-1$
		f9 = new JCheckBox(Messages.getString("GameSettingFrame.KEY_SCREENSHOT")); //$NON-NLS-1$
		dKeys.add(esc);
		dKeys.add(f1);
		dKeys.add(f4);
		dKeys.add(f5);
		dKeys.add(f9);
		plf.make(esc,PGameSettings.LET_ESC_END_GAME);
		plf.make(f1,PGameSettings.LET_F1_SHOW_GAME_INFO);
		plf.make(f4,PGameSettings.LET_F4_SWITCH_FULLSCREEN);
		plf.make(f5,PGameSettings.LET_F5_SAVE_F6_LOAD);
		plf.make(f9,PGameSettings.LET_F9_SCREENSHOT);

		String priorities[] = { "GameSettingFrame.PRIORITY_NORMAL", //$NON-NLS-1$
				"GameSettingFrame.PRIORITY_HIGH","GameSettingFrame.PRIORITY_HIHGEST" }; //$NON-NLS-1$ //$NON-NLS-2$
		JPanel priority = makeRadioPane(Messages.getString("GameSettingFrame.TITLE_PRIORITY"), //$NON-NLS-1$
				gamePriority = new ButtonGroup(),PGameSettings.GAME_PRIORITY,Priority.class,priorities);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(dKeys,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(priority,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(dKeys)
		/**/.addComponent(priority));
		return panel;
		}

	public JCheckBox showCustomLoadImage;
	public BufferedImage customLoadingImage;
	public JButton changeCustomLoad;
	public JCheckBox imagePartiallyTransparent;
	public NumberField loadImageAlpha;
	public ButtonGroup loadBarMode;
	public JRadioButton pbCustom;
	public JButton backLoad;
	public JButton frontLoad;
	public BufferedImage backLoadImage;
	public BufferedImage frontLoadImage;
	public JCheckBox scaleProgressBar;
	public JLabel iconPreview;
	public ICOFile gameIcon;
	public JButton changeIcon;
	public NumberField gameId;
	public JButton randomise;
	private CustomFileChooser iconFc;

	private JPanel makeLoadingPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		JPanel loadImage = new JPanel();
		String t = Messages.getString("GameSettingFrame.TITLE_LOADING_IMAGE"); //$NON-NLS-1$
		loadImage.setBorder(BorderFactory.createTitledBorder(t));
		GroupLayout liLayout = new GroupLayout(loadImage);
		loadImage.setLayout(liLayout);
		showCustomLoadImage = new JCheckBox(Messages.getString("GameSettingFrame.CUSTOM_LOAD_IMAGE")); //$NON-NLS-1$
		plf.make(showCustomLoadImage,PGameSettings.SHOW_CUSTOM_LOAD_IMAGE);
		showCustomLoadImage.addActionListener(this);
		customLoadingImage = res.properties.get(PGameSettings.LOADING_IMAGE);

		changeCustomLoad = new JButton(Messages.getString("GameSettingFrame.CHANGE_IMAGE")); //$NON-NLS-1$
		changeCustomLoad.setEnabled(showCustomLoadImage.isSelected());
		changeCustomLoad.addActionListener(this);

		imagePartiallyTransparent = new JCheckBox(
				Messages.getString("GameSettingFrame.MAKE_TRANSPARENT")); //$NON-NLS-1$
		plf.make(imagePartiallyTransparent,PGameSettings.IMAGE_PARTIALLY_TRANSPARENTY);
		JLabel lAlpha = new JLabel(Messages.getString("GameSettingFrame.ALPHA_TRANSPARENCY")); //$NON-NLS-1$
		loadImageAlpha = new NumberField(0,255);
		plf.make(loadImageAlpha,PGameSettings.LOAD_IMAGE_ALPHA);

		liLayout.setHorizontalGroup(liLayout.createParallelGroup()
		/**/.addGroup(liLayout.createSequentialGroup()
		/*		*/.addComponent(showCustomLoadImage).addPreferredGap(ComponentPlacement.RELATED)
		/*		*/.addComponent(changeCustomLoad))
		/**/.addComponent(imagePartiallyTransparent)
		/**/.addGroup(liLayout.createSequentialGroup().addContainerGap()
		/*		*/.addComponent(lAlpha).addPreferredGap(ComponentPlacement.RELATED)
		/*		*/.addComponent(loadImageAlpha,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/*		*/.addContainerGap()));
		liLayout.setVerticalGroup(liLayout.createSequentialGroup()
		/**/.addGroup(liLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(showCustomLoadImage)
		/*		*/.addComponent(changeCustomLoad))
		/**/.addComponent(imagePartiallyTransparent)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addGroup(liLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lAlpha)
		/*		*/.addComponent(loadImageAlpha))
		/**/.addContainerGap());

		JRadioButton pbNo, pbDef;
		JPanel progBar = new JPanel();
		GroupLayout pbLayout = new GroupLayout(progBar);
		t = Messages.getString("GameSettingFrame.TITLE_LOADING_PROGRESS_BAR"); //$NON-NLS-1$
		progBar.setBorder(BorderFactory.createTitledBorder(t));
		progBar.setLayout(pbLayout);
		loadBarMode = new ButtonGroup();
		loadBarMode.add(pbNo = new JRadioButton(Messages.getString("GameSettingFrame.NO_PROGRESS_BAR"))); //$NON-NLS-1$
		loadBarMode.add(pbDef = new JRadioButton(
				Messages.getString("GameSettingFrame.DEF_PROGRESS_BAR"))); //$NON-NLS-1$
		loadBarMode.add(pbCustom = new JRadioButton(
				Messages.getString("GameSettingFrame.CUSTOM_PROGRESS_BAR"))); //$NON-NLS-1$
		plf.make(loadBarMode,PGameSettings.LOAD_BAR_MODE,ProgressBar.class);

		backLoad = new JButton(Messages.getString("GameSettingFrame.BACK_IMAGE")); //$NON-NLS-1$
		backLoad.addActionListener(this);
		backLoadImage = res.properties.get(PGameSettings.BACK_LOAD_BAR);
		frontLoad = new JButton(Messages.getString("GameSettingFrame.FRONT_IMAGE")); //$NON-NLS-1$
		frontLoad.addActionListener(this);
		frontLoadImage = res.properties.get(PGameSettings.FRONT_LOAD_BAR);
		backLoad.setEnabled(pbCustom.isSelected());
		frontLoad.setEnabled(backLoad.isEnabled());
		scaleProgressBar = new JCheckBox(Messages.getString("GameSettingFrame.SCALE_IMAGE")); //$NON-NLS-1$
		plf.make(scaleProgressBar,PGameSettings.SCALE_PROGRESS_BAR);

		pbLayout.setHorizontalGroup(pbLayout.createParallelGroup()
		/**/.addComponent(pbNo).addComponent(pbDef).addComponent(pbCustom)
		/**/.addGroup(pbLayout.createSequentialGroup().addContainerGap()
		/*		*/.addComponent(backLoad).addPreferredGap(ComponentPlacement.RELATED)
		/*		*/.addComponent(frontLoad).addContainerGap())
		/**/.addComponent(scaleProgressBar));
		pbLayout.setVerticalGroup(pbLayout.createSequentialGroup()
		/**/.addComponent(pbNo).addComponent(pbDef).addComponent(pbCustom)
		/**/.addGroup(pbLayout.createParallelGroup()
		/*		*/.addComponent(backLoad)
		/*		*/.addComponent(frontLoad))
		/**/.addComponent(scaleProgressBar));

		gameIcon = res.properties.get(PGameSettings.GAME_ICON);
		iconPreview = new JLabel(Messages.getString("GameSettingFrame.GAME_ICON")); //$NON-NLS-1$
		if (gameIcon != null) iconPreview.setIcon(new ImageIcon(gameIcon.getDisplayImage()));

		iconPreview.setHorizontalTextPosition(SwingConstants.LEFT);
		changeIcon = new JButton(Messages.getString("GameSettingFrame.CHANGE_ICON")); //$NON-NLS-1$
		changeIcon.addActionListener(this);
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(Messages.getString("GameSettingFrame.ICO_FILES"),".ico")); //$NON-NLS-1$ //$NON-NLS-2$
		JLabel lId = new JLabel(Messages.getString("GameSettingFrame.GAME_ID")); //$NON-NLS-1$
		gameId = new NumberField(0,100000000);
		plf.make(gameId,PGameSettings.GAME_ID);
		randomise = new JButton(Messages.getString("GameSettingFrame.RANDOMIZE")); //$NON-NLS-1$
		randomise.addActionListener(this);

		iconFc = new CustomFileChooser("/org/lateralgm","LAST_ICON_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		iconFc.setFileFilter(new CustomFileFilter(
				Messages.getString("GameSettingFrame.ICO_FILES"),".ico")); //$NON-NLS-1$ //$NON-NLS-2$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(loadImage,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(progBar,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(iconPreview)
		/*				*/.addGroup(layout.createSequentialGroup()
		/*						*/.addComponent(lId)
		/*						*/.addComponent(gameId,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(changeIcon,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(randomise,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(loadImage)
		/**/.addComponent(progBar)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(iconPreview)
		/*		*/.addComponent(changeIcon))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lId)
		/*		*/.addComponent(gameId)
		/*		*/.addComponent(randomise)));
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
	private CustomFileChooser constantsFc;

	private JPanel makeConstantsPane(List<Constant> curConstList)
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		importBut = new JButton(Messages.getString("GameSettingFrame.IMPORT")); //$NON-NLS-1$
		importBut.addActionListener(this);
		exportBut = new JButton(Messages.getString("GameSettingFrame.EXPORT")); //$NON-NLS-1$
		exportBut.addActionListener(this);

		cModel = new ConstantsTableModel(curConstList);
		constants = new JTable(cModel);
		JScrollPane scroll = new JScrollPane(constants);
		constants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		constants.getTableHeader().setReorderingAllowed(false);
		constants.setTransferHandler(null);
		//this fixes java bug 4709394, where the cell editor does not commit on focus lost,
		//causing the value to remain in-limbo even after the "Save" button is clicked.
		constants.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE); //$NON-NLS-1$

		add = new JButton(Messages.getString("GameSettingFrame.ADD")); //$NON-NLS-1$
		add.addActionListener(this);
		insert = new JButton(Messages.getString("GameSettingFrame.INSERT")); //$NON-NLS-1$
		insert.addActionListener(this);
		delete = new JButton(Messages.getString("GameSettingFrame.DELETE")); //$NON-NLS-1$
		delete.addActionListener(this);
		clear = new JButton(Messages.getString("GameSettingFrame.CLEAR")); //$NON-NLS-1$
		clear.addActionListener(this);
		up = new JButton(Messages.getString("GameSettingFrame.UP")); //$NON-NLS-1$
		up.addActionListener(this);
		down = new JButton(Messages.getString("GameSettingFrame.DOWN")); //$NON-NLS-1$
		down.addActionListener(this);
		sort = new JButton(Messages.getString("GameSettingFrame.SORT")); //$NON-NLS-1$
		sort.addActionListener(this);

		constantsFc = new CustomFileChooser("/org/lateralgm","LAST_LGC_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		constantsFc.setFileFilter(new CustomFileFilter(
				Messages.getString("GameSettingFrame.LGC_FILES"),".lgc")); //$NON-NLS-1$ //$NON-NLS-2$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(importBut,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(exportBut,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/**/.addComponent(scroll)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(add,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(insert,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(delete,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(clear,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addPreferredGap(ComponentPlacement.UNRELATED)
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(up,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(down,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addComponent(sort,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(importBut)
		/*		*/.addComponent(exportBut))
		/**/.addComponent(scroll,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(add)
		/*		*/.addComponent(delete)
		/*		*/.addComponent(up)
		/*		*/.addComponent(sort))
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(insert)
		/*		*/.addComponent(clear)
		/*		*/.addComponent(down)));
		return panel;
		}

	private class ConstantsTableModel extends AbstractTableModel
		{
		private static final long serialVersionUID = 1L;
		List<Constant> constants;

		ConstantsTableModel(List<Constant> list)
			{
			constants = GmFile.copyConstants(list);
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
			return (columnIndex == 0) ? c.name : c.value;
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
			String ind = (column == 0) ? "NAME" : "VALUE"; //$NON-NLS-1$ //$NON-NLS-2$
			return Messages.getString("GameSettingFrame." + ind); //$NON-NLS-1$
			}

		public void removeEmptyConstants()
			{
			for (int i = constants.size() - 1; i >= 0; i--)
				if (constants.get(i).name.equals("")) constants.remove(i); //$NON-NLS-1$
			fireTableDataChanged();
			}
		}

	public JList includes;
	public IncludesListModel iModel;
	public JButton iAdd;
	public JButton iDelete;
	public JButton iClear;
	public ButtonGroup exportFolder;
	public JCheckBox overwriteExisting;
	public JCheckBox removeAtGameEnd;
	private CustomFileChooser includesFc;

	private JPanel makeIncludePane(List<Include> curIncludeList)
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		JLabel lFiles = new JLabel(Messages.getString("GameSettingFrame.FILES_TO_INCLUDE")); //$NON-NLS-1$

		iModel = new IncludesListModel(curIncludeList);
		includes = new JList(iModel);
		JScrollPane iScroll = new JScrollPane(includes);
		iAdd = new JButton(Messages.getString("GameSettingFrame.ADD_INCLUDE")); //$NON-NLS-1$
		iAdd.addActionListener(this);
		iDelete = new JButton(Messages.getString("GameSettingFrame.DELETE_INCLUDE")); //$NON-NLS-1$
		iDelete.addActionListener(this);
		iClear = new JButton(Messages.getString("GameSettingFrame.CLEAR_INCLUDES")); //$NON-NLS-1$
		iClear.addActionListener(this);

		String incFolders[] = { "GameSettingFrame.SAME_FOLDER","GameSettingFrame.TEMP_DIRECTORY" }; //$NON-NLS-1$ //$NON-NLS-2$
		JPanel folderPanel = makeRadioPane(
				Messages.getString("GameSettingFrame.EXPORT_TO"), //$NON-NLS-1$
				exportFolder = new ButtonGroup(),PGameSettings.INCLUDE_FOLDER,IncludeFolder.class,
				incFolders);

		overwriteExisting = new JCheckBox(Messages.getString("GameSettingFrame.OVERWRITE_EXISTING")); //$NON-NLS-1$
		removeAtGameEnd = new JCheckBox(Messages.getString("GameSettingFrame.REMOVE_FILES_AT_END")); //$NON-NLS-1$
		plf.make(overwriteExisting,PGameSettings.OVERWRITE_EXISTING);
		plf.make(removeAtGameEnd,PGameSettings.REMOVE_AT_GAME_END);

		includesFc = new CustomFileChooser("/org/lateralgm","LAST_INCLUDES_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		includesFc.setMultiSelectionEnabled(true);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(lFiles)
		/**/.addComponent(iScroll,DEFAULT_SIZE,320,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(iAdd,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(iDelete,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(iClear,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(folderPanel).addGap(4,8,MAX_VALUE)
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(overwriteExisting)
		/*				*/.addComponent(removeAtGameEnd))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(lFiles)
		/**/.addComponent(iScroll)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(iAdd)
		/*		*/.addComponent(iDelete)
		/*		*/.addComponent(iClear))
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(folderPanel)
		/*		*/.addGroup(layout.createSequentialGroup()
		/*				*/.addComponent(overwriteExisting)
		/*				*/.addComponent(removeAtGameEnd))));
		return panel;
		}

	private class IncludesListModel extends DefaultListModel
		{
		private static final long serialVersionUID = 1L;

		IncludesListModel(List<Include> list)
			{
			for (Include i : list)
				addElement(i.copy());
			}

		public List<Include> toArrayList()
			{
			List<Include> list = new ArrayList<Include>();
			for (Object o : toArray())
				list.add((Include) o);
			return list;
			}
		}

	JCheckBox displayErrors;
	JCheckBox writeToLog;
	JCheckBox abortOnError;
	JCheckBox treatUninitialisedAs0;
	JCheckBox errorOnArgs;

	private JPanel makeErrorPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		displayErrors = new JCheckBox(Messages.getString("GameSettingFrame.ERRORS_DISPLAY")); //$NON-NLS-1$
		writeToLog = new JCheckBox(Messages.getString("GameSettingFrame.ERRORS_LOG")); //$NON-NLS-1$
		abortOnError = new JCheckBox(Messages.getString("GameSettingFrame.ERRORS_ABORT")); //$NON-NLS-1$
		treatUninitialisedAs0 = new JCheckBox(Messages.getString("GameSettingFrame.UNINITZERO")); //$NON-NLS-1$
		errorOnArgs = new JCheckBox(Messages.getString("GameSettingFrame.ERRORS_ARGS")); //$NON-NLS-1$

		plf.make(displayErrors,PGameSettings.DISPLAY_ERRORS);
		plf.make(writeToLog,PGameSettings.WRITE_TO_LOG);
		plf.make(abortOnError,PGameSettings.ABORT_ON_ERROR);
		plf.make(treatUninitialisedAs0,PGameSettings.TREAT_UNINIT_AS_0);
		plf.make(errorOnArgs,PGameSettings.ERROR_ON_ARGS);

		layout.setHorizontalGroup(layout.createParallelGroup().
		/**/addComponent(displayErrors).addComponent(writeToLog).addComponent(abortOnError).
		/**/addComponent(treatUninitialisedAs0).addComponent(errorOnArgs));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(displayErrors).addComponent(writeToLog).addComponent(abortOnError).
		/**/addComponent(treatUninitialisedAs0).addComponent(errorOnArgs));
		return panel;
		}

	JTextField author;
	JTextField version;
	JTextField lastChanged;
	JTextArea information;

	private JPanel makeInfoPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		JLabel lAuthor = new JLabel(Messages.getString("GameSettingFrame.AUTHOR")); //$NON-NLS-1$
		author = new JTextField();
		JLabel lVersion = new JLabel(Messages.getString("GameSettingFrame.VERSION")); //$NON-NLS-1$
		version = new JTextField();
		JLabel lChanged = new JLabel(Messages.getString("GameSettingFrame.LASTCHANGED")); //$NON-NLS-1$
		lastChanged = new JTextField(GmFile.gmTimeToString(res.getLastChanged()));
		lastChanged.setEditable(false);
		JLabel lInfo = new JLabel(Messages.getString("GameSettingFrame.INFORMATION")); //$NON-NLS-1$
		information = new JTextArea();
		information.setLineWrap(true);
		JScrollPane infoScroll = new JScrollPane(information);

		plf.make(author.getDocument(),PGameSettings.AUTHOR);
		plf.make(version.getDocument(),PGameSettings.VERSION);
		plf.make(information.getDocument(),PGameSettings.INFORMATION);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(lAuthor)
		/*				*/.addComponent(lVersion)
		/*				*/.addComponent(lChanged))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(author,DEFAULT_SIZE,240,MAX_VALUE)
		/*				*/.addComponent(version,DEFAULT_SIZE,240,MAX_VALUE)
		/*				*/.addComponent(lastChanged,DEFAULT_SIZE,240,MAX_VALUE)))
		/**/.addComponent(lInfo,DEFAULT_SIZE,320,MAX_VALUE)
		/**/.addComponent(infoScroll));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lAuthor)
		/*		*/.addComponent(author))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lVersion)
		/*		*/.addComponent(version))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lChanged)
		/*		*/.addComponent(lastChanged))
		/**/.addComponent(lInfo)
		/**/.addComponent(infoScroll));
		return panel;
		}

	public JButton saveButton;
	public JButton discardButton;

	public GameSettingFrame(GameSettings res, List<Constant> constants, List<Include> includes)
		{
		super(Messages.getString("GameSettingFrame.TITLE"),false,true,true,true); //$NON-NLS-1$
		plf = new PropertyLinkFactory<PGameSettings>(res.properties,this);
		this.res = res;
		resOriginal = res.clone();
		setFrameIcon(LGM.findIcon("restree/gm.png")); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		setLayout(layout);

		buildTabs(constants,includes);

		String t = Messages.getString("GameSettingFrame.BUTTON_SAVE"); //$NON-NLS-1$
		saveButton = new JButton(t);
		saveButton.addActionListener(this);
		t = Messages.getString("GameSettingFrame.BUTTON_DISCARD"); //$NON-NLS-1$
		discardButton = new JButton(t);
		discardButton.addActionListener(this);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(tabbedPane)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addContainerGap()
		/*		*/.addComponent(saveButton,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(discardButton,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addContainerGap()));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(tabbedPane)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(saveButton)
		/*		*/.addComponent(discardButton))
		/**/.addContainerGap());
		pack();
		}

	private void buildTabs(List<Constant> constants, List<Include> includes)
		{
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

		pane = makeConstantsPane(LGM.currentFile.constants);
		tabbedPane.addTab(Messages.getString("GameSettingFrame.TAB_CONSTANTS"), //$NON-NLS-1$
				null,pane,Messages.getString("GameSettingFrame.HINT_CONSTANTS")); //$NON-NLS-1$
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		pane = makeIncludePane(LGM.currentFile.includes);
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
			updateResource();
			close();
			return;
			}

		if (e.getSource() == discardButton)
			{
			revertResource();
			close();
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
				imagesChanged = true;
				}
			catch (Throwable ex)
				{
				JOptionPane.showMessageDialog(LGM.frame,
						Messages.getString("GameSettingFrame.ERROR_LOADING_IMAGE")); //$NON-NLS-1$
				}
			}
		else if (e.getSource() instanceof JRadioButton)
			{
			backLoad.setEnabled(pbCustom.isSelected());
			frontLoad.setEnabled(backLoad.isEnabled());
			}
		else if (e.getSource() == backLoad)
			{
			BufferedImage img = Util.getValidImage();
			if (img != null)
				{
				backLoadImage = img;
				imagesChanged = true;
				}
			}
		else if (e.getSource() == frontLoad)
			{
			BufferedImage img = Util.getValidImage();
			if (img != null)
				{
				frontLoadImage = img;
				imagesChanged = true;
				}
			}
		else if (e.getSource() == changeIcon)
			{
			if (iconFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
				{
				File f = iconFc.getSelectedFile();
				if (f.exists()) try
					{
					gameIcon = new ICOFile(new FileInputStream(f));
					iconPreview.setIcon(new ImageIcon(gameIcon.getDisplayImage()));
					imagesChanged = true;
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
			gameId.setValue(new Random().nextInt(100000001));
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
			if (includesFc.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION)
				{
				File[] f = includesFc.getSelectedFiles();
				for (File file : f)
					{
					Include inc = new Include();
					inc.filepath = file.getAbsolutePath();
					iModel.addElement(inc);
					}
				}
			return;
			}
		if (e.getSource() == iDelete)
			{
			int[] ind = includes.getSelectedIndices();
			for (int i = ind.length - 1; i >= 0; i--)
				iModel.removeElementAt(ind[i]);
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
		if (constantsFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			cModel.removeEmptyConstants();
			GmStreamDecoder in = null;
			try
				{
				File f = constantsFc.getSelectedFile();
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
				JOptionPane.showMessageDialog(LGM.frame,
						Messages.getString("GameSettingFrame.ERROR_IMPORTING_CONSTANTS"), //$NON-NLS-1$
						Messages.getString("GameSettingFrame.TITLE_ERROR"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
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
		while (constantsFc.showSaveDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			File f = constantsFc.getSelectedFile();
			if (f == null) return;
			if (!f.getPath().endsWith(".lgc")) f = new File(f.getPath() + ".lgc"); //$NON-NLS-1$ //$NON-NLS-2$
			int result = 0;
			if (f.exists())
				{
				result = JOptionPane.showConfirmDialog(LGM.frame,
						Messages.getString("GameSettingFrame.REPLACE_FILE"), //$NON-NLS-1$
						Messages.getString("GameSettingFrame.TITLE_REPLACE_FILE"), //$NON-NLS-1$
						JOptionPane.YES_NO_CANCEL_OPTION);
				}
			if (result == 2) return;
			if (result == 1) continue;

			cModel.removeEmptyConstants();
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
		res.put(PGameSettings.SCALING,scaling.getValue() > 0 ? scale.getIntValue() : scaling.getValue());
		res.put(PGameSettings.LOADING_IMAGE,customLoadingImage);
		res.put(PGameSettings.BACK_LOAD_BAR,backLoadImage);
		res.put(PGameSettings.FRONT_LOAD_BAR,frontLoadImage);
		res.put(PGameSettings.GAME_ICON,gameIcon);
		//we don't update the lastChanged time - that's only altered on file save/load

		//Constants
		cModel.removeEmptyConstants();
		LGM.currentFile.constants = GmFile.copyConstants(cModel.constants);

		//Includes
		LGM.currentFile.includes = iModel.toArrayList();
		}

	public void setComponents(GameSettings g)
		{
		int s = g.get(PGameSettings.SCALING);
		scaling.setValue(s > 1 ? 1 : s);
		if (s > 1) scale.setValue(s);
		scale.setEnabled(s > 0);

		lastChanged.setText(GmFile.gmTimeToString(g.getLastChanged()));

		customLoadingImage = g.get(PGameSettings.LOADING_IMAGE);
		backLoadImage = g.get(PGameSettings.BACK_LOAD_BAR);
		frontLoadImage = g.get(PGameSettings.FRONT_LOAD_BAR);
		gameIcon = g.get(PGameSettings.GAME_ICON);

		//Constants
		cModel = new ConstantsTableModel(LGM.currentFile.constants);
		constants.setModel(cModel);
		constants.updateUI();

		//Includes
		iModel = new IncludesListModel(LGM.currentFile.includes);
		includes.setModel(iModel);
		includes.updateUI();
		}

	@Override
	public String getConfirmationName()
		{
		return getTitle();
		}

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		if (imagesChanged) return true;
		return !res.properties.equals(resOriginal.properties);
		}

	@Override
	public void revertResource()
		{
		res.properties.putAll(resOriginal.properties);
		setComponents(res);
		imagesChanged = false;
		}

	@Override
	public void updateResource()
		{
		commitChanges();
		resOriginal = res.clone();
		imagesChanged = false;
		}

	public void exceptionThrown(Exception e)
		{
		e.printStackTrace();
		}
	}
