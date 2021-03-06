/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.wsclient.unmarshallers;

import org.json.simple.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

// Godin: we will use raw types here, because typically JSONObject passed as an argument
@SuppressWarnings("rawtypes")
public final class JsonUtils {

  private JsonUtils() {
    // only static methods
  }

  public static String getString(Map obj, String field) {
    Object value = obj.get(field);
    if (value instanceof String || value instanceof Number) {
      return value.toString();
    }
    return null;
  }

  public static Integer getInteger(Map obj, String field) {
    Object value = obj.get(field);
    if (value != null) {
      return ((Long) value).intValue();
    }
    return null;
  }

  public static Boolean getBoolean(Map obj, String field) {
    Object value = obj.get(field);
    if (value != null) {
      return (Boolean) value;
    }
    return null;
  }

  public static Long getLong(Map obj, String field) {
    Object value = obj.get(field);
    if (value != null) {
      return (Long) value;
    }
    return null;
  }

  public static Double getDouble(Map obj, String field) {
    Object value = obj.get(field);
    if (value != null) {
      return (Double) value;
    }
    return null;
  }

  /**
   * @since 2.5
   */
  public static JSONArray getArray(Map obj, String field) {
    return (JSONArray) obj.get(field);
  }

  public static Date getDateTime(Map obj, String field) {
    return parseDate(obj, field, "yyyy-MM-dd'T'HH:mm:ssZ");
  }

  public static Date getDate(Map obj, String field) {
    return parseDate(obj, field, "yyyy-MM-dd");
  }

  private static Date parseDate(Map obj, String field, String format) {
    String value = getString(obj, field);
    if (value != null) {
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(value);

      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

}
