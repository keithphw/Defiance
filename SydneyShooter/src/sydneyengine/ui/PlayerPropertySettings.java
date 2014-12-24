/*
 * PlayerPropertySettings.java
 *
 * Created on 10 October 2007, 08:09
 */

package sydneyengine.ui;

import sydneyengine.shooter.ViewPane;
import javax.swing.*;
import sydneyengine.*;
/**
 *
 * @author  Leeder
 */
public class PlayerPropertySettings extends javax.swing.JPanel {
	ViewPane v;
	JInternalFrame f;
	/** Creates new form PlayerPropertySettings */
	public PlayerPropertySettings(ViewPane v, JInternalFrame f) {
		this.v = v;
		this.f = f;
		initComponents();
		/*PlayerStats s = v.getPlayer().getPlayerStats();
		this.accelerationSlider.setValue((int)s.getAcceleration());
		this.gunRotationSlider.setValue((int)s.getGunRotationSpeed());
		this.rateOfFireSlider.setValue((int)(s.getReloadSeconds()*1000f));
		this.bulletSpeedSlider.setValue((int)s.getOriginalBulletSpeed());*/
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jSlider5 = new javax.swing.JSlider();
        accelerationSlider = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        maxFundsTextField = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        bulletSpeedSlider = new javax.swing.JSlider();
        gunRotationSlider = new javax.swing.JSlider();
        rateOfFireSlider = new javax.swing.JSlider();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        jMenu1.setText("Menu");
        jMenuBar1.add(jMenu1);

        jSlider5.setMajorTickSpacing(10);
        jSlider5.setPaintLabels(true);
        jSlider5.setPaintTicks(true);

        accelerationSlider.setMajorTickSpacing(100);
        accelerationSlider.setMaximum(1000);
        accelerationSlider.setMinorTickSpacing(100);
        accelerationSlider.setPaintLabels(true);
        accelerationSlider.setPaintTicks(true);
        accelerationSlider.setValue(100);

        jLabel2.setText("Acceleration (m/s)");

        jLabel3.setText("Turret turn speed (radians/s)");

        jLabel4.setText("Rate of fire (bullets/s)");

        jLabel5.setText("Bullet speed (m/s)");

        jLabel7.setText("Total funds:");

        jLabel6.setText("Funds remaining:");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("$0");
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        maxFundsTextField.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        maxFundsTextField.setText("$400");
        maxFundsTextField.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jTextField1.setText("CommanderKeith");

        jLabel1.setText("Name:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
                        .add(67, 67, 67)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                            .add(maxFundsTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(7, 7, 7)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxFundsTextField)
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 14, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jLabel6)))
        );

        bulletSpeedSlider.setMajorTickSpacing(100);
        bulletSpeedSlider.setMaximum(1000);
        bulletSpeedSlider.setPaintLabels(true);
        bulletSpeedSlider.setPaintTicks(true);
        bulletSpeedSlider.setValue(100);

        gunRotationSlider.setMajorTickSpacing(1);
        gunRotationSlider.setMaximum(10);
        gunRotationSlider.setMinorTickSpacing(1);
        gunRotationSlider.setPaintLabels(true);
        gunRotationSlider.setPaintTicks(true);
        gunRotationSlider.setValue(1);

        rateOfFireSlider.setMajorTickSpacing(250);
        rateOfFireSlider.setMaximum(2000);
        rateOfFireSlider.setPaintLabels(true);
        rateOfFireSlider.setPaintTicks(true);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButton(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButton(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(rateOfFireSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 151, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 145, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, bulletSpeedSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, accelerationSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(gunRotationSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(31, 31, 31)
                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(accelerationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gunRotationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bulletSpeedSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rateOfFireSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void cancelButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButton
		f.dispose();
	}//GEN-LAST:event_cancelButton

	private void okButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButton
		/*Player p = v.getPlayer();
		float acceleration = this.accelerationSlider.getValue();
		float gunRotationSpeed = this.gunRotationSlider.getValue();
		float reloadSeconds = this.rateOfFireSlider.getValue()/1000f;
		float originalBulletSpeed = this.bulletSpeedSlider.getValue();
		PlayerStats oldPlayerStats = p.getPlayerStats();
		PlayerStats newPlayerStats = new PlayerStats(acceleration, gunRotationSpeed, reloadSeconds, originalBulletSpeed, oldPlayerStats.getMaxFunds(), oldPlayerStats.getHitPoints(), oldPlayerStats.getMaxHitPoints());
		
		UserPlayerOptionsEvent userPlayerOptionsEvent = new UserPlayerOptionsEvent(p, newPlayerStats, oldPlayerStats);
		v.sendCommand(userPlayerOptionsEvent);*/
		f.dispose();
	}//GEN-LAST:event_okButton
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider accelerationSlider;
    private javax.swing.JSlider bulletSpeedSlider;
    private javax.swing.JButton cancelButton;
    private javax.swing.JSlider gunRotationSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSlider jSlider5;
    private javax.swing.JTextField jTextField1;
    protected javax.swing.JLabel maxFundsTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JSlider rateOfFireSlider;
    // End of variables declaration//GEN-END:variables
	
}
