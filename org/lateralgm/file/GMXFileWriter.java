/**
* @file  GMXFileWriter.java
* @brief Class implementing a GMX file writer.
*
* @section License
*
* Copyright (C) 2013 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.file;

import java.io.IOException;
import java.io.OutputStream;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.resources.sub.ActionContainer;

public final class GMXFileWriter
	{
	private GMXFileWriter()
		{
		}

	//TODO: Fuck somebody write this shit
	public static void writeProjectFile(OutputStream os, ProjectFile f, ResNode root, int ver)
			throws IOException
		{
		
		}

	public static void writeSettings(ProjectFile f, GmStreamEncoder out, int ver, long savetime)
			throws IOException
		{
		
		}

	public static void writeTriggers(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeConstants(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeSounds(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeSprites(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
	
		}

	public static void writeBackgrounds(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writePaths(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeScripts(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeFonts(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeTimelines(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeGmObjects(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeRooms(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeIncludedFiles(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writePackages(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		
		}

	public static void writeGameInformation(ProjectFile f, GmStreamEncoder out, int ver)
			throws IOException
		{
		
		}

	public static void writeTree(GmStreamEncoder out, ResNode root) throws IOException
		{
		
		}

	public static void writeActions(GmStreamEncoder out, ActionContainer container)
			throws IOException
		{
		
		}
	}
