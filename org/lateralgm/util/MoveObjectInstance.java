/**
* Record the effect of moving an object instance for the undo
*
* 
*/

package org.lateralgm.util;

import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.components.visual.RoomEditor;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.sub.Instance;

public class MoveObjectInstance extends AbstractUndoableEdit
{

  private Instance instance;
  //private Piece piece;
  private Point oldPosition;
  private Point newPosition;
  private RoomEditor roomEditor;

  public MoveObjectInstance(RoomEditor roomEditor, Instance instance, Point oldPosition, Point newPosition)
  { 
  	this.instance = instance;
  	this.oldPosition = oldPosition;
  	this.newPosition = newPosition;
  	this.roomEditor = roomEditor;
  }

  public void undo() throws CannotUndoException
  {
  	// Select the current instance
  	roomEditor.setCursor(instance);
  	instance.setPosition(oldPosition);

  }

  public void redo() throws CannotRedoException
  {
  	// Select the current instance
		roomEditor.setCursor(instance);
		instance.setPosition(newPosition);
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