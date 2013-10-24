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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.Script.PScript;
import org.lateralgm.resources.sub.ActionContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class GMXFileWriter
	{
	
	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
      .newInstance();
	static DocumentBuilder documentBuilder;
	
	private GMXFileWriter()
		{
		}
	
	//Workaround for Parameter limit
	private static class ProjectFileContext
		{
		ProjectFile f;
		Document dom;

		public ProjectFileContext(ProjectFile f, Document d)
			{
			this.f = f;
			this.dom = d;
			}

		public ProjectFileContext copy()
			{
			return new ProjectFileContext(f,dom);
			}
		}

	public static String getUnixPath(String path) {
		return path.replace("\\","/");
	}
	
	public static void writeProjectFile(OutputStream os, ProjectFile f, ResNode rootRes, int ver)
			throws IOException
	{
		f.format = ProjectFile.FormatFlavor.getVersionFlavor(ver);
		long savetime = System.currentTimeMillis();
		
		Document dom = null;	
		try
			{
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			dom = documentBuilder.newDocument();
			}
		catch (ParserConfigurationException pce)
			{
			pce.printStackTrace();
			}
		
		ProjectFileContext c = new ProjectFileContext(f,dom);
		Element root = dom.createElement("assets");
		//TODO: Handle actual fuck loading here
		/*
		writeSprites(f, root);
		writeSounds(f, root);
		writeBackgrounds(f, root);
		writePaths(f, root);*/
		writeScripts(c, root);
		/*
		writeShaders(f, root);
		writeFonts(f, root);
		writeTimelines(f, root);
		writeGmObjects(f, root);
		writeRooms(f, root);
		writeIncludedFiles(f, root);
		writePackages(f, root);
		writeExtensions(f, root);
		writeGameInformation(f, root);
		writeSettings(f, root);
		*/
		
		dom.appendChild(root);
	
		// Now take the serialized XML data and format and write it to the actual file
    try {
    Transformer tr = TransformerFactory.newInstance().newTransformer();
    tr.setOutputProperty(OutputKeys.INDENT, "yes");
    tr.setOutputProperty(OutputKeys.METHOD, "xml");;
    tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    // send DOM to file
    tr.transform(new DOMSource(dom), 
                         new StreamResult(os));

    
		} catch (TransformerException te) {
		    System.out.println(te.getMessage());
		}
		finally
		{
			// close up the stream and release the lock on the file
			os.close();
		}
    return;
	}

	public static void writeSettings(ProjectFile f, ResNode root, int ver, long savetime)
			throws IOException
	{
		//TODO: Implement
	}

	public static void writeTriggers(ProjectFile f, ResNode root, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeConstants(ProjectFile f, ResNode root, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeSounds(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeSprites(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeBackgrounds(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writePaths(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	private static void iterateScripts(ProjectFileContext c, ResNode root, Element node) {
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null) { return; }
		for (Object obj : children) {
			if (obj instanceof ResNode) {
					ResNode resNode = (ResNode) obj;
					Element res = null;
					switch (resNode.status) {
					case 1:
						res = dom.createElement("scripts");
						res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
						iterateScripts(c, resNode, res);
						break;
					case 2:
						res = dom.createElement("scripts");
						res.setAttribute("name", resNode.getUserObject().toString());
						iterateScripts(c, resNode, res);
						break;
					case 3:
						res = dom.createElement("script");
						String fname = "scripts\\" + resNode.getUserObject().toString() + ".gml";
						res.setTextContent(fname);
						File file = new File(f.getDirectory() + "/scripts");
						file.mkdir();
						PrintWriter out = null;
						try
							{
							out = new PrintWriter(f.getDirectory() + "/" + getUnixPath(fname));
							out.println(resNode.getRes().get().properties.get(PScript.CODE));
							out.close();
							}
						catch (FileNotFoundException e)
							{
							// TODO Auto-generated catch block
							e.printStackTrace();
							}
						break;
					}
					node.appendChild(res);
				}
		}
	/*
	out.write4(f.resMap.getList(Script.class).lastId + 1);
	for (int i = 0; i <= f.resMap.getList(Script.class).lastId; i++)
		{
		Script scr = f.resMap.getList(Script.class).getUnsafe(i);
		if (scr != null)
			{
			out.writeStr(scr.getName());
			out.write4(ver);
			out.writeStr(scr.properties,PScript.CODE);
			}
		out.endDeflate();
		}*/
	}
	
	public static void writeScripts(ProjectFileContext c, Element root) throws IOException
	{
	Document dom = c.dom;
	
	Element node = dom.createElement("scripts");
	node.setAttribute("name","scripts");
	root.appendChild(node);
	
	ResourceList<Script> scrList = c.f.resMap.getList(Script.class);
	if (scrList.size() == 0) {
		return;
	}
	if (scrList.getUnsafe(0) == null) {
		JOptionPane.showMessageDialog(null,"wtf");
	}
	ResNode scrRoot = (ResNode) scrList.getUnsafe(0).getNode().getParent(); 

	iterateScripts(c, scrRoot, node);
	}

	public static void writeFonts(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeTimelines(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeGmObjects(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeRooms(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeIncludedFiles(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writePackages(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeGameInformation(ProjectFile f, GmStreamEncoder out, int ver)
			throws IOException
	{
		//TODO: Implement
	}

	public static void writeTree(GmStreamEncoder out, ResNode root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeActions(GmStreamEncoder out, ActionContainer container)
			throws IOException
	{
		//TODO: Implement
	}
}
