package SubFrames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
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
import javax.swing.text.rtf.RTFEditorKit;

import componentRes.CustomFileFilter;

import resourcesRes.GameInformation;

import mainRes.LGM;

public class GameInformationFrame extends JInternalFrame implements ActionListener
	{
	private static JEditorPane editor;

	private static RTFEditorKit rtf = new RTFEditorKit();

	public GameInformationFrame()
		{
		super("Game Information",true,true,true,true);

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
			JMenu Fmenu = new JMenu("Edit");
			menuBar.add(Fmenu);

			// Create a menu item
			JMenuItem item = new JMenuItem("Undo");
			// item.addActionListener(actionListener);
			Fmenu.add(item);
			}

			// Create Format menu
			{
			JMenu Fmenu = new JMenu("Format");
			menuBar.add(Fmenu);

			// Create a menu item
			JMenuItem item = new JMenuItem("Font...");
			// item.addActionListener(actionListener);
			Fmenu.add(item);
			}

		// Install the menu bar in the frame
		setJMenuBar(menuBar);

		// Setup the toolbar
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		add("North",tool);

		// Setup the buttons
		JButton but = new JButton(LGM.findIcon("save.png"));
		but.setActionCommand("Save");
		// but.addActionListener(listener);
		tool.add(but);

		// Create an RTF editor window
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel,BorderLayout.CENTER);

		editor = new JEditorPane();
		editor.setEditable(false);
		editor.setEditorKit(rtf);
		// editor.setBackground(Color.);

		// This text could be big so add a scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(editor);
		topPanel.add(scroller,BorderLayout.CENTER);

		GameInformation gi = new GameInformation();

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

	public void save_to_file()
		{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new CustomFileFilter(".rtf","Rich text Files"));
		fc.showSaveDialog(this);
		if (fc.getSelectedFile() != null)
			{
			String name = fc.getSelectedFile().getPath();
			if (name.endsWith("")) name += ".rtf";
			try
				{
				BufferedWriter out = new BufferedWriter(new FileWriter(name));
				out.write(rtf.toString());
				out.close();
				}
			catch (IOException e)
				{
				}

			}

		}

	public void actionPerformed(ActionEvent arg0)
		{
		String com = arg0.getActionCommand();
		System.out.println(com);
		if (com.equals("Load from a file"))
			{

			}
		if (com.equals("Save to a file"))
			{
			save_to_file();
			}
		}

	}
