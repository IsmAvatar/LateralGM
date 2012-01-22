/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.lateralgm.joshedit.Bindings.KeystrokeTableModel.Row;

public class Bindings extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle DEFAULTS = ResourceBundle.getBundle("org.lateralgm.joshedit.defaults"); //$NON-NLS-1$
	private static final ResourceBundle TRANSLATE = ResourceBundle.getBundle("org.lateralgm.joshedit.translate"); //$NON-NLS-1$
	public static final Preferences PREFS = Preferences.userRoot().node("/org/lateralgm/joshedit"); //$NON-NLS-1$

	JTable list;
	HashMap<String,String> shortcutName;

	class KeystrokeTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		class Row
		{
			String desc, key;

			Row(String d, String k)
			{
				desc = d; key = k;
			}

		}

		ArrayList<Row> rows;

		public KeystrokeTableModel()
		{
			rows = new ArrayList<Row>();
		}

		public void addRow(String desc, String key)
		{
			rows.add(new Row(desc,key));
		}

		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex)
		{
			return columnIndex == 0 ? "Description" : "Keystroke";
		}

		public int getRowCount()
		{
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			return columnIndex == 0 ? rows.get(rowIndex).desc : rows.get(rowIndex).key;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if (columnIndex == 0)
				rows.get(rowIndex).desc = aValue.toString();
			else
				rows.get(rowIndex).key = aValue.toString();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 1;
		}
	}

	private static String keyToEnglish(String key2)
	{
		String pieces[] = key2.split("\\+");
		if (pieces.length == 0) return null;
		int mods = 0;
		if (pieces.length > 1)
		{
			if (pieces[0].contains("C")) mods |= InputEvent.CTRL_DOWN_MASK;
			if (pieces[0].contains("S")) mods |= InputEvent.SHIFT_DOWN_MASK;
			if (pieces[0].contains("A")) mods |= InputEvent.ALT_DOWN_MASK;
			if (pieces[0].contains("M")) mods |= InputEvent.META_DOWN_MASK;
			if (pieces[0].contains("G")) mods |= InputEvent.ALT_GRAPH_DOWN_MASK;
		}
		String lastPiece = pieces[pieces.length - 1];
		if (mods == 0) return lastPiece;
		return InputEvent.getModifiersExText(mods) + " + " + lastPiece;
	}

	class KeystrokeRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Object value)
		{
			super.setValue((value == null) ? "" : value instanceof String ? keyToEnglish((String) value)
					: value);
		}
	}

	class KeystrokeSelector extends JTextField
	{
		private static final long serialVersionUID = 1L;
		public String coreValue = "";
		private String beginValue = "";
		private JTable myTable;
		private int myRow;

		KeystrokeSelector()
		{
			setFocusTraversalKeysEnabled(false);
			setDragEnabled(false);
			setEditable(false);
		}

		@Override
		public void processKeyEvent(KeyEvent e)
		{
			if (e.getID() != KeyEvent.KEY_PRESSED) return;

			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				coreValue = beginValue;
				super.setText(keyToEnglish(coreValue));
				return;
			}
			if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			{
				coreValue = "";
				super.setText("<Not set>");
				return;
			}

			System.out.println(e.getModifiersEx() + " " + e.getKeyCode());

			// Modifiers by themselves invalid
			if (e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_ALT
					|| e.getKeyCode() == KeyEvent.VK_SHIFT || e.getKeyCode() == 0) return;

			// Typable characters invalid
			if (e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK //and nothing else
					|| e.getModifiersEx() == 0 && !e.isActionKey()) return;

			// Generate name
			String mods = "";
			if (e.isControlDown()) mods += "C";
			if (e.isAltDown()) mods += "A";
			if (e.isShiftDown()) mods += "S";
			if (e.isMetaDown()) mods += "M";
			if (e.isAltGraphDown()) mods += "G";
			if (mods.length() > 0) mods += "+";

			String name = mods + KeyEvent.getKeyText(e.getKeyCode());
			
			if (!resolveCollision(name))
				return;

			setText(name);
			fireActionPerformed();
		}

		private boolean resolveCollision(String name)
		{
			int row = 0;
			for (Row i : ((KeystrokeTableModel)myTable.getModel()).rows) {
				if (i.key.equals(name) && row != myRow) {
					boolean ret = JOptionPane.showConfirmDialog(null,"Key combination already set for \"" + i.desc + ".\" Reassign?","Shortcut conflict",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
					if (ret) {
						i.key = "";
					  myTable.repaint();
					}
					return ret;
				}
				row++;
			}
			return true;
		}

		@Override
		public void setText(String text)
		{
			coreValue = text;
			super.setText(keyToEnglish(text));
		}

		@Override
		public String getText()
		{
			return super.getText();
		}

		public String getValue()
		{
			return getText();
		}

		public void init(int row, JTable table, String string)
		{
			myRow = row;
			myTable = table;
			setText(beginValue = string);
		}
	}

	class KeystrokeEditor extends AbstractCellEditor implements TableCellEditor
	{
		private static final long serialVersionUID = 1L;

		private KeystrokeSelector comp;

		public KeystrokeEditor()
		{
			comp = new KeystrokeSelector();
		}

		public Object getCellEditorValue()
		{
			return comp.coreValue;
		}

		/*public boolean isCellEditable(EventObject anEvent)
		{
			if (anEvent instanceof MouseEvent)
			{
				return ((MouseEvent) anEvent).getClickCount() >= 2;
			}
			return true;
		}*/

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
				int row, int column)
		{
			comp.init(row, table, value.toString());
			return comp;
		}
	}

	public Bindings()
	{
		super();
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		add(new JLabel("Use the list below to edit key bindings."));

		KeystrokeTableModel model = new KeystrokeTableModel();
		//		DefaultTableColumnModel colmodel = new DefaultTableColumnModel();
		list = new JTable(model);
		add(new JScrollPane(list));
		TableColumn c = list.getColumnModel().getColumn(1);
		c.setCellRenderer(new KeystrokeRenderer());
		c.setCellEditor(new KeystrokeEditor());
		//c.setCellEditor(new DefaultCellEditor(new KeystrokeSelector()));

		System.out.println("Populate bindings");
		populateBindings(model);

		/*TableColumn descc = new TableColumn();
		descc.setHeaderRenderer(new DefaultTableCellRenderer());
		colmodel.addColumn(descc);
		TableColumn keyc = new TableColumn();
		keyc.setHeaderRenderer(new DefaultTableCellRenderer());
		colmodel.addColumn(keyc);*/
		//		model.addRow("Description","Key");
		//		list.updateUI();

	}

	static void populateBindings(KeystrokeTableModel model)
	{
		TreeMap<String,ArrayList<String>> items = new TreeMap<String,ArrayList<String>>();
		Preferences bindings = PREFS.node("bindings"); //$NON-NLS-1$
		try
		{
			for (String key : bindings.keys())
			{
				String act = bindings.get(key,null);
				if (act != null) {
					ArrayList<String> a = items.get(act);
					if (a == null) items.put(act,a = new ArrayList<String>());
					a.add(key);
				}
			}
		}
		catch (BackingStoreException e)
		{
			System.err.println("Failed to read JoshEdit keybindings!");
		}
		if (model.getRowCount() == 0)
		{
			for (String key : DEFAULTS.keySet())
			{
				String act = DEFAULTS.getString(key);
				ArrayList<String> a = items.get(act);
				if (a == null) items.put(act,a = new ArrayList<String>());
				a.add(key);
			}
		}
		for (Entry<String,ArrayList<String>> i : items.entrySet())
		{
			String act = i.getKey();
			for (String key : i.getValue()) {
				model.addRow(getString("bindings." + act,act),key);
				act = "";
			}
		}
	}

	public static String getString(String key, String def)
	{
		String r;
		try
		{
			r = TRANSLATE.getString(key);
		}
		catch (MissingResourceException e)
		{
			r = def == null ? '!' + key + '!' : def;
		}
		return PREFS.get(key,r);
	}

	public static void readMappings(InputMap im)
	{
		Preferences bindings = PREFS.node("bindings"); //$NON-NLS-1$
		try
		{
			boolean changed = false;
			for (String key : bindings.keys())
			{
				String act = bindings.get(key,null);
				if (act != null)
				{
					changed = true;
					im.put(JoshText.key(key),act);
				}
			}
			if (changed) return;
		}
		catch (BackingStoreException e)
		{
			System.err.println("Failed to read JoshEdit keybindings!");
		}

		for (String key : DEFAULTS.keySet())
		{
			String act = DEFAULTS.getString(key);
			im.put(JoshText.key(key),act);
		}
	}
}
