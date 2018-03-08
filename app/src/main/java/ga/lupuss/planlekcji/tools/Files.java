package ga.lupuss.planlekcji.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ga.lupuss.simplehttp.SimpleHttp;

public class Files {

    public static byte[] readAllBytes(File file) throws IOException {

        BufferedInputStream in = null;

        try {

            in = new BufferedInputStream(new FileInputStream(file));

            ByteArrayOutputStream array = new ByteArrayOutputStream();

            final int BLOCK_SIZE = 1024;

            byte[] block = new byte[BLOCK_SIZE];

            int length;

            while ((length = in.read(block)) != -1) {

                array.write(block, 0, length);
            }

            return array.toByteArray();

        }  finally {

            if (in!= null) {

                try {

                    in.close();

                } catch(IOException e){
                    //ignored
                }

            }
        }

    }

    public static void writeAllBytes(File file, byte[] bytes) throws IOException {

        BufferedOutputStream out = null;

        try {

            out = new BufferedOutputStream(new FileOutputStream(file));

            out.write(bytes);

        }  finally {

            if (out!= null) {

                try {

                    out.close();

                } catch(IOException e){
                    //ignored
                }

            }
        }
    }

    public static boolean fileOnServerExists(String url) {

        return SimpleHttp.head(url)
                .timeouts(5000, 5000)
                .response()
                .isResponseCodeOK();
    }
}
