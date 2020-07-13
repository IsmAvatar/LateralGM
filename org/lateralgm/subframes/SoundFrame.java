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

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
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
	private JLabel fileLabel, memoryLabel, lPosition;
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
		if (clip == null) return;
		// the index is 0 based so the last frame is
		// one less than the number of frames
		int lastFrameIndex = clip.getFrameLength()-1;
		if (lastFrameIndex < 0) lastFrameIndex = 0;
		float playbackPercent = position.getValue() / 100.0f;
		clip.setFramePosition(Math.round(playbackPercent * lastFrameIndex));
		}

	// Match the playback slider with the clip.
	public void updatePlaybackPosition()
		{
		if (clip == null) return;
		// see updateClipPosition() comment
		int lastFrameIndex = clip.getFrameLength()-1;
		float pos = clip.getLongFramePosition();
		if (lastFrameIndex > 0)
			pos /= (float) lastFrameIndex;
		position.setValue(Math.round(pos * position.getMaximum()));
		}

	public SoundFrame(Sound res, ResNode node)
		{
		super(res,node);
		this.getRootPane().setDefaultButton(save);

		setLayout(new BorderLayout());

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

		JSplitPane orientationSplit = new JSplitPane();
		if (Prefs.rightOrientation) {
			orientationSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					makeRightPane(),makeLeftPane());
			orientationSplit.setResizeWeight(1d);
		} else {
			orientationSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					makeLeftPane(),makeRightPane());
		}
		add(orientationSplit, BorderLayout.CENTER);

		data = res.data;
		loadClip();

		pack();
		}

	private JButton makeJButton(String key, ImageIcon icon)
		{
		JButton button = new JButton(icon);
		button.setToolTipText(Messages.getString(key));
		button.addActionListener(this);
		return button;
		}
	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);

		tool.add(load = makeJButton("SoundFrame.LOAD",LOAD_ICON)); //$NON-NLS-1$
		tool.add(store = makeJButton("SoundFrame.STORE",STORE_ICON)); //$NON-NLS-1$
		tool.add(edit = makeJButton("SoundFrame.EDIT",EDIT_ICON)); //$NON-NLS-1$
		tool.addSeparator();
		tool.add(play = makeJButton("SoundFrame.PLAY",PLAY_ICON)); //$NON-NLS-1$
		tool.add(stop = makeJButton("SoundFrame.STOP",STOP_ICON)); //$NON-NLS-1$
		play.setEnabled(false);
		stop.setEnabled(false);

		return tool;
		}

	private JPanel makeLeftPane()
		{
		JPanel panel = new JPanel();

		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		JLabel nameLabel = new JLabel(Messages.getString("SoundFrame.NAME")); //$NON-NLS-1$

		preload = new JCheckBox(Messages.getString("SoundFrame.PRELOAD")); //$NON-NLS-1$
		preload.setOpaque(false);
		plf.make(preload,PSound.PRELOAD);

		fileLabel = new JLabel();
		memoryLabel = new JLabel();

		JPanel pKind = makeKindPane();
		JPanel pEffects = makeEffectsPane();

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(nameLabel)
		/*	*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(fileLabel,0,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addComponent(memoryLabel,0,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addComponent(preload)
		/**/.addComponent(pEffects,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(pKind,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(nameLabel)
		/*	*/.addComponent(name))
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addComponent(fileLabel)
		/**/.addComponent(memoryLabel)
		/**/.addComponent(preload)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addComponent(pKind)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED)
		/**/.addComponent(pEffects)
		/**/.addPreferredGap(ComponentPlacement.UNRELATED,8,Integer.MAX_VALUE)
		/**/.addComponent(save));

		panel.setLayout(layout);
		return panel;
		}

	private JPanel makeRightPane()
		{
		JPanel panel = new JPanel(new BorderLayout());

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
					if (position.getValueIsAdjusting())
						updateClipPosition();
					updatePositionLabel();
					}
			});
		// Update the playback slider at 16 millisecond intervals or 60hz.
		// Timer ensures that the component is only updated on the EDT.
		playbackTimer = new Timer(16,new ActionListener()
			{
			@Override
			public void actionPerformed(ActionEvent e)
				{
				if (position.getValueIsAdjusting()) return;
				updatePlaybackPosition();
				}
			});
		playbackTimer.setInitialDelay(0);

		save.setText(Messages.getString("ResourceFrame.SAVE")); //$NON-NLS-1$

		JPanel content = new JPanel();
		GroupLayout layout = new GroupLayout(content);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(pFormat)
		/**/.addComponent(lPan)
		/**/.addComponent(pan)
		/**/.addComponent(lVolume)
		/**/.addComponent(volume)
		/**/.addComponent(position));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(position)
		/**/.addComponent(lVolume)
		/**/.addComponent(volume)
		/**/.addComponent(lPan)
		/**/.addComponent(pan)
		/**/.addComponent(pFormat));

		content.setLayout(layout);

		JToolBar playbackToolBar = makeToolBar();
		playbackToolBar.add(lPosition);
		panel.add(playbackToolBar,BorderLayout.NORTH);
		panel.add(content,BorderLayout.CENTER);

		return panel;
		}

	private JPanel makeKindPane()
		{
		// The options must be added in the order corresponding to Sound.SoundKind
		final String kindOptions[] = { "SoundFrame.NORMAL", //$NON-NLS-1$
				"SoundFrame.BACKGROUND","SoundFrame.THREE", //$NON-NLS-1$ //$NON-NLS-2$
				"SoundFrame.MULT" }; //$NON-NLS-1$
		Messages.translate(kindOptions);

		JComboBox<String> kindCombo = new JComboBox<String>(kindOptions);
		plf.make(kindCombo,PSound.KIND,new KeyComboBoxConversion<SoundKind>(ProjectFile.SOUND_KIND,
			ProjectFile.SOUND_KIND_CODE));

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.KIND")));

		GroupLayout gl = new GroupLayout(panel);
		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addComponent(kindCombo));

		gl.setVerticalGroup(gl.createParallelGroup()
		/**/.addComponent(kindCombo,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE));

		panel.setLayout(gl);

		return panel;
		}

	private JPanel makeFormatPane()
		{
		JPanel pFormat = new JPanel();

		final String typeOptions[] = { "SoundFrame.MONO", //$NON-NLS-1$
				"SoundFrame.STEREO","SoundFrame.THREE" }; //$NON-NLS-1$ //$NON-NLS-2$
		Messages.translate(typeOptions);
		JComboBox<String> typeCombo = new JComboBox<String>(typeOptions);
		plf.make(typeCombo,PSound.TYPE,new KeyComboBoxConversion<SoundType>(ProjectFile.SOUND_TYPE,
			ProjectFile.SOUND_TYPE_CODE));

		final Integer depthOptions[] = { 8, 16 };
		JComboBox<Integer> depthCombo = new JComboBox<Integer>(depthOptions);
		plf.make(depthCombo,PSound.BIT_DEPTH,new DefaultComboBoxConversion<Integer>());

		final Integer sampleOptions[] = { 5512,11025,22050,32000,44100,48000 };
		JComboBox<Integer> sampleCombo = new JComboBox<Integer>(sampleOptions);
		plf.make(sampleCombo,PSound.SAMPLE_RATE,new DefaultComboBoxConversion<Integer>());
		JLabel sampleLabel = new JLabel(Messages.getString("SoundFrame.SAMPLERATE")); //$NON-NLS-1$

		ArrayList<Integer> bitOptions = new ArrayList<Integer>();
		for (int i = 8; i <= 512; i += 8 * Math.floor(Math.log(i) / Math.log(8)))
			{
			bitOptions.add(i);
			}
		JComboBox<Integer> bitCombo = new JComboBox<Integer>(
				bitOptions.toArray(new Integer[bitOptions.size()]));
		plf.make(bitCombo,PSound.BIT_RATE,new DefaultComboBoxConversion<Integer>());
		JLabel bitLabel = new JLabel(Messages.getString("SoundFrame.BITRATE")); //$NON-NLS-1$

		JCheckBox compressedCB = new JCheckBox(Messages.getString("SoundFrame.COMPRESSED")); //$NON-NLS-1$
		plf.make(compressedCB,PSound.COMPRESSED);
		JCheckBox streamedCB = new JCheckBox(Messages.getString("SoundFrame.STREAMED")); //$NON-NLS-1$
		plf.make(streamedCB,PSound.STREAMED);
		JCheckBox decompressCB = new JCheckBox(Messages.getString("SoundFrame.DECOMPRESS")); //$NON-NLS-1$
		plf.make(decompressCB,PSound.DECOMPRESS_ON_LOAD);

		GroupLayout aLayout = new GroupLayout(pFormat);
		aLayout.setAutoCreateGaps(true);
		aLayout.setAutoCreateContainerGaps(true);
		pFormat.setLayout(aLayout);
		pFormat.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.FORMAT"))); //$NON-NLS-1$
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
		eLayout.setAutoCreateContainerGaps(true);
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

		lPosition.setText(formatTime(position) + " / " + formatTime(length));
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
				data = Util.readFully(f);
				loadClip();
				}
			catch (Exception ex)
				{
				ex.printStackTrace();
				}
			modified = true;
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

	public void loadClip()
		{
		cleanup();
		position.setValue(0);
		playbackTimer.stop();
		play.setEnabled(false);
		updateStatusLabels();
		if (data == null || data.length <= 0)
			{
			updatePositionLabel();
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
							stop.setEnabled(false);
							play.setEnabled(true);
							playbackTimer.stop();
							// see updateClipPosition() comment
							int lastFrameIndex = clip.getFrameLength()-1;
							if (event.getFramePosition() < lastFrameIndex) return; 
							clip.setFramePosition(0);
							position.setValue(0);
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

	private void updateStatusLabels()
		{
		String fileName = res.get(PSound.FILE_NAME);
		String shortName = new File(fileName).getName();
		fileLabel.setText(Messages.format("SoundFrame.FILENAME",shortName)); //$NON-NLS-1$
		if (!fileName.isEmpty()) fileLabel.setToolTipText(fileName);

		long length = 0;
		if (res.data != null && res.data.length != 0)
			length = res.data.length;
		else if (data != null)
			length = data.length;

		String sizeString = Util.formatDataSize(length);
		memoryLabel.setText(Messages.format("SoundFrame.MEMORY",sizeString)); //$NON-NLS-1$
		memoryLabel.setToolTipText(sizeString);
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
			clip.flush();
			}
		clip = null;
		}
	}
