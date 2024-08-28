/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.core.operators;

import com.ericsson.oss.ap.core.test.model.APFileInfo;
import com.ericsson.oss.ap.core.test.model.CommandResult;

/**
 * This interface describes the TAF operator used when interacting with AutoProvisioning (ap).
 *
 * The AP SUT can be accessed using multiple contexts.
 * <ul>
 * <li>Context.REST - interacts with the AP Rest service</li>
 * <li>Context.CLI - interacts with AP via the common CLI REST service</li>
 * <li>Context.API - interacts with AP via its remote CLI command handler ejb</li>
 * </ul>
 */
public interface APServiceOperator {

    /**
     * Import a ZIP archive represented by a byte array into the AP SUT.
     *
     *
     * @param apZipEntry
     * @return an {@code CommandResult} instance that encapsulates the result of the operation.
     */
    CommandResult importArchive(APFileInfo apZipEntry);

    /**
     * Validate a ZIP archive.
     *
     * @param apZipEntry
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     */
    CommandResult validateProjectFile(APFileInfo apZipEntry);

    /**
     * return a list view of all projects.
     *
     * @return an {@code CommandResult} instance that encapsulates the result of the operation and which will include a list view of projects
     */
    CommandResult viewProjects();

    /**
     * Deletes a project.
     *
     * @param projectName
     *            the name of the project to be deleted
     *
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     *
     */
    CommandResult deleteProject(final String projectName);

    /**
     * Deletes a node.
     *
     * @param nodeName
     *            the name of the node to be deleted
     *
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     *
     */
    CommandResult deleteNode(final String nodeName);

    /**
     * returns the node attributes providing details of a node.
     *
     * @param nodeName
     *            the name of the node to be viewed
     *
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     *
     */
    CommandResult viewNodeDetails(String theNodeName);

    /**
     * returns the project attributes and some node attributes providing details of a project and its nodes.
     *
     * @param projectName
     *            the name of the project to be viewed
     *
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     *
     */
    CommandResult viewProject(String theProjectName);

    /**
     * starts the order integrate workflow for a single node
     *
     * @param nodeName
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     */
    CommandResult orderNode(final String nodeName);

    /**
     * starts the order integrate workflow for all nodes in a project
     *
     * @param projectName the name of the project
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     */
    CommandResult orderProject(final String projectName);

    /**
     * starts the unorder integration workflow to remove the integration data generated during order integration for a node.
     *
     * @param nodeName
     * @return an {@code CommandResult} instance that encapsulates the result of the operation
     */
    CommandResult unorder(String nodeName);

    /**
     * Returns the time in milliseconds for the last executed operation. This call is thread-safe (the execution time is maintained independently per thread).
     *
     * @return the execution time of the last request in milliseconds.
     */
    Long getLastExecutionTime();

    /**
     * View node status
     *
     * @param theNodeName
     *            the node name
     * @return an {@code CommandResult} instance that encapsulates the status of the node
     */
    CommandResult viewNodeStatus(String theNodeName);


    /**
     * View projectstatus for a single node
     *
     * @param projectName
     *            the name of the project
     * @return an {@code CommandResult} instance that encapsulates the status of the project
     */
    CommandResult viewProjectStatus(final String projectName);

    /**
     * Uploads an artifact for a given node
     *
     * @param nodeName
     * @param artifactType
     * @param fileName
     * @param artifactContent
     * @return {@code CommandResult} instance that encapsulates the result of the operation
     */
    CommandResult upload(String nodeName, String artifactType, String fileName, byte[] artifactContent);

    /**
     * Returns response when command is entered.
     *
     * @param command
     *            the command being tested
     * @return the command response
     */
    CommandResult executeCommand(String invalidCommand);

    /**
     * @param nodeName
     * @param downloadArtifactOption
     * @return
     */
    CommandResult downloadNodeArtifact(String nodeName, String downloadArtifactOption);

    /**
     * Executes a download samples command and returns a response.
     *
     * @param cliCommand
     *            the download command
     * @return the command response
     */
    CommandResult downloadSchemasAndSamples(String cliCommand);

    /**
     * View project statuses for all projects
     *
     * @return an {@code CommandResult} instance that encapsulates the status of all projects
     */
    CommandResult viewSummaryOfProjectStates();

}
