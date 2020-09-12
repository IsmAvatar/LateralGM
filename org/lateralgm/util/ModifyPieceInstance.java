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

import java.awt.Color;
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
	public enum Type
		{
		NAME, POSITION, SCALE, ROTATION, ALPHA, COLOR
		};

	private static final long serialVersionUID = 1L;

	private final Piece piece;
	private RoomFrame roomFrame;
	private Type type;
	
	private Object oldVal = null;
	private Object newVal = null;

	public ModifyPieceInstance(RoomFrame roomFrame, Piece piece, Type type, Object oldVal, Object newVal)
		{
		this.roomFrame = roomFrame;
		this.piece = piece;
		this.type = type;
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
		switch (type)
			{
			case NAME: piece.setName((String) oldVal); break;
			case POSITION: piece.setPosition((Point) oldVal); break;
			case SCALE: piece.setScale((Point2D) oldVal); break;
			case ROTATION: piece.setRotation((double) oldVal); break;
			case ALPHA: piece.setAlpha((int) oldVal); break;
			case COLOR: piece.setColor((Color) oldVal); break;
			}
		}

	@Override
	public void redo() throws CannotRedoException
		{
		selectPiece();
		switch (type)
			{
			case NAME: piece.setName((String) newVal); break;
			case POSITION: piece.setPosition((Point) newVal); break;
			case SCALE: piece.setScale((Point2D) newVal); break;
			case ROTATION: piece.setRotation((double) newVal); break;
			case ALPHA: piece.setAlpha((int) newVal); break;
			case COLOR: piece.setColor((Color) newVal); break;
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
