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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code ResultEntity} represent any type of entity inside the {@code CommandResult} that is returned from any command line operation.
 * It can e.g. be an ManagedObject, or anything that can be grouped.
 * It can have child ResultEntities, e.g. to reflect nodes underneath a project
 * 
 * @since 1.2.42
 */
public class ResultEntity {

    private final Map<String, String> attributes = new HashMap<>();
    private final List<ResultEntity> children = new ArrayList<>();
    private String name;

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void addAttribute(final String key, final String value) {
        attributes.put(key, value);
    }

    public void addAttributes(final Map<String, String> extractedAttributes) {
        attributes.putAll(extractedAttributes);
    }

    public List<ResultEntity> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(final ResultEntity newChild) {
        children.add(newChild);
    }

    public void addChildren(final List<ResultEntity> newChildren) {
        children.addAll(newChildren);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
