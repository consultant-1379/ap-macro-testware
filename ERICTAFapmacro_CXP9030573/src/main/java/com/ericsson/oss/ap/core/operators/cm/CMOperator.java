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
package com.ericsson.oss.ap.core.operators.cm;

import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;

/**
 * Configuration Management Operator providing support for manipulating (create, read, delete) managed objects persisted in the sytsem.
 * 
 * @author eeiciky
 */
public interface CMOperator {


    /**
     * Find a managed object by fdn.
     * 
     * @param host
     *            the host instance to connect to
     * @param fdn
     *            the fdn of the
     *            to retrieve
     * @return a instance or
     *         if no instance exists
     */
    ManagedObjectDto findMoByFdn(Host host, String fdn);
    
    /**
     * Find a managed object by fdn in a specific configuration.
     * 
     * @param host
     *            the host instance to connect to
     * @param fdn
     *            the fdn of the to retrieve
     * @param configuration
     *            the name of the configuration
     * @return a instance or null if no instance exists
     */
    ManagedObjectDto findMoByFdnInConfiguration(final Host host, final String fdn, final String configuration);


    /**
     * Delete a ManagedObject based on an FDN.
     * 
     * @param host
     *            the host instance to connect to
     * @param fdn
     *            the fdn of the mo to be deleted
     * 
     * @throws CMOperatorException
     *             if error occurs during ManagedObject deletion
     */
    void deleteMo(Host host, String fdn) throws CMOperatorException;


    /**
     * Retrieve all ManagedObjects of a specfic type in a namespace.
     * <p/>
     * The fdn can be used to restrict the scope in the namespace. Set to null in order to find all mos of a given type in the namespace. If an fdn value is supplied, then the moType must be a direct
     * child of that fdn.
     * 
     * @param host
     *            the host instance to connect to
     * @param namespace
     *            the namespace of the model
     * @param moType
     *            the type of ManagedObject to be returned
     * @param fdn
     *            the fdn of the parent ManagedObject or null in order to search the full namespace
     * 
     * @return a list of {@code ManagedObjectDto} instances. This list will never be {@code null} but could be empty.
     * 
     * @throws CMOperatorException
     *             if error occurs retrieving the ManagedObjects
     */
    List<ManagedObjectDto> getMosByType(Host host, String namespace, String moType, String fdn);

    /**
     * Retrieve all ManagedObjects of a specfic type in a namespace.
     * <p/>
     * The fdn can be used to restrict the scope in the namespace. Set to null in order to find all mos of a given type in the namespace. If an fdn value is supplied, then the moType can be a direct
     * child of that fdn. However if a deeper level of ancestry is required, e.g. grand children or great-grand children, then the childFilterPath must contain all intermediate moTypes. For example,
     * if one wants to find the NodeArtifacts underneath an Project, then the childFilterPath must be "Node,NodeArtifactContainer,NodeArtifact"
     * 
     * @param host
     *            the host instance to connect to
     * @param namespace
     *            the namespace of the model
     * @param moType
     *            the type of ManagedObject to be returned
     * @param fdn
     *            the fdn of the parent ManagedObject or null in order to search the full namespace
     * 
     * @param childFilterPath
     *            the child path leading from the parent fdn to the desired moType
     * 
     * @return a list of {@code ManagedObjectDto} instances. This list will never be {@code null} but could be empty.
     * 
     * @throws CMOperatorException
     *             if error occurs retrieving the ManagedObjects
     */
    List<ManagedObjectDto> getMosByType(Host host, String namespace, String moType, String fdn, String childFilterPath);


    /**
     * Create a ManagedObject instance.
     * <p/>
     * The parent ManagedObject must already exist.
     * 
     * @param host
     *            the host instance to connect to
     * @param fdn
     *            the FDN of the ManagedObject to be created
     * @param namespace
     *            the namespace of the model
     * @param version
     *            the version of the model
     * @param attributes
     *            ManagedObject attributes
     * 
     * @return the ManagedObjectDto instance representing the newly created ManagedObject
     * 
     * @throws CMOperatorException
     *             if error occurs creating the ManagedObject
     */
    ManagedObjectDto createMo(Host host, String fdn, String namespace, String version, Map<String, Object> attributes);


    /**
     * Updates a ManagedObject based on an FDN.
     * 
     * @param host
     *            the host instance to connect to
     * @param fdn
     *            the fdn of the mo to be updated
     * @param attributes
     *            ManagedObject attributes
     * 
     * @throws CMOperatorException
     *             if error occurs during ManagedObject update
     */
    void updateMo(Host host, final String fdn, final Map<String, Object> attributes);

    /**
     * Gets the children of the specified type belonging to the specified fdn.
     * 
     * @param host
     *            the host instance to connect to
     * @param fdn
     *            the fdn of the parent
     * @param moTypes
     *            the mo types that should be retrieved.
     * @return the children matching the specified moTypes. The list returned will never be null but may be empty.
     */
    List<ManagedObjectDto> getChildren(Host host, String namespace, String fdn, String... moTypes);

}
