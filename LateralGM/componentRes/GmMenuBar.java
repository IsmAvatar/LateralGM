package componentRes;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import mainRes.LGM;

public class GmMenuBar extends JMenuBar
	{
	private static final long serialVersionUID = 1L;
	public static GmMenu menu;

	public GmMenuBar()
		{
		menu = new GmMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		add(menu);

		menu.addItem("New",KeyEvent.VK_N,KeyEvent.VK_N,ActionEvent.CTRL_MASK);
		menu.addItem("Open...",KeyEvent.VK_O,KeyEvent.VK_O,ActionEvent.CTRL_MASK);
		menu.addItem("Save",KeyEvent.VK_S,KeyEvent.VK_S,ActionEvent.CTRL_MASK);
		menu.addItem("Save As...",KeyEvent.VK_A);
		menu.add(new JSeparator());
		JCheckBoxMenuItem check = new JCheckBoxMenuItem("Advanced Mode");
		check.setMnemonic(KeyEvent.VK_V);
		menu.add(check);
		menu.addItem("Preferences...",KeyEvent.VK_P);
		menu.add(new JSeparator());
		menu.addItem("Exit",KeyEvent.VK_X,KeyEvent.VK_F4,ActionEvent.ALT_MASK);

		menu = new GmMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		add(menu);

		GmMenu sub = new GmMenu("Insert");
		sub.setMnemonic(KeyEvent.VK_I);
		menu.add(sub);
		sub.addItem("Group",KeyEvent.VK_G);
		sub.add(new JSeparator());
		sub.addItem("Sprite",KeyEvent.VK_I);
		sub.addItem("Sound",KeyEvent.VK_M);
		sub.addItem("Background",KeyEvent.VK_B);
		sub.addItem("Path",KeyEvent.VK_P);
		sub.addItem("Script",KeyEvent.VK_S);
		sub.addItem("Font",KeyEvent.VK_F);
		sub.addItem("Timeline",KeyEvent.VK_T);
		sub.addItem("Object",KeyEvent.VK_O);
		sub.addItem("Room",KeyEvent.VK_R);

		sub = new GmMenu("Add");
		sub.setMnemonic(KeyEvent.VK_I);
		menu.add(sub);
		sub.addItem("Add Group",KeyEvent.VK_G);
		sub.add(new JSeparator());
		sub.addItem("Add Sprite",KeyEvent.VK_I);
		sub.addItem("Add Sound",KeyEvent.VK_M);
		sub.addItem("Add Background",KeyEvent.VK_B);
		sub.addItem("Add Path",KeyEvent.VK_P);
		sub.addItem("Add Script",KeyEvent.VK_S);
		sub.addItem("Add Font",KeyEvent.VK_F);
		sub.addItem("Add Timeline",KeyEvent.VK_T);
		sub.addItem("Add Object",KeyEvent.VK_O);
		sub.addItem("Add Room",KeyEvent.VK_R);

		menu.add(new JSeparator());
		menu.addItem("Rename",KeyEvent.VK_R,KeyEvent.VK_F2,0);
		menu.addItem("Delete",KeyEvent.VK_D,KeyEvent.VK_DELETE,ActionEvent.SHIFT_MASK);
		menu.addItem("Copy",KeyEvent.VK_C,KeyEvent.VK_INSERT,ActionEvent.ALT_MASK);
		menu.add(new JSeparator());
		menu.addItem("Properties",KeyEvent.VK_P,KeyEvent.VK_ENTER,ActionEvent.ALT_MASK);

		menu = new GmMenu("Resources");
		menu.setMnemonic(KeyEvent.VK_R);
		add(menu);

		menu.addItem("Verify Names",KeyEvent.VK_V);
		menu.addItem("Syntax Check",KeyEvent.VK_S);
		menu.add(new JSeparator());
		menu.addItem("Find...",KeyEvent.VK_F,KeyEvent.VK_F,ActionEvent.ALT_MASK + ActionEvent.CTRL_MASK);
		menu.addItem("Annotate",KeyEvent.VK_A);
		menu.add(new JSeparator());
		menu.addItem("Expand",KeyEvent.VK_E);
		menu.addItem("Collapse",KeyEvent.VK_C);

		menu = new GmMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		add(menu);

		menu.addItem("Manual",KeyEvent.VK_M,KeyEvent.VK_F1,0);
		menu.addItem("About",KeyEvent.VK_A);
		}

	public class GmMenu extends JMenu
		{
		private static final long serialVersionUID = 1L;

		public GmMenu(String s)
			{
			super(s);
			}

		public JMenuItem addItem(String name,int alt)
			{
			JMenuItem item = new JMenuItem(name.replaceAll("Add ","").replaceAll("Insert ",""),alt);
			item.setIcon(LGM.findIcon(name.replaceAll("Add ","").replaceAll("Insert ","") + ".png"));
			item.setActionCommand(name);
			item.addActionListener(LGM.listener);
			add(item);
			return item;
			}

		public JMenuItem addItem(String name,int alt,int shortcut,int control)
			{
			JMenuItem item = new JMenuItem(name,alt);
			item.setIcon(LGM.findIcon(name + ".png"));
			item.setActionCommand(name);
			item.setAccelerator(KeyStroke.getKeyStroke(shortcut,control));
			item.addActionListener(LGM.listener);
			add(item);
			return item;
			}
		}
	}