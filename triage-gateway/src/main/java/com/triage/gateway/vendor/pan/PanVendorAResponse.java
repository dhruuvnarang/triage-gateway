package com.triage.gateway.vendor.pan;

/** Response body PAN-A returns. Wire format only; never leaves the adapter package. */
public record PanVendorAResponse(String pan, boolean valid, String name) {
}
