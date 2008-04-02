/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

/*
 * TODO: Make the RTFEditor grab focus when the user enters a font size
 * Stolen from Font Family listener. Not sure what m_monitor was... 
 * 	String m_fontName = m_cbFonts.getSelectedItem().toString();
 * 	MutableAttributeSet attr = new SimpleAttributeSet();
 * 	StyleConstants.setFontFamily(attr,m_fontName);
 * 	// setAttributeSet(attr);
 * 	// m_monitor.grabFocus();
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

public class GameInformationFrame extends MDIFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	protected static JEditorPane editor;
	private static RTFEditorKit rtf = new RTFEditorKit();
	protected JComboBox cbFonts;
	protected JSpinner sSizes;
	protected JToggleButton tbBold;
	protected JToggleButton tbItalic;
	protected JToggleButton tbUnderline;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	private CustomFileChooser fc;

	// These prevent the Formatting Bar things from firing when the caret moves
	// because that would cause the selection to conform the text to the caret format
	protected boolean fFamilyChange = false;
	protected boolean fSizeChange = false;

	protected boolean documentChanged = false;

	private JMenuBar makeMenuBar()
		{
		JMenuBar menuBar = new JMenuBar();

		// Create FILE menu
		JMenu menu = new JMenu(Messages.getString("GameInformationFrame.MENU_FILE")); //$NON-NLS-1$
		menuBar.add(menu);
		menu.addActionListener(this);

		JMenuItem item = addItem("GameInformationFrame.LOAD"); //$NON-NLS-1$
		menu.add(item);
		item = addItem("GameInformationFrame.FILESAVE"); //$NON-NLS-1$
		menu.add(item);
		menu.addSeparator();
		item = addItem("GameInformationFrame.OPTIONS"); //$NON-NLS-1$
		item.setEnabled(false);
		menu.add(item);
		menu.addSeparator();
		item = addItem("GameInformationFrame.PRINT"); //$NON-NLS-1$
		item.setEnabled(false);
		menu.add(item);
		menu.addSeparator();
		item = addItem("GameInformationFrame.CLOSESAVE"); //$NON-NLS-1$
		menu.add(item);

		// Create EDIT menu
		menu = new JMenu(Messages.getString("GameInformationFrame.MENU_EDIT")); //$NON-NLS-1$
		menuBar.add(menu);

		item = new JMenuItem(undoManager.getUndoAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		item = new JMenuItem(undoManager.getRedoAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		menu.addSeparator();
		item = addItem("GameInformationFrame.CUT"); //$NON-NLS-1$
		menu.add(item);
		item.setEnabled(false);
		item = addItem("GameInformationFrame.COPY"); //$NON-NLS-1$
		menu.add(item);
		item.setEnabled(false);
		item = addItem("GameInformationFrame.PASTE"); //$NON-NLS-1$
		menu.add(item);
		item.setEnabled(false);
		menu.addSeparator();
		item = addItem("GameInformationFrame.SELECTALL"); //$NON-NLS-1$
		menu.add(item);
		item.setEnabled(false);
		menu.addSeparator();
		item = addItem("GameInformationFrame.GOTO"); //$NON-NLS-1$
		menu.add(item);
		item.setEnabled(false);

		// Create FORMAT menu
		menu = new JMenu(Messages.getString("GameInformationFrame.MENU_FORMAT")); //$NON-NLS-1$
		menuBar.add(menu);

		// Create a menu item
		item = addItem("GameInformationFrame.FONT"); //$NON-NLS-1$
		// item.addActionListener(actionListener);
		menu.add(item);

		return menuBar;
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);

		// Setup the buttons
		JButton but = new JButton(LGM.getIconForKey("GameInformationFrame.CLOSESAVE")); //$NON-NLS-1$
		but.setRequestFocusEnabled(false);
		but.setActionCommand("GameInformationFrame.CLOSESAVE"); //$NON-NLS-1$
		but.addActionListener(this);
		tool.add(but);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();
		tool.addSeparator();
		cbFonts = new JComboBox(fontNames);
		cbFonts.setRequestFocusEnabled(false);
		cbFonts.setMaximumSize(cbFonts.getPreferredSize());
		cbFonts.setEditable(true);
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
					setSelectionAttribute(StyleConstants.Family,cbFonts.getSelectedItem().toString());
					}
			};

		cbFonts.addActionListener(lst);
		tool.add(cbFonts);
		tool.addSeparator();
		sSizes = new JSpinner(new SpinnerNumberModel(12,1,100,1));
		sSizes.setRequestFocusEnabled(false);
		sSizes.setMaximumSize(sSizes.getPreferredSize());
		sSizes.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent arg0)
					{
					if (fSizeChange)
						{
						fSizeChange = false;
						return;
						}
					editor.grabFocus();
					setSelectionAttribute(StyleConstants.Size,sSizes.getValue());
					}
			});
		tool.add(sSizes);
		tool.addSeparator();

		tbBold = new JToggleButton(LGM.getIconForKey("GameInformationFrame.BOLD")); //$NON-NLS-1$
		tbBold.setRequestFocusEnabled(false);
		lst = new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
					{
					setSelectionAttribute(StyleConstants.Bold,tbBold.isSelected());
					}
			};
		tbBold.addActionListener(lst);
		tool.add(tbBold);
		tbItalic = new JToggleButton(LGM.getIconForKey("GameInformationFrame.ITALIC")); //$NON-NLS-1$
		tbItalic.setRequestFocusEnabled(false);
		lst = new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
					{
					setSelectionAttribute(StyleConstants.Italic,tbItalic.isSelected());
					}
			};
		tbItalic.addActionListener(lst);
		tool.add(tbItalic);
		tbUnderline = new JToggleButton(LGM.getIconForKey("GameInformationFrame.UNDERLINED")); //$NON-NLS-1$
		tbUnderline.setRequestFocusEnabled(false);
		lst = new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
					{
					setSelectionAttribute(StyleConstants.Underline,tbUnderline.isSelected());
					}
			};
		tbUnderline.addActionListener(lst);
		tool.add(tbUnderline);

		tool.addSeparator();
		but = new JButton(LGM.getIconForKey("GameInformationFrame.COLOR")); //$NON-NLS-1$
		but.setRequestFocusEnabled(false);
		but.setActionCommand("GameInformationFrame.COLOR"); //$NON-NLS-1$
		but.addActionListener(this);
		tool.add(but);
		return tool;
		}

	public GameInformationFrame()
		{
		super(Messages.getString("GameInformationFrame.TITLE"),true,true,true,true); //$NON-NLS-1$
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(600,400);
		setFrameIcon(LGM.getIconForKey("GameInformationFrame.INFO")); //$NON-NLS-1$

		setJMenuBar(makeMenuBar());
		add("North",makeToolBar()); //$NON-NLS-1$

		// Create an RTF editor window
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel,BorderLayout.CENTER);

		editor = new JEditorPane();
		// editor.setEditable(false);
		editor.setEditorKit(rtf);

		editor.getDocument().addDocumentListener(new DocumentListener()
			{
				public void removeUpdate(DocumentEvent e)
					{
					documentChanged = true;
					}

				public void changedUpdate(DocumentEvent e)
					{
					documentChanged = true;
					}

				public void insertUpdate(DocumentEvent e)
					{
					documentChanged = true;
					}
			});
		editor.addCaretListener(undoManager);
		editor.getDocument().addUndoableEditListener(undoManager);
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
					cbFonts.setSelectedItem(f);
					sSizes.setValue(s);
					tbBold.setSelected(b);
					tbItalic.setSelected(i);
					tbUnderline.setSelected(u);
					}
			});

		// This text could be big so add a scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(editor);
		topPanel.add(scroller,BorderLayout.CENTER);
		revertResource();

		fc = new CustomFileChooser("/org/lateralgm","LAST_GAMEINFO_DIR");
		fc.setFileFilter(new CustomFileFilter(".rtf", //$NON-NLS-1$
				Messages.getString("GameInformationFrame.TYPE_RTF"))); //$NON-NLS-1$
		}

	public void setEditorBackground(Color c)
		{
		editor.setBackground(c);
		Color sc = new Color(c.getRed() > 127 ? 0 : 255,c.getGreen() > 127 ? 0 : 255,
				c.getBlue() > 127 ? 0 : 255);
		editor.setSelectedTextColor(c);
		editor.setSelectionColor(sc);
		Color cc = new Color((c.getRed() + sc.getRed()) / 2,(c.getGreen() + sc.getGreen()) / 2,
				(c.getBlue() + sc.getBlue()) / 2);
		editor.setCaretColor(cc);
		documentChanged = true;
		}

	public void setSelectionAttribute(Object key, Object value)
		{
		StyledDocument sd = (StyledDocument) editor.getDocument();
		int a = editor.getSelectionStart();
		int b = editor.getSelectionEnd();
		if (a == b)
			{
			rtf.getInputAttributes().addAttribute(key,value);
			return;
			}
		SimpleAttributeSet sas = new SimpleAttributeSet();
		sas.addAttribute(key,value);
		sd.setCharacterAttributes(a,b - a,sas,false);
		}

	public static void addRTF(String str)
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

	public void loadFromFile()
		{
		fc.setDialogTitle(Messages.getString("GameInformationFrame.LOAD_TITLE")); //$NON-NLS-1$
		while (true)
			{
			if (fc.showOpenDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			if (fc.getSelectedFile().exists()) break;
			JOptionPane.showMessageDialog(null,fc.getSelectedFile().getName()
					+ Messages.getString("SoundFrame.FILE_MISSING"), //$NON-NLS-1$
					Messages.getString("GameInformationFrame.LOAD_TITLE"), //$NON-NLS-1$
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

	public void saveToFile()
		{
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
		return Messages.getString("LGM.GAMEINFO"); //$NON-NLS-1$
		}

	public void updateResource()
		{
		LGM.currentFile.gameInfo.backgroundColor = editor.getBackground();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
			{
			rtf.write(baos,editor.getDocument(),0,0);
			LGM.currentFile.gameInfo.gameInfoStr = baos.toString("UTF-8");
			}
		catch (IOException e)
			{
			}
		catch (BadLocationException e)
			{
			}
		documentChanged = false;
		}

	public void revertResource()
		{
		setEditorBackground(LGM.currentFile.gameInfo.backgroundColor);
		editor.setText(LGM.currentFile.gameInfo.gameInfoStr);
		undoManager.die();
		undoManager.updateActions();
		documentChanged = false;
		}

	public boolean resourceChanged()
		{
		return documentChanged;
		}

	public void actionPerformed(ActionEvent arg0)
		{
		String com = arg0.getActionCommand();
		if (com.equals("GameInformationFrame.LOAD")) //$NON-NLS-1$
			{
			loadFromFile();
			}
		if (com.equals("GameInformationFrame.CLOSESAVE")) //$NON-NLS-1$
			{
			updateResource();
			setVisible(false);
			}
		if (com.equals("GameInformationFrame.FILESAVE")) //$NON-NLS-1$
			{
			saveToFile();
			}
		if (com.equals("GameInformationFrame.COLOR")) //$NON-NLS-1$
			{
			String colorStr = Messages.getString("GameInformationFrame.COLOR"); //$NON-NLS-1$
			Color c = JColorChooser.showDialog(this,colorStr,editor.getBackground());
			if (c != null) setEditorBackground(c);
			}
		}

	protected void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING)
			{
			if (resourceChanged())
				{
				int ret = JOptionPane.showConfirmDialog(LGM.frame,Messages.format(
						"ResourceFrame.KEEPCHANGES",(String) getUserObject()), //$NON-NLS-1$
						Messages.getString("ResourceFrame.KEEPCHANGES_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION); //$NON-NLS-1$
				if (ret == JOptionPane.YES_OPTION)
					{
					updateResource();
					setVisible(false);
					}
				else if (ret == JOptionPane.NO_OPTION)
					{
					revertResource();
					setVisible(false);
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
