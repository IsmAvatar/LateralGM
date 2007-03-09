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
		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_FILE")); //$NON-NLS-1$
		menu.setMnemonic(KeyEvent.VK_F);
		add(menu);

		menu.addItem("GmMenuBar.NEW",KeyEvent.VK_N,KeyEvent.VK_N,ActionEvent.CTRL_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.OPEN",KeyEvent.VK_O,KeyEvent.VK_O,ActionEvent.CTRL_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.SAVE",KeyEvent.VK_S,KeyEvent.VK_S,ActionEvent.CTRL_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.SAVEAS",KeyEvent.VK_A); //$NON-NLS-1$
		menu.add(new JSeparator());
		JCheckBoxMenuItem check = new JCheckBoxMenuItem(Messages.getString("GmMenuBar.ADVANCED")); //$NON-NLS-1$
		check.setMnemonic(KeyEvent.VK_V);
		menu.add(check);
		menu.addItem("GmMenuBar.PREFERENCES",KeyEvent.VK_P); //$NON-NLS-1$
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.EXIT",KeyEvent.VK_X,KeyEvent.VK_F4,ActionEvent.ALT_MASK); //$NON-NLS-1$

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_EDIT")); //$NON-NLS-1$
		menu.setMnemonic(KeyEvent.VK_E);
		add(menu);

		GmMenu sub = new GmMenu(Messages.getString("GmMenuBar.MENU_INSERT")); //$NON-NLS-1$
		sub.setMnemonic(KeyEvent.VK_I);
		menu.add(sub);
		sub.addItem("GmMenuBar.INSERT_GROUP",KeyEvent.VK_G); //$NON-NLS-1$
		sub.add(new JSeparator());
		sub.addItem("GmMenuBar.INSERT_SPRITE",KeyEvent.VK_I); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_SOUND",KeyEvent.VK_M); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_BACKGROUND",KeyEvent.VK_B); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_PATH",KeyEvent.VK_P); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_SCRIPT",KeyEvent.VK_S); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_FONT",KeyEvent.VK_F); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_TIMELINE",KeyEvent.VK_T); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_OBJECT",KeyEvent.VK_O); //$NON-NLS-1$
		sub.addItem("GmMenuBar.INSERT_ROOM",KeyEvent.VK_R); //$NON-NLS-1$

		sub = new GmMenu(Messages.getString("GmMenuBar.MENU_ADD")); //$NON-NLS-1$
		sub.setMnemonic(KeyEvent.VK_I);
		menu.add(sub);
		sub.addItem("GmMenuBar.ADD_GROUP",KeyEvent.VK_G); //$NON-NLS-1$
		sub.add(new JSeparator());
		sub.addItem("GmMenuBar.ADD_SPRITE",KeyEvent.VK_I); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_SOUND",KeyEvent.VK_M); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_BACKGROUND",KeyEvent.VK_B); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_PATH",KeyEvent.VK_P); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_SCRIPT",KeyEvent.VK_S); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_FONT",KeyEvent.VK_F); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_TIMELINE",KeyEvent.VK_T); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_OBJECT",KeyEvent.VK_O); //$NON-NLS-1$
		sub.addItem("GmMenuBar.ADD_ROOM",KeyEvent.VK_R); //$NON-NLS-1$

		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.RENAME",KeyEvent.VK_R,KeyEvent.VK_F2,0); //$NON-NLS-1$
		menu.addItem("GmMenuBar.DELETE",KeyEvent.VK_D,KeyEvent.VK_DELETE,ActionEvent.SHIFT_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.COPY",KeyEvent.VK_C,KeyEvent.VK_INSERT,ActionEvent.ALT_MASK); //$NON-NLS-1$
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.PROPERTIES",KeyEvent.VK_P,KeyEvent.VK_ENTER,ActionEvent.ALT_MASK); //$NON-NLS-1$

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_RESOURCES")); //$NON-NLS-1$
		menu.setMnemonic(KeyEvent.VK_R);
		add(menu);

		menu.addItem("GmMenuBar.VERIFYNAMES",KeyEvent.VK_V); //$NON-NLS-1$
		menu.addItem("GmMenuBar.SYNTAXCHECK",KeyEvent.VK_S); //$NON-NLS-1$
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.FIND",KeyEvent.VK_F,KeyEvent.VK_F,ActionEvent.ALT_MASK + ActionEvent.CTRL_MASK); //$NON-NLS-1$
		menu.addItem("GmMenuBar.ANNOTATE",KeyEvent.VK_A); //$NON-NLS-1$
		menu.add(new JSeparator());
		menu.addItem("GmMenuBar.EXPAND",KeyEvent.VK_E); //$NON-NLS-1$
		menu.addItem("GmMenuBar.COLLAPSE",KeyEvent.VK_C); //$NON-NLS-1$

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_HELP")); //$NON-NLS-1$
		menu.setMnemonic(KeyEvent.VK_H);
		add(menu);

		menu.addItem("GmMenuBar.MANUAL",KeyEvent.VK_M,KeyEvent.VK_F1,0); //$NON-NLS-1$
		menu.addItem("GmMenuBar.ABOUT",KeyEvent.VK_A); //$NON-NLS-1$
		}

	public class GmMenu extends JMenu
		{
		private static final long serialVersionUID = 1L;

		public GmMenu(String s)
			{
			super(s);
			}

		public JMenuItem addItem(String key,int alt)
			{
			return addItem(key,alt,-1,-1);
			}

		public JMenuItem addItem(String key,int alt,int shortcut,int control)
			{
			JMenuItem item = new JMenuItem(Messages.getString(key),alt);
			item.setIcon(LGM.getIconForKey(key));
			item.setActionCommand(key);
			if (shortcut >= 0)
				item.setAccelerator(KeyStroke.getKeyStroke(shortcut,control));
			item.addActionListener(LGM.listener);
			add(item);
			return item;
			}
		}
	}