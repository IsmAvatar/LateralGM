/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.library.Library;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.ScriptFrame.GMLTextArea;

public class GmObjectFrame extends ResourceFrame<GmObject> implements ActionListener,
		TreeSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static ImageIcon frameIcon = LGM.getIconForKey("GmObjectFrame.GMOBJECT"); //$NON-NLS-1$$
	private static ImageIcon saveIcon = LGM.getIconForKey("GmObjectFrame.SAVE"); //$NON-NLS-1$

	public JLabel preview;
	public JComboBox sprite;
	public JButton newsprite;
	public JButton edit;
	public JButton delete;
	public JButton duplicate;
	public JButton shift;
	public JButton merge;
	public JButton clear;

	public JTree events;
	public JList actions;
	public GMLTextArea code;

	public GmObjectFrame(GmObject res, ResNode node)
		{
		super(res,node);

		setSize(560,385);
		setMinimumSize(new Dimension(560,385));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setFrameIcon(frameIcon);

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setPreferredSize(new Dimension(180,280));

		JLabel lab = new JLabel(Messages.getString("GmObjectFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(160,14));
		side1.add(lab);
		name.setPreferredSize(new Dimension(160,20));
		side1.add(name);

		JPanel origin = new JPanel(new FlowLayout());
		String t = Messages.getString("GmObjectFrame.SPRITE"); //$NON-NLS-1$
		origin.setBorder(BorderFactory.createTitledBorder(t));
		origin.setPreferredSize(new Dimension(180,80));
		Sprite s = LGM.currentFile.sprites.get(res.sprite);
		BufferedImage bi = s.getSubImage(0);
		ImageIcon ii;
		if (s.transparent)
			ii = new ImageIcon(Util.getTransparentIcon(bi));
		else
			ii = new ImageIcon(bi);
		preview = new JLabel(ii);
		preview.setPreferredSize(new Dimension(16,16));
		origin.add(preview);
		sprite = new JComboBox(LGM.currentFile.sprites.resources.toArray());
		sprite.setPreferredSize(new Dimension(140,20));
		origin.add(sprite);
		newsprite = new JButton(Messages.getString("GmObjectFrame.NEW")); //$NON-NLS-1$
		newsprite.setPreferredSize(new Dimension(80,20));
		newsprite.addActionListener(this);
		origin.add(newsprite);
		edit = new JButton(Messages.getString("GmObjectFrame.EDIT")); //$NON-NLS-1$
		edit.setPreferredSize(new Dimension(80,20));
		edit.addActionListener(this);
		origin.add(edit);
		side1.add(origin);

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
		save.setIcon(saveIcon);
		side1.add(save);

		JPanel side2 = new JPanel(new BorderLayout());
		side2.setMaximumSize(new Dimension(90,Integer.MAX_VALUE));
		lab = new JLabel(Messages.getString("GmObjectFrame.EVENTS")); //$NON-NLS-1$
		side2.add(lab,"North");
		events = makeEventTree(res);
		JScrollPane scroll = new JScrollPane(events);
		scroll.setPreferredSize(new Dimension(90,260));
		side2.add(scroll,"Center");

		add(side1);
		add(side2);

		if (false)
			{
			code = new GMLTextArea();
			JScrollPane codePane = new JScrollPane(code);
			add(codePane);
			}
		else
			{
			JPanel side3 = new JPanel(new BorderLayout());
			side3.setMinimumSize(new Dimension(0,0));
			side3.setPreferredSize(new Dimension(100,260));
			lab = new JLabel(Messages.getString("GmObjectFrame.ACTIONS")); //$NON-NLS-1$
			side3.add(lab,"North");
			actions = new JList();
			scroll = new JScrollPane(actions);
			side3.add(scroll,"Center");

			JTabbedPane side4 = getLibraryTabs();
			side4.setPreferredSize(new Dimension(165,260)); //319

			add(side3);
			add(side4);
			}
		}

	public JTree makeEventTree(GmObject res)
		{
		DefaultMutableTreeNode rootEvent = new DefaultMutableTreeNode();
		for (int m = 0; m < 11; m++)
			{
			MainEvent me = res.mainEvents[m];
			ArrayList<Event> ale = me.events;
			if (ale.size() == 1)
				{
				rootEvent.add(new DefaultMutableTreeNode(ale.get(0)));
				}
			if (ale.size() > 1)
				{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(
						Messages.getString("MainEvent.EVENT" + m)); //$NON-NLS-1$
				rootEvent.add(node);
				for (Event e : ale)
					node.add(new DefaultMutableTreeNode(e));
				}
			}
		JTree tree = new JTree(rootEvent);
		tree.setScrollsOnExpand(true);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
			{
				private static final long serialVersionUID = 1L;
//				private ResNode last;

				public Component getTreeCellRendererComponent(JTree tree, Object val, boolean sel,
						boolean exp, boolean leaf, int row, boolean focus)
					{
//					last = (ResNode) val;
					super.getTreeCellRendererComponent(tree,val,sel,exp,leaf,row,focus);
					return this;
					}
			};
		tree.setCellRenderer(renderer);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		return tree;
		}

	//possibly extract to some place like resources.library.LibManager
	public JTabbedPane getLibraryTabs()
		{
		JTabbedPane tp = new JTabbedPane(JTabbedPane.RIGHT);

		//should not have to reload libraries every time
		String ll = getLibraryLocation();
		if (ll == null) return tp;
		LibManager.autoLoad(ll);

		for (Library l : LibManager.libs)
			{
			if (l.advanced) continue;
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
			for (LibAction la : l.libActions)
				{
				if (la.hidden || la.actionKind == Action.ACT_SEPARATOR) continue;
				if (la.advanced && !la.advanced) continue;
				JLabel b;
				if (la.actionKind == Action.ACT_LABEL)
					{
					b = new JLabel();
					b.setBorder(BorderFactory.createTitledBorder(la.name));
					b.setPreferredSize(new Dimension(90,14));
					p.add(b);
					continue;
					}
				if (la.actionKind == Action.ACT_PLACEHOLDER)
					b = new JLabel();
				else
					b = new JLabel(new ImageIcon(org.lateralgm.main.Util.getTransparentIcon(la.actImage)));
				b.setHorizontalAlignment(JLabel.LEFT);
				b.setVerticalAlignment(JLabel.TOP);
				b.setPreferredSize(new Dimension(32,32));
				p.add(b);
				}
			tp.add(l.tabCaption,p);
			}
		return tp;
		}

	//extract to program start or give something to main.Prefs
	private String libraryLocation = "org/lateralgm/resources/library/lib/"; //null; //singleton

	public String getLibraryLocation()
		{
		//see if our singleton exists
		if (libraryLocation != null)
			{
			if (libraryLocation.equals("")) return null; //we know we have no libraries
			return libraryLocation;
			}

		//see if we have a "lib" folder

		//try and find GM6 (it should have a "lib" folder)

		//ask the user
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Locate GM6 or Libraries");
		File loc = new File("");
		while (!loc.exists())
			{
			if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				{
				libraryLocation = "";
				return null;
				}
			loc = fc.getSelectedFile();
			}

		//see if this folder is just GM6 or the Libraries folder
		/*
		 * How this is achieved requires a little philosophy
		 * Should we check to see if the folder's name is "lib"?
		 * Should we check if there's any .lib files here?
		 * Should we check to see if there is a "lib" subfolder?
		 * Or should we not ask the user and just require a "lib" folder?
		 */

		return "";
		}

	@Override
	public boolean resourceChanged()
		{
		return true;
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.gmObjects.replace(res.getId(),resOriginal);
		}

	@Override
	public void updateResource()
		{
		res.setName(name.getText());

		resOriginal = (GmObject) res.copy(false,null);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == newsprite)
			{
			return;
			}
		super.actionPerformed(e);
		}

	//Moments selection changed
	public void valueChanged(TreeSelectionEvent e)
		{
		//		Moment m = (Moment) events.getSelectedValue();
		//		if (m == null) return;
		//		actions.setListData(m.actions.toArray());
		}
	}
