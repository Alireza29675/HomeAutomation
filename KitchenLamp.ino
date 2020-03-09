#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

#ifndef STASSID
#define STASSID "SSID OF WIFI"
#define STAPSK "WIFI PASSWORD"
#define UNIQUE_NAME "KITCHEN_LAMP"
#endif

const char *ssid = STASSID;
const char *password = STAPSK;
const String uniqueName = UNIQUE_NAME;

IPAddress ip(192, 168, 1, 100);     // set static ip
IPAddress gateway(192, 168, 1, 1);  // set gateway
IPAddress subnet(255, 255, 255, 0); // set subnet

ESP8266WebServer server(80);

const int RELAY_PIN = 13;
bool isOn = false;

void setup(void)
{
    pinMode(RELAY_PIN, OUTPUT);
    pinMode(LED_BUILTIN, OUTPUT);
    setRelayOff();

    Serial.begin(9600);
    WiFi.enableAP(0);
    WiFi.config(ip, gateway, subnet);
    WiFi.begin(ssid, password);

    // Wait for connection
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(150);
        blinkLED();
    }
    Serial.println("");
    Serial.print("Connected to ");
    Serial.println(ssid);
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());

    if (MDNS.begin("esp8266"))
    {
        Serial.println("MDNS responder started");
    }

    server.on("/", onRouteRoot);
    server.on("/on", onRouteOn);
    server.on("/off", onRouteOff);

    server.onNotFound(handleNotFound);

    server.begin();
    Serial.println("HTTP server started");
}

void loop(void)
{
    server.handleClient();
    MDNS.update();
}

// Methods

void setRelayOff()
{
    digitalWrite(RELAY_PIN, HIGH);
    isOn = false;
}
void setRelayOn()
{
    digitalWrite(RELAY_PIN, LOW);
    isOn = true;
}

void blinkLED()
{
    digitalWrite(LED_BUILTIN, LOW);
    delay(50);
    digitalWrite(LED_BUILTIN, HIGH);
}

String getInfo()
{
    String body = "{";
    body += "\"status\": 200, ";
    body += "\"name\": \"" + uniqueName + "\", ";
    body += "\"isOn\": ";
    if (isOn)
    {
        body += "true";
    }
    else
    {
        body += "false";
    }
    body += "}";
    return body;
}

// Routes
void onRouteRoot()
{
    blinkLED();
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", getInfo());
}
void onRouteOn()
{
    blinkLED();
    setRelayOn();
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", getInfo());
}
void onRouteOff()
{
    blinkLED();
    setRelayOff();
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", getInfo());
}
void handleNotFound()
{
    blinkLED();
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", "{ \"status\": 404 }");
}