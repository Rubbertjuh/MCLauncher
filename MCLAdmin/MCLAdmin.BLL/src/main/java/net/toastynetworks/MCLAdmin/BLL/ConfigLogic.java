package net.toastynetworks.MCLAdmin.BLL;

import net.toastynetworks.MCLAdmin.BLL.Interfaces.IConfigLogic;

import java.io.*;
import java.util.Properties;

public class ConfigLogic implements IConfigLogic {
    private String configPath = System.getenv("APPDATA") + "/" + ".MCLauncher/admin/" + "config.properties";

    public void CreateConfig() {
        try {
            File file = new File(configPath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                OutputStream output = new FileOutputStream(configPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String GetWorkSpaceFromConfig() {
        Properties prop = null;
        try {
            prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configPath);
                if (input == null) {
                    CreateConfig();
                    input = new FileInputStream(configPath);
                }
            } catch (FileNotFoundException e) {
                CreateConfig();
                input = new FileInputStream(configPath);
            }
            prop.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop.getProperty("Workspace:");
    }

    public void EditConfig( String newWorkspace) {
        try {
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                prop.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            OutputStream output = new FileOutputStream(configPath);
            prop.setProperty("Workspace:", newWorkspace);
            prop.store(output, null);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void DeleteWorkSpaceFromConfig(String modpackName) {
        try {
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configPath);
                prop.load(input);
                prop.remove(modpackName);
                OutputStream output = new FileOutputStream(configPath);
                prop.store(output, null);
            } catch (FileNotFoundException io) {
                System.out.println(io);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}