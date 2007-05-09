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

/*
 * TODO:
 * Code updateResource, revertResource, and resourceChanged
 * Make the RTFEditor grab focus when the user enters a font size
 * Stolen from Font Family listener. Not sure what m_monitor was... 
 * 	String m_fontName = m_cbFonts.getSelectedItem().toString();
 * 	MutableAttributeSet attr = new SimpleAttributeSet();
 * 	StyleConstants.setFontFamily(attr,m_fontName);
 * 	// setAttributeSet(attr);
 * 	// m_monitor.grabFocus();
 */

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
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.lateralgm.components.CustomFileFilter;
import org.lateralgm.components.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.Resource;

public class GameInformationFrame extends JInternalFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private static JEditorPane editor;
	private static RTFEditorKit rtf = new RTFEditorKit();
	public static GameInformation gi = new GameInformation();
	private JComboBox m_cbFonts;
	private JSpinner m_sSizes;
	private JToggleButton m_tbBold;
	private JToggleButton m_tbItalic;
	private JToggleButton m_tbUnderline;

	// These prevent the Formatting Bar things from firing when the caret moves
	// because that would cause the selection to conform the text to the caret format
	private static boolean fFamilyChange = false;
	private static boolean fSizeChange = false;

	public GameInformationFrame()
		{
		super(Messages.getString("GameInformationFrame.TITLE"),true,true,true,true); //$NON-NLS-1$
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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
			but.setRequestFocusEnabled(false);
			but.setActionCommand("GameInformationFrame.SAVE"); //$NON-NLS-1$
			but.addActionListener(this);
			tool.add(but);

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			String[] fontNames = ge.getAvailableFontFamilyNames();
			tool.addSeparator();
			m_cbFonts = new JComboBox(fontNames);
			m_cbFonts.setRequestFocusEnabled(false);
			m_cbFonts.setMaximumSize(m_cbFonts.getPreferredSize());
			m_cbFonts.setEditable(true);
			ActionListener lst = new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						if (fFamilyChange)
							{
							fFamilyChange = false;
							return;
							}
						editor.grabFocus();
						setSelectionAttribute(StyleConstants.Family,m_cbFonts.getSelectedItem().toString());
						}
				};

			m_cbFonts.addActionListener(lst);
			tool.add(m_cbFonts);
			tool.addSeparator();
			m_sSizes = new JSpinner(new SpinnerNumberModel(12,1,100,1));
			m_sSizes.setRequestFocusEnabled(false);
			m_sSizes.setMaximumSize(m_sSizes.getPreferredSize());
			m_sSizes.addChangeListener(new ChangeListener()
				{
					public void stateChanged(ChangeEvent arg0)
						{
						if (fSizeChange)
							{
							fSizeChange = false;
							return;
							}
						editor.grabFocus();
						setSelectionAttribute(StyleConstants.Size,m_sSizes.getValue());
						}
				});
			tool.add(m_sSizes);
			tool.addSeparator();

			m_tbBold = new JToggleButton("B");
			m_tbBold.setRequestFocusEnabled(false);
			// m_tbBold.setFont(new java.awt.Font("Courier New",java.awt.Font.BOLD,10));
			lst = new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)
						{
						setSelectionAttribute(StyleConstants.Bold,m_tbBold.isSelected());
						}
				};
			m_tbBold.addActionListener(lst);
			tool.add(m_tbBold);
			m_tbItalic = new JToggleButton("I");
			m_tbItalic.setRequestFocusEnabled(false);
			// m_tbItalic.setFont(m_tbBold.getFont().deriveFont(java.awt.Font.ITALIC));
			lst = new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)
						{
						setSelectionAttribute(StyleConstants.Italic,m_tbItalic.isSelected());
						}
				};
			m_tbItalic.addActionListener(lst);
			tool.add(m_tbItalic);
			m_tbUnderline = new JToggleButton("U");
			m_tbUnderline.setRequestFocusEnabled(false);
			// m_tbUnderline = new JToggleButton("<html><u>U</u></html>");
			// m_tbUnderline.setFont(m_tbBold.getFont().deriveFont(java.awt.Font.PLAIN));
			// m_tbUnderline.setMaximumSize(m_tbBold.getSize());
			lst = new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)
						{
						setSelectionAttribute(StyleConstants.Underline,m_tbUnderline.isSelected());
						}
				};
			m_tbUnderline.addActionListener(lst);
			tool.add(m_tbUnderline);

			tool.addSeparator();
			but = new JButton(LGM.getIconForKey("GameInformationFrame.COLOR")); //$NON-NLS-1$
			but.setRequestFocusEnabled(false);
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
					fFamilyChange = true;
					fSizeChange = true;
					StyledDocument d = (StyledDocument) editor.getDocument();
					int dot = ce.getDot();
					if (ce.getMark() <= dot) dot--;
					AttributeSet as = d.getCharacterElement(dot).getAttributes();
					String f = StyleConstants.getFontFamily(as);
					int s = StyleConstants.getFontSize(as);
					boolean b = StyleConstants.isBold(as);
					boolean i = StyleConstants.isItalic(as);
					boolean u = StyleConstants.isUnderline(as);
					m_cbFonts.setSelectedItem(f);
					m_sSizes.setValue(s);
					m_tbBold.setSelected(b);
					m_tbItalic.setSelected(i);
					m_tbUnderline.setSelected(u);
					}
			});

		// This text could be big so add a scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(editor);
		topPanel.add(scroller,BorderLayout.CENTER);

		add_rtf(LGM.currentFile.GameInfo.GameInfoStr);
		}

	public void setSelectionAttribute(Object key, Object value)
		{
		StyledDocument sd = (StyledDocument) editor.getDocument();
		int a = editor.getSelectionStart();
		int b = editor.getSelectionEnd();
		if (a == b)
			{
			rtf.getInputAttributes().addAttribute(key, value);
			return;
			}
		SimpleAttributeSet sas = new SimpleAttributeSet();
		sas.addAttribute(key,value);
		sd.setCharacterAttributes(a,b - a,sas,false);
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

	public Object getUserObject()
		{
		for (int m = 0; m < LGM.root.getChildCount(); m++)
			{
			ResNode n = (ResNode) LGM.root.getChildAt(m);
			if (n.kind == Resource.GAMEINFO) return n.getUserObject();
			}
		return org.lateralgm.main.Messages.getString("LGM.GAMEINFO"); //$NON-NLS-1$
		}

	public void updateResource()
		{

		}

	public void revertResource()
		{

		}

	public boolean resourceChanged()
		{
		return true;
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

	protected void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING)
			{
			if (resourceChanged())
				{
				switch (JOptionPane.showConfirmDialog(LGM.frame,String.format(Messages
						.getString("ResourceFrame.KEEPCHANGES"),(String) getUserObject()),Messages //$NON-NLS-1$
						.getString("ResourceFrame.KEEPCHANGES_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION)) //$NON-NLS-1$
					{
					case 0: // yes
						updateResource();
						setVisible(false);
						break;
					case 1: // no
						revertResource();
						setVisible(false);
						break;
					}
				}
			else
				{
				updateResource();
				setVisible(false);
				}
			}
		super.fireInternalFrameEvent(id);
		}
	}