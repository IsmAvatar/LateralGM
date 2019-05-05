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
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.sub.CharacterRange;
import org.lateralgm.resources.sub.CharacterRange.PCharacterRange;
import org.lateralgm.ui.swing.propertylink.FormattedLink;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.IndexComboBoxConversion;
import org.lateralgm.ui.swing.util.ArrayListModel;
import org.lateralgm.util.AutoComboBox;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class FontFrame extends InstantiableResourceFrame<Font,PFont> implements
		ListSelectionListener,ListDataListener,UpdateListener
	{
	private static final long serialVersionUID = 1L;

	public JComboBox<String> fonts;
	public NumberField size;
	public JCheckBox italic, bold;
	public JComboBox<String> aa;
	public NumberField charMin, charMax;
	private FormattedLink<PCharacterRange> minLink, maxLink;
	public JEditorPane previewText;
	public JTextArea previewRange;
	private JMenuItem cutItem, copyItem, pasteItem, selAllItem;
	private CharacterRange lastRange = null; //non-guaranteed copy of rangeList.getLastSelectedValue()
	public JList<CharacterRange> rangeList;
	private final JPanel crPane;

	private PropertyUpdateListener<PFont> propUpdateListener;

	@Override
	public void dispose()
		{
		super.dispose();
		res.properties.updateSource.removeListener(propUpdateListener);
		res.rangeUpdateSource.removeListener(this);
		}

	public FontFrame(Font res, ResNode node)
		{
		super(res,node);

		this.getRootPane().setDefaultButton(save);
		((JComponent) getContentPane()).setBorder(new EmptyBorder(4,4,4,4));

		propUpdateListener = new PropertyUpdateListener<PFont>()
			{
				public void updated(PropertyUpdateEvent<PFont> e)
					{
					updatePreviewText();
					updatePreviewRange();
					}
			};

		res.properties.updateSource.addListener(propUpdateListener);
		res.rangeUpdateSource.addListener(this);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(false);
		setLayout(layout);

		JLabel lName = new JLabel(Messages.getString("FontFrame.NAME")); //$NON-NLS-1$

		JLabel lFont = new JLabel(Messages.getString("FontFrame.FONT")); //$NON-NLS-1$
		fonts = new AutoComboBox<String>(
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		//fonts.setEditable(true);
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
		aa = new JComboBox<String>(aalevels);
		plf.make(aa, PFont.ANTIALIAS, new IndexComboBoxConversion());

		JLabel aaLabel = new JLabel(Messages.getString("FontFrame.ANTIALIAS")); //$NON-NLS-1$

		crPane = makeCRPane();

		previewText = new JEditorPane();
		previewRange = new JTextArea();
		previewRange.setEditable(false);
		// show the caret anyway
		previewRange.addFocusListener(new FocusListener()
			{
				@Override
				public void focusGained(FocusEvent e)
					{
						previewRange.getCaret().setVisible(true);
						previewRange.getCaret().setSelectionVisible(true);
					}

				@Override
				public void focusLost(FocusEvent e)
					{
						previewRange.getCaret().setVisible(false);
						previewRange.getCaret().setSelectionVisible(false);
					}
			});
		previewRange.setWrapStyleWord(false);

		rangeList = new JList<CharacterRange>();
		rangeList.setModel(new ArrayListModel<CharacterRange>(res.characterRanges));
		rangeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		RangeListComponentRenderer renderer = new RangeListComponentRenderer();
		rangeList.setCellRenderer(renderer);
		rangeList.getModel().addListDataListener(this);
		rangeList.addListSelectionListener(this);
		rangeList.setSelectedIndex(0);
		rangeList.setLayoutOrientation(JList.VERTICAL);
		rangeList.setVisibleRowCount(6);
		if (res.getNode().newRes && res.characterRanges.isEmpty())
			{
			res.addRange();
			}

		String keyString = Messages.getKeyboardString("FontFrame.REM_RANGE"); //$NON-NLS-1$
		KeyStroke stroke = KeyStroke.getKeyStroke(keyString);
		rangeList.getInputMap().put(stroke, "REM_RANGE"); //$NON-NLS-1$
		rangeList.getActionMap().put("REM_RANGE", new AbstractAction() //$NON-NLS-1$
			{
			/**
			 * TODO: Default UID generated, change if necessary.
			 */
			private static final long serialVersionUID = 5308536583486764117L;

			@Override
			public void actionPerformed(ActionEvent e)
				{
				deleteSelectedRange();
				}
			});

		JToolBar rangesTB = new JToolBar();
		rangesTB.setFloatable(false);

		JButton fromPreview = new JButton(Messages.getString("FontFrame.FROMPREVIEW")); //$NON-NLS-1$
		fromPreview.setActionCommand("FromPreview"); //$NON-NLS-1$
		fromPreview.addActionListener(this);
		JButton fromString = new JButton(Messages.getString("FontFrame.FROMSTRING")); //$NON-NLS-1$
		fromString.setActionCommand("FromString"); //$NON-NLS-1$
		fromString.addActionListener(this);
		JButton fromFile = new JButton(Messages.getString("FontFrame.FROMFILE")); //$NON-NLS-1$
		fromFile.setActionCommand("FromFile"); //$NON-NLS-1$
		fromFile.addActionListener(this);
		JButton addRange = new JButton();
		addRange.setIcon(LGM.getIconForKey("FontFrame.ADD_RANGE")); //$NON-NLS-1$
		addRange.setActionCommand("Add"); //$NON-NLS-1$
		addRange.addActionListener(this);
		JButton remRange = new JButton();
		remRange.setIcon(LGM.getIconForKey("FontFrame.REMOVE_RANGE")); //$NON-NLS-1$
		remRange.setActionCommand("Remove"); //$NON-NLS-1$
		remRange.addActionListener(this);
		JButton clearRange = new JButton(Messages.getString("FontFrame.CLEAR")); //$NON-NLS-1$
		clearRange.setActionCommand("Clear"); //$NON-NLS-1$
		clearRange.addActionListener(this);

		rangesTB.add(addRange);
		rangesTB.addSeparator();
		rangesTB.add(fromPreview);
		rangesTB.addSeparator();
		rangesTB.add(fromString);
		rangesTB.addSeparator();
		rangesTB.add(fromFile);
		rangesTB.addSeparator();
		rangesTB.add(remRange);
		rangesTB.addSeparator();
		rangesTB.add(clearRange);

		JScrollPane listScroller = new JScrollPane(rangeList);
		listScroller.setPreferredSize(new Dimension(250,80));
		add(listScroller);
		previewText.setText(Messages.getString("FontFrame.PREVIEW_DEFAULT")); //$NON-NLS-1$
		makeContextMenu();

		JScrollPane previewTextScroll = new JScrollPane(previewText);
		JScrollPane previewRangeScroll = new JScrollPane(previewRange);
		previewRangeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		previewText.setSize(500,500);
		updatePreviewText();
		updatePreviewRange();

		save.setText(Messages.getString("FontFrame.SAVE")); //$NON-NLS-1$

		SequentialGroup orientationGroup = layout.createSequentialGroup();

		if (Prefs.rightOrientation) {
			orientationGroup.addGroup(
					layout.createParallelGroup().addComponent(previewTextScroll,0,500,MAX_VALUE).addComponent(
							previewRangeScroll,0,500,MAX_VALUE));
		}

		orientationGroup.addGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
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
		/**/.addComponent(rangesTB)
		/**/.addGroup(layout.createSequentialGroup()
		/**/.addComponent(listScroller,120,220,MAX_VALUE))
		/**/.addComponent(crPane)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));

		if (!Prefs.rightOrientation) {
			orientationGroup.addGroup(
					layout.createParallelGroup().addComponent(previewTextScroll,0,500,MAX_VALUE).addComponent(
							previewRangeScroll,0,500,MAX_VALUE));
		}

		layout.setHorizontalGroup(orientationGroup);

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
		/**/.addComponent(rangesTB,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/**/.addComponent(listScroller,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(crPane)
		/**/.addComponent(save)).addGroup(
				layout.createSequentialGroup().addComponent(previewTextScroll,DEFAULT_SIZE,100,MAX_VALUE).addComponent(
						previewRangeScroll,DEFAULT_SIZE,100,MAX_VALUE)));

		Util.setComponentTreeEnabled(crPane,!rangeList.isSelectionEmpty());
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

	public void makeContextMenu()
		{
		// build popup menu
		JPopupMenu popup = new JPopupMenu();

		cutItem = addItem("FontFrame.CUT"); //$NON-NLS-1$
		cutItem.setAccelerator(Messages.getKeyboardStroke("FontFrame.CUT")); //$NON-NLS-1$
		popup.add(cutItem);
		copyItem = addItem("FontFrame.COPY"); //$NON-NLS-1$
		copyItem.setAccelerator(Messages.getKeyboardStroke("FontFrame.COPY")); //$NON-NLS-1$
		popup.add(copyItem);
		pasteItem = addItem("FontFrame.PASTE"); //$NON-NLS-1$
		pasteItem.setAccelerator(Messages.getKeyboardStroke("FontFrame.PASTE")); //$NON-NLS-1$
		popup.add(pasteItem);
		popup.addSeparator();
		selAllItem = addItem("FontFrame.SELECTALL"); //$NON-NLS-1$
		selAllItem.setAccelerator(Messages.getKeyboardStroke("FontFrame.SELECTALL")); //$NON-NLS-1$
		popup.add(selAllItem);

		previewText.setComponentPopupMenu(popup);

		popup = new JPopupMenu();

		copyItem = addItem("FontFrame.COPY"); //$NON-NLS-1$
		copyItem.setAccelerator(Messages.getKeyboardStroke("FontFrame.COPY")); //$NON-NLS-1$
		copyItem.setActionCommand("COPYRANGE"); //$NON-NLS-1$
		popup.add(copyItem);
		popup.addSeparator();
		selAllItem = addItem("FontFrame.SELECTALL"); //$NON-NLS-1$
		selAllItem.setAccelerator(Messages.getKeyboardStroke("FontFrame.SELECTALL")); //$NON-NLS-1$
		selAllItem.setActionCommand("SELECTALLRANGE"); //$NON-NLS-1$
		popup.add(selAllItem);

		previewRange.setComponentPopupMenu(popup);
		}

	private JPanel makeCRPane()
		{
		JPanel panel = new JPanel();
		String title = Messages.getString("FontFrame.CHARRANGE"); //$NON-NLS-1$
		panel.setBorder(BorderFactory.createTitledBorder(title));
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		charMin = new NumberField(0,Integer.MAX_VALUE);
		charMin.setCommitsOnValidEdit(true);
		//charMin.addValueChangeListener(this);
		//plf.make(charMin,PFont.RANGE_MIN);
		JLabel lTo = new JLabel(Messages.getString("FontFrame.TO")); //$NON-NLS-1$
		charMax = new NumberField(0,Integer.MAX_VALUE);
		charMax.setCommitsOnValidEdit(true);
		//charMin.addValueChangeListener(this);
		//plf.make(charMax,PFont.RANGE_MAX);

		JButton crNormal = new JButton(Messages.getString("FontFrame.NORMAL")); //$NON-NLS-1$
		crNormal.setActionCommand("Normal"); //$NON-NLS-1$
		crNormal.addActionListener(this);

		JButton crAll = new JButton(Messages.getString("FontFrame.ASCII")); //$NON-NLS-1$
		crAll.setActionCommand("ASCII"); //$NON-NLS-1$
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

	protected boolean areResourceFieldsEqual()
		{
		return res.characterRanges.equals(resOriginal.characterRanges);
		}

	public void commitChanges()
		{
		charMin.commitOrRevert();
		charMax.commitOrRevert();
		res.setName(name.getText());
		}

	private void deleteSelectedRange()
		{
		int sel = rangeList.getSelectedIndex();
		if (sel != -1)
			res.characterRanges.remove(sel);
		if (sel >= res.characterRanges.size())
			{
			if (res.characterRanges.isEmpty())
				rangeList.clearSelection();
			else
				rangeList.setSelectedIndex(res.characterRanges.size() - 1);
			}
		else
			fireRangeUpdate();
		}

	private void setRange(int min, int max)
		{
		CharacterRange cr = rangeList.getSelectedValue();
		if (cr != null)
			{
			cr.properties.put(PCharacterRange.RANGE_MIN, min);
			cr.properties.put(PCharacterRange.RANGE_MAX, max);
			}
		}

	public void actionPerformed(ActionEvent ev)
		{
		switch (ev.getActionCommand())
			{
			case "Normal": //$NON-NLS-1$
				setRange(32, 127);
				break;
			case "ASCII": //$NON-NLS-1$
				setRange(0, 255);
				break;
			case "Digits": //$NON-NLS-1$
				setRange('0', '9');
				break;
			case "Letters": //$NON-NLS-1$
				setRange('A', 'z');
				break;
			case "FromPreview": //$NON-NLS-1$
				res.addRangesFromString(previewText.getText());
				break;
			case "FromString": //$NON-NLS-1$
				{
				String result = JOptionPane.showInputDialog(this, "", "Character Sequence",
						JOptionPane.PLAIN_MESSAGE);
				if (result != null)
					{
					res.addRangesFromString(result);
					}
				break;
				}
			case "FromFile": //$NON-NLS-1$
				{
				CustomFileChooser fc = new CustomFileChooser("/org/lateralgm", "LAST_FILE_DIR");  //$NON-NLS-1$//$NON-NLS-2$
				fc.setMultiSelectionEnabled(false);
				if (fc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
					res.addRangesFromFile(fc.getSelectedFile());
				break;
				}
			case "Add": //$NON-NLS-1$
				res.addRange();
				break;
			case "Remove": //$NON-NLS-1$
				deleteSelectedRange();
				break;
			case "Clear": //$NON-NLS-1$
				res.characterRanges.clear();
				rangeList.clearSelection();
				return;
			case "FontFrame.CUT": //$NON-NLS-1$
				previewText.cut();
				break;
			case "FontFrame.COPY": //$NON-NLS-1$
				previewText.copy();
				break;
			case "FontFrame.PASTE": //$NON-NLS-1$
				previewText.paste();
				break;
			case "FontFrame.SELECTALL": //$NON-NLS-1$
				previewText.selectAll();
				break;
			case "SELECTALLRANGE": //$NON-NLS-1$
				previewRange.selectAll();
				break;
			case "COPYRANGE": //$NON-NLS-1$
				previewRange.copy();
				break;
			default:
				super.actionPerformed(ev);
			}
		}

	public void updatePreviewText()
		{
		previewText.setFont(res.getAWTFont());
		}

	public void updatePreviewRange()
		{
		StringBuilder text = new StringBuilder();
		for (CharacterRange cr : res.characterRanges)
			{
			int min = cr.properties.get(PCharacterRange.RANGE_MIN);
			int max = cr.properties.get(PCharacterRange.RANGE_MAX);
			// NOTE: Arbitrarily limit a single range to no more than 1000 sequential characters to stop
			// our editor from lagging like GM's
			if (max - min > 1000 || max < min)
				{
				max = min + 1000;
				}
			for (int i = min; i <= max; i++)
				{
				// TODO: Replace new line character with just an empty space,
				// otherwise it will screw up word wrapping in the preview area.
				if (i == '\n')
					{
					text.append(' ');
					continue;
					}
				text.append(Character.toChars(i));
				}
			text.append('\n');
			}
		previewRange.setText(text.toString());
		previewRange.setFont(res.getAWTFont());
		}

	private static class RangeListComponentRenderer extends DefaultListCellRenderer
		{
		/**
		 * Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = 6095060644468207193L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object val, int ind, boolean selected, boolean focus)
			{
			Component comp = super.getListCellRendererComponent(list,val,ind,selected,focus);
			if (!(val instanceof CharacterRange)) return comp;
			CharacterRange cr = (CharacterRange) val;
			this.setText(Messages.format("FontFrame.CHARRANGE_FORMAT",cr.properties.get(PCharacterRange.RANGE_MIN), //$NON-NLS-1$
					cr.properties.get(PCharacterRange.RANGE_MAX)));
			return comp;
			}
		}

	public void fireRangeUpdate()
		{
		CharacterRange cr = rangeList.getSelectedValue();
		if (lastRange == cr) return;
		lastRange = cr;
		PropertyLink.removeAll(minLink,maxLink);
		if (cr != null)
			{
			PropertyLinkFactory<PCharacterRange> rplf = new PropertyLinkFactory<PCharacterRange>(
					cr.properties,this);
			minLink = rplf.make(charMin,PCharacterRange.RANGE_MIN);
			maxLink = rplf.make(charMax,PCharacterRange.RANGE_MAX);
			}
		}

	public void updated(UpdateEvent e)
		{
		if (e.source == res.rangeUpdateSource) rangeList.setPrototypeCellValue(null);
		updatePreviewRange();
		}

	public void valueChanged(ListSelectionEvent e)
		{
		if (e.getValueIsAdjusting()) return;

		if (e.getSource() == rangeList)
			{
			fireRangeUpdate();
			Util.setComponentTreeEnabled(crPane,!rangeList.isSelectionEmpty());
			}

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
