/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.erbs.test.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;

import com.ericsson.oss.ap.erbs.test.cases.TemplateHandler;
import com.ericsson.oss.ap.erbs.test.model.APNodeArtifactTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

import freemarker.template.TemplateException;

public class APImportERBSZipTestData extends AbstractTestData {

    public static final String VALID_MACRO_ARCHIVE = "validAPMacroArchive";
    public static final String INVALID_MACRO_ARCHIVE_MISSING_PROJECT = "invalidAPMacroArchiveNoProjectInfo";
    public static final String VALID_AP_MACRO_ARCHIVE_DUPLICATE_PROJECT = "validAPMacroArchiveForDuplicateProject";
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String VALID_DATA_CSVFILE = "/data/apMacroArchiveSpec.csv";

    private static int nodeIpAddressUsedCounter = 1;

    private static final TemplateHandler freemarker = new TemplateHandler(APImportERBSZipTestData.class, "/templates/");

    private final static Logger LOGGER = Logger.getLogger(APImportERBSZipTestData.class);

    private final static AtomicLong UNIQUE_ID = new AtomicLong(System.currentTimeMillis());

    private APImportERBSZipTestData() {
    }

    @DataProvider(name = VALID_MACRO_ARCHIVE)
    public static Object[][] createAPValidArchive() {
        final Object[][] provider = createDataForProvider(VALID_MACRO_ARCHIVE);

        return provider;
    }

    @DataProvider(name = VALID_AP_MACRO_ARCHIVE_DUPLICATE_PROJECT)
    public static Object[][] createAPValidArchiveForDuplicate() {
        return createDataForProvider(VALID_AP_MACRO_ARCHIVE_DUPLICATE_PROJECT);
    }

    @DataProvider(name = INVALID_MACRO_ARCHIVE_MISSING_PROJECT)
    public static Object[][] createArchiveWithNoProjectInfo() throws IOException {
        final Object[][] result = new Object[1][3];

        try {
            final Object[][] provider = createDataForProvider(INVALID_MACRO_ARCHIVE_MISSING_PROJECT);
            final List<APZipEntry> zipEntrylist = new ArrayList<>();

            final APZipEntry entry = createTextFile();
            zipEntrylist.add(entry);

            result[0][0] = createZipByteArray(zipEntrylist);
            result[0][1] = provider[0][1];
            result[0][2] = provider[0][2];
        } catch (final Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    public static Object[][] createDataForProvider(final String theFilter) {
        final List<Object[]> result = new ArrayList<>();

        final List<ImportCsvTestData> testDataList = loadCsvFile(VALID_DATA_CSVFILE, APImportERBSZipTestData.class, ImportCsvTestData.class, ImportCsvTestData.CELL_PROCESSORS);
        for (final ImportCsvTestData csvTestData : testDataList) {
            try {
                if (csvTestData.getDataProvider().equals(theFilter)) {
                    final Object[] rowData = new Object[3];
                    final APProjectTestInfo projectTestInfo = generateProjectModel(csvTestData);
                    final List<APZipEntry> zipEntrylist = createArtifactList(projectTestInfo);
                    rowData[0] = createZipByteArray(zipEntrylist);
                    rowData[1] = projectTestInfo;
                    rowData[2] = projectTestInfo.getAttribute("description");
                    result.add(rowData);

                    if (Boolean.getBoolean("autoprovisioning.taf.save.archive")) {
                        final String saveDirectory = System.getProperty("autoprovisioning.taf.save.archive.directory", System.getProperty("java.io.tmpdir"));
                        final File file = new File(saveDirectory + projectTestInfo.getAttribute("fileName"));
                        try {
                            final FileOutputStream fileWriter = new FileOutputStream(file);
                            fileWriter.write((byte[]) rowData[0]);
                            fileWriter.flush();
                            fileWriter.close();
                        } catch (final IOException e) {
                            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }

            } catch (final Exception e) {
                LOGGER.error("An exception occurred while generating the data for " + theFilter, e);
            }
        }
        return result.toArray(new Object[result.size()][3]);
    }

    private static List<APZipEntry> createArtifactList(final APProjectTestInfo projectInfo) throws URISyntaxException {
        final List<APZipEntry> artifacts = new ArrayList<>();
        APZipEntry entry;

        final Object contents = projectInfo.getAttribute("contents");
        if (contents != null) {
            entry = new APZipEntry();
            entry.artifactName = "projectInfo.xml";
            entry.artifactContents = projectInfo.getAttributeAsString("contents");
            artifacts.add(entry);
        }

        int nodeID = 1;
        for (final APNodeTestInfo node : projectInfo.getNodes()) {
            entry = new APZipEntry();
            final String nodeDirectory = "node-" + nodeID++;
            entry.artifactName = String.format("%s/nodeInfo.xml", nodeDirectory);
            entry.artifactContents = node.getAttributeAsString("contents");
            artifacts.add(entry);

            for (final Map.Entry<String, APNodeArtifactTestInfo> nodeArtifact : node.getArtifacts().entrySet()) {
                entry = new APZipEntry();
                final APNodeArtifactTestInfo artifactInfo = nodeArtifact.getValue();
                entry.artifactName = String.format("%s/%s", nodeDirectory, artifactInfo.getName());
                entry.artifactContents = artifactInfo.getAttributeAsString("contents");
                artifacts.add(entry);
            }
        }

        Collections.sort(artifacts);
        return artifacts;
    }

    private static APProjectTestInfo generateProjectModel(final ImportCsvTestData theArchiveSpec) throws IOException, TemplateException {
        final APProjectTestInfo testInfo = new APProjectTestInfo();

        testInfo.setAttribute("testCase", theArchiveSpec.getTestCase());
        testInfo.setAttribute("testDescription", theArchiveSpec.getTestDescription());

        String projectFileName = theArchiveSpec.getFileName();
        if (isFeatureEnabled(theArchiveSpec.getUniqueFileName())) {
            projectFileName += generateUniqueSuffix() + ".zip";
        }
        testInfo.setAttribute("fileName", projectFileName);

        if (theArchiveSpec.getProjectName() == null) {
            return testInfo;
        }

        String projectName = theArchiveSpec.getProjectName().toString();

        if (isFeatureEnabled(theArchiveSpec.getUniqueProject())) {
            projectName = projectName += generateUniqueSuffix();
        }
        testInfo.setName(projectName);

        testInfo.setAttribute("creator", theArchiveSpec.getCreator());
        testInfo.setAttribute("description", theArchiveSpec.getDescription());
        testInfo.setAttribute("projectType", theArchiveSpec.getProjectType());
        testInfo.setAttribute("apProjectFdn", getApProjectFdn(projectName));

        testInfo.setAttribute("aiAttributesPresent", isFeatureEnabled(theArchiveSpec.getProjectAiAttributesPresent()));

        if (testInfo.getAiAttributesPresent()) {
            testInfo.setAttribute("unlockCells", isFeatureEnabled(theArchiveSpec.getProjectUnlockCells()));
            testInfo.setAttribute("uploadCVAfterConfiguration", isFeatureEnabled(theArchiveSpec.getProjectUploadCVAfterConfiguration()));
            testInfo.setAttribute("uploadCVAfterIntegration", isFeatureEnabled(theArchiveSpec.getProjectUploadCVAfterIntegration()));
            final String upgradePackageName = theArchiveSpec.getUpgradePackageName();
            testInfo.setAttribute("upgradePackageName", upgradePackageName);
            testInfo.setAttribute("installLicense", isFeatureEnabled(theArchiveSpec.getInstallLicense()));
        }

        if (theArchiveSpec.getProjectInfoTemplate() != null) {
            testInfo.setAttribute("contents", loadTemplate("project/" + theArchiveSpec.getProjectInfoTemplate(), testInfo).toString());
        }

        final int nodeCount = theArchiveSpec.getNodeCount();
        if (nodeCount > 0) {
            final int ipAddressPoolSize = theArchiveSpec.getAddressPoolSize();
            nodeIpAddressUsedCounter = (ipAddressPoolSize == -1) ? 0 : (nodeIpAddressUsedCounter > ipAddressPoolSize) ? 1 : nodeIpAddressUsedCounter == 0 ? 1 : nodeIpAddressUsedCounter;

            for (int i = 0; i < nodeCount; i++) {
                final APNodeTestInfo node = generateNodeModel(theArchiveSpec, nodeIpAddressUsedCounter, projectName);
                node.setAttribute("project", testInfo.getName());
                testInfo.addNode(node);

                if (nodeIpAddressUsedCounter != 0) {
                    if (nodeIpAddressUsedCounter > ipAddressPoolSize || nodeIpAddressUsedCounter > 255) {
                        nodeIpAddressUsedCounter = 1;
                    } else {
                        nodeIpAddressUsedCounter++;
                    }
                }
            }

        }

        return testInfo;

    }

    private static StringBuffer loadTemplate(final String template, final Object theTemplateData) throws IOException, TemplateException {
        final StringWriter writer = new StringWriter(2048);
        freemarker.processTemplate(template, theTemplateData, writer);
        return writer.getBuffer();
    }

    private static String generateUniqueSuffix() {
        return String.format("%s%d", Thread.currentThread().getName(), UNIQUE_ID.getAndIncrement());
    }

    private static APNodeTestInfo generateNodeModel(final ImportCsvTestData theArchiveSpec, final int theCurrIp, final String projectName) throws IOException, TemplateException {
        final APNodeTestInfo nodeTestInfo = new APNodeTestInfo();

        String nodeName = theArchiveSpec.getNodeName();
        String text;
        if (isFeatureEnabled(theArchiveSpec.getUniqueNode())) {
            nodeName = nodeName += generateUniqueSuffix();
        }
        nodeTestInfo.setName(nodeName);

        nodeTestInfo.setAttribute("nodeType", theArchiveSpec.getNodeType());
        nodeTestInfo.setAttribute("mimVersion", theArchiveSpec.getMimVersion());
        nodeTestInfo.setAttribute("apNodeFdn", getApNodeFdn(nodeName, projectName));

        text = theArchiveSpec.getIpAddress();
        if (theCurrIp > 0) {
            text += theCurrIp;
        }
        nodeTestInfo.setAttribute("ipAddress", text);

        text = theArchiveSpec.getSite();
        if (isFeatureEnabled(theArchiveSpec.getUniqueSite())) {
            text += generateUniqueSuffix();
        }
        nodeTestInfo.setAttribute("site", text);

        nodeTestInfo.setAttribute("aiAttributesPresent", isFeatureEnabled(theArchiveSpec.getNodeAiAttributesPresent()));

        if (nodeTestInfo.getAiAttributesPresent()) {
            nodeTestInfo.setAttribute("unlockCells", isFeatureEnabled(theArchiveSpec.getNodeUnlockCells()));
            nodeTestInfo.setAttribute("uploadCVAfterConfiguration", isFeatureEnabled(theArchiveSpec.getNodeUploadCVAfterConfiguration()));
            nodeTestInfo.setAttribute("uploadCVAfterIntegration", isFeatureEnabled(theArchiveSpec.getNodeUploadCVAfterIntegration()));
            nodeTestInfo.setAttribute("upgradePackageName", theArchiveSpec.getUpgradePackageName());
            nodeTestInfo.setAttribute("installLicense", isFeatureEnabled(theArchiveSpec.getInstallLicense()));
        }

        nodeTestInfo.setAttribute("securityPresent", isFeatureEnabled(theArchiveSpec.getSecurityPresent()));
        if (nodeTestInfo.getSecurityPresent()) {
            nodeTestInfo.setAttribute("minimumSecurityLevel", theArchiveSpec.getMinimumSecurityLevel());
            nodeTestInfo.setAttribute("optimumSecurityLevel", theArchiveSpec.getOptimumSecurityLevel());
            nodeTestInfo.setAttribute("enrollmentMode", theArchiveSpec.getEnrollmentMode());

            nodeTestInfo.setAttribute("ipSecurityPresent", isFeatureEnabled(theArchiveSpec.getIpSecurityPresent()));
            if (nodeTestInfo.getIpSecurityPresent()) {
                nodeTestInfo.setAttribute("ipSecLevel", theArchiveSpec.getIpSecLevel());
                nodeTestInfo.setAttribute("subjectAltNameType", theArchiveSpec.getSubjectAltNameType());
                nodeTestInfo.setAttribute("subjectAltName", theArchiveSpec.getSubjectAltName());
            }
        }

        addArtifactsInTest(theArchiveSpec, nodeTestInfo);

        nodeTestInfo.setAttribute("contents", loadTemplate("node/" + theArchiveSpec.getNodeInfoTemplate(), nodeTestInfo));

        if (!nodeTestInfo.getAiAttributesPresent()) {
            nodeTestInfo.setAttribute("unlockCells", isFeatureEnabled(theArchiveSpec.getProjectUnlockCells()));
            nodeTestInfo.setAttribute("uploadCVAfterConfiguration", isFeatureEnabled(theArchiveSpec.getProjectUploadCVAfterConfiguration()));
            nodeTestInfo.setAttribute("uploadCVAfterIntegration", isFeatureEnabled(theArchiveSpec.getProjectUploadCVAfterIntegration()));
            nodeTestInfo.setAttribute("upgradePackageName", theArchiveSpec.getUpgradePackageName());
            nodeTestInfo.setAttribute("installLicense", isFeatureEnabled(theArchiveSpec.getInstallLicense()));
        }

        return nodeTestInfo;
    }

    //
    private static void addArtifactsInTest(final ImportCsvTestData theArchiveSpec, final APNodeTestInfo theNode) throws IOException, TemplateException {
        for (final String artifact : AbstractTestData.SUPPORTED_ARTIFACTS) {
            final String artifactFileName = theArchiveSpec.getArtifactGen(artifact);
            if (artifactFileName != null && !"".equals(artifactFileName.trim())) {
                final APNodeArtifactTestInfo artifactInfo = new APNodeArtifactTestInfo();
                artifactInfo.setNodeName(theNode.getName());
                artifactInfo.setAttribute("type", artifact);
                artifactInfo.setName(artifact + ".xml");

                // The RDN and the Artifact type can be different.
                // In the tests, the only difference is that the RDN value starts with an upper case:
                // I.e. artifact radio has RDN value of Radio in DPS model.
                final String rdnValue = Character.toUpperCase(artifact.charAt(0)) + artifact.substring(1);
                artifactInfo.setAttribute("rdn", rdnValue);

                artifactInfo.setAttribute("contents", loadTemplate(String.format("node/artifacts/%s", artifactFileName), theNode));
                theNode.addArtifact(artifact, artifactInfo);

                theNode.setAttribute(artifact, artifact + ".xml");
            }
        }
    }

    private static String getApProjectFdn(final String projectName) {
        return new StringBuilder().append("Project=").append(projectName).toString();
    }

    private static String getApNodeFdn(final String nodeName, final String projectName) {
        return new StringBuilder().append("Project=").append(projectName).append(",").append("Node").append("=").append(nodeName).toString();
    }

    private static APZipEntry createTextFile() {
        final APZipEntry entry = new APZipEntry();
        entry.artifactName = "dummy.txt";
        entry.artifactContents = "This is a dummy text file";
        return entry;
    }

    private static byte[] createZipByteArray(final List<APZipEntry> apZipEntry) throws IOException {
        final ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOS);

        for (final APZipEntry entry : apZipEntry) {
            final byte[] bytes = entry.artifactContents.getBytes();
            final ZipEntry zipEntry = new ZipEntry(entry.artifactName);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(bytes, 0, bytes.length);
            zipOutputStream.flush();
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();

        final byte[] zippedData = byteArrayOS.toByteArray();
        return zippedData;
    }

    private static boolean isFeatureEnabled(final double probability) {
        final int factor = (int) (probability * 100);
        if (factor == 0) {
            return false;
        } else if (factor == 100) {
            return true;
        }

        return (RANDOM.nextInt(100) + factor >= 100);
    }

    static class APZipEntry implements Comparable<APZipEntry> {

        public String artifactName;
        public String artifactContents;

        @Override
        public int compareTo(final APZipEntry zipEntryObject) {
            return artifactName.compareTo(zipEntryObject.artifactName);
        }
    }
}
