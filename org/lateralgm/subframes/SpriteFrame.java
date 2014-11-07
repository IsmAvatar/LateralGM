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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
import org.lateralgm.components.JSplitPaneExpandable;
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
		MouseListener,UpdateListener,ValueChangeListener,ClipboardOwner
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("SpriteFrame.LOAD"); //$NON-NLS-1$
	private static final ImageIcon LOAD_SUBIMAGE_ICON = LGM.getIconForKey("SpriteFrame.LOAD_SUBIMAGE"); //$NON-NLS-1$
	private static final ImageIcon LOAD_STRIP_ICON = LGM.getIconForKey("SpriteFrame.LOAD_STRIP"); //$NON-NLS-1$
	private static final ImageIcon PLAY_ICON = LGM.getIconForKey("SpriteFrame.PLAY"); //$NON-NLS-1$
	private static final ImageIcon STOP_ICON = LGM.getIconForKey("SpriteFrame.STOP"); //$NON-NLS-1$
	private static final ImageIcon ZOOM_ICON = LGM.getIconForKey("SpriteFrame.ZOOM"); //$NON-NLS-1$
	private static final ImageIcon ZOOM_IN_ICON = LGM.getIconForKey("SpriteFrame.ZOOM_IN"); //$NON-NLS-1$
	private static final ImageIcon ZOOM_OUT_ICON = LGM.getIconForKey("SpriteFrame.ZOOM_OUT"); //$NON-NLS-1$

	//toolbar
	public JButton load, loadSubimage, loadStrip, zoomIn, zoomOut;
	public JToggleButton zoomButton;

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
	public JLabel statusLabel;

	//subimages
	public JList<ImageIcon> subList;

	//preview
	public JScrollPane previewScroll, subimagesScroll;
	public SubimagePreview preview;
	public NumberField show, speed;
	public JButton subLeft, subRight, play, cut, copy, paste, undo, redo;
	public JLabel showLab;
	public int currSub;
	public JCheckBox showBbox, showOrigin;
	public JCheckBox wrapBox, shiftBox;

	public boolean imageChanged = false;
	public JSplitPaneExpandable splitPane;

	/** Used for animation, or null when not animating */
	public Timer timer;

	/** Prevents <code>show</code> from resetting when it changes */
	private boolean updateSub = true;

	private final SpritePropertyListener spl = new SpritePropertyListener();

	private Map<BufferedImage,ImageEditor> editors;
	private MouseListener mouseListener;
	private MouseMotionListener mouseMotionListener;

	/** Zoom in, centering around a specific point, usually the mouse. */
	public void zoomIn(Point point)
		{
		this.setZoom(this.getZoom() * 1.2f);
		Dimension size = previewScroll.getViewport().getSize();

		int newX = (int) (point.x * 1.2) - size.width / 2;
		int newY = (int) (point.y * 1.2) - size.height / 2;
		previewScroll.getViewport().setViewPosition(new Point(newX,newY));

		previewScroll.revalidate();
		previewScroll.repaint();
		}

	/** Zoom out, centering around a specific point, usually the mouse. */
	public void zoomOut(Point point)
		{
		this.setZoom(this.getZoom() * 0.8f);
		Dimension size = previewScroll.getViewport().getSize();

		int newX = (int) (point.x * 0.8) - size.width / 2;
		int newY = (int) (point.y * 0.8) - size.height / 2;
		previewScroll.getViewport().setViewPosition(new Point(newX,newY));

		previewScroll.revalidate();
		previewScroll.repaint();
		}
	
	public void zoomIn()
		{
		Dimension size = previewScroll.getViewport().getViewSize();
		zoomIn(new Point(size.width/2,size.height/2));
		}

	public void zoomOut()
		{
		Dimension size = previewScroll.getViewport().getViewSize();
		zoomOut(new Point(size.width/2,size.height/2));
		}

	public SpriteFrame(Sprite res, ResNode node)
		{
		super(res,node);
		res.properties.getUpdateSource(PSprite.BB_MODE).addListener(spl);
		res.reference.updateSource.addListener(this);

		setLayout(new BorderLayout());

		JSplitPaneExpandable previewPane = new JSplitPaneExpandable(JSplitPane.VERTICAL_SPLIT,makePreviewPane(),
				makeSubimagesPane());
		splitPane = new JSplitPaneExpandable(JSplitPane.HORIZONTAL_SPLIT,makePropertiesPane(),previewPane);
		splitPane.setDoubleClickExpandable(true);

		add(makeToolBar(),BorderLayout.NORTH);
		add(splitPane,BorderLayout.CENTER);
		add(makeStatusBar(),BorderLayout.SOUTH);

		mouseMotionListener = new MouseMotionListener()
			{

				public void mouseMoved(MouseEvent e)
					{
					final int x = e.getX();
					final int y = e.getY();
					// only display a hand if the cursor is over the items
					final Rectangle cellBounds = preview.getBounds();
					if (cellBounds != null && cellBounds.contains(x,y))
						{
						//preview.setCursor(new Cursor(Cursor.HAND_CURSOR));
						}
					else
						{
						// 
						}
					}

				public void mouseDragged(MouseEvent e)
					{
					}
			};

		mouseListener = new MouseListener()
			{

				public void mouseClicked(MouseEvent ev)
					{
					// TODO Auto-generated method stub

					}

				public void mouseEntered(MouseEvent ev)
					{
					// TODO Auto-generated method stub

					preview.setCursor(LGM.zoomCursor);
					}

				public void mouseExited(MouseEvent ev)
					{
					// TODO Auto-generated method stub
					preview.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}

				public void mousePressed(MouseEvent ev)
					{
					// TODO Auto-generated method stub
					if (ev.getButton() == MouseEvent.BUTTON1)
						{
						preview.setCursor(LGM.zoomInCursor);
						}
					if (ev.getButton() == MouseEvent.BUTTON3)
						{
						preview.setCursor(LGM.zoomOutCursor);
						}
					}

				public void mouseReleased(MouseEvent ev)
					{
					// TODO Auto-generated method stub
					if (ev.getButton() == MouseEvent.BUTTON1)
						{
						zoomIn(ev.getPoint());
						}
					if (ev.getButton() == MouseEvent.BUTTON3)
						{
						zoomOut(ev.getPoint());
						}
					preview.setCursor(LGM.zoomCursor);
					}
			};

		updateImageList();
		updateStatusLabel();

		pack();
		this.setSize(750,500);
		previewPane.setDividerLocation(getHeight() / 2);
		updateScrollBars();
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

		zoomButton = new JToggleButton(ZOOM_ICON);
		zoomButton.setToolTipText(Messages.getString("SpriteFrame.ZOOM"));
		zoomButton.addActionListener(this);
		tool.add(zoomButton);

		zoomIn = new JButton(ZOOM_IN_ICON);
		zoomIn.setToolTipText(Messages.getString("SpriteFrame.ZOOM_IN")); //$NON-NLS-1$
		zoomIn.addActionListener(this);
		tool.add(zoomIn);

		zoomOut = new JButton(ZOOM_OUT_ICON);
		zoomOut.setToolTipText(Messages.getString("SpriteFrame.ZOOM_OUT")); //$NON-NLS-1$
		zoomOut.addActionListener(this);
		tool.add(zoomOut);

		showBbox = new JCheckBox(Messages.getString("SpriteFrame.SHOW_BBOX"),true);
		showBbox.addActionListener(this);
		tool.add(showBbox);
		showOrigin = new JCheckBox(Messages.getString("SpriteFrame.SHOW_ORIGIN"),true);
		showOrigin.addActionListener(this);
		tool.add(showOrigin);

		tool.addSeparator();
		JLabel lab2 = new JLabel(Messages.getString("SpriteFrame.ANIM_SPEED")); //$NON-NLS-1$
		//lab2.setHorizontalAlignment(SwingConstants.CENTER);
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

	public static class ObjectSizeFetcher
		{
		private static Instrumentation instrumentation;

		public static void premain(String args, Instrumentation inst)
			{
			instrumentation = inst;
			}

		public static long getObjectSize(Object o)
			{
			return instrumentation.getObjectSize(o);
			}
		}

	public static String formatData(long bytes)
		{
		if (bytes <= 0) return "0 B";
		final String[] units = new String[] { "B","KB","MB","GB","TB" };
		int digits = (int) (Math.log(bytes) / Math.log(1024));
		return new DecimalFormat("#,##0.##").format(bytes / Math.pow(1024,digits)) + " "
				+ units[digits];
		}

	private void updateStatusLabel()
		{
		String stat = " " + Messages.getString("SpriteFrame.WIDTH") + ": " + res.getWidth() + " | "
				+ Messages.getString("SpriteFrame.HEIGHT") + ": " + res.getHeight() + " | "
				+ Messages.getString("SpriteFrame.NO_OF_SUBIMAGES") + ": " + res.subImages.size() + " | "
				+ Messages.getString("SpriteFrame.MEMORY") + ": ";

		if (res.subImages != null)
			{
			stat += formatData(res.subImages.getSize());
			}
		else
			{
			stat += formatData(0);
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

	private JPanel makePropertiesPane()
		{
		JPanel pane = new JPanel();
		GroupLayout layout = new GroupLayout(pane);
		layout.setAutoCreateContainerGaps(true);

		pane.setLayout(layout);

		smooth = new JCheckBox(Messages.getString("SpriteFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PSprite.SMOOTH_EDGES);
		preload = new JCheckBox(Messages.getString("SpriteFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PSprite.PRELOAD);
		transparent = new JCheckBox(Messages.getString("SpriteFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setToolTipText(Messages.getString("SpriteFrame.TRANSP_TIP")); //$NON-NLS-1$
		plf.make(transparent,PSprite.TRANSPARENT);

		JPanel origin = makeOriginPane();
		JPanel coll = makeCollisionPane();
		JPanel bbox = makeBBoxPane();

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(origin)
		/**/.addComponent(coll)
		/**/.addComponent(bbox));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(origin)
		/**/.addComponent(coll)
		/**/.addComponent(bbox));

		return pane;
		}
	
	public BufferedImage paintBackground(int width, int height, int tile)
		{
		BufferedImage dest = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dest.createGraphics();

		g.setClip(0,0,width,height);
		g.setColor(new Color(Prefs.imagePreviewBackgroundColor));
		g.fillRect(0,0,width,height);
		int TILE = tile;
		g.setColor(new Color(Prefs.imagePreviewForegroundColor));
		int w = width / TILE + 1;
		int h = height / TILE + 1;
		for (int row = 0; row < h; row++)
			{
			for (int col = 0; col < w; col++)
				{
				if ((row + col) % 2 == 0)
					{
					g.fillRect(col * TILE,row * TILE,TILE,TILE);
					}
				}
			}
		return dest;
		}

	public BufferedImage compositeImage(BufferedImage dst, BufferedImage src)
		{
		BufferedImage img = new BufferedImage(src.getWidth(),src.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.drawImage(dst,0,0,src.getWidth(),src.getHeight(),null);
		g.drawImage(src,0,0,null);
		return img;
		}

	public class ImageLabel extends JLabel
		{
		/**
		 * 
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
				g.setColor(Color.red);
				g.drawRect(0,0,this.getWidth() - 1,this.getHeight() - 1);
				}
			g.dispose();
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
			float imgwidth = img.getWidth();
			float imgheight = img.getHeight();
			float width = 61;
			float height = width / imgwidth * imgheight;
			l.setPreferredSize(new Dimension(61,(int) height));
			//subList.setFixedCellWidth(61);
			//subList.setFixedCellHeight(61);
			if (transparencyBackground == null)
				{
				transparencyBackground = paintBackground((int) width,(int) height,7);
				}
			if (!(Boolean) res.get(PSprite.TRANSPARENT))
				{
				l.img = compositeImage(transparencyBackground,img);
				}
			else
				{
				l.img = compositeImage(transparencyBackground,Util.getTransparentIcon(img));
				}

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

		popup.add(makeJMenuItem("SpriteFrame.EDIT"));

		popup.addSeparator();

		popup.add(makeJMenuItem("SpriteFrame.SELECT_ALL"));

		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		pane.add(tool,BorderLayout.NORTH);

		tool.add(makeJButton("SpriteFrame.ADD"));
		tool.add(makeJButton("SpriteFrame.EDIT"));
		tool.add(makeJButton("SpriteFrame.EFFECT"));
		tool.add(makeJButton("SpriteFrame.REMOVE"));

		tool.addSeparator();

		tool.add(makeJButton("SpriteFrame.UNDO"));
		tool.add(makeJButton("SpriteFrame.REDO"));

		tool.addSeparator();

		//TODO: Implement undo/redo for this and effects
		tool.add(makeJButton("SpriteFrame.CUT"));
		tool.add(makeJButton("SpriteFrame.COPY"));
		tool.add(makeJButton("SpriteFrame.PASTE"));

		tool.addSeparator();

		subLeft = new JButton(LGM.getIconForKey("SpriteFrame.PREVIOUS")); //$NON-NLS-1$
		subLeft.addActionListener(this);
		tool.add(subLeft);

		show = new NumberField(0,res.subImages.size() - 1);
		show.setHorizontalAlignment(SwingConstants.CENTER);
		show.addValueChangeListener(this);
		show.setColumns(10);
		show.setMaximumSize(show.getPreferredSize());
		//		show.setValue(0);
		tool.add(show);

		subRight = new JButton(LGM.getIconForKey("SpriteFrame.NEXT")); //$NON-NLS-1$
		subRight.addActionListener(this);
		tool.add(subRight);

		//JLabel lab = new JLabel(Messages.getString("SpriteFrame.ANIM_SUBIMG")); //$NON-NLS-1$

		shiftBox = new JCheckBox(Messages.getString("SpriteFrame.SHIFT"),true);
		shiftBox.setSelected(false);
		tool.add(shiftBox);
		wrapBox = new JCheckBox(Messages.getString("SpriteFrame.WRAP"),true);
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
		subList.addMouseListener(this);
		subList.setDragEnabled(true);

		subList.addListSelectionListener(new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent ev)
					{
					if (timer == null)
						{
						setSubIndex(subList.getSelectedIndex());
						}

					}
			});

		subList.setCellRenderer(new ImageCellRenderer(subList));
		subList.setComponentPopupMenu(popup);

		subimagesScroll = new JScrollPane(subList);
		subimagesScroll.getVerticalScrollBar().setUnitIncrement(0);
		subimagesScroll.getHorizontalScrollBar().setUnitIncrement(0);
		//scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		pane.add(subimagesScroll,BorderLayout.CENTER);

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
			JFormattedTextField wField = new JFormattedTextField();
			wField.setValue(new Integer(width));
			JFormattedTextField hField = new JFormattedTextField();
			hField.setValue(new Integer(height));

			JPanel myPanel = new JPanel();
			GridLayout layout = new GridLayout(0,2);
			myPanel.setLayout(layout);
			myPanel.add(new JLabel("Width:"));
			myPanel.add(wField);
			//myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			myPanel.add(new JLabel("Height:"));
			myPanel.add(hField);

			int result = JOptionPane.showConfirmDialog(null,myPanel,"Enter Size of New Image",
					JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				{
				return null;
				}

			width = (Integer) wField.getValue();
			height = (Integer) hField.getValue();
			}
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = bi.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width,height);
		imageChanged = true;

		return bi;
		}

	public void editActionsPerformed(String cmd)
		{
		int pos = subList.getSelectedIndex();
		if (cmd.endsWith(".UNDO"))
			{

			return;
			}
		else if (cmd.endsWith(".REDO"))
			{

			return;
			}
		else if (cmd.endsWith(".SELECT_ALL"))
			{
			subList.setSelectionInterval(0,res.subImages.size() - 1);
			return;
			}
		else if (cmd.endsWith(".CUT"))
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
			subList.setSelectedIndex(pos - 1);

			return;
			}
		else if (cmd.endsWith(".COPY"))
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
		else if (cmd.endsWith(".PASTE"))
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
				res.subImages.addAll(pos + 1,images.bi);
				subList.setSelectionInterval(pos + 1,pos + images.bi.size());
				subList.setSelectionInterval(pos + 1,pos + images.bi.size());
				}

			//subList.setSelectedIndex(pos);
			return;
			}
		else if (cmd.endsWith(".ADD")) //$NON-NLS-1$
			{
			BufferedImage bi = createNewImage(res.subImages.size() == 0);
			if (bi != null)
				{
				pos = pos >= 0 ? pos + 1 : res.subImages.size();
				imageChanged = true;
				res.subImages.add(pos,bi);
				subList.setSelectedIndex(pos);
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
			subList.setSelectedIndex(Math.min(res.subImages.size() - 1,pos));
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

	private class AnimThread extends Thread
		{
		public boolean freeze = false;

		public void run()
			{
			while (!freeze && subList != null && preview != null)
				{
				//TODO: Shit throws all kinds of NPE's
				//These two are threaded because updating the Swing controls
				//Slows down the animation so its best to thread them to within a 
				//60 frame per second quality playback.
				//subList.setSelectedIndex(preview.getIndex());
				//updateImageControls();
				try
					{
					Thread.sleep(25);
					}
				catch (InterruptedException e)
					{
					LGM.showDefaultExceptionHandler(e);
					}
				}
			}
		}

	private AnimThread animThread = null;

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
			addFromStrip(true);
			return;
			}
		else if (e.getSource() == loadSubimage)
			{
			BufferedImage[] img = Util.getValidImages();
			if (img != null) addSubimages(img,false);
			return;
			}
		else if (e.getSource() == showBbox)
			{
			preview.setShowBbox(showBbox.isSelected());
			preview.updateUI();
			return;
			}
		else if (e.getSource() == showOrigin)
			{
			preview.setShowOrigin(showOrigin.isSelected());
			preview.updateUI();
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
				preview.enablemouse = false;
				preview.addMouseListener(mouseListener);
				preview.addMouseMotionListener(mouseMotionListener);
				}
			else
				{
				preview.enablemouse = true;
				preview.removeMouseListener(mouseListener);
				preview.removeMouseMotionListener(mouseMotionListener);
				}
			}
		else if (e.getSource() == zoomIn)
			{
			zoomIn();
			return;
			}
		else if (e.getSource() == zoomOut)
			{
			zoomOut();
			return;
			}
		else if (e.getSource() == play)
			{
			if (timer != null)
				{
				play.setIcon(PLAY_ICON);
				animThread = null;
				timer.stop();
				timer = null; //used to indicate that this is not animating, and frees memory
				updateImageControls();
				}
			else if (res.subImages.size() > 1)
				{
				if (animThread == null)
					{
					animThread = new AnimThread();

					animThread.start();
					}
				animThread.freeze = false;
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

	private void realizeScrollBarIncrement(JScrollPane scroll, Dimension size, Dimension scale)
		{
		JScrollBar vertical = scroll.getVerticalScrollBar();
		JScrollBar horizontal = scroll.getHorizontalScrollBar();
		if (vertical != null)
			{
			vertical.setUnitIncrement((int) (size.getWidth() / scale.width));
			}
		if (horizontal != null)
			{
			horizontal.setUnitIncrement((int) (size.getHeight() / scale.height));
			}
		}

	private void updateScrollBars()
		{
		realizeScrollBarIncrement(previewScroll,previewScroll.getSize(),new Dimension(5,5));
		realizeScrollBarIncrement(subimagesScroll,subimagesScroll.getPreferredSize(),new Dimension(4,4));
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

		Component[] comps = subList.getComponents();
		for (Component comp : comps)
			{
			comp.setSize(50,50);
			}
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

	public void lostOwnership(Clipboard arg0, Transferable arg1)
		{
		// TODO Auto-generated method stub
		System.out.println("Sprite editor has lost clipboard ownership.");
		}
	}
