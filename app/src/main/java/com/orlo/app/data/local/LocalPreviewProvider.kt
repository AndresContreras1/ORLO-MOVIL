package com.orlo.app.data.local

import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.ConversacionUi
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.CuestionarioUi
import com.orlo.app.data.model.EjemploUi
import com.orlo.app.data.model.ElementoHistorialUi
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.GuardadoUi
import com.orlo.app.data.model.IdeaCombinarUi
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.LeccionResumenUi
import com.orlo.app.data.model.LeccionUi
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.OcasionOutfit
import com.orlo.app.data.model.OpcionUi
import com.orlo.app.data.model.OrigenOutfit
import com.orlo.app.data.model.OutfitDelDiaUi
import com.orlo.app.data.model.OutfitPropuestoUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PreguntaUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.RepasoUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.data.model.SugerenciaArmarioUi
import com.orlo.app.data.model.TiendaUi

// Datos locales de ejemplo para los previews y las pruebas con mocks. No los usa la app en tiempo
// de ejecucion: los datos reales vienen de Firebase y del backend
object LocalPreviewProvider {

    private const val FECHA = 1_794_250_000_000L

    val estiloUrbano = EstiloUi(
        id = "2",
        code = "urbano",
        nombre = "Urbano",
        descripcion = "Prendas cómodas con un toque actual: tenis, jeans y capas que se ven bien sin esfuerzo.",
        paleta = listOf("#2B2622", "#3D5A80", "#98C1D9", "#E0FBFC", "#EE6C4D"),
        galeria = listOf(
            "https://images.unsplash.com/photo-1523398002811-999ca8dec234",
            "https://images.unsplash.com/photo-1529139574466-a303027c1d8b",
        ),
    )

    val estiloCasual = EstiloUi(
        id = "1",
        code = "casual",
        nombre = "Casual",
        descripcion = "Prendas cómodas y fáciles de combinar para el día a día.",
        paleta = listOf("#33658A", "#86BBD8", "#F6F1E9", "#D9A066"),
    )

    val resultado = ResultadoUi(id = "r1", principal = estiloUrbano, secundario = estiloCasual, fecha = FECHA)

    val perfil = PerfilUi(
        nombre = "Laura Gómez",
        rangoEdad = "25_34",
        tratoPrendas = "femenino",
        clima = "templado",
        ocasiones = listOf("trabajo", "salidas"),
        consentimientoDatosSensibles = true,
        tonoPiel = "piel_media",
        colorCabello = "cabello_castano_oscuro",
        colorOjos = "ojos_cafes",
        resultadoActual = resultado,
    )

    val cuenta = CuentaUi(uid = "u1", premium = true, limiteDiario = 20, mensajesRestantes = 17)

    val cuestionario = CuestionarioUi(
        version = 1,
        preguntas = listOf(
            PreguntaUi(
                id = "1", texto = "¿Qué prendas usas más en la semana?", multiple = true,
                opciones = listOf(OpcionUi("10", "Camisetas y jeans"), OpcionUi("11", "Camisas y pantalón de tela"), OpcionUi("12", "Ropa deportiva")),
            ),
            PreguntaUi(
                id = "2", texto = "¿Cómo quieres sentirte al vestirte?",
                opciones = listOf(OpcionUi("20", "Cómodo"), OpcionUi("21", "Seguro"), OpcionUi("22", "Original")),
            ),
        ),
    )

    val lecciones = listOf(
        LeccionResumenUi(id = "1", titulo = "Conoce tu cuerpo", resumen = "Cómo los cortes cambian lo que se ve.", orden = 1, muestraGratis = true, minutos = 4, estado = EstadoLeccion.COMPLETADA),
        LeccionResumenUi(id = "2", titulo = "Aprende sobre las proporciones", resumen = "El largo de cada prenda cambia el equilibrio.", orden = 2, minutos = 4, estado = EstadoLeccion.EN_CURSO),
        LeccionResumenUi(id = "3", titulo = "Construye tu armario", resumen = "Prendas versátiles y combinar lo que ya tienes.", orden = 3, minutos = 5),
    )

    val leccion = LeccionUi(
        id = "4",
        titulo = "Cómo combinar los colores",
        ideaPrincipal = "Los colores neutros combinan con casi todo y te ayudan a probar un color nuevo sin miedo.",
        ejemplos = listOf(
            EjemploUi(texto = "Jean azul con camiseta blanca: la base que nunca falla."),
            EjemploUi(texto = "Un suéter de color con pantalón beige."),
        ),
        consejo = "Hoy prueba una prenda de color con todo lo demás neutro.",
        repaso = RepasoUi(pregunta = "¿Qué color combina con casi todo?", opciones = listOf("Un neutro", "Un fucsia"), indiceCorrecto = 0),
        usaColorimetria = true,
    )

    val prendas = listOf(
        PrendaUi(id = "p1", nombre = "Jean azul", categoria = CategoriaPrenda.ABAJO, color = "azul", favorita = true, fecha = FECHA),
        PrendaUi(id = "p2", nombre = "Camiseta blanca", categoria = CategoriaPrenda.ARRIBA, color = "blanco", fecha = FECHA - 1),
        PrendaUi(id = "p3", nombre = "Tenis blancos", categoria = CategoriaPrenda.CALZADO, color = "blanco", fecha = FECHA - 2),
        PrendaUi(id = "p4", nombre = "Chaqueta de jean", categoria = CategoriaPrenda.ABRIGO, color = "celeste", fecha = FECHA - 3),
    )

    val outfits = listOf(
        OutfitUi(id = "o1", nombre = "Lunes de clase", ocasion = OcasionOutfit.UNIVERSIDAD, prendasIds = listOf("p1", "p2", "p3"), favorito = true, fecha = FECHA),
        OutfitUi(
            id = "o2", nombre = "Salida con amigos", ocasion = OcasionOutfit.SALIDA,
            prendasTexto = listOf("Jean azul", "Camisa estampada", "Tenis blancos"),
            porQue = "Relajado pero con un toque distinto.", origen = OrigenOutfit.ASESOR, fecha = FECHA - 5,
        ),
    )

    val conversacion = ConversacionUi(id = "c1", titulo = "¿Qué me pongo para la universidad?", fecha = FECHA - 10)

    val mensajes = listOf(
        MensajeChatUi(id = "m1", rol = RolMensaje.USUARIO, texto = "¿Qué me pongo para la universidad?", fecha = FECHA),
        MensajeChatUi(
            id = "m2", rol = RolMensaje.ASESOR, texto = "Aquí van dos ideas con lo que ya tienes:",
            outfits = listOf(
                OutfitPropuestoUi("Cómodo y fresco", listOf("Jean azul", "Camiseta blanca", "Tenis blancos"), "Fácil de combinar y cómodo para caminar."),
                OutfitPropuestoUi("Con una capa", listOf("Jean azul", "Camiseta blanca", "Chaqueta de jean"), "La chaqueta te sirve si refresca."),
            ),
            fecha = FECHA + 1,
        ),
    )

    val historial = listOf(
        ElementoHistorialUi.Outfit(outfits.first()),
        ElementoHistorialUi.Resultado(resultado),
        ElementoHistorialUi.Conversacion(conversacion),
    )

    val itemsExplora = listOf(
        ItemExploraUi(
            id = "17",
            nombre = "Blazer azul oscuro",
            imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Navy_blazer_jacket.jpg/960px-Navy_blazer_jacket.jpg",
            credito = "Wikimedia Commons",
            categoria = CategoriaPrenda.ABRIGO,
            color = "azul",
            estilos = listOf("clasico", "elegante"),
            porQueTeQueda = "El blazer azul oscuro arregla al instante; sobre camiseta se ve moderno.",
            ideas = listOf(
                IdeaCombinarUi(contexto = "casual", prendas = listOf("Jean azul oscuro", "Tenis blancos clásicos")),
                IdeaCombinarUi(contexto = "elegante", prendas = listOf("Camisa por dentro", "Mocasines de cuero")),
            ),
        ),
        ItemExploraUi(
            id = "5",
            nombre = "Tenis blancos",
            categoria = CategoriaPrenda.CALZADO,
            color = "blanco",
            porQueTeQueda = "Van con casi todo y bajan el tono de lo formal.",
            tendencia = true,
        ),
    )

    val sugerencias = listOf(
        SugerenciaArmarioUi(
            categoria = CategoriaPrenda.ABRIGO,
            titulo = "Una chaqueta o abrigo",
            razon = "Con una capa encima, lo que ya tienes sirve para más ocasiones.",
            items = itemsExplora,
        ),
    )

    val tiendas = listOf(
        TiendaUi(id = "1", nombre = "Tienda de ejemplo", tipos = listOf("Camisas", "Pantalones"), url = "https://example.com"),
    )

    val guardados = listOf(
        GuardadoUi(itemId = "17", nombre = "Blazer azul oscuro", categoria = CategoriaPrenda.ABRIGO, fecha = FECHA),
    )

    val outfitDelDia = OutfitDelDiaUi(
        titulo = "Lunes cómodo y arreglado",
        prendas = listOf("Jean azul", "Camisa blanca", "Tenis blancos"),
        porQue = "Los neutros te dejan sumar color con un accesorio.",
        fecha = FECHA,
    )
}
