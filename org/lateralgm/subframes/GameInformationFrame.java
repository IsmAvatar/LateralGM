/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

/*
 * Stolen from Font Family listener. Not sure what m_monitor was... 
 * 	String m_fontName = m_cbFonts.getSelectedItem().toString();
 * 	MutableAttributeSet attr = new SimpleAttributeSet();
 * 	StyleConstants.setFontFamily(attr,m_fontName);
 * 	// setAttributeSet(attr);
 * 	// m_monitor.grabFocus();
 * 
 * TODO: Add left, right, center text alignment
 */

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalExclusionType;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameInformation.PGameInformation;

public class GameInformationFrame extends ResourceFrame<GameInformation,PGameInformation>
	{
	private static final long serialVersionUID = 1L;
	protected SettingsFrame settings;
	protected JEditorPane editor;
	private RTFEditorKit rtf = new RTFEditorKit();
	protected JMenuBar menubar;
	protected JToolBar toolbar;
	protected JComboBox<String> cbFonts;
	protected JSpinner sSizes;
	protected JToggleButton tbBold;
	protected JToggleButton tbItalic;
	protected JToggleButton tbUnderline;
	
	protected JToggleButton tbLeft;
	protected JToggleButton tbCenter;
	protected JToggleButton tbRight;
	
	private JMenuItem miBold;
	private JMenuItem miItalic;
	private JMenuItem miUnderline;
	
	private JMenuItem miLeft;
	private JMenuItem miCenter;
	private JMenuItem miRight;
	
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	private CustomFileChooser fc;
	protected Color fgColor;

	// These prevent the Formatting Bar things from firing when the caret moves
	// because that would cause the selection to conform the text to the caret format
	protected boolean fFamilyChange = false;
	protected boolean fSizeChange = false;

	protected boolean documentChanged = false;
	
	public class SettingsFrame extends JFrame
	{
	
	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	public SettingsFrame() {
		super();
  	setAlwaysOnTop(true);
  	setDefaultCloseOperation(HIDE_ON_CLOSE);
  	setLocationRelativeTo(LGM.getGameInfo());
  	
		setTitle(Messages.getString("GameInformationFrame.SETTINGS"));
		setIconImage(LGM.getIconForKey("GameInformationFrame.SETTINGS").getImage());
		setResizable(false);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		this.add(makeSettings());
		pack();
	}
	
	}

	private JMenuBar makeMenuBar()
		{
		JMenuBar menuBar = new JMenuBar();

		//File
		JMenu menu = new JMenu(Messages.getString("GameInformationFrame.MENU_FILE")); //$NON-NLS-1$
		menuBar.add(menu);
		menu.addActionListener(this);

		JMenuItem item = addItem("GameInformationFrame.LOAD"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		item = addItem("GameInformationFrame.FILESAVE"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		menu.addSeparator();
		item = addItem("GameInformationFrame.SETTINGS"); //$NON-NLS-1$
		menu.add(item);
		item = addItem("GameInformationFrame.PRINT"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem(Messages.getString("GameInformationFrame.CLOSESAVE"));
		item.setIcon(save.getIcon());
		item.setActionCommand("GameInformationFrame.CLOSESAVE");
		item.addActionListener(this);
		menu.add(item);

		//Edit
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
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		item = addItem("GameInformationFrame.COPY"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		item = addItem("GameInformationFrame.PASTE"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		menu.addSeparator();
		item = addItem("GameInformationFrame.SELECTALL"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		
		//Format
		menu = new JMenu(Messages.getString("GameInformationFrame.MENU_FORMAT")); //$NON-NLS-1$
		menuBar.add(menu);
		
		miBold = addItem("GameInformationFrame.BOLD"); //$NON-NLS-1$
		miBold.setActionCommand("GameInformationFrame.MENU_BOLD");
		menu.add(miBold);
		miItalic = addItem("GameInformationFrame.ITALIC"); //$NON-NLS-1$
		miItalic.setActionCommand("GameInformationFrame.MENU_ITALIC");
		menu.add(miItalic);
		miUnderline = addItem("GameInformationFrame.UNDERLINE"); //$NON-NLS-1$
		miUnderline.setActionCommand("GameInformationFrame.MENU_UNDERLINE");
		menu.add(miUnderline);
		menu.addSeparator();
		miLeft = addItem("GameInformationFrame.ALIGN_LEFT"); //$NON-NLS-1$
		miLeft.setActionCommand("GameInformationFrame.ALIGN_LEFT");
		miLeft.setSelected(true);
		menu.add(miLeft);
		miCenter = addItem("GameInformationFrame.ALIGN_CENTER"); //$NON-NLS-1$
		miCenter.setActionCommand("GameInformationFrame.ALIGN_CENTER");
		menu.add(miCenter);
		miRight = addItem("GameInformationFrame.ALIGN_RIGHT"); //$NON-NLS-1$
		miRight.setActionCommand("GameInformationFrame.ALIGN_RIGHT");
		menu.add(miRight);
		menu.addSeparator();
		item = addItem("GameInformationFrame.FONTCOLOR"); //$NON-NLS-1$
		menu.add(item);
		item = addItem("GameInformationFrame.COLOR"); //$NON-NLS-1$
		menu.add(item);

		return menuBar;
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);

		// Setup the buttons
		save.setRequestFocusEnabled(false);
		tool.add(save);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();
		tool.addSeparator();
		cbFonts = new JComboBox<String>(fontNames);
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
					setSelectionAttribute(StyleConstants.Size,sSizes.getValue());
					editor.grabFocus();
					}
			});
		tool.add(sSizes);
		tool.addSeparator();

		tbBold = addToggleButton("GameInformationFrame.BOLD"); //$NON-NLS-1$
		tbBold.setRequestFocusEnabled(false);
		tool.add(tbBold);
		tbItalic = addToggleButton("GameInformationFrame.ITALIC"); //$NON-NLS-1$
		tbItalic.setRequestFocusEnabled(false);
		tool.add(tbItalic);
		tbUnderline = addToggleButton("GameInformationFrame.UNDERLINE"); //$NON-NLS-1$
		tbUnderline.setRequestFocusEnabled(false);
		tool.add(tbUnderline);
		
		tool.addSeparator();
		
		tbLeft = addToggleButton("GameInformationFrame.ALIGN_LEFT"); //$NON-NLS-1$
		tbLeft.setRequestFocusEnabled(false);
		tbLeft.setSelected(true);
		tool.add(tbLeft);
		tbCenter = addToggleButton("GameInformationFrame.ALIGN_CENTER"); //$NON-NLS-1$
		tbCenter.setRequestFocusEnabled(false);
		tool.add(tbCenter);
		tbRight = addToggleButton("GameInformationFrame.ALIGN_RIGHT"); //$NON-NLS-1$
		tbRight.setRequestFocusEnabled(false);
		tool.add(tbRight);

		tool.addSeparator();
		
		JButton butFontColor = new JButton(LGM.getIconForKey("GameInformationFrame.FONTCOLOR")); //$NON-NLS-1$
		butFontColor.setRequestFocusEnabled(false);
		butFontColor.setActionCommand("GameInformationFrame.FONTCOLOR"); //$NON-NLS-1$
		butFontColor.addActionListener(this);
		butFontColor.setToolTipText(Messages.getString("GameInformationFrame.FONTCOLOR"));
		tool.add(butFontColor);
		JButton but = new JButton(LGM.getIconForKey("GameInformationFrame.COLOR")); //$NON-NLS-1$
		but.setRequestFocusEnabled(false);
		but.setActionCommand("GameInformationFrame.COLOR"); //$NON-NLS-1$
		but.addActionListener(this);
		but.setToolTipText(Messages.getString("GameInformationFrame.COLOR"));
		tool.add(but);
		
		tool.addSeparator();
		
		JButton button;
		
		button = new JButton(undoManager.getUndoAction());//$NON-NLS-1$
		button.setText("");
		button.setToolTipText(Messages.getString("GameInformationFrame.UNDO"));
		tool.add(button);
		button = new JButton(undoManager.getRedoAction());//$NON-NLS-1$
		button.setText("");
		button.setToolTipText(Messages.getString("GameInformationFrame.REDO"));
		tool.add(button);
		
		tool.addSeparator();
		
		button = addToolButton("GameInformationFrame.CUT");
		tool.add(button);
		button = addToolButton("GameInformationFrame.COPY");
		tool.add(button);
		button = addToolButton("GameInformationFrame.PASTE");
		tool.add(button);
		
		return tool;
		}

	public JTextField sTitle;
	public NumberField sX;
	public NumberField sY;
	public NumberField sWidth;
	public NumberField sHeight;
	public JCheckBox sShowBorder;
	public JCheckBox sAllowResize;
	public JCheckBox sAlwaysOnTop;
	public JCheckBox sPauseGame;
	public JCheckBox sEmbed;

	private JPanel makeSettings()
		{
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		JLabel lTitle = new JLabel(Messages.getString("GameInformationFrame.WINDOW_TITLE")); //$NON-NLS-1$
		sTitle = new JTextField();
		plf.make(sTitle.getDocument(),PGameInformation.FORM_CAPTION);

		JPanel position = new JPanel();
		position.setBorder(BorderFactory.createTitledBorder(
		/**/Messages.getString("GameInformationFrame.POSITION"))); //$NON-NLS-1$
		GroupLayout pl = new GroupLayout(position);
		position.setLayout(pl);
		pl.setAutoCreateGaps(true);
		pl.setAutoCreateContainerGaps(true);

		JLabel lX = new JLabel(Messages.getString("GameInformationFrame.X")); //$NON-NLS-1$
		JLabel lY = new JLabel(Messages.getString("GameInformationFrame.Y")); //$NON-NLS-1$
		JLabel lWidth = new JLabel(Messages.getString("GameInformationFrame.WIDTH")); //$NON-NLS-1$
		JLabel lHeight = new JLabel(Messages.getString("GameInformationFrame.HEIGHT")); //$NON-NLS-1$
		sX = new NumberField(0);
		sY = new NumberField(0);
		sWidth = new NumberField(0);
		sHeight = new NumberField(0);
		plf.make(sX,PGameInformation.LEFT);
		plf.make(sY,PGameInformation.TOP);
		plf.make(sWidth,PGameInformation.WIDTH);
		plf.make(sHeight,PGameInformation.HEIGHT);

		sShowBorder = new JCheckBox(Messages.getString("GameInformationFrame.SHOW_BORDER")); //$NON-NLS-1$
		sAllowResize = new JCheckBox(Messages.getString("GameInformationFrame.RESIZABLE")); //$NON-NLS-1$
		sAlwaysOnTop = new JCheckBox(Messages.getString("GameInformationFrame.ALWAYS_ON_TOP")); //$NON-NLS-1$
		sPauseGame = new JCheckBox(Messages.getString("GameInformationFrame.PAUSE")); //$NON-NLS-1$
		sEmbed = new JCheckBox(Messages.getString("GameInformationFrame.EMBED")); //$NON-NLS-1$

		plf.make(sShowBorder,PGameInformation.SHOW_BORDER);
		plf.make(sAllowResize,PGameInformation.ALLOW_RESIZE);
		plf.make(sAlwaysOnTop,PGameInformation.STAY_ON_TOP);
		plf.make(sPauseGame,PGameInformation.PAUSE_GAME);
		plf.make(sEmbed,PGameInformation.EMBED_GAME_WINDOW);

		pl.setHorizontalGroup(pl.createSequentialGroup()

		/**/.addGroup(pl.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(lX)
		/*		*/.addComponent(lWidth))
		/**/.addGroup(pl.createParallelGroup()
		/*		*/.addComponent(sX)
		/*		*/.addComponent(sWidth))
		/**/.addGroup(pl.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(lY)
		/*		*/.addComponent(lHeight))
		/**/.addGroup(pl.createParallelGroup()
		/*		*/.addComponent(sY)
		/*		*/.addComponent(sHeight)));

		pl.setVerticalGroup(pl.createSequentialGroup()
		/*		*/.addGroup(pl.createParallelGroup()
		/*				*/.addComponent(lX)
		/*				*/.addComponent(sX,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/*				*/.addComponent(lY)
		/*				*/.addComponent(sY,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/*		*/.addGroup(pl.createParallelGroup()
		/*				*/.addComponent(lWidth)
		/*				*/.addComponent(sWidth,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/*				*/.addComponent(lHeight)
		/*				*/.addComponent(sHeight,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)));

		JButton closeButton = new JButton(Messages.getString("GameInformationFrame.CLOSE"));
		closeButton.setActionCommand("GameInformationFrame.CLOSE");
		closeButton.addActionListener(this);
		
		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*		*/.addComponent(lTitle)
		/*		*/.addComponent(sTitle))
		/**/.addComponent(position)
		/**/.addComponent(sShowBorder)
		/**/.addComponent(sAllowResize)
		/**/.addComponent(sAlwaysOnTop)
		/**/.addComponent(sPauseGame)
		/**/.addComponent(sEmbed)
		/**/.addComponent(closeButton, Alignment.CENTER));

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(lTitle)
		/*		*/.addComponent(sTitle,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/**/.addComponent(position)
		/**/.addComponent(sShowBorder)
		/**/.addComponent(sAllowResize)
		/**/.addComponent(sAlwaysOnTop)
		/**/.addComponent(sPauseGame)
		/**/.addComponent(sEmbed)
		/**/.addComponent(closeButton));
		
		return p;
		}

	public GameInformationFrame(GameInformation res)
		{
		this(res,null);
		}

	public GameInformationFrame(GameInformation res, ResNode node)
		{
		super(res,node,Messages.getString("GameInformationFrame.TITLE"),true); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(725,500);

		menubar = makeMenuBar();
		setJMenuBar(menubar);
		toolbar = makeToolBar();
		add(toolbar,BorderLayout.NORTH);
		fgColor = Color.BLACK;

		editor = new JEditorPane();
		editor.setEditorKit(rtf);
		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(editor));

		addDocumentListeners();
		editor.addCaretListener(undoManager);

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
					fgColor = StyleConstants.getForeground(as);
					int s = StyleConstants.getFontSize(as);
					boolean b = StyleConstants.isBold(as);
					boolean i = StyleConstants.isItalic(as);
					boolean u = StyleConstants.isUnderline(as);
					cbFonts.setSelectedItem(f);
					sSizes.setValue(s);
					miBold.setSelected(b);
					tbBold.setSelected(b);
					
					tbItalic.setSelected(i);
					miItalic.setSelected(i);
					tbUnderline.setSelected(u);
					miUnderline.setSelected(u);
					
					setAlignmentOptions(StyleConstants.getAlignment(as));
					}
			});

		revertResource();

		this.add(new JScrollPane(editor), BorderLayout.CENTER);
		
		fc = new CustomFileChooser("/org/lateralgm","LAST_GAMEINFO_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setFileFilter(new CustomFileFilter(
				Messages.getString("GameInformationFrame.TYPE_RTF"),".rtf")); //$NON-NLS-1$ //$NON-NLS-2$

    // build popup menu
    final JPopupMenu popup = new JPopupMenu();
    JMenuItem item;
    
		item = new JMenuItem(undoManager.getUndoAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		item = new JMenuItem(undoManager.getRedoAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		popup.addSeparator();
		item = addItem("GameInformationFrame.CUT"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		item = addItem("GameInformationFrame.COPY"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		item = addItem("GameInformationFrame.PASTE"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		popup.addSeparator();
		item = addItem("GameInformationFrame.SELECTALL"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,KeyEvent.CTRL_DOWN_MASK));
		popup.add(item);
		
    editor.setComponentPopupMenu(popup);
	}

	private void addDocumentListeners()
	{
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
		editor.getDocument().addUndoableEditListener(undoManager);
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

	public JMenuItem addItem(String key)
	{
		JMenuItem item = new JMenuItem(Messages.getString(key));
		item.setIcon(LGM.getIconForKey(key));
		item.setActionCommand(key);
		item.addActionListener(this);
		return item;
	}
	
	public JButton addToolButton(String key)
	{
		JButton item = new JButton();
		item.setToolTipText(Messages.getString(key));
		item.setIcon(LGM.getIconForKey(key));
		item.setActionCommand(key);
		item.addActionListener(this);
		return item;
	}
	
	public JToggleButton addToggleButton(String key)
	{
		JToggleButton item = new JToggleButton();
		item.setToolTipText(Messages.getString(key));
		item.setIcon(LGM.getIconForKey(key));
		item.setActionCommand(key);
		item.addActionListener(this);
		return item;
	}

	public void loadFromFile()
	{
		fc.setDialogTitle(Messages.getString("GameInformationFrame.LOAD_TITLE")); //$NON-NLS-1$
		while (true)
		{
			if (fc.showOpenDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			if (fc.getSelectedFile().exists()) break;
			JOptionPane.showMessageDialog(null,
					fc.getSelectedFile().getName() + Messages.getString("GameInformationFrame.FILE_MISSING"), //$NON-NLS-1$
					Messages.getString("GameInformationFrame.LOAD_TITLE"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			}
		try
			{
			FileInputStream i = new FileInputStream(fc.getSelectedFile());
			editor.setText(""); //$NON-NLS-1$
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
			FileOutputStream out = new FileOutputStream(new File(name));
			StyledDocument doc = (StyledDocument)editor.getDocument();
			rtf.write(out,doc, doc.getStartPosition().getOffset(), doc.getLength());
			out.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public Object getUserObject()
		{
		if (node != null) return node.getUserObject();
		for (int m = 0; m < LGM.root.getChildCount(); m++)
			{
			ResNode n = (ResNode) LGM.root.getChildAt(m);
			if (n.kind == GameInformation.class) return n.getUserObject();
			}
		return Messages.getString("LGM.GMI"); //$NON-NLS-1$
		}
	
	public void setAlignmentOptions(int alignment) {
		miLeft.setSelected(alignment == StyleConstants.ALIGN_LEFT);
		miCenter.setSelected(alignment == StyleConstants.ALIGN_CENTER);
		miRight.setSelected(alignment == StyleConstants.ALIGN_RIGHT);
		tbLeft.setSelected(alignment == StyleConstants.ALIGN_LEFT);
		tbCenter.setSelected(alignment == StyleConstants.ALIGN_CENTER);
		tbRight.setSelected(alignment == StyleConstants.ALIGN_RIGHT);
	}
	
	public void setSelectionAlignment(int alignment) {
		setAlignmentOptions(alignment);

		StyledDocument sd = (StyledDocument) editor.getDocument();
		int a = editor.getSelectionStart();
		int b = editor.getSelectionEnd();
		if (a == b)
			{
			rtf.getInputAttributes().addAttribute(StyleConstants.Alignment,alignment);
			return;
			}
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setAlignment(sas,alignment);
		sd.setParagraphAttributes(a,b - a,sas,false);
		return;
	}

	public void actionPerformed(ActionEvent ev)
		{
		super.actionPerformed(ev);
		String com = ev.getActionCommand();
		
		if (com.equals("GameInformationFrame.LOAD")) //$NON-NLS-1$
			{
			loadFromFile();
			}
		else if (com.equals("GameInformationFrame.PRINT")) //$NON-NLS-1$
		{
	    //TODO: Make the fucker actually print
	    PrinterJob pj = PrinterJob.getPrinterJob();
      if (pj.printDialog()) {
          try {
            pj.print();
          }
          catch (PrinterException exc) {
            System.out.println(exc);
          }
       }   
			return;
		}
		else if (com.equals("GameInformationFrame.SETTINGS")) {
			if (settings == null) {
				settings = new SettingsFrame();

		
				JPanel settingsPanel = makeSettings();
				settings.add(settingsPanel);

			}
			settings.setVisible(true);
		}
		else if (com.equals("GameInformationFrame.FILESAVE")) //$NON-NLS-1$
			{
			saveToFile();
			return;
			}
		else if (com.equals("GameInformationFrame.FONTCOLOR")) //$NON-NLS-1$
			{
			String colorStr = Messages.getString("GameInformationFrame.FONTCOLOR"); //$NON-NLS-1$
			Color c = JColorChooser.showDialog(this,colorStr,fgColor);
			if (c != null)
				{
				fgColor = c;
				setSelectionAttribute(StyleConstants.Foreground, c);
				}
			return;
			}
		else if (com.equals("GameInformationFrame.BOLD")) //$NON-NLS-1$
			{
			miBold.setSelected(tbBold.isSelected());
			setSelectionAttribute(StyleConstants.Bold,tbBold.isSelected());
			return;
			}
		else if (com.equals("GameInformationFrame.ITALIC")) //$NON-NLS-1$
			{
			miItalic.setSelected(tbItalic.isSelected());
			setSelectionAttribute(StyleConstants.Italic,tbItalic.isSelected());
			return;
			}
		else if (com.equals("GameInformationFrame.UNDERLINE")) //$NON-NLS-1$
			{
			miUnderline.setSelected(tbUnderline.isSelected());
			setSelectionAttribute(StyleConstants.Underline,tbUnderline.isSelected());
			return;
			}
		else if (com.equals("GameInformationFrame.MENU_BOLD")) //$NON-NLS-1$
			{
			miBold.setSelected(!miBold.isSelected());
			tbBold.setSelected(miBold.isSelected());
			setSelectionAttribute(StyleConstants.Bold,miBold.isSelected());
			return;
			}
		else if (com.equals("GameInformationFrame.MENU_ITALIC")) //$NON-NLS-1$
			{
			miItalic.setSelected(!miItalic.isSelected());
			tbItalic.setSelected(miItalic.isSelected());
			setSelectionAttribute(StyleConstants.Italic,miItalic.isSelected());
			return;
			}
		else if (com.equals("GameInformationFrame.MENU_UNDERLINE")) //$NON-NLS-1$
			{
			miUnderline.setSelected(!miUnderline.isSelected());
			tbUnderline.setSelected(miUnderline.isSelected());
			setSelectionAttribute(StyleConstants.Underline,miUnderline.isSelected());
			return;
			}
		else if (com.equals("GameInformationFrame.ALIGN_LEFT")) //$NON-NLS-1$
			{
			setSelectionAlignment(StyleConstants.ALIGN_LEFT);
			return;
			}
		else if (com.equals("GameInformationFrame.ALIGN_CENTER")) //$NON-NLS-1$
			{
			setSelectionAlignment(StyleConstants.ALIGN_CENTER);
			return;
			}
		else if (com.equals("GameInformationFrame.ALIGN_RIGHT")) //$NON-NLS-1$
			{
			setSelectionAlignment(StyleConstants.ALIGN_RIGHT);
			return;
			}
		else if (com.equals("GameInformationFrame.COLOR")) //$NON-NLS-1$
			{
			String colorStr = Messages.getString("GameInformationFrame.COLOR"); //$NON-NLS-1$
			Color c = JColorChooser.showDialog(this,colorStr,editor.getBackground());
			if (c != null) setEditorBackground(c);
			return;
			}
		else if (com.equals("GameInformationFrame.CUT")) //$NON-NLS-1$
			{
			editor.cut();
			return;
			}
		else if (com.equals("GameInformationFrame.COPY")) //$NON-NLS-1$
			{
			editor.copy();
			return;
			}
		else if (com.equals("GameInformationFrame.PASTE")) //$NON-NLS-1$
			{
			editor.paste();
			return;
			}
		else if (com.equals("GameInformationFrame.SELECTALL")) //$NON-NLS-1$
			{
			editor.selectAll();
			return;
			}		
		else if (com.equals("GameInformationFrame.CLOSE")) //$NON-NLS-1$
			{
				settings.setVisible(false);
				return;
			}
		}

	public void commitChanges()
		{
		res.put(PGameInformation.BACKGROUND_COLOR,editor.getBackground());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
			{
			StyledDocument doc = (StyledDocument)editor.getDocument();
			rtf.write(baos,doc, doc.getStartPosition().getOffset(), doc.getLength());
			res.put(PGameInformation.TEXT,baos.toString("UTF-8")); //$NON-NLS-1$
			}
		catch (IOException e)
			{ //Nevermind
			e.printStackTrace();
			}
		catch (BadLocationException e)
			{ //Should never happen, but we have to catch this anyways
			e.printStackTrace();
			}
			LGM.currentFile.gameInfo = res;
		}

	public void setComponents(GameInformation info)
		{
		setEditorBackground((Color) res.get(PGameInformation.BACKGROUND_COLOR));
		editor.setText(null);
		try
			{
			rtf.read(
					new ByteArrayInputStream(((String) res.get(PGameInformation.TEXT)).getBytes("UTF-8")), //$NON-NLS-1$
					editor.getDocument(),0);
			editor.setCaretPosition(0);
			}
		catch (IOException e)
			{ //Nevermind
			}
		catch (BadLocationException e)
			{ //Should never happen, but we have to catch this anyways
			}
		}

	@Override
	public String getConfirmationName()
		{
		return (String) getUserObject();
		}

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		return !res.properties.equals(resOriginal.properties);
		}

	@Override
	public void revertResource()
		{
		res.properties.putAll(resOriginal.properties);
		setComponents(res);
		}
	}
