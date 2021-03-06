/*
 * JoinPane.java
 *
 * Created on 14 October 2007, 11:01
 */

package sydneyengine.ui;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JDialog;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import sydneyengine.shooter.ViewPane;

/**
 *
 * @author  Nastia
 */
public class JoinPane extends javax.swing.JPanel {
	ViewPane v;
	Object mutex = new Object();
	/**
	 * Creates new form JoinPane
	 */
	public JoinPane(ViewPane f) {
		this.v = f;
		initComponents();
		String exampleLocalHostName = "";
		try{
			exampleLocalHostName = InetAddress.getLocalHost().getHostAddress();
		}catch(UnknownHostException e){
			e.printStackTrace();
			exampleLocalHostName = "error";
		}
		serverNameOrIPTextField.setText(exampleLocalHostName);
		guideTextArea.setText("The IP address of the server computer is needed to connect and join that game. Your IP, for example, is "+exampleLocalHostName+"\n\nThe server's IP will be written in the top left of their in-game help menu. Ask the player who is server to tell you their IP address then type it in above and click join.");
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        serverNameOrIPTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        guideTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel2.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 36));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Defiance");

        serverNameOrIPTextField.setHorizontalAlignment(SwingConstants.CENTER);

        jButton1.setText("Join!");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinGame(evt);
            }
        });

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJoinGame(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        guideTextArea.setColumns(20);
        guideTextArea.setEditable(false);
        guideTextArea.setFont(new java.awt.Font("Tahoma", 0, 13));
        guideTextArea.setLineWrap(true);
        guideTextArea.setRows(5);
        guideTextArea.setTabSize(4);
        jScrollPane2.setViewportView(guideTextArea);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Please type the Server's IP.");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 436, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 436, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 436, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(serverNameOrIPTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serverNameOrIPTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void cancelJoinGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJoinGame
		v.getGameFrame().doStart();
	}//GEN-LAST:event_cancelJoinGame

	private void joinGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinGame
		final JDialog dialog = new JDialog(v.getGameFrame());
		dialog.setTitle("Join Progress");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// Need to implement cancelling the join process mid-way thru.
		dialog.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				//close();
			}
		});
		JoinProgressPane joinProgressPane = new JoinProgressPane(v, dialog);
		dialog.add(joinProgressPane);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(v);
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				try {
					// bug here - exiting JDialog doesn't terminate the join method...
					v.getGameFrame().doJoinGame(false, serverNameOrIPTextField.getText());
				} catch (IOException ex) {
					ex.printStackTrace();
					Toolkit.getDefaultToolkit().beep();
				}
				dialog.dispose();
			}
		});
		synchronized(mutex){
			// this si done in a synchronized code block so that the setVisible actually 
			// happens before this button is pressed again and then two threads try to join. 
			// This can occur since setVisible queues an event on Swing's EDT and it 
			// may not happen before this button is pressed again.
			t.start();
			dialog.setVisible(true);
		}
	}//GEN-LAST:event_joinGame
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea guideTextArea;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField serverNameOrIPTextField;
    // End of variables declaration//GEN-END:variables
	
}
