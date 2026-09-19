package com.yxlanyu.refreshrate.adb;
import android.content.Context;
import android.os.Build;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import io.github.muntashirakon.adb.AbsAdbConnectionManager;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
public class AdbConnectionManager extends AbsAdbConnectionManager {
    private static AbsAdbConnectionManager INSTANCE;
    public static AbsAdbConnectionManager getInstance(Context context) throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new AdbConnectionManager(context.getApplicationContext());
        }
        return INSTANCE;
    }
    public static void reset() {
        INSTANCE = null;
    }
    public static AbsAdbConnectionManager newInstance(Context context) throws Exception {
        return new AdbConnectionManager(context.getApplicationContext());
    }
    private PrivateKey mPrivateKey;
    private Certificate mCertificate;
    private final File keyFile;
    private final File certFile;
    private AdbConnectionManager(Context context) throws Exception {
        setApi(Build.VERSION.SDK_INT);
        keyFile = new File(context.getFilesDir(), "adbkey");
        certFile = new File(context.getFilesDir(), "adbcert.pem");
        mPrivateKey = readPrivateKey();
        mCertificate = readCertificate();
        if (mPrivateKey == null || mCertificate == null) {
            generate();
        }
    }
    private void generate() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();
        mPrivateKey = keyPair.getPrivate();
        X500Name x500 = new X500Name("CN=RefreshRate");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 86400000L);
        Date notAfter = new Date(System.currentTimeMillis() + 10L * 365 * 24 * 60 * 60 * 1000);
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                x500, serial, notBefore, notAfter, x500, keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(mPrivateKey);
        mCertificate = new JcaX509CertificateConverter().getCertificate(builder.build(signer));
        FileOutputStream kos = new FileOutputStream(keyFile);
        kos.write(mPrivateKey.getEncoded());
        kos.close();
        FileOutputStream cos = new FileOutputStream(certFile);
        cos.write(mCertificate.getEncoded());
        cos.close();
    }
    private PrivateKey readPrivateKey() {
        try {
            if (!keyFile.exists()) return null;
            byte[] bytes = readAll(keyFile);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception e) {
            return null;
        }
    }
    private Certificate readCertificate() {
        try {
            if (!certFile.exists()) return null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            FileInputStream fis = new FileInputStream(certFile);
            Certificate c = cf.generateCertificate(fis);
            fis.close();
            return c;
        } catch (Exception e) {
            return null;
        }
    }
    private static byte[] readAll(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = fis.read(buf)) > 0) bos.write(buf, 0, n);
        fis.close();
        return bos.toByteArray();
    }
    @Override
    protected PrivateKey getPrivateKey() {
        return mPrivateKey;
    }
    @Override
    protected Certificate getCertificate() {
        return mCertificate;
    }
    @Override
    protected String getDeviceName() {
        return "RefreshRate";
    }
}
