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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a detached DPS {@code PersistenceObject}.
 * 
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class PersistenceObjectDto implements Serializable {

    protected String type;
    protected String namespace;
    protected String version;
    private long poId;
    protected Map<String, Object> attributes = new HashMap<>();

    public void setType(final String type) {
        this.type = type;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getType() {
        return type;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getVersion() {
        return version;
    }

    public void setPoId(final long poId) {
        this.poId = poId;
    }

    public long getPoId() {
        return poId;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(final String attributeName) {
        return (T) this.attributes.get(attributeName);
    }

    public Boolean getBooleanAttribute(final String attributeName) {
        Boolean value;
        final Object attr = this.attributes.get(attributeName);
        if (attr instanceof String) {
            value = Boolean.parseBoolean((String) attr);
        } else if (attr instanceof Boolean) {
            value = (Boolean) attr;
        } else {
            value = Boolean.FALSE;
        }
        return value;
    }

    public Map<String, Object> getAttributes(final Collection<String> attributeNames) {
        return getAttributes(attributeNames.toArray(new String[attributeNames.size()]));
    }

    public Map<String, Object> getAttributes(final String[] attributeNames) {
        final Map<String, Object> attributeSet = new HashMap<>();

        for (final String attrName : attributeNames) {
            final Object attrValue = this.attributes.get(attrName);
            if (attrValue != null) {
                attributeSet.put(attrName, attrValue);
            }
        }
        return attributeSet;
    }

    public Map<String, Object> getAllAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(final String attributeName, final Object attributeValue) {
        attributes.put(attributeName, attributeValue);
    }
}
