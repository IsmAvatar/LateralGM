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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
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
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
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
	//private JSlider pitch;
	private JSlider position;

	public String formatTime(long duration)
		{
		String formated = String.format(
				"%dm%ds",
				TimeUnit.MICROSECONDS.toMinutes(duration),
				TimeUnit.MICROSECONDS.toSeconds(duration)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(duration)));
		return formated;
		}

	public SoundFrame(Sound res, ResNode node)
		{
		super(res,node);
		setLayout(new BorderLayout());

		statusBar = makeStatusBar();
		add(statusBar,BorderLayout.SOUTH);
		add(makeToolBar(),BorderLayout.NORTH);
		JPanel content = new JPanel();
		add(content,BorderLayout.CENTER);

		GroupLayout layout = new GroupLayout(content);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		content.setLayout(layout);

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

		JPanel pKind = makeAttributesPane();
		JPanel pEffects = makeEffectsPane();
		JPanel pAttr = makeFormatPane();

		final JLabel lVolume = new JLabel(Messages.getString("SoundFrame.VOLUME") + ": 100"); //$NON-NLS-1$
		volume = new JSlider(0,100,100);
		//volume.setPaintLabels(true);
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
		//pan.setPaintLabels(true);
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

		lPosition = new JLabel(Messages.getString("SoundFrame.DURATION") + ": 0m0s | "
				+ Messages.getString("SoundFrame.POSITION") + ": 0m0s"); //$NON-NLS-1$
		position = new JSlider(0,100,0);
		//pan.setPaintLabels(true);
		position.setMajorTickSpacing(10);
		position.setMinorTickSpacing(2);
		position.setPaintTicks(true);
		position.addChangeListener(new ChangeListener()
			{

				public void stateChanged(ChangeEvent ev)
					{
					if (clip != null && position.getValueIsAdjusting())
						{
						clip.setFramePosition(
								(int) (((float) position.getValue() / 100) * clip.getFrameLength()));
						}
					updatePositionLabel();
					}

			});

		/* TODO: Not sure if this button is needed since I added the label listener
		 * and you can tell when you set it to 0.
		 */
		/*
		center = new JButton(Messages.getString("SoundFrame.PAN_CENTER")); //$NON-NLS-1$
		center.addActionListener(this);
		center.setPreferredSize(edit.getSize());
		*/

		data = res.data;
		loadClip();
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(lPosition))
		/*	*/.addComponent(position).addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(lVolume))
		/*	*/.addComponent(volume).addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(lPan))
		/*	*/.addComponent(pan).addGroup(layout.createSequentialGroup())
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(pKind)
		/*	*/.addComponent(pEffects)
		/*	*/.addComponent(pAttr,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lPosition).addGap(0))
		/*	*/.addComponent(position).addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lVolume).addGap(0))
		/*	*/.addComponent(volume).addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lPan).addGap(0))
		/*	*/.addComponent(pan)
		/**/.addGroup(layout.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(pKind)
		/*	*/.addComponent(pEffects)
		/*	*/.addComponent(pAttr)));

		layout.linkSize(SwingConstants.VERTICAL,pEffects,pAttr);

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

		edit = new JButton(EDIT_ICON); //$NON-NLS-1$
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

	private JPanel makeAttributesPane()
		{
		JPanel pAttr = new JPanel();
		// The options must be added in the order corresponding to Sound.SoundKind
		final String kindOptions[] = { Messages.getString("SoundFrame.NORMAL"),
				Messages.getString("SoundFrame.BACKGROUND"),Messages.getString("SoundFrame.THREE"),
				Messages.getString("SoundFrame.MULT") };

		JComboBox<String> kindCombo = new JComboBox<String>(kindOptions);
		plf.make(kindCombo,PSound.KIND,new KeyComboBoxConversion<SoundKind>(ProjectFile.SOUND_KIND,
			ProjectFile.SOUND_KIND_CODE));
		JLabel kindLabel = new JLabel(Messages.getString("SoundFrame.KIND"));

		JCheckBox compressedCB = new JCheckBox(Messages.getString("SoundFrame.COMPRESSED"));
		plf.make(compressedCB,PSound.COMPRESSED);
		JCheckBox streamedCB = new JCheckBox(Messages.getString("SoundFrame.STREAMED"));
		plf.make(streamedCB,PSound.STREAMED);
		JCheckBox decompressCB = new JCheckBox(Messages.getString("SoundFrame.DECOMPRESS"));
		plf.make(decompressCB,PSound.DECOMPRESS_ON_LOAD);

		GroupLayout aLayout = new GroupLayout(pAttr);
		pAttr.setLayout(aLayout);
		aLayout.setHorizontalGroup(aLayout.createParallelGroup()
		/**/.addGroup(aLayout.createSequentialGroup()
		/*  */.addComponent(kindLabel)
		/*  */.addComponent(kindCombo,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE))
		/**/.addComponent(compressedCB)
		/**/.addComponent(streamedCB)
		/**/.addComponent(decompressCB));
		aLayout.setVerticalGroup(aLayout.createSequentialGroup()
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(kindLabel)
		/*  */.addComponent(kindCombo))
		/**/.addComponent(compressedCB)
		/**/.addComponent(streamedCB)
		/**/.addComponent(decompressCB));
		return pAttr;
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

		GroupLayout aLayout = new GroupLayout(pFormat);
		aLayout.setAutoCreateGaps(true);
		pFormat.setLayout(aLayout);
		pFormat.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.FORMAT")));
		aLayout.setHorizontalGroup(aLayout.createSequentialGroup()
		/**/.addGroup(aLayout.createParallelGroup()
		/*  */.addComponent(typeCombo)
		/*  */.addComponent(depthCombo))
		/**/.addGroup(aLayout.createParallelGroup(Alignment.TRAILING)
		/*  */.addComponent(sampleLabel)
		/*  */.addComponent(bitLabel))
		/**/.addGroup(aLayout.createParallelGroup()
		/*  */.addComponent(sampleCombo)
		/*  */.addComponent(bitCombo)));
		aLayout.setVerticalGroup(aLayout.createSequentialGroup()
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(typeCombo)
		/*  */.addComponent(sampleLabel)
		/*  */.addComponent(sampleCombo))
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(depthCombo)
		/*  */.addComponent(bitLabel)
		/*  */.addComponent(bitCombo)));

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
				data = Util.readFully(f);
				//loadClip();
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
				updateStatusLabel();
				if (clip != null && clip.isOpen())
					{
					clip.stop();
					clip.close();
					clip.flush();
					}
				clip = null;
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
			clip.setFramePosition((int) (((float) position.getValue() / 100) * clip.getFrameLength()));
			clip.start();
			new Thread()
				{
					public void run()
						{
						LGM.addDefaultExceptionHandler();
						while (clip != null && clip.isActive())
							{
							float pos = (float) clip.getLongFramePosition() / (float) clip.getFrameLength();

							position.setValue((int) (pos * position.getMaximum()));
							try
								{
								Thread.sleep(50);
								}
							catch (InterruptedException e)
								{
								if (clip.isOpen()) clip.stop();
								break;
								}
							}

						if (clip != null) {
							clip.setFramePosition(0);
							play.setEnabled(true);
						}
						position.setValue(0);
						stop.setEnabled(false);
						}
				}.start();
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
		play.setEnabled(false);
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
						updateStatusLabel();
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
