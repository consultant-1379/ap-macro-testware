/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.core.test.util.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for reading the contents of a zip archive.
 * 
 * @author epaudoy
 * @Since 3.21.0
 *
 */
public class ZipFileReader {

    public static Logger logger = LoggerFactory.getLogger(ZipFileReader.class);

    /**
     * Returns the list of files, including directory paths, found in a zip
     * archive.
     * 
     * @param archiveFileStream
     *            the zip file stream
     * @return List the list of files in the zip archive
     */
    public List<String> getListOfFilesInZipArchive(final byte[] archiveFileStream) {

        final List<String> listOfFiles = new ArrayList<>();

        final ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(archiveFileStream));

        ZipEntry zipEntry = null;
        try {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    listOfFiles.add(zipEntry.getName());
                }
            }
        } catch (IOException e) {
            logger.error("A problem occured while trying to get the list of files in a zip archive", e);
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                logger.error("A problem occured while trying to close the zip input stream", e);
            }
        }

        return listOfFiles;

    }

}
