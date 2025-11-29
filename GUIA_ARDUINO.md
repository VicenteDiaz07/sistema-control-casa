# 🚀 Sistema Listo para Pruebas con Arduino

## ✅ Cambios Realizados

### 📱 **App Android**
- ✅ **Eliminada simulación** de alertas cada 5 segundos
- ✅ **Implementada lectura real** desde Arduino vía WiFi
- ✅ Consulta al Arduino cada **3 segundos**
- ✅ Solo guarda alertas **nuevas** en Firestore (evita duplicados)
- ✅ Parsea respuesta JSON del Arduino correctamente

### 🤖 **Código Arduino**
- ✅ **Completamente funcional** y listo para usar
- ✅ Servidor HTTP en puerto 80
- ✅ 4 endpoints REST API implementados
- ✅ **Historial de alertas** (últimas 10)
- ✅ **Headers CORS** para compatibilidad
- ✅ **Logging mejorado** con emojis en Serial Monitor
- ✅ Manejo robusto de conexión WiFi
- ✅ LED indicador de estado

---

## 🔧 Configuración del Arduino

### **Paso 1: Hardware Necesario**
- ✅ ESP8266 (NodeMCU v1.0 recomendado) o ESP32
- ✅ Sensor PIR (HC-SR501 o similar)
- ✅ Buzzer activo o pasivo
- ✅ LED (opcional, NodeMCU ya tiene uno integrado)
- ✅ Cables Dupont
- ✅ Protoboard (opcional)

### **Paso 2: Conexiones**

```
Sensor PIR → Arduino
├─ VCC → 3.3V o 5V
├─ GND → GND
└─ OUT → D1

Buzzer → Arduino
├─ (+) → D2
└─ (-) → GND

LED (opcional) → Arduino
├─ Ánodo (+) → D4
└─ Cátodo (-) → GND (con resistencia 220Ω)
```

**Diagrama Visual:**
```
         NodeMCU ESP8266
         ┌─────────────┐
    3.3V │●           ●│ VIN
     GND │●           ●│ GND
      TX │●           ●│ RST
      RX │●           ●│ EN
      D8 │●           ●│ 3.3V
      D7 │●  NodeMCU  ●│ GND
      D6 │●           ●│ CLK
      D5 │●           ●│ SD0
     GND │●           ●│ CMD
    3.3V │●           ●│ SD1
      D4 │●  (LED)    ●│ SD2
      D3 │●           ●│ SD3
      D2 │●  BUZZER   ●│ RSV
      D1 │●  PIR      ●│ RSV
      D0 │●           ●│ A0
         └─────────────┘
```

### **Paso 3: Configurar el Código**

1. **Abre** `arduino_codigo/sistema_seguridad_wifi.ino` en Arduino IDE
2. **Modifica** las líneas 18-19:
   ```cpp
   const char* ssid = "TU_WIFI_SSID";        // Tu red WiFi
   const char* password = "TU_WIFI_PASSWORD"; // Tu contraseña
   ```
3. **Verifica** los pines si usas conexiones diferentes:
   ```cpp
   const int SENSOR_PIN = D1;    // Pin del sensor PIR
   const int BUZZER_PIN = D2;    // Pin del buzzer
   const int LED_PIN = D4;       // Pin del LED
   ```

### **Paso 4: Cargar el Código**

1. **Conecta** el ESP8266 a tu PC vía USB
2. En Arduino IDE:
   - **Tools → Board** → ESP8266 Boards → NodeMCU 1.0 (ESP-12E Module)
   - **Tools → Port** → Selecciona el puerto COM del Arduino
   - **Tools → Upload Speed** → 115200
3. **Haz clic** en "Upload" (→)
4. **Espera** a que termine la carga

### **Paso 5: Obtener la IP del Arduino**

1. **Abre** el Monitor Serie: `Tools → Serial Monitor`
2. **Configura** la velocidad a **115200 baud**
3. **Presiona** el botón RESET en el Arduino
4. Verás algo como:
   ```
   =================================
   Sistema de Seguridad WiFi
   =================================
   Conectando a WiFi: MiWiFi
   ......
   ✓ WiFi conectado!
   ✓ Dirección IP: 192.168.1.105  ← ¡ESTA ES TU IP!
   =================================
   Usa esta IP en la app Android
   =================================
   ✓ Servidor HTTP iniciado en puerto 80
   =================================
   ```
5. **Anota** la dirección IP (ej: `192.168.1.105`)

---

## 📱 Configuración de la App

### **Paso 1: Configurar IP del Arduino**
1. **Abre** la app en tu celular
2. **Inicia sesión**
3. Ve a **Configuración** ⚙️
4. En **"IP del Arduino"**, ingresa la IP que obtuviste (ej: `192.168.1.105`)
5. Toca **"Probar Conexión"**
6. Deberías ver: **"✓ Conectado"** en verde
7. Toca **"Guardar Cambios"**

### **Paso 2: Activar el Sistema**
1. En Configuración, toca el campo **"Modo Automático"**
2. Cambia a **"Encendido"**
3. Toca **"Guardar Cambios"**

### **Paso 3: Probar el Sistema**
1. Ve a la **Pantalla Principal** 🏠
2. **Mueve tu mano** frente al sensor PIR
3. Deberías:
   - 🔊 Escuchar el buzzer
   - 💡 Ver el LED encenderse
   - 📱 Ver la alerta en la app
4. Ve al **Historial** 📋
5. Deberías ver la alerta guardada

---

## 🔍 Verificación del Sistema

### **Prueba 1: Conexión WiFi**
```
Monitor Serie debe mostrar:
✓ WiFi conectado!
✓ Dirección IP: 192.168.1.XXX
```

### **Prueba 2: Servidor HTTP**
Abre un navegador en tu PC y ve a: `http://192.168.1.XXX/status`

Deberías ver:
```json
{
  "sistema":"encendido",
  "alarma":"inactiva",
  "ultima_alerta":"",
  "tiempo_alerta":0,
  "ip":"192.168.1.XXX"
}
```

### **Prueba 3: Sensor PIR**
1. Mueve tu mano frente al sensor
2. Monitor Serie debe mostrar:
   ```
   🚨 ¡ALERTA! Movimiento detectado
   Timestamp: 12345678
   ```

### **Prueba 4: App Android**
1. Abre la app
2. Modo = "Encendido"
3. IP configurada correctamente
4. Mueve tu mano frente al sensor
5. Espera 3 segundos
6. La alerta debe aparecer en la app

---

## 🐛 Solución de Problemas

### ❌ **Arduino no se conecta a WiFi**
**Síntomas**: Monitor Serie muestra "✗ Error: No se pudo conectar a WiFi"

**Soluciones**:
1. Verifica que el SSID y contraseña sean correctos
2. Asegúrate de que el WiFi sea de 2.4GHz (ESP8266 no soporta 5GHz)
3. Acerca el Arduino al router
4. Reinicia el Arduino (botón RESET)

### ❌ **App no se conecta al Arduino**
**Síntomas**: "Estado de Conexión: Error de conexión"

**Soluciones**:
1. Verifica que la IP sea correcta
2. Asegúrate de que el celular y el Arduino estén en la **misma red WiFi**
3. Desactiva el firewall del router temporalmente
4. Prueba abrir `http://IP_ARDUINO/status` en el navegador del celular

### ❌ **Sensor no detecta movimiento**
**Síntomas**: No hay alertas al mover la mano

**Soluciones**:
1. Verifica las conexiones del sensor PIR
2. Espera 30-60 segundos después de encender (el PIR necesita calibrarse)
3. Ajusta la sensibilidad del sensor (potenciómetros en el PIR)
4. Prueba con movimientos más amplios

### ❌ **Buzzer no suena**
**Síntomas**: LED enciende pero no hay sonido

**Soluciones**:
1. Verifica la polaridad del buzzer
2. Si es buzzer pasivo, prueba con diferentes frecuencias
3. Cambia el pin del buzzer en el código
4. Prueba con un buzzer diferente

### ❌ **Alertas no se guardan en Firestore**
**Síntomas**: Alertas aparecen en la app pero no en el historial

**Soluciones**:
1. Verifica las reglas de Firestore (deben permitir escritura)
2. Asegúrate de estar autenticado en la app
3. Revisa la consola de Firebase para ver errores
4. Verifica que tengas conexión a Internet

---

## 📊 Endpoints del Arduino

### **GET /status**
Obtiene el estado actual del sistema

**Respuesta**:
```json
{
  "sistema": "encendido",
  "alarma": "activa",
  "ultima_alerta": "Movimiento detectado",
  "tiempo_alerta": 12345678,
  "ip": "192.168.1.105"
}
```

### **POST /command**
Envía comandos al sistema

**Body**:
```json
{"command": "silence"}
```

**Comandos disponibles**:
- `alarm_on` - Activar alarma manualmente
- `alarm_off` - Desactivar alarma
- `silence` - Silenciar alarma

### **GET /alerts**
Obtiene el historial de alertas (últimas 10)

**Respuesta**:
```json
{
  "alertas": [
    {"mensaje": "Movimiento detectado", "tiempo": 12345678},
    {"mensaje": "Movimiento detectado", "tiempo": 12340000}
  ],
  "total": 2
}
```

### **POST /mode**
Cambia el modo del sistema

**Body**:
```json
{"mode": "on"}
```

**Modos disponibles**:
- `on` - Encender sistema
- `off` - Apagar sistema

---

## 🎯 Flujo de Funcionamiento

```
1. Arduino detecta movimiento (PIR)
   ↓
2. Arduino activa alarma (LED + Buzzer)
   ↓
3. Arduino guarda alerta en memoria
   ↓
4. App consulta /status cada 3 segundos
   ↓
5. App detecta nueva alerta
   ↓
6. App guarda alerta en Firestore
   ↓
7. App muestra alerta en pantalla
   ↓
8. Usuario puede ver historial en Firestore
```

---

## 📝 Notas Importantes

- ⚠️ El Arduino y el celular **DEBEN** estar en la misma red WiFi
- ⚠️ La IP del Arduino puede cambiar si se reinicia el router
- ⚠️ El sensor PIR necesita 30-60 segundos para calibrarse al encender
- ⚠️ El ESP8266 solo soporta WiFi de **2.4GHz**, no 5GHz
- ✅ El sistema guarda las últimas **10 alertas** en el Arduino
- ✅ Todas las alertas se guardan en **Firestore** sin límite
- ✅ La app consulta el Arduino cada **3 segundos**

---

## 🚀 ¡Listo para Usar!

Tu sistema está completamente configurado y listo para funcionar. Solo necesitas:

1. ✅ Cargar el código en el Arduino
2. ✅ Conectar el sensor PIR y el buzzer
3. ✅ Configurar la IP en la app
4. ✅ ¡Probar el sistema!

¡Disfruta de tu sistema de seguridad IoT! 🎉
