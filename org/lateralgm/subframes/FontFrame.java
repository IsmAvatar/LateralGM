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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.IndexComboBoxConversion;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class FontFrame extends InstantiableResourceFrame<Font,PFont>
	{
	private static final long serialVersionUID = 1L;

	public JComboBox fonts;
	public NumberField size;
	public JCheckBox italic, bold;
	public JComboBox aa;
	public NumberField charMin, charMax;
	public JEditorPane previewText;

	private FontPropertyListener fpl = new FontPropertyListener();

	public FontFrame(Font res, ResNode node)
		{
		super(res,node);
		res.properties.updateSource.addListener(fpl);

		setResizable(false);
		setMaximizable(false);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(false);
    int lmargin = 3;
    int rmargin = 3;
    int bmargin = 3;
    int tmargin = 3;
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

		previewText = new JEditorPane();
		
    add(new JScrollPane(previewText),BorderLayout.CENTER);
    previewText.setText(Messages.getString("FontFrame.PREVIEW_DEFAULT"));
    //editor.setFont(new Font("Courier 10 Pitch", Font.PLAIN, 12));
    //editor.setEditable(false);
    //editor.getCaret().setVisible(true); // show the caret anyway
		makeContextMenu();

		JScrollPane prev = new JScrollPane(previewText);
		//prev.setBorder(BorderFactory.createEtchedBorder());
		updatePreview();

		save.setText(Messages.getString("FontFrame.SAVE")); //$NON-NLS-1$
		
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
				.addGap(lmargin)
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(lName)
		/*				*/.addComponent(lFont)
		/*				*/.addComponent(lSize))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE)
		/*				*/.addComponent(fonts,120,160,MAX_VALUE)
		/*				*/.addComponent(size))
		     .addGap(rmargin))
		/**/.addGroup(layout.createSequentialGroup()
				 .addGap(lmargin)
		/*		*/.addComponent(aaLabel).addComponent(aa)
		/*		*/.addComponent(bold)
		/*		*/.addComponent(italic)
		     .addGap(rmargin))
		    .addGap(lmargin)
		/**/.addComponent(crPane)
		    .addGap(rmargin)
		/**/.addGroup(layout.createSequentialGroup()
				.addGap(lmargin)
		/**/.addComponent(prev,120,220,MAX_VALUE)
		    .addGap(rmargin))
		    .addGap(lmargin)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		.addGap(rmargin));
		layout.setVerticalGroup(layout.createSequentialGroup()
		.addGap(tmargin)
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
		/*		*/.addComponent(aa).addComponent(aaLabel)
		/*		*/.addComponent(bold)
		/*		*/.addComponent(italic))
		/**/.addComponent(crPane)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/**/.addComponent(prev,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(save)
		.addGap(bmargin));
		pack();
		}
	
	public JMenuItem addItem(String key)
		{
			JMenuItem item = new JMenuItem(Messages.getString(key));
			item.setIcon(LGM.getIconForKey(key));
			item.setActionCommand(key);
			item.addActionListener(this);
			return item;
		}
	
	public JPopupMenu makeContextMenu()
		{
	  // build popup menu
	  final JPopupMenu popup = new JPopupMenu();
	  JMenuItem item;
	  
		item = addItem("FontFrame.CUT"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		item = addItem("FontFrame.COPY"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		item = addItem("FontFrame.PASTE"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		popup.addSeparator();
		item = addItem("FontFrame.SELECTALL"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		
	  previewText.setComponentPopupMenu(popup);
		previewText.addMouseListener(new MouseAdapter() {

	  	@Override
	  	public void mousePressed(MouseEvent e) {
	      showPopup(e);
	  	}

	  	@Override
	  	public void mouseReleased(MouseEvent e) {
	      showPopup(e);
	  	}

	  	private void showPopup(MouseEvent e) {
	  		if (e.isPopupTrigger()) {
	  			popup.show(e.getComponent(), e.getX(), e.getY());
	  		}
	  	}
		});
		
		return popup;
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

    int lmargin = 2;
    int rmargin = 2;
    int bmargin = 2;
    int tmargin = 2;
    layout.setAutoCreateContainerGaps(false);
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
		.addGap(tmargin)
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
		/*				*/.addComponent(crLetters)))
		.addGap(bmargin));
		return panel;
		}

	public void commitChanges()
		{
		charMin.commitOrRevert();
		charMax.commitOrRevert();
		res.setName(name.getText());
		}

	public void actionPerformed(ActionEvent ev)
		{
		String com = ev.getActionCommand();
		if (com.equals("Normal")) //$NON-NLS-1$
			{
			res.setRange(32,127);
			return;
			}
		else if (com.equals("All")) //$NON-NLS-1$
			{
			res.setRange(0,255);
			return;
			}
		else if (com.equals("Digits")) //$NON-NLS-1$
			{
			res.setRange(48,57);
			return;
			}
		else if (com.equals("Letters")) //$NON-NLS-1$
			{
			res.setRange(65,122);
			return;
			}
		else if (com.equals("FontFrame.CUT")) //$NON-NLS-1$
		{
			previewText.cut();
			return;
		}
		else if (com.equals("FontFrame.COPY")) //$NON-NLS-1$
		{
			previewText.copy();
			return;
		}
		else if (com.equals("FontFrame.PASTE")) //$NON-NLS-1$
		{
			previewText.paste();
			return;
		}
		else if (com.equals("FontFrame.SELECTALL")) //$NON-NLS-1$
		{
			previewText.selectAll();
			return;
		}
		super.actionPerformed(ev);
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
		previewText.setFont(new java.awt.Font(fn,makeStyle(b,i),fontSize));
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
