/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014, Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.SwingConstants;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.FileChooserImagePreview;
import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GameSettings.ColorDepth;
import org.lateralgm.resources.GameSettings.Frequency;
import org.lateralgm.resources.GameSettings.IncludeFolder;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GameSettings.Priority;
import org.lateralgm.resources.GameSettings.ProgressBar;
import org.lateralgm.resources.GameSettings.Resolution;
import org.lateralgm.resources.Include;

public class GameSettingFrame extends ResourceFrame<GameSettings,PGameSettings>
	{
	private static final long serialVersionUID = 1L;

	private static final int MAX_VIEWABLE_ICON_SIZE = 64; //Icons bigger than this are scaled down (for viewing only).

	boolean imagesChanged = false;
	public JPanel cardPane;

	public JCheckBox startFullscreen;
	public IndexButtonGroup scaling;
	public NumberField scale;
	public JCheckBox interpolatecolors;
	public JCheckBox softwareVertexProcessing;
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
		softwareVertexProcessing = new JCheckBox(
				Messages.getString("GameSettingFrame.FORCE_SOFTWARE_VERTEX_PROCESSING")); //$NON-NLS-1$
		plf.make(softwareVertexProcessing,PGameSettings.FORCE_SOFTWARE_VERTEX_PROCESSING);

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
		/**/.addComponent(softwareVertexProcessing)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(backcolor)
		/*	*/.addComponent(colorbutton))
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
		/**/.addComponent(softwareVertexProcessing)
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

	public JCheckBox esc, close, f1, f4, f5, f9;
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
		close = new JCheckBox(Messages.getString("GameSettingFrame.KEY_CLOSEGAME")); //$NON-NLS-1$
		f1 = new JCheckBox(Messages.getString("GameSettingFrame.KEY_INFO")); //$NON-NLS-1$
		f4 = new JCheckBox(Messages.getString("GameSettingFrame.KEY_SWITCHFULLSCREEN")); //$NON-NLS-1$
		f5 = new JCheckBox(Messages.getString("GameSettingFrame.SAVELOAD")); //$NON-NLS-1$
		f9 = new JCheckBox(Messages.getString("GameSettingFrame.KEY_SCREENSHOT")); //$NON-NLS-1$
		dKeys.add(esc);
		dKeys.add(close);
		dKeys.add(f1);
		dKeys.add(f4);
		dKeys.add(f5);
		dKeys.add(f9);
		plf.make(esc,PGameSettings.LET_ESC_END_GAME);
		plf.make(close,PGameSettings.TREAT_CLOSE_AS_ESCAPE);
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

	private JPanel makeTextureAtlasesPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
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
	public NumberField gameId;
	public JButton randomise;

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


		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(Messages.getString("GameSettingFrame.ICO_FILES"),".ico")); //$NON-NLS-1$ //$NON-NLS-2$
		JLabel lId = new JLabel(Messages.getString("GameSettingFrame.GAME_ID")); //$NON-NLS-1$
		gameId = new NumberField(0,100000000);
		plf.make(gameId,PGameSettings.GAME_ID);
		randomise = new JButton(Messages.getString("GameSettingFrame.RANDOMIZE")); //$NON-NLS-1$
		randomise.addActionListener(this);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(loadImage,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(progBar,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addGroup(layout.createSequentialGroup()
		/*						*/.addComponent(lId)
		/*						*/.addComponent(gameId,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(randomise,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(loadImage)
		/**/.addComponent(progBar)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lId)
		/*		*/.addComponent(gameId)
		/*		*/.addComponent(randomise)));
		return panel;
		}

	public JList<Include> includes;
	public ButtonGroup exportFolder;
	public JCheckBox overwriteExisting;
	public JCheckBox removeAtGameEnd;
	private CustomFileChooser includesFc;

	private JPanel makeIncludePane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

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
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(folderPanel).addGap(4,8,MAX_VALUE)
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(overwriteExisting)
		/*				*/.addComponent(removeAtGameEnd))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(folderPanel)
		/*		*/.addGroup(layout.createSequentialGroup()
		/*				*/.addComponent(overwriteExisting)
		/*				*/.addComponent(removeAtGameEnd))));
		return panel;
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

		JLabel lAuthor = new JLabel(Messages.getString("GameSettingFrame.AUTHOR")); //$NON-NLS-1$
		author = new JTextField();
		JLabel lVersion = new JLabel(Messages.getString("GameSettingFrame.VERSION")); //$NON-NLS-1$
		version = new JTextField();
		JLabel lChanged = new JLabel(Messages.getString("GameSettingFrame.LASTCHANGED")); //$NON-NLS-1$
		lastChanged = new JTextField(ProjectFile.gmTimeToString(res.getLastChanged()));
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
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
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

		panel.setLayout(layout);
		return panel;
		}

	public JLabel iconPreview;
	public ICOFile gameIcon;
	public JButton changeIcon;
	private CustomFileChooser iconFc;

	private static BufferedImage scale_image(BufferedImage src, int imgType, int destSize) {
		if(src == null) { return null; }
			BufferedImage dest = new BufferedImage(destSize, destSize, imgType);
			Graphics2D g = dest.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			AffineTransform at = AffineTransform.getScaleInstance(destSize/((float)src.getWidth()), destSize/((float)src.getHeight()));
			g.drawRenderedImage(src, at);

			return dest;
	}

	private void setIconPreviewToGameIcon() {
		BufferedImage src = null;
		if (gameIcon != null) {
			src = (BufferedImage)gameIcon.getDisplayImage();
			if (src!=null) {
				if (src.getWidth()>32 || src.getHeight()>32) {
					src = scale_image((BufferedImage)src, BufferedImage.TYPE_INT_ARGB, MAX_VIEWABLE_ICON_SIZE);
				}
			}
		}
		iconPreview.setIcon(new ImageIcon(src));
	}

	NumberField versionMajorField;
	NumberField versionMinorField;
	NumberField versionReleaseField;
	NumberField versionBuildField;
	JTextField companyField;
	JTextField productField;
	JTextField copyrightField;
	JTextField descriptionField;

	private JPanel makeWindowsPane() {
		JPanel panel = new JPanel();

		gameIcon = res.properties.get(PGameSettings.GAME_ICON);
		iconPreview = new JLabel(Messages.getString("GameSettingFrame.GAME_ICON")); //$NON-NLS-1$
		setIconPreviewToGameIcon();

		iconPreview.setHorizontalTextPosition(SwingConstants.LEFT);
		changeIcon = new JButton(Messages.getString("GameSettingFrame.CHANGE_ICON")); //$NON-NLS-1$
		changeIcon.addActionListener(this);

		iconFc = new CustomFileChooser("/org/lateralgm","LAST_ICON_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		iconFc.setAccessory(new FileChooserImagePreview(iconFc));
		iconFc.setFileFilter(new CustomFileFilter(
				Messages.getString("GameSettingFrame.ICO_FILES"),".ico")); //$NON-NLS-1$ //$NON-NLS-2$

		JPanel versionPanel = new JPanel();
		versionPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("GameSettingFrame.VERSION_INFORMATION")));

		JLabel versionLabel = new JLabel(Messages.getString("GameSettingFrame.VERSION"));
		versionMajorField = new NumberField(0);
		plf.make(versionMajorField,PGameSettings.VERSION_MAJOR);
		versionMinorField = new NumberField(0);
		plf.make(versionMinorField,PGameSettings.VERSION_MINOR);
		versionReleaseField = new NumberField(0);
		plf.make(versionReleaseField,PGameSettings.VERSION_RELEASE);
		versionBuildField = new NumberField(0);
		plf.make(versionBuildField,PGameSettings.VERSION_BUILD);
		JLabel companyLabel = new JLabel(Messages.getString("GameSettingFrame.COMPANY"));
		companyField = new JTextField("");
		plf.make(companyField.getDocument(),PGameSettings.COMPANY);
		JLabel productLabel = new JLabel(Messages.getString("GameSettingFrame.PRODUCT"));
		productField = new JTextField("");
		plf.make(productField.getDocument(),PGameSettings.PRODUCT);
		JLabel copyrightLabel = new JLabel(Messages.getString("GameSettingFrame.COPYRIGHT"));
		copyrightField = new JTextField("");
		plf.make(copyrightField.getDocument(),PGameSettings.COPYRIGHT);
		JLabel descriptionLabel = new JLabel(Messages.getString("GameSettingFrame.DESCRIPTION"));
		descriptionField = new JTextField("");
		plf.make(descriptionField.getDocument(),PGameSettings.DESCRIPTION);

		GroupLayout vl = new GroupLayout(versionPanel);
		vl.setAutoCreateGaps(true);
		vl.setAutoCreateContainerGaps(true);

		vl.setHorizontalGroup(vl.createSequentialGroup()
		/**/.addGroup(vl.createParallelGroup(Alignment.TRAILING)
		/*	*/.addComponent(versionLabel)
		/*	*/.addComponent(companyLabel)
		/*	*/.addComponent(productLabel)
		/*	*/.addComponent(copyrightLabel)
		/*	*/.addComponent(descriptionLabel))
		/**/.addGroup(vl.createParallelGroup()
		/*	*/.addGroup(vl.createSequentialGroup()
		/*		*/.addComponent(versionMajorField)
		/*		*/.addComponent(versionMinorField)
		/*		*/.addComponent(versionReleaseField)
		/*		*/.addComponent(versionBuildField))
		/*	*/.addComponent(companyField)
		/*	*/.addComponent(productField)
		/*	*/.addComponent(copyrightField)
		/*	*/.addComponent(descriptionField)));
		vl.setVerticalGroup(vl.createSequentialGroup()
		/**/.addGroup(vl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(versionLabel)
		/*	*/.addComponent(versionMajorField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		/*	*/.addComponent(versionMinorField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		/*	*/.addComponent(versionReleaseField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		/*	*/.addComponent(versionBuildField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(vl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(companyLabel)
		/*	*/.addComponent(companyField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(vl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(productLabel)
		/*	*/.addComponent(productField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(vl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(copyrightLabel)
		/*	*/.addComponent(copyrightField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(vl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(descriptionLabel)
		/*	*/.addComponent(descriptionField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)));

		versionPanel.setLayout(vl);

		GroupLayout gl = new GroupLayout(panel);

		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(iconPreview)
		/*	*/.addComponent(changeIcon))
		/**/.addComponent(versionPanel));
		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(iconPreview)
		/*	*/.addComponent(changeIcon))
		/**/.addComponent(versionPanel));

		panel.setLayout(gl);

		return panel;
	}

	public JButton discardButton;

	public JTree tree;

	public GameSettingFrame(GameSettings res)
		{
		this(res,null);
		}

	public void updateTitle() {
		this.setTitle(Messages.getString("GameSettingFrame.TITLE") + " : " + resOriginal.getName());
	}

	public GameSettingFrame(GameSettings res, ResNode node)
		{
		super(res,node,Messages.getString("GameSettingFrame.TITLE"),true,true,true,true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		setLayout(layout);

		String t = Messages.getString("GameSettingFrame.BUTTON_SAVE"); //$NON-NLS-1$
		save.setText(t);
		t = Messages.getString("GameSettingFrame.BUTTON_DISCARD"); //$NON-NLS-1$
		discardButton = new JButton(t);
		discardButton.addActionListener(this);
		discardButton.setIcon(LGM.getIconForKey("GameSettingFrame.BUTTON_DISCARD"));
		// make discard button the height as save, Win32 look and feel makes
		// buttons with icons 2x as tall
		discardButton.setMinimumSize(save.getMaximumSize());

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");

		tree = new JTree(new DefaultTreeModel(root));
		tree.setEditable(false);
		//tree.expandRow(0);
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

		buildTabs(root);

		// reload after adding all root children to make sure its children are visible
		((DefaultTreeModel)tree.getModel()).reload();

		tree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
													 tree.getLastSelectedPathComponent();

				// if nothing is selected
				if (node == null) return;

				// retrieve the node that was selected
				String nodeInfo = node.getUserObject().toString();

				CardLayout cl = (CardLayout)(cardPane.getLayout());
				cl.show(cardPane, nodeInfo);
			}
		});

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,
				new JScrollPane(tree),cardPane);
		split.setDividerLocation(200);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(split)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addContainerGap()
		/*		*/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(discardButton,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addContainerGap()));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(split)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(save)
		/*		*/.addComponent(discardButton))
		/**/.addContainerGap());
		pack();
		this.setSize(600,500);
		}

	private DefaultMutableTreeNode buildTab(DefaultMutableTreeNode root, String key, JComponent pane) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(Messages.getString(key));
		root.add(node);
		if (pane != null) {
			pane.setName(key);
			cardPane.add(Messages.getString(key),pane);
		}
		return node;
	}

	private void buildTabs(DefaultMutableTreeNode root)
		{
		cardPane = new JPanel(new CardLayout());

		buildTab(root, "GameSettingFrame.TAB_GRAPHICS", makeGraphicsPane());
		buildTab(root, "GameSettingFrame.TAB_RESOLUTION", makeResolutionPane());
		buildTab(root, "GameSettingFrame.TAB_OTHER", makeOtherPane());
		buildTab(root, "GameSettingFrame.TAB_LOADING", makeLoadingPane());
		buildTab(root, "GameSettingFrame.TAB_INCLUDE", makeIncludePane());
		buildTab(root, "GameSettingFrame.TAB_ERRORS", makeErrorPane());
		buildTab(root, "GameSettingFrame.TAB_INFO", makeInfoPane());
		buildTab(root, "GameSettingFrame.TAB_TEXTUREATLASES", makeTextureAtlasesPane());

		DefaultMutableTreeNode pnode = buildTab(root, "GameSettingFrame.TAB_PLATFORMS", null);

		buildTab(pnode, "GameSettingFrame.TAB_WINDOWS", makeWindowsPane());
		buildTab(pnode, "GameSettingFrame.TAB_MAC", null);
		buildTab(pnode, "GameSettingFrame.TAB_UBUNTU", null);
		}

	public void actionPerformed(ActionEvent e)
		{
		super.actionPerformed(e);

		if (e.getSource() == discardButton)
			{
			revertResource();
			close();
			return;
			}
		//TODO: icky way of getting the selected index
		String name = null;
		for (Component comp : cardPane.getComponents()) {
			name = comp.getName();
			if (comp.isVisible() == true && name != null) {
				break;
			}
		}
		if (name == null) return;
		if (name.endsWith(".TAB_GRAPHICS")) {
			if (e.getSource() instanceof JRadioButton) scale.setEnabled(scaling.getValue() > 0);
		} else if (name.endsWith(".TAB_RESOLUTION")) {
			resolutionPane.setVisible(setResolution.isSelected());
		} else if (name.endsWith(".TAB_LOADING")) {
			loadActionPerformed(e);
		} else if (name.endsWith(".TAB_WINDOWS")) {
			windowsActionPerformed(e);
		}

		}

	private void windowsActionPerformed(ActionEvent e)
		{
			if (e.getSource() == changeIcon)
				{
				if (iconFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
					{
					File f = iconFc.getSelectedFile();
					if (f.exists()) try
						{
						FileInputStream fis = new FileInputStream(f);
						gameIcon = new ICOFile(fis);
						fis.close();
						setIconPreviewToGameIcon();
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
		else if (e.getSource() == randomise)
			{
			gameId.setValue(new Random().nextInt(100000001));
			}
		}

	public void commitChanges()
		{
		// in GMX this is two options binded together into one value
		//res.put(PGameSettings.FORCE_SOFTWARE_VERTEX_PROCESSING,softwareVertexProcessing.is);
		res.put(PGameSettings.SCALING,scaling.getValue() > 0 ? scale.getIntValue() : scaling.getValue());
		res.put(PGameSettings.LOADING_IMAGE,customLoadingImage);
		res.put(PGameSettings.BACK_LOAD_BAR,backLoadImage);
		res.put(PGameSettings.FRONT_LOAD_BAR,frontLoadImage);
		res.put(PGameSettings.GAME_ICON,gameIcon);
		// we don't update the lastChanged time - that's only altered on file save/load
		}

	public void setComponents(GameSettings g)
		{
		int s = g.get(PGameSettings.SCALING);
		scaling.setValue(s > 1 ? 1 : s);
		if (s > 1) scale.setValue(s);
		scale.setEnabled(s > 0);
		lastChanged.setText(ProjectFile.gmTimeToString(g.getLastChanged()));

		customLoadingImage = g.get(PGameSettings.LOADING_IMAGE);
		backLoadImage = g.get(PGameSettings.BACK_LOAD_BAR);
		frontLoadImage = g.get(PGameSettings.FRONT_LOAD_BAR);
		gameIcon = g.get(PGameSettings.GAME_ICON);
		setIconPreviewToGameIcon();
		imagesChanged = true;
		}

	@Override
	public String getConfirmationName()
		{
		return getTitle();
		}

	@Override
	public boolean resourceChanged()
		{
		// NOTE: commit changes must be the first line because if we don't
		// the method will be flagged that we handled committing ourselves,
		// and the changes wont actually get committed.
		commitChanges();
		if (frameListener != null && frameListener.resourceChanged()) return true;
		if (imagesChanged) return true;
		return !res.properties.equals(resOriginal.properties);
		}

	@Override
	public void revertResource()
		{
		if (frameListener != null) frameListener.revertResource();
		res.properties.putAll(resOriginal.properties);
		setComponents(res);
		plf.setMap(res.properties);
		imagesChanged = false;
		}

	@Override
	public void updateResource(boolean commit)
		{
		super.updateResource(commit);
		imagesChanged = false;
		}
	}
