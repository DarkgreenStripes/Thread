# Simulador de producción y consumo

Aplicación de escritorio Java Swing que simula productores y consumidores que operan sobre una reserva compartida. Permite configurar participantes, tipos y cantidades de recursos, intervalos de operación, límites del stock y sincronización; también muestra el estado y las estadísticas de la simulación.

## Índice

- [Requisitos](#requisitos)
- [Configuración e instalación](#configuración-e-instalación)
- [Cómo utilizar la aplicación](#cómo-utilizar-la-aplicación)
- [Cómo funciona](#cómo-funciona)
- [Estructura de archivos](#estructura-de-archivos)
- [Problemas conocidos](#problemas-conocidos)
- [Solución de problemas](#solución-de-problemas)
- [Licencia y derechos de autor](#licencia-y-derechos-de-autor)
- [Créditos y contacto](#créditos-y-contacto)
- [Cambios y novedades](#cambios-y-novedades)

## Requisitos

- JDK 25 o posterior. El proyecto está configurado con nivel de lenguaje Java 25 y `Main.java` utiliza el formato compacto de programa Java.
- No se necesitan bibliotecas externas: la interfaz utiliza Swing, incluido en el JDK.
- No hay un archivo de Maven o Gradle; el proyecto se compila directamente con `javac` o desde un IDE compatible con Java 25.

## Configuración e instalación

1. Instala un JDK 25 o posterior y comprueba que `java` y `javac` están disponibles desde la terminal:

   ```sh
   java -version
   javac -version
   ```

2. Abre la carpeta del proyecto en IntelliJ IDEA u otro IDE con soporte para Java 25, o sitúate en la raíz del proyecto desde una terminal.

3. Compila los archivos fuente:

   ```sh
   mkdir -p out
   javac -d out src/Main.java src/Model/*.java src/Presenter/*.java src/View/*.java
   ```

   `out/` contiene los archivos compilados y está excluida del control de versiones mediante `.gitignore`.

4. Inicia la aplicación:

   ```sh
   java -cp out Main
   ```

   También puedes ejecutar `Main.java` desde el IDE usando un JDK 25 o posterior.

## Cómo utilizar la aplicación

1. Introduce el número de productores, consumidores y tipos de recursos.
2. Configura los intervalos mínimo y máximo entre operaciones (en milisegundos) y las cantidades mínimas y máximas por operación. En cada operación se sortea una cantidad dentro del rango. Marca **Ilimitats** para que cada participante repita operaciones aleatorias hasta detener la simulación.
3. Activa **Control stock recursos** para limitar producción y consumo. Al iniciar se sortea una capacidad máxima por tipo entre los límites configurados. El selector permite esperar con `sleep` o con `wait/notifyAll`. Si la sincronización está desactivada, el control utiliza consultas periódicas con `sleep`.
4. Elige para producción y consumo un tipo **Fijo** (y el tipo concreto) o **Variable**, que sortea un tipo de recurso por cada unidad. Pulsa **Iniciar simulación** para comenzar. La ventana muestra el stock total, los intervalos recientes y el estado; la tabla muestra también el stock por tipo y los totales.
5. Pulsa **Detener** para interrumpir los hilos activos. Pulsa **Restablecer** para detenerlos, vaciar el stock y volver a los valores iniciales de los controles.

Al iniciar, se crea un hilo por productor y otro por consumidor. Sin **Ilimitats**, cada hilo realiza una operación con una cantidad aleatoria dentro del rango configurado; con **Ilimitats**, repite operaciones de ese tipo hasta detenerse. La capacidad máxima aleatoria se sortea una vez por simulación y se aplica a cada tipo de recurso. La simulación actualiza los totales y puede iniciarse de nuevo sin restablecerlos; al restablecer el stock a cero, los totales de producción y consumo también vuelven a cero.

## Cómo funciona

- `Model.Recurs` mantiene el stock de cada tipo, los límites, los totales y el estado de la sección crítica. Puede esperar con `wait()`/`notifyAll()` o consultar periódicamente mediante `sleep`.
- Con el control de stock activado, los productores esperan cuando cada tipo alcanza la capacidad sorteada y los consumidores esperan hasta que haya recursos disponibles.
- `Model.Productor` y `Model.Consumidor` sortean la cantidad en cada operación y usan el tipo fijo seleccionado o sortean el tipo de cada unidad en modo variable.
- `Model.Model` crea, inicia, detiene y espera a los hilos de la simulación.
- `Presenter.Presenter` conecta el modelo con la interfaz y publica las actualizaciones en el hilo de eventos de Swing.
- `View` contiene la ventana, los controles y los paneles de estado y estadísticas.

## Estructura de archivos

```text
.
├── README.md
├── .gitignore
├── .idea/                   Configuración del proyecto para IntelliJ IDEA
├── Apuntes/                 Apuntes del proyecto
├── out/                     Archivos compilados (generados, ignorados por Git)
└── src/
    ├── Main.java            Punto de entrada
    ├── Model/
    │   ├── Consumidor.java  Tarea de consumo ejecutada por cada cliente
    │   ├── Model.java       Configuración, gestión de la simulación y sus hilos
    │   ├── Productor.java   Tarea de producción ejecutada por cada chef
    │   └── Recurs.java      Reserva compartida y coordinación sincronizada
    ├── Presenter/
    │   └── Presenter.java   Coordinación entre modelo e interfaz
    └── View/
        ├── ControlPanel.java Controles de configuración y ejecución
        ├── DataPanel.java   Tabla de configuración y estadísticas
        ├── View.java        Ventana principal
        └── ViewPanel.java   Estado visual de la reserva y sección crítica
```

## Problemas conocidos

- El repositorio no incluye una lista de incidencias o errores conocidos ni pruebas automatizadas.
- Si los consumidores necesitan un mínimo de recursos que los productores no llegan a producir, pueden quedar esperando hasta que se pulse **Detener**. Del mismo modo, si la reserva alcanza el máximo y no hay consumidores que liberen espacio, los productores esperan.

## Solución de problemas

- **`javac` o `java` no reconoce el código:** comprueba que estás usando JDK 25 o posterior y que `java -version` y `javac -version` apuntan a esa instalación.
- **No aparece la ventana:** la aplicación necesita un entorno gráfico con soporte para Swing; una terminal sin pantalla gráfica no puede mostrarla.
- **La simulación parece detenida:** revisa el rango del límite de stock y que haya productores para abastecer a los consumidores. Usa **Detener** para finalizar una espera.
- **Aparece un error en los tiempos o límites:** los campos deben contener enteros no negativos y cada mínimo debe ser menor o igual que su máximo.
- **No se encuentra `Main` al ejecutarla:** compila desde la raíz con el comando indicado y comprueba que la carpeta `out/` contiene las clases generadas.

## Licencia y derechos de autor

El proyecto no especifica una licencia ni titulares de derechos de autor en los archivos disponibles. No se debe asumir que el código puede redistribuirse o reutilizarse bajo una licencia concreta; consulta con la persona autora o responsable antes de hacerlo.

## Créditos y contacto

La autoría y los datos de contacto no están indicados en el proyecto. La aplicación está implementada en Java y utiliza Swing para la interfaz gráfica.

## Cambios y novedades

No hay un historial de cambios formal en el repositorio. La versión actual permite configurar productores, consumidores, tipos de recursos, rangos de cantidades y tiempos, límites de stock y sincronización; también muestra estadísticas y ofrece controles para iniciar, detener y restablecer la simulación.
