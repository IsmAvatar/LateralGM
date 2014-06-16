/**
* Record the effect of removing an object instance for the undo
*
* 
*/

package org.lateralgm.util;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.Room;

public class RemoveObjectInstance extends AbstractUndoableEdit
{

  private Instance instance;
  private int index;
  private Room room;

  public RemoveObjectInstance(Room room, Instance instance, int index)
  {
  	this.instance = instance;
  	this.index = index;
  	this.room = room;
  }

  public void undo() throws CannotUndoException
  {
		room.instances.add(index, instance);
  }

  public void redo() throws CannotRedoException
  {
  	room.instances.remove(index);
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