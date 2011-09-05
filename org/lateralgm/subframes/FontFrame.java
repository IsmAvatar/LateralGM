/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.IndexComboBoxConversion;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class FontFrame extends ResourceFrame<Font,PFont>
	{
	private static final long serialVersionUID = 1L;

	public JComboBox fonts;
	public NumberField size;
	public JCheckBox italic, bold;
	public JComboBox aa;
	public NumberField charMin, charMax;
	public JLabel preview;
	public JTextField previewText;

	private FontPropertyListener fpl = new FontPropertyListener();

	public FontFrame(Font res, ResNode node)
		{
		super(res,node);
		res.properties.updateSource.addListener(fpl);

		setResizable(false);
		setMaximizable(false);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);

		JLabel lName = new JLabel(Messages.getString("FontFrame.NAME")); //$NON-NLS-1$

		JLabel lFont = new JLabel(Messages.getString("FontFrame.FONT")); //$NON-NLS-1$
		fonts = new JComboBox(
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		fonts.setEditable(true);
		plf.make(fonts,PFont.FONT_NAME,null);

		JLabel lSize = new JLabel(Messages.getString("FontFrame.SIZE")); //$NON-NLS-1$
		size = new NumberField(1,99);
		plf.make(size,PFont.SIZE);

		bold = new JCheckBox(Messages.getString("FontFrame.BOLD")); //$NON-NLS-1$
		plf.make(bold,PFont.BOLD);
		italic = new JCheckBox(Messages.getString("FontFrame.ITALIC")); //$NON-NLS-1$
		plf.make(italic,PFont.ITALIC);

		String aaprefix = "FontFrame.AA"; //$NON-NLS-1$
		String aalevels[] = new String[4];
		for (int i = 0; i < aalevels.length; i++)
			aalevels[i] = Messages.getString(aaprefix + i);
		aa = new JComboBox(aalevels);
		plf.make(aa,PFont.ANTIALIAS,new IndexComboBoxConversion());
		JLabel aaLabel = new JLabel(Messages.getString("FontFrame.ANTIALIAS")); //$NON-NLS-1$
		//		aa.addActionListener(this);

		JPanel crPane = makeCRPane();

		JLabel lPreview = new JLabel(Messages.getString("FontFrame.PREVIEW")); //$NON-NLS-1$
		previewText = new JTextField(Messages.getString("FontFrame.PREVIEW_DEFAULT"));
		previewText.setColumns(10);
		previewText.getDocument().addDocumentListener(new DocumentListener()
			{
				public void changedUpdate(DocumentEvent e)
					{
					}

				public void insertUpdate(DocumentEvent e)
					{
					preview.setText(previewText.getText());
					}

				public void removeUpdate(DocumentEvent e)
					{
					preview.setText(previewText.getText());
					}
			});
		JPanel prev = new JPanel(new BorderLayout());
		prev.setBorder(BorderFactory.createEtchedBorder());
		preview = new JLabel(previewText.getText());
		preview.setHorizontalAlignment(SwingConstants.CENTER);
		prev.add(preview,"Center"); //$NON-NLS-1$
		updatePreview();

		save.setText(Messages.getString("FontFrame.SAVE")); //$NON-NLS-1$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(lName)
		/*				*/.addComponent(lFont)
		/*				*/.addComponent(lSize))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE)
		/*				*/.addComponent(fonts,120,160,MAX_VALUE)
		/*				*/.addComponent(size)))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(bold)
		/*		*/.addComponent(italic)
		/*		*/.addComponent(aa).addComponent(aaLabel))
		/**/.addComponent(crPane)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lPreview)
		/*		*/.addComponent(previewText))
		/**/.addComponent(prev,120,220,MAX_VALUE)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lName)
		/*		*/.addComponent(name))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lFont)
		/*		*/.addComponent(fonts))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lSize)
		/*		*/.addComponent(size))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(bold)
		/*		*/.addComponent(italic)
		/*		*/.addComponent(aa).addComponent(aaLabel))
		/**/.addComponent(crPane)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lPreview)
		/*		*/.addComponent(previewText))
		/**/.addComponent(prev,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addComponent(save));
		pack();
		}

	private JPanel makeCRPane()
		{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(Messages.getString("FontFrame.CHARRANGE")));
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		charMin = new NumberField(0,255);
		charMin.setCommitsOnValidEdit(false);
		plf.make(charMin,PFont.RANGE_MIN);
		JLabel lTo = new JLabel(Messages.getString("FontFrame.TO")); //$NON-NLS-1$
		charMax = new NumberField(0,255);
		charMax.setCommitsOnValidEdit(false);
		plf.make(charMax,PFont.RANGE_MAX);

		JButton crNormal = new JButton(Messages.getString("FontFrame.NORMAL")); //$NON-NLS-1$
		crNormal.setActionCommand("Normal"); //$NON-NLS-1$
		crNormal.addActionListener(this);

		JButton crAll = new JButton(Messages.getString("FontFrame.ALL")); //$NON-NLS-1$
		crAll.setActionCommand("All"); //$NON-NLS-1$
		crAll.addActionListener(this);

		JButton crDigits = new JButton(Messages.getString("FontFrame.DIGITS")); //$NON-NLS-1$
		crDigits.setActionCommand("Digits"); //$NON-NLS-1$
		crDigits.addActionListener(this);

		JButton crLetters = new JButton(Messages.getString("FontFrame.LETTERS")); //$NON-NLS-1$
		crLetters.setActionCommand("Letters"); //$NON-NLS-1$
		crLetters.addActionListener(this);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(charMin)
		/*		*/.addComponent(lTo)
		/*		*/.addComponent(charMax))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(crNormal,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(crDigits,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(crAll,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(crLetters,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(charMin)
		/*		*/.addComponent(lTo)
		/*		*/.addComponent(charMax))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(crNormal)
		/*				*/.addComponent(crAll))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(crDigits)
		/*				*/.addComponent(crLetters))));
		return panel;
		}

	public void commitChanges()
		{
		charMin.commitOrRevert();
		charMax.commitOrRevert();
		res.setName(name.getText());
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getActionCommand() == "Normal") //$NON-NLS-1$
			{
			res.setRange(32,127);
			return;
			}
		if (e.getActionCommand() == "All") //$NON-NLS-1$
			{
			res.setRange(0,255);
			return;
			}
		if (e.getActionCommand() == "Digits") //$NON-NLS-1$
			{
			res.setRange(48,57);
			return;
			}
		if (e.getActionCommand() == "Letters") //$NON-NLS-1$
			{
			res.setRange(65,122);
			return;
			}
		super.actionPerformed(e);
		}

	public void updatePreview()
		{
		int s = res.get(PFont.SIZE);
		String fn = res.get(PFont.FONT_NAME);
		boolean b = res.get(PFont.BOLD);
		boolean i = res.get(PFont.ITALIC);
		/* Java assumes 72 dpi, but we shouldn't depend on the native resolution either.
		 * For consistent pixel size across different systems, we should pick a common default.
		 * AFAIK, the default in Windows (and thus GM) is 96 dpi. */
		int fontSize = (int) Math.round(s * 96.0 / 72.0);
		preview.setFont(new java.awt.Font(fn,makeStyle(b,i),fontSize));
		}

	public static int makeStyle(boolean bold, boolean italic)
		{
		return (italic ? java.awt.Font.ITALIC : 0) | (bold ? java.awt.Font.BOLD : 0);
		}

	private class FontPropertyListener extends PropertyUpdateListener<PFont>
		{
		public void updated(PropertyUpdateEvent<PFont> e)
			{
			switch (e.key)
				{
				case RANGE_MIN:
				case RANGE_MAX:
					break;
				default:
					updatePreview();
				}
			}
		}
	}
