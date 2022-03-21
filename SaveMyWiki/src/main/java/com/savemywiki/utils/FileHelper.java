package com.savemywiki.utils;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.TaskStatus;

public class FileHelper {

	/**
	 * A constants for buffer size used to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	public boolean storeNamesAsZipArchive(List<ExportData> dataList, File destZipFile) {

		List<File> filesToZip = new ArrayList<File>();

		try {

			// 1 - create data files
			for (ExportData data : dataList) {

				// file name
				String fileName = "[Noms] "
						+ (data.getRetrySource() == null ? data.getId()
								: (data.getRetrySource().getId() + "-" + data.getId()))
						+ " - " + data.getNamespace().format() + ".txt";

				// file content
				StringBuffer sb = new StringBuffer();
				sb.append("Liste #" + data.getId() + " - Noms de page :\r\n");
				sb.append("======================================================================");
				for (String name : data.getPageNames()) {
					sb.append("\r\n");
					sb.append(name);
				}

				// write file
				File fileToZip = new File(System.getProperty("java.io.tmpdir"), fileName);
				Files.write(fileToZip.toPath(), sb.toString().getBytes());

				filesToZip.add(fileToZip);
			}

			// 2 - archive to a zip file
			zip(filesToZip, destZipFile);

			Desktop.getDesktop().open(destZipFile.getParentFile());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			filesToZip.forEach(f -> f.delete());
		}
		return true;
	}

	/**
	 * Compresses a simple list of files to a destination zip file.
	 */
	public void zip(List<File> files, File destZipFile) throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(destZipFile);
				ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			for (File file : files) {
				zipFile(file, zipOut);
			}
		}
	}

	/**
	 * Store export data as ZIP file archive.
	 */
	public boolean storeExportsAsZipArchive(List<ExportData> dataList, File destZipFile) {

		List<File> filesToZip = new ArrayList<File>();

		try {

			List<File> successFiles = new ArrayList<File>();
			List<File> errorFiles = new ArrayList<File>();
			List<File> undefinedFiles = new ArrayList<File>();

			// Create data files
			for (ExportData data : dataList) {
				System.out.print("\r\n"+ (data.getRetrySource() != null ? data.getRetrySource().getId() + ":" : "") + data.getId());

				if (data.getStatus() == TaskStatus.DONE_SUCCESS) {
					System.out.print(" > case SUCCESS");
					// case : export success

					// - XML data
					successFiles.add(writeXMLExportFile(data));
					// - page names
					successFiles.add(writeWellExportedPageNamesFile(data));

				} else if (data.getStatus() == TaskStatus.DONE_FAILED) {
					System.out.print(" > case FAILED");
					// case : export failed
					if (data.getRetrySource() == null) {
						System.out.print(" > case FIRST ATTEMPT");
						// case : first attempt failed
						if (!hasBeenRetried(data, dataList) || hasBeenRetriedAllFailed(data, dataList)) {
							// case : failed without retry launched or all retry failed
							errorFiles.add(writeWrongExportedPageNamesFile(data));
						} else {
							// case : retry success or partial => no first attempt result storage
						}
					} else {
						System.out.print(" > case RETRY");
						// case : failed retry
						if (!hasBeenRetriedAllFailed(data.getRetrySource(), dataList)) {
							errorFiles.add(writeWrongExportedPageNamesFile(data));
						}
					}
				} else {
					System.out.print(" > case OTHER");
					if (data.getRetrySource() == null) {
						System.out.print(" > case FIRST ATTEMPT");
						// data without export attempt
						undefinedFiles.add(writeNotExportedPageNamesFile(data));
					} else {
						System.out.print(" > case RETRY");
						// retry not launched
						if (hasBeenRetried(data.getRetrySource(), dataList)) {
							// retry not initiated => No store data
							undefinedFiles.add(writeNotExportedPageNamesFile(data));
						}
					}
				}
			}

			// Archive to a ZIP file
			zip(successFiles, errorFiles, undefinedFiles, destZipFile);

			Desktop.getDesktop().open(destZipFile.getParentFile());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			filesToZip.forEach(f -> f.delete());
		}
		return true;
	}

	private boolean hasBeenRetried(ExportData dataToCheck, List<ExportData> dataList) {
		for (ExportData exportData : dataList) {
			if (exportData.getRetrySource() != null && exportData.getId() == dataToCheck.getId()
					&& exportData.getStatus() != TaskStatus.UNDEFINED) {
				return true;
			}
		}
		return false;
	}

	private boolean hasBeenRetriedAllFailed(ExportData dataToCheck, List<ExportData> dataList) {
		boolean retryInit = false;
		for (ExportData exportData : dataList) {
			if (exportData.getRetrySource() != null && exportData.getId() == dataToCheck.getId()) {
				retryInit = true;
				switch (exportData.getStatus()) {
				case DONE_FAILED:
					continue;
				default:
					return false;
				}
			}
		}
		return retryInit;
	}

	private File writeWrongExportedPageNamesFile(ExportData data) throws IOException {
		// file name
		StringBuffer sbName = new StringBuffer();
		sbName.append("[Echec][Noms] ");
		sbName.append(
				data.getRetrySource() == null ? data.getId() : (data.getRetrySource().getId() + "-" + data.getId()));
		sbName.append(" - ");
		sbName.append(data.getNamespace().format());
		sbName.append(".txt");

		// file content
		StringBuffer sb = new StringBuffer();
		sb.append("Export #" + data.getId() + " échoué - Page non-exportés :\r\n");
		sb.append("======================================================================");
		for (String name : data.getPageNames()) {
			sb.append("\r\n");
			sb.append(name);
		}

		// write file
		File fileToZip = new File(System.getProperty("java.io.tmpdir"), sbName.toString());
		Files.write(fileToZip.toPath(), sb.toString().getBytes());
		return fileToZip;
	}

	private File writeWellExportedPageNamesFile(ExportData data) throws IOException {
		// File name
		StringBuffer sbName = new StringBuffer();
		sbName.append("[Noms] ");
		sbName.append(
				data.getRetrySource() == null ? data.getId() : (data.getRetrySource().getId() + "-" + data.getId()));
		sbName.append(" - ");
		sbName.append(data.getNamespace().format());
		sbName.append(".txt");

		// file content
		StringBuffer sb = new StringBuffer();
		sb.append("Export #" + data.getId() + " réussi - Liste des page exportés :\r\n");
		sb.append("======================================================================");
		for (String name : data.getPageNames()) {
			sb.append("\r\n");
			sb.append(name);
		}

		// write file
		File fileToZip = new File(System.getProperty("java.io.tmpdir"), sbName.toString());
		Files.write(fileToZip.toPath(), sb.toString().getBytes());
		return fileToZip;
	}

	private File writeNotExportedPageNamesFile(ExportData data) throws IOException {
		// File name
		StringBuffer sbName = new StringBuffer();
		sbName.append("[Non-Traités][Noms] ");
		sbName.append(
				data.getRetrySource() == null ? data.getId() : (data.getRetrySource().getId() + "-" + data.getId()));
		sbName.append(" - ");
		sbName.append(data.getNamespace().format());
		sbName.append(".txt");

		// file content
		StringBuffer sb = new StringBuffer();
		sb.append("Export #" + data.getId() + "non-traité - Pages non-exportés :\r\n");
		sb.append("======================================================================");
		for (String name : data.getPageNames()) {
			sb.append("\r\n");
			sb.append(name);
		}

		// write file
		File fileToZip = new File(System.getProperty("java.io.tmpdir"), sbName.toString());
		Files.write(fileToZip.toPath(), sb.toString().getBytes());
		return fileToZip;
	}

	private File writeXMLExportFile(ExportData data) throws IOException {
		// file name
		StringBuffer sbName = new StringBuffer();
		sbName.append("[Données] ");
		sbName.append(
				data.getRetrySource() == null ? data.getId() : (data.getRetrySource().getId() + "-" + data.getId()));
		sbName.append(" - ");
		sbName.append(data.getNamespace().format());
		sbName.append(".xml");

		// file content
		byte[] fileContent = data.getExportXML().getBytes();

		// write file
		File fileToZip = new File(System.getProperty("java.io.tmpdir"), sbName.toString());
		Files.write(fileToZip.toPath(), fileContent);
		return fileToZip;
	}

	public boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	/**
	 * Compresses success and error list of files to a destination zip file.
	 */
	public void zip(List<File> successFiles, List<File> errorFiles, List<File> undefinedFiles, File destZipFile)
			throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(destZipFile);
				ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			zipDirectory(successFiles, "Exports réussis", zipOut);
			zipDirectory(errorFiles, "Exports échoués", zipOut);
			zipDirectory(undefinedFiles, "Exports non-traités", zipOut);
		}
	}

	/**
	 * Adds a directory to the current zip output stream.
	 * 
	 * @param folder       the directory to be added
	 * @param relativePath the relative path of the directory
	 * @param zos          the current zip output stream
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void zipDirectory(List<File> fileList, String relativePath, ZipOutputStream zos)
			throws FileNotFoundException, IOException {
		for (File file : fileList) {
			zos.putNextEntry(
					new ZipEntry(relativePath != null ? (relativePath + "/" + file.getName()) : file.getName()));
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
				byte[] bytesIn = new byte[BUFFER_SIZE];
				int read = 0;
				while ((read = bis.read(bytesIn)) != -1) {
					zos.write(bytesIn, 0, read);
				}
			}
			zos.closeEntry();
		}
	}

	/**
	 * Adds a file to the current zip output stream
	 * 
	 * @param file the file to be added
	 * @param zos  the current zip output stream
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void zipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
		zos.putNextEntry(new ZipEntry(file.getName()));
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = bis.read(bytesIn)) != -1) {
				zos.write(bytesIn, 0, read);
			}
		}
		zos.closeEntry();
	}

	public File openSaveFileWindow(Frame parentFrame, FileFilter fileFilter) {
		File fileChoosen = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(fileFilter);
		int rVal = fileChooser.showSaveDialog(parentFrame);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			fileChoosen = fileChooser.getSelectedFile();
		}
		if (rVal == JFileChooser.CANCEL_OPTION) {
			return null;
		}
		return fileChoosen;
	}

	public static class ZipFileFilter extends FileFilter {
		@Override
		public String getDescription() {
			return "ZIP (*.zip)";
		}

		@Override
		public boolean accept(File f) {
			String filename = f.getName().toLowerCase();
			return f.isFile() && !f.isDirectory() && filename.contains(".")
					&& filename.substring(filename.lastIndexOf(".") + 1).equals("zip");
		}
	}

}
