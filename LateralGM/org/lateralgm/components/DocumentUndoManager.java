/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 *
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.lateralgm.messages.Messages;

@SuppressWarnings("serial")
public class DocumentUndoManager extends UndoManager implements CaretListener
	{
	protected final AbstractAction undoAction;
	protected final AbstractAction redoAction;
	protected int caretUpdates = 0;

	public DocumentUndoManager()
		{
		undoAction = new AbstractAction(Messages.getString("DocumentUndoManager.UNDO"))
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae)
					{
					endGroupEdit();
					try
						{
						undo();
						}
					catch (CannotUndoException e)
						{
						}
					updateActions();
					}
			};
		redoAction = new AbstractAction(Messages.getString("DocumentUndoManager.REDO"))
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae)
					{
					try
						{
						redo();
						}
					catch (CannotRedoException e)
						{
						}
					updateActions();
					}
			};
		updateActions();
		}

	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit)
		{
		UndoableEdit le = lastEdit();
		try
			{
			if (le instanceof GroupEdit)
				{
				GroupEdit ge = (GroupEdit) le;
				if (ge.addEdit(anEdit)) return true;
				}
			endGroupEdit();
			GroupEdit ge = new GroupEdit();
			if (ge.addEdit(anEdit) && super.addEdit(ge)) return true;
			return super.addEdit(anEdit);
			}
		finally
			{
			updateActions();
			}
		}

	public void endGroupEdit()
		{
		UndoableEdit le = lastEdit();
		if (le instanceof GroupEdit) ((GroupEdit) le).end();
		}

	private class GroupEdit extends CompoundEdit
		{
		@Override
		public boolean canUndo()
			{
			if (edits.size() == 0 || edits.lastElement() != null && edits.lastElement().canUndo()
					|| super.canUndo()) return true;
			return false;
			}

		@Override
		public boolean addEdit(UndoableEdit anEdit)
			{
			UndoableEdit le = lastEdit();
			if (!(anEdit instanceof AbstractDocument.DefaultDocumentEvent)) return false;
			AbstractDocument.DefaultDocumentEvent dde1, dde2;
			dde2 = (AbstractDocument.DefaultDocumentEvent) anEdit;
			EventType et = dde2.getType();
			if (le != null)
				{
				dde1 = (AbstractDocument.DefaultDocumentEvent) le;
				if (dde1.getType().equals(et))
					{
					int o1 = dde1.getOffset();
					int o2 = dde2.getOffset();
					int l1 = dde1.getLength();
					int l2 = dde2.getLength();
					if (et.equals(EventType.INSERT) && o2 == o1 + l1)
						{
						return super.addEdit(anEdit);
						}
					else if (et.equals(EventType.REMOVE) && (o2 == o1 + l1 || o1 == o2 + l2))
						{
						return super.addEdit(anEdit);
						}
					}
				}
			else if (et.equals(EventType.INSERT) || et.equals(EventType.REMOVE))
				return super.addEdit(anEdit);
			return false;
			}
		}

	public void updateActions()
		{
		undoAction.setEnabled(canUndo());
		redoAction.setEnabled(canRedo());
		}

	public AbstractAction getRedoAction()
		{
		return redoAction;
		}

	public AbstractAction getUndoAction()
		{
		return undoAction;
		}

	public void caretUpdate(CaretEvent e)
		{
		if (caretUpdates > 0) endGroupEdit();
		caretUpdates++;
		}

	public void undoableEditHappened(UndoableEditEvent e)
		{
		caretUpdates = 0;
		addEdit(e.getEdit());
		}
	}
