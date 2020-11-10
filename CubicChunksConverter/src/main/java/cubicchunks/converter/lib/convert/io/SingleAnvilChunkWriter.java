/*
 *  This file is part of CubicChunksConverter, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2017 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package cubicchunks.converter.lib.convert.io;

import cubicchunks.converter.lib.Dimension;
import cubicchunks.converter.lib.convert.ChunkDataWriter;
import cubicchunks.converter.lib.convert.data.AnvilChunkData;
import cubicchunks.converter.lib.util.Utils;
import cubicchunks.regionlib.impl.save.MinecraftSaveSection;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cubicchunks.converter.lib.util.Utils.*;
import static cubicchunks.regionlib.impl.save.MinecraftSaveSection.MinecraftRegionType.*;

public class SingleAnvilChunkWriter implements ChunkDataWriter<AnvilChunkData> {
    private Path dstPath;
    private Map<Dimension, MinecraftSaveSection> saves = new ConcurrentHashMap<>();

    public SingleAnvilChunkWriter(Path dstPath) {
        this.dstPath = dstPath;
    }

    @Override
    public void accept(AnvilChunkData chunk) throws IOException {
        Map<Dimension, MinecraftSaveSection> layer = this.saves;
        MinecraftSaveSection save = layer.computeIfAbsent(chunk.getDimension(), propagateExceptions(dim -> {
            Path regionDir = getDimensionPath(chunk.getDimension(), this.dstPath);
            Utils.createDirectories(regionDir);
            return MinecraftSaveSection.createAt(regionDir, MCA);
        }));
        save.save(chunk.getPosition(), chunk.getData());
    }

    static Path getDimensionPath(Dimension d, Path worldDir) {
        if (!d.getDirectory().isEmpty()) {
            worldDir = worldDir.resolve(d.getDirectory());
        }
        return worldDir.resolve("region");
    }

    @Override
    public void discardData() throws IOException {
        Utils.rm(this.dstPath);
    }

    @Override
    public void close() throws Exception {
        boolean exception = false;
        for (Closeable save : this.saves.values()) {
            try {
                save.close();
            } catch (IOException e) {
                e.printStackTrace();
                exception = true;
            }
        }

        if (exception) {
            throw new IOException();
        }
    }
}
