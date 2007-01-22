package fileRes;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

import resourcesRes.ResId;
import resourcesRes.Resource;
import resourcesRes.subRes.Argument;

import componentRes.ResNode;

public class GmStreamEncoder
	{
	private BufferedOutputStream _out;

	public GmStreamEncoder(String FilePath) throws FileNotFoundException
		{
		_out = new BufferedOutputStream(new FileOutputStream(FilePath));
		}

	public void write(int b) throws IOException
		{
		_out.write(b);
		}

	public void write(byte[] b) throws IOException
		{
		write(b,0,b.length);
		}

	public void write(byte[] b,int off,int len) throws IOException
		{
		writei(len);
		_out.write(b,off,len);
		}

	public void writei(int val) throws IOException
		{
		_out.write(val & 255);
		_out.write((val >> 8) & 255);
		_out.write((val >> 16) & 255);
		_out.write((val >> 24) & 255);
		}

	public void writeStr(String str) throws IOException
		{
		writei(str.length());
		_out.write(str.getBytes("ascii"));
		}

	public void writeBool(boolean val) throws IOException
		{
		if (val)
			writei(1);
		else
			writei(0);
		}

	public void writeD(double val) throws IOException
		{
		long num = Double.doubleToLongBits(val);
		_out.write((int) ((num) & 255));
		_out.write((int) ((num >> 8) & 255));
		_out.write((int) ((num >> 16) & 255));
		_out.write((int) ((num >> 24) & 255));
		_out.write((int) ((num >> 32) & 255));
		_out.write((int) ((num >> 40) & 255));
		_out.write((int) ((num >> 48) & 255));
		_out.write((int) ((num >> 56) & 255));
		}

	public void compress(byte[] data) throws IOException
		{
		Deflater compresser = new Deflater();
		compresser.setInput(data);
		compresser.finish();
		byte[] buffer = new byte[100];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!compresser.finished())
			{
			int len = compresser.deflate(buffer);
			baos.write(buffer,0,len);
			}
		writei(baos.size());
		_out.write(baos.toByteArray());
		}

	public void writeImage(Image image) throws IOException
		{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ImageIO.write((RenderedImage) image,"bmp",data);
		compress(data.toByteArray());
		}

	public void close() throws IOException
		{
		_out.close();
		}

	public void fill(int count) throws IOException
		{
		for (int i = 0; i < count; i++)
			{
			writei(0);
			}
		}

	public void writeId(ResId id,byte type,Gm6File src) throws IOException
		{
		writeId(id,type,-1,src);
		}

	public void writeId(ResId id,byte type,int noneval,Gm6File src) throws IOException
		{
		Resource res = null;
		switch (type)
			{
			case Resource.SPRITE:
				res = src.getSprite(id);
				break;
			case Resource.SOUND:
				res = src.getSound(id);
				break;
			case Resource.BACKGROUND:
				res = src.getBackground(id);
				break;
			case Resource.PATH:
				res = src.getPath(id);
				break;
			case Resource.SCRIPT:
				res = src.getScript(id);
				break;
			case Resource.FONT:
				res = src.getFont(id);
				break;
			case Resource.TIMELINE:
				res = src.getTimeline(id);
				break;
			case Resource.GMOBJECT:
				res = src.getGmObject(id);
				break;
			case Resource.ROOM:
				res = src.getRoom(id);
				break;
			}
		if (id != null && res != null)
			{
			writei(id.value);
			}
		else
			{
			writei(noneval);
			}
		}

	public void writeIdStr(ResId id,byte type,Gm6File src) throws IOException
		{
		Resource res = null;
		switch (type)
			{
			case Argument.ARG_SPRITE:
				res = src.getSprite(id);
				break;
			case Argument.ARG_SOUND:
				res = src.getSound(id);
				break;
			case Argument.ARG_BACKGROUND:
				res = src.getBackground(id);
				break;
			case Argument.ARG_PATH:
				res = src.getPath(id);
				break;
			case Argument.ARG_SCRIPT:
				res = src.getScript(id);
				break;
			case Argument.ARG_FONT:
				res = src.getFont(id);
				break;
			case Argument.ARG_TIMELINE:
				res = src.getTimeline(id);
				break;
			case Argument.ARG_GMOBJECT:
				res = src.getGmObject(id);
				break;
			case Argument.ARG_ROOM:
				res = src.getRoom(id);
				break;
			}
		if (id != null && res != null)
			{
			writeStr(Integer.toString(id.value));
			}
		else
			{
			writeStr("-1");
			}
		}

	public void writeTree(ResNode root) throws IOException
		{
		Enumeration e = root.preorderEnumeration();
		e.nextElement();
		while (e.hasMoreElements())
			{
			ResNode node = (ResNode)e.nextElement();
			writei(node.status);
			writei(node.kind);
			if (node.resourceId != null)
				writei(node.resourceId.value);
			else
				writei(0);
			writeStr((String)node.getUserObject());
			writei(node.getChildCount());
			}
		}
	}