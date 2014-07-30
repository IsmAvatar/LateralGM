/*
 * Copyright (C) 2007, 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014, egofree
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import static org.lateralgm.main.Util.deRef;
import static org.lateralgm.main.Util.gcd;
import static org.lateralgm.main.Util.negDiv;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.resources.sub.View;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.subframes.CodeFrame;
import org.lateralgm.ui.swing.visuals.RoomVisual;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.AddPieceInstance;
import org.lateralgm.util.MovePieceInstance;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.PropertyMap.PropertyValidator;
import org.lateralgm.util.RemovePieceInstance;

public class RoomEditor extends VisualPanel
	{
	private static final long serialVersionUID = 1L;

	public static final int ZOOM_MIN = -1;
	public static final int ZOOM_MAX = 2;

	private final Room room;
	protected final RoomFrame frame;
	private Piece cursor;
	public final PropertyMap<PRoomEditor> properties;
	public final RoomVisual roomVisual;

	private final RoomPropertyListener rpl = new RoomPropertyListener();
	private final RoomEditorPropertyValidator repv = new RoomEditorPropertyValidator();

	// Save the original position of a selected piece (Used when moving an object for the undo)
	private Point objectFirstPosition = null;

	public enum PRoomEditor
		{
		SHOW_GRID,SHOW_OBJECTS(RoomVisual.Show.INSTANCES),SHOW_TILES,SHOW_BACKGROUNDS,SHOW_FOREGROUNDS,
		SHOW_VIEWS,DELETE_UNDERLYING_OBJECTS,DELETE_UNDERLYING_TILES,GRID_OFFSET_X,GRID_OFFSET_Y,ZOOM;
		final RoomVisual.Show rvBinding;

		private PRoomEditor()
			{
			String n = name();
			if (n.startsWith("SHOW_"))
				rvBinding = RoomVisual.Show.valueOf(n.substring(5));
			else
				rvBinding = null;
			}

		private PRoomEditor(RoomVisual.Show b)
			{
			rvBinding = b;
			}
		}

	private static final EnumMap<PRoomEditor,Object> DEFS = PropertyMap.makeDefaultMap(
			PRoomEditor.class,true,true,true,true,true,false,true,true,0,0,1);

	public RoomEditor(Room r, RoomFrame frame)
		{
		if (r.get(PRoom.REMEMBER_WINDOW_SIZE))
			{
			EnumMap<PRoomEditor,Object> m = new EnumMap<PRoomEditor,Object>(PRoomEditor.class);
			for (PRoomEditor pre : PRoomEditor.values())
				try
					{
					m.put(pre,r.get(PRoom.valueOf(pre.toString())));
					}
				catch (IllegalArgumentException e)
					{
					m.put(pre,DEFS.get(pre));
					}
			properties = new PropertyMap<PRoomEditor>(PRoomEditor.class,repv,m);
			}
		else
			properties = new PropertyMap<PRoomEditor>(PRoomEditor.class,repv,DEFS);

		room = r;
		this.frame = frame;
		setFocusable(true);
		zoomOrigin = ORIGIN_MOUSE;

		r.properties.updateSource.addListener(rpl);

		enableEvents(MouseEvent.MOUSE_EVENT_MASK | MouseEvent.MOUSE_MOTION_EVENT_MASK);
		EnumSet<RoomVisual.Show> s = EnumSet.noneOf(RoomVisual.Show.class);
		for (PRoomEditor p : PRoomEditor.values())
			if (p.rvBinding != null && (Boolean) properties.get(p)) s.add(p.rvBinding);
		lockBounds();
		roomVisual = new RoomVisual(container,r,s);
		unlockBounds();
		put(0,roomVisual);
		setZoom((Integer) properties.get(PRoomEditor.ZOOM));
		refresh();
		}

	public Room getRoom()
		{
		return room;
		}

	public void refresh()
		{
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

	public void releaseCursor(Point lastPosition)
		{
		// Stores several actions in one compound action for the undo
		CompoundEdit compoundEdit = new CompoundEdit();
		UndoableEdit edit = null;

		// If the piece was moved
		if (objectFirstPosition != null)
			// For the undo, record that the object was moved
			edit = new MovePieceInstance(frame,cursor,objectFirstPosition,new Point(lastPosition));
		else
			// A new piece has been added
			{
			if (cursor instanceof Instance)
				edit = new AddPieceInstance(frame,cursor,room.instances.size() - 1);
			else
				edit = new AddPieceInstance(frame,cursor,room.tiles.size() - 1);
			}

		compoundEdit.addEdit(edit);
		objectFirstPosition = null;

		//it must be guaranteed that cursor != null
		boolean deleteUnderlyingObjects = properties.get(PRoomEditor.DELETE_UNDERLYING_OBJECTS);
		boolean deleteUnderlyingTiles = properties.get(PRoomEditor.DELETE_UNDERLYING_TILES);

		if (deleteUnderlyingObjects && cursor instanceof Instance)
			deleteUnderlying(
					roomVisual.intersectInstances(new Rectangle(lastPosition.x,lastPosition.y,1,1)),
					room.instances,compoundEdit);
		else if (deleteUnderlyingTiles && cursor instanceof Tile)
			deleteUnderlying(
					roomVisual.intersectTiles(new Rectangle(lastPosition.x,lastPosition.y,1,1),getTileDepth()),
					room.tiles,compoundEdit);

		// Save the action for the undo
		compoundEdit.end();
		frame.undoSupport.postEdit(compoundEdit);

		unlockBounds();
		cursor = null;
		}

	private <T>void deleteUnderlying(Iterator<T> i, ActiveArrayList<T> l, CompoundEdit compoundEdit)
		{
		HashSet<T> s = new HashSet<T>();
		while (i.hasNext())
			{
			T t = i.next();
			if (t != cursor)
				{
				UndoableEdit edit;

				// Record the effect of removing an piece for the undo
				if (cursor instanceof Instance)
					edit = new RemovePieceInstance(frame,(Piece) t,room.instances.indexOf(t));
				else
					edit = new RemovePieceInstance(frame,(Piece) t,room.tiles.indexOf(t));

				compoundEdit.addEdit(edit);

				s.add(t);

				}
			}
		l.removeAll(s);
		}

	/** Do not call with null */
	public void setCursor(Piece ds)
		{
		cursor = ds;
		if (ds instanceof Instance)
			{
			frame.oList.setSelectedValue(ds,true);
			frame.fireObjUpdate();
			}
		else if (ds instanceof Tile)
			{
			frame.tList.setSelectedValue(ds,true);
			frame.fireTileUpdate();
			}
		lockBounds();
		}

	private void processLeftButton(int modifiers, boolean pressed, Piece pieceUnderCursor,
			Point position)
		{
		// If we are modifying the position of a piece with the text fields, save the position for the undo
		if (frame.selectedPiece != null)
			{
			frame.processFocusLost();
			this.requestFocusInWindow();
			}

		boolean shiftKeyPressed = ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0);
		boolean ctrlKeyPressed = ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0);

		// If the ctrl and shift keys are not pressed
		if (shiftKeyPressed == false && ctrlKeyPressed == false)
			{
			// If left button has been clicked and if there is an object under the cursor, move the object
			if (pressed && pieceUnderCursor != null && !pieceUnderCursor.isLocked())
				{
				// Record the original position of the object (without snapping) for the undo
				objectFirstPosition = pieceUnderCursor.getPosition();

				setCursor(pieceUnderCursor);
				}

			// If there is no objects under the cursor, add a new object
			if (pressed && pieceUnderCursor == null) addNewPieceInstance(shiftKeyPressed,position);

			}
		else
			{
			// If the shift key is pressed, add objects under the cursor
			if (shiftKeyPressed && cursor != null)
				if (!roomVisual.intersects(new Rectangle(position.x,position.y,1,1),cursor))
					{
					releaseCursor(position);
					pressed = true; //ensures that a new instance is created below
					}

			// If we have pressed the ctrl key
			if (pressed && cursor == null) addNewPieceInstance(shiftKeyPressed,position);

			}

		if (cursor != null && !shiftKeyPressed) cursor.setPosition(position);
		}

	private void addNewPieceInstance(boolean shiftKeyPressed, Point position)
		{
		if (frame.tabs.getSelectedIndex() == Room.TAB_TILES)
			{
			ResourceReference<Background> bkg = frame.taSource.getSelected();
			if (bkg == null) return; //I'd rather just break out of this IF, but this works
			Background b = bkg.get();
			Tile t = new Tile(room,LGM.currentFile);
			t.properties.put(PTile.BACKGROUND,bkg);
			t.setBackgroundPosition(new Point(frame.tSelect.tx,frame.tSelect.ty));
			t.setPosition(position);

			if (!(Boolean) b.get(PBackground.USE_AS_TILESET))
				t.setSize(new Dimension(b.getWidth(),b.getHeight()));
			else
				t.setSize(new Dimension((Integer) b.get(PBackground.TILE_WIDTH),
						(Integer) b.get(PBackground.TILE_HEIGHT)));

			t.setDepth((Integer) frame.taDepth.getValue());
			room.tiles.add(t);
			setCursor(t);
			shiftKeyPressed = true; //prevents unnecessary coordinate update below
			}
		else if (frame.tabs.getSelectedIndex() == Room.TAB_OBJECTS)
			{
			ResourceReference<GmObject> obj = frame.oNew.getSelected();
			if (obj == null) return; //I'd rather just break out of this IF, but this works
			Instance instance = room.addInstance();
			instance.properties.put(PInstance.OBJECT,obj);
			instance.setPosition(position);

			setCursor(instance);
			shiftKeyPressed = true; //prevents unnecessary coordinate update below
			}
		}

	private void processRightButton(int modifiers, boolean pressed, final Piece mc, Point p)
		{
		// If we are modifying the position of a piece with the text fields, save the position for the undo
		if (frame.selectedPiece != null)
			{
			frame.processFocusLost();
			this.requestFocusInWindow();
			}

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

			if (mc instanceof Instance)
				{
				final Instance i = (Instance) mc;
				JMenuItem mi = new JMenuItem(Messages.getString("RoomEditor.CREATION_CODE")); //$NON-NLS-1$
				mi.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
							{
							frame.openCodeFrame(i);
							}
					});
				jp.add(mi);
				}
			Point cp = p.getLocation();
			visualToComponent(cp);
			jp.show(this,cp.x,cp.y);
			}
		else if (!mc.isLocked())
			{
			ArrayList<?> alist = null;
			int pieceIndex = -1;
			JList<?> jlist = null;

			if (mc instanceof Instance)
				{
				pieceIndex = room.instances.indexOf(mc);
				if (pieceIndex == -1) return;

				alist = room.instances;
				jlist = frame.oList;
				CodeFrame fr = frame.codeFrames.get(mc);
				if (fr != null) fr.dispose();
				}
			else if (mc instanceof Tile)
				{
				pieceIndex = room.tiles.indexOf(mc);
				if (pieceIndex == -1) return;
				alist = room.tiles;
				jlist = frame.tList;
				}
			else
				return; //unknown component with unknown lists

			// Record the effect of removing an object for the undo
			UndoableEdit edit = new RemovePieceInstance(frame,mc,pieceIndex);
			// notify the listeners
			frame.undoSupport.postEdit(edit);

			int i2 = jlist.getSelectedIndex();
			alist.remove(pieceIndex);
			jlist.setSelectedIndex(Math.min(alist.size() - 1,i2));
			}
		}

	protected void mouseEdit(MouseEvent e)
		{
		int modifiers = e.getModifiersEx();
		int type = e.getID();
		Point currentPosition = e.getPoint().getLocation();
		componentToVisual(currentPosition);
		int x = currentPosition.x;
		int y = currentPosition.y;

		// If the alt key is not pressed, apply the 'snapping' to the current position
		if ((modifiers & MouseEvent.ALT_DOWN_MASK) == 0)
			{
			int sx = room.get(PRoom.SNAP_X);
			int sy = room.get(PRoom.SNAP_Y);
			int ox = properties.get(PRoomEditor.GRID_OFFSET_X);
			int oy = properties.get(PRoomEditor.GRID_OFFSET_Y);

			if (room.get(PRoom.ISOMETRIC))
				{
				int gx = ox + negDiv(x - ox,sx) * sx;
				int gy = oy + negDiv(y - oy,sy) * sy;
				boolean d = (Math.abs(x - gx - sx / 2) * sy + Math.abs(y - gy - sy / 2) * sx) < sx * sy / 2;
				x = gx + (d ? sx / 2 : x > gx + sx / 2 ? sx : 0);
				y = gy + (d ? sy / 2 : y > gy + sy / 2 ? sy : 0);
				}
			else
				{
				x = ox + negDiv(x - ox,sx) * sx;
				y = oy + negDiv(y - oy,sy) * sy;
				}
			}

		frame.statX.setText(Messages.getString("RoomFrame.STAT_X") + x); //$NON-NLS-1$
		frame.statY.setText(Messages.getString("RoomFrame.STAT_Y") + y); //$NON-NLS-1$
		frame.statId.setText(""); //$NON-NLS-1$
		frame.statSrc.setText(""); //$NON-NLS-1$

		Piece mc = null;

		if (frame.tabs.getSelectedIndex() == Room.TAB_TILES)
			{
			Tile tile = getTopPiece(currentPosition,Tile.class,getTileDepth());
			mc = tile;
			if (mc != null)
				{
				String idt = Messages.getString("RoomFrame.STAT_ID") //$NON-NLS-1$
						+ tile.properties.get(PTile.ID);
				if (mc.isLocked()) idt += " X"; //$NON-NLS-1$
				frame.statId.setText(idt);
				ResourceReference<Background> rb = tile.properties.get(PTile.BACKGROUND);
				Background b = deRef(rb);
				String name = b == null ? Messages.getString("RoomFrame.NO_BACKGROUND") : b.getName();
				idt = Messages.getString("RoomFrame.STAT_TILESET") + name; //$NON-NLS-1$
				frame.statSrc.setText(idt);
				}
			}
		else
			{

			Instance instance = getTopPiece(currentPosition,Instance.class);
			mc = instance;

			if (instance != null)
				{
				String idt = Messages.getString("RoomFrame.STAT_ID") //$NON-NLS-1$
						+ instance.properties.get(PInstance.ID);
				if (mc.isLocked()) idt += " X"; //$NON-NLS-1$
				frame.statId.setText(idt);
				ResourceReference<GmObject> or = instance.properties.get(PInstance.OBJECT);
				GmObject o = deRef(or);
				String name = o == null ? Messages.getString("RoomFrame.NO_OBJECT") : o.getName();
				idt = Messages.getString("RoomFrame.STAT_OBJECT") + name; //$NON-NLS-1$
				frame.statSrc.setText(idt);
				}
			if (frame.tabs.getSelectedIndex() != Room.TAB_OBJECTS) return;
			}

		if ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0)
			processLeftButton(modifiers,type == MouseEvent.MOUSE_PRESSED,mc,new Point(x,y));
		else if (cursor != null) releaseCursor(new Point(x,y));

		if ((modifiers & MouseEvent.BUTTON3_DOWN_MASK) != 0 && mc != null)
			processRightButton(modifiers,type == MouseEvent.MOUSE_PRESSED,mc,currentPosition); //use mouse point
		}

	private <P extends Piece>P getTopPiece(Point p, Class<P> c)
		{
		Iterator<P> pi = roomVisual.intersect(new Rectangle(p.x,p.y,1,1),c);
		P piece = null;
		while (pi.hasNext())
			piece = pi.next();
		return piece;
		}

	private <P extends Piece>P getTopPiece(Point p, Class<P> c, int depth)
		{
		Iterator<P> pi = roomVisual.intersect(new Rectangle(p.x,p.y,1,1),c,depth);
		P piece = null;
		while (pi.hasNext())
			piece = pi.next();
		return piece;
		}

	protected int getTileDepth()
		{
		try
			{
			frame.taDepth.commitEdit();
			}
		catch (ParseException e)
			{ //use the old value, but don't force a revert
			}
		return (Integer) frame.taDepth.getValue();
		}

	public static interface CommandHandler
		{
		void openCodeFrame(Instance i);
		}

	private class RoomPropertyListener extends PropertyUpdateListener<PRoom>
		{
		public void updated(PropertyUpdateEvent<PRoom> e)
			{
			switch (e.key)
				{
				case SNAP_X:
				case SNAP_Y:
					setZoom((Integer) properties.get(PRoomEditor.ZOOM));
					break;
				case DELETE_UNDERLYING_OBJECTS:
				case DELETE_UNDERLYING_TILES:
				case SHOW_BACKGROUNDS:
				case SHOW_FOREGROUNDS:
				case SHOW_GRID:
				case SHOW_OBJECTS:
				case SHOW_TILES:
				case SHOW_VIEWS:
					if (!validating) properties.put(PRoomEditor.valueOf(e.key.name()),room.get(e.key));
					break;
				case REMEMBER_WINDOW_SIZE:
					if (room.get(PRoom.REMEMBER_WINDOW_SIZE)) for (PRoomEditor pre : PRoomEditor.values())
						try
							{
							room.put(PRoom.valueOf(pre.name()),properties.get(pre));
							}
						catch (IllegalArgumentException iae)
							{
							//Some of these settings aren't reflected in the
							//PRoom structure, so we just discard them for now.
						}
				default:
					break;
				}
			}
		}

	@Override
	public void setZoom(int z)
		{
		super.setZoom(z);
		if (z >= 1)
			roomVisual.setGridFactor(1);
		else
			{
			int sx = room.get(PRoom.SNAP_X);
			int sy = room.get(PRoom.SNAP_Y);
			roomVisual.setGridFactor((2 - z) / gcd(2 - z,gcd(sx < 2 ? 0 : sx,sy < 2 ? 0 : sy)));
			}
		}

	private boolean validating;

	private class RoomEditorPropertyValidator implements PropertyValidator<PRoomEditor>
		{
		public Object validate(PRoomEditor k, Object v)
			{
			switch (k)
				{
				case GRID_OFFSET_X:
					roomVisual.setGridXOffset((Integer) v);
					break;
				case GRID_OFFSET_Y:
					roomVisual.setGridYOffset((Integer) v);
					break;
				case ZOOM:
					int i = Math.max(ZOOM_MIN,Math.min(ZOOM_MAX,(Integer) v));
					setZoom(i);
					return i;
				case SHOW_BACKGROUNDS:
				case SHOW_FOREGROUNDS:
				case SHOW_GRID:
				case SHOW_OBJECTS:
				case SHOW_TILES:
				case SHOW_VIEWS:
					updateViewsObjectFollowingProperty();
					roomVisual.setVisible(k.rvBinding,(Boolean) v);
					break;
				case DELETE_UNDERLYING_OBJECTS:
				case DELETE_UNDERLYING_TILES:
					if (room.get(PRoom.REMEMBER_WINDOW_SIZE))
						{
						PRoom prk = PRoom.valueOf(k.name());
						validating = true;
						try
							{
							room.put(prk,v);
							}
						finally
							{
							validating = false;
							}
						return room.get(prk);
						}
				}
			return v;
			}

		// Set the 'object to follow' coordinates for each view in the room
		private void updateViewsObjectFollowingProperty()
			{
			// If the views are not enabled
			if ((Boolean) room.get(PRoom.VIEWS_ENABLED) == false) return;

			for (View view : room.views)
				{
				// If the view is not visible, don't show it
				if ((Boolean) view.properties.get(PView.VISIBLE) == false) return;

				// Get the reference to the 'Object following' object
				ResourceReference<GmObject> objectToFollowReference = null;

				// If there is 'Object following' object for the selected view
				if (view.properties.get(PView.OBJECT) != null)
					objectToFollowReference = view.properties.get(PView.OBJECT);

				// If there is no object to follow, reset the corresponding view properties
				if (objectToFollowReference == null)
					{
					view.properties.put(PView.OBJECT_FOLLOWING_X,-1);
					view.properties.put(PView.OBJECT_FOLLOWING_Y,-1);
					continue;
					}

				Instance instanceToFollow = null;

				// get the first instance in the room
				for (Instance instance : room.instances)
					{
					ResourceReference<GmObject> instanceObject = instance.properties.get(PInstance.OBJECT);

					if (instanceObject == objectToFollowReference)
						{
						instanceToFollow = instance;
						break;
						}
					}

				// If there is an instance to follow
				if (instanceToFollow != null)
					{
					// Properties of the view
					Point viewPosition = new Point(0,0);
					int viewWidth = (Integer) view.properties.get(PView.VIEW_W);
					int viewHeight = (Integer) view.properties.get(PView.VIEW_H);

					// Get the instance position
					Point instancePosition = new Point(0,0);
					instancePosition.x = (Integer) instanceToFollow.properties.get(PInstance.X);
					instancePosition.y = (Integer) instanceToFollow.properties.get(PInstance.Y);

					viewPosition.x = instancePosition.x - viewWidth / 2;
					viewPosition.y = instancePosition.y - viewHeight / 2;

					// Set this new location into the view properties
					view.properties.put(PView.OBJECT_FOLLOWING_X,viewPosition.x);
					view.properties.put(PView.OBJECT_FOLLOWING_Y,viewPosition.y);
					}
				else
					{
					view.properties.put(PView.OBJECT_FOLLOWING_X,-1);
					view.properties.put(PView.OBJECT_FOLLOWING_Y,-1);
					}
				}
			}
		}
	}
