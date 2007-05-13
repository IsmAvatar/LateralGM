/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
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


public class LibManager
	{
	public static class libFilenameFilter implements FilenameFilter
		{
		public boolean accept(File dir, String name)
			{
			return name.toLowerCase().endsWith(".lib"); //$NON-NLS-1$
			}
		}

	private static ArrayList<Library> libs = new ArrayList<Library>();

	public static int NoLibs()
		{
		return libs.size();
		}

	private static LibAction getLibAction(int LibraryId, int LibActionId)
		{
		int no = noLibraries(LibraryId);
		for (int i = 0; i < no; i++)
			{
//			int ListIndex = LibActionIndex(LibActionId);
//			if (ListIndex != -1) return libActions.get(ListIndex);
//			return null;
			
			LibAction act = getLibrary(LibraryId,i).getLibAction(LibActionId);
			if (act != null) return act;
			}
		return null;
		}

	public static Library addLibrary()
		{
		Library lib = new Library();
		libs.add(lib);
		return lib;
		}

	public static Library getLibrary(int id, int n)
		{
		int ListIndex = LibraryIndex(id,n);
		if (ListIndex != -1) return libs.get(ListIndex);
		return null;
		}

	public static Library getLibraryList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoLibs()) return libs.get(ListIndex);
		return null;
		}

	public static int noLibraries(int id)
		{
		int nofound = 0;
		for (int i = 0; i < NoLibs(); i++)
			{
			if (getLibraryList(i).id == id)
				{
				nofound++;
				}
			}
		return nofound;
		}

	private static int LibraryIndex(int id, int n)
		{
		int nofound = 0;
		for (int i = 0; i < NoLibs(); i++)
			{
			if (getLibraryList(i).id == id)
				{
				if (nofound == n)
					{
					return i;
					}
				nofound++;
				}
			}
		return -1;
		}

	public static void clearLibraries()
		{
		libs.clear();
		}

	public static void autoLoad(String libdir)
		{
		File[] files = new File(libdir).listFiles(new libFilenameFilter());
		Arrays.sort(files);// listFiles does not guarantee a particular order
		for (int i = 0; i < files.length; i++)
			{
			System.out.printf(Messages.getString("LibManager.LOADING"),files[i].getPath()); //$NON-NLS-1$
			System.out.println();
			try
				{
				LoadLibFile(files[i].getPath());
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
			// System.out.println("GM version: "+version);
			lib = new Library();
			lib.tabCaption = in.readStr();// System.out.println("tab caption is: "+lib.TabCaption);
			lib.id = in.readi();// System.out.println("lib id is: "+lib.Id);
			in.skip(in.readi());// Author
			in.skip(4);// lib version
			in.skip(8);// last changed
			in.skip(in.readi());// info
			in.skip(in.readi());// initialisation code
			lib.advanced = in.readBool();// System.out.println("advanced lib: "+lib.Advanced);
			in.skip(4);// no of actions/official lib identifier thingy
			int noacts = in.readi();// System.out.println("no of actions: "+noacts);
			for (int j = 0; j < noacts; j++)
				{
				// System.out.println("Action Entry "+j+"------------------");
				int ver = in.readi();
				if (ver != 520)
					{
					throw new LibFormatException(String.format(
							Messages.getString("LibManager.ERROR_INVALIDACTION"),j,FileName,ver)); //$NON-NLS-1$
					}

				LibAction act = new LibAction();
				lib.libActions.add(act);
				in.skip(in.readi());// name
				act.id = in.readi();// System.out.println("Action id is: "+act.Id);

				byte[] data = new byte[in.readi()];
				in.read(data);
				act.actImage = ImageIO.read(new ByteArrayInputStream(data));

				act.hidden = in.readBool();// System.out.println("hidden: "+act.Hidden);
				act.advanced = in.readBool();// System.out.println("advanced: "+act.Advanced);
				act.registeredOnly = in.readBool();// System.out.println("registered only: "+act.RegisteredOnly);
				act.description = in.readStr();// System.out.println("description: "+act.Description);
				act.listText = in.readStr();// System.out.println("list text: "+act.ListText);
				act.hintText = in.readStr();// System.out.println("hint text :"+act.HintText);
				act.actionKind = (byte) in.readi();// System.out.println("action kind: "+act.ActionKind);
				act.interfaceKind = (byte) in.readi();// System.out.println("interface: "+act.InterfaceKind);
				act.question = in.readBool();// System.out.println("question: "+act.Question);
				act.canApplyTo = in.readBool();// System.out.println("show apply to: "+act.CanApplyTo);
				act.allowRelative = in.readBool();// System.out.println("show relative: "+act.AllowRelative);
				act.noLibArguments = in.readi();// System.out.println("no of arguments: "+act.NoLibArguments);
				// System.out.println("___________________");
				int noinsertions = in.readi();
				for (int k = 0; k < noinsertions; k++)
					{
					if (k < act.noLibArguments)
						{
						LibArgument arg = act.libArguments[k];
						arg.caption = in.readStr();// System.out.println("argument "+k+" caption: "+arg.Caption);
						arg.kind = (byte) in.readi();// System.out.println("argument "+k+" kind: "+arg.Kind);
						arg.defaultVal = in.readStr();// System.out.println("argument "+k+" default value:
						// "+arg.DefaultVal);
						arg.menu = in.readStr();
						/*
						 * if (arg.Kind==Argument.ARG_MENU) { System.out.println("argument "+k+" menu string is:
						 * "+arg.Menu); } if (k==act.NoLibArguments-1) { System.out.println("___________________\n"); }
						 * else { System.out.println("___________________"); }
						 */
						}
					else
						{
						in.skip(in.readi());// skip arg caption
						in.skip(4);// skip argument kind
						in.skip(in.readi());// skip Default value
						in.skip(in.readi());// skip Menu string
						}
					}
				act.execType = (byte) in.readi();
				// System.out.println("read in exec type: "+act.ExecType);
				act.execFunction = in.readStr();
				// System.out.println("read in exec function str: "+act.ExecFunction);
				act.execCode = in.readStr();
				// System.out.println("read in exec code: "+act.ExecCode);
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