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

import static org.lateralgm.main.Util.deRef;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
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
import org.lateralgm.main.Util;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.InstantiableResource;
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
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.util.PropertyMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

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
		fos = new FileOutputStream(new File(getUnixPath(filename)));
	
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
	
	// For some odd reason these two settings are fucked up; combined; and not even combined properly
	//2147483649 - Both
	//2147483648 - Software Vertex Processing only
	//1 - Synchronization Only
	//0 - None

	long syncvertex = 0;
	if (f.gameSettings.get(PGameSettings.USE_SYNCHRONIZATION)) {
		syncvertex += 1;
	}
	if (f.gameSettings.get(PGameSettings.FORCE_SOFTWARE_VERTEX_PROCESSING)) {
		syncvertex += 2147483648L;
	}
	optNode.appendChild(createElement(dom,
			"option_sync_vertex",Long.toString(syncvertex)));
	
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
	String guid = HexBin.encode((byte[]) f.gameSettings.get(PGameSettings.GAME_GUID));
	optNode.appendChild(createElement(dom,
			"option_gameguid", "{" + guid.substring(0, 8) + "-" + guid.substring(8, 12) + "-" +
			guid.substring(12, 16) + "-" + guid.substring(16, 20) + "-" + guid.substring(20, 32) + "}" ));
	
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
	File file = new File(getUnixPath(icoPath)).getParentFile();
	file.mkdirs();
	
	FileOutputStream fos = new FileOutputStream(getUnixPath(icoPath));
	((ICOFile) f.gameSettings.get(PGameSettings.GAME_ICON)).write(fos);
	fos.close();
	
	fos = null;
  try {
	  Transformer tr = TransformerFactory.newInstance().newTransformer();
	  tr.setOutputProperty(OutputKeys.INDENT, "yes");
	  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
	  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
		file = new File(getUnixPath(f.getDirectory() + "/Configs"));
		file.mkdir();
		
	  // send DOM to file
		fos = new FileOutputStream(getUnixPath(f.getDirectory() + "/Configs/Default.config.gmx"));
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
			File file = new File(getUnixPath(fname + "\\images"));
			file.mkdirs();
			
			Document doc = documentBuilder.newDocument();
			
			Element sprroot = doc.createElement("sprite");
			doc.appendChild(sprroot);
			
			sprroot.appendChild(createElement(doc, "xorig", 
					spr.get(PSprite.ORIGIN_X).toString()));
			sprroot.appendChild(createElement(doc, "yorigin", 
					spr.get(PSprite.ORIGIN_Y).toString()));
			sprroot.appendChild(createElement(doc, "colkind", 
					ProjectFile.SPRITE_MASK_CODE.get(spr.get(PSprite.SHAPE)).toString()));
			sprroot.appendChild(createElement(doc, "sepmasks", 
					boolToString((Boolean) spr.get(PSprite.SEPARATE_MASK))));
			sprroot.appendChild(createElement(doc, "bbox_left", 
					spr.get(PSprite.BB_LEFT).toString()));
			sprroot.appendChild(createElement(doc, "bbox_right", 
					spr.get(PSprite.BB_RIGHT).toString()));
			sprroot.appendChild(createElement(doc, "bbox_top", 
					spr.get(PSprite.BB_TOP).toString()));
			sprroot.appendChild(createElement(doc, "bbox_bottom", 
					spr.get(PSprite.BB_BOTTOM).toString()));
			sprroot.appendChild(createElement(doc, "bboxmode", 
					ProjectFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)).toString()));
			sprroot.appendChild(createElement(doc, "coltolerance", 
					spr.get(PSprite.ALPHA_TOLERANCE).toString()));
			
			Element frameroot = doc.createElement("frames");
			for (int j = 0; j < spr.subImages.size(); j++)
				{
					String framefname = "images\\" + spr.getName() + "_" + j + ".png";
					File outputfile = new File(getUnixPath(fname + framefname));
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
			  fos = new FileOutputStream(getUnixPath(fname + spr.getName() + ".sprite.gmx"));
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

	//This is used to obtain the primary node for a resource type.
	//TODO: This is rather ugly and doesn't allow multiple primary nodes.
	private static ResNode getPrimaryNode(ResNode first) {
		while (first.status != ResNode.STATUS_PRIMARY) 
			first = (ResNode) first.getParent();
		return first;
	}

	
	//This is used to stored booleans since GMX uses -1 and 0 and other times false and true
	private static String boolToString(boolean bool)
		{
		if (bool) { return "-1"; }
		else { return "0"; } 
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
	
		iterateSprites(c, getPrimaryNode(sprList.first().getNode()), node);
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
			iterateSounds(c, resNode, res);
			break;
		case ResNode.STATUS_GROUP:
			res = dom.createElement("sounds");
			res.setAttribute("name", resNode.getUserObject().toString());
			iterateSounds(c, resNode, res);
			break;
		case ResNode.STATUS_SECONDARY:
			Sound snd = (Sound) resNode.getRes().get();
			res = dom.createElement("sound");
			String fname = f.getDirectory() + "\\sound\\";
			res.setTextContent("sound\\" + snd.getName());
			File file = new File(getUnixPath(fname + "\\audio"));
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
			  fos = new FileOutputStream(getUnixPath(fname + resNode.getUserObject().toString() + ".sound.gmx"));
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
		iterateSounds(c, getPrimaryNode(sndList.first().getNode()), node);
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
			File file = new File(getUnixPath(fname + "\\images"));
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
			File outputfile = new File(getUnixPath(fname + "images\\" + bkg.getName() + ".png"));
			try
				{
				//TODO: Can't handle image with 0x0 dimensions
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
			  fos = new FileOutputStream(getUnixPath(fname + resNode.getUserObject().toString() + ".background.gmx"));
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
		
		iterateBackgrounds(c, getPrimaryNode(bkgList.first().getNode()), node);
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
				File file = new File(getUnixPath(f.getDirectory() + "/paths"));
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
				
				ResourceReference<Room> bgroom = path.get(PPath.BACKGROUND_ROOM);
				if (bgroom != null) {
					pathroot.appendChild(createElement(doc, "backroom", bgroom.get().getName()));
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
				  fos = new FileOutputStream(getUnixPath(fname + path.getName() + ".path.gmx"));
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

		iteratePaths(c, getPrimaryNode(pthList.first().getNode()), node);
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
				File file = new File(getUnixPath(f.getDirectory() + "/scripts"));
				file.mkdir();
				PrintWriter out = null;
				try
					{
					out = new PrintWriter(getUnixPath(f.getDirectory() + "/" + getUnixPath(fname)));
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

	iterateScripts(c, getPrimaryNode(scrList.first().getNode()), node);
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
			File file = new File(getUnixPath(f.getDirectory() + "/shaders"));
			file.mkdir();
			PrintWriter out = null;
			try
				{
				out = new PrintWriter(getUnixPath(f.getDirectory() + "/" + fname));
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
		iterateShaders(c, getPrimaryNode(shrList.first().getNode()), node);
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
			File file = new File(getUnixPath(fname));
			file.mkdirs();
			
			Document doc = documentBuilder.newDocument();
			
			Element fntroot = doc.createElement("font");
			doc.appendChild(fntroot);
			
			fntroot.appendChild(createElement(doc, "name", 
					fnt.get(PFont.FONT_NAME).toString()));
			fntroot.appendChild(createElement(doc, "size", 
					fnt.get(PFont.SIZE).toString()));
			fntroot.appendChild(createElement(doc, "bold", 
					boolToString((Boolean) fnt.get(PFont.BOLD))));
			fntroot.appendChild(createElement(doc, "italic", 
					boolToString((Boolean) fnt.get(PFont.ITALIC))));
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
			File outputfile = new File(getUnixPath(fname + fnt.getName() + ".png"));
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
			  fos = new FileOutputStream(getUnixPath(fname + fnt.getName() + ".font.gmx"));
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
		iterateFonts(c, getPrimaryNode(fntList.first().getNode()), node);
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
				File file = new File(getUnixPath(f.getDirectory() + "/timelines"));
				file.mkdir();
				
				Document doc = documentBuilder.newDocument();
				
				Element tmlroot = doc.createElement("timeline");
				doc.appendChild(tmlroot);

				for (Moment mom : timeline.moments)
				{
					Element entroot = doc.createElement("entry");
					tmlroot.appendChild(entroot);
					entroot.appendChild(createElement(doc, "step", Integer.toString(mom.stepNo)));
					Element evtroot = doc.createElement("event");
					entroot.appendChild(evtroot);
					writeActions(doc,evtroot,mom);
				}
				
				FileOutputStream fos = null;
			  try {
				  Transformer tr = TransformerFactory.newInstance().newTransformer();
				  tr.setOutputProperty(OutputKeys.INDENT, "yes");
				  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
				  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				
				  // send DOM to file
				  fos = new FileOutputStream(getUnixPath(fname + timeline.getName() + ".timeline.gmx"));
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

		iterateTimelines(c, getPrimaryNode(tmlList.first().getNode()), node);
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
				File file = new File(getUnixPath(f.getDirectory() + "/objects"));
				file.mkdir();
				
				Document doc = documentBuilder.newDocument();
				
				Element objroot = doc.createElement("object");
				doc.appendChild(objroot);
				
				ResourceReference<Sprite> spr =
						((ResourceReference<Sprite>)object.get(PGmObject.SPRITE));
				if (spr != null) {
					objroot.appendChild(createElement(doc, "spriteName", spr.get().getName()));
				} else {
					objroot.appendChild(createElement(doc, "spriteName", "<undefined>"));
				}
				objroot.appendChild(createElement(doc, "solid", 
						boolToString((Boolean)object.get(PGmObject.SOLID))));
				objroot.appendChild(createElement(doc, "visible", 
						boolToString((Boolean)object.get(PGmObject.VISIBLE))));
				objroot.appendChild(createElement(doc, "depth", 
						object.get(PGmObject.DEPTH).toString()));
				objroot.appendChild(createElement(doc, "persistent", 
						boolToString((Boolean)object.get(PGmObject.PERSISTENT))));
				spr = ((ResourceReference<Sprite>)object.get(PGmObject.MASK));
				if (spr != null) {
					objroot.appendChild(createElement(doc, "maskName", spr.get().getName()));
				} else {
					objroot.appendChild(createElement(doc, "maskName", "<undefined>"));
				}
				
				ResourceReference<GmObject> par = ((ResourceReference<GmObject>)object.get(PGmObject.PARENT));
				if (par != null) {
					objroot.appendChild(createElement(doc, "parentName", par.get().getName()));
				} else {
					objroot.appendChild(createElement(doc, "parentName", "<undefined>"));
				}
				
				Element evtroot = doc.createElement("events");
				objroot.appendChild(evtroot);
				for (int i = 0; i < object.mainEvents.size(); i++) {
					MainEvent me = object.mainEvents.get(i);
					for (int k = me.events.size(); k > 0; k--)
						{
						Event ev = me.events.get(k - 1);
						Element evtelement = doc.createElement("event");
						evtelement.setAttribute("eventtype",Integer.toString(ev.mainId));
						if (ev.mainId == MainEvent.EV_COLLISION) {
							ResourceReference<GmObject> other = ev.other;
							if (other != null) {
								evtelement.setAttribute("ename", other.get().getName());
							} else {
								evtelement.setAttribute("ename", "<undefined>");
							}
						} else {
							evtelement.setAttribute("enumb",Integer.toString(ev.id));
						}
						evtroot.appendChild(evtelement);
						writeActions(doc,evtelement,ev);
						}
				}
				
				FileOutputStream fos = null;
			  try {
				  Transformer tr = TransformerFactory.newInstance().newTransformer();
				  tr.setOutputProperty(OutputKeys.INDENT, "yes");
				  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
				  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				
				  // send DOM to file
				  fos = new FileOutputStream(getUnixPath(fname + object.getName() + ".object.gmx"));
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

		iterateGmObjects(c, getPrimaryNode(objList.first().getNode()), node);
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
				File file = new File(getUnixPath(f.getDirectory() + "/rooms"));
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
						boolToString((Boolean)room.get(PRoom.ISOMETRIC))));
				roomroot.appendChild(createElement(doc, "speed", 
						room.get(PRoom.SPEED).toString()));
				roomroot.appendChild(createElement(doc, "persistent", 
						boolToString((Boolean)room.get(PRoom.PERSISTENT))));
				roomroot.appendChild(createElement(doc, "colour", 
						Integer.toString(Util.getGmColor((Color)room.get(PRoom.BACKGROUND_COLOR)))));
				roomroot.appendChild(createElement(doc, "showcolour", 
						boolToString((Boolean)room.get(PRoom.DRAW_BACKGROUND_COLOR))));
				roomroot.appendChild(createElement(doc, "code", 
						room.get(PRoom.CREATION_CODE).toString()));
				roomroot.appendChild(createElement(doc, "enableViews", 
						boolToString((Boolean)room.get(PRoom.ENABLE_VIEWS))));
				//roomroot.appendChild(createElement(doc, "clearViewBackground", 
						//boolToString((Boolean)room.get(PRoom.clearViewBackground))));
				
				// Write the maker settings, or basically the settings of the editor.
				Element mkeroot = doc.createElement("makerSettings");
				mkeroot.appendChild(createElement(doc, "isSet", 
						boolToString((Boolean)room.get(PRoom.REMEMBER_WINDOW_SIZE))));
				mkeroot.appendChild(createElement(doc, "w", 
						room.get(PRoom.WIDTH).toString()));
				mkeroot.appendChild(createElement(doc, "h", 
						room.get(PRoom.HEIGHT).toString()));
				mkeroot.appendChild(createElement(doc, "showGrid",
						boolToString((Boolean)room.get(PRoom.SHOW_GRID))));
				mkeroot.appendChild(createElement(doc, "showObjects", 
						boolToString((Boolean)room.get(PRoom.SHOW_OBJECTS))));
				mkeroot.appendChild(createElement(doc, "showTiles", 
						boolToString((Boolean)room.get(PRoom.SHOW_TILES))));
				mkeroot.appendChild(createElement(doc, "showBackgrounds", 
						boolToString((Boolean)room.get(PRoom.SHOW_BACKGROUNDS))));
				mkeroot.appendChild(createElement(doc, "showForegrounds", 
						boolToString((Boolean)room.get(PRoom.SHOW_FOREGROUNDS))));
				mkeroot.appendChild(createElement(doc, "showViews", 
						boolToString((Boolean)room.get(PRoom.SHOW_VIEWS))));
				mkeroot.appendChild(createElement(doc, "deleteUnderlyingObj", 
						boolToString((Boolean)room.get(PRoom.DELETE_UNDERLYING_OBJECTS))));
				mkeroot.appendChild(createElement(doc, "deleteUnderlyingTiles", 
						boolToString((Boolean)room.get(PRoom.DELETE_UNDERLYING_TILES))));
				mkeroot.appendChild(createElement(doc, "page", 
						room.get(PRoom.CURRENT_TAB).toString()));
				mkeroot.appendChild(createElement(doc, "xoffset", 
						room.get(PRoom.SCROLL_BAR_X).toString()));
				mkeroot.appendChild(createElement(doc, "yoffset", 
						room.get(PRoom.SCROLL_BAR_Y).toString()));
				roomroot.appendChild(mkeroot);
				
				// Write Backgrounds
				Element backroot = doc.createElement("backgrounds");
				roomroot.appendChild(backroot);
				for (BackgroundDef back : room.backgroundDefs) {
					PropertyMap<PBackgroundDef> props = back.properties;
					Element bckelement = doc.createElement("background");
					backroot.appendChild(bckelement);
					
					bckelement.setAttribute("visible",boolToString((Boolean)props.get(PBackgroundDef.VISIBLE)));
					bckelement.setAttribute("foreground",boolToString((Boolean)props.get(PBackgroundDef.FOREGROUND)));
					ResourceReference<Background> br = ((ResourceReference<Background>) props.get(PBackgroundDef.BACKGROUND));
					if (br != null) { 
						bckelement.setAttribute("name", br.get().getName());
					} else {
						bckelement.setAttribute("name", "");
					}
					bckelement.setAttribute("x",Integer.toString((Integer)props.get(PBackgroundDef.X)));
					bckelement.setAttribute("y",Integer.toString((Integer)props.get(PBackgroundDef.Y)));
					bckelement.setAttribute("htiled",boolToString((Boolean)props.get(PBackgroundDef.TILE_HORIZ)));
					bckelement.setAttribute("vtiled",boolToString((Boolean)props.get(PBackgroundDef.TILE_VERT)));
					bckelement.setAttribute("hspeed",Integer.toString((Integer)props.get(PBackgroundDef.H_SPEED)));
					bckelement.setAttribute("vspeed",Integer.toString((Integer)props.get(PBackgroundDef.V_SPEED)));
					bckelement.setAttribute("stretch",boolToString((Boolean)props.get(PBackgroundDef.STRETCH)));
				}
				
				// Write Views
				Element viewroot = doc.createElement("views");
				roomroot.appendChild(viewroot);
				for (View view : room.views) {
					PropertyMap<PView> props = view.properties;
					Element vwelement = doc.createElement("view");
					viewroot.appendChild(vwelement);
					
					vwelement.setAttribute("visible",boolToString((Boolean)props.get(PView.VISIBLE)));
					ResourceReference<GmObject> or = ((ResourceReference<GmObject>) props.get(PInstance.OBJECT));
					if (or != null) { 
						vwelement.setAttribute("objName",or.get().getName());
					} else {
						vwelement.setAttribute("objName","<undefined>");
					}
					vwelement.setAttribute("xview",Integer.toString((Integer)props.get(PView.VIEW_X)));
					vwelement.setAttribute("yview",Integer.toString((Integer)props.get(PView.VIEW_Y)));
					vwelement.setAttribute("wview",Integer.toString((Integer)props.get(PView.VIEW_W)));
					vwelement.setAttribute("hview",Integer.toString((Integer)props.get(PView.VIEW_H)));
					vwelement.setAttribute("xport",Integer.toString((Integer)props.get(PView.PORT_X)));
					vwelement.setAttribute("yport",Integer.toString((Integer)props.get(PView.PORT_Y)));
					vwelement.setAttribute("wport",Integer.toString((Integer)props.get(PView.PORT_W)));
					vwelement.setAttribute("hport",Integer.toString((Integer)props.get(PView.PORT_H)));
					vwelement.setAttribute("hborder",Integer.toString((Integer)props.get(PView.BORDER_H)));
					vwelement.setAttribute("vborder",Integer.toString((Integer)props.get(PView.BORDER_V)));
					vwelement.setAttribute("hspeed",Integer.toString((Integer)props.get(PView.SPEED_H)));
					vwelement.setAttribute("vspeed",Integer.toString((Integer)props.get(PView.SPEED_V)));
				}
				
				// Write instances
				Element insroot = doc.createElement("instances");
				roomroot.appendChild(insroot);
				for (Instance in : room.instances) {
					Element inselement = doc.createElement("instance");
					insroot.appendChild(inselement);
					ResourceReference<GmObject> or = in.properties.get(PInstance.OBJECT);
					if (or != null) {
						inselement.setAttribute("objName",or.get().getName());
					} else {
						inselement.setAttribute("objName","<undefined>");
					}
					inselement.setAttribute("x",Integer.toString(in.getPosition().x));
					inselement.setAttribute("y",Integer.toString(in.getPosition().y));
					inselement.setAttribute("name","inst_");
					inselement.setAttribute("locked",boolToString(in.isLocked()));
					inselement.setAttribute("code",in.getCreationCode());
					inselement.setAttribute("scaleX","1");
					inselement.setAttribute("scaleY","1");
					inselement.setAttribute("colour","4294967295"); // default white
					inselement.setAttribute("rotation","0");
				}
				
				// Write Tiles
				Element tileroot = doc.createElement("tiles");
				roomroot.appendChild(tileroot);
				for (Tile tile : room.tiles) {
					PropertyMap<PTile> props = tile.properties;
					Element tileelement = doc.createElement("tile");
					tileroot.appendChild(tileelement);
					
					ResourceReference<Background> br = ((ResourceReference<Background>) props.get(PTile.BACKGROUND));
					if (br != null) { 
						tileelement.setAttribute("bgName", br.get().getName());
					} else {
						tileelement.setAttribute("bgName", "");
					}
					tileelement.setAttribute("x",Integer.toString((Integer)props.get(PTile.ROOM_X)));
					tileelement.setAttribute("y",Integer.toString((Integer)props.get(PTile.ROOM_Y)));
					tileelement.setAttribute("w",Integer.toString((Integer)props.get(PTile.WIDTH)));
					tileelement.setAttribute("h",Integer.toString((Integer)props.get(PTile.HEIGHT)));
					tileelement.setAttribute("xo",Integer.toString((Integer)props.get(PTile.BG_X)));
					tileelement.setAttribute("yo",Integer.toString((Integer)props.get(PTile.BG_Y)));
					tileelement.setAttribute("id",Integer.toString((Integer)props.get(PTile.ID)));
					tileelement.setAttribute("name","inst_");
					tileelement.setAttribute("depth",Integer.toString((Integer)props.get(PTile.DEPTH)));
					tileelement.setAttribute("locked",boolToString((Boolean)props.get(PTile.LOCKED)));
					tileelement.setAttribute("colour","4294967295"); //TODO: Use white until we add this property
					tileelement.setAttribute("scaleX","1");
					tileelement.setAttribute("scaleY","1");
				}
				
				FileOutputStream fos = null;
			  try {
				  Transformer tr = TransformerFactory.newInstance().newTransformer();
				  tr.setOutputProperty(OutputKeys.INDENT, "yes");
				  tr.setOutputProperty(OutputKeys.METHOD, "xml");;
				  tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				
				  // send DOM to file
				  fos = new FileOutputStream(getUnixPath(fname + room.getName() + ".room.gmx"));
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
		iterateRooms(c, getPrimaryNode(rmnList.first().getNode()), node);
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
	{
		Document dom = c.dom;
		ProjectFile f = c.f;
	
		Element helpNode = dom.createElement("help");
		Element rtfNode = dom.createElement("rtf");
		rtfNode.setTextContent("help.rtf");
		helpNode.appendChild(rtfNode);
		
		PrintWriter out = null;
		try
			{
			out = new PrintWriter(getUnixPath(f.getDirectory() + "/help.rtf"));
			out.println(f.gameInfo.properties.get(PGameInformation.TEXT));
			}
		catch (FileNotFoundException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		finally { 
			out.close();
		}
		
		root.appendChild(helpNode);
	}

	public static void writeActions(Document doc, Element root, ActionContainer container)
	{
		for (Action act : container.actions)
		{
			Element actelement = doc.createElement("action");
			root.appendChild(actelement);
			LibAction la = act.getLibAction();
			
			actelement.appendChild(createElement(doc, "libid", 
					Integer.toString(la.parent != null ? la.parent.id : la.parentId)));
			actelement.appendChild(createElement(doc, "id", 
					Integer.toString(la.id)));
			actelement.appendChild(createElement(doc, "kind", 
					Integer.toString(la.actionKind)));
			actelement.appendChild(createElement(doc, "userelative", 
					boolToString(la.allowRelative)));
			actelement.appendChild(createElement(doc, "useapplyto", 
					boolToString(la.canApplyTo)));
			actelement.appendChild(createElement(doc, "isquestion", 
					boolToString(la.question)));
			actelement.appendChild(createElement(doc, "exetype", 
					Integer.toString(la.execType)));
			String execinfo = "";
			if (la.execType == Action.EXEC_FUNCTION) {
				execinfo = la.execInfo;
			}
			actelement.appendChild(createElement(doc, "functionname", ""));
			execinfo = "";
			if (la.execType == Action.EXEC_CODE) {
				execinfo = la.execInfo;
			}
			actelement.appendChild(createElement(doc, "codestring", execinfo));
			
			ResourceReference<GmObject> at = act.getAppliesTo();
			if (at != null)
				{
				if (at == GmObject.OBJECT_OTHER)
					actelement.appendChild(createElement(doc, "whoName", "other"));
				else if (at == GmObject.OBJECT_SELF)
					actelement.appendChild(createElement(doc, "whoName", "self"));
				else
					actelement.appendChild(createElement(doc, "whoName", at.get().getName()));
				}
			else
				actelement.appendChild(createElement(doc, "whoName", "self"));
			
			actelement.appendChild(createElement(doc, "relative", 
					boolToString(act.isRelative())));
			actelement.appendChild(createElement(doc, "isnot", 
					boolToString(act.isNot())));
			
			// Now we write the arguments
			Element argsroot = doc.createElement("arguments");
		  actelement.appendChild(argsroot);
			
			List<Argument> args = act.getArguments();
			for (Argument arg : args)
			{
				Element argelement = doc.createElement("argument");
				argsroot.appendChild(argelement);
				
				argelement.appendChild(createElement(doc, "kind", 
						Integer.toString(arg.kind)));
				Class<? extends Resource<?,?>> kind = Argument.getResourceKind(arg.kind);
				if (kind != null && InstantiableResource.class.isAssignableFrom(kind)) {
					Resource<?,?> r = deRef((ResourceReference<?>) arg.getRes());
					String name = "<undefined>";
					if (r != null && r instanceof InstantiableResource<?,?>) {
						name = ((InstantiableResource<?,?>) r).getName();
					}
					argelement.appendChild(createElement(doc, Resource.kindNames.get(kind).toLowerCase(), 
							name));
				} else {
					argelement.appendChild(createElement(doc, "string", arg.getVal()));
				}
			}
		}
	}
	
}
