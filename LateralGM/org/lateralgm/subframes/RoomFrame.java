/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM. Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package org.lateralgm.subframes;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.RoomEditor;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

//TODO: Feature: Zoom for RoomEditor (add buttons here first)
public class RoomFrame extends ResourceFrame<Room> implements ListSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon CODE_ICON = LGM.getIconForKey("RoomFrame.CODE"); //$NON-NLS-1$

	//prevents List selection updates from firing ResourceMenu changes
	public static boolean manualUpdate = true;
	public RoomEditor editor;
	public JTabbedPane tabs;
	public JLabel statX, statY, statObj, statId;
	//Objects
	public JCheckBox oUnderlying, oLocked;
	public JList oList;
	private Instance lastObj = null; //non-guaranteed copy of oList.getLastSelectedValue()
	public JButton oAdd, oDel;
	public ResourceMenu<GmObject> oSource;
	public IntegerField oX, oY;
	public JButton oCreationCode;
	//Settings
	public JTextField sCaption;
	public IntegerField sWidth, sHeight, sSpeed, sSnapX, sSnapY;
	public JCheckBox sPersistent, sGridVis, sGridIso;
	public JButton sCreationCode, sShow;
	public JCheckBoxMenuItem sSObj, sSTile, sSBack, sSFore, sSView;
	//Tiles
	public JCheckBox tUnderlying, tLocked;
	public JList tList;
	private Tile lastTile = null; //non-guaranteed copy of tList.getLastSelectedValue()
	public JButton tAdd, tDel;
	public ResourceMenu<Background> tSource;
	public IntegerField tsX, tsY, tX, tY, tLayer;
	//Backgrounds
	public JCheckBox bDrawColor, bVisible, bForeground, bTileH, bTileV, bStretch;
	public ColorSelect bColor;
	public JList bList;
	/**Guaranteed valid version of bList.getLastSelectedIndex()*/
	public int lastValidBack = 0;
	public ResourceMenu<Background> bSource;
	public IntegerField bX, bY, bH, bV;
	//Views
	public JCheckBox vEnabled, vVisible;
	public JList vList;
	/**Guaranteed valid version of vList.getLastSelectedIndex()*/
	public int lastValidView = 0;
	public IntegerField vRX, vRY, vRW, vRH;
	public IntegerField vPX, vPY, vPW, vPH;
	public ResourceMenu<GmObject> vObj;
	public IntegerField vOHBor, vOVBor, vOHSp, vOVSp;

	public JPanel makeObjectsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		panel.add(new JLabel(Messages.getString("RoomFrame.WIP"))); //$NON-NLS-1$
		oUnderlying = new JCheckBox(Messages.getString("RoomFrame.OBJ_UNDERLYING")); //$NON-NLS-1$
		oUnderlying.setSelected(res.rememberWindowSize ? res.deleteUnderlyingObjects : true);
		panel.add(oUnderlying);
		JLabel lab = new JLabel(Messages.getString("RoomFrame.OBJ_INSTANCES")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(150,20));
		panel.add(lab);
		oList = new JList(res.instances.toArray());
		//		oList.setDragEnabled(true);
		//		oList.setDropMode(DropMode.INSERT);
		//		oList.setTransferHandler(null);
		oList.addListSelectionListener(this);
		oList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		oList.setVisibleRowCount(8);
		oList.setCellRenderer(new ListComponentRenderer()
			{
				public Component getListCellRendererComponent(JList list, Object val, int ind,
						boolean selected, boolean focus)
					{
					Instance i = (Instance) val;
					GmObject go = i.gmObjectId.getRes();
					JLabel lab = new JLabel(go.getName() + " " + i.instanceId,
							GmTreeGraphics.getSpriteIcon(go.sprite),JLabel.LEFT);
					super.getListCellRendererComponent(list,lab,ind,selected,focus);
					lab.setOpaque(true);
					return lab;
					}
			});
		JScrollPane sp = new JScrollPane(oList);
		sp.setPreferredSize(new Dimension(190,128));
		panel.add(sp);
		oAdd = new JButton(Messages.getString("RoomFrame.OBJ_ADD")); //$NON-NLS-1$
		oAdd.addActionListener(this);
		panel.add(oAdd);
		oDel = new JButton(Messages.getString("RoomFrame.OBJ_DELETE")); //$NON-NLS-1$
		oDel.addActionListener(this);
		panel.add(oDel);
		oSource = new ResourceMenu<GmObject>(Room.GMOBJECT,"<no object>",true,110);
		oSource.setPreferredSize(new Dimension(120,20));
		oSource.addActionListener(this);
		panel.add(oSource);
		oLocked = new JCheckBox(Messages.getString("RoomFrame.OBJ_LOCKED")); //$NON-NLS-1$
		oLocked.setPreferredSize(new Dimension(180,20));
		oLocked.setHorizontalAlignment(JCheckBox.CENTER);
		panel.add(oLocked);
		panel.add(new JLabel(Messages.getString("RoomFrame.OBJ_X"))); //$NON-NLS-1$
		oX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		oX.setPreferredSize(new Dimension(60,20));
		panel.add(oX);
		panel.add(new JLabel(Messages.getString("RoomFrame.OBJ_Y"))); //$NON-NLS-1$
		oY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		oY.setPreferredSize(new Dimension(60,20));
		panel.add(oY);
		oCreationCode = new JButton(Messages.getString("RoomFrame.OBJ_CODE")); //$NON-NLS-1$
		oCreationCode.setIcon(CODE_ICON);
		panel.add(oCreationCode);

		oList.setSelectedIndex(0);

		return panel;
		}

	public JPanel makeSettingsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

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
		sWidth.addActionListener(this);
		panel.add(sWidth);

		lab = new JLabel(Messages.getString("RoomFrame.HEIGHT")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,14));
		panel.add(lab);
		sHeight = new IntegerField(1,999999,res.height);
		sHeight.setPreferredSize(new Dimension(120,20));
		sHeight.addActionListener(this);
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
		sCreationCode.addActionListener(this);
		panel.add(sCreationCode);

		JPanel p2 = Util.makeTitledPanel(Messages.getString("RoomFrame.GRID"),170,112); //$NON-NLS-1$
		String st = Messages.getString("RoomFrame.GRID_VISIBLE"); //$NON-NLS-1$
		sGridVis = new JCheckBox(st,res.rememberWindowSize ? res.showGrid : true);
		sGridVis.addActionListener(this);
		p2.add(sGridVis);
		st = Messages.getString("RoomFrame.GRID_ISOMETRIC"); //$NON-NLS-1$
		sGridIso = new JCheckBox(st,res.isometricGrid);
		sGridIso.addActionListener(this);
		p2.add(sGridIso);
		addGap(p2,10,1);
		lab = new JLabel(Messages.getString("RoomFrame.SNAP_X")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(44,14));
		p2.add(lab);
		sSnapX = new IntegerField(1,999,res.snapX);
		sSnapX.setPreferredSize(new Dimension(60,20));
		sSnapX.addActionListener(this);
		p2.add(sSnapX);
		addGap(p2,10,1);
		lab = new JLabel(Messages.getString("RoomFrame.SNAP_Y")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(44,14));
		p2.add(lab);
		sSnapY = new IntegerField(1,999,res.snapY);
		sSnapY.setPreferredSize(new Dimension(60,20));
		sSnapY.addActionListener(this);
		p2.add(sSnapY);
		panel.add(p2);

		final JPopupMenu showMenu = new JPopupMenu();
		st = Messages.getString("RoomFrame.SHOW_OBJECTS"); //$NON-NLS-1$
		sSObj = new JCheckBoxMenuItem(st,res.rememberWindowSize ? res.showObjects : true);
		sSObj.addActionListener(this);
		showMenu.add(sSObj);
		st = Messages.getString("RoomFrame.SHOW_TILES"); //$NON-NLS-1$
		sSTile = new JCheckBoxMenuItem(st,res.rememberWindowSize ? res.showTiles : true);
		sSTile.addActionListener(this);
		showMenu.add(sSTile);
		st = Messages.getString("RoomFrame.SHOW_BACKGROUNDS"); //$NON-NLS-1$
		sSBack = new JCheckBoxMenuItem(st,res.rememberWindowSize ? res.showBackgrounds : true);
		sSBack.addActionListener(this);
		showMenu.add(sSBack);
		st = Messages.getString("RoomFrame.SHOW_FOREGROUNDS"); //$NON-NLS-1$
		sSFore = new JCheckBoxMenuItem(st,res.rememberWindowSize ? res.showForegrounds : true);
		sSFore.addActionListener(this);
		showMenu.add(sSFore);
		st = Messages.getString("RoomFrame.SHOW_VIEWS"); //$NON-NLS-1$
		sSView = new JCheckBoxMenuItem(st,res.showViews);
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
		panel.add(sShow);

		return panel;
		}

	public JPanel makeTilesPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		panel.add(new JLabel(Messages.getString("RoomFrame.WIP"))); //$NON-NLS-1$
		tUnderlying = new JCheckBox(Messages.getString("RoomFrame.TILE_UNDERLYING")); //$NON-NLS-1$
		tUnderlying.setSelected(res.rememberWindowSize ? res.deleteUnderlyingTiles : true);
		panel.add(tUnderlying);
		JLabel lab = new JLabel(Messages.getString("RoomFrame.TILE_LIST")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(150,20));
		panel.add(lab);
		tList = new JList(res.tiles.toArray());
		//		tList.setDragEnabled(true);
		//		tList.setDropMode(DropMode.INSERT);
		//		tList.setTransferHandler(null);
		tList.addListSelectionListener(this);
		tList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		tList.setVisibleRowCount(4);
		//		tList.setPreferredSize(new Dimension(180,128));
		tList.setCellRenderer(new ListComponentRenderer()
			{
				public Component getListCellRendererComponent(JList list, Object val, int ind,
						boolean selected, boolean focus)
					{
					Tile i = (Tile) val;
					Background bg = i.backgroundId.getRes();
					ImageIcon ii = new ImageIcon(bg.backgroundImage.getSubimage(i.tileX,i.tileY,i.width,
							i.height));
					JLabel lab = new JLabel(bg.getName() + " " + i.tileId,ii,JLabel.LEFT);
					super.getListCellRendererComponent(list,lab,ind,selected,focus);
					lab.setOpaque(true);
					return lab;
					}
			});
		JScrollPane sp = new JScrollPane(tList);
		sp.setPreferredSize(new Dimension(190,70));
		panel.add(sp);
		tAdd = new JButton(Messages.getString("RoomFrame.TILE_ADD")); //$NON-NLS-1$
		tAdd.addActionListener(this);
		panel.add(tAdd);
		tDel = new JButton(Messages.getString("RoomFrame.TILE_DELETE")); //$NON-NLS-1$
		tDel.addActionListener(this);
		panel.add(tDel);
		tLocked = new JCheckBox(Messages.getString("RoomFrame.TILE_LOCKED")); //$NON-NLS-1$
		tLocked.setPreferredSize(new Dimension(180,20));
		tLocked.setHorizontalAlignment(JCheckBox.CENTER);
		panel.add(tLocked);

		JPanel p = Util.makeTitledPanel(Messages.getString("RoomFrame.TILESET"),160,80); //$NON-NLS-1$
		tSource = new ResourceMenu<Background>(Room.BACKGROUND,"<no background>",true,110);
		tSource.setPreferredSize(new Dimension(140,20));
		p.add(tSource);
		p.add(new JLabel(Messages.getString("RoomFrame.TILESET_X"))); //$NON-NLS-1$
		tsX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tsX.setPreferredSize(new Dimension(50,20));
		p.add(tsX);
		p.add(new JLabel(Messages.getString("RoomFrame.TILESET_Y"))); //$NON-NLS-1$
		tsY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tsY.setPreferredSize(new Dimension(50,20));
		p.add(tsY);
		panel.add(p);

		p = Util.makeTitledPanel(Messages.getString("RoomFrame.TILE"),160,80); //$NON-NLS-1$
		p.add(new JLabel(Messages.getString("RoomFrame.TILE_X"))); //$NON-NLS-1$
		tX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tX.setPreferredSize(new Dimension(50,20));
		p.add(tX);
		p.add(new JLabel(Messages.getString("RoomFrame.TILE_Y"))); //$NON-NLS-1$
		tY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tY.setPreferredSize(new Dimension(50,20));
		p.add(tY);
		p.add(new JLabel(Messages.getString("RoomFrame.TILE_LAYER"))); //$NON-NLS-1$
		tLayer = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,1000000);
		tLayer.setPreferredSize(new Dimension(60,20));
		p.add(tLayer);
		panel.add(p);

		return panel;
		}

	public JPanel makeBackgroundsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		String st = Messages.getString("RoomFrame.DRAW_COLOR"); //$NON-NLS-1$
		bDrawColor = new JCheckBox(st,res.drawBackgroundColor);
		bDrawColor.addActionListener(this);
		panel.add(bDrawColor);
		panel.add(new JLabel(Messages.getString("RoomFrame.COLOR"))); //$NON-NLS-1$
		bColor = new ColorSelect(res.backgroundColor);
		bColor.setPreferredSize(new Dimension(100,20));
		bColor.addActionListener(this);
		panel.add(bColor);

		JLabel[] backLabs = new JLabel[8];
		for (int i = 0; i < 8; i++)
			{
			backLabs[i] = new JLabel(Messages.getString("RoomFrame.BACK") + i); //$NON-NLS-1$
			backLabs[i].setFont(backLabs[i].getFont().deriveFont(
					res.backgroundDefs[i].visible ? Font.BOLD : Font.PLAIN));
			backLabs[i].setOpaque(true);
			}
		bList = new JList(backLabs);
		bList.setCellRenderer(new ListComponentRenderer());
		bList.addListSelectionListener(this);
		bList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bList.setPreferredSize(new Dimension(150,128));
		panel.add(new JScrollPane(bList));

		st = Messages.getString("RoomFrame.BACK_VISIBLE"); //$NON-NLS-1$
		bVisible = new JCheckBox(st,res.backgroundDefs[0].visible);
		bVisible.addActionListener(this);
		panel.add(bVisible);
		st = Messages.getString("RoomFrame.BACK_FOREGROUND"); //$NON-NLS-1$
		bForeground = new JCheckBox(st,res.backgroundDefs[0].foreground);
		panel.add(bForeground);

		bSource = new ResourceMenu<Background>(Room.BACKGROUND,"<no background>",true,150);
		bSource.setRefSelected(res.backgroundDefs[0].backgroundId);
		panel.add(bSource);

		st = Messages.getString("RoomFrame.BACK_TILE_HOR"); //$NON-NLS-1$
		bTileH = new JCheckBox(st,res.backgroundDefs[0].tileHoriz);
		bTileH.setPreferredSize(new Dimension(100,20));
		bTileH.addActionListener(this);
		panel.add(bTileH);
		panel.add(new JLabel(Messages.getString("RoomFrame.BACK_X"))); //$NON-NLS-1$
		bX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.backgroundDefs[0].x);
		bX.setPreferredSize(new Dimension(40,20));
		bX.addActionListener(this);
		panel.add(bX);
		st = Messages.getString("RoomFrame.BACK_TILE_VERT"); //$NON-NLS-1$
		bTileV = new JCheckBox(st,res.backgroundDefs[0].tileVert);
		bTileV.setPreferredSize(new Dimension(100,20));
		bTileV.addActionListener(this);
		panel.add(bTileV);
		panel.add(new JLabel(Messages.getString("RoomFrame.BACK_Y"))); //$NON-NLS-1$
		bY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.backgroundDefs[0].x);
		bY.setPreferredSize(new Dimension(40,20));
		bY.addActionListener(this);
		panel.add(bY);
		st = Messages.getString("RoomFrame.BACK_STRETCH"); //$NON-NLS-1$
		bStretch = new JCheckBox(st,res.backgroundDefs[0].stretch);
		bStretch.setPreferredSize(new Dimension(156,20));
		bStretch.addActionListener(this);
		panel.add(bStretch);
		JLabel lab = new JLabel(Messages.getString("RoomFrame.BACK_HSPEED")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(112,20));
		lab.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(lab);
		bH = new IntegerField(-999,999,res.backgroundDefs[0].horizSpeed);
		bH.setPreferredSize(new Dimension(40,20));
		panel.add(bH);
		lab = new JLabel(Messages.getString("RoomFrame.BACK_VSPEED")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(112,20));
		lab.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(lab);
		bV = new IntegerField(-999,999,res.backgroundDefs[0].vertSpeed);
		bV.setPreferredSize(new Dimension(40,20));
		panel.add(bV);

		bList.setSelectedIndex(lastValidBack);

		return panel;
		}

	public JPanel makeViewsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		String st = Messages.getString("RoomFrame.ENABLE_VIEWS"); //$NON-NLS-1$
		vEnabled = new JCheckBox(st,res.enableViews);
		panel.add(vEnabled);

		JLabel[] viewLabs = new JLabel[8];
		for (int i = 0; i < 8; i++)
			{
			viewLabs[i] = new JLabel(Messages.getString("RoomFrame.VIEW") + i); //$NON-NLS-1$
			viewLabs[i].setFont(viewLabs[i].getFont().deriveFont(
					res.views[i].visible ? Font.BOLD : Font.PLAIN));
			viewLabs[i].setOpaque(true);
			}
		vList = new JList(viewLabs);
		vList.setCellRenderer(new ListComponentRenderer());
		vList.setVisibleRowCount(4);
		vList.addListSelectionListener(this);
		bList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		vList.setPreferredSize(new Dimension(160,200));
		panel.add(new JScrollPane(vList));

		st = Messages.getString("RoomFrame.VIEW_ENABLED"); //$NON-NLS-1$
		vVisible = new JCheckBox(st,res.views[0].visible);
		vVisible.addActionListener(this);
		panel.add(vVisible);

		JPanel p = Util.makeTitledPanel(Messages.getString("RoomFrame.VIEW_IN_ROOM"),130,80); //$NON-NLS-1$
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_X"))); //$NON-NLS-1$
		vRX = new IntegerField(0,999999,res.views[0].viewX);
		vRX.setPreferredSize(new Dimension(32,20));
		p.add(vRX);
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_W"))); //$NON-NLS-1$
		vRW = new IntegerField(1,999999,res.views[0].viewW);
		vRW.setPreferredSize(new Dimension(32,20));
		p.add(vRW);
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_Y"))); //$NON-NLS-1$
		vRY = new IntegerField(0,999999,res.views[0].viewY);
		vRY.setPreferredSize(new Dimension(32,20));
		p.add(vRY);
		addGap(p,2,0);
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_H"))); //$NON-NLS-1$
		vRH = new IntegerField(1,999999,res.views[0].viewH);
		vRH.setPreferredSize(new Dimension(32,20));
		p.add(vRH);
		panel.add(p);

		p = Util.makeTitledPanel(Messages.getString("RoomFrame.PORT"),130,80); //$NON-NLS-1$
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_X"))); //$NON-NLS-1$
		vPX = new IntegerField(0,999999,res.views[0].portX);
		vPX.setPreferredSize(new Dimension(32,20));
		p.add(vPX);
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_W"))); //$NON-NLS-1$
		vPW = new IntegerField(1,999999,res.views[0].portW);
		vPW.setPreferredSize(new Dimension(32,20));
		p.add(vPW);
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_Y"))); //$NON-NLS-1$
		vPY = new IntegerField(0,999999,res.views[0].portY);
		vPY.setPreferredSize(new Dimension(32,20));
		p.add(vPY);
		addGap(p,2,0);
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_H"))); //$NON-NLS-1$
		vPH = new IntegerField(1,999999,res.views[0].portH);
		vPH.setPreferredSize(new Dimension(32,20));
		p.add(vPH);
		panel.add(p);

		p = Util.makeTitledPanel(Messages.getString("RoomFrame.FOLLOW"),150,104); //$NON-NLS-1$
		vObj = new ResourceMenu<GmObject>(Room.GMOBJECT,"<no object>",true,110);
		vObj.setRefSelected(res.views[0].objectFollowing);
		p.add(vObj);
		p.add(new JLabel(Messages.getString("RoomFrame.HBOR"))); //$NON-NLS-1$
		vOHBor = new IntegerField(0,32000,res.views[0].hbor);
		vOHBor.setPreferredSize(new Dimension(32,20));
		p.add(vOHBor);
		p.add(new JLabel(Messages.getString("RoomFrame.HSP"))); //$NON-NLS-1$
		vOHSp = new IntegerField(-1,32000,res.views[0].hspeed);
		vOHSp.setPreferredSize(new Dimension(32,20));
		p.add(vOHSp);
		p.add(new JLabel(Messages.getString("RoomFrame.VBOR"))); //$NON-NLS-1$
		vOVBor = new IntegerField(0,32000,res.views[0].vbor);
		vOVBor.setPreferredSize(new Dimension(32,20));
		p.add(vOVBor);
		p.add(new JLabel(Messages.getString("RoomFrame.VSP"))); //$NON-NLS-1$
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
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.setMinimumSize(new Dimension(200,350));
		pane.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
		pane.setPreferredSize(new Dimension(200,350));

		//conveniently, these tabs happen to have the same indexes as GM's tabs
		tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.addTab(Messages.getString("RoomFrame.TAB_OBJECTS"),makeObjectsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_SETTINGS"),makeSettingsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_TILES"),makeTilesPane()); //$NON-NLS-1$
		String bks = Messages.getString("RoomFrame.TAB_BACKGROUNDS"); //$NON-NLS-1$
		tabs.addTab(bks,makeBackgroundsPane());
		tabs.addTab(Messages.getString("RoomFrame.TAB_VIEWS"),makeViewsPane()); //$NON-NLS-1$
		tabs.setSelectedIndex(res.currentTab);
		pane.add(tabs);

		FlowLayout fl = new FlowLayout();
		fl.setHgap(0);
		fl.setVgap(0);
		JPanel cont = new JPanel(fl);
		cont.setMaximumSize(new Dimension(130,22));
		cont.setMinimumSize(new Dimension(130,22));
		save.setText(Messages.getString("RoomFrame.SAVE")); //$NON-NLS-1$
		cont.add(save);
		pane.add(cont);
		add(pane);

		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.setPreferredSize(new Dimension(240,280));

		editor = new RoomEditor(res,this);
		pane.add(new JScrollPane(editor));

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

	public class ListComponentRenderer implements ListCellRenderer
		{
		public Component getListCellRendererComponent(JList list, Object val, int ind,
				boolean selected, boolean focus)
			{
			Component lab = (Component) val;
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

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		ResourceComparator c = new ResourceComparator();
		c.addExclusions(Room.class,"parent","currentTab"); //$NON-NLS-1$ //$NON-NLS-2$
		return c.areEqual(res,resOriginal);
		}

	@Override
	public void revertResource()
		{
		resOriginal.currentTab = tabs.getSelectedIndex();
		LGM.currentFile.rooms.replace(res,resOriginal);
		}

	public void commitChanges()
		{
		res.setName(name.getText());

		res.currentTab = tabs.getSelectedIndex();
		//objects
		res.deleteUnderlyingObjects = oUnderlying.isSelected();
		fireObjUpdate();
		//settings
		res.caption = sCaption.getText();
		res.width = sWidth.getIntValue();
		res.height = sHeight.getIntValue();
		res.speed = sSpeed.getIntValue();
		res.persistent = sPersistent.isSelected();
		res.showGrid = sGridVis.isSelected();
		res.isometricGrid = sGridIso.isSelected();
		res.snapX = sSnapX.getIntValue();
		res.snapY = sSnapY.getIntValue();
		res.showObjects = sSObj.isSelected();
		res.showTiles = sSTile.isSelected();
		res.showBackgrounds = sSBack.isSelected();
		res.showForegrounds = sSFore.isSelected();
		res.showViews = sSView.isSelected();
		//tiles
		res.deleteUnderlyingTiles = tUnderlying.isSelected();
		fireTileUpdate();
		//backgrounds
		res.drawBackgroundColor = bDrawColor.isSelected();
		res.backgroundColor = bColor.getSelectedColor();
		fireBackUpdate();
		//views
		res.enableViews = vEnabled.isSelected();
		fireViewUpdate();
		}

	//TODO: Room and Instance CreationCode
	public void actionPerformed(ActionEvent e)
		{
		if(editor != null)
			editor.refresh();
		Object s = e.getSource();
		if (s == bVisible)
			{
			JLabel lab = ((JLabel) bList.getSelectedValue());
			res.backgroundDefs[lastValidBack].visible = bVisible.isSelected();
			lab.setFont(lab.getFont().deriveFont(bVisible.isSelected() ? Font.BOLD : Font.PLAIN));
			bList.updateUI();
			return;
			}
		if(s == bStretch)
			{
			res.backgroundDefs[lastValidBack].stretch = bStretch.isSelected();
			return;
			}
		if(s == bTileH)
			{
			res.backgroundDefs[lastValidBack].tileHoriz = bTileH.isSelected();
			return;
			}
		if(s == bTileV)
			{
			res.backgroundDefs[lastValidBack].tileVert = bTileV.isSelected();
			return;
			}
		if(s == bX)
			{
			res.backgroundDefs[lastValidBack].x = bX.getIntValue();
			return;
			}
		if(s == bY)
			{
			res.backgroundDefs[lastValidBack].y = bY.getIntValue();
			return;
			}
		if (s == vVisible)
			{
			JLabel lab = ((JLabel) vList.getSelectedValue());
			res.views[lastValidView].visible = vVisible.isSelected();
			lab.setFont(lab.getFont().deriveFont(vVisible.isSelected() ? Font.BOLD : Font.PLAIN));
			vList.updateUI();
			return;
			}
		if (s == oSource)
			{
			if (!manualUpdate) return;
			Instance i = (Instance) oList.getSelectedValue();
			if (oSource.getSelected() == null)
				{
				oSource.setRefSelected(i.gmObjectId);
				return;
				}
			i.gmObjectId = oSource.getSelectedRef();
			oList.updateUI();
			}
		if (s == oAdd)
			{
			if (oSource.getSelected() == null) return;
			Instance i = res.addInstance();
			i.gmObjectId = oSource.getSelectedRef();
			oList.setListData(res.instances.toArray());
			oList.setSelectedIndex(res.instances.size() - 1);
			}
		if (s == oDel)
			{
			int i = oList.getSelectedIndex();
			if (i == -1) return;
			res.instances.remove(i);
			oList.setListData(res.instances.toArray());
			oList.setSelectedIndex(Math.min(res.instances.size() - 1,i));
			}
		if (s == tSource)
			{
			if (!manualUpdate) return;
			Tile t = (Tile) oList.getSelectedValue();
			if (t == null) return;
			if (tSource.getSelected() == null)
				{
				tSource.setRefSelected(t.backgroundId);
				return;
				}
			t.backgroundId = tSource.getSelectedRef();
			tList.updateUI();
			}
		if (s == tAdd)
			{
			if (tSource.getSelected() == null) return;
			Tile t = res.addTile();
			t.backgroundId = tSource.getSelectedRef();
			tList.setListData(res.tiles.toArray());
			tList.setSelectedIndex(res.tiles.size() - 1);
			}
		if (s == tDel)
			{
			int i = tList.getSelectedIndex();
			if (i == -1) return;
			res.tiles.remove(i);
			tList.setListData(res.tiles.toArray());
			tList.setSelectedIndex(Math.min(res.tiles.size() - 1,i));
			}
		super.actionPerformed(e);
		}

	public void fireObjUpdate()
		{
		if (lastObj != null)
			{
			lastObj.locked = oLocked.isSelected();
			if (oSource.getSelected() != null) lastObj.gmObjectId = oSource.getSelectedRef();
			lastObj.x = oX.getIntValue();
			lastObj.y = oY.getIntValue();
			}
		lastObj = (Instance) oList.getSelectedValue();
		if (lastObj == null) return;
		oLocked.setSelected(lastObj.locked);
		manualUpdate = false;
		oSource.setRefSelected(lastObj.gmObjectId);
		manualUpdate = true;
		oX.setIntValue(lastObj.x);
		oY.setIntValue(lastObj.y);
		}

	public void fireTileUpdate()
		{
		if (lastTile != null)
			{
			lastTile.locked = tLocked.isSelected();
			if (tSource.getSelected() != null) lastTile.backgroundId = bSource.getSelectedRef();
			lastTile.tileX = tsX.getIntValue();
			lastTile.tileY = tsY.getIntValue();
			lastTile.x = tX.getIntValue();
			lastTile.y = tY.getIntValue();
			lastTile.depth = tLayer.getIntValue();
			}
		lastTile = (Tile) tList.getSelectedValue();
		if (lastTile == null) return;
		tLocked.setSelected(lastTile.locked);
		manualUpdate = false;
		tSource.setRefSelected(lastTile.backgroundId);
		manualUpdate = true;
		tsX.setIntValue(lastTile.tileX);
		tsY.setIntValue(lastTile.tileY);
		tX.setIntValue(lastTile.x);
		tY.setIntValue(lastTile.y);
		tLayer.setIntValue(lastTile.depth);
		}

	public void fireBackUpdate()
		{
		BackgroundDef b = res.backgroundDefs[lastValidBack];
		b.visible = bVisible.isSelected();
		b.foreground = bForeground.isSelected();
		b.backgroundId = bSource.getSelectedRef();
		b.x = bX.getIntValue();
		b.y = bY.getIntValue();
		b.tileHoriz = bTileH.isSelected();
		b.tileVert = bTileV.isSelected();
		b.stretch = bStretch.isSelected();
		b.horizSpeed = bH.getIntValue();
		b.vertSpeed = bV.getIntValue();

		if (bList.getSelectedIndex() == -1)
			{
			bList.setSelectedIndex(lastValidBack);
			return;
			}
		lastValidBack = bList.getSelectedIndex();

		b = res.backgroundDefs[lastValidBack];
		bVisible.setSelected(b.visible);
		bForeground.setSelected(b.foreground);
		bSource.setRefSelected(b.backgroundId);
		bX.setIntValue(b.x);
		bY.setIntValue(b.y);
		bTileH.setSelected(b.tileHoriz);
		bTileV.setSelected(b.tileVert);
		bStretch.setSelected(b.stretch);
		bH.setIntValue(b.horizSpeed);
		bV.setIntValue(b.vertSpeed);
		}

	public void fireViewUpdate()
		{
		View v = res.views[lastValidView];
		v.visible = vVisible.isSelected();
		v.viewX = vRX.getIntValue();
		v.viewY = vRY.getIntValue();
		v.viewW = vRW.getIntValue();
		v.viewH = vRH.getIntValue();
		v.portX = vPX.getIntValue();
		v.portY = vPY.getIntValue();
		v.portW = vPW.getIntValue();
		v.portH = vPH.getIntValue();
		v.objectFollowing = vObj.getSelectedRef();
		v.hbor = vOHBor.getIntValue();
		v.vbor = vOVBor.getIntValue();
		v.hspeed = vOHSp.getIntValue();
		v.vspeed = vOVSp.getIntValue();

		if (vList.getSelectedIndex() == -1)
			{
			vList.setSelectedIndex(lastValidView);
			return;
			}
		lastValidView = vList.getSelectedIndex();

		v = res.views[lastValidView];
		vVisible.setSelected(v.visible);
		vRX.setIntValue(v.viewX);
		vRY.setIntValue(v.viewY);
		vRW.setIntValue(v.viewW);
		vRH.setIntValue(v.viewH);
		vPX.setIntValue(v.portX);
		vPY.setIntValue(v.portY);
		vPW.setIntValue(v.portW);
		vPH.setIntValue(v.portH);
		vObj.setRefSelected(v.objectFollowing);
		vOHBor.setIntValue(v.hbor);
		vOVBor.setIntValue(v.vbor);
		vOHSp.setIntValue(v.hspeed);
		vOVSp.setIntValue(v.vspeed);
		}

	public void valueChanged(ListSelectionEvent e)
		{
		if (e.getValueIsAdjusting()) return;

		if (e.getSource() == oList) fireObjUpdate();
		if (e.getSource() == tList) fireTileUpdate();
		if (e.getSource() == bList) fireBackUpdate();
		if (e.getSource() == vList) fireViewUpdate();
		}
	}
