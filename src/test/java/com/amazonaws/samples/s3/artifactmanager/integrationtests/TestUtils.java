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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.iterable.S3Versions;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;

public class TestUtils {

  public static void deleteS3Bucket(AmazonS3 s3Client, String bucketName) {

    // delete objects or mark objects as deleted if versioned bucket
    for (S3ObjectSummary summary : S3Objects.inBucket(s3Client, bucketName)) {
      s3Client.deleteObject(bucketName, summary.getKey());
    }

    // delete versions
    for (S3VersionSummary summary : S3Versions.inBucket(s3Client, bucketName)) {
      s3Client.deleteVersion(bucketName, summary.getKey(), summary.getVersionId());
    }

    // delete bucket 
    s3Client.deleteBucket(bucketName);
  }

  public static File createSampleFile(String fileName) throws IOException {
    File file = File.createTempFile(fileName, ".txt");
    file.deleteOnExit();
    Writer writer = new OutputStreamWriter(new FileOutputStream(file));
    writer.write("unit and integration security testing AWS applications\n");
    writer.close();
    return file;
  }
}
