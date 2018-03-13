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

package com.logistimo.returns.entity.values;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Mohan Raja
 */
@Embeddable
public class GeoLocation {

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Column(name = "geo_accuracy")
  private Double geoAccuracy;

  @Column(name = "geo_error")
  private String geoError;

  @SuppressWarnings("unused")
  private GeoLocation() {
    //Added to support JPA
  }

  public GeoLocation(Double latitude, Double longitude, Double geoAccuracy, String geoError) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.geoAccuracy = geoAccuracy;
    this.geoError = geoError;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public Double getGeoAccuracy() {
    return geoAccuracy;
  }

  public String getGeoError() {
    return geoError;
  }

}