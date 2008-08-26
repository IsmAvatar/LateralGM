package org.lateralgm.components.visual;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.plaf.ToolTipUI;

public class ImageToolTip extends JToolTip
	{
	private static final long serialVersionUID = 1L;

	public ImageToolTip(ImageInformer ii)
		{
		setUI(new ImageToolTipUI(ii,getUI()));
		}

	public ImageToolTip(final BufferedImage img)
		{
		this(new ImageInformer()
			{
				public BufferedImage getImage()
					{
					return img;
					}
			});
		}

	public class ImageToolTipUI extends ToolTipUI
		{
		private ImageInformer ii;
		private ToolTipUI ttui;

		public ImageToolTipUI(ImageInformer ii, ToolTipUI ttui)
			{
			this.ii = ii;
			this.ttui = ttui;
			}

		public void paint(Graphics g, JComponent c)
			{
			ttui.paint(g,c);
			BufferedImage img = ii.getImage();
			if (img != null) g.drawImage(img,0,0,null);
			}

		public Dimension getPreferredSize(JComponent c)
			{
			BufferedImage img = ii.getImage();
			if (img == null) return new Dimension(16,16);
			return new Dimension(Math.max(img.getWidth(),16),Math.max(img.getHeight(),16));
			}
		}
	}
