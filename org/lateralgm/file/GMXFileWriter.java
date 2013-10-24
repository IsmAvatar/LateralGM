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
import java.io.ByteArrayOutputStream;
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
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Shader.PShader;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.resources.GameSettings.PGameSettings;
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

	private static void WriteBinaryFile(String filename, byte[] data)
	{
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
		//create an object of FileOutputStream
		fos = new FileOutputStream(new File(filename));
	
		//create an object of BufferedOutputStream
		bos = new BufferedOutputStream(fos);
		bos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(LGM.frame,
			    "There was an issue opening a file output stream.",
			    "Write Error",
			    JOptionPane.ERROR_MESSAGE);
		} finally {
			try
				{
				bos.close();
				fos.close();
				}
			catch (IOException e)
				{
				e.printStackTrace();
				JOptionPane.showMessageDialog(LGM.frame,
				    "There was an issue closing a file output stream.",
				    "Write Error",
				    JOptionPane.ERROR_MESSAGE);
				}
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
		writeSettings(c, root);
		/*
		writeSprites(c, root);
		writeSounds(c, root);
		writeBackgrounds(c, root);
		writePaths(c, root);*/
		writeScripts(c, root);
		writeShaders(c, root);
		/*
		writeFonts(c, root);
		writeTimelines(c, root);
		writeGmObjects(c, root);
		writeRooms(c, root);
		writeIncludedFiles(c, root);
		writePackages(c, root);
		writeExtensions(c, root);*/
		writeGameInformation(c, root);
		
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
		    te.printStackTrace();
		    JOptionPane.showMessageDialog(LGM.frame,
				    "There was an issue saving the project.",
				    "Read Error",
				    JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			// close up the stream and release the lock on the file
			os.close();
		}
    return;
	}
	
	private static Element createElement(Document dom, String name, String value) {
		Element ret = dom.createElement(name);
		ret.setTextContent(value);
		return ret;
	}

	public static void writeSettings(ProjectFileContext c, Element root)
			throws IOException
	{
	Document dom = c.dom;
	ProjectFile f = c.f;

	Element conNode = dom.createElement("Configs");
	Element setNode = dom.createElement("Config");
	conNode.setAttribute("name","configs");
	setNode.setTextContent("Configs\\Default");
	conNode.appendChild(setNode);
	root.appendChild(conNode);
	
	dom = documentBuilder.newDocument();
	conNode = dom.createElement("Config");
	dom.appendChild(conNode);
	Element optNode = dom.createElement("Options");
	conNode.appendChild(optNode);
	
	optNode.appendChild(createElement(dom,
			"option_fullscreen",f.gameSettings.get(PGameSettings.START_FULLSCREEN).toString()));
	optNode.appendChild(createElement(dom,
			"option_sizeable",f.gameSettings.get(PGameSettings.ALLOW_WINDOW_RESIZE).toString()));
	optNode.appendChild(createElement(dom,
			"option_stayontop",f.gameSettings.get(PGameSettings.ALWAYS_ON_TOP).toString()));
	optNode.appendChild(createElement(dom,
			"option_aborterrors",f.gameSettings.get(PGameSettings.ABORT_ON_ERROR).toString()));
	
	optNode.appendChild(createElement(dom,
			"option_noscreensaver",(String)f.gameSettings.get(PGameSettings.DISABLE_SCREENSAVERS).toString()));
	optNode.appendChild(createElement(dom,
			"option_showcursor",(String)f.gameSettings.get(PGameSettings.DISPLAY_CURSOR).toString()));
	optNode.appendChild(createElement(dom,
			"option_displayerrors",(String)f.gameSettings.get(PGameSettings.DISPLAY_ERRORS).toString()));
	optNode.appendChild(createElement(dom,
			"option_noborder",(String)f.gameSettings.get(PGameSettings.DONT_DRAW_BORDER).toString()));
	optNode.appendChild(createElement(dom,
			"option_nobuttons",(String)f.gameSettings.get(PGameSettings.DONT_SHOW_BUTTONS).toString()));
	optNode.appendChild(createElement(dom,
			"option_argumenterrors",(String)f.gameSettings.get(PGameSettings.ERROR_ON_ARGS).toString()));
	optNode.appendChild(createElement(dom,
			"option_freeze",(String)f.gameSettings.get(PGameSettings.FREEZE_ON_LOSE_FOCUS).toString()));
	
	optNode.appendChild(createElement(dom,
			"option_colordepth",ProjectFile.GS_DEPTH_CODE.get(f.gameSettings.get(PGameSettings.COLOR_DEPTH)).toString()));
	
	optNode.appendChild(createElement(dom,
			"option_frequency",ProjectFile.GS_FREQ_CODE.get(f.gameSettings.get(PGameSettings.FREQUENCY)).toString()));
	optNode.appendChild(createElement(dom,
			"option_resolution",ProjectFile.GS_RESOL_CODE.get(f.gameSettings.get(PGameSettings.RESOLUTION)).toString()));
	optNode.appendChild(createElement(dom,
			"option_changeresolution",f.gameSettings.get(PGameSettings.SET_RESOLUTION).toString()));
	optNode.appendChild(createElement(dom,
			"option_priority",ProjectFile.GS_PRIORITY_CODE.get(f.gameSettings.get(PGameSettings.GAME_PRIORITY)).toString()));
	
	optNode.appendChild(createElement(dom,
			"option_closeesc",f.gameSettings.get(PGameSettings.LET_ESC_END_GAME).toString()));
	optNode.appendChild(createElement(dom,
			"option_interpolate",f.gameSettings.get(PGameSettings.INTERPOLATE).toString()));
	optNode.appendChild(createElement(dom,
			"option_scale",f.gameSettings.get(PGameSettings.SCALING).toString()));
	optNode.appendChild(createElement(dom,
			"option_closeesc",f.gameSettings.get(PGameSettings.TREAT_CLOSE_AS_ESCAPE).toString()));
	optNode.appendChild(createElement(dom,
			"option_lastchanged",f.gameSettings.get(PGameSettings.LAST_CHANGED).toString()));
	
	optNode.appendChild(createElement(dom,
			"option_gameid",f.gameSettings.get(PGameSettings.GAME_ID).toString()));
	optNode.appendChild(createElement(dom,
			"option_gameguid",f.gameSettings.get(PGameSettings.DPLAY_GUID).toString()));
	
	optNode.appendChild(createElement(dom,
			"option_author",(String)f.gameSettings.get(PGameSettings.AUTHOR)));
	optNode.appendChild(createElement(dom,
			"option_version_company",(String)f.gameSettings.get(PGameSettings.COMPANY)));
	optNode.appendChild(createElement(dom,
			"option_version_copyright",(String)f.gameSettings.get(PGameSettings.COPYRIGHT)));
	optNode.appendChild(createElement(dom,
			"option_version_description",(String)f.gameSettings.get(PGameSettings.DESCRIPTION)));
	optNode.appendChild(createElement(dom,
			"option_version_product",(String)f.gameSettings.get(PGameSettings.PRODUCT)));
	optNode.appendChild(createElement(dom,
			"option_version",f.gameSettings.get(PGameSettings.VERSION).toString()));
	optNode.appendChild(createElement(dom,
			"option_version_build",f.gameSettings.get(PGameSettings.VERSION_BUILD).toString()));
	optNode.appendChild(createElement(dom,
			"option_version_major",f.gameSettings.get(PGameSettings.VERSION_MAJOR).toString()));
	optNode.appendChild(createElement(dom,
			"option_version_minor",f.gameSettings.get(PGameSettings.VERSION_MINOR).toString()));
	optNode.appendChild(createElement(dom,
			"option_version_release",f.gameSettings.get(PGameSettings.VERSION_RELEASE).toString()));
	
	String icoPath = "Configs\\Default\\windows\\runner_icon.ico";
	optNode.appendChild(createElement(dom,
			"option_windows_game_icon",icoPath));
	
	icoPath = f.getDirectory() + "\\" + icoPath;
	File file = new File(icoPath).getParentFile();
	file.mkdirs();
	
	FileOutputStream fos = new FileOutputStream(icoPath);
	((ICOFile) f.gameSettings.get(PGameSettings.GAME_ICON)).write(fos);

  try {
	  Transformer tr = TransformerFactory.newInstance().newTransformer();
	  tr.setOutputProperty(OutputKeys.INDENT, "yes");
	  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
	  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
		file = new File(f.getDirectory() + "/Configs");
		file.mkdir();
		
	  // send DOM to file
	  tr.transform(new DOMSource(dom), 
	            new StreamResult(new FileOutputStream(f.getDirectory() + "/Configs/Default.config.gmx")));
	} catch (TransformerException te) {
	    System.out.println(te.getMessage());
	}
  return;
	}

	public static void writeTriggers(ProjectFile f, ResNode root, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeConstants(ProjectFile f, ResNode root, int ver) throws IOException
	{
		//TODO: Implement
	}

	public static void writeSounds(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeSprites(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeBackgrounds(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writePaths(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	private static void iterateScripts(ProjectFileContext c, ResNode root, Element node) {
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null) { return; }
		for (Object obj : children) {
			if (!(obj instanceof ResNode)) { continue; }
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
	ResNode scrRoot = (ResNode) scrList.getUnsafe(0).getNode().getParent(); 

	iterateScripts(c, scrRoot, node);
	}
	
	private static void iterateShaders(ProjectFileContext c, ResNode root, Element node) {
	ProjectFile f = c.f;
	Document dom = c.dom;
	Vector<ResNode> children = root.getChildren();
	if (children == null) { return; }
	for (Object obj : children) {
		if (!(obj instanceof ResNode)) { continue; }
		ResNode resNode = (ResNode) obj;
		Element res = null;
		switch (resNode.status) {
		case 1:
			res = dom.createElement("shaders");
			res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
			iterateShaders(c, resNode, res);
			break;
		case 2:
			res = dom.createElement("shaders");
			res.setAttribute("name", resNode.getUserObject().toString());
			iterateShaders(c, resNode, res);
			break;
		case 3:
			res = dom.createElement("shader"
					+ "");
			String fname = "shaders\\" + resNode.getUserObject().toString() + ".gml";
			res.setTextContent(fname);
			res.setAttribute("type",(String) resNode.getRes().get().properties.get(PShader.TYPE));
			File file = new File(f.getDirectory() + "/shaders");
			file.mkdir();
			PrintWriter out = null;
			try
				{
				out = new PrintWriter(f.getDirectory() + "/" + getUnixPath(fname));
				String code = resNode.getRes().get().properties.get(PShader.VERTEX)
						+ "\n//######################_==_YOYO_SHADER_MARKER_==_######################@~//\n" +
								resNode.getRes().get().properties.get(PShader.FRAGMENT);
				out.println(code);
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
	
	public static void writeShaders(ProjectFileContext c, Element root) throws IOException
	{
	Document dom = c.dom;
	
	Element node = dom.createElement("shaders");
	node.setAttribute("name","shaders");
	root.appendChild(node);
	
	ResourceList<Shader> shrList = c.f.resMap.getList(Shader.class);
	if (shrList.size() == 0) {
		return;
	}
	ResNode shrRoot = (ResNode) shrList.getUnsafe(0).getNode().getParent(); 

	iterateShaders(c, shrRoot, node);
	}

	public static void writeFonts(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeTimelines(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeGmObjects(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeRooms(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeIncludedFiles(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writePackages(ProjectFileContext c, Element root) throws IOException
	{
		//TODO: Implement
	}

	public static void writeGameInformation(ProjectFileContext c, Element root)
			throws IOException
	{
		Document dom = c.dom;
		ProjectFile f = c.f;
	
		Element helpNode = dom.createElement("help");
		Element rtfNode = dom.createElement("rtf");
		rtfNode.setTextContent("help.rtf");
		helpNode.appendChild(rtfNode);
		
		PrintWriter out = new PrintWriter(f.getDirectory() + "/help.rtf");
		out.println(f.gameInfo.properties.get(PGameInformation.TEXT));
		out.close();
		
		root.appendChild(helpNode);
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
