/*
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import org.lateralgm.components.EffectsFrame;
import org.lateralgm.components.EffectsFrame.EffectsFrameListener;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.BackgroundPreview;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.ui.swing.util.SwingExecutor;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class BackgroundFrame extends InstantiableResourceFrame<Background,PBackground> implements
		UpdateListener, EffectsFrameListener
	{
	private static final long serialVersionUID = 1L;
	public JButton load;
	public JLabel statusLabel;
	public JCheckBox transparent;
	public JButton edit, zoomIn, zoomOut;
	public JToggleButton zoomButton;
	public JCheckBox smooth;
	public JCheckBox preload;
	public JCheckBox tileset;

	public MouseAdapter mouseAdapter = null;

	public NumberField tWidth;
	public NumberField tHeight;
	public NumberField hOffset;
	public NumberField vOffset;
	public NumberField hSep;
	public NumberField vSep;
	public JScrollPane previewScroll;
	public BackgroundPreview preview;
	public boolean imageChanged = false;
	private BackgroundEditor editor;

	private final BackgroundPropertyListener bpl = new BackgroundPropertyListener();

	/** Zoom in, centering around a specific point, usually the mouse. */
	public void zoomIn(Point point)
		{
		if (this.getZoom() >= 32) return;
		this.setZoom(this.getZoom() * 2);
		Dimension size = previewScroll.getViewport().getSize();

		int newX = (int) (point.x * 2) - size.width / 2;
		int newY = (int) (point.y * 2) - size.height / 2;
		previewScroll.getViewport().setViewPosition(new Point(newX,newY));

		previewScroll.revalidate();
		previewScroll.repaint();
		}

	/** Zoom out, centering around a specific point, usually the mouse. */
	public void zoomOut(Point point)
		{
		if (this.getZoom() <= 0.04) return;
		this.setZoom(this.getZoom() / 2);
		Dimension size = previewScroll.getViewport().getSize();

		int newX = (int) (point.x / 2) - size.width / 2;
		int newY = (int) (point.y / 2) - size.height / 2;
		previewScroll.getViewport().setViewPosition(new Point(newX,newY));

		previewScroll.revalidate();
		previewScroll.repaint();
		}

	public void zoomIn()
		{
		Dimension size = previewScroll.getViewport().getViewSize();
		zoomIn(new Point(size.width/2,size.height/2));
		}

	public void zoomOut()
		{
		Dimension size = previewScroll.getViewport().getViewSize();
		zoomOut(new Point(size.width/2,size.height/2));
		}

	public BackgroundFrame(Background res, ResNode node)
		{
		super(res,node);
		res.properties.getUpdateSource(PBackground.USE_AS_TILESET).addListener(bpl);
		res.reference.updateSource.addListener(this);

		this.setLayout(new BorderLayout());

		preview = new BackgroundPreview(res);
		preview.setVerticalAlignment(SwingConstants.TOP);

		mouseAdapter = new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent ev)
					{
					if (ev.getButton() == MouseEvent.BUTTON1)
						{
						preview.setCursor(LGM.zoomInCursor);
						}
					if (ev.getButton() == MouseEvent.BUTTON3)
						{
						preview.setCursor(LGM.zoomOutCursor);
						}
					}

				@Override
				public void mouseReleased(MouseEvent ev)
					{
					if (ev.getButton() == MouseEvent.BUTTON1)
						{
						zoomIn(ev.getPoint());
						}
					if (ev.getButton() == MouseEvent.BUTTON3)
						{
						zoomOut(ev.getPoint());
						}
					preview.setCursor(LGM.zoomCursor);
					}
			};

		previewScroll = new JScrollPane(preview);

		this.add(makeToolBar(),BorderLayout.NORTH);
		JSplitPane orientationSplit = new JSplitPane();
		if (Prefs.rightOrientation) {
			orientationSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					previewScroll,makeOptionsPanel());
			orientationSplit.setResizeWeight(1d);
		} else {
			orientationSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					makeOptionsPanel(),previewScroll);
		}
		this.add(orientationSplit, BorderLayout.CENTER);
		this.add(makeStatusBar(),BorderLayout.SOUTH);

		updateStatusBar();
		updateScrollBars();

		pack();
		this.setSize(640,400);
		}

	private JButton makeJButton(String key)
		{
		JButton but = new JButton(LGM.getIconForKey(key));
		but.setToolTipText(Messages.getString(key));
		but.addActionListener(this);
		but.setActionCommand(key);
		return but;
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);

		tool.add(save);
		tool.addSeparator();

		tool.add(makeJButton("BackgroundFrame.CREATE"));
		tool.add(makeJButton("BackgroundFrame.LOAD"));
		tool.add(makeJButton("BackgroundFrame.SAVE"));
		tool.add(makeJButton("BackgroundFrame.EDIT"));
		tool.add(makeJButton("BackgroundFrame.EFFECT"));

		tool.addSeparator();

		// TODO: Implement undo/redo
		//tool.add(makeJButton("BackgroundFrame.UNDO"));
		//tool.add(makeJButton("BackgroundFrame.REDO"));

		//tool.addSeparator();

		zoomButton = new JToggleButton(LGM.getIconForKey("BackgroundFrame.ZOOM"));
		zoomButton.setToolTipText(Messages.getString("BackgroundFrame.ZOOM"));
		zoomButton.addActionListener(this);
		zoomButton.setActionCommand("BackgroundFrame.ZOOM");
		tool.add(zoomButton);
		tool.add(makeJButton("BackgroundFrame.ZOOM_IN"));
		tool.add(makeJButton("BackgroundFrame.ZOOM_OUT"));

		tool.addSeparator();

		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("BackgroundFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		return tool;
		}

	private JPanel makeStatusBar()
		{
		JPanel status = new JPanel();
		BoxLayout layout = new BoxLayout(status,BoxLayout.X_AXIS);
		status.setLayout(layout);
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));

		statusLabel = new JLabel();

		status.add(statusLabel);

		return status;
		}

	private JPanel makeOptionsPanel()
		{
		JPanel panel = new JPanel(new BorderLayout());
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateContainerGaps(true);

		panel.setLayout(layout);

		transparent = new JCheckBox(Messages.getString("BackgroundFrame.TRANSPARENT")); //$NON-NLS-1$
		plf.make(transparent,PBackground.TRANSPARENT);
		smooth = new JCheckBox(Messages.getString("BackgroundFrame.SMOOTH")); //$NON-NLS-1$
		plf.make(smooth,PBackground.SMOOTH_EDGES);
		preload = new JCheckBox(Messages.getString("BackgroundFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PBackground.PRELOAD);
		tileset = new JCheckBox(Messages.getString("BackgroundFrame.USE_AS_TILESET")); //$NON-NLS-1$
		plf.make(tileset,PBackground.USE_AS_TILESET);

		panel.add(transparent);
		panel.add(smooth);
		panel.add(preload);
		panel.add(tileset);

		JPanel groupPanel = new JPanel();
		GroupLayout pLayout = new GroupLayout(groupPanel);
		groupPanel.setLayout(pLayout);
		String tileProps = Messages.getString("BackgroundFrame.TILE_PROPERTIES"); //$NON-NLS-1$
		groupPanel.setBorder(BorderFactory.createTitledBorder(tileProps));

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

		pLayout.setHorizontalGroup(pLayout.createSequentialGroup()
		/**/.addContainerGap(4,4)
		/**/.addGroup(pLayout.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(twLabel)
		/*		*/.addComponent(thLabel)
		/*		*/.addComponent(hoLabel)
		/*		*/.addComponent(voLabel)
		/*		*/.addComponent(hsLabel)
		/*		*/.addComponent(vsLabel))
		/**/.addGap(4)
		/**/.addGroup(pLayout.createParallelGroup()
		/*		*/.addComponent(tWidth,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(tHeight,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(hOffset,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(vOffset,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(hSep,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE)
		/*		*/.addComponent(vSep,PREFERRED_SIZE,DEFAULT_SIZE,DEFAULT_SIZE))
		/**/.addContainerGap(4,4));
		pLayout.setVerticalGroup(pLayout.createSequentialGroup()
		/**/.addGap(2)
		/**/.addGroup(pLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(twLabel)
		/*		*/.addComponent(tWidth))
		/**/.addGap(2)
		/**/.addGroup(pLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(thLabel)
		/*		*/.addComponent(tHeight))
		/**/.addGap(8)
		/**/.addGroup(pLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(hoLabel)
		/*		*/.addComponent(hOffset))
		/**/.addGap(2)
		/**/.addGroup(pLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(voLabel)
		/*		*/.addComponent(vOffset))
		/**/.addGap(8)
		/**/.addGroup(pLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(hsLabel)
		/*		*/.addComponent(hSep))
		/**/.addGap(2)
		/**/.addGroup(pLayout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(vsLabel)
		/*		*/.addComponent(vSep))
		/**/.addContainerGap(8,8));

		//groupPanel.setVisible(tileset.isSelected());
		panel.add(groupPanel);
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(tileset)
		/**/.addComponent(groupPanel));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(smooth)
		/**/.addComponent(preload)
		/**/.addComponent(transparent)
		/**/.addComponent(tileset)
		/**/.addGap(8)
		/**/.addComponent(groupPanel));

		return panel;
		}

	private void realizeScrollBarIncrement(JScrollPane scroll)
		{
		JScrollBar vertical = scroll.getVerticalScrollBar();
		JScrollBar horizontal = scroll.getHorizontalScrollBar();
		if (vertical != null)
			{
			vertical.setUnitIncrement((int) getZoom());
			}
		if (horizontal != null)
			{
			horizontal.setUnitIncrement((int) getZoom());
			}
		}

	private void updateScrollBars()
		{
		realizeScrollBarIncrement(previewScroll);
		}

	public double getZoom()
		{
		return preview.getZoom();
		}

	public void setZoom(double nzoom)
		{
		preview.setZoom(nzoom);
		updateStatusBar();
		updateScrollBars();
		}

	private void updateStatusBar()
		{
		String stat = " " + Messages.getString("BackgroundFrame.WIDTH") + ": " + res.getWidth() + " | "
				+ Messages.getString("BackgroundFrame.HEIGHT") + ": " + res.getHeight() + " | "
				+ Messages.getString("BackgroundFrame.MEMORY") + ": ";

		if (res.getBackgroundImage() != null)
			{
			stat += Util.formatDataSize(res.getSize());
			}
		else
			{
			stat += Util.formatDataSize(0);
			}
		String zoom = new DecimalFormat("#,##0.##").format(getZoom() * 100);
		stat += " | " + Messages.getString("BackgroundFrame.ZOOM") + ": " + zoom + "%";

		statusLabel.setText(stat);
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
	public void updateResource(boolean commit)
		{
		super.updateResource(commit);
		imageChanged = false;
		updateStatusBar();
		updateScrollBars();
		}

	public void handleToolBar(String cmd)
		{
		if (cmd.endsWith(".LOAD"))
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
		else if (cmd.endsWith(".SAVE"))
			{
			BufferedImage img = res.getBackgroundImage();
			// utility function will check if the image is null and display an appropriate warning
			// telling the user to create the image before saving
			Util.saveImage(img);
			return;
			}
		else if (cmd.endsWith(".CREATE"))
			{
			createNewImage(true);
			}
		else if (cmd.endsWith(".EDIT"))
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
				LGM.showDefaultExceptionHandler(ex);
				}
			return;
			}
		else if (cmd.endsWith(".EFFECT")) {
			List<BufferedImage> imgs = new ArrayList<BufferedImage>(1);
			imgs.add(res.getBackgroundImage());

			EffectsFrame ef = EffectsFrame.getInstance();
			ef.setEffectsListener(this, imgs);
			ef.setVisible(true);
		}
		else if (cmd.endsWith(".ZOOM"))
			{
			if (zoomButton.isSelected())
				{
				preview.setCursor(LGM.zoomCursor);
				preview.addMouseListener(mouseAdapter);
				}
			else
				{
				preview.removeMouseListener(mouseAdapter);
				preview.setCursor(Cursor.getDefaultCursor());
				}
			}
		else if (cmd.endsWith(".ZOOM_IN"))
			{
			zoomIn();
			return;
			}
		else if (cmd.endsWith(".ZOOM_OUT"))
			{
			zoomOut();
			return;
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		handleToolBar(e.getActionCommand());
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

	private BufferedImage createNewImage(boolean askforsize)
		{
		int width = 256;
		int height = 256;
		if (askforsize)
			{
			NumberFormatter nf = new NumberFormatter();
			nf.setMinimum(new Integer(1));
			JFormattedTextField wField = new JFormattedTextField(nf);
			wField.setValue(new Integer(width));
			JFormattedTextField hField = new JFormattedTextField(nf);
			hField.setValue(new Integer(height));

			JPanel myPanel = new JPanel();
			GridLayout layout = new GridLayout(0,2,0,3);
			myPanel.setLayout(layout);
			myPanel.add(new JLabel(Messages.getString("BackgroundFrame.NEW_WIDTH")));
			myPanel.add(wField);
			myPanel.add(new JLabel(Messages.getString("BackgroundFrame.NEW_HEIGHT")));
			myPanel.add(hField);

			int result = JOptionPane.showConfirmDialog(LGM.frame,myPanel,Messages.getString("BackgroundFrame.NEW_TITLE"),
					JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				{
				return null;
				}

			width = (Integer) wField.getValue();
			height = (Integer) hField.getValue();
			}
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		res.setBackgroundImage(bi);
		imageChanged = true;
		return bi;
		}

	private class BackgroundEditor implements UpdateListener
		{
		private FileChangeMonitor monitor;
		private File f;

		public BackgroundEditor() throws IOException
			{
			editor = this;
			start();
			}

		public void start() throws IOException
			{
			if (monitor != null)
				monitor.stop();

			if (f == null || !f.exists())
				{
				f = File.createTempFile(res.getName(),'.' + Prefs.externalBackgroundExtension,LGM.tempDir);
				f.deleteOnExit();
				}

			BufferedImage bi = res.getBackgroundImage();
			if (bi == null)
				bi = createNewImage(false);

			try (FileOutputStream out = new FileOutputStream(f))
				{
				ImageIO.write(bi,Prefs.externalBackgroundExtension,out);
				}

			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this);

			if (!Prefs.useExternalBackgroundEditor || Prefs.externalBackgroundEditorCommand == null)
				try
					{
					Desktop.getDesktop().edit(monitor.file);
					}
				catch (UnsupportedOperationException e)
					{
					LGM.showDefaultExceptionHandler(e);
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
					try (FileInputStream stream = new FileInputStream(monitor.file))
						{
							img = ImageIO.read(stream);
						}
					catch (IOException ioe)
						{
							LGM.showDefaultExceptionHandler(ioe);
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
		updateStatusBar();
		updateScrollBars();
		}

	private class BackgroundPropertyListener extends PropertyUpdateListener<PBackground>
		{
		public void updated(PropertyUpdateEvent<PBackground> e)
			{
			//TODO: Maybe remove this
			//USE_AS_TILESET
			//side2.setVisible((Boolean)
			//res.get(PBackground.USE_AS_TILESET));
			}
		}

	@Override
	public void applyEffects(List<BufferedImage> imgs)
		{
		res.setBackgroundImage(imgs.get(0));
		}
	}
