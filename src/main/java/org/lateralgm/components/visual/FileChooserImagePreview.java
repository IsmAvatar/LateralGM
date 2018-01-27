/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class FileChooserImagePreview extends JLabel implements PropertyChangeListener
	{
	private static final long serialVersionUID = 1L;

	private ImageIcon prev = null;

	private static final int WIDTH = 150;
	private static final int HEIGHT = 150;

	public FileChooserImagePreview(JFileChooser choose)
		{
		choose.addPropertyChangeListener(this);
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		setHorizontalAlignment(SwingConstants.CENTER);
		}

	public void propertyChange(PropertyChangeEvent e)
		{
		if (e.getPropertyName() == JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
			{
			if (isShowing())
				{
				File f = (File) e.getNewValue();
				if (f == null)
					prev = null;
				else
					{
					BufferedImage img = null;
					try
						{
						img = ImageIO.read(f); //can return null
						}
					catch (Throwable t)
						{
						//img = null
						}
					if (img != null)
						{
						if (img.getWidth() > WIDTH && img.getHeight() > HEIGHT)
							{
							prev = new ImageIcon(img.getScaledInstance(img.getWidth() >= img.getHeight() ? WIDTH
									: -1,img.getHeight() > img.getWidth() ? HEIGHT : -1,Image.SCALE_FAST));
							}
						else if (img.getWidth() > WIDTH || img.getHeight() > HEIGHT)
							{
							prev = new ImageIcon(img.getScaledInstance(img.getWidth() > WIDTH ? WIDTH : -1,
									img.getHeight() > HEIGHT ? HEIGHT : -1,Image.SCALE_FAST));
							}
						else
							prev = new ImageIcon(img);
						}
					else
						prev = null;
					}
				setIcon(prev);
				}
			}
		}
	}
