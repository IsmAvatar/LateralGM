package resourcesRes.libraryRes;

import java.awt.image.BufferedImage;

import resourcesRes.subRes.Action;
public class LibAction
{
    public static final byte INTERFACE_NORMAL=0;
    public static final byte INTERFACE_NONE=1;
    public static final byte INTERFACE_ARROWS=2;
    public static final byte INTERFACE_CODE=3;
    public static final byte INTERFACE_TEXT=4;
    
    public int Id=0;
    public BufferedImage ActImage;
    public boolean Hidden=false;
    public boolean Advanced=false;
    public boolean RegisteredOnly=false;
    public String Description="";
    public String ListText="";
    public String HintText="";
    public byte ActionKind=Action.ACT_NORMAL;
    public byte InterfaceKind=INTERFACE_NORMAL;
    public boolean Question=false;
    public boolean CanApplyTo=false;
    public boolean AllowRelative=false;
    public byte ExecType=Action.EXEC_FUNCTION;
    public String ExecFunction="";
    public String ExecCode="";
    public int NoLibArguments=0;
    public LibArgument[] LibArguments=new LibArgument[6];
    public LibAction()
    {
        for(int i=0;i<6;i++)
        {
            LibArguments[i]=new LibArgument();
        }
    }
}