<div align="center">

# SCommunity

### Misiones diarias que todo el servidor completa junto.

Une a tu comunidad con objetivos globales: todos aportan, todos ganan.

[![build](https://github.com/JhonTova/SCommunity/actions/workflows/build.yml/badge.svg)](https://github.com/JhonTova/SCommunity/actions/workflows/build.yml) [![release](https://img.shields.io/github/v/release/JhonTova/SCommunity?label=release)](https://github.com/JhonTova/SCommunity/releases/latest) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%2B-62B47A) ![Java](https://img.shields.io/badge/Java-21-E76F00)

</div>

---

## Características

- **Misiones comunitarias diarias** — un objetivo que el servidor entero persigue en equipo.
- **Progreso global** que se acumula con la contribución de cada jugador.
- **Niveles de comunidad** que suben al completar misiones y desbloquean recompensas.
- **Misiones personales** además de las globales.
- **GUI de misiones** y de depósito de ítems.
- **Recompensas** para quienes participan.

## Comandos

| Comando | Acción |
|---|---|
| `/mission` | Abre el menú de misiones. |
| `/missionadmin` | Gestión de administrador (reset, recarga…). |

## Requisitos

Requiere **[SiriusCore](https://github.com/JhonTova/SiriusCore)**.

## Instalación

`SiriusCore.jar` + `SCommunity.jar` en `plugins/`. Spigot / Paper / Purpur 1.21.11+.

## Descarga

El `.jar` de cada versión está en [Releases](https://github.com/JhonTova/SCommunity/releases/latest).

## Compilar desde el código

Requiere Java 21 y Maven. SiriusCore no está en un repositorio Maven público, así que
primero se instala en local la versión que declara el `pom.xml`:

```bash
git clone --branch v1.1.1 https://github.com/JhonTova/SiriusCore.git
mvn -f SiriusCore/pom.xml install -DskipTests
mvn package        # en este repositorio; el .jar queda en target/
```

Cada push y pull request se compila en GitHub Actions. Al subir una tag `v*`, el `.jar`
se adjunta solo a la release.
