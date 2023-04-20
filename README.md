# Synchronization between Wise and Buchhaltungsbutler

This project aims to automatically sync [Wise](https://wise.com/) multi-currency accounts to German bookkeeping
software [Buchhaltungsbutler](https://buchhaltungsbutler.de/) ("BHB").

## Features

- Supports Wise multi-currency accounts
- Synchronize Wise transactions to Buchhaltungsbutler transactions
- Synchronize Wise transactions for a given month or multiple months
- Creates separate BHB transactions for fees charged by Wise
- Creates separate BHB transactions for Wise cashback
- Tags BHB transactions with the Wise reference number to avoid duplicate synchronization

## Building The Software

```bash
./gradlew clean build
```

JAR file `./build/libs/buchhaltungsbutler-wise-sync-all.jar` can be used as a stand-alone file to execute
with `java -jar`.
Please see below for usage instructions.

## Using the Software

At first, you need to create the configuration file.

The synced accounts are defined in the configuration with property `bhb_accounts`.
The key is the DATEV account it of the BuchhaltungsButler account, e.g. `1200` or `1210`.
The value is the currency of your Wise account.

For example, use these settings to sync your Wise USD account to BuchhaltungsButler account `1200` and your Wise EUR
account to
BuchhaltungsButler account `1210`:

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

### Regular Synchronization

```bash
java -jar ./build/libs/buchhaltungsbutler-wise-sync-all.jar config.yaml year-month-first [year-month-last]
```

For example, to synchronize all Wise transactions of the first 6 months of 2023, you need to run this command:

```bash
java -jar ./build/libs/buchhaltungsbutler-wise-sync-all.jar config.yaml 2023-01 2023-06
```

### Read-Only Mode

With read-only mode, it's possible to test the synchronization without making changes to your BHB data.

Wise.com is always accessed read-only.
No modifications to your Wise data are made, even in the regular mode of synchronization.

```bash
java -jar ./build/libs/buchhaltungsbutler-wise-sync-all.jar --read-only config.yaml 2023-01 2023-06
```

## Limitations of This Software

Unfortunately, the API of BHB is pretty weak.

It does not provide a way to retrieve the exchange rate for newly created transactions.

This makes it impossible to automatically add "postings" to the newly created transactions, e.g. to mark the
transactions with the needed DATEV account it.

## License

This software is licensed under [GNU Affero General Public License](https://www.gnu.org/licenses/agpl-3.0.en.html).
