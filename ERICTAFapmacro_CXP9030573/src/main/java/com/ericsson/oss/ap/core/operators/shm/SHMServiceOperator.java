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

/**
 * Operator providing support for SHM operations.
 *
 * since 1.2.24
 */
public interface SHMServiceOperator {

    /**
     * Uploads the upgrade package to SHM. Does nothing in case the upgrade package already exists.
     *
     * @param upgradePackageName
     *            the name of the upgrade package
     * @param upgradePackageFile
     *            the upgrade package file contents
     * @throws SHMServiceOperatorException
     *             if there is an error uploading the upgrade package
     */
    void uploadUpgradePackage(String upgradePackageName, final byte[] upgradePackageFile) throws SHMServiceOperatorException;

    /**
     * Uploads the license zip file using SHM. Does nothing in case the license file already exists.
     *
     * @param licenseFileName
     *            the name of the license zip file
     * @param licenseFile
     *            the contents of the license zip file
     * @throws SHMServiceOperatorException
     *             if there is an error uploading the license zip file
     */
    void uploadLicense(String licenseFileName, final byte[] licenseFile) throws SHMServiceOperatorException;

}
