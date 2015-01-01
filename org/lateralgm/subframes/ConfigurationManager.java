package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameSettings;

public class ConfigurationManager extends JFrame implements ActionListener
	{

	/**
	 * TODO: Change if needed.
	 */
	private static final long serialVersionUID = 1L;
	JList<GameSettings> configList = null;
	
	// The purpose of this internal class is because Vectors and ArrayLists are handled by reference for Java
	// so instead of converting to an array and doing all kinds of crazy updating, we can just make a list model
	// that handles the array by reference.
	public class VectorListModel<T> extends AbstractListModel<T> {

		/**
		 * TODO: Change if needed.
		 */
		private static final long serialVersionUID = 1L;
		private Vector<T> vector = null;
		
		
		public VectorListModel(Vector<T> vec) {
			vector = vec;
		}
		
		public void add(int index, T element) {
			vector.add(index,element);
			super.fireIntervalAdded(this,index,index);
		}
		
		public void addElement(T element) {
			vector.addElement(element);
			super.fireIntervalAdded(this,vector.size()-1,vector.size()-1);
		}
		
		public boolean remove(T element) {
			boolean ret = vector.remove(element);
			super.fireIntervalRemoved(this,vector.size(),vector.size());
			return ret;
		}
		
		public boolean removeAll(List<T> elements) {
			boolean ret = vector.removeAll(elements);
			super.fireIntervalRemoved(this,vector.size(),vector.size());
			return ret;
		}
		
		public T getElementAt(int index) {
			return vector.get(index);
		}
		
		public T get(int index) {
			return vector.get(index);
		}
		
		public Enumeration<T> elements() {
			return vector.elements();
		}
		
		public Object[] toArray() {
			return vector.toArray();
		}
		
		public int size() {
			return vector.size();
		}
		
		public boolean isEmpty() {
			return vector.isEmpty();
		}

		@Override
		public int getSize()
			{
			return vector.size();
			}
	
	}

	public ConfigurationManager() {
		super();
		setResizable(false);
		setTitle(Messages.getString("ConfigurationManager.TITLE"));
		setIconImage(LGM.getIconForKey("ConfigurationManager.ICON").getImage());
		
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		toolbar.add(makeToolbarButton("ADD"));
		toolbar.add(makeToolbarButton("DELETE"));
		toolbar.addSeparator();
		toolbar.add(new JLabel(Messages.getString("ConfigurationManager.NAME")));
		JTextField nameField = new JTextField();
		nameField.setColumns(20);
		nameField.setMaximumSize(nameField.getPreferredSize());
		toolbar.add(nameField);
		
		this.add(toolbar, BorderLayout.NORTH);
		configList = new JList<GameSettings>(new VectorListModel<GameSettings>(LGM.currentFile.gameSettings));
		
		JScrollPane scroll = new JScrollPane(configList);
		this.add(scroll, BorderLayout.CENTER);
		
		this.pack();
		this.setSize(300,340);
		setLocationRelativeTo(LGM.frame);
	}
	
	JButton makeToolbarButton(String key) {
		key = "ConfigurationManager." + key;
		JButton jb = new JButton();
		jb.setToolTipText(Messages.getString(key));
		jb.setIcon(LGM.getIconForKey(key));
		jb.setActionCommand(key);
		jb.addActionListener(this);
		return jb;
	}

	@Override
	public void actionPerformed(ActionEvent ev)
		{
		String cmd = ev.getActionCommand();
		VectorListModel<GameSettings> model = (VectorListModel<GameSettings>) configList.getModel();
		if (cmd.endsWith("ADD")) {
			GameSettings config = new GameSettings();
			config.setName("Ass");
			model.addElement(config);
		} else if (cmd.endsWith("DELETE")) {
			model.removeAll(configList.getSelectedValuesList());
		}
		}
	
	}
