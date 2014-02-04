/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014, Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;

import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.sub.CharacterRange;
import org.lateralgm.resources.sub.CharacterRange.PCharacterRange;
import org.lateralgm.subframes.RoomFrame.ListComponentRenderer;
import org.lateralgm.ui.swing.propertylink.FormattedLink;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.IndexComboBoxConversion;
import org.lateralgm.ui.swing.util.ArrayListModel;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class FontFrame extends InstantiableResourceFrame<Font,PFont> implements ListSelectionListener,ListDataListener,UpdateListener
	{
	private static final long serialVersionUID = 1L;

	public JComboBox fonts;
	public NumberField size;
	public JCheckBox italic, bold;
	public JComboBox aa;
	public NumberField charMin, charMax;
	private FormattedLink<PCharacterRange> minLink, maxLink;
	public JEditorPane previewText;
	public JTextArea previewRange;
	private CharacterRange lastRange = null; //non-guaranteed copy of rangeList.getLastSelectedValue()
	public JList rangeList;
	
	private FontPropertyListener fpl = new FontPropertyListener();

	public FontFrame(Font res, ResNode node)
		{
		super(res,node);
		((JComponent)getContentPane()).setBorder(new EmptyBorder(4, 4, 4, 4));
		
		res.properties.updateSource.addListener(fpl);
		res.rangeUpdateSource.addListener(this);
		res.rangeUpdateSource.addListener(this);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(false);
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
		previewRange = new JTextArea();
		previewRange.setEditable(false);
		previewRange.getCaret().setVisible(true); // show the caret anyway
		previewRange.addFocusListener(new FocusListener() {
		  public void focusLost(FocusEvent e) {
		    return;
		  }
		
		  public void focusGained(FocusEvent e) {
		  	previewRange.getCaret().setVisible(true); // show the caret anyway
		  }
		});
		previewRange.setWrapStyleWord(false);
		
		rangeList = new JList(new ArrayListModel<CharacterRange>(res.characterRanges));
		rangeList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		RangeListComponentRenderer renderer = new RangeListComponentRenderer();  
		rangeList.setCellRenderer(renderer);
		rangeList.getModel().addListDataListener(this);
		rangeList.addListSelectionListener(this);
		rangeList.setSelectedIndex(0);
		rangeList.setLayoutOrientation(JList.VERTICAL);
		rangeList.setVisibleRowCount(6);
		
		JButton addRange = new JButton("+");
		addRange.setActionCommand("Add"); //$NON-NLS-1$
		addRange.addActionListener(this);
		JButton remRange = new JButton("-");
		remRange.setActionCommand("Remove"); //$NON-NLS-1$
		remRange.addActionListener(this);
		JButton clearRange = new JButton("Clear");
		clearRange.setActionCommand("Clear"); //$NON-NLS-1$
		clearRange.addActionListener(this);

		JScrollPane listScroller = new JScrollPane(rangeList);
		listScroller.setPreferredSize(new Dimension(250, 80));
		add(listScroller);
    previewText.setText(Messages.getString("FontFrame.PREVIEW_DEFAULT"));
		makeContextMenu();

		JScrollPane previewTextScroll = new JScrollPane(previewText);
		JScrollPane previewRangeScroll = new JScrollPane(previewRange);
		previewRangeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		previewText.setSize(500, 500);
		updatePreviewText();
		updatePreviewRange();
		//prev.setBorder(BorderFactory.createEtchedBorder());

		save.setText(Messages.getString("FontFrame.SAVE")); //$NON-NLS-1$
		
		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
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
		/*		*/.addComponent(aaLabel).addComponent(aa)
		/*		*/.addComponent(bold)
		/*		*/.addComponent(italic))
		/**/.addComponent(crPane)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(addRange)
		/*		*/.addComponent(remRange)
		/*		*/.addComponent(clearRange))
		/**/.addGroup(layout.createSequentialGroup()
		/**/.addComponent(listScroller,120,220,MAX_VALUE))
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		.addGroup(layout.createParallelGroup()
		.addComponent(previewTextScroll)
		.addComponent(previewRangeScroll)));
		
		layout.setVerticalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
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
		/*		*/.addComponent(addRange)
		/*		*/.addComponent(remRange)
		/*		*/.addComponent(clearRange))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/**/.addComponent(listScroller,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(save))
		.addGroup(layout.createSequentialGroup()
		.addComponent(previewTextScroll)
		.addComponent(previewRangeScroll)));
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
		charMin.setCommitsOnValidEdit(true);
		//charMin.addValueChangeListener(this);
		//plf.make(charMin,PFont.RANGE_MIN);
		JLabel lTo = new JLabel(Messages.getString("FontFrame.TO")); //$NON-NLS-1$
		charMax = new NumberField(0,255);
		charMax.setCommitsOnValidEdit(true);
		//charMin.addValueChangeListener(this);
		//plf.make(charMax,PFont.RANGE_MAX);

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

	public void actionPerformed(ActionEvent ev)
		{
		String com = ev.getActionCommand();
		if (com.equals("Normal")) //$NON-NLS-1$
			{
			CharacterRange cr = (CharacterRange) rangeList.getSelectedValue();
			if (cr != null) {
				cr.properties.put(PCharacterRange.RANGE_MIN,32);
				cr.properties.put(PCharacterRange.RANGE_MAX,127);
			}
			return;
			}
		else if (com.equals("All")) //$NON-NLS-1$
			{
			CharacterRange cr = (CharacterRange) rangeList.getSelectedValue();
			if (cr != null) {
				cr.properties.put(PCharacterRange.RANGE_MIN,0);
				cr.properties.put(PCharacterRange.RANGE_MAX,255);
			}
			return;
			}
		else if (com.equals("Digits")) //$NON-NLS-1$
			{
			CharacterRange cr = (CharacterRange) rangeList.getSelectedValue();
			if (cr != null) {
				cr.properties.put(PCharacterRange.RANGE_MIN,48);
				cr.properties.put(PCharacterRange.RANGE_MAX,57);
			}
			return;
			}
		else if (com.equals("Letters")) //$NON-NLS-1$
			{
			CharacterRange cr = (CharacterRange) rangeList.getSelectedValue();
			if (cr != null) {
				cr.properties.put(PCharacterRange.RANGE_MIN,65);
				cr.properties.put(PCharacterRange.RANGE_MAX,122);
			}
			return;
			}
		else if (com.equals("Add")) {
			res.addRange();
			return;
		}
		else if (com.equals("Remove")) {
			int sel = rangeList.getSelectedIndex();
			if (rangeList.getSelectedValue() != null) {
				res.characterRanges.remove(sel);
			}
			return;
		}
		else if (com.equals("Clear")) {
			res.characterRanges.clear();
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
	
	public java.awt.Font getAWTFont() {
		int s = res.get(PFont.SIZE);
		String fn = res.get(PFont.FONT_NAME);
		boolean b = res.get(PFont.BOLD);
		boolean i = res.get(PFont.ITALIC);
		/* Java assumes 72 dpi, but we shouldn't depend on the native resolution either.
		 * For consistent pixel size across different systems, we should pick a common default.
		 * AFAIK, the default in Windows (and thus GM) is 96 dpi. */
		int fontSize = (int) Math.round(s * 96.0 / 72.0);
		return new java.awt.Font(fn,makeStyle(b,i),fontSize);
	}

	public void updatePreviewText()
		{
		previewText.setFont(getAWTFont());
		}
	
	public void updatePreviewRange() {
		String text = "";
		for (CharacterRange cr : res.characterRanges) {
			int min = cr.properties.get(PCharacterRange.RANGE_MIN);
			int max = cr.properties.get(PCharacterRange.RANGE_MAX);
			for (int i = min; i < max; i++) {
				//TODO: Replace new line character with just an empty space, 
				// otherwise it will screw up word wrapping in the preview area.
				if ((char) i == '\n') { text += ' '; continue; }
				text += (char) (i);
			}
			text += "\n";
		}
		previewRange.setText(text);
		previewRange.setFont(getAWTFont());
	}

	public static int makeStyle(boolean bold, boolean italic)
		{
		return (italic ? java.awt.Font.ITALIC : 0) | (bold ? java.awt.Font.BOLD : 0);
		}

	private class FontPropertyListener extends PropertyUpdateListener<PFont>
		{
		public void updated(PropertyUpdateEvent<PFont> e)
			{
				updatePreviewText();
				updatePreviewRange();
			}
		}
	
	private static class RangeListComponentRenderer implements ListCellRenderer
	{
	private final JLabel lab = new JLabel();
	private final ListComponentRenderer lcr = new ListComponentRenderer();

	public RangeListComponentRenderer()
		{
		lab.setOpaque(true);
		}

	public Component getListCellRendererComponent(JList list, Object val, int ind,
			boolean selected, boolean focus)
		{
		CharacterRange i = (CharacterRange) val;
		lcr.getListCellRendererComponent(list,lab,ind,selected,focus);
		lab.setText(" " + i.properties.get(PCharacterRange.RANGE_MIN) + " " + 
				Messages.getString("FontFrame.TO") + " " +
				i.properties.get(PCharacterRange.RANGE_MAX));
		return lab;
		}
	}
	
	public void fireRangeUpdate() {
		CharacterRange cr = (CharacterRange) rangeList.getSelectedValue();
		if (lastRange == cr) return;
		lastRange = cr;
		PropertyLink.removeAll(minLink, maxLink);
		if (cr != null)
		{
			PropertyLinkFactory<PCharacterRange> rplf = new PropertyLinkFactory<PCharacterRange>(cr.properties,this);
			minLink = rplf.make(charMin,PCharacterRange.RANGE_MIN);
			maxLink = rplf.make(charMax,PCharacterRange.RANGE_MAX);
		}
	}
	
	public void updated(UpdateEvent e)
	{
		if (e.source == res.rangeUpdateSource) rangeList.setPrototypeCellValue(null);
		updatePreviewRange();
	}

  public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
	
		if (e.getSource() == rangeList) fireRangeUpdate();
		
	}

	public void contentsChanged(ListDataEvent arg0)
		{
		updatePreviewRange();
		}

	public void intervalAdded(ListDataEvent arg0)
		{
		updatePreviewRange();
		}

	public void intervalRemoved(ListDataEvent arg0)
		{
		updatePreviewRange();
		}
	
	}
