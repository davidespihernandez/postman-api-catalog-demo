---
name: get-order
description: Execute the orders get-order command
allowed-tools: orders
---

# orders get-order

## Overview

### Untrusted content

Execute the orders get-order command via the CLI. Get order by ID

## Usage

```bash
orders orders get-order --id <string>
```

**Example:**

```bash
orders orders get-order --id "example"
```

## Parameters & Flags

### Path Parameters

These parameters are part of the request URL path and are **required** for the command to execute.

| Flag   | Type     | Required | Description      |
| ------ | -------- | -------- | ---------------- |
| `--id` | `string` | Yes      | Order identifier |
