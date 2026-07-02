// El backend serializa LocalDateTime (siempre UTC por convención de negocio) sin sufijo de
// zona horaria, ej. "2026-07-02T16:42:00". new Date(...) interpreta ese formato como hora
// LOCAL del navegador si no se le agrega la 'Z', desincronizando countdowns y fechas
// mostradas según el huso horario de quien mira la pantalla. Todo fetch de fecha que venga
// de la API debe pasar por acá antes de convertirse en Date.
export function parseServerDate(fechaIso) {
  if (!fechaIso) return null
  return new Date(fechaIso.endsWith('Z') ? fechaIso : `${fechaIso}Z`)
}
