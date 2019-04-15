/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import static org.lateralgm.main.Util.negDiv;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.resources.sub.PathPoint.PPathPoint;
import org.lateralgm.ui.swing.visuals.BinVisual;
import org.lateralgm.ui.swing.visuals.GridVisual;
import org.lateralgm.ui.swing.visuals.RoomVisual;
import org.lateralgm.ui.swing.visuals.Visual;
import org.lateralgm.ui.swing.visuals.VisualBox;
import org.lateralgm.ui.swing.visuals.RoomVisual.Show;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.ActiveArrayList.ListUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.PropertyMap.PropertyValidator;

public class PathEditor extends VisualPanel implements UpdateListener
	{
	private static final int ROOM_LAYER = -1;
	private static final int BIN_LAYER = 0;
	private static final int GRID_LAYER = 1;
	private static final int POINT_SIZE = 8;
	private static final int POINT_MOUSE_RANGE = 8;
	private static final int ARROW_SIZE = 10;
	private static final int LINE_WIDTH = 5;
	private static final long serialVersionUID = 1L;
	private final Path path;
	private RoomVisual roomVisual;
	private final BinVisual binVisual;
	private final GridVisual gridVisual;
	private final PathPropertyListener ppl = new PathPropertyListener();
	private final PointListListener pll = new PointListListener();

	public final PropertyMap<PPathEditor> properties;

	private final PathEditorPropertyValidator pepv = new PathEditorPropertyValidator();

	public enum PPathEditor
		{
		SHOW_GRID,SELECTED_POINT
		}

	private static final EnumMap<PPathEditor,Object> DEFS = PropertyMap.makeDefaultMap(
			PPathEditor.class,true,null);

	private final ArrayList<PointVisual> pvList;
	private final IdentityHashMap<PathPoint,PointVisual> pvMap;

	private PathArrow arrow;

	public PathEditor(Path p)
		{
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
		properties = new PropertyMap<PPathEditor>(PPathEditor.class,pepv,DEFS);
		binVisual = new BinVisual(container,128,512,512);
		put(BIN_LAYER,binVisual);
		int sx = p.get(PPath.SNAP_X);
		int sy = p.get(PPath.SNAP_Y);
		gridVisual = new GridVisual(false,sx,sy);
		put(GRID_LAYER,gridVisual);
		path = p;
		path.reference.updateSource.addListener(this);
		path.properties.updateSource.addListener(ppl);
		path.points.updateSource.addListener(pll);
		int s = path.points.size();
		pvList = new ArrayList<PointVisual>(s);
		pvMap = new IdentityHashMap<PathPoint,PointVisual>(Math.max((int) (s / .75f) + 1,16));
		updatePointList();
		ResourceReference<Room> r = path.get(PPath.BACKGROUND_ROOM);
		setRoom(r == null ? null : r.get());
		}

	public void updated(UpdateEvent e)
		{
		invalidate();
		repaint();
		}

	private boolean dragging = false;
	private Point dragOffset;
	private PathPoint ppPressed;

	private void mouseEvent(MouseEvent e)
		{
		Point p = e.getPoint().getLocation();
		componentToVisual(p);
		int s = POINT_MOUSE_RANGE - POINT_SIZE / 2 + 1;
		Iterator<Visual> vi = binVisual.intersect(new Rectangle(p.x - s,p.y - s,2 * s,2 * s));
		PathPoint ppOver = null;
		int posd = POINT_MOUSE_RANGE * POINT_MOUSE_RANGE;
		while (vi.hasNext())
			{
			Visual v = vi.next();
			if (v instanceof PointVisual)
				{
				PathPoint pp = ((PointVisual) v).point;
				int xd = pp.getX() - p.x;
				int yd = pp.getY() - p.y;
				int sd = xd * xd + yd * yd;
				if (sd <= posd)
					{
					ppOver = pp;
					posd = sd;
					break;
					}
				}
			}
		switch (e.getID())
			{
			case MouseEvent.MOUSE_PRESSED:
				ppPressed = ppOver;
				switch (e.getButton())
					{
					case MouseEvent.BUTTON1:
						if (ppOver == null)
							{
							PathPoint pp = properties.get(PPathEditor.SELECTED_POINT);
							ppOver = new PathPoint(p.x,p.y,pp == null ? 100 : pp.getSpeed());
							if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0)
								movePathPoint(ppOver,p.x,p.y,true);
							path.points.add(ppOver);
							ppPressed = ppOver;
							}
						else if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0)
							{
							int i = path.points.indexOf(ppOver);
							ppOver = new PathPoint(ppOver.getX(),ppOver.getY(),ppOver.getSpeed());
							path.points.add(i,ppOver);
							ppPressed = ppOver;
							}
						properties.put(PPathEditor.SELECTED_POINT,ppOver);
						dragging = true;
						dragOffset = p.getLocation();
						dragOffset.translate(-ppOver.getX(),-ppOver.getY());
						break;
					}
				break;
			case MouseEvent.MOUSE_DRAGGED:
				if (dragging)
					{
					lockBounds();
					PathPoint pp = properties.get(PPathEditor.SELECTED_POINT);
					if (pp == null)
						{
						ppPressed = null;
						dragging = false;
						unlockBounds();
						break;
						}
					movePathPoint(pp,p.x - dragOffset.x,p.y - dragOffset.y,
							(e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0);
					}
				else if (ppPressed != ppOver) ppPressed = null;
				break;
			case MouseEvent.MOUSE_RELEASED:
				switch (e.getButton())
					{
					case MouseEvent.BUTTON1:
						dragging = false;
						unlockBounds();
						break;
					case MouseEvent.BUTTON3:
						if (dragging)
							{
							dragging = false;
							unlockBounds();
							}
						else if (ppPressed != null) path.points.remove(ppPressed);
						break;
					}
				ppPressed = null;
			}
		}

	private void movePathPoint(PathPoint pp, int x, int y, boolean snap)
		{
		if (snap)
			{
			int sx = path.get(PPath.SNAP_X);
			int sy = path.get(PPath.SNAP_Y);
			pp.setX(negDiv(x + sx / 2,sx) * sx);
			pp.setY(negDiv(y + sy / 2,sy) * sy);
			}
		else
			{
			pp.setX(x);
			pp.setY(y);
			}
		}

	@Override
	protected void processMouseMotionEvent(MouseEvent e)
		{
		mouseEvent(e);
		super.processMouseMotionEvent(e);
		}

	@Override
	protected void processMouseEvent(MouseEvent e)
		{
		mouseEvent(e);
		super.processMouseEvent(e);
		}

	static final int HPS = POINT_SIZE >> 1;
	final BufferedImage[] pointImage = new BufferedImage[2];

	private class PointVisual extends PathVisual
		{
		final PathPoint point;
		final Rectangle bounds = new Rectangle(POINT_SIZE,POINT_SIZE);
		final PointPositionListener ppl = new PointPositionListener();
		private boolean selected;

		public PointVisual(PathPoint p)
			{
			point = p;
			binVisual.setDepth(this,0);
			p.properties.getUpdateSource(PPathPoint.X).addListener(ppl);
			p.properties.getUpdateSource(PPathPoint.Y).addListener(ppl);
			validate();
			}

		public void setSelected(boolean s)
			{
			selected = s;
			binVisual.setDepth(this,s ? -2 : 0);
			}

		protected void calculateBounds()
			{
			bounds.setLocation(point.getX() - HPS,point.getY() - HPS);
			}

		protected void validate()
			{
			calculateBounds();
			setBounds(bounds);
			}

		public void paint(Graphics g)
			{
			int i = selected ? 1 : 0;
			if (pointImage[i] == null)
				{
				pointImage[i] = getGraphicsConfiguration().createCompatibleImage(POINT_SIZE,POINT_SIZE,
						Transparency.BITMASK);
				Graphics2D g2 = pointImage[i].createGraphics();
				g2.setColor(selected ? Color.RED : Color.BLUE);
				g2.fillOval(0,0,POINT_SIZE - 1,POINT_SIZE - 1);
				g2.setColor(Color.BLACK);
				g2.drawOval(0,0,POINT_SIZE - 1,POINT_SIZE - 1);
				}
			g.drawImage(pointImage[i],0,0,null);
			}

		private class PointPositionListener extends PropertyUpdateListener<PPathPoint>
			{
			@Override
			public void updated(PropertyUpdateEvent<PPathPoint> e)
				{
				invalidate();
				}
			}
		}

	static final int HLW = LINE_WIDTH + 1 >> 1;

	static final Stroke STROKE_OUTER = new BasicStroke(LINE_WIDTH,BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	static final Stroke STROKE_INNER = new BasicStroke(LINE_WIDTH - 2.83f,BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);

	private int spPrecision;
	private float[] spBlending;

	private float getBlending(int p, int t)
		{
		if (p != spPrecision)
			{
			spPrecision = p;
			spBlending = new float[1 + 3 * p];
			float isp = 1f / (p * p);
			float ip = 1f / p;
			for (int i = 0; i < p; i++)
				spBlending[i] = i * i * isp * 0.5f;
			for (int i = p; i < 2 * p; i++)
				spBlending[i] = (-2 * i * i * isp + 6 * i * ip - 3) * 0.5f;
			for (int i = 2 * p; i <= 3 * p; i++)
				spBlending[i] = (i * i * isp - 6 * i * ip + 9) * 0.5f;
			}
		return spBlending[t];
		}

	private class LinearPathSegment extends PathVisual
		{
		final PathPoint[] pp = new PathPoint[2];
		final Rectangle bounds = new Rectangle();
		final PointPositionListener ppl = new PointPositionListener();
		int px0, py0, px1, py1;

		public LinearPathSegment(PathPoint...p)
			{
			if (p.length != 2) throw new IllegalArgumentException();
			System.arraycopy(p,0,pp,0,2);
			binVisual.setDepth(this,1);
			for (PathPoint point : p)
				{
				point.properties.getUpdateSource(PPathPoint.X).addListener(ppl);
				point.properties.getUpdateSource(PPathPoint.Y).addListener(ppl);
				}
			validate();
			}

		protected void calculateBounds()
			{
			px0 = pp[0].getX();
			py0 = pp[0].getY();
			px1 = pp[1].getX();
			py1 = pp[1].getY();
			bounds.setBounds(0,0,-1,-1);
			bounds.add(px0,py0);
			bounds.add(px1,py1);
			bounds.grow(HLW,HLW);
			px0 -= bounds.x;
			py0 -= bounds.y;
			px1 -= bounds.x;
			py1 -= bounds.y;
			}

		@Override
		protected void validate()
			{
			calculateBounds();
			setBounds(bounds);
			}

		public void paint(Graphics g)
			{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.BLACK);
			g2.setStroke(STROKE_OUTER);
			g2.drawLine(px0,py0,px1,py1);
			g2.setColor(Color.WHITE);
			g2.setStroke(STROKE_INNER);
			g2.drawLine(px0,py0,px1,py1);
			}

		private class PointPositionListener extends PropertyUpdateListener<PPathPoint>
			{
			@Override
			public void updated(PropertyUpdateEvent<PPathPoint> e)
				{
				invalidate();
				}
			}
		}

	private class SmoothPathSegment extends PathVisual
		{
		final PathPoint[] pp = new PathPoint[4];
		final Rectangle bounds = new Rectangle();
		final PointPositionListener ppl = new PointPositionListener();
		final InnerSegment innerSegment = new InnerSegment();
		int[] px, py;

		public SmoothPathSegment(PathPoint...p)
			{
			if (p.length != 4) throw new IllegalArgumentException();
			System.arraycopy(p,0,pp,0,4);
			binVisual.setDepth(this,2);
			for (PathPoint point : p)
				{
				if (point == null) continue;
				point.properties.getUpdateSource(PPathPoint.X).addListener(ppl);
				point.properties.getUpdateSource(PPathPoint.Y).addListener(ppl);
				}
			validate();
			}

		@Override
		public void remove()
			{
			innerSegment.remove();
			super.remove();
			}

		private void pBlend(int i, int n, int t, int[] x, int[] y)
			{
			float w0 = getBlending(n,t + 1 + 2 * n);
			float w2 = getBlending(n,t + 1);
			px[i] = x[1] + Math.round(w0 * (x[0] - x[1]) + w2 * (x[2] - x[1]));
			py[i] = y[1] + Math.round(w0 * (y[0] - y[1]) + w2 * (y[2] - y[1]));
			}

		protected void calculateBounds()
			{
			bounds.setBounds(0,0,-1,-1);
			int[] x = new int[4];
			int[] y = new int[4];
			for (int i = 0; i < 4; i++)
				{
				if (pp[i] == null) continue;
				x[i] = pp[i].getX();
				y[i] = pp[i].getY();
				}
			int p = path.properties.get(PPath.PRECISION);
			int n = (1 << p);
			if (pp[0] == null)
				{
				px = new int[2];
				py = new int[2];
				px[0] = x[1];
				py[0] = y[1];
				bounds.add(x[1],y[1]);
				pBlend(1,n,0,Arrays.copyOfRange(x,1,4),Arrays.copyOfRange(y,1,4));
				bounds.add(px[1],py[1]);
				}
			else
				{
				px = new int[n];
				py = new int[n];
				for (int t = 0; t < n - 1; t++)
					{
					pBlend(t,n,t,x,y);
					bounds.add(px[t],py[t]);
					}
				if (pp[3] == null)
					{
					px[n - 1] = x[2];
					py[n - 1] = y[2];
					}
				else
					pBlend(n - 1,n,0,Arrays.copyOfRange(x,1,4),Arrays.copyOfRange(y,1,4));
				bounds.add(px[n - 1],py[n - 1]);
				}
			bounds.grow(HLW,HLW);
			for (int t = 0; t < px.length; t++)
				{
				px[t] -= bounds.x;
				py[t] -= bounds.y;
				}
			}

		@Override
		protected void validate()
			{
			calculateBounds();
			setBounds(bounds);
			innerSegment.setBounds(bounds);
			}

		public void paint(Graphics g)
			{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.BLACK);
			g2.setStroke(STROKE_OUTER);
			g2.drawPolyline(px,py,px.length);
			}

		private class InnerSegment extends VisualBox
			{
			public InnerSegment()
				{
				super(binVisual);
				binVisual.setDepth(this,1);
				}

			@Override
			protected void setBounds(Rectangle b)
				{
				super.setBounds(b);
				}

			public void paint(Graphics g)
				{
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.setStroke(STROKE_INNER);
				g2.drawPolyline(px,py,px.length);
				}
			}

		private class PointPositionListener extends PropertyUpdateListener<PPathPoint>
			{
			@Override
			public void updated(PropertyUpdateEvent<PPathPoint> e)
				{
				invalidate();
				}
			}
		}

	private static final double ARROW_ANGLE = 2 * Math.PI / 3;

	private class PathArrow extends PathVisual
		{
		final PointPositionListener ppl = new PointPositionListener();
		final SmoothPathSegment segment;
		final int[] px = new int[4];
		final int[] py = new int[4];

		public PathArrow(SmoothPathSegment s)
			{
			segment = s;
			binVisual.setDepth(this,-1);
			int i2 = s == null ? 2 : path.get(PPath.CLOSED) ? 4 : 3;
			for (int i = 0; i < i2; i++)
				{
				PathPoint p = path.points.get(i == 3 ? path.points.size() - 1 : i);
				p.properties.getUpdateSource(PPathPoint.X).addListener(ppl);
				p.properties.getUpdateSource(PPathPoint.Y).addListener(ppl);
				}
			validate();
			}

		private void calculatePoints(int x, int y, double d)
			{
			px[0] = x + (int) Math.round(ARROW_SIZE * Math.cos(d));
			py[0] = y - (int) Math.round(ARROW_SIZE * Math.sin(d));
			px[1] = x + (int) Math.round(ARROW_SIZE * Math.cos(d + ARROW_ANGLE));
			py[1] = y - (int) Math.round(ARROW_SIZE * Math.sin(d + ARROW_ANGLE));
			px[2] = x;
			py[2] = y;
			px[3] = x + (int) Math.round(ARROW_SIZE * Math.cos(d - ARROW_ANGLE));
			py[3] = y - (int) Math.round(ARROW_SIZE * Math.sin(d - ARROW_ANGLE));
			}

		private int sqrdist(int x, int y)
			{
			return x * x + y * y;
			}

		@Override
		protected void validate()
			{
			if (segment == null)
				{
				PathPoint p = path.points.get(0);
				PathPoint p2 = path.points.get(1);
				int x = p.getX();
				int y = p.getY();
				calculatePoints(x,y,Math.atan2(y - p2.getY(),p2.getX() - x));
				}
			else
				{
				segment.validate();
				if (path.get(PPath.CLOSED))
					{
					int i = segment.px.length - 1;
					int x = segment.px[i];
					int y = segment.py[i];
					int i2 = i - 1;
					while (i2 > 0 && sqrdist(segment.px[i2] - x,segment.py[i2] - y) < 4)
						i2--;
					calculatePoints(x + segment.bounds.x,y + segment.bounds.y,
							Math.atan2(segment.py[i2] - y,x - segment.px[i2]));
					}
				else
					{
					int x = segment.px[0];
					int y = segment.py[0];
					int i = 1;
					while (i < segment.px.length - 1 && sqrdist(segment.px[i] - x,segment.py[i] - y) < 4)
						i++;
					calculatePoints(x + segment.bounds.x,y + segment.bounds.y,
							Math.atan2(y - segment.py[i],segment.px[i] - x));
					}
				}
			Rectangle bounds = new Rectangle(-1,-1);
			for (int i = 0; i < 4; i++)
				bounds.add(px[i],py[i]);
			for (int i = 0; i < 4; i++)
				{
				px[i] -= bounds.x;
				py[i] -= bounds.y;
				}
			setBounds(bounds);
			}

		public void paint(Graphics g)
			{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(0,224,0));
			g2.fillPolygon(px,py,4);
			g2.setColor(Color.BLACK);
			g2.drawPolygon(px,py,4);
			}

		private class PointPositionListener extends PropertyUpdateListener<PPathPoint>
			{
			@Override
			public void updated(PropertyUpdateEvent<PPathPoint> e)
				{
				invalidate();
				}
			}
		}

	private abstract class PathVisual extends VisualBox
		{
		private boolean invalid;

		public PathVisual()
			{
			super(binVisual);
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
		}

	final IdentityHashMap<PathPoint,SmoothPathSegment> spsMap =
			new IdentityHashMap<PathPoint,SmoothPathSegment>();
	final IdentityHashMap<PathPoint,LinearPathSegment> lpsMap =
			new IdentityHashMap<PathPoint,LinearPathSegment>();

	@SuppressWarnings("unchecked")
	private void updatePointList()
		{
		for (SmoothPathSegment sps : spsMap.values())
			sps.remove();
		spsMap.clear();
		for (LinearPathSegment lps : lpsMap.values())
			lps.remove();
		lpsMap.clear();
		if (arrow != null) arrow.remove();
		Set<PathPoint> pps = ((IdentityHashMap<PathPoint,PointVisual>) pvMap.clone()).keySet();
		int s = pvList.size();
		ActiveArrayList<PathPoint> pp = path.points;
		int s2 = pp.size();
		while (s > s2)
			pvList.remove(--s);
		pvList.ensureCapacity(s2);
		for (int i = 0; i < s2; i++)
			{
			PathPoint p = pp.get(i);
			PointVisual v = pvMap.get(p);
			if (v == null)
				{
				v = new PointVisual(p);
				pvMap.put(p,v);
				}
			else
				{
				pps.remove(p);
				}
			if (i >= s)
				pvList.add(v);
			else
				pvList.set(i,v);
			}
		if (pps.contains(properties.get(PPathEditor.SELECTED_POINT)))
			properties.put(PPathEditor.SELECTED_POINT,null);
		for (PathPoint pathPoint : pps)
			pvMap.remove(pathPoint).remove();
		if (path.get(PPath.SMOOTH))
			{
			boolean closed = path.properties.get(PPath.CLOSED);
			if (s2 >= 3)
				{
				PathPoint[] rpp = new PathPoint[4];
				for (int i = 0; i < 4; i++)
					rpp[i] = pp.get(i % s2);
				int i2 = closed ? s2 + 1 : s2 - 2;
				for (int i = 1; i < i2; i++)
					{
					PathPoint p = rpp[1];
					spsMap.put(p,new SmoothPathSegment(rpp));
					rpp = Arrays.copyOfRange(rpp,1,5);
					rpp[3] = pp.get((i + 3) % s2);
					}
				if (!closed)
					{
					PathPoint p = pp.get(0);
					spsMap.put(p,new SmoothPathSegment(null,p,pp.get(1),pp.get(2)));
					p = pp.get(s2 - 2);
					spsMap.put(p,new SmoothPathSegment(pp.get(s2 - 3),p,pp.get(s2 - 1),null));
					}
				arrow = new PathArrow(spsMap.get(pp.get(0)));
				}
			}
		else
			{
			if (s2 >= 2)
				{
				for (int i = 0; i < s2 - 1; i++)
					{
					PathPoint p = path.points.get(i);
					lpsMap.put(p,new LinearPathSegment(p,path.points.get(i + 1)));
					}
				if (path.properties.get(PPath.CLOSED))
					{
					PathPoint p = path.points.get(s2 - 1);
					lpsMap.put(p,new LinearPathSegment(p,path.points.get(0)));
					}
				arrow = new PathArrow(null);
				}
			}
		}

	private static final EnumSet<Show> ROOM_SHOW = EnumSet.of(Show.BACKGROUNDS,Show.INSTANCES,
			Show.TILES,Show.FOREGROUNDS);

	private void setRoom(Room r)
		{
		if (roomVisual == null ? r == null : roomVisual.room == r) return;
		roomVisual = r == null ? null : new RoomVisual(container,r,ROOM_SHOW);
		put(ROOM_LAYER,roomVisual);
		}

	private class PathPropertyListener extends PropertyUpdateListener<PPath>
		{
		@Override
		public void updated(PropertyUpdateEvent<PPath> e)
			{
			switch (e.key)
				{
				case SNAP_X:
					gridVisual.setWidth((Integer) path.get(PPath.SNAP_X));
					repaint();
					break;
				case SNAP_Y:
					gridVisual.setHeight((Integer) path.get(PPath.SNAP_Y));
					repaint();
					break;
				case PRECISION:
					for (SmoothPathSegment s : spsMap.values())
						s.validate();
					arrow.validate();
					break;
				case CLOSED:
				case SMOOTH:
					// TODO: Optimize
					updatePointList();
					break;
				case BACKGROUND_ROOM:
					ResourceReference<Room> r = path.get(PPath.BACKGROUND_ROOM);
					setRoom(r == null ? null : r.get());
				}
			}
		}

	private class PointListListener implements UpdateListener
		{
		public void updated(UpdateEvent e)
			{
			ListUpdateEvent lue = (ListUpdateEvent) e;
			switch (lue.type)
				{
				case ADDED:
					//					for (int i = lue.fromIndex; i <= lue.toIndex; i++)
					//						{
					//						PathPoint p = path.points.get(i);
					//						PointVisual v = new PointVisual(p);
					//						pvMap.put(p,v);
					//						pvList.add(i,v);
					//						}
					//					break;
				case REMOVED:
					//					for (int i = lue.toIndex; i <= lue.fromIndex; i++)
					//						{
					//						PathPoint p = path.points.get(i);
					//						pvMap.remove(p);
					//						pvList.remove(i).box.remove();
					//						}
					//					break;
				case CHANGED:
					updatePointList();
				}
			}
		}

	private class PathEditorPropertyValidator implements PropertyValidator<PPathEditor>
		{
		public Object validate(PPathEditor k, Object v)
			{
			switch (k)
				{
				case SELECTED_POINT:
					PointVisual pv = pvMap.get(properties.get(k));
					if (pv != null)
						{
						if (v == pv.point) break;
						pv.setSelected(false);
						}
					pv = pvMap.get(v);
					if (pv == null)
						{
						if (pvList.size() < 1) return null;
						pv = pvList.get(0);
						}
					pv.setSelected(true);
					return pv.point;
				case SHOW_GRID:
					if (v instanceof Boolean)
						put(GRID_LAYER,(Boolean) v ? gridVisual : null);
					else
						return properties.get(k);
					break;
				}
			return v;
			}
		}
	}
