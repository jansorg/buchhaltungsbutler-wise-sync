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

## Using The Software

### Creating The Configuration File

### Regular Synchronization

```bash
java -jar bhb-wise-sync config.json year-month [year-month]
```

### Read-Only Mode

With read-only mode, it's possible to test the synchronization without making changes to your BHB data.

Wise.com is always accessed read-only.
No modifications to your Wise data are made

```bash
java -jar bhb-wise-sync --read-only config.json 2023-01
```

## Limitations of This Software

Unfortunately, the API of BHB is pretty weak.
It does not provide a way to retrieve the exchange rate for newly created transactions.

## License

This software is licensed under [GNU Affero General Public License](https://www.gnu.org/licenses/agpl-3.0.en.html).