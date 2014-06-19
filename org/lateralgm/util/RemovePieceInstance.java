/**
* Record the effect of removing a piece (object/tile) instance for the undo
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

public class RemovePieceInstance extends AbstractUndoableEdit
{

  private Piece piece;
  private int index;
  private RoomEditor roomEditor;

  public RemovePieceInstance(RoomEditor roomEditor, Piece piece, int index)
  {
  	this.piece = piece;
  	this.index = index;
  	this.roomEditor = roomEditor;
  }

  public void undo() throws CannotUndoException
  {
		if (piece instanceof Instance)
			roomEditor.room.instances.add(index, (Instance)piece);
		else
			roomEditor.room.tiles.add(index, (Tile)piece);
		
  	// Select the current instance
  	roomEditor.setCursor(piece);
  }

  public void redo() throws CannotRedoException
  {
		if (piece instanceof Instance)
			roomEditor.room.instances.remove(index);
		else
			roomEditor.room.tiles.remove(index);
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