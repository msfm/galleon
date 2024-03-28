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
package org.jboss.galleon.cli.cmd.state.pkg;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.StateActivators;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-included-package", description = HelpDescriptions.REMOVE_INCLUDED_PACKAGE, activator = StateActivators.ProvisionedPackageCommandActivator.class)
public class StateRemoveIncludedPackageCommand extends AbstractProvisionedPackageCommand {
    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.pkg.core.CoreStateRemoveIncludedPackageCommand";
    }
    @Override
    public boolean isIncludedPackages() {
        return true;
    }
}