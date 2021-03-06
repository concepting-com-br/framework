package br.com.concepting.framework.security.util;

import br.com.concepting.framework.security.constants.SecurityConstants;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Class that implements the Triple DES Cryptography.
 *
 * @author fvilarinho
 * @since 3.1.0
 *
 * <pre>Copyright (C) 2007 Innovative Thinking.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.</pre>
 */
public class Crypto3DES extends BaseCrypto{
    /**
     * Constructor - Defines the cryptography default parameters.
     *
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        this(SecurityConstants.DEFAULT_CRYPTO_3DES_KEY_SIZE);
    }
    
    /**
     * Constructor - Defines the cryptography parameters.
     *
     * @param keySize Numeric value that contains the length of the cryptography
     * key.
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES(Integer keySize) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        this(keySize, true);
    }
    
    /**
     * Constructor - Defines the cryptography parameters.
     *
     * @param passPhrase String that contains cryptography key.
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES(String passPhrase) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        this(passPhrase, SecurityConstants.DEFAULT_CRYPTO_3DES_KEY_SIZE);
    }
    
    /**
     * Constructor - Defines the cryptography parameters.
     *
     * @param passPhrase String that contains cryptography key.
     * @param keySize Numeric value that contains the length of the cryptography
     * key.
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES(String passPhrase, Integer keySize) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        this(passPhrase, keySize, true);
    }
    
    /**
     * Constructor - Defines the cryptography parameters.
     *
     * @param useBase64 Indicates if the encrypted message should be encoded
     * using Base64.
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES(Boolean useBase64) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        this(SecurityConstants.DEFAULT_CRYPTO_3DES_KEY_SIZE, useBase64);
    }
    
    /**
     * Constructor - Defines the cryptography parameters.
     *
     * @param keySize Numeric value that contains the length of the cryptography
     * key.
     * @param useBase64 Indicates if the encrypted message should be encoded
     * using Base64.
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES(Integer keySize, Boolean useBase64) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        super(SecurityConstants.DEFAULT_CRYPTO_3DES_ALGORITHM_ID, null, keySize, useBase64);
    }
    
    /**
     * Constructor - Defines the cryptography parameters.
     *
     * @param passPhrase String that contains cryptography key.
     * @param useBase64 Indicates if the encrypted message should be encoded
     * using Base64.
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES(String passPhrase, Boolean useBase64) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        this(passPhrase, SecurityConstants.DEFAULT_CRYPTO_3DES_KEY_SIZE, useBase64);
    }
    
    /**
     * Constructor - Defines the cryptography parameters.
     *
     * @param passPhrase String that contains cryptography key.
     * @param keySize Numeric value that contains the length of the cryptography
     * key.
     * @param useBase64 Indicates if the encrypted message should be encoded
     * using Base64.
     * @throws InvalidKeyException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchAlgorithmException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchPaddingException Occurs when was not possible to execute
     * the operation.
     * @throws InvalidKeySpecException Occurs when was not possible to execute
     * the operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     */
    public Crypto3DES(String passPhrase, Integer keySize, Boolean useBase64) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException{
        super(SecurityConstants.DEFAULT_CRYPTO_3DES_ALGORITHM_ID, passPhrase, keySize, useBase64);
    }
    
    /**
     * @see br.com.concepting.framework.security.util.BaseCrypto#generateKey()
     */
    protected SecretKey generateKey() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
        if(this.passPhrase == null || this.passPhrase.length() == 0)
            return super.generateKey();
        
        String keyDigest = new CryptoDigest().encrypt(this.passPhrase);
        byte[] keyBytes = Arrays.copyOf(keyDigest.getBytes(), 24);
        
        for(int j = 0, k = 16; j < 8; )
            keyBytes[k++] = keyBytes[j++];
        
        KeySpec keySpec = new DESedeKeySpec(keyBytes);
        SecretKey key = SecretKeyFactory.getInstance(this.algorithm).generateSecret(keySpec);
        
        return key;
    }
}