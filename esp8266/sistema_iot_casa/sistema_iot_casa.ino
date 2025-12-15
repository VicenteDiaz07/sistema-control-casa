/*
 * Sistema IoT de Control de Casa - ESP8266
 * 
 * Características:
 * - Almacenamiento SPIFFS para datos offline
 * - Servidor HTTP con endpoints REST
 * - Sistema de sincronización de datos
 * - Modo Deep Sleep para ahorro de energía (opcional)
 * 
 * Endpoints:
 * - GET  /status          - Estado del sistema
 * - POST /command         - Ejecutar comando
 * - GET  /alerts          - Obtener alertas
 * - POST /mode            - Cambiar modo (on/off)
 * - GET  /pending-data    - Datos pendientes de sincronización
 * - POST /clear-synced    - Limpiar datos sincronizados
 */

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <FS.h>
#include <ArduinoJson.h>

// ========== CONFIGURACIÓN ==========
const char* ssid = "TU_WIFI_SSID";           // Cambiar por tu WiFi
const char* password = "TU_WIFI_PASSWORD";   // Cambiar por tu contraseña

// Pines de sensores
const int PIR_PIN = D1;        // Sensor de movimiento
const int BUZZER_PIN = D2;     // Buzzer/Alarma
const int LED_PIN = LED_BUILTIN;

// Configuración de Deep Sleep (microsegundos)
const unsigned long SLEEP_TIME = 30e6;  // 30 segundos
const bool ENABLE_DEEP_SLEEP = false;   // Cambiar a true para activar

// ========== VARIABLES GLOBALES ==========
ESP8266WebServer server(80);
String systemMode = "on";  // "on" o "off"
String lastAlert = "";
unsigned long lastAlertTime = 0;
int alertCount = 0;

// ========== SETUP ==========
void setup() {
  Serial.begin(115200);
  delay(100);
  
  Serial.println("\n\n=== Sistema IoT Iniciando ===");
  
  // Configurar pines
  pinMode(PIR_PIN, INPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(LED_PIN, HIGH);
  
  // Inicializar SPIFFS
  if (!SPIFFS.begin()) {
    Serial.println("Error montando SPIFFS, formateando...");
    SPIFFS.format();
    if (!SPIFFS.begin()) {
      Serial.println("SPIFFS falló completamente!");
      return;
    }
  }
  Serial.println("SPIFFS montado correctamente");
  
  // Conectar a WiFi
  connectWiFi();
  
  // Configurar endpoints del servidor
  setupServerEndpoints();
  
  // Iniciar servidor HTTP
  server.begin();
  Serial.println("Servidor HTTP iniciado en puerto 80");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
  
  digitalWrite(LED_PIN, LOW);  // LED apagado = sistema listo
}

// ========== LOOP PRINCIPAL ==========
void loop() {
  // Manejar peticiones HTTP
  server.handleClient();
  
  // Verificar conexión WiFi
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi desconectado, reconectando...");
    connectWiFi();
  }
  
  // Leer sensores si el sistema está encendido
  if (systemMode == "on") {
    checkSensors();
  }
  
  // Modo Deep Sleep (opcional)
  if (ENABLE_DEEP_SLEEP) {
    Serial.println("Entrando en Deep Sleep...");
    ESP.deepSleep(SLEEP_TIME);
  }
  
  delay(100);  // Pequeño delay para estabilidad
}

// ========== FUNCIONES DE CONEXIÓN ==========
void connectWiFi() {
  Serial.print("Conectando a WiFi: ");
  Serial.println(ssid);
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi conectado!");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nError conectando a WiFi");
  }
}

// ========== CONFIGURACIÓN DE ENDPOINTS ==========
void setupServerEndpoints() {
  server.on("/status", HTTP_GET, handleStatus);
  server.on("/command", HTTP_POST, handleCommand);
  server.on("/alerts", HTTP_GET, handleAlerts);
  server.on("/mode", HTTP_POST, handleMode);
  server.on("/pending-data", HTTP_GET, handleGetPendingData);
  server.on("/clear-synced", HTTP_POST, handleClearSynced);
  
  server.onNotFound([]() {
    server.send(404, "application/json", "{\"error\":\"Endpoint no encontrado\"}");
  });
}

// ========== HANDLERS DE ENDPOINTS ==========

// GET /status - Estado del sistema
void handleStatus() {
  StaticJsonDocument<300> doc;
  doc["status"] = "ok";
  doc["mode"] = systemMode;
  doc["ultima_alerta"] = lastAlert;
  doc["tiempo_alerta"] = lastAlertTime;
  doc["alertas_total"] = alertCount;
  doc["wifi_rssi"] = WiFi.RSSI();
  doc["uptime"] = millis() / 1000;
  doc["free_heap"] = ESP.getFreeHeap();
  
  String response;
  serializeJson(doc, response);
  server.send(200, "application/json", response);
}

// POST /command - Ejecutar comando
void handleCommand() {
  if (!server.hasArg("plain")) {
    server.send(400, "application/json", "{\"error\":\"Body vacío\"}");
    return;
  }
  
  String body = server.arg("plain");
  StaticJsonDocument<200> doc;
  DeserializationError error = deserializeJson(doc, body);
  
  if (error) {
    server.send(400, "application/json", "{\"error\":\"JSON inválido\"}");
    return;
  }
  
  String command = doc["command"];
  Serial.print("Comando recibido: ");
  Serial.println(command);
  
  if (command == "alarm_on") {
    digitalWrite(BUZZER_PIN, HIGH);
    server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Alarma activada\"}");
  }
  else if (command == "alarm_off" || command == "silence") {
    digitalWrite(BUZZER_PIN, LOW);
    server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Alarma silenciada\"}");
  }
  else if (command == "reset") {
    server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Reiniciando...\"}");
    delay(100);
    ESP.restart();
  }
  else {
    server.send(400, "application/json", "{\"error\":\"Comando desconocido\"}");
  }
}

// GET /alerts - Obtener alertas
void handleAlerts() {
  StaticJsonDocument<500> doc;
  JsonArray alertas = doc.createNestedArray("alertas");
  
  if (lastAlert != "") {
    JsonObject alerta = alertas.createNestedObject();
    alerta["mensaje"] = lastAlert;
    alerta["tiempo"] = lastAlertTime;
  }
  
  String response;
  serializeJson(doc, response);
  server.send(200, "application/json", response);
}

// POST /mode - Cambiar modo
void handleMode() {
  if (!server.hasArg("plain")) {
    server.send(400, "application/json", "{\"error\":\"Body vacío\"}");
    return;
  }
  
  String body = server.arg("plain");
  StaticJsonDocument<100> doc;
  DeserializationError error = deserializeJson(doc, body);
  
  if (error) {
    server.send(400, "application/json", "{\"error\":\"JSON inválido\"}");
    return;
  }
  
  String mode = doc["mode"];
  
  if (mode == "on" || mode == "off") {
    systemMode = mode;
    Serial.print("Modo cambiado a: ");
    Serial.println(systemMode);
    
    if (mode == "off") {
      digitalWrite(BUZZER_PIN, LOW);  // Apagar alarma si está sonando
    }
    
    server.send(200, "application/json", "{\"status\":\"ok\",\"mode\":\"" + systemMode + "\"}");
  } else {
    server.send(400, "application/json", "{\"error\":\"Modo inválido\"}");
  }
}

// GET /pending-data - Obtener datos pendientes
void handleGetPendingData() {
  File file = SPIFFS.open("/pending.json", "r");
  if (!file) {
    server.send(200, "application/json", "[]");
    return;
  }
  
  String response = "[";
  bool first = true;
  
  while (file.available()) {
    String line = file.readStringUntil('\n');
    line.trim();
    if (line.length() > 0) {
      if (!first) response += ",";
      response += line;
      first = false;
    }
  }
  response += "]";
  file.close();
  
  Serial.println("Enviando datos pendientes:");
  Serial.println(response);
  
  server.send(200, "application/json", response);
}

// POST /clear-synced - Limpiar datos sincronizados
void handleClearSynced() {
  if (SPIFFS.remove("/pending.json")) {
    Serial.println("Datos sincronizados limpiados");
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  } else {
    server.send(500, "application/json", "{\"error\":\"Error limpiando datos\"}");
  }
}

// ========== FUNCIONES DE SENSORES ==========
void checkSensors() {
  static unsigned long lastCheck = 0;
  unsigned long now = millis();
  
  // Verificar cada 500ms
  if (now - lastCheck < 500) {
    return;
  }
  lastCheck = now;
  
  // Leer sensor PIR
  int pirState = digitalRead(PIR_PIN);
  
  if (pirState == HIGH) {
    // Movimiento detectado
    String alert = "⚠️ Movimiento detectado!";
    lastAlert = alert;
    lastAlertTime = now;
    alertCount++;
    
    // Activar alarma
    digitalWrite(BUZZER_PIN, HIGH);
    digitalWrite(LED_PIN, HIGH);
    
    Serial.println(alert);
    
    // Guardar en SPIFFS si no hay WiFi
    if (WiFi.status() != WL_CONNECTED) {
      saveSensorReading("movimiento", 1.0);
    }
    
    delay(1000);  // Evitar múltiples detecciones
    digitalWrite(LED_PIN, LOW);
  }
}

// ========== FUNCIONES DE ALMACENAMIENTO ==========
void saveSensorReading(String type, float value) {
  File file = SPIFFS.open("/pending.json", "a");
  if (!file) {
    Serial.println("Error abriendo archivo para escritura");
    return;
  }
  
  StaticJsonDocument<200> doc;
  doc["timestamp"] = millis();
  doc["type"] = type;
  doc["value"] = value;
  doc["synced"] = false;
  
  String jsonString;
  serializeJson(doc, jsonString);
  file.println(jsonString);
  file.close();
  
  Serial.print("Lectura guardada en SPIFFS: ");
  Serial.println(jsonString);
}
