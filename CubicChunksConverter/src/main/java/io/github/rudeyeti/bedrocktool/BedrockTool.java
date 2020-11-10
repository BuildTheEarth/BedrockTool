package io.github.rudeyeti.bedrocktool;

import cubicchunks.converter.lib.*;
import cubicchunks.converter.lib.Registry;
import cubicchunks.converter.lib.convert.ChunkDataConverter;
import cubicchunks.converter.lib.convert.ChunkDataReader;
import cubicchunks.converter.lib.convert.ChunkDataWriter;
import cubicchunks.converter.lib.convert.LevelInfoConverter;
import cubicchunks.converter.lib.convert.WorldConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class BedrockTool {

    public static void main(String[] args) throws IOException {
        String src = args[0];
        String out = args[1];
        File Height = new File(src + "//height");
        BufferedReader br = new BufferedReader(new FileReader(Height));
        int minHeight = Integer.parseInt(br.readLine());
        br.close();

        Path tempDir = Paths.get(src).getParent().resolve("temp");
        File temp = tempDir.toFile();
        if (temp.mkdir()) {
            System.out.println("[Bedrock Tool]Converting from CubicChunks to Anvil...");
        }

        WorldConverter<?, ?> converter = new WorldConverter<Object, Object>(
                (LevelInfoConverter<Object, Object>)Registry.getLevelConverter("CubicChunks", "Anvil (layered)").apply(
                        Paths.get(src),
                        tempDir
                ),
                (ChunkDataReader<Object>)Registry.getReader("CubicChunks").apply(Paths.get(src)),
                (ChunkDataConverter<Object, Object>)Registry.getConverter("CubicChunks", "Anvil (layered)").get(),
                (ChunkDataWriter<Object>)Registry.getWriter("Anvil (layered)").apply(tempDir));
        converter.convert(new IProgressListener() {
            public void update(Void aVoid) {
            }

            public ErrorHandleResult error(Throwable throwable) {
                return null;
            }
        });
        System.out.println("[Bedrock Tool]Converting from Anvil to Nukkit...");

        String src2 = tempDir + "/layer [" + minHeight + ", " + (minHeight + 256) + "]";
        WorldConverter<?, ?> converter2 = new WorldConverter<>(
                (LevelInfoConverter<Object, Object>)Registry.getLevelConverter("Anvil", "Nukkit").apply(
                        Paths.get(src2),
                        Paths.get(out)
                ),
                (ChunkDataReader<Object>)Registry.getReader("Anvil").apply(Paths.get(src2)),
                (ChunkDataConverter<Object, Object>)Registry.getConverter("Anvil", "Nukkit").get(),
                (ChunkDataWriter<Object>)Registry.getWriter("Nukkit").apply(Paths.get(out)));
        converter2.convert(new IProgressListener() {
            public void update(Void aVoid) {
            }

            public ErrorHandleResult error(Throwable throwable) {
                return null;
            }
        });
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        Files.copy(Paths.get(src).resolve("height"),Paths.get(out).resolve("height"));
    }
}
