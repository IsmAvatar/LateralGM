/**
* Record the effect of removing a piece (object/tile) instance for the undo
*
* Copyright (C) 2014, egofree
*
* This file is part of LateralGM.
* LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
* See LICENSE for details.
*/

package org.lateralgm.util;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.subframes.RoomFrame;

public class RemovePieceInstance extends AbstractUndoableEdit
	{
	private static final long serialVersionUID = 1L;

	private Piece piece;
	private int index;
	private RoomFrame roomFrame;

	public RemovePieceInstance(RoomFrame roomFrame, Piece piece, int index)
		{
		this.piece = piece;
		this.index = index;
		this.roomFrame = roomFrame;
		}

	public void undo() throws CannotUndoException
		{
		if (piece instanceof Instance)
			{
			roomFrame.res.instances.add(index,(Instance) piece);
			roomFrame.oList.setSelectedValue(piece,true);
			roomFrame.fireObjUpdate();
			}
		else
			{
			roomFrame.res.tiles.add(index,(Tile) piece);
			roomFrame.tList.setSelectedValue(piece,true);
			roomFrame.fireTileUpdate();
			}

		}

	public void redo() throws CannotRedoException
		{
		if (piece instanceof Instance)
			roomFrame.res.instances.remove(index);
		else
			roomFrame.res.tiles.remove(index);
		}

	public boolean canUndo()
		{
		return true;
		}

	public boolean canRedo()
		{
		return true;
		}

	}
