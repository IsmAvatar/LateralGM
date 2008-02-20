/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
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
	private InstanceComponent cursorIC;
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

	public InstanceComponent findInstanceComponent(Point p)
		{
		for (Component c : getComponents())
			{
			if (c instanceof InstanceComponent)
				{
				InstanceComponent ic = (InstanceComponent) c;
				if (new Rectangle(ic.x,ic.y,ic.width,ic.height).contains(p)) return ic;
				}
			}
		return null;
		}

	public void releaseCursorInstance(Point p)
		{
		if (frame.oUnderlying.isSelected())
			{
			for (Component c : getComponents())
				{
				if (c instanceof InstanceComponent)
					{
					InstanceComponent ic = (InstanceComponent) c;
					if (new Rectangle(ic.x,ic.y,ic.width,ic.height).contains(p) && ic != cursorIC
							&& !ic.instance.locked)
						{
						remove(ic);
						room.instances.remove(ic.instance);
						}
					}
				}
			}
		cursorIC = null;
		}

	private void processLeftButton(int modifiers, boolean pressed, InstanceComponent mc, Point p)
		{
		boolean shift = ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0);
		if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
			{
			if (pressed && mc != null && !mc.instance.locked) cursorIC = mc;
			}
		else
			{
			if (shift && cursorIC != null
					&& !new Rectangle(cursorIC.x,cursorIC.y,cursorIC.width,cursorIC.height).contains(p))
				{
				releaseCursorInstance(p);
				pressed = true; //ensures that a new instance is created below
				}
			if (frame.oSource.getSelected() != null && cursorIC == null && pressed)
				{
				Instance i = room.addInstance();
				i.gmObjectId = frame.oSource.getSelected();
				i.setX(p.x);
				i.setY(p.y);
				cursorIC = new InstanceComponent(i);
				add(cursorIC);
				shift = true; //prevents unnecessary coordinate update below
				}
			}
		if (cursorIC != null && !shift)
			{
			cursorIC.instance.setX(p.x);
			cursorIC.instance.setY(p.y);
			}
		}

	private void processRightButton(int modifiers, boolean pressed, InstanceComponent mc, Point p)
		{
		if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
			{
			if (!pressed) return;

			final Instance i = mc.instance;
			JPopupMenu jp = new JPopupMenu();
			JCheckBoxMenuItem cb = new JCheckBoxMenuItem(Messages.getString("RoomEditor.LOCKED"),i.locked); //$NON-NLS-1$
			cb.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						i.locked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
						}
				});
			jp.add(cb);
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

			jp.show(this,p.x,p.y);
			}
		else if (!mc.instance.locked)
			{
			remove(mc);
			room.instances.remove(mc.instance);
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
		frame.statX.setText(Messages.getString("RoomFrame.X") + x); //$NON-NLS-1$
		frame.statY.setText(Messages.getString("RoomFrame.Y") + y); //$NON-NLS-1$
		frame.statId.setText(""); //$NON-NLS-1$
		frame.statSrc.setText(""); //$NON-NLS-1$

		if (frame.tabs.getSelectedIndex() != Room.TAB_TILES)
			{
			InstanceComponent mc = findInstanceComponent(e.getPoint());
			if (mc != null)
				{
				String idt = Messages.getString("RoomFrame.ID") + mc.instance.instanceId; //$NON-NLS-1$
				if (mc.instance.locked) idt += " X"; //$NON-NLS-1$
				frame.statId.setText(idt);
				idt = Messages.getString("RoomFrame.OBJECT") + mc.instance.gmObjectId.get().getName(); //$NON-NLS-1$
				frame.statSrc.setText(idt);
				}
			if (frame.tabs.getSelectedIndex() != Room.TAB_OBJECTS) return;

			if ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0)
				processLeftButton(modifiers,type == MouseEvent.MOUSE_PRESSED,mc,new Point(x,y));
			else if (cursorIC != null) releaseCursorInstance(new Point(x,y));
			if ((modifiers & MouseEvent.BUTTON3_DOWN_MASK) != 0 && mc != null)
				processRightButton(modifiers,type == MouseEvent.MOUSE_PRESSED,mc,e.getPoint()); //use mouse point
			repaint();
			}
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
					JComponent c = (JComponent) e;
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

	public interface DepthSortable
		{
		int getDepth();

		int getId();
		}

	public class DepthComparator implements java.util.Comparator<DepthSortable>
		{
		public int compare(DepthSortable s1, DepthSortable s2)
			{
			int c = Integer.valueOf(s2.getDepth()).compareTo(s1.getDepth());
			if (c == 0)
				{
				Class<?> c1 = s1.getClass();
				Class<?> c2 = s2.getClass();
				if (c1.equals(c2))
					{
					return Integer.valueOf(s1.getId()).compareTo(s2.getId());
					}
				return Integer.valueOf(c1.hashCode()).compareTo(c2.hashCode());
				}
			return c;
			}
		}

	public class InstanceComponent extends JComponent implements DepthSortable
		{
		private static final long serialVersionUID = 1L;
		private final Instance instance;
		private final GmObject object;
		private Sprite sprite;
		private BufferedImage image;
		private final ResourceChangeListener rcl;
		private int x, y, width, height;
		private boolean doListen;

		public InstanceComponent(Instance i)
			{
			instance = i;
			object = deRef(i.gmObjectId);
			rcl = new ResourceChangeListener();
			if (object == null) image = EMPTY_IMAGE;
			}

		private void setListen(boolean l)
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

		private void updateSprite()
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

		private void updateBounds()
			{
			List<DepthSortable> ds = RoomEditor.this.depthSortables;
			int i = ds.indexOf(this);
			int d = getDepth();
			if (i < 0 || (i > 0 && ds.get(i - 1).getDepth() < d)
					|| (i < ds.size() - 1 && ds.get(i + 1).getDepth() > d))
				{
				if (i >= 0) ds.remove(i);
				i = Collections.binarySearch(ds,this,new DepthComparator());
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
			updateSprite();
			updateBounds();
			setListen(true);
			}

		@Override
		public void removeNotify()
			{
			super.removeNotify();
			RoomEditor.this.depthSortables.remove(this);
			setListen(false);
			}

		private class ResourceChangeListener implements ChangeListener
			{
			public void stateChanged(ChangeEvent e)
				{
				updateSprite();
				updateBounds();
				repaint();
				}
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

	public class TileComponent extends JComponent implements DepthSortable
		{
		private static final long serialVersionUID = 1L;
		private final Tile tile;
		private Background background;
		private BufferedImage image;
		private final ResourceChangeListener rcl;
		private int x, y, width, height;
		private boolean doListen;

		WeakReference<Background> bg = null;
		BufferedImage bi = null;

		public TileComponent(Tile t)
			{
			tile = t;
			background = deRef(t.getBackgroundId());
			rcl = new ResourceChangeListener();
			if (background == null) image = EMPTY_IMAGE;
			}

		private void setListen(boolean l)
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

		private void updateBackground()
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

		private void updateBounds()
			{
			List<DepthSortable> ds = RoomEditor.this.depthSortables;
			int i = ds.indexOf(this);
			int d = getDepth();
			if (i < 0 || (i > 0 && ds.get(i - 1).getDepth() < d)
					|| (i < ds.size() - 1 && ds.get(i + 1).getDepth() > d))
				{
				if (i >= 0) ds.remove(i);
				i = Collections.binarySearch(ds,this,new DepthComparator());
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

		public void paintComponent(Graphics g)
			{
			if (image == null) updateImage();
			g.drawImage(image,0,0,null);
			}

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
			updateBackground();
			updateBounds();
			setListen(true);
			}

		@Override
		public void removeNotify()
			{
			super.removeNotify();
			RoomEditor.this.depthSortables.remove(this);
			setListen(false);
			}

		private class ResourceChangeListener implements ChangeListener
			{
			public void stateChanged(ChangeEvent e)
				{
				updateBackground();
				updateBounds();
				repaint();
				}
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
