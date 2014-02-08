/*
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
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

public class BackgroundFrame extends InstantiableResourceFrame<Background,PBackground> implements
		UpdateListener
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
	
	public MouseMotionListener mouseMotionListener = null;
	public MouseListener mouseListener = null;

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
	public void zoomIn(Point point) {
	    this.setZoom(this.getZoom() * 1.1f);
	    Point pos = previewScroll.getViewport().getViewPosition();

	    int newX = (int)(point.x*(1.1f - 1f) + 1.1f*pos.x);
	    int newY = (int)(point.y*(1.1f - 1f) + 1.1f*pos.y);
	    previewScroll.getViewport().setViewPosition(new Point(newX, newY));

	    previewScroll.revalidate();
	    previewScroll.repaint();
	}
	
	/** Zoom out, centering around a specific point, usually the mouse. */
	public void zoomOut(Point point) {
	    this.setZoom(this.getZoom() * 0.9f);
	    Point pos = previewScroll.getViewport().getViewPosition();

	    int newX = (int)(point.x*(0.9f - 1f) + 0.9f*pos.x);
	    int newY = (int)(point.y*(0.9f - 1f) + 0.9f*pos.y);
	    previewScroll.getViewport().setViewPosition(new Point(newX, newY));

	    previewScroll.revalidate();
	    previewScroll.repaint();
		}
	
	public BackgroundFrame(Background res, ResNode node)
		{
		super(res,node);
		res.properties.getUpdateSource(PBackground.USE_AS_TILESET).addListener(bpl);
		res.reference.updateSource.addListener(this);
		
		this.setLayout(new BorderLayout());

		preview = new BackgroundPreview(res);
		preview.setVerticalAlignment(SwingConstants.TOP);
		
		mouseMotionListener = new MouseMotionListener() {
		
    public void mouseMoved(MouseEvent e) {
        final int x = e.getX();
        final int y = e.getY();
        // only display a hand if the cursor is over the items
        final Rectangle cellBounds = preview.getBounds();
        if (cellBounds != null && cellBounds.contains(x, y)) {
            //preview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
           // 
        }
    }

    public void mouseDragged(MouseEvent e) {
    }
		};
		
		mouseListener = new MouseListener() {

		public void mouseClicked(MouseEvent ev)
			{
			// TODO Auto-generated method stub

			}

		public void mouseEntered(MouseEvent ev)
			{
			// TODO Auto-generated method stub

			preview.setCursor(LGM.zoomCursor);
			}

		public void mouseExited(MouseEvent ev)
			{
			// TODO Auto-generated method stub
			preview.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		public void mousePressed(MouseEvent ev)
			{
			// TODO Auto-generated method stub
			if (ev.getButton() == MouseEvent.BUTTON1) {
				preview.setCursor(LGM.zoomInCursor);
			}
			if (ev.getButton() == MouseEvent.BUTTON3) {
				preview.setCursor(LGM.zoomOutCursor);
			}
			}

		public void mouseReleased(MouseEvent ev)
			{
			// TODO Auto-generated method stub
			if (ev.getButton() == MouseEvent.BUTTON1) {
				zoomIn(ev.getPoint());
			}
			if (ev.getButton() == MouseEvent.BUTTON3) {
				zoomOut(ev.getPoint());
			}
			preview.setCursor(LGM.zoomCursor);
			}
		};
		
		previewScroll = new JScrollPane(preview);
		
		this.add(makeToolBar(), BorderLayout.NORTH);
		this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeOptionsPanel(), previewScroll), BorderLayout.CENTER);
		this.add(makeStatusBar(), BorderLayout.SOUTH);

		updateStatusLabel();
		updateScrollBars();

		pack();
		this.setSize(600, 400);
		}
	
  private JButton makeJButton(String key) {
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
		tool.add(makeJButton("BackgroundFrame.LOAD"));
		tool.add(makeJButton("BackgroundFrame.CREATE"));
		tool.add(makeJButton("BackgroundFrame.EDIT"));
		tool.add(makeJButton("BackgroundFrame.EFFECT"));
		
		tool.addSeparator();
		
		tool.add(makeJButton("BackgroundFrame.UNDO"));
		tool.add(makeJButton("BackgroundFrame.REDO"));

		tool.addSeparator();
		
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
	
	private JPanel makeStatusBar() {
		JPanel status = new JPanel(new FlowLayout());
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
	
	private void realizeScrollBarIncrement(JScrollPane scroll, Dimension size)
		{
		JScrollBar vertical = scroll.getVerticalScrollBar();
		JScrollBar horizontal = scroll.getHorizontalScrollBar();
		if (vertical != null) {
			vertical.setUnitIncrement((int) (size.getHeight() / 5));
		}
		if (horizontal != null) {
			horizontal.setUnitIncrement((int) (size.getHeight() / 5));
		}
		}
	
	private void updateScrollBars()
		{
		realizeScrollBarIncrement(previewScroll, previewScroll.getPreferredSize());
		}

	public double getZoom() {
		return preview.getZoom();
	}

	public void setZoom(double nzoom) {
		preview.setZoom(nzoom);
		updateStatusLabel();
		updateScrollBars();
	}
	
	public void zoomIn() {
		double zoom = getZoom();
		if (zoom < 5) {
			setZoom(getZoom() * 2);
		}
		return;
	}
	
	public void zoomOut() {
		double zoom = getZoom();
		if (zoom > 0.125) {
			setZoom(getZoom() / 2);
		}
		return;
	}
	
	public static String formatData(long bytes) {
		if (bytes <= 0) return "0 B";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digits = (int) (Math.log(bytes)/Math.log(1024));
		return new DecimalFormat("#,##0.##").format(bytes/Math.pow(1024, digits)) + " " + 
			units[digits];
	}
	
	private void updateStatusLabel() {
		String stat = " " + Messages.getString("BackgroundFrame.WIDTH") + ": " + res.getWidth() + " | " +
				Messages.getString("BackgroundFrame.HEIGHT") + ": " + res.getHeight() 
				+ " | " + Messages.getString("BackgroundFrame.MEMORY") + ": ";
		
		if (res.getBackgroundImage() != null) {
			stat += formatData(res.getSize());
		} else {
			stat += formatData(0);
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
	public void updateResource()
		{
		super.updateResource();
		imageChanged = false;
		updateStatusLabel();
		updateScrollBars();
		}

	public void handleToolBar(String cmd) {
	if (cmd.endsWith(".LOAD")) {
		BufferedImage img = Util.getValidImage();
		if (img != null)
			{
			res.setBackgroundImage(img);
			imageChanged = true;
			cleanup();
			}
		return;
	} else if (cmd.endsWith(".CREATE")) {
		createNewImage(true);
	} else if (cmd.endsWith(".EDIT")) {
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
	else if (cmd.endsWith(".ZOOM")) 
	{
		if (zoomButton.isSelected()) {
			preview.addMouseListener(mouseListener);
			preview.addMouseMotionListener(mouseMotionListener);
		} else {
			preview.removeMouseListener(mouseListener);
			preview.removeMouseMotionListener(mouseMotionListener);
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
		if (askforsize) { 
	    JFormattedTextField wField = new JFormattedTextField();
	    wField.setValue(new Integer(width));
	    JFormattedTextField hField = new JFormattedTextField();
	    hField.setValue(new Integer(height));
	    
	    JPanel myPanel = new JPanel();
	    GridLayout layout = new GridLayout(0, 2);
	    myPanel.setLayout(layout);
	    myPanel.add(new JLabel("Width:"));
	    myPanel.add(wField);
	    //myPanel.add(Box.createHorizontalStrut(15)); // a spacer
	    myPanel.add(new JLabel("Height:"));
	    myPanel.add(hField);

	    int result = JOptionPane.showConfirmDialog(null, myPanel, 
	        "Enter Size of New Image", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.CANCEL_OPTION) {
	    	return null;
	    }
	
	    width = (Integer)wField.getValue();
	    height = (Integer)hField.getValue();
		}
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = bi.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width,height);
		res.setBackgroundImage(bi);
		imageChanged = true;
		return bi;
		}

	private class BackgroundEditor implements UpdateListener
		{
		public final FileChangeMonitor monitor;

		public BackgroundEditor() throws IOException
			{
			BufferedImage bi = res.getBackgroundImage();
			if (bi == null) {
				bi = createNewImage(false);
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
			updateStatusLabel();
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
	}
