package com.caibowen.gplume.misc.test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Bowen Cai on 3/11/2015.
 */

class Base {

    public static void main(String...sdfsd) throws Throwable {
        Thread.sleep(5000L);
        System.out.println("?");
    }
    int doCommand(int cmd, String optionalMsg, String logPath) throws Exception {
        int counter = 1;
        File f = new File(logPath);
        if (cmd == 1) {
            System.out.printf("(%d) HELLO", counter);
        } else if (cmd == 2) {
            System.out.printf(optionalMsg);
        }
        DataOutputStream s = new DataOutputStream(new FileOutputStream(f));
        s.writeUTF(String.format("Cmd %d DONE", counter));
        s.close();
        return counter;
    }
}
class Derived extends Base {
    @Override
    int doCommand(int cmd, String optionalMsg, String logPath) throws Exception {
        int counter = 1;
        int bytes = 0; // New variable
        File f = new File(logPath);
        if (cmd == 1) {
            System.out.printf("(%d) HELLO", counter);
            bytes += 4;
        } else if (cmd == 2) {
            System.out.printf(optionalMsg);
            bytes += optionalMsg.length();
            /* This option is new: */
        } else if (cmd == 3) {
            System.out.printf("(%d) BYE", counter);
            bytes += 3;
        }
        DataOutputStream s = new DataOutputStream(new FileOutputStream(f));
        s.writeUTF(String.format("Cmd %d\\%d DONE", counter, bytes));
        s.close();
        return counter;
    }
}



