/*
 * Kantpoll Project
 * https://github.com/kantpoll
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kantpoll.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

class UserAuthentication {
    //Constants
    private static final String CHARSET_NAME = "UTF-8";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEYTSTORE_ALIAS = "KeystoreAlias";
    protected static final String PASSWORD_KEY = "password";
    private static final String ENC_IV_KEY = "encryptionIv";
    private static final String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
    private static final int AUTHENTICATION_DURATION_SECONDS = 30;

    //Global variables
    private final Activity context;

    /**
     * Constructor
     * @param the_context {Activity}
     */
    UserAuthentication(Activity the_context) {
        context = the_context;
    }

    /**
     * It encrypts and stores the voter's password
     * @param password {String}
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public void saveUserPassword(String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        SecretKey secretKey = createKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        //encrypt password
        byte[] encryptionIv = cipher.getIV();
        byte[] passwordBytes = password.getBytes(CHARSET_NAME);
        byte[] encryptedPasswordBytes = cipher.doFinal(passwordBytes);
        String encryptedPassword = Base64.encodeToString(encryptedPasswordBytes, Base64.DEFAULT);

        SharedPreferences.Editor editor = context.getSharedPreferences("com.kantpoll.android", Context.MODE_PRIVATE).edit();
        editor.putString(PASSWORD_KEY, encryptedPassword);
        editor.putString(ENC_IV_KEY, Base64.encodeToString(encryptionIv, Base64.DEFAULT));
        editor.apply();
    }

    /**
     * It creates a symmetric key to encrypt/decrypt the voter's password
     * @return a key
     */
    private SecretKey createKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEYTSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(AUTHENTICATION_DURATION_SECONDS)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    /**
     * It returns the voter's password if he or she has authenticated
     * @return {String}
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws UnrecoverableKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public String getUserPassword() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchPaddingException, UnrecoverableKeyException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.kantpoll.android", Context.MODE_PRIVATE);
        String base64EncryptedPassword = sharedPreferences.getString(PASSWORD_KEY, null);
        String base64EncryptionIv = sharedPreferences.getString(ENC_IV_KEY, null);

        if (base64EncryptedPassword == null || base64EncryptionIv == null) {
            return null;
        }

        byte[] encryptionIv = Base64.decode(base64EncryptionIv, Base64.DEFAULT);
        byte[] encryptedPassword = Base64.decode(base64EncryptedPassword, Base64.DEFAULT);

        // decrypt the password
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEYTSTORE_ALIAS, null);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(encryptionIv));

        byte[] passwordBytes = cipher.doFinal(encryptedPassword);

        return new String(passwordBytes, CHARSET_NAME);
    }
}
