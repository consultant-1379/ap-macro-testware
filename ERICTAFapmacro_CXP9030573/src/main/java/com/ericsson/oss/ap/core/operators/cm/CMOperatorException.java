/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.core.operators.cm;

/**
 * Thrown when an error occurs executing a CM operation.
 * 
 * @author eeiciky
 *
 */
public class CMOperatorException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
		public CMOperatorException(final String message) {
		super(message);
	}

}
