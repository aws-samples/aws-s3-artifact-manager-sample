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

package com.amazonaws.samples.s3.artifactmanager.integrationtests;

import static com.amazonaws.samples.s3.artifactmanager.integrationtests.TestUtils.createSampleFile;
import static com.amazonaws.samples.s3.artifactmanager.integrationtests.TestUtils.deleteS3Bucket;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.samples.s3.artifactmanager.S3ArtifactManager;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class S3ArtifactManagerIntegrationTest {

   private final String AES256 = "AES256";
   private final AmazonS3 s3Client = new AmazonS3Client();
   private final Regions awsRegion = Regions.US_EAST_1;
   private final String s3Bucket = "bucket-" + UUID.randomUUID();
   private final String s3Key = String.format("file-%s-%s",
         new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()), UUID.randomUUID());

   // Object under test
   private S3ArtifactManager s3ArtifactManager;

   @Before
   public void setup() throws AmazonServiceException, AmazonClientException, IOException {
      s3ArtifactManager = new S3ArtifactManager(s3Client, awsRegion);
   }

   @After
   public void destroy() {
      deleteS3Bucket(s3Client, s3Bucket);
   }

   @Test
   public void testUploadWillCreateBucketWithVersioningEnabled() throws Exception {

      // call object under test
      String versionId = s3ArtifactManager.upload(s3Bucket, s3Key, createSampleFile(s3Key));

      // assert object's version exists in the bucket
      S3Object s3Object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
      assertEquals("Uploaded S3 object's versionId does not match expected value", versionId,
            s3Object.getObjectMetadata().getVersionId());

      // assert bucket versioning is enabled
      BucketVersioningConfiguration bucketConfig =
            s3Client.getBucketVersioningConfiguration(s3Bucket);
      Assert.assertEquals(BucketVersioningConfiguration.ENABLED, bucketConfig.getStatus());
   }

   @Test
   public void testUploadWillEnableVersioningOnExistingS3Bucket() throws Exception {

      // bucket must exist prior to uploading object for this test
      s3Client.createBucket(s3Bucket);

      // call object under test
      String versionId = s3ArtifactManager.upload(s3Bucket, s3Key, createSampleFile(s3Key));

      // assert object's version exists in the bucket
      S3Object s3Object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
      assertEquals("Uploaded S3 object's versionId does not match expected value", versionId,
            s3Object.getObjectMetadata().getVersionId());

      // assert bucket versioning is enabled
      BucketVersioningConfiguration bucketConfig =
            s3Client.getBucketVersioningConfiguration(s3Bucket);
      assertEquals(BucketVersioningConfiguration.ENABLED, bucketConfig.getStatus());

   }

   @Test
   public void testUploadAddsSSE_S3EncryptedObjectToBucket() throws Exception {

      // call object under test
      s3ArtifactManager.upload(s3Bucket, s3Key, createSampleFile(s3Key));

      // verify uploaded object is encrypted (SSE-S3)
      ObjectMetadata s3ObjectMetadata =
            s3Client.getObjectMetadata(new GetObjectMetadataRequest(s3Bucket, s3Key));
      assertEquals("Object has not been encrypted using SSE-S3 (AES256 encryption algorithm)",
            AES256, s3ObjectMetadata.getSSEAlgorithm());
   }

   // TODO:
   // Homework: Add a test to make sure multiple versions of the same S3
   // object have been uploaded correctly

}
