# Chat distribuido con ZeroC Ice y Gradle multimódulo

## Requisitos

- Java 17 o superior.
- Gradle disponible en `PATH`.
- ZeroC Ice 3.7.x con `slice2java` disponible en `PATH`.

## Compilación

Desde la raíz del proyecto `ice-chat-project`:

```text
gradle clean build
```

El resultado esperado es `BUILD SUCCESSFUL`.

## Ejecución

Abra tres terminales independientes en la raíz del proyecto.

### Terminal 1 - Servidor

```text
gradle :server:run --console=plain
```

El servidor queda disponible en el puerto TCP `10000` con la identidad `ChatService`.

### Terminal 2 - Primer cliente

```text
gradle :client:run --console=plain
```

Ingrese el nickname `Alice`.

### Terminal 3 - Segundo cliente

```text
gradle :client:run --console=plain
```

Ingrese el nickname `Bob`.

Durante la ejecución del cliente:

```text
/users
```

muestra los usuarios conectados, y:

```text
/exit
```

cierra la sesión.
