package org.kopingenieria.util.security.crl;

import java.security.cert.CRL;
import java.util.Optional;

public class CrlConfig {

    private String localCrlFile;
    private CRL certificateRevocationList;

    public CrlConfig(String localCrlFile) {
        this.localCrlFile = localCrlFile;
    }

    public CrlConfig(CRL crl) {
        this.certificateRevocationList = crl;
    }

    public String getLocalCrlFile() {
        return localCrlFile;
    }

    public Optional<CRL> getCertificateRevocationList() {
        return Optional.ofNullable(certificateRevocationList);
    }

    public void setLocalCrlFile(String localCrlFile) {
        this.localCrlFile = localCrlFile;
    }

    public void setCertificateRevocationList(CRL crl) {
        this.certificateRevocationList = crl;
    }
}
