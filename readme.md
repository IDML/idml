# Ingestion Data Mapping Language

[![Build Status](https://cloud.drone.io/api/badges/IDML/idml/status.svg)](https://cloud.drone.io/IDML/idml)


See http://idml.io/

## History

IDML has been developed and maintained by [DataSift](https://datasift.com) (now a [Meltwater](https://www.meltwater.com) company), where it's been used to deliver high throughput social media firehoses and to allow customers to onboard data easily.

## Releasing to Sonatype

If you're a member of the `io.idml` organisation on sonatype you can perform a release with these commands:

1. add `default-key $KEYID` to `~/.gnupg/gpg.conf` to specify the key to release with
2. `++ publishSigned`
3. `sonatypePrepare`
4. `sonatypeBundleUpload`
5. `sonatypeRelease`
6. `project tool` then `assembly` to build the tool jar
7. get the tool jar from `./tool/target/scala-2.12/idml-tool-$VERSION-assembly.jar`
8. create a release on github with the format `$VERSION: Title of Release`
9. Upload the assembly jar to that release and write release notes


## Special Thanks

This project has had many contributors before being open sourced, these include:

* Andi Miller
* Jon Davey
* Stuart Dallas
* Courtney Robinson
* James Bloomer
