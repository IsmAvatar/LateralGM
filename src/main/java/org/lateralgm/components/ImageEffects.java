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

package org.lateralgm.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.Kernel;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.components.NumberField.ValueChangeEvent;
import org.lateralgm.components.NumberField.ValueChangeListener;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;

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
		public String getName() {
			return Messages.getString("ImageEffects." + getKey()); //$NON-NLS-1$
		}
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
		private final String key = "BlackAndWhiteEffect"; //$NON-NLS-1$

		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			BufferedImage dest = op.createCompatibleDestImage(img, img.getColorModel());
			return op.filter(img, dest);
			}

		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();

			return pane;
			}

		@Override
		public String getKey()
			{
			return key;
			}
	}

	public static class OpacityEffect extends ImageEffect {
		private final String key = "OpacityEffect"; //$NON-NLS-1$

		private JSlider alphaSlider = null;

		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			BufferedImage target = new BufferedImage(img.getWidth(),
					img.getHeight(), BufferedImage.TYPE_INT_ARGB);
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
			JLabel alphaLabel = new JLabel(Messages.getString("ImageEffects.TRANSPARENCY")); //$NON-NLS-1$
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
		public String getKey()
			{
			return key;
			}
	}

	public static class InvertEffect extends ImageEffect {
		private final String key = "InvertEffect"; //$NON-NLS-1$

		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			int width = img.getWidth();
			int height = img.getHeight();
			BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < width; i++) {
				for (int ii = 0; ii < height; ii++) {
					int rgba = img.getRGB(i,ii);
					Color col = new Color(rgba, true);
					col = new Color(255 - col.getRed(),
													255 - col.getGreen(),
													255 - col.getBlue(),
													col.getAlpha());
					dst.setRGB(i,ii,col.getRGB());
				}
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
		public String getKey()
			{
			return key;
			}
	}

	public static class EdgeDetectEffect extends ImageEffect {
		private final String key = "EdgeDetectEffect"; //$NON-NLS-1$

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
		public String getKey()
			{
			return key;
			}
	}

	public static class EmbossEffect extends ImageEffect {
		private final String key = "EmbossColorEffect"; //$NON-NLS-1$

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
		public String getKey()
			{
			return key;
			}
	}

	public static class BlurEffect extends ImageEffect {
		private final String key = "BlurEffect"; //$NON-NLS-1$
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
			JLabel alphaLabel = new JLabel(Messages.getString("ImageEffects.REPETITIONS")); //$NON-NLS-1$
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
		public String getKey()
			{
			return key;
			}
	}

	public static class SharpenEffect extends ImageEffect {
		private final String key = "SharpenEffect"; //$NON-NLS-1$
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
			JLabel alphaLabel = new JLabel(Messages.getString("ImageEffects.REPETITIONS")); //$NON-NLS-1$
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
		public String getKey()
			{
			return key;
			}
	}

	public static class RemoveTransparencyEffect extends ImageEffect {
		private final String key = "RemoveTransparencyEffect"; //$NON-NLS-1$
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
			JLabel colorLabel = new JLabel(Messages.getString("ImageEffects.COLOR")); //$NON-NLS-1$
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
			/*	*/.addComponent(colorSelect)));

			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
			/*	*/.addComponent(colorLabel)
			/*	*/.addComponent(colorSelect)));

			pane.setLayout(gl);
			return pane;
			}

		@Override
		public String getKey()
			{
			return key;
			}
	}

	public static double ColourDistance(Color c1, Color c2)
		{
		double rmean = ( c1.getRed() + c2.getRed() )/2;
		int r = c1.getRed() - c2.getRed();
		int g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		double weightR = 2 + rmean/256;
		double weightG = 4.0;
		double weightB = 2 + (255-rmean)/256;
		return Math.sqrt(weightR*r*r + weightG*g*g + weightB*b*b);
		}

	public static class RemoveColorEffect extends ImageEffect {
		private final String key = "RemoveColorEffect"; //$NON-NLS-1$
		private ColorSelect colorSelect;
		private JSlider toleranceSlider;

		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			final int col = colorSelect.getSelectedColor().getRGB();
			ImageFilter filter = new RGBImageFilter()
				{
					@Override
					public int filterRGB(int x, int y, int rgb)
						{

						if (ColourDistance(new Color(rgb), new Color(col)) < toleranceSlider.getValue())
							return col & 0x00FFFFFF;
						return rgb;
						}
				};
			ImageProducer ip = new FilteredImageSource(img.getSource(),filter);
			return Util.toBufferedImage(Toolkit.getDefaultToolkit().createImage(ip));
		}

		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			JLabel colorLabel = new JLabel(Messages.getString("ImageEffects.COLOR")); //$NON-NLS-1$
			colorSelect = new ColorSelect(Color.WHITE,true);
			colorSelect.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie)
					{
						optionsUpdated();
					}
			});

			JLabel toleranceLabel = new JLabel(Messages.getString("ImageEffects.TOLERANCE")); //$NON-NLS-1$
			toleranceSlider = new JSlider(0,255,15);
			toleranceSlider.setPaintTicks(true);
			toleranceSlider.setSnapToTicks(true);
			toleranceSlider.setMajorTickSpacing(15);
			toleranceSlider.setMinorTickSpacing(3);
			toleranceSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});

			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);

			gl.setHorizontalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
			/*	*/.addComponent(colorLabel)
			/*	*/.addComponent(toleranceLabel))
			/**/.addGroup(gl.createParallelGroup()
			/*	*/.addComponent(colorSelect)
			/*	*/.addComponent(toleranceSlider)));

			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
			/*	*/.addComponent(colorLabel)
			/*	*/.addComponent(colorSelect))
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(toleranceLabel)
			/*	*/.addComponent(toleranceSlider)));

			pane.setLayout(gl);
			return pane;
			}

		@Override
		public String getKey()
			{
			return key;
			}
	}

	public static class FadeColorEffect extends ImageEffect {
		private final String key = "FadeColorEffect"; //$NON-NLS-1$
		private ColorSelect colorSelect;
		private JSlider intensitySlider;

		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			BufferedImage target = new BufferedImage(img.getWidth(),
					img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			// Get the images graphics
			Graphics2D g = target.createGraphics();
			// Draw the image into the prepared reciver image
			g.drawImage(img, null, 0, 0);
			// Set the Graphics composite to Alpha
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					(float) intensitySlider.getValue()/255));
			g.setColor(colorSelect.getSelectedColor());
			g.fillRect(0,0,img.getWidth(),img.getHeight());
			// let go of all system resources in this Graphics
			g.dispose();
			// Return the image
			return target;
			}

		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			JLabel colorLabel = new JLabel(Messages.getString("ImageEffects.COLOR")); //$NON-NLS-1$
			colorSelect = new ColorSelect(Color.YELLOW,false);
			colorSelect.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie)
					{
						optionsUpdated();
					}
			});

			JLabel toleranceLabel = new JLabel(Messages.getString("ImageEffects.INTENSITY")); //$NON-NLS-1$
			intensitySlider = new JSlider(0,255,155);
			intensitySlider.setPaintTicks(true);
			intensitySlider.setSnapToTicks(true);
			intensitySlider.setMajorTickSpacing(15);
			intensitySlider.setMinorTickSpacing(3);
			intensitySlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});

			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);

			gl.setHorizontalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
			/*	*/.addComponent(colorLabel)
			/*	*/.addComponent(toleranceLabel))
			/**/.addGroup(gl.createParallelGroup()
			/*	*/.addComponent(colorSelect)
			/*	*/.addComponent(intensitySlider)));

			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.BASELINE)
			/*	*/.addComponent(colorLabel)
			/*	*/.addComponent(colorSelect))
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(toleranceLabel)
			/*	*/.addComponent(intensitySlider)));

			pane.setLayout(gl);
			return pane;
			}

		@Override
		public String getKey()
			{
			return key;
			}
	}

	public static float clamp(float val, float min, float max) {
		return Math.max(min, Math.min(max, val));
	}

	public static float wrap(float val, float min, float max) {
		float dif = max - min;
		while (val < min) val += dif;
		while (val > max) val -= dif;
		return val;
	}

	public static class ColorizeEffect extends ImageEffect {
		private final String key = "ColorizeEffect"; //$NON-NLS-1$
		private JSlider hueSlider;
		private JCheckBox hueShift;
		private JSlider satSlider;
		private JCheckBox satShift;
		private JSlider valSlider;
		private JCheckBox valShift;

		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
			int width = img.getWidth();
			int height = img.getHeight();
			BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < width; i++) {
				for (int ii = 0; ii < height; ii++) {
					int rgba = img.getRGB(i,ii);
					Color col = new Color(rgba, true);

					float[] hslVals = new float[3];
					int alpha = col.getAlpha();
					Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), hslVals);

					// Pass .5 (= 180 degrees) as HUE
					col = new Color(Color.HSBtoRGB(
							wrap((hueShift.isSelected() ? hslVals[0] : 0) + hueSlider.getValue() / 360.0f,0,1),
							satShift.isSelected() ? clamp(hslVals[1] + satSlider.getValue() / 100.0f, 0.0f, 1.0f)
									: (100.0f + satSlider.getValue()) / 200.0f,
							valShift.isSelected() ? clamp(hslVals[2] + valSlider.getValue() / 100.0f, 0.0f, 1.0f)
									: (100.0f + valSlider.getValue()) / 200.0f));
					col = new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);
					dst.setRGB(i,ii,col.getRGB());
				}
			}
			return dst;
			}

		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			JLabel hueLabel = new JLabel(Messages.getString("ImageEffects.HUE")); //$NON-NLS-1$
			hueSlider = new JSlider(-180,180,0);
			hueSlider.setPaintTicks(true);
			hueSlider.setSnapToTicks(false);
			hueSlider.setMajorTickSpacing(45);
			hueSlider.setMinorTickSpacing(5);
			hueSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});

			hueShift = new JCheckBox(Messages.getString("ImageEffects.RELATIVE"), true); //$NON-NLS-1$
			hueShift.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0)
					{
						optionsUpdated();
					}
			});

			JLabel satLabel = new JLabel(Messages.getString("ImageEffects.SATURATION")); //$NON-NLS-1$
			satSlider = new JSlider(-100,100,0);
			satSlider.setPaintTicks(true);
			satSlider.setSnapToTicks(false);
			satSlider.setMajorTickSpacing(20);
			satSlider.setMinorTickSpacing(5);
			satSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});

			satShift = new JCheckBox(Messages.getString("ImageEffects.RELATIVE"), true); //$NON-NLS-1$
			satShift.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0)
					{
						optionsUpdated();
					}
			});

			JLabel valLabel = new JLabel(Messages.getString("ImageEffects.VALUE")); //$NON-NLS-1$
			valSlider = new JSlider(-100,100,0);
			valSlider.setPaintTicks(true);
			valSlider.setSnapToTicks(false);
			valSlider.setMajorTickSpacing(20);
			valSlider.setMinorTickSpacing(5);
			valSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});

			valShift = new JCheckBox(Messages.getString("ImageEffects.RELATIVE"), true); //$NON-NLS-1$
			valShift.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0)
					{
						optionsUpdated();
					}
			});


			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);

			gl.setHorizontalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
			/*	*/.addComponent(hueLabel)
			/*	*/.addComponent(satLabel)
			/*	*/.addComponent(valLabel))
			/**/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
			/*	*/.addGroup(gl.createSequentialGroup()
			/*		*/.addComponent(hueSlider)
			/*		*/.addComponent(hueShift))
			/*	*/.addGroup(gl.createSequentialGroup()
			/*		*/.addComponent(satSlider)
			/*		*/.addComponent(satShift))
			/*	*/.addGroup(gl.createSequentialGroup()
			/*		*/.addComponent(valSlider)
			/*		*/.addComponent(valShift))));

			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(hueLabel)
			/*	*/.addComponent(hueSlider)
			/*	*/.addComponent(hueShift))
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(satLabel)
			/*	*/.addComponent(satSlider)
			/*	*/.addComponent(satShift))
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(valLabel)
			/*	*/.addComponent(valSlider)
			/*	*/.addComponent(valShift)));

			pane.setLayout(gl);
			return pane;
			}

		@Override
		public String getKey()
			{
			return key;
			}
	}

	public static class IntensityEffect extends ImageEffect {
		private final String key = "IntensityEffect"; //$NON-NLS-1$
		private JSlider brightnessSlider;
		private JSlider contrastSlider;

			//from David Fichtmüller
			public BufferedImage applyBrightnessAndContrast(BufferedImage bi, double brightness,
					double contrast) {
				final double gamma = 0.25;

				if (contrast > 0) {
					contrast = (100 * Math.pow(contrast - 1, 1 / gamma) / Math.pow(100, 1 / gamma)) + 1;
				} else if (contrast == 0) {
					contrast = 1;
				} else {
					contrast = 1 / ((100 * Math.pow(-contrast + 1,
							1 / gamma) / Math.pow(100, 1 / gamma)) + 1);
				}

				if (brightness > 0) {
					brightness = (100 * Math.pow(brightness, 1 / gamma) / Math.pow(100, 1 / gamma)) + 1;
				} else if (brightness == 0) {
					brightness = 1;
				} else {
					brightness = 1 / ((100 * Math.pow(-brightness,
							1 / gamma) / Math.pow(100, 1 / gamma)) + 1);
				}

				final int w = bi.getWidth();
				final int h = bi.getHeight();
				final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

				for (int x = 0; x < w; x++) {
						for (int y = 0; y < h; y++) {
							int rgb = bi.getRGB(x, y);

							// get the rgb-values
							int alpha = (int) ((rgb & 0xff000000l) >> 24);
							int r = ((rgb & 0x00ff0000) >> 16);
							int g = ((rgb & 0x0000ff00) >> 8);
							int b = ((rgb & 0x000000ff));

							// apply brightness filter
							r = (int) (r * brightness);
							g = (int) (g * brightness);
							b = (int) (b * brightness);

							// convert to YCbCr
							double Y = r * 0.299 + g * 0.587 + b * 0.114;
							double Cb = r * -0.168736 + g * -0.331264 + b * 0.5;
							double Cr = r * 0.5 + g * -0.418688 + b * -0.081312;

							// apply contrast filter
							Y = (Y + brightness - 127) * contrast + 127;
							Cb = Cb * contrast;
							Cr = Cr * contrast;

							// convert back to RGB
							r = (int) (Y + (Cr * 1.402));
							g = (int) (Y + (Cb * -0.344136) + (Cr * -0.714136));
							b = (int) (Y + (Cb * 1.772));

							// check sizes of return values
							if (alpha > 255) {
								alpha = 255;
							} else if (alpha < 0) {
								alpha = 0;
							}
							if (g > 255) {
								g = 255;
							} else if (g < 0) {
								g = 0;
							}
							if (r > 255) {
								r = 255;
							} else if (r < 0) {
								r = 0;
							}
							if (b > 255) {
								b = 255;
							} else if (b < 0) {
								b = 0;
							}

							rgb = ((alpha & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
							out.setRGB(x, y, rgb);
						}
				}
				return out;
		}


		@Override
		public BufferedImage getAppliedImage(BufferedImage img)
			{
				return this.applyBrightnessAndContrast(img,brightnessSlider.getValue(),
						contrastSlider.getValue());
			}

		@Override
		public JPanel getOptionsPanel()
			{
			JPanel pane = new JPanel();
			JLabel hueLabel = new JLabel(Messages.getString("ImageEffects.BRIGHTNESS")); //$NON-NLS-1$
			brightnessSlider = new JSlider(-100,100,0);
			brightnessSlider.setPaintTicks(true);
			brightnessSlider.setSnapToTicks(false);
			brightnessSlider.setMajorTickSpacing(20);
			brightnessSlider.setMinorTickSpacing(5);
			brightnessSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});

			JLabel satLabel = new JLabel(Messages.getString("ImageEffects.CONTRAST")); //$NON-NLS-1$
			contrastSlider = new JSlider(-100,100,0);
			contrastSlider.setPaintTicks(true);
			contrastSlider.setSnapToTicks(false);
			contrastSlider.setMajorTickSpacing(20);
			contrastSlider.setMinorTickSpacing(5);
			contrastSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce)
					{
						optionsUpdated();
					}
			});


			GroupLayout gl = new GroupLayout(pane);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);

			gl.setHorizontalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
			/*	*/.addComponent(hueLabel)
			/*	*/.addComponent(satLabel))
			/**/.addGroup(gl.createParallelGroup(Alignment.TRAILING)
			/*	*/.addComponent(brightnessSlider)
			/*	*/.addComponent(contrastSlider)));

			gl.setVerticalGroup(gl.createSequentialGroup()
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(hueLabel)
			/*	*/.addComponent(brightnessSlider))
			/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
			/*	*/.addComponent(satLabel)
			/*	*/.addComponent(contrastSlider)));

			pane.setLayout(gl);
			return pane;
			}

		@Override
		public String getKey()
			{
			return key;
			}
	}

	}
