package com.qi.cpa;

/*
* This application is aimed towards proving two things:
*
 * 1. the EMV functions associated with the CPA cards required during transaction authorization
 * could be achieved with native Java code without the need for external libraries such as
 * Bouncy-Castle or HSM device.
 *
 * 2. provide a step by step guide for the functionality of the CPA application to the developer
  * who will port it to the enterprise application
  *
  *
* Application functionality:
* 1. generate session key out of the master key
* 2. compute the ARQC
* 3. compute the ARPC
*
*
 */

public class Main {

    public static void main(String[] args) {

        /*
        The start point would be defining the static values of the input parameters such as
        master key, Application Transaction Counter (ATC) and transaction data.
        in live scenario, these values will be dynamic. the master key will be pre-stored in the
        application while the ATC and transaction data will be acquired from DE-55 of the ISO message
         */

        String strMKey = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
        String strATC = "00B4";

        /*

        The values of the data won't be available as String in live scenario hence we should convert
       `the above values into byte arrays before we start

        */

        byte[] baMKEY = Utilities.hexStringToByteArray(strMKey);
        byte[] baATC = Utilities.hexStringToByteArray(strATC);

        /*
        EMV 4.2 session key generation method:

        the algorithm for creation the session key is:
        session key = TDESmk (diversification Data) 'encrypt the diversification data using Maskter Key and TDES algorithm'

        the ATC value is the diversification data used to create the session key.
        because the session key is double length (16 bytes) the ATC shoud be concatednated into 16 bytes
        the logic of the concatenation is as follows:

        ATC = ATCleft + ATCright

        ATCleft = ATC + 0xF0 + 5 bytes 0x00
        ATCright = ATC + 0x0F + 5 bytes 0x00


         */


        String strAddLeft = "F00000000000";
        byte[] addLeft = Utilities.hexStringToByteArray(strAddLeft);
        byte[] baATCleft = Utilities.concat(baATC, addLeft);

        String strAddRight = "0F0000000000";
        byte[] addRight = Utilities.hexStringToByteArray(strAddRight);
        byte[] baATCright = Utilities.concat(baATC, addRight);

        /**
         * the transaction data is extracted from field 55 from the ISO message in live scenario
         */

        String strTransactionData = "0000000010000000000000000710000000000007101302050030901B6A3C00005503A4A082";
        byte[] baTransactionData = Utilities.hexStringToByteArray(strTransactionData);

        /**
         * baDiversificationData has the full data should be encrypted by MK to create the session key
         */

        byte[] baDiversificationData = Utilities.concat(baATCleft, baATCright);


        try{

            /**
             *generate session key
             *this value is assuming parity = none. in case required to adjust parity to ODD then it
             *needs to be added
             */

            byte[] SessionKey = Cryptography.ComputeSessionKey(baMKEY, baDiversificationData);

            System.out.print("Session Key Value is: ");
            for (byte a : SessionKey){
                System.out.printf("%X \t", a);
            }

            /**
             * After successfully generating the session key, now we need to created the ARQC
             * the alogrithm is create MAC for the transaction data using the session key created above.
             */

            byte[] ARQC = Cryptography.computeARQC(SessionKey,baTransactionData);

            System.out.print("\nARQC Value is: ");

            for (byte a : ARQC){
                System.out.printf("%X \t", a);
            }



        } catch (Exception e){

        }

    }
}
