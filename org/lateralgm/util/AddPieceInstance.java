/**
* Record the effect of adding a piece (object/tile) for the undo
*
* 
*/

package org.lateralgm.util;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.Piece;

public class AddPieceInstance extends AbstractUndoableEdit
{

  private Instance instance;
  private int index;
  private Room room;
  private Piece piece;

  public AddPieceInstance(Room room, Piece piece, int index)
  {
  	this.piece = piece;
  	this.index = index;
  	this.room = room;
  }

  public void undo() throws CannotUndoException
  {
  	if (piece instanceof Instance)
  		room.instances.remove(index);
  	else
  		room.tiles.remove(index);
  }

  public void redo() throws CannotRedoException
  {
		if (piece instanceof Instance)
			room.instances.add((Instance)piece);
		else
			room.tiles.add((Tile)piece);
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