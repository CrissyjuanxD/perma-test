# MobCap Manager

Sistema avanzado de gestión de MobCap basado en la lógica de Permadeath, diseñado para ser una clase independiente con optimizaciones para servidores con muchos jugadores.

## Características

### 🎯 Funcionalidades Principales
- **MobCap Base Configurable**: Establece un valor base para todos los mundos
- **Doble MobCap**: Duplica la mob cap cuando está activado (igual que Permadeath)
- **Optimización Automática**: Reduce automáticamente la mob cap cuando hay muchos jugadores (60+) y valores altos (140+)
- **Gestión por Mundos**: Maneja cada mundo individualmente
- **Limpieza Inteligente**: Remueve mobs distantes cuando es necesario

### 🚀 Optimizaciones para Servidores Grandes
- **Detección Automática**: Detecta cuando hay más de 60 jugadores y mob cap > 140
- **Reducción Gradual**: Aplica factor de reducción basado en número de jugadores
- **Limpieza Suave**: Remueve mobs que están lejos de jugadores (32+ bloques)
- **Procesamiento Asíncrono**: Las optimizaciones se ejecutan en hilos separados
- **Límites Inteligentes**: Mantiene un mínimo razonable incluso con optimizaciones

### 📊 Sistema de Información
- **Comando `/mobcapinfo`**: Muestra información detallada del estado actual
- **Monitoreo en Tiempo Real**: Ve el estado de cada mundo
- **Indicadores de Optimización**: Sabe cuándo las optimizaciones están activas

## Comandos

### Para Todos los Usuarios
```
/mobcapinfo - Muestra información detallada del MobCap actual
```

### Para Administradores
```
/mobcap set <número>     - Establece el MobCap base (1-500)
/mobcap double [on/off]  - Activa/desactiva doble MobCap
/mobcap reset           - Restablece valores originales
/mobcap reload          - Recarga la configuración
/mobcap info            - Alias de mobcapinfo
```

## Permisos

- `mobcap.use` - Permite usar comandos básicos (default: true)
- `mobcap.admin` - Permite administrar la configuración (default: op)

## Integración

### Uso Básico
```java
// Obtener instancia del manager
MobCapManager manager = MobCapManager.getInstance(plugin);

// Establecer mob cap base
manager.setBaseMobCap(140);

// Activar doble mob cap (como Permadeath)
manager.setDoubleMobCap(true);

// Obtener información actual
MobCapInfo info = manager.getMobCapInfo();
```

### Integración con Otros Plugins
```java
public class MiPlugin extends JavaPlugin {
    private MobCapManager mobCapManager;
    
    @Override
    public void onEnable() {
        // Verificar si MobCapManager está disponible
        Plugin mobCapPlugin = getServer().getPluginManager().getPlugin("MobCapManager");
        if (mobCapPlugin != null && mobCapPlugin instanceof MobCapPlugin) {
            mobCapManager = ((MobCapPlugin) mobCapPlugin).getMobCapManager();
            
            // Configurar según tus necesidades
            if (esDiaAvanzado()) {
                mobCapManager.setDoubleMobCap(true);
            }
        }
    }
}
```

## Lógica de Optimización

### Cuándo se Activa
- **Jugadores**: Más de 60 jugadores online
- **MobCap**: Valor efectivo >= 140
- **Frecuencia**: Cada 30 segundos

### Cómo Funciona
1. **Factor de Reducción**: Calcula basado en número de jugadores
   - 60 jugadores = 100% (sin reducción)
   - 80 jugadores = 75% del valor original
   - 100+ jugadores = 60% del valor original

2. **Limpieza de Mobs**: 
   - Solo mobs sin nombre personalizado
   - Que estén a más de 32 bloques de cualquier jugador
   - Máximo 50 mobs por ciclo de limpieza

3. **Límites de Seguridad**:
   - Mínimo: 60% del valor original
   - Nunca menos de 70 o 1/3 del valor original

## Diferencias con el Sistema Original

### Mejoras Implementadas
- ✅ **Optimización Automática**: No presente en Permadeath
- ✅ **Comandos de Información**: Sistema completo de monitoreo
- ✅ **Gestión de Memoria**: Mejor manejo de recursos
- ✅ **Procesamiento Asíncrono**: Mejor rendimiento
- ✅ **Límites Inteligentes**: Previene valores extremos

### Compatibilidad con Permadeath
- ✅ **Doble MobCap**: Funcionalidad idéntica
- ✅ **Mensajes**: Estilo similar de notificaciones
- ✅ **Comportamiento**: Lógica base mantenida
- ✅ **Configuración**: Valores por defecto compatibles

## Instalación

1. Coloca el archivo JAR en la carpeta `plugins/`
2. Reinicia el servidor
3. Usa `/mobcapinfo` para verificar el estado
4. Configura según tus necesidades con `/mobcap set <valor>`

## Configuración Recomendada

### Servidor Pequeño (< 30 jugadores)
```
/mobcap set 70
/mobcap double off
```

### Servidor Mediano (30-60 jugadores)
```
/mobcap set 100
/mobcap double on  # = 200 efectivo
```

### Servidor Grande (60+ jugadores)
```
/mobcap set 140
/mobcap double on  # = 280 efectivo, optimizado automáticamente
```

## Soporte

Para reportar bugs o solicitar características, crea un issue en el repositorio del proyecto.