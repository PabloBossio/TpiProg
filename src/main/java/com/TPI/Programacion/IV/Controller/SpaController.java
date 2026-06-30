package com.TPI.Programacion.IV.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Reenvía transparentemente a index.html cualquier ruta de navegación del SPA
 * que no corresponda a un endpoint de API ni a un archivo estático.
 *
 * Exclusiones en la regex:
 *  - api      → manejado por los @RestController
 *  - assets   → bundles JS/CSS generados por Vite (classpath:/static/assets/)
 *  - static   → posibles recursos bajo /static/
 *  - index.html, favicon.ico, favicon.svg, icons.svg → archivos raíz estáticos
 */
@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/{path:^(?!api|assets|static|index\\.html|favicon\\.ico|favicon\\.svg|icons\\.svg).*$}/**"
    })
    public String spa() {
        return "forward:/index.html";
    }
}
