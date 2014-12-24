/*
 * ControlsPane.java
 *
 * Created on 13 October 2007, 18:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine.ui;

import sydneyengine.shooter.ViewPane;
import sydneyengine.shooter.GameFrame;
import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.lang.reflect.*;
import sydneyengine.*;
import sydneyengine.network.*;
import sydneyengine.superserializable.*;

/**
 *
 * @author Nastia
 */
public class ControlsPane extends JPanel {

	JToggleButton activateKeyPresserButton;
	JLabel keyPresserSleepIntervalLabel;
	JTextField keyPresserSleepIntervalField;
	JLabel eventTimingStrategyLabel;
	JComboBox eventTimingStrategyCombo;
	JLabel maxLatencyLabel;
	JTextField maxLatencyField;
	JLabel minLatencyLabel;
	JTextField minLatencyField;
	JLabel eventDelayLabel;
	JTextField eventDelayField;
	JLabel millisToSleepBetweenUpdatesLabel;
	JTextField millisToSleepBetweenUpdatesField;
	JLabel worldMaxUpdateElapsedSecondsLabel;
	JTextField worldMaxUpdateElapsedSecondsField;
	JLabel eventTimeStampMultipleSecondsLabel;
	JTextField eventTimeStampMultipleSecondsField;
	JLabel worldMinTimeGapSecondsLabel;
	JTextField worldMinTimeGapSecondsField;
	JLabel worldMaxTimeGapSecondsLabel;
	JTextField worldMaxTimeGapSecondsField;
	JLabel eventStoreMinSecondsToKeepUserEventsLabel;
	JTextField eventStoreMinSecondsToKeepUserEventsField;
	JLabel nanoTimeBetweenClientUpdatesLabel;
	JTextField minTimeBetweenClientUpdatesNanosField;
	JLabel sleepTimeAfterNoMoreRecievesLabel;
	JTextField sleepTimeAfterNoMoreRecievesField;
	JLabel numLatencyChecksToKeepLabel;
	JTextField numLatencyChecksToKeepField;
	JLabel sleepTimeLatencyCheckerMillisLabel;
	JTextField sleepTimeLatencyCheckerMillisField;
	JRadioButton showStatsButton;
	JTable table;
	JButton reComputeLatencyToPlayerButton;
	JButton exitButton;
	JSplitPane split;
	ViewPane v;
	JInternalFrame f;

	public GameFrame getGameFrame() {
		return v.getGameFrame();
	}

	/** Creates a new instance of ControlsPane */
	public ControlsPane(final ViewPane v, final JInternalFrame f) {
		this.v = v;
		this.f = f;
		this.setLayout(new BorderLayout());

		// Here we create all of the buttons and controls etc...
		int textFieldColumns = 12;
		Box motherPane = new Box(BoxLayout.Y_AXIS);
		JPanel daughterPane = new JPanel(new GridLayout(16, 2));
		daughterPane.setBorder(new javax.swing.border.EmptyBorder(2, 2, 2, 2));
				
		
		// toggle stats
		
		showStatsButton = new JRadioButton("Show Stats");
		showStatsButton.setSelected(ViewPane.isSHOW_STATS());		
		JPanel panel8 = new JPanel(new BorderLayout());
		panel8.add(showStatsButton, BorderLayout.WEST);
		daughterPane.add(panel8);

		
		

		if (getGameFrame().getController() instanceof Controller) {//DedicatedServerController == false){
			eventDelayLabel = new JLabel("WrapperEvent.setTimeDelayBeforeEventApplied()() ");
			eventDelayLabel.setToolTipText("<html>The delay that's added to the time that the event happened.<br>" +
					"This reduces responsiveness for this player but it reduces the jumpyness that other players see<br>" +
					"since the event is sent to the server (and then clients) straight away and the delay time can act as a buffer.</html>");
			eventDelayField = new JTextField("" + EventWrapper.getStaticTimeDelayBeforeEventApplied(), textFieldColumns);
			eventDelayField.setHorizontalAlignment(JTextField.RIGHT);
			JPanel panel3 = new JPanel(new BorderLayout());
			panel3.add(eventDelayLabel, BorderLayout.WEST);
			panel3.add(eventDelayField, BorderLayout.EAST);
			daughterPane.add(panel3);
		}
		millisToSleepBetweenUpdatesLabel = new JLabel("Controller.setSleepBetweenUpdatesMillis() ");
		millisToSleepBetweenUpdatesLabel.setToolTipText("<html>This is the sleep time between frames.  The higher it is, the lower the frames per second (FPS).<br>" +
				"In general it should be at least one so that the other threads are given a turn to run and do their job.</html>");
		millisToSleepBetweenUpdatesField = new JTextField("" + getGameFrame().getController().getSleepBetweenUpdatesMillis(), textFieldColumns);
		millisToSleepBetweenUpdatesField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel panel4 = new JPanel(new BorderLayout());
		panel4.add(millisToSleepBetweenUpdatesLabel, BorderLayout.WEST);
		panel4.add(millisToSleepBetweenUpdatesField, BorderLayout.EAST);
		daughterPane.add(panel4);
		if (getGameFrame().getController() instanceof ServerController) {//ServingController){
			nanoTimeBetweenClientUpdatesLabel = new JLabel("ServingController.setTimeBetweenClientUpdatesNanos() ");
			nanoTimeBetweenClientUpdatesLabel.setToolTipText("<html>The minimum time to wait before the servingController should send the clients a GameWorld update.<br>" +
					"Note that a value of -1 means that the clients should never be sent the GameWorld as an update.<br>" +
					"If the value is very small like 1, an update will be sent after each frame (this can be inefficent!).</html>");
			minTimeBetweenClientUpdatesNanosField = new JTextField("" + ((ServerController) getGameFrame().getController()).getMinTimeBetweenClientUpdatesNanos(), textFieldColumns);
			minTimeBetweenClientUpdatesNanosField.setHorizontalAlignment(JTextField.RIGHT);
			JPanel panel6 = new JPanel(new BorderLayout());
			panel6.add(nanoTimeBetweenClientUpdatesLabel, BorderLayout.WEST);
			panel6.add(minTimeBetweenClientUpdatesNanosField, BorderLayout.EAST);
			daughterPane.add(panel6);
		}
		if (getGameFrame().getController().getSender() != null) {
			maxLatencyLabel = new JLabel("SenderLagSimulator.setMaxLagNanos() ");
			maxLatencyLabel.setToolTipText("<html>The max latency (the time it takes to send bytes to the server).  The latency of each message<br>" +
					"will be between min and maxLatency and the order of the messages is still guaranteed.</html>");
			maxLatencyField = new JTextField("" + ((SenderLagSimulator) getGameFrame().getController().getSender()).getMaxLagNanos(), textFieldColumns);
			maxLatencyField.setHorizontalAlignment(JTextField.RIGHT);
			minLatencyLabel = new JLabel("SenderLagSimulator.setMinLagNanos() ");
			minLatencyLabel.setToolTipText("<html>The min latency (the time it takes to send bytes to the server).  The latency of each message<br>" +
					"will be between min and maxLatency and the order of the messages is still guaranteed.</html>");
			minLatencyField = new JTextField("" + ((SenderLagSimulator) getGameFrame().getController().getSender()).getMinLagNanos(), textFieldColumns);
			minLatencyField.setHorizontalAlignment(JTextField.RIGHT);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(maxLatencyLabel, BorderLayout.WEST);
			panel.add(maxLatencyField, BorderLayout.EAST);
			daughterPane.add(panel);
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.add(minLatencyLabel, BorderLayout.WEST);
			panel2.add(minLatencyField, BorderLayout.EAST);
			daughterPane.add(panel2);
		}
	
		eventTimeStampMultipleSecondsLabel = new JLabel("world.setEventTimeStampMultipleSeconds()");
		eventTimeStampMultipleSecondsLabel.setToolTipText("<html>For performance reasons, events are grouped together in multiples of this number.</html>");
		eventTimeStampMultipleSecondsField = new JTextField("" + getGameFrame().getController().getWorld().getEventTimeStampMultipleSeconds(), textFieldColumns);
		eventTimeStampMultipleSecondsField.setHorizontalAlignment(JTextField.RIGHT);
		worldMaxUpdateElapsedSecondsLabel = new JLabel("world.setMaxUpdateElapsedSeconds()");
		worldMaxUpdateElapsedSecondsLabel.setToolTipText("<html>The maximum time that the world can be updated by. If the time exceeds this, it is broken into smaller time-chunks and each time chunk is processed.<br>This should be slightly bigger than world.eventTimeStampMultipleSeconds</html>");
		worldMaxUpdateElapsedSecondsField = new JTextField("" + getGameFrame().getController().getWorld().getMaxUpdateElapsedSeconds(), textFieldColumns);
		worldMaxUpdateElapsedSecondsField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel panela = new JPanel(new BorderLayout());
		panela.add(eventTimeStampMultipleSecondsLabel, BorderLayout.WEST);
		panela.add(eventTimeStampMultipleSecondsField, BorderLayout.EAST);
		daughterPane.add(panela);
		JPanel panel2a = new JPanel(new BorderLayout());
		panel2a.add(worldMaxUpdateElapsedSecondsLabel, BorderLayout.WEST);
		panel2a.add(worldMaxUpdateElapsedSecondsField, BorderLayout.EAST);
		daughterPane.add(panel2a);
		if (getGameFrame().getController() instanceof ClientController) {
			eventTimeStampMultipleSecondsField.setEditable(false);
			worldMaxUpdateElapsedSecondsField.setEditable(false);
		}
		
		worldMaxTimeGapSecondsLabel = new JLabel("world.setMaxTimeGapSeconds()");
		worldMaxTimeGapSecondsLabel.setToolTipText("<html>The max latency (the time it takes to send bytes to the server).  The latency of each message<br>" +
				"will be between min and maxLatency and the order of the messages is still guaranteed.</html>");
		worldMaxTimeGapSecondsField = new JTextField("" + getGameFrame().getController().getWorld().getMaxTimeGapSeconds(), textFieldColumns);
		worldMaxTimeGapSecondsField.setHorizontalAlignment(JTextField.RIGHT);
		worldMinTimeGapSecondsLabel = new JLabel("world.setMinTimeGapSeconds()");
		worldMinTimeGapSecondsLabel.setToolTipText("<html>The min latency (the time it takes to send bytes to the server).  The latency of each message<br>" +
				"will be between min and maxLatency and the order of the messages is still guaranteed.</html>");
		worldMinTimeGapSecondsField = new JTextField("" + getGameFrame().getController().getWorld().getMinTimeGapSeconds(), textFieldColumns);
		worldMinTimeGapSecondsField.setHorizontalAlignment(JTextField.RIGHT);
		if (getGameFrame().getController() instanceof ClientController) {
			worldMinTimeGapSecondsField.setEditable(false);
			worldMaxTimeGapSecondsField.setEditable(false);
		}
		eventStoreMinSecondsToKeepUserEventsLabel = new JLabel("EventStore.setMinSecondsToKeepUserEvents()");
		eventStoreMinSecondsToKeepUserEventsLabel.setToolTipText("<html>The The minimum amount of seconds that events are kept in the events list.</html>");
		eventStoreMinSecondsToKeepUserEventsField = new JTextField("" + getGameFrame().getController().getEventStore().getMinSecondsToKeepUserEvents(), textFieldColumns);
		eventStoreMinSecondsToKeepUserEventsField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(worldMinTimeGapSecondsLabel, BorderLayout.WEST);
		panel.add(worldMinTimeGapSecondsField, BorderLayout.EAST);
		daughterPane.add(panel);
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(worldMaxTimeGapSecondsLabel, BorderLayout.WEST);
		panel2.add(worldMaxTimeGapSecondsField, BorderLayout.EAST);
		daughterPane.add(panel2);
		JPanel panel3 = new JPanel(new BorderLayout());
		panel3.add(eventStoreMinSecondsToKeepUserEventsLabel, BorderLayout.WEST);
		panel3.add(eventStoreMinSecondsToKeepUserEventsField, BorderLayout.EAST);
		daughterPane.add(panel3);

		if (getGameFrame().getController() instanceof ServerController) {
			sleepTimeAfterNoMoreRecievesLabel = new JLabel("RelayerPolling.setSleepTimeAfterNoMoreRecievesMillis() ");
			sleepTimeAfterNoMoreRecievesLabel.setToolTipText("<html>The sleep time after all players' nexus's have had recieve() called on them until they returned null.<br>" +
					"This should be at least 1 or else the RelayerPolling thread will hog the processor and not let the other threads do their job.</html>");
			sleepTimeAfterNoMoreRecievesField = new JTextField("" + ((ReceiverPolling) ((ServerController) getGameFrame().getController()).getReceiver()).getSleepTimeAfterNoMoreRecievesMillis(), textFieldColumns);
			sleepTimeAfterNoMoreRecievesField.setHorizontalAlignment(JTextField.RIGHT);
			JPanel panel6 = new JPanel(new BorderLayout());
			panel6.add(sleepTimeAfterNoMoreRecievesLabel, BorderLayout.WEST);
			panel6.add(sleepTimeAfterNoMoreRecievesField, BorderLayout.EAST);
			daughterPane.add(panel6);
		}
		
		
		
		daughterPane.add(new JLabel(System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version")));

		exitButton = new JButton("Update and Resume Game");
		exitButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				UPDATE();
				
				v.getGameFrame().getDesktopPane().remove(f);
				f.dispose();
				v.requestFocus();
				
			}
		});
		daughterPane.add(exitButton);
		motherPane.add(daughterPane);
		
		this.add(motherPane);
	}
	public void UPDATE()
	{
		try {
			// requests garbage collection by JVM, may or may not be satisfied
			System.gc();
			System.out.println(this.getClass().getSimpleName() + ": attempted System.gc();");
			
			
			if (((SenderLagSimulator) getGameFrame().getController().getSender()) != null) {
				((SenderLagSimulator) getGameFrame().getController().getSender()).setMaxLagNanos(Long.parseLong(maxLatencyField.getText()));
				((SenderLagSimulator) getGameFrame().getController().getSender()).setMinLagNanos(Long.parseLong(minLatencyField.getText()));
			}
			EventWrapper.setStaticTimeDelayBeforeEventApplied(Float.parseFloat(eventDelayField.getText()));
			getGameFrame().getController().setSleepBetweenUpdatesMillis(Integer.parseInt(millisToSleepBetweenUpdatesField.getText()));
			getGameFrame().getController().getEventStore().setMinSecondsToKeepUserEvents(Double.parseDouble(eventStoreMinSecondsToKeepUserEventsField.getText()));
			if (getGameFrame().getController() instanceof ClientController == false) {
				// ClientControllers are not allowed to change these variables, they are controlled by the server.
				getGameFrame().getController().getWorld().getHead().setMaxUpdateElapsedSeconds(Double.parseDouble(worldMaxUpdateElapsedSecondsField.getText()));
				getGameFrame().getController().getWorld().getHead().setEventTimeStampMultipleSeconds(Double.parseDouble(eventTimeStampMultipleSecondsField.getText()));
				getGameFrame().getController().getWorld().getTail().setMaxUpdateElapsedSeconds(Double.parseDouble(worldMaxUpdateElapsedSecondsField.getText()));
				getGameFrame().getController().getWorld().getTail().setEventTimeStampMultipleSeconds(Double.parseDouble(eventTimeStampMultipleSecondsField.getText()));
				getGameFrame().getController().getWorld().getHead().setMaxTimeGapSeconds(Double.parseDouble(worldMaxTimeGapSecondsField.getText()));
				getGameFrame().getController().getWorld().getTail().setMaxTimeGapSeconds(Double.parseDouble(worldMaxTimeGapSecondsField.getText()));
				getGameFrame().getController().getWorld().getHead().setMinTimeGapSeconds(Double.parseDouble(worldMinTimeGapSecondsField.getText()));
				getGameFrame().getController().getWorld().getTail().setMinTimeGapSeconds(Double.parseDouble(worldMinTimeGapSecondsField.getText()));
				((ServingController)getGameFrame().getController()).setMinTimeBetweenClientUpdatesNanos(Long.parseLong(minTimeBetweenClientUpdatesNanosField.getText()));
			}
			ViewPane.setSHOW_STATS(showStatsButton.isSelected());
		} catch (Exception ex) {
			ex.printStackTrace();
			Toolkit.getDefaultToolkit().beep();
		}
	}

}
