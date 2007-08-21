/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM. Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.View;

public class RoomFrame extends ResourceFrame<Room> implements ListSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon CODE_ICON = LGM.getIconForKey("RoomFrame.CODE"); //$NON-NLS-1$

	public JLabel statX, statY, statObj, statId;
	//Settings
	public JTextField sCaption;
	public IntegerField sWidth, sHeight, sSpeed;
	public JCheckBox sPersistent;
	public JButton sCreationCode, sShow;
	public IntegerField sSnapX, sSnapY;
	public JCheckBoxMenuItem sSObj, sSTile, sSBack, sSFore, sSView;
	//Views
	public JCheckBox vEnabled, vVisible;
	public JList vList;
	/**Guaranteed valid version of vList.getLastSelectedIndex()*/
	public int lastValidView = 0;
	public IntegerField vRX, vRY, vRW, vRH;
	public IntegerField vPX, vPY, vPW, vPH;
	public ResourceMenu vObj;
	public IntegerField vOHBor, vOVBor, vOHSp, vOVSp;

	//TODO:
	public JPanel makeObjectsPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	public JPanel makeSettingsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());
		//		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		//		panel.setMinimumSize(new Dimension(170,270));
		//		panel.setPreferredSize(new Dimension(170,270));

		JLabel lab = new JLabel(Messages.getString("RoomFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,14));
		panel.add(lab);
		name.setPreferredSize(new Dimension(120,20));
		panel.add(name);

		lab = new JLabel(Messages.getString("RoomFrame.CAPTION")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(120,14));
		panel.add(lab);
		sCaption = new JTextField(res.caption);
		sCaption.setPreferredSize(new Dimension(165,20));
		panel.add(sCaption);

		lab = new JLabel(Messages.getString("RoomFrame.WIDTH")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,14));
		panel.add(lab);
		sWidth = new IntegerField(1,999999,res.width);
		sWidth.setPreferredSize(new Dimension(120,20));
		panel.add(sWidth);

		lab = new JLabel(Messages.getString("RoomFrame.HEIGHT")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,14));
		panel.add(lab);
		sHeight = new IntegerField(1,999999,res.height);
		sHeight.setPreferredSize(new Dimension(120,20));
		panel.add(sHeight);

		lab = new JLabel(Messages.getString("RoomFrame.SPEED")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,32));
		panel.add(lab);
		sSpeed = new IntegerField(1,9999,res.speed);
		sSpeed.setPreferredSize(new Dimension(80,20));
		panel.add(sSpeed);
		addGap(panel,34,1);

		String str = Messages.getString("RoomFrame.PERSISTENT"); //$NON-NLS-1$
		sPersistent = new JCheckBox(str,res.persistent);
		sPersistent.setPreferredSize(new Dimension(150,20));
		panel.add(sPersistent);

		str = Messages.getString("RoomFrame.CREATION_CODE"); //$NON-NLS-1$
		sCreationCode = new JButton(str,CODE_ICON);
		//		creationCode.setPreferredSize(new Dimension());
		sCreationCode.addActionListener(this);
		panel.add(sCreationCode);

		JPanel p2 = new JPanel(new FlowLayout());
		//		p2.setBorder(BorderFactory.createCompoundBorder());
		p2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		p2.setPreferredSize(new Dimension(170,90));

		lab = new JLabel(Messages.getString("RoomFrame.SNAP_X")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(44,14));
		p2.add(lab);
		sSnapX = new IntegerField(1,999,res.snapX);
		sSnapX.setPreferredSize(new Dimension(80,20));
		p2.add(sSnapX);
		addGap(p2,25,1);

		lab = new JLabel(Messages.getString("RoomFrame.SNAP_Y")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(44,14));
		p2.add(lab);
		sSnapY = new IntegerField(1,999,res.snapY);
		sSnapY.setPreferredSize(new Dimension(80,20));
		p2.add(sSnapY);
		addGap(p2,25,1);

		final JPopupMenu showMenu = new JPopupMenu();
		String st = Messages.getString("RoomFrame.SHOW_OBJECTS"); //$NON-NLS-1$
		sSObj = new JCheckBoxMenuItem(st);
		sSObj.addActionListener(this);
		showMenu.add(sSObj);
		st = Messages.getString("RoomFrame.SHOW_TILES"); //$NON-NLS-1$
		sSTile = new JCheckBoxMenuItem(st);
		sSTile.addActionListener(this);
		showMenu.add(sSTile);
		st = Messages.getString("RoomFrame.SHOW_BACKGROUNDS"); //$NON-NLS-1$
		sSBack = new JCheckBoxMenuItem(st);
		sSBack.addActionListener(this);
		showMenu.add(sSBack);
		st = Messages.getString("RoomFrame.SHOW_FOREGROUNDS"); //$NON-NLS-1$
		sSFore = new JCheckBoxMenuItem(st);
		sSFore.addActionListener(this);
		showMenu.add(sSFore);
		st = Messages.getString("RoomFrame.SHOW_VIEWS"); //$NON-NLS-1$
		sSView = new JCheckBoxMenuItem(st);
		sSView.addActionListener(this);
		showMenu.add(sSView);

		sShow = new JButton(Messages.getString("RoomFrame.SHOW")); //$NON-NLS-1$
		sShow.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					showMenu.show(sShow,0,sShow.getHeight());
					}
			});
		p2.add(sShow);

		panel.add(p2);

		return panel;
		}

	//TODO:
	public JPanel makeTilesPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	//TODO:
	public JPanel makeBackgroundsPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	public JPanel makeViewsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());
		//		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		String st = Messages.getString("RoomFrame.ENABLE_VIEWS"); //$NON-NLS-1$
		vEnabled = new JCheckBox(st,res.enableViews);
		panel.add(vEnabled);

		JLabel[] viewLabs = new JLabel[8];
		for (int i = 0; i < 8; i++)
			{
			viewLabs[i] = new JLabel(Messages.getString("RoomFrame.VIEW") + i);
			viewLabs[i].setFont(viewLabs[i].getFont().deriveFont(
					res.views[i].enabled ? Font.BOLD : Font.PLAIN));
			viewLabs[i].setOpaque(true);
			}

		vList = new JList(viewLabs);
		vList.setVisibleRowCount(4);
		vList.addListSelectionListener(this);
		vList.setPreferredSize(new Dimension(160,200));
		class ViewLabelRenderer implements ListCellRenderer
			{
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList list, Object val, int ind,
					boolean selected, boolean focus)
				{
				JLabel lab = (JLabel) val;
				if (selected)
					{
					lab.setBackground(list.getSelectionBackground());
					lab.setForeground(list.getSelectionForeground());
					}
				else
					{
					lab.setBackground(list.getBackground());
					lab.setForeground(list.getForeground());
					}
				return lab;
				}
			}
		vList.setCellRenderer(new ViewLabelRenderer());
		panel.add(new JScrollPane(vList));

		st = Messages.getString("RoomFrame.VIEW_ENABLED"); //$NON-NLS-1$
		vVisible = new JCheckBox(st,res.views[0].enabled);
		vVisible.addActionListener(this);
		panel.add(vVisible);

		JPanel p = new JPanel(new FlowLayout());
		st = Messages.getString("RoomFrame.VIEW_IN_ROOM"); //$NON_NLS_1$
		p.setBorder(BorderFactory.createTitledBorder(st));
		p.setPreferredSize(new Dimension(130,80));
		JLabel lab = new JLabel(Messages.getString("RoomFrame.VIEW_X")); //$NON_NLS_1$
		p.add(lab);
		vRX = new IntegerField(0,999999,res.views[0].viewX);
		vRX.setPreferredSize(new Dimension(32,20));
		p.add(vRX);
		lab = new JLabel(Messages.getString("RoomFrame.VIEW_W")); //$NON_NLS_1$
		p.add(lab);
		vRW = new IntegerField(1,999999,res.views[0].viewW);
		vRW.setPreferredSize(new Dimension(32,20));
		p.add(vRW);
		lab = new JLabel(Messages.getString("RoomFrame.VIEW_Y")); //$NON_NLS_1$
		p.add(lab);
		vRY = new IntegerField(0,999999,res.views[0].viewY);
		vRY.setPreferredSize(new Dimension(32,20));
		p.add(vRY);
		addGap(p,2,0);
		lab = new JLabel(Messages.getString("RoomFrame.VIEW_H")); //$NON_NLS_1$
		p.add(lab);
		vRH = new IntegerField(1,999999,res.views[0].viewH);
		vRH.setPreferredSize(new Dimension(32,20));
		p.add(vRH);
		panel.add(p);

		p = new JPanel(new FlowLayout());
		st = Messages.getString("RoomFrame.PORT"); //$NON_NLS_1$
		p.setBorder(BorderFactory.createTitledBorder(st));
		p.setPreferredSize(new Dimension(130,80));
		lab = new JLabel(Messages.getString("RoomFrame.PORT_X")); //$NON_NLS_1$
		p.add(lab);
		vPX = new IntegerField(0,999999,res.views[0].portX);
		vPX.setPreferredSize(new Dimension(32,20));
		p.add(vPX);
		lab = new JLabel(Messages.getString("RoomFrame.PORT_W")); //$NON_NLS_1$
		p.add(lab);
		vPW = new IntegerField(1,999999,res.views[0].portW);
		vPW.setPreferredSize(new Dimension(32,20));
		p.add(vPW);
		lab = new JLabel(Messages.getString("RoomFrame.PORT_Y")); //$NON_NLS_1$
		p.add(lab);
		vPY = new IntegerField(0,999999,res.views[0].portY);
		vPY.setPreferredSize(new Dimension(32,20));
		p.add(vPY);
		addGap(p,2,0);
		lab = new JLabel(Messages.getString("RoomFrame.PORT_H")); //$NON_NLS_1$
		p.add(lab);
		vPH = new IntegerField(1,999999,res.views[0].portH);
		vPH.setPreferredSize(new Dimension(32,20));
		p.add(vPH);
		panel.add(p);

		p = new JPanel(new FlowLayout());
		st = Messages.getString("RoomFrame.FOLLOW"); //$NON_NLS_1$
		p.setBorder(BorderFactory.createTitledBorder(st));
		p.setPreferredSize(new Dimension(150,104));
		vObj = new ResourceMenu(Room.GMOBJECT,"<no object>",true,110);
		vObj.setSelected(LGM.currentFile.gmObjects.get(res.views[0].objectFollowing));
		p.add(vObj);
		lab = new JLabel(Messages.getString("RoomFrame.HBOR")); //$NON_NLS_1$
		p.add(lab);
		vOHBor = new IntegerField(0,32000,res.views[0].hbor);
		vOHBor.setPreferredSize(new Dimension(32,20));
		p.add(vOHBor);
		lab = new JLabel(Messages.getString("RoomFrame.HSP")); //$NON_NLS_1$
		p.add(lab);
		vOHSp = new IntegerField(-1,32000,res.views[0].hspeed);
		vOHSp.setPreferredSize(new Dimension(32,20));
		p.add(vOHSp);
		lab = new JLabel(Messages.getString("RoomFrame.VBOR")); //$NON_NLS_1$
		p.add(lab);
		vOVBor = new IntegerField(0,32000,res.views[0].vbor);
		vOVBor.setPreferredSize(new Dimension(32,20));
		p.add(vOVBor);
		lab = new JLabel(Messages.getString("RoomFrame.VSP")); //$NON_NLS_1$
		p.add(lab);
		vOVSp = new IntegerField(-1,32000,res.views[0].vspeed);
		vOVSp.setPreferredSize(new Dimension(32,20));
		p.add(vOVSp);
		panel.add(p);

		vList.setSelectedIndex(lastValidView);

		return panel;
		}

	public RoomFrame(Room res, ResNode node)
		{
		super(res,node);

		final int sizeWidth = 450;
		final int sizeHeight = 550;
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setMinimumSize(new Dimension(sizeWidth,sizeHeight));
		if (!res.rememberWindowSize || res.editorWidth < sizeWidth || res.editorHeight < sizeHeight)
			setSize(sizeWidth,sizeHeight);
		else
			setSize(res.editorWidth,res.editorHeight);

		JPanel pane = new JPanel();
		//		pane.setLayout(new FlowLayout());
		//		pane.setLayout(new BoxLayout(pane,BoxLayout.PAGE_AXIS));
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.setMinimumSize(new Dimension(200,350));
		pane.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
		pane.setPreferredSize(new Dimension(200,350));

		JTabbedPane tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.addTab(Messages.getString("RoomFrame.TAB_OBJECTS"),makeObjectsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_SETTINGS"),makeSettingsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_TILES"),makeTilesPane()); //$NON-NLS-1$
		String bks = Messages.getString("RoomFrame.TAB_BACKGROUNDS"); //$NON-NLS-1$
		tabs.addTab(bks,makeBackgroundsPane());
		tabs.addTab(Messages.getString("RoomFrame.TAB_VIEWS"),makeViewsPane()); //$NON-NLS-1$
		tabs.setSelectedIndex(res.currentTab);
		//		tabs.setPreferredSize(new Dimension());
		pane.add(tabs);

		FlowLayout fl = new FlowLayout();
		fl.setHgap(0);
		fl.setVgap(0);
		JPanel cont = new JPanel(fl);
		cont.setMaximumSize(new Dimension(130,22));
		cont.setMinimumSize(new Dimension(130,22));
		save.setText(Messages.getString("RoomFrame.SAVE")); //$NON-NLS-1$
		//		addDim(pane,save,130,24);
		cont.add(save);
		pane.add(cont);
		add(pane);

		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.setPreferredSize(new Dimension(240,280));

		//TODO: 1.6 - 1.7 Add room editor area
		pane.add(new JScrollPane());

		//TODO: 1.6 - 1.7 Work on status bar
		JPanel stat = new JPanel();
		stat.setMaximumSize(new Dimension(200,11));
		statX = new JLabel("x:");
		statX.setPreferredSize(new Dimension(25,14));
		stat.add(statX);
		//		JToolBar.Separator sep = new JToolBar.Separator();
		//		sep.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		//		sep.setPreferredSize(new Dimension());
		JLabel l = new JLabel();
		l.setMinimumSize(new Dimension(5,5));
		l.setMaximumSize(new Dimension(5,5));
		stat.add(l);

		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		sep.setMinimumSize(new Dimension(2,13));
		sep.setMaximumSize(new Dimension(2,13));
		//		stat.add(sep);

		l = new JLabel();
		l.setMinimumSize(new Dimension(5,5));
		l.setMaximumSize(new Dimension(5,5));
		stat.add(l);
		//		addDim(stat,sep,10,10);
		//		stat.add(sep);

		//		stat.addSeparator(new Dimension(10,10));
		statY = new JLabel("y:");
		statY.setPreferredSize(new Dimension(25,13));
		stat.add(statY);
		//		stat.add(new JToolBar.Separator());
		statObj = new JLabel("object:");
		statObj.setPreferredSize(new Dimension(50,13));
		stat.add(statObj);
		//		stat.add(new JToolBar.Separator());
		statId = new JLabel("id:");
		statId.setPreferredSize(new Dimension(50,13));
		stat.add(statId);

		pane.add(stat);
		add(pane);
		}

	//TODO:
	@Override
	public boolean resourceChanged()
		{
		return true;
		/*return !resOriginal.getName().equals(name.getText())
		 || resOriginal.transparent != transparent.isSelected()
		 || resOriginal.smoothEdges != smooth.isSelected()
		 || resOriginal.preload != preload.isSelected()
		 || resOriginal.useAsTileSet != tileset.isSelected()
		 || resOriginal.tileWidth != tWidth.getIntValue()
		 || resOriginal.tileHeight != tWidth.getIntValue()
		 || resOriginal.horizOffset != hOffset.getIntValue()
		 || resOriginal.vertOffset != vOffset.getIntValue()
		 || resOriginal.horizSep != hSep.getIntValue() || resOriginal.vertSep != vSep.getIntValue();*/
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.rooms.replace(res.getId(),resOriginal);
		}

	//TODO:
	@Override
	public void updateResource()
		{
		res.setName(name.getText());
		/*res.transparent = transparent.isSelected();
		 res.smoothEdges = smooth.isSelected();
		 res.preload = preload.isSelected();
		 res.useAsTileSet = tileset.isSelected();
		 res.tileWidth = tWidth.getIntValue();
		 res.tileHeight = tWidth.getIntValue();
		 res.horizOffset = hOffset.getIntValue();
		 res.vertOffset = vOffset.getIntValue();
		 res.horizSep = hSep.getIntValue();
		 res.vertSep = vSep.getIntValue();*/
		resOriginal = res.copy();
		}

	//TODO:
	public void actionPerformed(ActionEvent e)
		{
		//Settings: creationCode, obj, tile, back, fore, view
		if (e.getSource().equals(vVisible))
			{
			JLabel lab = ((JLabel) vList.getSelectedValue());
			res.views[lastValidView].enabled = vVisible.isSelected();
			lab.setFont(lab.getFont().deriveFont(vVisible.isSelected() ? Font.BOLD : Font.PLAIN));
			vList.updateUI();
			return;
			}

		super.actionPerformed(e);
		}

	public void valueChanged(ListSelectionEvent e)
		{
		if (e.getValueIsAdjusting()) return;
		if (vList.getSelectedIndex() == -1)
			{
			vList.setSelectedIndex(lastValidView);
			return;
			}
		lastValidView = vList.getSelectedIndex();

		View v = res.views[lastValidView];
		vVisible.setSelected(v.enabled);
		vRX.setIntValue(v.viewX);
		vRY.setIntValue(v.viewY);
		vRW.setIntValue(v.viewW);
		vRH.setIntValue(v.viewH);
		vPX.setIntValue(v.portX);
		vPY.setIntValue(v.portY);
		vPW.setIntValue(v.portW);
		vPH.setIntValue(v.portH);
		vObj.setSelected(LGM.currentFile.gmObjects.get(v.objectFollowing));
		vOHBor.setIntValue(v.hbor);
		vOVBor.setIntValue(v.vbor);
		vOHSp.setIntValue(v.hspeed);
		vOVSp.setIntValue(v.vspeed);
		}
	}
