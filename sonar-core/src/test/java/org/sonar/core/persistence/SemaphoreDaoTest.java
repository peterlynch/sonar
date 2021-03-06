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

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fest.assertions.Assertions.assertThat;

public class SemaphoreDaoTest extends AbstractDaoTestCase {

  @Test
  public void create_and_acquire_semaphore() throws Exception {
    SemaphoreDao dao = new SemaphoreDao(getMyBatis());
    assertThat(dao.acquire("foo", 60)).isTrue();

    Semaphore semaphore = selectSemaphore("foo");
    assertThat(semaphore).isNotNull();
    assertThat(semaphore.name).isEqualTo("foo");
    assertThat(isRecent(semaphore.createdAt, 60)).isTrue();
    assertThat(isRecent(semaphore.updatedAt, 60)).isTrue();
    assertThat(isRecent(semaphore.lockedAt, 60)).isTrue();

    dao.release("foo");
    assertThat(selectSemaphore("foo")).isNull();
  }

  @Test
  public void fail_to_acquire_locked_semaphore() throws Exception {
    setupData("old_semaphore");
    SemaphoreDao dao = new SemaphoreDao(getMyBatis());
    assertThat(dao.acquire("foo", Integer.MAX_VALUE)).isFalse();

    Semaphore semaphore = selectSemaphore("foo");
    assertThat(semaphore).isNotNull();
    assertThat(semaphore.name).isEqualTo("foo");
    assertThat(isRecent(semaphore.createdAt, 60)).isFalse();
    assertThat(isRecent(semaphore.updatedAt, 60)).isFalse();
    assertThat(isRecent(semaphore.lockedAt, 60)).isFalse();
  }

  @Test
  public void acquire_long_locked_semaphore() throws Exception {
    setupData("old_semaphore");
    SemaphoreDao dao = new SemaphoreDao(getMyBatis());
    assertThat(dao.acquire("foo", 60)).isTrue();

    Semaphore semaphore = selectSemaphore("foo");
    assertThat(semaphore).isNotNull();
    assertThat(semaphore.name).isEqualTo("foo");
    assertThat(isRecent(semaphore.createdAt, 60)).isFalse();
    assertThat(isRecent(semaphore.updatedAt, 60)).isTrue();
    assertThat(isRecent(semaphore.lockedAt, 60)).isTrue();
  }

  @Test
  public void test_concurrent_locks() throws Exception {
    SemaphoreDao dao = new SemaphoreDao(getMyBatis());

    for (int tests = 0; tests < 5; tests++) {
      dao.release("my-lock");
      int size = 5;
      CyclicBarrier barrier = new CyclicBarrier(size);
      CountDownLatch latch = new CountDownLatch(size);

      AtomicInteger locks = new AtomicInteger(0);
      for (int i = 0; i < size; i++) {
        new Runner(dao, locks, barrier, latch).start();
      }
      latch.await();

      // semaphore was locked only 1 time
      assertThat(locks.get()).isEqualTo(1);
    }
  }

  private Semaphore selectSemaphore(String name) throws Exception {
    Connection connection = getConnection();
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.prepareStatement("SELECT * FROM semaphores WHERE name='" + name + "'");
      rs = statement.executeQuery();
      if (rs.next()) {
        return new Semaphore(rs.getString("name"), rs.getTimestamp("created_at"), rs.getTimestamp("updated_at"), rs.getTimestamp("locked_at"));
      }
      return null;
    } finally {
      DatabaseUtils.closeQuietly(rs);
      DatabaseUtils.closeQuietly(statement);
      DatabaseUtils.closeQuietly(connection);
    }
  }

  private static class Semaphore {
    String name;
    Date createdAt, updatedAt, lockedAt;

    private Semaphore(String name, Timestamp createdAt, Timestamp updatedAt, Timestamp lockedAt) {
      this.name = name;
      this.createdAt = createdAt;
      this.updatedAt = updatedAt;
      this.lockedAt = lockedAt;
    }
  }

  private static boolean isRecent(Date date, int durationInSeconds) {
    Date now = new Date();
    return date.before(now) && DateUtils.addSeconds(date, durationInSeconds).after(now);
  }

  private static class Runner extends Thread {
    SemaphoreDao dao;
    AtomicInteger locks;
    CountDownLatch latch;
    CyclicBarrier barrier;

    Runner(SemaphoreDao dao, AtomicInteger atomicSeq, CyclicBarrier barrier, CountDownLatch latch) {
      this.dao = dao;
      this.locks = atomicSeq;
      this.latch = latch;
      this.barrier = barrier;
    }

    public void run() {
      try {
        barrier.await();
        for (int i = 0; i < 100; i++) {
          if (dao.acquire("my-lock", 60 * 5)) {
            locks.incrementAndGet();
          }
        }
        latch.countDown();

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
