package com.motorlab.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motorlab.dto.pieza.PiezaResponse;
import com.motorlab.entity.Pieza;

@Component
public class PiezaMapper {

    private final int umbralStockBajo;

    public PiezaMapper(@Value("${app.stock-bajo-umbral}") int umbralStockBajo) {
        this.umbralStockBajo = umbralStockBajo;
    }

    public PiezaResponse aResponse(Pieza pieza) {
        return new PiezaResponse(
                pieza.getId(),
                pieza.getReferencia(),
                pieza.getNombre(),
                pieza.getPrecio(),
                pieza.getStock(),
                pieza.isActiva(),
                pieza.getStock() <= umbralStockBajo);
    }
}
