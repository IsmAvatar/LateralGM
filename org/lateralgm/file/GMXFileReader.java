/**
* @file  GMXFileReader.java
* @brief Class implementing a GMX file reader.
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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.GmFileReader.PostponedRef;
import org.lateralgm.file.GmFileReader.ProjectFileContext;
import org.lateralgm.file.ProjectFile.ResourceHolder;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Extensions;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Shader.PShader;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.Script.PScript;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.util.PropertyMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class GMXFileReader
	{
	
	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
      .newInstance();
	static DocumentBuilder documentBuilder;
	
	private GMXFileReader()
		{
		}

	static Queue<PostponedRef> postpone = new LinkedList<PostponedRef>();

	static interface PostponedRef
		{
		boolean invoke();
		}

	static class DefaultPostponedRef<K extends Enum<K>> implements PostponedRef
		{
		ResourceList<?> list;
		String name;
		PropertyMap<K> p;
		K key;

		DefaultPostponedRef(ResourceList<?> list, PropertyMap<K> p, K key, String name)
			{
			this.list = list;
			this.p = p;
			this.key = key;
			this.name = name;
			}

		public boolean invoke()
			{
			Resource<?,?> temp = list.get(name);
			if (temp != null) p.put(key,temp.reference);
			return temp != null;
			}
		}

	//Workaround for Parameter limit
	private static class ProjectFileContext
		{
		ProjectFile f;
		Document in;
		RefList<Timeline> timeids;
		RefList<GmObject> objids;
		RefList<Room> rmids;

		public ProjectFileContext(ProjectFile f, Document d, RefList<Timeline> timeids,
				RefList<GmObject> objids, RefList<Room> rmids)
			{
			this.f = f;
			this.in = d;
			this.timeids = timeids;
			this.objids = objids;
			this.rmids = rmids;
			}

		public ProjectFileContext copy()
			{
			return new ProjectFileContext(f,in,timeids,objids,rmids);
			}
		}

	private static GmFormatException versionError(ProjectFile f, String error, String res, int ver)
		{
		return versionError(f,error,res,0,ver);
		}

	private static GmFormatException versionError(ProjectFile f, String error, String res, int i, int ver)
		{
		return new GmFormatException(f,Messages.format(
				"ProjectFileReader.ERROR_UNSUPPORTED",Messages.format(//$NON-NLS-1$
						"ProjectFileReader." + error,Messages.getString("LGM." + res),i),ver)); //$NON-NLS-1$  //$NON-NLS-2$
		}

	public static ProjectFile readProjectFile(InputStream stream, URI uri, ResNode root)
			throws GmFormatException
		{
		return readProjectFile(stream,uri,root,null);
		}
	
	public static String getUnixPath(String path) {
		return path.replace("\\","/");
	}
	
	public static ProjectFile readProjectFile(InputStream stream, URI uri, ResNode root, Charset forceCharset)
			throws GmFormatException
		{
		ProjectFile f = new ProjectFile();
		f.uri = uri;
		
		File file = new File(uri);
		Document document = null;	
		try
			{
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			try
				{
				document = documentBuilder.parse(file);
				}
			catch (SAXException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			catch (IOException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}
		catch (ParserConfigurationException e1)
			{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		
		RefList<Timeline> timeids = new RefList<Timeline>(Timeline.class); // timeline ids
		RefList<GmObject> objids = new RefList<GmObject>(GmObject.class); // object ids
		RefList<Room> rmids = new RefList<Room>(Room.class); // room id
		try
			{
			long startTime = System.currentTimeMillis();

			ProjectFileContext c = new ProjectFileContext(f,document,timeids,objids,rmids);
			int ver = 1110;
			
			readSprites(c, root);
			readSounds(c, root);
			readBackgrounds(c, root);
			readPaths(c, root);
			readScripts(c, root);
			readShaders(c, root);
			readFonts(c, root);
			readTimelines(c, root);
			readGmObjects(c, root);
			readRooms(c, root);
			readIncludedFiles(c, root);
			readPackages(c, root);
			readExtensions(c, root);
			readGameInformation(c, root);
			readSettings(c, root);
			
			//Resources read. Now we can invoke our postpones.
			for (PostponedRef i : postpone)
				i.invoke();
		
		}
		catch (Exception e)
			{
			e.printStackTrace();
			if ((e instanceof GmFormatException)) throw (GmFormatException) e;
			throw new GmFormatException(f,e);
			}
		finally
			{
			try
				{
					// close up any open file streams
				}
			catch (Exception ex) //IOException
				{
				String key = Messages.getString("ProjectFileReader.ERROR_CLOSEFAILED"); //$NON-NLS-1$
				throw new GmFormatException(f,key);
				}
			}
		return f;
		}

	private static void readSettings(ProjectFileContext c, ResNode root) throws IOException,GmFormatException,
			DataFormatException, SAXException
		{
		Document in = c.in;
		
		GameSettings gSet = c.f.gameSettings;
		
		NodeList rtfNodes = in.getElementsByTagName("Configs"); 
		Node rtfNode = null;
		for (int i = 0; i < rtfNodes.getLength(); i++) {
		  Node node = rtfNodes.item(i);
		  if (node.getAttributes().getNamedItem("name").getTextContent().equals("configs")) {
		  	rtfNode = node;
		  	break;
		  }
		}
		
		rtfNodes = rtfNode.getChildNodes(); 
		rtfNode = null;
		for (int i = 0; i < rtfNodes.getLength(); i++) {
		  Node node = rtfNodes.item(i);
		  
		  if (node.getNodeName().equals("Config")) {
		  	rtfNode = node;
		  	break;
		  }
		}
		
	  String path = c.f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(rtfNode.getTextContent());
		
		Document setdoc = documentBuilder.parse(path + ".config.gmx");
		
		gSet.put(PGameSettings.START_FULLSCREEN, Boolean.parseBoolean(setdoc.getElementsByTagName("option_fullscreen").item(0).getTextContent()));
		//gSet.put(PSprite.ORIGIN_Y, Integer.parseInt(setdoc.getElementsByTagName("yorigin").item(0).getTextContent()));
		//gameInfo.put(PGameInformation.TEXT, text);
		
		ResNode node = new ResNode("Global Game Settings", (byte)3, GameSettings.class, gSet.reference);
		root.add(node);
		}

	private static void readSettingsIncludes(ProjectFile f, GmStreamDecoder in) throws IOException
		{
		
		}

	private static void readTriggers(ProjectFileContext c) throws IOException,GmFormatException
		{
		
		}

	private static void readConstants(ProjectFileContext c) throws IOException,GmFormatException
		{
		
		}
	
	private static void iterateSounds(ProjectFileContext c, NodeList sndList, ResNode node) throws IOException,GmFormatException, ParserConfigurationException, SAXException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < sndList.getLength(); i++) {
	Node cNode = sndList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("sounds")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Sound.class, null);
		node.add(rnode);
	} else if (cname.equals("sound")) {
	  f.resMap.getList(Sound.class).lastId++;
	  Sound snd = f.resMap.getList(Sound.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  snd.setName(fileName);
	  snd.setNode(rnode);
	  rnode = new ResNode(snd.getName(), (byte)3, Sound.class, snd.reference);
	  node.add(rnode);
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document snddoc = documentBuilder.parse(path + ".sound.gmx");
		
		snd.put(PSound.FILE_NAME, snddoc.getElementsByTagName("data").item(0).getTextContent());
		// TODO: The fuckin, god damn Studio has the volume tag nested inside itself in
		// some versions of their gay ass format
		NodeList nl = snddoc.getElementsByTagName("volume");
		snd.put(PSound.VOLUME, Double.parseDouble(nl.item(nl.getLength() - 1).getTextContent()));
		snd.put(PSound.PAN, Double.parseDouble(snddoc.getElementsByTagName("pan").item(0).getTextContent()));
		snd.put(PSound.PRELOAD, Boolean.parseBoolean(snddoc.getElementsByTagName("preload").item(0).getTextContent()));
		int sndkind =  Integer.parseInt(snddoc.getElementsByTagName("kind").item(0).getTextContent());
		snd.put(PSound.KIND, ProjectFile.SOUND_KIND[sndkind]);
		snd.put(PSound.FILE_TYPE, snddoc.getElementsByTagName("extension").item(0).getTextContent());
		String fname = snddoc.getElementsByTagName("data").item(0).getTextContent();
		snd.put(PSound.FILE_NAME, fname);
		
	  path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + "/sound/audio/" + fname;
	  
	  File file = new File(path);
	  byte [] fileData = new byte[(int)file.length()];
	  DataInputStream dis = new DataInputStream(new FileInputStream(file));
	  dis.readFully(fileData);
	  dis.close();
	  snd.data = fileData;
	}
	iterateSounds(c, cNode.getChildNodes(), rnode);
	}
	}

	private static void readSounds(ProjectFileContext c, ResNode root) throws IOException,GmFormatException,
			DataFormatException, ParserConfigurationException, SAXException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Sounds", (byte)1, Sound.class, null);
		root.add(node);
		
		NodeList sndList = in.getElementsByTagName("sounds"); 
		if (sndList.getLength() > 0) {
		  sndList = sndList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateSounds(c, sndList, node);
		}
	
	private static void iterateSprites(ProjectFileContext c, NodeList sprList, ResNode node) throws IOException,GmFormatException, ParserConfigurationException, SAXException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < sprList.getLength(); i++) {
	Node cNode = sprList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("sprites")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Sprite.class, null);
		node.add(rnode);
	} else if (cname.equals("sprite")) {
	  f.resMap.getList(Sprite.class).lastId++;
	  Sprite spr = f.resMap.getList(Sprite.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  spr.setName(fileName);
	  spr.setNode(rnode);
	  rnode = new ResNode(spr.getName(), (byte)3, Sprite.class, spr.reference);
	  node.add(rnode);
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document sprdoc = documentBuilder.parse(path + ".sprite.gmx");
		
		spr.put(PSprite.ORIGIN_X, Integer.parseInt(sprdoc.getElementsByTagName("xorig").item(0).getTextContent()));
		spr.put(PSprite.ORIGIN_Y, Integer.parseInt(sprdoc.getElementsByTagName("yorigin").item(0).getTextContent()));
		//TODO: Our mode is not equal to their integer modes for bbox, this errors
		//spr.put(PSprite.BB_MODE, Integer.parseInt(sprdoc.getElementsByTagName("bboxmode").item(0).getTextContent()));
		spr.put(PSprite.BB_LEFT, Integer.parseInt(sprdoc.getElementsByTagName("bbox_left").item(0).getTextContent()));
		spr.put(PSprite.BB_RIGHT, Integer.parseInt(sprdoc.getElementsByTagName("bbox_right").item(0).getTextContent()));
		spr.put(PSprite.BB_TOP, Integer.parseInt(sprdoc.getElementsByTagName("bbox_top").item(0).getTextContent()));
		spr.put(PSprite.BB_BOTTOM, Integer.parseInt(sprdoc.getElementsByTagName("bbox_bottom").item(0).getTextContent()));
		spr.put(PSprite.ALPHA_TOLERANCE, Integer.parseInt(sprdoc.getElementsByTagName("coltolerance").item(0).getTextContent()));

		//TODO: Just extra shit stored in the GMX by studio
		int width = Integer.parseInt(sprdoc.getElementsByTagName("width").item(0).getTextContent());
		int height = Integer.parseInt(sprdoc.getElementsByTagName("height").item(0).getTextContent());

		
	  // iterate and load the sprites subimages
		NodeList frList = sprdoc.getElementsByTagName("frame"); 
	  path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + "/sprites/";
		for (int ii = 0; ii < frList.getLength(); ii++) {
		  Node fnode = frList.item(ii);
		  BufferedImage img = null;
		  img = ImageIO.read(new File(path + getUnixPath(fnode.getTextContent())));
		  spr.subImages.add(img);
		}
	}
	iterateSprites(c, cNode.getChildNodes(), rnode);
	}
	}

	private static void readSprites(ProjectFileContext c, ResNode root) throws IOException,GmFormatException,
			DataFormatException, ParserConfigurationException, SAXException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Sprites", (byte)1, Sprite.class, null);
		root.add(node);
		
		NodeList sprList = in.getElementsByTagName("sprites"); 
		if (sprList.getLength() > 0) {
		  sprList = sprList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateSprites(c, sprList, node);
		}

	private static void iterateBackgrounds(ProjectFileContext c, NodeList bkgList, ResNode node) throws IOException,GmFormatException, ParserConfigurationException, SAXException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < bkgList.getLength(); i++) {
	Node cNode = bkgList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("backgrounds")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Background.class, null);
		node.add(rnode);
	} else if (cname.equals("background")) {
	  f.resMap.getList(Background.class).lastId++;
	  Background bkg = f.resMap.getList(Background.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  bkg.setName(fileName);
	  bkg.setNode(rnode);
	  rnode = new ResNode(bkg.getName(), (byte)3, Background.class, bkg.reference);
	  node.add(rnode);
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document bkgdoc = documentBuilder.parse(path + ".background.gmx");
		
		bkg.put(PBackground.USE_AS_TILESET, Boolean.parseBoolean(bkgdoc.getElementsByTagName("istileset").item(0).getTextContent()));
		bkg.put(PBackground.TILE_WIDTH, Integer.parseInt(bkgdoc.getElementsByTagName("tilewidth").item(0).getTextContent()));
		bkg.put(PBackground.TILE_HEIGHT, Integer.parseInt(bkgdoc.getElementsByTagName("tileheight").item(0).getTextContent()));
		bkg.put(PBackground.H_OFFSET, Integer.parseInt(bkgdoc.getElementsByTagName("tilexoff").item(0).getTextContent()));
		bkg.put(PBackground.V_OFFSET, Integer.parseInt(bkgdoc.getElementsByTagName("tileyoff").item(0).getTextContent()));
		bkg.put(PBackground.H_SEP, Integer.parseInt(bkgdoc.getElementsByTagName("tilehsep").item(0).getTextContent()));
		bkg.put(PBackground.V_SEP, Integer.parseInt(bkgdoc.getElementsByTagName("tilevsep").item(0).getTextContent()));

		//TODO: Just extra shit stored in the GMX by studio
		int width = Integer.parseInt(bkgdoc.getElementsByTagName("width").item(0).getTextContent());
		int height = Integer.parseInt(bkgdoc.getElementsByTagName("height").item(0).getTextContent());

	  path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + "/background/";
		Node fnode = bkgdoc.getElementsByTagName("data").item(0);
		BufferedImage img = null;
		img = ImageIO.read(new File(path + getUnixPath(fnode.getTextContent())));
		bkg.setBackgroundImage(img);
	}
	iterateSprites(c, cNode.getChildNodes(), rnode);
	}
	}
	
	private static void readBackgrounds(ProjectFileContext c, ResNode root) throws IOException,GmFormatException,
			DataFormatException, ParserConfigurationException, SAXException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Backgrounds", (byte)1, Background.class, null);
		root.add(node);
		
		NodeList bkgList = in.getElementsByTagName("backgrounds"); 
		if (bkgList.getLength() > 0) {
		  bkgList = bkgList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateBackgrounds(c, bkgList, node);
		}

	private static void iteratePaths(ProjectFileContext c, NodeList pthList, ResNode node) throws IOException,GmFormatException, SAXException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < pthList.getLength(); i++) {
	Node cNode = pthList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("paths")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Path.class, null);
		node.add(rnode);
	} else if (cname.equals("path")){
	  f.resMap.getList(Path.class).lastId++;
	  Path pth = f.resMap.getList(Path.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  pth.setName(fileName);
	  pth.setNode(rnode);
	  rnode = new ResNode(pth.getName(), (byte)3, Path.class, pth.reference);
	  node.add(rnode);
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document pthdoc = documentBuilder.parse(path + ".path.gmx");
		//pth.put(PPath.PRECISION, pthdoc.getElementsByTagName("name").item(0).getTextContent());
		pth.put(PPath.PRECISION, Integer.parseInt(pthdoc.getElementsByTagName("precision").item(0).getTextContent()));
	  pth.put(PPath.CLOSED, Integer.parseInt(pthdoc.getElementsByTagName("closed").item(0).getTextContent()) < 0);
	  pth.put(PPath.BACKGROUND_ROOM, c.rmids.get(Integer.parseInt(pthdoc.getElementsByTagName("backroom").item(0).getTextContent())));
	  pth.put(PPath.SNAP_X, Integer.parseInt(pthdoc.getElementsByTagName("hsnap").item(0).getTextContent()));
	  pth.put(PPath.SNAP_Y, Integer.parseInt(pthdoc.getElementsByTagName("vsnap").item(0).getTextContent()));
	
	  // iterate and add each path point
		NodeList frList = pthdoc.getElementsByTagName("point"); 
		for (int ii = 0; ii < frList.getLength(); ii++) {
		  Node fnode = frList.item(ii);
		  String[] coords = fnode.getTextContent().split(",");
		  pth.points.add(new PathPoint(Integer.parseInt(coords[0]), 
		  		Integer.parseInt(coords[1]), Integer.parseInt(coords[2])));
		}
	}
	
	iteratePaths(c, cNode.getChildNodes(), rnode);
	}
	
	}
	
	private static void readPaths(ProjectFileContext c, ResNode root) throws IOException,GmFormatException, SAXException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Paths", (byte)1, Sprite.class, null);
		root.add(node);
		
		NodeList pthList = in.getElementsByTagName("paths"); 
		if (pthList.getLength() > 0) {
		  pthList = pthList.item(0).getChildNodes();
		} else {
			return;
		}
		iteratePaths(c, pthList, node);
		}
	
	private static void iterateScripts(ProjectFileContext c, NodeList scrList, ResNode node) throws IOException,GmFormatException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < scrList.getLength(); i++) {
	Node cNode = scrList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("scripts")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Script.class, null);
		node.add(rnode);
	} else if (cname.equals("script")){
	  f.resMap.getList(Script.class).lastId++;
	  Script scr = f.resMap.getList(Script.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  scr.setName(fileName.substring(0, fileName.lastIndexOf(".")));
	  scr.setNode(rnode);
	  rnode = new ResNode(scr.getName(), (byte)3, Script.class, scr.reference);
	  node.add(rnode);
	  String code = "";
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  FileInputStream ins = new FileInputStream(path);
    try {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
    	String line = "";
      while ((line = reader.readLine()) != null) {
          code += line + "\n";
      }
    } finally {
        ins.close();
    }
	  scr.put(PScript.CODE, code);
	}
	iterateScripts(c, cNode.getChildNodes(), rnode);
	}
	
	}
	
	private static void readScripts(ProjectFileContext c, ResNode root) throws IOException,GmFormatException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Scripts", (byte)1, Script.class, null);
		root.add(node);
		
		NodeList scrList = in.getElementsByTagName("scripts"); 
		if (scrList.getLength() > 0) {
		  scrList = scrList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateScripts(c, scrList, node);
		}
	
	private static void iterateShaders(ProjectFileContext c, NodeList shrList, ResNode node) throws IOException,GmFormatException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < shrList.getLength(); i++) {
	Node cNode = shrList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("shaders")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Shader.class, null);
		node.add(rnode);
	} else if (cname.equals("shader")){
	  f.resMap.getList(Script.class).lastId++;
	  Shader shr = f.resMap.getList(Shader.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  shr.setName(fileName.substring(0, fileName.lastIndexOf(".")));
	  shr.setNode(rnode);
	  rnode = new ResNode(shr.getName(), (byte)3, Shader.class, shr.reference);
	  node.add(rnode);
	  shr.put(PShader.TYPE, cNode.getAttributes().item(0).getTextContent());
	  String code = "";
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  FileInputStream ins = new FileInputStream(path);
    try {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
    	String line = "";
      while ((line = reader.readLine()) != null) {
          code += line + "\n";
      }
    } finally {
        ins.close();
    }
    String[] splitcode = code.split("//######################_==_YOYO_SHADER_MARKER_==_######################@~//");
	  shr.put(PShader.VERTEX, splitcode[0]);
	  shr.put(PShader.FRAGMENT, splitcode[1]);
	}
	iterateScripts(c, cNode.getChildNodes(), rnode);
	}
	
	}
	
	
	private static void readShaders(ProjectFileContext c, ResNode root) throws IOException,GmFormatException
	{
	Document in = c.in;
	
	ResNode node = new ResNode("Shaders", (byte)1, Shader.class, null);
	root.add(node);
	
	NodeList shrList = in.getElementsByTagName("shaders"); 
	if (shrList.getLength() > 0) {
	  shrList = shrList.item(0).getChildNodes();
	} else {
		return;
	}
	iterateShaders(c, shrList, node);
	}
	
	private static void iterateFonts(ProjectFileContext c, NodeList fntList, ResNode node) throws IOException,GmFormatException, SAXException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < fntList.getLength(); i++) {
	Node cNode = fntList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("fonts")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Font.class, null);
		node.add(rnode);
	} else if (cname.equals("font")){
	  f.resMap.getList(Font.class).lastId++;
	  Font fnt = f.resMap.getList(Font.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  fnt.setName(fileName);
	  fnt.setNode(rnode);
	  rnode = new ResNode(fnt.getName(), (byte)3, Font.class, fnt.reference);
	  node.add(rnode);
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document fntdoc = documentBuilder.parse(path + ".font.gmx");
		fnt.put(PFont.FONT_NAME, fntdoc.getElementsByTagName("name").item(0).getTextContent());
		fnt.put(PFont.SIZE, Integer.parseInt(fntdoc.getElementsByTagName("size").item(0).getTextContent()));
		fnt.put(PFont.BOLD, Integer.parseInt(fntdoc.getElementsByTagName("bold").item(0).getTextContent()) < 0);
		fnt.put(PFont.ITALIC, Integer.parseInt(fntdoc.getElementsByTagName("italic").item(0).getTextContent()) < 0);
		fnt.put(PFont.CHARSET, Integer.parseInt(fntdoc.getElementsByTagName("charset").item(0).getTextContent()));
		fnt.put(PFont.ANTIALIAS, Integer.parseInt(fntdoc.getElementsByTagName("aa").item(0).getTextContent()));
		String range = fntdoc.getElementsByTagName("range0").item(0).getTextContent();
		fnt.put(PFont.RANGE_MIN, Integer.parseInt(range.substring(0, range.indexOf(','))));
		fnt.put(PFont.RANGE_MAX, Integer.parseInt(range.substring(range.indexOf(',') + 1, range.length() - range.indexOf(',') + 1)));
	}
	iterateFonts(c, cNode.getChildNodes(), rnode);
	}
	
	}

	private static void readFonts(ProjectFileContext c, ResNode root) throws IOException,GmFormatException, SAXException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Fonts", (byte)1, Font.class, null);
		root.add(node);
		
		NodeList fntList = in.getElementsByTagName("fonts"); 
		if (fntList.getLength() > 0) {
		  fntList = fntList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateFonts(c, fntList, node);
		}
	
	private static void iterateTimelines(ProjectFileContext c, NodeList tmlList, ResNode node) throws IOException,GmFormatException, SAXException
	{
	ProjectFile f = c.f;
	
	for (int i = 0; i < tmlList.getLength(); i++) {
	Node cNode = tmlList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("timelines")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Timeline.class, null);
		node.add(rnode);
	} else if (cname.equals("timeline")){
	  f.resMap.getList(Timeline.class).lastId++;
	  Timeline tml = f.resMap.getList(Timeline.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  tml.setName(fileName);
	  tml.setNode(rnode);
	  rnode = new ResNode(tml.getName(), (byte)3, Timeline.class, tml.reference);
	  node.add(rnode);
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document tmldoc = documentBuilder.parse(path + ".timeline.gmx");

		//iterate the events and load the actions
		NodeList frList = tmldoc.getElementsByTagName("entry"); 
		for (int ii = 0; ii < frList.getLength(); ii++) {
		  Node fnode = frList.item(ii);
		  Moment mom = tml.addMoment();
			
		  NodeList children = fnode.getChildNodes();
		  for (int x = 0; x < children.getLength(); x++) {
		  	Node cnode = children.item(x);
		  	if (cnode.getNodeName().equals("#text")) { 
		  	  continue; 
		    } else if (cnode.getNodeName().equals("step")) {
		  	  mom.stepNo = Integer.parseInt(cnode.getTextContent());
		  	} else if (cnode.getNodeName().equals("event")) {
		  	  readActions(c,mom,"INTIMELINEACTION", i, mom.stepNo, cnode.getChildNodes()); //$NON-NLS-1$
		  	}
		  }
		}
	}
	iterateTimelines(c, cNode.getChildNodes(), rnode);
	}
	
	}

	private static void readTimelines(ProjectFileContext c, ResNode root) throws IOException,GmFormatException, SAXException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Timelines", (byte)1, Timeline.class, null);
		root.add(node);
		
		NodeList tmlList = in.getElementsByTagName("timelines"); 
		if (tmlList.getLength() > 0) {
		  tmlList = tmlList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateTimelines(c, tmlList, node);
		}

	private static void iterateGmObjects(ProjectFileContext c, NodeList objList, ResNode node) throws IOException,GmFormatException, SAXException
	{
	final ProjectFile f = c.f;
	
	for (int i = 0; i < objList.getLength(); i++) {
	Node cNode = objList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("objects")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, GmObject.class, null);
		node.add(rnode);
	} else if (cname.equals("object")){
		ResourceReference<GmObject> r = c.objids.get(i); //includes ID
		final GmObject obj = r.get();
		f.resMap.getList(GmObject.class).add(obj);
	  f.resMap.getList(GmObject.class).lastId++;
	  
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  obj.setName(fileName);
	  obj.setNode(rnode);

	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document objdoc = documentBuilder.parse(path + ".object.gmx");

		final String sprname = objdoc.getElementsByTagName("spriteName").item(0).getTextContent();
		if (!sprname.equals("<undefined>")) {
			PostponedRef pr = new PostponedRef()
			{
				public boolean invoke()
				{
					ResourceList<Sprite> list = f.resMap.getList(Sprite.class);
					if (list == null) {	return false; }						
					Sprite spr = list.get(sprname);
					if (spr == null) { return false; }
					obj.put(PGmObject.SPRITE, spr.reference);
					return true;
				}
			};
			postpone.add(pr);
		} else {
		  obj.put(PGmObject.SPRITE,null);
		}
		
		final String mskname = objdoc.getElementsByTagName("maskName").item(0).getTextContent();		
		if (!mskname.equals("<undefined>")) {
			PostponedRef pr = new PostponedRef()
			{
				public boolean invoke()
				{
					ResourceList<Sprite> list = f.resMap.getList(Sprite.class);
					if (list == null) {	return false; }						
					Sprite msk = list.get(mskname);
					if (msk == null) { return false; }
					obj.put(PGmObject.MASK, msk.reference);
					return true;
				}
			};
			postpone.add(pr);
		} else {
		  obj.put(PGmObject.MASK,null);
		}
		
		final String parname = objdoc.getElementsByTagName("parentName").item(0).getTextContent();
		if (!parname.equals("<undefined>") && !parname.equals("self")) {
				PostponedRef pr = new PostponedRef()
					{
						public boolean invoke()
						{
							ResourceList<GmObject> list = f.resMap.getList(GmObject.class);
							if (list == null) { return false; }			
							GmObject par = list.get(parname);
							if (par == null) { return false; }
							obj.put(PGmObject.PARENT, par.reference);
							return true;
						}
					};
				postpone.add(pr);
		} else {
		  obj.put(PGmObject.PARENT,null);
		}
		
		obj.put(PGmObject.SOLID, Integer.parseInt(objdoc.getElementsByTagName("solid").item(0).getTextContent()) < 0);
		obj.put(PGmObject.VISIBLE, Integer.parseInt(objdoc.getElementsByTagName("visible").item(0).getTextContent()) < 0);
		obj.put(PGmObject.DEPTH, Integer.parseInt(objdoc.getElementsByTagName("depth").item(0).getTextContent()));
		obj.put(PGmObject.PERSISTENT, Integer.parseInt(objdoc.getElementsByTagName("persistent").item(0).getTextContent()) < 0);
	
		//Now that properties are loaded iterate the events and load the actions
		NodeList frList = objdoc.getElementsByTagName("event"); 
		for (int ii = 0; ii < frList.getLength(); ii++) {
		  Node fnode = frList.item(ii);
		  final Event ev = new Event();
			
			ev.mainId = Integer.parseInt(fnode.getAttributes().getNamedItem("eventtype").getTextContent());
		  MainEvent me = obj.mainEvents.get(ev.mainId);
			me.events.add(0,ev);
			if (ev.mainId == MainEvent.EV_COLLISION) {
			  final String colname = fnode.getAttributes().getNamedItem("ename").getTextContent();
				PostponedRef pr = new PostponedRef()
					{
						public boolean invoke()
						{
							ResourceList<GmObject> list = f.resMap.getList(GmObject.class);
							if (list == null) {	return false; }	
							GmObject col = list.get(colname);
							if (col == null) { return false; }
							ev.other = col.reference;
							return true;
						}
					};
					postpone.add(pr);
			} else {
			  ev.id = Integer.parseInt(fnode.getAttributes().getNamedItem("enumb").getTextContent());
			}
			
			readActions(c,ev,"INOBJECTACTION", i, ii * 1000 + ev.id, fnode.getChildNodes()); //$NON-NLS-1$
		}
		f.resMap.getList(GmObject.class).lastId = frList.getLength() - 1;
	  rnode = new ResNode(obj.getName(), (byte)3, GmObject.class, obj.reference);
	  node.add(rnode);
	}
	iterateGmObjects(c, cNode.getChildNodes(), rnode);
	}
	
	}
	
	private static void readGmObjects(ProjectFileContext c, ResNode root) throws IOException,GmFormatException, SAXException
		{
		Document in = c.in;
		ProjectFile f = c.f;
		ResNode node = new ResNode("Objects", (byte)0, GmObject.class, null);
		root.add(node);

		NodeList objList = in.getElementsByTagName("objects"); 
		if (objList.getLength() > 0) {
		  objList = objList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateGmObjects(c, objList, node);
		
		f.resMap.getList(GmObject.class).lastId = objList.getLength() - 1;
		}

	private static void iterateRooms(ProjectFileContext c, NodeList rmnList, ResNode node) throws IOException,GmFormatException, SAXException
	{
	ProjectFile f = c.f;

	for (int i = 0; i < rmnList.getLength(); i++) {
	Node cNode = rmnList.item(i);
	String cname = cNode.getNodeName();
	if (cname.equals("#text")) {
	  continue;
	}
	
	ResNode rnode = null;
	
	if (cname.equals("rooms")) { 
		rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(), (byte)2, Room.class, null);
		node.add(rnode);
	} else if (cname.equals("room")){
	  f.resMap.getList(Timeline.class).lastId++;
	  Room rmn = f.resMap.getList(Room.class).add();
	  String fileName = new File(getUnixPath(cNode.getTextContent())).getName();
	  rmn.setName(fileName);
	  rmn.setNode(rnode);
	  rnode = new ResNode(rmn.getName(), (byte)3, Room.class, rmn.reference);
	  node.add(rnode);
	  String path = f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(cNode.getTextContent());
	  
		Document rmndoc = documentBuilder.parse(path + ".room.gmx");
		String caption = rmndoc.getElementsByTagName("caption").item(0).getTextContent();
		rmn.put(PRoom.CAPTION, caption);
		
		NodeList cnodes = rmndoc.getElementsByTagName("room").item(0).getChildNodes();
		for (int x = 0; x < cnodes.getLength(); x++) {
			Node pnode = cnodes.item(x);
			String pname = pnode.getNodeName();
			if (pname.equals("#text")) { 
			  continue; 
			} else if (pname.equals("caption")) {
			  rmn.put(PRoom.CAPTION, pnode.getTextContent());
			} else if (pname.equals("width")) {
			  rmn.put(PRoom.WIDTH, Integer.parseInt(pnode.getTextContent()));
			} else if (pname.equals("height")) {
			  rmn.put(PRoom.HEIGHT, Integer.parseInt(pnode.getTextContent()));
			} else if (pname.equals("vsnap")) {
			  rmn.put(PRoom.SNAP_Y, Integer.parseInt(pnode.getTextContent()));
			} else if (pname.equals("hsnap")) {
			  rmn.put(PRoom.SNAP_X, Integer.parseInt(pnode.getTextContent()));
			} else if (pname.equals("isometric")) {
			  rmn.put(PRoom.ISOMETRIC, Integer.parseInt(pnode.getTextContent()) < 0);
			} else if (pname.equals("speed")) {
			  rmn.put(PRoom.SPEED, Integer.parseInt(pnode.getTextContent()));
			} else if (pname.equals("pesistent")) {
			  rmn.put(PRoom.PERSISTENT, Integer.parseInt(pnode.getTextContent()) < 0);
			} else if (pname.equals("colour")) {
			  int col = Integer.parseInt(pnode.getTextContent());
			  Color color = new Color(col & 0x0000FF, (col & 0x00FF00)>>8, (col & 0xFF0000)>>16);
			  rmn.put(PRoom.BACKGROUND_COLOR, color);
			} else if (pname.equals("showcolour")) {
			  rmn.put(PRoom.DRAW_BACKGROUND_COLOR, Integer.parseInt(pnode.getTextContent()) < 0);
			} else if (pname.equals("code")) {
			  rmn.put(PRoom.CREATION_CODE, pnode.getTextContent());
			} else if (pname.equals("enableViews")) {
			  rmn.put(PRoom.ENABLE_VIEWS, Integer.parseInt(pnode.getTextContent()) < 0);
			} else if (pname.equals("clearViewBackground")) {
			  //TODO: This setting is not implemented in ENIGMA
			} else if (pname.equals("makerSettings")) {
			  //TODO: add this shit
		    NodeList msnodes = pnode.getChildNodes();
		    for (int y = 0; y < msnodes.getLength(); y++) {
		      Node mnode = msnodes.item(y);
		      String mname = mnode.getNodeName();
		      if (mname.equals("#text")) { continue; }
		    }
			} else if (pname.equals("backgrounds")) {
			  //TODO: add background reading
		  	NodeList bgnodes = pnode.getChildNodes();
		  	for (int y = 0; y < bgnodes.getLength(); y++) {
		    	Node bnode = bgnodes.item(y);
		    	String bname = bnode.getNodeName();
		    	if (bname.equals("#text")) { continue; }
		    	}
			} else if (pname.equals("views")) {
			  //TODO: add view reading
		  	NodeList vinodes = pnode.getChildNodes();
		  	for (int y = 0; y < vinodes.getLength(); y++) {
		    	Node vnode = vinodes.item(y);
		    	String vname = vnode.getNodeName();
		    	if (vname.equals("#text")) { continue; }
		    	}
			} else if (pname.equals("instances")) {
			  NodeList insnodes = pnode.getChildNodes();
			  for (int y = 0; y < insnodes.getLength(); y++) {
			    Node inode = insnodes.item(y);
			    String iname = inode.getNodeName();
			    if (iname.equals("#text")) { 
			    	continue; 
			    } else if (iname.equals("instance") && inode.getAttributes().getLength() > 0) {
						Instance inst = rmn.addInstance();
						
						//TODO: Replace this with DelayedRef
						String objname = inode.getAttributes().getNamedItem("objName").getTextContent();
						
						//TODO: because of the way this is set up sprites must be loaded before objects
						GmObject temp = f.resMap.getList(GmObject.class).get(objname);
								if (temp != null) inst.properties.put(PInstance.OBJECT,temp.reference);
						
						int xx = Integer.parseInt(inode.getAttributes().getNamedItem("x").getNodeValue());
						int yy = Integer.parseInt(inode.getAttributes().getNamedItem("y").getNodeValue());
						//TODO: fuck they use strings we use integers
						//inst.properties.put(PInstance.ID, inode.getAttributes().getNamedItem("name").getNodeValue());
						inst.setPosition(new Point(xx,yy));
						inst.setCreationCode(inode.getAttributes().getNamedItem("code").getNodeValue());
						inst.setLocked(Integer.parseInt(inode.getAttributes().getNamedItem("locked").getNodeValue()) < 0);
			    }
			  }
			} else if (pname.equals("tiles")) {
			  //TODO: Add tile reading
		  	NodeList tinodes = pnode.getChildNodes();
		  	for (int y = 0; y < tinodes.getLength(); y++) {
		    	Node tnode = tinodes.item(y);
		    	String tname = tnode.getNodeName();
		    	if (tname.equals("#text")) { continue; }
		    }
			}
		
		  //TODO: Ignoring physics settings for now
		}
	}
	iterateTimelines(c, cNode.getChildNodes(), rnode);
	}
	
	}
	
	private static void readRooms(ProjectFileContext c, ResNode root) throws IOException,GmFormatException, SAXException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Rooms", (byte)1, Room.class, null);
		root.add(node);
		
		NodeList rmnList = in.getElementsByTagName("rooms"); 
		if (rmnList.getLength() > 0) {
		  rmnList = rmnList.item(0).getChildNodes();
		} else {
			return;
		}
		iterateRooms(c, rmnList, node);
		}

	private static void readIncludedFiles(ProjectFileContext c, ResNode root) throws IOException,GmFormatException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Includes", (byte)1, Include.class, null);
		root.add(node);
		
		NodeList incList = in.getElementsByTagName("includes"); 
		if (incList.getLength() > 0) {
		  incList = incList.item(0).getChildNodes();
		} else {
			return;
		}
		//iterateIncludes(c, incList, node);
		}

	private static void readPackages(ProjectFileContext c, ResNode root) throws IOException,GmFormatException
	{
	Document in = c.in;
	
	NodeList pkgList = in.getElementsByTagName("packages"); 
	if (pkgList.getLength() > 0) {
	  pkgList = pkgList.item(0).getChildNodes();
	} else {
		return;
	}
	//iteratePackages(c, extList, node);
	}
	
	private static void readExtensions(ProjectFileContext c, ResNode root) throws IOException,GmFormatException
		{
		Document in = c.in;
		
		ResNode node = new ResNode("Extensions", (byte)1, Extensions.class, null);
		root.add(node);
		
		NodeList extList = in.getElementsByTagName("extensions"); 
		if (extList.getLength() > 0) {
		  extList = extList.item(0).getChildNodes();
		} else {
			return;
		}
		//iterateExtensions(c, extList, node);
		}

	private static void readGameInformation(ProjectFileContext c, ResNode root) throws IOException,GmFormatException
		{
		Document in = c.in;
		
		GameInformation gameInfo = c.f.gameInfo;
		PropertyMap<PGameInformation> p = gameInfo.properties;
		
		NodeList rtfNodes = in.getElementsByTagName("rtf"); 
		Node rtfNode = rtfNodes.item(rtfNodes.getLength() - 1);
		
	  String path = c.f.getPath();
	  path = path.substring(0, path.lastIndexOf('/')+1) + getUnixPath(rtfNode.getTextContent());
		
		String text = "";
		
	  FileInputStream ins = new FileInputStream(path);
    try {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
    	String line = "";
      while ((line = reader.readLine()) != null) {
          text += line + "\n";
      }
    } finally {
        ins.close();
    }
		
		gameInfo.put(PGameInformation.TEXT, text);
		
		ResNode node = new ResNode("Game Information", (byte)3, GameInformation.class, gameInfo.reference);
		root.add(node);
		}
	
	private static void readActions(ProjectFileContext c, ActionContainer container, String errorKey,
			int format1, int format2, NodeList actList) throws IOException,GmFormatException
		{
		final ProjectFile f = c.f;
		
		for (int i = 0; i < actList.getLength(); i++)
			{
			Node actNode = actList.item(i);
			
			if (actNode.getNodeName().equals("#text")) {
				continue;
			} 
			
			int libid = 0;
			int actid = 0;
			byte kind = 0;
			boolean userelative = false;
			boolean isquestion = false;
			boolean isquestiontrue = false;
			boolean isrelative = false;
			boolean useapplyto = false;
			byte exectype = 0;
			
			String code;
			String execInfo = "";
			String appliesto = "";
		
			Argument[] args = null;
			byte[] argkinds = null;
			
			LibAction la = null;
			
			NodeList propList = actNode.getChildNodes();
			for (int ii = 0; ii < propList.getLength(); ii++) {
				Node prop = propList.item(ii);
				
				if (prop.getNodeName().equals("#text")) { continue; }
				
				if (prop.getNodeName().equals("libid")) {
					libid = Integer.parseInt(prop.getTextContent());
				} else if (prop.getNodeName().equals("id")) {
				  actid = Integer.parseInt(prop.getTextContent());
			  } else if (prop.getNodeName().equals("kind")) {
			  	kind = Byte.parseByte(prop.getTextContent());
			  } else if (prop.getNodeName().equals("userelative")) {
			  	userelative = Integer.parseInt(prop.getTextContent()) < 0;
			  } else if (prop.getNodeName().equals("relative")) {
		  		isrelative = Integer.parseInt(prop.getTextContent()) < 0;
			  } else if (prop.getNodeName().equals("isquestion")) {
		  		isquestion = Integer.parseInt(prop.getTextContent()) < 0;
			  } else if (prop.getNodeName().equals("isnot")) {
	  			isquestiontrue = Integer.parseInt(prop.getTextContent()) < 0;
			  } else if (prop.getNodeName().equals("useapplyto")) {
			  	useapplyto = Integer.parseInt(prop.getTextContent()) < 0;
			  } else if (prop.getNodeName().equals("exetype")) {
		  		exectype = Byte.parseByte(prop.getTextContent());
			  } else if (prop.getNodeName().equals("whoName")) {
	  			appliesto = prop.getTextContent();
			  } else if (prop.getNodeName().equals("arguments")) {
					NodeList targList = prop.getChildNodes();
					
					List<Node> argList = new ArrayList<Node>();
					for (int x = 0; x < targList.getLength(); x++) {
					Node arg = targList.item(x);
					if (!arg.getNodeName().equals("#text")) { argList.add(arg); }
					}
					
					args = new Argument[argList.size()];

					for (int x = 0; x < argList.size(); x++) {
						Node arg = argList.get(x);

						if (arg.getNodeName().equals("#text")) {  continue; }
					
						args[x] = new Argument((byte) 0);

						NodeList argproplist = arg.getChildNodes();
						for (int xx = 0; xx < argproplist.getLength(); xx++) {
							Node argprop = argproplist.item(xx);
							
							if (prop.getNodeName().equals("#text")) { continue; }
							
							
							final String proptext = argprop.getTextContent();
							final Argument argument = args[x];
							if (argprop.getNodeName().equals("kind")) {
								argument.kind = Byte.parseByte(argprop.getTextContent());
							} else if (argprop.getNodeName().equals("sprite")) {
								PostponedRef pr = new PostponedRef()
								{
									public boolean invoke()
									{
										ResourceList<Sprite> list = f.resMap.getList(Sprite.class);
										if (list == null) {	return false; }						
										Sprite spr = list.get(proptext);
										if (spr == null) { return false; }
										argument.setRes(spr.reference);
										return true;
									}
								};
								postpone.add(pr);
							} else if (argprop.getNodeName().equals("background")) {
								PostponedRef pr = new PostponedRef()
								{
									public boolean invoke()
									{
										ResourceList<Background> list = f.resMap.getList(Background.class);
										if (list == null) {	return false; }						
									  Background bkg = list.get(proptext);
										if (bkg == null) { return false; }
										argument.setRes(bkg.reference);
										return true;
									}
								};
								postpone.add(pr);
							} else if (argprop.getNodeName().equals("path")) {
								PostponedRef pr = new PostponedRef()
								{
									public boolean invoke()
									{
										ResourceList<Path> list = f.resMap.getList(Path.class);
										if (list == null) {	return false; }						
										Path pth = list.get(proptext);
										if (pth == null) { return false; }
										argument.setRes(pth.reference);
										return true;
									}
								};
								postpone.add(pr);
							} else if (argprop.getNodeName().equals("script")) {
								PostponedRef pr = new PostponedRef()
								{
									public boolean invoke()
									{
										ResourceList<Script> list = f.resMap.getList(Script.class);
										if (list == null) {	return false; }						
										Script scr = list.get(proptext);
										if (scr == null) { return false; }
										argument.setRes(scr.reference);
										return true;
									}
								};
								postpone.add(pr);
							} else if (argprop.getNodeName().equals("font")) {
								PostponedRef pr = new PostponedRef()
								{
									public boolean invoke()
									{
										ResourceList<Font> list = f.resMap.getList(Font.class);
										if (list == null) {	return false; }						
										Font fnt = list.get(proptext);
										if (fnt == null) { return false; }
										argument.setRes(fnt.reference);
										return true;
									}
								};
								postpone.add(pr);
							} else if (argprop.getNodeName().equals("object")) {
								PostponedRef pr = new PostponedRef()
								{
									public boolean invoke()
									{
										ResourceList<GmObject> list = f.resMap.getList(GmObject.class);
										if (list == null) {	return false; }						
										GmObject obj = list.get(proptext);
										if (obj == null) { return false; }
										argument.setRes(obj.reference);
										return true;
									}
								};
								postpone.add(pr);
							} else if (argprop.getNodeName().equals("string")) {
								argument.setVal(proptext);
							}
						}
					}
				}
			}
			
			la = LibManager.getLibAction(libid, actid);
			boolean unknownLib = la == null;
			//The libAction will have a null parent, among other things
			if (unknownLib)
			{
				la = new LibAction();
				la.id = actid;
				la.parentId = libid;
				la.actionKind = kind;
        //XXX: Maybe make this more agnostic?"
				if (la.actionKind == Action.ACT_CODE) {
				  la = LibManager.codeAction;
				} else {
					la.allowRelative = userelative;
					la.question = isquestion;
					la.canApplyTo = useapplyto;
					la.execType = exectype;
					if (la.execType == Action.EXEC_FUNCTION)
						la.execInfo = execInfo;
					if (la.execType == Action.EXEC_CODE)
						la.execInfo = execInfo;
				}
				if (args != null) {
					la.libArguments = new LibArgument[args.length];
					for (int b = 0; b < args.length; b++) {
						LibArgument argument = new LibArgument();
						argument.kind = args[b].kind;
						la.libArguments[b] = argument;
					}
				}
			}
			
			final Action act = container.addAction(la);
			if (appliesto.equals("self")) {
				act.setAppliesTo(GmObject.OBJECT_SELF);
			} else if (appliesto.equals("other")) {
				act.setAppliesTo(GmObject.OBJECT_OTHER);
			} else {
				final String objname = appliesto;
				PostponedRef pr = new PostponedRef()
					{
						public boolean invoke()
						{
							ResourceList<GmObject> list = f.resMap.getList(GmObject.class);
							if (list == null) {	return false; }						
							GmObject obj = list.get(objname);
							if (obj == null) { return false; }
							act.setAppliesTo(obj.reference);
							return true;
						}
					};
					postpone.add(pr);
			}
			
			act.setRelative(isrelative);
			if (args != null && args.length > 0) {
			  act.setArguments(args);
			}
			act.setNot(isquestiontrue);
			}
			
		}
	
	}
