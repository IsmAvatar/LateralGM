/*
 * Copyright (C) 2007, 2008 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
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
import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.BackgroundPreview;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;

public class BackgroundFrame extends ResourceFrame<Background> implements ChangeListener
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
	public IntegerField tWidth;
	public IntegerField tHeight;
	public IntegerField hOffset;
	public IntegerField vOffset;
	public IntegerField hSep;
	public IntegerField vSep;
	public BackgroundPreview preview;
	public boolean imageChanged = false;
	public File extFile;

	public BackgroundFrame(Background res, ResNode node)
		{
		super(res,node);
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

		preview = new BackgroundPreview(this);
		if (res.backgroundImage != null)
			preview.setIcon(new ImageIcon(res.backgroundImage));
		else
			preview.setPreferredSize(new Dimension(0,0));
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
		width = new JLabel(Messages.getString("BackgroundFrame.WIDTH") + res.width); //$NON-NLS-1$
		height = new JLabel(Messages.getString("BackgroundFrame.HEIGHT") + res.height); //$NON-NLS-1$

		edit = new JButton(Messages.getString("BackgroundFrame.EDIT")); //$NON-NLS-1$
		edit.addActionListener(this);

		transparent = new JCheckBox(Messages.getString("BackgroundFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setSelected(res.transparent);
		transparent.addActionListener(this);
		smooth = new JCheckBox(Messages.getString("BackgroundFrame.SMOOTH")); //$NON-NLS-1$
		smooth.setSelected(res.smoothEdges);
		preload = new JCheckBox(Messages.getString("BackgroundFrame.PRELOAD")); //$NON-NLS-1$
		preload.setSelected(res.preload);
		tileset = new JCheckBox(Messages.getString("BackgroundFrame.USE_AS_TILESET")); //$NON-NLS-1$
		tileset.setSelected(res.useAsTileSet);
		tileset.addActionListener(this);

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
		tWidth = new IntegerField(0,Integer.MAX_VALUE,res.tileWidth);
		tWidth.addActionListener(this);
		tWidth.setColumns(3);

		JLabel thLabel = new JLabel(Messages.getString("BackgroundFrame.TILE_HEIGHT")); //$NON-NLS-1$
		thLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		tHeight = new IntegerField(0,Integer.MAX_VALUE,res.tileHeight);
		tHeight.addActionListener(this);
		tHeight.setColumns(3);

		JLabel hoLabel = new JLabel(Messages.getString("BackgroundFrame.H_OFFSET")); //$NON-NLS-1$
		hoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		hOffset = new IntegerField(0,Integer.MAX_VALUE,res.horizOffset);
		hOffset.addActionListener(this);
		hOffset.setColumns(3);

		JLabel voLabel = new JLabel(Messages.getString("BackgroundFrame.V_OFFSET")); //$NON-NLS-1$
		voLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		vOffset = new IntegerField(0,Integer.MAX_VALUE,res.vertOffset);
		vOffset.addActionListener(this);
		vOffset.setColumns(3);

		JLabel hsLabel = new JLabel(Messages.getString("BackgroundFrame.H_SEP")); //$NON-NLS-1$
		hsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		hSep = new IntegerField(0,Integer.MAX_VALUE,res.horizSep);
		hSep.addActionListener(this);
		hSep.setColumns(3);

		JLabel vsLabel = new JLabel(Messages.getString("BackgroundFrame.V_SEP")); //$NON-NLS-1$
		vsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		vSep = new IntegerField(0,Integer.MAX_VALUE,res.vertSep);
		vSep.addActionListener(this);
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

	@Override
	public boolean resourceChanged()
		{
		commitChanges();
		if (imageChanged) return true;
		ResourceComparator c = new ResourceComparator();
		c.addExclusions(Background.class,"backgroundImage","imageCache"); //$NON-NLS-1$
		return !c.areEqual(res,resOriginal);
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.backgrounds.replace(res,resOriginal);
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		res.transparent = transparent.isSelected();
		res.smoothEdges = smooth.isSelected();
		res.preload = preload.isSelected();
		res.useAsTileSet = tileset.isSelected();
		res.tileWidth = tWidth.getIntValue();
		res.tileHeight = tWidth.getIntValue();
		res.horizOffset = hOffset.getIntValue();
		res.vertOffset = vOffset.getIntValue();
		res.horizSep = hSep.getIntValue();
		res.vertSep = vSep.getIntValue();
		}

	@Override
	public void updateResource()
		{
		super.updateResource();
		imageChanged = false;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == tileset || e.getSource() == tWidth || e.getSource() == tHeight
				|| e.getSource() == hOffset || e.getSource() == vOffset || e.getSource() == hSep
				|| e.getSource() == vSep)
			{
			side2.setVisible(tileset.isSelected());
			preview.repaint(((JViewport) preview.getParent()).getViewRect());
			return;
			}
		if (e.getSource() == load)
			{
			BufferedImage img = Util.getValidImage();
			if (img != null)
				{
				res.backgroundImage = img;
				cleanup();
				updateImage();
				}
			return;
			}
		if (e.getSource() == edit)
			{
			if (Prefs.useExternalBackgroundEditor && res.backgroundImage != null)
				{
				try
					{
					if (extFile == null)
						{
						extFile = new File(LGM.tempDir + File.separator + "back" + res.hashCode() + ".bmp");
						FileOutputStream out = new FileOutputStream(extFile);
						ImageIO.write(res.backgroundImage,"bmp",out);
						out.close();
						FileChangeMonitor fcm = new FileChangeMonitor(extFile);
						fcm.addChangeListener(this);
						fcm.start();
						}
					Runtime.getRuntime().exec(
							String.format(Prefs.externalBackgroundEditorCommand,extFile.getAbsolutePath()));
					}
				catch (Exception ex)
					{
					ex.printStackTrace();
					}
				}
			return;
			}
		if (e.getSource() == transparent)
			{
			res.transparent = transparent.isSelected();
			node.updateIcon();
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

	public void stateChanged(ChangeEvent e)
		{
		if (e.getSource() instanceof FileChangeMonitor)
			{
			int flag = ((FileChangeMonitor) e.getSource()).getFlag();
			if (flag == FileChangeMonitor.FLAG_CHANGED)
				{
				try
					{
					BufferedImage img = ImageIO.read(new FileInputStream(extFile));
					ColorConvertOp conv = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB),null);
					BufferedImage dest = new BufferedImage(img.getWidth(),img.getHeight(),
							BufferedImage.TYPE_3BYTE_BGR);
					conv.filter(img,dest);
					res.backgroundImage = dest;
					//not entirely sure if this is necessary, but
					//stateChanged does get called from another thread
					SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
								{
								updateImage();
								}
						});
					}
				catch (Exception ex)
					{
					ex.printStackTrace();
					}
				}
			else if (flag == FileChangeMonitor.FLAG_DELETED)
				{
				extFile = null;
				}
			}
		}

	protected void updateImage()
		{
		res.width = res.backgroundImage.getWidth();
		res.height = res.backgroundImage.getHeight();
		width.setText(Messages.getString("BackgroundFrame.WIDTH") + res.width); //$NON-NLS-1$
		height.setText(Messages.getString("BackgroundFrame.HEIGHT") + res.height); //$NON-NLS-1$
		imageChanged = true;
		preview.setIcon(new ImageIcon(res.backgroundImage));
		node.updateIcon();
		}

	public void dispose()
		{
		cleanup();
		super.dispose();
		}

	protected void cleanup()
		{
		try
			{
			extFile.delete();
			}
		catch (Exception e)
			{
			}
		extFile = null;
		}
	}
