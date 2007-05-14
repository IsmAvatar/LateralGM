/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.lateralgm.components.CustomFileFilter;
import org.lateralgm.components.IndexButtonGroup;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sprite;

public class SpriteFrame extends ResourceFrame<Sprite> implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon frameIcon = LGM.getIconForKey("SpriteFrame.SPRITE"); //$NON-NLS-1$
	private static final ImageIcon saveIcon = LGM.getIconForKey("SpriteFrame.SAVE"); //$NON-NLS-1$
	private static final ImageIcon loadIcon = LGM.getIconForKey("SpriteFrame.LOAD");//$NON-NLS-1$

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
	private JFileChooser fc;
	public boolean imageChanged = false;

	public JLabel preview;

	public SpriteFrame(Sprite res, ResNode node)
		{
		super(res,node);

		fc = new JFileChooser();
		// fc.setAccessory(new ImagePreview(fc));
		String exts[] = ImageIO.getReaderFileSuffixes();
		for (int i = 0; i < exts.length; i++)
			exts[i] = "." + exts[i]; //$NON-NLS-1$
		CustomFileFilter filt = new CustomFileFilter(exts,Messages.getString("SpriteFrame.ALL_SPI_IMAGES")); //$NON-NLS-1$
		fc.addChoosableFileFilter(filt);
		for (int i = 0; i < exts.length; i++)
			{
			fc.addChoosableFileFilter(new CustomFileFilter(exts[i],exts[i]
					+ Messages.getString("SpriteFrame.FILES"))); //$NON-NLS-1$
			}
		fc.setFileFilter(filt);

		setSize(560,320);
		setMinimumSize(new Dimension(560,320));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setFrameIcon(frameIcon);

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setMinimumSize(new Dimension(180,280));
		side1.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));
		side1.setPreferredSize(new Dimension(180,280));

		// side1.setBackground(Color.RED);
		JLabel lab = new JLabel(Messages.getString("SpriteFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,14));
		side1.add(lab);
		name.setPreferredSize(new Dimension(120,20));
		side1.add(name);
		load = new JButton(Messages.getString("SpriteFrame.LOAD")); //$NON-NLS-1$
		load.setIcon(loadIcon);
		load.setPreferredSize(new Dimension(130,24));
		load.addActionListener(this);
		side1.add(load);

		width = new JLabel();
		width.setPreferredSize(new Dimension(80,16));
		side1.add(width);
		height = new JLabel();
		height.setPreferredSize(new Dimension(80,16));
		side1.add(height);
		subCount = new JLabel();
		subCount.setPreferredSize(new Dimension(160,16));
		side1.add(subCount);

		addGap(side1,160,10);
		showLab = new JLabel(Messages.getString("SpriteFrame.SHOW")); //$NON-NLS-1$
		showLab.setPreferredSize(new Dimension(200,16));
		showLab.setHorizontalAlignment(JLabel.CENTER);
		side1.add(showLab);
		// TODO Possibly get an icon for the arrows
		subLeft = new JButton("<"); //$NON-NLS-1$
		subLeft.setPreferredSize(new Dimension(45,20));
		subLeft.addActionListener(this);
		side1.add(subLeft);
		currSub = 0;
		show = new JLabel();
		show.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		show.setPreferredSize(new Dimension(30,20));
		show.setHorizontalAlignment(JLabel.CENTER);
		side1.add(show);
		subRight = new JButton(">"); //$NON-NLS-1$
		subRight.setPreferredSize(new Dimension(45,20));
		subRight.addActionListener(this);
		side1.add(subRight);

		addGap(side1,160,10);

		edit = new JButton(Messages.getString("SpriteFrame.EDIT_SPRITE")); //$NON-NLS-1$
		edit.setPreferredSize(new Dimension(130,24));
		side1.add(edit);
		transparent = new JCheckBox(Messages.getString("SpriteFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setPreferredSize(new Dimension(160,16));
		transparent.setSelected(res.transparent);
		side1.add(transparent);

		addGap(side1,100,20);

		save.setPreferredSize(new Dimension(130,24));
		save.setText(Messages.getString("SpriteFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(saveIcon);
		side1.add(save);

		JPanel side2 = new JPanel(new FlowLayout());
		side2.setMinimumSize(new Dimension(200,280));
		side2.setPreferredSize(new Dimension(200,280));
		side2.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
		// side2.setPreferredSize(new Dimension(200,280));
		// side2.setBackground(Color.GREEN);

		preciseCC = new JCheckBox(Messages.getString("SpriteFrame.PRECISE_CC")); //$NON-NLS-1$
		preciseCC.setPreferredSize(new Dimension(180,16));
		preciseCC.setSelected(res.preciseCC);
		side2.add(preciseCC);
		smooth = new JCheckBox(Messages.getString("SpriteFrame.SMOOTH")); //$NON-NLS-1$
		smooth.setPreferredSize(new Dimension(180,16));
		smooth.setSelected(res.smoothEdges);
		side2.add(smooth);
		preload = new JCheckBox(Messages.getString("SpriteFrame.PRELOAD")); //$NON-NLS-1$
		preload.setPreferredSize(new Dimension(180,16));
		preload.setSelected(res.preload);
		side2.add(preload);

		JPanel origin = new JPanel(new FlowLayout());
		origin.setBorder(BorderFactory.createTitledBorder(Messages.getString("SpriteFrame.ORIGIN"))); //$NON-NLS-1$
		origin.setPreferredSize(new Dimension(200,80));
		lab = new JLabel(Messages.getString("SpriteFrame.X")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(20,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		origin.add(lab);
		originX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.originX);
		originX.setPreferredSize(new Dimension(40,20));
		origin.add(originX);
		lab = new JLabel(Messages.getString("SpriteFrame.Y")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(20,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		origin.add(lab);
		originY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.originY);
		originY.setPreferredSize(new Dimension(40,20));
		origin.add(originY);
		centre = new JButton(Messages.getString("SpriteFrame.CENTRE")); //$NON-NLS-1$
		centre.setPreferredSize(new Dimension(80,20));
		centre.addActionListener(this);
		origin.add(centre);
		side2.add(origin);

		JPanel bbox = new JPanel(new FlowLayout());
		bbox.setBorder(BorderFactory.createTitledBorder(Messages.getString("SpriteFrame.BBOX"))); //$NON-NLS-1$
		bbox.setPreferredSize(new Dimension(200,120));

		bboxGroup = new IndexButtonGroup(3);
		auto = new JRadioButton(Messages.getString("SpriteFrame.AUTO")); //$NON-NLS-1$
		auto.setPreferredSize(new Dimension(85,16));
		auto.addActionListener(this);
		bboxGroup.add(auto,Sprite.BBOX_AUTO);
		full = new JRadioButton(Messages.getString("SpriteFrame.FULL")); //$NON-NLS-1$
		full.setPreferredSize(new Dimension(85,16));
		full.addActionListener(this);
		bboxGroup.add(full,Sprite.BBOX_FULL);
		manual = new JRadioButton(Messages.getString("SpriteFrame.MANUAL")); //$NON-NLS-1$
		manual.setPreferredSize(new Dimension(85,16));
		manual.addActionListener(this);
		bboxGroup.add(manual,Sprite.BBOX_MANUAL);
		bboxGroup.setValue(res.boundingBoxMode);
		bboxGroup.populate(bbox);
		addGap(bbox,85,16);

		lab = new JLabel(Messages.getString("SpriteFrame.LEFT")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(25,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxLeft = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxLeft);
		bboxLeft.setPreferredSize(new Dimension(40,20));
		bbox.add(bboxLeft);

		lab = new JLabel(Messages.getString("SpriteFrame.RIGHT")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(45,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxRight = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxRight);
		bboxRight.setPreferredSize(new Dimension(40,20));
		bbox.add(bboxRight);

		lab = new JLabel(Messages.getString("SpriteFrame.TOP")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(25,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxTop = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxTop);
		bboxTop.setPreferredSize(new Dimension(40,20));
		bbox.add(bboxTop);

		lab = new JLabel(Messages.getString("SpriteFrame.BOTTOM")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(45,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxBottom = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxBottom);
		bboxBottom.setPreferredSize(new Dimension(40,20));
		bbox.add(bboxBottom);

		side2.add(bbox);

		// TODO draw the origin and BBox
		preview = new JLabel();
		preview.setVerticalAlignment(SwingConstants.TOP);
		JScrollPane scroll = new JScrollPane(preview);

		updatePreview();

		add(side1);
		add(side2);
		add(scroll);
		}

	@Override
	public boolean resourceChanged()
		{
		System.out.println(!resOriginal.getName().equals(name.getText()));
		System.out.println(imageChanged);
		System.out.println(resOriginal.transparent != transparent.isSelected());
		System.out.println(resOriginal.preciseCC != preciseCC.isSelected());
		System.out.println(resOriginal.smoothEdges != smooth.isSelected());
		System.out.println(resOriginal.preload != preload.isSelected());
		System.out.println(resOriginal.originX != originX.getIntValue());
		System.out.println(resOriginal.originY != originY.getIntValue());
		System.out.println(resOriginal.boundingBoxMode != (byte) bboxGroup.getValue());
		System.out.println(resOriginal.boundingBoxLeft != bboxLeft.getIntValue());
		System.out.println(resOriginal.boundingBoxRight != bboxRight.getIntValue());
		System.out.println(resOriginal.boundingBoxTop != bboxTop.getIntValue());
		System.out.println(resOriginal.boundingBoxBottom != bboxBottom.getIntValue());

		// For now, BBOX_AUTO is the same as full, which triggers false changed dialogs
		if (bboxGroup.getValue() == Sprite.BBOX_MANUAL)
			return (!resOriginal.getName().equals(name.getText()) || imageChanged
					|| resOriginal.transparent != transparent.isSelected()
					|| resOriginal.preciseCC != preciseCC.isSelected()
					|| resOriginal.smoothEdges != smooth.isSelected() || resOriginal.preload != preload.isSelected()
					|| resOriginal.originX != originX.getIntValue() || resOriginal.originY != originY.getIntValue()
					|| resOriginal.boundingBoxMode != (byte) bboxGroup.getValue()
					|| resOriginal.boundingBoxLeft != bboxLeft.getIntValue()
					|| resOriginal.boundingBoxRight != bboxRight.getIntValue()
					|| resOriginal.boundingBoxTop != bboxTop.getIntValue() || resOriginal.boundingBoxBottom != bboxBottom
					.getIntValue());
		else
			return (!resOriginal.getName().equals(name.getText()) || imageChanged
					|| resOriginal.transparent != transparent.isSelected()
					|| resOriginal.preciseCC != preciseCC.isSelected()
					|| resOriginal.smoothEdges != smooth.isSelected() || resOriginal.preload != preload.isSelected()
					|| resOriginal.originX != originX.getIntValue() || resOriginal.originY != originY.getIntValue() || resOriginal.boundingBoxMode != (byte) bboxGroup
					.getValue());
		}

	public void revertResource()
		{
		LGM.currentFile.sprites.replace(res.getId(),resOriginal);
		}

	public void updateResource()
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
		resOriginal = (Sprite) res.copy(false,null);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			if (fc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
				{
				try
					{
					BufferedImage img = ImageIO.read(fc.getSelectedFile());
					res.clearSubImages();
					res.addSubImage(img);
					res.width = img.getWidth();
					res.height = img.getHeight();
					imageChanged = true;
					updatePreview();
					}
				catch (Throwable t)
					{
					JOptionPane.showMessageDialog(LGM.frame,
							Messages.getString("SpriteFrame.ERROR_LOADING") + fc.getSelectedFile().getPath()); //$NON-NLS-1$
					}
				}
			return;
			}
		if (e.getSource() == subRight)
			{
			if (currSub < res.NoSubImages() - 1)
				{
				currSub += 1;
				updatePreview();
				}
			return;
			}
		if (e.getSource() == subLeft)
			{
			if (currSub > 0)
				{
				currSub -= 1;
				updatePreview();
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
		if (e.getSource() == auto || e.getSource() == manual || e.getSource() == full)
			{
			updatePreview();
			return;
			}
		super.actionPerformed(e);
		}

	public void updatePreview()
		{
		width.setText(Messages.getString("SpriteFrame.WIDTH") + res.width); //$NON-NLS-1$
		height.setText(Messages.getString("SpriteFrame.HEIGHT") + res.height); //$NON-NLS-1$
		subCount.setText(Messages.getString("SpriteFrame.NO_OF_SUBIMAGES") + res.NoSubImages()); //$NON-NLS-1$
		if (bboxGroup.getValue() == Sprite.BBOX_AUTO || bboxGroup.getValue() == Sprite.BBOX_FULL)
			{
			// TODO Implement Auto BBox code
			bboxLeft.setIntValue(0);
			bboxRight.setIntValue(res.width - 1);
			bboxTop.setIntValue(0);
			bboxBottom.setIntValue(res.height - 1);
			bboxLeft.setEnabled(false);
			bboxRight.setEnabled(false);
			bboxTop.setEnabled(false);
			bboxBottom.setEnabled(false);
			}
		else
			{
			bboxLeft.setEnabled(true);
			bboxRight.setEnabled(true);
			bboxTop.setEnabled(true);
			bboxBottom.setEnabled(true);
			}
		switch (res.NoSubImages())
			{
			case 0:
				subLeft.setEnabled(false);
				subRight.setEnabled(false);
				show.setText(""); //$NON-NLS-1$
				break;
			case 1:
				preview.setIcon(new ImageIcon(res.getSubImage(0)));
				subLeft.setEnabled(false);
				subRight.setEnabled(false);
				show.setText("0"); //$NON-NLS-1$
				break;
			default:
				preview.setIcon(new ImageIcon(res.getSubImage(currSub)));
				subLeft.setEnabled(currSub > 0);
				subRight.setEnabled(currSub < res.NoSubImages() - 1);
				show.setText(Integer.toString(currSub));
				break;
			}
		}
	}
