package osp;

import crosby.binary.file.*;

import java.io.*;


public class Main {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Type key:");
        String key = br.readLine().toLowerCase().trim();
        System.out.println("Type value:");
        String value = br.readLine().toLowerCase().trim();

        InputStream input = Main.class.getClassLoader().getResourceAsStream("czech-republic-snapshot.osm.pbf");
        BlockReaderAdapter brad = new OSPBinaryParser(key, value);
        new BlockInputStream(input, brad).process(); // parse necessary nodes
        input = Main.class.getClassLoader().getResourceAsStream("czech-republic-snapshot.osm.pbf");
        new BlockInputStream(input, brad).process(); // count length
    }



}
