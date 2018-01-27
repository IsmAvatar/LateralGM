/*
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014, Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.GmStreamDecoder;
import org.lateralgm.file.GmStreamEncoder;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.Constants.PConstants;
import org.lateralgm.resources.sub.Constant;

public class ConstantsFrame extends ResourceFrame<Constants,PConstants>
	{
	private static final long serialVersionUID = 1L;

	public JToolBar toolbar;
	public JButton importBut;
	public JButton exportBut;
	public JTable constants;
	public ConstantsTableModel cModel;
	public JButton add;
	public JButton insert;
	public JButton delete;
	public JButton clear;
	public JButton up;
	public JButton down;
	public JButton sort;
	private CustomFileChooser constantsFc;

	private class ConstantsTableModel extends AbstractTableModel
		{
		private static final long serialVersionUID = 1L;
		List<Constant> constants;

		ConstantsTableModel(List<Constant> list)
			{
			constants = ProjectFile.copyConstants(list);
			}

		public int getColumnCount()
			{
			return 2;
			}

		public int getRowCount()
			{
			return constants.size();
			}

		public Object getValueAt(int rowIndex, int columnIndex)
			{
			Constant c = constants.get(rowIndex);
			return (columnIndex == 0) ? c.name : c.value;
			}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
			Constant c = constants.get(rowIndex);
			if (columnIndex == 0)
				c.name = aValue.toString();
			else
				c.value = aValue.toString();
			}

		public boolean isCellEditable(int row, int col)
			{
			return true;
			}

		public String getColumnName(int column)
			{
			String ind = (column == 0) ? "NAME" : "VALUE"; //$NON-NLS-1$ //$NON-NLS-2$
			return Messages.getString("ConstantsFrame." + ind); //$NON-NLS-1$
			}

		public void removeEmptyConstants()
			{
			for (int i = constants.size() - 1; i >= 0; i--)
				if (constants.get(i).name.equals("")) constants.remove(i); //$NON-NLS-1$
			fireTableDataChanged();
			}
		}

	public ConstantsFrame(Constants res)
		{
		this(res,null);
		}

	public void updateTitle() {
		String name = res.getName();
		if (res == LGM.currentFile.defaultConstants) name = "All Configurations";
		this.setTitle(Messages.getString("ConstantsFrame.TITLE") + " : " + name);
	}

	public ConstantsFrame(Constants res, ResNode node)
		{
		super(res,node); //$NON-NLS-1$
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setTitle(Messages.getString("ConstantsFrame.TITLE"));

		JPanel content = new JPanel();
		GroupLayout layout = new GroupLayout(content);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		//this.setLayout(layout);
		content.setLayout(layout);

		this.setLayout(new BorderLayout());

		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(save);
		toolbar.addSeparator();
		exportBut = new JButton(LGM.getIconForKey("ConstantsFrame.EXPORT")); //$NON-NLS-1$
		exportBut.setToolTipText(Messages.getString("ConstantsFrame.EXPORT"));
		exportBut.addActionListener(this);
		toolbar.add(exportBut);
		importBut = new JButton(LGM.getIconForKey("ConstantsFrame.IMPORT")); //$NON-NLS-1$
		importBut.setToolTipText(Messages.getString("ConstantsFrame.IMPORT"));
		importBut.addActionListener(this);
		toolbar.add(importBut);
		//toolbar.addSeparator();
		//toolbar.add(new JLabel("Configuration:"));
		//String strs[] = { "All Configurations","Default" };
		//JComboBox<String> configCombo = new JComboBox<String>(strs);
		//configCombo.setMaximumSize(new Dimension(130,22));
		//toolbar.add(configCombo);

		this.add(toolbar,BorderLayout.NORTH);
		this.add(content,BorderLayout.CENTER);

		cModel = new ConstantsTableModel(res.constants);
		constants = new JTable(cModel);
		JScrollPane scroll = new JScrollPane(constants);
		constants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		constants.getTableHeader().setReorderingAllowed(false);
		constants.setTransferHandler(null);
		//this fixes java bug 4709394, where the cell editor does not commit on focus lost,
		//causing the value to remain in-limbo even after the "Save" button is clicked.
		constants.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE); //$NON-NLS-1$

		add = new JButton(Messages.getString("ConstantsFrame.ADD")); //$NON-NLS-1$
		add.addActionListener(this);
		insert = new JButton(Messages.getString("ConstantsFrame.INSERT")); //$NON-NLS-1$
		insert.addActionListener(this);
		delete = new JButton(Messages.getString("ConstantsFrame.DELETE")); //$NON-NLS-1$
		delete.addActionListener(this);
		clear = new JButton(Messages.getString("ConstantsFrame.CLEAR")); //$NON-NLS-1$
		clear.addActionListener(this);
		up = new JButton(Messages.getString("ConstantsFrame.UP")); //$NON-NLS-1$
		up.addActionListener(this);
		down = new JButton(Messages.getString("ConstantsFrame.DOWN")); //$NON-NLS-1$
		down.addActionListener(this);
		sort = new JButton(Messages.getString("ConstantsFrame.SORT")); //$NON-NLS-1$
		sort.addActionListener(this);

		constantsFc = new CustomFileChooser("/org/lateralgm","LAST_LGC_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		constantsFc.setFileFilter(new CustomFileFilter(
				Messages.getString("ConstantsFrame.LGC_FILES"),".lgc")); //$NON-NLS-1$ //$NON-NLS-2$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(scroll)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(add,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(insert,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(delete,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(clear,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addPreferredGap(ComponentPlacement.UNRELATED)
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(up,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(down,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addComponent(sort,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(scroll,DEFAULT_SIZE,300,MAX_VALUE)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(add)
		/*		*/.addComponent(delete)
		/*		*/.addComponent(up)
		/*		*/.addComponent(sort))
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(insert)
		/*		*/.addComponent(clear)
		/*		*/.addComponent(down)));

		pack();
		}

	public void actionPerformed(ActionEvent e)
		{
		super.actionPerformed(e);

		if (e.getSource() == importBut)
			{
			importConstants();
			return;
			}
		if (e.getSource() == exportBut)
			{
			exportConstants();
			return;
			}
		if (e.getSource() == add)
			{
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			cModel.constants.add(new Constant());
			int row = cModel.constants.size() - 1;
			cModel.fireTableRowsInserted(row,row);
			constants.getSelectionModel().setSelectionInterval(row,row);
			return;
			}
		if (e.getSource() == insert)
			{
			if (constants.getSelectedRow() == -1) return;
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			cModel.constants.add(constants.getSelectedRow(),new Constant());
			cModel.fireTableRowsInserted(constants.getSelectedRow(),constants.getSelectedRow());
			constants.getSelectionModel().setSelectionInterval(0,constants.getSelectedRow() - 1);
			return;
			}
		if (e.getSource() == delete)
			{
			if (constants.getSelectedRow() == -1) return;
			int row = constants.getSelectedRow();
			cModel.constants.remove(row);
			cModel.fireTableRowsDeleted(row,row);
			if (cModel.constants.size() > 0)
				constants.getSelectionModel().setSelectionInterval(0,
						Math.min(row,cModel.constants.size() - 1));
			return;
			}
		if (e.getSource() == clear)
			{
			if (cModel.constants.size() == 0) return;
			int last = cModel.constants.size() - 1;
			cModel.constants.clear();
			cModel.fireTableRowsDeleted(0,last);
			return;
			}
		if (e.getSource() == up)
			{
			int row = constants.getSelectedRow();
			if (row <= 0) return;
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();

			Constant c = cModel.constants.get(row - 1);
			cModel.constants.set(row - 1,cModel.constants.get(row));
			cModel.constants.set(row,c);
			cModel.fireTableDataChanged();
			constants.getSelectionModel().setSelectionInterval(0,row - 1);
			return;
			}
		if (e.getSource() == down)
			{
			int row = constants.getSelectedRow();
			if (row == -1 || row >= cModel.constants.size() - 1) return;
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			Constant c = cModel.constants.get(row + 1);
			cModel.constants.set(row + 1,cModel.constants.get(row));
			cModel.constants.set(row,c);
			cModel.fireTableDataChanged();
			constants.getSelectionModel().setSelectionInterval(0,row + 1);
			return;
			}
		if (e.getSource() == sort)
			{
			if (constants.getCellEditor() != null) constants.getCellEditor().stopCellEditing();
			Collections.sort(cModel.constants);
			cModel.fireTableDataChanged();
			if (cModel.constants.size() > 0) constants.getSelectionModel().setSelectionInterval(0,0);
			return;
			}
		}

	private void importConstants()
		{
		if (constantsFc.showOpenDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			cModel.removeEmptyConstants();
			GmStreamDecoder in = null;
			try
				{
				File f = constantsFc.getSelectedFile();
				if (f == null || !f.exists()) throw new Exception();

				in = new GmStreamDecoder(f);
				if (in.read3() != ('L' | ('G' << 8) | ('C' << 16))) throw new Exception();
				int count = in.read2();
				for (int i = 0; i < count; i++)
					{
					Constant c = new Constant();
					c.name = in.readStr1();
					c.value = in.readStr1();
					if (!cModel.constants.contains(c)) cModel.constants.add(c);
					}
				cModel.fireTableDataChanged();
				if (cModel.constants.size() > 0) constants.getSelectionModel().setSelectionInterval(0,0);
				}
			catch (Exception ex)
				{
				JOptionPane.showMessageDialog(LGM.frame,
						Messages.getString("ConstantsFrame.ERROR_IMPORTING_CONSTANTS"), //$NON-NLS-1$
						Messages.getString("ConstantsFrame.TITLE_ERROR"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				}
			finally
				{
				if (in != null) try
					{
					in.close();
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				}
			}
		}

	private void exportConstants()
		{
		while (constantsFc.showSaveDialog(LGM.frame) == JFileChooser.APPROVE_OPTION)
			{
			File f = constantsFc.getSelectedFile();
			if (f == null) return;
			if (!f.getPath().endsWith(".lgc")) f = new File(f.getPath() + ".lgc"); //$NON-NLS-1$ //$NON-NLS-2$
			int result = 0;
			if (f.exists())
				{
				result = JOptionPane.showConfirmDialog(LGM.frame,
						Messages.getString("ConstantsFrame.REPLACE_FILE"), //$NON-NLS-1$
						Messages.getString("ConstantsFrame.TITLE_REPLACE_FILE"), //$NON-NLS-1$
						JOptionPane.YES_NO_CANCEL_OPTION);
				}
			if (result == 2) return;
			if (result == 1) continue;

			cModel.removeEmptyConstants();
			GmStreamEncoder out = null;
			try
				{
				out = new GmStreamEncoder(f);
				out.write('L');
				out.write('G');
				out.write('C');
				out.write2(cModel.constants.size());
				for (Constant c : cModel.constants)
					{
					out.writeStr1(c.name);
					out.writeStr1(c.value);
					}
				}
			catch (FileNotFoundException e1)
				{
				e1.printStackTrace();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			finally
				{
				if (out != null) try
					{
					out.close();
					}
				catch (IOException ex)
					{
					ex.printStackTrace();
					}
				}
			return;
			}
		}

	public void commitChanges()
		{
		//Constants
		cModel.removeEmptyConstants();
		res.constants = ProjectFile.copyConstants(cModel.constants);
		}

	public void setComponents(Constants c)
		{
		//Constants
		cModel = new ConstantsTableModel(c.constants);
		constants.setModel(cModel);
		constants.updateUI();
		}

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		return !res.properties.equals(resOriginal.properties) || !res.constants.equals(resOriginal.constants);
		}

	@Override
	public void revertResource()
		{
		resOriginal.copy(res);
		setComponents(res);
		}

	@Override
	public void updateResource(boolean commit)
		{
		super.updateResource(commit);
		}
	}
