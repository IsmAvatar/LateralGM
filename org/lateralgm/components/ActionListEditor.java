/*
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.Border;

import org.lateralgm.components.ActionList.ActionListModel;
import org.lateralgm.components.ActionList.LibActionTransferHandler;
import org.lateralgm.components.visual.VTextIcon;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.library.Library;
import org.lateralgm.resources.sub.Action;

public class ActionListEditor extends JPanel
	{
	private static final long serialVersionUID = 1L;

	public ActionListEditor(ActionList list)
		{
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		JLabel lab = new JLabel(Messages.getString("GmObjectFrame.ACTIONS")); //$NON-NLS-1$
		JScrollPane scroll = new JScrollPane(list);
		JTabbedPane actionTabs = makeLibraryTabs(list);

		SequentialGroup orientationGroup = layout.createSequentialGroup();

		if (Prefs.rightOrientation) {
			orientationGroup.addComponent(actionTabs,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE);
		}

		orientationGroup.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(lab)
		/*		*/.addComponent(scroll,DEFAULT_SIZE,240,DEFAULT_SIZE));

		if (!Prefs.rightOrientation) {
			orientationGroup.addComponent(actionTabs,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE);
		}

		layout.setHorizontalGroup(orientationGroup);
		layout.setVerticalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lab)
		/*		*/.addComponent(scroll))
		/**/.addComponent(actionTabs));
		}

	private static JPanel makeLabelPane(String name)
		{
		JPanel lp = new JPanel(new GridLayout(0,3,0,0));
		Border mb = BorderFactory.createMatteBorder(1,0,0,0,new Color(184,184,184));
		Border tb = BorderFactory.createTitledBorder(mb,name);
		lp.setBorder(tb);
		return lp;
		}

	public static JTabbedPane makeLibraryTabs(ActionList actions)
		{
		JTabbedPane tp = new JTabbedPane(Prefs.rightOrientation ? JTabbedPane.LEFT : JTabbedPane.RIGHT);

		tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		for (Library l : LibManager.libs)
			{
			JPanel p = new JPanel();
			JPanel lp = null;
			GroupLayout layout = new GroupLayout(p);
			p.setLayout(layout);
			ParallelGroup hg = layout.createParallelGroup();
			SequentialGroup vg = layout.createSequentialGroup();
			layout.setHorizontalGroup(hg);
			layout.setVerticalGroup(vg);
			for (LibAction la : l.libActions)
				{
				if (la.hidden || la.actionKind == Action.ACT_SEPARATOR) continue;
				JLabel b;
				if (la.actionKind == Action.ACT_LABEL)
					{
					lp = makeLabelPane(la.name);
					hg.addComponent(lp,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
					vg.addComponent(lp,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
					continue;
					}
				if (la.actionKind == Action.ACT_PLACEHOLDER)
					b = new JLabel();
				else
					b = new LibActionButton(la,actions);
				b.setHorizontalAlignment(JLabel.LEFT);
				b.setVerticalAlignment(JLabel.TOP);
				b.setPreferredSize(new Dimension(30,30));
				if (lp == null)
					{
					lp = makeLabelPane(null);
					hg.addComponent(lp,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
					vg.addComponent(lp,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
					}
				lp.add(b);
				}
			tp.addTab(l.tabCaption,p);
			if (LGM.javaVersion >= 10600)
				tp.setTabComponentAt(tp.getTabCount() - 1,new JLabel(new VTextIcon(tp,l.tabCaption,
						tp.getTabPlacement() == JTabbedPane.LEFT ?
								VTextIcon.ROTATE_LEFT : VTextIcon.ROTATE_DEFAULT)));
			}
		return tp;
		}

	public static class LibActionButton extends JLabel
		{
		private static final long serialVersionUID = 1L;
		private static LibActionTransferHandler transferHandler = new LibActionTransferHandler();
		private LibAction libAction;
		private ActionList list;

		public LibActionButton(LibAction la, ActionList list)
			{
			super(new ImageIcon(la.actImage));
			this.list = list;
			setToolTipText(la.description);
			libAction = la;
			setTransferHandler(transferHandler);
			}

		public void processMouseEvent(MouseEvent e)
			{
			if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1)
				{
				JComponent c = (JComponent) e.getSource();
				TransferHandler handler = c.getTransferHandler();
				handler.exportAsDrag(c,e,TransferHandler.COPY);
				}
			else if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON3
					&& list.getActionContainer() != null)
				{
				Action act = new Action(libAction);
				((ActionListModel) list.getModel()).add(act);
				list.setSelectedValue(act,true);
				ActionList.openActionFrame(list.parent.get(),act);
				}
			super.processMouseEvent(e);
			}

		public LibAction getLibAction()
			{
			return libAction;
			}
		}

	public void dispose()
		{
		ActionList.closeFrames();
		}

	}
