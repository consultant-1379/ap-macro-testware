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
package com.ericsson.oss.ap.core.operators;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.test.model.APFileInfo;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.services.commonCLI.operator.ApCmEditorRestOperator;
import com.ericsson.oss.services.commonCLI.operator.CommonCLIFileWrapper;

/**
 * Wrapper class for AutoProvisioning TAF tests. It implements the methods used
 * by the APServiceOperator and converts CommandResponseDto provided by
 * CommonCLIServiceOperator into APServiceOperatorResult
 */
@Operator(context = Context.CLI)
@Singleton
public class ApCliOperator implements APServiceOperator {

    @Inject
    private APHostResolver hostResolver;

    private final static String AP_COMMAND = "ap ";
    private final static String DELETE_COMMAND_PROJECT = "ap delete -p ";
    private final static String DELETE_COMMAND_NODE = "ap delete -n ";
    private final static String IMPORT_COMMAND = "ap import";
    private final static String ORDER_NODE_COMMAND = "ap order -n ";
    private final static String ORDER_PROJECT_COMMAND = "ap order -p ";
    private final static String UNORDER_COMMAND = "ap unorder -n ";
    private final static String VALIDATE_COMMAND = "ap validate";
    private final static String DOWNLOAD_COMMAND = "ap download ";
    private final static String VIEW_COMMAND = "ap view";
    private final static String VIEW_COMMAND_PROJECT = "ap view -p ";
    private final static String VIEW_COMMAND_NODE = "ap view -n ";
    private final static String VIEW_NODE_STATUS_COMMAND = "ap status -n ";
    private final static String VIEW_PROJECT_STATUS_COMMAND = "ap status -p ";
    private final static String VIEW_ALL_PROJECTS_STATUS_COMMAND = "ap status ";

    final ApCmEditorRestOperator apCmEditorRestOperator = new ApCmEditorRestOperator();

    private final static String NODE_FLAG = " -n ";

    private final static String FILE_FLAG = " file:";
    private final static ThreadLocal<Long> EXECUTION_TIME = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    private CommandResult commandResult;

    private String command;
    private Host host;

    @Override
    public CommandResult importArchive(final APFileInfo apZipEntry) {
        host = hostResolver.getApacheHost();
        command = IMPORT_COMMAND;

        CommonCLIFileWrapper fileWrapper = null;
        if (apZipEntry != null) {
            fileWrapper = new CommonCLIFileWrapper(apZipEntry.getName(), apZipEntry.getContents());
            fileWrapper.setMimeType(apZipEntry.getMimeType());
            command += FILE_FLAG + apZipEntry.getName();
        }
        try {
            commandResult = apCmEditorRestOperator.executeUploadCommand(host, command, fileWrapper);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return commandResult;
    }

    @Override
    public CommandResult validateProjectFile(final APFileInfo apZipEntry) {
        host = hostResolver.getApacheHost();
        command = VALIDATE_COMMAND;

        CommonCLIFileWrapper fileWrapper = null;
        if (apZipEntry != null) {
            fileWrapper = new CommonCLIFileWrapper(apZipEntry.getName(), apZipEntry.getContents());
            fileWrapper.setMimeType(apZipEntry.getMimeType());
            command += FILE_FLAG + apZipEntry.getName();
        }
        try {
            commandResult = apCmEditorRestOperator.executeUploadCommand(host, command, fileWrapper);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return commandResult;
    }

    @Override
    public CommandResult viewProjects() {
        host = hostResolver.getApacheHost();
        command = VIEW_COMMAND;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult viewProject(final String projectName) {
        host = hostResolver.getApacheHost();
        command = VIEW_COMMAND_PROJECT + projectName;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public Long getLastExecutionTime() {
        return EXECUTION_TIME.get();
    }

    @Override
    public CommandResult deleteProject(final String projectName) {
        host = hostResolver.getApacheHost();
        command = DELETE_COMMAND_PROJECT + projectName;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult deleteNode(final String nodeName) {
        host = hostResolver.getApacheHost();
        command = DELETE_COMMAND_NODE + nodeName;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult orderNode(final String nodeName) {
        host = hostResolver.getApacheHost();
        command = ORDER_NODE_COMMAND + nodeName;
        commandResult = apCmEditorRestOperator.executeCommand(host, command);

        return commandResult;
    }

    @Override
    public CommandResult orderProject(final String projectName) {
        host = hostResolver.getApacheHost();
        command = ORDER_PROJECT_COMMAND + projectName;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult downloadNodeArtifact(final String nodeName, final String downloadArtifactOption) {
        host = hostResolver.getApacheHost();
        command = DOWNLOAD_COMMAND + downloadArtifactOption + NODE_FLAG + nodeName;
        commandResult = apCmEditorRestOperator.executeDownloadCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult unorder(final String nodeName) {
        host = hostResolver.getApacheHost();
        command = UNORDER_COMMAND + nodeName;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult viewNodeDetails(final String nodeName) {
        host = hostResolver.getApacheHost();
        command = VIEW_COMMAND_NODE + nodeName;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult downloadSchemasAndSamples(final String cliCommand) {
        host = hostResolver.getApacheHost();

        return apCmEditorRestOperator.executeDownloadCommand(host, cliCommand);
    }

    @Override
    public CommandResult viewNodeStatus(final String nodeName) {
        host = hostResolver.getApacheHost();
        command = VIEW_NODE_STATUS_COMMAND + nodeName;

        commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult viewProjectStatus(final String projectName) {
        final String command = VIEW_PROJECT_STATUS_COMMAND + projectName;

        final Host host = hostResolver.getApacheHost();

        final CommandResult commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult viewSummaryOfProjectStates() {
        final String command = VIEW_ALL_PROJECTS_STATUS_COMMAND;

        final Host host = hostResolver.getApacheHost();

        final CommandResult commandResult = apCmEditorRestOperator.executeCommand(host, command);
        return commandResult;
    }

    @Override
    public CommandResult upload(final String nodeName, final String artifactType, final String fileName, final byte[] artifactContent) {
        final Host host = hostResolver.getApacheHost();

        final String uploadCommand = "ap upload -n " + nodeName + " -a " + artifactType + " file:" + fileName;

        final CommonCLIFileWrapper fileWrapper = new CommonCLIFileWrapper(fileName, artifactContent);
        commandResult = apCmEditorRestOperator.executeUploadCommand(host, uploadCommand, fileWrapper);

        return commandResult;
    }

    @Override
    public CommandResult executeCommand(final String invalidCommand) {
        host = hostResolver.getApacheHost();

        command = AP_COMMAND;

        if (invalidCommand != null) {
            command += invalidCommand;
        }
        commandResult = apCmEditorRestOperator.executeCommand(host, command);

        return commandResult;
    }

}