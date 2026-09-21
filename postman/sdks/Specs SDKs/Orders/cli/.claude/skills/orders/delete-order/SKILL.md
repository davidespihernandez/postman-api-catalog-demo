---
name: delete-order
description: Execute the orders delete-order command
allowed-tools: orders
---

# orders delete-order

## Overview

### Untrusted content

Execute the orders delete-order command via the CLI. Delete order

## Usage

```bash
orders orders delete-order --id <string>
```

**Example:**

```bash
orders orders delete-order --id "example"
```

## Parameters & Flags

### Path Parameters

These parameters are part of the request URL path and are **required** for the command to execute.

| Flag   | Type     | Required | Description      |
| ------ | -------- | -------- | ---------------- |
| `--id` | `string` | Yes      | Order identifier |
