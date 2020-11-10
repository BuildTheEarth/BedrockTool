/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2020-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.ccpregen;

import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Scanner;

@Mod(modid = CCPregen.MODID,
        name = CCPregen.NAME,
        version = CCPregen.VERSION,
        dependencies = "required:cubicchunks@[1.12.2-0.0.1015.0,)",
        acceptableRemoteVersions = "*")

public class CCPregen {
    public static final String MODID = "ccpregen";
    public static final String NAME = "Cubic Chunks Pregenerator";
    public static final String VERSION = "0.0.4-1.12.2";

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) throws Exception {
        String[] region;
        File response = new File("./assets/region");
        BufferedReader fileReader = new BufferedReader(new FileReader(response));
        region = fileReader.readLine().split("\\.");
        fileReader.close();
        MinecraftServer server = event.getServer();
        int intX = Integer.parseInt(region[0]);
        int intZ = Integer.parseInt(region[1]);
        int X = intX * 512;
        int Y = 1024;
        int Z = intZ * 512;

        while(server.getWorld(0).getBlockState(new BlockPos(X, Y, Z)).getBlock() != Blocks.AIR) {
            Y += 1024;
        }
        while(server.getWorld(0).getBlockState(new BlockPos(X, Y, Z)).getBlock() == Blocks.AIR) {
            Y -= 1;
        }

        Y = (int) Math.floor(Y >> 8) * 256;
        BlockPos min = new BlockPos(X, Y, Z);
        BlockPos max = new BlockPos(X + 511, Y + 255, Z + 511);
        String path = ".\\" + server.getFolderName();
        File zeroOffset = new File(path + "\\height");
        Writer wr;

        zeroOffset.createNewFile();
        wr = new FileWriter(path + "\\height");
        wr.write(String.valueOf(Y));
        wr.close();

        logger.info("[Bedrock Tool]Generating the region...");
        PregenState.startPregeneration(server, min, max, 0);
    }
}