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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.subframes.CodeFrame;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.ui.swing.visuals.RoomVisual;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.AddPieceInstance;
import org.lateralgm.util.ModifyPieceInstance;
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
	// The selected piece has a white border
	private Piece selectedPiece;
	public final PropertyMap<PRoomEditor> properties;
	public final RoomVisual roomVisual;

	private final RoomPropertyListener rpl = new RoomPropertyListener();
	private final RoomEditorPropertyValidator repv = new RoomEditorPropertyValidator();

	// Save the original position of a selected piece (Used when moving an object for the undo)
	private Point objectFirstPosition = null;
	// Option which set if the tiles editing is available for all layers or only for the selected one
	private boolean editOtherLayers = false;
	// Rectangle which stores the user's selection
	public Rectangle selection = null;
	// Original position when drawing a selection
	private Point selectionOrigin = null;
	// Save the original position of the selected instances/tiles
	private Point selectedPiecesOrigin = null;

	// The instances selected by the user
	private List<Instance> selectedInstances = new ArrayList<Instance>();
	private List<Tile> selectedTiles = new ArrayList<Tile>();
	// Show if the user has pasted a region
	private boolean pasteMode = false;
	// Show if the alt key has been pressed
	private boolean altKeyHasBeenPressed = false;
	// Save if the ctrl key has been pressed
	private boolean ctrlKeyHasBeenPressed = false;
	// Show if the shift key has been pressed
	private boolean shiftKeyHasBeenPressed = false;

	public enum PRoomEditor
		{
		SHOW_GRID,SHOW_OBJECTS(RoomVisual.Show.INSTANCES),SHOW_TILES,SHOW_BACKGROUNDS,SHOW_FOREGROUNDS,
		SHOW_VIEWS,DELETE_UNDERLYING_OBJECTS,DELETE_UNDERLYING_TILES,GRID_OFFSET_X,GRID_OFFSET_Y,ZOOM,
		SINGLE_SELECTION,MULTI_SELECTION,SNAP_TO_GRID,ADD_ON_TOP,ADD_MULTIPLE;
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
			PRoomEditor.class,true,true,true,true,true,false,true,true,0,0,1,true,false,true,false,false);

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
		setOpaque(false); // so the EditorScrollPane's transparency pattern shows through
		setZoom((Integer) properties.get(PRoomEditor.ZOOM));
		refresh();
		}

	// Set if the tiles editing is available for all layers or only for the selected one
	public void editOtherLayers(boolean editOtherLayers)
		{
		this.editOtherLayers = editOtherLayers;
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

	public Piece getSelectedPiece()
		{
		return selectedPiece;
		}

	public void setSelectedPiece(Piece selectedPiece)
		{
		this.selectedPiece = selectedPiece;
		}

	// Save the selected tiles and make a buffer image
	public void copySelectionTiles()
		{
		if (selection == null) return;

		selectedTiles.clear();
		selectedInstances.clear();

		Room currentRoom = getRoom();
		Point tilePosition;
		// Get the selected layer
		Integer depth = (Integer) frame.tileLayer.getSelectedItem();

		// Save all tiles in the selected region
		for (Tile tile : currentRoom.tiles)
			{
			tilePosition = tile.getPosition();

			// If the instance is in the selected region
			if (tilePosition.x >= selection.x && tilePosition.x < (selection.x + selection.width)
					&& tilePosition.y >= selection.y && tilePosition.y < (selection.y + selection.height))
				{
				// If the were editing only the current layer, and if the tile is not in the current layer
				if (!frame.tEditOtherLayers.isSelected() && tile.getDepth() != depth) continue;

				selectedTiles.add(tile);
				}
			}

		// Save the origin of the selected tiles
		selectedPiecesOrigin = new Point(selection.x,selection.y);
		// Make an image of the region made by the user
		roomVisual.setSelectionImage(null,selectedTiles);
		}

	// Save the selected instances and make a buffer image
	public void copySelectionInstances()
		{
		if (selection == null) return;

		selectedInstances.clear();
		selectedTiles.clear();

		Room currentRoom = getRoom();
		Point instancePosition;

		// Save all instances in the selected region
		for (Instance instance : currentRoom.instances)
			{
			instancePosition = instance.getPosition();

			// If the instance is in the selected region
			if (instancePosition.x >= selection.x && instancePosition.x < (selection.x + selection.width)
					&& instancePosition.y >= selection.y
					&& instancePosition.y < (selection.y + selection.height))
				selectedInstances.add(instance);
			}

		// Save the origin of the selected instances;
		selectedPiecesOrigin = new Point(selection.x,selection.y);
		// Make an image of the region made by the user
		roomVisual.setSelectionImage(selectedInstances,null);
		}

	// Activate the object selection mode
	public void activateSelectObjectMode()
		{
		properties.put(PRoomEditor.SINGLE_SELECTION,true);
		}

	// Deactivate the object selection mode
	public void deactivateSelectObjectMode()
		{
		properties.put(PRoomEditor.SINGLE_SELECTION,false);
		}

	// Activate the rectangular selection mode
	public void activateSelectRegionMode()
		{
		properties.put(PRoomEditor.MULTI_SELECTION,true);
		}

	// Deactivate the rectangular selection mode
	public void deactivateSelectRegionMode()
		{
		properties.put(PRoomEditor.MULTI_SELECTION,false);
		roomVisual.setSelection(null);
		selection = null;
		}

	// Deactivate the paste mode
	public void deactivatePasteMode()
		{
		pasteMode = false;
		roomVisual.deactivatePasteMode();
		}

	// Activate the paste mode
	public void activatePasteMode()
		{
		pasteMode = true;
		roomVisual.activatePasteMode();
		// Disable the selection tool
		properties.put(PRoomEditor.MULTI_SELECTION,false);
		}

	// Paste the selected instances on the given mouse position
	private void pasteInstances(Point mousePosition)
		{
		boolean deleteUnderlyingInstances = properties.get(PRoomEditor.DELETE_UNDERLYING_OBJECTS);

		// Stores several actions in one compound action for the undo
		CompoundEdit compoundEdit = new CompoundEdit();

		// If the 'Delete underlying' option is checked, delete all instances for the selected region
		if (deleteUnderlyingInstances)
			frame.deleteInstancesInSelection(
					new Rectangle(mousePosition.x,mousePosition.y,roomVisual.getSelectionImageWidth(),
							roomVisual.getSelectionImageHeight()),compoundEdit);

		for (Instance instance : selectedInstances)
			{
			Point position = instance.getPosition();
			// Get the relative position of the instance inside the selected region
			Point newPosition = new Point(position.x - selectedPiecesOrigin.x + mousePosition.x,
					position.y - selectedPiecesOrigin.y + mousePosition.y);

			Instance newInstance = room.addInstance();
			newInstance.properties.put(PInstance.OBJECT,instance.properties.get(PInstance.OBJECT));
			newInstance.setRotation(instance.getRotation());
			newInstance.setScale(instance.getScale());
			newInstance.setColor(instance.getColor());
			newInstance.setAlpha(instance.getAlpha());
			newInstance.setCode(instance.getCode());
			newInstance.setCreationCode(instance.getCreationCode());
			newInstance.setPosition(newPosition);

			// Record the effect of adding a new instance for the undo
			UndoableEdit edit = new AddPieceInstance(frame,newInstance,room.instances.size() - 1);
			compoundEdit.addEdit(edit);
			}

		// Save the action for the undo
		compoundEdit.end();
		frame.undoSupport.postEdit(compoundEdit);

		}

	// Paste the selected tiles on the given mouse position
	private void pasteTiles(Point mousePosition)
		{
		boolean deleteUnderlyingTiles = properties.get(PRoomEditor.DELETE_UNDERLYING_TILES);

		// Stores several actions in one compound action for the undo
		CompoundEdit compoundEdit = new CompoundEdit();

		// If the 'Delete underlying' option is checked, delete all tiles for the selected region
		if (deleteUnderlyingTiles)
			frame.deleteTilesInSelection(
					new Rectangle(mousePosition.x,mousePosition.y,roomVisual.getSelectionImageWidth(),
							roomVisual.getSelectionImageHeight()),compoundEdit);

		for (Tile tile : selectedTiles)
			{
			Point position = tile.getPosition();
			// Get the relative position of the tile inside the selected region
			Point newPosition = new Point(position.x - selectedPiecesOrigin.x + mousePosition.x,
					position.y - selectedPiecesOrigin.y + mousePosition.y);

			Tile newTile = new Tile(room,LGM.currentFile);
			newTile.properties.put(PTile.BACKGROUND,tile.properties.get(PTile.BACKGROUND));
			newTile.setBackgroundPosition(tile.getBackgroundPosition());
			newTile.setPosition(newPosition);
			newTile.setSize(tile.getSize());
			newTile.setDepth(tile.getDepth());
			room.tiles.add(newTile);

			// Record the effect of adding a new tile for the undo
			UndoableEdit edit = new AddPieceInstance(frame,newTile,room.tiles.size() - 1);
			compoundEdit.addEdit(edit);
			}

		// Save the action for the undo
		compoundEdit.end();
		frame.undoSupport.postEdit(compoundEdit);

		}

	@Override
	protected void processMouseEvent(MouseEvent e)
		{
		super.processMouseEvent(e);
		mouseEdit(e);
		}

	@Override
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
			edit = new ModifyPieceInstance(frame,cursor,objectFirstPosition,new Point(lastPosition));
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
		boolean addMultipleMode = properties.get(PRoomEditor.ADD_MULTIPLE);

		// If there was a selected piece, deselect it
		if (selectedPiece != null) selectedPiece.setSelected(false);

		// Save the selected piece
		if (ds != null) selectedPiece = ds;

		cursor = ds;
		// If we are not in 'Add multiple' mode, select the piece
		if (addMultipleMode == false) cursor.setSelected(true);

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

	// Distance from the piece's origin
	int offsetX = 0;
	int offsetY = 0;

	private void processLeftButton(int modifiers, boolean pressed, Piece pieceUnderCursor,
			Point position)
		{
		// If we are modifying the position of a piece with the text fields, save the position for the undo
		if (frame.selectedPiece != null)
			{
			frame.processFocusLost();
			this.requestFocusInWindow();
			}

		boolean addMultipleMode = properties.get(PRoomEditor.ADD_MULTIPLE);
		boolean addOnTopMode = properties.get(PRoomEditor.ADD_ON_TOP);

		// If the 'add multiple' mode and 'add on top' modes are disabled
		if (addMultipleMode == false && addOnTopMode == false)
			{
			// If left button has been clicked and if there is an object under the cursor, move the object
			if (pressed && pieceUnderCursor != null && !pieceUnderCursor.isLocked())
				{
				// Record the original position of the object (without snapping) for the undo
				objectFirstPosition = pieceUnderCursor.getPosition();

				// Get distance from the piece's origin to prevent shifting when selecting a piece
				offsetX = position.x - pieceUnderCursor.getPosition().x;
				offsetY = position.y - pieceUnderCursor.getPosition().y;

				setCursor(pieceUnderCursor);
				}

			// If there is no objects under the cursor, add a new object
			if (pressed && pieceUnderCursor == null)
				{
				offsetX = 0;
				offsetY = 0;
				addNewPieceInstance(position);
				addMultipleMode = true; //prevents unnecessary coordinate update below
				}

			}
		else
			{
			offsetX = 0;
			offsetY = 0;

			// If the shift key is pressed, add objects under the cursor
			if (addMultipleMode && cursor != null)
				if (!roomVisual.intersects(new Rectangle(position.x,position.y,1,1),cursor))
					{
					releaseCursor(position);
					pressed = true; //ensures that a new instance is created below
					}

			// If we have pressed the ctrl key
			if (pressed && cursor == null)
				{
				addNewPieceInstance(position);
				addMultipleMode = true; //prevents unnecessary coordinate update below
				}

			}

		if (cursor != null && !addMultipleMode)
			cursor.setPosition(new Point(position.x - offsetX,position.y - offsetY));
		}

	private void addNewPieceInstance(Point position)
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

			t.setDepth((Integer) frame.tileLayer.getSelectedItem());
			room.tiles.add(t);
			setCursor(t);

			}
		else if (frame.tabs.getSelectedIndex() == Room.TAB_OBJECTS)
			{
			ResourceReference<GmObject> obj = frame.oNew.getSelected();
			if (obj == null) return; //I'd rather just break out of this IF, but this works
			Instance instance = room.addInstance();
			instance.properties.put(PInstance.OBJECT,obj);
			instance.setPosition(position);

			setCursor(instance);
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

		// If there is a selected piece, deselect it
		if (selectedPiece != null) selectedPiece.setSelected(false);

		boolean addOnTopMode = properties.get(PRoomEditor.ADD_ON_TOP);

		if (addOnTopMode == true)
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
							frame.openInstanceCodeFrame(i);
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

	// If the alt key was pressed, disable the snap to grid mode, if needed
	public void altKeyPressed()
		{
		boolean snapToGridMode = properties.get(PRoomEditor.SNAP_TO_GRID);

		// Save that the alt key has been pressed
		if (snapToGridMode)
			{
			altKeyHasBeenPressed = true;
			properties.put(PRoomEditor.SNAP_TO_GRID,false);
			}
		}

	// If the alt key was released, activate the snap to grid mode, if needed
	public void altKeyReleased()
		{
		if (altKeyHasBeenPressed)
			{
			altKeyHasBeenPressed = false;
			properties.put(PRoomEditor.SNAP_TO_GRID,true);
			}
		}

	// If the ctrl key was pressed, enable add on top mode, if needed
	public void ctrlKeyPressed()
		{
		boolean addOnTopMode = properties.get(PRoomEditor.ADD_ON_TOP);

		// Save that the ctrl key has been pressed
		if (addOnTopMode == false)
			{
			ctrlKeyHasBeenPressed = true;
			properties.put(PRoomEditor.ADD_ON_TOP,true);
			}
		}

	// If the ctrl key was released, disable add on top mode, if needed
	public void ctrlKeyReleased()
		{
		if (ctrlKeyHasBeenPressed)
			{
			ctrlKeyHasBeenPressed = false;
			properties.put(PRoomEditor.ADD_ON_TOP,false);
			}
		}

	// If the shift key was pressed, enable add multiple mode, if needed
	public void shiftKeyPressed()
		{
		boolean addMultipleMode = properties.get(PRoomEditor.ADD_MULTIPLE);

		// Save that the shift key has been pressed
		if (addMultipleMode == false)
			{
			shiftKeyHasBeenPressed = true;
			properties.put(PRoomEditor.ADD_MULTIPLE,true);
			}
		}

	// If the shift key was released, disable add multiple mode, if needed
	public void shiftKeyReleased()
		{
		if (shiftKeyHasBeenPressed)
			{
			shiftKeyHasBeenPressed = false;
			properties.put(PRoomEditor.ADD_MULTIPLE,false);
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

		boolean leftButtonPressed = ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0);
		boolean rightButtonPressed = ((modifiers & MouseEvent.BUTTON3_DOWN_MASK) != 0);
		boolean selectionMode = properties.get(PRoomEditor.MULTI_SELECTION);
		boolean snapToGridMode = properties.get(PRoomEditor.SNAP_TO_GRID);

		// If the 'snap to grid' mode is activated
		if (snapToGridMode == true)
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

		// If the selection button is pressed
		if (selectionMode)
			{
			// If the user has pressed the left button
			if (leftButtonPressed)
				{
				// Ensure the selection is inside the room
				if (x < 0) x = 0;
				if (y < 0) y = 0;
				if (x > room.getWidth()) x = room.getWidth();
				if (y > room.getHeight()) y = room.getHeight();

				// If the drag process starts, save the position
				if (selectionOrigin == null)
					{
					// If there was a selected piece, deselect it
					if (selectedPiece != null) selectedPiece.setSelected(false);
					selectionOrigin = new Point(x,y);
					return;
					}
				else
					{
					// Calculate the origin and the dimension of the selection
					int newSelectionOriginX = Math.min(selectionOrigin.x,x);
					int newSelectionOriginY = Math.min(selectionOrigin.y,y);
					int width = Math.abs(x - selectionOrigin.x);
					int height = Math.abs(y - selectionOrigin.y);

					// Save the selection and display it
					selection = new Rectangle(newSelectionOriginX,newSelectionOriginY,width,height);
					roomVisual.setSelection(selection);
					return;
					}

				}
			else
				{
				// if the drag process ends, reset the selection
				if (selectionOrigin != null)
					{
					selectionOrigin = null;
					return;
					}
				}
			}

		frame.statX.setText(Messages.getString("RoomFrame.STAT_X") + x); //$NON-NLS-1$
		frame.statY.setText(Messages.getString("RoomFrame.STAT_Y") + y); //$NON-NLS-1$
		frame.statId.setText(""); //$NON-NLS-1$
		frame.statSrc.setText(""); //$NON-NLS-1$

		// Update the mouse position in room visual
		roomVisual.setMousePosition(new Point(x,y));

		// If the user is doing a paste
		if (pasteMode && leftButtonPressed)
			{
			// If the user has selected instances, paste them
			if (selectedInstances.size() > 0) pasteInstances(new Point(x,y));
			if (selectedTiles.size() > 0) pasteTiles(new Point(x,y));
			return;
			}

		// If we are in paste mode and the user has clicked the right button, deactivate the paste mode
		if (pasteMode && rightButtonPressed) deactivatePasteMode();
		if (selectionMode && rightButtonPressed) return;

		Piece mc = null;

		if (frame.tabs.getSelectedIndex() == Room.TAB_TILES)
			{
			Tile tile = getTopPiece(currentPosition,Tile.class,getTileDepth());

			// If we can edit all layers and no tiles have been found, look for a tile, regardless of its layer
			if (tile == null && editOtherLayers == true) tile = getTopPiece(currentPosition,Tile.class);

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

		if (leftButtonPressed)
			processLeftButton(modifiers,type == MouseEvent.MOUSE_PRESSED,mc,new Point(x,y));
		else if (cursor != null) releaseCursor(new Point(x - offsetX,y - offsetY));

		if (rightButtonPressed && mc != null)
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
		return (Integer) frame.tileLayer.getSelectedItem();
		}

	public static interface CommandHandler
		{
		CodeFrame openInstanceCodeFrame(Instance i);
		}

	private class RoomPropertyListener extends PropertyUpdateListener<PRoom>
		{
		@Override
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
				case MULTI_SELECTION:
					// If the multi selection mode is set to off, reset the selection
					if (((Boolean) v) == false)
						{
						roomVisual.setSelection(null);
						selection = null;
						}
					break;
				case ZOOM:
					int i = Math.max(ZOOM_MIN,Math.min(ZOOM_MAX,(Integer) v));
					setZoom(i);
					return i;
				case ADD_MULTIPLE:
				case ADD_ON_TOP:
				case SNAP_TO_GRID:
				case SINGLE_SELECTION:
					break;
				case SHOW_BACKGROUNDS:
				case SHOW_FOREGROUNDS:
				case SHOW_OBJECTS:
				case SHOW_TILES:
				case SHOW_GRID:
					roomVisual.setVisible(k.rvBinding,(Boolean) v);
					break;
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
