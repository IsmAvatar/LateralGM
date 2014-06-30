/**
* Record the effect of moving a piece (object/tile) instance for the undo
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

import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.subframes.RoomFrame;

public class MovePieceInstance extends AbstractUndoableEdit
{
  private Piece piece;
  private Point oldPosition;
  private Point newPosition;
  private RoomFrame roomFrame;

  public MovePieceInstance(RoomFrame roomFrame, Piece piece, Point oldPosition, Point newPosition)
  { 
  	this.piece = piece;
  	this.oldPosition = oldPosition;
  	this.newPosition = newPosition;
  	this.roomFrame = roomFrame;
  }

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

  	piece.setPosition(oldPosition);
  }

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
  
		piece.setPosition(newPosition);
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