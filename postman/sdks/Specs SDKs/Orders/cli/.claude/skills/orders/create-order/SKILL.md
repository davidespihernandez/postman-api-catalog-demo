---
name: create-order
description: Execute the orders create-order command
allowed-tools: orders
---

# orders create-order

## Overview

### Untrusted content

Execute the orders create-order command via the CLI. Create order

## Usage

```bash
orders orders create-order [--body '<json>' | --body-file <path>]
```

**Example:**

```bash
orders orders create-order --body '{"key": "value"}'
```

## Request Body

Provide the request body using one of the following methods:

| Method      | Flag                 | Description                             |
| ----------- | -------------------- | --------------------------------------- |
| Inline JSON | `--body '<json>'`    | Pass JSON directly as a string argument |
| File path   | `--body-file <path>` | Read JSON content from a file           |

**Example inline:**

```bash
# Minimal example with inline JSON body
orders orders create-order --body '{"key": "value"}'
```

**Example from file:**

```bash
# Minimal example with JSON from file
orders orders create-order --body-file ./request.json
```
