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

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.subframes.RoomFrame;

public class ModifyPieceInstance extends AbstractUndoableEdit
	{
	private static final long serialVersionUID = 1L;

	private Piece piece;
	private Point oldPosition;
	private Point newPosition;
	private RoomFrame roomFrame;
	private Point2D oldScale;
	private Point2D newScale;

	public ModifyPieceInstance(RoomFrame roomFrame, Piece piece, Point oldPosition, Point newPosition)
		{
		this.piece = piece;
		this.oldPosition = oldPosition;
		this.newPosition = newPosition;
		this.roomFrame = roomFrame;
		this.oldScale = null;
		this.newScale = null;
		}

	public ModifyPieceInstance(RoomFrame roomFrame, Piece piece, Point oldPosition,
			Point newPosition, Point2D oldScale, Point2D newScale)
		{
		this.piece = piece;
		this.oldPosition = oldPosition;
		this.newPosition = newPosition;
		this.roomFrame = roomFrame;
		this.oldScale = oldScale;
		this.newScale = newScale;
		}

	@Override
	public void undo() throws CannotUndoException
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

		if (oldPosition != null) piece.setPosition(oldPosition);
		if (oldScale != null) piece.setScale(oldScale);
		}

	@Override
	public void redo() throws CannotRedoException
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

		if (newPosition != null) piece.setPosition(newPosition);
		if (newScale != null) piece.setScale(newScale);
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