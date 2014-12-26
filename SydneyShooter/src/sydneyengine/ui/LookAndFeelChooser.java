/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import sydneyengine.shooter.GameFrame;

import com.grexengine.jgf.ClassLocater;


public class LookAndFeelChooser extends JDialog{
	
	GameFrame frame;
	JPanel content;
	JScrollPane scrollPane;
	JTable table;
	JPanel buttonPane;
	JButton oKButton;
	
	final JDialog dialog;	//JInternalFrame dialog;
	
	final ArrayList<LookAndFeelInfo> list;
	
	public LookAndFeelChooser(final GameFrame frame){
		super(frame);
		setTitle("Choose a New Look & Feel!");
		this.frame = frame;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(false);
		
		
		dialog = new JDialog(this, "Transforming", false);	//new JInternalFrame("Transforming");
		dialog.add(new JLabel("Please Wait..."), BorderLayout.NORTH);
		JProgressBar pBar = new JProgressBar();
		pBar.setIndeterminate(true);
		dialog.add(pBar);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setSize(230, 85);
		dialog.setLocation(100,100);
		dialog.setLocationRelativeTo(null);
		
		//list = new ArrayList<UIManager.LookAndFeelInfo>();
		list = new ArrayList();
		
		/*ArrayList lookAndFeelClasses = getSortedClasses(LookAndFeel.class);
		for (int i = 0; i < lookAndFeelClasses.size(); i++){
			list.add(new UIManager.LookAndFeelInfo(((Class)lookAndFeelClasses.get(i)).getSimpleName(), ((Class)lookAndFeelClasses.get(i)).getName()));
		}*/
		
		
		UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < lookAndFeelInfos.length; i++){
			list.add(lookAndFeelInfos[i]);
		}
		
		if (GameFrame.isSubstanceLnFPresent()){
			list.add(new UIManager.LookAndFeelInfo("SubstanceAutumn", "org.jvnet.substance.skin.SubstanceAutumnLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceAqua", "org.jvnet.substance.SubstanceDefaultLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceBusinessBlackSteel", "org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceBusinessBlueSteel", "org.jvnet.substance.skin.SubstanceBusinessBlueSteelLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceBusiness", "org.jvnet.substance.skin.SubstanceBusinessLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceChallengerDeep", "org.jvnet.substance.skin.SubstanceChallengerDeepLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceCreme", "org.jvnet.substance.skin.SubstanceCremeLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceEmeraldDusk", "org.jvnet.substance.skin.SubstanceEmeraldDuskLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceFieldOfWheat", "org.jvnet.substance.skin.SubstanceFieldOfWheatLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceGreenMagic", "org.jvnet.substance.skin.SubstanceGreenMagicLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceMagma", "org.jvnet.substance.skin.SubstanceMagmaLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceMango", "org.jvnet.substance.skin.SubstanceMangoLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceMistAquaLookAndFeel", "org.jvnet.substance.skin.SubstanceMistAquaLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceMistSilverLookAndFeel", "org.jvnet.substance.skin.SubstanceMistSilverLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceModerate", "org.jvnet.substance.skin.SubstanceModerateLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceNebulaBrickWallLookAndFeel", "org.jvnet.substance.skin.SubstanceNebulaBrickWallLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceNebulaLookAndFeel", "org.jvnet.substance.skin.SubstanceNebulaLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceOfficeBlue2007", "org.jvnet.substance.skin.SubstanceOfficeBlue2007LookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceOfficeSilver2007", "org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceRavenGraphiteGlass", "org.jvnet.substance.skin.SubstanceRavenGraphiteGlassLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceRavenGraphite", "org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceRaven", "org.jvnet.substance.skin.SubstanceRavenLookAndFeel"));
			list.add(new UIManager.LookAndFeelInfo("SubstanceSahara", "org.jvnet.substance.skin.SubstanceSaharaLookAndFeel"));
		}
		//System.out.println("list.size(): "+list.size());
		
		
		content = new JPanel(new BorderLayout());
		setContentPane(content);
		
		// table code
		
		table = new JTable();
		table.setModel(new AbstractTableModel() {
			@Override
			public String getColumnName(int col) {
				return "Name";
			}
			@Override
			public int getRowCount() { return list.size(); }
			@Override
			public int getColumnCount() { return 1; }
			@Override
			public Object getValueAt(int row, int col) {
				return list.get(row).getName();//+" " +list.get(row).getClassName();
			}
			@Override
			public boolean isCellEditable(int row, int col){
				return false;
			}
			@Override
			public void setValueAt(Object value, int row, int col) {
			}
		});
		table.setDragEnabled(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//Ask to be notified of selection changes.
		ListSelectionModel rowSM = table.getSelectionModel();
		final LookAndFeelChooser thisLookAndFeelChooser = this;
		rowSM.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				//Ignore extra messages.
				if (e.getValueIsAdjusting()){
					return;
				}
				
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!(lsm.isSelectionEmpty())) {
					table.setEnabled(false);
					//progress dialog when changing LnF
					SwingUtilities.updateComponentTreeUI(dialog);
					dialog.setLocationRelativeTo(frame);
					dialog.setVisible(true);
					dialog.requestFocusInWindow();
					
					final int selectedRow = lsm.getMinSelectionIndex();
					Thread t = new Thread(){
						@Override
						public void run(){
							try{Thread.sleep(500);}catch(Exception e){}	// do this just to allow the progress bar to animate for a while
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									boolean was_wm_decorated = !frame.isUndecorated();
									
									try {
										UIManager.setLookAndFeel(list.get(selectedRow).getClassName());
									} catch (ClassNotFoundException exc) {
										exc.printStackTrace();//out("LAF main class '" + lafClassName + "' not found");
									} catch (Exception exc) {
										exc.printStackTrace();
									}finally{
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												/*Window[] allFrames = frame.getWindows();
												for (Window aWindow : allFrames){
													SwingUtilities.updateComponentTreeUI(aWindow);	
												}*/
												frame.updateLookAndFeel();
												SwingUtilities.updateComponentTreeUI(thisLookAndFeelChooser);
												dialog.dispose();
												table.setEnabled(true);
											}
										});
									}
									
									// if (System.getProperty("substancelaf.useDecorations") !=
									// null) {
									boolean is_wm_decorated = !UIManager.getLookAndFeel().getSupportsWindowDecorations();
									if (is_wm_decorated != was_wm_decorated) {
										//out("Changing decoration policy\n");
										Frame[] allFrames = Frame.getFrames();
										for (int i = 0; i < allFrames.length; i++){
											Frame aFrame = allFrames[i];
											if (aFrame instanceof JFrame){
												JFrame aJFrame = (JFrame)aFrame;
												//System.out.println("here's a frame!: "+aJFrame.getTitle());
												boolean wasVisible = aJFrame.isVisible();
												
												
												aJFrame.setVisible(false);
												aJFrame.dispose();
												if (is_wm_decorated == true){
													// see the java docs under the method JFrame.setDefaultLookAndFeelDecorated(boolean value) for description of these 2 lines:
													aJFrame.setUndecorated(false);
													aJFrame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
													
												}else{
													aJFrame.setUndecorated(true);
													aJFrame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
												}
												//aJFrame.pack();
												aJFrame.setVisible(wasVisible);
												was_wm_decorated = !aJFrame.isUndecorated();
											}
										}
										Dialog[] allDialogs = new Dialog[2];
										allDialogs[0] = LookAndFeelChooser.this;
										allDialogs[1] = dialog;
										for (int i = 0; i < allDialogs.length; i++){
											Dialog aFrame = allDialogs[i];
											if (aFrame instanceof JDialog){
												JDialog aJDialog = (JDialog)aFrame;
												//System.out.println("here's a dialog!: "+aJDialog.getTitle());
												boolean wasVisible = aJDialog.isVisible();
												
												//if (aFrame != dialog){
													aJDialog.setVisible(false);
													aJDialog.dispose();
												//}
												if (is_wm_decorated == true){
													// see the java docs under the method JFrame.setDefaultLookAndFeelDecorated(boolean value) for description of these 2 lines:
													aJDialog.setUndecorated(false);
													aJDialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
													
												}else{
													aJDialog.setUndecorated(true);
													aJDialog.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
												}
												//aJFrame.pack();
												aJDialog.setVisible(wasVisible);
												was_wm_decorated = !aJDialog.isUndecorated();
											}
										}
									}
								}
								// }
							});
						}
					};
					t.start();
					
					/*Thread t = new Thread(){
						public void run(){
					 
							try{Thread.sleep(1000);}catch(Exception e){}
							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									try{
										UIManager.setLookAndFeel(list.get(selectedRow).getClassName());//"net.sourceforge.napkinlaf.NapkinLookAndFeel");//"com.birosoft.liquid.LiquidLookAndFeel");//new org.jvnet.substance.SubstanceLookAndFeel());
										frame.updateLookAndFeel();
					 
									}catch(ClassNotFoundException ex){
										ex.printStackTrace();
									}catch(InstantiationException ex){
										ex.printStackTrace();
									}catch(IllegalAccessException ex){
										ex.printStackTrace();
									}catch(UnsupportedLookAndFeelException ex){
										ex.printStackTrace();
									}
					 
									dialog.dispose();
									table.setEnabled(true);
								}
							});
						}
					};
					t.start();*/
				}
			}
		});
		
		scrollPane = new JScrollPane(table);
		
		buttonPane = new JPanel();
		oKButton = new JButton("OK");
		//cancelButton = new JButton("Cancel");
		oKButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				setVisible(false);
			}
		});
		oKButton.setPreferredSize(new Dimension(130, 30));
		buttonPane.add(oKButton);
		content.add(scrollPane);
		content.add(buttonPane, BorderLayout.SOUTH);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//setSize((int)(screenSize.width/3), (int)(screenSize.height/2));
		setSize((300), (400));
		setLocationRelativeTo(frame);
		oKButton.requestFocus();
		setVisible(true);
	}
	
	
	//public static ArrayList<Class> getSortedClasses(Class clazz){
	public static ArrayList<Class<?>> getSortedClasses(Class clazz){
		return getSortedClasses(clazz, ".*");
	}
	
	//public static ArrayList<Class> getSortedClasses(Class clazz, String regex){
	public static ArrayList<Class<?>> getSortedClasses(Class clazz, String regex){
		ClassLocater classLocater = new ClassLocater();
		classLocater.addSkipPrefix("javax");
		classLocater.addSkipPrefix("bsh");
		Class[] classes = classLocater.getSubclassesOf(clazz);
		// order them alphabetically
		boolean noChange = false;
		Class holder = null;
		while (noChange){
			noChange = true;
			for (int i = 0; i < classes.length-1; i++){
				if (classes[i].getName().compareTo(classes[i+1].getName()) > 0){
					holder = classes[i];
					classes[i] = classes[i+1];
					classes[i+1] = holder;
					noChange = false;
				}
			}
		}
		// eliminate copies.
		//ArrayList<Class> sortedClasses = new ArrayList<Class>(classes.length);
		ArrayList sortedClasses = new ArrayList(classes.length);
		for (int i = 0; i < classes.length; i++){
			Class aClazz = classes[i];
			System.out.println(aClazz.getName());
			if (!(sortedClasses.contains(aClazz))){
				sortedClasses.add(aClazz);
			}
		}
		System.out.println("classes.length: "+classes.length+" sortedClasses.size(): "+sortedClasses.size());
		return sortedClasses;
		
		
	}
	
	
}