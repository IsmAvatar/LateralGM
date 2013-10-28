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

import java.awt.Color;
import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;
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
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Shader.PShader;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.Script.PScript;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.PathPoint;
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
		
		writeSprites(c, root);
		writeSounds(c, root); 
		writeBackgrounds(c, root);
		writePaths(c, root);
		writeScripts(c, root);
		writeShaders(c, root);
		writeFonts(c, root);
		writeTimelines(c, root);
		writeGmObjects(c, root);
		writeRooms(c, root);
		//writeIncludedFiles(c, root);
		//writePackages(c, root);
		//writeExtensions(c, root);
		writeGameInformation(c, root);
		
		dom.appendChild(root);
	
		// Now take the serialized XML data and format and write it to the actual file
    try {
    Transformer tr = TransformerFactory.newInstance().newTransformer();
    tr.setOutputProperty(OutputKeys.INDENT, "yes");
    tr.setOutputProperty(OutputKeys.METHOD, "xml");;
    tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

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
	fos.close();
	
	fos = null;
  try {
	  Transformer tr = TransformerFactory.newInstance().newTransformer();
	  tr.setOutputProperty(OutputKeys.INDENT, "yes");
	  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
	  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
		file = new File(f.getDirectory() + "/Configs");
		file.mkdir();
		
	  // send DOM to file
		fos = new FileOutputStream(f.getDirectory() + "/Configs/Default.config.gmx");
	  tr.transform(new DOMSource(dom), 
	            new StreamResult(fos));
	} catch (TransformerException te) {
	    System.out.println(te.getMessage());
	} finally {
		fos.close();
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
	
	private static void iterateSprites(ProjectFileContext c, ResNode root, Element node) {
	ProjectFile f = c.f;
	Document dom = c.dom;
	Vector<ResNode> children = root.getChildren();
	if (children == null) { return; }
	for (Object obj : children) {
		if (!(obj instanceof ResNode)) { continue; }
		ResNode resNode = (ResNode) obj;
		Element res = null;
		switch (resNode.status) {
		case ResNode.STATUS_PRIMARY:
			res = dom.createElement("sprites");
			res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
			iterateSprites(c, resNode, res);
			break;
		case ResNode.STATUS_GROUP:
			res = dom.createElement("sprites");
			res.setAttribute("name", resNode.getUserObject().toString());
			iterateSprites(c, resNode, res);
			break;
		case ResNode.STATUS_SECONDARY:
			Sprite spr = (Sprite) resNode.getRes().get();
			res = dom.createElement("sprite");
			String fname = f.getDirectory() + "\\sprites\\";
			res.setTextContent("sprites\\" + spr.getName());
			File file = new File(fname + "\\images");
			file.mkdirs();
			
			Document doc = documentBuilder.newDocument();
			
			Element sprroot = doc.createElement("sprite");
			doc.appendChild(sprroot);
			
			sprroot.appendChild(createElement(doc, "xorig", 
					spr.get(PSprite.ORIGIN_X).toString()));
			sprroot.appendChild(createElement(doc, "yorigin", 
					spr.get(PSprite.ORIGIN_Y).toString()));
			sprroot.appendChild(createElement(doc, "bbox_left", 
					spr.get(PSprite.BB_LEFT).toString()));
			sprroot.appendChild(createElement(doc, "bbox_right", 
					spr.get(PSprite.BB_RIGHT).toString()));
			sprroot.appendChild(createElement(doc, "bbox_top", 
					spr.get(PSprite.BB_TOP).toString()));
			sprroot.appendChild(createElement(doc, "bbox_bottom", 
					spr.get(PSprite.BB_BOTTOM).toString()));
			//TODO: Causin error
			sprroot.appendChild(createElement(doc, "bboxmode", 
					ProjectFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)).toString()));
			sprroot.appendChild(createElement(doc, "coltolerance", 
					spr.get(PSprite.ALPHA_TOLERANCE).toString()));
			
			Element frameroot = doc.createElement("frames");
			for (int j = 0; j < spr.subImages.size(); j++)
				{
					String framefname = "images\\" + spr.getName() + "_" + j + ".png";
					File outputfile = new File(fname + framefname);
					Element frameNode = createElement(doc, "frame", framefname);
					frameNode.setAttribute("index",Integer.toString(j));
					frameroot.appendChild(frameNode);
					BufferedImage sub = spr.subImages.get(j);
					try
						{
						ImageIO.write(sub, "png", outputfile);
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
				}
			sprroot.appendChild(frameroot);
			
			FileOutputStream fos = null;
		  try {
			  Transformer tr = TransformerFactory.newInstance().newTransformer();
			  tr.setOutputProperty(OutputKeys.INDENT, "yes");
			  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
			  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			  // send DOM to file
			  fos = new FileOutputStream(fname + spr.getName() + ".sprite.gmx");
			  tr.transform(new DOMSource(doc), 
			            new StreamResult(fos));
			} catch (TransformerException te) {
			   System.out.println(te.getMessage());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try
					{
					fos.close();
					}
				catch (IOException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			}
		  break;
		}
		node.appendChild(res);
		}
	}

	public static void writeSprites(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("sprites");
		node.setAttribute("name","sprites");
		root.appendChild(node);
		
		ResourceList<Sprite> sprList = c.f.resMap.getList(Sprite.class);
		if (sprList.size() == 0) {
			return;
		}
		ResNode sprRoot = (ResNode) sprList.getUnsafe(0).getNode().getParent(); 
	
		iterateSprites(c, sprRoot, node);
	}
	
	private static void iterateSounds(ProjectFileContext c, ResNode root, Element node) {
	ProjectFile f = c.f;
	Document dom = c.dom;
	Vector<ResNode> children = root.getChildren();
	if (children == null) { return; }
	for (Object obj : children) {
		if (!(obj instanceof ResNode)) { continue; }
		ResNode resNode = (ResNode) obj;
		Element res = null;
		switch (resNode.status) {
		case ResNode.STATUS_PRIMARY:
			res = dom.createElement("sounds");
			res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
			iterateBackgrounds(c, resNode, res);
			break;
		case ResNode.STATUS_GROUP:
			res = dom.createElement("sounds");
			res.setAttribute("name", resNode.getUserObject().toString());
			iterateBackgrounds(c, resNode, res);
			break;
		case ResNode.STATUS_SECONDARY:
			Sound snd = (Sound) resNode.getRes().get();
			res = dom.createElement("sound");
			String fname = f.getDirectory() + "\\sound\\";
			res.setTextContent("sound\\" + snd.getName());
			File file = new File(fname + "\\audio");
			file.mkdirs();
			
			Document doc = documentBuilder.newDocument();
			
			Element sndroot = doc.createElement("sound");
			doc.appendChild(sndroot);
			
			String ftype = snd.get(PSound.FILE_TYPE).toString();
			sndroot.appendChild(createElement(doc, "extension", ftype));
			sndroot.appendChild(createElement(doc, "origname", 
					snd.get(PSound.FILE_NAME).toString()));
			sndroot.appendChild(createElement(doc, "kind", 
					ProjectFile.SOUND_CODE.get(snd.get(PSound.KIND)).toString()));
			sndroot.appendChild(createElement(doc, "volume", 
					snd.get(PSound.VOLUME).toString()));
			sndroot.appendChild(createElement(doc, "pan", 
					snd.get(PSound.PAN).toString()));
			sndroot.appendChild(createElement(doc, "preload", 
					snd.get(PSound.PRELOAD).toString()));
			int effects = 0;
			int n = 1;
			for (PSound k : ProjectFile.SOUND_FX_FLAGS)
				{
				if (snd.get(k)) effects |= n;
				n <<= 1;
				}
			sndroot.appendChild(createElement(doc, "effects", Integer.toString(effects)));

			sndroot.appendChild(createElement(doc, "data", snd.getName() + ftype));
			WriteBinaryFile(fname + "audio\\" + snd.getName() + ftype, snd.data);
			
			FileOutputStream fos = null;
		  try {
			  Transformer tr = TransformerFactory.newInstance().newTransformer();
			  tr.setOutputProperty(OutputKeys.INDENT, "yes");
			  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
			  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			  // send DOM to file
			  fos = new FileOutputStream(fname + resNode.getUserObject().toString() + ".sound.gmx");
			  tr.transform(new DOMSource(doc), 
			            new StreamResult(fos));
			} catch (TransformerException te) {
			   System.out.println(te.getMessage());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try
					{
					fos.close();
					}
				catch (IOException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			}
		  break;
		}
		node.appendChild(res);
		}
	}

	public static void writeSounds(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("sounds");
		node.setAttribute("name","sound");
		root.appendChild(node);
		
		ResourceList<Sound> sndList = c.f.resMap.getList(Sound.class);
		if (sndList.size() == 0) {
			return;
		}
		ResNode sndRoot = (ResNode) sndList.getUnsafe(0).getNode().getParent(); 
	
		iterateSounds(c, sndRoot, node);
	}
	
	private static void iterateBackgrounds(ProjectFileContext c, ResNode root, Element node) {
	ProjectFile f = c.f;
	Document dom = c.dom;
	Vector<ResNode> children = root.getChildren();
	if (children == null) { return; }
	for (Object obj : children) {
		if (!(obj instanceof ResNode)) { continue; }
		ResNode resNode = (ResNode) obj;
		Element res = null;
		switch (resNode.status) {
		case ResNode.STATUS_PRIMARY:
			res = dom.createElement("backgrounds");
			res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
			iterateBackgrounds(c, resNode, res);
			break;
		case ResNode.STATUS_GROUP:
			res = dom.createElement("backgrounds");
			res.setAttribute("name", resNode.getUserObject().toString());
			iterateBackgrounds(c, resNode, res);
			break;
		case ResNode.STATUS_SECONDARY:
			Background bkg = (Background) resNode.getRes().get();
			res = dom.createElement("background");
			String fname = f.getDirectory() + "\\background\\";
			res.setTextContent("background\\" + bkg.getName());
			File file = new File(fname + "\\images");
			file.mkdirs();
			
			Document doc = documentBuilder.newDocument();
			
			Element bkgroot = doc.createElement("background");
			doc.appendChild(bkgroot);
			
			bkgroot.appendChild(createElement(doc, "istileset", 
					bkg.get(PBackground.USE_AS_TILESET).toString()));
			bkgroot.appendChild(createElement(doc, "tilewidth", 
					bkg.get(PBackground.TILE_WIDTH).toString()));
			bkgroot.appendChild(createElement(doc, "tileheight", 
					bkg.get(PBackground.TILE_HEIGHT).toString()));
			bkgroot.appendChild(createElement(doc, "tilexoff", 
					bkg.get(PBackground.H_OFFSET).toString()));
			bkgroot.appendChild(createElement(doc, "tileyoff", 
					bkg.get(PBackground.V_OFFSET).toString()));
			bkgroot.appendChild(createElement(doc, "tilehsep", 
					bkg.get(PBackground.H_SEP).toString()));
			bkgroot.appendChild(createElement(doc, "tilevsep", 
					bkg.get(PBackground.V_SEP).toString()));

			bkgroot.appendChild(createElement(doc, "data", 
					"images\\" + bkg.getName() + ".png"));
			File outputfile = new File(fname + "images\\" + bkg.getName() + ".png");
			try
				{
				ImageIO.write(bkg.getBackgroundImage(), "png", outputfile);
				}
			catch (IOException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			
			FileOutputStream fos = null;
		  try {
			  Transformer tr = TransformerFactory.newInstance().newTransformer();
			  tr.setOutputProperty(OutputKeys.INDENT, "yes");
			  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
			  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			  // send DOM to file
			  fos = new FileOutputStream(fname + resNode.getUserObject().toString() + ".background.gmx");
			  tr.transform(new DOMSource(doc), 
			            new StreamResult(fos));
			} catch (TransformerException te) {
			   System.out.println(te.getMessage());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try
					{
					fos.close();
					}
				catch (IOException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			}
		  break;
		}
		node.appendChild(res);
		}
	}

	public static void writeBackgrounds(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("backgrounds");
		node.setAttribute("name","background");
		root.appendChild(node);
		
		ResourceList<Background> bkgList = c.f.resMap.getList(Background.class);
		if (bkgList.size() == 0) {
			return;
		}
		ResNode bkgRoot = (ResNode) bkgList.getUnsafe(0).getNode().getParent(); 
	
		iterateBackgrounds(c, bkgRoot, node);
	}
	
	private static void iteratePaths(ProjectFileContext c, ResNode root, Element node) {
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null) { return; }
		for (Object obj : children) {
			if (!(obj instanceof ResNode)) { continue; }
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status) {
			case ResNode.STATUS_PRIMARY:
				res = dom.createElement("paths");
				res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
				iteratePaths(c, resNode, res);
				break;
			case ResNode.STATUS_GROUP:
				res = dom.createElement("paths");
				res.setAttribute("name", resNode.getUserObject().toString());
				iteratePaths(c, resNode, res);
				break;
			case ResNode.STATUS_SECONDARY:
				Path path = (Path) resNode.getRes().get();
				res = dom.createElement("path");
				String fname = f.getDirectory() + "\\paths\\";
				res.setTextContent("paths\\" + path.getName());
				File file = new File(f.getDirectory() + "/paths");
				file.mkdir();
				
				Document doc = documentBuilder.newDocument();
				
				Element pathroot = doc.createElement("path");
				doc.appendChild(pathroot);

				int kind = path.get(PPath.SMOOTH) ? 1 : 0;
				pathroot.appendChild(createElement(doc, "kind", Integer.toString(kind)));
				int closed = path.get(PPath.CLOSED) ? -1 : 0;
				pathroot.appendChild(createElement(doc, "closed", Integer.toString(closed)));
				pathroot.appendChild(createElement(doc, "precision", 
						path.get(PPath.PRECISION).toString()));
				
				Room bgroom = path.get(PPath.BACKGROUND_ROOM);
				if (bgroom != null) {
					pathroot.appendChild(createElement(doc, "backroom", bgroom.getName()));
				} else {
					pathroot.appendChild(createElement(doc, "backroom", "-1"));
				}
				
				pathroot.appendChild(createElement(doc, "hsnap", 
						path.get(PPath.SNAP_X).toString()));
				pathroot.appendChild(createElement(doc, "vsnap", 
						path.get(PPath.SNAP_Y).toString()));
				
				Element rootpoint = doc.createElement("points");
				pathroot.appendChild(rootpoint);
				for (PathPoint p : path.points)
				{
					rootpoint.appendChild(createElement(doc,"point",
							p.getX() + "," + p.getY() + "," + p.getSpeed()));
				}
				
				FileOutputStream fos = null;
			  try {
				  Transformer tr = TransformerFactory.newInstance().newTransformer();
				  tr.setOutputProperty(OutputKeys.INDENT, "yes");
				  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
				  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				
				  // send DOM to file
				  fos = new FileOutputStream(fname + path.getName() + ".path.gmx");
				  tr.transform(new DOMSource(doc), 
				            new StreamResult(fos));
				} catch (TransformerException te) {
				   System.out.println(te.getMessage());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try
						{
						fos.close();
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
				}
				break;
			}
			node.appendChild(res);
		}
	}

	public static void writePaths(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("paths");
		node.setAttribute("name","paths");
		root.appendChild(node);
		
		ResourceList<Path> pthList = c.f.resMap.getList(Path.class);
		if (pthList.size() == 0) {
			return;
		}
		ResNode pthRoot = (ResNode) pthList.getUnsafe(0).getNode().getParent(); 
	
		iteratePaths(c, pthRoot, node);
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
			case ResNode.STATUS_PRIMARY:
				res = dom.createElement("scripts");
				res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
				iterateScripts(c, resNode, res);
				break;
			case ResNode.STATUS_GROUP:
				res = dom.createElement("scripts");
				res.setAttribute("name", resNode.getUserObject().toString());
				iterateScripts(c, resNode, res);
				break;
			case ResNode.STATUS_SECONDARY:
				Script scr = (Script) resNode.getRes().get();
				res = dom.createElement("script");
				String fname = "scripts\\" + scr.getName() + ".gml";
				res.setTextContent(fname);
				File file = new File(f.getDirectory() + "/scripts");
				file.mkdir();
				PrintWriter out = null;
				try
					{
					out = new PrintWriter(f.getDirectory() + "/" + getUnixPath(fname));
					out.println(scr.properties.get(PScript.CODE));
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
		case ResNode.STATUS_PRIMARY:
			res = dom.createElement("shaders");
			res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
			iterateShaders(c, resNode, res);
			break;
		case ResNode.STATUS_GROUP:
			res = dom.createElement("shaders");
			res.setAttribute("name", resNode.getUserObject().toString());
			iterateShaders(c, resNode, res);
			break;
		case ResNode.STATUS_SECONDARY:
			Shader shr = (Shader) resNode.getRes().get();
			res = dom.createElement("shader"
					+ "");
			String fname = "shaders\\" + shr.getName() + ".gml";
			res.setTextContent(fname);
			res.setAttribute("type",shr.properties.get(PShader.TYPE).toString());
			File file = new File(f.getDirectory() + "/shaders");
			file.mkdir();
			PrintWriter out = null;
			try
				{
				out = new PrintWriter(f.getDirectory() + "/" + getUnixPath(fname));
				String code = shr.properties.get(PShader.VERTEX)
						+ "\n//######################_==_YOYO_SHADER_MARKER_==_######################@~//\n" +
								shr.properties.get(PShader.FRAGMENT);
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
	
	private static void iterateFonts(ProjectFileContext c, ResNode root, Element node) {
	ProjectFile f = c.f;
	Document dom = c.dom;
	Vector<ResNode> children = root.getChildren();
	if (children == null) { return; }
	for (Object obj : children) {
		if (!(obj instanceof ResNode)) { continue; }
		ResNode resNode = (ResNode) obj;
		Element res = null;
		switch (resNode.status) {
		case ResNode.STATUS_PRIMARY:
			res = dom.createElement("fonts");
			res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
			iterateFonts(c, resNode, res);
			break;
		case ResNode.STATUS_GROUP:
			res = dom.createElement("fonts");
			res.setAttribute("name", resNode.getUserObject().toString());
			iterateFonts(c, resNode, res);
			break;
		case ResNode.STATUS_SECONDARY:
			Font fnt = (Font) resNode.getRes().get();
			res = dom.createElement("font");
			String fname = f.getDirectory() + "\\fonts\\";
			res.setTextContent("fonts\\" + fnt.getName());
			File file = new File(fname);
			file.mkdirs();
			
			Document doc = documentBuilder.newDocument();
			
			Element fntroot = doc.createElement("font");
			doc.appendChild(fntroot);
			
			fntroot.appendChild(createElement(doc, "name", 
					fnt.get(PFont.FONT_NAME).toString()));
			fntroot.appendChild(createElement(doc, "size", 
					fnt.get(PFont.SIZE).toString()));
			fntroot.appendChild(createElement(doc, "bold", 
					fnt.get(PFont.BOLD).toString()));
			fntroot.appendChild(createElement(doc, "italic", 
					fnt.get(PFont.ITALIC).toString()));
			fntroot.appendChild(createElement(doc, "charset", 
					fnt.get(PFont.CHARSET).toString()));
			fntroot.appendChild(createElement(doc, "aa", 
					fnt.get(PFont.ANTIALIAS).toString()));
			
			//TODO: Implement multiple ranges
			Element rangeroot = doc.createElement("ranges");
			fntroot.appendChild(rangeroot);
			rangeroot.appendChild(createElement(doc, "range0", fnt.get(PFont.RANGE_MIN).toString()
					+ "," + fnt.get(PFont.RANGE_MAX).toString()));

			// TODO: Move glyph renderer from the plugin to LGM and write glyphs here
			fntroot.appendChild(createElement(doc, "image", 
					fnt.getName() + ".png"));
			File outputfile = new File(fname + fnt.getName() + ".png");
			/*
			try
				{
				ImageIO.write(fnt.getBackgroundImage(), "png", outputfile);
				}
			catch (IOException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			*/
			
			FileOutputStream fos = null;
		  try {
			  Transformer tr = TransformerFactory.newInstance().newTransformer();
			  tr.setOutputProperty(OutputKeys.INDENT, "yes");
			  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
			  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			  // send DOM to file
			  fos = new FileOutputStream(fname + resNode.getUserObject().toString() + ".font.gmx");
			  tr.transform(new DOMSource(doc), 
			            new StreamResult(fos));
			} catch (TransformerException te) {
			   System.out.println(te.getMessage());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try
					{
					fos.close();
					}
				catch (IOException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			}
		  break;
		}
		node.appendChild(res);
		}
	}

	public static void writeFonts(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("fonts");
		node.setAttribute("name","fonts");
		root.appendChild(node);
		
		ResourceList<Font> fntList = c.f.resMap.getList(Font.class);
		if (fntList.size() == 0) {
			return;
		}
		ResNode fntRoot = (ResNode) fntList.getUnsafe(0).getNode().getParent(); 
	
		iterateFonts(c, fntRoot, node);
	}
	
	private static void iterateTimelines(ProjectFileContext c, ResNode root, Element node) {
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null) { return; }
		for (Object obj : children) {
			if (!(obj instanceof ResNode)) { continue; }
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status) {
			case ResNode.STATUS_PRIMARY:
				res = dom.createElement("timelines");
				res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
				iterateTimelines(c, resNode, res);
				break;
			case ResNode.STATUS_GROUP:
				res = dom.createElement("timelines");
				res.setAttribute("name", resNode.getUserObject().toString());
				iterateTimelines(c, resNode, res);
				break;
			case ResNode.STATUS_SECONDARY:
				Timeline timeline = (Timeline) resNode.getRes().get();
				res = dom.createElement("timeline");
				String fname = f.getDirectory() + "\\timelines\\";
				res.setTextContent("timelines\\" + timeline.getName());
				File file = new File(f.getDirectory() + "/timelines");
				file.mkdir();
				
				Document doc = documentBuilder.newDocument();
				
				Element tmlroot = doc.createElement("timeline");
				doc.appendChild(tmlroot);

				//TODO: Write properties
				
				FileOutputStream fos = null;
			  try {
				  Transformer tr = TransformerFactory.newInstance().newTransformer();
				  tr.setOutputProperty(OutputKeys.INDENT, "yes");
				  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
				  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				
				  // send DOM to file
				  fos = new FileOutputStream(fname + timeline.getName() + ".timeline.gmx");
				  tr.transform(new DOMSource(doc), 
				            new StreamResult(fos));
				} catch (TransformerException te) {
				   System.out.println(te.getMessage());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try
						{
						fos.close();
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
				}
				break;
			}
			node.appendChild(res);
		}
	}

	public static void writeTimelines(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("timelines");
		node.setAttribute("name","timelines");
		root.appendChild(node);
		
		ResourceList<Timeline> tmlList = c.f.resMap.getList(Timeline.class);
		if (tmlList.size() == 0) {
			return;
		}
		ResNode tmlRoot = (ResNode) tmlList.getUnsafe(0).getNode().getParent(); 
	
		iterateTimelines(c, tmlRoot, node);
	}

	private static void iterateGmObjects(ProjectFileContext c, ResNode root, Element node) {
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null) { return; }
		for (Object obj : children) {
			if (!(obj instanceof ResNode)) { continue; }
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status) {
			case ResNode.STATUS_PRIMARY:
				res = dom.createElement("objects");
				res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
				iterateGmObjects(c, resNode, res);
				break;
			case ResNode.STATUS_GROUP:
				res = dom.createElement("objects");
				res.setAttribute("name", resNode.getUserObject().toString());
				iterateGmObjects(c, resNode, res);
				break;
			case ResNode.STATUS_SECONDARY:
				GmObject object = (GmObject) resNode.getRes().get();
				res = dom.createElement("object");
				String fname = f.getDirectory() + "\\objects\\";
				res.setTextContent("objects\\" + object.getName());
				File file = new File(f.getDirectory() + "/objects");
				file.mkdir();
				
				Document doc = documentBuilder.newDocument();
				
				Element objroot = doc.createElement("object");
				doc.appendChild(objroot);
				
				objroot.appendChild(createElement(doc, "spriteName", 
						object.get(PGmObject.SPRITE).toString()));
				objroot.appendChild(createElement(doc, "solid", 
						object.get(PGmObject.SOLID).toString()));
				objroot.appendChild(createElement(doc, "visible", 
						object.get(PGmObject.VISIBLE).toString()));
				objroot.appendChild(createElement(doc, "depth", 
						object.get(PGmObject.DEPTH).toString()));
				objroot.appendChild(createElement(doc, "persistent", 
						object.get(PGmObject.PERSISTENT).toString()));
				objroot.appendChild(createElement(doc, "maskName", 
						object.get(PGmObject.MASK).toString()));
				
				// TODO: Write actions
				
				FileOutputStream fos = null;
			  try {
				  Transformer tr = TransformerFactory.newInstance().newTransformer();
				  tr.setOutputProperty(OutputKeys.INDENT, "yes");
				  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
				  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				
				  // send DOM to file
				  fos = new FileOutputStream(fname + object.getName() + ".object.gmx");
				  tr.transform(new DOMSource(doc), 
				            new StreamResult(fos));
				} catch (TransformerException te) {
				   System.out.println(te.getMessage());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try
						{
						fos.close();
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
				}
				break;
			}
			node.appendChild(res);
		}
	}
	
	public static void writeGmObjects(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("objects");
		node.setAttribute("name","objects");
		root.appendChild(node);
		
		ResourceList<GmObject> objList = c.f.resMap.getList(GmObject.class);
		if (objList.size() == 0) {
			return;
		}
		ResNode objRoot = (ResNode) objList.getUnsafe(0).getNode().getParent(); 
	
		iterateGmObjects(c, objRoot, node);
	}
	
	private static void iterateRooms(ProjectFileContext c, ResNode root, Element node) {
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null) { return; }
		for (Object obj : children) {
			if (!(obj instanceof ResNode)) { continue; }
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status) {
			case ResNode.STATUS_PRIMARY:
				res = dom.createElement("rooms");
				res.setAttribute("name", resNode.getUserObject().toString().toLowerCase());
				iterateRooms(c, resNode, res);
				break;
			case ResNode.STATUS_GROUP:
				res = dom.createElement("rooms");
				res.setAttribute("name", resNode.getUserObject().toString());
				iterateRooms(c, resNode, res);
				break;
			case ResNode.STATUS_SECONDARY:
				Room room = (Room) resNode.getRes().get();
				res = dom.createElement("room");
				String fname = f.getDirectory() + "\\rooms\\";
				res.setTextContent("rooms\\" + room.getName());
				File file = new File(f.getDirectory() + "/rooms");
				file.mkdir();
				
				Document doc = documentBuilder.newDocument();
				
				Element roomroot = doc.createElement("room");
				doc.appendChild(roomroot);
				
				roomroot.appendChild(createElement(doc, "caption", 
						room.get(PRoom.CAPTION).toString()));
				roomroot.appendChild(createElement(doc, "width", 
						room.get(PRoom.WIDTH).toString()));
				roomroot.appendChild(createElement(doc, "height", 
						room.get(PRoom.HEIGHT).toString()));
				roomroot.appendChild(createElement(doc, "vsnap", 
						room.get(PRoom.SNAP_X).toString()));
				roomroot.appendChild(createElement(doc, "hsnap", 
						room.get(PRoom.SNAP_Y).toString()));
				roomroot.appendChild(createElement(doc, "isometric", 
						room.get(PRoom.ISOMETRIC).toString()));
				roomroot.appendChild(createElement(doc, "speed", 
						room.get(PRoom.SPEED).toString()));
				roomroot.appendChild(createElement(doc, "persistent", 
						room.get(PRoom.PERSISTENT).toString()));
				roomroot.appendChild(createElement(doc, "colour", 
						room.get(PRoom.BACKGROUND_COLOR).toString()));
				roomroot.appendChild(createElement(doc, "showcolour", 
						room.get(PRoom.DRAW_BACKGROUND_COLOR).toString()));
				roomroot.appendChild(createElement(doc, "code", 
						room.get(PRoom.CREATION_CODE).toString()));
				roomroot.appendChild(createElement(doc, "enableViews", 
						room.get(PRoom.ENABLE_VIEWS).toString()));
				roomroot.appendChild(createElement(doc, "clearViewBackground", 
						room.get(PRoom.CAPTION).toString()));
				
				// Write the maker settings, or basically the settings of the editor.
				Element mkeroot = doc.createElement("makerSettings");
				mkeroot.appendChild(createElement(doc, "isSet", 
						room.get(PRoom.REMEMBER_WINDOW_SIZE).toString()));
				mkeroot.appendChild(createElement(doc, "w", 
						room.get(PRoom.WIDTH).toString()));
				mkeroot.appendChild(createElement(doc, "h", 
						room.get(PRoom.HEIGHT).toString()));
				mkeroot.appendChild(createElement(doc, "showGrid",
						room.get(PRoom.SHOW_GRID).toString()));
				mkeroot.appendChild(createElement(doc, "showObjects", 
						room.get(PRoom.SHOW_OBJECTS).toString()));
				mkeroot.appendChild(createElement(doc, "showTiles", 
						room.get(PRoom.SHOW_TILES).toString()));
				mkeroot.appendChild(createElement(doc, "showBackgrounds", 
						room.get(PRoom.SHOW_BACKGROUNDS).toString()));
				mkeroot.appendChild(createElement(doc, "showForegrounds", 
						room.get(PRoom.SHOW_FOREGROUNDS).toString()));
				mkeroot.appendChild(createElement(doc, "showViews", 
						room.get(PRoom.SHOW_VIEWS).toString()));
				mkeroot.appendChild(createElement(doc, "deleteUnderlyingObj", 
						room.get(PRoom.DELETE_UNDERLYING_OBJECTS).toString()));
				mkeroot.appendChild(createElement(doc, "deleteUnderlyingTiles", 
						room.get(PRoom.DELETE_UNDERLYING_TILES).toString()));
				mkeroot.appendChild(createElement(doc, "page", 
						room.get(PRoom.CURRENT_TAB).toString()));
				mkeroot.appendChild(createElement(doc, "xoffset", 
						room.get(PRoom.SCROLL_BAR_X).toString()));
				mkeroot.appendChild(createElement(doc, "yoffset", 
						room.get(PRoom.SCROLL_BAR_Y).toString()));
				roomroot.appendChild(mkeroot);
				
				//TODO: Iterate Backgrounds
				
				//TODO: Iterate Views
				
				//TODO: Iterate Instances
				
				//TODO: Iterate Tiles
				
				FileOutputStream fos = null;
			  try {
				  Transformer tr = TransformerFactory.newInstance().newTransformer();
				  tr.setOutputProperty(OutputKeys.INDENT, "yes");
				  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
				  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				
				  // send DOM to file
				  fos = new FileOutputStream(fname + room.getName() + ".room.gmx");
				  tr.transform(new DOMSource(doc), 
				            new StreamResult(fos));
				} catch (TransformerException te) {
				   System.out.println(te.getMessage());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try
						{
						fos.close();
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
				}
				break;
			}
			node.appendChild(res);
		}
	}

	public static void writeRooms(ProjectFileContext c, Element root) throws IOException
	{
		Document dom = c.dom;
		
		Element node = dom.createElement("rooms");
		node.setAttribute("name","rooms");
		root.appendChild(node);
		
		ResourceList<Room> rmnList = c.f.resMap.getList(Room.class);
		if (rmnList.size() == 0) {
			return;
		}
		ResNode rmnRoot = (ResNode) rmnList.getUnsafe(0).getNode().getParent(); 
	
		iterateRooms(c, rmnRoot, node);
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

	public static void writeActions(GmStreamEncoder out, ActionContainer container)
			throws IOException
	{
		//TODO: Implement
	}
	
}
