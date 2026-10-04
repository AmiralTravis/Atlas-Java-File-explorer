package atlas.scanner;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


public class IndexStore {
    
    static void writeIndex(Index atlasIndex) throws IOException {

        Path tempFile = Path.of("atlas.index.tmp");
        Path indexFile = Path.of("atlas.index");

        try {

            try (
                OutputStream out = new FileOutputStream(tempFile.toString());
                DataOutputStream data = new DataOutputStream(out)
            ) {

                data.writeBytes("ATL");
                
                try (ObjectOutputStream obj = new ObjectOutputStream(data)) {
        
                    obj.writeObject(atlasIndex);

                }
                
            }

            Files.move(tempFile, indexFile, StandardCopyOption.REPLACE_EXISTING);


        } catch (IOException e) {

            Files.deleteIfExists(tempFile);
            throw e;

        }
        

    }

    public static Index readIndex(Path indexFile) 
            throws IOException, ClassNotFoundException {

        try (
            InputStream in = new FileInputStream(indexFile.toString());
            DataInputStream data = new DataInputStream(in);
        ) {

            byte[] header = new byte[3];
            data.readFully(header);
            
            if (!new String(header, java.nio.charset.StandardCharsets.US_ASCII)
                    .equals("ATL")) {
                throw new IOException("Not an ATL file");
            }


            try (ObjectInputStream obj = new ObjectInputStream(data)) {

                return (Index) obj.readObject();
                
            }
        }
    } 
}
