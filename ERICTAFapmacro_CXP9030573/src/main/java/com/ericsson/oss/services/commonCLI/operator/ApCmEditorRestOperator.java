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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.nms.security.ENMUser;
import com.ericsson.oss.ap.core.helpers.LoginHelper;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.services.ap.common.Constants;
import com.ericsson.oss.services.cm.rest.CmEditorRestOperator;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileDownloadRequestDto;

/**
 * Implementation of the TAF operator used when interacting with the
 * CmEditorRestOperator
 */
public class ApCmEditorRestOperator implements CommonCLIServiceOperator {

    public static final Logger logger = LoggerFactory.getLogger(ApCmEditorRestOperator.class);

    private final CmEditorRestOperator cmEditorRestOperator = new CmEditorRestOperator();

    private static final String COOKIE_HEADER = "Cookie";
    private static final String USER_ID_HEADER = "X-TOR-userid";
    private static final String SESSION_COOKIE = "iPlanetDirectoryPro=AQIC5wM2LY4SfcwNPrcXlzFTsoG5t9sdf_DBM-t2ubdkyZc.AAJTSQACMDE";

    private final Map<String, String> headersMap = new HashMap<String, String>();
    final ResponseToCommandResultConverter responseConverter = new ResponseToCommandResultConverter();

    /**
     * Request, which performs a request without a file
     * 
     * @param Host
     *            host, the host instance to use
     * @param String
     *            command Command to be executed
     * @return CommandResult containing the results from the request.
     */
    @Override
    public CommandResult executeCommand(final Host host, final String command) {

        setHeadersAndHttpTool(host);

        final HttpResponse getResponse = cmEditorRestOperator.getHttpResponse(command);

        final CommandResult commandResult = responseConverter.extractCommandResultFromMultipartFormDataResponse(getResponse, command);

        return commandResult;
    }

    /**
     * Request, which performs a request with file content
     * 
     * @param Host
     *            host, the host instance to use
     * @param String
     *            command Command to be executed
     * @param fileWrapper
     *            a {@code CommonCLIFileWrapper} instance containing the file to
     *            be uploaded
     * @return CommandResult containing the results from the request.
     */
    @Override
    public CommandResult executeUploadCommand(final Host host, final String command, final CommonCLIFileWrapper fileWrapper) {

        setHeadersAndHttpTool(host);

        final String fileName = fileWrapper.getFileName().trim();
        final File file = createFile(fileWrapper, fileName);

        final HttpResponse getResponse = cmEditorRestOperator.getHttpResponseWithFile(command, file);

        file.delete();

        final CommandResult commandResult = responseConverter.extractCommandResultFromMultipartFormDataResponse(getResponse, command);

        return commandResult;
    }

    /**
     * GET Request with file info, to download the file specified in the fileId.
     * 
     * @param Host
     *            host, the host instance to use
     * @param String
     *            command Command to be executed
     * @return CommandResult containing the contents of the file specified in
     *         the download request
     */
    @Override
    public CommandResult executeDownloadCommand(final Host host, final String command) {

        setHeadersAndHttpTool(host);

        final HttpResponse getResponse = cmEditorRestOperator.getHttpResponse(command);

        final CommandResult commandResult = responseConverter.extractCommandResultFromDownloadResponse(getResponse);

        if (commandResult.getResult().isSuccessful()) {

            final FileDownloadRequestDto fileDownloadRequestDto = responseConverter.extractFileAndApplicationId(getResponse);

            final HttpResponse downloadResponse = cmEditorRestOperator.getHttpResponseForFileDownload(fileDownloadRequestDto.getApplicationId(), fileDownloadRequestDto.getFileId());
            final CommonCLIFileWrapper downloadedFile = extractFileContents(downloadResponse);

            commandResult.setCommonCLIFileWrapper(downloadedFile);
        }

        return commandResult;
    }

    private File createFile(final CommonCLIFileWrapper fileWrapper, final String fileName) {
        final File file = new File(fileName);
        try (final BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(file))) {

            fileWriter.write(fileWrapper.getFileContent());

        } catch (IOException e) {
            logger.error("Failed to create file using fileName and fileWrapper", e);
            throw new IllegalStateException(e);
        }
        return file;
    }

    private CommonCLIFileWrapper extractFileContents(final HttpResponse downloadResponse) {
        final String contentDisposition = downloadResponse.getHeaders().get("Content-Disposition");
        final String fileName = contentDisposition.split("=")[1];

        final CommonCLIFileWrapper downloadedFile = new CommonCLIFileWrapper(fileName, getFileStreamContent(downloadResponse));
        downloadedFile.setMimeType(downloadResponse.getContentType());
        return downloadedFile;
    }

    private void setHeadersAndHttpTool(final Host host) {

        final ENMUser user = setUserAndCookie();
        final HttpTool httpTool = LoginHelper.performSecureLogin(user.getUsername(), user.getPassword(), host);
        cmEditorRestOperator.setAdditionalHttpHeaders(headersMap);
        cmEditorRestOperator.setTool(httpTool);
    }

    private byte[] getFileStreamContent(final HttpResponse downloadResponse) {

        byte[] fileStream = new byte[0];

        try {
            fileStream = IOUtils.toByteArray(downloadResponse.getContent());
        } catch (final IOException e) {
            logger.error("Problem occured while streaming a file in the HttpResponse", e);
            throw new IllegalStateException(e);
        }

        return fileStream;
    }

    private ENMUser setUserAndCookie() {

        final ENMUser user = LoginHelper.getCurrentUser();

        headersMap.put(USER_ID_HEADER, user.getUsername());

        if (Constants.ENV_LOCAL) {
            headersMap.put(COOKIE_HEADER, SESSION_COOKIE);
        }

        return user;
    }

}
