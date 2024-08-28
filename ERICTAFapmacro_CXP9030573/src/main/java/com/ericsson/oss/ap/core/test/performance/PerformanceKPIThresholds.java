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
package com.ericsson.oss.ap.core.test.performance;

/**
 * Class containing the Performance KPI threshold values
 * 
 * @since 1.2.1
 * 
 */
public class PerformanceKPIThresholds {

    public static final long IMPORT_SINGLE_PROJECT_THRESHOLD = 3000;
    public static final long VIEW_SINGLE_NODE_THRESHOLD = 1000;
    public static final long VIEW_SINGLE_PROJECT_THRESHOLD = 1000;
    public static final long VIEW_LIST_PROJECTS_THRESHOLD = 3000;
    public static final long DELETE_SINGLE_PROJECT_THRESHOLD = 1000;
    public static final long DELETE_SINGLE_NODE_THRESHOLD = 1000;
    public static final long INVALID_COMMANDS_THRESHOLD = 1000;

    public static final String ORDER = "order";
    private static final long ORDER_THRESHOLD_1_NODE = 20000; //20 seconds
    private static final long ORDER_THRESHOLD_10_NODE = 420000; //420 seconds
    private static final long ORDER_THRESHOLD_100_NODE = 3600000; //3600 seconds

    public static final String UNORDER = "unorder";
    private static final long UNORDER_THRESHOLD_1_NODE = 20000; //20 seconds
    private static final long UNORDER_THRESHOLD_10_NODE = 120000; //120 seconds
    private static final long UNORDER_THRESHOLD_100_NODE = 420000; //420 seconds

    public static final String FAILURE_MESSAGE = "Performance KPI test failure. Test run took longer than %s milliseconds to run. Actual time was %s milliseconds.";

    public static final int REQUIRED_PROJECT_LOAD = 75;

    private PerformanceKPIThresholds() {
        //private constructor, as there is no need to instantiate this class
    }

    /**
     * Performance threshold time increases linearly between the specified thresholds.
     * Above the maximum specified threshold, the threshold time will continue to linearly increase.
     * 
     * @param nodeCount
     *            number of nodes in the command
     * @param useCaseName
     *            useCase for which to calculated the performance threshold
     * @return threshold the performance threshold time in milliseconds
     */
    static public int getPerformanceThreshold(final int nodeCount, final String useCaseName) {
        final int threshold = 0;
        if (useCaseName.equalsIgnoreCase(ORDER)) {
            return getPerformanceThresholdForOrder(nodeCount);
        } else if (useCaseName.equalsIgnoreCase(UNORDER)) {
            return getPerformanceThresholdForUnorder(nodeCount);
        }
        return threshold;
    }

    static private int getPerformanceThresholdForOrder(final int nodeCount) {
        int threshold = 0;
        if (nodeCount >= 1 && nodeCount <= 10) {
            final int stepSize = (int) ((ORDER_THRESHOLD_10_NODE - ORDER_THRESHOLD_1_NODE) / 9);
            threshold = (int) (ORDER_THRESHOLD_1_NODE + (stepSize * (nodeCount - 1)));
        } else if (nodeCount > 10) {
            final int stepSize = (int) ((ORDER_THRESHOLD_100_NODE - ORDER_THRESHOLD_10_NODE) / 90);
            threshold = (int) (ORDER_THRESHOLD_10_NODE + (stepSize * (nodeCount - 10)));
        }
        return threshold;
    }

    static private int getPerformanceThresholdForUnorder(final int nodeCount) {
        int threshold = 0;
        if (nodeCount >= 1 && nodeCount <= 10) {
            final int stepSize = (int) ((UNORDER_THRESHOLD_10_NODE - UNORDER_THRESHOLD_1_NODE) / 9);
            threshold = (int) (UNORDER_THRESHOLD_1_NODE + (stepSize * (nodeCount - 1)));
        } else if (nodeCount > 10) {
            final int stepSize = (int) ((UNORDER_THRESHOLD_100_NODE - UNORDER_THRESHOLD_10_NODE) / 90);
            threshold = (int) (UNORDER_THRESHOLD_10_NODE + (stepSize * (nodeCount - 10)));
        }
        return threshold;
    }

}
