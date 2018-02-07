/*
 * Copyright © 2018 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.exports.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by pratheeka on 29/01/18.
 */
public class RequestModel {

  @SerializedName(value = "TOKEN_DID")
  @Expose
  private String domainId;

  @SerializedName(value = "TOKEN_KID",alternate = "entity_id")
  @Expose
  private String entityId;

  @SerializedName(value = "TOKEN_START_TIME",alternate = "from_date")
  @Expose
  private String fromDate;

  @SerializedName(value = "TOKEN_END_TIME",alternate = "end_date")
  @Expose
  private String endDate;

  @SerializedName(value = "TOKEN_MTAG",alternate = "mtag")
  @Expose
  private String mtag;

  @SerializedName(value = "TOKEN_KTAG",alternate = "ktag")
  @Expose
  private String ktag;

  @SerializedName(value = "TOKEN_MID",alternate = "material_id")
  @Expose
  private String materialId;

  @SerializedName(value = "TOKEN_BID",alternate = "batch_id")
  @Expose
  private String batchId;

  @SerializedName(value = "TOKEN_REASON",alternate = "reason")
  @Expose
  private String reason;

  @SerializedName(value = "TOKEN_LKID",alternate = "linked_kid")
  @Expose
  private String linkedKioskId;

  @SerializedName(value = "TOKEN_TRANSACTION_TYPE",alternate = "transaction_type")
  @Expose
  private String transactionType;

  @SerializedName(value = "TOKEN_ATD",alternate = "atd")
  @Expose
  private String atd;

  @SerializedName(value = "TOKEN_FN", alternate = "first_name")
  @Expose
  private String firstName;

  @SerializedName(value = "TOKEN_MOBILE",alternate = "mobile_no")
  @Expose
  private String mobileNumber;

  @SerializedName(value = "TOKEN_ROLE",alternate = "role")
  @Expose
  private String role;

  @SerializedName(value = "TOKEN_USER_ACTIVE",alternate = "user_active")
  @Expose
  private String userActive;

  @SerializedName(value = "TOKEN_APP_VERSION",alternate = "app_version")
  @Expose
  private String appVersion;

  @SerializedName(value = "TOKEN_USER_NO_LOGIN",alternate = "user_no_log")
  @Expose
  private String showNeverLoggedInUsers;

  @SerializedName(value = "TOKEN_UTAG",alternate = "utag")
  @Expose
  private String userTag;

  @SerializedName(value = "TOKEN_MNAME",alternate = "mat_name")
  @Expose
  private String materialName;

  @SerializedName(value = "TOKEN_KNAME",alternate = "ent_name")
  @Expose
  private String entityName;

  @Expose(deserialize = false,serialize = false)
  private String module;

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  @Expose(deserialize = false,serialize = false)
  private String templateId;

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public Map<String, String> getTitles() {
    return titles;
  }

  public void setTitles(Map<String, String> titles) {
    this.titles = titles;
  }

  @Expose(deserialize = false,serialize = false)
  private Map<String, String> titles;

  public String getDomainId() {
    return domainId;
  }

  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getFromDate() {
    return fromDate;
  }

  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getMtag() {
    return mtag;
  }

  public void setMtag(String mtag) {
    this.mtag = mtag;
  }

  public String getKtag() {
    return ktag;
  }

  public void setKtag(String ktag) {
    this.ktag = ktag;
  }

  public String getMaterialId() {
    return materialId;
  }

  public String getAtd() {
    return atd;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getMobileNumber() {
    return mobileNumber;
  }

  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getUserActive() {
    return userActive;
  }

  public void setUserActive(String userActive) {
    this.userActive = userActive;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

  public String getShowNeverLoggedInUsers() {
    return showNeverLoggedInUsers;
  }

  public void setShowNeverLoggedInUsers(String showNeverLoggedInUsers) {
    this.showNeverLoggedInUsers = showNeverLoggedInUsers;
  }

  public String getUserTag() {
    return userTag;
  }

  public void setUserTag(String userTag) {
    this.userTag = userTag;
  }

  public void setMaterialId(String materialId) {
    this.materialId = materialId;
  }

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getLinkedKioskId() {
    return linkedKioskId;
  }

  public void setLinkedKioskId(String linkedKioskId) {
    this.linkedKioskId = linkedKioskId;
  }

  public String getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(String transactionType) {
    this.transactionType = transactionType;
  }

  public String isAtd() {
    return atd;
  }

  public void setAtd(String atd) {
    this.atd = atd;
  }

  public String getMaterialName() {
    return materialName;
  }

  public void setMaterialName(String materialName) {
    this.materialName = materialName;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }
}