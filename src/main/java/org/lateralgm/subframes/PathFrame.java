/*
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;

import org.lateralgm.components.NumberField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.EditorScrollPane;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.PathEditor;
import org.lateralgm.components.visual.PathEditor.PPathEditor;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.resources.sub.PathPoint.PPathPoint;
import org.lateralgm.ui.swing.propertylink.FormattedLink;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;
import org.lateralgm.ui.swing.util.ArrayListModel;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class PathFrame extends InstantiableResourceFrame<Path,PPath>
	{
	private static final long serialVersionUID = 1L;

	private JList<PathPoint> list;
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

		GroupLayout layout = new GroupLayout(getContentPane())
			{
				@Override
				public void layoutContainer(Container parent)
					{
					// TODO: The layout of the preview/editor does not need to effect the layout of the
					// toolbar and preview pane. I feel as though this is grossly overengineered for the path
					// frame and the room editor. Not to mention the path editor feels clunky anyway.
					Dimension m = PathFrame.this.getMinimumSize();
					Dimension s = PathFrame.this.getSize();
					Dimension r = new Dimension(Math.max(m.width,s.width),Math.max(m.height,s.height));
					if (!r.equals(s))
						PathFrame.this.setSize(r);
					else
						super.layoutContainer(parent);
					}
			};
		setLayout(layout);

		JToolBar toolbar = makeToolBar();
		JPanel side = makeSide(res);
		JComponent preview = makePreview();

		SequentialGroup orientationGroup = layout.createSequentialGroup();

		if (Prefs.rightOrientation) {
			orientationGroup
			/*	*/.addComponent(preview,240,640,DEFAULT_SIZE)
			/*	*/.addComponent(side,200,DEFAULT_SIZE,200);
		} else {
			orientationGroup
			/*	*/.addComponent(side,200,DEFAULT_SIZE,200)
			/*	*/.addComponent(preview,240,640,DEFAULT_SIZE);
		}

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(toolbar, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
		/**/.addGroup(orientationGroup));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(toolbar,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(side)
		/*	*/.addComponent(preview,DEFAULT_SIZE,480,DEFAULT_SIZE)));
		pack();
		list.setSelectedIndex(0);
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		// Using GroupLayout here for baseline alignment support.
		GroupLayout layout = new GroupLayout(tool);
		tool.setLayout(layout);
		tool.setFloatable(false);
		JLabel lsx = new JLabel(Messages.getString("PathFrame.SNAP_X"));
		NumberField sx = new NumberField(1,999);
		plf.make(sx,PPath.SNAP_X);
		JLabel lsy = new JLabel(Messages.getString("PathFrame.SNAP_Y"));
		NumberField sy = new NumberField(1,999);
		plf.make(sy,PPath.SNAP_Y);
		// For some reason, JToolBar + GroupLayout makes the button too small to show all the text.
		// Using a JCheckBox instead. This also mathces the other components better.

		JToggleButton grid = new JToggleButton();
		grid.setToolTipText(Messages.getString("PathFrame.GRID"));
		grid.setActionCommand("PathFrame.GRID");
		grid.setIcon(LGM.getIconForKey("PathFrame.GRID"));

		peplf.make(grid,PPathEditor.SHOW_GRID);
		ResourceMenu<Room> room = new ResourceMenu<Room>(Room.class,
				Messages.getString("PathFrame.NO_ROOM"),160);
		plf.make(room,PPath.BACKGROUND_ROOM);
		JSeparator saveSep = new JSeparator(JSeparator.VERTICAL);
		layout.setHorizontalGroup(layout.createSequentialGroup()
		/**/.addComponent(save)
		/**/.addPreferredGap(ComponentPlacement.RELATED)
		/**/.addComponent(saveSep,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addPreferredGap(ComponentPlacement.RELATED)
		/**/.addComponent(lsx).addComponent(sx,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addPreferredGap(ComponentPlacement.RELATED)
		/**/.addComponent(lsy).addComponent(sy,PREFERRED_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addPreferredGap(ComponentPlacement.RELATED)
		/**/.addComponent(grid)
		/**/.addPreferredGap(ComponentPlacement.RELATED)
		/**/.addComponent(room,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE).addContainerGap());
		layout.setVerticalGroup(layout.createBaselineGroup(false,false)
		/**/.addComponent(save)
		/**/.addComponent(saveSep)
		/**/.addComponent(lsx).addComponent(sx)
		/**/.addComponent(lsy).addComponent(sy)
		/**/.addComponent(grid).addComponent(room));
		return tool;
		}

	private JPanel makeSide(Path res)
		{
		JPanel side1 = new JPanel(null);
		GroupLayout layout = new GroupLayout(side1);
		side1.setLayout(layout);

		final JLabel lName = new JLabel(Messages.getString("PathFrame.NAME")); //$NON-NLS-1$

		list = new JList<PathPoint>(new ArrayListModel<PathPoint>(res.points));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		peplf.make(list,PPathEditor.SELECTED_POINT);
		list.setFont(new Font("Monospaced",Font.PLAIN,10)); //$NON-NLS-1$
		list.setVisibleRowCount(5);
		JScrollPane p = new JScrollPane(list);

		JLabel lx = new JLabel(Messages.getString("PathFrame.X")); //$NON-NLS-1$
		tx = new NumberField(0);
		tx.setColumns(5);
		add = new JButton(Messages.getString("PathFrame.ADD")); //$NON-NLS-1$
		add.addActionListener(this);

		JLabel ly = new JLabel(Messages.getString("PathFrame.Y")); //$NON-NLS-1$
		ty = new NumberField(0);
		ty.setColumns(4);
		insert = new JButton(Messages.getString("PathFrame.INSERT")); //$NON-NLS-1$
		insert.addActionListener(this);

		JLabel lsp = new JLabel(Messages.getString("PathFrame.SP")); //$NON-NLS-1$
		tsp = new NumberField(0,1000000,100);
		tsp.setColumns(5);
		delete = new JButton(Messages.getString("PathFrame.DELETE")); //$NON-NLS-1$
		delete.addActionListener(this);

		smooth = new JCheckBox(Messages.getString("PathFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PPath.SMOOTH);
		closed = new JCheckBox(Messages.getString("PathFrame.CLOSED")); //$NON-NLS-1$
		plf.make(closed,PPath.CLOSED);

		JLabel lpr = new JLabel(Messages.getString("PathFrame.PRECISION")); //$NON-NLS-1$
		tpr = new NumberField(1,8);
		plf.make(tpr,PPath.PRECISION);

		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(lName).addComponent(name))
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(smooth).addComponent(closed))
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(lpr).addComponent(tpr))
		/**/.addComponent(p)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(add,DEFAULT_SIZE,DEFAULT_SIZE,Integer.MAX_VALUE)
		/*	*/.addComponent(insert,DEFAULT_SIZE,DEFAULT_SIZE,Integer.MAX_VALUE))
		/**/.addComponent(delete,DEFAULT_SIZE,DEFAULT_SIZE,Integer.MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(lx).addComponent(ly).addComponent(lsp))
		/*	*/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(tx).addComponent(ty).addComponent(tsp))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createBaselineGroup(true,false)
		/*	*/.addComponent(lName).addComponent(name))
		/**/.addGroup(layout.createBaselineGroup(true,false)
		/*	*/.addComponent(smooth).addComponent(closed))
		/**/.addGroup(layout.createBaselineGroup(true,false)
		/*	*/.addComponent(lpr).addComponent(tpr))
		/**/.addComponent(p,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/**/.addGroup(layout.createBaselineGroup(true,false)
		/*	*/.addComponent(add).addComponent(insert))
		/**/.addComponent(delete)
		/**/.addGroup(layout.createBaselineGroup(true,false)
		/*	*/.addComponent(lx).addComponent(tx))
		/**/.addGroup(layout.createBaselineGroup(true,false)
		/*	*/.addComponent(ly).addComponent(ty))
		/**/.addGroup(layout.createBaselineGroup(true,false)
		/*	*/.addComponent(lsp).addComponent(tsp)));
		return side1;
		}

	private JComponent makePreview()
		{
		//include a status bar
		return new EditorScrollPane(pathEditor);
		}

	protected boolean areResourceFieldsEqual()
		{
		return res.points.equals(resOriginal.points);
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		}

	@Override
	public Dimension getMinimumSize()
		{
		Dimension p = getContentPane().getSize();
		Dimension l = getContentPane().getMinimumSize();
		Dimension s = getSize();
		l.width += s.width - p.width;
		l.height += s.height - p.height;
		return l;
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
			res.points.add(i,
					new PathPoint((Integer) tx.getValue(),(Integer) ty.getValue(),(Integer) tsp.getValue()));
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
					PropertyLink.removeAll(ltx,lty,ltsp);
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
				default:
					break;
				}
			}
		}
	}
