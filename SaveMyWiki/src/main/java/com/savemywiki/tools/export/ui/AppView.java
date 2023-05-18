package com.savemywiki.tools.export.ui;

import java.awt.BorderLayout;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import com.savemywiki.tools.export.ctrl.AppController;
import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.AppState;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.IModelListener;
import com.savemywiki.tools.export.model.TaskStatus;
import com.savemywiki.tools.export.model.WikiNamespaceData;
import com.savemywiki.tools.export.ui.table.ColumnHeaderToolTips;
import com.savemywiki.tools.export.ui.table.PaginatedTableDecorator;
import com.savemywiki.tools.export.ui.table.wikinamespace.WikiNamespaceTable;
import com.savemywiki.tools.export.ui.table.wikinamespace.WikiNamespaceTableCellRenderer;
import com.savemywiki.tools.export.ui.table.wikinamespace.WikiNamespaceTableHeaderRenderer;
import com.savemywiki.tools.export.ui.table.wikinamespace.WikiNamespaceTableModel;
import com.savemywiki.tools.export.ui.table.wikinamespace.WikiNamespaceTablePaginationProvider;

/**
 * Application GUI.
 * 
 * @author Marc Alemany
 */
public class AppView extends JFrame implements IModelListener {

	private static final long serialVersionUID = 2041262664326157307L;

	private AppModel model;
	private List<IActionListener> listeners;

	// inner UI components
	private UIHelper ui;
	
	private PlaceholderTextField urlInput;

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

	private JButton saveNamesBtn;

	private WikiNamespaceTable wikiTable;

	private JPanel actionBar;

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
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		this.urlInput.requestFocusInWindow();
	}

	private JPanel buildMainPane() {
		// Define the panel to hold the buttons
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);

		// top
		addConfigPanelUI(panel);
		addActionBar(panel);

		// middle
		JPanel middle = createMiddlePane(panel);
		JComponent progression = buildProgressionPane();
		JComponent logs = buildLogsPane();
		JComponent data = buildDataPane();

		JSplitPane splitterH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, data, progression);
		splitterH.setDividerLocation(600);

		JSplitPane splitterV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitterH, logs);
		splitterV.setPreferredSize(new Dimension(500, 500));
		splitterV.setDividerLocation(400);

		middle.add(splitterV);

		// bottom
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
		urlInput = new PlaceholderTextField(model.getWebsiteURL());
		urlInput.setPlaceholder("https://my-wiki.com");
		urlInput.addActionListener((ActionEvent e) -> {
			String url = urlInput.getText().trim();
			updateWikiURL(url);
		});
		urlInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String url = urlInput.getText().trim();
				updateWikiURL(url);
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
		SpinnerNumberModel namesLimitModel = new SpinnerNumberModel(model.getNamesQueryLimit(), 0.0, AppController.LIMIT_PAGES_BY_QUERY_LIST, 10.0);
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

	private void updateWikiURL(String url) {
		actionBar.setEnabled(url.length() > 0);
		if (url.length() > 0 && url.charAt(url.length() - 1) == '/') {
			url = url.substring(0, url.length() - 1);
		}
		if (url.indexOf("/index.php") > 0) {
			url = url.substring(0, url.indexOf("/index.php"));
		}
		actionBar.setVisible(url.length() > 0);
		model.setWebsiteURL(url);
	}

	private JPanel createMiddlePane(JComponent parent) {
		JPanel middle = new JPanel(new BorderLayout());
		middle.setPreferredSize(new Dimension(500, 500));
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
		parent.add(middle, cst);
		return middle;
	}

	private JComponent buildDataPane() {

		wikiTable = new WikiNamespaceTable(new WikiNamespaceTableModel(model));
		// Cells renderers
		wikiTable.getTableHeader().setDefaultRenderer(new WikiNamespaceTableHeaderRenderer());
		wikiTable.setDefaultRenderer(Object.class, new WikiNamespaceTableCellRenderer(model));

		// pagination
		WikiNamespaceTablePaginationProvider dataProvider = new WikiNamespaceTablePaginationProvider(model);
		PaginatedTableDecorator.decorate(wikiTable, dataProvider, null, 100);
		
		// column size
		ui.setColumnPreferredWidths(wikiTable, 40, 200, 90, 120);
		ui.setColumnMinWidths(wikiTable, 40, 200, 120, 120);
		ui.setColumnMaxWidths(wikiTable, 40, 200, 120, 120);

		// header tooltips
	    ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
	    for (int coldIdx = 0; coldIdx < wikiTable.getColumnCount(); coldIdx++) {
	      switch (coldIdx) {
			case 0:
			      tips.setToolTip(wikiTable.getColumnModel().getColumn(coldIdx), "ID Wiki du Type de Page (namespace)");
				break;
			case 1:
			    tips.setToolTip(wikiTable.getColumnModel().getColumn(coldIdx), "Type de Page Wiki (namespace)");
				break;
			default:
				break;
			}
	    }
	    wikiTable.getTableHeader().addMouseMotionListener(tips);

		JScrollPane wikiScroll = new JScrollPane(wikiTable);
		wikiScroll.setBorder(ui.createTitleBorder("Données du Wiki"));

		return wikiScroll;
	}

	private JComponent buildProgressionPane() {

		progressLogs = new JTextPane();
		progressLogs.setContentType("text/html"); // let the text pane know this is what you want
		progressLogs.setEditable(false); // as before
		progressLogs.setBackground(null); // this is the same as a JLabel
		progressLogs.setBorder(new EmptyBorder(10, 10, 10, 10)); // remove the border

		progressScroll = new JScrollPane(progressLogs);
		progressScroll.setBorder(ui.createTitleBorder("Progression"));
		progressScroll.setPreferredSize(new Dimension(500, 300));

		return progressScroll;
	}

	private JComponent buildLogsPane() {
		logsUI = new JTextArea(15, 10);
		logsUI.setEditable(false);

		JScrollPane component = new JScrollPane(logsUI);
		component.setBorder(ui.createTitleBorder("Logs"));

		return component;
	}

	private void addActionBar(JPanel parent) {
		actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		TitledBorder titleBorder = ui.createTitleBorder("Actions");
		actionBar.setBorder(titleBorder);
		actionBar.setPreferredSize(new Dimension(-1, 70));
		actionBar.setVisible(false);
		GridBagConstraints cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 1;
		cst.gridwidth = 2;
		cst.gridheight = 1;
		cst.insets = new Insets(5, 5, 5, 5);
		cst.weightx = 0;
		cst.weighty = 0;
		cst.fill = GridBagConstraints.HORIZONTAL;
		cst.anchor = GridBagConstraints.NORTHWEST;
		parent.add(actionBar, cst);

		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());
		container.setBorder(new EmptyBorder(2, 5, 5, 5));
		actionBar.setPreferredSize(new Dimension(-1, 70));
		actionBar.add(container);

		// Button about : retrieve wiki page names
		actionBtnGetPageNames = ui.createButton(
				"<html><div style=\"white-space: nowrap;\">&nbsp;Récupérer les noms de pages <span style=\"color: fuchsia;\">*</span></div></html>");
		actionBtnGetPageNames.addActionListener((ActionEvent e) -> {
			AppView.this.performAction(ActionKey.GET_PAGE_NAMES);
		});
		actionBtnGetPageNames.setIcon(ui.launchIcon);
		cst = new GridBagConstraints();
		cst.weightx = 0;
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		container.add(actionBtnGetPageNames, cst);

		// Infos label about : retrieve wiki page names
		JPanel namesProcessPane = new JPanel();
		namesProcessPane.setBorder(new EmptyBorder(0, 10, 0, 0));
		namesProcessPane.setLayout(new GridLayout(1, 2));
		cst = new GridBagConstraints();
		cst.weightx = 2;
		cst.weighty = 1;
		cst.gridwidth = 2;
//		cst.fill = GridBagConstraints.BOTH;
		container.add(namesProcessPane, cst);

		JPanel namesStatusPane = new JPanel(new FlowLayout());
		namesProcessPane.add(namesStatusPane);

		statusProcessNamesState = new JLabel(" ");
		namesStatusPane.add(statusProcessNamesState);

		// Action button : wiki page names
		saveNamesBtn = ui.createButton(ui.downloadIcon);
		saveNamesBtn.setBackground(null);
		saveNamesBtn.setVisible(false);
		saveNamesBtn.addActionListener((ActionEvent e) -> {
			AppView.this.performAction(ActionKey.SAVE_NAMES_FILES);
		});
		namesStatusPane.add(saveNamesBtn);

		// next arrow
		cst = new GridBagConstraints();
		cst.weighty = 1;
		cst.fill = GridBagConstraints.VERTICAL;
		JLabel next = new JLabel(ui.nextArrowIcon);
		next.setPreferredSize(new Dimension(100, -1));
		next.setMinimumSize(new Dimension(100, -1));
		container.add(next, cst);

		// Button about : export process
		actionBtnExport = ui
				.createButton("<html><div style=\"white-space: nowrap;\">&nbsp;Exporter les pages</div></html>");
		actionBtnExport.setEnabled(false);
		actionBtnExport.addActionListener((ActionEvent e) -> {
			AppView.this.performAction(ActionKey.EXPORT_PAGES);
		});
		cst = new GridBagConstraints();
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		container.add(actionBtnExport, cst);

		// Infos label about : export process
		statusProcessExportState = new JLabel(" ");
		JPanel exportStatus = new JPanel();
		exportStatus.setBorder(new EmptyBorder(0, 10, 0, 0));
		exportStatus.setLayout(new GridLayout(1, 2));
		exportStatus.add(statusProcessExportState);
		cst = new GridBagConstraints();
		cst.weightx = 2;
		cst.weighty = 1;
		cst.gridwidth = 2;
		container.add(exportStatus, cst);

		// next arrow
		cst = new GridBagConstraints();
		cst.weighty = 1;
		cst.fill = GridBagConstraints.VERTICAL;
		next = new JLabel(ui.nextArrowIcon);
		next.setPreferredSize(new Dimension(100, -1));
		next.setMinimumSize(new Dimension(100, -1));
		container.add(next, cst);

		// Button : save exports ZIP file
		actionBtnSaveXML = ui
				.createButton("<html><div style=\"white-space: nowrap;\">&nbsp;Enregistrer...</div></html>");
		actionBtnSaveXML.setEnabled(false);
		actionBtnSaveXML.addActionListener((ActionEvent e) -> {
			AppView.this.performAction(ActionKey.SAVE_XML_FILES);
		});

		cst = new GridBagConstraints();
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		container.add(actionBtnSaveXML, cst);
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
		cancelButtonContainer.setVisible(false);
		cst = new GridBagConstraints();
		cst.gridx = 1;
		cst.gridy = 0;
		cst.insets = new Insets(10, 8, 10, 8);
		cst.weightx = 0;
		cst.weighty = 0;
		cst.fill = GridBagConstraints.VERTICAL;
		component.add(cancelButtonContainer, cst);

		cancelButton = ui.createButton("Interrompre", ui.cancelIcon);
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
		switch (key) {
		case GET_PAGE_NAMES:
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.performGetPageNames();
				}
			}).start();
			break;
		case EXPORT_PAGES:
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.performExportPages();
				}
			}).start();
			break;
		case SAVE_XML_FILES:
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.performSaveXMLFiles();
				}
			}).start();
			break;
		case STOP_CURRENT_PROCESS:
			progressBar.setString("En cours d'interruption ...");
			progressBar.setIndeterminate(true);
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.stopCurrentProcess();
				}
			}).start();
			break;
		case SAVE_NAMES_FILES:
			new Thread(() -> {
				for (IActionListener listener : this.listeners) {
					listener.performSaveNamesOnlyFiles();
				}
			}).start();
			break;
		default:
			break;
		}
	}

	@Override
	public void onApplicationStateChange(AppState state) {
		SwingUtilities.invokeLater(() -> {
			updateCursor(state);
			switch (state) {
			case GET_NAMES_PROCESSING:
				// logs
				cleanLog();
				cleanProgress();

				// disable UI action buttons
				disable(this.actionBtnGetPageNames);
				disable(this.actionBtnExport);
				disable(this.actionBtnSaveXML);
				saveNamesBtn.setEnabled(false);
				saveNamesBtn.setVisible(false);

				// Progress bar update
				cleanProgress();
				progressBar.setString("Récupération des noms de pages ...");
				progressBar.setIndeterminate(true);
				progressBar.setVisible(true);

				// cancel process button
				cancelButton.getParent().setVisible(true);
				cancelButton.setEnabled(true);
				cancelButton.requestFocusInWindow();

				// process indicator
				statusProcessNamesState.setText(" En cours");
				statusProcessNamesState.setIcon(ui.loadingIcon);
				statusProcessNamesState.setForeground(Color.ORANGE);
				break;
			case GET_NAMES_DONE:

				// UI action buttons
				enable(this.actionBtnGetPageNames);
				enable(this.actionBtnExport);
				this.actionBtnExport.requestFocusInWindow();
				saveNamesBtn.setVisible(true);
				saveNamesBtn.setEnabled(true);

				// process cancel button
				cancelButton.setEnabled(false);

				// Progress bar update
				progressBar.setValue(0);
				progressBar.setIndeterminate(false);
				progressBar.setString("Noms de pages récupérés : " + model.countNames());
				progressBar.getModel().setMinimum(0);
				progressBar.getModel().setMaximum(model.countNames());

				// process indicator
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
				wikiTable.repaint();
				break;
			case EXPORT_PAGES_PROCESSING:
				// logs
				cleanLog();
				cleanProgress();

				// disable UI action buttons
				disable(this.actionBtnGetPageNames);
				disable(this.actionBtnExport);
				disable(this.actionBtnSaveXML);
				saveNamesBtn.setEnabled(false);

				// process cancel button
				cancelButton.getParent().setVisible(true);
				cancelButton.setEnabled(true);
				cancelButton.requestFocusInWindow();

				// Progress bar update
				cleanProgress();
				progressBar.setValue(0);
				progressBar.setIndeterminate(false);
				progressBar.setVisible(true);
				progressBar.setString("Export des pages wiki ...");

				// process indicator
				statusProcessExportState.setText(" En cours");
				statusProcessExportState.setIcon(ui.loadingIcon);
				statusProcessExportState.setForeground(Color.ORANGE);
				break;
			case EXPORT_PAGES_DONE:
				// UI action buttons
				enable(this.actionBtnGetPageNames);
				enable(this.actionBtnExport);
				this.actionBtnSaveXML.setEnabled(true);
				this.actionBtnSaveXML.setIcon(ui.downloadIcon);
				this.actionBtnSaveXML.requestFocusInWindow();
				saveNamesBtn.setEnabled(true);

				// process cancel button
				cancelButton.setEnabled(false);

				// Progress bar update
				progressBar.setIndeterminate(false);
				progressBar.setString("Page exportées : " + model.countExportSuccess());

				// process indicator
				if (model.getCurrenProcessState() == TaskStatus.DONE_FAILED) {
					statusProcessExportState.setIcon(ui.failIcon);
					statusProcessExportState.setText("Erreur(s)");
					statusProcessExportState.setForeground(Color.RED);
				} else {
					statusProcessExportState.setIcon(ui.successIcon);
					statusProcessExportState.setText("OK");
					statusProcessExportState.setForeground(Color.GREEN);
				}
				wikiTable.repaint();
				break;
			case SAVE_XML_FILES_PROCESSING:
			case SAVE_NAMES_FILES_PROCESSING:
				// logs
				cleanLog();
				cleanProgress();

				// disable UI action buttons
				disable(this.actionBtnGetPageNames);
				disable(this.actionBtnExport);
				disable(this.actionBtnSaveXML);
				saveNamesBtn.setEnabled(false);

				// process cancel button
				cancelButton.getParent().setVisible(false);
				cancelButton.setEnabled(false);

				// Progress bar update
				this.cleanProgress();
				progressBar.setString("Création de l'archive ZIP");
				progressBar.setIndeterminate(true);
				break;
			case SAVE_XML_FILES_DONE:
			case SAVE_NAMES_FILES_DONE:
				// logs
				File exportsDataFile = model.getExportsDataFile();
				if (exportsDataFile != null) {
					this.appendProgress("<br>Données d'export enregistré dans l'archive :<br><br><b>"
							+ exportsDataFile.getAbsolutePath() + "</b>");
				}

				// update action buttons
				enable(this.actionBtnGetPageNames);
				enable(this.actionBtnExport);
				saveNamesBtn.setVisible(true);
				saveNamesBtn.setEnabled(true);
				if (isExportXMLAvailable()) {
					this.actionBtnSaveXML.setEnabled(true);
					this.actionBtnSaveXML.setIcon(ui.downloadIcon);
					this.actionBtnSaveXML.requestFocusInWindow();
				}

				// process cancel button
				cancelButton.setEnabled(false);

				// progress bar
				progressBar.setValue(0);
				progressBar.setIndeterminate(false);
				progressBar.setVisible(false);
				break;
			default:
				break;
			}
		});
	}

	private boolean isExportXMLAvailable() {
		for (ExportData data : model.getExportDataList()) {
			if (data.getExportXML() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onExportDone(ExportData data) {
		if (data.getStatus() == TaskStatus.DONE_SUCCESS) {
			SwingUtilities.invokeLater(() -> {
				this.progressBar.setValue(this.progressBar.getValue() + data.getPageNames().size());
			});
		}
	}

	@Override
	public void onNamespaceDataUpdate(WikiNamespaceData wikiData, TaskStatus oldStatus, TaskStatus newStatus) {
	}

	@Override
	public void onExportInitDone(ExportData data) {
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
		case SAVE_NAMES_FILES_DONE:
			AppView.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case GET_NAMES_PROCESSING:
		case EXPORT_PAGES_PROCESSING:
		case SAVE_XML_FILES_PROCESSING:
		case SAVE_NAMES_FILES_PROCESSING:
			AppView.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			break;
		default:
			break;
		}
	}
}