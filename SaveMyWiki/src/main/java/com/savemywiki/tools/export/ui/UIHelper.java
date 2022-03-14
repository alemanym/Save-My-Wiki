package com.savemywiki.tools.export.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightContrastIJTheme;
import com.formdev.flatlaf.ui.FlatButtonBorder;
import com.formdev.flatlaf.ui.FlatDropShadowBorder;

public class UIHelper {
	
	protected ImageIcon loadingIcon;
	protected ImageIcon launchIcon;
	protected ImageIcon failIcon;
	protected ImageIcon successIcon;
	protected ImageIcon downloadIcon;
	protected ImageIcon nextArrowIcon;
	protected ImageIcon cancelIcon;
	
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

}
