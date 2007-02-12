package SubFrames;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

import mainRes.LGM;

import fileRes.Gm6File;

public class GameSettingFrame extends JInternalFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public static JCheckBox startFullscreen;

	JTabbedPane tabbedPane = new JTabbedPane();

	public GameSettingFrame()
		{
		super("Game Settings",true,true,true,true);
		setSize(540,470);
		setLayout(new FlowLayout());
		tabbedPane.setPreferredSize(new Dimension(530,400));
		setResizable(false);
		getContentPane().add(tabbedPane);

			// Graphics tab
			{
			JComponent panel1 = new JPanel(false);
			tabbedPane.addTab("Graphics",null,panel1,"Configure Graphics settings");
			tabbedPane.setMnemonicAt(0,KeyEvent.VK_1);
			panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
			startFullscreen = new JCheckBox("Start in FullScreen Mode",LGM.currentFile.StartFullscreen);

			JPanel scaling = new JPanel();
			scaling.setBorder(BorderFactory.createTitledBorder("Scaling"));
			scaling.setLayout(new BoxLayout(scaling,BoxLayout.PAGE_AXIS));
			ButtonGroup group = new ButtonGroup();
			JRadioButton option;
			option = new JRadioButton("Fixed scale (in %)");
			group.add(option);
			scaling.add(option);
			option = new JRadioButton("Keep aspect ratio");
			group.add(option);
			scaling.add(option);
			option = new JRadioButton("Full scale");
			group.add(option);
			scaling.add(option);

			JCheckBox Interpolatecolors = new JCheckBox("Interpolate colors between pixels",
					LGM.currentFile.Interpolate);
			JLabel backcolor = new JLabel(" Color outside the room region:");
			JButton colorbutton = new JButton("Set color!");
			colorbutton.setBackground(new Color(LGM.currentFile.ColorOutsideRoom));
			colorbutton.setHideActionText(true);

			JCheckBox ResizeWindow = new JCheckBox("Allow the player to resize the game window",
					LGM.currentFile.AllowWindowResize);
			JCheckBox StayOnTop = new JCheckBox("Let the game window always stay on top",
					LGM.currentFile.AlwaysOnTop);
			JCheckBox DrawBorderedWindow = new JCheckBox("Don't draw a border in windowed mode",
					LGM.currentFile.DontDrawBorder);
			JCheckBox DrawButtonsCaption = new JCheckBox("Don't show the buttons in the window caption",
					LGM.currentFile.DontShowButtons);
			JCheckBox DisplayMouse = new JCheckBox("Display Mouse",LGM.currentFile.DisplayCursor);
			JCheckBox FreezeGame = new JCheckBox("Freeze the game when the game looses focus",
					LGM.currentFile.FreezeOnLoseFocus);
			panel1.add(startFullscreen);
			panel1.add(scaling);
			panel1.add(Interpolatecolors);
			panel1.add(backcolor);
			panel1.add(colorbutton);
			panel1.add(ResizeWindow);
			panel1.add(StayOnTop);
			panel1.add(DrawBorderedWindow);
			panel1.add(DrawButtonsCaption);
			panel1.add(DisplayMouse);
			panel1.add(FreezeGame);
			}

		JComponent panel2 = new JPanel(false);
		tabbedPane.addTab("Resolution",null,panel2,"Configure Resolution");
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

			// other tab
			{
			JComponent panel3 = new JPanel(false);
			tabbedPane.addTab("Other",null,panel3,"Configure Other Settings");
			tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);
			panel3.setLayout(new BoxLayout(panel3,BoxLayout.PAGE_AXIS));
			JPanel Dkeys = new JPanel();
			Dkeys.setBorder(BorderFactory.createTitledBorder("Default Keys"));
			Dkeys.setLayout(new BoxLayout(Dkeys,BoxLayout.PAGE_AXIS));
			panel3.add(Dkeys);
			JCheckBox ESC = new JCheckBox("Let <ESC> end the game",LGM.currentFile.LetEscEndGame);
			JCheckBox F1 = new JCheckBox("Let <F1> show the game information",LGM.currentFile.LetF1ShowGameInfo);
			JCheckBox F4 = new JCheckBox("Let <F4> switch between screen modes",
					LGM.currentFile.LetF4SwitchFullscreen);
			JCheckBox F5 = new JCheckBox("Let <F5> save the game and <F6> load the game",
					LGM.currentFile.LetF5SaveF6Load);
			Dkeys.add(ESC);
			Dkeys.add(F1);
			Dkeys.add(F4);
			Dkeys.add(F5);
			JPanel GPP = new JPanel();
			GPP.setBorder(BorderFactory.createTitledBorder("Game Process Priority"));
			GPP.setLayout(new BoxLayout(GPP,BoxLayout.PAGE_AXIS));
			panel3.add(GPP);

			ButtonGroup group = new ButtonGroup();
			JRadioButton option;
			option = new JRadioButton("Normal");
			group.add(option);
			GPP.add(option);
			option = new JRadioButton("High");
			group.add(option);
			GPP.add(option);
			option = new JRadioButton("Highest");
			group.add(option);
			GPP.add(option);
			}

		JComponent panel4 = new JPanel(false);
		tabbedPane.addTab("Loading",null,panel4,"Configure Loading Settings");
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		JComponent panel5 = new JPanel(false);
		tabbedPane.addTab("Constants",null,panel5,"Configure Constants");
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

		JComponent panel6 = new JPanel(false);
		tabbedPane.addTab("Include",null,panel6,"Configure Includes");
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);

			// error tab
			{
			JComponent panel7 = new JPanel(false);
			tabbedPane.addTab("Errors",null,panel7,"Configure Error handling");
			tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);
			panel7.setLayout(new BoxLayout(panel7,BoxLayout.PAGE_AXIS));
			JCheckBox DEM = new JCheckBox("Display error messages",LGM.currentFile.DisplayErrors);
			JCheckBox WGE = new JCheckBox("Write game error messages to file game_errors.log",LGM.currentFile.WriteToLog);
			JCheckBox Abort = new JCheckBox("Abort on all error messages",LGM.currentFile.AbortOnError);
			JCheckBox TUV0 = new JCheckBox("Treat uninitialized variables as 0",LGM.currentFile.TreatUninitializedAs0);
			panel7.add(DEM);
			panel7.add(WGE);
			panel7.add(Abort);
			panel7.add(TUV0);
			}

				{
		JComponent panel8 = new JPanel(false);
		tabbedPane.addTab("Info",null,panel8,"Configure Information");
		tabbedPane.setMnemonicAt(1,KeyEvent.VK_2);
		panel8.setLayout(new FlowLayout());
		JLabel label = new JLabel("Author");
		label.setPreferredSize(new Dimension(80,25));
		panel8.add(label);
		 JTextField box = new JTextField(LGM.currentFile.Author);
		 box.setPreferredSize(new Dimension(390,25));
		 panel8.add(box);
		 label = new JLabel("Version");
		 label.setPreferredSize(new Dimension(80,25));
		 panel8.add(label);
		 box = new JTextField(""+LGM.currentFile.Version);
		 box.setPreferredSize(new Dimension(390,25));
		 panel8.add(box);
		 label = new JLabel("Last Changed");
		 label.setPreferredSize(new Dimension(80,25));
		 panel8.add(label);
		 box = new JTextField(""+LGM.currentFile.LastChanged);
		 box.setPreferredSize(new Dimension(390,25));
		 box.setEnabled(false);
		 panel8.add(box);
		 label = new JLabel("Information");
		 label.setPreferredSize(new Dimension(70,25));
		 panel8.add(label);
		 JTextArea boxa = new JTextArea(LGM.currentFile.Information);
		 boxa.setPreferredSize(new Dimension(500,200));
		 boxa.setLineWrap(true);
		 panel8.add(boxa);
				}

		JButton okButton = new JButton("Save");
		getContentPane().add(okButton);
		JButton cancelButton = new JButton("Don't save");
		add(cancelButton);

		}

	public void actionPerformed(ActionEvent arg0)
		{
		// unused
		}
	}