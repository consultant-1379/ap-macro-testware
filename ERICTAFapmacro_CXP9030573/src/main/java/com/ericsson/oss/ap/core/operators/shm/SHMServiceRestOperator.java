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
package com.ericsson.oss.ap.core.operators.shm;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.services.commonCLI.operator.ApCmEditorRestOperator;
import com.ericsson.oss.services.commonCLI.operator.CommonCLIFileWrapper;

/**
 * SHM Service Rest operator.
 * 
 * @since 1.2.24
 * 
 */
@Operator(context = Context.REST)
@Singleton
public class SHMServiceRestOperator implements SHMServiceOperator {

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private ApCmEditorRestOperator apCmEditorRestOperator;

    @Override
    public void uploadUpgradePackage(final String upgradePackageName, final byte[] upgradePackageFile) throws SHMServiceOperatorException {
        final Host host = hostResolver.getApacheHost();

        final StringBuilder command = new StringBuilder("shm import --swp file:");

        command.append(upgradePackageName);

        try {
            final CommandResult result = apCmEditorRestOperator.executeUploadCommand(host, command.toString(), new CommonCLIFileWrapper(upgradePackageName, upgradePackageFile));

            if (!result.isSuccessful() && !result.getStatusMessage().contains("already exist")) { // no error code so need to check status message
                throw new SHMServiceOperatorException(result.getStatusMessage());
            }

        } catch (final Exception e) {
            throw new SHMServiceOperatorException("Error uploading package " + upgradePackageName, e);
        }
    }

    @Override
    public void uploadLicense(final String licenseFileName, final byte[] licenseFile) throws SHMServiceOperatorException {
        final Host host = hostResolver.getApacheHost();

        final StringBuilder command = new StringBuilder("shm import -l file:");

        command.append(licenseFileName);

        try {
            final CommandResult result = apCmEditorRestOperator.executeUploadCommand(host, command.toString(), new CommonCLIFileWrapper(licenseFileName, licenseFile));

            if (!result.isSuccessful() && !result.getStatusMessage().contains("already exist")) {
                throw new SHMServiceOperatorException(result.getStatusMessage());
            }

        } catch (final Exception e) {
            throw new SHMServiceOperatorException("Error uploading license " + licenseFileName, e);
        }

    }

}
