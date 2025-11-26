/*
 * Sistema de Seguridad WiFi para Arduino
 * Compatible con la App Android de Control de Casa
 * 
 * Este código debe ser cargado en tu Arduino con módulo WiFi (ESP8266 o ESP32)
 * 
 * Endpoints disponibles:
 * - GET  /status  -> Obtiene el estado del sistema
 * - POST /command -> Envía comandos (alarm_on, alarm_off, silence)
 * - GET  /alerts  -> Obtiene las alertas recientes
 * - POST /mode    -> Cambia el modo (on/off)
 */

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

// ========== CONFIGURACIÓN WiFi ==========
const char* ssid = "TU_WIFI_SSID";        // Cambia esto por tu red WiFi
const char* password = "TU_WIFI_PASSWORD"; // Cambia esto por tu contraseña WiFi

// ========== CONFIGURACIÓN DE PINES ==========
const int SENSOR_PIN = D1;    // Pin del sensor de movimiento (PIR)
const int BUZZER_PIN = D2;    // Pin del buzzer/alarma
const int LED_PIN = D4;       // Pin del LED indicador

// ========== VARIABLES GLOBALES ==========
ESP8266WebServer server(80);
bool sistemaEncendido = true;
bool alarmaActiva = false;
String ultimaAlerta = "";
unsigned long tiempoUltimaAlerta = 0;

void setup() {
  Serial.begin(115200);
  
  // Configurar pines
  pinMode(SENSOR_PIN, INPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(LED_PIN, LOW);
  
  // Conectar a WiFi
  Serial.println();
  Serial.print("Conectando a WiFi: ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println();
  Serial.println("WiFi conectado!");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
  Serial.println("Usa esta IP en la app Android");
  
  // Configurar endpoints del servidor web
  server.on("/status", HTTP_GET, handleStatus);
  server.on("/command", HTTP_POST, handleCommand);
  server.on("/alerts", HTTP_GET, handleAlerts);
  server.on("/mode", HTTP_POST, handleMode);
  
  // Iniciar servidor
  server.begin();
  Serial.println("Servidor HTTP iniciado");
  
  // Parpadear LED para indicar que está listo
  for(int i = 0; i < 3; i++) {
    digitalWrite(LED_PIN, HIGH);
    delay(200);
    digitalWrite(LED_PIN, LOW);
    delay(200);
  }
}

void loop() {
  server.handleClient();
  
  // Si el sistema está encendido, monitorear el sensor
  if (sistemaEncendido) {
    int movimiento = digitalRead(SENSOR_PIN);
    
    if (movimiento == HIGH && !alarmaActiva) {
      // Movimiento detectado!
      alarmaActiva = true;
      ultimaAlerta = "Movimiento detectado";
      tiempoUltimaAlerta = millis();
      
      // Activar alarma
      digitalWrite(LED_PIN, HIGH);
      tone(BUZZER_PIN, 1000); // Tono de 1000 Hz
      
      Serial.println("¡ALERTA! Movimiento detectado");
    }
  } else {
    // Sistema apagado
    digitalWrite(LED_PIN, LOW);
    noTone(BUZZER_PIN);
    alarmaActiva = false;
  }
}

// ========== MANEJADORES DE ENDPOINTS ==========

void handleStatus() {
  String json = "{";
  json += "\"sistema\":\"" + String(sistemaEncendido ? "encendido" : "apagado") + "\",";
  json += "\"alarma\":\"" + String(alarmaActiva ? "activa" : "inactiva") + "\",";
  json += "\"ultima_alerta\":\"" + ultimaAlerta + "\",";
  json += "\"tiempo_alerta\":" + String(tiempoUltimaAlerta);
  json += "}";
  
  server.send(200, "application/json", json);
  Serial.println("Status solicitado");
}

void handleCommand() {
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    Serial.println("Comando recibido: " + body);
    
    if (body.indexOf("\"alarm_on\"") > 0) {
      alarmaActiva = true;
      digitalWrite(LED_PIN, HIGH);
      tone(BUZZER_PIN, 1000);
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Alarma activada\"}");
    }
    else if (body.indexOf("\"alarm_off\"") > 0 || body.indexOf("\"silence\"") > 0) {
      alarmaActiva = false;
      digitalWrite(LED_PIN, LOW);
      noTone(BUZZER_PIN);
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Alarma silenciada\"}");
    }
    else {
      server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Comando no reconocido\"}");
    }
  } else {
    server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Sin datos\"}");
  }
}

void handleAlerts() {
  String json = "{";
  json += "\"alertas\":[";
  if (ultimaAlerta != "") {
    json += "{\"mensaje\":\"" + ultimaAlerta + "\",\"tiempo\":" + String(tiempoUltimaAlerta) + "}";
  }
  json += "]";
  json += "}";
  
  server.send(200, "application/json", json);
  Serial.println("Alertas solicitadas");
}

void handleMode() {
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    Serial.println("Cambio de modo: " + body);
    
    if (body.indexOf("\"on\"") > 0) {
      sistemaEncendido = true;
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Sistema encendido\"}");
      Serial.println("Sistema ENCENDIDO");
    }
    else if (body.indexOf("\"off\"") > 0) {
      sistemaEncendido = false;
      alarmaActiva = false;
      digitalWrite(LED_PIN, LOW);
      noTone(BUZZER_PIN);
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Sistema apagado\"}");
      Serial.println("Sistema APAGADO");
    }
    else {
      server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Modo no reconocido\"}");
    }
  } else {
    server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Sin datos\"}");
  }
}
