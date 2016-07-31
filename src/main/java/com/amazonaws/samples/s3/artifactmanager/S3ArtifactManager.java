/**
 * Copyright 2016-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
 * in compliance with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License
 **/

package com.amazonaws.samples.s3.artifactmanager;

import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;

public class S3ArtifactManager {

  private final AmazonS3 s3;

  public S3ArtifactManager(AmazonS3 s3, Regions awsRegion) {
    this.s3 = s3;
    this.s3.setRegion(Region.getRegion(awsRegion));
  }

  public String upload(String s3Bucket, String s3Key, File file)
      throws AmazonServiceException, AmazonClientException {

    if (!s3.doesBucketExist(s3Bucket)) {
      s3.createBucket(s3Bucket);
    }

    // enable bucket versioning
    SetBucketVersioningConfigurationRequest configRequest =
        new SetBucketVersioningConfigurationRequest(s3Bucket,
            new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED));
    s3.setBucketVersioningConfiguration(configRequest);

    // enable server-side encryption (SSE-S3)
    PutObjectRequest request = new PutObjectRequest(s3Bucket, s3Key, file);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
    request.setMetadata(objectMetadata);

    // upload object to S3
    PutObjectResult putObjectResult = s3.putObject(request);

    return putObjectResult.getVersionId();
  }

}
