/*
 * Copyright (C) 2007, 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.lateralgm.main.Util.deRef;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.components.visual.RoomEditor;
import org.lateralgm.main.LGM;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.ActiveArrayList;
import org.lateralgm.resources.Room.ActiveArrayList.ListUpdateEvent;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

public class RoomFrame extends ResourceFrame<Room> implements ListSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon CODE_ICON = LGM.getIconForKey("RoomFrame.CODE"); //$NON-NLS-1$

	//prevents List selection updates from firing ResourceMenu changes
	public static boolean manualUpdate = true;
	public RoomEditor editor;
	public JScrollPane editorPane;
	public JTabbedPane tabs;
	public JLabel statX, statY, statId, statSrc;
	//ToolBar
	public JButton zoomIn, zoomOut;
	public JToggleButton gridVis, gridIso;
	//	public IntegerField snapX, snapY;
	//Objects
	public JCheckBox oUnderlying, oLocked;
	public JList oList;
	private Instance lastObj = null; //non-guaranteed copy of oList.getLastSelectedValue()
	public JButton oAdd, oDel;
	public ResourceMenu<GmObject> oNew, oSource;
	public IntegerField oX, oY;
	public JButton oCreationCode;
	//Settings
	public JTextField sCaption;
	public IntegerField sWidth, sHeight, sSpeed, sGX, sGY, sGW, sGH;
	public JCheckBox sPersistent;
	public JButton sCreationCode, sShow;
	public JPopupMenu sShowMenu;

	public HashMap<Object,CodeFrame> codeFrames = new HashMap<Object,CodeFrame>();

	public JCheckBoxMenuItem sSObj, sSTile, sSBack, sSFore, sSView;
	//Tiles
	public JCheckBox tUnderlying, tLocked;
	public TileSelector tSelect;
	public JScrollPane tScroll;
	public JList tList;
	private Tile lastTile = null; //non-guaranteed copy of tList.getLastSelectedValue()
	public JButton tDel;
	public ResourceMenu<Background> taSource, teSource;
	public IntegerField tsX, tsY, tX, tY, taDepth, teDepth;
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

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.add(save);
		tool.addSeparator();

		zoomIn = new JButton(LGM.getIconForKey("RoomFrame.ZOOM_IN")); //$NON-NLS-1$
		zoomIn.setEnabled(false); //because zoom is 100%
		zoomIn.addActionListener(this);
		tool.add(zoomIn);
		zoomOut = new JButton(LGM.getIconForKey("RoomFrame.ZOOM_OUT")); //$NON-NLS-1$
		zoomOut.addActionListener(this);
		tool.add(zoomOut);
		tool.addSeparator();

		String st = Messages.getString("RoomFrame.GRID_VISIBLE"); //$NON-NLS-1$
		gridVis = new JToggleButton(st,res.rememberWindowSize ? res.showGrid : true);
		gridVis.addActionListener(this); //causes editor to update on fire
		tool.add(gridVis);
		st = Messages.getString("RoomFrame.GRID_ISOMETRIC"); //$NON-NLS-1$
		gridIso = new JToggleButton(st,res.isometricGrid);
		gridIso.addActionListener(this);
		tool.add(gridIso);

		sShowMenu = makeShowMenu();
		sShow = new JButton(Messages.getString("RoomFrame.SHOW")); //$NON-NLS-1$
		sShow.addActionListener(this);
		tool.add(sShow);

		tool.addSeparator();

		return tool;
		}

	public static class ArrayListModel<E> implements ListModel,UpdateListener
	{
	ActiveArrayList<E> list;
	ArrayList<ListDataListener> listeners;
	
	public ArrayListModel(ActiveArrayList<E> l)
		{
		list = l;
		l.updateSource.addListener(this);
		listeners = new ArrayList<ListDataListener>();
		}
	
	public void addListDataListener(ListDataListener l)
		{
		listeners.add(l);
		}
	
	public Object getElementAt(int index)
		{
		return list.get(index);
		}
	
	public int getSize()
		{
		return list.size();
		}
	
	public void removeListDataListener(ListDataListener l)
		{
		listeners.remove(l);
		}
	
	public void updated(UpdateEvent e)
		{
		ListDataEvent lde;
		if (e instanceof ListUpdateEvent)
			{
			ListUpdateEvent lue = (ListUpdateEvent) e;
			int t;
			switch (lue.type)
				{
				case ADDED:
					t = ListDataEvent.INTERVAL_ADDED;
					break;
				case REMOVED:
					t = ListDataEvent.INTERVAL_REMOVED;
					break;
				case CHANGED:
					t = ListDataEvent.CONTENTS_CHANGED;
					break;
				default:
					throw new AssertionError();
				}
			lde = new ListDataEvent(e.source.owner,t,lue.fromIndex,lue.toIndex);
			}
		else
			lde = new ListDataEvent(e.source.owner,ListDataEvent.CONTENTS_CHANGED,0,MAX_VALUE);
		for (ListDataListener l : listeners)
			l.contentsChanged(lde);
		}
	}

	private static class ObjectListComponentRenderer implements ListCellRenderer
		{
		private final JLabel lab = new JLabel();
		private final ListComponentRenderer lcr = new ListComponentRenderer();

		public ObjectListComponentRenderer()
			{
			lab.setOpaque(true);
			}

		public Component getListCellRendererComponent(JList list, Object val, int ind,
				boolean selected, boolean focus)
			{
			Instance i = (Instance) val;
			GmObject go = deRef(i.getObject());
			String name = go == null ? Messages.getString("RoomFrame.NO_OBJECT") : go.getName();
			lcr.getListCellRendererComponent(list,lab,ind,selected,focus);
			lab.setText(name + " " + i.instanceId);
			ResNode rn = go.getNode();
			lab.setIcon(rn == null ? null : rn.getIcon());
			return lab;
			}
		}

	private static class TileListComponentRenderer implements ListCellRenderer
		{
		private final JLabel lab = new JLabel();
		private final TileIcon ti = new TileIcon();
		private final ListComponentRenderer lcr = new ListComponentRenderer();

		public TileListComponentRenderer()
			{
			lab.setOpaque(true);
			lab.setIcon(ti);
			}

		public Component getListCellRendererComponent(JList list, Object val, int ind,
				boolean selected, boolean focus)
			{
			Tile t = (Tile) val;
			Background bg = deRef(t.getBackground());
			String name = bg == null ? Messages.getString("RoomFrame.NO_BACKGROUND") : bg.getName();
			lab.setText(name + " " + t.tileId);
			ti.tile = t;
			lcr.getListCellRendererComponent(list,lab,ind,selected,focus);
			return lab;
			}

		static class TileIcon implements Icon
			{
			Tile tile;

			public int getIconHeight()
				{
				return tile.getSize().height;
				}

			public int getIconWidth()
				{
				return tile.getSize().width;
				}

			public void paintIcon(Component c, Graphics g, int x, int y)
				{
				Background bg = deRef(tile.getBackground());
				BufferedImage bi = bg == null ? null : bg.getBackgroundImage();
				if (bi != null)
					{
					Point p = tile.getBackgroundPosition();
					Dimension d = tile.getSize();
					g.drawImage(bi,0,0,d.width,d.height,p.x,p.y,p.x + d.width,p.y + d.height,c);
					}
				}
			}
		}

	public JPanel makeObjectsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		oNew = new ResourceMenu<GmObject>(Room.GMOBJECT,
				Messages.getString("RoomFrame.NO_OBJECT"),true,110); //$NON-NLS-1$
		oNew.addActionListener(this);
		oUnderlying = new JCheckBox(Messages.getString("RoomFrame.OBJ_UNDERLYING")); //$NON-NLS-1$
		oUnderlying.setSelected(res.rememberWindowSize ? res.deleteUnderlyingObjects : true);

		oList = new JList(new ArrayListModel<Instance>(res.instances));
		oList.addListSelectionListener(this);
		oList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		oList.setVisibleRowCount(8);
		oList.setCellRenderer(new ObjectListComponentRenderer());
		JScrollPane sp = new JScrollPane(oList);
		oAdd = new JButton(Messages.getString("RoomFrame.OBJ_ADD")); //$NON-NLS-1$
		oAdd.addActionListener(this);
		oDel = new JButton(Messages.getString("RoomFrame.OBJ_DELETE")); //$NON-NLS-1$
		oDel.addActionListener(this);

		JPanel edit = new JPanel();
		String title = Messages.getString("RoomFrame.OBJ_INSTANCES"); //$NON-NLS-1$
		edit.setBorder(BorderFactory.createTitledBorder(title));
		GroupLayout layout2 = new GroupLayout(edit);
		layout2.setAutoCreateGaps(true);
		layout2.setAutoCreateContainerGaps(true);
		edit.setLayout(layout2);

		oSource = new ResourceMenu<GmObject>(Room.GMOBJECT,
				Messages.getString("RoomFrame.NO_OBJECT"),true,110); //$NON-NLS-1$
		oSource.addActionListener(this);
		oLocked = new JCheckBox(Messages.getString("RoomFrame.OBJ_LOCKED")); //$NON-NLS-1$
		oLocked.setHorizontalAlignment(JCheckBox.CENTER);
		JLabel lObjX = new JLabel(Messages.getString("RoomFrame.OBJ_X")); //$NON-NLS-1$
		oX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		oX.setColumns(4);
		oX.addActionListener(this);
		JLabel lObjY = new JLabel(Messages.getString("RoomFrame.OBJ_Y")); //$NON-NLS-1$
		oY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		oY.setColumns(4);
		oY.addActionListener(this);
		oCreationCode = new JButton(Messages.getString("RoomFrame.OBJ_CODE")); //$NON-NLS-1$
		oCreationCode.setIcon(CODE_ICON);
		oCreationCode.addActionListener(this);

		//Causes fireObjUpdate, requires oList, oSource, oLocked, oX, and oY
		oList.setSelectedIndex(0);

		layout2.setHorizontalGroup(layout2.createParallelGroup()
		/**/.addComponent(oSource)
		/**/.addComponent(oLocked)
		/**/.addGroup(layout2.createSequentialGroup()
		/*		*/.addComponent(lObjX)
		/*		*/.addComponent(oX)
		/*		*/.addComponent(lObjY)
		/*		*/.addComponent(oY))
		/**/.addComponent(oCreationCode,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		layout2.setVerticalGroup(layout2.createSequentialGroup()
		/**/.addComponent(oSource)
		/**/.addComponent(oLocked)
		/**/.addGroup(layout2.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lObjX)
		/*		*/.addComponent(oX)
		/*		*/.addComponent(lObjY)
		/*		*/.addComponent(oY))
		/**/.addComponent(oCreationCode));

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(oNew)
		/**/.addComponent(oUnderlying)
		/**/.addComponent(sp,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(oAdd,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(oDel,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/**/.addComponent(edit));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(oNew)
		/**/.addComponent(oUnderlying)
		/**/.addComponent(sp)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(oAdd)
		/*		*/.addComponent(oDel))
		/**/.addComponent(edit));

		return panel;
		}

	private JPopupMenu makeShowMenu()
		{
		JPopupMenu showMenu = new JPopupMenu();
		String st = Messages.getString("RoomFrame.SHOW_OBJECTS"); //$NON-NLS-1$
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
		return showMenu;
		}

	public JPanel makeSettingsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		JLabel lName = new JLabel(Messages.getString("RoomFrame.NAME")); //$NON-NLS-1$

		JLabel lCaption = new JLabel(Messages.getString("RoomFrame.CAPTION")); //$NON-NLS-1$
		sCaption = new JTextField(res.caption);

		JLabel lWidth = new JLabel(Messages.getString("RoomFrame.WIDTH")); //$NON-NLS-1$
		sWidth = new IntegerField(1,999999,res.width);
		sWidth.setColumns(7);
		sWidth.addActionListener(this);

		JLabel lHeight = new JLabel(Messages.getString("RoomFrame.HEIGHT")); //$NON-NLS-1$
		sHeight = new IntegerField(1,999999,res.height);
		sHeight.setColumns(7);
		sHeight.addActionListener(this);

		JLabel lSpeed = new JLabel(Messages.getString("RoomFrame.SPEED")); //$NON-NLS-1$
		sSpeed = new IntegerField(1,9999,res.speed);
		sSpeed.setColumns(5);

		String str = Messages.getString("RoomFrame.PERSISTENT"); //$NON-NLS-1$
		sPersistent = new JCheckBox(str,res.persistent);

		str = Messages.getString("RoomFrame.CREATION_CODE"); //$NON-NLS-1$
		sCreationCode = new JButton(str,CODE_ICON);
		sCreationCode.addActionListener(this);

		JPanel pg = makeGridPane();

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lName)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(lCaption)
		/**/.addComponent(sCaption,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(lWidth)
		/*				*/.addComponent(lHeight)
		/*				*/.addComponent(lSpeed))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(sWidth)
		/*				*/.addComponent(sHeight)
		/*				*/.addComponent(sSpeed)))
		/**/.addComponent(sPersistent)
		/**/.addComponent(sCreationCode,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(pg));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lName)
		/*		*/.addComponent(name))
		/**/.addComponent(lCaption)
		/**/.addComponent(sCaption,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lWidth)
		/*		*/.addComponent(sWidth))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lHeight)
		/*		*/.addComponent(sHeight))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lSpeed)
		/*		*/.addComponent(sSpeed))
		/**/.addComponent(sPersistent)
		/**/.addComponent(sCreationCode)
		/**/.addComponent(pg));
		return panel;
		}

	public JPanel makeGridPane()
		{
		JPanel pg = new JPanel();
		GroupLayout lr = new GroupLayout(pg);
		pg.setLayout(lr);
		pg.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.GRID"))); //$NON-NLS-1$

		JLabel lGX = new JLabel(Messages.getString("RoomFrame.GRID_X")); //$NON-NLS-1$
		sGX = new IntegerField(0,999,0);
		sGX.setColumns(4);
		sGX.addActionListener(this);
		JLabel lGY = new JLabel(Messages.getString("RoomFrame.GRID_Y")); //$NON-NLS-1$
		sGY = new IntegerField(0,999,0);
		sGY.setColumns(4);
		sGY.addActionListener(this);
		JLabel lGW = new JLabel(Messages.getString("RoomFrame.GRID_W")); //$NON-NLS-1$
		sGW = new IntegerField(1,999,res.snapX);
		sGW.setColumns(4);
		sGW.addActionListener(this);
		JLabel lGH = new JLabel(Messages.getString("RoomFrame.GRID_H")); //$NON-NLS-1$
		sGH = new IntegerField(1,999,res.snapY);
		sGH.setColumns(4);
		sGH.addActionListener(this);
		lr.setHorizontalGroup(lr.createSequentialGroup().addContainerGap()
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lGX)
		/*		*/.addComponent(lGY)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(sGX)
		/*		*/.addComponent(sGY)).addGap(8)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lGW)
		/*		*/.addComponent(lGH)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(sGW)
		/*		*/.addComponent(sGH)).addContainerGap());
		lr.setVerticalGroup(lr.createSequentialGroup().addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lGX)
		/*		*/.addComponent(sGX)
		/*		*/.addComponent(lGW)
		/*		*/.addComponent(sGW)).addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lGY)
		/*		*/.addComponent(sGY)
		/*		*/.addComponent(lGH)
		/*		*/.addComponent(sGH)).addGap(8));

		return pg;
		}

	public JTabbedPane makeTilesPane()
		{
		JTabbedPane tab = new JTabbedPane();
		tab.addTab(Messages.getString("RoomFrame.TILE_ADD"),makeTilesAddPane());
		tab.addTab(Messages.getString("RoomFrame.TILE_EDIT"),makeTilesEditPane());
		tab.addTab(Messages.getString("RoomFrame.TILE_BATCH"),makeTilesBatchPane());
		tab.setSelectedIndex(0);
		return tab;
		}

	public JPanel makeTilesAddPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		taSource = new ResourceMenu<Background>(Room.BACKGROUND,
				Messages.getString("RoomFrame.NO_BACKGROUND"),true,110);
		taSource.addActionListener(this);
		tSelect = new TileSelector();
		tScroll = new JScrollPane(tSelect);
		tUnderlying = new JCheckBox(Messages.getString("RoomFrame.TILE_UNDERLYING")); //$NON-NLS-1$
		tUnderlying.setSelected(res.rememberWindowSize ? res.deleteUnderlyingTiles : true);
		JLabel lab = new JLabel(Messages.getString("RoomFrame.TILE_LAYER"));
		taDepth = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		taDepth.setMaximumSize(new Dimension(Integer.MAX_VALUE,taDepth.getHeight()));

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(tScroll)
		/**/.addComponent(taSource)
		/**/.addComponent(tUnderlying)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(lab)
		/*	*/.addComponent(taDepth)));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(tScroll)
		/**/.addComponent(taSource)
		/**/.addComponent(tUnderlying)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(lab)
		/*	*/.addComponent(taDepth)));

		return panel;
		}

	//XXX: Extract to own class?
	//FIXME: Do not resize
	public static class TileSelector extends JLabel
		{
		private static final long serialVersionUID = 1L;
		public int tx, ty;
		private ResourceReference<Background> bkg;

		public TileSelector()
			{
			super();
			setVerticalAlignment(TOP);
			enableEvents(MouseEvent.MOUSE_PRESSED);
			enableEvents(MouseEvent.MOUSE_DRAGGED);
			}

		public void setBackground(ResourceReference<Background> bkg)
			{
			this.bkg = bkg;
			Background b = deRef(bkg);
			if (b == null)
				{
				setIcon(null);
				setPreferredSize(new Dimension(0,0));
				return;
				}
			setPreferredSize(new Dimension(b.width,b.height));
			BufferedImage bi = b.getDisplayImage();
			setIcon(bi == null ? null : new ImageIcon(bi));
			}

		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			Background b = deRef(bkg);
			if (b == null) return;
			//BufferedImage img = bkg.getDisplayImage();
			//if (img == null) return;

			Shape oldClip = g.getClip(); //backup the old clip
			Rectangle oldc = g.getClipBounds();
			//Set the clip properly
			g.setClip(new Rectangle(oldc.x,oldc.y,Math.min(oldc.x + oldc.width,b.width) - oldc.x,
					Math.min(oldc.y + oldc.height,b.height) - oldc.y));

			g.setXORMode(Color.BLACK);
			g.setColor(Color.WHITE);
			g.drawRect(tx,ty,b.tileWidth,b.tileHeight);
			g.setPaintMode(); //just in case
			g.setClip(oldClip); //restore the clip
			}

		protected void processMouseEvent(MouseEvent e)
			{
			if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1
					&& e.getX() < getPreferredSize().width && e.getY() < getPreferredSize().height)
				selectTile(e.getX(),e.getY());
			super.processMouseEvent(e);
			}

		protected void processMouseMotionEvent(MouseEvent e)
			{
			if (e.getID() == MouseEvent.MOUSE_DRAGGED
					&& (e.getModifiers() | MouseEvent.BUTTON1_MASK) != 0) selectTile(e.getX(),e.getY());
			super.processMouseMotionEvent(e);
			}

		public void selectTile(int x, int y)
			{
			Background hardBkg = deRef(bkg);
			if (hardBkg == null)
				{
				tx = x;
				ty = y;
				}
			else
				{
				int w = hardBkg.tileWidth + hardBkg.horizSep;
				int h = hardBkg.tileHeight + hardBkg.vertSep;
				tx = (int) Math.floor((x - hardBkg.horizOffset) / w) * w + hardBkg.horizOffset;
				ty = (int) Math.floor((y - hardBkg.vertOffset) / h) * h + hardBkg.vertOffset;
				}
			repaint();
			}
		}

	public JPanel makeTilesEditPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		tList = new JList(new ArrayListModel<Tile>(res.tiles));
		tList.addListSelectionListener(this);
		tList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tList.setCellRenderer(new TileListComponentRenderer());
		JScrollPane sp = new JScrollPane(tList);
		tDel = new JButton(Messages.getString("RoomFrame.TILE_DELETE")); //$NON-NLS-1$
		tDel.addActionListener(this);
		tLocked = new JCheckBox(Messages.getString("RoomFrame.TILE_LOCKED")); //$NON-NLS-1$

		JPanel pSet = new JPanel();
		pSet.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.TILESET"))); //$NON-NLS-1$
		GroupLayout psl = new GroupLayout(pSet);
		psl.setAutoCreateGaps(true);
		psl.setAutoCreateContainerGaps(true);
		pSet.setLayout(psl);
		teSource = new ResourceMenu<Background>(Room.BACKGROUND,
				Messages.getString("RoomFrame.NO_BACKGROUND"),true,110); //$NON-NLS-1$
		JLabel ltsx = new JLabel(Messages.getString("RoomFrame.TILESET_X")); //$NON-NLS-1$
		tsX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tsX.setColumns(4);
		JLabel ltsy = new JLabel(Messages.getString("RoomFrame.TILESET_Y")); //$NON-NLS-1$
		tsY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tsY.setColumns(4);
		psl.setHorizontalGroup(psl.createParallelGroup()
		/**/.addComponent(teSource)
		/**/.addGroup(psl.createSequentialGroup()
		/*		*/.addComponent(ltsx)
		/*		*/.addComponent(tsX)
		/*		*/.addComponent(ltsy)
		/*		*/.addComponent(tsY)));
		psl.setVerticalGroup(psl.createSequentialGroup()
		/**/.addComponent(teSource)
		/**/.addGroup(psl.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(ltsx)
		/*		*/.addComponent(tsX)
		/*		*/.addComponent(ltsy)
		/*		*/.addComponent(tsY)));

		JPanel pTile = new JPanel();
		pTile.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.TILE"))); //$NON-NLS-1$
		GroupLayout ptl = new GroupLayout(pTile);
		ptl.setAutoCreateGaps(true);
		ptl.setAutoCreateContainerGaps(true);
		pTile.setLayout(ptl);
		JLabel ltx = new JLabel(Messages.getString("RoomFrame.TILE_X")); //$NON-NLS-1$
		tX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tX.setColumns(4);
		tX.addActionListener(this);
		JLabel lty = new JLabel(Messages.getString("RoomFrame.TILE_Y")); //$NON-NLS-1$
		tY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
		tY.setColumns(4);
		tY.addActionListener(this);
		JLabel ltl = new JLabel(Messages.getString("RoomFrame.TILE_LAYER")); //$NON-NLS-1$
		teDepth = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,1000000);
		teDepth.setColumns(8);
		teDepth.addActionListener(this);
		ptl.setHorizontalGroup(ptl.createParallelGroup()
		/**/.addGroup(ptl.createSequentialGroup()
		/*		*/.addComponent(ltx)
		/*		*/.addComponent(tX)
		/*		*/.addComponent(lty)
		/*		*/.addComponent(tY))
		/**/.addGroup(ptl.createSequentialGroup()
		/*		*/.addComponent(ltl)
		/*		*/.addComponent(teDepth)));
		ptl.setVerticalGroup(ptl.createSequentialGroup()
		/**/.addGroup(ptl.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(ltx)
		/*		*/.addComponent(tX)
		/*		*/.addComponent(lty)
		/*		*/.addComponent(tY))
		/**/.addGroup(ptl.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(ltl)
		/*		*/.addComponent(teDepth)));

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(sp,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addComponent(tDel,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(tLocked)
		/**/.addComponent(pSet)
		/**/.addComponent(pTile));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(sp,DEFAULT_SIZE,60,MAX_VALUE)
		/**/.addComponent(tDel)
		/**/.addComponent(tLocked)
		/**/.addComponent(pSet)
		/**/.addComponent(pTile));
		return panel;
		}

	//TODO 1.7?: Batch tile operations
	public JPanel makeTilesBatchPane()
		{
		JPanel panel = new JPanel();
		//		GroupLayout layout = new GroupLayout(panel);
		//		layout.setAutoCreateGaps(true);
		//		layout.setAutoCreateContainerGaps(true);
		//		panel.setLayout(layout);
		panel.add(new JLabel("<html>This tab will offer ways to<br />"
				+ "perform batch operations on several<br />"
				+ "tiles at once, or regions of tiles.</html>"));

		return panel;
		}

	public JPanel makeBackgroundsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		String st = Messages.getString("RoomFrame.DRAW_COLOR"); //$NON-NLS-1$
		bDrawColor = new JCheckBox(st,res.drawBackgroundColor);
		bDrawColor.addActionListener(this);
		JLabel lColor = new JLabel(Messages.getString("RoomFrame.COLOR")); //$NON-NLS-1$
		bColor = new ColorSelect(res.backgroundColor);
		bColor.addActionListener(this);

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
		bList.setVisibleRowCount(4);
		JScrollPane sp = new JScrollPane(bList);

		st = Messages.getString("RoomFrame.BACK_VISIBLE"); //$NON-NLS-1$
		bVisible = new JCheckBox(st,res.backgroundDefs[0].visible);
		bVisible.addActionListener(this);
		st = Messages.getString("RoomFrame.BACK_FOREGROUND"); //$NON-NLS-1$
		bForeground = new JCheckBox(st,res.backgroundDefs[0].foreground);

		bSource = new ResourceMenu<Background>(Room.BACKGROUND,
				Messages.getString("RoomFrame.NO_BACKGROUND"),true,150); //$NON-NLS-1$
		bSource.setSelected(res.backgroundDefs[0].backgroundId);
		bSource.addActionListener(this);

		st = Messages.getString("RoomFrame.BACK_TILE_HOR"); //$NON-NLS-1$
		bTileH = new JCheckBox(st,res.backgroundDefs[0].tileHoriz);
		bTileH.addActionListener(this);
		JLabel lbx = new JLabel(Messages.getString("RoomFrame.BACK_X")); //$NON-NLS-1$
		bX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.backgroundDefs[0].x);
		bX.setColumns(4);
		bX.addActionListener(this);
		st = Messages.getString("RoomFrame.BACK_TILE_VERT"); //$NON-NLS-1$
		bTileV = new JCheckBox(st,res.backgroundDefs[0].tileVert);
		bTileV.addActionListener(this);
		JLabel lby = new JLabel(Messages.getString("RoomFrame.BACK_Y")); //$NON-NLS-1$
		bY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.backgroundDefs[0].y);
		bY.setColumns(4);
		bY.addActionListener(this);
		st = Messages.getString("RoomFrame.BACK_STRETCH"); //$NON-NLS-1$
		bStretch = new JCheckBox(st,res.backgroundDefs[0].stretch);
		bStretch.addActionListener(this);
		JLabel lbh = new JLabel(Messages.getString("RoomFrame.BACK_HSPEED")); //$NON-NLS-1$
		bH = new IntegerField(-999,999,res.backgroundDefs[0].horizSpeed);
		bH.setColumns(4);
		JLabel lbv = new JLabel(Messages.getString("RoomFrame.BACK_VSPEED")); //$NON-NLS-1$
		bV = new IntegerField(-999,999,res.backgroundDefs[0].vertSpeed);
		bH.setColumns(4);

		bList.setSelectedIndex(lastValidBack);

		Insets spi = sp.getInsets();
		int spmh = bList.getMaximumSize().height + spi.bottom + spi.top;
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(bDrawColor)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lColor)
		/*		*/.addComponent(bColor))
		/**/.addComponent(sp)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(bVisible)
		/*		*/.addComponent(bForeground))
		/**/.addComponent(bSource)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lbx)
		/*		*/.addComponent(bX)
		/*		*/.addComponent(lby)
		/*		*/.addComponent(bY))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(lbh)
		/*				*/.addComponent(lbv))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(bH)
		/*				*/.addComponent(bV)))
		/**/.addComponent(bTileH)
		/**/.addComponent(bTileV)
		/**/.addComponent(bStretch));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(bDrawColor)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE,false)
		/*		*/.addComponent(lColor)
		/*		*/.addComponent(bColor))
		/**/.addComponent(sp,DEFAULT_SIZE,DEFAULT_SIZE,spmh)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(bVisible)
		/*		*/.addComponent(bForeground))
		/**/.addComponent(bSource)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lbx)
		/*		*/.addComponent(bX)
		/*		*/.addComponent(lby)
		/*		*/.addComponent(bY))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lbh)
		/*		*/.addComponent(bH))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lbv)
		/*		*/.addComponent(bV))
		/**/.addComponent(bTileH)
		/**/.addComponent(bTileV)
		/**/.addComponent(bStretch));
		return panel;
		}

	public JPanel makeViewsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		String st = Messages.getString("RoomFrame.ENABLE_VIEWS"); //$NON-NLS-1$
		vEnabled = new JCheckBox(st,res.enableViews);

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
		//vList.setVisibleRowCount(4);
		vList.addListSelectionListener(this);
		vList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(vList);

		st = Messages.getString("RoomFrame.VIEW_ENABLED"); //$NON-NLS-1$
		vVisible = new JCheckBox(st,res.views[0].visible);
		vVisible.addActionListener(this);

		JTabbedPane tp = makeViewsDimensionsPane();
		JPanel pf = makeViewsFollowPane();

		vList.setSelectedIndex(lastValidView);

		Insets spi = sp.getInsets();
		int spmh = vList.getMaximumSize().height + spi.bottom + spi.top;
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(vEnabled)
		/**/.addComponent(sp)
		/**/.addComponent(vVisible)
		/**/.addComponent(tp)
		/**/.addComponent(pf));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(vEnabled)
		/**/.addComponent(sp,DEFAULT_SIZE,DEFAULT_SIZE,spmh)
		/**/.addComponent(vVisible)
		/**/.addComponent(tp,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addComponent(pf));
		return panel;
		}

	private JTabbedPane makeViewsDimensionsPane()
		{
		JPanel pr = new JPanel();
		GroupLayout lr = new GroupLayout(pr);
		pr.setLayout(lr);

		JLabel lRX = new JLabel(Messages.getString("RoomFrame.VIEW_X")); //$NON-NLS-1$
		vRX = new IntegerField(0,999999,res.views[0].viewX);
		vRX.setColumns(4);
		JLabel lRW = new JLabel(Messages.getString("RoomFrame.VIEW_W")); //$NON-NLS-1$
		vRW = new IntegerField(1,999999,res.views[0].viewW);
		vRW.setColumns(4);
		JLabel lRY = new JLabel(Messages.getString("RoomFrame.VIEW_Y")); //$NON-NLS-1$
		vRY = new IntegerField(0,999999,res.views[0].viewY);
		vRY.setColumns(4);
		JLabel lRH = new JLabel(Messages.getString("RoomFrame.VIEW_H")); //$NON-NLS-1$
		vRH = new IntegerField(1,999999,res.views[0].viewH);
		vRH.setColumns(4);
		lr.setHorizontalGroup(lr.createSequentialGroup().addContainerGap()
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lRX)
		/*		*/.addComponent(lRY)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(vRX)
		/*		*/.addComponent(vRY)).addGap(8)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lRW)
		/*		*/.addComponent(lRH)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(vRW)
		/*		*/.addComponent(vRH)).addContainerGap());
		lr.setVerticalGroup(lr.createSequentialGroup().addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lRX)
		/*		*/.addComponent(vRX)
		/*		*/.addComponent(lRW)
		/*		*/.addComponent(vRW)).addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lRY)
		/*		*/.addComponent(vRY)
		/*		*/.addComponent(lRH)
		/*		*/.addComponent(vRH)).addGap(8));

		JPanel pp = new JPanel();
		GroupLayout lp = new GroupLayout(pp);
		pp.setLayout(lp);

		JLabel lPX = new JLabel(Messages.getString("RoomFrame.PORT_X")); //$NON-NLS-1$
		vPX = new IntegerField(0,999999,res.views[0].portX);
		vPX.setColumns(4);
		JLabel lPW = new JLabel(Messages.getString("RoomFrame.PORT_W")); //$NON-NLS-1$
		vPW = new IntegerField(1,999999,res.views[0].portW);
		vPW.setColumns(4);
		JLabel lPY = new JLabel(Messages.getString("RoomFrame.PORT_Y")); //$NON-NLS-1$
		vPY = new IntegerField(0,999999,res.views[0].portY);
		vPY.setColumns(4);
		JLabel lPH = new JLabel(Messages.getString("RoomFrame.PORT_H")); //$NON-NLS-1$
		vPH = new IntegerField(1,999999,res.views[0].portH);
		vPH.setColumns(4);
		lp.setHorizontalGroup(lp.createSequentialGroup().addContainerGap()
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(lPX)
		/*		*/.addComponent(lPY)).addGap(4)
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(vPX)
		/*		*/.addComponent(vPY)).addGap(8)
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(lPW)
		/*		*/.addComponent(lPH)).addGap(4)
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(vPW)
		/*		*/.addComponent(vPH)).addContainerGap());
		lp.setVerticalGroup(lp.createSequentialGroup().addGap(4)
		/**/.addGroup(lp.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lPX)
		/*		*/.addComponent(vPX)
		/*		*/.addComponent(lPW)
		/*		*/.addComponent(vPW)).addGap(4)
		/**/.addGroup(lp.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lPY)
		/*		*/.addComponent(vPY)
		/*		*/.addComponent(lPH)
		/*		*/.addComponent(vPH)).addGap(8));

		JTabbedPane tp = new JTabbedPane();
		tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tp.addTab(Messages.getString("RoomFrame.VIEW_IN_ROOM"),pr); //$NON-NLS-1$
		tp.addTab(Messages.getString("RoomFrame.PORT"),pp); //$NON-NLS-1$
		return tp;
		}

	private JPanel makeViewsFollowPane()
		{
		JPanel pf = new JPanel();
		pf.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.FOLLOW"))); //$NON-NLS-1$
		GroupLayout lf = new GroupLayout(pf);
		pf.setLayout(lf);
		vObj = new ResourceMenu<GmObject>(Room.GMOBJECT,
				Messages.getString("RoomFrame.NO_OBJECT"),true,110); //$NON-NLS-1$
		vObj.setSelected(res.views[0].objectFollowing);
		JLabel lH = new JLabel(Messages.getString("RoomFrame.VIEW_HORIZONTAL"));
		JLabel lV = new JLabel(Messages.getString("RoomFrame.VIEW_VERTICAL"));
		JLabel lBorder = new JLabel(Messages.getString("RoomFrame.VIEW_BORDER"));
		JLabel lSpeed = new JLabel(Messages.getString("RoomFrame.VIEW_SPEED"));
		vOHBor = new IntegerField(0,32000,res.views[0].hbor);
		vOHBor.setColumns(4);
		vOHSp = new IntegerField(-1,32000,res.views[0].hspeed);
		vOHSp.setColumns(4);
		vOVBor = new IntegerField(0,32000,res.views[0].vbor);
		vOVBor.setColumns(4);
		vOVSp = new IntegerField(-1,32000,res.views[0].vspeed);
		vOVSp.setColumns(4);
		lf.setHorizontalGroup(lf.createSequentialGroup().addContainerGap()
		/**/.addGroup(lf.createParallelGroup()
		/*		*/.addComponent(vObj)
		/*		*/.addGroup(lf.createSequentialGroup()
		/*				*/.addGroup(lf.createParallelGroup(Alignment.TRAILING)
		/*						*/.addComponent(lH)
		/*						*/.addComponent(lV)).addGap(4)
		/*				*/.addGroup(lf.createParallelGroup()
		/*						*/.addComponent(lBorder)
		/*						*/.addComponent(vOHBor)
		/*						*/.addComponent(vOVBor)).addGap(4)
		/*				*/.addGroup(lf.createParallelGroup()
		/*						*/.addComponent(lSpeed)
		/*						*/.addComponent(vOHSp)
		/*						*/.addComponent(vOVSp)))).addContainerGap());
		lf.setVerticalGroup(lf.createSequentialGroup().addGap(4)
		/**/.addComponent(vObj).addGap(4)
		/*		*/.addGroup(lf.createParallelGroup(Alignment.BASELINE)
		/*				*/.addComponent(lBorder)
		/*				*/.addComponent(lSpeed)).addGap(4)
		/*		*/.addGroup(lf.createParallelGroup(Alignment.BASELINE)
		/*				*/.addComponent(lH)
		/*				*/.addComponent(vOHBor)
		/*				*/.addComponent(vOHSp)).addGap(4)
		/*		*/.addGroup(lf.createParallelGroup(Alignment.BASELINE)
		/*				*/.addComponent(lV)
		/*				*/.addComponent(vOVBor)
		/*				*/.addComponent(vOVSp)).addGap(8));
		return pf;
		}

	private JPanel makeStatsPane()
		{
		JPanel stat = new JPanel();
		stat.setLayout(new BoxLayout(stat,BoxLayout.X_AXIS));
		stat.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));

		statX = new JLabel(Messages.getString("RoomFrame.STAT_X")); //$NON-NLS-1$
		statX.setMaximumSize(new Dimension(50,14));
		stat.add(statX);
		stat.add(new JLabel("|")); //$NON-NLS-1$
		//visible divider    ^   since JSeparator isn't visible

		statY = new JLabel(Messages.getString("RoomFrame.STAT_Y")); //$NON-NLS-1$
		statY.setMaximumSize(new Dimension(50,13));
		stat.add(statY);
		stat.add(new JLabel("|")); //$NON-NLS-1$

		statId = new JLabel();
		statId.setMaximumSize(new Dimension(75,13));
		stat.add(statId);
		stat.add(new JLabel("|")); //$NON-NLS-1$

		statSrc = new JLabel();
		stat.add(statSrc); //resizes at will, so no Max size

		return stat;
		}

	public RoomFrame(Room res, ResNode node)
		{
		super(res,node);
		GroupLayout layout = new GroupLayout(getContentPane())
			{
				@Override
				public void layoutContainer(Container parent)
					{
					Dimension m = RoomFrame.this.getMinimumSize();
					Dimension s = RoomFrame.this.getSize();
					Dimension r = new Dimension(Math.max(m.width,s.width),Math.max(m.height,s.height));
					if (!r.equals(s))
						RoomFrame.this.setSize(r);
					else
						super.layoutContainer(parent);
					}
			};
		setLayout(layout);

		JToolBar tools = makeToolBar();

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

		editor = new RoomEditor(res,this);
		editorPane = new JScrollPane(editor);
		editorPane.getVerticalScrollBar().setUnitIncrement(16);
		editorPane.getVerticalScrollBar().setBlockIncrement(64);
		editorPane.getHorizontalScrollBar().setUnitIncrement(16);
		editorPane.getHorizontalScrollBar().setBlockIncrement(64);
		JPanel stats = makeStatsPane();

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(tools)
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(tabs)
		/*	*/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(editorPane,240,640,DEFAULT_SIZE)
		/*		*/.addComponent(stats,0,DEFAULT_SIZE,DEFAULT_SIZE))));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(tools)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(tabs)
		/*	*/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(editorPane,DEFAULT_SIZE,480,DEFAULT_SIZE)
		/*		*/.addComponent(stats))));
		if (res.rememberWindowSize)
			{
			Dimension d = LGM.mdi.getSize();
			if (d.width < res.editorWidth && d.height < res.editorHeight)
				maximize = true;
			else
				setSize(res.editorWidth,res.editorHeight);
			}
		else
			pack();
		}

	private boolean maximize;

	//maximizes over-sized RoomFrames, since setMaximum can't
	//be called until after it's been added to the MDI
	public void setVisible(boolean b)
		{
		super.setVisible(b);
		if (!maximize) return;
		try
			{
			setMaximum(true);
			}
		catch (PropertyVetoException e)
			{
			setSize(res.editorWidth,res.editorHeight);
			e.printStackTrace();
			}
		}

	public static class ListComponentRenderer implements ListCellRenderer
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
		c.addExclusions(Room.class,"parent","currentTab","deleteUnderlyingObjects","showGrid",
				"showObjects","showTiles","showBackgrounds","showForegrounds","deleteUnderlyingTiles");
		c.addExclusions(Instance.class,"updateTrigger","updateSource");
		c.addExclusions(Tile.class,"updateTrigger","updateSource");
		if (!c.areEqual(res,resOriginal)) return true;
		for (CodeFrame cf : codeFrames.values())
			if (cf.isChanged()) return true;
		return false;
		}

	@Override
	public void revertResource()
		{
		resOriginal.updateReference();
		resOriginal.currentTab = tabs.getSelectedIndex();
		}

	public void commitChanges()
		{
		res.setName(name.getText());

		for (CodeFrame cf : codeFrames.values())
			cf.commit();

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
		res.showGrid = gridVis.isSelected();
		res.isometricGrid = gridIso.isSelected();
		res.snapX = sGW.getIntValue();
		res.snapY = sGH.getIntValue();
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

	private boolean performBackgrounds(Object s)
		{
		if (s == bVisible)
			{
			JLabel lab = ((JLabel) bList.getSelectedValue());
			res.backgroundDefs[lastValidBack].visible = bVisible.isSelected();
			lab.setFont(lab.getFont().deriveFont(bVisible.isSelected() ? Font.BOLD : Font.PLAIN));
			bList.updateUI();
			return true;
			}
		if (s == bSource)
			{
			res.backgroundDefs[lastValidBack].backgroundId = bSource.getSelected();
			return true;
			}
		if (s == bStretch)
			{
			res.backgroundDefs[lastValidBack].stretch = bStretch.isSelected();
			return true;
			}
		if (s == bTileH)
			{
			res.backgroundDefs[lastValidBack].tileHoriz = bTileH.isSelected();
			return true;
			}
		if (s == bTileV)
			{
			res.backgroundDefs[lastValidBack].tileVert = bTileV.isSelected();
			return true;
			}
		if (s == bX)
			{
			res.backgroundDefs[lastValidBack].x = bX.getIntValue();
			return true;
			}
		if (s == bY)
			{
			res.backgroundDefs[lastValidBack].y = bY.getIntValue();
			return true;
			}
		return false;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (editor != null) editor.refresh();
		Object s = e.getSource();

		if (performBackgrounds(s)) return;
		if (s == vVisible)
			{
			JLabel lab = ((JLabel) vList.getSelectedValue());
			res.views[lastValidView].visible = vVisible.isSelected();
			lab.setFont(lab.getFont().deriveFont(vVisible.isSelected() ? Font.BOLD : Font.PLAIN));
			vList.updateUI();
			return;
			}
		if (s == sShow)
			{
			sShowMenu.show(sShow,0,sShow.getHeight());
			return;
			}
		if (s == oSource)
			{
			if (!manualUpdate) return;
			Instance i = (Instance) oList.getSelectedValue();
			if (i == null) return;
			if (oSource.getSelected() == null)
				{
				oSource.setSelected(i.getObject());
				return;
				}
			i.setObject(oSource.getSelected());
			oList.updateUI();
			return;
			}
		if (s == oAdd)
			{
			if (oNew.getSelected() == null) return;
			Instance i = res.addInstance();
			i.setObject(oNew.getSelected());
			oList.setSelectedIndex(res.instances.size() - 1);
			return;
			}
		if (s == oDel)
			{
			int i = oList.getSelectedIndex();
			if (i == -1) return;
			CodeFrame frame = codeFrames.get(res.instances.remove(i));
			if (frame != null) frame.dispose();
			oList.setSelectedIndex(Math.min(res.instances.size() - 1,i));
			return;
			}
		if (s == oX || s == oY)
			{
			Instance i = (Instance) oList.getSelectedValue();
			if (i == null) return;
			//do not wrap into 1 function call, or it will break code in fireObjUpdate
			if (s == oX) i.setPosition(new Point(oX.getIntValue(),i.getPosition().y));
			if (s == oY) i.setPosition(new Point(i.getPosition().x,oY.getIntValue()));
			return;
			}
		if (s == zoomIn)
			{
			if (editor.zoom > 1)
				{
				editor.zoom /= 2;
				editor.refresh();
				zoomOut.setEnabled(true);
				zoomIn.setEnabled(editor.zoom > 1);
				}
			return;
			}
		if (s == zoomOut)
			{
			if (editor.zoom < 32)
				{
				editor.zoom *= 2;
				editor.refresh();
				zoomOut.setEnabled(editor.zoom < 32);
				zoomIn.setEnabled(true);
				}
			return;
			}
		if (s == teSource)
			{
			if (!manualUpdate) return;
			Tile t = (Tile) tList.getSelectedValue();
			if (t == null) return;
			if (teSource.getSelected() == null)
				{
				teSource.setSelected(t.getBackground());
				return;
				}
			t.setBackground(teSource.getSelected());
			tList.updateUI();
			return;
			}
		if (s == taSource)
			{
			tSelect.setBackground(taSource.getSelected());
			return;
			}
		if (s == tDel)
			{
			int i = tList.getSelectedIndex();
			if (i == -1) return;
			res.tiles.remove(i);
			tList.setSelectedIndex(Math.min(res.tiles.size() - 1,i));
			return;
			}
		if (s == tX || s == tY || s == teDepth)
			{
			Tile t = (Tile) tList.getSelectedValue();
			if (t == null) return;
			if (s == teDepth)
				t.setDepth(teDepth.getIntValue());
			else
				{ //do not wrap into 1 function call, or it will break code in fireTileUpdate
				if (s == tX) t.setRoomPosition(new Point(tX.getIntValue(),t.getRoomPosition().y));
				if (s == tY) t.setRoomPosition(new Point(t.getRoomPosition().x,tY.getIntValue()));
				}
			return;
			}
		if (e.getSource() == sCreationCode)
			{
			openCodeFrame(res,"RoomFrame.TITLE_FORMAT_CREATION",res.getName()); //$NON-NLS-1$
			return;
			}
		if (e.getSource() == oCreationCode)
			{
			if (lastObj != null) openCodeFrame(lastObj,"RoomFrame.TITLE_FORMAT_CREATION",Messages.format(//$NON-NLS-1$
					"RoomFrame.INSTANCE",lastObj.instanceId)); //$NON-NLS-1$
			return;
			}
		super.actionPerformed(e);
		}

	public void fireObjUpdate()
		{
		if (lastObj != null)
			{
			lastObj.locked = oLocked.isSelected();
			if (oSource.getSelected() != null) lastObj.setObject(oSource.getSelected());
			lastObj.setPosition(new Point(oX.getIntValue(),oY.getIntValue()));
			}
		lastObj = (Instance) oList.getSelectedValue();
		if (lastObj == null) return;
		oLocked.setSelected(lastObj.locked);
		manualUpdate = false;
		oSource.setSelected(lastObj.getObject());
		manualUpdate = true;
		oX.setIntValue(lastObj.getPosition().x);
		oY.setIntValue(lastObj.getPosition().y);
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

	public void fireTileUpdate()
		{
		if (lastTile != null)
			{
			lastTile.setAutoUpdate(false);
			lastTile.locked = tLocked.isSelected();
			if (teSource.getSelected() != null) lastTile.setBackground(teSource.getSelected());
			lastTile.setBackgroundPosition(new Point(tsX.getIntValue(),tsY.getIntValue()));
			lastTile.setRoomPosition(new Point(tX.getIntValue(),tY.getIntValue()));
			lastTile.setDepth(teDepth.getIntValue());
			lastTile.setAutoUpdate(true);
			}
		lastTile = (Tile) tList.getSelectedValue();
		if (lastTile == null) return;
		tLocked.setSelected(lastTile.locked);
		manualUpdate = false;
		teSource.setSelected(lastTile.getBackground());
		manualUpdate = true;
		tsX.setIntValue(lastTile.getBackgroundPosition().x);
		tsY.setIntValue(lastTile.getBackgroundPosition().y);
		tX.setIntValue(lastTile.getRoomPosition().x);
		tY.setIntValue(lastTile.getRoomPosition().y);
		teDepth.setIntValue(lastTile.getDepth());
		}

	public void fireBackUpdate()
		{
		BackgroundDef b = res.backgroundDefs[lastValidBack];
		b.visible = bVisible.isSelected();
		b.foreground = bForeground.isSelected();
		b.backgroundId = bSource.getSelected();
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
		bSource.setSelected(b.backgroundId);
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
		v.objectFollowing = vObj.getSelected();
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
		vObj.setSelected(v.objectFollowing);
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

	public void openCodeFrame(Object obj, String format, Object arg)
		{
		CodeFrame frame = codeFrames.get(obj);
		if (frame == null)
			{
			frame = new CodeFrame(obj,format,arg,codeFrames);
			LGM.mdi.add(frame);
			LGM.mdi.addZChild(this,frame);
			frame.toTop();
			}
		else
			frame.toTop();
		}

	public static class CodeFrame extends MDIFrame implements ActionListener
		{
		private static final long serialVersionUID = 1L;

		private String getCode()
			{
			if (code instanceof Room) return ((Room) code).creationCode;
			if (code instanceof Instance) return ((Instance) code).getCreationCode();
			throw new RuntimeException(Messages.getString("RoomFrame.CODE_ERROR")); //$NON-NLS-1$
			}

		public void commit()
			{
			if (code instanceof Room)
				((Room) code).creationCode = gta.getTextCompat();
			else if (code instanceof Instance)
				((Instance) code).setCreationCode(gta.getTextCompat());
			else
				throw new RuntimeException(Messages.getString("RoomFrame.CODE_ERROR")); //$NON-NLS-1$
			}

		public void setTitleFormatArg(Object arg)
			{
			this.arg = arg;
			setTitle(Messages.format(format,arg));
			}

		public boolean isChanged()
			{
			return gta.getUndoManager().isModified();
			}

		final Object code;
		final GMLTextArea gta;
		final String format;
		Object arg;
		final JButton save;
		final HashMap<Object,CodeFrame> codeFrames;

		public CodeFrame(Object code, String format, Object arg, HashMap<Object,CodeFrame> cf)
			{
			super(Messages.format(format,arg),true,true,true,true);
			this.code = code;
			this.format = format;
			this.arg = arg;
			codeFrames = cf;
			cf.put(code,this);
			setSize(600,400);
			setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
			// the code text area
			gta = new GMLTextArea(getCode());
			// Setup the toolbar
			JToolBar tool = new JToolBar();
			tool.setFloatable(false);
			tool.setAlignmentX(0);
			add("North",tool); //$NON-NLS-1$
			// Setup the buttons
			save = new JButton(LGM.getIconForKey("ResourceFrame.SAVE")); //$NON-NLS-1$
			save.addActionListener(this);
			tool.add(save);
			tool.addSeparator();
			gta.addEditorButtons(tool);
			getContentPane().add(gta);
			setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(gta));
			}

		public void dispose()
			{
			super.dispose();
			save.removeActionListener(this);
			codeFrames.remove(code);
			}

		public void fireInternalFrameEvent(int id)
			{
			if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING)
				{
				if (isChanged())
					{
					int res = JOptionPane.showConfirmDialog(getParent(),Messages.format(
							"RoomFrame.CODE_CHANGED",arg,Messages.getString("RoomFrame.TITLE_CHANGES"), //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE));
					if (res == JOptionPane.YES_OPTION)
						commit();
					else if (res == JOptionPane.CANCEL_OPTION)
						{
						super.fireInternalFrameEvent(id);
						return;
						}
					}
				dispose();
				}
			super.fireInternalFrameEvent(id);
			}

		public void actionPerformed(ActionEvent e)
			{
			commit();
			dispose();
			}
		}

	public void removeUpdate(DocumentEvent e)
		{
		CodeFrame f = codeFrames.get(res);
		if (f != null) f.setTitleFormatArg(name.getText());
		super.removeUpdate(e);
		}

	public void insertUpdate(DocumentEvent e)
		{
		CodeFrame f = codeFrames.get(res);
		if (f != null) f.setTitleFormatArg(name.getText());
		super.insertUpdate(e);
		}

	public void dispose()
		{
		super.dispose();
		for (CodeFrame cf : codeFrames.values())
			cf.dispose();
		// XXX: These components could still be referenced by InputContext or similar.
		// Removing their references to this frame is therefore necessary in order to ensure
		// garbage collection.
		zoomIn.removeActionListener(this);
		zoomOut.removeActionListener(this);
		sGX.removeActionListener(this);
		sGY.removeActionListener(this);
		sGW.removeActionListener(this);
		sGH.removeActionListener(this);
		gridVis.removeActionListener(this);
		gridIso.removeActionListener(this);
		oNew.removeActionListener(this);
		oList.removeListSelectionListener(this);
		oAdd.removeActionListener(this);
		oDel.removeActionListener(this);
		oSource.removeActionListener(this);
		oX.removeActionListener(this);
		oY.removeActionListener(this);
		oCreationCode.removeActionListener(this);
		sSObj.removeActionListener(this);
		sSTile.removeActionListener(this);
		sSBack.removeActionListener(this);
		sSFore.removeActionListener(this);
		sSView.removeActionListener(this);
		sWidth.removeActionListener(this);
		sHeight.removeActionListener(this);
		sCreationCode.removeActionListener(this);
		sShow.removeActionListener(this);
		teDepth.removeActionListener(this);
		taSource.removeActionListener(this);
		tList.removeListSelectionListener(this);
		tDel.removeActionListener(this);
		tX.removeActionListener(this);
		tY.removeActionListener(this);
		bDrawColor.removeActionListener(this);
		bColor.removeActionListener(this);
		bList.removeListSelectionListener(this);
		bVisible.removeActionListener(this);
		bTileH.removeActionListener(this);
		bX.removeActionListener(this);
		bTileV.removeActionListener(this);
		bY.removeActionListener(this);
		bStretch.removeActionListener(this);
		vList.removeListSelectionListener(this);
		vVisible.removeActionListener(this);
		editorPane.setViewport(null);
		setLayout(null);
		}
	}
