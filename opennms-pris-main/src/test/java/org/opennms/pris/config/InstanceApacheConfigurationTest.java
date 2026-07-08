/*
 * Copyright 2026 The OpenNMS Group, Inc.
 * SPDX-License-Identifier: GPL-3.0-only
 * Created by Ronny Trommer <ronny@opennms.com>
 */

package org.opennms.pris.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.pris.api.InstanceConfiguration;

/**
 * Covers the Commons Configuration 1.x -> 2.x migration for the per-requisition
 * configuration, in particular that comma-separated values are still read as a
 * multi-valued list (the behaviour Commons Configuration 2.x disables by default).
 */
public class InstanceApacheConfigurationTest {

    private Path dir;

    @Before
    public void setUp() throws IOException {
        dir = Files.createTempDirectory("pris-cfg-");
        dir.toFile().deleteOnExit();
    }

    private void writeConfig(final String content) throws IOException {
        Files.write(dir.resolve("requisition.properties"), content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void readsScalarsAndTypes() throws IOException {
        writeConfig(String.join("\n",
                "source = xls",
                "count = 5",
                "flag = true"));
        final InstanceConfiguration config = new InstanceApacheConfiguration(dir, "myInstance");

        assertThat(config.getInstanceIdentifier(), is("myInstance"));
        assertThat(config.getString("source"), is("xls"));
        assertThat(config.getInt("count"), is(5));
        assertThat(config.getBoolean("flag"), is(true));
        assertThat(config.containsKey("source"), is(true));
        assertThat(config.containsKey("missing"), is(false));
        assertThat(config.getString("missing", "fallback"), is("fallback"));
    }

    @Test
    public void splitsCommaSeparatedValuesIntoAList() throws IOException {
        writeConfig("script.file = a.groovy, b.groovy, c.groovy");
        final InstanceConfiguration config = new InstanceApacheConfiguration(dir, "myInstance");

        assertThat(config.getStringArray("script.file"),
                   arrayContaining("a.groovy", "b.groovy", "c.groovy"));

        final List<Path> paths = config.getPaths("script.file");
        assertThat(paths, contains(dir.resolve("a.groovy"),
                                   dir.resolve("b.groovy"),
                                   dir.resolve("c.groovy")));
    }

    @Test
    public void missingConfigFileFailsLoudly() {
        try {
            new InstanceApacheConfiguration(dir, "myInstance"); // no requisition.properties written
            Assert.fail("expected RuntimeException for missing requisition.properties");
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage().startsWith("Config file not found:"), is(true));
        }
    }

    @Test
    public void splitsCommaListsThroughSubset() throws IOException {
        // The production path: RequisitionGenerator reads config.subset("script").getPaths("file").
        writeConfig("script.file = a.groovy, b.groovy");
        final InstanceConfiguration config = new InstanceApacheConfiguration(dir, "myInstance");

        final InstanceConfiguration subset = config.subset("script");
        assertThat(subset.getStringArray("file"), arrayContaining("a.groovy", "b.groovy"));
        assertThat(subset.getPaths("file"), contains(dir.resolve("a.groovy"), dir.resolve("b.groovy")));
    }

    @Test
    public void interpolatesLocalReferences() throws IOException {
        writeConfig(String.join("\n",
                "base = /opt/pris",
                "full = ${base}/requisitions"));
        final InstanceConfiguration config = new InstanceApacheConfiguration(dir, "myInstance");

        assertThat(config.getString("full"), is("/opt/pris/requisitions"));
    }
}
