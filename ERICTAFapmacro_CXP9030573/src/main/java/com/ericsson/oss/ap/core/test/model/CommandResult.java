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
package com.ericsson.oss.ap.core.test.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.commonCLI.operator.CommonCLIFileWrapper;

/**
 * The {@code CommandResult} encapsulation the result returned from any command
 * line operation (e.g. ap, cmedit, config, etc.).
 * 
 * @since 1.2.42
 */
public class CommandResult {
    public enum Result {
        UNKNOWN(false), SUCCESS(true), FAILED(false);

        private final boolean successful;

        Result(final boolean isSuccess) {
            this.successful = isSuccess;
        }

        public boolean isSuccessful() {
            return successful;
        }
    }

    private Result result = Result.UNKNOWN;
    private String statusMessage;
    private String validationMessage;
    private String solution;
    private int errorCode;
    private final List<ResultEntity> resultEntities = new ArrayList<>();
    private CommonCLIFileWrapper commonCLIFileWrapper = null;

    public Result getResult() {
        return result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

    public boolean isSuccessful() {
        return result.isSuccessful();
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(final String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(final String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(final String solution) {
        this.solution = solution;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

    public List<ResultEntity> getResultEntities() {
        return Collections.unmodifiableList(resultEntities);
    }

    public void addResultEntity(final ResultEntity newResultEntity) {
        resultEntities.add(newResultEntity);
    }

    public void addResultEntities(final List<ResultEntity> newResultEntities) {
        resultEntities.addAll(newResultEntities);
    }
    
    public CommonCLIFileWrapper getCommonCLIFileWrapper() {
        return commonCLIFileWrapper;
    }

    public void setCommonCLIFileWrapper(final CommonCLIFileWrapper commonCLIFileWrapper) {
        this.commonCLIFileWrapper = commonCLIFileWrapper;
    }

    /**
     * Builds a response containing the error code, status and suggested
     * solution, to match the response given by the CLI.
     * 
     * @param errorCode
     *            the error code of the response
     * @param statusMessage
     *            the status message of the response
     * @param solution
     *            the suggested solution of the response
     * @return the fully formatted response with solution
     */
    public String buildResponseWithSolution() {
        return new StringBuilder("Error ").append(errorCode).append(" : ").append(statusMessage).append("\nSuggested Solution : ").append(solution).toString();
    }
}
