/**
* Record the effect of adding a piece (object/tile) for the undo
*
* 
*/

package org.lateralgm.util;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.components.visual.RoomEditor;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.subframes.RoomFrame;

public class AddPieceInstance extends AbstractUndoableEdit
{
  private int index;
  private RoomFrame roomFrame;
  private Piece piece;

  public AddPieceInstance(RoomFrame roomFrame, Piece piece, int index)
  {
  	this.piece = piece;
  	this.index = index;
  	this.roomFrame = roomFrame;
  }

  public void undo() throws CannotUndoException
  {
		if (piece instanceof Instance)
			roomFrame.res.instances.remove(index);
		else
			roomFrame.res.tiles.remove(index);
  }

  public void redo() throws CannotRedoException
  {
		if (piece instanceof Instance)
			roomFrame.res.instances.add(index, (Instance)piece);
		else
			roomFrame.res.tiles.add(index, (Tile)piece);
		
		// Select the current piece
	  roomFrame.oList.setSelectedValue(piece,true);
	  roomFrame.fireObjUpdate();
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