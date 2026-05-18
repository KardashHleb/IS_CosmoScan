package com.cosmoscan.analysis.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Technical check outcome")
public enum ReportStatus {
    ACCEPTED,
    NEEDS_REVISION
}
