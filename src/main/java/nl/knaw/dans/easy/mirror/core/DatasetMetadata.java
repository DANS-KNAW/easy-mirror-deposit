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

public class DatasetMetadata {
    private String title;
    private String description;
    private String creator;
    private String created;
    private String published;
    private String available;
    private String audience;
    private String accessRights;
    private String rightsHolder;

    public DatasetMetadata() {
    }

    public DatasetMetadata(String jsonLdString) {
        DocumentContext context = JsonPath.parse(jsonLdString);
        title = context.read("$['ore:describes']['Title']");
        description = context.read("$['ore:describes']['citation:Description']['dsDescription:Text']");
        creator = context.read("$['ore:describes']['Author']['author:Name']");
        published = context.read("$['ore:describes']['schema:datePublished']");
        try {
            created = context.read("$['ore:describes']['citation:Date Produced']");
        } catch (PathNotFoundException e) {
            created = published; // Since Date Produced is not required in the Data Station, fall back to publication dates
        }
        available = "2100-01-01"; // Arbitrary date in the future. This will never be available throught EASY.
        audience = extractNarcisIdFromUri(context.read("$['ore:describes']['dansRelationMetadata:Audience']['@id']"));
        accessRights = "NO_ACCESS"; // No access through EASY
        rightsHolder = context.read("$['ore:describes']['dansRights:Rights Holder']");
    }

    private static String extractNarcisIdFromUri(String narcisTermUri) {
        String[] components = narcisTermUri.split("/");
        return components[components.length - 1];
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
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

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public String getRightsHolder() {
        return rightsHolder;
    }

    public void setRightsHolder(String rightsHolder) {
        this.rightsHolder = rightsHolder;
    }
}
