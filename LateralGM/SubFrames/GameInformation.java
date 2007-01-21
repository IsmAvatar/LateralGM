package SubFrames;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import mainRes.LGM;

public class GameInformation extends JInternalFrame
	{
	public GameInformation()
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
		
		}
	}
