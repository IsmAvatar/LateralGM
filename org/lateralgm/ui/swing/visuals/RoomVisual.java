/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014, egofree
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.ActiveArrayList.ListUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class RoomVisual extends AbstractVisual implements BoundedVisual,UpdateListener
	{
	protected static final ImageIcon EMPTY_SPRITE = LGM.getIconForKey("Resource.EMPTY_OBJ"); //$NON-NLS-1$
	protected static final BufferedImage EMPTY_IMAGE = EMPTY_SPRITE.getIconWidth() <= 0 ? null
			: new BufferedImage(EMPTY_SPRITE.getIconWidth(),EMPTY_SPRITE.getIconHeight(),
					BufferedImage.TYPE_INT_ARGB);

	private final BinVisual binVisual;
	private final GridVisual gridVisual;
	public final Room room;

	// These variables are here to keep the managers from being GC'd.
	protected final InstanceVisualListManager ivlm;
	protected final TileVisualListManager tvlm;

	private final RoomPropertyListener rpl = new RoomPropertyListener();
	private final BgDefPropertyListener bdpl = new BgDefPropertyListener();
	private final ViewPropertyListener viewPropertyListener = new ViewPropertyListener();

	// Contains the region selected by the user
	private Rectangle selection = null;
	// The position of the mouse cursor
	private Point mousePosition = null;
	// Image of the region made by the user
	private BufferedImage selectionImage = null;
	// Show if the user has pasted a region
	private boolean pasteMode = false;

	private EnumSet<Show> show;
	private int gridFactor = 1;
	private int gridX, gridY;

	private boolean viewsVisible;
	private Integer visibleLayer = null;

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

		// Set the property listener for each background
		for (BackgroundDef bd : room.backgroundDefs)
			{
			bd.properties.updateSource.addListener(bdpl);
			bd.updateSource.addListener(this);
			}

		// Set the property listener for each view
		for (View view : room.views)
			view.properties.updateSource.addListener(viewPropertyListener);

		}

	public int getSelectionImageWidth()
		{
		return selectionImage.getWidth();
		}

	public int getSelectionImageHeight()
		{
		return selectionImage.getHeight();
		}

	// Deactivate the paste mode
	public void deactivatePasteMode()
		{
		pasteMode = false;
		repaint(null);
		}

	// Activate the paste mode
	public void activatePasteMode()
		{
		pasteMode = true;
		repaint(null);
		}

	// Make an image of the region selected by the user
	public void setSelectionImage(List<Instance> selectedInstances, List<Tile> selectedTiles)
		{
		// Create an empty image
		BufferedImage selectionImage = new BufferedImage(selection.width,selection.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = selectionImage.getGraphics();
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setColor(Util.convertGmColorWithAlpha(Prefs.multipleSelectionInsideColor));

		// If the option 'Fill rectangle' is set
		if (Prefs.useFilledRectangleForMultipleSelection)
			g2.fillRect(1,1,selection.width - 2,selection.height - 2);
		else
			g2.drawRect(1,1,selection.width - 3,selection.height - 3);

		g.setColor(Util.convertGmColorWithAlpha(Prefs.multipleSelectionOutsideColor));

		// Draw the outside border
		if (Prefs.useFilledRectangleForMultipleSelection)
			g2.drawRect(0,0,selection.width - 1,selection.height - 1);
		else
			g2.drawRect(0,0,selection.width - 1,selection.height - 1);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);
		g2.setComposite(ac);

		// If the user selected instances
		if (selectedInstances != null)
			{
			// Get each selected instance and draw it on the buffer image
			for (Instance instance : selectedInstances)
				{
				Graphics2D g3 = (Graphics2D) g2.create();

				// Get instance's properties
				Point2D scale = instance.getScale();
				int alpha = instance.getAlpha();
				double rotation = instance.getRotation();
				Point position = instance.getPosition();
				// Sprite's origin
				int originx = 0, originy = 0;
				// Used to modify the position when scaling
				int offsetx = 0, offsety = 0;
				// Get the relative position of the instance in the selection
				Point newPosition = new Point(position.x - selection.x,position.y - selection.y);

				// Get the instance's image
				ResourceReference<GmObject> instanceObject = instance.properties.get(PInstance.OBJECT);
				BufferedImage instanceImage = instanceObject.get().getDisplayImage();

				// If there is no image, draw a sphere
				if (instanceImage == null || alpha == 0)
					{
					g3.drawImage(EMPTY_SPRITE.getImage(),newPosition.x,newPosition.y,null);
					g3.dispose();
					continue;
					}

				// Get sprite's origin
				ResourceReference<Sprite> sprite = instanceObject.get().get(PGmObject.SPRITE);
				originx = (Integer) sprite.get().get(PSprite.ORIGIN_X);
				originy = (Integer) sprite.get().get(PSprite.ORIGIN_Y);

				if (originx != 0 || originy != 0)
					newPosition.translate(-(int) (originx * scale.getX()),-(int) (originy * scale.getY()));

				// Ensure that the position stays the same when there is a scaling
				if (scale.getX() != 1.0 || scale.getY() != 1.0)
					{
					offsetx = (int) (newPosition.x * scale.getX() - newPosition.x);
					offsety = (int) (newPosition.y * scale.getY() - newPosition.y);
					}

				// Apply scaling, rotation and translation
				if (offsetx != 0 || offsety != 0) g3.translate(-offsetx,-offsety);
				if (rotation != 0)
					g3.rotate(Math.toRadians(-rotation),newPosition.x + offsetx,newPosition.y + offsety);
				g3.scale(scale.getX(),scale.getY());

				Image newImage;
				Color selectedColor = instance.getAWTColor();

				// If a color has been selected, apply color blending
				if (!Color.WHITE.equals(selectedColor))
					{
					ImageFilter filter = new ColorFilter(selectedColor);
					FilteredImageSource filteredSrc = new FilteredImageSource(instanceImage.getSource(),
							filter);
					newImage = Toolkit.getDefaultToolkit().createImage(filteredSrc);
					}
				else
					{
					newImage = instanceImage;
					}

				// If instance's alpha value is lower than the default one, apply alpha
				if (alpha > 0 && alpha < ac.getAlpha() * 255)
					{
					AlphaComposite newAc = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
							(float) (alpha / 255.0));
					g3.setComposite(newAc);
					}

				g3.drawImage(newImage,newPosition.x,newPosition.y,null);
				g3.dispose();
				}
			}
		else
			{
			// Get each selected tile and draw it on the buffer image
			for (Tile tile : selectedTiles)
				{
				Point newPosition = tile.getPosition();
				// Get tile's background
				ResourceReference<Background> background = tile.properties.get(PTile.BACKGROUND);
				BufferedImage backgroundImage = background.get().getDisplayImage();
				Point tilePosition = tile.getBackgroundPosition();
				Dimension tileSize = tile.getSize();
				// Get tile's image
				BufferedImage tileImage = backgroundImage.getSubimage(tilePosition.x,tilePosition.y,
						tileSize.width,tileSize.height);

				g2.drawImage(tileImage,newPosition.x - selection.x,newPosition.y - selection.y,null);
				}
			}

		this.selectionImage = selectionImage;
		}

	// Update the mouse position. Needed for displaying the selected region
	public void setMousePosition(Point mousePosition)
		{
		this.mousePosition = mousePosition;
		repaint(null);
		}

	// set the region selected by the user
	public void setSelection(Rectangle selection)
		{
		this.selection = selection;
		repaint(null);
		}

	// Set if the views should visible or not (used when the 'views' tab is selected)
	public void setViewsVisible(boolean visible)
		{
		viewsVisible = visible;
		repaint(null);
		}

	// Set the visible layer property
	public void setVisibleLayer(Integer layer)
		{
		visibleLayer = layer;
		repaint(null);
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

		// If 'Show views' option has been set or if the 'Views' tab is selected
		if (show.contains(Show.VIEWS) || viewsVisible)
			{
			boolean viewsEnabled = room.get(PRoom.VIEWS_ENABLED);

			// Display the view when the views are enabled
			if (viewsEnabled) for (View view : room.views)
				if (view.properties.get(PView.VISIBLE)) paintView(g2,view);
			}

		// If the user is moving a selected region, display it
		if (pasteMode) g2.drawImage(selectionImage,mousePosition.x,mousePosition.y,null);

		// If there is a selection, display it
		if (selection != null) paintSelection(g2);

		g2.dispose();
		}

	// Display the selection made by the user
	private void paintSelection(Graphics g)
		{
		// If the option 'Invert colors' is set
		if (Prefs.useInvertedColorForMultipleSelection)
			g.setXORMode(Util.convertGmColorWithAlpha(Prefs.multipleSelectionInsideColor));
		else
			g.setColor(Util.convertGmColorWithAlpha(Prefs.multipleSelectionInsideColor));

		// If the option 'Fill rectangle' is set
		if (Prefs.useFilledRectangleForMultipleSelection)
			g.fillRect(selection.x + 1,selection.y + 1,selection.width - 1,selection.height - 1);
		else
			g.drawRect(selection.x + 1,selection.y + 1,selection.width - 2,selection.height - 2);

		// If the option 'Invert colors' is set
		if (Prefs.useInvertedColorForMultipleSelection)
			g.setXORMode(Util.convertGmColorWithAlpha(Prefs.multipleSelectionOutsideColor));
		else
			g.setColor(Util.convertGmColorWithAlpha(Prefs.multipleSelectionOutsideColor));

		// Draw the outside border
		g.drawRect(selection.x,selection.y,selection.width,selection.height);
		}

	// Display a view on the panel
	private void paintView(Graphics g, View view)
		{
		Graphics2D g2 = (Graphics2D) g;

		// View location
		int x;
		int y;

		int objectFollowingX = view.properties.get(PView.OBJECT_FOLLOWING_X);
		int objectFollowingY = view.properties.get(PView.OBJECT_FOLLOWING_Y);

		// Get the view dimension
		int width = view.properties.get(PView.VIEW_W);
		int height = view.properties.get(PView.VIEW_H);

		// If the view is following an object, center the view around the object
		if (objectFollowingX > -1)
			{
			x = objectFollowingX;
			y = objectFollowingY;
			}
		else
			{
			// Use the 'normal' view location
			x = view.properties.get(PView.VIEW_X);
			y = view.properties.get(PView.VIEW_Y);
			}

		// If the option 'invert colors' is set
		if (Prefs.useInvertedColorForViews)
			g2.setXORMode(Util.convertGmColorWithAlpha(Prefs.viewOutsideColor));
		else
			g2.setColor(Util.convertGmColorWithAlpha(Prefs.viewOutsideColor));

		// Draw the 'outside' rectangle
		if (Prefs.useFilledRectangleForViews)
			{
			g2.drawRect(x - 2,y - 2,width + 3,height + 3);
			g2.drawRect(x - 1,y - 1,width + 1,height + 1);
			}
		else
			{
			g2.drawRect(x,y,width,height);
			g2.drawRect(x + 2,y + 2,width - 4,height - 4);
			}

		// If the option 'invert colors' is set
		if (Prefs.useInvertedColorForViews)
			g2.setXORMode(Util.convertGmColorWithAlpha(Prefs.viewInsideColor));
		else
			g2.setColor(Util.convertGmColorWithAlpha(Prefs.viewInsideColor));

		// Draw the 'inside' rectangle
		if (Prefs.useFilledRectangleForViews)
			g2.fillRect(x,y,width,height);
		else
			g2.drawRect(x + 1,y + 1,width - 2,height - 2);

		// If the view is following an object
		if (objectFollowingX > -1)
			{
			// Get the border zone properties
			int borderH = view.properties.get(PView.BORDER_H);
			int borderV = view.properties.get(PView.BORDER_V);

			// If the border zone is not empty
			if (!(borderH == 0 & borderV == 0))
				{
				if (Prefs.useFilledRectangleForViews)
					{
					// Define the stroke for the border zone
					float dash[] = { 10.0f };
					BasicStroke dashed = new BasicStroke(2.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,
							10.0f,dash,0.0f);

					// Draw the border zone
					g2.setColor(Util.convertGmColorWithAlpha(Prefs.viewOutsideColor));
					g2.setStroke(dashed);
					g2.drawRect(x + borderH,y + borderV,width - borderH * 2,height - borderV * 2);
					}
				else
					{
					// Define the strokes for the border zone
					float outside[] = { 10.0f };
					float inside[] = { 8.0f,12.0f };
					BasicStroke dashed_black = new BasicStroke(3.0f,BasicStroke.CAP_BUTT,
							BasicStroke.JOIN_MITER,10.0f,outside,0.0f);
					BasicStroke dashed_white = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,
							BasicStroke.JOIN_MITER,10.0f,inside,19.0f);

					// Draw the border zone
					g2.setColor(Util.convertGmColorWithAlpha(Prefs.viewOutsideColor));
					g2.setStroke(dashed_black);
					g2.drawRect(x + borderH,y + borderV,width - borderH * 2,height - borderV * 2);

					g2.setColor(Util.convertGmColorWithAlpha(Prefs.viewInsideColor));
					g2.setStroke(dashed_white);
					g2.drawRect(x + borderH,y + borderV,width - borderH * 2,height - borderV * 2);
					}

				g2.setStroke(new BasicStroke());
				}

			}

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
		return new PieceIterator<P>(binVisual.intersect(r,getVisualClass(p)));
		}

	public <P extends Piece>Iterator<P> intersect(Rectangle r, Class<P> p, int depth)
		{
		return new PieceIterator<P>(binVisual.intersect(r,getVisualClass(p),depth));
		}

	private static class PieceIterator<P extends Piece> implements Iterator<P>
		{
		private Iterator<PieceVisual<P>> vi;

		public PieceIterator(Iterator<PieceVisual<P>> vi)
			{
			this.vi = vi;
			}

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
		}

	public boolean intersects(Rectangle r, Piece p)
		{
		Iterator<Piece> pi = intersect(r);
		while (pi.hasNext())
			if (pi.next() == p) return true;
		return false;
		}

	@SuppressWarnings("unchecked")
	private static <P extends Piece, V extends PieceVisual<P>>Class<V> getVisualClass(Class<P> p)
		{
		if (p == Piece.class) return (Class<V>) (Class) PieceVisual.class;
		if (p == Instance.class) return (Class<V>) (Class) InstanceVisual.class;
		if (p == Tile.class) return (Class<V>) (Class) TileVisual.class;
		throw new IllegalArgumentException();
		}

	public Iterator<Tile> intersectTiles(Rectangle r, int depth)
		{
		return intersect(r,Tile.class,depth);
		}

	public Iterator<Instance> intersectInstances(Rectangle r)
		{
		return intersect(r,Instance.class);
		}

	public Iterator<Piece> intersect(Rectangle r)
		{
		return intersect(r,Piece.class);
		}

	private static void paintBackground(Graphics g, BackgroundDef bd, int width, int height)
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

	// Apply a color filter to an image
	class ColorFilter extends RGBImageFilter
		{
		// The RGB components of the new color
		byte newColorRed;
		byte newColorGreen;
		byte newColorBlue;

		public ColorFilter(Color color)
			{
			newColorRed = (byte) color.getRed();
			newColorGreen = (byte) color.getGreen();
			newColorBlue = (byte) color.getBlue();
			}

		@Override
		public int filterRGB(int x, int y, int rgb)
			{
			int alpha = (rgb >> 24) & 0xff;
			int red = (rgb >> 16) & 0xff;
			int green = (rgb >> 8) & 0xff;
			int blue = (rgb) & 0xff;

			// Filter with the new color
			red = red & newColorRed;
			green = green & newColorGreen;
			blue = blue & newColorBlue;

			// Set the pixel with the new color
			return (alpha << 24) | (red << 16) | (green << 8) | blue;

			}
		}

	private class InstanceVisual extends PieceVisual<Instance>
		{
		private BufferedImage image;
		private final InstancePropertyListener ipl = new InstancePropertyListener();

		// When rotating an instance, used to set the new position
		private int offsetx = 0, offsety = 0;
		// Sprite's origin. Used for rotation
		private int originx = 0, originy = 0;

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

			// Get sprite's origin
			if (s != null)
				{
				originx = (Integer) s.get(PSprite.ORIGIN_X);
				originy = (Integer) s.get(PSprite.ORIGIN_Y);
				}
			else
				{
				originx = 0;
				originy = 0;
				}

			Point2D scale = piece.getScale();

			Point position = piece.getPosition();
			if (s != null)
				position.translate(-(int) (originx * scale.getX()),-(int) (originy * scale.getY()));

			// Get instance's properties
			double angle = piece.getRotation();
			int newWidth = image.getWidth();
			int newHeight = image.getHeight();

			int borderOffsetx = 0;
			int borderOffsety = 0;

			// If the instance is selected use bigger bounds for border, and make sure the instance is visible
			if (piece.isSelected())
				{
				binVisual.setDepth(this,o == null ? 0 : Integer.MIN_VALUE,true);
				newWidth += 4;
				newHeight += 4;
				borderOffsetx = (int) (2 * scale.getX());
				borderOffsety = (int) (2 * scale.getY());
				}
			else
				{
				binVisual.setDepth(this,o == null ? 0 : (Integer) o.get(PGmObject.DEPTH),false);
				}

			// Apply scaling
			if (scale.getX() != 1.0 || scale.getY() != 1.0)
				{
				newWidth *= scale.getX();
				newHeight *= scale.getY();
				}

			// Calculate the new bounds when there is a rotation
			if (angle != 0)
				{
				AffineTransform at = new AffineTransform();
				// Create a rectangle with image's size
				Rectangle myRect = new Rectangle(position.x,position.y,newWidth,newHeight);
				// Apply the rotation
				at = AffineTransform.getRotateInstance(Math.toRadians(-angle),
						position.x + originx * scale.getX(),position.y + originy * scale.getY());
				Shape rotatedRect = at.createTransformedShape(myRect);

				// Use a rectangle2D and round manually values with Math.round. getBounds doesn't give correct rounded values.
				Rectangle2D newBounds2D = rotatedRect.getBounds2D();

				newWidth = (int) Math.round(newBounds2D.getWidth());
				newHeight = (int) Math.round(newBounds2D.getHeight());

				offsetx = (int) Math.round(newBounds2D.getX()) - position.x;
				offsety = (int) Math.round(newBounds2D.getY()) - position.y;
				}
			else
				{
				offsetx = 0;
				offsety = 0;
				}

			setBounds(new Rectangle(position.x + offsetx - borderOffsetx,position.y + offsety
					- borderOffsety,newWidth,newHeight));
			}

		public void paint(Graphics g)
			{
			if (show.contains(Show.INSTANCES))
				{
				Graphics2D g2 = (Graphics2D) g;

				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);

				boolean rotationOrScaling = false;

				// Get instance's properties
				Point2D scale = piece.getScale();
				double rotation = piece.getRotation();
				int alpha = piece.getAlpha();

				// If there is a rotation or a scaling, the border size is different
				if (rotation != 0 || scale.getX() != 1.0 || scale.getY() != 1.0) rotationOrScaling = true;

				// Apply scaling, rotation and translation
				if (offsetx != 0 || offsety != 0) g2.translate(-offsetx,-offsety);
				if (rotation != 0)
					g2.rotate(Math.toRadians(-rotation),originx * scale.getX(),originy * scale.getY());
				if (scale.getX() != 1.0 || scale.getY() != 1.0) g2.scale(scale.getX(),scale.getY());

				Image newImage;

				Color selectedColor = piece.getAWTColor();

				// If a color has been selected, apply color blending
				if (!Color.WHITE.equals(selectedColor))
					{
					ImageFilter filter = new ColorFilter(selectedColor);
					FilteredImageSource filteredSrc = new FilteredImageSource(image.getSource(),filter);
					newImage = Toolkit.getDefaultToolkit().createImage(filteredSrc);
					}
				else
					{
					newImage = image;
					}

				// Original composite
				Composite oc = null;

				// Apply alpha
				if (alpha > 0 && alpha < 255)
					{
					// Save the original composite
					oc = g2.getComposite();

					AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
							(float) (alpha / 255.0));
					g2.setComposite(ac);
					}

				// Draw the instance
				if (piece.isSelected())
					g2.drawImage((image == EMPTY_IMAGE || alpha == 0) ? EMPTY_SPRITE.getImage() : newImage,2,
							2,null);
				else
					g2.drawImage((image == EMPTY_IMAGE || alpha == 0) ? EMPTY_SPRITE.getImage() : newImage,0,
							0,null);

				// If the instance is selected, display a border around it
				if (piece.isSelected())
					{
					// If there was an alpha filtering, remove it
					if (oc != null) g2.setComposite(oc);

					// If the option 'Invert colors' is set
					if (Prefs.useInvertedColorForSelection)
						g2.setXORMode(Util.convertGmColorWithAlpha(Prefs.selectionInsideColor));
					else
						g2.setColor(Util.convertGmColorWithAlpha(Prefs.selectionInsideColor));

					// If the option 'Fill rectangle' is set
					if (Prefs.useFilledRectangleForSelection)
						{
						g2.fillRect(1,1,image.getWidth() + 2,image.getHeight() + 2);
						}
					else
						{
						if (rotationOrScaling == false)
							g2.drawRect(1,1,image.getWidth() + 1,image.getHeight() + 1);
						else
							g2.drawRect(1,1,image.getWidth() + 2,image.getHeight() + 2);
						}

					// If the option 'Invert colors' is set
					if (Prefs.useInvertedColorForSelection)
						g2.setXORMode(Util.convertGmColorWithAlpha(Prefs.selectionOutsideColor));
					else
						g2.setColor(Util.convertGmColorWithAlpha(Prefs.selectionOutsideColor));

					// Draw the outside border
					if (rotationOrScaling == false)
						g2.drawRect(0,0,image.getWidth() + 3,image.getHeight() + 3);
					else
						g2.drawRect(0,0,image.getWidth() + 4,image.getHeight() + 4);
					}

				}

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
					default:
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

			// If the tile is selected use bigger bounds for border
			if (piece.isSelected())
				{
				binVisual.setDepth(this,piece.getDepth(),true);
				Point piecePosition = piece.getPosition();
				Dimension pieceSize = piece.getSize();
				setBounds(new Rectangle(piecePosition.x - 2,piecePosition.y - 2,pieceSize.width + 4,
						pieceSize.height + 4));
				}
			else
				{
				binVisual.setDepth(this,piece.getDepth(),false);
				Rectangle r = new Rectangle(piece.getPosition(),piece.getSize());
				setBounds(r);
				}

			}

		public void paint(Graphics g)
			{
			if (show.contains(Show.TILES))
				{
				Graphics2D g2 = (Graphics2D) g;

				// If we display only the visible layer, test if the current tile is in the visible layer
				if (visibleLayer != null && piece.getDepth() != visibleLayer) return;

				// If the tile is selected, display a border around it
				if (piece.isSelected())
					{
					g2.drawImage(image,2,2,null);

					// If the option 'Invert colors' is set
					if (Prefs.useInvertedColorForSelection)
						g2.setXORMode(Util.convertGmColorWithAlpha(Prefs.selectionInsideColor));
					else
						g2.setColor(Util.convertGmColorWithAlpha(Prefs.selectionInsideColor));

					// If the option 'Fill rectangle' is set
					if (Prefs.useFilledRectangleForSelection)
						g2.fillRect(1,1,image.getWidth() + 2,image.getHeight() + 2);
					else
						g2.drawRect(1,1,image.getWidth() + 1,image.getHeight() + 1);

					// If the option 'Invert colors' is set
					if (Prefs.useInvertedColorForSelection)
						g2.setXORMode(Util.convertGmColorWithAlpha(Prefs.selectionOutsideColor));
					else
						g2.setColor(Util.convertGmColorWithAlpha(Prefs.selectionOutsideColor));

					// Draw the outside border
					g2.drawRect(0,0,image.getWidth() + 3,image.getHeight() + 3);
					}
				else
					{
					g.drawImage(image,0,0,null);
					}

				}
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
					default:
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

		@Override
		protected InstanceVisual createVisual(Instance t)
			{
			return new InstanceVisual(t);
			}

		@Override
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

		@Override
		protected TileVisual createVisual(Tile t)
			{
			return new TileVisual(t);
			}

		@Override
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
		@Override
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
				case VIEWS_ENABLED:
					if (show.contains(Show.VIEWS) || viewsVisible) repaint(null);
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
				default:
					break;
				}
			}
		}

	// Class which manages the update of view's properties
	private class ViewPropertyListener extends PropertyUpdateListener<PView>
		{
		@Override
		public void updated(PropertyUpdateEvent<PView> e)
			{
			// Update the display of the view only when updating the position or the size of the view
			switch (e.key)
				{
				case VISIBLE:
				case VIEW_X:
				case VIEW_Y:
				case VIEW_W:
				case VIEW_H:
				case BORDER_H:
				case BORDER_V:
					repaint(null);
				default:
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
				default:
					break;
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
