Security Testing Your AWS Application
=====================================

This repository provides several sample unit and integration tests for a simple Java component called *S3ArtifactManager*. As this component's main functionality is to upload objects to Amazon S3 in a securely fashion (eg, uploaded objects are always versioned and encrypted) the main goal of the tests provided is to automatically assert the component's expected security behavior. Automated tests like these become even more important as code is refactored and/or evolved in order to make sure the expected behavior has not changed.

We hope that the set of tests provided can inspire AWS Java developers to build even richer test suites for their AWS Java components and applications, whether security-related or not.

Running the Tests
=================

The sample code provided in this repo is part of a blog post series published in the AWS Java Blog  entitled: "DevOps Meets Security - Security Testing Your AWS Application".

Please check out that publication for further details.

License
=======

The sample code is licensed under Apache 2.0.