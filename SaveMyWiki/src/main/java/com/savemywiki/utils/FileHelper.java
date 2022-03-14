package com.savemywiki.utils;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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

	public boolean storeExportsAsZipArchive(List<ExportData> dataList, File destZipFile) {

		List<File> filesToZip = new ArrayList<File>();

		try {

			List<File> successFiles = new ArrayList<File>();
			List<File> errorFiles = new ArrayList<File>();

			for (ExportData data : dataList) {

				if (data.getStatus() == TaskStatus.DONE_SUCCESS) {

					// write export XML
					String fileName = (data.getRetrySource() == null ? data.getId()
							: (data.getRetrySource().getId() + "-" + data.getId())) + " - "
							+ data.getNamespace().format() + ".xml";
					File fileToZip = new File(System.getProperty("java.io.tmpdir"), fileName);
					FileWriter fw = new FileWriter(fileToZip);
					fw.write(data.getExportXML());
					fw.close();
					successFiles.add(fileToZip);

					// write page names not exported
					fileName = "[Exportés] "
							+ (data.getRetrySource() == null ? data.getId()
									: (data.getRetrySource().getId() + "-" + data.getId()))
							+ " - " + data.getNamespace().format() + " - noms de pages.txt";
					fileToZip = new File(System.getProperty("java.io.tmpdir"), fileName);
					fw = new FileWriter(fileToZip);

					StringBuffer sb = new StringBuffer();
					sb.append("Export #" + data.getId() + " échoué - Liste des noms de page non-exportés :\r\n");
					sb.append("======================================================================");
					for (String name : data.getPageNames()) {
						sb.append("\r\n");
						sb.append(name);
					}
					fw.write(sb.toString());
					fw.close();
					successFiles.add(fileToZip);

				} else if (data.getStatus() != TaskStatus.DONE_FAILED || data.getRetrySource() != null) {

					// write page names not exported
					String fileName = "[Non-exportés] "
							+ (data.getRetrySource() == null ? data.getId()
									: (data.getRetrySource().getId() + "-" + data.getId()))
							+ " - " + data.getNamespace().format() + " - noms de pages.txt";
					File fileToZip = new File(System.getProperty("java.io.tmpdir"), fileName);
					FileWriter fw = new FileWriter(fileToZip);

					StringBuffer sb = new StringBuffer();
					sb.append("Export #" + data.getId() + " échoué - Liste des noms de page non-exportés :\r\n");
					sb.append("======================================================================");
					for (String name : data.getPageNames()) {
						sb.append("\r\n");
						sb.append(name);
					}
					fw.write(sb.toString());
					fw.close();
					errorFiles.add(fileToZip);

				}
			}

			zip(successFiles, errorFiles, destZipFile);

			Desktop.getDesktop().open(destZipFile.getParentFile());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			filesToZip.forEach(f -> f.delete());
		}

		return true;
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
	 * Compresses a list of files to a destination zip file
	 * 
	 * @param listFiles   A collection of files and directories
	 * @param destZipFile The path of the destination zip file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void zip(List<File> successFiles, List<File> errorFiles, File destZipFile) throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(destZipFile);
				ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			zipDirectory(successFiles, "succès", zipOut);
			zipDirectory(errorFiles, "errors", zipOut);
		}
	}

	/**
	 * Adds a directory to the current zip output stream
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
			zos.putNextEntry(new ZipEntry(relativePath + "/" + file.getName()));
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			long bytesRead = 0;
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = bis.read(bytesIn)) != -1) {
				zos.write(bytesIn, 0, read);
				bytesRead += read;
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
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		long bytesRead = 0;
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = bis.read(bytesIn)) != -1) {
			zos.write(bytesIn, 0, read);
			bytesRead += read;
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
