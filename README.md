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



Development
-----------

```
kpm install_java_plugin qualpay --from-source-file=target/killbill-qualpay-plugin-0.0.1-SNAPSHOT.jar  --destination=/var/tmp/bundles
```
