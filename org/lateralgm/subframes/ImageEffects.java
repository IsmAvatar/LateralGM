/**
* @file  ImageEffects.java
* @brief Class implementing the generic pluggable image effects.
*
* @section License
*
* Copyright (C) 2014 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.subframes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.NumberField.ValueChangeEvent;
import org.lateralgm.components.NumberField.ValueChangeListener;
import org.lateralgm.main.Util;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

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
			JLabel alphaLabel = new JLabel("Transparency:");
			alphaSlider = new JSlider(0,255,155);
			alphaSlider.setPaintTicks(true);
			alphaSlider.setMajorTickSpacing(15);
			alphaSlider.setMinorTickSpacing(3);
			final NumberField alphaField = new NumberField(0,255,155);
			alphaSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						alphaField.setValue(alphaSlider.getValue());
						optionsUpdated();
					}
			});
			alphaField.addValueChangeListener(new ValueChangeListener() {

				@Override
				public void valueChange(ValueChangeEvent evt)
					{
						alphaSlider.setValue((int) evt.getNewValue());
					}
			
			});
			
			
			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			gl.setHorizontalGroup(gl.createParallelGroup()
			/**/.addGroup(gl.createSequentialGroup()
			/*	*/.addComponent(alphaLabel)
			/*	*/.addComponent(alphaSlider)
			/*	*/.addComponent(alphaField,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)));
			
			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(alphaLabel)
			/*	*/.addComponent(alphaSlider)
			/*	*/.addComponent(alphaField,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)));
			
			pane.setLayout(gl);
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
	
			BufferedImageOp op = new ConvolveOp(kernel,ConvolveOp.EDGE_ZERO_FILL,null);
	
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
		private JSlider repeatSlider;
		
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
 
			BufferedImageOp op = new ConvolveOp(kernel,ConvolveOp.EDGE_ZERO_FILL,null);
 
			dst = op.filter(img, dst);
			
			for (int i = 0; i < repeatSlider.getValue() - 1; i++) {
				dst = op.filter(dst,null);
			}
			return dst;
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			JLabel alphaLabel = new JLabel("Repetitions:");
			repeatSlider = new JSlider(1,20,2);
			repeatSlider.setPaintTicks(true);
			repeatSlider.setSnapToTicks(true);
			repeatSlider.setMajorTickSpacing(5);
			repeatSlider.setMinorTickSpacing(1);
			repeatSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});
			
			
			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			gl.setHorizontalGroup(gl.createParallelGroup()
			/**/.addGroup(gl.createSequentialGroup()
			/*	*/.addComponent(alphaLabel)
			/*	*/.addComponent(repeatSlider)));
			
			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(alphaLabel)
			/*	*/.addComponent(repeatSlider)));
			
			pane.setLayout(gl);
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
		private JSlider repeatSlider;
		
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
 
			BufferedImageOp op = new ConvolveOp(kernel,ConvolveOp.EDGE_ZERO_FILL,null);
			 
			dst = op.filter(img, dst);
			for (int i = 0; i < repeatSlider.getValue() - 1; i++) {
				dst = op.filter(dst,null);
			}
			return dst;
			}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			JLabel alphaLabel = new JLabel("Repetitions:");
			repeatSlider = new JSlider(1,20,2);
			repeatSlider.setPaintTicks(true);
			repeatSlider.setSnapToTicks(true);
			repeatSlider.setMajorTickSpacing(5);
			repeatSlider.setMinorTickSpacing(1);
			repeatSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});
			
			
			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			gl.setHorizontalGroup(gl.createParallelGroup()
			/**/.addGroup(gl.createSequentialGroup()
			/*	*/.addComponent(alphaLabel)
			/*	*/.addComponent(repeatSlider)));
			
			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(alphaLabel)
			/*	*/.addComponent(repeatSlider)));
			
			pane.setLayout(gl);
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
	
	public static class RemoveTransparencyEffect extends ImageEffect {
		private final String key = "RemoveTransparencyEffect";
		private ColorSelect colorSelect;
		
		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			BufferedImage dst = Util.clearBackground(img,colorSelect.getSelectedColor());
			return dst;
		}
	
		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			JLabel colorLabel = new JLabel("Color:");
			colorSelect = new ColorSelect(Color.WHITE,true);
			colorSelect.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie)
					{
						optionsUpdated();
					}
			});
			
			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			gl.setHorizontalGroup(gl.createParallelGroup()
			/**/.addGroup(gl.createSequentialGroup()
			/*	*/.addComponent(colorLabel)
			/*	*/.addComponent(colorSelect,120,120,120)));
			
			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(colorLabel)
			/*	*/.addComponent(colorSelect,18,18,18)));
			
			pane.setLayout(gl);
			return pane;
			}
	
		@Override
		public String getName()
			{
			return "Remove Transparency";
			}
		
		@Override
		public String getKey()
			{
			return key;
			}
	}
	
	}
