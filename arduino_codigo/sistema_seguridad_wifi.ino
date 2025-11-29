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
const char* ssid = "TU_WIFI_SSID";        // ⚠️ CAMBIA ESTO por tu red WiFi
const char* password = "TU_WIFI_PASSWORD"; // ⚠️ CAMBIA ESTO por tu contraseña WiFi

// ========== CONFIGURACIÓN DE PINES ==========
const int SENSOR_PIN = D1;    // Pin del sensor de movimiento (PIR)
const int BUZZER_PIN = D2;    // Pin del buzzer/alarma
const int LED_PIN = D4;       // Pin del LED indicador (LED integrado en NodeMCU)

// ========== VARIABLES GLOBALES ==========
ESP8266WebServer server(80);
bool sistemaEncendido = true;
bool alarmaActiva = false;
String ultimaAlerta = "";
unsigned long tiempoUltimaAlerta = 0;

// Historial de alertas (últimas 10)
const int MAX_ALERTAS = 10;
String historialAlertas[MAX_ALERTAS];
unsigned long historialTiempos[MAX_ALERTAS];
int contadorAlertas = 0;

void setup() {
  Serial.begin(115200);
  delay(100);
  
  // Configurar pines
  pinMode(SENSOR_PIN, INPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(LED_PIN, HIGH); // LED apagado (LOW = encendido en NodeMCU)
  
  // Conectar a WiFi
  Serial.println();
  Serial.println("=================================");
  Serial.println("Sistema de Seguridad WiFi");
  Serial.println("=================================");
  Serial.print("Conectando a WiFi: ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  int intentos = 0;
  while (WiFi.status() != WL_CONNECTED && intentos < 30) {
    delay(500);
    Serial.print(".");
    intentos++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println();
    Serial.println("✓ WiFi conectado!");
    Serial.print("✓ Dirección IP: ");
    Serial.println(WiFi.localIP());
    Serial.println("=================================");
    Serial.println("Usa esta IP en la app Android");
    Serial.println("=================================");
  } else {
    Serial.println();
    Serial.println("✗ Error: No se pudo conectar a WiFi");
    Serial.println("Verifica SSID y contraseña");
  }
  
  // Configurar endpoints del servidor web
  server.on("/status", HTTP_GET, handleStatus);
  server.on("/command", HTTP_POST, handleCommand);
  server.on("/alerts", HTTP_GET, handleAlerts);
  server.on("/mode", HTTP_POST, handleMode);
  
  // Endpoint raíz para verificar que el servidor funciona
  server.on("/", HTTP_GET, []() {
    server.send(200, "text/plain", "Sistema de Seguridad WiFi - OK");
  });
  
  // Iniciar servidor
  server.begin();
  Serial.println("✓ Servidor HTTP iniciado en puerto 80");
  Serial.println("=================================");
  
  // Parpadear LED para indicar que está listo
  for(int i = 0; i < 3; i++) {
    digitalWrite(LED_PIN, LOW);  // Encender
    delay(200);
    digitalWrite(LED_PIN, HIGH); // Apagar
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
      
      // Guardar en historial
      agregarAlerta(ultimaAlerta, tiempoUltimaAlerta);
      
      // Activar alarma
      digitalWrite(LED_PIN, LOW); // Encender LED
      tone(BUZZER_PIN, 1000); // Tono de 1000 Hz
      
      Serial.println("🚨 ¡ALERTA! Movimiento detectado");
      Serial.print("Timestamp: ");
      Serial.println(tiempoUltimaAlerta);
    }
  } else {
    // Sistema apagado
    digitalWrite(LED_PIN, HIGH); // Apagar LED
    noTone(BUZZER_PIN);
    alarmaActiva = false;
  }
}

// ========== FUNCIONES AUXILIARES ==========

void agregarAlerta(String mensaje, unsigned long tiempo) {
  // Agregar alerta al historial (circular buffer)
  int indice = contadorAlertas % MAX_ALERTAS;
  historialAlertas[indice] = mensaje;
  historialTiempos[indice] = tiempo;
  contadorAlertas++;
}

void agregarHeadersCORS() {
  // Permitir peticiones desde cualquier origen (para desarrollo)
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
}

// ========== MANEJADORES DE ENDPOINTS ==========

void handleStatus() {
  agregarHeadersCORS();
  
  String json = "{";
  json += "\"sistema\":\"" + String(sistemaEncendido ? "encendido" : "apagado") + "\",";
  json += "\"alarma\":\"" + String(alarmaActiva ? "activa" : "inactiva") + "\",";
  json += "\"ultima_alerta\":\"" + ultimaAlerta + "\",";
  json += "\"tiempo_alerta\":" + String(tiempoUltimaAlerta) + ",";
  json += "\"ip\":\"" + WiFi.localIP().toString() + "\"";
  json += "}";
  
  server.send(200, "application/json", json);
  Serial.println("📊 Status solicitado");
}

void handleCommand() {
  agregarHeadersCORS();
  
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    Serial.println("📥 Comando recibido: " + body);
    
    if (body.indexOf("\"alarm_on\"") > 0) {
      alarmaActiva = true;
      digitalWrite(LED_PIN, LOW);
      tone(BUZZER_PIN, 1000);
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Alarma activada\"}");
      Serial.println("✓ Alarma activada manualmente");
    }
    else if (body.indexOf("\"alarm_off\"") > 0 || body.indexOf("\"silence\"") > 0) {
      alarmaActiva = false;
      digitalWrite(LED_PIN, HIGH);
      noTone(BUZZER_PIN);
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Alarma silenciada\"}");
      Serial.println("✓ Alarma silenciada");
    }
    else {
      server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Comando no reconocido\"}");
      Serial.println("✗ Comando no reconocido");
    }
  } else {
    server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Sin datos\"}");
  }
}

void handleAlerts() {
  agregarHeadersCORS();
  
  String json = "{";
  json += "\"alertas\":[";
  
  // Obtener las últimas alertas (máximo 10)
  int totalAlertas = min(contadorAlertas, MAX_ALERTAS);
  for (int i = 0; i < totalAlertas; i++) {
    int indice = (contadorAlertas - 1 - i) % MAX_ALERTAS;
    if (indice < 0) indice += MAX_ALERTAS;
    
    if (historialAlertas[indice] != "") {
      if (i > 0) json += ",";
      json += "{";
      json += "\"mensaje\":\"" + historialAlertas[indice] + "\",";
      json += "\"tiempo\":" + String(historialTiempos[indice]);
      json += "}";
    }
  }
  
  json += "],";
  json += "\"total\":" + String(totalAlertas);
  json += "}";
  
  server.send(200, "application/json", json);
  Serial.println("📋 Alertas solicitadas (" + String(totalAlertas) + " alertas)");
}

void handleMode() {
  agregarHeadersCORS();
  
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    Serial.println("🔄 Cambio de modo: " + body);
    
    if (body.indexOf("\"on\"") > 0) {
      sistemaEncendido = true;
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Sistema encendido\"}");
      Serial.println("✓ Sistema ENCENDIDO");
    }
    else if (body.indexOf("\"off\"") > 0) {
      sistemaEncendido = false;
      alarmaActiva = false;
      digitalWrite(LED_PIN, HIGH);
      noTone(BUZZER_PIN);
      server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"Sistema apagado\"}");
      Serial.println("✓ Sistema APAGADO");
    }
    else {
      server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Modo no reconocido\"}");
      Serial.println("✗ Modo no reconocido");
    }
  } else {
    server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Sin datos\"}");
  }
}
