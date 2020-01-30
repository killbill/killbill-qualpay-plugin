killbill-qualpay-plugin
=======================

Plugin to use [Qualpay](https://www.qualpay.com) as a gateway.

Release builds are available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kill-bill.billing.plugin.java%22%20AND%20a%3A%22qualpay-plugin%22) with coordinates `org.kill-bill.billing.plugin.java:qualpay-plugin`.

Kill Bill compatibility
-----------------------

| Plugin version | Kill Bill version |
| -------------: | ----------------: |
| 0.0.y          | 0.22.z            |

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

To avoid sending the full PAN to Kill Bill, your front-end application should tokenize first the card in Qualpay using either the [Add a Customer](https://www.qualpay.com/developer/api/customer-vault/add-a-customer) Vault API (recommended) or [Tokenize Card](https://www.qualpay.com/developer/api/payment-gateway/tokenize-card) API. The Vault API is recommended as some functionality (such as retrieving the payment method details from Qualpay or deleting the card in Qualpay) won't be available when using the Gateway API.

When using the Vault API, Qualpay will return a customer id that needs to be set as a the custom field `QUALPAY_CUSTOMER_ID` on the Kill Bill account. You can then trigger a [refresh](https://killbill.github.io/slate/#account-refresh-account-payment-methods) to sync back all cards in the Vault to Kill Bill.

When using the Payment Gateway API, you need to [add the payment method](https://killbill.github.io/slate/#account-add-a-payment-method) directly by passing the card id as the `card_id` plugin property.

Migration
---------

To migrate to Kill Bill, you first need to create one Kill Bill account for each of your customers and follow the tokenization step(s) above for each account.

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
