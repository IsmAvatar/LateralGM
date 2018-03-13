/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.lateralgm.components.NumberField;
import org.lateralgm.components.NumberField.ValueChangeEvent;
import org.lateralgm.components.NumberField.ValueChangeListener;
import org.lateralgm.components.visual.SpriteStripPreview;
import org.lateralgm.messages.Messages;

public class SpriteStripDialog extends JDialog implements Iterable<Rectangle>,ActionListener
	{
	private static final long serialVersionUID = 1L;

	public BufferedImage img;
	public SpriteStripPreview preview;
	/** Whether this dialog was confirmed yet (via press to OK button). */
	public boolean confirmed = false;

	private NumberField fields[];
	private static final int IMAGE_NUMBER = 0, IMAGES_PER_ROW = 1, CELL_WIDTH = 2, CELL_HEIGHT = 3,
			HOR_CELL_OFFSET = 4, VERT_CELL_OFFSET = 5, HOR_PIXEL_OFFSET = 6, VERT_PIXEL_OFFSET = 7,
			HOR_SEP = 8, VERT_SEP = 9;

	public SpriteStripDialog(Frame owner, BufferedImage src)
		{
		super(owner,Messages.getString("SpriteStripDialog.TITLE"),true);

		img = src;

		String labels[] = { "IMAGE_NUMBER","IMAGES_PER_ROW","CELL_WIDTH","CELL_HEIGHT",
				"HOR_CELL_OFFSET","VERT_CELL_OFFSET","HOR_PIXEL_OFFSET","VERT_PIXEL_OFFSET","HOR_SEP",
				"VERT_SEP" };

		JLabel l[] = new JLabel[labels.length];
		fields = new NumberField[labels.length];

		JPanel p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		layout.setAutoCreateContainerGaps(true);
		p.setLayout(layout);

		ParallelGroup g1 = layout.createParallelGroup();
		ParallelGroup g2 = layout.createParallelGroup();
		SequentialGroup g3 = layout.createSequentialGroup();

		preview = new SpriteStripPreview(this);

		fields[IMAGE_NUMBER] = new NumberField(1,99999,1);
		fields[IMAGES_PER_ROW] = new NumberField(1,99999,1);
		fields[CELL_WIDTH] = new NumberField(1,99999,32);
		fields[CELL_HEIGHT] = new NumberField(1,99999,32);

		//link ImgNum together with ImgPerRow if they are equal, thus expanding horizontally.
		fields[IMAGE_NUMBER].addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent evt)
					{
					if (fields[IMAGES_PER_ROW].getValue().equals(evt.getOldValue()))
						fields[IMAGES_PER_ROW].setValue(evt.getNewValue());
					}
			});

		for (int i = 0; i < labels.length; i++)
			{
			l[i] = new JLabel(Messages.getString("SpriteStripDialog." + labels[i]));
			g1.addComponent(l[i]);
			if (i > 3) fields[i] = new NumberField(0);
			fields[i].addValueChangeListener(preview);
			g2.addComponent(fields[i],50,50,50);

			if ((i > 1 && i % 2 == 0) || i == 1)
				g3.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED);
			else
				g3.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
			g3.addGroup(layout.createParallelGroup(Alignment.BASELINE)
			/**/.addComponent(l[i])
			/**/.addComponent(fields[i]));
			}

		String str = "SpriteStripDialog.IMPORT";
		JButton ok = new JButton(Messages.getString(str));
		ok.setActionCommand(str);
		ok.addActionListener(this);
		str = "SpriteStripDialog.CANCEL";
		JButton cancel = new JButton(Messages.getString(str));
		cancel.setActionCommand(str);
		cancel.addActionListener(this);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addGroup(g1)
		/*	*/.addPreferredGap(ComponentPlacement.RELATED)
		/*	*/.addGroup(g2))
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(ok)
		/*	*/.addPreferredGap(ComponentPlacement.RELATED)
		/*	*/.addComponent(cancel)));

		g3.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED);
		g3.addGroup(layout.createParallelGroup()
		/**/.addComponent(ok)
		/**/.addComponent(cancel));

		layout.setVerticalGroup(g3);

		add(p,BorderLayout.WEST);
		JScrollPane scroll = new JScrollPane(preview);
		scroll.setPreferredSize(new Dimension(300,300));
		add(scroll,BorderLayout.CENTER);

		pack();
		}

	/**
	 * Sets the origin, or x/y position of top-left cell, which in turn updates the preview.
	 * This is really only used by the preview, when the mouse is clicked.
	 */
	public void setOrigin(int x, int y)
		{
		fields[HOR_CELL_OFFSET].setValue(0);
		fields[VERT_CELL_OFFSET].setValue(0);
		fields[HOR_PIXEL_OFFSET].setValue(x);
		fields[VERT_PIXEL_OFFSET].setValue(y);
		}

	public BufferedImage[] getStrip()
		{
		if (!confirmed) return null;

		BufferedImage[] ret = new BufferedImage[fields[IMAGE_NUMBER].getIntValue()];

		int i = 0;
		for (Rectangle r : this)
			ret[i++] = img.getSubimage(r.x,r.y,r.width,r.height);

		return ret;
		}

	public Iterator<Rectangle> iterator()
		{
		ArrayList<Rectangle> list = new ArrayList<Rectangle>();
		int cw = fields[CELL_WIDTH].getIntValue();
		int ch = fields[CELL_HEIGHT].getIntValue();
		int x = fields[HOR_CELL_OFFSET].getIntValue() * cw;
		int y = fields[VERT_CELL_OFFSET].getIntValue() * ch;
		x += fields[HOR_PIXEL_OFFSET].getIntValue();
		y += fields[VERT_PIXEL_OFFSET].getIntValue();

		int xx = x, yy = y;
		for (int i = 0; i < fields[IMAGE_NUMBER].getIntValue(); i++)
			{
			if (i != 0 && i % fields[IMAGES_PER_ROW].getIntValue() == 0)
				{
				xx = x;
				yy += ch + fields[VERT_SEP].getIntValue();
				}

			list.add(new Rectangle(xx,yy,cw,ch));

			xx += cw + fields[HOR_SEP].getIntValue();
			}

		return list.listIterator();
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getActionCommand().equals("SpriteStripDialog.IMPORT")) confirmed = true;
		setVisible(false);
		}
	}
