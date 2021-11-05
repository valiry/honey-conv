package net.valiry.honeyconv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import net.valiry.honey.HoneyChunk;
import net.valiry.honey.HoneyWorld;
import net.valiry.honey.writer.HoneyWriters;
import org.jglrxavpok.hephaistos.mca.AnvilException;
import org.jglrxavpok.hephaistos.mca.BlockState;
import org.jglrxavpok.hephaistos.mca.ChunkColumn;
import org.jglrxavpok.hephaistos.mca.ChunkSection;
import org.jglrxavpok.hephaistos.mca.RegionFile;

public class Launcher {

    public static void main(final String[] args) throws IOException, AnvilException {
        if (args.length != 3) {
            System.err.println("Usage:");
            System.err.println("java -jar honey-conv.jar path/to/blocks.json path/to/anvil/dir converted_world.honey");
            return;
        }

        final String blockDumpPath = args[0];
        final String anvilPath = args[1];
        final String honeyPath = args[2];

        final File blockDumpFile = new File(blockDumpPath);
        if (!blockDumpFile.exists()) {
            System.err.println("Block dump not found");
            return;
        }

        BlockRegistry.load(new FileInputStream(blockDumpFile));

        final File dir = new File(anvilPath, "region");
        if (!dir.exists()) {
            System.err.println("File not found");
            return;
        }

        final HoneyWorld world = new HoneyWorld();

        long totalAnvilSize = 0;

        int totalChunks = 0;
        final File[] regionFiles = dir.listFiles(pathname -> pathname.getName().matches("r\\.-?\\d+\\.-?\\d+\\.mca"));
        for (final File regionFile : regionFiles) {
            // Ignore 0B files
            if (regionFile.length() == 0) {
                continue;
            }

            // Get region x and z
            final String[] split = regionFile.getName().split("\\.");
            final int rx = Integer.parseInt(split[1]);
            final int rz = Integer.parseInt(split[2]);

            // Increment total size
            totalAnvilSize += regionFile.length();

            // Load region file
            final RandomAccessFile randomAccessFile = new RandomAccessFile(regionFile, "r");
            final RegionFile region = new RegionFile(randomAccessFile, rx, rz);

            for (int cx = 0; cx < 32; cx++) {
                for (int cz = 0; cz < 32; cz++) {
                    final int chunkX = cx + 32 * rx;
                    final int chunkZ = cz + 32 * rz;
                    if (region.hasLoadedChunk(chunkX, chunkZ)) {
                        // Create honey chunk from anvil chunk
                        final ChunkColumn anvilChunk = region.getChunk(chunkX, chunkZ);
                        final HoneyChunk honeyChunk = new HoneyChunk(HoneyChunk.ChunkId.of(chunkX, chunkZ), new HashMap<>(),
                                anvilChunk.toNBT().getCompound("Level").getList("Entities").toByteArray(),
                                anvilChunk.toNBT().getCompound("Level").getList("TileEntities").toByteArray());

                        for (int sectionIndex = 0; sectionIndex < 256 / 16; sectionIndex++) {
                            // Get anvil section and check for air
                            final ChunkSection sec = anvilChunk.getSection((byte) sectionIndex);
                            boolean onlyAir = true;
                            outer:
                            for (int x = 0; x < 16; x++) {
                                for (int y = 0; y < 16; y++) {
                                    for (int z = 0; z < 16; z++) {
                                        if (!sec.get(x, y, z).getName().equals("minecraft:air")) {
                                            onlyAir = false;
                                            break outer;
                                        }
                                    }
                                }
                            }

                            // Skip sections with only air
                            if (onlyAir) {
                                continue;
                            }

                            // Get honey section
                            final HoneyChunk.HoneyChunkSection section = honeyChunk.getSectionMap()
                                    .computeIfAbsent(sectionIndex, integer -> new HoneyChunk.HoneyChunkSection(new short[4096]));

                            // Get and convert all the blocks
                            int stateIndex = 0;
                            for (int x = 0; x < 16; x++) {
                                for (int y = 0; y < 16; y++) {
                                    for (int z = 0; z < 16; z++) {
                                        final BlockState blockState = sec.get(x, y, z);
                                        section.getStates()[stateIndex++] = (short) BlockRegistry.getProtocolId(blockState);
                                    }
                                }
                            }

                            honeyChunk.getSectionMap().put(sectionIndex, section);
                        }

                        world.put(honeyChunk);
                        totalChunks++;
                    }
                }
            }
        }

        // Save world
        final byte[] data = HoneyWriters.writeWithLatest(world);
        try (final FileOutputStream outputStream = new FileOutputStream(honeyPath)) {
            outputStream.write(data);
        }
        final long honeySize = data.length;

        System.out.println("Converted " + totalChunks + " chunks ");
        System.out.println("Size of Anvil region files: " + totalAnvilSize + " bytes (" + (totalAnvilSize / 1024) + " KB, " + (totalAnvilSize / 1024 / 1024) + " MB)");
        System.out.println("Size of honey file: " + honeySize + " bytes (" + (honeySize / 1024) + " KB, " + (honeySize / 1024 / 1024) + " MB)");
        System.out.println("The honey size is " + String.format("%.2f", (((double) honeySize / (double) totalAnvilSize) * 100D)) + "% of the Anvil size.");
    }

}
