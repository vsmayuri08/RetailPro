import React, { useEffect, useRef } from 'react';
import { Html5Qrcode } from 'html5-qrcode';

const BarcodeScanner = ({ onScan, onError }) => {
  const regionId = useRef(`barcode-reader-${Math.random().toString(36).slice(2)}`);
  const scannerRef = useRef(null);
  const lastCode = useRef('');
  const lastAt = useRef(0);

  useEffect(() => {
    const scanner = new Html5Qrcode(regionId.current);
    scannerRef.current = scanner;

    scanner.start(
      { facingMode: 'environment' },
      { fps: 8, qrbox: { width: 240, height: 140 } },
      (decoded) => {
        const now = Date.now();
        if (decoded === lastCode.current && now - lastAt.current < 1500) {
          return;
        }
        lastCode.current = decoded;
        lastAt.current = now;
        onScan(decoded);
      },
      () => {},
    ).catch((err) => {
      onError?.(err?.message || 'Camera could not be started. Allow camera access, or type the barcode in search.');
    });

    return () => {
      scanner.stop().catch(() => {});
      scanner.clear().catch(() => {});
    };
  }, [onScan, onError]);

  return (
    <div className="barcode-scanner">
      <div id={regionId.current} />
      <p className="form-hint" style={{ textAlign: 'center', marginTop: '0.5rem' }}>
        Point the camera at a barcode or QR code. Seeded products use the SKU as the barcode (e.g. GRO-RICE-5KG).
      </p>
    </div>
  );
};

export default BarcodeScanner;
