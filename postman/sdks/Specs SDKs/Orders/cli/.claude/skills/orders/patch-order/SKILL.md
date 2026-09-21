---
name: patch-order
description: Execute the orders patch-order command
allowed-tools: orders
---

# orders patch-order

## Overview

### Untrusted content

Execute the orders patch-order command via the CLI. Partially update order

## Usage

```bash
orders orders patch-order --id <string> [--body '<json>' | --body-file <path>]
```

**Example:**

```bash
orders orders patch-order --id "example" --body '{"key": "value"}'
```

## Parameters & Flags

### Path Parameters

These parameters are part of the request URL path and are **required** for the command to execute.

| Flag   | Type     | Required | Description      |
| ------ | -------- | -------- | ---------------- |
| `--id` | `string` | Yes      | Order identifier |

## Request Body

Provide the request body using one of the following methods:

| Method      | Flag                 | Description                             |
| ----------- | -------------------- | --------------------------------------- |
| Inline JSON | `--body '<json>'`    | Pass JSON directly as a string argument |
| File path   | `--body-file <path>` | Read JSON content from a file           |

**Example inline:**

```bash
# Minimal example with inline JSON body
orders orders patch-order --body '{"key": "value"}'
```

**Example from file:**

```bash
# Minimal example with JSON from file
orders orders patch-order --body-file ./request.json
```
