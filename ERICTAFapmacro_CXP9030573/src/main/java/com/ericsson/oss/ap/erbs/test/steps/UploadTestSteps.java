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
package com.ericsson.oss.ap.erbs.test.steps;

import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertEquals;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.operators.file.CommonFileOperator;
import com.ericsson.oss.ap.core.test.helper.NodeStatusHelper;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Upload test steps
 * 
 * @author exuuguu
 * @since 1.6.4
 */
public class UploadTestSteps {

    public static final String TEST_STEP_UPLOAD_ARTIFACT = "uploadArtifact";
    public static final String TEST_STEP_VERIFY_UPLOAD = "verifyUpload";
    public static final String TEST_STEP_EDIT_NODE_STATUS = "editNodeStatus";
    public static final String TEST_STEP_DELETE_ARTIFACT = "deleteArtifact";

    public static final String TEST_CONTEXT_KEY_UPLOAD_RESPONSE = "uploadResponse";

    private static final String TEMPLATES_DIRECTORY = "/templates/node/artifacts/";

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private CommonFileOperator fileOperator;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    @Inject
    private TestContext context;

    @TestStep(id = TEST_STEP_UPLOAD_ARTIFACT)
    public void uploadArtifact(@Input("artifactType") final String artifactType, @Input("filename") final String filename, @Input("artifactTemplateName") final String artifactTemplateName)
            throws IOException {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        setTestStepBegin("Upload Artifact " + artifactType);
        final String nodeName = projectInfo.getNodes().get(0).getName();
        final byte[] artifactContent = getArtifactContent(artifactTemplateName);

        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.upload(nodeName, artifactType, filename, artifactContent);

        context.setAttribute(TEST_CONTEXT_KEY_UPLOAD_RESPONSE, result);

        setTestStepEnd();
    }

    @TestStep(id = TEST_STEP_VERIFY_UPLOAD)
    public void verifyResult(@Input("result") final String expectedResult, @Input("resultMessage") final String resultMessage, @Input("validationMessage") final String validationMessage,
            @Input("artifactType") final String artifactType, @Input("filename") final String filename) {

        final CommandResult result = context.getAttribute(TEST_CONTEXT_KEY_UPLOAD_RESPONSE);
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        setTestStepBegin("Verify uploaded artifact");

        final String nodeFdn = projectInfo.getNodes().get(0).getApNodeFdn();

        assertEquals(expectedResult, result.getResult().name());

        if (result.isSuccessful()) {

            verifyArtifactUploaded(result, resultMessage, artifactType, nodeFdn, filename);
        } else {

            verifyUploadFailed(resultMessage, validationMessage, result);
        }

        setTestStepEnd();
    }

    @TestStep(id = TEST_STEP_EDIT_NODE_STATUS)
    public void editStatusOfFirstNodeInProject(@Input("state") final String state) {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final String nodeFdn = projectInfo.getNodes().get(0).getApNodeFdn();

        nodeStatusHelper.setState(nodeFdn, state);
    }

    @TestStep(id = TEST_STEP_DELETE_ARTIFACT)
    public void deleteArtifactFile(@Input("artifactToDelete") final String missedArtifacts) {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final String nodeFdn = projectInfo.getNodes().get(0).getApNodeFdn();

        final String[] artifactArray = missedArtifacts.split(",");

        for (final String artifactToBeDeleted : artifactArray) {
            final ManagedObjectDto nodeArtifactMo = getNodeArtifactMoByType(artifactToBeDeleted, nodeFdn);
            final String rawLocation = nodeArtifactMo.getAttribute("rawLocation");
            assertTrue("The artifact file cannot be deleted for preparation of the test.", fileOperator.deleteFile(rawLocation));
        }
    }

    private byte[] getArtifactContent(final String templateName) throws IOException {

        final InputStream in = getClass().getResourceAsStream(TEMPLATES_DIRECTORY + templateName);
        final byte[] artifactContent = IOUtils.toByteArray(in);

        return artifactContent;
    }

    private void verifyArtifactUploaded(final CommandResult result, final String resultMessage, final String artifactType, final String nodeFdn, final String fileName) {

        assertTrue(result.getStatusMessage().contains(resultMessage));

        final ManagedObjectDto nodeArtifactMo = getNodeArtifactMoByType(artifactType, nodeFdn);
        assertTrue("The artifact MO does not exist.", nodeArtifactMo != null);

        final String rawLocation = nodeArtifactMo.getAttribute("rawLocation");
        assertTrue("The rawLocation of the artifact MO is empty.", StringUtils.isNotBlank(rawLocation));

        final File file = new File(rawLocation);
        assertTrue("The uploaded artifact file does not exist.", fileOperator.fileExists(rawLocation));
        assertTrue("The file name of the artifact in system is not same with the one uploaded.", file.getName().equals(fileName));
    }

    private void verifyUploadFailed(final String errorMessage, final String validationMessage, final CommandResult result) {

        if (StringUtils.isNotBlank(errorMessage)) {
            assertTrue(result.getStatusMessage().contains(errorMessage));
        }

        if (StringUtils.isNotBlank(validationMessage)) {
            assertTrue(result.getValidationMessage().contains(validationMessage));
        }
    }

    private ManagedObjectDto getNodeArtifactMoByType(final String artifactType, final String nodeFdn) {
        final  List<ManagedObjectDto> nodeArtifactMos = cmOperator.getChildren(hostResolver.getApacheHost(), "ap", nodeFdn, new String[] {"NodeArtifact"});
        for (final ManagedObjectDto nodeArtifactMo : nodeArtifactMos) {
            final String currentArtifactName = nodeArtifactMo.getAttribute("name");
            if (currentArtifactName.equalsIgnoreCase(artifactType)) {
                return nodeArtifactMo;
            }
        }
        return null;
    }

}
