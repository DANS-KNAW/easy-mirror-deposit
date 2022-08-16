/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.mirror.core;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetMetadata {
    private String doi;
    private String nbn;
    private String title;
    private String description;
    private List<String> creators;
    private String created;
    private String modified;
    private String published;
    private String available;
    private List<String> audiences;
    private String accessRights;
    private List<String> rightsHolders;

    public DatasetMetadata() {
    }

    public DatasetMetadata(String jsonLdString) {
        DocumentContext context = JsonPath.parse(jsonLdString);
        nbn = readSingleValue(context,"$['ore:describes']['dansDataVaultMetadata:NBN']");
        title = readSingleValue(context,"$['ore:describes']['Title']");
        description = StringUtils.join(readMultiValue(context,
            "$['ore:describes']['citation:Description']['dsDescription:Text']",
            "$['ore:describes']['citation:Description'][*]['dsDescription:Text']"), "\n\n");
        creators = readMultiValue(context,
            "$['ore:describes']['Author']['author:Name']",
            "$['ore:describes']['Author'][*]['author:Name']");

        modified = readSingleValue(context,"$['ore:describes']['schema:dateModified']");
        published = readSingleValue(context,"$['ore:describes']['schema:datePublished']");
        created = readStringWithDefaultValue(context, "$['ore:describes']['citation:Date Produced']", published);
        available = published;
        audiences = readMultiValue(context, "$['ore:describes']['dansRelationMetadata:Audience']['@id']",
            "$['ore:describes']['dansRelationMetadata:Audience'][*]['@id']").stream().map(DatasetMetadata::extractNarcisIdFromUri).collect(
            Collectors.toList());
        // If no audiences were found, it could be that there are none, but it could also be that they are not stored as objects with an @id field, but rather as simple
        // Strings containing the term URIs. Maybe this happens when Dataverse is unable to download metadata from Skosmos?
        List<String> simpleStringUris = readOnlyStringElements(context, "$['ore:describes']['dansRelationMetadata:Audience']").stream()
            .map(DatasetMetadata::extractNarcisIdFromUri).collect(
                Collectors.toList());
        audiences.addAll(simpleStringUris);
        accessRights = "NO_ACCESS"; // No access through EASY
        rightsHolders = readMultiValueString(context, "$['ore:describes']['dansRights:Rights Holder']");
    }

    private static String readSingleValue(DocumentContext context, String path) {
        return StringEscapeUtils.escapeXml(context.read(path));
    }

    private static List<String> readMultiValue(DocumentContext context, String pathSingle, String pathMulti) {
        try {
            return Collections.singletonList(StringEscapeUtils.escapeXml(context.read(pathSingle)));
        }
        catch (PathNotFoundException e) {
            return context.read(pathMulti);
        }
    }

    private static List<String> readOnlyStringElements(DocumentContext context, String path) {
        Object value = context.read(path);
        if (value instanceof String) {
            return Collections.singletonList((String) value);
        }
        else if (!(value instanceof LinkedHashMap)){
            LinkedList<String> results = new LinkedList<>();
            Iterable<Object> objects = (Iterable<Object>) value;
            for (Object o : objects) {
                if (o instanceof String)
                    results.add(StringEscapeUtils.escapeXml((String) o));
            }
            return results;
        }
        return Collections.emptyList();
    }

    private static List<String> readMultiValueString(DocumentContext context, String path) {
        Object value = context.read(path);
        if (value instanceof String) {
            return Collections.singletonList((String) value);
        }
        else {
            return (List<String>) value;
        }
    }

    private static String readStringWithDefaultValue(DocumentContext context, String path, String defaultValue) {
        try {
            return StringEscapeUtils.escapeXml(context.read(path));
        }
        catch (PathNotFoundException e) {
            return defaultValue;
        }
    }

    private static String extractNarcisIdFromUri(String narcisTermUri) {
        String[] components = narcisTermUri.split("/");
        return components[components.length - 1];
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getNbn() {
        return nbn;
    }

    public void setNbn(String nbn) {
        this.nbn = nbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public List<String> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public List<String> getRightsHolders() {
        return rightsHolders;
    }

    public void setRightsHolders(List<String> rightsHolders) {
        this.rightsHolders = rightsHolders;
    }
}
