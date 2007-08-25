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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
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

import org.lateralgm.compare.ReflectionComparator;
import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.View;

//TODO: Handle res.rememberWindowSize - may also apply to other options
public class RoomFrame extends ResourceFrame<Room> implements ListSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon CODE_ICON = LGM.getIconForKey("RoomFrame.CODE"); //$NON-NLS-1$

	public JTabbedPane tabs;
	public JLabel statX, statY, statObj, statId;
	//Objects
	public JCheckBox oUnderlying, oLocked;
	public JList oList;
	public JButton oAdd, oDel;
	public ResourceMenu oSource;
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
	//Backgrounds
	public JCheckBox bDrawColor, bVisible, bForeground, bTileH, bTileV, bStretch;
	public ColorSelect bColor;
	public JList bList;
	/**Guaranteed valid version of bList.getLastSelectedIndex()*/
	public int lastValidBack = 0;
	public ResourceMenu bSource;
	public IntegerField bX, bY, bH, bV;
	//Views
	public JCheckBox vEnabled, vVisible;
	public JList vList;
	/**Guaranteed valid version of vList.getLastSelectedIndex()*/
	public int lastValidView = 0;
	public IntegerField vRX, vRY, vRW, vRH;
	public IntegerField vPX, vPY, vPW, vPH;
	public ResourceMenu vObj;
	public IntegerField vOHBor, vOVBor, vOHSp, vOVSp;

	public JPanel makeObjectsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		panel.add(new JLabel(Messages.getString("RoomFrame.OBJ_WIP"))); //$NON-NLS-1$
		oUnderlying = new JCheckBox(Messages.getString("RoomFrame.OBJ_UNDERLYING")); //$NON-NLS-1$
		oUnderlying.setSelected(res.deleteUnderlyingObjects);
		panel.add(oUnderlying);
		JLabel lab = new JLabel(Messages.getString("RoomFrame.OBJ_INSTANCES")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(150,20));
		panel.add(lab);
		oList = new JList(res.instances.toArray());
		oList.setDragEnabled(true);
		oList.setDropMode(DropMode.INSERT);
		oList.setTransferHandler(null); //TODO (drag and drop)
		oList.addListSelectionListener(this);
		oList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//oList.setVisibleRowCount(8);
		oList.setPreferredSize(new Dimension(160,128));
		oList.setCellRenderer(new ListCellRenderer()
			{
				public Component getListCellRendererComponent(JList list, Object val, int ind,
						boolean selected, boolean focus)
					{
					Instance i = (Instance) val;
					GmObject go = LGM.currentFile.gmObjects.get(i.gmObjectId);
					JLabel lab = new JLabel(go.getName() + " " + i.instanceId,
							GmTreeGraphics.getSpriteIcon(LGM.currentFile.sprites.get(go.sprite)),JLabel.LEFT);
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
					lab.setOpaque(true);
					return lab;
					}
			});
		panel.add(new JScrollPane(oList));
		oAdd = new JButton(Messages.getString("RoomFrame.OBJ_ADD")); //$NON-NLS-1$
		oAdd.addActionListener(this);
		panel.add(oAdd);
		oDel = new JButton(Messages.getString("RoomFrame.OBJ_DELETE")); //$NON-NLS-1$
		oDel.addActionListener(this);
		panel.add(oDel);
		oSource = new ResourceMenu(Room.GMOBJECT,"<no object>",true,110);
		oSource.setPreferredSize(new Dimension(120,20));
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
		sCreationCode.addActionListener(this);
		panel.add(sCreationCode);

		JPanel p2 = new JPanel(new FlowLayout());
		String st = Messages.getString("RoomFrame.GRID"); //$NON-NLS-1$
		p2.setBorder(BorderFactory.createTitledBorder(st));
		p2.setPreferredSize(new Dimension(170,112));
		st = Messages.getString("RoomFrame.GRID_VISIBLE"); //$NON-NLS-1$
		sGridVis = new JCheckBox(st,res.showGrid);
		p2.add(sGridVis);
		st = Messages.getString("RoomFrame.GRID_ISOMETRIC"); //$NON-NLS-1$
		sGridIso = new JCheckBox(st,res.isometricGrid);
		p2.add(sGridIso);
		addGap(p2,10,1);
		lab = new JLabel(Messages.getString("RoomFrame.SNAP_X")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(44,14));
		p2.add(lab);
		sSnapY = new IntegerField(1,999,res.snapY);
		sSnapY.setPreferredSize(new Dimension(60,20));
		p2.add(sSnapY);
		addGap(p2,10,1);
		lab = new JLabel(Messages.getString("RoomFrame.SNAP_Y")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(44,14));
		p2.add(lab);
		sSnapX = new IntegerField(1,999,res.snapX);
		sSnapX.setPreferredSize(new Dimension(60,20));
		p2.add(sSnapX);
		panel.add(p2);

		final JPopupMenu showMenu = new JPopupMenu();
		st = Messages.getString("RoomFrame.SHOW_OBJECTS"); //$NON-NLS-1$
		sSObj = new JCheckBoxMenuItem(st,res.showObjects);
		showMenu.add(sSObj);
		st = Messages.getString("RoomFrame.SHOW_TILES"); //$NON-NLS-1$
		sSTile = new JCheckBoxMenuItem(st,res.showTiles);
		showMenu.add(sSTile);
		st = Messages.getString("RoomFrame.SHOW_BACKGROUNDS"); //$NON-NLS-1$
		sSBack = new JCheckBoxMenuItem(st,res.showBackgrounds);
		showMenu.add(sSBack);
		st = Messages.getString("RoomFrame.SHOW_FOREGROUNDS"); //$NON-NLS-1$
		sSFore = new JCheckBoxMenuItem(st,res.showForegrounds);
		showMenu.add(sSFore);
		st = Messages.getString("RoomFrame.SHOW_VIEWS"); //$NON-NLS-1$
		sSView = new JCheckBoxMenuItem(st,res.showViews);
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

	//TODO: (GUI)
	public JPanel makeTilesPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		// String is a Temporary fix
		tUnderlying = new JCheckBox("",res.deleteUnderlyingTiles);

		return panel;
		}

	public JPanel makeBackgroundsPane()
		{
		JPanel panel = new JPanel(new FlowLayout());

		String st = Messages.getString("RoomFrame.DRAW_COLOR"); //$NON-NLS-1$
		bDrawColor = new JCheckBox(st,res.drawBackgroundColor);
		panel.add(bDrawColor);
		panel.add(new JLabel(Messages.getString("RoomFrame.COLOR"))); //$NON-NLS-1$
		bColor = new ColorSelect(res.backgroundColor);
		//	bColor.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		//	bColor.setAlignmentX(0f);
		bColor.setPreferredSize(new Dimension(100,20));
		panel.add(bColor);

		JLabel[] backLabs = new JLabel[8];
		for (int i = 0; i < 8; i++)
			{
			backLabs[i] = new JLabel(Messages.getString("RoomFrame.BACK") + i);
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

		bSource = new ResourceMenu(Room.BACKGROUND,"<no background>",true,150);
		bSource.setSelected(LGM.currentFile.backgrounds.get(res.backgroundDefs[0].backgroundId));
		panel.add(bSource);

		st = Messages.getString("RoomFrame.BACK_TILE_HOR"); //$NON-NLS-1$
		bTileH = new JCheckBox(st,res.backgroundDefs[0].tileHoriz);
		bTileH.setPreferredSize(new Dimension(100,20));
		panel.add(bTileH);
		panel.add(new JLabel(Messages.getString("RoomFrame.BACK_X"))); //$NON-NLS-1$
		bX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.backgroundDefs[0].x);
		bX.setPreferredSize(new Dimension(40,20));
		panel.add(bX);
		st = Messages.getString("RoomFrame.BACK_TILE_VERT"); //$NON-NLS-1$
		bTileV = new JCheckBox(st,res.backgroundDefs[0].tileVert);
		bTileV.setPreferredSize(new Dimension(100,20));
		panel.add(bTileV);
		panel.add(new JLabel(Messages.getString("RoomFrame.BACK_Y"))); //$NON-NLS-1$
		bY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.backgroundDefs[0].x);
		bY.setPreferredSize(new Dimension(40,20));
		panel.add(bY);
		st = Messages.getString("RoomFrame.BACK_STRETCH"); //$NON-NLS-1$
		bStretch = new JCheckBox(st,res.backgroundDefs[0].stretch);
		bStretch.setPreferredSize(new Dimension(156,20));
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
			viewLabs[i] = new JLabel(Messages.getString("RoomFrame.VIEW") + i);
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

		JPanel p = new JPanel(new FlowLayout());
		st = Messages.getString("RoomFrame.VIEW_IN_ROOM"); //$NON_NLS_1$
		p.setBorder(BorderFactory.createTitledBorder(st));
		p.setPreferredSize(new Dimension(130,80));
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_X"))); //$NON_NLS_1$
		vRX = new IntegerField(0,999999,res.views[0].viewX);
		vRX.setPreferredSize(new Dimension(32,20));
		p.add(vRX);
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_W"))); //$NON_NLS_1$
		vRW = new IntegerField(1,999999,res.views[0].viewW);
		vRW.setPreferredSize(new Dimension(32,20));
		p.add(vRW);
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_Y"))); //$NON_NLS_1$
		vRY = new IntegerField(0,999999,res.views[0].viewY);
		vRY.setPreferredSize(new Dimension(32,20));
		p.add(vRY);
		addGap(p,2,0);
		p.add(new JLabel(Messages.getString("RoomFrame.VIEW_H"))); //$NON_NLS_1$
		vRH = new IntegerField(1,999999,res.views[0].viewH);
		vRH.setPreferredSize(new Dimension(32,20));
		p.add(vRH);
		panel.add(p);

		p = new JPanel(new FlowLayout());
		st = Messages.getString("RoomFrame.PORT"); //$NON_NLS_1$
		p.setBorder(BorderFactory.createTitledBorder(st));
		p.setPreferredSize(new Dimension(130,80));
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_X"))); //$NON_NLS_1$
		vPX = new IntegerField(0,999999,res.views[0].portX);
		vPX.setPreferredSize(new Dimension(32,20));
		p.add(vPX);
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_W"))); //$NON_NLS_1$
		vPW = new IntegerField(1,999999,res.views[0].portW);
		vPW.setPreferredSize(new Dimension(32,20));
		p.add(vPW);
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_Y"))); //$NON_NLS_1$
		vPY = new IntegerField(0,999999,res.views[0].portY);
		vPY.setPreferredSize(new Dimension(32,20));
		p.add(vPY);
		addGap(p,2,0);
		p.add(new JLabel(Messages.getString("RoomFrame.PORT_H"))); //$NON_NLS_1$
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
		p.add(new JLabel(Messages.getString("RoomFrame.HBOR"))); //$NON_NLS_1$
		vOHBor = new IntegerField(0,32000,res.views[0].hbor);
		vOHBor.setPreferredSize(new Dimension(32,20));
		p.add(vOHBor);
		p.add(new JLabel(Messages.getString("RoomFrame.HSP"))); //$NON_NLS_1$
		vOHSp = new IntegerField(-1,32000,res.views[0].hspeed);
		vOHSp.setPreferredSize(new Dimension(32,20));
		p.add(vOHSp);
		p.add(new JLabel(Messages.getString("RoomFrame.VBOR"))); //$NON_NLS_1$
		vOVBor = new IntegerField(0,32000,res.views[0].vbor);
		vOVBor.setPreferredSize(new Dimension(32,20));
		p.add(vOVBor);
		p.add(new JLabel(Messages.getString("RoomFrame.VSP"))); //$NON_NLS_1$
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

	/**
	 * This class is public in case it is useful for other classes.<br>
	 * It is intended for RoomFrame lists. All list items must be Components
	 */
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

	//TODO:
	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		ReflectionComparator c = new ResourceComparator();
		c.addExclusions(Room.class,"parent","currentTab");
		return c.areEqual(res,resOriginal);
		}

	@Override
	public void revertResource()
		{
		resOriginal.currentTab = tabs.getSelectedIndex();
		LGM.currentFile.rooms.replace(res.getId(),resOriginal);
		}

	private void commitChanges()
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
		//backgrounds
		res.drawBackgroundColor = bDrawColor.isSelected();
		res.backgroundColor = bColor.getSelectedColor();
		fireBackUpdate();
		//views
		res.enableViews = vEnabled.isSelected();
		fireViewUpdate();
		}

	@Override
	public void updateResource()
		{
		commitChanges();
		resOriginal = res.copy();
		}

	//TODO: (CreationCode, among other things. Backgrounds and Views are done)
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == bVisible)
			{
			JLabel lab = ((JLabel) bList.getSelectedValue());
			res.backgroundDefs[lastValidBack].visible = bVisible.isSelected();
			lab.setFont(lab.getFont().deriveFont(bVisible.isSelected() ? Font.BOLD : Font.PLAIN));
			bList.updateUI();
			return;
			}
		if (e.getSource() == vVisible)
			{
			JLabel lab = ((JLabel) vList.getSelectedValue());
			res.views[lastValidView].visible = vVisible.isSelected();
			lab.setFont(lab.getFont().deriveFont(vVisible.isSelected() ? Font.BOLD : Font.PLAIN));
			vList.updateUI();
			return;
			}

		super.actionPerformed(e);
		}

	//TODO:
	public void fireObjUpdate()
		{

		}

	//TODO:
	public void fireTileUpdate()
		{

		}

	public void fireBackUpdate()
		{
		BackgroundDef b = res.backgroundDefs[lastValidBack];
		b.visible = bVisible.isSelected();
		b.foreground = bForeground.isSelected();
		if (bSource.getSelected() == null)
			b.backgroundId = null;
		else
			b.backgroundId = bSource.getSelected().getId();
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
		bSource.setSelected(LGM.currentFile.backgrounds.get(b.backgroundId));
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
		if (vObj.getSelected() == null)
			v.objectFollowing = null;
		else
			v.objectFollowing = vObj.getSelected().getId();
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
		vObj.setSelected(LGM.currentFile.gmObjects.get(v.objectFollowing));
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
