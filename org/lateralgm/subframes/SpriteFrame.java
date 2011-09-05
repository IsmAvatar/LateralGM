/*
 * Copyright (C) 2008, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.components.NumberField;
import org.lateralgm.components.NumberField.ValueChangeEvent;
import org.lateralgm.components.NumberField.ValueChangeListener;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.SpriteStripDialog;
import org.lateralgm.components.visual.SubimagePreview;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.BBMode;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.ui.swing.util.SwingExecutor;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class SpriteFrame extends ResourceFrame<Sprite,PSprite> implements ActionListener,
		MouseListener,UpdateListener,ValueChangeListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("SpriteFrame.LOAD"); //$NON-NLS-1$
	private static final ImageIcon LOAD_STRIP_ICON = LGM.getIconForKey("SpriteFrame.LOAD_STRIP"); //$NON-NLS-1$
	private static final ImageIcon PLAY_ICON = LGM.getIconForKey("SpriteFrame.PLAY"); //$NON-NLS-1$
	private static final ImageIcon STOP_ICON = LGM.getIconForKey("SpriteFrame.STOP"); //$NON-NLS-1$

	//toolbar
	public JButton load, loadStrip;
	public JCheckBox showBbox;

	//origin
	public NumberField originX, originY;
	public JButton centre;

	//bbox
	public IndexButtonGroup bboxGroup;
	public NumberField bboxLeft, bboxRight;
	public NumberField bboxTop, bboxBottom;
	public JRadioButton auto, full, manual;

	//properties
	public JRadioButton rect, prec, disk, diam;
	public JCheckBox preciseCC, smooth, preload, transparent;
	public JLabel subCount, width, height;

	//subimages
	public JList subList;

	//preview
	public SubimagePreview preview;
	public NumberField show, speed;
	public JButton subLeft, subRight, play;
	public JLabel showLab;
	public int currSub;

	public boolean imageChanged = false;
	public JSplitPane splitPane;

	/** Used for animation, or null when not animating */
	public Timer timer;

	/** Prevents <code>show</code> from resetting when it changes */
	private boolean updateSub = true;

	private final SpritePropertyListener spl = new SpritePropertyListener();

	private Map<BufferedImage,ImageEditor> editors;

	public SpriteFrame(Sprite res, ResNode node)
		{
		super(res,node);
		res.properties.getUpdateSource(PSprite.BB_MODE).addListener(spl);
		res.reference.updateSource.addListener(this);

		setLayout(new BorderLayout());

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(Messages.getString("SpriteFrame.PROPERTIES"),makePropertiesPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("SpriteFrame.MASK"),makeMaskPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("SpriteFrame.SUBIMAGES"),makeSubimagesPane()); //$NON-NLS-1$

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,tabs,makePreviewPane());
		splitPane.setOneTouchExpandable(true);

		add(makeToolBar(),BorderLayout.NORTH);
		add(splitPane,BorderLayout.CENTER);

		updateImageList();
		updateInfo();

		pack();
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);

		tool.add(save);

		load = new JButton(LOAD_ICON);
		load.setToolTipText(Messages.getString("SpriteFrame.LOAD")); //$NON-NLS-1$
		load.addActionListener(this);
		tool.add(load);

		loadStrip = new JButton(LOAD_STRIP_ICON);
		loadStrip.setToolTipText(Messages.getString("SpriteFrame.LOAD_STRIP")); //$NON-NLS-1$
		loadStrip.addActionListener(this);
		tool.add(loadStrip);

		tool.addSeparator();

		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("SpriteFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		tool.addSeparator();

		showBbox = new JCheckBox(Messages.getString("SpriteFrame.SHOW_BBOX"),true);
		showBbox.setOpaque(false); //so it doesn't hide the toolbar gradient...
		showBbox.addActionListener(this);
		tool.add(showBbox);

		return tool;
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
		GroupLayout bLayout = new GroupLayout(pane);
		pane.setLayout(bLayout);
		pane.setBorder(BorderFactory.createTitledBorder(Messages.getString("SpriteFrame.COLLISION"))); //$NON-NLS-1$

		ButtonGroup g = new ButtonGroup();
		prec = new JRadioButton(Messages.getString("SpriteFrame.PRECISE")); //$NON-NLS-1$
		g.add(prec);
		rect = new JRadioButton(Messages.getString("SpriteFrame.RECTANGLE")); //$NON-NLS-1$
		g.add(rect);
		disk = new JRadioButton(Messages.getString("SpriteFrame.DISK")); //$NON-NLS-1$
		g.add(disk);
		diam = new JRadioButton(Messages.getString("SpriteFrame.DIAMOND")); //$NON-NLS-1$
		g.add(diam);
		plf.make(g,PSprite.SHAPE,Sprite.MaskShape.class);

		bLayout.setHorizontalGroup(bLayout.createParallelGroup()
		/**/.addComponent(prec)
		/**/.addComponent(rect)
		/**/.addComponent(disk)
		/**/.addComponent(diam));

		bLayout.setVerticalGroup(bLayout.createSequentialGroup()
		/**/.addComponent(prec)
		/**/.addComponent(rect)
		/**/.addComponent(disk)
		/**/.addComponent(diam));

		return pane;
		}

	private JPanel makeBBoxPane()
		{
		JPanel pane = new JPanel();
		GroupLayout bLayout = new GroupLayout(pane);
		pane.setLayout(bLayout);
		pane.setBorder(BorderFactory.createTitledBorder(Messages.getString("SpriteFrame.BBOX"))); //$NON-NLS-1$
		ButtonGroup g = new ButtonGroup();
		auto = new JRadioButton(Messages.getString("SpriteFrame.AUTO")); //$NON-NLS-1$
		g.add(auto);
		full = new JRadioButton(Messages.getString("SpriteFrame.FULL")); //$NON-NLS-1$
		g.add(full);
		manual = new JRadioButton(Messages.getString("SpriteFrame.MANUAL")); //$NON-NLS-1$
		g.add(manual);
		plf.make(g,PSprite.BB_MODE,BBMode.class);

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

		bLayout.setHorizontalGroup(bLayout.createParallelGroup()
		/**/.addGroup(bLayout.createSequentialGroup()
		/*		*/.addComponent(auto)
		/*		*/.addComponent(full))
		/**/.addComponent(manual)
		/*		*/.addGroup(bLayout.createSequentialGroup()
		/*		*/.addContainerGap(4,4)
		/*		*/.addGroup(bLayout.createParallelGroup()
		/*				*/.addComponent(lLab)
		/*				*/.addComponent(tLab))
		/*		*/.addGap(2)
		/*		*/.addGroup(bLayout.createParallelGroup()
		/*				*/.addComponent(bboxLeft)
		/*				*/.addComponent(bboxTop))
		/*		*/.addGap(8)
		/*		*/.addGroup(bLayout.createParallelGroup()
		/*				*/.addComponent(rLab)
		/*				*/.addComponent(bLab))
		/*				*/.addGap(2)
		/*				*/.addGroup(bLayout.createParallelGroup()
		/*						*/.addComponent(bboxRight)
		/*						*/.addComponent(bboxBottom))
		/*				*/.addContainerGap(4,4)));
		bLayout.setVerticalGroup(bLayout.createSequentialGroup()
		/**/.addGroup(bLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(auto)
		/*		*/.addComponent(full))
		/**/.addComponent(manual)
		/**/.addGap(4)
		/**/.addGroup(bLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lLab)
		/*		*/.addComponent(bboxLeft)
		/*		*/.addComponent(rLab)
		/*		*/.addComponent(bboxRight))
		/**/.addGap(4)
		/**/.addGroup(bLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(tLab)
		/*		*/.addComponent(bboxTop)
		/*		*/.addComponent(bLab)
		/*		*/.addComponent(bboxBottom))
		/**/.addContainerGap(2,2));

		return pane;
		}

	private JPanel makePropertiesPane()
		{
		JPanel pane = new JPanel();
		GroupLayout layout = new GroupLayout(pane);
		layout.setAutoCreateContainerGaps(true);

		pane.setLayout(layout);

		subCount = new JLabel();
		width = new JLabel();
		height = new JLabel();

		smooth = new JCheckBox(Messages.getString("SpriteFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PSprite.SMOOTH_EDGES);
		preload = new JCheckBox(Messages.getString("SpriteFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PSprite.PRELOAD);
		transparent = new JCheckBox(Messages.getString("SpriteFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setToolTipText(Messages.getString("SpriteFrame.TRANSP_TIP")); //$NON-NLS-1$
		plf.make(transparent,PSprite.TRANSPARENT);

		JPanel origin = makeOriginPane();

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(subCount,Alignment.CENTER)
		/**/.addGroup(Alignment.CENTER,layout.createSequentialGroup()
		/*	*/.addComponent(width)
		/*	*/.addGap(12)
		/*	*/.addComponent(height))
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(origin));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(subCount)
		/**/.addGap(4)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(width)
		/*	*/.addComponent(height))
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(origin));

		return pane;
		}

	private JPanel makeMaskPane()
		{
		JPanel pane = new JPanel();
		GroupLayout layout = new GroupLayout(pane);
		layout.setAutoCreateContainerGaps(true);

		pane.setLayout(layout);

		JPanel coll = makeCollisionPane();
		JPanel bbox = makeBBoxPane();

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(coll)
		/**/.addComponent(bbox));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(coll)
		/**/.addComponent(bbox));

		return pane;
		}

	private JPanel makeSubimagesPane()
		{
		JPanel pane = new JPanel(new BorderLayout());
		//prevents resizing on large subimages with size(1,1)
		pane.setPreferredSize(new Dimension(1,1));

		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		pane.add(tool,BorderLayout.NORTH);

		makeToolButton(tool,"SpriteFrame.ADD"); //$NON-NLS-1$
		makeToolButton(tool,"SpriteFrame.REMOVE"); //$NON-NLS-1$
		tool.addSeparator();
		makeToolButton(tool,"SpriteFrame.PREVIOUS"); //$NON-NLS-1$
		makeToolButton(tool,"SpriteFrame.NEXT"); //$NON-NLS-1$

		subList = new SubImageList();
		subList.addMouseListener(this);
		subList.setDragEnabled(true);
		pane.add(new JScrollPane(subList),BorderLayout.CENTER);
		return pane;
		}

	class SubImageList extends JList
		{
		private static final long serialVersionUID = 1L;

		public SubImageList()
			{
			setDragEnabled(true);
			setDropMode(DropMode.INSERT);
			setTransferHandler(new SubImageTransfer());
			}
		}

	class SubImageTransfer extends TransferHandler
		{
		private static final long serialVersionUID = 1L;

		public int getSourceActions(JComponent c)
			{
			return COPY_OR_MOVE;
			}

		public Transferable createTransferable(JComponent c)
			{
			return null;
			}

		public void exportDone(JComponent c, Transferable t, int action)
			{
			if (action == MOVE)
				{
				}
			}

		public boolean canImport(TransferHandler.TransferSupport s)
			{
			System.out.println(s.getTransferable());
			DataFlavor fl[] = s.getDataFlavors();
			//			System.out.println(fl);
			for (DataFlavor f : fl)
				{
				if (f.equals(DataFlavor.javaFileListFlavor)) System.out.println("Oh hi");
				if (f.equals(DataFlavor.stringFlavor))
					{
					Transferable t = s.getTransferable();
					if (t instanceof StringSelection)
						{

						}
					try
						{
						System.out.println(s.getTransferable().getTransferData(f));
						}
					catch (UnsupportedFlavorException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
					//					System.out.println(f);
					}
				//				System.out.println(" " + f.getRepresentationClass());
				}
			return false;
			}

		public boolean importData(TransferHandler.TransferSupport s)
			{
			return false;
			}
		}

	private void makeToolButton(JToolBar tool, String icon)
		{
		ImageIcon ii = LGM.getIconForKey(icon);
		JButton but = new JButton(ii);
		but.setActionCommand(icon);
		but.addActionListener(this);
		tool.add(but);
		}

	private JPanel makePreviewPane()
		{
		JPanel pane = new JPanel(new BorderLayout());

		preview = new SubimagePreview(res);
		JScrollPane scroll = new JScrollPane(preview);
		scroll.setPreferredSize(scroll.getSize());

		JPanel controls = new JPanel();
		GroupLayout layout = new GroupLayout(controls);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		controls.setLayout(layout);

		subLeft = new JButton(LGM.getIconForKey("SpriteFrame.PREVIOUS")); //$NON-NLS-1$
		subLeft.addActionListener(this);

		show = new NumberField(0,res.subImages.size() - 1);
		show.setHorizontalAlignment(SwingConstants.CENTER);
		show.addValueChangeListener(this);
		//		show.setValue(0);

		subRight = new JButton(LGM.getIconForKey("SpriteFrame.NEXT")); //$NON-NLS-1$
		subRight.addActionListener(this);

		JLabel lab = new JLabel(Messages.getString("SpriteFrame.ANIM_SUBIMG")); //$NON-NLS-1$
		JLabel lab2 = new JLabel(Messages.getString("SpriteFrame.ANIM_SPEED")); //$NON-NLS-1$
		lab2.setHorizontalAlignment(SwingConstants.CENTER);

		speed = new NumberField(1,Integer.MAX_VALUE,30);
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
		play = new JButton(PLAY_ICON);
		play.addActionListener(this);

		layout.setHorizontalGroup(layout.createSequentialGroup()
		/**/.addGap(5)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(lab,Alignment.CENTER)
		/*	*/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(subLeft,20,20,20)
		/*		*/.addComponent(show)
		/*		*/.addComponent(subRight,20,20,20)))
		/**/.addGap(10,10,Integer.MAX_VALUE)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(lab2,Alignment.CENTER)
		/*	*/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(speed)
		/*		*/.addComponent(play,20,20,20)))
		/**/.addGap(5));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(lab,Alignment.CENTER)
		/*	*/.addComponent(lab2,Alignment.CENTER))
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(subLeft,20,20,20)
		/*	*/.addComponent(show,21,21,21)
		/*	*/.addComponent(subRight,20,20,20)
		/*	*/.addComponent(speed,21,21,21)
		/*	*/.addComponent(play,20,20,20))
		/**/.addGap(5));

		pane.add(scroll,BorderLayout.CENTER);
		pane.add(controls,BorderLayout.SOUTH);

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

	public void updateResource()
		{
		super.updateResource();
		imageChanged = false;
		}

	public void valueChange(ValueChangeEvent e)
		{
		if (e.getSource() == show)
			{
			updateSub = false;
			setSubIndex(show.getIntValue());
			updateSub = true;
			return;
			}
		if (e.getSource() == speed)
			{
			if (timer != null) timer.setDelay(1000 / speed.getIntValue());
			return;
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			BufferedImage[] img = Util.getValidImages();
			if (img != null) addSubimages(img,true);
			return;
			}
		if (e.getSource() == loadStrip)
			{
			addFromStrip(true);
			return;
			}
		if (e.getSource() == showBbox)
			{
			preview.setShowBbox(showBbox.isSelected());
			preview.updateUI();
			return;
			}
		if (e.getSource() == subLeft)
			{
			if (currSub > 0) setSubIndex(currSub - 1);
			return;
			}
		if (e.getSource() == subRight)
			{
			if (currSub < res.subImages.size() - 1) setSubIndex(currSub + 1);
			return;
			}
		if (e.getSource() == play)
			{
			if (timer != null)
				{
				play.setIcon(PLAY_ICON);
				timer.stop();
				timer = null; //used to indicate that this is not animating, and frees memory
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
		if (e.getSource() == timer)
			{
			int s = res.subImages.size();
			if (s > 0) setSubIndex((currSub + 1) % s);
			return;
			}
		if (e.getSource() == centre)
			{
			res.put(PSprite.ORIGIN_X,res.subImages.getWidth() / 2);
			res.put(PSprite.ORIGIN_Y,res.subImages.getHeight() / 2);
			return;
			}
		String cmd = e.getActionCommand();
		if (cmd != null && cmd.startsWith("SpriteFrame.")) //$NON-NLS-1$
			handleToolbarEvent(cmd.substring(12));

		super.actionPerformed(e);
		}

	private void handleToolbarEvent(String cmd)
		{
		int pos = subList.getSelectedIndex();
		if (cmd.equals("ADD")) //$NON-NLS-1$
			{
			BufferedImage bi = res.addSubImage();
			pos = pos >= 0 ? pos + 1 : res.subImages.size();
			imageChanged = true;
			res.subImages.add(pos,bi);
			subList.setSelectedIndex(pos);
			editSubimage(bi);
			return;
			}
		if (pos == -1) return;
		if (cmd.equals("REMOVE")) //$NON-NLS-1$
			{
			ImageEditor ie = editors == null ? null : editors.get(res.subImages.get(pos));
			imageChanged = true;
			res.subImages.remove(pos);
			if (ie != null) ie.stop();
			subList.setSelectedIndex(Math.min(res.subImages.size() - 1,pos));
			return;
			}
		if (cmd.equals("PREVIOUS")) //$NON-NLS-1$
			{
			if (pos == 0) return;
			imageChanged = true;
			BufferedImage bi = res.subImages.remove(pos);
			res.subImages.add(pos - 1,bi);
			subList.setSelectedIndex(pos - 1);
			return;
			}
		if (cmd.equals("NEXT")) //$NON-NLS-1$
			{
			if (pos == res.subImages.size() - 1) return;
			imageChanged = true;
			BufferedImage bi = res.subImages.remove(pos);
			res.subImages.add(pos + 1,bi);
			subList.setSelectedIndex(pos + 1);
			return;
			}
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
		updateInfo();
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

	public void updateInfo()
		{
		width.setText(Messages.getString("SpriteFrame.WIDTH") + res.subImages.getWidth()); //$NON-NLS-1$
		height.setText(Messages.getString("SpriteFrame.HEIGHT") + res.subImages.getHeight()); //$NON-NLS-1$
		subCount.setText(Messages.getString("SpriteFrame.NO_OF_SUBIMAGES") //$NON-NLS-1$
				+ res.subImages.size());
		}

	private void updateImageControls()
		{
		int s = res.subImages.size();
		if (s > 0)
			{
			if (currSub > s)
				{
				setSubIndex(s - 1);
				return;
				}
			subLeft.setEnabled(timer == null && currSub > 0);
			subRight.setEnabled(timer == null && currSub < s - 1);
			play.setEnabled(s > 1);
			if (updateSub)
				{
				show.setRange(0,s - 1);
				show.setEnabled(timer == null);
				show.setValue(currSub);
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
		for (int i = 0; i < res.subImages.size(); i++)
			ii[i] = new ImageIcon(res.subImages.get(i));
		subList.setListData(ii);
		updateImageControls();
		}

	private void setSubIndex(int i)
		{
		currSub = i;
		preview.setIndex(i);
		updateImageControls();
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

	//unused
	public void mouseClicked(MouseEvent e)
		{
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mouseReleased(MouseEvent e)
		{
		}

	public void updated(UpdateEvent e)
		{
		updateInfo();
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
		public final FileChangeMonitor monitor;

		public ImageEditor(BufferedImage i) throws IOException,UnsupportedOperationException
			{
			image = i;
			File f = File.createTempFile(res.getName(),"." + Prefs.externalSpriteExtension,LGM.tempDir); //$NON-NLS-1$
			f.deleteOnExit();
			FileOutputStream out = new FileOutputStream(f);
			ImageIO.write(i,Prefs.externalSpriteExtension,out); //$NON-NLS-1$
			out.close();
			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this,true);
			if (editors == null) editors = new HashMap<BufferedImage,ImageEditor>();
			editors.put(i,this);
			start();
			}

		public void start() throws IOException,UnsupportedOperationException
			{
			if (!Prefs.useExternalSpriteEditor || Prefs.externalSpriteEditorCommand == null)
				try
					{
					Desktop.getDesktop().edit(monitor.file);
					}
				catch (UnsupportedOperationException e)
					{
					throw new UnsupportedOperationException("no internal or system sprite editor",e);
					}
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
					try
						{
						img = ImageIO.read(new FileInputStream(monitor.file));
						}
					catch (IOException ioe)
						{
						ioe.printStackTrace();
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
		cleanup();
		}

	/** Stops file monitors, detaching any open editors. */
	protected void cleanup()
		{
		if (editors != null)
			for (ImageEditor ie : editors.values().toArray(new ImageEditor[editors.size()]))
				ie.stop();
		}
	}
