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
package com.ericsson.oss.services.commonCLI.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.CommandResult.Result;
import com.ericsson.oss.ap.core.test.model.ResultEntity;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.HeaderRowDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.RowCell;
import com.ericsson.oss.services.scriptengine.spi.dtos.RowDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileDownloadRequestDto;

/**
 * Class converts the http response string (containing ResponseDto), which is
 * returned from the common CLI, into a CommandResult object, which is used
 * internally in the AP TAF.
 * 
 * This is the only class in the AP TAF that knows and should know about the Dto classes from Script Engine.
 * So impact of changes to Script Engine should be confined to this class only.
 * 
 * @since 1.4.12
 * 
 */
public class ResponseToCommandResultConverter {

    private static final String ERROR = "Error";
    private static final String SUGGESTED_SOLUTION = "Suggested Solution :";
    private static final String NAMEVALUE_SEPARATOR = " : ";
    private static final String APPLICATION_ID = "applicationId";
    private static final String FILE_ID = "fileId";

    public static Logger logger = LoggerFactory.getLogger(ResponseToCommandResultConverter.class);

    /**
     * Extracts the data from the HttpResponse returned from a Multipart/FormData request, and returns it inside a CommandResult.
     * If the ResponseDto contains a LineDto that contains a "Suggested Solution :" string then the request failed and we extract failure information
     * Otherwise we extract the data from the successful request response.
     * 
     * A successful request response resulting from a "cmedit get" command needs to be handled differently, as in this case the response can
     * contain multiple MOs. These MOs are returned as a series of LineDtos (one for each attribute) and the end of each MO is indicated by a LineDto
     * that starts with FDN.
     * 
     * @param httpResponse
     *            HttpResponse returned from a file upload request
     * @param command
     *            Command passed in the Multipart/FormData request
     * @return commandResult
     */
    public CommandResult extractCommandResultFromMultipartFormDataResponse(final HttpResponse httpResponse, final String command) {

        final ResponseDto responseDto = extractResponseDto(httpResponse);
        final String strippedCommand = StringUtils.deleteWhitespace(command);

        if (isResponseDtoContainingLineDtoContainingValue(responseDto, SUGGESTED_SOLUTION)) {
            return extractCommandResultForFailure(responseDto);
        } else {
            if (strippedCommand.contains("cmeditget")) {
                return extractCommandResultForCmEditResponse(responseDto);
            } else if (strippedCommand.contains("configlist")) {
                return extractCommandResultForConfigList(responseDto);
            } else {
                return extractCommandResultForSuccessfulMultipartFormData(responseDto);
            }
        }
    }

    /**
     * Extracts the data from the HttpResponse returned from a file upload request, and returns it inside a CommandResult.
     * If the ResponseDto contains a LineDto that contains a "Suggested Solution :" string then the request failed and we extract failure information
     * Otherwise we extract the data from the successful request
     * 
     * @param httpResponse
     *            HttpResponse returned from a file upload request
     * @return commandResult
     */
    public CommandResult extractCommandResultFromUploadResponse(final HttpResponse httpResponse) {

        final ResponseDto responseDto = extractResponseDto(httpResponse);

        if (isResponseDtoContainingLineDtoContainingValue(responseDto, SUGGESTED_SOLUTION)) {
            return extractCommandResultForFailure(responseDto);
        } else {
            return extractCommandResultForSuccessfulUpload(responseDto);
        }
    }

    /**
     * Extracts the data from the HttpResponse returned from a file download request, and returns it inside a CommandResult.
     * If the ResponseDto contains a LineDto that contains a "Suggested Solution :" string then the request failed and we extract failure information
     * Otherwise we extract the data from the successful request
     * 
     * @param httpResponse
     *            HttpResponse returned from a file download request
     * @return commandResult
     */
    public CommandResult extractCommandResultFromDownloadResponse(final HttpResponse httpResponse) {

        final ResponseDto responseDto = extractResponseDto(httpResponse);

        if (isResponseDtoContainingLineDtoContainingValue(responseDto, SUGGESTED_SOLUTION)) {
            return extractCommandResultForFailure(responseDto);
        } else {
            return extractCommandResultForSuccessfulDownload(responseDto);
        }
    }

    /**
     * gets the applicationId and fileId strings, which are needed to download a file
     * 
     * @param httpResponse
     *            HttpResponse returned from the post part of a file download request
     * @return Map containing applicationId and fileId
     */
    public FileDownloadRequestDto extractFileAndApplicationId(final HttpResponse httpResponse) {
        FileDownloadRequestDto fileDownloadRequestDto = null;
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final JsonNode result = mapper.readTree(httpResponse.getBody());
            fileDownloadRequestDto = new FileDownloadRequestDto(result.findValue(APPLICATION_ID).asText(), result.findValue(FILE_ID).asText());
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException(e);
        }
        return fileDownloadRequestDto;
    }

    /**
     * Extracts the data from the ResponseDto returned from a failed request, and returns it inside a CommandResult.
     * The ResponseDto in a failure scenario only contains LineDtos
     * 
     * If the request has failed then the ResponseDto contains:
     * - a LineDto which contains SUGGESTED_SOLUTION String
     * - a LineDto which contains ERROR String
     * - possibly 1 or more LineDtos which contains validation message (its values start with a number)
     * 
     * @param responseDto
     *            ResponseDto with the data from a failed request
     * @return commandResult
     */
    private CommandResult extractCommandResultForFailure(final ResponseDto responseDto) {
        final CommandResult commandResult = new CommandResult();
        commandResult.setResult(Result.FAILED);

        final List<AbstractDto> elements = responseDto.getElements();
        for (final AbstractDto dto : elements) {
            if (dto instanceof LineDto) {
                final LineDto lineDto = (LineDto) dto;
                if (isLineDtoContainingValue(lineDto, SUGGESTED_SOLUTION)) {

                    commandResult.setSolution(extractNamevalue(lineDto)[1]);

                } else if (isLineDtoStartingWithANumber(lineDto)) {

                    commandResult.setValidationMessage(extractNamevalue(lineDto)[1]); //TODO won't work with multiple validation messages

                } else if (isLineDtoContainingValue(lineDto, ERROR)) {

                    final String[] nameValue = extractNamevalue(lineDto);
                    commandResult.setStatusMessage(nameValue[1]);

                    //get ErrorCode
                    final String nameContainingErrorCode = nameValue[0];
                    final int errorCode = Integer.valueOf(nameContainingErrorCode.split("\\s+")[1]);
                    commandResult.setErrorCode(errorCode);
                }
            }
        }

        return commandResult;
    }

    /**
     * Extracts the data from the ResponseDto returned from a successful Multipart/FormData request, and returns it inside a CommandResult.
     * The ResponseDto from the Multipart/FormData request contains a mixture of LineDtos and RowDtos
     * 
     * @param responseDto
     *            ResponseDto with the data from a successful upload request
     * @return commandResult
     */
    private CommandResult extractCommandResultForSuccessfulMultipartFormData(final ResponseDto responseDto) {
        final CommandResult commandResult = new CommandResult();
        commandResult.setResult(Result.SUCCESS);

        final ResultEntity parentEntity = new ResultEntity();
        final Map<String, String> parentAttributes = new HashMap<>();

        List<RowDto> rowDtos = new ArrayList<RowDto>();
        final List<Table> tableList = new ArrayList<Table>();

        final List<AbstractDto> elements = responseDto.getElements();
        for (final AbstractDto dto : elements) {
            if (dto instanceof LineDto) {

                final LineDto lineDto = (LineDto) dto;
                if (isLineDtoContainingValue(lineDto, NAMEVALUE_SEPARATOR)) {

                    final String[] nameValue = extractNamevalue(lineDto);
                    parentAttributes.put(nameValue[0], nameValue[1]);

                } else {
                    setStatusMessage(commandResult, lineDto);
                }

            } else if (dto instanceof RowDto) {

                if (dto instanceof HeaderRowDto) {
                    // there can be more than 1 table, so need to extract each table from the ResponseDto
                    // this is the end of the current table, if it is a HeaderRowDto  ... temporary solution until ScriptEngine puts in improvements
                    final HeaderRowDto headerRowDto = (HeaderRowDto) dto;

                    final Table table = new Table();
                    table.setHeaderRowDto(headerRowDto);
                    table.setRowDtos(rowDtos);

                    tableList.add(table);

                    // reset rowDtos to allow for next table to be processed
                    rowDtos = new ArrayList<RowDto>();
                } else {
                    //this is a rowDto
                    final RowDto rowDto = (RowDto) dto;
                    rowDtos.add(rowDto);
                }
            }
        }

        final List<ResultEntity> childEntities = getResultEntitiesFromTables(tableList);

        if (!parentAttributes.isEmpty()) {
            //add ResultEntity to CommandResult only if parentAttributes is not empty (otherwise there is no MO in the results)
            parentEntity.addAttributes(parentAttributes);
            commandResult.addResultEntity(parentEntity);
        }

        if (commandResult.getResultEntities().isEmpty()) {
            //if commandResult has no ResultEntity, then childEntities extracted from TableDto will be treated as parentEntities, e.g. "ap view" returns all projects in a TableDto
            commandResult.addResultEntities(childEntities);
        } else {
            //if commandResult has a ResultEntity, then childEntities extracted from TableDto will be treated as chidren, e.g. "ap view -p <projectName>" returns project and its nodes..
            commandResult.getResultEntities().get(0).addChildren(childEntities);
        }

        return commandResult;
    }

    /**
     * @param commandResult
     * @param lineDto
     */
    private void setStatusMessage(final CommandResult commandResult, final LineDto lineDto) {
        if (isLineDtoNotNullAndNotEmptyString(lineDto) && (!(lineDto instanceof CommandDto))) {
            // this is the status message, i.e. it has no " : " separator
            commandResult.setStatusMessage(lineDto.getValue());
        }
    }

    /**
     * Extracting the ResultEntities from the Tables in the ResponseDto for MultiPartFormData requests
     * The ResultEntities will be added to the CommandResult
     * 
     * If there are 2 tables then the first table contains node level data and the second table contains project level data
     * If there is 1 table then this contains either project level or node level data
     * Temporary solution until we get more support from ScriptEngine ... e.g. when dtoNames can be set
     * looping over the table backwards so I get the top level data first, as this is expected in the testcases
     * 
     * @param tableList
     *            List of Tables containing the tables extracted from the ResponseDto
     * @return List<ResultEntity> the resultEntities, each containing a separate table.
     */
    private List<ResultEntity> getResultEntitiesFromTables(final List<Table> tableList) {
        // process the tables. 
        final List<ResultEntity> childEntities = new ArrayList<>();

        for (int i = tableList.size() - 1; i >= 0; i--) {

            final Table table = tableList.get(i);

            final List<String> columnNames = new ArrayList<String>();

            final List<RowCell> headerRows = table.getHeaderRowDto().getElements();
            for (final RowCell rowCell : headerRows) {
                columnNames.add(rowCell.getValue());
            }

            final List<RowDto> rowDtoList = table.getRowDtos();

            // process the HeaderRow and RowDtos, i.e. create name-value pairs and put them in attribute maps for ResultEntities
            for (final RowDto rowDto : rowDtoList) {

                int columnIndex = 0;
                final ResultEntity childEntity = new ResultEntity();
                final Map<String, String> childAttributes = new HashMap<>();

                final List<RowCell> rows = rowDto.getElements();

                for (final String columnName : columnNames) {
                    final String value = rows.get(columnIndex).getValue();
                    childAttributes.put(columnName, value);
                    columnIndex++;
                }

                if (!childAttributes.isEmpty()) {
                    //add childEntity to ChildEntities only if childAttributes is not empty
                    childEntity.addAttributes(childAttributes);
                    childEntity.setName(getEntityLevel(tableList.size(), i + 1));
                    childEntities.add(childEntity);
                }
            }
        }
        return childEntities;
    }

    /**
     * Extracts the data from the ResponseDto returned from a successful upload request, and returns it inside a CommandResult.
     * 
     * @param responseDto
     *            ResponseDto with the data from a successful upload request
     * @return commandResult
     */
    private CommandResult extractCommandResultForSuccessfulUpload(final ResponseDto responseDto) {

        final CommandResult commandResult = new CommandResult();
        commandResult.setResult(Result.SUCCESS);

        final ResultEntity parentEntity = new ResultEntity();
        final Map<String, String> parentAttributes = new HashMap<>();

        final List<AbstractDto> elements = responseDto.getElements();
        for (final AbstractDto dto : elements) {
            if (dto instanceof LineDto) {

                final LineDto lineDto = (LineDto) dto;
                if (isLineDtoContainingValue(lineDto, NAMEVALUE_SEPARATOR)) {

                    final String[] nameValue = extractNamevalue(lineDto);
                    parentAttributes.put(nameValue[0], nameValue[1]);

                } else {
                    setStatusMessage(commandResult, lineDto);
                }
            }
        }
        parentEntity.addAttributes(parentAttributes);
        commandResult.addResultEntity(parentEntity);
        return commandResult;
    }

    /**
     * Extracts the data from the ResponseDto returned from a successful download request, and returns it inside a CommandResult.
     * 
     * @param responseDto
     *            ResponseDto with the data from a successful download request
     * @return commandResult
     */
    private CommandResult extractCommandResultForSuccessfulDownload(final ResponseDto responseDto) {
        final CommandResult commandResult = new CommandResult();
        commandResult.setResult(Result.SUCCESS);

        final List<AbstractDto> elements = responseDto.getElements();
        for (final AbstractDto dto : elements) {
            if (dto instanceof LineDto) {

                final LineDto lineDto = (LineDto) dto;
                setStatusMessage(commandResult, lineDto);
            }
        }
        return commandResult;
    }

    /**
     * Extracts the data from the ResponseDto returned from a "cmedit get" request, and returns it inside a CommandResult.
     * 
     * A successful request response from a "cmedit get" command needs to be handled differently, as in this case the response can
     * contain multiple MOs. These MOs are returned as a series of LineDtos (one for each attribute) and the end of each MO is indicated by a LineDto
     * that starts with FDN.
     */
    private CommandResult extractCommandResultForCmEditResponse(final ResponseDto responseDto) {

        final CommandResult commandResult = new CommandResult();
        commandResult.setResult(Result.SUCCESS);

        final Map<String, Map<String, String>> attrsForAllFDNs = getAttributesPerFdn(responseDto);

        for (final Entry<String, Map<String, String>> atrrsForSingleFDN : attrsForAllFDNs.entrySet()) {
            final ResultEntity entity = new ResultEntity();
            entity.addAttributes(atrrsForSingleFDN.getValue());
            commandResult.addResultEntity(entity);
        }
        return commandResult;
    }

    /**
     * Extracts the data from the ResponseDto returned from a "cmedit get" request, and returns it inside a map containing attribute maps for each FDN.
     * 
     * A successful request response from a "cmedit get" command needs to be handled differently, as in this case the response can
     * contain multiple MOs. These MOs are returned as a series of LineDtos (one for each attribute) and the end of each MO is indicated by a LineDto
     * that starts with FDN.
     */
    private Map<String, Map<String, String>> getAttributesPerFdn(final ResponseDto responseDto) {
        final Map<String, Map<String, String>> attributesPerFdn = new HashMap<>();
        Map<String, String> attributeMap = new HashMap<>();
        if (responseDto != null) {
            for (final AbstractDto abstractDto : responseDto.getElements()) {
                final LineDto lineDto = (LineDto) abstractDto;
                final String value = lineDto.getValue();
                if (value != null && value != "" && !value.contains("instance(s)")) {
                    final String[] nameValue = lineDto.getValue().split(" : ");
                    if (nameValue.length > 1) {
                        attributeMap.put(nameValue[0], nameValue[1]);
                    } else {
                        attributeMap.put(nameValue[0], "");
                    }
                    if (lineDto.getValue().startsWith("FDN : ")) {
                        attributesPerFdn.put(attributeMap.get("FDN"), attributeMap);
                        attributeMap = new HashMap<>();
                    }
                }
            }
        }
        return attributesPerFdn;
    }

    /**
     * Extracts the data from the ResponseDto returned from a successful config list request, and returns it inside a CommandResult.
     * 
     * @param responseDto
     *            ResponseDto with the data from a successful config list request
     * @return commandResult
     */
    private CommandResult extractCommandResultForConfigList(final ResponseDto responseDto) {

        final CommandResult commandResult = new CommandResult();
        commandResult.setResult(Result.SUCCESS);

        final ResultEntity parentEntity = new ResultEntity();
        final Map<String, String> parentAttributes = new HashMap<>();

        final List<AbstractDto> elements = responseDto.getElements();
        for (final AbstractDto dto : elements) {

            if (dto instanceof LineDto) {

                final LineDto lineDto = (LineDto) dto;
                final String value = lineDto.getValue();

                if (value != null && value != "" && !value.contains("config(s)")) {
                    parentAttributes.put(value, ""); //TODO not the nicest solution, but expected by CMConfigRestOperator in this format 
                } else {
                    commandResult.setStatusMessage(value);
                }
            }
        }
        parentEntity.addAttributes(parentAttributes);
        commandResult.addResultEntity(parentEntity);
        return commandResult;
    }

    /**
     * Extracts a NameValue array from a LineDto
     */
    private String[] extractNamevalue(final LineDto lineDto) {
        final String[] nameValue = lineDto.getValue().split(NAMEVALUE_SEPARATOR, 2);

        if (nameValue.length != 2) {
            nameValue[1] = "";
        }

        return nameValue;
    }

    /**
     * Extracts a ResponseDto from the HttpResponse
     */
    private ResponseDto extractResponseDto(final HttpResponse httpResponse) {
        logger.debug("extractResponseDto : " + httpResponse.getBody());

        final ObjectMapper mapper = new ObjectMapper();
        ResponseDto responseDto = null;
        try {
            final JsonNode result = mapper.readTree(httpResponse.getBody());
            final JsonNode dtoResult = result.get("responseDto");
            responseDto = mapper.readValue(dtoResult, ResponseDto.class);
        } catch (final IOException e) {
            logger.debug(e.getMessage());
            throw new RuntimeException(e); //NOPMD
        }
        return responseDto;
    }

    /**
     * returns the level to which a table can belong. Tables can be returned at 2 levels, e.g. project level and node level
     * 
     * Temporary solution until we get more support from ScriptEngine, i.e. until we can set dtoName on a table
     * all table info will go into childEntities, but the info can be at 2 levels, e.g. project level and node level
     */
    private String getEntityLevel(final int tableSize, final int count) {
        if (tableSize == count) {
            //if multiple tables then the last table happens to contain the project level information.
            //this 
            return "TOP_LEVEL";
        }
        return "BOTTOM_LEVEL";
    }

    /**
     * returns true if a lineDto starts with a number
     * When a lineDto starts with a number then this contain a validation message
     */
    private boolean isLineDtoStartingWithANumber(final LineDto lineDto) {
        if (isLineDtoNotNullAndNotEmptyString(lineDto) && Character.isDigit(lineDto.getValue().charAt(0)) && (!(lineDto instanceof CommandDto))) {
            return true;
        }
        return false;
    }

    /**
     * returns true if ResponseDto contains a LineDto that contains a given value
     */
    private boolean isResponseDtoContainingLineDtoContainingValue(final ResponseDto responseDto, final String value) {
        return getFirstLineDtoWhichContainsValue(responseDto, value) != null;
    }

    /**
     * returns the first LineDto inside a ResponseDto that contains a given value
     */
    private LineDto getFirstLineDtoWhichContainsValue(final ResponseDto responseDto, final String value) {
        final List<AbstractDto> entities = responseDto.getElements();
        for (final AbstractDto dto : entities) {
            if (dto instanceof LineDto) {
                final LineDto lineDto = (LineDto) dto;
                if (isLineDtoContainingValue(lineDto, value)) {
                    return lineDto;
                }
            }
        }
        return null;
    }

    /**
     * returns true if a lineDto contains a given value
     */
    private boolean isLineDtoContainingValue(final LineDto lineDto, final String value) {
        if (lineDto.getValue() != null && lineDto.getValue().contains(value)) {
            return true;
        }
        return false;
    }

    /**
     * returns true if a lineDto is not null and does not contain empty String ("")
     */
    private boolean isLineDtoNotNullAndNotEmptyString(final LineDto lineDto) {
        if (lineDto.getValue() != null && !(lineDto.getValue().equals(""))) {
            return true;
        }
        return false;
    }

}

/**
 * Table class, used to hold rowDtos and headerRowDto belonging to one table.
 * The ResponseDto can contain rowDtos and headerRowDto belonging to multiple tables, so this table class is needed to
 * keep the correct data together while extracting the data from the ResponseDto
 * 
 */
class Table {
    private List<RowDto> rowDtos;
    private HeaderRowDto headerRowDto;

    public List<RowDto> getRowDtos() {
        return rowDtos;
    }

    public void setRowDtos(final List<RowDto> rowDtos) {
        this.rowDtos = rowDtos;
    }

    public HeaderRowDto getHeaderRowDto() {
        return headerRowDto;
    }

    public void setHeaderRowDto(final HeaderRowDto headerRowDto) {
        this.headerRowDto = headerRowDto;
    }

}
