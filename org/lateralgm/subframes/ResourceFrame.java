/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013-2014 Robert B. Colton
 *
 * This file is part of LateralGM.
 *
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.subframes;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.ExceptionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.RevertableMDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.Extension;
import org.lateralgm.resources.ExtensionPackages;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;

/** Provides common functionality and structure to Resource editing frames */
public abstract class ResourceFrame<R extends Resource<R,P>, P extends Enum<P>> extends
		RevertableMDIFrame implements ActionListener,ExceptionListener
	{
	private static final long serialVersionUID = 1L;
	/** Automatically set up to save and close the frame */
	public final JButton save = new JButton();
	/** The resource this frame is editing (feel free to change it as you wish) */
	public R res;
	/** Backup of res as it was before changes were made */
	public R resOriginal;
	/** Whether changes were made and reported by the PropertyUpdateListener **/
	public boolean resChanged;
	/** The ResNode this frame is linked to */
	public final ResNode node;

	protected ResourceFrameListener frameListener;

	protected final PropertyLinkFactory<P> plf;

	// Static Factory methods //
	public static Map<Class<?>,ResourceFrameFactory> factories = new HashMap<Class<?>,ResourceFrameFactory>();

	public static interface ResourceFrameFactory
		{
		public ResourceFrame<?,?> makeFrame(Resource<?,?> r, ResNode node);
		}

	private static class DefaultResourceFrameFactory implements ResourceFrameFactory
		{
		Class<?> kind;

		DefaultResourceFrameFactory(Class<?> kind)
			{
			this.kind = kind;
			}

		public ResourceFrame<?,?> makeFrame(Resource<?,?> r, ResNode node)
			{
			if (kind == Sprite.class)
				return new SpriteFrame((Sprite) r,node);
			else if (kind == Sound.class)
				return new SoundFrame((Sound) r,node);
			else if (kind == Background.class)
				return new BackgroundFrame((Background) r,node);
			else if (kind == Path.class)
				return new PathFrame((Path) r,node);
			else if (kind == Script.class)
				return new ScriptFrame((Script) r,node);
			else if (kind == Shader.class)
				return new ShaderFrame((Shader) r,node);
			else if (kind == Font.class)
				return new FontFrame((Font) r,node);
			else if (kind == Timeline.class)
				return new TimelineFrame((Timeline) r,node);
			else if (kind == GmObject.class)
				return new GmObjectFrame((GmObject) r,node);
			else if (kind == Room.class)
				return new RoomFrame((Room) r,node);
			else if (kind == Include.class)
				return new IncludeFrame((Include) r,node);
			else if (kind == Extension.class)
				return new ExtensionFrame((Extension) r,node);
			else if (kind == Constants.class) {
				LGM.showConstantsFrame(LGM.currentFile.defaultConstants);
				return null;
			}
			else if (kind == GameInformation.class)
				return LGM.getGameInfo();
			else if (kind == GameSettings.class) {
				LGM.showGameSettings(LGM.getSelectedConfig());
				return null;
			}
			else if (kind == ExtensionPackages.class)
				return LGM.getExtensionPackages();
			else
				return null;
			}
		}

	static
		{
		for (Class<?> k : Resource.kinds)
			factories.put(k,new DefaultResourceFrameFactory(k));
		}

	/**
	 * Note for inheriting classes. Be sure to call this parent instantiation for proper setup.
	 * The res and node parameters are only needed in the instantiation to assign globals;
	 * That is, once you call this, they will immediately gain global scope and may be treated thusly.
	 */
	public ResourceFrame(R res, ResNode node)
		{
		this(res,node,res.getName(),true);
		}

	public ResourceFrame(R res, ResNode node, String title, boolean functional)
		{
		this(res,node,title,functional,functional,functional,functional);
		}

	public ResourceFrame(R res, ResNode node, String title, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable)
		{
		super(title,resizable,closable,maximizable,iconifiable);

		plf = new PropertyLinkFactory<P>(res.properties,this);
		this.res = res;
		this.node = node;
		resOriginal = res.clone();
		setFrameIcon(ResNode.ICON.get(res.getClass()));

		save.setToolTipText(Messages.getString("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(LGM.getIconForKey("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.addActionListener(this);
		}

	public String getConfirmationName()
		{
		return res.getName();
		}

	public boolean resourceChanged()
		{
		commitChanges();
		//NOTE: Any children that override this should call this.
		if (frameListener != null && frameListener.resourceChanged()) return true;
		if (!areResourceFieldsEqual()) return true;
		return !res.equals(resOriginal);
		}

	/** Override to check additional fields other than the Resource<> defaults. */
	@SuppressWarnings("static-method")
	protected boolean areResourceFieldsEqual()
		{
		return true;
		}

	public void setFrameListener(ResourceFrameListener listener) {
		frameListener = listener;
	}

	//TODO: There is a fundamental flaw in the way this function is utilized.
	//Checking for changes causes the changes to be committed, and then if the user
	//does choose to save this method will also commit changes a second time.
	public void updateResource(boolean commit)
		{
		if (frameListener != null) frameListener.updateResource(commit);
		if (commit) {
			commitChanges();
		}
		resOriginal = res.clone();
		}

	@Override
	public void setResourceChanged() {
		if (frameListener != null) frameListener.setResourceChanged();
		res.changed = true;
	}

	public void revertResource()
		{
		if (frameListener != null) frameListener.revertResource();
		resOriginal.updateReference();
		}

	public abstract void commitChanges();

	public static void addGap(Container c, int w, int h)
		{
		JLabel l = new JLabel();
		l.setPreferredSize(new Dimension(w,h));
		c.add(l);
		}

	public void doDefaultSaveAction() {
		if (resourceChanged()) {
			setResourceChanged();
			updateResource(false);
		}
		close();
	}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == save)
			{
				this.doDefaultSaveAction();
			}
		}

	public void exceptionThrown(Exception e)
		{
		e.printStackTrace();
		}

	public void dispose()
		{
		if (frameListener != null) frameListener.dispose();
		super.dispose();
		if (node != null) node.frame = null; // allows a new frame to open
		save.removeActionListener(this);
		removeAll();
		plf.removeAllLinks();
		}

	@Override
	public void setVisible(boolean visible) {
		if (frameListener != null) frameListener.setVisible(visible);;
		super.setVisible(visible);
	}

	public abstract interface ResourceFrameListener
	{
		public abstract void dispose();
		public abstract void setVisible(boolean visible);
		public abstract void updateResource(boolean commit);
		public abstract void revertResource();
		public abstract boolean resourceChanged();
		public abstract void setResourceChanged();
	}

	}
