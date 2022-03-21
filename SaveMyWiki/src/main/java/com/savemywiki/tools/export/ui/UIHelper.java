package com.savemywiki.tools.export.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumnModel;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightContrastIJTheme;
import com.formdev.flatlaf.ui.FlatButtonBorder;
import com.formdev.flatlaf.ui.FlatDropShadowBorder;

/**
 * Swing Look&feel helper and UI Component builder.
 * 
 * @author Marc Alemany
 */
public class UIHelper {

	public ImageIcon loadingIcon;
	public ImageIcon launchIcon;
	public ImageIcon failIcon;
	public ImageIcon successIcon;
	public ImageIcon downloadIcon;
	public ImageIcon nextArrowIcon;
	public ImageIcon cancelIcon;

	public UIHelper() {
		// -- Look & feel
		try {
			UIManager.setLookAndFeel(new FlatMoonlightContrastIJTheme());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// custom Progress Bar colors
		UIManager.put("ProgressBar.background", new Color(25, 26, 42));
		UIManager.put("ProgressBar.foreground", new Color(93, 121, 181));
		UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);

		// custom tables
		UIManager.put("Table.alternateRowColor", ((Color) UIManager.get("Table.background")).darker());

		// tooltips
		UIManager.put("ToolTip.background", new Color(93, 121, 181));
		UIManager.put("ToolTip.foreground", new Color(33, 35, 53));

		loadingIcon = new ImageIcon(AppView.class.getResource("/assets/loading-spinner.gif"));
		launchIcon = new ImageIcon(AppView.class.getResource("/assets/launch-icon.png"));
		successIcon = new ImageIcon(AppView.class.getResource("/assets/ok.png"));
		failIcon = new ImageIcon(AppView.class.getResource("/assets/error.png"));
		downloadIcon = new ImageIcon(AppView.class.getResource("/assets/download.png"));
		nextArrowIcon = new ImageIcon(AppView.class.getResource("/assets/arrow-right.png"));
		cancelIcon = new ImageIcon(AppView.class.getResource("/assets/cancel.png"));
	}

	public TitledBorder createTitleBorder(String title) {
		TitledBorder paneTitle;
		FlatDropShadowBorder lineBorder = new FlatDropShadowBorder(Color.BLACK, new Insets(3, 3, 3, 3), 0.3f);
		paneTitle = new TitledBorder(lineBorder, title);
		paneTitle.setTitleFont(paneTitle.getTitleFont().deriveFont(14.f));
		paneTitle.setTitleColor(Color.WHITE);
		return paneTitle;
	}

	public JButton createButton(ImageIcon icon) {
		return createButton(null, icon);
	}

	public JButton createButton(String title, Icon ico) {
		JButton btn = new JButton(title, ico);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setVerticalAlignment(SwingConstants.CENTER);
		Color defaultColor = btn.getForeground();
		FlatButtonBorder defaultBorder = (FlatButtonBorder) btn.getBorder();
		Border noBorder = new EmptyBorder(defaultBorder.getBorderInsets(btn));
		btn.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				btn.setForeground(defaultColor);
				btn.setBorder(noBorder);
			}

			@Override
			public void focusGained(FocusEvent e) {
				btn.setForeground(Color.WHITE);
				btn.setBorder(defaultBorder);
			}
		});
		btn.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!btn.isEnabled()) {
					btn.setBorder(noBorder);
				}
			}
		});
		return btn;
	}

	public JButton createButton(String title) {
		return createButton(title, null);
	}

	public void setColumnPreferredWidths(JTable table, Integer... widths) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < widths.length; i++) {
			if (i < columnModel.getColumnCount() && widths[i] != null) {
				columnModel.getColumn(i).setPreferredWidth(widths[i]);
			}
		}
	}

	public void setColumnMinWidths(JTable table, Integer... widths) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < widths.length; i++) {
			if (i < columnModel.getColumnCount() && widths[i] != null) {
				columnModel.getColumn(i).setMinWidth(widths[i]);
			}
		}
	}

	public void setColumnMaxWidths(JTable table, Integer... widths) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < widths.length; i++) {
			if (i < columnModel.getColumnCount() && widths[i] != null) {
				columnModel.getColumn(i).setMaxWidth(widths[i]);
			}
		}
	}

}
