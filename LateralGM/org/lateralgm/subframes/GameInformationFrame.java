/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.lateralgm.components.CustomFileFilter;
import org.lateralgm.main.LGM;
import org.lateralgm.resources.GameInformation;

public class GameInformationFrame extends JInternalFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private static JEditorPane editor;
	private static RTFEditorKit rtf = new RTFEditorKit();
	public static GameInformation gi = new GameInformation();
	private JComboBox m_cbFonts;
	private JComboBox m_cbSizes;
	private JSpinner m_sSizes;
	private JToggleButton m_tbBold;
	private JToggleButton m_tbItalic;
	private JToggleButton m_tbUnderline;

	public GameInformationFrame()
		{
		super(Messages.getString("GameInformationFrame.TITLE"),true,true,true,true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(600,400);
		setFrameIcon(LGM.getIconForKey("GameInformationFrame.INFO")); //$NON-NLS-1$
		// Setup the Menu
		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();

			// Create File menu
			{
			JMenu Fmenu = new JMenu(Messages.getString("GameInformationFrame.MENU_FILE")); //$NON-NLS-1$
			menuBar.add(Fmenu);
			Fmenu.addActionListener(this);

			// Create a file menu items
			JMenuItem item = addItem("GameInformationFrame.LOAD"); //$NON-NLS-1$
			Fmenu.add(item);
			item = addItem("GameInformationFrame.SAVE"); //$NON-NLS-1$
			Fmenu.add(item);
			Fmenu.addSeparator();
			item = addItem("GameInformationFrame.OPTIONS"); //$NON-NLS-1$
			item.setEnabled(false);
			Fmenu.add(item);
			Fmenu.addSeparator();
			item = addItem("GameInformationFrame.PRINT"); //$NON-NLS-1$
			item.setEnabled(false);
			Fmenu.add(item);
			Fmenu.addSeparator();
			item = addItem("GameInformationFrame.CLOSESAVE"); //$NON-NLS-1$
			Fmenu.add(item);
			}

			// Create Edit menu
			{
			JMenu Emenu = new JMenu(Messages.getString("GameInformationFrame.MENU_EDIT")); //$NON-NLS-1$
			menuBar.add(Emenu);

			// Create a menu item
			JMenuItem item = addItem("GameInformationFrame.UNDO"); //$NON-NLS-1$
			Emenu.add(item);
			item.setEnabled(false);
			Emenu.addSeparator();
			item = addItem("GameInformationFrame.CUT"); //$NON-NLS-1$
			Emenu.add(item);
			item.setEnabled(false);
			item = addItem("GameInformationFrame.COPY"); //$NON-NLS-1$
			Emenu.add(item);
			item.setEnabled(false);
			item = addItem("GameInformationFrame.PASTE"); //$NON-NLS-1$
			Emenu.add(item);
			item.setEnabled(false);
			Emenu.addSeparator();
			item = addItem("GameInformationFrame.SELECTALL"); //$NON-NLS-1$
			Emenu.add(item);
			item.setEnabled(false);
			Emenu.addSeparator();
			item = addItem("GameInformationFrame.GOTO"); //$NON-NLS-1$
			Emenu.add(item);
			item.setEnabled(false);
			}

			// Create Format menu
			{
			JMenu Fmenu = new JMenu(Messages.getString("GameInformationFrame.MENU_FORMAT")); //$NON-NLS-1$
			menuBar.add(Fmenu);

			// Create a menu item
			JMenuItem item = addItem("GameInformationFrame.FONT"); //$NON-NLS-1$
			// item.addActionListener(actionListener);
			Fmenu.add(item);
			}

		// Install the menu bar in the frame
		setJMenuBar(menuBar);

			// Setup the toolbar
			{
			JToolBar tool = new JToolBar();
			tool.setFloatable(false);
			add("North",tool); //$NON-NLS-1$

			// Setup the buttons
			JButton but = new JButton(LGM.getIconForKey("GameInformationFrame.SAVE")); //$NON-NLS-1$
			but.setActionCommand("GameInformationFrame.SAVE"); //$NON-NLS-1$
			but.addActionListener(this);
			tool.add(but);

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			String[] fontNames = ge.getAvailableFontFamilyNames();
			tool.addSeparator();
			m_cbFonts = new JComboBox(fontNames);
			m_cbFonts.setMaximumSize(m_cbFonts.getPreferredSize());
			m_cbFonts.setEditable(true);
			ActionListener lst = new ActionListener()
				{
					private String m_fontName;

					public void actionPerformed(ActionEvent e)
						{
						m_fontName = m_cbFonts.getSelectedItem().toString();
						MutableAttributeSet attr = new SimpleAttributeSet();
						StyleConstants.setFontFamily(attr,m_fontName);
						// setAttributeSet(attr);
						// m_monitor.grabFocus();
						}
				};

			m_cbFonts.addActionListener(lst);
			tool.add(m_cbFonts);
			tool.addSeparator();
			m_sSizes = new JSpinner(new SpinnerNumberModel(12,1,100,1));
			m_sSizes.setMaximumSize(m_sSizes.getPreferredSize());
			//m_sSizes.setEditor(new JSpinner.DefaultEditor(m_sSizes));
			m_sSizes.addChangeListener(new ChangeListener()
				{
					public void stateChanged(ChangeEvent arg0)
						{
//						int wtfgod = ((SpinnerNumberModel)m_sSizes.getModel()).getNumber().intValue();
						//TODO: code this! Also, make the RTFEditor stop losing focus.
						}
				});
			tool.add(m_sSizes);

			m_cbSizes = new JComboBox(new String[] { "8","9","10","11","12","14","16","18","20","22","24","26",
					"28","36","48","72" });
			m_cbSizes.setMaximumSize(m_cbSizes.getPreferredSize());
			m_cbSizes.setEditable(true);
			lst = new ActionListener()
				{
					// private int m_fontSize;

					public void actionPerformed(ActionEvent e)
						{
						int fontSize = 0;
						try
							{
							fontSize = Integer.parseInt(m_cbSizes.getSelectedItem().toString());
							}
						catch (NumberFormatException ex)
							{
							return;
							}
						// m_fontSize = fontSize;
						MutableAttributeSet attr = new SimpleAttributeSet();
						StyleConstants.setFontSize(attr,fontSize);
						// setAttributeSet(attr);
						// m_monitor.grabFocus();
						}
				};

			m_cbSizes.addActionListener(lst);
			tool.add(m_cbSizes);
			tool.addSeparator();

			m_tbBold = new JToggleButton("B");
			// m_tbBold.setFont(new java.awt.Font("Courier New",java.awt.Font.BOLD,10));
			tool.add(m_tbBold);
			m_tbItalic = new JToggleButton("I");
			// m_tbItalic.setFont(m_tbBold.getFont().deriveFont(java.awt.Font.ITALIC));
			tool.add(m_tbItalic);
			m_tbUnderline = new JToggleButton("U");
			// m_tbUnderline = new JToggleButton("<html><u>U</u></html>");
			// m_tbUnderline.setFont(m_tbBold.getFont().deriveFont(java.awt.Font.PLAIN));
			// m_tbUnderline.setMaximumSize(m_tbBold.getSize());
			tool.add(m_tbUnderline);

			tool.addSeparator();
			but = new JButton(LGM.getIconForKey("GameInformationFrame.COLOR")); //$NON-NLS-1$
			but.setActionCommand("BackgroundColor");
			but.addActionListener(this);
			tool.add(but);
			}

		// Create an RTF editor window
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel,BorderLayout.CENTER);

		editor = new JEditorPane();
		// editor.setEditable(false);
		editor.setEditorKit(rtf);
		editor.setBackground(LGM.currentFile.GameInfo.BackgroundColor);
		editor.addCaretListener(new CaretListener()
			{
				public void caretUpdate(CaretEvent ce)
					{
					StyledDocument d = (StyledDocument) editor.getDocument();
					AttributeSet as = d.getCharacterElement(ce.getDot()).getAttributes();
					Object f = as.getAttribute(StyleConstants.Family);
					Object s = as.getAttribute(StyleConstants.Size);
					Object b = as.getAttribute(StyleConstants.Bold);
					Object i = as.getAttribute(StyleConstants.Italic);
					Object u = as.getAttribute(StyleConstants.Underline);
					if (f instanceof String) m_cbFonts.setSelectedItem((String) f);
					if (s instanceof Integer) m_cbSizes.setSelectedItem((Integer) s);
					if (b instanceof Boolean) m_tbBold.setSelected((Boolean) b);
					if (i instanceof Boolean) m_tbItalic.setSelected((Boolean) i);
					if (u instanceof Boolean) m_tbUnderline.setSelected((Boolean) u);
					}
			});

		// This text could be big so add a scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(editor);
		topPanel.add(scroller,BorderLayout.CENTER);

		add_rtf(LGM.currentFile.GameInfo.GameInfoStr);
		}

	public static void add_rtf(String str)
		{
		try
			{
			rtf.read(new ByteArrayInputStream(str.getBytes()),editor.getDocument(),0);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public JMenuItem addItem(String key)
		{
		JMenuItem item = new JMenuItem(Messages.getString(key));
		item.setIcon(LGM.getIconForKey(key));
		item.setActionCommand(key);
		item.addActionListener(this);
		add(item);
		return item;
		}

	public void load_from_file()
		{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(".rtf",Messages.getString("GameInformationFrame.TYPE_RTF"))); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setDialogTitle(Messages.getString("GameInformationFrame.LOAD_TITLE")); //$NON-NLS-1$
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
		boolean repeat = true;
		while (repeat)
			{
			if (fc.showOpenDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			if (fc.getSelectedFile().exists())
				repeat = false;
			else
				JOptionPane
						.showMessageDialog(
								null,
								fc.getSelectedFile().getName() + Messages.getString("SoundFrame.FILE_MISSING"),Messages.getString("GameInformationFrame.LOAD_TITLE"), //$NON-NLS-1$ //$NON-NLS-2$
								JOptionPane.WARNING_MESSAGE);
			}
		try
			{
			FileInputStream i = new FileInputStream(fc.getSelectedFile());
			rtf.read(i,editor.getDocument(),0);
			i.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public void save_to_file()
		{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(".rtf",Messages.getString("GameInformationFrame.TYPE_RTF"))); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setDialogTitle(Messages.getString("GameInformationFrame.SAVE_TITLE")); //$NON-NLS-1$
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		String name = fc.getSelectedFile().getPath();
		if (CustomFileFilter.getExtension(name) == null) name += ".rtf"; //$NON-NLS-1$
		try
			{
			FileOutputStream i = new FileOutputStream(new File(name));
			rtf.write(i,editor.getDocument(),0,0);
			i.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public void actionPerformed(ActionEvent arg0)
		{
		String com = arg0.getActionCommand();
		if (com.equals("GameInformationFrame.LOAD")) //$NON-NLS-1$
			{
			load_from_file();
			}
		if (com.equals("GameInformationFrame.SAVE")) //$NON-NLS-1$
			{
			save_to_file();
			}
		}
	}