# 🔧 Solución: Error de Conexión HTTP en Android

## 🎯 Problema Identificado

**Síntoma:** El navegador del celular puede acceder al Arduino (`http://IP/status`), pero la app no se conecta.

**Causa:** Android 9+ (API 28+) bloquea conexiones HTTP no seguras por defecto por razones de seguridad.

---

## ✅ Solución Aplicada

Se agregó `android:usesCleartextTraffic="true"` en el `AndroidManifest.xml`:

```xml
<application
    ...
    android:usesCleartextTraffic="true">
```

### ¿Qué hace esto?

Permite que la app haga peticiones HTTP (no HTTPS) a direcciones IP locales como `192.168.1.XXX`.

---

## 📱 Pasos para que Funcione

### **1. Recompilar la App**

Después de este cambio, tu amigo necesita:

1. **Desinstalar** la app actual del celular
2. **Recompilar** el proyecto en Android Studio:
   - `Build → Clean Project`
   - `Build → Rebuild Project`
3. **Instalar** la nueva versión en el celular

### **2. Configurar la IP**

1. Abre la app
2. Inicia sesión
3. Ve a **Configuración** ⚙️
4. Ingresa la IP del Arduino (ej: `192.168.1.105`)
5. Toca **"Probar Conexión"**
6. Debería mostrar: **"✓ Conectado"** en verde

### **3. Activar el Sistema**

1. Cambia el modo a **"Encendido"**
2. Toca **"Guardar Cambios"**
3. Ve a la **Pantalla Principal** 🏠
4. ¡Listo para detectar movimiento!

---

## 🔍 Verificación

### **Antes del cambio:**
- ❌ Navegador: Funciona ✅
- ❌ App: No se conecta ❌

### **Después del cambio:**
- ✅ Navegador: Funciona ✅
- ✅ App: Funciona ✅

---

## ⚠️ Nota de Seguridad

**¿Es seguro usar `usesCleartextTraffic="true"`?**

✅ **SÍ**, en este caso porque:
- Solo se usa para comunicación **local** (misma red WiFi)
- No se envían datos sensibles al Arduino
- La IP es privada (192.168.x.x)
- No se expone a Internet

❌ **NO lo uses** si:
- Necesitas comunicarte con servidores en Internet
- Envías contraseñas o datos sensibles
- La app se conecta a APIs públicas

---

## 🚀 Alternativa Más Segura (Opcional)

Si quieres ser más específico y solo permitir HTTP para el Arduino:

### **Opción 1: Network Security Config**

1. Crea `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Permitir HTTP solo para IPs locales -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.1.0/24</domain>
        <domain includeSubdomains="true">192.168.0.0/24</domain>
        <domain includeSubdomains="true">10.0.0.0/8</domain>
    </domain-config>
</network-security-config>
```

2. Referencia en `AndroidManifest.xml`:

```xml
<application
    ...
    android:networkSecurityConfig="@xml/network_security_config">
```

**Ventaja:** Solo permite HTTP para redes locales, mantiene HTTPS obligatorio para Internet.

---

## 📋 Resumen

| Paso | Acción |
|------|--------|
| 1 | ✅ Agregado `usesCleartextTraffic="true"` |
| 2 | 🔄 Recompilar la app |
| 3 | 📱 Reinstalar en el celular |
| 4 | ⚙️ Configurar IP del Arduino |
| 5 | 🎉 ¡Funciona! |

---

## 🐛 Si Aún No Funciona

Si después de recompilar sigue sin funcionar, verifica:

1. **¿Realmente recompiló?**
   - Haz `Build → Clean Project`
   - Luego `Build → Rebuild Project`

2. **¿Desinstaló la app anterior?**
   - Desinstala completamente
   - Vuelve a instalar desde Android Studio

3. **¿La IP es correcta?**
   - Verifica en el Monitor Serie del Arduino
   - Usa exactamente la misma IP

4. **¿Tiene permisos de Internet?**
   - Ve a Configuración del celular
   - Apps → Tu App → Permisos
   - Verifica que tenga acceso a Internet

---

¡Ahora debería funcionar perfectamente! 🎉
