/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
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
 * TODO: Add font color functionality
 */

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.ExceptionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.GroupLayout.Alignment;
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
import org.lateralgm.components.mdi.RevertableMDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;

public class GameInformationFrame extends RevertableMDIFrame implements ActionListener,
		ExceptionListener
	{
	private static final long serialVersionUID = 1L;
	protected GameInformation res, resOriginal;
	protected final PropertyLinkFactory<PGameInformation> plf;
	protected JTabbedPane tabs;
	protected JEditorPane editor;
	private RTFEditorKit rtf = new RTFEditorKit();
	protected JMenuBar menubar;
	protected JToolBar toolbar;
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
		item = addItem("GameInformationFrame.CLOSESAVE"); //$NON-NLS-1$
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

		return menuBar;
		}

	protected JButton save;

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);

		// Setup the buttons
		save = new JButton(LGM.getIconForKey("GameInformationFrame.CLOSESAVE")); //$NON-NLS-1$
		save.setRequestFocusEnabled(false);
		save.setActionCommand("GameInformationFrame.CLOSESAVE"); //$NON-NLS-1$
		save.addActionListener(this);
		tool.add(save);

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
					setSelectionAttribute(StyleConstants.Size,sSizes.getValue());
					editor.grabFocus();
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
		JButton but = new JButton(LGM.getIconForKey("GameInformationFrame.COLOR")); //$NON-NLS-1$
		but.setRequestFocusEnabled(false);
		but.setActionCommand("GameInformationFrame.COLOR"); //$NON-NLS-1$
		but.addActionListener(this);
		tool.add(but);
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
		plf.make(sEmbed,PGameInformation.MIMIC_GAME_WINDOW);

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

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*		*/.addComponent(lTitle)
		/*		*/.addComponent(sTitle))
		/**/.addComponent(position)
		/**/.addComponent(sShowBorder)
		/**/.addComponent(sAllowResize)
		/**/.addComponent(sAlwaysOnTop)
		/**/.addComponent(sPauseGame)
		/**/.addComponent(sEmbed));

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(lTitle)
		/*		*/.addComponent(sTitle,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/**/.addComponent(position)
		/**/.addComponent(sShowBorder)
		/**/.addComponent(sAllowResize)
		/**/.addComponent(sAlwaysOnTop)
		/**/.addComponent(sPauseGame)
		/**/.addComponent(sEmbed));
		return p;
		}

	public GameInformationFrame(GameInformation res)
		{
		super(Messages.getString("GameInformationFrame.TITLE"),true); //$NON-NLS-1$
		plf = new PropertyLinkFactory<PGameInformation>(res.properties,this);
		this.res = res;
		resOriginal = res.clone();
		setFrameIcon(LGM.getIconForKey("GameInformationFrame.INFO")); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(600,400);

		menubar = makeMenuBar();
		setJMenuBar(menubar);
		toolbar = makeToolBar();
		add(toolbar,BorderLayout.NORTH);

		tabs = new JTabbedPane();
		add(tabs,BorderLayout.CENTER);
		tabs.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
					{
					boolean enabled = tabs.getSelectedIndex() == 0;
					if (enabled) editor.requestFocusInWindow();
					JMenuBar mb = getJMenuBar();
					for (int i = 0; i < mb.getComponentCount(); i++)
						{
						JMenu m = (JMenu) mb.getComponent(i);
						if (!m.getText().equals(Messages.getString("GameInformationFrame.MENU_FILE"))) //$NON-NLS-1$
							m.setEnabled(enabled);
						}
					for (int i = 0; i < toolbar.getComponentCount(); i++)
						{
						Component c = toolbar.getComponent(i);
						if (c != save) c.setEnabled(enabled);
						}
					}
			});

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

		tabs.addTab(Messages.getString("GameInformationFrame.TAB_INFO"), //$NON-NLS-1$
				/**/null,new JScrollPane(editor),Messages.getString("GameInformationFrame.HINT_INFO")); //$NON-NLS-1$ 
		tabs.addTab(Messages.getString("GameInformationFrame.TAB_SETTINGS"), //$NON-NLS-1$
				/**/null,makeSettings(),Messages.getString("GameInformationFrame.HINT_SETTINGS")); //$NON-NLS-1$ 
		revertResource();

		fc = new CustomFileChooser("/org/lateralgm","LAST_GAMEINFO_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setFileFilter(new CustomFileFilter(
				Messages.getString("GameInformationFrame.TYPE_RTF"),".rtf")); //$NON-NLS-1$ //$NON-NLS-2$
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
			if (n.kind == Resource.Kind.GAMEINFO) return n.getUserObject();
			}
		return Messages.getString("LGM.GAMEINFO"); //$NON-NLS-1$
		}

	public void actionPerformed(ActionEvent arg0)
		{
		String com = arg0.getActionCommand();
		if (com.equals("GameInformationFrame.LOAD")) //$NON-NLS-1$
			{
			tabs.setSelectedIndex(0);
			loadFromFile();
			}
		if (com.equals("GameInformationFrame.CLOSESAVE")) //$NON-NLS-1$
			{
			updateResource();
			close();
			return;
			}
		if (com.equals("GameInformationFrame.FILESAVE")) //$NON-NLS-1$
			{
			tabs.setSelectedIndex(0);
			saveToFile();
			return;
			}
		if (com.equals("GameInformationFrame.COLOR")) //$NON-NLS-1$
			{
			String colorStr = Messages.getString("GameInformationFrame.COLOR"); //$NON-NLS-1$
			Color c = JColorChooser.showDialog(this,colorStr,editor.getBackground());
			if (c != null) setEditorBackground(c);
			return;
			}
		if (com.equals("GameInformationFrame.CUT")) //$NON-NLS-1$
			{
			editor.cut();
			return;
			}
		if (com.equals("GameInformationFrame.COPY")) //$NON-NLS-1$
			{
			editor.copy();
			return;
			}
		if (com.equals("GameInformationFrame.PASTE")) //$NON-NLS-1$
			{
			editor.paste();
			return;
			}
		if (com.equals("GameInformationFrame.SELECTALL")) //$NON-NLS-1$
			{
			editor.selectAll();
			return;
			}
		}

	public void commitChanges()
		{
		res.put(PGameInformation.BACKGROUND_COLOR,editor.getBackground());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
			{
			rtf.write(baos,editor.getDocument(),0,0);
			res.put(PGameInformation.TEXT,baos.toString("UTF-8")); //$NON-NLS-1$
			}
		catch (IOException e)
			{
			}
		catch (BadLocationException e)
			{
			}
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
			}
		catch (IOException e)
			{
			}
		catch (BadLocationException e)
			{
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

	@Override
	public void updateResource()
		{
		commitChanges();
		resOriginal = res.clone();
		}

	public void exceptionThrown(Exception e)
		{
		e.printStackTrace();
		}
	}
