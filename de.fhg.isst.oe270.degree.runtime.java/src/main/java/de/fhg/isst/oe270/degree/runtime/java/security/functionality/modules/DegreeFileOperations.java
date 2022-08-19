/*
 * Copyright 2020-2022 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhg.isst.oe270.degree.runtime.java.security.functionality.modules;

import de.fhg.isst.oe270.degree.runtime.java.security.evaluation.PermissionScope;
import kotlin.Pair;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The D° security manager forbids the direct access to file operations (like opening a file).
 * This module provides functionalities for file I/O that can be used instead.
 */
public final class DegreeFileOperations {

    /*
     * Tags begin
     */

    /**
     * Tag used to identify the number of bytes read.
     */
    public static final String READ_BYTES = "READ_BYTES";

    /**
     * Tag used to identify the number of bytes written.
     */
    public static final String WRITTEN_BYTES = "WRITTEN_BYTES";

    /**
     * Tag used to identify the used file.
     */
    public static final String FILE_PATH = "FILE_PATH";

    /*
     * Tags end
     */

    /**
     * The used logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("DegreeFileOperations");

    /**
     * Default Constructor.
     */
    private DegreeFileOperations() {
    }

    /**
     * Initialize the module.
     */
    public static void init() {
        LOGGER.info(DegreeFileOperations.class.getSimpleName() + " initialized.");
    }

    /*
     * File reading
     */

    /**
     * Read the whole content of a file to a string.
     *
     * @param filepath the file to read
     * @return the content of the file
     * @throws IOException in case of I/O errors
     */
    public static String readFileToString(final String filepath) throws IOException {
        return readFile(filepath, "UTF-8");
    }

    /**
     * Read the whole content of a file to a string.
     *
     * @param filepath the file to read
     * @param encoding the encoding to use during reading
     * @return the content of the file
     * @throws IOException in case of I/O errors
     */
    public static String readFileToString(
            final String filepath, final String encoding) throws IOException {
        return readFile(filepath, encoding);
    }

    /**
     * Read the whole content of a file to a string.
     *
     * @param filepath the file to read
     * @param encoding the encoding to use during reading
     * @return the content of the file
     * @throws IOException in case of I/O errors
     */
    public static String readFileToString(
            final String filepath, final Charset encoding) throws IOException {
        return readFile(filepath, encoding.displayName());
    }

    /**
     * Internal function to read the whole content of a file.
     * Will collect additional metadata which can be used for policy enforcement.
     *
     * @param filepath Path of the file to read
     * @param encoding Used encoding
     * @return The file content as string
     * @throws IOException In case something fails during reading the file
     */
    private static String readFile(
            final String filepath, final String encoding) throws IOException {
        // open the file
        File file = new File(filepath);
        String fileContent = "";
        // we need to collect meta data which will be used by policies
        long fileSize = getFileSize(file);
        PermissionScope.getInstance().addAdditionalPermissionData(READ_BYTES, fileSize);
        PermissionScope.getInstance().addAdditionalPermissionData(FILE_PATH,
                file.getCanonicalPath());
        // the actual reading
        try {
            fileContent = FileUtils.readFileToString(file, Charset.forName(encoding));
        } catch (IOException e) {
            LOGGER.error("An error occurred during file reading. " + e.getMessage());
            throw e;
        }

        // now return the read file content
        return fileContent;
    }

    /**
     * Internal function to read the whole content of a file.
     * Will collect additional metadata which can be used for policy enforcement.
     *
     * @param filepath Path of the file to read
     * @return The file content as byte array
     * @throws IOException In case something fails during reading the file
     */
    public static byte[] readFileToByteArray(final String filepath) throws IOException {
        // open the file
        File file = new File(filepath);
        byte[] fileContent;
        // we need to collect meta data which will be used by policies
        long fileSize = getFileSize(file);
        PermissionScope.getInstance().addAdditionalPermissionData(READ_BYTES, fileSize);
        PermissionScope.getInstance().addAdditionalPermissionData(FILE_PATH,
                file.getCanonicalPath());
        // the actual reading
        try {
            fileContent = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            LOGGER.error("An error occurred during file reading. " + e.getMessage());
            throw e;
        }

        // now return the read file content
        return fileContent;
    }

    /*
     * File writing
     */

    /**
     * Write something to a file.
     *
     * @param filepath the file which is written
     * @param content  the content to write
     * @throws IOException in case any I/O error occurs
     */
    public static void writeBytesToFile(
            final String filepath,
            final byte[] content) throws IOException {
        writeFile(filepath, content, true);
    }

    /**
     * Write something to a file.
     *
     * @param filepath the file which is written
     * @param content  the content to write
     * @throws IOException in case any I/O error occurs
     */
    public static void writeStringToFile(
            final String filepath,
            final String content) throws IOException {
        writeFile(filepath, content, "UTF-8", true);
    }

    /**
     * Write something to a file.
     *
     * @param filepath the file which is written
     * @param content  the content to write
     * @param append   flag if the content should be appended to already existing file content
     * @throws IOException in case any I/O error occurs
     */
    public static void writeStringToFile(
            final String filepath,
            final String content,
            final boolean append) throws IOException {
        writeFile(filepath, content, "UTF-8", append);
    }

    /**
     * Write something to a file.
     *
     * @param filepath the file which is written
     * @param content  the content to write
     * @param encoding the encoding to use during writing
     * @throws IOException in case any I/O error occurs
     */
    public static void writeStringToFile(
            final String filepath,
            final String content,
            final String encoding) throws IOException {
        writeFile(filepath, content, encoding, true);
    }

    /**
     * Write something to a file.
     *
     * @param filepath the file which is written
     * @param content  the content to write
     * @param encoding the encoding to use during writing
     * @param append   flag if the content should be appended to already existing file content
     * @throws IOException in case any I/O error occurs
     */
    public static void writeStringToFile(
            final String filepath,
            final String content,
            final String encoding,
            final boolean append) throws IOException {
        writeFile(filepath, content, encoding, append);
    }

    /**
     * Write something to a file.
     *
     * @param filepath the file which is written
     * @param content  the content to write
     * @param encoding the encoding to use during writing
     * @throws IOException in case any I/O error occurs
     */
    public static void writeStringToFile(
            final String filepath,
            final String content,
            final Charset encoding) throws IOException {
        writeFile(filepath, content, encoding.displayName(), true);
    }

    /**
     * Write something to a file.
     *
     * @param filepath the file which is written
     * @param content  the content to write
     * @param append   flag if the content should be appended to already existing file content
     * @param encoding the encoding to use during writing
     * @throws IOException in case any I/O error occurs
     */
    public static void writeStringToFile(
            final String filepath,
            final String content,
            final Charset encoding,
            final boolean append) throws IOException {
        writeFile(filepath, content, encoding.displayName(), append);
    }

    /**
     * Internal function to write a given string to a file.
     * <p>
     * Will collect additional metadata which can be used for policy enforcement.
     *
     * @param filepath Path of the file to read
     * @param content  The file content as string
     * @param encoding Used encoding
     * @param append   Indicator if the written content will be appended if the file already exists
     * @throws IOException In case something fails during writing the file
     */
    private static void writeFile(
            final String filepath,
            final String content,
            final String encoding,
            final boolean append) throws IOException {
        // open the file
        File file = new File(filepath);
        // we need to collect meta data which will be used by policies
        long writtenSize = content.getBytes().length;
        PermissionScope.getInstance().addAdditionalPermissionData(WRITTEN_BYTES, writtenSize);
        PermissionScope.getInstance().addAdditionalPermissionData(FILE_PATH,
                file.getCanonicalPath());

        FileUtils.writeStringToFile(file, content, encoding, append);
    }

    /**
     * Internal function to write a given string to a file.
     * <p>
     * Will collect additional metadata which can be used for policy enforcement.
     *
     * @param filepath Path of the file to read
     * @param content  The file content as string
     * @param append   Indicator if the written content will be appended if the file already exists
     * @throws IOException In case something fails during writing the file
     */
    private static void writeFile(
            final String filepath,
            final byte[] content,
            final boolean append) throws IOException {
        // open the file
        File file = new File(filepath);
        // we need to collect meta data which will be used by policies
        long writtenSize = content.length;
        PermissionScope.getInstance().addAdditionalPermissionData(WRITTEN_BYTES, writtenSize);
        PermissionScope.getInstance().addAdditionalPermissionData(FILE_PATH,
                file.getCanonicalPath());

        FileUtils.writeByteArrayToFile(file, content, append);
    }

    /**
     * Executes a command with attached parameters and return the output.
     *
     * @param command the command to execute
     * @param workingDir the working directory of the command
     * @param timeout the timeout for the executed command
     * @return the return console output and errors from the execution
     * @throws IOException          In case the execution cannot be started because of IO-errors
     * @throws InterruptedException If the current thread is interrupted by another thread while
     *                              it is waiting, then the wait is ended and an
     *                              InterruptedException is thrown.
     */
    public static Pair<Integer, String> executeFile(
            final String command,
            final String workingDir,
            final int timeout) throws IOException, InterruptedException {
        int exitCode = -1;

        // execute the cmd
        Process exec = new ProcessBuilder(command.split("\\s"))
                .directory(new File(workingDir))
                .redirectErrorStream(true)
                .start();
        exec.waitFor();
        if (exec.waitFor(timeout, TimeUnit.MINUTES)) {
            exitCode = exec.exitValue();
        }

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

        return new Pair<>(exitCode, reader.lines()
                .collect(Collectors.joining(System.lineSeparator())));
    }

    /*
     * File execution
     */

    /**
     * Recursively create directories to build a given path.
     *
     * @param path the path to build
     */
    public static void mkdirs(final Path path) {
        path.toFile().mkdirs();
    }

    /**
     * Executes a command with attached parameters and return the output.
     *
     * @param command the command to execute
     * @return the return console output and errors from the execution
     * @throws IOException          In case the execution cannot be started because of IO-errors
     * @throws InterruptedException If the current thread is interrupted by another thread while
     *                              it is waiting, then the wait is ended and an
     *                              InterruptedException is thrown.
     */
    public static String executeFile(
            final String command) throws IOException, InterruptedException {
        // execute the cmd
        Process exec = new ProcessBuilder(command.split("\\s"))
                .directory(new File(System.getProperty("user.dir")))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start();
        exec.waitFor();

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Delete a file.
     *
     * @param filepath the file to delete
     * @return true if the deletion was successful
     */
    public static boolean deleteFile(final String filepath) {
        File f = new File(filepath);

        if (!f.exists()) {
            LOGGER.error("Tried to delete non existing file '" + filepath + "'.");
            return false;
        }
        if (f.isDirectory()) {
            LOGGER.error("Tried to delete directory with file deletion method.");
            return false;
        }

        return f.delete();
    }

    /*
     * File deletion
     */

    /**
     * Deletes a directory recursively.
     *
     * @param dirPath directory to delete.
     * @return true if the deletion was successful, false otherwise
     * @throws IOException If an error during deletion occurs
     */
    public static boolean deleteDirectory(final String dirPath) throws IOException {
        File f = new File(dirPath);

        if (!f.exists()) {
            LOGGER.error("Tried to delete non existing directory '" + dirPath + "'.");
            return false;
        }
        if (f.isFile()) {
            LOGGER.error("Tried to delete file with directory deletion method.");
            return false;
        }

        FileUtils.deleteDirectory(f);
        return true;
    }

    /**
     * Determine if a file with given path exists.
     * The method does not distinguish files from directories.
     *
     * @param filePath the file/directory
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(final String filePath) {
        return new File(filePath).exists();
    }

    /*
     * Helper functions.
     */

    /**
     * Retrieve size in bytes of a file/directory.
     * Throws an exception in case file does not exist.
     *
     * @param f the file/directory
     * @return the length of the file, or recursive size of the directory, provided (in bytes).
     */
    private static long getFileSize(final File f) {
        return FileUtils.sizeOf(f);
    }

}
