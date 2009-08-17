/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.lateralgm.main.Util;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.ActiveArrayList.ListUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class RoomVisual extends AbstractVisual implements BoundedVisual,UpdateListener
	{
	protected static final BufferedImage EMPTY_IMAGE = new BufferedImage(16,16,
			BufferedImage.TYPE_INT_ARGB);

	private final BinVisual binVisual;
	private final GridVisual gridVisual;
	public final Room room;

	// These variables are here to keep the managers from being GC'd.
	protected final InstanceVisualListManager ivlm;
	protected final TileVisualListManager tvlm;

	private final RoomPropertyListener rpl = new RoomPropertyListener();
	private final BgDefPropertyListener bdpl = new BgDefPropertyListener();

	private EnumSet<Show> show;
	private int gridFactor = 1;
	private int gridX, gridY;

	public enum Show
		{
		BACKGROUNDS,INSTANCES,TILES,FOREGROUNDS,GRID,VIEWS
		}

	public RoomVisual(VisualContainer vc, Room r)
		{
		this(vc,r,EnumSet.range(Show.BACKGROUNDS,Show.GRID));
		}

	public RoomVisual(VisualContainer vc, Room r, EnumSet<Show> s)
		{
		super(vc);
		room = r;
		show = EnumSet.copyOf(s);
		binVisual = new BinVisual(vc,128,(Integer) r.get(PRoom.WIDTH),(Integer) r.get(PRoom.HEIGHT));
		gridVisual = new GridVisual((Boolean) r.get(PRoom.ISOMETRIC),(Integer) r.get(PRoom.SNAP_X),
				(Integer) r.get(PRoom.SNAP_Y));
		r.properties.updateSource.addListener(rpl);
		ivlm = new InstanceVisualListManager();
		tvlm = new TileVisualListManager();
		for (BackgroundDef bd : room.backgroundDefs)
			{
			bd.properties.updateSource.addListener(bdpl);
			bd.updateSource.addListener(this);
			}
		}

	public void extendBounds(Rectangle b)
		{
		b.add(new Rectangle(0,0,(Integer) room.get(PRoom.WIDTH),(Integer) room.get(PRoom.HEIGHT)));
		binVisual.extendBounds(b);
		}

	public void paint(Graphics g)
		{
		int width = (Integer) room.get(PRoom.WIDTH);
		int height = (Integer) room.get(PRoom.HEIGHT);
		Graphics g2 = g.create();
		g2.clipRect(0,0,width,height);
		if (room.get(PRoom.DRAW_BACKGROUND_COLOR))
			{
			g2.setColor((Color) room.get(PRoom.BACKGROUND_COLOR));
			g2.fillRect(0,0,width,height);
			}
		if (show.contains(Show.BACKGROUNDS)) for (BackgroundDef bd : room.backgroundDefs)
			if (shouldPaint(bd,false)) paintBackground(g2,bd,width,height);
		// Paint pieces and tiles on the unclipped g, so that they are visible
		// even if outside the room
		if (show.contains(Show.INSTANCES) || show.contains(Show.TILES)) binVisual.paint(g);
		if (show.contains(Show.FOREGROUNDS)) for (BackgroundDef bd : room.backgroundDefs)
			if (shouldPaint(bd,true)) paintBackground(g2,bd,width,height);
		if (show.contains(Show.GRID))
			{
			g2.translate(gridX
					- (room.get(PRoom.ISOMETRIC) ? (Integer) room.get(PRoom.SNAP_X) * (gridFactor - 1) / 2
							: 0),gridY);
			gridVisual.paint(g2);
			}
		g2.dispose();
		}

	private static boolean shouldPaint(BackgroundDef bd, Boolean fg)
		{
		if (!(Boolean) bd.properties.get(PBackgroundDef.VISIBLE)) return false;
		return fg.equals(bd.properties.get(PBackgroundDef.FOREGROUND));
		}

	public void setVisible(Show s, boolean v)
		{
		if (v ? show.add(s) : show.remove(s))
			{
			if (s == Show.GRID && !v) gridVisual.flush(true);
			repaint(null);
			}
		}

	public void setGridFactor(int f)
		{
		gridFactor = f;
		gridVisual.setWidth(gridFactor * (Integer) room.get(PRoom.SNAP_X));
		gridVisual.setHeight(gridFactor * (Integer) room.get(PRoom.SNAP_Y));
		}

	public void setGridOffset(int x, int y)
		{
		if (gridX == x && gridY == y) return;
		gridX = x;
		gridY = y;
		if (show.contains(Show.GRID)) repaint(null);
		}

	public void setGridXOffset(int x)
		{
		if (gridX == x) return;
		gridX = x;
		if (show.contains(Show.GRID)) repaint(null);
		}

	public void setGridYOffset(int y)
		{
		if (gridY == y) return;
		gridY = y;
		if (show.contains(Show.GRID)) repaint(null);
		}

	public <P extends Piece>Iterator<P> intersect(Rectangle r, Class<P> p)
		{
		final Iterator<PieceVisual<P>> vi = binVisual.intersect(r,getVisualClass(p));
		return new Iterator<P>()
			{
				public boolean hasNext()
					{
					return vi.hasNext();
					}

				public P next()
					{
					return vi.next().piece;
					}

				public void remove()
					{
					vi.remove();
					}
			};
		}

	public boolean intersects(Rectangle r, Piece p)
		{
		Iterator<Piece> pi = intersect(r);
		while (pi.hasNext())
			if (pi.next() == p) return true;
		return false;
		}

	@SuppressWarnings("unchecked")
	private <P extends Piece, V extends PieceVisual<P>>Class<V> getVisualClass(Class<P> p)
		{
		if (p == Piece.class) return (Class<V>) PieceVisual.class;
		if (p == Instance.class) return (Class<V>) InstanceVisual.class;
		if (p == Tile.class) return (Class<V>) TileVisual.class;
		throw new IllegalArgumentException();
		}

	public Iterator<Tile> intersectTiles(Rectangle r)
		{
		return intersect(r,Tile.class);
		}

	public Iterator<Instance> intersectInstances(Rectangle r)
		{
		return intersect(r,Instance.class);
		}

	public Iterator<Piece> intersect(Rectangle r)
		{
		return intersect(r,Piece.class);
		}

	private void paintBackground(Graphics g, BackgroundDef bd, int width, int height)
		{
		Rectangle c = g.getClipBounds();
		ResourceReference<Background> rb = bd.properties.get(PBackgroundDef.BACKGROUND);
		Background b = Util.deRef(rb);
		if (b == null) return;
		BufferedImage bi = b.getDisplayImage();
		if (bi == null) return;
		boolean stretch = bd.properties.get(PBackgroundDef.STRETCH);
		int w = stretch ? width : bi.getWidth();
		int h = stretch ? height : bi.getHeight();
		boolean tileHoriz = bd.properties.get(PBackgroundDef.TILE_HORIZ);
		boolean tileVert = bd.properties.get(PBackgroundDef.TILE_VERT);
		int x = bd.properties.get(PBackgroundDef.X);
		int y = bd.properties.get(PBackgroundDef.Y);
		if (tileHoriz || tileVert)
			{
			int ncol = 1;
			int nrow = 1;
			if (tileHoriz)
				{
				x = 1 + c.x + ((x + w - 1 - c.x) % w) - w;
				ncol = 1 + (c.x + c.width - x - 1) / w;
				}
			if (tileVert)
				{
				y = 1 + c.y + ((y + h - 1 - c.y) % h) - h;
				nrow = 1 + (c.y + c.height - y - 1) / h;
				}
			for (int row = 0; row < nrow; row++)
				for (int col = 0; col < ncol; col++)
					g.drawImage(bi,(x + w * col),(y + h * row),w,h,null);
			}
		else
			g.drawImage(bi,x,y,w,h,null);
		}

	private abstract class PieceVisual<P extends Piece> extends VisualBox
		{
		protected final ResourceUpdateListener rul = new ResourceUpdateListener();
		public final P piece;
		private boolean invalid;

		public PieceVisual(P p)
			{
			super(binVisual);
			piece = p;
			}

		protected abstract void validate();

		protected final void invalidate()
			{
			if (invalid) return;
			invalid = true;
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
						{
						try
							{
							if (invalid) validate();
							}
						finally
							{
							invalid = false;
							}
						}
				});
			}

		protected class ResourceUpdateListener implements UpdateListener
			{
			public void updated(UpdateEvent e)
				{
				invalidate();
				}
			}
		}

	private class InstanceVisual extends PieceVisual<Instance>
		{
		private BufferedImage image;
		private final InstancePropertyListener ipl = new InstancePropertyListener();

		public InstanceVisual(Instance i)
			{
			super(i);
			i.updateSource.addListener(rul);
			i.properties.updateSource.addListener(ipl);
			validate();
			}

		@Override
		protected void validate()
			{
			ResourceReference<GmObject> ro = piece.properties.get(PInstance.OBJECT);
			GmObject o = ro == null ? null : ro.get();
			ResourceReference<Sprite> rs = null;
			if (o != null) rs = o.get(PGmObject.SPRITE);
			Sprite s = rs == null ? null : rs.get();
			image = s == null ? null : s.getDisplayImage();
			if (image == null) image = EMPTY_IMAGE;
			binVisual.setDepth(this,o == null ? 0 : (Integer) o.get(PGmObject.DEPTH));
			Point p = piece.getPosition();
			if (s != null)
				p.translate(-(Integer) s.get(PSprite.ORIGIN_X),-(Integer) s.get(PSprite.ORIGIN_Y));
			setBounds(new Rectangle(p.x,p.y,image.getWidth(),image.getHeight()));
			}

		public void paint(Graphics g)
			{
			if (show.contains(Show.INSTANCES)) g.drawImage(image,0,0,null);
			}

		@Override
		public void remove()
			{
			piece.updateSource.removeListener(rul);
			piece.properties.updateSource.removeListener(ipl);
			image = null;
			super.remove();
			}

		class InstancePropertyListener extends PropertyUpdateListener<PInstance>
			{
			@Override
			public void updated(PropertyUpdateEvent<PInstance> e)
				{
				switch (e.key)
					{
					case X:
					case Y:
					case OBJECT:
						invalidate();
						break;
					}
				}
			}
		}

	private class TileVisual extends PieceVisual<Tile>
		{
		private BufferedImage image;
		private final TilePropertyListener tpl = new TilePropertyListener();

		public TileVisual(Tile t)
			{
			super(t);
			t.updateSource.addListener(rul);
			t.properties.updateSource.addListener(tpl);
			validate();
			}

		@Override
		protected void validate()
			{
			ResourceReference<Background> rb = piece.properties.get(PTile.BACKGROUND);
			Background b = rb == null ? null : rb.get();
			BufferedImage bi = b == null ? null : b.getDisplayImage();
			if (bi == null)
				image = EMPTY_IMAGE;
			else
				{
				Point p = piece.getBackgroundPosition();
				Dimension d = piece.getSize();
				try
					{
					image = bi.getSubimage(p.x,p.y,d.width,d.height);
					}
				catch (RasterFormatException e)
					{
					image = EMPTY_IMAGE;
					}
				}
			binVisual.setDepth(this,piece.getDepth());
			Rectangle r = new Rectangle(piece.getRoomPosition(),piece.getSize());
			setBounds(r);
			}

		public void paint(Graphics g)
			{
			if (show.contains(Show.TILES)) g.drawImage(image,0,0,null);
			}

		@Override
		public void remove()
			{
			piece.updateSource.removeListener(rul);
			piece.properties.updateSource.removeListener(tpl);
			image = null;
			super.remove();
			}

		class TilePropertyListener extends PropertyUpdateListener<PTile>
			{
			@Override
			public void updated(PropertyUpdateEvent<PTile> e)
				{
				switch (e.key)
					{
					case DEPTH:
					case ROOM_X:
					case ROOM_Y:
						invalidate();
						break;
					}
				}
			}
		}

	private class InstanceVisualListManager extends VisualListManager<Instance,InstanceVisual>
		{
		public InstanceVisualListManager()
			{
			super(room.instances);
			}

		protected InstanceVisual createVisual(Instance t)
			{
			return new InstanceVisual(t);
			}

		protected Instance getT(InstanceVisual v)
			{
			return v.piece;
			}
		}

	private class TileVisualListManager extends VisualListManager<Tile,TileVisual>
		{
		public TileVisualListManager()
			{
			super(room.tiles);
			}

		protected TileVisual createVisual(Tile t)
			{
			return new TileVisual(t);
			}

		protected Tile getT(TileVisual v)
			{
			return v.piece;
			}
		}

	private abstract static class VisualListManager<T, V extends VisualBox> implements UpdateListener
		{
		public final ActiveArrayList<T> tList;
		private final ArrayList<V> vList;

		public VisualListManager(ActiveArrayList<T> tl)
			{
			tList = tl;
			vList = new ArrayList<V>(tl.size());
			for (T t : tl)
				vList.add(createVisual(t));
			tl.updateSource.addListener(this);
			}

		protected abstract V createVisual(T t);

		protected abstract T getT(V v);

		public void updated(UpdateEvent e)
			{
			ListUpdateEvent lue = (ListUpdateEvent) e;
			switch (lue.type)
				{
				case ADDED:
					for (int i = lue.fromIndex; i <= lue.toIndex; i++)
						{
						T t = tList.get(i);
						V v = createVisual(t);
						vList.add(i,v);
						}
					break;
				case REMOVED:
					for (int i = lue.toIndex; i >= lue.fromIndex; i--)
						vList.remove(i).remove();
					break;
				case CHANGED:
					HashSet<T> ts = new HashSet<T>(tList);
					HashMap<T,V> tm = new HashMap<T,V>(Math.min(vList.size(),tList.size()));
					for (V v : vList)
						{
						T t = getT(v);
						if (ts.contains(t))
							tm.put(t,v);
						else
							v.remove();
						}
					vList.clear();
					for (T t : tList)
						{
						V v = tm.get(t);
						vList.add(v == null ? createVisual(t) : v);
						}
				}
			assert tList.size() == vList.size();
			}
		}

	private class RoomPropertyListener extends PropertyUpdateListener<PRoom>
		{
		public void updated(PropertyUpdateEvent<PRoom> e)
			{
			switch (e.key)
				{
				case BACKGROUND_COLOR:
					if (room.get(PRoom.DRAW_BACKGROUND_COLOR)) repaint(null);
					break;
				case DRAW_BACKGROUND_COLOR:
					repaint(null);
					break;
				case ENABLE_VIEWS:
					if (show.contains(Show.VIEWS)) repaint(null);
					break;
				case ISOMETRIC:
					gridVisual.setRhombic((Boolean) room.get(PRoom.ISOMETRIC));
					if (show.contains(Show.GRID)) repaint(null);
					break;
				case SNAP_X:
					gridVisual.setWidth(gridFactor * (Integer) room.get(PRoom.SNAP_X));
					if (show.contains(Show.GRID)) repaint(null);
					break;
				case SNAP_Y:
					gridVisual.setHeight(gridFactor * (Integer) room.get(PRoom.SNAP_Y));
					if (show.contains(Show.GRID)) repaint(null);
					break;
				case WIDTH:
				case HEIGHT:
					parent.updateBounds();
					break;
				}
			}
		}

	private class BgDefPropertyListener extends PropertyUpdateListener<PBackgroundDef>
		{
		@Override
		public void updated(PropertyUpdateEvent<PBackgroundDef> e)
			{
			boolean bg = show.contains(Show.BACKGROUNDS);
			boolean fg = show.contains(Show.FOREGROUNDS);
			if (!bg && !fg) return;
			switch (e.key)
				{
				case FOREGROUND:
					if (!(Boolean) e.map.get(PBackgroundDef.VISIBLE)) return;
				case VISIBLE:
					repaint(null);
				case H_SPEED:
				case V_SPEED:
					return;
				}
			if (e.map.get(PBackgroundDef.VISIBLE))
				if ((bg && fg) || (e.map.get(PBackgroundDef.FOREGROUND) ? fg : bg)) repaint(null);
			}
		}

	public void updated(UpdateEvent e)
		{
		if (e.source.owner instanceof BackgroundDef)
			{
			boolean bg = show.contains(Show.BACKGROUNDS);
			boolean fg = show.contains(Show.FOREGROUNDS);
			if (!bg && !fg) return;
			BackgroundDef bd = (BackgroundDef) e.source.owner;
			if (bd.properties.get(PBackgroundDef.VISIBLE))
				if ((bg && fg) || (bd.properties.get(PBackgroundDef.FOREGROUND) ? fg : bg)) repaint(null);
			}
		}
	}
