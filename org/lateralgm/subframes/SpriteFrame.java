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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
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
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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

import org.lateralgm.components.NumberField;
import org.lateralgm.components.NumberField.ValueChangeEvent;
import org.lateralgm.components.NumberField.ValueChangeListener;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.SpriteStripDialog;
import org.lateralgm.components.visual.SubimagePreview;
import org.lateralgm.file.FileChangeMonitor;
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
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.ui.swing.util.SwingExecutor;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class SpriteFrame extends InstantiableResourceFrame<Sprite,PSprite> implements
		MouseListener,UpdateListener,ValueChangeListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("SpriteFrame.LOAD"); //$NON-NLS-1$
	private static final ImageIcon LOAD_SUBIMAGE_ICON = LGM.getIconForKey("SpriteFrame.LOAD_SUBIMAGE"); //$NON-NLS-1$
	private static final ImageIcon LOAD_STRIP_ICON = LGM.getIconForKey("SpriteFrame.LOAD_STRIP"); //$NON-NLS-1$
	private static final ImageIcon PLAY_ICON = LGM.getIconForKey("SpriteFrame.PLAY"); //$NON-NLS-1$
	private static final ImageIcon STOP_ICON = LGM.getIconForKey("SpriteFrame.STOP"); //$NON-NLS-1$

	//toolbar
	public JButton load, loadSubimage, loadStrip;

	//origin
	public NumberField originX, originY;
	public JButton centre;

	//bbox
	public IndexButtonGroup bboxGroup;
	public NumberField bboxLeft, bboxRight;
	public NumberField bboxTop, bboxBottom;
	public JRadioButton auto, full, manual;

	//properties
	public JRadioButton rect, prec, disk, diam, poly;
	public JCheckBox smooth, preload, transparent;
	public JLabel subCount, width, height;

	//subimages
	public JList subList;
	
  //effects
  public JButton invert, flip, rotate,
  reverse, addreverse, fade, rotfract, srhink, grow, colalpha, alphacol; 
  public JLabel notimplemented;
  
	//preview
	public SubimagePreview preview;
	public NumberField show, speed;
	public JButton subLeft, subRight, play;
	public JLabel showLab;
	public int currSub;
	public JCheckBox showBbox, showOrigin;

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
		tabs.addTab(Messages.getString("SpriteFrame.PROPERTIES"), makePropertiesPane()); //$NON-NLS-1$
    tabs.addTab(Messages.getString("SpriteFrame.MASK"), makeMaskPane()); //$NON-NLS-1$
    tabs.addTab(Messages.getString("SpriteFrame.SUBIMAGES"), makeSubimagesPane()); //$NON-NLS-1$
    tabs.addTab(Messages.getString("SpriteFrame.EFFECTS"), makeEffectsPane()); //$NON-NLS-1$ 

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

		loadSubimage = new JButton(LOAD_SUBIMAGE_ICON);
		loadSubimage.setToolTipText(Messages.getString("SpriteFrame.LOAD_SUBIMAGE")); //$NON-NLS-1$
		loadSubimage.addActionListener(this);
		tool.add(loadSubimage);
		
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
		poly = new JRadioButton(Messages.getString("SpriteFrame.POLYGON")); //$NON-NLS-1$
		g.add(poly);
		plf.make(g,PSprite.SHAPE,Sprite.MaskShape.class);

		bLayout.setHorizontalGroup(bLayout.createParallelGroup()
		/**/.addComponent(prec)
		/**/.addComponent(rect)
		/**/.addComponent(disk)
		/**/.addComponent(diam)
		/**/.addComponent(poly));

		bLayout.setVerticalGroup(bLayout.createSequentialGroup()
		/**/.addComponent(prec)
		/**/.addComponent(rect)
		/**/.addComponent(disk)
		/**/.addComponent(diam)
		/**/.addComponent(poly));

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

  private JPanel makeEffectsPane()
  {
    JPanel pane = new JPanel();
    GroupLayout layout = new GroupLayout(pane);
    layout.setAutoCreateContainerGaps(true);

    pane.setLayout(layout);

    JLabel notimplemented = new JLabel("Note: These buttons do not do anything yet."); //$NON-NLS-1$
    
    JButton invert = new JButton(Messages.getString("SpriteFrame.INVERT")); //$NON-NLS-1$
    invert.setToolTipText("Invert All Subframes"); 
    invert.addActionListener(this);
    
    JButton flip = new JButton(Messages.getString("SpriteFrame.FLIP")); //$NON-NLS-1$
    flip.setToolTipText("Flip"); 
    flip.addActionListener(this);
    
    JButton rotate = new JButton(Messages.getString("SpriteFrame.ROTATE")); //$NON-NLS-1$
    rotate.setToolTipText("Rotate"); 
    rotate.addActionListener(this);
    
    JButton rotfract = new JButton(Messages.getString("SpriteFrame.ROTATEFRACTION")); //$NON-NLS-1$
    rotfract.setToolTipText("Rotate By An Incremented Fraction Each Subframe"); 
    rotfract.addActionListener(this);
    
    JButton reverse = new JButton(Messages.getString("SpriteFrame.REVERSE")); //$NON-NLS-1$
    reverse.setToolTipText("Reverse"); 
    reverse.addActionListener(this);
    
    JButton addreverse = new JButton(Messages.getString("SpriteFrame.ADDREVERSE")); //$NON-NLS-1$
    addreverse.setToolTipText("Add Reverse"); 
    addreverse.addActionListener(this);
    
    JButton colalpha = new JButton(Messages.getString("SpriteFrame.COLORALPHA")); //$NON-NLS-1$
    colalpha.setToolTipText("Color to Alpha");
    colalpha.addActionListener(this);
    
    JButton alphacol = new JButton(Messages.getString("SpriteFrame.ALPHACOLOR")); //$NON-NLS-1$
    alphacol.setToolTipText("Alpha to Color"); 
    alphacol.addActionListener(this);
    
    JButton fade = new JButton(Messages.getString("SpriteFrame.FADE")); //$NON-NLS-1$
    fade.setToolTipText("Fade"); 
    fade.addActionListener(this);
    
    JButton shrink = new JButton(Messages.getString("SpriteFrame.SHRINK")); //$NON-NLS-1$
    shrink.setToolTipText("Shrink"); 
    shrink.addActionListener(this);
    
    JButton grow = new JButton(Messages.getString("SpriteFrame.GROW")); //$NON-NLS-1$
    grow.setToolTipText("Grow");
    grow.addActionListener(this);

    
    //public JButton invert, flip, rotate,
    //reverse, addreverse, fade, rotfract, srhink, grow, colalpha, alphacol;

    layout.setHorizontalGroup(layout.createParallelGroup()
    		
    /**/.addGroup(Alignment.LEADING, layout.createSequentialGroup()
    /**/.addComponent(notimplemented))
            
    /**/.addGroup(Alignment.LEADING, layout.createSequentialGroup()
    /**/.addComponent(invert)
    /**/.addGap(2)
    /**/.addComponent(flip)
    /**/.addGap(2))
    
    /**/.addGroup(Alignment.LEADING, layout.createSequentialGroup()
    /**/.addComponent(rotate)
    /**/.addGap(2)
    /**/.addComponent(rotfract)
    /**/.addGap(2))
    
    /**/.addGroup(Alignment.LEADING, layout.createSequentialGroup()
    /**/.addComponent(reverse)
    /**/.addGap(2)
    /**/.addComponent(addreverse)
    /**/.addGap(2))
    
    /**/.addGroup(Alignment.LEADING, layout.createSequentialGroup()
    /**/.addComponent(colalpha)
    /**/.addGap(2)
    /**/.addComponent(alphacol)
    /**/.addGap(2))
    
    /**/.addGroup(Alignment.LEADING, layout.createSequentialGroup()
    /**/.addComponent(fade)
    /**/.addGap(2)
    /**/.addComponent(shrink)
    /**/.addGap(2)
    /**/.addComponent(grow)
    /**/.addGap(2))
    );

    layout.setVerticalGroup(layout.createSequentialGroup()
    		
    /**/.addGroup(layout.createParallelGroup()
    /**/.addComponent(notimplemented))
    /**/.addGap(12)
            
    /**/.addGroup(layout.createParallelGroup()
    /**/.addComponent(invert)
    /**/.addComponent(flip))
    /**/.addGap(12)
    
    /**/.addGroup(layout.createParallelGroup()
    /**/.addComponent(rotate)
    /**/.addComponent(rotfract))
    /**/.addGap(12)
    
    /**/.addGroup(layout.createParallelGroup()
    /**/.addComponent(reverse)
    /**/.addComponent(addreverse))
    /**/.addGap(12)
    
    /**/.addGroup(layout.createParallelGroup()
    /**/.addComponent(colalpha)
    /**/.addComponent(alphacol))
    /**/.addGap(12)
    
    /**/.addGroup(layout.createParallelGroup()
    /**/.addComponent(fade)
    /**/.addComponent(shrink)
    /**/.addComponent(grow))
    /**/.addGap(12));
    
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

		JCheckBox cb = new JCheckBox("Wrap",true);
		tool.add(cb);
		cb.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					boolean b = ((JCheckBox) e.getSource()).isSelected();
					if (b)
						subList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
					else
						subList.setLayoutOrientation(JList.VERTICAL);
					}
			});

		subList = new JList();
		subList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		subList.setVisibleRowCount(-1);
		subList.setBackground(Color.LIGHT_GRAY);
		subList.setDragEnabled(true);
		subList.setDropMode(DropMode.INSERT);
		subList.setTransferHandler(new SubImageTransfer());
		subList.addMouseListener(this);
		subList.setDragEnabled(true);
		pane.add(new JScrollPane(subList),BorderLayout.CENTER);

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

		public Transferable createTransferable(JComponent c)
			{
			JList l = ((JList) c);
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

		showBbox = new JCheckBox(Messages.getString("SpriteFrame.SHOW_BBOX"),true);
		showBbox.addActionListener(this);
		showOrigin = new JCheckBox(Messages.getString("SpriteFrame.SHOW_ORIGIN"),true);
		showOrigin.addActionListener(this);

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
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
		/*		*/.addComponent(play,20,20,20))))
		/*	*/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(showBbox)
		/*		*/.addComponent(showOrigin))
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
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(showBbox)
		/*	*/.addComponent(showOrigin))
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
		if (e.getSource() == loadSubimage)
			{
			BufferedImage[] img = Util.getValidImages();
			if (img != null) addSubimages(img,false);
			return;
			}
		if (e.getSource() == showBbox)
			{
			preview.setShowBbox(showBbox.isSelected());
			preview.updateUI();
			return;
			}
		if (e.getSource() == showOrigin)
			{
			preview.setShowOrigin(showOrigin.isSelected());
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
		int maxWidth = -1;
		for (int i = 0; i < res.subImages.size(); i++)
			{
			ii[i] = new ImageIcon(res.subImages.get(i));
			maxWidth = Math.max(maxWidth,ii[i].getIconWidth());
			}
		subList.setListData(ii);
		//		subList.setFixedCellWidth(maxWidth);
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

	//unused
	public void mouseClicked(MouseEvent e)
		{ //unused
		}

	public void mouseEntered(MouseEvent e)
		{ //unused
		}

	public void mouseExited(MouseEvent e)
		{ //unused
		}

	public void mouseReleased(MouseEvent e)
		{ //unused
		}
	}
