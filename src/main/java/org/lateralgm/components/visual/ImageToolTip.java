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

	public ImageToolTip(AbstractImagePreview ii)
		{
		setUI(new ImageToolTipUI(ii));
		}

	public ImageToolTip(final BufferedImage img)
		{
		this(new AbstractImagePreview()
			{
				private static final long serialVersionUID = 1L;

				public BufferedImage getImage()
					{
					return img;
					}
			});
		}

	public class ImageToolTipUI extends ToolTipUI
		{
		private AbstractImagePreview aip;

		public ImageToolTipUI(AbstractImagePreview aip)
			{
			this.aip = aip;
			}

		@Override
		public void paint(Graphics g, JComponent c)
			{
			super.paint(g,c);
			BufferedImage img = aip.getImage();
			if (img != null) g.drawImage(img,0,0,null);
			}

		@Override
		public Dimension getPreferredSize(JComponent c)
			{
			BufferedImage img = aip.getImage();
			if (img == null) return new Dimension(16,16);
			return new Dimension(Math.max(img.getWidth(),16),Math.max(img.getHeight(),16));
			}
		}
	}
