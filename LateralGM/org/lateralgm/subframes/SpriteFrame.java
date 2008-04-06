/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.BevelBorder;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.impl.IndexButtonGroup;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.SubimagePreview;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sprite;

public class SpriteFrame extends ResourceFrame<Sprite> implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("SpriteFrame.LOAD"); //$NON-NLS-1$

	public JButton load;
	public JLabel width;
	public JLabel height;
	public JLabel subCount;
	public JButton subLeft;
	public JButton subRight;
	public JLabel showLab;
	public JLabel show;
	public int currSub;
	public JButton edit;
	public JCheckBox transparent;

	public JCheckBox preciseCC;
	public JCheckBox smooth;
	public JCheckBox preload;
	public IntegerField originX;
	public IntegerField originY;
	public JButton centre;
	public IndexButtonGroup bboxGroup;
	public JRadioButton auto;
	public JRadioButton full;
	public JRadioButton manual;
	public IntegerField bboxLeft;
	public IntegerField bboxRight;
	public IntegerField bboxTop;
	public IntegerField bboxBottom;
	public boolean imageChanged = false;

	public SubimagePreview preview;

	public SpriteFrame(Sprite res, ResNode node)
		{
		super(res,node);
		GroupLayout layout = new GroupLayout(getContentPane());
		setLayout(layout);
		JPanel side1 = new JPanel();
		makeSide1(side1);

		JPanel side2 = new JPanel();
		makeSide2(side2);

		preview = new SubimagePreview(this);
		preview.setVerticalAlignment(SwingConstants.TOP);
		JScrollPane scroll = new JScrollPane(preview);

		updateBoundingBox();
		updateImage();
		updateInfo();

		// The empty comments here prevent the formatter from messing up line wrapping and indentation.
		layout.setHorizontalGroup(layout.createSequentialGroup()
		/**/.addComponent(side1,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addComponent(side2,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addComponent(scroll,64,240,DEFAULT_SIZE));
		layout.setVerticalGroup(layout.createParallelGroup()
		/**/.addComponent(side1)
		/**/.addComponent(side2)
		/**/.addComponent(scroll,64,240,DEFAULT_SIZE));

		pack();
		}

	private void makeSide1(JPanel side1)
		{
		GroupLayout layout = new GroupLayout(side1);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		side1.setLayout(layout);

		JLabel lab = new JLabel(Messages.getString("SpriteFrame.NAME")); //$NON-NLS-1$
		load = new JButton(Messages.getString("SpriteFrame.LOAD")); //$NON-NLS-1$
		load.setIcon(LOAD_ICON);
		load.addActionListener(this);

		width = new JLabel();
		height = new JLabel();

		subCount = new JLabel();

		showLab = new JLabel(Messages.getString("SpriteFrame.SHOW")); //$NON-NLS-1$
		showLab.setHorizontalAlignment(JLabel.CENTER);
		subLeft = new JButton(LGM.getIconForKey("SpriteFrame.PREVIOUS")); //$NON-NLS-1$
		subLeft.addActionListener(this);
		currSub = 0;
		show = new JLabel();
		show.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		show.setHorizontalAlignment(JLabel.CENTER);
		subRight = new JButton(LGM.getIconForKey("SpriteFrame.NEXT")); //$NON-NLS-1$
		subRight.addActionListener(this);

		edit = new JButton(Messages.getString("SpriteFrame.EDIT_SPRITE")); //$NON-NLS-1$
		transparent = new JCheckBox(Messages.getString("SpriteFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setSelected(res.transparent);
		transparent.addActionListener(this);
		save.setText(Messages.getString("SpriteFrame.SAVE")); //$NON-NLS-1$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lab)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(load,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(width)
		/*		*/.addComponent(height))
		/**/.addComponent(subCount)
		/**/.addComponent(showLab)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(subLeft)
		/*		*/.addGap(4)
		/*		*/.addComponent(show,32,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addGap(4)
		/*		*/.addComponent(subRight))
		/**/.addComponent(edit,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(transparent)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lab)
		/*		*/.addComponent(name))
		/**/.addComponent(load)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(width)
		/*		*/.addComponent(height))
		/**/.addComponent(subCount)
		/**/.addComponent(showLab)
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*		*/.addComponent(subLeft)
		/*		*/.addComponent(show,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/*		*/.addComponent(subRight))
		/**/.addComponent(edit)
		/**/.addComponent(transparent)
		/**/.addGap(8,8,MAX_VALUE)
		/**/.addComponent(save));
		}

	private void makeSide2(JPanel side2)
		{
		GroupLayout layout = new GroupLayout(side2);
		layout.setAutoCreateContainerGaps(true);

		side2.setLayout(layout);

		preciseCC = new JCheckBox(Messages.getString("SpriteFrame.PRECISE_CC")); //$NON-NLS-1$
		preciseCC.setSelected(res.preciseCC);
		smooth = new JCheckBox(Messages.getString("SpriteFrame.SMOOTH")); //$NON-NLS-1$
		smooth.setSelected(res.smoothEdges);
		preload = new JCheckBox(Messages.getString("SpriteFrame.PRELOAD")); //$NON-NLS-1$
		preload.setSelected(res.preload);

		JPanel origin = new JPanel();
		GroupLayout oLayout = new GroupLayout(origin);
		oLayout.setAutoCreateGaps(true);
		oLayout.setAutoCreateContainerGaps(true);
		origin.setLayout(oLayout);
		origin.setBorder(BorderFactory.createTitledBorder(Messages.getString("SpriteFrame.ORIGIN"))); //$NON-NLS-1$
		JLabel oxLab = new JLabel(Messages.getString("SpriteFrame.X")); //$NON-NLS-1$
		oxLab.setHorizontalAlignment(SwingConstants.RIGHT);
		originX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.originX);
		originX.setColumns(4);
		originX.addActionListener(this);
		JLabel oyLab = new JLabel(Messages.getString("SpriteFrame.Y")); //$NON-NLS-1$;
		oyLab.setHorizontalAlignment(SwingConstants.RIGHT);
		originY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.originY);
		originY.setColumns(4);
		originY.addActionListener(this);
		centre = new JButton(Messages.getString("SpriteFrame.CENTER")); //$NON-NLS-1$
		centre.addActionListener(this);
		oLayout.setHorizontalGroup(oLayout.createParallelGroup(Alignment.CENTER)
		/**/.addGroup(oLayout.createSequentialGroup()
		/*		*/.addComponent(oxLab)
		/*		*/.addGap(4)
		/*		*/.addComponent(originX)
		/*		*/.addGap(12)
		/*		*/.addComponent(oyLab)
		/*		*/.addGap(4)
		/*		*/.addComponent(originY))
		/**/.addComponent(centre));
		oLayout.setVerticalGroup(oLayout.createSequentialGroup()
		/**/.addGroup(oLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(oxLab)
		/*		*/.addComponent(originX)
		/*		*/.addComponent(oyLab)
		/*		*/.addComponent(originY))
		/**/.addGap(8)
		/**/.addComponent(centre));

		JPanel bbox = new JPanel();
		GroupLayout bLayout = new GroupLayout(bbox);
		bbox.setLayout(bLayout);
		bbox.setBorder(BorderFactory.createTitledBorder(Messages.getString("SpriteFrame.BBOX"))); //$NON-NLS-1$
		bboxGroup = new IndexButtonGroup(3,true,false,this);
		auto = new JRadioButton(Messages.getString("SpriteFrame.AUTO")); //$NON-NLS-1$
		bboxGroup.add(auto);
		full = new JRadioButton(Messages.getString("SpriteFrame.FULL")); //$NON-NLS-1$
		bboxGroup.add(full);
		manual = new JRadioButton(Messages.getString("SpriteFrame.MANUAL")); //$NON-NLS-1$
		bboxGroup.add(manual);
		bboxGroup.setValue(res.boundingBoxMode);

		JLabel lLab = new JLabel(Messages.getString("SpriteFrame.LEFT")); //$NON-NLS-1$
		lLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxLeft = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxLeft);
		bboxLeft.setColumns(3);
		bboxLeft.addActionListener(this);

		JLabel rLab = new JLabel(Messages.getString("SpriteFrame.RIGHT")); //$NON-NLS-1$
		rLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxRight = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxRight);
		bboxRight.setColumns(3);
		bboxRight.addActionListener(this);

		JLabel tLab = new JLabel(Messages.getString("SpriteFrame.TOP")); //$NON-NLS-1$
		tLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxTop = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxTop);
		bboxTop.setColumns(3);
		bboxTop.addActionListener(this);

		JLabel bLab = new JLabel(Messages.getString("SpriteFrame.BOTTOM")); //$NON-NLS-1$
		bLab.setHorizontalAlignment(SwingConstants.RIGHT);
		bboxBottom = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxBottom);
		bboxBottom.setColumns(3);
		bboxBottom.addActionListener(this);

		bLayout.setHorizontalGroup(bLayout.createParallelGroup()
		/**/.addGroup(bLayout.createSequentialGroup()
		/*		*/.addComponent(auto)
		/**/.addComponent(full))
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

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(preciseCC)
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(origin)
		/**/.addComponent(bbox));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(preciseCC)
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(origin)
		/**/.addComponent(bbox));
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
		res.transparent = transparent.isSelected();
		res.preciseCC = preciseCC.isSelected();
		res.smoothEdges = smooth.isSelected();
		res.preload = preload.isSelected();
		res.originX = originX.getIntValue();
		res.originY = originY.getIntValue();
		res.boundingBoxMode = (byte) bboxGroup.getValue();
		res.boundingBoxLeft = bboxLeft.getIntValue();
		res.boundingBoxRight = bboxRight.getIntValue();
		res.boundingBoxTop = bboxTop.getIntValue();
		res.boundingBoxBottom = bboxBottom.getIntValue();
		}

	public void revertResource()
		{
		LGM.currentFile.sprites.replace(res,resOriginal);
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
				res.subImages.clear();
				imageChanged = true;
				currSub = 0;
				res.width = img[0].getWidth();
				res.height = img[0].getHeight();
				for (BufferedImage i : img)
					res.addSubImage(i);
				preview.setIcon(new ImageIcon(res.subImages.get(0)));
				updateInfo();
				updateBoundingBox();
				updateImage();
				node.updateIcon();
				return;
				}
			}
		if (e.getSource() == subRight)
			{
			if (currSub < res.subImages.size() - 1)
				{
				currSub += 1;
				updateImage();
				}
			return;
			}
		if (e.getSource() == subLeft)
			{
			if (currSub > 0)
				{
				currSub -= 1;
				updateImage();
				}
			return;
			}
		if (e.getSource() == edit)
			{
			return;
			}
		if (e.getSource() == centre)
			{
			originX.setIntValue(res.width / 2);
			originY.setIntValue(res.height / 2);
			return;
			}
		if (e.getSource() == auto || e.getSource() == full || e.getSource() == manual)
			{
			updateBoundingBox();
			return;
			}
		if (e.getSource() == transparent)
			{
			updateBoundingBox();
			res.transparent = transparent.isSelected();
			node.updateIcon();
			return;
			}
		if (e.getSource() == originX || e.getSource() == originY || e.getSource() == bboxLeft
				|| e.getSource() == bboxRight || e.getSource() == bboxTop || e.getSource() == bboxBottom)
			{
			preview.repaint(((JViewport) preview.getParent()).getViewRect());
			return;
			}
		super.actionPerformed(e);
		}

	public void updateInfo()
		{
		width.setText(Messages.getString("SpriteFrame.WIDTH") + res.width); //$NON-NLS-1$
		height.setText(Messages.getString("SpriteFrame.HEIGHT") + res.height); //$NON-NLS-1$
		subCount.setText(Messages.getString("SpriteFrame.NO_OF_SUBIMAGES") //$NON-NLS-1$
				+ res.subImages.size());
		}

	// TODO cache auto bbox
	public void updateBoundingBox()
		{
		int mode = bboxGroup.getValue();
		switch (mode)
			{
			case Sprite.BBOX_AUTO:
				Rectangle r = transparent.isSelected() ? getOverallBounds() : new Rectangle(0,0,
						res.width - 1,res.height - 1);
				res.boundingBoxLeft = r.x;
				res.boundingBoxRight = r.x + r.width;
				res.boundingBoxTop = r.y;
				res.boundingBoxBottom = r.y + r.height;
				break;
			case Sprite.BBOX_FULL:
				res.boundingBoxLeft = 0;
				res.boundingBoxRight = res.width - 1;
				res.boundingBoxTop = 0;
				res.boundingBoxBottom = res.height - 1;
				break;
			default:
				break;
			}
		bboxLeft.setIntValue(res.boundingBoxLeft);
		bboxRight.setIntValue(res.boundingBoxRight);
		bboxTop.setIntValue(res.boundingBoxTop);
		bboxBottom.setIntValue(res.boundingBoxBottom);
		bboxLeft.setEnabled(mode == Sprite.BBOX_MANUAL);
		bboxRight.setEnabled(mode == Sprite.BBOX_MANUAL);
		bboxTop.setEnabled(mode == Sprite.BBOX_MANUAL);
		bboxBottom.setEnabled(mode == Sprite.BBOX_MANUAL);
		}

	public void updateImage()
		{
		BufferedImage img = getSubimage();
		if (img != null)
			{
			preview.setIcon(new ImageIcon(img));
			subLeft.setEnabled(currSub > 0);
			subRight.setEnabled(currSub < res.subImages.size() - 1);
			show.setText(Integer.toString(currSub));
			}
		else
			{
			preview.setIcon(null);
			subLeft.setEnabled(false);
			subRight.setEnabled(false);
			show.setText(""); //$NON-NLS-1$
			}
		}

	public BufferedImage getSubimage()
		{
		return res.subImages.size() > 0 ? res.subImages.get(currSub) : null;
		}

	public static Rectangle getCropBounds(BufferedImage img)
		{
		int transparent = img.getRGB(0,img.getHeight() - 1);
		int width = img.getWidth();
		int height = img.getHeight();

		int y1 = -1;
		y1loop: for (int j = 0; j < height; j++)
			for (int i = 0; i < width; i++)
				if (img.getRGB(i,j) != transparent)
					{
					y1 = j;
					break y1loop;
					}
		if (y1 == -1) return new Rectangle(0,0,0,0);

		int x1 = 0;
		x1loop: for (int i = 0; i < width; i++)
			for (int j = y1; j < height; j++)
				if (img.getRGB(i,j) != transparent)
					{
					x1 = i;
					break x1loop;
					}

		int y2 = 0;
		y2loop: for (int j = height - 1; j > 0; j--)
			for (int i = x1; i < width; i++)
				if (img.getRGB(i,j) != transparent)
					{
					y2 = j;
					break y2loop;
					}

		int x2 = 0;
		x2loop: for (int i = width - 1; i > 0; i--)
			for (int j = y1; j < y2; j++)
				if (img.getRGB(i,j) != transparent)
					{
					x2 = i;
					break x2loop;
					}
		return new Rectangle(x1,y1,x2 - x1,y2 - y1);
		}

	public Rectangle getOverallBounds()
		{
		Rectangle rects[] = new Rectangle[res.subImages.size()];
		for (int i = 0; i < rects.length; i++)
			rects[i] = getCropBounds(res.subImages.get(i));
		for (int i = 1; i < rects.length; i++)
			rects[0] = rects[0].union(rects[i]);
		return rects.length > 0 ? rects[0] : new Rectangle(0,0,res.width - 1,res.height - 1);
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
	}
