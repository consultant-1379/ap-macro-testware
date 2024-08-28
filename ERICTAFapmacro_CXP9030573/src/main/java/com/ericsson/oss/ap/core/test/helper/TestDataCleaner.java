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
package com.ericsson.oss.ap.core.test.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.ap.core.getters.APDirectoryResolver;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.getters.SMRSResolver;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.operators.file.CommonFileOperator;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Handles cleaning of data such as mos and files which are persisted during
 * test case execution.
 * 
 * @author eeibky
 * @since 1.6.4
 */
@Singleton
public class TestDataCleaner {

    private final static Logger LOGGER = Logger.getLogger(TestDataCleaner.class);

    private final static Collection<String> fdnsForCleanup = new HashSet<String>();
    
    private final static Collection<String> fdnsForSupervisionCleanup = new HashSet<String>();

    @Inject
    protected CmCliOperator cmOperator;

    @Inject
    protected APHostResolver hostResolver;

    @Inject
    protected CommonFileOperator fileOperator;

    @Inject
    private SMRSResolver smrsResolver;

    @Inject
    private APDirectoryResolver directoryResolver;

    /**
     * Mark the fdn for cleanup. When cleanup is performed all mos and files
     * created during execution will be deleted.
     * 
     * @param fdn
     */
    public void markFdnForCleanUp(final String fdn) {
        if (fdn != null) {
            fdnsForCleanup.add(fdn);
        }
    }

    /**
     * Mark the NetworkElement and MeContext MOs persisted during execution of
     * order node for cleanup
     * 
     * @param orderedNodes
     *            the ordered nodes
     */
    public void markOrderedNodesForCleanup(final List<APNodeTestInfo> orderedNodes) {

        for (final APNodeTestInfo nodeInfo : orderedNodes) {

            final String networkElementFdn = "NetworkElement=" + nodeInfo.getName();
            markFdnForCleanUp(networkElementFdn);

            final String meContextFdn = "MeContext=" + nodeInfo.getName();
            markFdnForCleanUp(meContextFdn);
        }
    }

    private void deleteMo(final Host host, final String fdn) {
        LOGGER.debug(String.format("Deleting %s", fdn));

        // Catch any cmedit Exception so that TAF does not fall over.
        try {
            if (cmOperator.findMoByFdn(host, fdn) != null) {
                cmOperator.deleteMo(host, fdn);
            }
        } catch (RuntimeException e) {
            LOGGER.debug("Error deleting " + fdn, e);
        }
    }
    
    /**
     * Mark the FDN to turn off supervision before deletion.
     */
    public void markFdnToDisableSupervision(final String fdn){
        if (fdn != null) {
            fdnsForSupervisionCleanup.add(fdn);
        }
    }
    
    private void disableSupervision(){
        final Map<String, Object> supervisionAttributes = new HashMap<>();
        supervisionAttributes.put("active", false);
        for (final String supervisionFdn : fdnsForSupervisionCleanup) {
            cmOperator.updateMo(hostResolver.getApacheHost(), supervisionFdn, supervisionAttributes);
        }
    }

    /**
     * Cleanup database/SFS of created MOs/files.
     */
    public void performCleanup() {
        disableSupervision();
        deleteNodeFiles();
        deleteMOs();
        fdnsForSupervisionCleanup.clear();
        fdnsForCleanup.clear();
    }

    /**
     * Delete AP files that were creating during TAF.
     */
    private void deleteNodeFiles() {
        LOGGER.debug("Deleting AP node files after test.");
        final Host host = hostResolver.getApacheHost();
        final String rawDir = directoryResolver.getRawDirectory();
        final String genDir = directoryResolver.getGeneratedDirectory();
        final String smrsDir = smrsResolver.getSmrsAiDir();

        for (final String fdn : fdnsForCleanup) {
            LOGGER.debug("Deleting AP files for: " + fdn);

            if (isApProjectFdn(fdn)) {
                final List<ManagedObjectDto> dtoList = cmOperator.getMosByType(host, "ap", "Node", fdn);
                for (final ManagedObjectDto nodeDto : dtoList) {

                    final String nodeName = nodeDto.getName();
                    // TAF may run on Windows but remote server might be linux,
                    // so using a '/' which will run on both windows/linux.
                    fileOperator.deleteDirectory(rawDir + "/" + nodeName);
                    fileOperator.deleteDirectory(genDir + "/" + nodeName);
                    fileOperator.deleteDirectory(smrsDir + "/" + nodeName);
                }
            }
        }
    }

    private boolean isApProjectFdn(final String fdn) {
        return (FDN.get(fdn).getType().equals("Project"));
    }

    private void deleteMOs() {
        LOGGER.debug("Tearing down database");
        final Host host = hostResolver.getApacheHost();
        for (final String fdn : fdnsForCleanup) {
            this.deleteMo(host, fdn);
        }
    }
}
