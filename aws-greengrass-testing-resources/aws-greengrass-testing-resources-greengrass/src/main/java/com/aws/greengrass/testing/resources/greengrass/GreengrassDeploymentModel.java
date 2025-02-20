/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.testing.resources.greengrass;

import com.aws.greengrass.testing.api.model.TestingModel;
import com.aws.greengrass.testing.resources.AWSResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;
import software.amazon.awssdk.services.greengrassv2.GreengrassV2Client;
import software.amazon.awssdk.services.greengrassv2.model.CancelDeploymentRequest;
import software.amazon.awssdk.services.greengrassv2.model.DeleteCoreDeviceRequest;
import software.amazon.awssdk.services.greengrassv2.model.GreengrassV2Exception;
import software.amazon.awssdk.services.greengrassv2.model.ValidationException;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

@TestingModel
@Value.Immutable
interface GreengrassDeploymentModel extends AWSResource<GreengrassV2Client> {
    Logger LOGGER = LogManager.getLogger(GreengrassDeployment.class);

    String deploymentId();

    @Nullable
    List<String> thingNames();

    @Override
    default void remove(GreengrassV2Client client) {
        Optional.ofNullable(thingNames()).ifPresent(thingNames -> {
            thingNames.forEach(thingName -> {
                try {
                    client.deleteCoreDevice(DeleteCoreDeviceRequest.builder()
                            .coreDeviceThingName(thingName)
                            .build());
                } catch (GreengrassV2Exception e) {
                    LOGGER.warn("Could not delete core device {}", thingName);
                }
            });
        });
        try {
            client.cancelDeployment(CancelDeploymentRequest.builder()
                    .deploymentId(deploymentId())
                    .build());
        } catch (ValidationException ve) {
            LOGGER.warn("Could not cancel deployment {}", deploymentId());
        }
    }
}
