/*
 * Copyright 2026 The OpenNMS Group, Inc.
 * SPDX-License-Identifier: GPL-3.0-only
 * Created by Ronny Trommer <ronny@opennms.com>
 */

package org.opennms.opennms.pris.plugins.script.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.pris.api.MockInstanceConfiguration;
import org.opennms.pris.model.Requisition;

/**
 * Exercises {@link ScriptManager#execute} end-to-end through the JSR-223 path
 * for the bundled default engines on the current JDK. Guards the script plugin
 * against a scripting-engine regression (the plugin previously had no tests).
 */
public class ScriptManagerTest {

    private static final String INSTANCE = "test-instance";

    /** Writes the script to a temp file and builds a config selecting {@code lang}. */
    private MockInstanceConfiguration config(final String lang, final String script, final String suffix) throws IOException {
        final Path file = Files.createTempFile("pris-script-", suffix);
        file.toFile().deleteOnExit();
        Files.write(file, script.getBytes(StandardCharsets.UTF_8));

        final MockInstanceConfiguration config = new MockInstanceConfiguration(INSTANCE);
        config.set("lang", lang);
        config.set("file", Collections.singletonList(file));
        return config;
    }

    @Test
    public void evaluatesGroovyAndReturnsRequisition() throws IOException, ScriptException {
        final String script = String.join("\n",
                "import org.opennms.pris.model.Requisition",
                "import org.opennms.pris.model.RequisitionNode",
                "logger.info('groovy sees instance={}', instance)",
                "Requisition r = new Requisition()",
                "r.setForeignSource(instance)",
                "RequisitionNode n = new RequisitionNode()",
                "n.setForeignId('1')",
                "n.setNodeLabel('node-1')",
                "r.getNodes().add(n)",
                "return r");

        final Requisition r = (Requisition) ScriptManager.execute(config("groovy", script, ".groovy"), Collections.emptyMap());

        assertThat(r.getForeignSource(), is(INSTANCE));
        assertThat(r.getNodes().size(), is(1));
        assertThat(r.getNodes().get(0).getForeignId(), is("1"));
    }

    @Test
    public void evaluatesBeanShellAndReturnsRequisition() throws IOException, ScriptException {
        final String script = String.join("\n",
                "import org.opennms.pris.model.Requisition;",
                "import org.opennms.pris.model.RequisitionNode;",
                "logger.info(\"beanshell sees instance=\" + instance);",
                "Requisition r = new Requisition();",
                "r.setForeignSource(instance);",
                "RequisitionNode n = new RequisitionNode();",
                "n.setForeignId(\"1\");",
                "n.setNodeLabel(\"node-1\");",
                "r.getNodes().add(n);",
                "return r;");

        final Requisition r = (Requisition) ScriptManager.execute(config("beanshell", script, ".bsh"), Collections.emptyMap());

        assertThat(r.getForeignSource(), is(INSTANCE));
        assertThat(r.getNodes().size(), is(1));
        assertThat(r.getNodes().get(0).getForeignId(), is("1"));
    }

    @Test
    public void unknownLanguageFailsLoudly() throws IOException {
        final MockInstanceConfiguration config = config("no-such-lang", "return null", ".txt");
        try {
            ScriptManager.execute(config, Collections.emptyMap());
            Assert.fail("expected a RuntimeException when no engine matches the lang");
        } catch (final ScriptException ex) {
            Assert.fail("expected a RuntimeException, got ScriptException: " + ex.getMessage());
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage(), is("Script engine implementation not found"));
        }
    }
}
