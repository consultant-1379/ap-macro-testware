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
package com.ericsson.oss.ap.erbs.test.steps.helper;

/**
 * Allows the correct Node States to be set in a data driven way
 * @since 1.14.7
 */
public class StateConsumer {

    public static class StateConsumerBuilder {

        private int notStarted;
        private int started;
        private int completed;
        private int failed;

        public static StateConsumerBuilder newBuilder() {
            return new StateConsumerBuilder();
        }

        public StateConsumerBuilder fromWrapper(final StateWrapper wrapper) {
            return this
                .withNotStartedNodes(wrapper.getNotStartedNodes())
                .withStartedNodes(wrapper.getStartedNodes())
                .withCompletedNodes(wrapper.getCompletedNodes())
                .withFailedNodes(wrapper.getFailedNodes());
        }

        public StateConsumerBuilder withCompletedNodes(final int completed) {
            this.completed = completed;
            return this;
        }

        public StateConsumerBuilder withFailedNodes(final int failed) {
            this.failed = failed;
            return this;
        }

        public StateConsumerBuilder withNotStartedNodes(final int notStarted) {
            this.notStarted = notStarted;
            return this;
        }

        public StateConsumerBuilder withStartedNodes(final int started) {
            this.started = started;
            return this;
        }

        public StateConsumer build() {
            return new StateConsumer(notStarted, started, completed, failed);
        }

    }

    private static final String[][] NODE_STATES = {
        {"READY_FOR_ORDER", "EDIT_STARTED", "INVALID_CONFIGURATION", "NOT_STARTED", "DELETE_STARTED"},
        {"ORDER_STARTED", "UNORDER_STARTED", "ORDER_COMPLETED", "INTEGRATION_STARTED"},
        {"INTEGRATION_COMPLETED"},
        {"ORDER_FAILED", "ORDER_ROLLBACK_FAILED", "UNORDER_FAILED", "INTEGRATION_FAILED", "DELETE_FAILED"}
    };

    private int currentState;
    private final int[][] counts;

    private StateConsumer(
        final int notStarted,
        final int started,
        final int completed,
        final int failed) {

        this.counts = new int[][] {{-1, notStarted}, {-1, started}, {-1, completed}, {-1, failed}};
        this.currentState = -1;
    }

    public String getNextState() {
        if (!hasNextState()) {
            throw new IllegalArgumentException("No more available states!");
        }

        moveStateIndex();
        final int nextStateValue = getNextStateValue();
        return NODE_STATES[currentState][nextStateValue];
    }

    private int getNextStateValue() {
        final int next = (this.counts[currentState][0] + 1) % (NODE_STATES[currentState].length);
        this.counts[currentState][0] = next;
        this.counts[currentState][1]--;
        return next;
    }

    private void moveStateIndex() {
        do {
            currentState = (currentState + 1) % NODE_STATES.length;
        } while (counts[currentState][1] == 0);
    }

    public boolean hasNextState() {
        for (final int[] count : counts) {
            if (count[1] > 0) {
                return true;
            }
        }

        return false;
    }
}