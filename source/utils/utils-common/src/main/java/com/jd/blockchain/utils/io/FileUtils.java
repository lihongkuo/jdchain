package com.jd.blockchain.utils.io;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author haiq
 *
 */
public class FileUtils {

	public static final String DEFAULT_CHARSET = "UTF-8";

	public static boolean existFile(String filePath) {
		File file = new File(filePath);
		return file.isFile();
	}

	public static boolean existDirectory(String dir) {
		File file = new File(dir);
		return file.isDirectory();
	}

	public static boolean makeDirectory(String dir) {
		File file = new File(dir);
		return file.mkdirs();
	}

	/**
	 * 返回完整的绝对路径；
	 * 
	 * @param path path
	 * @return String
	 */
	public static String getFullPath(String path) {
		try {
			File file = new File(path);
			return file.getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * 以默认字符集（UTF-8）读取指定文件的首行；
	 * 
	 * @param file file
	 * @return String
	 * @throws IOException exception
	 */
	public static String readFirstLine(File file) throws IOException {
		return readFirstLine(file, DEFAULT_CHARSET);
	}

	/**
	 * 读取指定文件的首行；
	 * 
	 * @param file file
	 * @param charset
	 *            字符集；
	 * @return 返回首行非空行；返回结果不会自动截取两头的空字符串；
	 * @throws IOException exception
	 */
	public static String readFirstLine(File file, String charset) throws IOException {
		FileInputStream in = new FileInputStream(file);
		try {
			InputStreamReader reader = new InputStreamReader(in, charset);
			return getFirstLine(reader);
		} finally {
			in.close();
		}
	}

	public static String[] readLines(File file) {
		return readLines(file, DEFAULT_CHARSET);
	}

	/**
	 * 返回指定文件的所有行；
	 * 
	 * @param file file
	 * @param charset
	 *            字符集；
	 * @return 返回首行非空行；返回结果不会自动截取两头的空字符串；
	 */
	public static String[] readLines(File file, String charset) {
		try (FileInputStream in = new FileInputStream(file);
				InputStreamReader reader = new InputStreamReader(in, charset);) {
			return getLines(reader);
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static void writeLines(String[] lines, File file) {
		writeLines(lines, file, DEFAULT_CHARSET);
	}

	public static void writeLines(String[] lines, File file, String charset) {
		try (FileOutputStream out = new FileOutputStream(file, false);
				OutputStreamWriter writer = new OutputStreamWriter(out, charset);
				BufferedWriter bfw = new BufferedWriter(writer);) {
			for (String line : lines) {
				writer.write(line);
				writer.write("\r\n");
			}

			bfw.flush();
			writer.flush();
			out.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static String getFirstLine(Reader reader) throws IOException {
		BufferedReader bfr = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
		try {
			String line = null;
			while ((line = bfr.readLine()) != null) {
				return line;
			}
			return null;
		} finally {
			bfr.close();
		}
	}

	public static String[] getLines(Reader reader) throws IOException {
		BufferedReader bfr = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
		try {
			ArrayList<String> lines = new ArrayList<String>();
			String line = null;
			while ((line = bfr.readLine()) != null) {
				lines.add(line);
			}
			return lines.toArray(new String[lines.size()]);
		} finally {
			bfr.close();
		}
	}

	/**
	 * 以默认字符集（UTF-8）将指定的文本保存到指定的文件中；
	 * 
	 * @param file
	 *            要保存的文件；
	 * @param text
	 *            文本内容；
	 */
	public static void writeText(String text, File file) {
		writeText(text, file, DEFAULT_CHARSET);
	}

	/**
	 * 将指定的文本保存到指定的文件中；
	 * @param text
	 *            文本内容；
	 * @param file
	 *            要保存的文件；
	 * @param charset
	 *            字符集；
	 */
	public static void writeText(String text, File file, String charset) {
		try (FileOutputStream out = new FileOutputStream(file, false)) {
			writeText(text, out, charset);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}
	
	public static void writeBytes(byte[] content, File file) {
		try (FileOutputStream out = new FileOutputStream(file, false)) {
			out.write(content);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}
	
	public static void appendBytes(byte[] content, File file) {
		try (FileOutputStream out = new FileOutputStream(file, true)) {
			out.write(content);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static void writeText(String text, OutputStream out, String charset) {
		try (OutputStreamWriter writer = new OutputStreamWriter(out, charset);) {
			writer.write(text);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static void writeProperties(Properties props, OutputStream out) {
		writeProperties(props, out, DEFAULT_CHARSET);
	}

	public static void writeProperties(Properties props, OutputStream out, String charset) {
		try (OutputStreamWriter writer = new OutputStreamWriter(out, charset);) {
			props.store(writer, null);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	/**
	 * 以默认字符集（UTF-8）从文件读取文本；
	 * @param file file
	 * @return String
	 */
	public static String readText(String file) {
		return readText(new File(file), DEFAULT_CHARSET);
	}

	/**
	 * 从文件读取文本；
	 * @param file file
	 * @param charset charset
	 * @return String
	 */
	public static String readText(String file, String charset) {
		return readText(new File(file), charset);
	}

	/**
	 * 以默认字符集（UTF-8）从文件读取文本；
	 * 
	 * @param file file
	 * @return String
	 */
	public static String readText(File file) {
		return readText(file, DEFAULT_CHARSET);
	}

	/**
	 * 从文件读取文本；
	 * 
	 * @param file file
	 * @param charset charset
	 * @return String
	 */
	public static String readText(File file, String charset) {
		try {
			FileInputStream in = new FileInputStream(file);
			try {
				return readText(in, charset);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static String readText(InputStream in) throws IOException {
		return readText(in, DEFAULT_CHARSET);
	}

	/**
	 * 从流读取文本；
	 * 
	 * @param in in
	 * @param charset charset
	 * @return String
	 * @throws IOException exception
	 */
	public static String readText(InputStream in, String charset) throws IOException {
		InputStreamReader reader = new InputStreamReader(in, charset);
		try {
			StringBuilder content = new StringBuilder();
			char[] buffer = new char[64];
			int len = 0;
			while ((len = reader.read(buffer)) > 0) {
				content.append(buffer, 0, len);
			}
			return content.toString();
		} finally {
			reader.close();
		}
	}

	public static byte[] readBytes(String file) {
		try {
			FileInputStream in = new FileInputStream(file);
			try {
				return BytesUtils.copyToBytes(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}
	
	public static byte[] readBytes(File file) {
		try {
			FileInputStream in = new FileInputStream(file);
			try {
				return BytesUtils.copyToBytes(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static Properties readProperties(String systemConfig) {
		return readProperties(systemConfig, DEFAULT_CHARSET);
	}

	public static Properties readProperties(String file, String charset) {
		try {
			FileInputStream in = new FileInputStream(file);
			try {
				return readProperties(in, charset);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static Properties readProperties(File file, String charset) {
		try {
			FileInputStream in = new FileInputStream(file);
			try {
				return readProperties(in, charset);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	public static Properties readProperties(InputStream in) {
		return readProperties(in, DEFAULT_CHARSET);
	}

	public static Properties readProperties(InputStream in, String charset) {
		try {
			InputStreamReader reader = new InputStreamReader(in, charset);
			try {
				Properties props = new Properties();
				props.load(reader);
				return props;
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}

	/**
	 * 根据byte数组，生成文件 filePath 文件路径 fileName 文件名称（需要带后缀，如*.jar）
	 */
	public static File getFile(byte[] bfile, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists()) {// 判断文件目录是否存在
				dir.mkdirs();
			}
			file = new File(filePath + File.separator + fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return file;
	}

	public static String getCurrentDir() {
		try {
			return new File("./").getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public static void deleteFile(String dir) {
		deleteFile(dir, false);
	}
	
	public static void deleteFile(File file) {
		deleteFile(file, false);
	}

	public static void deleteFile(String dir, boolean silent) {
		File directory = new File(dir);
		deleteFile(directory, silent);
	}

	/**
	 * 删除文件；
	 * 
	 * @param file
	 * @param silent
	 *            是否静默删除；如果为 true ，则吞噬删除过程中的异常，意味着方法即便正常返回时也有可能删除不完全；
	 */
	public static void deleteFile(File file, boolean silent) {
		if (file.isFile()) {
			try {
				file.delete();
				return;
			} catch (Exception e) {
				if (!silent) {
					throw new RuntimeIOException(e.getMessage(), e);
				}
			}
		}
		File[] files = file.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			deleteFile(f, silent);
		}
	}
}
