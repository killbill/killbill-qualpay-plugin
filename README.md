killbill-qualpay-plugin
=======================

Plugin to use [Qualpay](https://www.qualpay.com) as a gateway.

Release builds are available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kill-bill.billing.plugin.java%22%20AND%20a%3A%22qualpay-plugin%22) with coordinates `org.kill-bill.billing.plugin.java:qualpay-plugin`.

Kill Bill compatibility
-----------------------

| Plugin version | Kill Bill version |
| -------------: | ----------------: |
| 0.0.y          | 0.20.z            |

Requirements
------------

The plugin needs a database. The latest version of the schema can be found [here](https://github.com/killbill/killbill-qualpay-plugin/blob/master/src/main/resources/ddl.sql).

Configuration
-------------

The following properties are required:

* org.killbill.billing.plugin.qualpay.apiKey: Qualpay API security key

The following properties are optional:

* org.killbill.billing.plugin.qualpay.merchantId: Qualpay merchant id

Migration
---------

The Qualpay customer id (used in the Vault APIs) is expected as the custom field `QUALPAY_CUSTOMER_ID` on the account. When migrating to Kill Bill, you need to first manually set this custom field for your existing customers.

To migrate payment methods, trigger a [refresh](https://killbill.github.io/slate/#account-refresh-account-payment-methods) for each account in your Vault.

Development
-----------

To build the plugin, you need to setup Maven to use the [GitHub Package Registry](https://help.github.com/en/articles/configuring-apache-maven-for-use-with-github-package-registry) https://maven.pkg.github.com/killbill/qualpay-java-client.

To install a development version:

```
kpm install_java_plugin qualpay --from-source-file=target/killbill-qualpay-plugin-0.0.1-SNAPSHOT.jar  --destination=/var/tmp/bundles
```
