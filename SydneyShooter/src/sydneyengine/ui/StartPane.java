/*
 * StartPane.java
 *
 * Created on 14 October 2007, 01:36
 */
package sydneyengine.ui;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import sydneyengine.lobby.LobbyInfo;
import sydneyengine.shooter.ViewPane;

/**
 *
 * @author  Nastia
 */
public class StartPane extends javax.swing.JPanel implements Updatable {

	protected ViewPane v;
	JInternalFrame f;
	HostedGamesTableModel model;
	String connectedLabelString = "Connected to central server!";
	String unConnectedLabelString = "Not yet connected to central server...";
	Object mutex = new Object();

	/** Creates new form StartPane */
	public StartPane(ViewPane v, JInternalFrame f) {
		this.v = v;
		this.f = f;
		initComponents();
		nameTextField.setText(v.getGameFrame().getPlayerName());
		model = new HostedGamesTableModel(v.getGameFrame().getLobbyClient().getLobbyInfo());
		System.out.println(this.getClass().getSimpleName() + ": v.getGameFrame().getLobbyClient().getLobbyInfo().getHostedGames().size() == "+v.getGameFrame().getLobbyClient().getLobbyInfo().getHostedGames().size());
		hostedGamesTable.setModel(model);
		hostedGamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// only works in java 6: hostedGamesTable.setAutoCreateRowSorter(true);
		// get the new lobbyInfo from the central server:
		try{
			v.getGameFrame().getLobbyClient().sendRequestForLobbyInfo();
		}catch(IOException e){}
	}

	@Override
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		//if (model.getLastReturnedValueOfGetRowCount() != v.getWorld().getPlayers().size()) {
		//System.out.println(this.getClass().getSimpleName() + ": hostedGamesTable.getSelectedRow() == "+hostedGamesTable.getSelectedRow());
		int oldSelectedRow = hostedGamesTable.getSelectedRow();
		model.setLobbyInfo(v.getGameFrame().getLobbyClient().getLobbyInfo());
		TableModelEvent tableModelEvent = new TableModelEvent(hostedGamesTable.getModel());
		hostedGamesTable.tableChanged(tableModelEvent);
		hostedGamesTable.revalidate();
		if (oldSelectedRow < model.getRowCount() && oldSelectedRow >= 0) {
			hostedGamesTable.setRowSelectionInterval(oldSelectedRow, oldSelectedRow);
		}
		if (hostedGamesTable.getSelectedRow() == -1 && model.getLobbyInfo().getHostedGames().size() > 0){
			hostedGamesTable.setRowSelectionInterval(0, 0);
		}
		//System.out.println(this.getClass().getSimpleName() + ": hostedGamesTable.getSelectedRow() == "+hostedGamesTable.getSelectedRow());

		//}
		if (v.getGameFrame().getLobbyClient().isConnected() == true) {
			if (connectedLabel.getText().startsWith(connectedLabelString) == false) {
				connectedLabel.setText(connectedLabelString);
			}
			int numOtherGamersConnected = v.getGameFrame().getLobbyClient().getLobbyInfo().getNumPlayersConnected();
			//System.out.println(this.getClass().getSimpleName()+": numOtherGamersConnected == "+numOtherGamersConnected+", "+connectedLabel.getText());	
			if (numOtherGamersConnected > 0){
				numOtherGamersConnected -= 1;
			}
			if (connectedLabel.getText().endsWith("" + numOtherGamersConnected) == false) {
				connectedLabel.setText(connectedLabelString + " Other players online: " + numOtherGamersConnected);
			}
		} else {
			if (connectedLabel.getText().equals(unConnectedLabelString) == false) {
				connectedLabel.setText(unConnectedLabelString);
			}
		}
		
		if (model.getLobbyInfo().getHostedGames().size() == 0 && joinSelectedInternetGame.isEnabled() == true){
			joinSelectedInternetGame.setEnabled(false);
		}else if (model.getLobbyInfo().getHostedGames().size() != 0 && joinSelectedInternetGame.isEnabled() == false){
			joinSelectedInternetGame.setEnabled(true);
		}
	}

	class HostedGamesTableModel extends AbstractTableModel {

		LobbyInfo lobbyInfo;
		protected int lastReturnedValueOfGetRowCount;

		public HostedGamesTableModel(LobbyInfo lobbyInfo) {
			this.lobbyInfo = lobbyInfo;
		}

		@Override
		public String getColumnName(int col) {
			return "IP";
		/*if (col == 0) {
		return "IP";
		} else if (col == 1) {
		return "Team";
		} else if (col == 2) {
		return "Kills";
		} else if (col == 3) {
		return "Flags";
		} else {
		return "Deaths";
		}*/
		}

		@Override
		public int getRowCount() {
			lastReturnedValueOfGetRowCount = lobbyInfo.getHostedGames().size();
			return lobbyInfo.getHostedGames().size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			//if (col == 0) {
			return lobbyInfo.getHostedGames().get(row).getInetSocketAddress().getAddress().getHostAddress();
		//}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		@Override
		public Class getColumnClass(int c) {
			Object theObject = getValueAt(0, c);
			return theObject.getClass();
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
		//rowData[row][col] = value;
		//fireTableCellUpdated(row, col);
		}

		public LobbyInfo getLobbyInfo() {
			return lobbyInfo;
		}

		public void setLobbyInfo(LobbyInfo lobbyInfo) {
			this.lobbyInfo = lobbyInfo;
		}

		public int getLastReturnedValueOfGetRowCount() {
			return lastReturnedValueOfGetRowCount;
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        lanGameButton = new javax.swing.JButton();
        internetGameButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        joinSelectedInternetGame = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        hostedGamesTable = new javax.swing.JTable();
        connectedLabel = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        createNewInternetGameButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        cancelInternetGameButton = new javax.swing.JButton();
        statsButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        createNewLanGameButton = new javax.swing.JButton();
        joinLANGameButton = new javax.swing.JButton();
        cancelLanGameButton = new javax.swing.JButton();
        createNewLanGameButton1 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 36));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Defiance");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Your name:");

        nameTextField.setHorizontalAlignment(SwingConstants.CENTER);
        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });
        nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
			public void focusLost(java.awt.event.FocusEvent evt) {
                nameTextFieldFocusLost(evt);
            }
        });

        lanGameButton.setText("LAN or single player game");
        lanGameButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                lanGame(evt);
            }
        });

        internetGameButton.setText("Internet games");
        internetGameButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                internetGame(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButton(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, internetGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, lanGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, exitButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(78, 78, 78)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(internetGameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lanGameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 77, Short.MAX_VALUE)
                .add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("Name", jPanel3);

        joinSelectedInternetGame.setText("Join selected game!");
        joinSelectedInternetGame.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinSelectedInternetGame(evt);
            }
        });

        hostedGamesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(hostedGamesTable);

        connectedLabel.setText("Not yet connected to central server...");

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        createNewInternetGameButton.setText("Create new game!");
        createNewInternetGameButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewInternetGame(evt);
            }
        });

        jLabel3.setText("<html>Thanks to <b>Riven</b> for providing the server and support.</html>");

        cancelInternetGameButton.setText("Cancel");
        cancelInternetGameButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelInternetGame(evt);
            }
        });

        statsButton.setText("Stats");
        statsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                statsButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(joinSelectedInternetGame, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(createNewInternetGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(cancelInternetGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                            .add(connectedLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(statsButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(refreshButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(refreshButton)
                    .add(connectedLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(statsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(joinSelectedInternetGame, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createNewInternetGameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelInternetGameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("Internet Games", jPanel1);

        createNewLanGameButton.setText("Create new LAN game!");
        createNewLanGameButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLanGame(evt);
            }
        });

        joinLANGameButton.setText("Join game LAN game!");
        joinLANGameButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                showJoinGameMenu(evt);
            }
        });

        cancelLanGameButton.setText("Cancel");
        cancelLanGameButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelLanGame(evt);
            }
        });

        createNewLanGameButton1.setText("Single Player Game!");
        createNewLanGameButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                createSinglePlayerGame(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cancelLanGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(joinLANGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, createNewLanGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .add(createNewLanGameButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(createNewLanGameButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(joinLANGameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createNewLanGameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 118, Short.MAX_VALUE)
                .add(cancelLanGameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("LAN or Single Player Game", jPanel5);

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("Thanks to: \nRiven for helping me set up the networked game and generously supplying the central server,"
        		+ "\nMarkus Borbely for giving me access to the code used in his excellent game 'gunslingers',"
        		+ "\nKev Glass for helping me to use java WebStart and for making some great tutorials (cokeandcode.com),"
        		+ "\nAdam Martin (blah^3) for his advice on network game design,"
        		+ "\nBleb for helping out with some tricky maths and networking stuff,"
        		+ "\nThijs and Jeff K for pointing me to the Simple Network Timing Protocol, "
        		+ "\n\nAnd a big thanks to all of the programmers who have made their code available for me to use:"
        		+ "\nDmitri T and Chris C for the excellent Java2D API,"
        		+ "\nKirill G for the supurb look and feel of the menus (Substance API),"
        		+ "\nTrustin Lee from the Apache MINA project."
        		+ "\n\nCan I also say hi to Mum, Dad, Camille, Andrea, Dominic, Leon, Renee (brothers and sisters), "
        		+ "Anastasia (the best girlfriend in the world), McAuleys (great cousins) and Spot (the most sensational dog).");
        jScrollPane3.setViewportView(jTextArea1);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Credits", jPanel6);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .add(18, 18, 18)
                .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void cancelLanGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelLanGame
		tabbedPane.setSelectedIndex(0);
	}//GEN-LAST:event_cancelLanGame

	private void showJoinGameMenu(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJoinGameMenu
		f.dispose();
		v.getGameFrame().setPlayerName(nameTextField.getText());
		v.getGameFrame().doJoinMenu(v);
	}//GEN-LAST:event_showJoinGameMenu

	private void createLanGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLanGame
		//f.dispose();
		v.getGameFrame().setPlayerName(nameTextField.getText());
		v.getGameFrame().doCreate(false);
	}//GEN-LAST:event_createLanGame

	private void statsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statsButtonActionPerformed
		final JDialog dialog = new JDialog(v.getGameFrame());
		dialog.setTitle("Riven's Amsterdam Game Lobby Server Statistics");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		LobbyInfo lobbyInfo = v.getGameFrame().getLobbyClient().getLobbyInfo();
		ServerStatisticsPane p = new ServerStatisticsPane(dialog, lobbyInfo);
		dialog.add(p);
		dialog.pack();
		dialog.setResizable(true);
		dialog.setLocationRelativeTo(v);
		dialog.setVisible(true);
	}//GEN-LAST:event_statsButtonActionPerformed

	private void cancelInternetGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelInternetGame
		tabbedPane.setSelectedIndex(0);
	}//GEN-LAST:event_cancelInternetGame

	private void createNewInternetGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewInternetGame
		v.getGameFrame().setPlayerName(nameTextField.getText());
		v.getGameFrame().doCreate(true);
	}//GEN-LAST:event_createNewInternetGame

	private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
		try {
			v.getGameFrame().getLobbyClient().sendRequestForLobbyInfo();
		} catch (java.io.IOException e) {
			e.printStackTrace();
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
	}//GEN-LAST:event_refreshButtonActionPerformed

	private void joinSelectedInternetGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinSelectedInternetGame
		if (hostedGamesTable.getSelectedRow() < 0 && hostedGamesTable.getSelectedRow() >= model.getLobbyInfo().getHostedGames().size()){
			// this is not meant to happen, but just in case we stop the method.
			return;
		}
		
		final JDialog dialog = new JDialog();
		dialog.setTitle("Join Progress");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// Need to implement cancelling the join process mid-way thru.
		dialog.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				//close();
			}
		});
		JoinProgressPane joinProgressPane = new JoinProgressPane(v, dialog);
		dialog.add(joinProgressPane);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(v);
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					v.getGameFrame().doJoinGame(true, model.getLobbyInfo().getHostedGames().get(hostedGamesTable.getSelectedRow()).getInetSocketAddress());
				} catch (IOException ex) {
					ex.printStackTrace();
					Toolkit.getDefaultToolkit().beep();
				}
				dialog.dispose();
			}
		});
		synchronized (mutex) {
			// this si done in a synchronized code block so that the setVisible actually
			// happens before this button is pressed again and then two threads try to join.
			// This can occur since setVisible queues an event on Swing's EDT and it
			// may not happen before this button is pressed again.
			t.start();
			dialog.setVisible(true);
		}
	}//GEN-LAST:event_joinSelectedInternetGame

	private void exitButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButton
		v.getGameFrame().close();
	}//GEN-LAST:event_exitButton

	private void internetGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internetGame
		tabbedPane.setSelectedIndex(1);
	}//GEN-LAST:event_internetGame

	private void lanGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lanGame
		tabbedPane.setSelectedIndex(2);
	}//GEN-LAST:event_lanGame

	private void nameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFieldFocusLost
		v.getGameFrame().setPlayerName(nameTextField.getText());
	}//GEN-LAST:event_nameTextFieldFocusLost

	private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
		v.getGameFrame().setPlayerName(nameTextField.getText());
		internetGameButton.requestFocus();
	}//GEN-LAST:event_nameTextFieldActionPerformed

private void createSinglePlayerGame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createSinglePlayerGame
		v.getGameFrame().setPlayerName(nameTextField.getText());
		v.getGameFrame().doCreateSinglePlayer(true);
}//GEN-LAST:event_createSinglePlayerGame

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelInternetGameButton;
    private javax.swing.JButton cancelLanGameButton;
    private javax.swing.JLabel connectedLabel;
    private javax.swing.JButton createNewInternetGameButton;
    private javax.swing.JButton createNewLanGameButton;
    private javax.swing.JButton createNewLanGameButton1;
    private javax.swing.JButton exitButton;
    private javax.swing.JTable hostedGamesTable;
    private javax.swing.JButton internetGameButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton joinLANGameButton;
    private javax.swing.JButton joinSelectedInternetGame;
    private javax.swing.JButton lanGameButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton statsButton;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
