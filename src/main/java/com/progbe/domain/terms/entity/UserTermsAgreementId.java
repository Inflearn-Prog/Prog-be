package com.progbe.domain.terms.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserTermsAgreementId implements Serializable {
    private Long userId;
    private Long termsId;
}
