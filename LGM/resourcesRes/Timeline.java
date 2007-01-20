package resourcesRes;

import java.util.ArrayList;

import resourcesRes.subRes.Moment;
public class Timeline extends Resource
{
    private ArrayList<Moment> Moments=new ArrayList<Moment>();
    public int NoMoments()
    {
        return Moments.size();
    }
    public Moment addMoment()
    {
        Moments.add(new Moment());
        return Moments.get(NoMoments()-1);
    }
    public Moment getMoment(int stepNo)
    {
        int ListIndex=MomentIndex(stepNo);
        if (ListIndex!=-1)
            return Moments.get(ListIndex);
        return null;
    }
    public Moment getMomentList(int ListIndex)
    {
        if (ListIndex>=0&&ListIndex<NoMoments())
            return Moments.get(ListIndex);
        return null;
    }
    public void removeMoment(int MomentVal)
    {
        int ListIndex=MomentIndex(MomentVal);
        if (ListIndex!=-1)
            Moments.remove(ListIndex);
    }
    public int MomentIndex(int stepNo)
    {
        for(int i=0;i<NoMoments();i++)
        {
            if (getMomentList(i).stepNo==stepNo)
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
}