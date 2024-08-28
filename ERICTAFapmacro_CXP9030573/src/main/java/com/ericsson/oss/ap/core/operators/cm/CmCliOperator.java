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
package com.ericsson.oss.ap.core.operators.cm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.ResultEntity;
import com.ericsson.oss.services.commonCLI.operator.ApCmEditorRestOperator;

/**
 * REST implementation of {@link CMOperator}.
 * 
 * 
 */
@Operator(context = Context.REST)
@Singleton
public class CmCliOperator implements CMOperator {

    private final static Logger LOGGER = Logger.getLogger(CmCliOperator.class);

    private final ApCmEditorRestOperator apCmEditorRestOperator = new ApCmEditorRestOperator();

    @Override
    public ManagedObjectDto findMoByFdn(final Host host, final String fdn) {
        return findMoByFdnInConfiguration(host, fdn, null);
    }

    @Override
    public ManagedObjectDto findMoByFdnInConfiguration(final Host host, final String fdn, final String configuration) {
        String command = "cmedit get " + fdn;
        if (configuration != null) {
            command = command + " -c=" + configuration;
        }
        final CommandResult result = apCmEditorRestOperator.executeCommand(host, command);

        ManagedObjectDto managedObjectDto = null;
        if (result.isSuccessful()) {
            final List<ManagedObjectDto> managedObjectDtos = extractManagedObjectDtos(result);

            final Iterator<ManagedObjectDto> iter = managedObjectDtos.iterator();
            if (iter.hasNext()) {
                managedObjectDto = iter.next();
            }
        } else {
            LOGGER.info(result.getStatusMessage());
        }

        return managedObjectDto;
    }

    @Override
    public void deleteMo(final Host host, final String fdn) throws CMOperatorException {

        final String command = "cmedit delete " + fdn + " -ALL";
        final CommandResult result = apCmEditorRestOperator.executeCommand(host, command);

        if (!result.isSuccessful()) {
            throw new CMOperatorException(result.getStatusMessage());
        }
    }

    @Override
    public List<ManagedObjectDto> getMosByType(final Host host, final String namespace, final String moType, final String fdn) {
        return getMosByType(host, namespace, moType, fdn, null);
    }

    @Override
    public List<ManagedObjectDto> getMosByType(final Host host, final String namespace, final String moType, final String fdn,
            final String childFilterPath) {

        final String scope = (fdn == null) ? "*" : fdn;

        final String command = "cmedit get " + scope + " " + moType + ".*" + " -ns=" + namespace;

        final CommandResult result = apCmEditorRestOperator.executeCommand(host, command);

        if (!result.isSuccessful()) {
            throw new CMOperatorException(result.getStatusMessage());
        }

        return extractManagedObjectDtos(result);
    }

    @Override
    public ManagedObjectDto createMo(final Host host, final String fdn, final String namespace, final String version,
            final Map<String, Object> attributes) {

        final String command = String.format("cmedit create %s %s -namespace=%s -version=%s", fdn, mapAsCommaSeparatedString(attributes), namespace,
                version);

        final CommandResult result = apCmEditorRestOperator.executeCommand(host, command);

        if (!result.isSuccessful()) {
            LOGGER.debug("Executing command: " + command);
            throw new CMOperatorException(result.getStatusMessage());
        }

        return extractManagedObjectDtos(result).iterator().next();
    }

    @Override
    public List<ManagedObjectDto> getChildren(final Host host, final String namespace, final String fdn, final String... moTypes) {
        if (moTypes == null) {
            throw new IllegalArgumentException("moTypes must not be null");
        }

        final List<ManagedObjectDto> children = new ArrayList<>();

        for (final String moType : moTypes) {
            final List<ManagedObjectDto> dtoList = this.getMosByType(host, namespace, moType, fdn);
            children.addAll(dtoList);
        }

        return children;
    }

    private String mapAsCommaSeparatedString(final Map<String, Object> attributes) {
        final StringBuilder nameValues = new StringBuilder();
        int numAttrs = attributes.size();
        for (final String attrName : attributes.keySet()) {
            nameValues.append(attrName).append('=');

            final Object attrValue = attributes.get(attrName);
            nameValues.append(attrValue);

            if (--numAttrs != 0) {
                nameValues.append(",");
            }
        }
        return nameValues.toString();
    }

    private List<ManagedObjectDto> extractManagedObjectDtos(final CommandResult result) {
        final List<ManagedObjectDto> managedObjectDtos = new ArrayList<>();

        final List<ResultEntity> entities = result.getResultEntities();
        for (final ResultEntity entity : entities) {
            final ManagedObjectDto managedObjectDto = new ManagedObjectDto();
            final Map<String, String> attributes = entity.getAttributes();

            for (final Map.Entry<String, String> entry : attributes.entrySet()) {
                final String value = "null".equals(entry.getValue()) ? null : entry.getValue();
                if ("FDN".equals(entry.getKey())) {
                    final String fdn = value;
                    managedObjectDto.setFdn(fdn);
                    managedObjectDto.setName(getRdn(fdn));
                    managedObjectDto.setType(getType(fdn));
                } else {
                    managedObjectDto.setAttribute(entry.getKey(), value);
                }
            }
            managedObjectDtos.add(managedObjectDto);
        }

        return managedObjectDtos;
    }

    private String getRdn(final String fdn) {
        return fdn.substring(fdn.lastIndexOf("=") + 1, fdn.length());
    }

    private String getType(final String fdn) {
        return fdn.substring(fdn.lastIndexOf(",") + 1, fdn.lastIndexOf("="));
    }

    @Override
    public void updateMo(final Host host, final String fdn, final Map<String, Object> attributes) {

        final StringBuilder updateCommandBuilder = new StringBuilder("cmedit set ");
        updateCommandBuilder.append(fdn).append(' ');

        final Iterator<Map.Entry<String, Object>> it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, Object> entry = it.next();
            updateCommandBuilder.append(entry.getKey()).append('=').append('"').append(entry.getValue().toString()).append('"');

            if (it.hasNext()) {
                updateCommandBuilder.append(",");
            }
        }

        final CommandResult result = apCmEditorRestOperator.executeCommand(host, updateCommandBuilder.toString());

        if (!result.isSuccessful()) {
            throw new CMOperatorException(result.getStatusMessage());
        }
    }

}
