package resourcesRes;

import java.util.ArrayList;

import mainRes.Prefs;
import resourcesRes.subRes.Action;
import resourcesRes.subRes.Moment;
import fileRes.ResourceList;

public class Timeline extends Resource
	{
	private ArrayList<Moment> Moments = new ArrayList<Moment>();

	public Timeline()
		{
		name = Prefs.prefixes[Resource.TIMELINE];
		}

	public int NoMoments()
		{
		return Moments.size();
		}

	public Moment addMoment()
		{
		Moments.add(new Moment());
		return Moments.get(NoMoments() - 1);
		}

	public Moment getMoment(int stepNo)
		{
		int ListIndex = MomentIndex(stepNo);
		if (ListIndex != -1) return Moments.get(ListIndex);
		return null;
		}

	public Moment getMomentList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoMoments()) return Moments.get(ListIndex);
		return null;
		}

	public void removeMoment(int MomentVal)
		{
		int ListIndex = MomentIndex(MomentVal);
		if (ListIndex != -1) Moments.remove(ListIndex);
		}

	public int MomentIndex(int stepNo)
		{
		for (int i = 0; i < NoMoments(); i++)
			{
			if (getMomentList(i).stepNo == stepNo)
				{
				return i;
				}
			}
		return -1;
		}

	public void clearMoments()
		{
		Moments.clear();
		}

	public Timeline copy(boolean update, ResourceList src)
		{
		Timeline time = new Timeline();
		for (int i = 0; i < NoMoments(); i++)
			{
			Moment mom = getMomentList(i);
			Moment mom2 = time.addMoment();
			mom2.stepNo = mom.stepNo;
			for (int j = 0; j < mom.NoActions(); j++)
				{
				Action act = mom.getAction(j);
				Action act2 = mom2.addAction();
				act2.LibraryId = act.LibraryId;
				act2.LibActionId = act.LibActionId;
				act2.ActionKind = act.ActionKind;
				act2.AllowRelative = act.AllowRelative;
				act2.Question = act.Question;
				act2.CanApplyTo = act.CanApplyTo;
				act2.ExecType = act.ExecType;
				act2.ExecFunction = act.ExecFunction;
				act2.ExecCode = act.ExecCode;
				act2.Relative = act.Relative;
				act2.Not = act.Not;
				act2.AppliesTo = act.AppliesTo;
				act2.NoArguments = act.NoArguments;
				for (int k = 0; k < act.NoArguments; k++)
					{
					act2.Arguments[k].Kind = act.Arguments[k].Kind;
					act2.Arguments[k].Res = act.Arguments[k].Res;
					act2.Arguments[k].Val = act.Arguments[k].Val;
					}
				}
			}
		if (update)
			{
			time.Id.value = ++src.LastId;
			time.name = Prefs.prefixes[Resource.TIMELINE] + src.LastId;
			src.add(time);
			}
		else
			{
			time.Id = Id;
			time.name = name;
			}
		return time;
		}
	}