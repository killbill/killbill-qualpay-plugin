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
* org.killbill.billing.plugin.qualpay.merchantId: Qualpay merchant id

The following properties are optional:

* org.killbill.billing.plugin.qualpay.baseUrl: Qualpay endpoint (default: `https://api-test.qualpay.com`)
* org.killbill.billing.plugin.qualpay.connectionTimeout: connect timeout in millis for the Qualpay client (default: `30000`)
* org.killbill.billing.plugin.qualpay.readTimeout: read timeout in mills for the Qualpay timeout (default: `60000`)
* org.killbill.billing.plugin.qualpay.chargeDescription: statement description (default: `Kill Bill charge`)
* org.killbill.billing.plugin.qualpay.kbUsername: plugin username to communicate with Kill Bill (default: `admin`)
* org.killbill.billing.plugin.qualpay.kbPassword: plugin password to communicate with Kill Bill (default `password`)

Tokenization
------------

To avoid sending the full PAN to Kill Bill, your front-end application should tokenize first the card in Qualpay using the [Add a Customer](https://www.qualpay.com/developer/api/customer-vault/add-a-customer) Vault API.

Qualpay will return a customer id that needs to be set as a the custom field `QUALPAY_CUSTOMER_ID` on the Kill Bill account. You can then trigger a [refresh](https://killbill.github.io/slate/#account-refresh-account-payment-methods) to sync back all card in the Vault to Kill Bill.

Migration
---------

When migrating to Kill Bill, you need to create one Kill Bill account for each of your customers and set the `QUALPAY_CUSTOMER_ID` custom field. Similar to the tokenization step above, you must then trigger a refresh of the payment methods for each account.

Development
-----------

To build the plugin, you need to setup Maven to use the [GitHub Package Registry](https://help.github.com/en/articles/configuring-apache-maven-for-use-with-github-package-registry) https://maven.pkg.github.com/killbill/qualpay-java-client.

Deployment
----------

To install the plugin:

```
kpm install_java_plugin qualpay --from-source-file=target/killbill-qualpay-plugin-0.0.1-SNAPSHOT.jar --destination=/var/tmp/bundles
```

On AWS or in our Docker images, the destination should be `/var/lib/killbill/bundles`.
