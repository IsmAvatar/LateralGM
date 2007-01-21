package SubFrames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.rtf.RTFEditorKit;

import mainRes.LGM;
import resourcesRes.GameInformation;

import componentRes.CustomFileFilter;

public class GameInformationFrame extends JInternalFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private static JEditorPane editor;
	private static RTFEditorKit rtf = new RTFEditorKit();
	public static GameInformation gi = new GameInformation();
	private JComboBox m_cbFonts;
	private JComboBox m_cbSizes;

	public GameInformationFrame()
		{
		super("Game Information",true,true,true,true);

		setSize(600,400);
		// Setup the Menu
		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();

			// Create File menu
			{
			JMenu Fmenu = new JMenu("File");
			menuBar.add(Fmenu);
			Fmenu.addActionListener(this);

			// Create a file menu items
			JMenuItem item = addItem("Load from a file");
			Fmenu.add(item);
			item = addItem("Save to a file");
			Fmenu.add(item);
			Fmenu.addSeparator();
			item = addItem("Options...");
			item.setEnabled(false);
			Fmenu.add(item);
			Fmenu.addSeparator();
			item = addItem("Print...");
			item.setEnabled(false);
			Fmenu.add(item);
			Fmenu.addSeparator();
			item = addItem("Close saving changes");
			Fmenu.add(item);
			}

			// Create Edit menu
			{
			JMenu Emenu = new JMenu("Edit");
			menuBar.add(Emenu);

			// Create a menu item
			JMenuItem item = addItem("Undo");
			Emenu.add(item);
			item.setEnabled(false);
			Emenu.addSeparator();
			item = addItem("Cut");
			Emenu.add(item);
			item.setEnabled(false);
			item = addItem("Copy");
			Emenu.add(item);
			item.setEnabled(false);
			item = addItem("Paste");
			Emenu.add(item);
			item.setEnabled(false);
			Emenu.addSeparator();
			item = addItem("Select All");
			Emenu.add(item);
			item.setEnabled(false);
			Emenu.addSeparator();
			item = addItem("Goto line");
			Emenu.add(item);
			item.setEnabled(false);
			}

			// Create Format menu
			{
			JMenu Fmenu = new JMenu("Format");
			menuBar.add(Fmenu);

			// Create a menu item
			JMenuItem item = addItem("Font...");
			// item.addActionListener(actionListener);
			Fmenu.add(item);
			}

		// Install the menu bar in the frame
		setJMenuBar(menuBar);

			// Setup the toolbar
			{
			JToolBar tool = new JToolBar();
			tool.setFloatable(false);
			add("North",tool);

			// Setup the buttons
			JButton but = new JButton(LGM.findIcon("save.png"));
			but.setActionCommand("Save");
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
			m_cbSizes = new JComboBox(new String[] { "8","9","10","11","12","14","16","18","20","22","24","26",
					"28","36","48","72" });
			m_cbSizes.setMaximumSize(m_cbSizes.getPreferredSize());
			m_cbSizes.setEditable(true);
			lst = new ActionListener()
				{
					private int m_fontSize;

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
						m_fontSize = fontSize;
						MutableAttributeSet attr = new SimpleAttributeSet();
						StyleConstants.setFontSize(attr,fontSize);
						// setAttributeSet(attr);
						// m_monitor.grabFocus();
						}
				};

			m_cbSizes.addActionListener(lst);
			tool.add(m_cbSizes);
			tool.addSeparator();
			but = new JButton(LGM.findIcon("Bcolor.png"));
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
		editor.setBackground(new Color(gi.BackgroundColor));

		// This text could be big so add a scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(editor);
		topPanel.add(scroller,BorderLayout.CENTER);

		add_rtf(gi.GameInfoStr);

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

	public JMenuItem addItem(String name)
		{
		JMenuItem item = new JMenuItem(name);
		item.setIcon(LGM.findIcon(name + ".png"));
		item.setActionCommand(name);
		item.addActionListener(this);
		add(item);
		return item;
		}

	public void load_from_file()
		{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(".rtf","Rich text Files"));
		fc.showOpenDialog(this);
		if (fc.getSelectedFile() != null)
			{
			String name = fc.getSelectedFile().getPath();

			try
				{
				FileInputStream i = new FileInputStream(new File(name));
				rtf.read(i,editor.getDocument(),0);
				i.close();
				}
			catch (IOException e)
				{
				}
			catch (BadLocationException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}

			}
		}

	public void save_to_file()
		{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(".rtf","Rich text Files"));
		fc.showSaveDialog(this);
		if (fc.getSelectedFile() != null)
			{
			String name = fc.getSelectedFile().getPath();
			if (!name.endsWith(".rtf")) name += ".rtf";
			try
				{
				FileOutputStream i = new FileOutputStream(new File(name));
				rtf.write(i,editor.getDocument(),0,0);
				i.close();

				}
			catch (IOException e)
				{
				}
			catch (BadLocationException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}

			}

		}

	public void actionPerformed(ActionEvent arg0)
		{
		String com = arg0.getActionCommand();
		System.out.println(com);
		if (com.equals("Load from a file"))
			{
			load_from_file();
			}
		if (com.equals("Save to a file"))
			{
			save_to_file();
			}
		}
	}