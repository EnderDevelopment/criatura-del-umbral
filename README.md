# Minecraft Horror Plugin: La Criatura del Umbral

La Criatura del Umbral es un plugin de terror para Minecraft basado en Paper que añade una entidad oscura con ojos blancos, niebla, sonidos ambientales, carteles inquietantes y un sistema de fases que evoluciona durante los primeros tres días del mundo.

## Install

1. Usa un servidor Paper 1.21.11 o una versión compatible con Paper API 1.21.
2. Usa Java 21.
3. Compila el proyecto con Maven:

```bash
mvn package
```

4. Copia el archivo `target/plugin.jar` a la carpeta `plugins` de tu servidor.
5. Inicia o reinicia el servidor.
6. Comprueba que el plugin esté activo desde la consola o con `/plugins`.

El plugin usa el nombre `ErrorWorld` dentro del servidor.

## Use

1. Entra en un mundo cargado del servidor.
2. Durante el día 1, el plugin genera niebla con partículas, aplica brevemente el efecto de oscuridad, reproduce sonidos ambientales y puede colocar carteles con el mensaje de error del mundo.
3. Durante el día 2, la criatura aparece a aproximadamente 10 bloques cuando encuentra una ubicación válida. También reproduce sonidos de Enderman y sculk, pero no ataca.
4. Durante el día 3 y los días posteriores, las criaturas pueden atacar a los jugadores, seguirlos y volver a aparecer.
5. Usa `/errorworld status` para consultar la fase actual de cada mundo cargado.
6. Usa `/errorworld reset` para reiniciar la cuenta de fases de los mundos cargados y eliminar las criaturas creadas por el plugin.
7. Los comandos administrativos requieren el permiso `errorworld.admin`, disponible por defecto para operadores.

La entidad se implementa como un Wither Skeleton personalizado con armadura de cuero negra, sin arma, ojos blancos representados mediante partículas y el nombre interno “The Error”. La niebla se representa mediante partículas y el efecto Darkness; no modifica la distancia de renderizado del cliente.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/59f40675-b7dd-4d40-b011-2d46aa79e85a?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=59f40675-b7dd-4d40-b011-2d46aa79e85a
