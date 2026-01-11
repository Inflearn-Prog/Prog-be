package com.progbe.domain.terms.dto;

import java.util.List;

public record TermsAgreementRequest(
        List<Long> agreedTermIds
) {
}
