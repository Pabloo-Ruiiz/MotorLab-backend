package com.motorlab.dto.common;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageResponse<T>(List<T> datos, int pagina, int limite, long total, int totalPaginas) {

    public static <T> PageResponse<T> de(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    public static Pageable pageable(Integer pagina, Integer limite) {
        int paginaNormalizada = pagina == null ? 1 : pagina;
        int limiteNormalizado = limite == null ? 20 : limite;
        if (paginaNormalizada < 1) {
            paginaNormalizada = 1;
        }
        if (limiteNormalizado < 1) {
            limiteNormalizado = 20;
        }
        return PageRequest.of(paginaNormalizada - 1, Math.min(limiteNormalizado, 100), Sort.unsorted());
    }
}
