/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.library;

import static org.lateralgm.file.GmStreamDecoder.mask;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.lateralgm.file.GmStreamDecoder;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.sub.Action;

public final class LibManager
	{
	private LibManager() //should not be instantiated
		{
		}

	public static class LibFilenameFilter implements FilenameFilter
		{
		public boolean accept(File dir, String name)
			{
			return name.toLowerCase().endsWith(".lib"); //$NON-NLS-1$
			}
		}

	public static ArrayList<Library> libs = new ArrayList<Library>();

	public static LibAction getLibAction(int libraryId, int libActionId)
		{
		for (Library l : libs)
			{
			if (l.id == libraryId)
				{
				LibAction act = l.getLibAction(libActionId);
				if (act != null) return act;
				}
			}
		return null;
		}

	//XXX : Maybe place the lib finding code here
	public static void autoLoad(String libdir)
		{
		File[] files = new File(libdir).listFiles(new LibFilenameFilter());
		if (files == null) return;
		Arrays.sort(files); // listFiles does not guarantee a particular order
		for (File f : files)
			{
			System.out.printf(Messages.getString("LibManager.LOADING"),f.getPath()); //$NON-NLS-1$
			System.out.println();
			try
				{
				loadFile(f.getPath());
				}
			catch (LibFormatException ex)
				{
				System.out.println(ex.getMessage());
				}
			}
		}

	/**
	 * Loads a library file of given fileName of either LIB or LGL format
	 * @param filename
	 * @return the library
	 * @throws LibFormatException
	 */
	public static Library loadFile(String filename) throws LibFormatException
		{
		Library lib = null;
		GmStreamDecoder in = null;
		try
			{
			in = new GmStreamDecoder(filename);
			int header = in.read3();
			if (header == (('L' << 8) | ('G' << 4) | 'L'))
				lib = loadLgl(in);
			else if (header == 500 || header == 520)
				lib = loadLib(in);
			else
				throw new LibFormatException(String.format(
						Messages.getString("LibManager.ERROR_INVALIDFILE"),filename)); //$NON-NLS-1$
			libs.add(lib);
			}
		catch (FileNotFoundException ex)
			{
			throw new LibFormatException(String.format(
					Messages.getString("LibManager.ERROR_NOTFOUND"),filename)); //$NON-NLS-1$
			}
		catch (IOException ex)
			{
			throw new LibFormatException(String.format(
					Messages.getString("LibManager.ERROR_READING"),filename,ex.getMessage())); //$NON-NLS-1$
			}
		catch (LibFormatException ex)
			{
			throw new LibFormatException(String.format(ex.getMessage(),filename));
			}
		finally
			{
			try
				{
				if (in != null)
					{
					in.close();
					in = null;
					}
				}
			catch (IOException ex)
				{
				String msg = Messages.getString("LibManager.ERROR_CLOSEFAILED"); //$NON-NLS-1$
				throw new LibFormatException(msg);
				}
			}
		return lib;
		}

	/**
	 * Workhorse for constructing a library out of given StreamDecoder of LIB format
	 * @param in
	 * @return the library (not yet added to the libs list)
	 * @throws LibFormatException
	 * @throws IOException
	 */
	public static Library loadLib(GmStreamDecoder in) throws LibFormatException,IOException
		{
		if (in.read() != 0)
			throw new LibFormatException(
					Messages.getString("LibManager.ERROR_INVALIDFILE")); //$NON-NLS-1$
		Library lib = new Library();
		lib.tabCaption = in.readStr();
		lib.id = in.read4();
		in.skip(in.read4());
		in.skip(4);
		in.skip(8);
		in.skip(in.read4());
		in.skip(in.read4());
		lib.advanced = in.readBool();
		in.skip(4); // no of actions/official lib identifier thingy
		int acts = in.read4();
		for (int j = 0; j < acts; j++)
			{
			int ver = in.read4();
			if (ver != 500 && ver != 520)
				{
				throw new LibFormatException(String.format(
						Messages.getString("LibManager.ERROR_INVALIDACTION"), //$NON-NLS-1$
						j,"%s",ver));  //$NON-NLS-1$
				}

			LibAction act = lib.addLibAction();
			act.parent = lib;
			act.name = in.readStr();
			act.id = in.read4();

			byte[] data = new byte[in.read4()];
			in.read(data);
			act.actImage = ImageIO.read(new ByteArrayInputStream(data));

			act.hidden = in.readBool();
			act.advanced = in.readBool();
			if (ver == 520) act.registeredOnly = in.readBool();
			act.description = in.readStr();
			act.listText = in.readStr();
			act.hintText = in.readStr();
			act.actionKind = (byte) in.read4();
			act.interfaceKind = (byte) in.read4();
			act.question = in.readBool();
			act.canApplyTo = in.readBool();
			act.allowRelative = in.readBool();
			act.libArguments = new LibArgument[in.read4()];
			int args = in.read4();
			for (int k = 0; k < args; k++)
				{
				if (k < act.libArguments.length)
					{
					LibArgument arg = new LibArgument();
					arg.caption = in.readStr();
					arg.kind = (byte) in.read4();
					arg.defaultVal = in.readStr();
					arg.menu = in.readStr();
					act.libArguments[k] = arg;
					}
				else
					{
					in.skip(in.read4());
					in.skip(4);
					in.skip(in.read4());
					in.skip(in.read4());
					}
				}
			act.execType = (byte) in.read4();
			if (act.execType == Action.EXEC_FUNCTION)
				act.execInfo = in.readStr();
			else
				in.skip(in.read4());
			if (act.execType == Action.EXEC_CODE)
				act.execInfo = in.readStr();
			else
				in.skip(in.read4());
			}
		return lib;
		}

	/**
	 * Workhorse for constructing a library out of given StreamDecoder of LGL format
	 * @param in
	 * @return the library (not yet added to the libs list)
	 * @throws LibFormatException
	 * @throws IOException
	 */
	public static Library loadLgl(GmStreamDecoder in) throws LibFormatException,IOException
		{
		if (in.read2() != 160)
			throw new LibFormatException(
					Messages.getString("LibManager.ERROR_INVALIDFILE")); //$NON-NLS-1$
		Library lib = new Library();
		lib.id = in.read3();
		lib.tabCaption = in.readStr1();
		in.skip(in.read());
		in.skip(4);
		in.skip(8);
		in.skip(in.read4());
		in.skip(in.read4());
		int acts = in.read();
		lib.advanced = mask(acts,128);
		acts &= 127;
		for (int j = 0; j < acts; j++)
			{
			if (in.read2() != 160)
				throw new LibFormatException(
						Messages.getString("LibManager.ERROR_INVALIDFILE")); //$NON-NLS-1$
			LibAction act = new LibAction();
			act.parent = lib;
			act.id = in.read2();
			act.name = in.readStr1();

			byte[] data = new byte[in.read4()];
			in.read(data);
			act.actImage = ImageIO.read(new ByteArrayInputStream(data));

			act.description = in.readStr1();
			act.listText = in.readStr1();
			act.hintText = in.readStr1();
			int tags = in.read();
			act.hidden = mask(tags,128);
			act.advanced = mask(tags,64);
			act.registeredOnly = mask(tags,32);
			act.question = mask(tags,16);
			act.canApplyTo = mask(tags,8);
			act.allowRelative = mask(tags,4);
			act.execType = (byte) (tags & 3);
			act.execInfo = in.readStr();
			tags = in.read4();
			act.actionKind = (byte) (tags >> 4);
			act.interfaceKind = (byte) (tags & 15);
			tags = in.read();
			for (int k = 0; k < tags; k++)
				{
				LibArgument arg = new LibArgument();
				arg.caption = in.readStr1();
				arg.kind = (byte) in.read();
				arg.defaultVal = in.readStr1();
				arg.menu = in.readStr1();
				act.libArguments[k] = arg;
				}
			}
		return lib;
		}
	}
