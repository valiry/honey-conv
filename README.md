# honey-conv

A makeshift Anvil to Honey converter

It's not the prettiest, but it works.

## Usage

You first need to generate a blocks.json file for the targeted Minecraft version. To do that download the Vanilla server and run the following command: `java -cp server.jar net.minecraft.data.Main --reports`\
The blocks.json report can be found in `./generated/reports`.

You can run the converter with this command: `java -jar honey-conv-1.0.0.jar ./path/to/blocks.json ./path/to/anvil/world/dir ./path/to/output.honey`