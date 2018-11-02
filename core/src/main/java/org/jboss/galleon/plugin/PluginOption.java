/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.plugin;

import org.jboss.galleon.ProvisioningOption;

/**
 * Provisioning plugin option description.
 *
 * Deprecated in 3.0.0 in favor of ProvisioningOption
 *
 * @author Alexey Loubyansky
 */
@Deprecated
public class PluginOption extends ProvisioningOption {

    public static class Builder extends ProvisioningOption.Builder {

        protected Builder(String name) {
            super(name);
        }

        public Builder setRequired() {
            super.setRequired();
            return this;
        }

        public Builder hasNoValue() {
            return this;
        }

        public Builder setDefaultValue(String defaultValue) {
            super.setDefaultValue(defaultValue);
            return this;
        }

        public Builder addToValueSet(String value) {
            super.addToValueSet(value);
            return this;
        }

        public Builder setPersistent(boolean persistent) {
            super.setPersistent(persistent);
            return this;
        }

        @Override
        public PluginOption build() {
            return new PluginOption(this);
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    protected PluginOption(String name) {
        super(name);
    }

    protected PluginOption(Builder builder) {
        super(builder);
    }
}
