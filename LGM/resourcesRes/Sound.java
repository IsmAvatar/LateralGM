package resourcesRes;

public class Sound extends Resource
{
    public static final byte SOUND_NORMAL=0;
    public static final byte SOUND_BACKGROUND=1;
    public static final byte SOUND_3D=2;
    public static final byte SOUND_MULTIMEDIA=3;
    
    public byte Type=SOUND_NORMAL;
    public String FileType="";
    public String FileName="";
    public boolean Chorus=false;
    public boolean Echo=false;
    public boolean Flanger=false;
    public boolean Gargle=false;
    public boolean Reverb=false;
    public double Volume=1;
    public double Pan=0;
    public boolean Preload=true;
    public byte[] Data;
    
    public static boolean getReverb(int effects)
    {
        if((effects&16)==16)
            return true;
        return false;
    }
    public static boolean getGargle(int effects)
    {
        if((effects&8)==8)
            return true;
        return false;
    }
    public static boolean getFlanger(int effects)
    {
        if((effects&4)==4)
            return true;
        return false;
    }
    public static boolean getEcho(int effects)
    {
        if((effects&2)==2)
            return true;
        return false;
    }
    public static boolean getChorus(int effects)
    {
        if((effects&1)==1)
            return true;
        return false;
    }
    public static int makeEffects(boolean chorus,boolean echo,boolean flanger,boolean gargle, boolean reverb)
    {
        int result=0;
        if(chorus) result+=1;
        if(echo) result+=2;
        if(flanger) result+=4;
        if(gargle) result+=8;
        if(reverb) result+=16;
        return result;
    }
}