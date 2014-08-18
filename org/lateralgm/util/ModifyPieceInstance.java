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
	private RoomFrame roomFrame;
	private Point oldPosition = null;
	private Point newPosition = null;
	private Point2D oldScale = null;
	private Point2D newScale = null;
	private Double oldRotation = null;
	private Double newRotation = null;

	// Record the effect of moving a piece
	public ModifyPieceInstance(RoomFrame roomFrame, Piece piece, Point oldPosition, Point newPosition)
		{
		this.roomFrame = roomFrame;
		this.piece = piece;
		this.oldPosition = oldPosition;
		this.newPosition = newPosition;
		}

	// Record the effect of modifying the scale of a piece
	public ModifyPieceInstance(RoomFrame roomFrame, Piece piece, Point2D oldScale, Point2D newScale)
		{
		this.roomFrame = roomFrame;
		this.piece = piece;
		this.oldScale = oldScale;
		this.newScale = newScale;
		}

	// Record the effect of modifying the rotation of a piece
	public ModifyPieceInstance(RoomFrame roomFrame, Piece piece, Double oldRotation, Double newRotation)
		{
		this.roomFrame = roomFrame;
		this.piece = piece;
		this.oldRotation = oldRotation;
		this.newRotation = newRotation;
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
		if (oldRotation != null) piece.setRotation(oldRotation);
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
		if (newRotation != null) piece.setRotation(newRotation);
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