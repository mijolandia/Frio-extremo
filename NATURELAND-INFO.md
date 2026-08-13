# 🔧 Guía: Reemplazar "Natureland" en tus Scripts

## ¿Qué es "Natureland"?

**Natureland** es el nombre del servidor original para el cual fueron creados estos scripts. Cuando descargas los archivos `.sk`, verás referencias a "natureland" en:

- Comentarios de cabecera
- Variables del servidor
- Permisos (ej: `natureland.tienda.admin`)
- Mensajes de broadcast

## 🎯 ¿Qué cambiar?

Debes reemplazar `natureland` con el **nombre de tu servidor** en estos lugares:

### 1️⃣ Permisos (IMPORTANTE)
```
❌ natureland.tienda.admin     →  ✅ [TU_SERVIDOR].tienda.admin
❌ natureland.spawn            →  ✅ [TU_SERVIDOR].spawn
❌ natureland.rtp              →  ✅ [TU_SERVIDOR].rtp
❌ natureland.bolsa.*          →  ✅ [TU_SERVIDOR].bolsa.*
```

### 2️⃣ Variables del servidor
```
❌ {natureland.spawn}          →  ✅ {miservidor.spawn}
❌ {natureland.data}           →  ✅ {miservidor.data}
```

### 3️⃣ Mensajes (Opcional, solo estética)
```
❌ "Natureland Admin"           →  ✅ "Mi Servidor Admin"
❌ &6NatureLand&8              →  ✅ &6Mi Servidor&8
```

## 📝 Cómo Reemplazar

### Opción A: En Editor de Texto (Recomendado)
1. Abre cada archivo `.sk` en tu editor (VS Code, Notepad++, etc)
2. Presiona `Ctrl + H` (Buscar y Reemplazar)
3. Busca: `natureland`
4. Reemplaza con: `[nombre-tu-servidor]` (sin espacios, todo minúsculas)
5. Click "Replace All"

### Opción B: En Minecraft Server Console
Si ya subiste los scripts, puedes usar comandos de Skript:

```
/sk reload [nombre-script]
```

Luego edita las líneas de permisos manualmente en tu permiso plugin (LuckPerms, PermissionsEx, etc)

## 🔐 Archivos donde cambiar "natureland"

Estos archivos contienen referencias a "natureland":

| Archivo | Líneas | Tipo |
|---------|--------|------|
| `tienda.sk` | ~15, ~36 | Permisos, Broadcast |
| `Bolsa.sk` | ~20 | Permisos |
| `Spawn.sk` | ~6, ~10, ~11, ~15 | Permisos, Variables |
| `Avisos.sk` | ~7 | Permisos |
| `limpieza.sk` | ~11-21 | Permisos |
| `mobs.sk` | ~2 | Comentario |
| `pocion.sk` | ~2 | Comentario |
| `Tienda-poderosa.sk` | ~2, ~44 | Comentarios |
| `ah.sk` | ~2 | Comentario |
| `iron.sk` | ~2 | Comentario |

## ✅ Verificación

Después de cambiar "natureland", verifica:

1. ✓ No haya errores en consola de servidor
2. ✓ Los comandos funcionan con nuevos permisos
3. ✓ Los jugadores con permisos pueden usar scripts

## 💡 Consejo

**La mayoría de permisos que importan:** `tienda`, `spawn`, `rtp`, `bolsa`

Enfócate primero en estos 4 archivos:
- `tienda.sk` - Sistema de tienda
- `Spawn.sk` - Gestión de spawn  
- `rtp.sk` - Teletransportación
- `Bolsa.sk` - Almacenamiento

---

**¿Tienes dudas?** Revisa los comentarios en cada archivo `.sk`
