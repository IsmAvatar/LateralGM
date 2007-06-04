/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au> This file is part of Lateral GM. Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY. See LICENSE for details.
 */

package org.lateralgm.subframes;

import org.lateralgm.components.ResNode;
import org.lateralgm.resources.Background;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

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
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.lateralgm.components.CustomFileFilter;
// import org.lateralgm.components.ImagePreview;
import org.lateralgm.components.BackgroundPreview;
import org.lateralgm.components.IndexButtonGroup;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResNode;
import org.lateralgm.components.SubimagePreview;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sprite;

public class BackgroundFrame extends ResourceFrame<Background>
	{
	private static final long serialVersionUID = 1L;
	private static ImageIcon frameIcon = LGM.getIconForKey("BackgroundFrame.BACKGROUND"); //$NON-NLS-1$
	private static ImageIcon loadIcon = LGM.getIconForKey("BackgroundFrame.LOAD"); //$NON-NLS-1$
	private static ImageIcon saveIcon = LGM.getIconForKey("BackgroundFrame.SAVE"); //$NON-NLS-1$
	public JButton load;
	public JLabel width;
	public JLabel height;
	public JCheckBox transparent;
	public JButton edit;
	public JCheckBox smooth;
	public JCheckBox preload;
	public JCheckBox tileset;

	public JPanel side2;
	public IntegerField tWidth;
	public IntegerField tHeight;
	public IntegerField hOffset;
	public IntegerField vOffset;
	public IntegerField hSep;
	public IntegerField vSep;
	public BackgroundPreview preview;
	public boolean imageChanged = false;

	public BackgroundFrame(Background res, ResNode node)
		{
		super(res,node);

		setSize(560,320);
		setMinimumSize(new Dimension(450,320));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setFrameIcon(frameIcon);

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setMinimumSize(new Dimension(180,280));
		side1.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));
		side1.setPreferredSize(new Dimension(180,280));

		// side1.setBackground(Color.RED);
		addDim(side1,new JLabel(Messages.getString("BackgroundFrame.NAME")),40,14); //$NON-NLS-1$
		addDim(name,120,20);

		load = new JButton(Messages.getString("SpriteFrame.LOAD")); //$NON-NLS-1$
		load.setIcon(loadIcon);
		load.addActionListener(this);
		addDim(load,130,24);
		width = new JLabel(Messages.getString("BackgroundFrame.WIDTH") + res.width); //$NON-NLS-1$
		addDim(width,80,16);
		height = new JLabel(Messages.getString("BackgroundFrame.HEIGHT") + res.height); //$NON-NLS-1$
		addDim(height,80,16);

		addGap(side1,160,10);

		edit = new JButton(Messages.getString("BackgroundFrame.EDIT")); //$NON-NLS-1$
		edit.addActionListener(this);
		addDim(edit,130,24);

		addGap(side1,160,15);

		transparent = new JCheckBox(Messages.getString("BackgroundFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setSelected(res.transparent);
		addDim(transparent,130,16);
		smooth = new JCheckBox(Messages.getString("BackgroundFrame.SMOOTH")); //$NON-NLS-1$
		smooth.setSelected(res.smoothEdges);
		addDim(smooth,130,16);
		preload = new JCheckBox(Messages.getString("BackgroundFrame.PRELOAD")); //$NON-NLS-1$
		preload.setSelected(res.preload);
		addDim(preload,130,16);
		tileset = new JCheckBox(Messages.getString("BackgroundFrame.USE_AS_TILESET")); //$NON-NLS-1$
		tileset.setSelected(res.useAsTileSet);
		tileset.addActionListener(this);
		addDim(tileset,130,16);

		addGap(side1,160,15);

		save.setText(Messages.getString("BackgroundFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(saveIcon);
		addDim(save,130,24);

		side2 = new JPanel(new FlowLayout());
		side2.setPreferredSize(new Dimension(180,260));
		side2.setMinimumSize(new Dimension(180,260));
		side2.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));
		JPanel group = new JPanel(new FlowLayout()); // BoxLayout does what it wants, so this has to be
		// separate to stay a constant size
		group.setPreferredSize(new Dimension(180,270));
		group.setBorder(BorderFactory.createTitledBorder(Messages.getString("BackgroundFrame.TILE_PROPERTIES"))); //$NON-NLS-1$

		JLabel lab = new JLabel(Messages.getString("BackgroundFrame.TILE_WIDTH")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		tWidth = new IntegerField(0,Integer.MAX_VALUE,res.tileWidth);
		tWidth.addActionListener(this);
		addDim(tWidth,50,20);

		lab = new JLabel(Messages.getString("BackgroundFrame.TILE_HEIGHT")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		tHeight = new IntegerField(0,Integer.MAX_VALUE,res.tileHeight);
		tHeight.addActionListener(this);
		addDim(tHeight,50,20);

		addGap(group,150,15);

		lab = new JLabel(Messages.getString("BackgroundFrame.H_OFFSET")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		hOffset = new IntegerField(0,Integer.MAX_VALUE,res.horizOffset);
		hOffset.addActionListener(this);
		addDim(hOffset,50,20);

		lab = new JLabel(Messages.getString("BackgroundFrame.V_OFFSET")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		vOffset = new IntegerField(0,Integer.MAX_VALUE,res.vertOffset);
		vOffset.addActionListener(this);
		addDim(vOffset,50,20);

		addGap(group,150,15);

		lab = new JLabel(Messages.getString("BackgroundFrame.H_SEP")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		hSep = new IntegerField(0,Integer.MAX_VALUE,res.horizSep);
		hSep.addActionListener(this);
		addDim(hSep,50,20);

		lab = new JLabel(Messages.getString("BackgroundFrame.V_SEP")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		vSep = new IntegerField(0,Integer.MAX_VALUE,res.vertSep);
		vSep.addActionListener(this);
		addDim(vSep,50,20);

		side2.add(group);
		side2.setVisible(tileset.isSelected());

		add(side1);
		add(side2);
		preview = new BackgroundPreview(this);
		if (res.backgroundImage != null)
			preview.setIcon(new ImageIcon(res.backgroundImage));
		else
			preview.setPreferredSize(new Dimension(0,0));
		preview.setVerticalAlignment(SwingConstants.TOP);
		add(new JScrollPane(preview));
		}

	@Override
	public boolean resourceChanged()
		{
		return !res.getName().equals(name.getText()) || res.transparent != transparent.isSelected()
				|| res.smoothEdges != smooth.isSelected() || res.preload != preload.isSelected()
				|| res.useAsTileSet != tileset.isSelected() || res.tileWidth != tWidth.getIntValue()
				|| res.tileHeight != tWidth.getIntValue() || res.horizOffset != hOffset.getIntValue()
				|| res.vertOffset != vOffset.getIntValue() || res.horizSep != hSep.getIntValue()
				|| res.vertSep != vSep.getIntValue();
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.backgrounds.replace(res.getId(),resOriginal);
		}

	@Override
	public void updateResource()
		{
		res.setName(name.getText());
		res.transparent = transparent.isSelected();
		res.smoothEdges = smooth.isSelected();
		res.preload = preload.isSelected();
		res.useAsTileSet = tileset.isSelected();
		res.tileWidth = tWidth.getIntValue();
		res.tileHeight = tWidth.getIntValue();
		res.horizOffset = hOffset.getIntValue();
		res.vertOffset = vOffset.getIntValue();
		res.horizSep = hSep.getIntValue();
		res.vertSep = vSep.getIntValue();
		resOriginal = (Background) res.copy(false,null);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == tileset || e.getSource() == tWidth || e.getSource() == tHeight
				|| e.getSource() == hOffset || e.getSource() == vOffset || e.getSource() == hSep
				|| e.getSource() == vSep)
			{
			side2.setVisible(tileset.isSelected());
			preview.repaint(((JViewport) preview.getParent()).getViewRect());
			return;
			}
		if (e.getSource() == load)
			{
			try
				{
				BufferedImage img = Util.getValidImage();
				if (img != null)
					{
					res.backgroundImage = img;
					res.width = img.getWidth();
					res.height = img.getHeight();
					width.setText(Messages.getString("BackgroundFrame.WIDTH") + res.width); //$NON-NLS-1$
					height.setText(Messages.getString("BackgroundFrame.HEIGHT") + res.height); //$NON-NLS-1$
					imageChanged = true;
					preview.setIcon(new ImageIcon(img));
					}
				}
			catch (Throwable t) // Includes out of memory errors
				{
				// t.printStackTrace();
				String msg = Messages.getString("BackgroundFrame.ERROR_LOADING"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(LGM.frame,msg + Util.imageFc.getSelectedFile().getPath());
				}
			return;
			}

		super.actionPerformed(e);
		}
	}
