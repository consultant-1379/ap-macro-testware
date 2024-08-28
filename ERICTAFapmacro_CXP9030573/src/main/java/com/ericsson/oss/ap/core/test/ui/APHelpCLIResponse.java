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
package com.ericsson.oss.ap.core.test.ui;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.BrowserType;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.test.ui.model.APHelpCLIModel;
import com.ericsson.oss.ap.erbs.test.data.APHelpTestData;

/**
 * TAF UI tests to confirm CLI "help ap {@literal <}command{@literal >}" responses are as expected.
 * 
 * @author eshemeh
 * @since 1.3.3
 */
public class APHelpCLIResponse extends TorTestCaseHelper {

    private final static String TEST_DESCRIPTION = "AP: CLI - Testing command response: ";
    private final static String TEST_CASE = "TORF-19956_Func_1";
    private final static String HELP_AP_COMMAND = "help ap ";
    private final static String ENTER_KEY = "\uE006";

    @Inject
    private APHostResolver hostResolver;

    private APHelpCLIModel viewModel;

    /**
     * Tests the responses shown by the CLI for "help ap {@literal <}command {@literal >}" commands.
     * 
     * @param helpCommand
     *            the command being tested
     * @param expectedResult
     *            the expected response
     */
    @Context(context = Context.UI)
    @Test(dataProvider = APHelpTestData.HELP_AP, dataProviderClass = APHelpTestData.class, groups = { "Acceptance", "GAT" })
    @TestId(id = TEST_CASE, title = "Testing \"help ap\" command responses")
    public void testHelpResponse(final String helpCommand, final String expectedResult) {
        setTestCase(TEST_CASE, TEST_DESCRIPTION + HELP_AP_COMMAND + helpCommand);

        loadCLIWebpage();
        final UiComponent cliInput = viewModel.getCLIInput();
        cliInput.sendKeys(HELP_AP_COMMAND + helpCommand + ENTER_KEY);

        final String response = viewModel.getHelpText();
        assertEquals(expectedResult, response);
    }

    private void loadCLIWebpage() {
        final Host host = hostResolver.getApacheHost();
        final Browser browser = UI.newBrowser(BrowserType.CHROME);
        final String ipAddress = host.getIp();
        final String port = "8585";

        final BrowserTab browserTab = browser.open("http://" + ipAddress + ":" + port);

        browserTab.waitUntilComponentIsDisplayed(SelectorType.CSS, APHelpCLIModel.CLI_INPUT, 5000);
        viewModel = browserTab.getView(APHelpCLIModel.class);
    }
}