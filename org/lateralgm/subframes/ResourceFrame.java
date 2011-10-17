/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
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
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
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
	/** The ResNode this frame is linked to */
	public final ResNode node;

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
			if (kind == Sprite.class) return new SpriteFrame((Sprite) r,node);
			if (kind == Sound.class) return new SoundFrame((Sound) r,node);
			if (kind == Background.class) return new BackgroundFrame((Background) r,node);
			if (kind == Path.class) return new PathFrame((Path) r,node);
			if (kind == Script.class) return new ScriptFrame((Script) r,node);
			if (kind == Font.class) return new FontFrame((Font) r,node);
			if (kind == Timeline.class) return new TimelineFrame((Timeline) r,node);
			if (kind == GmObject.class) return new GmObjectFrame((GmObject) r,node);
			if (kind == Room.class) return new RoomFrame((Room) r,node);

			if (kind == GameInformation.class) return LGM.getGameInfo();
			if (kind == GameSettings.class) return LGM.getGameSettings();
			//extensions returns null too, for now
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
		if (!areResourceFieldsEqual()) return true;
		return !res.equals(resOriginal);
		}

	/** Override to check additional fields other than the Resource<> defaults. */
	@SuppressWarnings("static-method")
	protected boolean areResourceFieldsEqual()
		{
		return true;
		}

	public void updateResource()
		{
		commitChanges();
		resOriginal = res.clone();
		}

	public void revertResource()
		{
		resOriginal.updateReference();
		}

	public abstract void commitChanges();

	public static void addGap(Container c, int w, int h)
		{
		JLabel l = new JLabel();
		l.setPreferredSize(new Dimension(w,h));
		c.add(l);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == save)
			{
			updateResource();
			close();
			}
		}

	public void exceptionThrown(Exception e)
		{
		e.printStackTrace();
		}

	public void dispose()
		{
		super.dispose();
		if (node != null) node.frame = null; // allows a new frame to open
		save.removeActionListener(this);
		removeAll();
		}
	}
