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
package com.ericsson.oss.ap.erbs.test.cases.validators;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Class provides methods for accessing data in an xml document.
 * 
 */
public class DocumentReader {

    private final Document document;

    public DocumentReader(final String xml) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            this.document = builder.parse(new InputSource(new StringReader(xml)));
        } catch (final Exception e) {
            throw new IllegalStateException("Error reading xml");
        }
    }

    /**
     * Gets the value for the specified element attribute. If multiple elements
     * of the same name exist then the first occurrence of the element will be
     * processed.
     * 
     * @param elementName
     *            the name of the element
     * @param attribute
     *            the name of the attribute
     * @return attribute string value or empty string if no element or attribute
     *         found.
     */
    public String getElementAttributeValue(final String elementName, final String attributeName) {
        final Collection<Element> elements = getAllElements(elementName);
        if (elements.isEmpty()) {
            return "";
        }
        final Element element = elements.iterator().next();
        return element.getAttribute(attributeName);
    }

    private Collection<Element> getAllElements(final String elementName) {
        final Element root = document.getDocumentElement();
        final NodeList nodeList = root.getElementsByTagName(elementName);
        return nodeListToElements(nodeList);
    }

    private Collection<Element> nodeListToElements(final NodeList nodeList) {
        final Collection<Element> elements = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node child = nodeList.item(i);
            elements.add((Element) child);
        }
        return elements;
    }

}