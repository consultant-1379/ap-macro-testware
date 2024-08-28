package com.ericsson.oss.ap.erbs.test.data;

import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Test data to generate zip files for import for AP TAF test cases.
 *
 * @since 1.0.10
 */
@SuppressWarnings("PMD.ExcessivePublicCount")
public class ImportCsvTestData { //NOPMD

    public static final CellProcessor[] CELL_PROCESSORS = { null, null, null, null, new ParseDouble(), null, null, null, new ParseDouble(), null, null, new ParseInt(), null,
    /* all optional after here */
    new Optional(), new Optional(new ParseDouble()), new Optional(), new Optional(), new Optional(), new Optional(new ParseInt()), new Optional(), new Optional(new ParseDouble()),
            new Optional(new ParseDouble()), new Optional(new ParseDouble()), new Optional(new ParseDouble()), new Optional(new ParseDouble()),
        new Optional(new ParseDouble()),
        new Optional(), // upgrade package
        new Optional(new ParseDouble()), new Optional(new ParseDouble()), new Optional(new ParseDouble()), new Optional(new ParseDouble()), new Optional(), new Optional(), new Optional(),
            new Optional(), new Optional(), new Optional(new ParseDouble()), new Optional(), new Optional(), new Optional(), new Optional(new ParseDouble()), new Optional(), new Optional(),
            new Optional() };

    private String dataProvider;
    private String testCase;
    private String testDescription;
    private String fileName;
    private double uniqueFileName;
    private String projectInfoTemplate;
    private String projectType;
    private String projectName;
    private double uniqueProject;
    private String creator;
    private String description;
    private int nodeCount;
    private String nodeInfoTemplate;
    private String nodeName;
    private double uniqueNode;
    private String nodeType;
    private String mimVersion;
    private String ipAddress;
    private String upgradePackageName;
    private int addressPoolSize;
    private String site;
    private double uniqueSite;
    private double nodeAiAttributesPresent;
    private double nodeUnlockCells;
    private double projectAiAttributesPresent;
    private double projectUnlockCells;
    private String siteBasic;
    private String siteEquipment;
    private String siteInstall;
    private String radio;
    private String transport;
    private String postIntegration;
    private double securityPresent;
    private String minimumSecurityLevel;
    private String optimumSecurityLevel;
    private String enrollmentMode;
    private double ipSecurityPresent;
    private String ipSecLevel;
    private String subjectAltNameType;
    private String subjectAltName;
    private final Map<String, String> artifactGen = new HashMap<>();
    private double projectUploadCVAfterIntegrationn;
    private double nodeUploadCVAfterConfiguration;
    private double nodeUploadCVAfterIntegrationn;
    private double projectUploadCVAfterConfiguration;
    private double installLicense;

    public String getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(final String dataProvider) {
        this.dataProvider = dataProvider;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(final String testCase) {
        this.testCase = testCase;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(final String testDescription) {
        this.testDescription = testDescription;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public double getUniqueFileName() {
        return uniqueFileName;
    }

    public void setUniqueFileName(final double uniqueFileName) {
        this.uniqueFileName = uniqueFileName;
    }

    public String getProjectInfoTemplate() {
        return projectInfoTemplate;
    }

    public void setProjectInfoTemplate(final String projectInfoTemplate) {
        this.projectInfoTemplate = projectInfoTemplate;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(final String projectType) {
        this.projectType = projectType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public double getUniqueProject() {
        return uniqueProject;
    }

    public void setUniqueProject(final double uniqueProject) {
        this.uniqueProject = uniqueProject;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(final String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(final int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public String getNodeInfoTemplate() {
        return nodeInfoTemplate;
    }

    public void setNodeInfoTemplate(final String nodeInfoTemplate) {
        this.nodeInfoTemplate = nodeInfoTemplate;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public double getUniqueNode() {
        return uniqueNode;
    }

    public void setUniqueNode(final double uniqueNode) {
        this.uniqueNode = uniqueNode;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(final String nodeType) {
        this.nodeType = nodeType;
    }

    public String getMimVersion() {
        return mimVersion;
    }

    public void setMimVersion(final String mimVersion) {
        this.mimVersion = mimVersion;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getAddressPoolSize() {
        return addressPoolSize;
    }

    public void setAddressPoolSize(final int addressPoolSize) {
        this.addressPoolSize = addressPoolSize;
    }

    public String getSite() {
        return site;
    }

    public String getUpgradePackageName() {
        return upgradePackageName;
    }

    public void setUpgradePackageName(final String upgradePackageName) {
        this.upgradePackageName = upgradePackageName;
    }

    public void setSite(final String site) {
        this.site = site;
    }

    public double getUniqueSite() {
        return uniqueSite;
    }

    public void setUniqueSite(final double uniqueSite) {
        this.uniqueSite = uniqueSite;
    }

    public double getNodeAiAttributesPresent() {
        return nodeAiAttributesPresent;
    }

    public void setNodeAiAttributesPresent(final double nodeAiAttributesPresent) {
        this.nodeAiAttributesPresent = nodeAiAttributesPresent;
    }

    public double getNodeUnlockCells() {
        return nodeUnlockCells;
    }

    public void setNodeUnlockCells(final double nodeUnlockCells) {
        this.nodeUnlockCells = nodeUnlockCells;
    }

    public double getProjectAiAttributesPresent() {
        return projectAiAttributesPresent;
    }

    public void setProjectAiAttributesPresent(final double projectAiAttributesPresent) {
        this.projectAiAttributesPresent = projectAiAttributesPresent;
    }

    public double getProjectUnlockCells() {
        return projectUnlockCells;
    }

    public void setProjectUnlockCells(final double projectUnlockCells) {
        this.projectUnlockCells = projectUnlockCells;
    }

    public String getSiteBasic() {
        return siteBasic;
    }

    public void setSiteBasic(final String siteBasic) {
        this.siteBasic = siteBasic;
        this.artifactGen.put("siteBasic", siteBasic);
    }

    public String getSiteEquipment() {
        return siteEquipment;
    }

    public void setSiteEquipment(final String siteEquipment) {
        this.siteEquipment = siteEquipment;
        this.artifactGen.put("siteEquipment", siteEquipment);
    }

    public String getSiteInstallation() {
        return siteInstall;
    }

    public void setSiteInstall(final String siteInstall) {
        this.siteInstall = siteInstall;
        this.artifactGen.put("siteInstall", siteInstall);
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(final String radio) {
        this.radio = radio;
        this.artifactGen.put("radio", radio);
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(final String transport) {
        this.transport = transport;
        this.artifactGen.put("transport", transport);
    }

    public String getPostInstallation() {
        return postIntegration;
    }

    public void setPostInstallation(final String postIntegration) {
        this.postIntegration = postIntegration;
        this.artifactGen.put("postIntegration", postIntegration);
    }

    public String getArtifactGen(final String artifact) {
        return artifactGen.get(artifact);
    }

    public double getSecurityPresent() {
        return securityPresent;
    }

    public void setSecurityPresent(final double securityPresent) {
        this.securityPresent = securityPresent;
    }

    public String getMinimumSecurityLevel() {
        return minimumSecurityLevel;
    }

    public void setMinimumSecurityLevel(final String minimumSecurityLevel) {
        this.minimumSecurityLevel = minimumSecurityLevel;
    }

    public String getOptimumSecurityLevel() {
        return optimumSecurityLevel;
    }

    public void setOptimumSecurityLevel(final String optimumSecurityLevel) {
        this.optimumSecurityLevel = optimumSecurityLevel;
    }

    public String getEnrollmentMode() {
        return enrollmentMode;
    }

    public void setEnrollmentMode(final String enrollmentMode) {
        this.enrollmentMode = enrollmentMode;
    }

    public double getIpSecurityPresent() {
        return ipSecurityPresent;
    }

    public void setIpSecurityPresent(final double ipSecurityPresent) {
        this.ipSecurityPresent = ipSecurityPresent;
    }

    public String getIpSecLevel() {
        return ipSecLevel;
    }

    public void setIpSecLevel(final String ipSecLevel) {
        this.ipSecLevel = ipSecLevel;
    }

    public String getSubjectAltNameType() {
        return subjectAltNameType;
    }

    public void setSubjectAltNameType(final String subjectAltNameType) {
        this.subjectAltNameType = subjectAltNameType;
    }

    public String getSubjectAltName() {
        return subjectAltName;
    }

    public void setSubjectAltName(final String subjectAltName) {
        this.subjectAltName = subjectAltName;
    }

    public void setNodeUploadCVAfterConfiguration(final double nodeUploadCVAfterConfiguration) {
        this.nodeUploadCVAfterConfiguration = nodeUploadCVAfterConfiguration;
    }

    public double getNodeUploadCVAfterConfiguration() {
        return nodeUploadCVAfterConfiguration;
    }

    public void setNodeUploadCVAfterIntegration(final double nodeUploadCVAfterIntegration) {
        this.nodeUploadCVAfterIntegrationn = nodeUploadCVAfterIntegration;
    }

    public double getNodeUploadCVAfterIntegration() {
        return nodeUploadCVAfterIntegrationn;
    }

    public void setProjectUploadCVAfterConfiguration(final double projectUploadCVAfterPlanActivation) {
        this.projectUploadCVAfterConfiguration = projectUploadCVAfterPlanActivation;
    }

    public double getProjectUploadCVAfterConfiguration() {
        return projectUploadCVAfterConfiguration;
    }

    public void setProjectUploadCVAfterIntegration(final double projectUploadCVAfterIntegration) {
        this.projectUploadCVAfterIntegrationn = projectUploadCVAfterIntegration;
    }

    public double getProjectUploadCVAfterIntegration() {
        return projectUploadCVAfterIntegrationn;
    }

    public double getInstallLicense() {
        return installLicense;
    }

    public void setInstallLicense(final double installLicense) {
        this.installLicense = installLicense;
    }
}