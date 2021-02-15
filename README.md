Foundation
==========

This repository houses system-agnostic [Scala](https://www.scala-lang.org/) types, concepts, and patterns used to support [Akka](https://akka.io/)-based applications.


## Overview

For the purposes of this document, the term "bundle" is used both in its [OSGi definition](https://docs.osgi.org/javadoc/r4v43/core/org/osgi/framework/Bundle.html) and as an informal description of a [Java JAR](https://docs.oracle.com/javase/tutorial/deployment/jar/).  

The "model" bundles can be used in an [OSGi container](https://www.osgi.org/) or in non-OSGi deployments.

The only bundle which requires an [OSGi container](https://www.osgi.org/) is "services-osgi" due to it being specifically designed for use in an [OSGi container](https://www.osgi.org/).


### models-core

This is the "lowest level" bundle and also the one with the most features.  All other Foundation bundles require it.  

It provides abstractions for supporting:

- [Domain Driven Design](https://www.dddcommunity.org/learning-ddd/what_is_ddd/)
- [Akka](https://akka.io/)
- [Typesafe Config](https://lightbend.github.io/config/latest/api/index.html)

As well as a bunch of other things which may be of interest.


### models-security

This bundle contains types related to *application-level* security concerns.  It does not address deployment security (such as network, OS, or JVM/container) nor management of same.  Instead, concerns ranging from being able to `Vault` [Scala](https://www.scala-lang.org/) types when interacting with external systems to creating random `ConfirmationCode`s are defined here.


### models-notification

This bundle reifies concepts related to producing notifications, such as sending email.  It requires both `models-core` and `models-security`.

Of the three "models" bundles, this one is the most specialized for its task.  However, the `EmailAddress` type may be of interest even if the notification workflow is not needed.


### services-osgi

Only those using an [OSGi container](https://www.osgi.org/) will be interested in this bundle.  In it are supporting types useful for defining and deploying bundles.  It uses the excellent [Domino](https://github.com/domino-osgi/domino) library (which is highly recommended even if you do not want to use `services-osgi`).


