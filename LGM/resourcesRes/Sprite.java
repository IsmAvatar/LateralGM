package resourcesRes;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import mainRes.LGM;

public class Sprite extends Resource
{
    public static final byte BBOX_AUTO=0;
    public static final byte BBOX_FULL=1;
    public static final byte BBOX_MANUAL=2;
    
    public int Width=32;
    public int Height=32;
    public boolean Transparent=true;
    public boolean PreciseCC=true;
    public boolean SmoothEdges=false;
    public boolean Preload=true;
    public int OriginX=0;
    public int OriginY=0;
    public byte BoundingBoxMode=BBOX_AUTO;
    public int BoundingBoxLeft=0;
    public int BoundingBoxRight=0;
    public int BoundingBoxTop=0;
    public int BoundingBoxBottom=0;
    private ArrayList<BufferedImage> SubImages=new ArrayList<BufferedImage>();
    public int NoSubImages()
    {
        return SubImages.size();
    }
    public BufferedImage addSubImage()
    {
        BufferedImage sub=null;
        try
        {
            sub=ImageIO.read(LGM.class.getResource("icons/default_sprite.png"));
            SubImages.add(sub);
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }        
        return sub;
    }
    public BufferedImage addSubImage(byte[] imagedata)
    {
        BufferedImage result=null;
        try
        {
            ByteArrayInputStream imagestr=new ByteArrayInputStream(imagedata);
            result=ImageIO.read(imagestr);
            SubImages.add(result);
        }
        catch (IOException ex)
        {
            System.err.println("error getting image data for subimage "+NoSubImages()+" sprite "+Id.value);
        }
        return result;
    }
    public void addSubImage(BufferedImage image)
    {
        SubImages.add(image);
    }
    public BufferedImage getSubImage(int ListIndex)
    {
        if (ListIndex>=0&&ListIndex<NoSubImages())
            return SubImages.get(ListIndex);
        return null;
    }
    public void removeSubImage(int ListIndex)
    {
        if (ListIndex>=0&&ListIndex<NoSubImages())
            SubImages.remove(ListIndex);
    }
    public void clearSubImages()
    {
        SubImages.clear();
    }
    public BufferedImage copySubImage(int ListIndex)//returns a copy of
    //subimage with given index (new subimage is not added to the sprite)
    {
        BufferedImage bf=getSubImage(ListIndex);
        if (bf!=null)
        {
            BufferedImage bf2=new BufferedImage(bf.getWidth(), bf.getHeight(), bf.getType());
            bf2.setData(bf.getData());
            return bf2;
        }
        return null;
    }
}
