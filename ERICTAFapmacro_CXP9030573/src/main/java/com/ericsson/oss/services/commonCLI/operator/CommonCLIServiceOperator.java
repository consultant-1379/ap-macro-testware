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
package com.ericsson.oss.services.commonCLI.operator;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.ap.core.test.model.CommandResult;

/**
 * This interface describes the TAF operator used when interacting with the
 * common CLI Rest Service
 */
public interface CommonCLIServiceOperator {

    /**
     * Executes a multipart/form-data command towards the Script Engine Rest
     * service
     * 
     * @param host
     *            the host instance to use
     * @param command
     *            the string command to be executed (e.g.. {@code ap view})
     * @return CommandResult the {@code CommandResult} encapsulates the result
     *         returned from command.
     */
    CommandResult executeCommand(Host host, final String command);

    /**
     * Executes a file upload command towards the Script Engine Rest service
     * 
     * @param host
     *            the host instance to use
     * @param command
     *            the string command to be executed (i.e.
     *            {@code ap import file:project.zip})
     * @param fileWrapper
     *            a {@code CommonCLIFileWrapper} instance containing the file to
     *            be uploaded
     * @return CommandResult the {@code CommandResult} encapsulates the result
     *         returned from command.
     */
    CommandResult executeUploadCommand(Host host, final String command, CommonCLIFileWrapper fileWrapper);

    /**
     * Executes a file download command towards the Script Engine Rest service
     * 
     * @param host
     *            the host instance to use
     * @param command
     *            the string command to be executed
     * @return CommandResult the {@code CommandResult} encapsulates the result
     *         returned from command, including the downloaded content.
     */
    CommandResult executeDownloadCommand(Host host, String command);

}
