# Configuración ESP8266 - Sistema IoT

## Requisitos de Hardware

- **ESP8266** (NodeMCU, Wemos D1 Mini, etc.)
- **Sensor PIR** (HC-SR501 o similar)
- **Buzzer** activo o pasivo
- **Cables jumper**
- **Fuente de alimentación** 5V

## Conexiones

```
ESP8266          Componente
-------          ----------
D1 (GPIO5)   →   PIR OUT
D2 (GPIO4)   →   Buzzer +
GND          →   PIR GND, Buzzer -
VCC (3.3V)   →   PIR VCC
```

## Librerías Necesarias

Instalar desde el Library Manager de Arduino IDE:

1. **ESP8266WiFi** (incluida con ESP8266 board)
2. **ESP8266WebServer** (incluida con ESP8266 board)
3. **ArduinoJson** v6.x
   - Sketch → Include Library → Manage Libraries
   - Buscar "ArduinoJson"
   - Instalar versión 6.21.0 o superior

## Configuración del Arduino IDE

### 1. Instalar soporte para ESP8266

1. Archivo → Preferencias
2. En "Gestor de URLs Adicionales de Tarjetas" agregar:
   ```
   http://arduino.esp8266.com/stable/package_esp8266com_index.json
   ```
3. Herramientas → Placa → Gestor de tarjetas
4. Buscar "esp8266" e instalar "ESP8266 by ESP8266 Community"

### 2. Configurar la placa

- **Placa:** NodeMCU 1.0 (ESP-12E Module) o tu modelo específico
- **Upload Speed:** 115200
- **CPU Frequency:** 80 MHz
- **Flash Size:** 4MB (FS:2MB OTA:~1019KB)
- **Puerto:** Seleccionar el puerto COM correspondiente

### 3. Configurar SPIFFS

En el menú Herramientas:
- **Flash Size:** Seleccionar opción con FS (File System)
  - Ejemplo: "4MB (FS:2MB OTA:~1019KB)"

## Configuración del Código

### 1. Editar credenciales WiFi

En `sistema_iot_casa.ino`, líneas 22-23:

```cpp
const char* ssid = "TU_WIFI_SSID";           // Cambiar
const char* password = "TU_WIFI_PASSWORD";   // Cambiar
```

### 2. Configurar pines (opcional)

Si usas pines diferentes, modificar líneas 25-27:

```cpp
const int PIR_PIN = D1;        // Cambiar si es necesario
const int BUZZER_PIN = D2;     // Cambiar si es necesario
const int LED_PIN = LED_BUILTIN;
```

### 3. Deep Sleep (opcional)

Para activar modo de ahorro de energía extremo:

```cpp
const bool ENABLE_DEEP_SLEEP = true;  // Cambiar a true
const unsigned long SLEEP_TIME = 30e6;  // 30 segundos
```

⚠️ **Nota:** Con Deep Sleep activado, el ESP8266 se reinicia cada 30 segundos.
Esto ahorra mucha energía pero puede perder eventos entre ciclos.

## Subir el Código

1. Conectar ESP8266 por USB
2. Seleccionar puerto correcto
3. Sketch → Subir
4. Esperar a que compile y suba
5. Abrir Monitor Serie (115200 baud)

## Verificar Funcionamiento

### 1. Monitor Serie

Deberías ver:

```
=== Sistema IoT Iniciando ===
SPIFFS montado correctamente
Conectando a WiFi: TuWiFi
..........
WiFi conectado!
IP: 192.168.1.XXX
Servidor HTTP iniciado en puerto 80
```

### 2. Probar Endpoints

Desde un navegador o Postman:

**Estado del sistema:**
```
GET http://192.168.1.XXX/status
```

**Cambiar modo:**
```
POST http://192.168.1.XXX/mode
Body: {"mode":"on"}
```

**Silenciar alarma:**
```
POST http://192.168.1.XXX/command
Body: {"command":"silence"}
```

### 3. Configurar IP en la App Android

1. Abrir app Android
2. Ir a Configuración
3. Ingresar IP del ESP8266: `192.168.1.XXX`
4. Presionar "Probar Conexión"
5. Debería mostrar "✓ Conectado"

## Solución de Problemas

### ESP8266 no conecta a WiFi

- Verificar SSID y contraseña
- Verificar que WiFi es 2.4GHz (ESP8266 no soporta 5GHz)
- Acercar ESP8266 al router

### Error "SPIFFS mount failed"

- Verificar Flash Size incluye FS
- Reformatear SPIFFS (se hace automáticamente en el código)

### Sensor PIR no detecta movimiento

- Verificar conexiones
- Ajustar sensibilidad del PIR (potenciómetros en el sensor)
- Esperar 30-60 segundos después de encender (calibración)

### App no se conecta al ESP8266

- Verificar que móvil y ESP8266 están en la misma red WiFi
- Verificar IP correcta en la app
- Verificar firewall del router

## Consumo de Energía

### Modo Normal
- **Consumo:** ~80mA en idle, ~170mA transmitiendo
- **Duración batería (2000mAh):** ~12-25 horas

### Modo Deep Sleep
- **Consumo:** ~20µA en sleep, ~170mA despierto
- **Duración batería (2000mAh):** Varios días

## Próximos Pasos

1. ✅ Subir código al ESP8266
2. ✅ Verificar conexión WiFi
3. ✅ Probar endpoints HTTP
4. ✅ Configurar IP en app Android
5. ⬜ Implementar HTTPS (opcional)
6. ⬜ Agregar más sensores
7. ⬜ Optimizar consumo de energía
