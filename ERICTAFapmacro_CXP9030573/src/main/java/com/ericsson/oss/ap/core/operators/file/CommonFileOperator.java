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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.utils.ssh.J2SshFileCopy;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.services.ap.common.Constants;

@Operator(context = Context.CLI)
@Singleton
public class CommonFileOperator implements FileOperator {

    @Inject
    private APHostResolver apHostResolver;

    @Override
    public boolean fileExists(final String file) {
        return Constants.ENV_LOCAL ? checkLocalFileExists(file) : checkRemoteFileExists(file);
    }

    private boolean checkLocalFileExists(final String file) {
        final File localFile = new File(file);
        return localFile.exists();

    }

    private boolean checkRemoteFileExists(final String file) {
        final CLI cli = new CLI(apHostResolver.getSC1Host());
        final Shell shell = cli.executeCommand("/bin/ls " + file);
        return shell.getExitValue() == 0;
    }
    
    @Override
    public boolean deleteDirectory(final String directory) {
        return Constants.ENV_LOCAL ? deleteLocalDirectory(directory) : deleteRemoteDirectory(directory);
    }

    @Override
    public boolean deleteFile(final String file) {
        return Constants.ENV_LOCAL ? deleteLocalFile(file) : deleteRemoteFile(file);
    }

    private boolean deleteRemoteFile(final String file) {
        final CLI cli = new CLI(apHostResolver.getSC1Host());
        final Shell shell = cli.executeCommand("/bin/rm -f " + file);
        return shell.getExitValue() == 0;
    }

    private boolean deleteLocalFile(final String file) {
        return FileUtils.deleteQuietly((new File(file)));
    }

    private boolean deleteRemoteDirectory(final String directory) {
        final CLI cli = new CLI(apHostResolver.getSC1Host());
        // delete everything inside the specified directory.
        final Shell shell = cli.executeCommand("/bin/rm -r " + directory);
        return shell.getExitValue() == 0;
    }
    
    private boolean deleteLocalDirectory(final String directory) {
        try {
            FileUtils.deleteDirectory(new File(directory));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public boolean getRemoteFile(final String remoteFile, final String localFile) {
        final Host host = apHostResolver.getSC1Host();
        return J2SshFileCopy.getFile(remoteFile, localFile, host.getIp(), host.getUser(), host.getPass(), host.getPort().get(Ports.SSH));
    }

    @Override
    public String readFile(final String file) {
        String fileContents = "";

        final File localFile = Constants.ENV_LOCAL ? new File(file) : Paths.get(System.getProperty("java.io.tmpdir"), "remotefile.tmp").toFile();

        if (!Constants.ENV_LOCAL) {
            final boolean retrievedFile = getRemoteFile(file, localFile.getAbsolutePath());
            if (!retrievedFile) {
                return null;
            }
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(localFile);
            fileContents = scanner.useDelimiter("\\A").next();
        } catch (final FileNotFoundException e) {
            fileContents = null;
        } finally {
            scanner.close();
            if (!Constants.ENV_LOCAL) {
                localFile.delete();
            }
        }
        return fileContents;
    }
}