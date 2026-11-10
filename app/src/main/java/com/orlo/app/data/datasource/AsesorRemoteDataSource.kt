package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.OutfitDelDiaDto
import com.orlo.app.data.dtos.OutfitDelDiaPedidoDto
import com.orlo.app.data.dtos.RespuestaAsesorDto

// Contrato del asesor con IA
interface AsesorRemoteDataSource {

    suspend fun enviarMensaje(mensaje: MensajeAsesorDto): RespuestaAsesorDto
    suspend fun outfitDelDia(pedido: OutfitDelDiaPedidoDto): OutfitDelDiaDto
}
