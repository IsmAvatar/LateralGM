/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007, 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.compare.ResourceComparator;
import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sound.SoundKind;

public class SoundFrame extends ResourceFrame<Sound,PSound>
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
	public JLabel filename;
	public JSlider volume;
	public JSlider pan;
	public JCheckBox preload;
	public JButton edit;
	public byte[] data;
	public boolean modified = false;
	private CustomFileChooser fc = new CustomFileChooser("/org/lateralgm","LAST_SOUND_DIR");

	public SoundFrame(Sound res, ResNode node)
		{
		super(res,node);
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);

		String s[] = { ".wav",".mid",".mp3" };
		String[] d = { Messages.getString("SoundFrame.FORMAT_SOUND"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_WAV"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_MID"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_MP3") }; //$NON-NLS-1$

		CustomFileFilter soundsFilter = new CustomFileFilter(s,d[0]);
		fc.addChoosableFileFilter(soundsFilter);
		fc.addChoosableFileFilter(new CustomFileFilter(s[0],d[1]));
		fc.addChoosableFileFilter(new CustomFileFilter(s[1],d[2]));
		fc.addChoosableFileFilter(new CustomFileFilter(s[2],d[3]));
		fc.setFileFilter(soundsFilter);

		setResizable(false);
		setMaximizable(false);

		JLabel lName = new JLabel(Messages.getString("SoundFrame.NAME")); //$NON-NLS-1$

		load = new JButton(Messages.getString("SoundFrame.LOAD"),LOAD_ICON); //$NON-NLS-1$
		load.addActionListener(this);
		store = new JButton(Messages.getString("SoundFrame.STORE"),STORE_ICON); //$NON-NLS-1$
		store.addActionListener(this);

		play = new JButton(PLAY_ICON);
		play.addActionListener(this);
		stop = new JButton(STOP_ICON);
		stop.addActionListener(this);

		filename = new JLabel(Messages.format("SoundFrame.FILE",res.get(PSound.FILE_NAME))); //$NON-NLS-1$

		JPanel pKind = makeKindPane();

		JPanel pEffects = makeEffectsPane();

		JLabel lVolume = new JLabel(Messages.getString("SoundFrame.VOLUME")); //$NON-NLS-1$
		volume = new JSlider(0,100,100);
		volume.setMajorTickSpacing(10);
		volume.setPaintTicks(true);
		plf.make(volume.getModel(),PSound.VOLUME,100.0);

		JLabel lPan = new JLabel(Messages.getString("SoundFrame.PAN")); //$NON-NLS-1$
		pan = new JSlider(-100,100,0);
		pan.setMajorTickSpacing(20);
		pan.setPaintTicks(true);
		plf.make(pan.getModel(),PSound.PAN,100.0);

		preload = new JCheckBox(Messages.getString("SoundFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PSound.PRELOAD);

		edit = new JButton(Messages.getString("SoundFrame.EDIT"),EDIT_ICON); //$NON-NLS-1$
		edit.addActionListener(this);

		save.setText(Messages.getString("SoundFrame.SAVE")); //$NON-NLS-1$

		data = res.data;

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lName)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(load,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*				*/.addComponent(store,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(play)
		/*				*/.addComponent(stop)))
		/**/.addComponent(filename,120,120,MAX_VALUE)
		/**/.addComponent(pKind,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(pEffects,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(lVolume)
		/**/.addComponent(volume)
		/**/.addComponent(lPan)
		/**/.addComponent(pan)
		/**/.addComponent(preload)
		/**/.addComponent(edit,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(save,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lName)
		/*		*/.addComponent(name))
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(load)
		/*		*/.addComponent(play))
		/**/.addGap(2)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(store)
		/*		*/.addComponent(stop))
		/**/.addComponent(filename)
		/**/.addComponent(pKind)
		/**/.addComponent(pEffects)
		/**/.addComponent(lVolume).addGap(0)
		/**/.addComponent(volume)
		/**/.addComponent(lPan).addGap(0)
		/**/.addComponent(pan)
		/**/.addComponent(preload)
		/**/.addComponent(edit)
		/**/.addComponent(save));
		pack();
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

	public boolean resourceChanged()
		{
		commitChanges();
		if (modified) return true;
		ResourceComparator c = new ResourceComparator();
		c.addExclusions(Sound.class,"data"); //$NON-NLS-1$
		return !c.areEqual(res,resOriginal);
		}

	public void revertResource()
		{
		resOriginal.updateReference();
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		res.data = data;
		}

	public void updateResource()
		{
		super.updateResource();
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
				JOptionPane.showMessageDialog(null,f.getName()
						+ Messages.getString("SoundFrame.FILE_MISSING"), //$NON-NLS-1$
						Messages.getString("SoundFrame.FILE_OPEN"),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				}
			try
				{
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int val = in.read();
				while (val != -1)
					{
					out.write(val);
					val = in.read();
					}
				data = out.toByteArray();
				String fn = f.getName();
				res.put(PSound.FILE_NAME,fn);
				String ft = CustomFileFilter.getExtension(fn);
				if (ft == null) ft = "";
				res.put(PSound.FILE_TYPE,ft);
				filename.setText(Messages.format("SoundFrame.FILE",fn));
				out.close();
				in.close();
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
			return;
			}
		if (e.getSource() == stop)
			{
			return;
			}
		if (e.getSource() == store)
			{
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			try
				{
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
						fc.getSelectedFile()));
				int val = in.read();
				while (val != -1)
					{
					out.write(val);
					val = in.read();
					}
				out.close();
				in.close();
				}
			catch (Exception ex)
				{
				ex.printStackTrace();
				}
			return;
			}
		if (e.getSource() == edit)
			{
			return;
			}
		super.actionPerformed(e);
		}
	}
