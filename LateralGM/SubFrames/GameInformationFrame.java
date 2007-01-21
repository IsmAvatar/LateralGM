package SubFrames;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;

import resourcesRes.GameInformation;

import mainRes.LGM;

public class GameInformationFrame extends JInternalFrame
	{
	private JEditorPane editor;

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

			// Create a menu item
			JMenuItem item = new JMenuItem("Load from a file");
			// item.addActionListener(actionListener);
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
		RTFEditorKit rtf = new RTFEditorKit();
		editor = new JEditorPane();
		editor.setEditable(false);
		editor.setEditorKit(rtf);
		// editor.setBackground(Color.);

		// This text could be big so add a scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(editor);
		topPanel.add(scroller,BorderLayout.CENTER);

		GameInformation gi = new GameInformation();
		try
			{
			rtf.read(new ByteArrayInputStream(gi.GameInfoStr.getBytes()),editor.getDocument(),0);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		// gi.GameInfoStr;

		}
	}
