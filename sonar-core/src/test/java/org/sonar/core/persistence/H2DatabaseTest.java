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
package org.sonar.core.persistence;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;

public class H2DatabaseTest {
  H2Database db = new H2Database();

  @Before
  public void startDb() {
    db.start();
  }

  @Before
  public void stopDb() {
    db.stop();
  }

  @Test
  public void shouldExecuteDdlAtStartup() throws SQLException {
    Connection connection = db.getDataSource().getConnection();

    int tables = 0;
    ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
    while (resultSet.next()) {
      tables++;
    }

    connection.close();

    assertThat(tables).isGreaterThan(30);
  }

  @Test
  public void shouldLimitThePoolSize() {
    assertThat(((BasicDataSource) db.getDataSource()).getMaxActive()).isEqualTo(2);
    assertThat(((BasicDataSource) db.getDataSource()).getMaxIdle()).isEqualTo(2);
  }
}