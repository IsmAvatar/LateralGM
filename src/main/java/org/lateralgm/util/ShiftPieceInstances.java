/**
 * Record the effect of shifting piece (objects/tiles) instances for the undo
 *
 * I don't know why, but before moving a piece, the piece must be selected in the list.
 *
 * Copyright (C) 2014, egofree
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.util;

import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.subframes.RoomFrame;

public class ShiftPieceInstances extends AbstractUndoableEdit
	{
	private static final long serialVersionUID = 1L;

	private int horizontalShift;
	private int verticalShift;
	private boolean tilesTabIsSelected;
	private RoomFrame roomFrame;

	public ShiftPieceInstances(RoomFrame roomFrame, boolean tilesTabIsSelected, int horizontalShift,
			int verticalShift)
		{
		this.horizontalShift = horizontalShift;
		this.verticalShift = verticalShift;
		this.roomFrame = roomFrame;
		this.tilesTabIsSelected = tilesTabIsSelected;
		}

	@Override
	public void undo() throws CannotUndoException
		{
		Room room = roomFrame.getRoomEditor().getRoom();

		// If we are shifting tiles
		if (tilesTabIsSelected)
			{

			for (Tile tile : room.tiles)
				{
				// Select the tile in the list, otherwise it is sometimes not correctly selected
				roomFrame.tList.setSelectedValue(tile,false);

				Point newPosition = new Point(tile.getPosition().x - horizontalShift,tile.getPosition().y
						- verticalShift);
				tile.setPosition(newPosition);
				}

			}
		else
			// Shift the objects
			{

			for (Instance instance : room.instances)
				{
				// Select the instance in the list, otherwise it is sometimes not correctly selected
				roomFrame.oList.setSelectedValue(instance,false);

				Point newPosition = new Point(instance.getPosition().x - horizontalShift,
						instance.getPosition().y - verticalShift);
				instance.setPosition(newPosition);
				}
			}
		}

	@Override
	public void redo() throws CannotRedoException
		{
		Room room = roomFrame.getRoomEditor().getRoom();

		// If we are shifting tiles
		if (tilesTabIsSelected)
			{

			for (Tile tile : room.tiles)
				{
				// Select the tile in the list, otherwise it is sometimes not correctly selected
				roomFrame.tList.setSelectedValue(tile,false);

				Point newPosition = new Point(tile.getPosition().x + horizontalShift,tile.getPosition().y
						+ verticalShift);
				tile.setPosition(newPosition);
				}

			}
		else
			// Shift the objects
			{

			for (Instance instance : room.instances)
				{
				// Select the instance in the list, otherwise it is sometimes not correctly selected
				roomFrame.oList.setSelectedValue(instance,false);

				Point newPosition = new Point(instance.getPosition().x + horizontalShift,
						instance.getPosition().y + verticalShift);
				instance.setPosition(newPosition);
				}
			}
		}

	@Override
	public boolean canUndo()
		{
		return true;
		}

	@Override
	public boolean canRedo()
		{
		return true;
		}

	}
