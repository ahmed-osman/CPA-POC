package com.qi.cpa;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by aosman on 30/10/16.
 */
public class Cryptography {

    public static byte[] ComputeSessionKey (byte[] MasterKey, byte[] DiversificationData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{


        /*
        generate SecretKeySpec object from the Master Key data
         */
        SecretKeySpec skey = new SecretKeySpec(MasterKey, "DESede");

        /*
        create a Cipher
         */
        Cipher TDES;
        /*
        get the Cipher instance and determine the alogrithm, mode and padding
        in case of EMV 4.2 the algorithm is Triple DES, mode is ECB and no padding required
         */



        TDES = Cipher.getInstance("DESede/ECB/NoPadding");



        /*
        initiate the Cipher in encryption mode
         */



        TDES.init(Cipher.ENCRYPT_MODE, skey);

        /*
        encrypt the diversification data using the master key.
         */




        byte[] baSKey = TDES.doFinal(DiversificationData);
        return baSKey;



        }


    public static byte[] computeARQC(byte[] key, byte[] TransactionData)  {


        /**
         *split the key into two keys, left nad right
         */
        byte[] KeyLeft = new byte[8];
        byte[] KeyRight = new byte[8];
        System.arraycopy(key, 0, KeyLeft, 0, 8);
        System.arraycopy(key, 8, KeyRight, 0, 8);

        /**
         * generate SecretKeySpec object from the Key data

         */

        SecretKeySpec skeyl = new SecretKeySpec(KeyLeft, "DES");
        SecretKeySpec skeyr = new SecretKeySpec(KeyRight, "DES");

        /**
         * temp array used in the computation
         */
        byte[] current = new byte[8];

        /**
         * initial value of the ARQC
         */
        byte[] ARQC = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

        /**
         * add padding to transaction data
         * if data could be divided into blocks of 8 bytes then do nothing
         * if not then add the value 0x80 to the end of the array then add 0x00 padding
         * till the data could be divided into blocks of 8 bytes each
         */

        TransactionData = Utilities.padding(TransactionData);

        /**
         * create Cipher
         */

        Cipher des;


        try {

            /**
             * get the instance of the Cipher and specify
             * algorithm = DES
             * mode = ECB
             * no padding
             */
            des = Cipher.getInstance("DES/ECB/NoPadding");
            /**
             * initiate the Cipher in encryption mode, with the left key
             */
            des.init(Cipher.ENCRYPT_MODE, skeyl);

            for (int i = 0; i < TransactionData.length; i += 8) {
                /**
                 * copy the first block of 8 bytes from transaction data into the temp array
                 */
                System.arraycopy(TransactionData, i, current, 0, 8);

                /**
                 * xor the temp data with the ARQC
                 */
                ARQC = Utilities.xor(current, ARQC);

                /**
                 * ARQC is encryption of the xored value
                 * then continue looping on the data till the end of the 8 bytes blocks
                 */
                ARQC = des.update(ARQC);
            }

            /**
             * get the instance of the Cipher and specify
             * algorithm = DES
             * mode = ECB
             * no padding
             */
            des = Cipher.getInstance("DES/ECB/NoPadding");
            /**
             * initiate the Cipher in decryption mode, with the right key
             */
            des.init(Cipher.DECRYPT_MODE, skeyr);
            /**
             * decrypt the final block using the right key
             */
            ARQC = des.update(ARQC);
            /**
             * initiate the Cipher in encryption mode, with the left key
             */
            des.init(Cipher.ENCRYPT_MODE, skeyl);
            /**
             * encrypt the final block using the left key. this is the ARQC
             */
            ARQC = des.doFinal(ARQC);
        } catch(Exception e) {

        }
        return ARQC;
    }


    public static byte[] computeARPC (byte[] ARQC, byte[] sKey, byte[] ARC){

        byte[] ARPC  = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

        System.out.println();

        byte[] temp = Utilities.xor(ARC, ARQC);
        byte[] TDESKey = new byte[24];
        System.arraycopy(sKey, 0, TDESKey, 0, 16);
        System.arraycopy(sKey, 0,TDESKey,16,8);

        /**
         * generate SecretKeySpec object from the Key data

         */

        SecretKeySpec skey = new SecretKeySpec(TDESKey, "DESede");
       /**
         * create Cipher
         */
        Cipher TDES;

        try {
            /**
             * get the instance of the Cipher and specify
             * algorithm = TDES
             * mode = ECB
             * no padding
             */
            TDES = Cipher.getInstance("DESede/ECB/NoPadding");

            /**
             * initiate the Cipher in encryption mode, with the left key
             */
            TDES.init(Cipher.ENCRYPT_MODE, skey);

            /**
             * encrypt the final block using the left key. this is the ARQC
             */
            ARPC = TDES.doFinal(temp);


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return ARPC;



    }



}
