package net.toastynetworks.MCLAdmin.UI.Controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import net.toastynetworks.MCLAdmin.BLL.Interfaces.IConfigLogic;
import net.toastynetworks.MCLAdmin.BLL.Interfaces.IModpackLogic;
import net.toastynetworks.MCLAdmin.BLL.Interfaces.IModpackUploadLogic;
import net.toastynetworks.MCLAdmin.Factory.ConfigFactory;
import net.toastynetworks.MCLAdmin.Factory.ModpackFactory;
import net.toastynetworks.MCLAdmin.Domain.Modpack;
import net.toastynetworks.MCLAdmin.Factory.ModpackUploadFactory;
import net.toastynetworks.MCLAdmin.UI.Utilities.SwitchSceneUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import static net.toastynetworks.MCLAdmin.UI.Utilities.ZipUtil.addDirToZipArchive;
import static net.toastynetworks.MCLAdmin.UI.Utilities.ZipUtil.unzipArchive;

public class MainController extends Application implements Initializable  {

    private IModpackLogic modpackLogic = ModpackFactory.CreateLogic();
    private IConfigLogic configLogic = ConfigFactory.CreateLogic();
    private IModpackUploadLogic modpackUploadLogic = ModpackUploadFactory.CreateLogic();
    @FXML
    private TableView<Modpack> modpackTable;
    @FXML
    private TableColumn<Modpack, String> modpackNameColumn;
    @FXML
    private TableColumn<Modpack, String> modpackVersionColumn;
    @FXML
    private TableColumn<Modpack, Integer> modpackIdColumn;
    @FXML
    private Button addNewModpackButton;
    @FXML
    private Button editModpackButton;
    @FXML
    private Button deleteModpackButton;
    @FXML
    private Button uploadModpackButton;

    public static Modpack selectedModpack;
    private String zipName;
    private String workspace = configLogic.GetWorkSpaceFromConfig() + "\\";


    @Override
    public void start(Stage primaryStage) throws Exception{
        URL fileLocation;
        if (configLogic.GetWorkSpaceFromConfig() != null) {
            fileLocation = getClass().getClassLoader().getResource("fxml/main.fxml");
        } else {
            fileLocation = getClass().getClassLoader().getResource("fxml/SelectWorkspaceScene.fxml");
        }
        Parent root = FXMLLoader.load(fileLocation);
        primaryStage.setTitle("MCL-Admin");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public ObservableList<Modpack> getModpacks() {
        ObservableList<Modpack> modpacks = FXCollections.observableArrayList();
        for (Modpack modpack :
                modpackLogic.GetAllModpacks()) {
            modpacks.add(modpack);
        }
        return modpacks;
    }



    public void initialize(URL location, ResourceBundle resources) {
        modpackNameColumn.setCellValueFactory(new PropertyValueFactory<Modpack, String>("name"));
        modpackVersionColumn.setCellValueFactory(new PropertyValueFactory<Modpack, String>("versionType"));
        modpackIdColumn.setCellValueFactory(new PropertyValueFactory<Modpack, Integer>("id"));

        modpackTable.setItems(getModpacks());
        configLogic.PrepareWorkspace(modpackLogic.GetAllModpacks());
    }


    public void addModpackButton() {
        try {
            new SwitchSceneUtil(addNewModpackButton, "fxml/AddModpackScene.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void editModpackButton() {
        try {
            selectedModpack = modpackTable.getSelectionModel().getSelectedItem();
            new SwitchSceneUtil(editModpackButton, "fxml/EditModpackScene.fxml");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void deleteModpackButtonClick() {
        try {
            modpackLogic.DeleteModpack(modpackTable.getSelectionModel().getSelectedItem().getId());
            modpackTable.setItems(getModpacks());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    //TODO: Move to logic layer and clean up
    public void uploadModpackButtonClick() {
        try {
            Modpack modpack = modpackTable.getSelectionModel().getSelectedItem();
            String uploadDirectory = configLogic.GetWorkSpaceFromConfig() + "/" + String.valueOf(modpack.getId()) + "-" + modpack.getName() + "/";
            File dir = new File(uploadDirectory);
            System.out.println(dir);
            ArrayList<File> files = new ArrayList<File>();
            if (dir != null) {
                zipName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + "--" + modpack.getName() + ".zip";
                File zip = new File(workspace + zipName);
                FileOutputStream fileOutputStream = new FileOutputStream(zip);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                addDirToZipArchive(zipOutputStream, new File(dir.toString()), null);
                zipOutputStream.flush();
                zipOutputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
                files.add(zip);
            }
            modpackUploadLogic.uploadMultipleFiles(files);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    //TODO: Move to logic layer and clean up
    //TODO: Remove unzip button, this was for debugging only
    public void unzipModpackButtonClick() {
        Modpack modpack = modpackTable.getSelectionModel().getSelectedItem();
        String folderName = String.valueOf(modpack.getId()) + "-" + modpack.getName();
        try {
            unzipArchive(workspace + zipName);
            File src = new File(workspace + zipName.replace(".zip", "") + "\\" + folderName);
            File dest = new File(workspace + folderName);
            FileUtils.copyDirectory(src, dest);
            System.out.println("Done!!");
            //TODO: Move this into a seperate method in the logic layer, it should clean up the files and such.
            FileUtils.deleteDirectory(new File(workspace + zipName.replace(".zip", "")));
            FileUtils.forceDelete(new File(workspace + zipName));
            System.out.println("Done!");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
