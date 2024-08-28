/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2013
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.ap.core.test.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.ap.erbs.test.model.TestInfo;

/**
 * This class is a base class for holding data related to the test execution.
 * 
 * @author ekarasi
 * @since 1.0.1
 */
public class GenericAPTestInfo implements TestInfo {
    private String name;
    private String value;
    private final Map<String, Object> additionalInfo = new HashMap<>();
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private List<GenericAPTestInfo> children;

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Object setAttribute(final String key, final Object value) {
        return this.attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(final String key) {
        return (T) this.attributes.get(key);
    }

    public String getAttributeAsString(final String key) {
        final Object value = this.attributes.get(key);
        return value == null ? null : value.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(final String theName) {
        name = theName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String theValue) {
        value = theValue;
    }

    public void addAttributes(final Map<String, Object> allAttributes) {
        this.attributes.putAll(allAttributes);
    }

    public List<GenericAPTestInfo> getChildren() {
        return children;
    }

    public void setChildren(final List<GenericAPTestInfo> theChildren) {
        children = theChildren;
    }
}
