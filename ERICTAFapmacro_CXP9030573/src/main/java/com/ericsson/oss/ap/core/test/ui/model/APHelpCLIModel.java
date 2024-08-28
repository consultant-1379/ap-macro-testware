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
package com.ericsson.oss.ap.core.test.ui.model;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

/**
 * Generic View Model to be used in TAF UI tests. <br />
 * Models the CLI page's input area and command response.
 * 
 * @author eshemeh
 * @since 1.3.3
 */
public class APHelpCLIModel extends GenericViewModel {

    private final static String HTML_PATH = "/html";
    private final static String RESPONSE_HEADER = "Available commands and/or commandsets:";

    public static final String CLI_INPUT = "#cliInput";

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = CLI_INPUT)
    private UiComponent cliInput;
    
    @UiComponentMapping(selectorType = SelectorType.XPATH, selector = HTML_PATH)
    private UiComponent helpDisplayArea;

    public UiComponent getCLIInput() {
        return cliInput;
    }

    /**
     * Returns content of display area, after stripping response header and
     * footer.
     * 
     * @return help command response
     */
    public String getHelpText() {
        final String helpText = helpDisplayArea.getText();
        return helpText.substring(helpText.indexOf(RESPONSE_HEADER) + RESPONSE_HEADER.length() + 1, helpText.length() - 2);
    }
}