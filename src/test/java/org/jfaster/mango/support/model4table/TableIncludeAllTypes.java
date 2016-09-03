/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.support.model4table;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

/**
 * @author ash
 */
public class TableIncludeAllTypes {

  private int id;

  private byte navByte;
  private short navShort;
  private int navInteger;
  private long navLong;
  private float navFloat;
  private double navDouble;
  private boolean navBollean;
  private char navChar;

  private Byte objByte;
  private Short objShort;
  private Integer objInteger;
  private Long objLong;
  private Float objFloat;
  private Double objDouble;
  private Boolean objBollean;
  private Character objChar;

  private String objString;
  private BigDecimal objBigDecimal;
  private BigInteger objBigInteger;
  private byte[] navBytes;
  private Byte[] objBytes;

  private Date objDate;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public byte getNavByte() {
    return navByte;
  }

  public void setNavByte(byte navByte) {
    this.navByte = navByte;
  }

  public short getNavShort() {
    return navShort;
  }

  public void setNavShort(short navShort) {
    this.navShort = navShort;
  }

  public int getNavInteger() {
    return navInteger;
  }

  public void setNavInteger(int navInteger) {
    this.navInteger = navInteger;
  }

  public long getNavLong() {
    return navLong;
  }

  public void setNavLong(long navLong) {
    this.navLong = navLong;
  }

  public float getNavFloat() {
    return navFloat;
  }

  public void setNavFloat(float navFloat) {
    this.navFloat = navFloat;
  }

  public double getNavDouble() {
    return navDouble;
  }

  public void setNavDouble(double navDouble) {
    this.navDouble = navDouble;
  }

  public boolean isNavBollean() {
    return navBollean;
  }

  public void setNavBollean(boolean navBollean) {
    this.navBollean = navBollean;
  }

  public char getNavChar() {
    return navChar;
  }

  public void setNavChar(char navChar) {
    this.navChar = navChar;
  }

  public Byte getObjByte() {
    return objByte;
  }

  public void setObjByte(Byte objByte) {
    this.objByte = objByte;
  }

  public Short getObjShort() {
    return objShort;
  }

  public void setObjShort(Short objShort) {
    this.objShort = objShort;
  }

  public Integer getObjInteger() {
    return objInteger;
  }

  public void setObjInteger(Integer objInteger) {
    this.objInteger = objInteger;
  }

  public Long getObjLong() {
    return objLong;
  }

  public void setObjLong(Long objLong) {
    this.objLong = objLong;
  }

  public Float getObjFloat() {
    return objFloat;
  }

  public void setObjFloat(Float objFloat) {
    this.objFloat = objFloat;
  }

  public Double getObjDouble() {
    return objDouble;
  }

  public void setObjDouble(Double objDouble) {
    this.objDouble = objDouble;
  }

  public Boolean getObjBollean() {
    return objBollean;
  }

  public void setObjBollean(Boolean objBollean) {
    this.objBollean = objBollean;
  }

  public Character getObjChar() {
    return objChar;
  }

  public void setObjChar(Character objChar) {
    this.objChar = objChar;
  }

  public String getObjString() {
    return objString;
  }

  public void setObjString(String objString) {
    this.objString = objString;
  }

  public BigDecimal getObjBigDecimal() {
    return objBigDecimal;
  }

  public void setObjBigDecimal(BigDecimal objBigDecimal) {
    this.objBigDecimal = objBigDecimal;
  }

  public BigInteger getObjBigInteger() {
    return objBigInteger;
  }

  public void setObjBigInteger(BigInteger objBigInteger) {
    this.objBigInteger = objBigInteger;
  }

  public byte[] getNavBytes() {
    return navBytes;
  }

  public void setNavBytes(byte[] navBytes) {
    this.navBytes = navBytes;
  }

  public Byte[] getObjBytes() {
    return objBytes;
  }

  public void setObjBytes(Byte[] objBytes) {
    this.objBytes = objBytes;
  }

  public Date getObjDate() {
    return objDate;
  }

  public void setObjDate(Date objDate) {
    if (objDate != null) {
      objDate = new Date(objDate.getTime() / 1000 * 1000);
    }
    this.objDate = objDate;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final TableIncludeAllTypes other = (TableIncludeAllTypes) obj;
    return Objects.equal(this.id, other.id)
        && Objects.equal(this.navByte, other.navByte)
        && Objects.equal(this.navShort, other.navShort)
        && Objects.equal(this.navInteger, other.navInteger)
        && Objects.equal(this.navLong, other.navLong)
        && Objects.equal(this.navFloat, other.navFloat)
        && Objects.equal(this.navDouble, other.navDouble)
        && Objects.equal(this.navBollean, other.navBollean)
        && Objects.equal(this.navChar, other.navChar)
        && Objects.equal(this.objByte, other.objByte)
        && Objects.equal(this.objShort, other.objShort)
        && Objects.equal(this.objInteger, other.objInteger)
        && Objects.equal(this.objLong, other.objLong)
        && Objects.equal(this.objFloat, other.objFloat)
        && Objects.equal(this.objDouble, other.objDouble)
        && Objects.equal(this.objBollean, other.objBollean)
        && Objects.equal(this.objChar, other.objChar)
        && Objects.equal(this.objString, other.objString)
        && Objects.equal(this.objBigDecimal, other.objBigDecimal)
        && Objects.equal(this.objBigInteger, other.objBigInteger)
        && Objects.equal(Arrays.toString(this.navBytes), Arrays.toString(other.navBytes))
        && Objects.equal(Arrays.toString(this.objBytes), Arrays.toString(other.objBytes))
        && Objects.equal(this.objDate, other.objDate);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).
        add("id", id).
        add("navByte", navByte).
        add("navShort", navShort).
        add("navInteger", navInteger).
        add("navLong", navLong).
        add("navFloat", navFloat).
        add("navDouble", navDouble).
        add("navBollean", navBollean).
        add("navChar", navChar).
        add("objByte", objByte).
        add("objShort", objShort).
        add("objInteger", objInteger).
        add("objLong", objLong).
        add("objFloat", objFloat).
        add("objDouble", objDouble).
        add("objBollean", objBollean).
        add("objChar", objChar).
        add("objString", objString).
        add("objBigDecimal", objBigDecimal).
        add("objBigInteger", objBigInteger).
        add("navBytes", navBytes).
        add("objBytes", objBytes).
        add("objDate", objDate).toString();
  }
}
