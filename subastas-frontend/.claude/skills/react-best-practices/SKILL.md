# Skill: Buenas Prácticas en React y Conexión con Axios

## Propósito
Asegurar que todo el código de React escrito sea óptimo, escalable, no genere renders innecesarios y maneje correctamente las peticiones asíncronas con el backend de Spring Boot.

## Reglas de Desarrollo
- **Estructura de componentes:** Separar componentes visuales en la carpeta `src/components` y las vistas completas en `src/views` o `src/pages`.
- **Manejo de Estado:** Usar `useState` solo cuando sea necesario. Agrupar estados relacionados para evitar renderizados múltiples.
- **Efectos secundarios (`useEffect`):** Siempre definir correctamente el arreglo de dependencias. Limpiar temporizadores o suscripciones si se utilizan (esencial para los contadores de tiempo de las subastas).
- **Conexión API (Axios):** 
  - Centralizar la URL base (`http://localhost:8080`) en una instancia configurable de Axios en `src/services/api.js`.
  - Implementar bloques `try/catch` en todas las peticiones asíncronas para capturar errores de red o del servidor.
  - Mostrar estados visuales de "Cargando..." o "Error al conectar" en la interfaz mientras se espera la respuesta del backend.