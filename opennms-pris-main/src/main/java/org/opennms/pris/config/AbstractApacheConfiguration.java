/*
 * Copyright 2014 The OpenNMS Group, Inc.
 * SPDX-License-Identifier: GPL-3.0-only
 * Created by Ronny Trommer <ronny@opennms.com>
 */

package org.opennms.pris.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.pris.api.Configuration;

public abstract class AbstractApacheConfiguration implements Configuration {
    
    private final org.apache.commons.configuration2.Configuration config;

    public org.apache.commons.configuration2.Configuration getConfiguration() {
        return config;
    }
    
    protected AbstractApacheConfiguration(final org.apache.commons.configuration2.Configuration config) {
        this.config = config;
    }

    protected org.apache.commons.configuration2.Configuration getConfig() {
        return config;
    }

    @Override
    public boolean isEmpty() {
        return this.config.isEmpty();
    }
    
    @Override
    public boolean containsKey(final String key) {
        return this.config.containsKey(key);
    }

    @Override
    public String getString(final String key) {
        if (!this.config.containsKey(key)) {
            return this.config.getString(key);
        }

        return String.join(",", this.config.getStringArray(key));
    }

    @Override
    public String getString(final String key,
                            final String defaultValue) {
        if (!this.config.containsKey(key)) {
            return defaultValue;
        }

        return String.join(",", this.config.getStringArray(key));
    }

    @Override
    public String[] getStringArray(final String key) {
        return this.config.getStringArray(key);
    }

    @Override
    public Path getPath(final String key) {
        Path path = Paths
                .get(this.config.getString(key));
        
        return this.getBasePath().resolve(path);
    }

    @Override
    public List<Path> getPaths(final String key) {
        List<Path> paths = new ArrayList<>();
        String[] pathStrings = this.config.getStringArray(key);
        
        for (String pathString : pathStrings) {
            paths.add(this.getBasePath().resolve(pathString));
        }
        
        return paths;
    }    
    
    @Override
    public Path getPath(final String key,
                        final Path defaultValue) {
        if (!this.config.containsKey(key)) {
            return defaultValue;
        }

        return this.getPath(key);
    }

    @Override
    public boolean getBoolean(final String key) {
        return this.config.getBoolean(key);
    }

    @Override
    public boolean getBoolean(final String key,
                              final boolean defaultValue) {
        return this.config.getBoolean(key,
                                      defaultValue);
    }

    @Override
    public int getInt(final String key) {
        return this.config.getInt(key);
    }

    @Override
    public int getInt(final String key,
                      final int defaultValue) {
        return this.config.getInt(key,
                                  defaultValue);
    }
    
    @Override
    public Iterator<String> getKeys() {
        return config.getKeys();
    }
    
    @Override
    public void addProperty(String key, String string) {
        config.addProperty(key, string);
    }
}
