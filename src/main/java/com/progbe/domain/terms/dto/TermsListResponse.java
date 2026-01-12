package com.progbe.domain.terms.dto;

import java.util.List;

public record TermsListResponse(
        List<TermsResponseDto> terms
) {
}