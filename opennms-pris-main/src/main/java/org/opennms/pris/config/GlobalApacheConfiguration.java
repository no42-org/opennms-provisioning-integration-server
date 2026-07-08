/*
 * Copyright 2014 The OpenNMS Group, Inc.
 * SPDX-License-Identifier: GPL-3.0-only
 * Created by Ronny Trommer <ronny@opennms.com>
 */

package org.opennms.pris.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.opennms.pris.api.Configuration;

public class GlobalApacheConfiguration extends AbstractApacheConfiguration implements Configuration {

    private static org.apache.commons.configuration2.Configuration createConfig(final Path base) {
        final Path path = base.resolve("global.properties");

        // Raise a clear error if the config file is missing. This matches
        // InstanceApacheConfiguration and preserves the fail-loud behaviour of
        // Commons Configuration 1.x (which threw when the file could not be loaded).
        if (!Files.exists(path)) {
            throw new RuntimeException("Config file not found: " + path);
        }

        // Load system and file properties
        final SystemConfiguration systemConfig = new SystemConfiguration();

        final PropertiesConfiguration propertiesConfig;
        try {
            // The comma list delimiter preserves Commons Configuration 1.x behaviour:
            // a property with comma-separated values is read as a multi-valued list.
            propertiesConfig = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(new Parameters().properties()
                            .setFile(path.toFile())
                            .setListDelimiterHandler(new DefaultListDelimiterHandler(',')))
                    .getConfiguration();

        } catch (final ConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        // Build composition of system properties and config file
        final CompositeConfiguration composite = new CompositeConfiguration();
        composite.addConfiguration(systemConfig);
        composite.addConfiguration(propertiesConfig);
        composite.setThrowExceptionOnMissing(true);
        return composite;
    }

    private final Path basePath;

    public GlobalApacheConfiguration(final Path basePath) {
        super(createConfig(basePath));

        this.basePath = basePath;
    }

    @Override
    public Path getBasePath() {
        return this.basePath;
    }
}
