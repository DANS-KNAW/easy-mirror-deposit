/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.knaw.dans.easy.mirror;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import nl.knaw.dans.easy.mirror.core.FileServiceImpl;
import nl.knaw.dans.easy.mirror.core.MirroringService;
import nl.knaw.dans.easy.mirror.core.TransferItemMetadataReaderImpl;
import nl.knaw.dans.easy.mirror.core.config.Inbox;
import nl.knaw.dans.easy.mirror.health.InboxHealth;

import java.util.concurrent.ExecutorService;

public class EasyMirrorDepositApplication extends Application<EasyMirrorDepositConfiguration> {

    public static void main(final String[] args) throws Exception {
        new EasyMirrorDepositApplication().run(args);
    }

    @Override
    public String getName() {
        return "Easy Mirror Deposit";
    }

    @Override
    public void run(final EasyMirrorDepositConfiguration configuration, final Environment environment) {
        final ExecutorService taskExecutor = configuration.getTaskQueue().build(environment);
        final MirroringService mirroringService = configuration.getMirroringService()
            .build(taskExecutor, new TransferItemMetadataReaderImpl(environment.getObjectMapper(), new FileServiceImpl()));
        environment.lifecycle().manage(mirroringService);
        for (Inbox inbox : configuration.getMirroringService().getInboxes()) {
            environment.healthChecks().register(String.format("Inbox-%s", inbox.getPath().toString()), new InboxHealth(inbox.getPath()));
        }

    }

}
