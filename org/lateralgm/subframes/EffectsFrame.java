/**
* @file  EffectsFrame.java
* @brief Class implementing an effect chooser for sprites and backgrounds.
*
* @section License
*
* Copyright (C) 2013-2014 Robert B. Colton
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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.subframes.ImageEffects.EffectOptionListener;
import org.lateralgm.subframes.ImageEffects.ImageEffect;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

public class EffectsFrame extends JFrame implements ActionListener, EffectOptionListener
	{

	/**
	 * TODO: Change if needed.
	 */
	private static final long serialVersionUID = 4668913557919192011L;
	private static EffectsFrame INSTANCE = null;
	private static ImageEffect[] effects = null;
	private JComboBox<ImageEffect> effectsCombo = null;
	private List<BufferedImage> images = null;
	private AbstractButton applyButton;
	private AbstractButton closeButton;
	
	private List<EffectsFrameListener> listeners = new ArrayList<EffectsFrameListener>();
	private ImageEffectPreview beforePreview;
	private ImageEffectPreview afterPreview;
	
	public abstract interface EffectsFrameListener {
		public abstract void applyEffects(List<BufferedImage> imgs);
	}
	
	public void addEffectsListener(EffectsFrameListener listener) {
		listeners.add(listener);
	}
	
	public void removeEffectsListener(EffectsFrameListener listener) {
		listeners.remove(listener);
	}

	public static EffectsFrame getInstance(List<BufferedImage> imgs) {
		if (INSTANCE == null) INSTANCE = new EffectsFrame();
		INSTANCE.setImages(imgs);
		return INSTANCE;
	}
	
	private class ImageEffectPreview extends JPanel {
	
		/**
		 * TODO: Change if needed.
		 */
		private static final long serialVersionUID = 1L;
		BufferedImage image = null;
	
		public ImageEffectPreview() {
		
			JPanel jp = new JPanel() {

				/**
				 * TODO: Change if needed.
				 */
				private static final long serialVersionUID = 1L;
				
				@Override
				public void paint(Graphics g) {
					if (image == null) return;
					Rectangle bounds = this.getBounds();
					int width = image.getWidth();
					int height = image.getHeight();
					if (image.getWidth() > bounds.width) {
						float factor = (float) (bounds.getWidth() / image.getWidth());
						width = bounds.width;
						height = (int) (factor * image.getHeight());
					}
					if (height > bounds.height) {
						float factor = (float) (bounds.getHeight() / height);
						width = (int) (factor * width);
						height = bounds.height;
					}
					g.drawImage(image,(bounds.width - width) / 2,(bounds.height - height) / 2,width,height,null);
				}
			
			};
			
			GroupLayout bl = new GroupLayout(this);
			bl.setHorizontalGroup(bl.createParallelGroup().addComponent(jp));
			bl.setVerticalGroup(bl.createParallelGroup().addComponent(jp));
			this.setLayout(bl);
		}

		public void setImage(BufferedImage img)
			{
			image = img;
			if (image != null) repaint();
			}
	
	}
	
	public EffectsFrame()
		{
		setAlwaysOnTop(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(700,500);
		setLocationRelativeTo(LGM.frame);
		setTitle(Messages.getString("EffectsFrame.TITLE"));
		setIconImage(LGM.getIconForKey("EffectsFrame.ICON").getImage());
		setResizable(true);
		
		beforePreview = new ImageEffectPreview();
		beforePreview.setBorder(BorderFactory.createTitledBorder(Messages.getString("EffectsFrame.BEFORE")));

		afterPreview = new ImageEffectPreview();
		afterPreview.setBorder(BorderFactory.createTitledBorder(Messages.getString("EffectsFrame.AFTER")));
		
		effects = new ImageEffect[8];
		effects[0] = new ImageEffects.BlackAndWhiteEffect();
		effects[1] = new ImageEffects.OpacityEffect();
		effects[2] = new ImageEffects.InvertEffect();
		effects[3] = new ImageEffects.EdgeDetectEffect();
		effects[4] = new ImageEffects.EmbossEffect();
		effects[5] = new ImageEffects.BlurEffect();
		effects[6] = new ImageEffects.SharpenEffect();
		effects[7] = new ImageEffects.RemoveTransparencyEffect();
		
		final JPanel effectsOptions = new JPanel(new CardLayout());
		for (ImageEffect effect : effects) {
			effect.addOptionUpdateListener(this);
			effectsOptions.add(effect.getOptionsPanel(),effect.getKey());
		}

		effectsCombo = new JComboBox<ImageEffect>(effects);
		effectsCombo.setRenderer(new ListCellRenderer<ImageEffect>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends ImageEffect> list,
					ImageEffect value, int index, boolean isSelected, boolean cellHasFocus)
				{
					return new JLabel(value.getName());
				}
		
		});
		effectsCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent ie)
				{
					if (ie.getStateChange() != ItemEvent.SELECTED) return;
					ImageEffect effect = (ImageEffect) ie.getItem();
					if (effect == null) return;
					
					CardLayout cl = (CardLayout)(effectsOptions.getLayout());
			    cl.show(effectsOptions, effect.getKey());

					if (images == null || images.size() <= 0) return;
					BufferedImage img = images.get(0);
					if (img == null) return;
					afterPreview.setImage(effect.getAppliedImage(img));
				}
		
		});
		
		applyButton = new JButton(Messages.getString("EffectsFrame.APPLY"));
		applyButton.addActionListener(this);
		closeButton = new JButton(Messages.getString("EffectsFrame.CLOSE"));
		closeButton.addActionListener(this);
		
		GroupLayout gl = new GroupLayout(this.getContentPane());
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(beforePreview)
		/*	*/.addComponent(afterPreview))
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(effectsCombo, PREFERRED_SIZE, PREFERRED_SIZE, effectsCombo.getPreferredSize().width * 2)
		/*	*/.addComponent(applyButton)
		/*	*/.addComponent(closeButton))
		/**/.addComponent(effectsOptions));
		
		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(beforePreview)
		/*	*/.addComponent(afterPreview))
		/**/.addGroup(gl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(effectsCombo, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		/*	*/.addComponent(applyButton)
		/*	*/.addComponent(closeButton))
		/**/.addComponent(effectsOptions));
		
		this.setLayout(gl);
		}

	
	public void setImages(List<BufferedImage> imgs) {
		images = imgs;
		
		ImageEffect effect = (ImageEffect) effectsCombo.getSelectedItem();
		if (effect == null) return;
		if (images == null || images.size() <= 0) return;
		BufferedImage img = images.get(0);
		if (img == null) return;
		beforePreview.setImage(img);
		afterPreview.setImage(effect.getAppliedImage(img));
	}

	@Override
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == applyButton) {
			for (EffectsFrameListener listener : listeners) {
				listener.applyEffects(images);
			}
		} else if (e.getSource() == closeButton) {
			this.setVisible(false);
			images.clear();
			listeners.clear();
		}
		}

	@Override
	public void optionsUpdated()
		{
		ImageEffect effect = (ImageEffect) effectsCombo.getSelectedItem();
		if (effect == null) return;
		if (images == null || images.size() <= 0) return;
		BufferedImage img = images.get(0);
		if (img == null) return;
		beforePreview.setImage(img);
		afterPreview.setImage(effect.getAppliedImage(img));
		}

	}
