package org.lateralgm.resources.library;

import java.util.ArrayList;

public class Library
	{
	public int id = 0;
	public String TabCaption = "";
	public boolean Advanced = false;
	private ArrayList<LibAction> LibActions = new ArrayList<LibAction>();

	public int NoLibActions()
		{
		return LibActions.size();
		}

	LibAction addLibAction()
		{
		LibAction act = new LibAction();
		LibActions.add(act);
		return act;
		}

	LibAction getLibAction(int id)
		{
		int ListIndex = LibActionIndex(id);
		if (ListIndex != -1) return LibActions.get(ListIndex);
		return null;
		}

	LibAction getLibActionList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoLibActions()) return LibActions.get(ListIndex);
		return null;
		}

	int LibActionIndex(int id)
		{
		for (int i = 0; i < NoLibActions(); i++)
			{
			if (getLibActionList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}
	}