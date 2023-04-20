# Synchronization between Wise and BuchhaltungsButler

This project aims to automatically sync [Wise](https://wise.com/) multi-currency accounts to German bookkeeping
software [BuchhaltungsButler](https://buchhaltungsbutler.de/) ("BHB").

## Disclaimer

**I won't take any responsibility for bugs, data loss or problems caused by this software!**

I've carefully made this software, and I'm using it for my own bookkeeping.

But I won't take responsibility if anything bad happens, for example if data is destroyed in BuchhaltungsButler.
This software is only reading data from Wise and creating new transactions in BuchhaltungsButler.
No data is intentionally modified or deleted from any of the systems.

If you'd like to be careful, I suggest the following:

- Create and use a new test account of BuchhaltungsButler (tested).
- Use the Wise sandbox environment (untested). Using your production Wise account should be fine, though. The Wise API
  is only used to fetch data.
- Use the software with sandbox and test account to verify that it's doing exactly what you want.
- Keep different config files for your test and production setups.

## Features

- Supports Wise multi-currency accounts
- Synchronize Wise transactions to BuchhaltungsButler transactions
- Synchronize Wise transactions for a given month or a range of months, e.g. "2023-01" for the first month or "2023-01
  to 2023-03" for the first quarter of 2023.
- Creates separate BuchhaltungsButler transactions for fees charged by Wise.
- Creates separate BuchhaltungsButler transactions for Wise cashback.
- Tags BuchhaltungsButler transactions with the Wise reference number to avoid duplicate synchronization. Multiple
  invocations of program **will not** create multiple transactions in BuchhaltungsButler for the same Wise transaction.

## Building The Software

Gradle is used a build system.
I recommend to install Java 17 or later on the host system.

```bash
./gradlew clean build
```

This will create JAR files in `./build/libs`.

JAR file `./build/libs/buchhaltungsbutler-wise-sync-all.jar` can be used as a stand-alone file to execute
with `java -jar`.
Please see below for usage instructions.

## Using the Software

At first, you need to create the configuration file (see below).

The synced accounts are defined in the configuration with object property `bhb_accounts`.
The key is the (DATEV) id of the BuchhaltungsButler bank account, e.g. `1200` or `1210`.
The value is the currency of your Wise account.

For example, use these settings to sync your Wise USD account to BuchhaltungsButler bank account `1200` and your Wise
EUR account to BuchhaltungsButler bank account `1210`:

```yaml
bhb_accounts:
  1200: USD
  1210: EUR
```

### Creating the Configuration File

The configuration is stored in a [YAML](https://yaml.org/) file.
You can use [sample-config.yaml](https://github.com/jansorg/buchhaltungsbutler-wise-sync/blob/main/sample-config.yaml)
as a template.

The Kotlin source
file [SyncConfig.kt](https://github.com/jansorg/buchhaltungsbutler-wise-sync/blob/main/src/main/kotlin/dev/ja/sync/model/SyncConfig.kt)
contains all available settings and should be easy to read.

#### Creating the Wise Private/Public Key Pair

Some calls to the Wise API are SCA protected, which means that they need "Strong Customer Authentication".
To enable SCA, you have to create a private/public key pair and upload the public part to Wise.

Please follow
the [instructions on Wise.com](https://api-docs.wise.com/features/strong-customer-authentication-2fa/personal-token-sca)
to create the key pair.

In a nutshell:

1. Create the key pair:
   ```bash
   openssl genrsa -out private.pem 2048
   openssl rsa -pubout -in private.pem -out public.pem
   ```
2. Upload the public key to Wise:
   > The public keys management page can be accessed via the "Manage public keys" button under the API tokens section of
   your Wise account settings.
3. Copy the private key into your config.yaml file

### Synchronization

General usage:

```bash
java -jar ./build/libs/buchhaltungsbutler-wise-sync-all.jar config.yaml year-month-first [year-month-last]
```

#### Examples

##### Synchronize all data of January 2023

```bash
java -jar ./build/libs/buchhaltungsbutler-wise-sync-all.jar config.yaml 2023-01
```

##### Synchronize the first quarter of 2023

```bash
java -jar ./build/libs/buchhaltungsbutler-wise-sync-all.jar config.yaml 2023-01 2023-03
```

#### Read-Only Mode

With read-only mode, it's possible to test the synchronization without making changes to your BuchhaltungsButler data.

Wise.com is always accessed read-only.
No modifications to your Wise data are made, even in the regular mode of synchronization.

```bash
java -jar ./build/libs/buchhaltungsbutler-wise-sync-all.jar --read-only config.yaml 2023-01 2023-06
```

## Limitations of This Software

Unfortunately, the API of BuchhaltungsButler is pretty weak.

It does not provide a way to retrieve the exchange rate for newly created transactions.

This makes it impossible to automatically add "postings" to the newly created transactions, e.g. to mark the
transactions with the needed DATEV account it.

## Kotlin client to Wise and BuchhaltungsButler APIs

This software includes a partial implementation of API clients to the APIs of Wise and BuchhaltungsButler.
The implementation is based on Kotlin and is using ktor for the HTTP abstraction.
The package structure and sources should hopefully be self-explanatory.

## License

This software is licensed under [GNU Affero General Public License](https://www.gnu.org/licenses/agpl-3.0.en.html).
