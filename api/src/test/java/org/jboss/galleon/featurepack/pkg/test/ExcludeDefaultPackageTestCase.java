/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.featurepack.pkg.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.api.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExcludeDefaultPackageTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1"))
            .newPackage("a", true)
                .writeContent("a.txt", "a")
                .getFeaturePack()
            .newPackage("b", true)
                .addDependency("c")
                .addDependency("d")
                .writeContent("b/b.txt", "b")
                .getFeaturePack()
            .newPackage("c", true)
                .writeContent("c/c/c.txt", "c")
                .getFeaturePack()
            .newPackage("d")
                .writeContent("c/d.txt", "d")
                .getFeaturePack();
    }

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return GalleonFeaturePackConfig
                .builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1").getLocation())
                .excludePackage("b")
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1"))
                        .addPackage("a")
                        .addPackage("c")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("a.txt", "a")
                .addFile("c/c/c.txt", "c")
                .build();
    }
}
