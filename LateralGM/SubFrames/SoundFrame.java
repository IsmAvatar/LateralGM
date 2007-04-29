package SubFrames;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import mainRes.LGM;
import resourcesRes.Sound;

import componentRes.CustomFileFilter;
import componentRes.IndexButtonGroup;
import componentRes.Messages;
import componentRes.ResNode;

public class SoundFrame extends ResourceFrame<Sound>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon frameIcon = LGM.findIcon("sound.png"); //$NON-NLS-1$
	private static final ImageIcon loadIcon = LGM.findIcon("open....png"); //$NON-NLS-1$
	private static final ImageIcon playIcon = LGM.findIcon("sound.png"); //$NON-NLS-1$
	private static final ImageIcon stopIcon = LGM.findIcon("room.png"); //$NON-NLS-1$
	private static final ImageIcon storeIcon = LGM.findIcon("save.png"); //$NON-NLS-1$
	private static final ImageIcon editIcon = LGM.findIcon("sound.png"); //$NON-NLS-1$
	private static final ImageIcon saveIcon = LGM.findIcon("save.png"); //$NON-NLS-1$

	public JButton load;
	public JButton play;
	public JButton stop;
	public JButton store;
	public JLabel filename;
	public IndexButtonGroup kind;
	public IndexButtonGroup effects;
	public JSlider volume;
	public JSlider pan;
	public JCheckBox preload;
	public JButton edit;
	public byte[] Data;
	public boolean modified = false;
	private JFileChooser fc = new JFileChooser();

	public SoundFrame(Sound res, ResNode node)
		{
		super(res,node);

		String s[] = { ".wav",".mid",".mp3" };
		fc.setFileFilter(new CustomFileFilter(s,Messages.getString("SoundFrame.FORMAT_SOUND"))); //$NON-NLS-1$//$NON-NLS-2$
		fc.addChoosableFileFilter(new CustomFileFilter(s[0],Messages.getString("SoundFrame.FORMAT_WAV"))); //$NON-NLS-1$//$NON-NLS-2$
		fc.addChoosableFileFilter(new CustomFileFilter(s[1],Messages.getString("SoundFrame.FORMAT_MID"))); //$NON-NLS-1$//$NON-NLS-2$
		fc.addChoosableFileFilter(new CustomFileFilter(s[2],Messages.getString("SoundFrame.FORMAT_MP3"))); //$NON-NLS-1$//$NON-NLS-2$

		setSize(250,390);
		setResizable(false);
		setMaximizable(false);
		setFrameIcon(frameIcon);

		setContentPane(new JPanel());
		setLayout(new FlowLayout());

		JLabel label = new JLabel(Messages.getString("SoundFrame.NAME")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(40,14));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		add(label);
		name.setPreferredSize(new Dimension(180,20));
		add(name);

		load = new JButton(Messages.getString("SoundFrame.LOAD"),loadIcon); //$NON-NLS-1$
		load.setPreferredSize(new Dimension(180,20));
		load.addActionListener(this);
		add(load);

		play = new JButton(playIcon);
		play.addActionListener(this);
		add(play);
		stop = new JButton(stopIcon);
		play.addActionListener(this);
		add(stop);

		store = new JButton(Messages.getString("SoundFrame.STORE"),storeIcon);
		store.addActionListener(this);
		add(store);

		label = new JLabel(Messages.getString("SoundFrame.FILE")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(40,14));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		add(label);
		filename = new JLabel(res.FileName);
		add(filename);

		kind = new IndexButtonGroup();
		AbstractButton b = new JRadioButton(Messages.getString("SoundFrame.NORMAL"));
		b.setPreferredSize(new Dimension(110,16));
		kind.add(b,Sound.NORMAL);
		b = new JRadioButton(Messages.getString("SoundFrame.BACKGROUND"));
		b.setPreferredSize(new Dimension(110,16));
		kind.add(b,Sound.BACKGROUND);
		b = new JRadioButton(Messages.getString("SoundFrame.THREE"));
		b.setPreferredSize(new Dimension(110,16));
		kind.add(b,Sound.THREE);
		b = new JRadioButton(Messages.getString("SoundFrame.MULT"));
		b.setPreferredSize(new Dimension(110,16));
		kind.add(b,Sound.MULTIMEDIA);
		kind.setValue(res.kind);
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.KIND"))); //$NON-NLS-1$
		p.setPreferredSize(new Dimension(220,110));
		kind.populate(p);
		add(p);

		// these are in bit order as appears in a GM6 file, not the same as GM shows them
		effects = new IndexButtonGroup();
		b = new JCheckBox(Messages.getString("SoundFrame.CHORUS"));
		b.setPreferredSize(new Dimension(110,16));
		effects.add(b,1);
		b = new JCheckBox(Messages.getString("SoundFrame.ECHO"));
		b.setPreferredSize(new Dimension(110,16));
		effects.add(b,2);
		b = new JCheckBox(Messages.getString("SoundFrame.FLANGER"));
		b.setPreferredSize(new Dimension(110,16));
		effects.add(b,4);
		b = new JCheckBox(Messages.getString("SoundFrame.GARGLE"));
		b.setPreferredSize(new Dimension(110,16));
		effects.add(b,8);
		b = new JCheckBox(Messages.getString("SoundFrame.REVERB"));
		b.setPreferredSize(new Dimension(110,16));
		effects.add(b,16);
		effects.setValue(res.getEffects());
		p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.EFFECTS"))); //$NON-NLS-1$
		p.setPreferredSize(new Dimension(220,110));
		effects.populate(p);
		add(p);

		label = new JLabel(Messages.getString("SoundFrame.VOLUME")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(40,14));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		add(label);
		volume = new JSlider(0,100,100);
		add(volume);

		label = new JLabel(Messages.getString("SoundFrame.PAN")); //$NON-NLS-1$
		label.setPreferredSize(new Dimension(40,14));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		add(label);
		pan = new JSlider(-100,100,0);
		add(volume);

		preload = new JCheckBox(Messages.getString("SoundFrame.PRELOAD"));
		preload.setPreferredSize(new Dimension(110,16));
		add(preload);

		edit = new JButton(Messages.getString("SoundFrame.EDIT"),editIcon);
		edit.addActionListener(this);
		add(edit);

		save.setPreferredSize(new Dimension(100,27));
		save.setIcon(saveIcon);
		save.setText(Messages.getString("SoundFrame.SAVE")); //$NON-NLS-1$
		save.setAlignmentX(0.5f);
		add(save);

		Data = res.Data;
		}

	public boolean resourceChanged()
		{
		if (res.name.equals(name.getText()) || modified || !res.FileName.equals(filename)
				|| res.kind != kind.getValue() || res.getEffects() != effects.getValue()
				|| res.volume != (double) volume.getValue() / 100.0 || res.pan != (double) pan.getValue() / 100.0
				|| res.preload != preload.isSelected()) return true;
		return false;
		}

	public void revertResource()
		{
		LGM.currentFile.Sounds.replace(res.Id,resOriginal);
		}

	public void updateResource()
		{
		res.name = name.getText();

		String n = filename.getText();
		res.FileName = n;
		res.FileType = n.substring(n.lastIndexOf("."));
		res.kind = kind.getValue();
		res.setEffects(effects.getValue());
		res.volume = (double) volume.getValue() / 100.0;
		res.pan = (double) pan.getValue() / 100.0;
		res.Data = Data;
		modified = false;
		resOriginal = (Sound) res.copy(false,null);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			boolean repeat = true;
			while (repeat)
				{
				if (fc.showOpenDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
				if (fc.getSelectedFile().exists())
					repeat = false;
				else
					JOptionPane.showMessageDialog(null,fc.getSelectedFile().getName()
							+ Messages.getString("SoundFrame.FILE_MISSING"),Messages.getString("SoundFrame.FILE_OPEN"),
							JOptionPane.OK_OPTION);
				}
			try
				{
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(fc.getSelectedFile()));
				ByteArrayOutputStream dat = new ByteArrayOutputStream();
				int val = in.read();
				while (val != -1)
					{
					dat.write(val);
					val = in.read();
					}
				Data = dat.toByteArray();
				}
			catch (Exception ex)
				{
				ex.printStackTrace();
				}
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
			return;
			}
		if (e.getSource() == edit)
			{
			return;
			}
		super.actionPerformed(e);
		}
	}