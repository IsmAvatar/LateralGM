/*
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.components.ActionList;
import org.lateralgm.components.ActionListEditor;
import org.lateralgm.components.CodeTextArea;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.ActionList.ActionListModel;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.Timeline.PTimeline;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Moment;

public class TimelineFrame extends InstantiableResourceFrame<Timeline,PTimeline> implements
		ListSelectionListener
	{
	private static final long serialVersionUID = 1L;

	public JButton add, edit, change, delete, duplicate;
	public JButton shift, merge, clear, showInfo;

	public JList<Moment> moments;
	public ActionList actions;
	public CodeTextArea code;
	private JComponent editor;

	private ResourceInfoFrame infoFrame;

	public TimelineFrame(Timeline res, ResNode node)
		{
		super(res,node);

		this.getRootPane().setDefaultButton(save);
		GroupLayout layout = new GroupLayout(getContentPane());
		setLayout(layout);

		JPanel propertiesSide = new JPanel();
		makeProperties(propertiesSide);

		JPanel momentsSide = new JPanel(new BorderLayout());
		//side2.setMaximumSize(new Dimension(90,Integer.MAX_VALUE));
		JLabel lab = new JLabel(Messages.getString("TimelineFrame.MOMENTS")); //$NON-NLS-1$
		momentsSide.add(lab,BorderLayout.NORTH);
		moments = new JList<Moment>(res.moments.toArray(new Moment[res.moments.size()]));
		moments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moments.addListSelectionListener(this);

		// This listener should be added to each node maybe
		// otherwise you can click on the whitespace and open it
		// but then again I suppose its fine like this because I have
		// ensured checks to make sure there are no NPE's trying to edit
		// an event that don't exist. Meh, this is fine as is.
		MouseListener ml = new MouseAdapter()
			{
				public void mousePressed(MouseEvent e)
					{
					if (e.getClickCount() == 2)
						{
						editSelectedMoment();
						}
					}
			};
		moments.addMouseListener(ml);

		JScrollPane scroll = new JScrollPane(moments);
		if (Prefs.enableDragAndDrop)
			{
			scroll.setPreferredSize(new Dimension(100,300));
			}
		else
			{
			scroll.setPreferredSize(new Dimension(200,400));
			}
		momentsSide.add(scroll,BorderLayout.CENTER);

		actions = new ActionList(this);
		if (Prefs.enableDragAndDrop)
			{
			editor = new ActionListEditor(actions);
			}

		SequentialGroup orientationGroup = layout.createSequentialGroup();

		if (Prefs.rightOrientation) {
			if (Prefs.enableDragAndDrop)
			{
				orientationGroup.addComponent(editor);
			}
			orientationGroup.addComponent(momentsSide)
			/**/.addComponent(propertiesSide,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
		} else {
			orientationGroup.addComponent(propertiesSide,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
			/**/.addComponent(momentsSide);
			if (Prefs.enableDragAndDrop)
			{
				orientationGroup.addComponent(editor);
			}
		}

		layout.setHorizontalGroup(orientationGroup);

		ParallelGroup verticalGroup = layout.createParallelGroup()
		/**/.addComponent(propertiesSide)
		/**/.addComponent(momentsSide);
		if (Prefs.enableDragAndDrop)
			{
			verticalGroup.addComponent(editor);
			}
		layout.setVerticalGroup(verticalGroup);

		pack();

		moments.setSelectedIndex(0);
		}

	private void makeProperties(JPanel side1)
		{
		GroupLayout layout = new GroupLayout(side1);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		side1.setLayout(layout);

		JLabel lab = new JLabel(Messages.getString("TimelineFrame.NAME")); //$NON-NLS-1$

		edit = new JButton(Messages.getString("TimelineFrame.EDIT")); //$NON-NLS-1$
		edit.addActionListener(this);
		add = new JButton(Messages.getString("TimelineFrame.ADD")); //$NON-NLS-1$
		add.addActionListener(this);
		change = new JButton(Messages.getString("TimelineFrame.CHANGE")); //$NON-NLS-1$
		change.addActionListener(this);
		delete = new JButton(Messages.getString("TimelineFrame.DELETE")); //$NON-NLS-1$
		delete.addActionListener(this);
		duplicate = new JButton(Messages.getString("TimelineFrame.DUPLICATE")); //$NON-NLS-1$
		duplicate.addActionListener(this);

		shift = new JButton(Messages.getString("TimelineFrame.SHIFT")); //$NON-NLS-1$
		shift.addActionListener(this);
		merge = new JButton(Messages.getString("TimelineFrame.MERGE")); //$NON-NLS-1$
		merge.addActionListener(this);
		clear = new JButton(Messages.getString("TimelineFrame.CLEAR")); //$NON-NLS-1$
		clear.addActionListener(this);

		showInfo = new JButton(Messages.getString("TimelineFrame.SHOWINFORMATION")); //$NON-NLS-1$
		showInfo.addActionListener(this);
		showInfo.setIcon(LGM.getIconForKey("TimelineFrame.SHOWINFORMATION"));

		save.setText(Messages.getString("TimelineFrame.SAVE")); //$NON-NLS-1$

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lab)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(edit,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(add,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(delete,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(change,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(duplicate,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(shift,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(merge,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/**/.addComponent(clear,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(showInfo,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lab)
		/*		*/.addComponent(name))
		/**/.addGap(32)
		/**/.addComponent(edit)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(add)
		/*		*/.addComponent(change))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(delete)
		/*		*/.addComponent(duplicate))
		/**/.addGap(32)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(shift)
		/*		*/.addComponent(merge))
		/**/.addComponent(clear)
		/**/.addGap(8,8,MAX_VALUE)
		/**/.addComponent(showInfo)
		/**/.addGap(32,32,MAX_VALUE)
		/**/.addComponent(save));
		}

	protected boolean areResourceFieldsEqual()
	// return new ResourceComparator().areEqual(res.moments,resOriginal.moments);
		{
		return Util.areInherentlyUniquesEqual(res.moments,resOriginal.moments);
		}

	public void commitChanges()
		{
		actions.save();
		res.setName(name.getText());
		}

	public int findMoment(int step) {
		ListModel<Moment> model = moments.getModel();

		for (int i = 0; i < model.getSize(); i++){
			Moment mom =  model.getElementAt(i);
			if (mom.stepNo == step) {
				return i;
			}
		}
		return -1;
	}

	public void setSelectedMoment(int step) {
		int index = findMoment(step);
		if (index != -1) {
			moments.setSelectedIndex(index);
		}
	}

	public void showInfoFrame()
		{
		// The first code here is a little routine to realize all changes to every moments actions
		// NOTE: This does affect reverting the resource, just makes it so
		// the info frame can use the up to date version.
		ListModel<Moment> mommodel = moments.getModel();

		if (mommodel.getSize() > 0)
			{
			Moment node = (Moment) mommodel.getElementAt(moments.getSelectedIndex());
			if (node != null)
				{
				actions.setActionContainer(node);
				}
			}
		if (infoFrame == null)
			{
			infoFrame = new ResourceInfoFrame();
			}
		infoFrame.updateTimelineInfo(res.reference);
		infoFrame.setVisible(true);
		}

	@Override
	public void dispose()
		{
		super.dispose();
		if (infoFrame != null)
			{
			infoFrame.dispose();
			}
		((ActionListEditor) editor).dispose();
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
			NumberField field = new NumberField(sn + (but == add ? 1 : 0));
			//			field.setPreferredSize(new Dimension(80,20));
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
					moments.setSelectedIndex(-p);
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
			moments.setListData(res.moments.toArray(new Moment[res.moments.size()]));
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
			moments.setListData(res.moments.toArray(new Moment[res.moments.size()]));
			moments.setSelectedIndex(Math.min(res.moments.size() - 1,p));
			return;
			}
		if (but == showInfo)
			{
			showInfoFrame();
			return;
			}
		if (but == edit)
			{
			editSelectedMoment();
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
			moments.setListData(res.moments.toArray(new Moment[res.moments.size()]));
			}
		if (but == shift)
			{
			if (res.moments.size() == 0) return;
			Moment m = (Moment) moments.getSelectedValue();
			int sn = (m == null) ? 0 : m.stepNo;
			String ttl = Messages.getString("TimelineFrame.MOM_SHIFT"); //$NON-NLS-1$
			JPanel pane = new JPanel(new GridLayout(0,2));
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_START"))); //$NON-NLS-1$
			NumberField iStart = new NumberField(sn);
			iStart.setPreferredSize(new Dimension(80,20));
			pane.add(iStart);
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_END"))); //$NON-NLS-1$
			NumberField iEnd = new NumberField(sn);
			iEnd.setPreferredSize(new Dimension(80,20));
			pane.add(iEnd);
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_AMOUNT"))); //$NON-NLS-1$
			NumberField iAmt = new NumberField(1);
			iAmt.setPreferredSize(new Dimension(80,20));
			pane.add(iAmt);
			int ret = JOptionPane.showConfirmDialog(this,pane,ttl,JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) return;
			int p = res.shiftMoments(iStart.getIntValue(),iEnd.getIntValue(),iAmt.getIntValue());
			moments.setListData(res.moments.toArray(new Moment[res.moments.size()]));
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
			NumberField iStart = new NumberField(0,Integer.MAX_VALUE,sn);
			iStart.setPreferredSize(new Dimension(80,20));
			pane.add(iStart);
			pane.add(new JLabel(Messages.getString("TimelineFrame.MOM_END"))); //$NON-NLS-1$
			NumberField iEnd = new NumberField(0,Integer.MAX_VALUE,sn);
			iEnd.setPreferredSize(new Dimension(80,20));
			pane.add(iEnd);
			int ret = JOptionPane.showConfirmDialog(this,pane,ttl,JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) return;
			actions.save(); //prevents "fresh" actions from being overwritten
			int p = res.mergeMoments(iStart.getIntValue(),iEnd.getIntValue());
			moments.setListData(res.moments.toArray(new Moment[res.moments.size()]));
			moments.setSelectedIndex(p);
			return;
			}
		super.actionPerformed(e);
		}

	private void editSelectedMoment()
		{
		int p = moments.getSelectedIndex();
		if (p == -1) return;
		Action a = null;
		LibAction la = null;
		Boolean prependNew = true;

		for (int i = 0; i < actions.model.list.size(); i++)
			{
			a = actions.model.list.get(i);
			la = a.getLibAction();
			if (la.actionKind == Action.ACT_CODE)
				{
				prependNew = false;
				break;
				}
			}

		if (prependNew)
			{
			a = new Action(LibManager.codeAction);
			((ActionListModel) actions.getModel()).add(0,a);
			actions.setSelectedValue(a,true);
			}

		MDIFrame af = ActionList.openActionFrame(actions.parent.get(),a);
		Object momentitem = moments.getSelectedValue();
		af.setTitle(this.name.getText() + " : " + momentitem.toString());
		af.setFrameIcon(LGM.getIconForKey("MomentNode.STEP"));
		return;
		}

	//Moments selection changed
	public void valueChanged(ListSelectionEvent e)
		{
		if (e.getValueIsAdjusting()) return;
		Moment m = (Moment) moments.getSelectedValue();
		actions.setActionContainer(m);
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
	}
