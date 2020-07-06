/*
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014, Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sound.SoundKind;
import org.lateralgm.resources.Sound.SoundType;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.DefaultComboBoxConversion;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.KeyComboBoxConversion;
import org.lateralgm.ui.swing.util.SwingExecutor;

public class SoundFrame extends InstantiableResourceFrame<Sound,PSound>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("SoundFrame.LOAD"); //$NON-NLS-1$
	private static final ImageIcon PLAY_ICON = LGM.getIconForKey("SoundFrame.PLAY"); //$NON-NLS-1$
	private static final ImageIcon STOP_ICON = LGM.getIconForKey("SoundFrame.STOP"); //$NON-NLS-1$
	private static final ImageIcon STORE_ICON = LGM.getIconForKey("SoundFrame.STORE"); //$NON-NLS-1$
	private static final ImageIcon EDIT_ICON = LGM.getIconForKey("SoundFrame.EDIT"); //$NON-NLS-1$

	public JButton load;
	public JButton play;
	public JButton stop;
	public JButton store;
	public JSlider volume;
	public JSlider pan;
	public JButton center;
	public JCheckBox preload;
	public JButton edit;
	public byte[] data;
	public boolean modified = false;
	private CustomFileChooser fc = new CustomFileChooser("/org/lateralgm","LAST_SOUND_DIR");
	private SoundEditor editor;
	private Clip clip;
	private JLabel statusLabel, lPosition;
	private JPanel statusBar;
	private JSlider position;
	private Timer playbackTimer;

	public String formatTime(long duration)
		{
		String formated = String.format(
				"%dm%ds",
				TimeUnit.MICROSECONDS.toMinutes(duration),
				TimeUnit.MICROSECONDS.toSeconds(duration)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(duration)));
		return formated;
		}

	// Match the clip with the playback slider.
	public void updateClipPosition()
		{
		updatePositionLabel();
		if (clip == null || !position.getValueIsAdjusting()) return;
		// the index is 0 based so the last frame is
		// one less than the number of frames
		int lastFrameIndex = clip.getFrameLength()-1;
		float playbackPercent = position.getValue() / 100.0f;
		clip.setFramePosition((int) (playbackPercent * lastFrameIndex));
		}

	// Match the playback slider with the clip.
	public void updatePlaybackPosition()
		{
		if (clip == null || position.getValueIsAdjusting()) return;
		// see updateClipPosition() comment
		int lastFrameIndex = clip.getFrameLength()-1;
		float pos = clip.getLongFramePosition();
		if (lastFrameIndex > 0)
			pos /= (float) lastFrameIndex;
		position.setValue((int) (pos * position.getMaximum()));
		}

	public SoundFrame(Sound res, ResNode node)
		{
		super(res,node);
		setLayout(new BorderLayout());

		statusBar = makeStatusBar();
		add(statusBar,BorderLayout.SOUTH);
		add(makeToolBar(),BorderLayout.NORTH);

		String s[] = { ".ogg",".wav",".mid",".mp3",".mod",".xm",".s3m",".it",".nfs",".gfs",".minigfs",
				".flac" };
		String[] d = { Messages.getString("SoundFrame.FORMAT_SOUND"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_OGG"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_WAV"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_MID"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_MP3") }; //$NON-NLS-1$

		CustomFileFilter soundsFilter = new CustomFileFilter(d[0],s);
		fc.addChoosableFileFilter(soundsFilter);
		fc.addChoosableFileFilter(new CustomFileFilter(d[1],s[0]));
		fc.addChoosableFileFilter(new CustomFileFilter(d[2],s[1]));
		fc.addChoosableFileFilter(new CustomFileFilter(d[3],s[2]));
		fc.addChoosableFileFilter(new CustomFileFilter(d[4],s[3]));
		fc.setFileFilter(soundsFilter);

		JPanel pKind = makeKindPane();
		JPanel pEffects = makeEffectsPane();
		JPanel pFormat = makeFormatPane();

		final JLabel lVolume = new JLabel(Messages.getString("SoundFrame.VOLUME") + ": 100"); //$NON-NLS-1$
		volume = new JSlider(0,100,100);
		volume.setMajorTickSpacing(10);
		volume.setPaintTicks(true);
		volume.setSize(new Dimension(50,50));
		volume.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent ev)
					{
					lVolume.setText(Messages.getString("SoundFrame.VOLUME") + ": " + volume.getValue());
					}
			});
		plf.make(volume.getModel(),PSound.VOLUME,100.0);

		final JLabel lPan = new JLabel(Messages.getString("SoundFrame.PAN") + ": 0"); //$NON-NLS-1$
		pan = new JSlider(-100,100,0);
		pan.setMajorTickSpacing(20);
		pan.setPaintTicks(true);
		pan.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent ev)
					{
					lPan.setText(Messages.getString("SoundFrame.PAN") + ": " + pan.getValue());
					}
			});
		plf.make(pan.getModel(),PSound.PAN,100.0);

		lPosition = new JLabel();
		updatePositionLabel();
		position = new JSlider(0,100,0);
		position.setMajorTickSpacing(10);
		position.setMinorTickSpacing(2);
		position.setPaintTicks(true);
		position.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent ev)
					{
					updateClipPosition();
					}
			});
		// Update the playback slider at 16 millisecond intervals or 60hz.
		// Timer ensures that the component is only updated on the EDT.
		playbackTimer = new Timer(16,new ActionListener()
			{
			@Override
			public void actionPerformed(ActionEvent e)
				{
				updatePlaybackPosition();
				}
			});
		playbackTimer.setInitialDelay(0);

		data = res.data;
		loadClip();

		JPanel content = new JPanel();
		GroupLayout layout = new GroupLayout(content);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(lPosition)
		/*	*/.addComponent(position)
		/*	*/.addComponent(lVolume)
		/*	*/.addComponent(volume)
		/*	*/.addComponent(lPan)
		/*	*/.addComponent(pan))
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(pKind)
		/*	*/.addComponent(pEffects)
		/*	*/.addComponent(pFormat,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(lPosition)
		/*	*/.addComponent(position)
		/*	*/.addComponent(lVolume)
		/*	*/.addComponent(volume)
		/*	*/.addComponent(lPan)
		/*	*/.addComponent(pan))
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(pKind)
		/*	*/.addComponent(pEffects)
		/*	*/.addComponent(pFormat)));

		layout.linkSize(SwingConstants.VERTICAL,pKind,pEffects,pFormat);
		content.setLayout(layout);
		add(content,BorderLayout.CENTER);

		pack();
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);

		tool.add(save);
		tool.addSeparator();

		load = new JButton(LOAD_ICON);
		load.setToolTipText(Messages.getString("SoundFrame.LOAD")); //$NON-NLS-1$
		load.addActionListener(this);
		tool.add(load);

		store = new JButton(STORE_ICON);
		store.setToolTipText(Messages.getString("SoundFrame.STORE")); //$NON-NLS-1$
		store.addActionListener(this);
		tool.add(store);

		tool.addSeparator();

		edit = new JButton(EDIT_ICON);
		edit.setToolTipText(Messages.getString("SoundFrame.EDIT"));
		edit.addActionListener(this);
		tool.add(edit);

		play = new JButton(PLAY_ICON);
		play.setToolTipText(Messages.getString("SoundFrame.PLAY"));
		play.addActionListener(this);
		play.setEnabled(false);
		tool.add(play);

		stop = new JButton(STOP_ICON);
		stop.setToolTipText(Messages.getString("SoundFrame.STOP"));
		stop.addActionListener(this);
		stop.setEnabled(false);
		tool.add(stop);

		tool.addSeparator();

		preload = new JCheckBox(Messages.getString("SoundFrame.PRELOAD")); //$NON-NLS-1$
		preload.setOpaque(false);
		plf.make(preload,PSound.PRELOAD);
		tool.add(preload);

		tool.addSeparator();

		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("SoundFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		return tool;
		}

	private JPanel makeKindPane()
		{
		ButtonGroup g = new ButtonGroup();
		// The buttons must be added in the order corresponding to Sound.SoundKind.
		AbstractButton kNormal = new JRadioButton(Messages.getString("SoundFrame.NORMAL")); //$NON-NLS-1$
		g.add(kNormal);
		AbstractButton kBackground = new JRadioButton(Messages.getString("SoundFrame.BACKGROUND")); //$NON-NLS-1$
		g.add(kBackground);
		AbstractButton k3d = new JRadioButton(Messages.getString("SoundFrame.THREE")); //$NON-NLS-1$
		g.add(k3d);
		AbstractButton kMult = new JRadioButton(Messages.getString("SoundFrame.MULT")); //$NON-NLS-1$
		g.add(kMult);
		plf.make(g,PSound.KIND,SoundKind.class);
		JPanel pKind = new JPanel();
		pKind.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.KIND")));
		pKind.setLayout(new BoxLayout(pKind,BoxLayout.PAGE_AXIS));
		for (Enumeration<AbstractButton> e = g.getElements(); e.hasMoreElements();)
			pKind.add(e.nextElement());
		return pKind;
		}

	private JPanel makeFormatPane()
		{
		JPanel pFormat = new JPanel();

		final String typeOptions[] = { Messages.getString("SoundFrame.MONO"),
				Messages.getString("SoundFrame.STEREO"),Messages.getString("SoundFrame.THREE") };
		JComboBox<String> typeCombo = new JComboBox<String>(typeOptions);
		plf.make(typeCombo,PSound.TYPE,new KeyComboBoxConversion<SoundType>(ProjectFile.SOUND_TYPE,
			ProjectFile.SOUND_TYPE_CODE));

		final Integer depthOptions[] = { 8, 16 };
		JComboBox<Integer> depthCombo = new JComboBox<Integer>(depthOptions);
		plf.make(depthCombo,PSound.BIT_DEPTH,new DefaultComboBoxConversion<Integer>());

		final Integer sampleOptions[] = { 5512,11025,22050,32000,44100,48000 };
		JComboBox<Integer> sampleCombo = new JComboBox<Integer>(sampleOptions);
		plf.make(sampleCombo,PSound.SAMPLE_RATE,new DefaultComboBoxConversion<Integer>());
		JLabel sampleLabel = new JLabel(Messages.getString("SoundFrame.SAMPLERATE"));

		ArrayList<Integer> bitOptions = new ArrayList<Integer>();
		for (int i = 8; i <= 512; i += 8 * Math.floor(Math.log(i) / Math.log(8)))
			{
			bitOptions.add(i);
			}
		JComboBox<Integer> bitCombo = new JComboBox<Integer>(
				bitOptions.toArray(new Integer[bitOptions.size()]));
		plf.make(bitCombo,PSound.BIT_RATE,new DefaultComboBoxConversion<Integer>());
		JLabel bitLabel = new JLabel(Messages.getString("SoundFrame.BITRATE"));

		JCheckBox compressedCB = new JCheckBox(Messages.getString("SoundFrame.COMPRESSED"));
		plf.make(compressedCB,PSound.COMPRESSED);
		JCheckBox streamedCB = new JCheckBox(Messages.getString("SoundFrame.STREAMED"));
		plf.make(streamedCB,PSound.STREAMED);
		JCheckBox decompressCB = new JCheckBox(Messages.getString("SoundFrame.DECOMPRESS"));
		plf.make(decompressCB,PSound.DECOMPRESS_ON_LOAD);

		GroupLayout aLayout = new GroupLayout(pFormat);
		aLayout.setAutoCreateGaps(true);
		pFormat.setLayout(aLayout);
		pFormat.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.FORMAT")));
		aLayout.setHorizontalGroup(aLayout.createParallelGroup()
		/**/.addGroup(aLayout.createSequentialGroup()
		/*  */.addGroup(aLayout.createParallelGroup()
		/*    */.addComponent(typeCombo)
		/*    */.addComponent(depthCombo))
		/*  */.addGroup(aLayout.createParallelGroup(Alignment.TRAILING)
		/*    */.addComponent(sampleLabel)
		/*    */.addComponent(bitLabel))
		/*  */.addGroup(aLayout.createParallelGroup()
		/*    */.addComponent(sampleCombo)
		/*    */.addComponent(bitCombo)))
		/**/.addGroup(aLayout.createSequentialGroup()
		/*  */.addComponent(compressedCB)
		/*  */.addComponent(streamedCB)
		/*  */.addComponent(decompressCB)));
		aLayout.setVerticalGroup(aLayout.createSequentialGroup()
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(typeCombo)
		/*  */.addComponent(sampleLabel)
		/*  */.addComponent(sampleCombo))
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(depthCombo)
		/*  */.addComponent(bitLabel)
		/*  */.addComponent(bitCombo))
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(compressedCB)
		/*  */.addComponent(streamedCB)
		/*  */.addComponent(decompressCB)));

		return pFormat;
		}

	private JPanel makeEffectsPane()
		{
		// these are in bit order as appears in a GM6 file, not the same as GM shows them
		//effects = new IndexButtonGroup(5,false);
		AbstractButton eChorus = new JCheckBox(Messages.getString("SoundFrame.CHORUS")); //$NON-NLS-1$
		plf.make(eChorus,PSound.CHORUS);
		AbstractButton eEcho = new JCheckBox(Messages.getString("SoundFrame.ECHO")); //$NON-NLS-1$
		plf.make(eEcho,PSound.ECHO);
		AbstractButton eFlanger = new JCheckBox(Messages.getString("SoundFrame.FLANGER")); //$NON-NLS-1$
		plf.make(eFlanger,PSound.FLANGER);
		AbstractButton eGargle = new JCheckBox(Messages.getString("SoundFrame.GARGLE")); //$NON-NLS-1$
		plf.make(eGargle,PSound.GARGLE);
		AbstractButton eReverb = new JCheckBox(Messages.getString("SoundFrame.REVERB")); //$NON-NLS-1$
		plf.make(eReverb,PSound.REVERB);
		JPanel pEffects = new JPanel();
		GroupLayout eLayout = new GroupLayout(pEffects);
		eLayout.setAutoCreateGaps(true);
		pEffects.setLayout(eLayout);
		pEffects.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.EFFECTS")));
		eLayout.setHorizontalGroup(eLayout.createSequentialGroup()
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eChorus)
		/*		*/.addComponent(eFlanger)
		/*		*/.addComponent(eReverb))
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eEcho)
		/*		*/.addComponent(eGargle)));
		eLayout.setVerticalGroup(eLayout.createSequentialGroup()
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eChorus)
		/*		*/.addComponent(eEcho))
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eFlanger)
		/*		*/.addComponent(eGargle))
		/**/.addComponent(eReverb));
		return pEffects;
		}

	private void updatePositionLabel()
		{
		long length = 0, position = 0;
		if (clip != null)
			{
			length = clip.getMicrosecondLength();
			position = clip.getMicrosecondPosition();
			}

		lPosition.setText(Messages.getString("SoundFrame.DURATION") + ": " + formatTime(length)
				+ " | " + Messages.getString("SoundFrame.POSITION") + ": " + formatTime(position));
		}

	protected boolean areResourceFieldsEqual()
		{
		return !modified;
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		res.data = data;
		}

	public void updateResource(boolean commit)
		{
		super.updateResource(commit);
		modified = false;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			File f;
			while (true)
				{
				if (fc.showOpenDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
				f = fc.getSelectedFile();
				if (f.exists()) break;
				JOptionPane.showMessageDialog(fc,
						f.getName() + Messages.getString("SoundFrame.FILE_MISSING"), //$NON-NLS-1$
						Messages.getString("SoundFrame.FILE_OPEN"),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				}
			try
				{
				String fn = f.getName();
				String extension = "";

				int i = fn.lastIndexOf('.');
				if (i > 0)
					{
					extension = fn.substring(i + 1);
					}
				// Set multi-media player for mp3's like Game Maker 8.1 one did for DirectSound
				// is ignored in OpenAL anyway so it don't matter.
				if (extension.toLowerCase().equals("mp3"))
					{
					res.put(PSound.KIND,Sound.SoundKind.MULTIMEDIA);
					}
				res.put(PSound.FILE_NAME,fn);
				String ft = CustomFileFilter.getExtension(fn);
				if (ft == null) ft = "";
				res.put(PSound.FILE_TYPE,ft);
				if (clip != null && clip.isOpen())
					{
					clip.stop();
					clip.close();
					clip.flush();
					}
				clip = null;
				data = Util.readFully(f);
				loadClip();
				}
			catch (Exception ex)
				{
				ex.printStackTrace();
				}
			modified = true;
			cleanup();
			return;
			}
		if (e.getSource() == play)
			{
			if (data == null || data.length == 0) return;
			if (clip == null)
				{
				loadClip();
				}
			play.setEnabled(false);
			stop.setEnabled(true);
			playbackTimer.start();
			updateClipPosition();
			clip.start();
			return;
			}
		if (e.getSource() == stop)
			{
			if (clip != null && clip.isOpen()) clip.stop();
			return;
			}
		if (e.getSource() == store)
			{
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			try
				{
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
						fc.getSelectedFile()));
				out.write(data);
				out.close();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			return;
			}
		if (e.getSource() == edit)
			{
			try
				{
				if (editor == null)
					new SoundEditor();
				else
					editor.start();
				}
			catch (IOException ex)
				{
				LGM.showDefaultExceptionHandler(ex);
				}
			return;
			}
		if (e.getSource() == center)
			{
			pan.setValue(0);
			return;
			}
		super.actionPerformed(e);
		}

	private JPanel makeStatusBar()
		{
		JPanel status = new JPanel(new FlowLayout());
		BoxLayout layout = new BoxLayout(status,BoxLayout.X_AXIS);
		status.setLayout(layout);
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));

		statusLabel = new JLabel();
		status.add(statusLabel);

		updateStatusLabel();

		return status;
		}

	public void loadClip()
		{
		playbackTimer.stop();
		play.setEnabled(false);
		updateStatusLabel();
		if (data == null || data.length <= 0)
			{
			return;
			}
		try
			{
			InputStream source = new ByteArrayInputStream(data);
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(source));
			AudioFormat fmt = ais.getFormat();
			//Forcibly convert to PCM Signed because non-pulse can't play unsigned (Java bug)
			if (fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
				{
				fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fmt.getSampleRate(),
						fmt.getSampleSizeInBits() * 2,fmt.getChannels(),fmt.getFrameSize() * 2,
						fmt.getFrameRate(),true);
				ais = AudioSystem.getAudioInputStream(fmt,ais);
				}
			//Clip c = AudioSystem.getClip() generates a bogus format instead of using ais.getFormat.
			clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class,fmt));

			clip.open(ais);
			clip.addLineListener(new LineListener()
				{

					@Override
					public void update(LineEvent event)
						{
						if (event.getType() == LineEvent.Type.STOP)
							{
							playbackTimer.stop();
							clip.setFramePosition(0);
							position.setValue(0);
							stop.setEnabled(false);
							play.setEnabled(true);
							}
						}

				});
			play.setEnabled(true);
			}
		catch (IOException e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		catch (LineUnavailableException e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		catch (UnsupportedAudioFileException e)
			{
			// do nothing, file was unsupported
			}
		updatePositionLabel();
		}

	private void updateStatusLabel()
		{
		String stat = " " + Messages.getString("SoundFrame.FILENAME") + ": "
				+ res.get(PSound.FILE_NAME) + " | " + Messages.getString("SoundFrame.MEMORY") + ": ";

		if (res.data != null && res.data.length != 0)
			{
			stat += Util.formatDataSize(res.data.length);
			}
		else if (data != null)
			{
			stat += Util.formatDataSize(data.length);
			}
		else
			{
			stat += Util.formatDataSize(0);
			}

		statusLabel.setText(stat);
		}

	private class SoundEditor implements UpdateListener
		{
		private FileChangeMonitor monitor;
		private File f;

		public SoundEditor() throws IOException
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
				f = File.createTempFile(res.getName(),
						new File((String) res.get(PSound.FILE_NAME)).getName(),LGM.tempDir);
				f.deleteOnExit();
				}

			try (FileOutputStream out = new FileOutputStream(f))
				{
				out.write(data);
				}

			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this);

			if (!Prefs.useExternalSoundEditor || Prefs.externalSoundEditorCommand == null)
				Util.OpenDesktopEditor(monitor.file);
			else
				Runtime.getRuntime().exec(
						String.format(Prefs.externalSoundEditorCommand,monitor.file.getAbsolutePath()));
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
					try
						{
						data = Util.readFully(monitor.file);
						loadClip();
						}
					catch (IOException ioe)
						{
						LGM.showDefaultExceptionHandler(ioe);
						return;
						}
					modified = true;
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
		if (clip != null && clip.isOpen())
			{
			clip.stop();
			clip.close();
			}
		clip = null;
		}
	}
