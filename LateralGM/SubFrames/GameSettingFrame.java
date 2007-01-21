package SubFrames;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class GameSettingFrame extends JInternalFrame implements ActionListener

	{
	
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent arg0)
		{
		// unused
		}

	
	public static JCheckBox startFullscreen;
	
	public GameSettingFrame()
	{
	super("Game Settings",true,true,true,true);
	setSize(540,400);
	
	
	JTabbedPane tabbedPane = new JTabbedPane();

	add(tabbedPane);
	
	// Graphics tab
		{
	JComponent panel1 =  new JPanel(false);
	tabbedPane.addTab("Graphics", null, panel1,
	                  "Configure Graphics settings");
	tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
	panel1.setLayout(new BoxLayout(panel1, BoxLayout.PAGE_AXIS));
	startFullscreen = new JCheckBox("Start in FullScreen Mode");

	JCheckBox Interpolatecolors = new JCheckBox("Interpolate colors between pixels");
	JCheckBox ResizeWindow = new JCheckBox("Allow the player to resize the game window");
	JCheckBox StayOnTop = new JCheckBox("Let the game window always stay on top");
	JCheckBox DrawBorderedWindow = new JCheckBox("Don't draw a border in windowed mode");
	JCheckBox DrawButtonsCaption = new JCheckBox("Don't show the buttons in the window caption");
	JCheckBox DisplayMouse = new JCheckBox("Display Mouse");
	JCheckBox FreezeGame = new JCheckBox("Freeze the game when the game looses focus");
	panel1.add(startFullscreen);
	panel1.add(Interpolatecolors);
	panel1.add(ResizeWindow);
	panel1.add(StayOnTop);
	panel1.add(DrawBorderedWindow);
	panel1.add(DrawButtonsCaption);
	panel1.add(DisplayMouse);
	panel1.add(FreezeGame);
	
		
		}
	

	JComponent panel2 = new JPanel(false);
	tabbedPane.addTab("Resolution", null, panel2,
	                  "Configure Resolution");
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	
	JComponent panel3 = new JPanel(false);
	tabbedPane.addTab("Other", null, panel3,
	                  "Configure Other Settings");
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	
	JComponent panel4 = new JPanel(false);
	tabbedPane.addTab("Loading", null, panel4,
	                  "Configure Loading Settings");
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	
	JComponent panel5 = new JPanel(false);
	tabbedPane.addTab("Constants", null, panel5,
	                  "Configure Constants");
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	
	JComponent panel6 = new JPanel(false);
	tabbedPane.addTab("Include", null, panel6,
	                  "Configure Includes");
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	
	JComponent panel7 = new JPanel(false);
	tabbedPane.addTab("Errors", null, panel7,
	                  "Configure Error handling");
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	
	JComponent panel8 = new JPanel(false);
	tabbedPane.addTab("Info", null, panel8,
	                  "Configure Information");
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	}


	
	}
