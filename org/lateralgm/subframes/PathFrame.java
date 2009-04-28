/*
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.PathEditor;
import org.lateralgm.components.visual.PathEditor.PPathEditor;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.resources.sub.PathPoint.PPathPoint;
import org.lateralgm.ui.swing.propertylink.FormattedLink;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;
import org.lateralgm.ui.swing.util.ArrayListModel;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class PathFrame extends ResourceFrame<Path,PPath> implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	private JList list;
	private NumberField tx, ty, tsp;
	private NumberField tpr;
	private JButton add, insert, delete;
	private JCheckBox smooth, closed;
	private final PathEditor pathEditor;
	private final PropertyLinkFactory<PPathEditor> peplf;
	private final PathEditorPropertyListener pepl = new PathEditorPropertyListener();

	public PathFrame(Path res, ResNode node)
		{
		super(res,node);

		pathEditor = new PathEditor(res);
		pathEditor.properties.updateSource.addListener(pepl);
		peplf = new PropertyLinkFactory<PPathEditor>(pathEditor.properties,this);

		setSize(600,400);
		setMinimumSize(new Dimension(188,400));
		setLayout(new BorderLayout());

		add(makeToolBar(),BorderLayout.NORTH);
		add(makeSide(res),BorderLayout.WEST);
		add(makePreview(),BorderLayout.CENTER);

		list.setSelectedIndex(0);
		}

	//TODO: add more buttons
	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		tool.add(save);
		tool.addSeparator();
		tool.add(new JLabel("Snap X: "));
		NumberField sx = new NumberField(0,999);
		plf.make(sx,PPath.SNAP_X);
		sx.setMaximumSize(sx.getPreferredSize());
		tool.add(sx);
		tool.add(new JLabel("Snap Y: "));
		NumberField sy = new NumberField(0,999);
		plf.make(sy,PPath.SNAP_Y);
		sy.setMaximumSize(sy.getPreferredSize());
		tool.add(sy);
		JToggleButton grid = new JToggleButton("Grid");
		grid.setMaximumSize(grid.getPreferredSize());
		peplf.make(grid,PPathEditor.SHOW_GRID);
		tool.add(grid);
		return tool;
		}

	private JPanel makeSide(Path res)
		{
		JPanel side1 = new JPanel(new FlowLayout());
		side1.setMinimumSize(new Dimension(180,350));
		side1.setMaximumSize(new Dimension(180,350));
		side1.setPreferredSize(new Dimension(180,350));

		JLabel lab = new JLabel(Messages.getString("PathFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,14));
		side1.add(lab);
		name.setPreferredSize(new Dimension(120,20));
		side1.add(name);

		list = new JList(new ArrayListModel<PathPoint>(res.points));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		peplf.make(list,PPathEditor.SELECTED_POINT);
		list.setFont(new Font("Monospaced",Font.PLAIN,10)); //$NON-NLS-1$
		JScrollPane p = new JScrollPane(list);
		p.setPreferredSize(new Dimension(160,180));
		side1.add(p);

		lab = new JLabel(Messages.getString("PathFrame.X")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(20,14));
		side1.add(lab);
		tx = new NumberField(0);
		tx.setColumns(5);
		side1.add(tx);
		add = new JButton(Messages.getString("PathFrame.ADD")); //$NON-NLS-1$
		add.setPreferredSize(new Dimension(70,16));
		add.addActionListener(this);
		side1.add(add);

		lab = new JLabel(Messages.getString("PathFrame.Y")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(20,14));
		side1.add(lab);
		ty = new NumberField(0);
		ty.setColumns(5);
		side1.add(ty);
		insert = new JButton(Messages.getString("PathFrame.INSERT")); //$NON-NLS-1$
		insert.setPreferredSize(new Dimension(70,16));
		insert.addActionListener(this);
		side1.add(insert);

		lab = new JLabel(Messages.getString("PathFrame.SP")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(20,14));
		side1.add(lab);
		tsp = new NumberField(0,1000000,100);
		tsp.setColumns(5);
		side1.add(tsp);
		delete = new JButton(Messages.getString("PathFrame.DELETE")); //$NON-NLS-1$
		delete.setPreferredSize(new Dimension(70,16));
		delete.addActionListener(this);
		side1.add(delete);

		smooth = new JCheckBox(Messages.getString("PathFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PPath.SMOOTH);
		side1.add(smooth);
		closed = new JCheckBox(Messages.getString("PathFrame.CLOSED")); //$NON-NLS-1$
		plf.make(closed,PPath.CLOSED);
		side1.add(closed);

		lab = new JLabel(Messages.getString("PathFrame.PRECISION")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(60,14));
		side1.add(lab);
		tpr = new NumberField(1,8);
		plf.make(tpr,PPath.PRECISION);
		tpr.setPreferredSize(new Dimension(40,16));
		side1.add(tpr);

		return side1;
		}

	//TODO: 1.7
	private JComponent makePreview()
		{
		//include a status bar
		return new JScrollPane(pathEditor);
		}

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		return !(new ResourceComparator().areEqual(res,resOriginal));
		}

	public void revertResource()
		{
		resOriginal.updateReference();
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		}

	//Button was clicked
	public void actionPerformed(ActionEvent e)
		{
		Object s = e.getSource();
		if (s == add)
			{
			res.points.add(new PathPoint((Integer) tx.getValue(),(Integer) ty.getValue(),
					(Integer) tsp.getValue()));
			list.setSelectedIndex(res.points.size() - 1);
			}
		if (s == insert)
			{
			int i = list.getSelectedIndex();
			if (i == -1) return;
			res.points.add(i,new PathPoint((Integer) tx.getValue(),(Integer) ty.getValue(),
					(Integer) tsp.getValue()));
			list.setSelectedIndex(i);
			}
		if (s == delete)
			{
			int i = list.getSelectedIndex();
			Object o = list.getSelectedValue();
			if (o == null) return;
			res.points.remove(o);
			if (i >= res.points.size()) i = res.points.size() - 1;
			list.setSelectedIndex(i);
			}
		super.actionPerformed(e);
		}

	FormattedLink<PPathPoint> ltx, lty, ltsp;

	private class PathEditorPropertyListener extends PropertyUpdateListener<PPathEditor>
		{
		@Override
		public void updated(PropertyUpdateEvent<PPathEditor> e)
			{
			switch (e.key)
				{
				case SELECTED_POINT:
					if (ltx != null) ltx.remove();
					if (lty != null) lty.remove();
					if (ltsp != null) ltsp.remove();
					PathPoint pp = e.map.get(e.key);
					if (pp != null)
						{
						PropertyLinkFactory<PPathPoint> ppplf = new PropertyLinkFactory<PPathPoint>(
								pp.properties,null);
						ltx = ppplf.make(tx,PPathPoint.X);
						lty = ppplf.make(ty,PPathPoint.Y);
						ltsp = ppplf.make(tsp,PPathPoint.SPEED);
						}
					else
						{
						ltx = null;
						lty = null;
						ltsp = null;
						}
					break;
				}
			}
		}
	}
