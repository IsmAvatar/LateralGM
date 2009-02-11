/*
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
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
		MouseListener,UpdateListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("SpriteFrame.LOAD"); //$NON-NLS-1$
	private static final ImageIcon PLAY_ICON = LGM.getIconForKey("SpriteFrame.PLAY"); //$NON-NLS-1$
	private static final ImageIcon STOP_ICON = LGM.getIconForKey("SpriteFrame.STOP"); //$NON-NLS-1$

	//toolbar
	public JButton load;
	public JCheckBox transparent;

	//origin
	public NumberField originX, originY;
	public JButton centre;

	//bbox
	public IndexButtonGroup bboxGroup;
	public NumberField bboxLeft, bboxRight;
	public NumberField bboxTop, bboxBottom;
	public JRadioButton auto, full, manual;

	//properties
	public JCheckBox preciseCC, smooth, preload;
	public JLabel subCount, width, height;

	//subimages
	public JList subList;

	//preview
	public SubimagePreview preview;
	public IntegerField show, speed;
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

		load = new JButton(Messages.getString("SpriteFrame.LOAD"),LOAD_ICON); //$NON-NLS-1$
		load.addActionListener(this);
		tool.add(load);

		tool.addSeparator();

		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("SpriteFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		transparent = new JCheckBox(Messages.getString("SpriteFrame.TRANSPARENT")); //$NON-NLS-1$
		plf.make(transparent,PSprite.TRANSPARENT);
		transparent.setOpaque(false); //prevent white background
		tool.add(transparent);

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
		originX = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE);
		originX.setColumns(4);
		plf.make(originX,PSprite.ORIGIN_X);
		JLabel oyLab = new JLabel(Messages.getString("SpriteFrame.Y")); //$NON-NLS-1$;
		oyLab.setHorizontalAlignment(SwingConstants.RIGHT);
		originY = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE);
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
		bboxLeft = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE);
		bboxLeft.setColumns(3);
		plf.make(bboxLeft,PSprite.BB_LEFT);

		JLabel rLab = new JLabel(Messages.getString("SpriteFrame.RIGHT")); //$NON-NLS-1$
		rLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxRight = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE);
		bboxRight.setColumns(3);
		plf.make(bboxRight,PSprite.BB_RIGHT);

		JLabel tLab = new JLabel(Messages.getString("SpriteFrame.TOP")); //$NON-NLS-1$
		tLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxTop = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE);
		bboxTop.setColumns(3);
		plf.make(bboxTop,PSprite.BB_TOP);

		JLabel bLab = new JLabel(Messages.getString("SpriteFrame.BOTTOM")); //$NON-NLS-1$
		bLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxBottom = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE);
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

		preciseCC = new JCheckBox(Messages.getString("SpriteFrame.PRECISE_CC")); //$NON-NLS-1$
		plf.make(preciseCC,PSprite.PRECISE);
		smooth = new JCheckBox(Messages.getString("SpriteFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PSprite.SMOOTH_EDGES);
		preload = new JCheckBox(Messages.getString("SpriteFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PSprite.PRELOAD);
		subCount = new JLabel();
		width = new JLabel();
		height = new JLabel();

		JPanel origin = makeOriginPane();
		JPanel bbox = makeBBoxPane();

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(preciseCC)
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(subCount,Alignment.CENTER)
		/**/.addGroup(Alignment.CENTER,layout.createSequentialGroup()
		/*	*/.addComponent(width)
		/*	*/.addGap(12)
		/*	*/.addComponent(height))
		/**/.addComponent(origin)
		/**/.addComponent(bbox));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(preciseCC)
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(subCount)
		/**/.addGap(4)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(width)
		/*	*/.addComponent(height))
		/**/.addComponent(origin)
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

		makeToolButton(tool,"SpriteFrame.ADD");
		makeToolButton(tool,"SpriteFrame.REMOVE");
		tool.addSeparator();
		makeToolButton(tool,"SpriteFrame.PREVIOUS");
		makeToolButton(tool,"SpriteFrame.NEXT");

		subList = new JList();
		subList.addMouseListener(this);
		pane.add(new JScrollPane(subList),BorderLayout.CENTER);
		return pane;
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
		preview.setVerticalAlignment(SwingConstants.TOP);
		JScrollPane scroll = new JScrollPane(preview);
		//scroll.setSize(64,240);

		JPanel controls = new JPanel();
		GroupLayout layout = new GroupLayout(controls);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		controls.setLayout(layout);

		subLeft = new JButton(LGM.getIconForKey("SpriteFrame.PREVIOUS")); //$NON-NLS-1$
		subLeft.addActionListener(this);

		show = new IntegerField(0,res.subImages.size() - 1);
		show.setHorizontalAlignment(IntegerField.CENTER);
		show.addActionListener(this);

		subRight = new JButton(LGM.getIconForKey("SpriteFrame.NEXT")); //$NON-NLS-1$
		subRight.addActionListener(this);

		JLabel lab = new JLabel(Messages.getString("SpriteFrame.ANIM_SUBIMG")); //$NON-NLS-1$
		JLabel lab2 = new JLabel(Messages.getString("SpriteFrame.ANIM_SPEED")); //$NON-NLS-1$
		lab2.setHorizontalAlignment(JLabel.CENTER);

		speed = new IntegerField(1,Integer.MAX_VALUE,30);
		speed.setToolTipText(Messages.getString("SpriteFrame.CALC_TIP")); //$NON-NLS-1$
		speed.addActionListener(this);
		speed.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
					{
					//works for all mouse buttons
					if ((e.getModifiers() | MouseEvent.CTRL_DOWN_MASK) != 0)
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
		IntegerField rs = new IntegerField(1,Integer.MAX_VALUE,speed.getIntValue());
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
		speed.setIntValue((int) (i * d));
		//triggers listener
		}

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		if (imageChanged) return true;
		ResourceComparator c = new ResourceComparator();
		c.addExclusions(Sprite.class,"subImages","imageCache"); //$NON-NLS-1$ //$NON-NLS-2$
		return !c.areEqual(res,resOriginal);
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		}

	public void revertResource()
		{
		resOriginal.updateReference();
		}

	public void updateResource()
		{
		super.updateResource();
		imageChanged = false;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			BufferedImage[] img = Util.getValidImages();
			if (img != null && img.length > 0)
				{
				cleanup();
				res.subImages.clear();
				imageChanged = true;
				for (BufferedImage i : img)
					res.addSubImage(i);
				preview.setIcon(new ImageIcon(res.subImages.get(0)));
				show.setRange(0,res.subImages.size());
				setSubIndex(0);
				updateInfo();
				return;
				}
			}
		if (e.getSource() == subLeft)
			{
			if (currSub > 0) setSubIndex(currSub - 1);
			return;
			}
		if (e.getSource() == show)
			{
			updateSub = false;
			setSubIndex(show.getIntValue());
			updateSub = true;
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
		if (e.getSource() == speed)
			{
			if (timer != null) timer.setDelay(1000 / speed.getIntValue());
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
		if (cmd != null && cmd.startsWith("SpriteFrame.")) handleToolbarEvent(cmd.substring(12));
		System.out.println(e);

		super.actionPerformed(e);
		}

	private void handleToolbarEvent(String cmd)
		{
		int pos = subList.getSelectedIndex();
		if (cmd.equals("ADD"))
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
		if (cmd.equals("REMOVE"))
			{
			ImageEditor ie = editors == null ? null : editors.get(res.subImages.get(pos));
			imageChanged = true;
			res.subImages.remove(pos);
			if (ie != null) ie.stop();
			subList.setSelectedIndex(Math.min(res.subImages.size() - 1,pos));
			return;
			}
		if (cmd.equals("PREVIOUS"))
			{
			if (pos == 0) return;
			imageChanged = true;
			BufferedImage bi = res.subImages.remove(pos);
			res.subImages.add(pos - 1,bi);
			subList.setSelectedIndex(pos - 1);
			return;
			}
		if (cmd.equals("NEXT"))
			{
			System.out.println("lo");
			if (pos == res.subImages.size() - 1) return;
			imageChanged = true;
			BufferedImage bi = res.subImages.remove(pos);
			res.subImages.add(pos + 1,bi);
			subList.setSelectedIndex(pos + 1);
			return;
			}
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
			play.setEnabled(res.subImages.size() > 1);
			if (updateSub)
				{
				show.setEnabled(timer == null);
				show.setIntValue(currSub);
				}
			}
		else
			{
			subLeft.setEnabled(false);
			subRight.setEnabled(false);
			play.setEnabled(false);
			if (updateSub)
				{
				show.setIntValue(0);
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
		if (!Prefs.useExternalSpriteEditor)
			{
			throw new UnsupportedOperationException("no internal sprite editor");
			}
		try
			{
			ImageEditor ie = editors == null ? null : editors.get(img);
			if (ie == null)
				new ImageEditor(img);
			else
				ie.start();
			}
		catch (IOException e)
			{
			e.printStackTrace();
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

		public ImageEditor(BufferedImage i) throws IOException
			{
			image = i;
			File f = File.createTempFile(res.getName(),".bmp",LGM.tempDir);
			f.deleteOnExit();
			FileOutputStream out = new FileOutputStream(f);
			ImageIO.write(i,"bmp",out);
			out.close();
			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this,true);
			if (editors == null) editors = new HashMap<BufferedImage,ImageEditor>();
			editors.put(i,this);
			start();
			}

		public void start() throws IOException
			{
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
					ColorConvertOp conv = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB),null);
					BufferedImage dest = new BufferedImage(img.getWidth(),img.getHeight(),
							BufferedImage.TYPE_3BYTE_BGR);
					conv.filter(img,dest);
					res.subImages.replace(image,dest);
					editors.remove(image);
					editors.put(dest,this);
					image = dest;
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

	protected void cleanup()
		{
		if (editors != null)
			for (ImageEditor ie : editors.values().toArray(new ImageEditor[editors.size()]))
				ie.stop();
		}
	}
