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
package org.jboss.galleon.cli.cmd.installation.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.Util;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.installation.AddUniverseCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreAddUniverseCommand extends AbstractInstallationCommand<AddUniverseCommand> {

    @Override
    public void execute(ProvisioningSession context, AddUniverseCommand command) throws CommandExecutionException {
        try {
            context.getUniverse().
                    addUniverse(Util.lookupInstallationDir(context.getCommandInvocation().getConfiguration().getAeshContext(),
                            command.getTargetDirArg() == null ? null : command.getTargetDirArg().toPath()),
                            command.getName(), command.getFactory(), command.getLocation());
        } catch (IOException | ProvisioningException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.addUniverseFailed(), ex);
        }
    }
}
