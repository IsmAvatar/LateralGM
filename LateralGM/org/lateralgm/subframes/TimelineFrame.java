/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.sub.Moment;

public class TimelineFrame extends ResourceFrame<Timeline> implements ActionListener,
		ListSelectionListener
	{
	private static final long serialVersionUID = 1L;

	public JButton add, change, delete, duplicate;
	public JButton shift, merge, clear;

	public JList moments;
	public GmObjectFrame.ActionList actions;
	public GMLTextArea code;

	public TimelineFrame(Timeline res, ResNode node)
		{
		super(res,node);

		setSize(560,400);
		setMinimumSize(new Dimension(560,400));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setPreferredSize(new Dimension(180,280));
		side1.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));

		JLabel lab = new JLabel(Messages.getString("TimelineFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(160,14));
		side1.add(lab);
		name.setPreferredSize(new Dimension(160,20));
		side1.add(name);

		addGap(side1,180,20);

		add = new JButton(Messages.getString("TimelineFrame.ADD")); //$NON-NLS-1$
		add.setPreferredSize(new Dimension(80,20));
		add.addActionListener(this);
		side1.add(add);
		change = new JButton(Messages.getString("TimelineFrame.CHANGE")); //$NON-NLS-1$
		change.setPreferredSize(new Dimension(80,20));
		change.addActionListener(this);
		side1.add(change);
		delete = new JButton(Messages.getString("TimelineFrame.DELETE")); //$NON-NLS-1$
		delete.setPreferredSize(new Dimension(80,20));
		delete.addActionListener(this);
		side1.add(delete);
		duplicate = new JButton(Messages.getString("TimelineFrame.DUPLICATE")); //$NON-NLS-1$
		duplicate.setPreferredSize(new Dimension(90,20));
		duplicate.addActionListener(this);
		side1.add(duplicate);

		addGap(side1,180,20);

		shift = new JButton(Messages.getString("TimelineFrame.SHIFT")); //$NON-NLS-1$
		shift.setPreferredSize(new Dimension(80,20));
		shift.addActionListener(this);
		side1.add(shift);
		merge = new JButton(Messages.getString("TimelineFrame.MERGE")); //$NON-NLS-1$
		merge.setPreferredSize(new Dimension(80,20));
		merge.addActionListener(this);
		side1.add(merge);
		clear = new JButton(Messages.getString("TimelineFrame.CLEAR")); //$NON-NLS-1$
		clear.setPreferredSize(new Dimension(80,20));
		clear.addActionListener(this);
		side1.add(clear);

		addGap(side1,180,50);

		save.setPreferredSize(new Dimension(130,24));
		save.setText(Messages.getString("TimelineFrame.SAVE")); //$NON-NLS-1$
		side1.add(save);

		JPanel side2 = new JPanel(new BorderLayout());
		side2.setMaximumSize(new Dimension(90,Integer.MAX_VALUE));
		lab = new JLabel(Messages.getString("TimelineFrame.MOMENTS")); //$NON-NLS-1$
		side2.add(lab,"North"); //$NON-NLS-1$
		moments = new JList(res.moments.toArray());
		moments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moments.addListSelectionListener(this);
		JScrollPane scroll = new JScrollPane(moments);
		scroll.setPreferredSize(new Dimension(90,260));
		side2.add(scroll,"Center"); //$NON-NLS-1$

		add(side1);
		add(side2);

		if (false)
			{
			code = new GMLTextArea(""); //$NON-NLS-1$
			JScrollPane codePane = new JScrollPane(code);
			add(codePane);
			}
		else
			{
			actions = GmObjectFrame.addActionPane(this);
			}

		moments.setSelectedIndex(0);
		}

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		ResourceComparator c = new ResourceComparator();
		return c.areEqual(res,resOriginal);
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.timelines.replace(res,resOriginal);
		}

	public void commitChanges()
		{
		actions.save();
		res.setName(name.getText());
		}

	public void actionPerformed(ActionEvent e)
		{
		if (!(e.getSource() instanceof JButton))
			{
			super.actionPerformed(e);
			return;
			}
		JButton but = (JButton) e.getSource();
		if (but == add || but == change || but == duplicate)
			{
			Moment m = (Moment) moments.getSelectedValue();
			if (m == null && but != add) return;
			int sn = (m == null) ? -1 : m.stepNo;
			String msg = Messages.getString("TimelineFrame.MOM_NUM"); //$NON-NLS-1$
			String ttl;
			if (but == add)
				ttl = "TimelineFrame.MOM_ADD"; //$NON-NLS-1$
			else if (but == change)
				ttl = "TimelineFrame.MOM_CHANGE"; //$NON-NLS-1$
			else
				ttl = "TimelineFrame.MOM_DUPLICATE"; //$NON-NLS-1$
			ttl = Messages.getString(ttl);
			JPanel pane = new JPanel();
			pane.add(new JLabel(msg));
			IntegerField field = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,sn
					+ (but == add ? 1 : 0));
			field.setPreferredSize(new Dimension(80,20));
			pane.add(field);
			int ret = JOptionPane.showConfirmDialog(this,pane,ttl,JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) return;
			ret = field.getIntValue();
			if (ret == sn) return;
			int p = -Collections.binarySearch(res.moments,ret) - 1;
			if (but == add)
				{
				if (p < 0)
					{
					moments.setSelectedIndex(p);
					return;
					}
				Moment m2 = new Moment();
				m2.stepNo = ret;
				res.moments.add(p,m2);
				}
			else if (but == change)
				{
				if (p < 0)
					{
					JOptionPane.showMessageDialog(this,Messages.getString("TimelineFrame.MOM_EXIST")); //$NON_NLS-1$ //$NON-NLS-1$
					return;
					}
				if (ret > sn) p--;
				m.stepNo = ret;
				res.moments.remove(moments.getSelectedIndex()); //should never be -1
				res.moments.add(p,m);
				}
			else
				{
				if (p >= 0)
					{
					JOptionPane.showMessageDialog(this,Messages.getString("TimelineFrame.MOM_EXIST")); //$NON_NLS-1$ //$NON-NLS-1$
					return;
					}
				Moment m2 = m.copy();
				m2.stepNo = ret;
				res.moments.add(p,m2);
				}
			moments.setListData(res.moments.toArray());
			moments.setSelectedIndex(p);
			return;
			}
		if (but == delete)
			{
			int p = moments.getSelectedIndex();
			if (p == -1) return;
			if (res.moments.get(p).actions.size() != 0)
				{
				String msg = Messages.getString("TimelineFrame.MOM_DELETE"); //$NON-NLS-1$
				String ttl = Messages.getString("TimelineFrame.MOM_CONFIRM"); //$NON-NLS-1$
				int r = JOptionPane.showConfirmDialog(this,msg,ttl,JOptionPane.YES_NO_OPTION);
				if (r == JOptionPane.NO_OPTION) return;
				}
			res.moments.remove(p);
			moments.setListData(res.moments.toArray());
			moments.setSelectedIndex(Math.min(res.moments.size() - 1,p));
			return;
			}
		if (but == clear)
			{
			if (res.moments.size() == 0) return;
			String msg = Messages.getString("TimelineFrame.MOM_CLEAR"); //$NON-NLS-1$
			String ttl = Messages.getString("TimelineFrame.MOM_CONFIRM"); //$NON-NLS-1$
			int r = JOptionPane.showConfirmDialog(this,msg,ttl,JOptionPane.YES_NO_OPTION);
			if (r == JOptionPane.NO_OPTION) return;
			res.moments.clear();
			moments.setListData(res.moments.toArray());
			}
		if (but == shift)
			{
			if (res.moments.size() == 0) return;
			Moment m = (Moment) moments.getSelectedValue();
			int sn = (m == null) ? 0 : m.stepNo;
			String ttl = Messages.getString("TimelineFrame.MOM_SHIFT"); //$NON-NLS-1$
			JPanel pane = new JPanel(new GridLayout(0,2));
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_START"))); //$NON-NLS-1$
			IntegerField iStart = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,sn);
			iStart.setPreferredSize(new Dimension(80,20));
			pane.add(iStart);
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_END"))); //$NON-NLS-1$
			IntegerField iEnd = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,sn);
			iEnd.setPreferredSize(new Dimension(80,20));
			pane.add(iEnd);
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_AMOUNT"))); //$NON-NLS-1$
			IntegerField iAmt = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,1);
			iAmt.setPreferredSize(new Dimension(80,20));
			pane.add(iAmt);
			int ret = JOptionPane.showConfirmDialog(this,pane,ttl,JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) return;
			int p = res.shiftMoments(iStart.getIntValue(),iEnd.getIntValue(),iAmt.getIntValue());
			moments.setListData(res.moments.toArray());
			//this is actually the *old* position of first shifted moment, the same as GM does it.
			//if we wanted to, we could find the new position, but it's a lot of work for nothing
			moments.setSelectedIndex(p);
			return;
			}
		if (but == merge)
			{
			if (res.moments.size() == 0) return;
			Moment m = (Moment) moments.getSelectedValue();
			int sn = (m == null) ? 0 : m.stepNo;
			String ttl = Messages.getString("TimelineFrame.MOM_MERGE"); //$NON-NLS-1$
			JPanel pane = new JPanel(new GridLayout(0,2));
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_START"))); //$NON-NLS-1$
			IntegerField iStart = new IntegerField(0,Integer.MAX_VALUE,sn);
			iStart.setPreferredSize(new Dimension(80,20));
			pane.add(iStart);
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_END"))); //$NON-NLS-1$
			IntegerField iEnd = new IntegerField(0,Integer.MAX_VALUE,sn);
			iEnd.setPreferredSize(new Dimension(80,20));
			pane.add(iEnd);
			int ret = JOptionPane.showConfirmDialog(this,pane,ttl,JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) return;
			actions.save(); //prevents "fresh" actions from being overwritten
			int p = res.mergeMoments(iStart.getIntValue(),iEnd.getIntValue());
			moments.setListData(res.moments.toArray());
			moments.setSelectedIndex(p);
			return;
			}
		super.actionPerformed(e);
		}

	//Moments selection changed
	public void valueChanged(ListSelectionEvent e)
		{
		if (e.getValueIsAdjusting()) return;
		Moment m = (Moment) moments.getSelectedValue();
		actions.setActionContainer(m);
		}
	}
