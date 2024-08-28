/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.core.operators.file;



/**
 * File operator to perform general file operations on local and remote hosts.
 * <p>
 * The system property 'env' is used to determine if the local or remote file system is being used.
 */
public interface FileOperator {

    /**
     * Checks that the given file exists on the local or remote file system.
     * @param file the absolute path of the file
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(final String file);
    
    /**
     * Retrieves the given file from the remote host.
     * @param remoteFile the absolute path of the remote file
     * @param localFile the absolute path of local file
     * @return true if the file is successfully retrieved
     */
    boolean getRemoteFile(final String remoteFile, final String localFile);
    
    /**
     * Reads a local or remote file.
     * @param file the absolute path of the file
     * @return the file contents or null if the file is not found or cannot be retrieved from a remote host
     */
    String readFile(final String file);

    /**
     * Delete the contents of the specified directory.
     * @param directory
     * @return
     */
    boolean deleteDirectory(final String directory);

    /**
     * Delete a local or remote file.
     * 
     * @param file
     *            the absolute path of the file
     * @return
     */
    boolean deleteFile(final String file);
}