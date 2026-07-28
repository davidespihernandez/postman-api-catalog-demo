#!/usr/bin/env node
/**
 * MQTT → Postman webhook bridge for the notifications async demo.
 * Subscribes to the demo topic and POSTs notification.processed to NOTIFICATION_WEBHOOK_URL.
 *
 * Run during SE setup (keep running in a terminal before/during the demo):
 *   ./demo.sh mqtt-bridge
 */
import mqtt from "mqtt";
import { readFileSync, existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");

function loadEnvFile(path) {
  if (!existsSync(path)) {
    return;
  }
  for (const line of readFileSync(path, "utf8").split("\n")) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) {
      continue;
    }
    const eq = trimmed.indexOf("=");
    if (eq === -1) {
      continue;
    }
    const key = trimmed.slice(0, eq);
    let value = trimmed.slice(eq + 1);
    if (
      (value.startsWith('"') && value.endsWith('"')) ||
      (value.startsWith("'") && value.endsWith("'"))
    ) {
      value = value.slice(1, -1);
    }
    if (process.env[key] == null) {
      process.env[key] = value;
    }
  }
}

loadEnvFile(join(ROOT, ".env"));

const brokerUrl = process.env.MQTT_BROKER_URL ?? "mqtt://broker.hivemq.com:1883";
const topic = process.env.MQTT_TOPIC ?? "postman-api-catalog-demo/notifications";
const webhookUrl = process.env.NOTIFICATION_WEBHOOK_URL;

if (!webhookUrl) {
  console.error("Error: NOTIFICATION_WEBHOOK_URL is not set in .env");
  process.exit(1);
}

console.log(`MQTT bridge: ${brokerUrl}`);
console.log(`Topic: ${topic}`);
console.log(`Webhook: ${webhookUrl}`);
console.log("Waiting for messages… (Ctrl+C to stop)\n");

const client = mqtt.connect(brokerUrl);

client.on("connect", () => {
  client.subscribe(topic, { qos: 1 }, (err) => {
    if (err) {
      console.error("Subscribe failed:", err.message);
      process.exit(1);
    }
    console.log("Subscribed.");
  });
});

client.on("message", async (_topic, payload) => {
  let notification;
  try {
    notification = JSON.parse(payload.toString());
  } catch {
    console.error("Ignored non-JSON message");
    return;
  }

  const body = {
    event: "notification.processed",
    occurredAt: new Date().toISOString(),
    notification,
  };

  try {
    const response = await fetch(webhookUrl, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(body),
    });
    console.log(
      `Forwarded ${notification.id ?? "message"} → webhook (${response.status})`,
    );
  } catch (err) {
    console.error("Webhook POST failed:", err.message);
  }
});

client.on("error", (err) => {
  console.error("MQTT error:", err.message);
});
