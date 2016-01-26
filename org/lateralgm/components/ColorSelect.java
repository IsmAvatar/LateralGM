/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2015 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.ItemSelectable;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.BevelBorder;
import javax.swing.text.MaskFormatter;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.util.PropertyEditor;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ColorSelect extends JPanel implements ItemSelectable,PropertyEditor<Color>
{

	private static final long serialVersionUID = 1L;
	private Color selectedColor;
	// If false, return always 255. Used for the instance's color.
	private boolean returnAlpha = true;

	private JPanel colorPanel;
	private JFormattedTextField textField;

	private static BufferedImage transparentImage;

	public ColorSelect(Color col)
	{
		if (transparentImage == null) {
			transparentImage = Util.paintBackground(2, 2);
		}
		colorPanel = new JPanel() {

		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = -948015130105405683L;

	@Override
			public void paint(Graphics g) {
				Rectangle clipBounds = g.getClipBounds();
				g.drawImage(transparentImage, 0, 0, clipBounds.width, clipBounds.height, null);
				super.paint(g);
			}
		};
		colorPanel.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED, Color.WHITE,
			Color.BLACK));
		colorPanel.setBackground(col);
		// needed by some look and feels such as Quaqua
		colorPanel.setOpaque(true);

		colorPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				colorPanel.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED, Color.WHITE,
					Color.BLACK));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				colorPanel.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED, Color.WHITE,
					Color.BLACK));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				Color newcol = JColorChooser.showDialog(getParent(),
					Messages.getString("ColorSelect.CHOOSE_TITLE"), selectedColor); //$NON-NLS-1$
				if (newcol != null) {
					setSelectedColor(newcol);
					fireItemChanged();
				}
			}
		});

		MaskFormatter formatter = new MaskFormatter();
		formatter.setPlaceholder("FFFFFFFF"); //$NON-NLS-1$
		formatter.setPlaceholderCharacter('F'); //$NON-NLS-1$

		try
		{
			formatter.setMask("HHHHHHHH"); //$NON-NLS-1$
		}
		catch (ParseException e)
		{
			// This should never occur because the format is correct
			// and this has been well tested.
			LGM.showDefaultExceptionHandler(e);
		}
		textField = new JFormattedTextField(formatter);
		textField.setColumns(8);

		textField.setText(Util.formatColortoHex(col));
		textField.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				selectedColor = new Color(Integer.rotateRight(
						(int) Long.parseLong(textField.getText(),16),textField.getText().length()), true);
				colorPanel.setBackground(selectedColor);
				fireItemChanged();
			}
		});

		GroupLayout gl = new GroupLayout(this);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(false);

		gl.setHorizontalGroup(gl.createSequentialGroup()
		/*	*/.addComponent(colorPanel, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		/*	*/.addComponent(textField, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		);

		gl.setVerticalGroup(gl.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(colorPanel, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		/*	*/.addComponent(textField)
		);

		this.setLayout(gl);
		selectedColor = col;
	}

	@Override
	public int getBaseline(int width, int height) {
		return textField.getBaseline(width, height);
	}

	public ColorSelect()
	{
		this(Color.BLACK);
	}

	public ColorSelect(Color col, boolean returnAlpha)
	{
		this(col);
		this.returnAlpha = returnAlpha;
	}

	protected void fireItemChanged()
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		ItemEvent e = new ItemEvent(this,ItemEvent.ITEM_STATE_CHANGED,selectedColor,ItemEvent.SELECTED);
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ItemListener.class) {
				((ItemListener) listeners[i + 1]).itemStateChanged(e);
			}
		}
	}

	public void setSelectedColor(Color selectedColor)
	{
		if (returnAlpha == false)
			selectedColor = new Color (selectedColor.getRed(),selectedColor.getGreen(),
				selectedColor.getBlue());

		colorPanel.setBackground(selectedColor);
		textField.setText(Util.formatColortoHex(selectedColor));

		this.selectedColor = selectedColor;
	}

	public Color getSelectedColor()
	{
		if (returnAlpha == true) {
			return selectedColor;
		} else {
			Color selectedColorWithoutAlpha = new Color (selectedColor.getRed(),selectedColor.getGreen(),
				selectedColor.getBlue());
			return selectedColorWithoutAlpha;
		}
	}

	@Override
	public void addItemListener(ItemListener l)
	{
		listenerList.add(ItemListener.class,l);
	}

	@Override
	public Object[] getSelectedObjects()
	{
		return new Color[] { selectedColor };
	}

	@Override
	public void removeItemListener(ItemListener l)
	{
		listenerList.remove(ItemListener.class,l);
	}

	public <K extends Enum<K>>PropertyLink<K,Color> getLink(PropertyMap<K> m, K k)
	{
		return new ColorSelectLink<K>(m,k);
	}

	private class ColorSelectLink<K extends Enum<K>> extends PropertyLink<K,Color> implements
		ItemListener
	{

		public ColorSelectLink(PropertyMap<K> m, K k)
		{
			super(m,k);
			reset();
			addItemListener(this);
		}

		@Override
		protected void setComponent(Color c)
		{
			setSelectedColor(c);
		}

		@Override
		public void remove()
		{
			super.remove();
			removeItemListener(this);
		}

		@Override
		public void updated(PropertyUpdateEvent<K> e)
		{
			editComponentIfChanged(selectedColor);
		}

		public void itemStateChanged(ItemEvent e)
		{
			if (selectedColor.equals(map.get(key))) return;
			editProperty(selectedColor);
		}
	}
}
