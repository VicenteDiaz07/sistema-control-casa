# 📡 Configuración de Conexión WiFi con Arduino

## 🎯 Resumen
Este sistema permite que tu app Android se comunique con un Arduino vía WiFi para controlar un sistema de seguridad.

## 🔧 Requisitos de Hardware

### Arduino:
- **ESP8266** (NodeMCU, Wemos D1 Mini) o **ESP32**
- Sensor de movimiento PIR (HC-SR501 o similar)
- Buzzer activo o pasivo
- LED indicador
- Cables de conexión

### Conexiones Recomendadas (ESP8266):
```
Sensor PIR:
  - VCC → 3.3V
  - GND → GND
  - OUT → D1

Buzzer:
  - (+) → D2
  - (-) → GND

LED:
  - Ánodo (+) → D4
  - Cátodo (-) → GND (con resistencia 220Ω)
```

## 📱 Configuración del Arduino

### Paso 1: Instalar Arduino IDE
1. Descarga Arduino IDE desde: https://www.arduino.cc/en/software
2. Instala el IDE en tu computadora

### Paso 2: Configurar ESP8266 en Arduino IDE
1. Abre Arduino IDE
2. Ve a **Archivo → Preferencias**
3. En "Gestor de URLs Adicionales de Tarjetas", agrega:
   ```
   http://arduino.esp8266.com/stable/package_esp8266com_index.json
   ```
4. Ve a **Herramientas → Placa → Gestor de tarjetas**
5. Busca "ESP8266" e instala "esp8266 by ESP8266 Community"

### Paso 3: Cargar el Código
1. Abre el archivo `arduino_codigo/sistema_seguridad_wifi.ino`
2. **IMPORTANTE**: Modifica estas líneas con tus datos WiFi:
   ```cpp
   const char* ssid = "TU_WIFI_SSID";        // Tu red WiFi
   const char* password = "TU_WIFI_PASSWORD"; // Tu contraseña WiFi
   ```
3. Selecciona tu placa: **Herramientas → Placa → ESP8266 Boards → NodeMCU 1.0**
4. Selecciona el puerto COM correcto: **Herramientas → Puerto**
5. Haz clic en **Subir** (flecha →)

### Paso 4: Obtener la IP del Arduino
1. Abre el **Monitor Serie**: **Herramientas → Monitor Serie**
2. Configura la velocidad a **115200 baudios**
3. Espera a que se conecte al WiFi
4. Verás un mensaje como:
   ```
   WiFi conectado!
   Dirección IP: 192.168.1.100
   ```
5. **Anota esta IP**, la necesitarás en la app

## 📱 Configuración de la App Android

### Paso 1: Conectar a la Misma Red WiFi
- Asegúrate de que tu teléfono Android esté conectado a la **misma red WiFi** que el Arduino

### Paso 2: Configurar la IP en la App
1. Abre la app en tu teléfono
2. Ve a la pestaña **Configuración** (ícono de engranaje)
3. En el campo **"IP del Arduino"**, ingresa la IP que obtuviste (ej: `192.168.1.100`)
4. Toca **"Probar Conexión"**
5. Deberías ver: **"✓ Conectado"** en verde
6. Toca **"Guardar Cambios"**

## 🚀 Uso del Sistema

### Funciones Disponibles:

1. **Probar Conexión**: Verifica que la app pueda comunicarse con el Arduino
2. **Modo Automático**: Enciende/Apaga el sistema de seguridad
3. **Silenciar Alarma**: Detiene la alarma cuando se detecta movimiento

### Endpoints HTTP del Arduino:

El Arduino expone los siguientes endpoints:

- `GET http://192.168.1.100/status` - Obtiene el estado del sistema
- `POST http://192.168.1.100/command` - Envía comandos
- `GET http://192.168.1.100/alerts` - Obtiene alertas recientes
- `POST http://192.168.1.100/mode` - Cambia el modo (on/off)

## 🔍 Solución de Problemas

### ❌ "Error de conexión" al probar
**Causas posibles:**
- El Arduino no está encendido
- El Arduino no está conectado al WiFi
- La IP es incorrecta
- El teléfono y Arduino están en redes WiFi diferentes
- Firewall bloqueando la conexión

**Soluciones:**
1. Verifica que el Arduino esté encendido y el LED parpadee 3 veces al iniciar
2. Abre el Monitor Serie del Arduino IDE y verifica que diga "WiFi conectado"
3. Verifica que la IP en la app coincida con la IP del Arduino
4. Asegúrate de que ambos dispositivos estén en la misma red WiFi
5. Intenta hacer ping desde tu PC a la IP del Arduino

### ❌ El Arduino no se conecta al WiFi
**Soluciones:**
1. Verifica que el SSID y contraseña sean correctos
2. Asegúrate de que tu WiFi sea de 2.4 GHz (ESP8266 no soporta 5 GHz)
3. Acerca el Arduino al router WiFi

### ❌ El sensor no detecta movimiento
**Soluciones:**
1. Verifica las conexiones del sensor PIR
2. Espera 30-60 segundos después de encender (tiempo de calibración del PIR)
3. Ajusta la sensibilidad del sensor PIR (tiene un potenciómetro)

## 📊 Diagrama de Comunicación

```
┌─────────────┐         WiFi          ┌──────────────┐
│   Android   │ ◄──────────────────► │   Arduino    │
│     App     │    HTTP Requests      │   ESP8266    │
└─────────────┘                       └──────────────┘
                                             │
                                             ├─► Sensor PIR
                                             ├─► Buzzer
                                             └─► LED
```

## 🔐 Seguridad

**IMPORTANTE**: Este sistema es para uso educativo/doméstico básico. Para un sistema de seguridad profesional, considera:
- Agregar autenticación (usuario/contraseña)
- Usar HTTPS en lugar de HTTP
- Implementar cifrado de datos
- Usar un servidor dedicado en lugar de conexión directa

## 📝 Notas Adicionales

- La IP del Arduino puede cambiar si se reinicia el router. En ese caso, verifica la nueva IP en el Monitor Serie
- Puedes configurar una IP estática en el código del Arduino si lo prefieres
- El sistema consume muy poca energía y puede dejarse encendido 24/7
- Puedes agregar más sensores modificando el código del Arduino

## 🆘 Soporte

Si tienes problemas, verifica:
1. Monitor Serie del Arduino (115200 baudios)
2. Logs de la app Android (Logcat en Android Studio)
3. Que ambos dispositivos estén en la misma red WiFi
4. Que la IP sea correcta
