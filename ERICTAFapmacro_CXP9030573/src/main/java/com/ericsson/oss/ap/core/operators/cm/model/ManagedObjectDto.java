/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.core.operators.cm.model;

/**
 * Represents a detached DPS {@link ManagedObject}.
 *
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class ManagedObjectDto extends PersistenceObjectDto {

    private short level;
    private String fdn;
    private String name;

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFdn() {
        return fdn;
    }

    public void setFdn(final String fdn) {
        this.fdn = fdn;
    }

    public void setLevel(final short level) {
        this.level = level;
    }

    public short getLevel() {
        return level;
    }
}
