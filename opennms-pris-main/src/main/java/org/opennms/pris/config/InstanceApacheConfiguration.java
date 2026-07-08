/*
 * Copyright 2014 The OpenNMS Group, Inc.
 * SPDX-License-Identifier: GPL-3.0-only
 * Created by Ronny Trommer <ronny@opennms.com>
 */

package org.opennms.pris.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.opennms.pris.api.InstanceConfiguration;

public class InstanceApacheConfiguration extends AbstractApacheConfiguration implements InstanceConfiguration {

    private static org.apache.commons.configuration2.Configuration createConfig(final Path basePath) {
        final Path path = basePath.resolve("requisition.properties");

        // Raise wrapped file not found exception if the config file does not exist
        if (!Files.exists(path)) {
            throw new RuntimeException("Config file not found: " + path);
        }

        // Load the properties file. A fresh configuration is built on every
        // request (see ConfigManager#getInstanceConfig), so the file is always
        // re-read and no reloading strategy is needed. The comma list delimiter
        // preserves Commons Configuration 1.x behaviour for multi-valued keys
        // (e.g. "script.file = a.groovy, b.groovy" -> a list of paths).
        try {
            return new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(new Parameters().properties()
                            .setFile(path.toFile())
                            .setListDelimiterHandler(new DefaultListDelimiterHandler(','))
                            .setThrowExceptionOnMissing(true))
                    .getConfiguration();

        } catch (final ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private final Path basePath;

    private final String instance;

    public InstanceApacheConfiguration(final Path basePath,
                                       final String instance) {
        this(basePath,
             instance,
             createConfig(basePath));
    }

    private InstanceApacheConfiguration(final Path basePath,
                                        final String instance,
                                        final org.apache.commons.configuration2.Configuration config) {
        super(config);

        this.basePath = basePath;
        this.instance = instance;
    }

    @Override
    public Path getBasePath() {
        return this.basePath;
    }

    @Override
    public String getInstanceIdentifier() {
        return this.instance;
    }

    @Override
    public InstanceConfiguration subset(final String prefix) {
        return new InstanceApacheConfiguration(this.basePath,
                                               this.instance,
                                               this.getConfig().subset(prefix));
    }
}
