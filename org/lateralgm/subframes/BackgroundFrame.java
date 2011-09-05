/*
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.BackgroundPreview;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.ui.swing.util.SwingExecutor;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class BackgroundFrame extends ResourceFrame<Background,PBackground> implements
		UpdateListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("BackgroundFrame.LOAD"); //$NON-NLS-1$
	public JButton load;
	public JLabel width;
	public JLabel height;
	public JCheckBox transparent;
	public JButton edit;
	public JCheckBox smooth;
	public JCheckBox preload;
	public JCheckBox tileset;

	public JPanel side2;
	public NumberField tWidth;
	public NumberField tHeight;
	public NumberField hOffset;
	public NumberField vOffset;
	public NumberField hSep;
	public NumberField vSep;
	public BackgroundPreview preview;
	public boolean imageChanged = false;
	private BackgroundEditor editor;

	private final BackgroundPropertyListener bpl = new BackgroundPropertyListener();

	public BackgroundFrame(Background res, ResNode node)
		{
		super(res,node);
		res.properties.getUpdateSource(PBackground.USE_AS_TILESET).addListener(bpl);
		res.reference.updateSource.addListener(this);

		GroupLayout layout = new GroupLayout(getContentPane())
			{
				@Override
				public void layoutContainer(Container parent)
					{
					Dimension m = BackgroundFrame.this.getMinimumSize();
					Dimension s = BackgroundFrame.this.getSize();
					Dimension r = new Dimension(Math.max(m.width,s.width),Math.max(m.height,s.height));
					if (!r.equals(s))
						BackgroundFrame.this.setSize(r);
					else
						super.layoutContainer(parent);
					}
			};
		setLayout(layout);

		JPanel side1 = new JPanel();
		makeSide1(side1);
		side2 = new JPanel();
		makeSide2(side2);

		preview = new BackgroundPreview(res);
		preview.setVerticalAlignment(SwingConstants.TOP);
		JScrollPane scroll = new JScrollPane(preview);

		layout.setHorizontalGroup(layout.createSequentialGroup()
		/**/.addComponent(side1,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addComponent(side2,DEFAULT_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addComponent(scroll,120,320,DEFAULT_SIZE));
		layout.setVerticalGroup(layout.createParallelGroup()
		/**/.addComponent(side1)
		/**/.addComponent(side2)
		/**/.addComponent(scroll,120,240,DEFAULT_SIZE));
		pack();
		}

	private void makeSide1(JPanel side1)
		{
		GroupLayout s1Layout = new GroupLayout(side1);
		s1Layout.setAutoCreateContainerGaps(true);
		s1Layout.setAutoCreateGaps(true);
		side1.setLayout(s1Layout);

		JLabel nLabel = new JLabel(Messages.getString("BackgroundFrame.NAME"));
		load = new JButton(Messages.getString("SpriteFrame.LOAD")); //$NON-NLS-1$
		load.setIcon(LOAD_ICON);
		load.addActionListener(this);
		width = new JLabel(Messages.getString("BackgroundFrame.WIDTH") + res.getWidth()); //$NON-NLS-1$
		height = new JLabel(Messages.getString("BackgroundFrame.HEIGHT") + res.getHeight()); //$NON-NLS-1$

		edit = new JButton(Messages.getString("BackgroundFrame.EDIT")); //$NON-NLS-1$
		edit.addActionListener(this);

		transparent = new JCheckBox(Messages.getString("BackgroundFrame.TRANSPARENT")); //$NON-NLS-1$
		plf.make(transparent,PBackground.TRANSPARENT);
		smooth = new JCheckBox(Messages.getString("BackgroundFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PBackground.SMOOTH_EDGES);
		preload = new JCheckBox(Messages.getString("BackgroundFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PBackground.PRELOAD);
		tileset = new JCheckBox(Messages.getString("BackgroundFrame.USE_AS_TILESET")); //$NON-NLS-1$
		plf.make(tileset,PBackground.USE_AS_TILESET);

		save.setText(Messages.getString("BackgroundFrame.SAVE")); //$NON-NLS-1$

		s1Layout.setHorizontalGroup(s1Layout.createParallelGroup()
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(load,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addGroup(s1Layout.createSequentialGroup()
		/*		*/.addComponent(width)
		/*		*/.addComponent(height))
		/**/.addComponent(edit,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(transparent)
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(tileset)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		s1Layout.setVerticalGroup(s1Layout.createSequentialGroup()
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(nLabel)
		/*		*/.addComponent(name))
		/**/.addComponent(load)
		/**/.addGroup(s1Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(width)
		/*		*/.addComponent(height))
		/**/.addComponent(edit)
		/**/.addComponent(transparent)
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(tileset)
		/**/.addGap(8,8,MAX_VALUE)
		/**/.addComponent(save));
		}

	private void makeSide2(JPanel side2)
		{
		GroupLayout s2Layout = new GroupLayout(side2);
		side2.setLayout(s2Layout);
		String tileProps = Messages.getString("BackgroundFrame.TILE_PROPERTIES"); //$NON-NLS-1$
		side2.setBorder(BorderFactory.createTitledBorder(tileProps));

		JLabel twLabel = new JLabel(Messages.getString("BackgroundFrame.TILE_WIDTH")); //$NON-NLS-1$
		twLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		tWidth = new NumberField(0,Integer.MAX_VALUE);
		plf.make(tWidth,PBackground.TILE_WIDTH);
		tWidth.setColumns(3);

		JLabel thLabel = new JLabel(Messages.getString("BackgroundFrame.TILE_HEIGHT")); //$NON-NLS-1$
		thLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		tHeight = new NumberField(0,Integer.MAX_VALUE);
		plf.make(tHeight,PBackground.TILE_HEIGHT);
		tHeight.setColumns(3);

		JLabel hoLabel = new JLabel(Messages.getString("BackgroundFrame.H_OFFSET")); //$NON-NLS-1$
		hoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		hOffset = new NumberField(0,Integer.MAX_VALUE);
		plf.make(hOffset,PBackground.H_OFFSET);
		hOffset.setColumns(3);

		JLabel voLabel = new JLabel(Messages.getString("BackgroundFrame.V_OFFSET")); //$NON-NLS-1$
		voLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		vOffset = new NumberField(0,Integer.MAX_VALUE);
		plf.make(vOffset,PBackground.V_OFFSET);
		vOffset.setColumns(3);

		JLabel hsLabel = new JLabel(Messages.getString("BackgroundFrame.H_SEP")); //$NON-NLS-1$
		hsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		hSep = new NumberField(0,Integer.MAX_VALUE);
		plf.make(hSep,PBackground.H_SEP);
		hSep.setColumns(3);

		JLabel vsLabel = new JLabel(Messages.getString("BackgroundFrame.V_SEP")); //$NON-NLS-1$
		vsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		vSep = new NumberField(0,Integer.MAX_VALUE);
		plf.make(vSep,PBackground.V_SEP);
		vSep.setColumns(3);

		s2Layout.setHorizontalGroup(s2Layout.createSequentialGroup()
		/**/.addContainerGap(4,4)
		/**/.addGroup(s2Layout.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(twLabel)
		/*		*/.addComponent(thLabel)
		/*		*/.addComponent(hoLabel)
		/*		*/.addComponent(voLabel)
		/*		*/.addComponent(hsLabel)
		/*		*/.addComponent(vsLabel))
		/**/.addGap(4)
		/**/.addGroup(s2Layout.createParallelGroup()
		/*		*/.addComponent(tWidth,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(tHeight,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(hOffset,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(vOffset,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(hSep,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(vSep,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE))
		/**/.addContainerGap(4,4));
		s2Layout.setVerticalGroup(s2Layout.createSequentialGroup()
		/**/.addGroup(s2Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(twLabel)
		/*		*/.addComponent(tWidth))
		/**/.addGap(2)
		/**/.addGroup(s2Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(thLabel)
		/*		*/.addComponent(tHeight))
		/**/.addGap(8)
		/**/.addGroup(s2Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(hoLabel)
		/*		*/.addComponent(hOffset))
		/**/.addGap(2)
		/**/.addGroup(s2Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(voLabel)
		/*		*/.addComponent(vOffset))
		/**/.addGap(8)
		/**/.addGroup(s2Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(hsLabel)
		/*		*/.addComponent(hSep))
		/**/.addGap(2)
		/**/.addGroup(s2Layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(vsLabel)
		/*		*/.addComponent(vSep))
		/**/.addContainerGap(8,8));

		side2.setVisible(tileset.isSelected());
		}

	protected boolean areResourceFieldsEqual()
		{
		return !imageChanged;
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		}

	@Override
	public void updateResource()
		{
		super.updateResource();
		imageChanged = false;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			BufferedImage img = Util.getValidImage();
			if (img != null)
				{
				res.setBackgroundImage(img);
				imageChanged = true;
				cleanup();
				}
			return;
			}
		if (e.getSource() == edit)
			{
			try
				{
				if (editor == null)
					new BackgroundEditor();
				else
					editor.start();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			return;
			}
		super.actionPerformed(e);
		}

	@Override
	public Dimension getMinimumSize()
		{
		Dimension p = getContentPane().getSize();
		Dimension l = getContentPane().getMinimumSize();
		Dimension s = getSize();
		l.width += s.width - p.width;
		l.height += s.height - p.height;
		return l;
		}

	private class BackgroundEditor implements UpdateListener
		{
		public final FileChangeMonitor monitor;

		public BackgroundEditor() throws IOException
			{
			BufferedImage bi = res.getBackgroundImage();
			if (bi == null)
				{
				bi = new BufferedImage(640,480,BufferedImage.TYPE_3BYTE_BGR);
				res.setBackgroundImage(bi);
				imageChanged = true;
				}
			File f = File.createTempFile(res.getName(),
					"." + Prefs.externalBackgroundExtension,LGM.tempDir); //$NON-NLS-1$
			f.deleteOnExit();
			FileOutputStream out = new FileOutputStream(f);
			ImageIO.write(bi,Prefs.externalBackgroundExtension,out);
			out.close();
			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this);
			editor = this;
			start();
			}

		public void start() throws IOException
			{
			if (!Prefs.useExternalBackgroundEditor || Prefs.externalBackgroundEditorCommand == null)
				try
					{
					Desktop.getDesktop().edit(monitor.file);
					}
				catch (UnsupportedOperationException e)
					{
					throw new UnsupportedOperationException("no internal or system background editor",e);
					}
			else
				Runtime.getRuntime().exec(
						String.format(Prefs.externalBackgroundEditorCommand,monitor.file.getAbsolutePath()));
			}

		public void stop()
			{
			monitor.stop();
			monitor.file.delete();
			editor = null;
			}

		public void updated(UpdateEvent e)
			{
			if (!(e instanceof FileUpdateEvent)) return;
			switch (((FileUpdateEvent) e).flag)
				{
				case CHANGED:
					BufferedImage img;
					try
						{
						img = ImageIO.read(new FileInputStream(monitor.file));
						}
					catch (IOException ioe)
						{
						ioe.printStackTrace();
						return;
						}
					res.setBackgroundImage(img);
					imageChanged = true;
					break;
				case DELETED:
					editor = null;
				}
			}
		}

	public void dispose()
		{
		cleanup();
		super.dispose();
		}

	protected void cleanup()
		{
		if (editor != null) editor.stop();
		}

	public void updated(UpdateEvent e)
		{
		width.setText(Messages.getString("BackgroundFrame.WIDTH") + res.getWidth()); //$NON-NLS-1$
		height.setText(Messages.getString("BackgroundFrame.HEIGHT") + res.getHeight()); //$NON-NLS-1$
		}

	private class BackgroundPropertyListener extends PropertyUpdateListener<PBackground>
		{
		public void updated(PropertyUpdateEvent<PBackground> e)
			{
			//USE_AS_TILESET
			side2.setVisible((Boolean) res.get(PBackground.USE_AS_TILESET));
			}
		}
	}
