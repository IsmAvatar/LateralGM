package resourcesRes;

import resourcesRes.subRes.MainEvent;

public class GmObject extends Resource
{
    public static final ResId OBJECT_SELF=new ResId(-1);
    public static final ResId OBJECT_OTHER=new ResId(-2);
    
    public ResId Sprite=null;
    public boolean Solid=false;
    public boolean Visible=true;
    public int Depth=0;
    public boolean Persistent=false;
    public ResId Parent=null;
    public ResId Mask=null;
    public  MainEvent[] MainEvents=new MainEvent[11];
    public GmObject()
    {
        for(int j=0;j<11;j++)
        {
            MainEvents[j]=new MainEvent();
        }
    }
}