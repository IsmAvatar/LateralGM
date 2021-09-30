/*
 * Copyright (C) 2008, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import org.lateralgm.components.EffectsFrame;
import org.lateralgm.components.EffectsFrame.EffectsFrameListener;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.NumberField.ValueChangeEvent;
import org.lateralgm.components.NumberField.ValueChangeListener;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.SpriteStripDialog;
import org.lateralgm.components.visual.SubimagePreview;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.FileChooser.FileDropHandler;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.BBMode;
import org.lateralgm.resources.Sprite.MaskShape;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.resources.sub.TextureGroup;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.KeyComboBoxConversion;
import org.lateralgm.ui.swing.util.ArrayComboBoxModel;
import org.lateralgm.ui.swing.util.SwingExecutor;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class SpriteFrame extends InstantiableResourceFrame<Sprite,PSprite> implements
		UpdateListener,ValueChangeListener,ClipboardOwner,EffectsFrameListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon PLAY_ICON = LGM.getIconForKey("SpriteFrame.PLAY"); //$NON-NLS-1$
	private static final ImageIcon STOP_ICON = LGM.getIconForKey("SpriteFrame.STOP"); //$NON-NLS-1$

	//toolbar
	public JButton load, loadSubimages, loadStrip, saveSubimages, zoomIn, zoomOut;
	public JToggleButton zoomButton;

	//origin
	public NumberField originX, originY;
	public JButton centre;

	//bbox
	public NumberField bboxLeft, bboxRight;
	public NumberField bboxTop, bboxBottom;

	//properties
	public JCheckBox smooth, preload, transparent, separateMasks;
	public JLabel statusLabel;

	//subimages
	public JList<ImageIcon> subList;

	//preview
	public JScrollPane previewScroll, subimagesScroll;
	public SubimagePreview preview;
	public NumberField show, speed;
	public JButton subLeft, subRight, play, cut, copy, paste;
	public JLabel showLab;
	public int currSub;
	public JCheckBox showBbox, showOrigin;
	public JCheckBox wrapBox, shiftBox;

	public boolean imageChanged = false;
	public JSplitPane splitPane;

	/** Used for animation, or null when not animating */
	public Timer timer;

	/** Prevents <code>show</code> from resetting when it changes */
	private boolean updateSub = true;

	private final SpritePropertyListener spl = new SpritePropertyListener();

	private Map<BufferedImage,ImageEditor> editors;
	private MouseAdapter previewMouseAdapter;

	public SpriteFrame(Sprite res, ResNode node)
		{
		super(res,node);
		this.getRootPane().setDefaultButton(save);

		res.properties.getUpdateSource(PSprite.BB_MODE).addListener(spl);
		res.reference.updateSource.addListener(this);

		setLayout(new BorderLayout());

		final JSplitPane previewPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,
				makePreviewPane(),makeSubimagesPane());
		previewPane.setResizeWeight(1);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true);
		Util.orientSplit(splitPane,Prefs.rightOrientation,makePropertiesPane(),previewPane);
		add(splitPane,BorderLayout.CENTER);

		previewMouseAdapter = new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent ev)
					{
					if (ev.getButton() == MouseEvent.BUTTON1)
						{
						preview.setCursor(LGM.zoomInCursor);
						}
					if (ev.getButton() == MouseEvent.BUTTON3)
						{
						preview.setCursor(LGM.zoomOutCursor);
						}
					}

				@Override
				public void mouseReleased(MouseEvent ev)
					{
					if (ev.getButton() == MouseEvent.BUTTON1)
						{
						preview.zoomIn(ev.getPoint(),previewScroll);
						}
					if (ev.getButton() == MouseEvent.BUTTON3)
						{
						preview.zoomOut(ev.getPoint(),previewScroll);
						}
					preview.setCursor(LGM.zoomCursor);
					}
			};

		updateImageList();
		updateStatusLabel();

		pack();
		// this must be here or for some reason l.setPreferredSize() for the subimage list
		// will cause a height of 13,000 with some elongated subimages (8 x 56 as an example)
		// setFixedCellWidth/Height is not an alternative because it does not work with subimages
		// of varying dimensions
		this.setSize(getWidth(),586);
		SwingUtilities.invokeLater(new Runnable()
			{
			@Override
			public void run()
				{
				previewPane.setDividerLocation(0.6d);
				}
			});
		updateScrollBars();
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);

		zoomButton = new JToggleButton(LGM.getIconForKey("SpriteFrame.ZOOM")); //$NON-NLS-1$
		zoomButton.setToolTipText(Messages.getString("SpriteFrame.ZOOM")); //$NON-NLS-1$
		zoomButton.addActionListener(this);
		tool.add(zoomButton);

		zoomIn = makeJButton("SpriteFrame.ZOOM_IN"); //$NON-NLS-1$
		zoomOut = makeJButton("SpriteFrame.ZOOM_OUT"); //$NON-NLS-1$
		tool.add(zoomIn);
		tool.add(zoomOut);

		tool.addSeparator();

		showBbox = new JCheckBox(Messages.getString("SpriteFrame.SHOW_BBOX"),true); //$NON-NLS-1$
		showBbox.addActionListener(this);
		showBbox.setOpaque(false);
		tool.add(showBbox);
		showOrigin = new JCheckBox(Messages.getString("SpriteFrame.SHOW_ORIGIN"),true); //$NON-NLS-1$
		showOrigin.addActionListener(this);
		showOrigin.setOpaque(false);
		tool.add(showOrigin);

		tool.addSeparator();
		JLabel lab2 = new JLabel(Messages.getString("SpriteFrame.ANIM_SPEED")); //$NON-NLS-1$
		tool.add(lab2);

		speed = new NumberField(1,Integer.MAX_VALUE,30);
		speed.setColumns(10);
		speed.setMaximumSize(speed.getPreferredSize());
		speed.setToolTipText(Messages.getString("SpriteFrame.CALC_TIP")); //$NON-NLS-1$
		speed.addValueChangeListener(this);
		speed.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
					{
					//works for all mouse buttons
					if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0)
						{
						showSpeedDialog();
						return;
						}
					}
			});
		tool.add(speed);
		play = new JButton(PLAY_ICON);
		play.addActionListener(this);
		tool.add(play);

		return tool;
		}

	private void updateStatusLabel()
		{
		String stat = " " + Messages.getString("SpriteFrame.WIDTH") + ": " + res.getWidth() + " | "
				+ Messages.getString("SpriteFrame.HEIGHT") + ": " + res.getHeight() + " | "
				+ Messages.getString("SpriteFrame.NO_OF_SUBIMAGES") + ": " + res.subImages.size() + " | "
				+ Messages.getString("SpriteFrame.MEMORY") + ": ";

		if (res.subImages != null)
			{
			stat += Util.formatDataSize(res.subImages.getSize());
			}
		else
			{
			stat += Util.formatDataSize(0);
			}

		String zoom = new DecimalFormat("#,##0.##").format(getZoom() * 100);
		stat += " | " + Messages.getString("SpriteFrame.ZOOM") + ": " + zoom + "%";

		statusLabel.setText(stat);
		}

	private JPanel makeStatusBar()
		{
		JPanel status = new JPanel(new FlowLayout());
		BoxLayout layout = new BoxLayout(status,BoxLayout.X_AXIS);
		status.setLayout(layout);
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));

		statusLabel = new JLabel();

		status.add(statusLabel);

		return status;
		}

	private JPanel makeOriginPane()
		{
		JPanel pane = new JPanel();
		GroupLayout oLayout = new GroupLayout(pane);
		oLayout.setAutoCreateGaps(true);
		//		oLayout.setAutoCreateContainerGaps(true);
		pane.setLayout(oLayout);
		pane.setBorder(BorderFactory.createTitledBorder(Messages.getString("SpriteFrame.ORIGIN"))); //$NON-NLS-1$

		JLabel oxLab = new JLabel(Messages.getString("SpriteFrame.X")); //$NON-NLS-1$
		oxLab.setHorizontalAlignment(SwingConstants.RIGHT);
		originX = new NumberField(0);
		originX.setColumns(4);
		plf.make(originX,PSprite.ORIGIN_X);
		JLabel oyLab = new JLabel(Messages.getString("SpriteFrame.Y")); //$NON-NLS-1$;
		oyLab.setHorizontalAlignment(SwingConstants.RIGHT);
		originY = new NumberField(0);
		originY.setColumns(4);
		plf.make(originY,PSprite.ORIGIN_Y);
		centre = new JButton(Messages.getString("SpriteFrame.CENTER")); //$NON-NLS-1$
		centre.addActionListener(this);

		// The empty comments here prevent the formatter from messing up line wrapping and indentation.
		oLayout.setHorizontalGroup(oLayout.createParallelGroup(Alignment.CENTER)
		/**/.addGroup(oLayout.createSequentialGroup()
		/*	*/.addGap(12)
		/*	*/.addComponent(oxLab)
		/*	*/.addGap(4)
		/*	*/.addComponent(originX)
		/*	*/.addGap(12)
		/*	*/.addComponent(oyLab)
		/*	*/.addGap(4)
		/*	*/.addComponent(originY)
		/*	*/.addGap(12))
		/**/.addComponent(centre));

		oLayout.setVerticalGroup(oLayout.createSequentialGroup()
		/**/.addGroup(oLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(oxLab)
		/*	*/.addComponent(originX)
		/*	*/.addComponent(oyLab)
		/*	*/.addComponent(originY))
		/**/.addGap(8)
		/**/.addComponent(centre)
		/**/.addGap(8));

		return pane;
		}

	private JPanel makeCollisionPane()
		{
		JPanel pane = new JPanel();
		GroupLayout cLayout = new GroupLayout(pane);
		cLayout.setAutoCreateGaps(true);
		cLayout.setAutoCreateContainerGaps(true);
		pane.setLayout(cLayout);

		JLabel toleranceLabel = new JLabel(
				Messages.getString("SpriteFrame.ALPHA_TOLERANCE")); //$NON-NLS-1$
		NumberField tolerance = new NumberField(0, 255);
		plf.make(tolerance, PSprite.ALPHA_TOLERANCE);
		JSlider toleranceSlider = new JSlider(0, 255);
		plf.make(toleranceSlider.getModel(),PSprite.ALPHA_TOLERANCE);

		// The options must be added in the order corresponding to Sprite.BBMode
		final String bboxOptions[] = { "SpriteFrame.AUTO", //$NON-NLS-1$
				"SpriteFrame.FULL","SpriteFrame.MANUAL" }; //$NON-NLS-1$ //$NON-NLS-2$
		Messages.translate(bboxOptions);

		JLabel bboxLabel = new JLabel(Messages.getString("SpriteFrame.MASK_MODE")); //$NON-NLS-1$
		JComboBox<String> bboxCombo = new JComboBox<String>(bboxOptions);
		plf.make(bboxCombo,PSprite.BB_MODE,new KeyComboBoxConversion<BBMode>(ProjectFile.SPRITE_BB_MODE,
			ProjectFile.SPRITE_BB_CODE));

		// The options must be added in the order corresponding to Sprite.MaskShape
		final String shapeOptions[] = { "SpriteFrame.PRECISE", //$NON-NLS-1$
				"SpriteFrame.RECTANGLE","SpriteFrame.DISK", //$NON-NLS-1$ //$NON-NLS-2$
				"SpriteFrame.DIAMOND" }; //$NON-NLS-1$ //$NON-NLS-2$
		//TODO: what the fuck "SpriteFrame.POLYGON"
		Messages.translate(shapeOptions);

		JLabel shapeLabel = new JLabel(Messages.getString("SpriteFrame.MASK_TYPE")); //$NON-NLS-1$
		JComboBox<String> shapeCombo = new JComboBox<String>(shapeOptions);
		plf.make(shapeCombo,PSprite.SHAPE,new KeyComboBoxConversion<MaskShape>(ProjectFile.SPRITE_MASK_SHAPE,
			ProjectFile.SPRITE_MASK_CODE));

		JLabel lLab = new JLabel(Messages.getString("SpriteFrame.LEFT")); //$NON-NLS-1$
		lLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxLeft = new NumberField(0);
		bboxLeft.setColumns(3);
		plf.make(bboxLeft,PSprite.BB_LEFT);

		JLabel rLab = new JLabel(Messages.getString("SpriteFrame.RIGHT")); //$NON-NLS-1$
		rLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxRight = new NumberField(0);
		bboxRight.setColumns(3);
		plf.make(bboxRight,PSprite.BB_RIGHT);

		JLabel tLab = new JLabel(Messages.getString("SpriteFrame.TOP")); //$NON-NLS-1$
		tLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxTop = new NumberField(0);
		bboxTop.setColumns(3);
		plf.make(bboxTop,PSprite.BB_TOP);

		JLabel bLab = new JLabel(Messages.getString("SpriteFrame.BOTTOM")); //$NON-NLS-1$
		bLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxBottom = new NumberField(0);
		bboxBottom.setColumns(3);
		plf.make(bboxBottom,PSprite.BB_BOTTOM);

		updateBoundingBoxEditors();

		cLayout.setHorizontalGroup(cLayout.createParallelGroup()
		/**/.addGroup(cLayout.createSequentialGroup()
		/*	*/.addGroup(cLayout.createParallelGroup()
		/*		*/.addComponent(bboxLabel)
		/*		*/.addComponent(shapeLabel))
		/*	*/.addGroup(cLayout.createParallelGroup()
		/*		*/.addComponent(bboxCombo)
		/*		*/.addComponent(shapeCombo)))
		/**/.addGroup(cLayout.createSequentialGroup()
		/*	*/.addComponent(toleranceLabel))
		/**/.addGroup(cLayout.createSequentialGroup()
		/*	*/.addComponent(toleranceSlider, 0, 0, Short.MAX_VALUE)
		/*	*/.addComponent(tolerance, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(cLayout.createSequentialGroup()
		/*	*/.addGroup(cLayout.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(lLab)
		/*		*/.addComponent(tLab))
		/*	*/.addGroup(cLayout.createParallelGroup()
		/*		*/.addComponent(bboxLeft, PREFERRED_SIZE, PREFERRED_SIZE, DEFAULT_SIZE)
		/*		*/.addComponent(bboxTop, PREFERRED_SIZE, PREFERRED_SIZE, DEFAULT_SIZE))
		/*	*/.addGroup(cLayout.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(rLab)
		/*		*/.addComponent(bLab))
		/*	*/.addGroup(cLayout.createParallelGroup()
		/*		*/.addComponent(bboxRight, PREFERRED_SIZE, PREFERRED_SIZE, DEFAULT_SIZE)
		/*		*/.addComponent(bboxBottom, PREFERRED_SIZE, PREFERRED_SIZE, DEFAULT_SIZE))));
		cLayout.setVerticalGroup(cLayout.createSequentialGroup()
		/**/.addGroup(cLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(bboxLabel)
		/*	*/.addComponent(bboxCombo,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/**/.addGroup(cLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(shapeLabel)
		/*	*/.addComponent(shapeCombo,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/**/.addGroup(cLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(toleranceLabel))
		/**/.addGroup(cLayout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(toleranceSlider)
		/*	*/.addComponent(tolerance, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
		/**/.addGroup(cLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lLab)
		/*	*/.addComponent(bboxLeft)
		/*	*/.addComponent(rLab)
		/*	*/.addComponent(bboxRight))
		/**/.addGroup(cLayout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(tLab)
		/*	*/.addComponent(bboxTop)
		/*	*/.addComponent(bLab)
		/*	*/.addComponent(bboxBottom)));

		return pane;
		}

	private JPanel makeTexturePane()
		{
		JPanel pane = new JPanel();
		GroupLayout tLayout = new GroupLayout(pane);
		tLayout.setAutoCreateGaps(true);
		tLayout.setAutoCreateContainerGaps(true);
		pane.setLayout(tLayout);

		JCheckBox usedFor3D = new JCheckBox("Used for 3D");
		plf.make(usedFor3D,PSprite.FOR3D);
		JCheckBox tileH = new JCheckBox("Tile Horizontal");
		plf.make(tileH,PSprite.TILE_HORIZONTALLY);
		JCheckBox tileV = new JCheckBox("Tile Vertical");
		plf.make(tileV,PSprite.TILE_VERTICALLY);
		JLabel groupLabel = new JLabel("Group:");
		JComboBox<TextureGroup> groupCombo = new JComboBox<TextureGroup>();
		groupCombo.setModel(new ArrayComboBoxModel<>(LGM.getSelectedConfig().textureGroups));

		tLayout.setHorizontalGroup(tLayout.createParallelGroup()
		/**/.addComponent(usedFor3D)
		/**/.addComponent(tileH)
		/**/.addComponent(tileV)
		/**/.addComponent(groupLabel)
		/**/.addComponent(groupCombo));
		tLayout.setVerticalGroup(tLayout.createSequentialGroup()
		/**/.addComponent(usedFor3D)
		/**/.addComponent(tileH)
		/**/.addComponent(tileV)
		/**/.addComponent(groupLabel)
		/**/.addComponent(groupCombo,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE));

		return pane;
		}

	private JPanel makePropertiesPane()
		{
		JPanel pane = new JPanel();
		GroupLayout layout = new GroupLayout(pane);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		pane.setLayout(layout);

		smooth = new JCheckBox(Messages.getString("SpriteFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PSprite.SMOOTH_EDGES);
		preload = new JCheckBox(Messages.getString("SpriteFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PSprite.PRELOAD);
		transparent = new JCheckBox(Messages.getString("SpriteFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setToolTipText(Messages.getString("SpriteFrame.TRANSP_TIP")); //$NON-NLS-1$
		plf.make(transparent,PSprite.TRANSPARENT);
		separateMasks = new JCheckBox(Messages.getString("SpriteFrame.SEPARATE")); //$NON-NLS-1$
		separateMasks.setToolTipText(Messages.getString("SpriteFrame.SEPARATE_TIP")); //$NON-NLS-1$
		plf.make(separateMasks,PSprite.SEPARATE_MASK);

		JPanel origin = makeOriginPane();
		JPanel mask = makeCollisionPane();
		JPanel text = makeTexturePane();
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Collision",mask);
		tabPane.addTab("Texture",text);

		JLabel nameLabel = new JLabel(Messages.getString("SpriteFrame.NAME")); //$NON-NLS-1$
		save.setText(Messages.getString("SpriteFrame.SAVE")); //$NON-NLS-1$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(nameLabel)
		/*	*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(separateMasks)
		/**/.addComponent(origin)
		/**/.addComponent(tabPane)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(nameLabel)
		/*	*/.addComponent(name))
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(separateMasks)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addComponent(origin)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addComponent(tabPane,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED,0,MAX_VALUE)
		/**/.addComponent(save));

		return pane;
		}

	public class ImageLabel extends JLabel
		{
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = 749151178684203437L;
		BufferedImage img;
		int index = -1;
		JList<ImageIcon> list;

		public void paintComponent(Graphics g)
			{
			g.drawImage(img,0,0,this.getWidth() - 1,this.getHeight() - 1,null);
			if (list.isSelectedIndex(index))
				{
				g.setColor(list.getSelectionBackground());
				g.drawRect(0,0,this.getWidth() - 1,this.getHeight() - 1);
				}
			}
		}

	BufferedImage transparencyBackground = null;

	public class ImageCellRenderer implements ListCellRenderer<ImageIcon>
		{
		private final JList<ImageIcon> list;

		public ImageCellRenderer(JList<ImageIcon> l)
			{
			super();
			this.list = l;
			}

		public Component getListCellRendererComponent(final JList<? extends ImageIcon> genericlist,
				final ImageIcon value, final int index, final boolean isSelected, final boolean hasFocus)
			{
			//create panel
			final JPanel p = new JPanel(new BorderLayout(0,0));
			//p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
			final ImageLabel l = new ImageLabel(); //<-- this will be an icon instead of a text
			BufferedImage img = res.subImages.get(index);

			if (img == null)
				{
				return null;
				}
			int imgwidth = img.getWidth();
			int imgheight = img.getHeight();
			int width = 64, height = 64;
			if (imgheight < imgwidth)
				{
				width = (int)(height / (float)imgheight * imgwidth);
				}
			else if (imgwidth < imgheight)
				{
				height = (int)(width / (float)imgwidth * imgheight);
				}
			//subList.setFixedCellWidth(width+1);
			//subList.setFixedCellHeight(height+1);
			l.setPreferredSize(new Dimension(width+1,height+1));

			if ((Boolean) res.get(PSprite.TRANSPARENT))
				{
				img = Util.getTransparentImage(img);
				}
			int bwidth = (int)Math.ceil(width/10f);
			int bheight = (int)Math.ceil(height/10f);
			bwidth = bwidth < 1 ? 1 : bwidth;
			bheight = bheight < 1 ? 1 : bheight;
			if (transparencyBackground == null ||
				transparencyBackground.getWidth() != bwidth ||
				transparencyBackground.getHeight() != bheight)
				{
				transparencyBackground = Util.paintBackground(bwidth, bheight);
				}

			BufferedImage cimg = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = cimg.createGraphics();
			g.drawImage(transparencyBackground,0,0,bwidth*10,bheight*10,null);
			g.drawImage(img,0,0,width,height,null);
			g.dispose();

			l.img = cimg;

			l.index = index;
			l.list = list;
			p.add(l);

			return p;
			}
		}

	private JButton makeJButton(String key)
		{
		JButton but = new JButton(LGM.getIconForKey(key));
		but.setToolTipText(Messages.getString(key));
		but.addActionListener(this);
		but.setActionCommand(key);
		return but;
		}

	private JMenuItem makeJMenuItem(String key)
		{
		JMenuItem but = new JMenuItem(LGM.getIconForKey(key));
		but.setText(Messages.getString(key));
		but.addActionListener(this);
		but.setActionCommand(key);
		return but;
		}

	private JPanel makeSubimagesPane()
		{
		JPanel pane = new JPanel(new BorderLayout());
		//prevents resizing on large subimages with size(1,1)
		//pane.setPreferredSize(pane.getMinimumSize());

		final JPopupMenu popup = new JPopupMenu();

		popup.add(makeJMenuItem("SpriteFrame.EDIT")); //$NON-NLS-1$

		popup.addSeparator();

		popup.add(makeJMenuItem("SpriteFrame.SELECT_ALL")); //$NON-NLS-1$

		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		pane.add(tool,BorderLayout.NORTH);

		load = makeJButton("SpriteFrame.LOAD"); //$NON-NLS-1$
		loadSubimages = makeJButton("SpriteFrame.LOAD_SUBIMAGE"); //$NON-NLS-1$
		loadStrip = makeJButton("SpriteFrame.LOAD_STRIP"); //$NON-NLS-1$
		saveSubimages = makeJButton("SpriteFrame.SAVE_SUBIMAGE"); //$NON-NLS-1$

		tool.add(makeJButton("SpriteFrame.ADD")); //$NON-NLS-1$
		tool.add(load);
		tool.add(loadSubimages);
		tool.add(loadStrip);
		tool.add(saveSubimages);

		tool.addSeparator();

		tool.add(makeJButton("SpriteFrame.REMOVE")); //$NON-NLS-1$

		tool.addSeparator();

		tool.add(makeJButton("SpriteFrame.EDIT")); //$NON-NLS-1$
		tool.add(makeJButton("SpriteFrame.EFFECT")); //$NON-NLS-1$

		tool.addSeparator();

		tool.add(makeJButton("SpriteFrame.CUT")); //$NON-NLS-1$
		tool.add(makeJButton("SpriteFrame.COPY")); //$NON-NLS-1$
		tool.add(makeJButton("SpriteFrame.PASTE")); //$NON-NLS-1$

		tool.addSeparator();

		subLeft = new JButton(LGM.getIconForKey("SpriteFrame.PREVIOUS")); //$NON-NLS-1$
		subLeft.addActionListener(this);
		tool.add(subLeft);

		show = new NumberField(0,res.subImages.size() - 1);
		show.setHorizontalAlignment(SwingConstants.CENTER);
		show.addValueChangeListener(this);
		show.setColumns(4);
		show.setMaximumSize(show.getPreferredSize());
		show.setMinimumSize(show.getPreferredSize());
		//		show.setValue(0);
		tool.add(show);

		subRight = new JButton(LGM.getIconForKey("SpriteFrame.NEXT")); //$NON-NLS-1$
		subRight.addActionListener(this);
		tool.add(subRight);

		//JLabel lab = new JLabel(Messages.getString("SpriteFrame.ANIM_SUBIMG")); //$NON-NLS-1$

		shiftBox = new JCheckBox(Messages.getString("SpriteFrame.SHIFT"),true);
		shiftBox.setSelected(false);
		shiftBox.setOpaque(false);
		tool.add(shiftBox);
		wrapBox = new JCheckBox(Messages.getString("SpriteFrame.WRAP"),true);
		wrapBox.setOpaque(false);
		wrapBox.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent arg0)
					{
					if (!wrapBox.isSelected())
						{
						subLeft.setEnabled(timer == null && currSub > 0);
						subRight.setEnabled(timer == null && currSub < res.subImages.size() - 1);
						}
					else
						{
						subLeft.setEnabled(true);
						subRight.setEnabled(true);
						}
					}
			});
		tool.add(wrapBox);

		subList = new JList<ImageIcon>();
		subList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		subList.setVisibleRowCount(-1);
		subList.setBackground(Color.LIGHT_GRAY);
		subList.setDragEnabled(true);
		subList.setDropMode(DropMode.INSERT);
		subList.setTransferHandler(new SubImageTransfer());
		subList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
				{
				Object s = e.getSource();
				if (e.getClickCount() == 2 && s == subList)
					{
					int i = subList.getSelectedIndex();
					if (i == -1 || i >= res.subImages.size()) return;
					editSubimage(res.subImages.get(i));
					}
				}
		});
		subList.setDragEnabled(true);

		subList.addListSelectionListener(new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent ev)
					{
					EffectsFrame.getInstance().setEffectsListener(SpriteFrame.this, getSelectedImages());
					int ind = subList.getSelectedIndex();
					if (ind < 0) return;
					if (timer == null)
						{
						setSubIndex(ind);
						}
					}
			});

		subList.setCellRenderer(new ImageCellRenderer(subList));
		subList.setComponentPopupMenu(popup);

		subimagesScroll = new JScrollPane(subList);
		subimagesScroll.getVerticalScrollBar().setUnitIncrement(0);
		subimagesScroll.getHorizontalScrollBar().setUnitIncrement(0);
		subimagesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.add(subimagesScroll,BorderLayout.CENTER);

		pane.add(makeStatusBar(),BorderLayout.SOUTH);

		return pane;
		}

	class SubImageTransfer extends FileDropHandler implements Transferable
		{
		private static final long serialVersionUID = 1L;
		private final DataFlavor flavors[] = { DataFlavor.imageFlavor };
		BufferedImage data;

		public int getSourceActions(JComponent c)
			{
			return COPY_OR_MOVE;
			}

		public DataFlavor[] getTransferDataFlavors()
			{
			return flavors;
			}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException
			{
			if (flavor != DataFlavor.imageFlavor) throw new UnsupportedFlavorException(flavor);
			return Util.cloneImage(data);
			}

		public Transferable createTransferable(JList<ImageIcon> c)
			{
			JList<ImageIcon> l = ((JList<ImageIcon>) c);
			int index = l.getSelectedIndex();
			if (index == -1) return null;
			data = res.subImages.get(index);
			return this;
			}

		public void exportDone(JComponent c, Transferable t, int action)
			{
			if (action == MOVE) res.subImages.remove(data);
			}

		public boolean isDataFlavorSupported(DataFlavor df)
			{
			if (super.isDataFlavorSupported(df)) return true;
			return df == DataFlavor.imageFlavor;
			}

		public boolean importData(TransferHandler.TransferSupport evt)
			{
			List<BufferedImage> bi = new LinkedList<BufferedImage>();

			try
				{
				if (evt.isDataFlavorSupported(DataFlavor.imageFlavor))
					{
					BufferedImage b = (BufferedImage) evt.getTransferable().getTransferData(
							DataFlavor.imageFlavor);
					if (b != null) bi.add(b);
					//otherwise, we'll see if there's a list flavor
					//(Yeah right, as if anybody else uses imageFlavor)
					}

				if (bi.isEmpty())
					{
					List<?> files = getDropList(evt);
					if (files == null || files.isEmpty()) return false;
					for (Object o : files)
						{
						ImageInputStream iis = null;
						if (o instanceof File) iis = ImageIO.createImageInputStream(o);
						if (o instanceof URI)
							iis = ImageIO.createImageInputStream(((URI) o).toURL().openStream());
						BufferedImage bia[] = Util.getValidImages(iis);
						if (files.size() != 1 && bia.length > 1) return false;
						Collections.addAll(bi,bia);
						}
					}
				}
			catch (Exception e)
				{
				//Bastard lied to us
				e.printStackTrace();
				}

			if (bi.isEmpty()) return false;

			int index = -1;
			if (evt.isDrop())
				{
				JList.DropLocation loc = (JList.DropLocation) evt.getDropLocation();
				index = loc.getIndex();
				if (!loc.isInsert()) res.subImages.remove(index);
				System.out.println(loc.isInsert());
				}
			if (index < 0) index = res.subImages.size();

			for (BufferedImage b : bi)
				res.subImages.add(index++,b);
			return true;
			}
		}

	private JPanel makePreviewPane()
		{
		JPanel pane = new JPanel(new BorderLayout());

		preview = new SubimagePreview(res);
		previewScroll = new JScrollPane(preview);
		previewScroll.setPreferredSize(previewScroll.getSize());

		pane.add(makeToolBar(),BorderLayout.NORTH);
		pane.add(previewScroll,BorderLayout.CENTER);

		return pane;
		}

	private void showSpeedDialog()
		{
		JPanel p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		p.setLayout(layout);

		JLabel caption = new JLabel(Messages.getString("SpriteFrame.CALC_CAPTION")); //$NON-NLS-1$
		JLabel lrs = new JLabel(Messages.getString("SpriteFrame.CALC_ROOM_SPEED")); //$NON-NLS-1$
		JLabel lis = new JLabel(Messages.getString("SpriteFrame.CALC_IMAGE_SPEED")); //$NON-NLS-1$
		NumberField rs = new NumberField(1,Integer.MAX_VALUE,speed.getIntValue());
		JTextField is = new JTextField("1.0"); //$NON-NLS-1$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(caption,Alignment.CENTER)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(lrs)
		/*		*/.addComponent(lis))
		/*	*/.addGap(5)
		/*	*/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(rs)
		/*		*/.addComponent(is))));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/*	*/.addComponent(caption)
		/*	*/.addGap(5)
		/*	*/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(lrs)
		/*		*/.addComponent(rs))
		/*	*/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(lis)
		/*		*/.addComponent(is)));

		JOptionPane.showMessageDialog(this,p);

		int i = rs.getIntValue();
		double d = 1.0;
		try
			{
			d = Double.parseDouble(is.getText());
			}
		catch (NumberFormatException nfe)
			{
			//d = 1.0
			}
		speed.setValue((int) (i * d));
		//triggers listener
		}

	protected boolean areResourceFieldsEqual()
		{
		return !imageChanged;
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		}

	public void updateResource(boolean commit)
		{
		super.updateResource(commit);
		imageChanged = false;
		}

	public void valueChange(ValueChangeEvent e)
		{
		if (e.getSource() == show)
			{
			subList.setSelectedIndex(show.getIntValue());
			return;
			}
		if (e.getSource() == speed)
			{
			if (timer != null) timer.setDelay(1000 / speed.getIntValue());
			return;
			}
		}

	private static class ClipboardImages
		{
		List<BufferedImage> bi;

		public ClipboardImages(List<BufferedImage> images)
			{
			bi = images;
			}
		}

	private static DataFlavor imgClipFlavor = new DataFlavor(ClipboardImages.class,
			"Buffered Images Clipboard");

	private static class TransferableImages implements Transferable
		{
		ClipboardImages ci;

		public TransferableImages(ClipboardImages images)
			{
			this.ci = images;
			}

		public DataFlavor[] getTransferDataFlavors()
			{
			DataFlavor[] ret = { imgClipFlavor };
			return ret;
			}

		public boolean isDataFlavorSupported(DataFlavor flavor)
			{
			return imgClipFlavor.equals(flavor);
			}

		public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
			{
			if (isDataFlavorSupported(flavor))
				{
				return this.ci;
				}
			else
				{
				throw new UnsupportedFlavorException(imgClipFlavor);
				}
			}
		}

	private BufferedImage createNewImage(boolean askforsize)
		{
		int width = res.getWidth();
		int height = res.getHeight();
		if (width == 0 || height == 0)
			{
			width = 32;
			height = 32;
			}
		if (askforsize)
			{
			NumberFormatter nf = new NumberFormatter();
			nf.setMinimum(1);
			JFormattedTextField wField = new JFormattedTextField(nf);
			wField.setValue(width);
			JFormattedTextField hField = new JFormattedTextField(nf);
			hField.setValue(height);

			JPanel myPanel = new JPanel();
			GridLayout layout = new GridLayout(0,2,0,3);
			myPanel.setLayout(layout);
			myPanel.add(new JLabel(Messages.getString("SpriteFrame.NEW_WIDTH")));
			myPanel.add(wField);
			myPanel.add(new JLabel(Messages.getString("SpriteFrame.NEW_HEIGHT")));
			myPanel.add(hField);

			int result = JOptionPane.showConfirmDialog(this,
					myPanel,Messages.getString("SpriteFrame.NEW_TITLE"),JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				{
				return null;
				}

			width = (Integer) wField.getValue();
			height = (Integer) hField.getValue();
			}
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		imageChanged = true;

		return bi;
		}

	public void editActionsPerformed(String cmd)
		{
		int pos = subList.getSelectedIndex();
		pos = pos >= 0 ? pos + 1 : res.subImages.size();
		if (cmd.endsWith(".SELECT_ALL"))
			{
			subList.setSelectionInterval(0,res.subImages.size() - 1);
			return;
			}
		else if (cmd.endsWith(".CUT")) //$NON-NLS-1$
			{
			int[] selections = subList.getSelectedIndices();
			if (selections.length == 0)
				{
				return;
				}
			List<BufferedImage> images = new ArrayList<BufferedImage>(selections.length);
			for (int i = 0; i < selections.length; i++)
				{
				images.add(res.subImages.get(selections[i] - i));
				res.subImages.remove(selections[i] - i);
				}

			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(new TransferableImages(new ClipboardImages(images)),this);
			imageChanged = true;
			return;
			}
		else if (cmd.endsWith(".COPY")) //$NON-NLS-1$
			{
			int[] selections = subList.getSelectedIndices();
			List<BufferedImage> images = new ArrayList<BufferedImage>(selections.length);
			for (int i = 0; i < selections.length; i++)
				{
				images.add(res.subImages.get(selections[i]));
				}

			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(new TransferableImages(new ClipboardImages(images)),this);
			return;
			}
		else if (cmd.endsWith(".PASTE")) //$NON-NLS-1$
			{
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable content = clipboard.getContents(this);
			if (content.isDataFlavorSupported(imgClipFlavor))
				{
				ClipboardImages images = null;
				try
					{
					images = (ClipboardImages) content.getTransferData(imgClipFlavor);
					}
				catch (UnsupportedFlavorException e)
					{
					LGM.showDefaultExceptionHandler(e);
					}
				catch (IOException e)
					{
					LGM.showDefaultExceptionHandler(e);
					}
				imageChanged = true;
				for (int i = 0; i < images.bi.size(); i++)
					{
					res.subImages.add(pos + i, Util.cloneImage(images.bi.get(i)));
					}
				subList.setSelectionInterval(pos, pos + images.bi.size() - 1);
				}
			return;
			}
		else if (cmd.endsWith(".ADD")) //$NON-NLS-1$
			{
			BufferedImage bi = createNewImage(res.subImages.size() == 0);
			if (bi != null)
				{
				imageChanged = true;
				res.subImages.add(pos,bi);
				subList.setSelectedIndex(pos);
				setSubIndex(pos);
				}
			return;
			}
		else if (cmd.endsWith(".EDIT")) //$NON-NLS-1$
			{
			int[] selections = subList.getSelectedIndices();
			for (int i = 0; i < selections.length; i++)
				{
				editSubimage(res.subImages.get(selections[i]));
				}
			return;
			}
		else if (cmd.endsWith(".EFFECT")) { //$NON-NLS-1$
			EffectsFrame ef = EffectsFrame.getInstance();
			ef.setEffectsListener(this, getSelectedImages());
			ef.setVisible(true);
		}
		else if (cmd.endsWith(".REMOVE")) //$NON-NLS-1$
			{
			int[] selections = subList.getSelectedIndices();
			for (int i = 0; i < selections.length; i++)
				{
				ImageEditor ie = editors == null ? null : editors.get(res.subImages.get(selections[i] - i));
				imageChanged = true;
				res.subImages.remove(selections[i] - i);
				if (ie != null) ie.stop();
				}
			subList.setSelectedIndex(Math.min(res.subImages.size() - 1, pos));
			return;
			}
		}

	public double getZoom()
		{
		return preview.getZoom();
		}

	public void setZoom(double nzoom)
		{
		preview.setZoom(nzoom);
		updateStatusLabel();
		updateScrollBars();
		}

	public ArrayList<BufferedImage> getSelectedImages() {
		int[] selected = subList.getSelectedIndices();
		if (selected.length <= 0) {
			return res.subImages;
		} else {
			if (res.subImages.getSize() <= 0) return null;
			ArrayList<BufferedImage> subimages = new ArrayList<BufferedImage>(selected.length);
			for (int id : selected) {
				subimages.add(res.subImages.get(id));
			}
			return subimages;
		}

	}

	public void actionPerformed(ActionEvent e)
		{
		String cmd = e.getActionCommand();
		if (cmd != null)
			{
			editActionsPerformed(cmd);
			}
		int pos = subList.getSelectedIndex();
		if (e.getSource() == load)
			{
			BufferedImage[] img = Util.getValidImages();
			if (img != null) addSubimages(img,true);
			return;
			}
		else if (e.getSource() == loadStrip)
			{
			addFromStrip(false);
			return;
			}
		else if (e.getSource() == loadSubimages)
			{
			BufferedImage[] img = Util.getValidImages();
			if (img != null) addSubimages(img,false);
			return;
			}
		else if (e.getSource() == saveSubimages)
			{
			ArrayList<BufferedImage> imgs = getSelectedImages();
			if (imgs != null) {
				Util.saveImages(imgs);
			}
			return;
			}
		else if (e.getSource() == showBbox)
			{
			preview.setShowBbox(showBbox.isSelected());
			return;
			}
		else if (e.getSource() == showOrigin)
			{
			preview.setShowOrigin(showOrigin.isSelected());
			return;
			}
		else if (e.getSource() == subLeft)
			{
			if (pos <= 0 && !wrapBox.isSelected())
				{
				subList.setSelectedIndex(currSub - 1);
				return;
				}

			if (shiftBox.isSelected())
				{
				//TODO: This may be firing an event causing you not to be able
				//to shift multiple images at a time.
				int[] selections = subList.getSelectedIndices();
				for (int i = 0; i < selections.length; i++)
					{
					pos = selections[i];
					BufferedImage bi = res.subImages.remove(pos);
					if (pos <= 0 && wrapBox.isSelected())
						{
						pos = res.subImages.size() + 1;
						}
					res.subImages.add(pos - 1,bi);
					}
				imageChanged = true;
				subList.setSelectedIndex(pos - 1);
				}
			else
				{
				if (currSub > 0)
					subList.setSelectedIndex(currSub - 1);
				else if (wrapBox.isSelected()) subList.setSelectedIndex(res.subImages.size() - 1);
				}
			return;
			}
		else if (e.getSource() == subRight)
			{
			if (pos >= res.subImages.size() - 1 && !wrapBox.isSelected())
				{
				subList.setSelectedIndex(res.subImages.size());
				return;
				}
			if (shiftBox.isSelected())
				{
				//TODO: This may be firing an event causing you not to be able
				//to shift multiple images at a time.
				int[] selections = subList.getSelectedIndices();
				for (int i = 0; i < selections.length; i++)
					{
					pos = selections[i];
					preview.setIndex(pos);
					BufferedImage bi = res.subImages.remove(pos);
					if (pos > res.subImages.size() - 1 && wrapBox.isSelected())
						{
						pos = -1;
						}
					res.subImages.add(pos + 1,bi);
					}
				imageChanged = true;
				subList.setSelectedIndex(pos + 1);
				}
			else
				{
				if (currSub < res.subImages.size() - 1)
					subList.setSelectedIndex(currSub + 1);
				else if (wrapBox.isSelected()) subList.setSelectedIndex(0);
				}

			return;
			}
		else if (e.getSource() == zoomButton)
			{
			if (zoomButton.isSelected())
				{
				preview.enableMouse = false;
				preview.setCursor(LGM.zoomCursor);
				preview.addMouseListener(previewMouseAdapter);
				}
			else
				{
				preview.enableMouse = true;
				preview.removeMouseListener(previewMouseAdapter);
				preview.setCursor(Cursor.getDefaultCursor());
				}
			}
		else if (e.getSource() == zoomIn)
			{
			preview.zoomIn(previewScroll);
			return;
			}
		else if (e.getSource() == zoomOut)
			{
			preview.zoomOut(previewScroll);
			return;
			}
		else if (e.getSource() == play)
			{
			if (timer != null)
				{
				play.setIcon(PLAY_ICON);
				stopAnimation();
				updateImageControls();
				}
			else if (res.subImages.size() > 1)
				{
				play.setIcon(STOP_ICON);
				timer = new Timer(1000 / speed.getIntValue(),this);
				timer.start();
				updateImageControls();
				}
			return;
			}
		else if (e.getSource() == timer)
			{
			int s = res.subImages.size();
			if (s > 0) setSubIndex((currSub + 1) % s);
			return;
			}
		else if (e.getSource() == centre)
			{
			res.put(PSprite.ORIGIN_X,res.getWidth() / 2);
			res.put(PSprite.ORIGIN_Y,res.getHeight() / 2);
			return;
			}

		super.actionPerformed(e);
		}

	private void realizeScrollBarIncrement(JScrollPane scroll)
		{
		JScrollBar vertical = scroll.getVerticalScrollBar();
		JScrollBar horizontal = scroll.getHorizontalScrollBar();
		if (vertical != null)
			{
			vertical.setUnitIncrement((int) getZoom());
			}
		if (horizontal != null)
			{
			horizontal.setUnitIncrement((int) getZoom());
			}
		}

	private void updateScrollBars()
		{
		realizeScrollBarIncrement(previewScroll);
		realizeScrollBarIncrement(subimagesScroll);
		}

	public void addSubimages(BufferedImage img[], boolean clear)
		{
		if (img.length == 0) return;
		if (clear)
			{
			cleanup();
			res.subImages.clear();
			}
		clear = res.subImages.isEmpty();
		imageChanged = true;
		for (BufferedImage i : img)
			res.subImages.add(i);
		show.setRange(0,res.subImages.size());
		if (clear) setSubIndex(0);
		updateStatusLabel();
		updateScrollBars();
		}

	public void addFromStrip(boolean clear)
		{
		//ask for an image first
		BufferedImage bi = Util.getValidImage();
		if (bi == null) return;
		//create the strip dialog
		SpriteStripDialog d = new SpriteStripDialog(LGM.frame,bi);
		d.setLocationRelativeTo(LGM.frame);
		d.setVisible(true); //modal at this point
		//add images
		BufferedImage[] img = d.getStrip();
		if (img == null) return; //cancelled/closed
		addSubimages(img,clear);
		}

	private void stopAnimation()
		{
		if (timer == null) return;
		timer.stop();
		timer = null; //used to indicate that this is not animating, and frees memory
		}

	private void updateImageControls()
		{
		int s = res.subImages.size();
		if (s > 0)
			{
			if (subList.getSelectedIndex() > s)
				{
				setSubIndex(s - 1);
				return;
				}
			if (!wrapBox.isSelected())
				{
				subLeft.setEnabled(timer == null && subList.getSelectedIndex() > 0);
				subRight.setEnabled(timer == null && subList.getSelectedIndex() < s - 1);
				}
			else
				{
				subLeft.setEnabled(timer == null);
				subRight.setEnabled(timer == null);
				}
			play.setEnabled(s > 1);
			if (updateSub)
				{
				try
					{
					show.setRange(0,s - 1);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				show.setEnabled(timer == null);
				//show.setValue(subList.getSelectedIndex());
				}
			}
		else
			{
			subLeft.setEnabled(false);
			subRight.setEnabled(false);
			play.setEnabled(false);
			if (updateSub)
				{
				show.setValue(0);
				show.setEnabled(false);
				}
			}
		}

	private void updateImageList()
		{
		ImageIcon ii[] = new ImageIcon[res.subImages.size()];
		int maxWidth = -1;
		for (int i = 0; i < res.subImages.size(); i++)
			{
			ii[i] = new ImageIcon(res.subImages.get(i));
			maxWidth = Math.max(maxWidth,ii[i].getIconWidth());
			}
		subList.setListData(ii);

		updateImageControls();
		}

	private void setSubIndex(int i)
		{
		if (currSub == i)
			{
			return;
			}
		currSub = i;
		preview.setIndex(i);
		if (timer == null)
			{
			updateImageControls();
			}
		}

	private void updateBoundingBoxEditors()
		{
		boolean m = res.get(PSprite.BB_MODE) == BBMode.MANUAL;
		bboxLeft.setEnabled(m);
		bboxRight.setEnabled(m);
		bboxTop.setEnabled(m);
		bboxBottom.setEnabled(m);
		}

	@Override
	public Dimension getMinimumSize()
		{
		Dimension p = getContentPane().getSize();
		Dimension l = getContentPane().getMinimumSize();
		Dimension s = getSize();
		l.width += s.width - p.width;
		l.height += s.height - p.height;
		return l;
		}

	public void editSubimage(BufferedImage img)
		{
		if (img == null) return;
		try
			{
			ImageEditor ie = editors == null ? null : editors.get(img);
			if (ie == null)
				new ImageEditor(img);
			else
				ie.start();
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		}

	public void updated(UpdateEvent e)
		{
		updateStatusLabel();
		updateImageList();
		}

	private class SpritePropertyListener extends PropertyUpdateListener<PSprite>
		{
		@Override
		public void updated(PropertyUpdateEvent<PSprite> e)
			{
			// BB_MODE
			updateBoundingBoxEditors();
			}
		}

	private class ImageEditor implements UpdateListener
		{
		private BufferedImage image;
		private FileChangeMonitor monitor;
		private File f;

		public ImageEditor(BufferedImage i) throws IOException,UnsupportedOperationException
			{
			image = i;
			if (editors == null) editors = new HashMap<BufferedImage,ImageEditor>();
			editors.put(i,this);
			start();
			}

		public void start() throws IOException,UnsupportedOperationException
			{
			if (monitor != null)
				monitor.stop();

			if (f == null || !f.exists())
				{
				f = File.createTempFile(res.getName(),'.' + Prefs.externalSpriteExtension,LGM.tempDir);
				f.deleteOnExit();
				}

			try (FileOutputStream out = new FileOutputStream(f))
				{
				ImageIO.write(image,Prefs.externalSpriteExtension,out);
				}

			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this,true);

			if (!Prefs.useExternalSpriteEditor || Prefs.externalSpriteEditorCommand == null)
				Util.OpenDesktopEditor(monitor.file);
			else
				Runtime.getRuntime().exec(
						String.format(Prefs.externalSpriteEditorCommand,monitor.file.getAbsolutePath()));
			}

		public void stop()
			{
			monitor.stop();
			monitor.file.delete();
			if (editors != null) editors.remove(image);
			}

		public void updated(UpdateEvent e)
			{
			if (!(e instanceof FileUpdateEvent)) return;
			switch (((FileUpdateEvent) e).flag)
				{
				case CHANGED:
					BufferedImage img;
					try (FileInputStream stream = new FileInputStream(monitor.file))
						{
						img = ImageIO.read(stream);
						}
					catch (IOException ioe)
						{
						LGM.showDefaultExceptionHandler(ioe);
						return;
						}
					res.subImages.replace(image,img);
					editors.remove(image);
					editors.put(img,this);
					image = img;
					imageChanged = true;
					break;
				case DELETED:
					editors.remove(image);
				}
			}
		}

	@Override
	public void dispose()
		{
		super.dispose();
		stopAnimation();
		cleanup();
		}

	/** Stops file monitors, detaching any open editors. */
	protected void cleanup()
		{
		if (editors != null)
			// because stopping an editor removes it from the collection we must iterate
			// here in a way that prevents concurrent modification exceptions by first 
			// removing the editor through the iterator so that stop's remove is a noop
			for (final Iterator<ImageEditor> it = editors.values().iterator(); it.hasNext();)
				{
				ImageEditor ie = it.next(); // << grab it first
				it.remove(); // << remove it the safe way before stop does
				ie.stop(); // << safe from concurrent modification now
				}
		}

	public void lostOwnership(Clipboard arg0, Transferable arg1)
		{
		// TODO Auto-generated method stub
		System.out.println("Sprite editor has lost clipboard ownership.");
		}

	@Override
	public void applyEffects(List<BufferedImage> imgs)
		{
		int[] selection = subList.getSelectedIndices();
		for (int i = 0; i < selection.length; i++) {
			res.subImages.set(selection[i],imgs.get(i));
		}
		imageChanged = true;
		subList.setSelectedIndices(selection);
		preview.repaint();
		}
	}
