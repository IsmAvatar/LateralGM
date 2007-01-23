package fileRes;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import mainRes.LGM;

import componentRes.ResNode;

public class GmStreamDecoder
	{
	private BufferedInputStream _in;

	public GmStreamDecoder(String path) throws FileNotFoundException
		{
		_in = new BufferedInputStream(new FileInputStream(path));
		}

	public int read() throws IOException
		{
		return _in.read();
		}

	public int read(byte b[]) throws IOException
		{
		return read(b,0,b.length);
		}

	public int read(byte b[],int off,int len) throws IOException
		{
		return _in.read(b,off,len);
		}

	public int readi() throws IOException
		{
		int a = _in.read();
		int b = _in.read();
		int c = _in.read();
		int d = _in.read();
		if (a == -1 || b == -1 || c == -1 || d == -1) throw new IOException("unexpected end of file reached");
		long result = (a | (b << 8) | (c << 16) | (d << 24));
		if ((result & 0x80000000L) == 0x80000000L) result = -(0x100000000L - result);
		return (int) result;
		}

	public String readStr() throws IOException
		{
		byte data[] = new byte[readi()];
		long check = _in.read(data);
		if (check < data.length) throw new IOException("unexpected end of file reached");
		return new String(data);
		}

	public boolean readBool() throws IOException
		{
		int val = readi();
		if (val != 0 && val != 1) throw new IOException("invalid boolean data: " + val);
		if (val == 0) return false;
		return true;
		}

	public double readD() throws IOException
		{
		int a = _in.read();
		int b = _in.read();
		int c = _in.read();
		int d = _in.read();
		int e = _in.read();
		int f = _in.read();
		int g = _in.read();
		int h = _in.read();
		long result = (long) a | (long) b << 8 | (long) c << 16 | (long) d << 24 | (long) e << 32
				| (long) f << 40 | (long) g << 48 | (long) h << 56;
		return Double.longBitsToDouble(result);
		}

	public byte[] decompress(int length) throws IOException, DataFormatException
		{
		Inflater decompresser = new Inflater();
		byte[] compressedData = new byte[length];
		_in.read(compressedData,0,length);
		decompresser.setInput(compressedData,0,compressedData.length);
		byte[] result = new byte[100];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!decompresser.finished())
			{
			int len = decompresser.inflate(result);
			baos.write(result,0,len);
			}
		decompresser.end();
		return baos.toByteArray();
		}

	public BufferedImage readImage() throws IOException, DataFormatException
		{
		int length = readi();
		return ImageIO.read(new ByteArrayInputStream(decompress(length)));
		}

	public void close() throws IOException
		{
		_in.close();
		}

	public long skip(long length) throws IOException
		{
		long total = _in.skip(length);
		while (total < length)
			{
			total += _in.skip(length - total);
			}
		return total;
		}

	public void readTree(ResNode root) throws IOException
		{
		Stack<ResNode> path = new Stack<ResNode>();
		Stack<Integer> left = new Stack<Integer>();
		path.add(root);
		int cur = 11;
		while (cur-- > 0)
			{
			byte status = (byte)readi();
			byte type = (byte)readi();
			int ind = readi();
			String name = readStr();
			ResNode node = path.peek().addChild(name,status,type);
			if (status == ResNode.STATUS_SECONDARY)
				{
				LGM.currentFile.getResUnsafe(node,ind);
				}
			int contents = readi();
			if (contents > 0)
				{
				left.push(new Integer(cur));
				cur = contents;
				path.push(node);
				}
			while (cur == 0 && !left.isEmpty())
				{
				cur = left.pop().intValue();
				path.pop();
				}
			}
		}
	}