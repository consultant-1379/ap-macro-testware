package com.ericsson.oss.ap.core.helpers;

import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.operators.cm.model.PersistenceObjectDto;

/**
 * This class provides some helper methods for extracting information from a PersistentMO.
 * 
 * @author ekarasi
 * @since 1.0.10
 */
public class PersistentMOHelper {
    
    private PersistentMOHelper() {}
    
    /**
     * Retrieves data from a {@code PersistentObjectDto}. It handles accessing data that is not directly contained in the attribute map so that data such
     * as name and fdn (in the case of a {@code ManagedObjectDto}) can also be easily retrieved using this method.
     * 
     * The non-attribute values supported by this method are
     * <ul>
     * <li>name (only if instance is a {@code ManagedObjectDto})</li>
     * <li>fdn (only if instance is a {@code ManagedObjectDto})</li>
     * <li>type</li>
     * </ul>
     * 
     * @param thePersistentObject
     *            the {@code PersistentObjectDto} instance to query. This cannot be {@code null}.
     * @param theAttribute
     *            the name of the attribute to retrieve. This cannot be {@code null}.
     * @throws ClassCastException
     *             thrown if the instance is not a {@code ManagedObjectDto} but the attribute requested only applies to a {@code ManagedObjectDto}.
     * @throws IllegalArgumentException
     *             thrown if {@code thePersistentObject} is {@code null} or {@code theAttribute} is {@code null} or has a length of 0.
     * @return the value of the attribute or {@code null} if no attribute is found.
     */
    public static String getAttributeAsString(final PersistenceObjectDto thePersistentObject, final String theAttribute) throws ClassCastException,
            IllegalArgumentException {
        if (thePersistentObject == null) {
            throw new IllegalArgumentException("The PersistentObject cannot be null");
        }
        if (theAttribute == null || theAttribute.isEmpty()) {
            throw new IllegalArgumentException("The attribute name cannot be null or empty.");
        }

        String value = null;
        if ("name".equals(theAttribute)) {
            value = ((ManagedObjectDto) thePersistentObject).getName();
        } else if (theAttribute.equalsIgnoreCase("fdn")) {
            value = ((ManagedObjectDto) thePersistentObject).getFdn();
        } else if (theAttribute.equalsIgnoreCase("type")) {
            value = thePersistentObject.getType();
        } else {
            // If e.g. upgradePackageName is null
            final Object valueObject = thePersistentObject.getAttribute(theAttribute);
            if (valueObject != null) {
                value = valueObject.toString();                
            }
        }
        return value;
    }
}
