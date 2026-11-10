package com.ventrax.alerting.notifications.infrastructure.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * PostgreSQL advisory lock used to keep a single dispatcher across replicas.
 *
 * <p>PostgreSQL advisory locks are session-scoped, so the lock is acquired and
 * released on the same dedicated connection, which is held during the whole
 * dispatch cycle. If the lock cannot be acquired, the cycle is skipped.</p>
 */
@Slf4j
@Component
public class DistributedDispatchLock {

    private static final long LOCK_ID = 92220001L;

    private final DataSource dataSource;

    public DistributedDispatchLock(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<LockHandle> tryAcquire() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            try (var statement = connection.prepareStatement("select pg_try_advisory_lock(?)")) {
                statement.setLong(1, LOCK_ID);
                try (var result = statement.executeQuery()) {
                    if (result.next() && result.getBoolean(1)) {
                        return Optional.of(new LockHandle(connection));
                    }
                }
            }
            closeQuietly(connection);
            return Optional.empty();
        } catch (SQLException exception) {
            log.warn("Distributed dispatch lock unavailable: {}", exception.getMessage());
            closeQuietly(connection);
            return Optional.empty();
        }
    }

    private static void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException exception) {
            log.debug("Could not close advisory lock connection: {}", exception.getMessage());
        }
    }

    public static final class LockHandle implements AutoCloseable {

        private final Connection connection;

        private LockHandle(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void close() {
            try (var statement = connection.prepareStatement("select pg_advisory_unlock(?)")) {
                statement.setLong(1, LOCK_ID);
                statement.executeQuery();
            } catch (SQLException exception) {
                log.debug("Could not release advisory lock: {}", exception.getMessage());
            } finally {
                closeQuietly(connection);
            }
        }
    }
}
