package org.lateralgm.subframes;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.main.Util;

public class ImageEffects
	{
	
	public abstract interface EffectOptionListener {
		public abstract void optionsUpdated();
	}
	
	public static abstract class ImageEffect {
		private List<EffectOptionListener> listeners = new ArrayList<EffectOptionListener>();
	
		public abstract BufferedImage getAppliedImage(BufferedImage img);
		public abstract JPanel getOptionsPanel();
		public abstract String getName();
		public abstract String getKey();
		
		protected void optionsUpdated() {
			for (EffectOptionListener listener : listeners) {
				listener.optionsUpdated();
			}
		}
		
		public void addOptionUpdateListener(EffectOptionListener listener)
			{
			listeners.add(listener);
			}
		public void removeOptionUpdateListener(EffectOptionListener listener)
			{
			listeners.remove(listener);
			}
	}
	
	public static class BlackAndWhiteEffect extends ImageEffect {
		private final String key = "BlackAndWhiteEffect";
	
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			BufferedImage ret = new BufferedImage(img.getColorModel(), img.copyData(null), img.isAlphaPremultiplied(), null);
	    ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
	    op.filter(img, ret);
			return ret;
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Black and White";
			}
	
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	public static class OpacityEffect extends ImageEffect {
		private final String key = "OpacityEffect";
	
		private JSlider alphaSlider = null;
		
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
	    BufferedImage target = new BufferedImage(img.getWidth(),
	        img.getHeight(), java.awt.Transparency.TRANSLUCENT);
	    // Get the images graphics
	    Graphics2D g = target.createGraphics();
	    // Set the Graphics composite to Alpha
	    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
	        (float) alphaSlider.getValue()/255));
	    // Draw the image into the prepared reciver image
	    g.drawImage(img, null, 0, 0);
	    // let go of all system resources in this Graphics
	    g.dispose();
	    // Return the image
	    return target;
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			alphaSlider = new JSlider();
			alphaSlider.setMinimum(0);
			alphaSlider.setMaximum(255);
			alphaSlider.setPaintTicks(true);
			alphaSlider.setMajorTickSpacing(5);
			alphaSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0)
					{
						optionsUpdated();
					}
			});
			
			pane.add(alphaSlider);
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Opacity";
			}
		
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	public static class InvertEffect extends ImageEffect {
		private final String key = "InvertEffect";
		
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			short[] invertTable = new short[256];
			for (int i = 0; i < 256; i++) {
				invertTable[i] = (short) (255 - i);
			}
			BufferedImage dst = new BufferedImage(img.getColorModel(), img.copyData(null), img.isAlphaPremultiplied(), null);
			 
			BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invertTable), null);
			return invertOp.filter(img, dst);
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
	
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Invert Color";
			}
		
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	public static class EdgeDetectEffect extends ImageEffect {
		private final String key = "EdgeDetectEffect";
		
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			img = Util.convertImage(img,BufferedImage.TYPE_INT_ARGB);
			BufferedImage dst = new BufferedImage(img.getWidth(),img.getHeight(), img.getType());
			 
			Kernel kernel = new Kernel(3, 3,
	              new float[]{
	              		-1, -1, -1,
	                  -1, 8, -1,
	                  -1, -1, -1});
	
			BufferedImageOp op = new ConvolveOp(kernel,ConvolveOp.EDGE_NO_OP,null);
	
			return op.filter(img, dst);
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
	
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Edge Detection";
			}
		
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	public static class EmbossEffect extends ImageEffect {
		private final String key = "EmbossColorEffect";
		
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			int width = img.getWidth();
			int height = img.getHeight();
			BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			 
			for (int i = 0; i < height; i++)
      for (int j = 0; j < width; j++) {
	      int upperLeft = 0;
	      int lowerRight = 0;
	
	      if (i > 0 && j > 0)
	        upperLeft = img.getRGB(j - 1, i - 1);
	
	      if (i < height - 1 && j < width - 1)
	        lowerRight = img.getRGB(j + 1, i + 1);
	
	      int redDiff = ((lowerRight >> 16) & 255) - ((upperLeft >> 16) & 255);
	
	      int greenDiff = ((lowerRight >> 8) & 255) - ((upperLeft >> 8) & 255);
	
	      int blueDiff = (lowerRight & 255) - (upperLeft & 255);
	
	      int diff = redDiff;
	      if (Math.abs(greenDiff) > Math.abs(diff))
	        diff = greenDiff;
	      if (Math.abs(blueDiff) > Math.abs(diff))
	        diff = blueDiff;
	
	      int grayColor = 128 + diff;
	
	      if (grayColor > 255)
	        grayColor = 255;
	      else if (grayColor < 0)
	        grayColor = 0;
	
	      int newColor = (grayColor << 16) + (grayColor << 8) + grayColor;
	
	      dst.setRGB(j, i, newColor);
	    }
 
			return dst;
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
	
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Emboss";
			}
		
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	public static class BlurEffect extends ImageEffect {
		private final String key = "BlurEffect";
		
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			img = Util.convertImage(img,BufferedImage.TYPE_INT_ARGB);
			BufferedImage dst = new BufferedImage(img.getWidth(),img.getHeight(), img.getType());
			 
			Kernel kernel = new Kernel(3, 3,
                new float[]{
                		1f/9f, 1f/9f, 1f/9f,
                		1f/9f, 1f/9f, 1f/9f,
                		1f/9f, 1f/9f, 1f/9f});
 
			BufferedImageOp op = new ConvolveOp(kernel,ConvolveOp.EDGE_NO_OP,null);
 
			return op.filter(img, dst);
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
	
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Blur";
			}
		
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	public static class SharpenEffect extends ImageEffect {
		private final String key = "SharpenEffect";
		
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			img = Util.convertImage(img,BufferedImage.TYPE_INT_ARGB);
			BufferedImage dst = new BufferedImage(img.getWidth(),img.getHeight(), img.getType());
			 
			Kernel kernel = new Kernel(3, 3,
                new float[]{
                		-1, -1, -1,
                    -1, 9, -1,
                    -1, -1, -1});
 
			BufferedImageOp op = new ConvolveOp(kernel,ConvolveOp.EDGE_NO_OP,null);
 
			return op.filter(img, dst);
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
	
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Sharpen";
			}
		
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	}
