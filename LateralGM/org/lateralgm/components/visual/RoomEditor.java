/*
 * Copyright (C) 2007, 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import static org.lateralgm.main.Util.deRef;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.subframes.RoomFrame;

public class RoomEditor extends JPanel implements ImageObserver
	{
	private static final long serialVersionUID = 1L;
	private static final BufferedImage EMPTY_IMAGE = new BufferedImage(16,16,
			BufferedImage.TYPE_INT_ARGB);

	private Room room;
	private RoomFrame frame;
	private DepthSortable cursor;
	private Hashtable<Instance,InstanceComponent> instances;
	private List<DepthSortable> depthSortables;

	public RoomEditor(Room r, RoomFrame frame)
		{
		setOpaque(false);
		room = r;
		this.frame = frame;
		refresh();
		enableEvents(MouseEvent.MOUSE_EVENT_MASK | MouseEvent.MOUSE_MOTION_EVENT_MASK);
		instances = new Hashtable<Instance,InstanceComponent>(room.instances.size());
		depthSortables = new ArrayList<DepthSortable>();
		for (Instance i : room.instances)
			{
			InstanceComponent ic = new RoomEditor.InstanceComponent(i);
			add(ic);
			instances.put(i,ic);
			}
		for (Tile t : room.tiles)
			{
			TileComponent tc = new RoomEditor.TileComponent(t);
			add(tc);
			}
		}

	public void refresh()
		{
		Dimension s = new Dimension(frame.sWidth.getIntValue(),frame.sHeight.getIntValue());
		setPreferredSize(s);
		revalidate();
		repaint();
		}

	protected void processMouseEvent(MouseEvent e)
		{
		super.processMouseEvent(e);
		mouseEdit(e);
		}

	protected void processMouseMotionEvent(MouseEvent e)
		{
		super.processMouseMotionEvent(e);
		mouseEdit(e);
		}

	public DepthSortable findSpecificDepthSortable(Point p, boolean instance)
		{
		for (Component c : getComponents())
			{
			if (c instanceof DepthSortable)
				{
				DepthSortable ds = (DepthSortable) c;
				if (new Rectangle(ds.x,ds.y,ds.width,ds.height).contains(p))
					{
					if (ds instanceof InstanceComponent && instance) return ds;
					if (ds instanceof TileComponent && !instance) return ds;
					}
				}
			}
		return null;
		}

	public void releaseCursor(Point p)
		{ //it must be guaranteed that cursor != null
		if ((cursor instanceof InstanceComponent && frame.oUnderlying.isSelected())
				|| (cursor instanceof TileComponent && frame.tUnderlying.isSelected()))
			{
			for (Component c : getComponents())
				{
				if (c instanceof DepthSortable)
					{
					DepthSortable ds = (DepthSortable) c;
					if (ds != cursor && !ds.isLocked() && ds.getClass() == cursor.getClass()
							&& new Rectangle(ds.x,ds.y,ds.width,ds.height).contains(p))
						{
						remove(ds);
						if (ds instanceof InstanceComponent)
							room.instances.remove(((InstanceComponent) ds).instance);
						else if (ds instanceof TileComponent) room.tiles.remove(((TileComponent) ds).tile);
						}
					}
				}
			}
		cursor = null;
		}

	private void processLeftButton(int modifiers, boolean pressed, DepthSortable mc, Point p)
		{
		boolean shift = ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0);
		if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
			{
			if (pressed && mc != null && !mc.isLocked()) cursor = mc;
			}
		else
			{
			if (shift && cursor != null)
				if (!new Rectangle(cursor.x,cursor.y,cursor.width,cursor.height).contains(p))
					{
					releaseCursor(p);
					pressed = true; //ensures that a new instance is created below
					}
			if (pressed && cursor == null)
				{
				if (frame.tabs.getSelectedIndex() == Room.TAB_TILES)
					{
					WeakReference<Background> bkg = frame.tSource.getSelected();
					if (bkg == null) return; //I'd rather just break out of this IF, but this works
					Tile t = room.addTile();
					t.setBackgroundId(bkg);
					t.setX(p.x);
					t.setY(p.y);
					cursor = new TileComponent(t);
					add(cursor);
					shift = true; //prevents unnecessary coordinate update below
					}
				else if (frame.tabs.getSelectedIndex() == Room.TAB_OBJECTS)
					{
					WeakReference<GmObject> obj = frame.oSource.getSelected();
					if (obj == null) return; //I'd rather just break out of this IF, but this works
					Instance i = room.addInstance();
					i.gmObjectId = obj;
					i.setX(p.x);
					i.setY(p.y);
					cursor = new InstanceComponent(i);
					add(cursor);
					shift = true; //prevents unnecessary coordinate update below
					}
				}
			}
		if (cursor != null && !shift)
			{
			if (cursor instanceof InstanceComponent)
				{
				InstanceComponent ic = (InstanceComponent) cursor;
				ic.instance.setX(p.x);
				ic.instance.setY(p.y);
				}
			else if (cursor instanceof TileComponent)
				{
				TileComponent ic = (TileComponent) cursor;
				ic.tile.setX(p.x);
				ic.tile.setY(p.y);
				}
			}
		}

	private void processRightButton(int modifiers, boolean pressed, final DepthSortable mc, Point p)
		{
		if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
			{
			if (!pressed) return;

			JPopupMenu jp = new JPopupMenu();
			JCheckBoxMenuItem cb = new JCheckBoxMenuItem(
					Messages.getString("RoomEditor.LOCKED"),mc.isLocked()); //$NON-NLS-1$
			cb.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						mc.setLocked(((JCheckBoxMenuItem) e.getSource()).isSelected());
						}
				});
			jp.add(cb);

			if (mc instanceof InstanceComponent)
				{
				final Instance i = ((InstanceComponent) mc).instance;
				JMenuItem mi = new JMenuItem(Messages.getString("RoomEditor.CREATION_CODE")); //$NON-NLS-1$
				mi.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
							{
							frame.openCodeFrame(i,Messages.getString("RoomFrame.TITLE_FORMAT_CREATION"), //$NON-NLS-1$
									String.format(Messages.getString("RoomFrame.INSTANCE"),i.instanceId)); //$NON-NLS-1$
							}
					});
				jp.add(mi);
				}
			jp.show(this,p.x,p.y);
			}
		else if (!mc.isLocked())
			{
			remove(mc);
			if (mc instanceof InstanceComponent)
				room.instances.remove(((InstanceComponent) mc).instance);
			else if (mc instanceof TileComponent) room.instances.remove(((TileComponent) mc).tile);
			}
		}

	protected void mouseEdit(MouseEvent e)
		{
		int modifiers = e.getModifiersEx();
		int type = e.getID();
		int x = e.getX();
		int y = e.getY();
		if ((modifiers & MouseEvent.ALT_DOWN_MASK) == 0)
			{
			x = x / room.snapX * room.snapX;
			y = y / room.snapY * room.snapY;
			}
		frame.statX.setText(Messages.getString("RoomFrame.STAT_X") + x); //$NON-NLS-1$
		frame.statY.setText(Messages.getString("RoomFrame.STAT_Y") + y); //$NON-NLS-1$
		frame.statId.setText(""); //$NON-NLS-1$
		frame.statSrc.setText(""); //$NON-NLS-1$

		DepthSortable mc = null;
		if (frame.tabs.getSelectedIndex() == Room.TAB_TILES)
			{
			mc = findSpecificDepthSortable(e.getPoint(),false);
			if (mc != null)
				{
				Tile tile = ((TileComponent) mc).tile;
				String idt = Messages.getString("RoomFrame.STAT_ID") + tile.tileId; //$NON-NLS-1$
				if (mc.isLocked()) idt += " X"; //$NON-NLS-1$
				frame.statId.setText(idt);
				idt = Messages.getString("RoomFrame.STAT_TILESET") + tile.getBackgroundId().get().getName(); //$NON-NLS-1$
				frame.statSrc.setText(idt);
				}
			}
		else
			{
			mc = findSpecificDepthSortable(e.getPoint(),true);
			if (mc != null)
				{
				Instance instance = ((InstanceComponent) mc).instance;
				String idt = Messages.getString("RoomFrame.STAT_ID") + instance.instanceId; //$NON-NLS-1$
				if (mc.isLocked()) idt += " X"; //$NON-NLS-1$
				frame.statId.setText(idt);
				idt = Messages.getString("RoomFrame.STAT_OBJECT") + instance.gmObjectId.get().getName(); //$NON-NLS-1$
				frame.statSrc.setText(idt);
				}
			if (frame.tabs.getSelectedIndex() != Room.TAB_OBJECTS) return;
			}

		if ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0)
			processLeftButton(modifiers,type == MouseEvent.MOUSE_PRESSED,mc,new Point(x,y));
		else if (cursor != null) releaseCursor(new Point(x,y));
		if ((modifiers & MouseEvent.BUTTON3_DOWN_MASK) != 0 && mc != null)
			processRightButton(modifiers,type == MouseEvent.MOUSE_PRESSED,mc,e.getPoint()); //use mouse point
		repaint();
		}

	@Override
	public void paintComponent(Graphics g)
		{
		Graphics g2 = g.create();
		int width = frame.sWidth.getIntValue();
		int height = frame.sHeight.getIntValue();
		g2.clipRect(0,0,width,height);
		g2.setColor(frame.bDrawColor.isSelected() ? frame.bColor.getSelectedColor() : Color.BLACK);
		g2.fillRect(0,0,width,height);
		g2.dispose();
		}

	private void paintBackground(Graphics g, BackgroundDef bd, int width, int height)
		{
		BufferedImage bi = bd.backgroundId.get().backgroundImage;
		if (bi == null) return;
		if (bd.tileHoriz || bd.tileVert)
			{
			int x = bd.x;
			int y = bd.y;
			int ncol = 1;
			int nrow = 1;
			int w = bd.stretch ? width : bi.getWidth();
			int h = bd.stretch ? height : bi.getHeight();
			if (bd.tileHoriz)
				{
				x = 1 + ((bd.x + w - 1) % w) - w;
				ncol = 1 + (width - x - 1) / w;
				}
			if (bd.tileVert)
				{
				y = 1 + ((bd.y + h - 1) % h) - h;
				nrow = 1 + (height - y - 1) / h;
				}
			for (int row = 0; row < nrow; row++)
				for (int col = 0; col < ncol; col++)
					if (bd.stretch)
						g.drawImage(bi,x + w * col,y + h * row,w,h,this);
					else
						g.drawImage(bi,x + w * col,y + h * row,this);
			}
		else if (bd.stretch)
			g.drawImage(bi,bd.x,bd.y,width,height,this);
		else
			g.drawImage(bi,bd.x,bd.y,this);
		}

	@Override
	public void paintChildren(Graphics g)
		{
		Graphics g2 = g.create();
		int width = frame.sWidth.getIntValue();
		int height = frame.sHeight.getIntValue();
		g2.clipRect(0,0,width,height);
		if (frame.sSBack.isSelected())
			{
			for (int i = 0; i < 8; i++)
				{
				BackgroundDef bd = frame.res.backgroundDefs[i];
				if (!bd.visible || bd.foreground || deRef(bd.backgroundId) == null) continue;
				paintBackground(g2,bd,width,height);
				}
			}
		if (frame.sSObj.isSelected() || frame.sSTile.isSelected())
			{
			for (DepthSortable e : depthSortables)
				{
				if (e instanceof InstanceComponent && !frame.sSObj.isSelected()) continue;
				if (e instanceof TileComponent && !frame.sSTile.isSelected()) continue;
				if (e instanceof JComponent)
					{
					JComponent c = e;
					Graphics cg = g.create(c.getX(),c.getY(),c.getWidth(),c.getHeight());
					c.paint(cg);
					cg.dispose();
					}
				}
			}
		if (frame.sSFore.isSelected())
			{
			for (int i = 0; i < 8; i++)
				{
				BackgroundDef bd = frame.res.backgroundDefs[i];
				if (!bd.visible || !bd.foreground || deRef(bd.backgroundId) == null) continue;
				paintBackground(g2,bd,width,height);
				}
			}
		if (frame.sGridVis.isSelected())
			{
			int w = frame.sSnapX.getIntValue();
			int h = frame.sSnapY.getIntValue();
			if (w > 3)
				{
				g2.setXORMode(Color.BLACK);
				g2.setColor(Color.WHITE);
				for (int x = 0; x < width; x += w)
					g2.drawLine(x,0,x,height - 1);
				}
			if (h > 3)
				{
				g2.setXORMode(Color.BLACK);
				g2.setColor(Color.WHITE);
				for (int y = 0; y < height; y += h)
					g2.drawLine(0,y,width - 1,y);
				}
			}
		g2.dispose();
		}

	public abstract class DepthSortable extends JComponent implements Comparable<DepthSortable>
		{
		protected final ResourceChangeListener rcl = new ResourceChangeListener();
		protected BufferedImage image;
		protected int x, y, width, height;
		protected boolean doListen;

		@Override
		public int getHeight()
			{
			return height;
			}

		@Override
		public int getWidth()
			{
			return width;
			}

		@Override
		public int getX()
			{
			return x;
			}

		@Override
		public int getY()
			{
			return y;
			}

		@Override
		public void addNotify()
			{
			super.addNotify();
			updateSource();
			updateBounds();
			setListen(true);
			}

		@Override
		public void removeNotify()
			{
			super.removeNotify();
			depthSortables.remove(this);
			setListen(false);
			}

		public int compareTo(DepthSortable s2)
			{
			int c = Integer.valueOf(s2.getDepth()).compareTo(getDepth());
			if (c == 0)
				{
				Class<?> c1 = getClass();
				Class<?> c2 = s2.getClass();
				if (c1.equals(c2))
					{
					return Integer.valueOf(getId()).compareTo(s2.getId());
					}
				return Integer.valueOf(c1.hashCode()).compareTo(c2.hashCode());
				}
			return c;
			}

		protected abstract void setListen(boolean l);

		public abstract boolean isLocked();

		public abstract void setLocked(boolean lock);

		protected abstract void updateSource();

		protected abstract void updateBounds();

		public abstract int getDepth();

		public abstract int getId();

		protected class ResourceChangeListener implements ChangeListener
			{
			public void stateChanged(ChangeEvent e)
				{
				updateSource();
				updateBounds();
				repaint();
				}
			}
		}

	public class InstanceComponent extends DepthSortable
		{
		private static final long serialVersionUID = 1L;
		private final Instance instance;
		private final GmObject object;
		private Sprite sprite;

		public InstanceComponent(Instance i)
			{
			instance = i;
			object = deRef(i.gmObjectId);
			if (object == null) image = EMPTY_IMAGE;
			}

		protected void setListen(boolean l)
			{
			if (l == doListen) return;
			if (l)
				{
				if (sprite != null) sprite.addChangeListener(rcl);
				if (object != null) object.addChangeListener(rcl);
				instance.addChangeListener(rcl);
				}
			else
				{
				if (sprite != null) sprite.removeChangeListener(rcl);
				if (object != null) object.removeChangeListener(rcl);
				instance.removeChangeListener(rcl);
				}
			doListen = l;
			}

		protected void updateSource()
			{
			Sprite s = deRef(object.sprite);
			if (s != sprite)
				{
				if (sprite != null) sprite.removeChangeListener(rcl);
				if (doListen && s != null) s.addChangeListener(rcl);
				image = null;
				sprite = s;
				}
			}

		protected void updateBounds()
			{
			List<DepthSortable> ds = depthSortables;
			int i = ds.indexOf(this);
			int d = getDepth();
			if (i < 0 || (i > 0 && ds.get(i - 1).getDepth() < d)
					|| (i < ds.size() - 1 && ds.get(i + 1).getDepth() > d))
				{
				if (i >= 0) ds.remove(i);
				i = Collections.binarySearch(ds,this);
				if (i < 0) ds.add(-i - 1,this);
				}
			x = instance.getX() - (sprite == null ? 0 : sprite.originX);
			y = instance.getY() - (sprite == null ? 0 : sprite.originY);
			if (sprite == null)
				{
				width = EMPTY_IMAGE.getWidth();
				height = EMPTY_IMAGE.getHeight();
				}
			else
				{
				width = sprite.width;
				height = sprite.height;
				}
			invalidate();
			}

		private void updateImage()
			{
			image = sprite == null ? null : sprite.getDisplayImage();
			if (image == null)
				{
				image = EMPTY_IMAGE;
				setOpaque(false);
				}
			else
				{
				setOpaque(!sprite.transparent);
				}
			}

		@Override
		public void paintComponent(Graphics g)
			{
			if (object == null)
				{
				getParent().remove(this);
				return;
				}
			if (image == null) updateImage();
			g.drawImage(image,0,0,null);
			}

		public boolean isLocked()
			{
			return instance.locked;
			}

		public void setLocked(boolean lock)
			{
			instance.locked = lock;
			}

		public int getDepth()
			{
			GmObject o = instance.gmObjectId.get();
			if (o == null)
				return 0;
			else
				return o.depth;
			}

		public int getId()
			{
			return instance.instanceId;
			}
		}

	public class TileComponent extends DepthSortable
		{
		private static final long serialVersionUID = 1L;
		private final Tile tile;
		private Background background;

		WeakReference<Background> bg = null;
		BufferedImage bi = null;

		public TileComponent(Tile t)
			{
			tile = t;
			background = deRef(t.getBackgroundId());
			if (background == null) image = EMPTY_IMAGE;
			}

		protected void setListen(boolean l)
			{
			if (l == doListen) return;
			if (l)
				{
				if (background != null) background.addChangeListener(rcl);
				tile.addChangeListener(rcl);
				}
			else
				{
				if (background != null) background.removeChangeListener(rcl);
				tile.removeChangeListener(rcl);
				}
			doListen = l;
			}

		protected void updateSource()
			{
			Background b = deRef(tile.getBackgroundId());
			if (b != background)
				{
				if (background != null) background.removeChangeListener(rcl);
				if (doListen && b != null) b.addChangeListener(rcl);
				image = null;
				background = b;
				}
			}

		protected void updateBounds()
			{
			List<DepthSortable> ds = depthSortables;
			int i = ds.indexOf(this);
			int d = getDepth();
			if (i < 0 || (i > 0 && ds.get(i - 1).getDepth() < d)
					|| (i < ds.size() - 1 && ds.get(i + 1).getDepth() > d))
				{
				if (i >= 0) ds.remove(i);
				i = Collections.binarySearch(ds,this);
				if (i < 0) ds.add(-i - 1,this);
				}
			x = tile.getX();
			y = tile.getY();
			width = tile.getWidth();
			height = tile.getHeight();
			invalidate();
			}

		private void updateImage()
			{
			image = background == null ? null : background.getDisplayImage();
			if (image == null)
				{
				image = EMPTY_IMAGE;
				setOpaque(false);
				}
			else
				{
				image = image.getSubimage(tile.getTileX(),tile.getTileY(),tile.getWidth(),tile.getHeight());
				setOpaque(!background.transparent);
				}
			}

		@Override
		public void paintComponent(Graphics g)
			{
			if (image == null) updateImage();
			g.drawImage(image,0,0,null);
			}

		public boolean isLocked()
			{
			return tile.locked;
			}

		public void setLocked(boolean lock)
			{
			tile.locked = lock;
			}

		public int getDepth()
			{
			return tile.getDepth();
			}

		public int getId()
			{
			return tile.tileId;
			}
		}
	}
