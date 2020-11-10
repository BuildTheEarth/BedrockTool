package io.github.rudeyeti.bedrocktool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BedrockTool {
    
    private static final Runtime runtime = Runtime.getRuntime();

    private static void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteDirectory(file);
            }
        }
        try {
            directory.delete();
        } catch (Exception error) {
            System.out.println("There was an error when deleting the directory: \"" + directory.getName() + "\"\n");
            error.printStackTrace();
            System.exit(-1);
        }
    }

    private static void deleteRegionFile() {
        try {
            new File("./assets/region".replace("/", File.separator)).delete();
        } catch (Exception error) {
            System.out.println("There was an error when deleting the file: \"region\"\n");
            error.printStackTrace();
            System.exit(-1);
        }
    }

    private static void deleteTemporaryFiles() {
        deleteDirectory(Paths.get("./TerraPreGenerated".replace("/", File.separator)).toFile());
        deleteDirectory(Paths.get("./world".replace("/", File.separator)).toFile());
        deleteDirectory(Paths.get("./temp".replace("/", File.separator)).toFile());
        deleteRegionFile();
    }

    private static void separateConsoleInput(BufferedReader bufferedReader) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("[Bedrock Tool]")) {
                System.out.println(line.split("\\[Bedrock Tool]")[1]);
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Arguments: <region> <discord-username>");
                System.exit(-1);
            }

            if (!Pattern.compile("^-?\\d+\\.-?\\d+$").matcher(args[0]).matches()) {
                System.out.println("The region must be specified in this format: 1234.1234");
                System.exit(-1);
            }

            String discordUsername = args[1];
            for (int i = 2; i < args.length; i++) {
                discordUsername += args[i];
            }

            if (!Pattern.compile("^.*#[0-9]{4}$").matcher(discordUsername).matches()) {
                System.out.println("The Discord Username must be specified in this format: Username#1234");
                System.exit(-1);
            }

            deleteTemporaryFiles();

            File region = new File("./assets/region".replace("/", File.separator));
            region.createNewFile();
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(region));
            fileWriter.write(args[0]);
            fileWriter.close();

            System.out.println("Starting the server...");
            Process minecraftServer = runtime.exec("java -XX:+UseG1GC -Xss4M -Xmx" + runtime.maxMemory() / (1024 * 1024) + "M -Xms128M -XX:+DisableExplicitGC -XX:+UnlockExperimentalVMOptions -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -jar forge.jar nogui");
            BufferedReader minecraftServerInput = new BufferedReader(new InputStreamReader(minecraftServer.getInputStream()));
            separateConsoleInput(minecraftServerInput);

            minecraftServer.waitFor();
            deleteRegionFile();
            System.out.println("Stopping the server...");

            System.out.println("Converting the region...");
            Process cubicChunksConverter = runtime.exec("java -jar ./assets/cubicchunksconverter.jar ./TerraPreGenerated ./world".replace("/", File.separator));
            BufferedReader cubicChunksConverterInput = new BufferedReader(new InputStreamReader(cubicChunksConverter.getInputStream()));
            separateConsoleInput(cubicChunksConverterInput);

            cubicChunksConverter.waitFor();
            System.out.println("Conversion finished...");

            System.out.println("Moving the region...");
            String[] coordinates = args[0].split("\\.");
            runtime.exec(("java -jar ./assets/mcaselector.jar --headless --mode import --world ./world/region --input ./world/region --offset-x " + Integer.parseInt(coordinates[0]) * -32 + " --offset-z " + Integer.parseInt(coordinates[1]) * -32).replace("/", File.separator)).waitFor();
            runtime.exec(("java -jar ./assets/mcaselector.jar --headless --mode delete --world ./world/region --query \"!(xPos <= 63 AND xPos >= -32 AND zPos <= 63 AND zPos >= -32)\"").replace("/", File.separator)).waitFor();
            System.out.println("Moving finished...");

            System.out.println("Compressing the region...");
            Files.deleteIfExists(Paths.get("./world/level.dat".replace("/", File.separator)));
            Files.deleteIfExists(Paths.get("./world/offset.txt".replace("/", File.separator)));
            Files.deleteIfExists(Paths.get("./world/height".replace("/", File.separator)));
            new File("./world".replace("/", File.separator)).renameTo(new File("./assets/server-pack/worlds/world".replace("/", File.separator)));

            Path zipFile = Files.createFile(Paths.get(discordUsername.replaceAll("[^\\p{L}\\d_.]*", "") + "_" + args[0] + ".zip"));
            Path zipSourceFile = Paths.get("./assets/server-pack".replace("/", File.separator));
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile));
                Stream<Path> paths = Files.walk(zipSourceFile)) {
                    paths.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(zipSourceFile.relativize(path).toString());
                        try {
                            zipOutputStream.putNextEntry(zipEntry);
                            Files.copy(path, zipOutputStream);
                            zipOutputStream.closeEntry();
                        } catch (IOException error) {
                            System.out.println("An error seems to have occurred when compressing the region:\n");
                            error.printStackTrace();
                            System.exit(-1);
                        }
                    });
                }

            deleteDirectory(Paths.get("./assets/server-pack/worlds/world".replace("/", File.separator)).toFile());
            System.out.println("Compression finished...");

            System.out.println("The region is complete!");
            deleteTemporaryFiles();
        } catch (Exception error) {
            System.out.println("An unknown error seems to have occurred:\n");
            error.printStackTrace();
            System.exit(-1);
        }
    }
}