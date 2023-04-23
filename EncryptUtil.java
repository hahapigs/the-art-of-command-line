package org.dwt.admin.utils;


import org.dwt.admin.constant.HeaderConstant;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p>加密工具类</p>
 *
 * @ClassName EncryptUtil
 * @Description 加密工具类
 * @Author zhaohongliang
 * @Date 2022-11-29 09:15
 * @Since 1.0
 */
public class EncryptUtil {

    private static final String CONTENT_CHARSET = "UTF-8";

    private static final String ENCRYPT = "RSA";

    private static final String HMAC_ALGORITHM = "HmacSHA1";

    private static final String ENCODE_ALGORITHM = "SHA-256";

    private static final String SIGNATURE_ALOGORITHM = "SHA256withRSA";

    private static final String DWT_PUBLICK_KEY = "key/dwt-key.pub";

    /**
     * 获取 Authorization
     * 腾讯云请求接口鉴权请求头
     *
     * @param secretId
     * @param sign
     * @return
     */
    public static String getAuthon(String secretId, String sign) {
        return "hmac id=\"" + secretId + "\", algorithm=\"hmac-sha1\", headers=\"date source\", signature=\"" + sign +"\"";
    }


    /**
     * 获取签名
     * 腾讯云请求接口鉴权签名
     *
     * @param secret
     * @param timeStr
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static String getSign(String secret, String timeStr) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        // 获取签名字符串，Source是签名水印值，可填写任意值
        String signStr = "date: " + timeStr + "\n" + "source: " + HeaderConstant.TENCENT_YXT_SOURCE;
        // 获取接口签名
        Mac mac1 = Mac.getInstance(HMAC_ALGORITHM);
        byte[] hash;
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(CONTENT_CHARSET), mac1.getAlgorithm());
        mac1.init(secretKey);
        hash = mac1.doFinal(signStr.getBytes(CONTENT_CHARSET));
        return Base64.getEncoder().encodeToString(hash);
    }


    /**
     * 获取报文签名
     *
     * @param plainText 参与签名字段+格式后的字符串
     * @param privateKey  私钥
     * @return
     */
    public static String getSign(String plainText, PrivateKey privateKey) {

        if (null == plainText) {
            return "";
        }
        try {
            // sha-256 摘要计算
            // MessageDigest messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
            // messageDigest.update(plainText.getBytes(StandardCharsets.UTF_8));
            // byte[] hashed = messageDigest.digest();

            Signature signature = Signature.getInstance(SIGNATURE_ALOGORITHM);
            signature.initSign(privateKey);
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] signedData = signature.sign();
            return Base64.getEncoder().encodeToString(signedData);
        } catch (Exception e) {
            throw new RuntimeException("encrypt sign is error!");
        }
    }

    /**
     * 验证签名
     *
     * @param plainText
     * @param sign
     * @param publicKey
     * @return
     */
    public static boolean verifySign(String plainText, String sign, PublicKey publicKey) {
        try {
            byte[] decodedByte = Base64.getDecoder().decode(sign);
            Signature signature = Signature.getInstance(SIGNATURE_ALOGORITHM);
            signature.initVerify(publicKey);
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            return signature.verify(decodedByte);
        } catch (Exception e) {
            throw new RuntimeException("verify sign is error!");
        }

    }

    /**
     * 从文件中获取公钥匙字符串
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String loadPublicKeyByFile(String filePath) {

        try {
            // 方式一
            // InputStream is = EncryptUtil.class.getClassLoader().getResourceAsStream("key/dwt-key.pub");

            // 方式二
            // InputStream is = EncryptUtil.class.getResource("/key/dwt-key.pub").openStream();

            // 方式三
            // Resource resource = new ClassPathResource("key/dwt-key.pub");
            // InputStream is = resource.getInputStream();

            // 方式四
            // String url = EncryptUtil.class.getResource("/key/dwt-key.pub").getFile();
            // File file = new File(url);
            // FileInputStream fis = new FileInputStream(file);
            // InputStream is = new InputStreamResource(fis).getInputStream();

            // 方式五
            // BufferedReader bf = new BufferedReader(new FileReader("/Users/zhaohongliang/marketing/backend/dwt/dwt-admin-app/src/main/resources/key/dwt-key.pub"));


            InputStream is = EncryptUtil.class.getClassLoader().getResourceAsStream(filePath);
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String readLine = null;
            while ((readLine = bf.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    // sb.append('\r');
                }
            }
            bf.lines();
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("load public key by path is error!");
        }
    }

    /**
     * 获取公钥
     *
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKeyByStr(String publicKeyStr) {
        try {
            byte[] bytes = Base64.getDecoder().decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception  e ) {
            throw new RuntimeException("get public key is error!");
        }
    }

    /**
     * 获取公钥
     *
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKeyByFile() {
        try {
            String publickKeyStr = EncryptUtil.loadPublicKeyByFile(DWT_PUBLICK_KEY);
            byte[] bytes = Base64.getDecoder().decode(publickKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("get public key is error!");
        }
    }

    /**
     * 获取公钥
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKeyByFile(String filePath) {
        try {
            String publickKeyStr = EncryptUtil.loadPublicKeyByFile(filePath);
            byte[] bytes = Base64.getDecoder().decode(publickKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | NullPointerException | InvalidKeySpecException e ) {
            throw new RuntimeException("load public key by file is error!");
        }
    }

    /**
     * 公钥加密
     *
     * @param plainText
     * @param publicKey
     * @return
     */
    public static String encrypt(String plainText, PublicKey publicKey) {
        if (null == publicKey) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPT);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return new String(output, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            throw new RuntimeException("encrypt is error!");
        }
    }


    /**
     * 公钥解密
     *
     * @param plainText
     * @param publicKey
     * @return
     */
    public static String decrypt(String plainText, PublicKey publicKey) {
        if (null == publicKey) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPT);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return new String(output, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            throw new RuntimeException("decrypt is error!");
        }
    }


    /**
     * 计算给定文本 SHA256 散列值
     *
     * @param plainText 文本
     * @return SHA256   散列值
     */
    public static byte[] sha256(String plainText) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
        messageDigest.update(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] hashed = messageDigest.digest();
        // System.out.println(bytesToHex(hashed));
        return hashed;
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)  {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }


    public static void main(String[] args) throws Exception {

        // 时间戳
        // Long timestamp = System.currentTimeMillis() / 1000;
        Long timestamp = 1668396821L;
        // 随机串
        String noce = "bX0jFwyTRHYnsj9UgJoyROQiGdaj3A1k";
        // body
        String body = "{\"CorpId\":\"1234567890\"}";

        // 验签名串
        String plainText = timestamp + "\n" + noce + "\n" + body;
        System.out.println("【验签名串】:");
        System.out.println(plainText);

        // 公钥字符串
        String publicKeyStr = EncryptUtil.loadPublicKeyByFile(DWT_PUBLICK_KEY);
        System.out.println("【dwt公钥】:");
        System.out.println(publicKeyStr);

        // 公钥
        PublicKey publicKey = EncryptUtil.getPublicKeyByFile(DWT_PUBLICK_KEY);

        // String signature = "WxQRLjWCS7JNgyQ6NysNhbSt14zJ+t7Al3F6A1nWBRch8/2/s5VaQNQUJ5ahtMSCneiwvY/ZtxY/RGiJ6MFQo5RXQ2+zJROTcmg8MmJimPZifh0h989/Nnkvvxa+fvKry814xOmqftSesfBaWGGwPxP4TJEV2JpvBH8zfp6a4+vWBtOVaeheVygGxJPJuYfxLyCR8BRywved5C9NmW+5Kdt8A57t9hOif1sCdSMF120Q8PngwzDAMcPHoCedQw/ik1imHyvQUhn+aG55qvItQLKGm3f8+tPZL4Cz9Kr1PdnR+qScPlXvuexLLUQD7fe47q0+rcZ3p7QULhAWxTeJoQ==";
        String signature = "ggH4iq66GG8t554TZbWj4BH3v7huATfQV2JKNo56S8bJp9nkAd+X8IZK76RDdO0KbHJO3bYGub+Fdtd/NFV92p1jLXaRWwXQniTo5JKixQLLF50iNdB8BO9xouQ/SBfniHsJPlhYvCM6liAa+dGBVZwH2fXxe8nAi1TCOlCwZjgj3dbCAhGn6/ZT7ERFQvCVGKGiWZds7CGshADb+gaJJxm6n2tkxkM8VJrWLBHUUlr/OUT6wVk9HJ38Nr80uklDmuo2wcJAYf4H75uYUlxaGfV1lUnHG+GnP0/HWr/LnyUPzUvkG1+Sy6f6X6umwWIu7UuJxGB1oOZXt31Ha1aCKA==";
        System.out.println("【应答签名】:");
        System.out.println(signature);

        // 验证签名
        System.out.println("【签名结果】:");
        System.out.println(EncryptUtil.verifySign(plainText, signature, publicKey));


    }
}
