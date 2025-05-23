# 📅 Agenda de Ocupación de Espacios

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

</div>

## 📋 Descripción
Aplicación web desarrollada en Spring Boot que permite gestionar y visualizar la ocupación de espacios mediante una agenda interactiva. La aplicación procesa archivos de configuración y peticiones para generar una vista visual de la ocupación de espacios, facilitando la gestión de reservas y la detección de conflictos.

## ✨ Características Principales
- 📅 Visualización de agenda por mes y año
- 🏢 Gestión múltiple de espacios/salas
- 📊 Visualización semanal de ocupación
- ⚠️ Detección automática de conflictos
- 🎨 Interfaz intuitiva y responsive
- 📝 Soporte para diferentes estados de ocupación:
  - 🟢 Libre (verde claro)
  - 🔴 Ocupado (rojo claro)
  - ⚪ Bloqueado (gris claro)

## 🛠️ Requisitos Técnicos
- Java 17 o superior
- Maven 3.6 o superior
- Navegador web moderno

## 🚀 Instalación
1. Clonar el repositorio:
```bash
git clone https://github.com/Rediaj04/Agenda-de-ocupacion-de-espacios.git
cd Agenda-de-ocupacion-de-espacios
```

2. Compilar el proyecto:
```bash
./mvnw clean install
```

3. Ejecutar la aplicación:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## 📖 Uso
1. Acceder a la aplicación a través del navegador
2. Subir los archivos necesarios:
   - `config.txt`: Archivo de configuración con los parámetros generales
   - `peticions.txt`: Archivo con las peticiones de ocupación
3. La aplicación procesará los archivos y generará una vista visual de la agenda
4. Navegar entre diferentes salas usando los botones de navegación
5. Revisar las incidencias detectadas en caso de conflictos

## 📁 Estructura de Archivos
- `src/main/java`: Código fuente de la aplicación
- `src/main/resources`: Recursos estáticos y plantillas

## 🛠️ Tecnologías Utilizadas
- Spring Boot - Framework principal para el desarrollo de la aplicación
- Thymeleaf - Motor de plantillas para la generación de vistas
- HTML5/CSS3 - Estructura y estilos de la interfaz de usuario
- JavaScript - Interactividad y funcionalidades del lado del cliente
- Maven - Gestión de dependencias y construcción del proyecto

## 🤝 Contribuidores
<table>
  <tr>
    <td align="center">
      <a href="https://github.com/SoyManoolo">
        <img src="https://avatars.githubusercontent.com/SoyManoolo" width="100px;" alt="SoyManoolo"/>
        <br />
        <sub><b>SoyManoolo</b></sub>
      </a>
      <br />
      <sub>Desarrollador Backend</sub>
    </td>
    <td align="center">
      <a href="https://github.com/Rediaj04">
        <img src="https://avatars.githubusercontent.com/Rediaj04" width="100px;" alt="Rediaj04"/>
        <br />
        <sub><b>Rediaj04</b></sub>
      </a>
      <br />
      <sub>Desarrollador Frontend</sub>
    </td>
    <td align="center">
      <a href="https://github.com/ireneurbano">
        <img src="https://avatars.githubusercontent.com/ireneurbano" width="100px;" alt="ireneurbano"/>
        <br />
        <sub><b>ireneurbano</b></sub>
      </a>
      <br />
      <sub>Desarrolladora Frontend</sub>
    </td>
  </tr>
</table>

## 📝 Contribución
Las contribuciones son bienvenidas. Por favor, sigue estos pasos:
1. Haz un Fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia
Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 🔗 Enlaces
- [Repositorio del Proyecto](https://github.com/Rediaj04/Agenda-de-ocupacion-de-espacios)

---

<div align="center">
  <sub>Construido con ❤️ por <a href="https://github.com/Rediaj04">Rediaj04</a></sub>
</div>
