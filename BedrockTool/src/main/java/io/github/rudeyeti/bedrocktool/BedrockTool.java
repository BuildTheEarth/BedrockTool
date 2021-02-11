package io.github.rudeyeti.bedrocktool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BedrockTool {
    
    private static final Runtime runtime = Runtime.getRuntime();
    private static final String rootDirectory = new File(".").getPath();

    // Different operating systems need to be accounted for.
    private static File pathToFile(String path) {
        return new File(path.replace("/", File.separator));
    }

    private static void error(String message, Exception error) {
        System.out.println(message);
        error.printStackTrace();
        System.exit(-1);
    }

    // Shouldn't usually happen, but it could be the result of unsupported operating systems.
    private static void fileError(String fileName, String action) {
        System.out.println("The file " + fileName + " was not " + action + " successfully.");
    }

    // This probably will never happen, but it's good just in case.
    private static void deleteError(String type, String fileName, Exception error) {
        error("\nAn unknown error seems to have occurred when deleting the " + type + ": \"" + fileName + "\"\n", error);
    }

    // Every single file in the directory must be deleted before the folder itself is.
    private static void deleteDirectory(File directory) {
        try {
            File[] contents = directory.listFiles();

            if (contents != null) {
                for (File file : contents) {
                    if (!file.delete()) {
                        fileError(file.getName(), "deleted");
                    }
                }
            }

            if (!directory.delete()) {
                fileError(directory.getName(), "deleted");
            }
        } catch (Exception error) {
            deleteError("directory", directory.getName(), error);
        }
    }

    // Quick way to delete certain temporary files.
    private static void deleteFile() {
        String fileName = "region";

        try {
            if (!pathToFile(rootDirectory + "assets/" + fileName).delete()) {
                fileError(fileName, "deleted");
            }
        } catch (Exception error) {
            deleteError("file", fileName, error);
        }
    }

    // This deletes everything, basically starting over which is necessary every run.
    private static void deleteTempFiles() {
        List<String> directories = Arrays.asList("TerraPreGenerated", "world", "temp");

        directories.forEach((directory) -> {
            deleteDirectory(pathToFile(rootDirectory + directory));
        });

        deleteFile();
    }

    // Most of the output received is filled with garbage, so this filters out important information.
    private static void printOutput(BufferedReader bufferedReader) {
        try {
            String line;

            while ((line = bufferedReader.readLine()) != null && line.contains("[Bedrock Tool]")) {
                System.out.println(line.replace("[Bedrock Tool]", ""));
            }
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Arguments: <region> <discord-username>");
                System.exit(-1);
            } else if (!Pattern.compile("-?\\d+\\.-?\\d+").matcher(args[0]).matches()) {
                System.out.println("The region must be specified in this format: 1234.1234");
                System.exit(-1);
            }

            StringBuilder discordUsername = new StringBuilder().append(args[1]);

            // Some Discord Usernames might have spaces, so all the remaining arguments are treated as one.
            for (int i = 2; i < args.length; i++) {
                discordUsername.append(args[i]);
            }

            if (!Pattern.compile(".{3,32}#\\d{4}").matcher(discordUsername.toString()).matches()) {
                System.out.println("The Discord Username must be specified in this format: Username#1234");
                System.exit(-1);
            }

            deleteTempFiles();

            File region = pathToFile(rootDirectory + "assets/region");

            if (!region.createNewFile()) {
                fileError(region.getName(), "created");
            }

            // The server reads the region coordinates directly from a file.
            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(region))) {
                fileWriter.write(args[0]);
            }

            // Generating the region on the server, which is activated on server startup.
            System.out.println("Starting the server...");

            Process minecraftServer = runtime.exec("java -XX:+UseG1GC -Xss4M -Xmx" + runtime.maxMemory() / (1024 * 1024) + "M -Xms128M -XX:+DisableExplicitGC -XX:+UnlockExperimentalVMOptions -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -jar forge.jar nogui");
            BufferedReader minecraftServerInput = new BufferedReader(new InputStreamReader(minecraftServer.getInputStream()));
            printOutput(minecraftServerInput);

            minecraftServer.waitFor();
            deleteFile();
            System.out.println("Stopping the server...");

            // The world needs to be converted from CubicChunks to Anvil and then to Nukkit.
            Process cubicChunksConverter = runtime.exec(pathToFile(
                    "java -jar " + rootDirectory +
                    "assets/cubicchunksconverter.jar " + rootDirectory +
                    "TerraPreGenerated " + rootDirectory +
                    "world"
            ).getPath());

            BufferedReader cubicChunksConverterInput = new BufferedReader(new InputStreamReader(cubicChunksConverter.getInputStream()));
            printOutput(cubicChunksConverterInput);

            cubicChunksConverter.waitFor();
            System.out.println("Conversion finished...");

            // The coordinates must be parsed correctly to extract the region properly.
            String[] coordinates = args[0].split("\\.");

            System.out.println("Moving the region...");

            // The world needs to be moved by MCASelector.
            runtime.exec(pathToFile(
                    "java -jar " + rootDirectory +
                    "assets/mcaselector.jar --headless --mode import --world " + rootDirectory +
                    "world/region --input " + rootDirectory +
                    "world/region --offset-x " + Integer.parseInt(coordinates[0]) * -32 +
                    " --offset-z " + Integer.parseInt(coordinates[1]) * -32
            ).getPath()).waitFor();

            // Once that is finished, the old area must be deleted.
            runtime.exec(pathToFile(
                    "java -jar " + rootDirectory +
                    "assets/mcaselector.jar --headless --mode delete --world " + rootDirectory +
                    "world/region --query \"!(xPos <= 63 AND xPos >= -32 AND zPos <= 63 AND zPos >= -32)\""
            ).getPath()).waitFor();

            System.out.println("Moving finished...");

            // Everything needs to be in a zip file so users can download it easily.
            System.out.println("Compressing the region...");
            Files.deleteIfExists(pathToFile(rootDirectory + "world/level.dat").toPath());
            Files.deleteIfExists(pathToFile(rootDirectory + "world/offset.txt").toPath());
            Files.deleteIfExists(pathToFile(rootDirectory + "world/height").toPath());

            if (!pathToFile(rootDirectory + "world").renameTo(pathToFile(rootDirectory + "assets/server-pack/worlds/world"))) {
                fileError("world", "renamed");
            }

            Path zipPath = Paths.get(discordUsername.toString().replaceAll("[^\\p{L}\\d_.]*", "") + "_" + args[0] + ".zip");

            Files.deleteIfExists(zipPath);

            Path zipFile = Files.createFile(zipPath);
            Path zipSourceFile = pathToFile(rootDirectory + "assets/server-pack").toPath();

            // Every file is packed up into the output stream before being written into the zip.
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile));
                Stream<Path> paths = Files.walk(zipSourceFile)) {
                    paths.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(zipSourceFile.relativize(path).toString());

                        try {
                            zipOutputStream.putNextEntry(zipEntry);
                            Files.copy(path, zipOutputStream);
                        } catch (IOException error) {
                            error("\nAn unknown error seems to have occurred when compressing the region:\n", error);
                        }
                    });
                }

            // Old files need to be cleaned up, even though it's already done at the start.
            deleteDirectory(pathToFile(rootDirectory + "assets/server-pack/worlds/world"));
            System.out.println("Compression finished...");

            System.out.println("The region is complete!");
            deleteTempFiles();
        } catch (Exception error) {
            error("\nAn unknown error seems to have occurred:\n", error);
        }
    }
}