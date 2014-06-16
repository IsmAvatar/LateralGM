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

import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.Room;

public class MoveObjectInstance extends AbstractUndoableEdit
{

  private Instance instance;
  private Point oldPosition;
  private Point newPosition;
  private Room room;

  public MoveObjectInstance(Room room, Instance instance, Point oldPosition, Point newPosition)
  {
  	this.instance = instance;
  	this.oldPosition = oldPosition;
  	this.newPosition = newPosition;
  	this.room = room;
  }

  public void undo() throws CannotUndoException
  {
  	instance.setPosition(oldPosition);
  }

  public void redo() throws CannotRedoException
  {
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