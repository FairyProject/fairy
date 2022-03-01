/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.util;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.function.BiConsumer;

@UtilityClass
public class FileUtil {
	private final char UNIX_SEPARATOR = '/';
	private final char WINDOWS_SEPARATOR = '\\';
	private final char SYSTEM_SEPARATOR = File.separatorChar;
	private final char OTHER_SEPARATOR;
	private final char EXTENSION_SEPARATOR = '.';

	static {
		if (isSystemWindows()) {
			OTHER_SEPARATOR = UNIX_SEPARATOR;
		} else {
			OTHER_SEPARATOR = WINDOWS_SEPARATOR;
		}
	}
	public File getSelfJar() throws URISyntaxException {
		return new File(FileUtil.class.getProtectionDomain().getCodeSource().getLocation()
				.toURI());
	}

	public void forEachDirectoryInResources(Class<?> searchBy, String folderName, BiConsumer<String, InputStream> inputStreamConsumer) {
		ClassLoader classLoader = searchBy.getClassLoader();

		URI uri;
		try {
			uri = classLoader.getResource(folderName).toURI();
		} catch (URISyntaxException | NullPointerException e) {
			throw new RuntimeException(e);
		}

		if (uri.getScheme().contains("jar")) {
			// jar
			try {
				URL jar = searchBy.getProtectionDomain().getCodeSource().getLocation();

				Path jarFile = Paths.get(jar.toString().substring("file:".length()));
				FileSystem fs = FileSystems.newFileSystem(jarFile, (ClassLoader) null);
				final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath(folderName));
				directoryStream.forEach(p -> {
					InputStream inputStream = searchBy.getResourceAsStream(p.toString());
					inputStreamConsumer.accept(p.getFileName().toString(), inputStream);
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			// IDE
			Path path = Paths.get(uri);
			try {
				DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
				for(Path p : directoryStream){
					InputStream inputStream = new FileInputStream(p.toFile());
					inputStreamConsumer.accept(p.getFileName().toString(), inputStream);
				}
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}


	}

	@SuppressWarnings("rawtypes")
	public InputStream getResource(Class target, String filename) {
		try {
			final URL url = target.getClassLoader().getResource(filename);
			if (url == null) {
				return null;
			} else {
				final URLConnection connection = url.openConnection();
				connection.setUseCaches(false);
				return connection.getInputStream();
			}
		} catch (final IOException ignored) {
			return null;
		}
	}
	
	public void writeInputStreamToFile(InputStream inputStream, File file) {
		try {
			final String text = new String(IOUtil.readFully(inputStream), StandardCharsets.UTF_8);
			final FileWriter fileWriter = new FileWriter(FileUtil.createNewFile(file));
			fileWriter.write(text);
			fileWriter.close();
		} catch (final IOException ignored) {
		}
	}

	public File createNewFile(File file) {
		if (file != null && !file.exists()) {
			try {
				file.createNewFile();
			} catch (final Exception ignored) {
			}
		}
		return file;
	}

	public void createNewFileAndPath(File file) {
		if (!file.exists()) {
			final String filePath = file.getPath();
			final int index = filePath.lastIndexOf(File.separator);
			File folder;
			if ((index >= 0) && (!(folder = new File(filePath.substring(0, index))).exists())) {
				folder.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (final IOException ignored) {
			}
		}
	}

	public String getName(final String fileName) {
		if (fileName == null) {
			return null;
		}
		final int index = indexOfLastSeparator(fileName);
		return fileName.substring(index + 1);
	}

	public int indexOfLastSeparator(final String fileName) {
		if (fileName == null) {
			return -1;
		}
		final int lastUnixPos = fileName.lastIndexOf(UNIX_SEPARATOR);
		final int lastWindowsPos = fileName.lastIndexOf(WINDOWS_SEPARATOR);
		return Math.max(lastUnixPos, lastWindowsPos);
	}

	public String getExtension(final String fileName) throws IllegalArgumentException {
		if (fileName == null) {
			return null;
		}
		final int index = indexOfExtension(fileName);
		if (index == -1) {
			return "";
		}
		return fileName.substring(index + 1);
	}

	public int indexOfExtension(final String fileName) throws IllegalArgumentException {
		if (fileName == null) {
			return -1;
		}
		if (isSystemWindows()) {
			// Special handling for NTFS ADS: Don't accept colon in the fileName.
			final int offset = fileName.indexOf(':', getAdsCriticalOffset(fileName));
			if (offset != -1) {
				throw new IllegalArgumentException("NTFS ADS separator (':') in file name is forbidden.");
			}
		}
		final int extensionPos = fileName.lastIndexOf(EXTENSION_SEPARATOR);
		final int lastSeparator = indexOfLastSeparator(fileName);
		return lastSeparator > extensionPos ? -1 : extensionPos;
	}

	private int getAdsCriticalOffset(final String fileName) {
		// Step 1: Remove leading path segments.
		final int offset1 = fileName.lastIndexOf(SYSTEM_SEPARATOR);
		final int offset2 = fileName.lastIndexOf(OTHER_SEPARATOR);
		if (offset1 == -1) {
			if (offset2 == -1) {
				return 0;
			}
			return offset2 + 1;
		}
		if (offset2 == -1) {
			return offset1 + 1;
		}
		return Math.max(offset1, offset2) + 1;
	}

	boolean isSystemWindows() {
		return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
	}

}
