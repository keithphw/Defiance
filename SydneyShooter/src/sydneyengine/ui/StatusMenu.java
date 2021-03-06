/*
 * StatusMenu.java
 *
 * Created on 25 October 2007, 11:28
 */

package sydneyengine.ui;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import sydneyengine.shooter.ChatTextEvent;
import sydneyengine.shooter.ViewPane;
/**
 *
 * @author  Leeder
 */
public class StatusMenu extends javax.swing.JPanel {
	ViewPane v;
	/** Creates new form StatusMenu */
	public StatusMenu(ViewPane v) {
		this.v = v;
		initComponents();
	}

	public JRadioButton getAllyChatSelector() {
		return allyChatSelector;
	}

	public JTextField getMessageField() {
		return messageField;
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageField = new javax.swing.JTextField();
        allyChatSelector = new javax.swing.JRadioButton();
        sendMessageButton = new javax.swing.JButton();
        menuButton = new javax.swing.JButton();

        messageField.setToolTipText("Type messages here to chat to allies or taunt enemies. Press Enter to send.");
        messageField.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageFieldActionPerformed(evt);
            }
        });
        messageField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
                messageFieldKeyPressed(evt);
            }
        });

        allyChatSelector.setText("Allies only");
        allyChatSelector.setToolTipText("If turned on, sends text to allies only. Otherwise, everyone hears.");
        allyChatSelector.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                allyChatSelectorActionPerformed(evt);
            }
        });

        sendMessageButton.setText("Send");
        sendMessageButton.setToolTipText("Press to send, or press enter in the message field.");
        sendMessageButton.setPreferredSize(new java.awt.Dimension(60, 23));
        sendMessageButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMessageButtonActionPerformed(evt);
            }
        });

        menuButton.setText("Menu");
        menuButton.setPreferredSize(new java.awt.Dimension(60, 23));
        menuButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(messageField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allyChatSelector)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sendMessageButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(menuButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(messageField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .add(allyChatSelector)
                    .add(sendMessageButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(menuButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void menu(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu
		v.getGameFrame().doGameMenu(v);
	}//GEN-LAST:event_menu

	private void messageFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageFieldActionPerformed
		v.sendEvent(new ChatTextEvent(v.getPlayer(), messageField.getText(), allyChatSelector.isSelected()));
		messageField.setText("");
		v.requestFocus();
}//GEN-LAST:event_messageFieldActionPerformed

	private void allyChatSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allyChatSelectorActionPerformed
		// TODO add your handling code here:
}//GEN-LAST:event_allyChatSelectorActionPerformed

	private void sendMessageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendMessageButtonActionPerformed
		v.sendEvent(new ChatTextEvent(v.getPlayer(), messageField.getText(), allyChatSelector.isSelected()));
		messageField.setText("");
		v.requestFocus();
}//GEN-LAST:event_sendMessageButtonActionPerformed

	private void messageFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageFieldKeyPressed
		if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE){
			v.requestFocus();
		}
	}//GEN-LAST:event_messageFieldKeyPressed
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allyChatSelector;
    private javax.swing.JButton menuButton;
    private javax.swing.JTextField messageField;
    private javax.swing.JButton sendMessageButton;
    // End of variables declaration//GEN-END:variables
	
}
