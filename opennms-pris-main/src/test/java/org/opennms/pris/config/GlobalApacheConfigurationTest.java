/*
 * Copyright 2026 The OpenNMS Group, Inc.
 * SPDX-License-Identifier: GPL-3.0-only
 * Created by Ronny Trommer <ronny@opennms.com>
 */

package org.opennms.pris.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.opennms.pris.api.Configuration;

/**
 * Verifies the global configuration composes system properties with the
 * {@code global.properties} file after the Commons Configuration 2.x migration.
 */
public class GlobalApacheConfigurationTest {

    private Path dir;

    @Before
    public void setUp() throws IOException {
        dir = Files.createTempDirectory("pris-global-");
        dir.toFile().deleteOnExit();
    }

    @Test
    public void readsFileAndSystemProperties() throws IOException {
        Files.write(dir.resolve("global.properties"),
                    "foo = bar".getBytes(StandardCharsets.UTF_8));
        System.setProperty("pris.test.sysprop", "sysval");
        try {
            final Configuration config = new GlobalApacheConfiguration(dir);

            assertThat(config.getString("foo"), is("bar"));
            assertThat(config.getString("pris.test.sysprop"), is("sysval"));
        } finally {
            System.clearProperty("pris.test.sysprop");
        }
    }
}
