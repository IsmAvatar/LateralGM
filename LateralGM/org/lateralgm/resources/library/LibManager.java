/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.library;

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

public class LibManager
	{
	public static class libFilenameFilter implements FilenameFilter
		{
		public boolean accept(File dir, String name)
			{
			return name.toLowerCase().endsWith(".lib"); //$NON-NLS-1$
			}
		}

	public static ArrayList<Library> libs = new ArrayList<Library>();

	public static LibAction getLibAction(int libraryId, int libActionId)
		{
		for(Library l : libs)
			{
			if(l.id==libraryId)
				{
				LibAction act=l.getLibAction(libActionId);
				if(act!=null) return act;
				}
			}
		return null;
		}

	//XXX : Maybe place the lib finding code here
	public static void autoLoad(String libdir)
		{
		File[] files = new File(libdir).listFiles(new libFilenameFilter());
		Arrays.sort(files);// listFiles does not guarantee a particular order
		for (File f : files)
			{
			System.out.printf(Messages.getString("LibManager.LOADING"),f.getPath()); //$NON-NLS-1$
			System.out.println();
			try
				{
				LoadLibFile(f.getPath());
				}
			catch (LibFormatException ex)
				{
				}
			}
		}

	public static Library LoadLibFile(String FileName) throws LibFormatException
		{
		Library lib = null;
		GmStreamDecoder in = null;
		// int id=-1;
		try
			{
			in = new GmStreamDecoder(FileName);
			int version = in.readi();
			if (version != 520)
				{
				throw new LibFormatException(String.format(
						Messages.getString("LibManager.ERROR_INVALIDFILE"),FileName)); //$NON-NLS-1$
				}
			lib = new Library();
			lib.tabCaption = in.readStr();
			lib.id = in.readi();
			in.skip(in.readi());
			in.skip(4);
			in.skip(8);
			in.skip(in.readi());
			in.skip(in.readi());
			lib.advanced = in.readBool();
			in.skip(4);// no of actions/official lib identifier thingy
			int noacts = in.readi();
			for (int j = 0; j < noacts; j++)
				{
				int ver = in.readi();
				if (ver != 520)
					{
					throw new LibFormatException(String.format(
							Messages.getString("LibManager.ERROR_INVALIDACTION"),j,FileName,ver)); //$NON-NLS-1$
					}

				LibAction act = new LibAction();
				act.parent = lib;
				lib.libActions.add(act);
				in.skip(in.readi());// name
				act.id = in.readi();

				byte[] data = new byte[in.readi()];
				in.read(data);
				act.actImage = ImageIO.read(new ByteArrayInputStream(data));

				act.hidden = in.readBool();
				act.advanced = in.readBool();
				act.registeredOnly = in.readBool();
				act.description = in.readStr();
				act.listText = in.readStr();
				act.hintText = in.readStr();
				act.actionKind = (byte) in.readi();
				act.interfaceKind = (byte) in.readi();
				act.question = in.readBool();
				act.canApplyTo = in.readBool();
				act.allowRelative = in.readBool();
				act.libArguments = new LibArgument[in.readi()];
				int noinsertions = in.readi();
				for (int k = 0; k < noinsertions; k++)
					{
					if (k < act.libArguments.length)
						{
						LibArgument arg = act.libArguments[k] = new LibArgument();
						arg.caption = in.readStr();
						arg.kind = (byte) in.readi();
						arg.defaultVal = in.readStr();
						arg.menu = in.readStr();
						}
					else
						{
						in.skip(in.readi());
						in.skip(4);
						in.skip(in.readi());
						in.skip(in.readi());
						}
					}
				act.execType = (byte) in.readi();
				act.execFunction = in.readStr();
				act.execCode = in.readStr();
				}
			}
		catch (FileNotFoundException ex)
			{
			throw new LibFormatException(String.format(Messages.getString("LibManager.ERROR_NOTFOUND"),FileName)); //$NON-NLS-1$
			}
		catch (IOException ex)
			{
			throw new LibFormatException(String.format(
					Messages.getString("LibManager.ERROR_READING"),FileName,ex.getMessage())); //$NON-NLS-1$
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
				throw new LibFormatException(Messages.getString("LibManager.ERROR_CLOSEFAILED")); //$NON-NLS-1$
				}
			}
		libs.add(lib);
		return lib;
		}
	}