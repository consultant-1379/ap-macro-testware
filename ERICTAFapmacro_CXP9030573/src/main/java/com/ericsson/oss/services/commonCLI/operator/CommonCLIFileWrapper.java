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
package com.ericsson.oss.services.commonCLI.operator;

public class CommonCLIFileWrapper {

    private String fileName;
    private String mimeType;
    private byte[] fileContent;

    /**
     * This is a wrapper class for files to be sent to the server using the implementation of {@code CommonCLIServiceOperator}
     */
    public CommonCLIFileWrapper(final String fileName, final byte[] fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the fileContent
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * @param fileContent
     *            the fileContent to set
     */
    public void setFileContent(final byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String theMimeType) {
        mimeType = theMimeType;
    }
}
