package com.savemywiki.tools.export.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.AppState;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.IModelListener;
import com.savemywiki.tools.export.model.TaskStatus;

public class AppView extends JFrame implements IModelListener {

	private static final long serialVersionUID = 2041262664326157307L;

	private AppModel model;
	private List<IActionListener> listeners;

	// inner UI components
	private UIHelper ui;

	private JTextArea logsUI;

	private JScrollPane progressScroll;
	private JTextPane progressLogs;
	private String progressStr = "";

	private JButton actionBtnGetPageNames;
	private JButton actionBtnExport;
	private JButton actionBtnSaveXML;

	private JLabel statusProcessNamesState;
	private JLabel statusProcessExportState;

	private JProgressBar progressBar;
	private JButton cancelButton;

	public AppView(UIHelper uiHelper, AppModel model) {
		super(model.getFrameTitle());
		this.ui = uiHelper;
		this.model = model;
		this.model.addActionListener(this);
		this.listeners = new ArrayList<IActionListener>();

		initUI();
	}

	private void initUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JFrame.setDefaultLookAndFeelDecorated(true);

		// icone
		URL resource = this.getClass().getResource(model.getAppIconURL());
		ImageIcon img = new ImageIcon(resource);
		setIconImage(img.getImage());

		// content pane
		JPanel contentPane = buildMainPane();

		// Set the window to be visible as the default to be false
		this.add(contentPane);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		this.actionBtnGetPageNames.requestFocusInWindow();
	}

	private JPanel buildMainPane() {
		// Define the panel to hold the buttons
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);

		addConfigPanelUI(panel);
		addLogUI(panel);
		addProgressionPane(panel);
		addActionBar(panel);
		addFooter(panel);

		return panel;
	}

	private void addConfigPanelUI(JComponent parent) {

		JPanel component = new JPanel();
		component.setBorder(ui.createTitleBorder("Configuration"));
		GridLayout layout = new GridLayout();
		component.setLayout(layout);

		// -- Web site URL --
		JPanel urlPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		component.add(urlPane);
		// label
		urlPane.add(new JLabel("URL du wiki : "));
		// input
		JTextField urlInput = new JTextField(model.getWebsiteURL());
		urlInput.addActionListener((ActionEvent e) -> {
			String url = urlInput.getText().trim();
			if (url.length() > 0 && url.charAt(url.length() - 1) == '/') {
				url = url.substring(0, url.length() - 1);
			}
			if (url.indexOf("/index.php") > 0) {
				url = url.substring(0, url.indexOf("/index.php"));
			}
			model.setWebsiteURL(url);
		});
		urlInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String url = urlInput.getText().trim();
				if (url.length() > 0 && url.charAt(url.length() - 1) == '/') {
					url = url.substring(0, url.length() - 1);
				}
				if (url.indexOf("/index.php") > 0) {
					url = url.substring(0, url.indexOf("/index.php"));
				}
				model.setWebsiteURL(url);
			}
		});
		urlInput.setPreferredSize(new Dimension(300, 30));
		urlPane.add(urlInput);

		// -- Pages limit by query --
		JPanel namesQueryLimitPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		component.add(namesQueryLimitPane);
		// label
		namesQueryLimitPane.add(
				new JLabel("<html>Limite de pages par requête <span style=\"color: fuchsia;\">*</span> : </html>"));
		// input
		SpinnerNumberModel namesLimitModel = new SpinnerNumberModel(model.getNamesQueryLimit(), 0.0, 500.0, 10.0);
		JSpinner namesLimitInput = new JSpinner(namesLimitModel);
		namesLimitModel.addChangeListener((ChangeEvent e) -> {
			model.setNamesQueryLimit(namesLimitModel.getNumber().intValue());
		});
		namesQueryLimitPane.add(namesLimitInput);

		// -- Waiting time between each query --
		JPanel pauseTimePane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		component.add(pauseTimePane);
		// label
		pauseTimePane.add(new JLabel("Pause entre chaque requête : "));
		// input
		SpinnerNumberModel pauseTimeModel = new SpinnerNumberModel(model.getQueryPauseTime(), 0.0, 6000.0, 100.0);
		JSpinner pauseTimeInput = new JSpinner(pauseTimeModel);
		pauseTimeModel.addChangeListener((ChangeEvent e) -> {
			model.setQueryPauseTime(pauseTimeModel.getNumber().intValue());
		});
		pauseTimePane.add(pauseTimeInput);
		pauseTimePane.add(new JLabel(" ms"));

		GridBagConstraints cst = new GridBagConstraints();
		cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 0;
		cst.gridwidth = 2;
		cst.gridheight = 1;
		cst.insets = new Insets(10, 10, 5, 10);
		cst.ipadx = 10;
		cst.ipady = 10;
		cst.weightx = 1;
		cst.weighty = 0;
		cst.fill = GridBagConstraints.BOTH;
		cst.anchor = GridBagConstraints.WEST;

		parent.add(component, cst);
	}

	private void addLogUI(JComponent parent) {
		logsUI = new JTextArea(15, 10);
		logsUI.setEditable(false);

		JScrollPane component = new JScrollPane(logsUI);

		GridBagConstraints cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 2;
		cst.gridwidth = 1;
		cst.gridheight = 1;
		cst.insets = new Insets(0, 10, 10, 10);
		cst.weightx = 1;
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		cst.anchor = GridBagConstraints.NORTHWEST;

		TitledBorder buildTitleBorder = ui.createTitleBorder("Logs");
		component.setBorder(buildTitleBorder);
		component.setPreferredSize(new Dimension(500, 500));

		parent.add(component, cst);
	}

	private void addProgressionPane(JPanel parent) {

		progressLogs = new JTextPane();
		progressLogs.setContentType("text/html"); // let the text pane know this is what you want
		progressLogs.setEditable(false); // as before
		progressLogs.setBackground(null); // this is the same as a JLabel
		progressLogs.setBorder(new EmptyBorder(10, 10, 10, 10)); // remove the border

		progressScroll = new JScrollPane(progressLogs);
		progressScroll.setBorder(ui.createTitleBorder("Progression"));
		progressScroll.setPreferredSize(new Dimension(600, 500));
		GridBagConstraints cst = new GridBagConstraints();
		cst.gridx = 1;
		cst.gridy = 2;
		cst.gridwidth = 1;
		cst.gridheight = 1;
		cst.insets = new Insets(0, 0, 10, 5);
		cst.weightx = 0;
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		cst.anchor = GridBagConstraints.NORTHWEST;
		parent.add(progressScroll, cst);
	}

	private void addActionBar(JPanel parent) {
		JPanel component = new JPanel(new GridLayout());
		TitledBorder titleBorder = ui.createTitleBorder("Actions");
		component.setBorder(titleBorder);
		component.setPreferredSize(new Dimension(500, 70));

		JPanel container = new JPanel();
		container.setLayout(new GridLayout(1, 3));
		container.setBorder(new EmptyBorder(2, 5, 5, 5));
		component.add(container);

		actionBtnGetPageNames = ui.createButton(
				"<html><div style=\"white-space: nowrap;\">&nbsp;Récupérer les noms de pages <span style=\"color: fuchsia;\">*</span></div></html>");
		actionBtnGetPageNames.addActionListener((ActionEvent e) -> {
			AppView.this.performAction(ActionKey.GET_PAGE_NAMES);
		});
		actionBtnGetPageNames.setIcon(ui.launchIcon);
		container.add(actionBtnGetPageNames);

		statusProcessNamesState = new JLabel(" ");
		JPanel namesStatusPane = new JPanel();
		namesStatusPane.setBorder(new EmptyBorder(0, 10, 0, 0));
		namesStatusPane.setLayout(new GridLayout(1, 2));
		namesStatusPane.add(statusProcessNamesState);
		namesStatusPane.add(new JLabel(ui.nextArrowIcon));
		container.add(namesStatusPane);

		actionBtnExport = ui.createButton(
				"<html><div style=\"white-space: nowrap;\">&nbsp;<b>2 -</b> Exporter les pages</div></html>");
		actionBtnExport.setEnabled(false);
		actionBtnExport.addActionListener((ActionEvent e) -> {
			AppView.this.performAction(ActionKey.EXPORT_PAGES);
		});
		container.add(actionBtnExport);

		statusProcessExportState = new JLabel(" ");
		JPanel exportStatus = new JPanel();
		exportStatus.setBorder(new EmptyBorder(0, 10, 0, 0));
		exportStatus.setLayout(new GridLayout(1, 2));
		exportStatus.add(statusProcessExportState);
		exportStatus.add(new JLabel(ui.nextArrowIcon));
		container.add(exportStatus);

		actionBtnSaveXML = ui.createButton(
				"<html><div style=\"white-space: nowrap;\">&nbsp;<b>3 -</b> Sauvegarder les fichiers...</div></html>");
		actionBtnSaveXML.setEnabled(false);
		actionBtnSaveXML.addActionListener((ActionEvent e) -> {
			AppView.this.performAction(ActionKey.SAVE_XML_FILES);
		});
		actionBtnSaveXML.setVerticalAlignment(SwingConstants.CENTER);
		container.add(actionBtnSaveXML);

		GridBagConstraints cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 1;
		cst.gridwidth = 2;
		cst.gridheight = 1;
		cst.insets = new Insets(5, 5, 5, 5);
		cst.weightx = 0;
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		cst.anchor = GridBagConstraints.NORTHWEST;
		parent.add(component, cst);
	}

	private void addFooter(JPanel parent) {
		GridBagConstraints cst;
		JPanel component = new JPanel();
		component.setLayout(new GridBagLayout());
		component.setBorder(ui.createTitleBorder(""));
		component.setPreferredSize(new Dimension(100, 55));
		cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 3;
		cst.gridwidth = 2;
		cst.gridheight = 1;
		cst.insets = new Insets(10, 10, 10, 10);
		cst.weightx = 1;
		cst.weighty = 0;
		cst.fill = GridBagConstraints.BOTH;
		cst.anchor = GridBagConstraints.CENTER;
		parent.add(component, cst);

		JPanel progressBarContainer = new JPanel(new GridLayout());
		cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 0;
		cst.insets = new Insets(15, 15, 15, 15);
		cst.weightx = 1;
		cst.weighty = 0;
		cst.fill = GridBagConstraints.BOTH;
		component.add(progressBarContainer, cst);

		progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		progressBar.setFont(progressBar.getFont().deriveFont(13.f));
		progressBarContainer.add(progressBar);

		JPanel cancelButtonContainer = new JPanel(new GridLayout());
		cst = new GridBagConstraints();
		cst.gridx = 1;
		cst.gridy = 0;
		cst.insets = new Insets(10, 8, 10, 8);
		cst.weightx = 0;
		cst.weighty = 0;
		cst.fill = GridBagConstraints.VERTICAL;
		component.add(cancelButtonContainer, cst);

		cancelButton = ui.createButton("Interrompre", ui.cancelIcon);
		cancelButton.setVisible(false);
		cancelButton.addActionListener((ActionEvent e) -> {
			cancelButton.setEnabled(false);
			AppView.this.performAction(ActionKey.STOP_CURRENT_PROCESS);
		});
		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				AppView.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				updateCursor();
			}
		});
		cancelButtonContainer.add(cancelButton);

	}

	public synchronized void addActionListener(IActionListener listener) {
		if (!listeners.contains(listener)) {
			new Thread(() -> {
				listeners.add(listener);
			}).start();
		}
	}

	public void cleanLog() {
		logsUI.setText("");
	}

	public void appendLog(String str) {
		SwingUtilities.invokeLater(() -> {
			logsUI.append(str + "\r\n");
		});
	}

	public void cleanProgress() {
		progressStr = "";
		progressLogs.setText("<html></html>");
	}

	public void appendProgress(String str) {
		SwingUtilities.invokeLater(() -> {
			progressStr += str + "<br>";
			progressLogs.setText("<html>" + progressStr + "</html>");
			SwingUtilities.invokeLater(() -> {
				JScrollBar vbar = progressScroll.getVerticalScrollBar();
				vbar.setValue(vbar.getMaximum());
			});
		});
	}

	private synchronized void performAction(ActionKey key) {
		System.out.println("Action performed : " + key.name());

		switch (key) {
		case OPEN_CONFIG_WINDOWS:

			break;
		case GET_PAGE_NAMES:
			cleanLog();
			cleanProgress();
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.performGetPageNames();
				}
			}).start();
			break;
		case EXPORT_PAGES:
			cleanLog();
			cleanProgress();
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.performExportPages();
				}
			}).start();
			break;
		case SAVE_XML_FILES:
			cleanLog();
			cleanProgress();
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.performSaveXMLFiles();
				}
			}).start();
			break;
		case STOP_CURRENT_PROCESS:
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.stopCurrentProcess();
				}
			}).start();
			break;
		default:
			break;
		}
	}

	@Override
	public void onApplicationStateChange(AppState state) {
		System.out.println("Application state : " + state.name());
		SwingUtilities.invokeLater(() -> {
			updateCursor(state);
			switch (state) {
			case GET_NAMES_PROCESSING:
				disable(this.actionBtnGetPageNames);
				disable(this.actionBtnExport);
				disable(this.actionBtnSaveXML);
				statusProcessNamesState.setText(" En cours");
				statusProcessNamesState.setIcon(ui.loadingIcon);
				statusProcessNamesState.setForeground(Color.ORANGE);
				cleanProgress();
				cleanLog();
				progressBar.setString("Récupération des noms de pages ...");
				progressBar.setIndeterminate(true);
				progressBar.setVisible(true);
				cancelButton.setVisible(true);
				cancelButton.setEnabled(true);
				cancelButton.requestFocusInWindow();
				break;
			case GET_NAMES_DONE:
				enable(this.actionBtnGetPageNames);
				enable(this.actionBtnExport);
				this.actionBtnExport.requestFocusInWindow();
				statusProcessNamesState.setText(model.countNames() + " noms");
				if (model.getCurrenProcessState() == TaskStatus.DONE_FAILED) {
					statusProcessNamesState.setIcon(ui.failIcon);
					statusProcessNamesState.setForeground(Color.RED);
					statusProcessNamesState.setText(model.countNames() + " noms");
				} else {
					statusProcessNamesState.setIcon(ui.successIcon);
					statusProcessNamesState.setForeground(Color.GREEN);
					statusProcessNamesState.setText(model.countNames() + " noms");
				}
				progressBar.setValue(0);
				progressBar.setIndeterminate(false);
				progressBar.setString("Noms de pages récupérés : " + model.countNames());
				progressBar.getModel().setMinimum(0);
				progressBar.getModel().setMaximum(model.countNames());
				cancelButton.setEnabled(false);
				break;
			case EXPORT_PAGES_PROCESSING:
				disable(this.actionBtnGetPageNames);
				disable(this.actionBtnExport);
				statusProcessExportState.setText(" En cours");
				statusProcessExportState.setIcon(ui.loadingIcon);
				statusProcessExportState.setForeground(Color.ORANGE);
				cleanProgress();
				cleanLog();
				progressBar.setValue(0);
				progressBar.setIndeterminate(false);
				progressBar.setVisible(true);
				progressBar.setString("Export des pages wiki ...");
				cancelButton.setVisible(true);
				cancelButton.setEnabled(true);
				cancelButton.requestFocusInWindow();
				break;
			case EXPORT_PAGES_DONE:
				enable(this.actionBtnGetPageNames);
				enable(this.actionBtnExport);
				this.actionBtnSaveXML.setEnabled(true);
				this.actionBtnSaveXML.setIcon(ui.downloadIcon);
				this.actionBtnSaveXML.requestFocusInWindow();
				if (model.getCurrenProcessState() == TaskStatus.DONE_FAILED) {
					statusProcessExportState.setIcon(ui.failIcon);
					statusProcessExportState.setText("Erreur(s)");
					statusProcessExportState.setForeground(Color.RED);
				} else {
					statusProcessExportState.setIcon(ui.successIcon);
					statusProcessExportState.setText("OK");
					statusProcessExportState.setForeground(Color.GREEN);
				}
				progressBar.setString("Page exportées : " + model.countExportSuccess());
				cancelButton.setEnabled(false);
				break;
			case SAVE_XML_FILES_PROCESSING:
				this.cleanProgress();
				disable(this.actionBtnGetPageNames);
				disable(this.actionBtnExport);
				disable(this.actionBtnSaveXML);
				progressBar.setString("Création de l'archive ZIP");
				progressBar.setIndeterminate(true);
				cancelButton.setVisible(false);
				cancelButton.setEnabled(false);
				break;
			case SAVE_XML_FILES_DONE:
				File exportsDataFile = model.getExportsDataFile();
				if (exportsDataFile != null) {
					this.appendProgress("<br>Données d'export enregistré dans l'archive :<br><br><b>"
							+ exportsDataFile.getAbsolutePath() + "</b>");
				}
				enable(this.actionBtnGetPageNames);
				enable(this.actionBtnExport);
				this.actionBtnSaveXML.setEnabled(true);
				this.actionBtnSaveXML.setIcon(ui.downloadIcon);
				this.actionBtnSaveXML.requestFocusInWindow();
				cancelButton.setEnabled(false);
				progressBar.setValue(0);
				progressBar.setIndeterminate(false);
				progressBar.setVisible(false);
				break;
			default:
				break;
			}
		});
	}

	@Override
	public void onExportDone(ExportData data) {
		if (data.getStatus() == TaskStatus.DONE_SUCCESS) {
			SwingUtilities.invokeLater(() -> {
				this.progressBar.setValue(this.progressBar.getValue() + data.getPageNames().size());
			});
		}
	}

	private void disable(JButton btn) {
		btn.setEnabled(false);
		btn.setIcon(null);
	}

	private void enable(JButton btn) {
		btn.setEnabled(true);
		btn.setIcon(ui.launchIcon);
	}

	private void updateCursor() {
		updateCursor(model.getApplicationState());
	}

	private void updateCursor(AppState appState) {
		switch (appState) {
		case GET_NAMES_DONE:
		case EXPORT_PAGES_DONE:
		case SAVE_XML_FILES_DONE:
			AppView.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case GET_NAMES_PROCESSING:
		case EXPORT_PAGES_PROCESSING:
		case SAVE_XML_FILES_PROCESSING:
			AppView.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			break;
		default:
			break;
		}
	}
}