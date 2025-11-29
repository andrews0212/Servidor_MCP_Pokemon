# Servidor MCP con Spring AI - Pokémon API

Este proyecto es un servidor MCP (Model Context Protocol) construido con Spring Boot y Spring AI que permite a los modelos de IA consultar información sobre Pokémon utilizando la [PokéAPI](https://pokeapi.co/).

## ¿Qué es MCP?

MCP (Model Context Protocol) es un protocolo que permite a los modelos de IA interactuar con herramientas y servicios externos. En este caso, creamos un servidor que expone herramientas para consultar información de Pokémon que cualquier cliente MCP (como Claude Desktop) puede utilizar.

## Estructura del Proyecto

```
mcp/
├── src/
│   └── main/
│       ├── java/com/example/mcp/
│       │   ├── McpApplication.java      # Clase principal de Spring Boot
│       │   └── PokemonService.java      # Servicio con las herramientas
│       └── resources/
│           └── application.properties   # Configuración de la aplicación
├── pom.xml                              # Dependencias Maven
└── README.md
```

## Explicación del Código

### 1. `pom.xml` - Dependencias del Proyecto

```xml
<dependencies>
    <!-- Spring Boot básico -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring AI MCP Server - La dependencia clave -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server</artifactId>
    </dependency>

    <!-- Para hacer llamadas HTTP a la API -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
    </dependency>

    <!-- Actuator para monitoreo (opcional) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

**Propiedades importantes:**
- Java 21 como versión mínima
- Spring Boot 3.4.12
- Spring AI 1.1.0

### 2. `McpApplication.java` - Clase Principal

```java
@SpringBootApplication
public class McpApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }

    // Bean que registra las herramientas del servicio
    @Bean
    public ToolCallbackProvider weatherTools(PokemonService pokemonService) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(pokemonService)
            .build();
    }
}
```

**¿Qué hace?**
- `@SpringBootApplication`: Marca esta clase como aplicación Spring Boot
- `ToolCallbackProvider`: Registra los métodos anotados con `@Tool` del `PokemonService` para que estén disponibles como herramientas MCP
- `MethodToolCallbackProvider`: Escanea automáticamente los métodos `@Tool` del objeto proporcionado

### 3. `PokemonService.java` - Servicio con las Herramientas

```java
@Service
public class PokemonService {
    private final RestClient restClient;

    public PokemonService() {
        // Configuramos el cliente REST para la PokéAPI
        this.restClient = RestClient.builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "SpringAI-Agent/1.0")
            .build();
    }

    @Tool(description = "Get information about a Pokemon by its name")
    public String getPokemonInfo(
        @ToolParam(description = "The name of the Pokemon in lowercase")
        String name
    ) {
        try {
            return restClient.get()
                .uri("pokemon/{name}", name.toLowerCase())
                .retrieve()
                .body(String.class);
        } catch (Exception e) {
            return "Error: Could not find Pokemon with name '" + name + "'";
        }
    }

    @Tool(description = "Get information about a Pokemon move or ability")
    public String getAbilityInfo(
        @ToolParam(description = "The name of the ability")
        String ability
    ) {
        try {
            return restClient.get()
                .uri("ability/{ability}", ability.toLowerCase())
                .retrieve()
                .body(String.class);
        } catch (Exception e) {
            return "Error: Could not find ability details.";
        }
    }
}
```

**Anotaciones clave:**
- `@Service`: Marca la clase como un servicio de Spring
- `@Tool`: Expone el método como una herramienta MCP que la IA puede invocar
  - `description`: Describe qué hace la herramienta (la IA usa esto para decidir cuándo llamarla)
- `@ToolParam`: Describe cada parámetro del método
  - La IA usa estas descripciones para saber qué valores pasar

**Cómo funciona:**
1. El modelo de IA lee las descripciones de las herramientas
2. Cuando el usuario pregunta algo sobre Pokémon, la IA decide usar la herramienta apropiada
3. La IA llama al método con los parámetros correctos
4. El método consulta la PokéAPI y devuelve el resultado
5. La IA procesa el resultado y responde al usuario en lenguaje natural

## Cómo Construir tu Propio Servidor MCP

### Paso 1: Crear el Proyecto

```bash
# Opción A: Usar Spring Initializr (https://start.spring.io/)
# - Project: Maven
# - Language: Java
# - Spring Boot: 3.4.12
# - Java: 21
# - Dependencies: Spring Boot Starter

# Opción B: Clonar este repositorio
git clone <tu-repo>
cd mcp
```

### Paso 2: Agregar Dependencias en `pom.xml`

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
    </dependency>
</dependencies>
```

### Paso 3: Crear tu Servicio con Herramientas

```java
@Service
public class TuServicio {

    @Tool(description = "Descripción clara de qué hace tu herramienta")
    public String tuMetodo(
        @ToolParam(description = "Qué representa este parámetro")
        String parametro
    ) {
        // Tu lógica aquí
        return "resultado";
    }
}
```

**Consejos para crear herramientas:**
- **Descripciones claras**: La IA las usa para decidir cuándo llamar tu herramienta
- **Manejo de errores**: Devuelve mensajes claros cuando algo falla
- **Parámetros simples**: Usa String, int, boolean (evita objetos complejos)
- **Una responsabilidad**: Cada herramienta debe hacer una cosa específica

### Paso 4: Registrar las Herramientas

```java
@SpringBootApplication
public class TuAplicacion {

    public static void main(String[] args) {
        SpringApplication.run(TuAplicacion.class, args);
    }

    @Bean
    public ToolCallbackProvider tusHerramientas(TuServicio servicio) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(servicio)
            .build();
    }
}
```

### Paso 5: Compilar y Ejecutar

```bash
# Compilar el proyecto
./mvnw clean package

# Ejecutar el servidor
java -jar target/mcp-0.0.1-SNAPSHOT.jar

# O directamente con Maven
./mvnw spring-boot:run
```

El servidor MCP se ejecutará y estará listo para recibir conexiones en modo STDIO (entrada/salida estándar).

## Cómo Usar el Servidor con Claude Desktop

### 1. Configurar Claude Desktop

Edita el archivo de configuración de Claude Desktop:

**macOS/Linux:** `~/Library/Application Support/Claude/claude_desktop_config.json`

**Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

Agrega tu servidor:

```json
{
  "mcpServers": {
    "pokemon-server": {
      "command": "java",
      "args": [
        "-jar",
        "/ruta/completa/a/tu/proyecto/target/mcp-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

### 2. Reiniciar Claude Desktop

Cierra completamente Claude Desktop y ábrelo de nuevo. El servidor se iniciará automáticamente.

### 3. Probar las Herramientas

En Claude Desktop, puedes hacer preguntas como:
- "¿Qué información tienes sobre Pikachu?"
- "Cuéntame sobre la habilidad 'overgrow'"
- "Dame los stats de Charizard"

Claude automáticamente usará las herramientas del servidor MCP para responder.

## Requisitos

- **Java 21** o superior
- **Maven 3.6+**
- **Spring Boot 3.4.12**
- **Spring AI 1.1.0**

## Instalar Java 21

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

### macOS
```bash
brew install openjdk@21
```

### Windows
Descarga e instala desde [Oracle](https://www.oracle.com/java/technologies/downloads/) o [Adoptium](https://adoptium.net/)

## Solución de Problemas

### Error: "Unsupported class file major version"
- Asegúrate de tener Java 21 instalado: `java -version`
- Verifica que Maven use Java 21: `mvn -version`

### El servidor no se conecta en Claude Desktop
- Verifica que la ruta del JAR en la configuración sea absoluta y correcta
- Revisa los logs de Claude Desktop
- Asegúrate de haber reiniciado completamente Claude Desktop

### La API no responde
- Verifica tu conexión a internet
- La PokéAPI es gratuita pero tiene límites de tasa
- Revisa los logs del servidor para ver errores HTTP

## Ideas para Extender el Proyecto

1. **Más herramientas:**
   - Buscar Pokémon por tipo
   - Obtener información sobre movimientos
   - Comparar stats de dos Pokémon

2. **Otras APIs:**
   - API del clima
   - API de noticias
   - API de bases de datos

3. **Funcionalidad local:**
   - Leer/escribir archivos
   - Ejecutar comandos del sistema
   - Consultar bases de datos locales

4. **Herramientas con estado:**
   - Mantener una lista de Pokémon favoritos
   - Historial de búsquedas
   - Cache de resultados

## Recursos Útiles

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [PokéAPI Documentation](https://pokeapi.co/docs/v2)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## Licencia

Este proyecto es de código abierto y está disponible para uso educativo.

---

**¡Feliz desarrollo con MCP y Spring AI!** 🚀
