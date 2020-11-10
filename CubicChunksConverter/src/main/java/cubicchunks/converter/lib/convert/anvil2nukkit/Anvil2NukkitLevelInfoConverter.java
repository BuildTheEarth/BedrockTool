package cubicchunks.converter.lib.convert.anvil2nukkit;

import cubicchunks.converter.lib.convert.LevelInfoConverter;
import cubicchunks.converter.lib.convert.data.AnvilChunkData;
import cubicchunks.converter.lib.convert.data.NukkitChunkData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author DaPorkchop_
 */
public class Anvil2NukkitLevelInfoConverter implements LevelInfoConverter<AnvilChunkData, NukkitChunkData> {
    private final Path srcDir;
    private final Path dstDir;

    public Anvil2NukkitLevelInfoConverter(Path srcDir, Path dstDir) {
        this.srcDir = srcDir;
        this.dstDir = dstDir;
    }

    @Override
    public void convert() throws IOException {
        Files.copy(this.srcDir.resolve("level.dat"), this.dstDir.resolve("level.dat"));
        if (Files.exists(this.srcDir.resolve("offset.txt")))    {
            Files.copy(this.srcDir.resolve("offset.txt"), this.dstDir.resolve("offset.txt"));
        }
    }
}
