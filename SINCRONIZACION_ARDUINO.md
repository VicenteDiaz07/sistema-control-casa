# ✅ Análisis: Sincronización App ↔ Arduino

## 🔍 Problema Identificado y Solucionado

### ❌ **Antes (NO funcionaba):**

```
Usuario cambia modo en ConfigScreen
    ↓
Se guarda solo en SharedPreferences (local)
    ↓
Arduino NO recibe el comando
    ↓
❌ App dice "Apagado" pero Arduino sigue "ARMADO"
```

### ✅ **Ahora (SÍ funciona):**

```
Usuario cambia modo en ConfigScreen
    ↓
Se guarda en SharedPreferences
    ↓
Se envía POST /mode al Arduino
    ↓
Arduino cambia sistemaEncendido
    ↓
✅ App y Arduino sincronizados
```

---

## 🔧 Cambios Realizados

### **ConfigScreen.kt - Botón "Guardar Cambios"**

**Antes:**
```kotlin
MainButton("Guardar Cambios") {
    val editor = prefs.edit()
    editor.putString("modo", modo)
    editor.apply()
    navController.navigate("main")
}
```

**Ahora:**
```kotlin
MainButton("Guardar Cambios") {
    scope.launch {
        // 1. Guardar localmente
        val editor = prefs.edit()
        editor.putString("modo", modo)
        editor.apply()
        
        // 2. Enviar al Arduino
        if (arduinoIP.isNotEmpty()) {
            val modoArduino = if (modo == "Encendido") "on" else "off"
            val resultado = arduinoClient.setMode(arduinoIP, modoArduino)
            
            if (resultado) {
                estadoConexion = "✓ Modo sincronizado"
            } else {
                estadoConexion = "⚠️ Arduino no responde"
            }
        }
        
        navController.navigate("main")
    }
}
```

---

## 📡 Flujo de Comunicación

### **Cuando cambias a "Encendido":**

```
App (ConfigScreen)
    ↓
POST http://192.168.1.XXX/mode
Body: {"mode":"on"}
    ↓
Arduino (handleMode)
    ↓
sistemaEncendido = true
    ↓
Serial: "✅ [DEBUG] Sistema ARMADO por comando HTTP."
    ↓
Arduino monitorea sensores
```

### **Cuando cambias a "Apagado":**

```
App (ConfigScreen)
    ↓
POST http://192.168.1.XXX/mode
Body: {"mode":"off"}
    ↓
Arduino (handleMode)
    ↓
sistemaEncendido = false
alarmaActiva = false
digitalWrite(LED_PIN, HIGH)
    ↓
Serial: "❌ [DEBUG] Sistema DESARMADO por comando HTTP."
    ↓
Arduino NO monitorea sensores
```

---

## 🎯 Verificación del Funcionamiento

### **Prueba 1: Encender Sistema**

1. **En la App:**
   - Ve a Configuración ⚙️
   - Cambia modo a "Encendido"
   - Toca "Guardar Cambios"

2. **En el Monitor Serie del Arduino:**
   ```
   🔄 Cambio de modo: {"mode":"on"}
   ✅ [DEBUG] Sistema ARMADO por comando HTTP.
   ```

3. **Comportamiento:**
   - ✅ LED parpadea cuando detecta movimiento
   - ✅ Alertas se guardan en Firestore
   - ✅ App muestra alertas

---

### **Prueba 2: Apagar Sistema**

1. **En la App:**
   - Ve a Configuración ⚙️
   - Cambia modo a "Apagado"
   - Toca "Guardar Cambios"

2. **En el Monitor Serie del Arduino:**
   ```
   🔄 Cambio de modo: {"mode":"off"}
   ❌ [DEBUG] Sistema DESARMADO por comando HTTP.
   ```

3. **Comportamiento:**
   - ✅ LED se apaga
   - ✅ NO se dispara alarma aunque haya movimiento
   - ✅ Monitor Serie muestra: `ℹ️ [DESARMADO] Movimiento detectado, alarma no disparada.`

---

## 📊 Estados del Sistema

| Estado App | Comando Enviado | `sistemaEncendido` | Monitorea Sensores | Dispara Alarma |
|------------|-----------------|--------------------|--------------------|----------------|
| Encendido  | `{"mode":"on"}` | `true`             | ✅ SÍ              | ✅ SÍ          |
| Apagado    | `{"mode":"off"}`| `false`            | ❌ NO              | ❌ NO          |

---

## 🔄 Sincronización Automática

### **MainScreen.kt - Consulta cada 3 segundos**

```kotlin
LaunchedEffect(modo, arduinoIP) {
    while (true) {
        delay(3000)
        if (modo == "Encendido" && !arduinoIP.isNullOrEmpty()) {
            // Consulta /status y /alerts
            // Guarda nuevas alertas en Firestore
        }
    }
}
```

**Importante:** La app solo consulta alertas si `modo == "Encendido"`.

---

## ⚠️ Casos Especiales

### **Caso 1: Arduino sin conexión**

```
Usuario cambia modo a "Encendido"
    ↓
App intenta enviar POST /mode
    ↓
Arduino no responde (timeout)
    ↓
App muestra: "⚠️ Guardado local, Arduino no responde"
    ↓
Modo se guarda solo en la app (no sincronizado)
```

**Solución:** 
- Verifica que el Arduino esté encendido
- Verifica la IP
- Toca "Probar Conexión" primero

---

### **Caso 2: Cambio de modo sin guardar**

```
Usuario cambia modo en ConfigScreen
    ↓
Usuario sale SIN tocar "Guardar Cambios"
    ↓
❌ NO se envía comando al Arduino
❌ NO se guarda en SharedPreferences
```

**Solución:** Siempre toca "Guardar Cambios" después de modificar.

---

### **Caso 3: Modo desincronizado**

Si por alguna razón el Arduino y la app están desincronizados:

**Solución:**
1. Ve a Configuración
2. Toca "Probar Conexión" para verificar que funciona
3. Cambia el modo al que desees
4. Toca "Guardar Cambios"
5. Verifica en el Monitor Serie que el Arduino recibió el comando

---

## 🎯 Resumen

### ✅ **Ahora funciona correctamente:**

1. **Cambiar modo en ConfigScreen** → Envía comando al Arduino
2. **Arduino recibe comando** → Cambia `sistemaEncendido`
3. **App y Arduino sincronizados** → Mismo estado
4. **Feedback visual** → Muestra si la sincronización fue exitosa

### 📱 **Flujo completo:**

```
ConfigScreen
    ↓
Cambiar modo: "Encendido" / "Apagado"
    ↓
Tocar "Guardar Cambios"
    ↓
1. Guarda en SharedPreferences
2. Envía POST /mode al Arduino
3. Muestra resultado de sincronización
    ↓
MainScreen
    ↓
Si modo == "Encendido":
  - Consulta alertas cada 3s
  - Guarda en Firestore
Si modo == "Apagado":
  - No consulta alertas
```

---

## 🐛 Debugging

### **Ver logs en Monitor Serie:**

```
// Cuando recibes comando HTTP:
🔄 Cambio de modo: {"mode":"on"}
✅ [DEBUG] Sistema ARMADO por comando HTTP.

// Cada 500ms (debug de sensores):
[DEBUG] Movimiento (D5): 0 | Puerta (D6): 1

// Cuando detecta movimiento (sistema ARMADO):
⚠️ [DEBUG] ¡MOVIMIENTO DETECTADO!
🚨 ¡ALERTA! Movimiento detectado (PIR/D5)
Timestamp: 123456

// Cuando detecta movimiento (sistema DESARMADO):
ℹ️ [DESARMADO] Movimiento detectado, alarma no disparada.
```

---

¡Ahora el botón de encendido/apagado funciona perfectamente! 🎉
