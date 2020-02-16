/**
* Record the effect of modifying a piece (object/tile) instance for the undo
*
* Before modifying a piece, the piece must be selected in the list.
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

import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.subframes.RoomFrame;

public class ModifyPieceInstance extends AbstractUndoableEdit
	{
	private static final long serialVersionUID = 1L;

	private final Piece piece;
	private RoomFrame roomFrame;
	
	private Object key;
	private Object oldVal = null;
	private Object newVal = null;

	public ModifyPieceInstance(RoomFrame roomFrame, Piece piece, Object key, Object oldVal, Object newVal)
		{
		this.roomFrame = roomFrame;
		this.piece = piece;
		this.key = key;
		this.oldVal = oldVal;
		this.newVal = newVal;
		}

	private void selectPiece()
		{
		// Select the current piece
		if (piece instanceof Instance)
			{
			roomFrame.oList.setSelectedValue(piece,true);
			roomFrame.fireObjUpdate();
			}
		else
			{
			roomFrame.tList.setSelectedValue(piece,true);
			roomFrame.fireTileUpdate();
			}
		}

	@Override
	public void undo() throws CannotUndoException
		{
		selectPiece();
		if (piece instanceof Instance)
			((Instance)piece).properties.put((PInstance) key, oldVal);
		else if (piece instanceof Tile)
			((Tile)piece).properties.put((PTile) key, oldVal);
		}

	@Override
	public void redo() throws CannotRedoException
		{
		selectPiece();
		if (piece instanceof Instance)
			((Instance)piece).properties.put((PInstance) key, newVal);
		else if (piece instanceof Tile)
			((Tile)piece).properties.put((PTile) key, newVal);
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
