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

package com.amazonaws.samples.s3.artifactmanager.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.regions.Regions;
import com.amazonaws.samples.s3.artifactmanager.S3ArtifactManager;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;

@RunWith(MockitoJUnitRunner.class)
public class S3ArtifactManagerUnitTest {

  private static final String AES256 = "AES256";
  private static final String VERSION_ID = "1234567890";
  private Regions awsRegion = Regions.US_EAST_1; // can be any region as unit
                                                 // tests use mocks objects
  private String s3Bucket = "MyBucket";
  private String s3Key = "MyKey";
  private File file = new File("myfile.txt");

  @Mock
  private AmazonS3 s3Client;

  // Object under test
  private S3ArtifactManager s3ArtifactManager;

  @Before
  public void setup() {
    s3ArtifactManager = new S3ArtifactManager(s3Client, awsRegion);
    prepareMocks();
  }

  private void prepareMocks() {
    PutObjectResult putObjectResult = mock(PutObjectResult.class);
    when(putObjectResult.getVersionId()).thenReturn(VERSION_ID);
    when(s3Client.putObject(Matchers.any(PutObjectRequest.class))).thenReturn(putObjectResult);
  }

  @Test
  public void testUploadWillEnableVersioningOnExistingS3Bucket() {

    // set Mock behavior
    when(s3Client.doesBucketExist(s3Bucket)).thenReturn(true);

    // call object under test
    String versionId = s3ArtifactManager.upload(s3Bucket, s3Key, file);

    // assert versionID is the expected value
    assertEquals("VersionId returned is incorrect", VERSION_ID, versionId);

    // assert that a new bucket has NOT been created
    verify(s3Client, never()).createBucket(s3Bucket);

    // capture BucketVersioningConfigurationReques object
    ArgumentCaptor<SetBucketVersioningConfigurationRequest> bucketVerConfigRequestCaptor =
        ArgumentCaptor.forClass(SetBucketVersioningConfigurationRequest.class);
    verify(s3Client).setBucketVersioningConfiguration(bucketVerConfigRequestCaptor.capture());

    // assert versioning is set on the bucket
    SetBucketVersioningConfigurationRequest bucketVerConfigRequest =
        bucketVerConfigRequestCaptor.getValue();
    assertEquals("Versioning of S3 bucket could not be verified",
        BucketVersioningConfiguration.ENABLED,
        bucketVerConfigRequest.getVersioningConfiguration().getStatus());
  }

  @Test
  public void testUploadWillCreateBucketWithVersioningEnabled() {

    // set Mock behavior
    when(s3Client.doesBucketExist(s3Bucket)).thenReturn(false);

    // call object under test
    String versionId = s3ArtifactManager.upload(s3Bucket, s3Key, file);

    // assert versionID is the expected value
    assertEquals("VersionId returned is incorrect", VERSION_ID, versionId);

    // assert that a new bucket HAS been created
    verify(s3Client, times(1)).createBucket(s3Bucket);

    // capture BucketVersioningConfigurationReques object
    ArgumentCaptor<SetBucketVersioningConfigurationRequest> bucketVerConfigRequestCaptor =
        ArgumentCaptor.forClass(SetBucketVersioningConfigurationRequest.class);
    verify(s3Client).setBucketVersioningConfiguration(bucketVerConfigRequestCaptor.capture());

    // assert versioning is set on the bucket
    SetBucketVersioningConfigurationRequest bucketVerConfigRequest =
        bucketVerConfigRequestCaptor.getValue();
    assertEquals("Versioning of S3 bucket could not be verified",
        BucketVersioningConfiguration.ENABLED,
        bucketVerConfigRequest.getVersioningConfiguration().getStatus());
  }

  @Test
  public void TestUploadAddsSSE_S3EncryptedObjectToBucket() {

    // call object under test
    s3ArtifactManager.upload(s3Bucket, s3Key, file);

    // capture putObjectRequest object
    ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(putObjectRequestCaptor.capture());
    PutObjectRequest putObjectRequest = putObjectRequestCaptor.getValue();

    // assert that there's no customer key provided as we're expecting
    // SSE-S3
    assertNull("A customer key was incorrectly used (SSE-C). SSE-S3 encryption expected instead.",
        putObjectRequest.getSSECustomerKey());

    // assert that the SSE-S3 'AES256' algorithm was set as part of the
    // request's metadata
    assertNotNull(
        "PutObjectRequest's metadata object must be non-null and enforce SSE-S3 encryption",
        putObjectRequest.getMetadata());
    assertEquals("Object has not been encrypted using SSE-S3 (AES256 encryption algorithm)", AES256,
        putObjectRequest.getMetadata().getSSEAlgorithm());

  }

}
