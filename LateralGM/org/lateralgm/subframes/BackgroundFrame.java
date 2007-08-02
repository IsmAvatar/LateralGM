/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM. Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static org.lateralgm.main.Util.addDim;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

import org.lateralgm.components.IntegerField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.BackgroundPreview;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;

public class BackgroundFrame extends ResourceFrame<Background>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon FRAME_ICON = Background.ICON[Background.BACKGROUND];
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("BackgroundFrame.LOAD");
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
		setFrameIcon(FRAME_ICON);

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setMinimumSize(new Dimension(180,280));
		side1.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));
		side1.setPreferredSize(new Dimension(180,280));

		// side1.setBackground(Color.RED);
		Util.addDim(side1,new JLabel(Messages.getString("BackgroundFrame.NAME")),40,14); //$NON-NLS-1$
		addDim(side1,name,120,20);

		load = new JButton(Messages.getString("SpriteFrame.LOAD")); //$NON-NLS-1$
		load.setIcon(LOAD_ICON);
		load.addActionListener(this);
		addDim(side1,load,130,24);
		width = new JLabel(Messages.getString("BackgroundFrame.WIDTH") + res.width); //$NON-NLS-1$
		addDim(side1,width,80,16);
		height = new JLabel(Messages.getString("BackgroundFrame.HEIGHT") + res.height); //$NON-NLS-1$
		addDim(side1,height,80,16);

		addGap(side1,160,10);

		edit = new JButton(Messages.getString("BackgroundFrame.EDIT")); //$NON-NLS-1$
		edit.addActionListener(this);
		addDim(side1,edit,130,24);

		addGap(side1,160,15);

		transparent = new JCheckBox(Messages.getString("BackgroundFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setSelected(res.transparent);
		transparent.addActionListener(this);
		addDim(side1,transparent,130,16);
		smooth = new JCheckBox(Messages.getString("BackgroundFrame.SMOOTH")); //$NON-NLS-1$
		smooth.setSelected(res.smoothEdges);
		addDim(side1,smooth,130,16);
		preload = new JCheckBox(Messages.getString("BackgroundFrame.PRELOAD")); //$NON-NLS-1$
		preload.setSelected(res.preload);
		addDim(side1,preload,130,16);
		tileset = new JCheckBox(Messages.getString("BackgroundFrame.USE_AS_TILESET")); //$NON-NLS-1$
		tileset.setSelected(res.useAsTileSet);
		tileset.addActionListener(this);
		addDim(side1,tileset,130,16);

		addGap(side1,160,15);

		save.setText(Messages.getString("BackgroundFrame.SAVE")); //$NON-NLS-1$
		addDim(side1,save,130,24);

		side2 = new JPanel(new FlowLayout());
		side2.setPreferredSize(new Dimension(180,260));
		side2.setMinimumSize(new Dimension(180,260));
		side2.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));
		JPanel group = new JPanel(new FlowLayout()); // BoxLayout does what it wants, so this has to be
		// separate to stay a constant size
		group.setPreferredSize(new Dimension(180,270));
		String tileProps = Messages.getString("BackgroundFrame.TILE_PROPERTIES"); //$NON-NLS-1$
		group.setBorder(BorderFactory.createTitledBorder(tileProps));

		JLabel lab = new JLabel(Messages.getString("BackgroundFrame.TILE_WIDTH")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		tWidth = new IntegerField(0,Integer.MAX_VALUE,res.tileWidth);
		tWidth.addActionListener(this);
		addDim(group,tWidth,50,20);

		lab = new JLabel(Messages.getString("BackgroundFrame.TILE_HEIGHT")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		tHeight = new IntegerField(0,Integer.MAX_VALUE,res.tileHeight);
		tHeight.addActionListener(this);
		addDim(group,tHeight,50,20);

		addGap(group,150,15);

		lab = new JLabel(Messages.getString("BackgroundFrame.H_OFFSET")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		hOffset = new IntegerField(0,Integer.MAX_VALUE,res.horizOffset);
		hOffset.addActionListener(this);
		addDim(group,hOffset,50,20);

		lab = new JLabel(Messages.getString("BackgroundFrame.V_OFFSET")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		vOffset = new IntegerField(0,Integer.MAX_VALUE,res.vertOffset);
		vOffset.addActionListener(this);
		addDim(group,vOffset,50,20);

		addGap(group,150,15);

		lab = new JLabel(Messages.getString("BackgroundFrame.H_SEP")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		hSep = new IntegerField(0,Integer.MAX_VALUE,res.horizSep);
		hSep.addActionListener(this);
		addDim(group,hSep,50,20);

		lab = new JLabel(Messages.getString("BackgroundFrame.V_SEP")); //$NON-NLS-1$
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		addDim(group,lab,100,16);
		vSep = new IntegerField(0,Integer.MAX_VALUE,res.vertSep);
		vSep.addActionListener(this);
		addDim(group,vSep,50,20);

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
		return !resOriginal.getName().equals(name.getText())
				|| resOriginal.transparent != transparent.isSelected()
				|| resOriginal.smoothEdges != smooth.isSelected()
				|| resOriginal.preload != preload.isSelected()
				|| resOriginal.useAsTileSet != tileset.isSelected()
				|| resOriginal.tileWidth != tWidth.getIntValue()
				|| resOriginal.tileHeight != tWidth.getIntValue()
				|| resOriginal.horizOffset != hOffset.getIntValue()
				|| resOriginal.vertOffset != vOffset.getIntValue()
				|| resOriginal.horizSep != hSep.getIntValue() || resOriginal.vertSep != vSep.getIntValue();
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
		resOriginal = res.copy();
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
				LGM.tree.repaint();
				}
			return;
			}
		if (e.getSource() == transparent)
			{
			res.transparent = transparent.isSelected();
			LGM.tree.repaint();
			return;
			}

		super.actionPerformed(e);
		}
	}
