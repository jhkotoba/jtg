package jtg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Generate {
	
	private static boolean useAdvanced = false;
    private static boolean isWhitelist = false;
    private static Set<String> extensionList = new HashSet<>();

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("Usage: java -jar jtg.jar <projectPath> [<-w | -b> <extensions comma separated>]");
            return;
        }
		
        String projectPath = args[0];
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            System.out.println("Invalid project path provided.");
            return;
        }
        
        if (args.length >= 3) {
            useAdvanced = true;
            String modeArg = args[1].toLowerCase();
            if (!(modeArg.equals("-w") || modeArg.equals("-b"))) {
                System.out.println("Second parameter must be '-w' for whitelist or '-b' for blacklist.");
                return;
            }
            isWhitelist = modeArg.equals("-w");
            
            String extensionsParam = args[2];
            String[] extensions = extensionsParam.split(",");
            for (String ext : extensions) {
                extensionList.add(ext.trim().toLowerCase());
            }
        }
        
        File outputFile = new File(projectDir.getName() + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            processDirectory(projectDir, writer);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("Output file created: " + outputFile.getAbsolutePath());

	}
	
    private static void processDirectory(File dir, BufferedWriter writer) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, writer);
            } else {
                if (shouldProcess(file)) {
                    writer.write("----- Start: " + file.getPath() + " -----\n");
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                    writer.write("----- End: " + file.getPath() + " -----\n\n");
                }
            }
        }
    } 
    
    private static boolean shouldProcess(File file) {
        String name = file.getName().toLowerCase();
        
        if (useAdvanced) {
            int dotIndex = name.lastIndexOf(".");
            if (dotIndex == -1) {
                return false;
            }
            String ext = name.substring(dotIndex + 1);
            if (isWhitelist) {
                return extensionList.contains(ext);
            } else {
                return !extensionList.contains(ext);
            }
        } else {
            return name.endsWith(".java") || name.endsWith(".yml") || 
                   name.endsWith(".properties") || name.endsWith(".css") || name.endsWith(".scss") ||
                   name.endsWith(".gradle") || name.endsWith(".xml") ||
                   name.endsWith(".js") || name.endsWith(".html");
        }
    }
}
