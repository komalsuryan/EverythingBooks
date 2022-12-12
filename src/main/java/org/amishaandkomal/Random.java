package org.amishaandkomal;

import com.password4j.Hash;
import com.password4j.Password;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Random {
    public static void main(String[] args) throws IOException {
        String choice = "1";
        DataInputStream dataInputStream = new DataInputStream(System.in);
        while (choice != "0") {
            Hash hash = Password.hash("password").withBcrypt();
            System.out.println(hash.getResult());
            choice = dataInputStream.readLine();
        }
    }
}
