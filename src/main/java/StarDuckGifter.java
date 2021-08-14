import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionFormLauncher;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.extra.tools.PacketInfoSupport;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

@ExtensionInfo(
        Title = "StarduckGifter",
        Description = "Send an amount of starducks to selected habbo.",
        Version = "1.0",
        Author = "-MrTn-"
)
public class StarDuckGifter extends ExtensionForm {
    private PacketInfoSupport packetInfoSupport;

    private static final Color redColor = Color.rgb(240,128,128);
    private static final Color greenColor = Color.rgb(132,193,100);

    public Label selectedHabboLabel;
    public TextArea amountTxt;
    public TextArea habboNameLbl;
    public Button continueButton;
    public CheckBox alwaysOnTopChk;

    private boolean canSend = false;

    private int amount = 0;
    private int habboID = 0;

    private Stage succesWindow;

    public static void main(String[] args) {
        ExtensionFormLauncher.trigger(StarDuckGifter.class, args);
    }

    @Override
    public ExtensionForm launchForm(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(StarDuckGifter.class.getClassLoader().getResource("starduckgifter.fxml"));
        Parent root = loader.load();

        stage.setTitle("StarduckGifter");

        stage.setScene(new Scene(root));

        stage.setResizable(false);

        return loader.getController();
    }

    @Override
    protected void initExtension() {
        habboID = 0;
        packetInfoSupport = new PacketInfoSupport(this);
        packetInfoSupport.intercept(HMessage.Direction.TOSERVER, "GetSelectedBadges", this::onGetSelectedBadges);
        packetInfoSupport.intercept(HMessage.Direction.TOSERVER, "Quit", this::onQuit);

        amountTxt.textProperty().addListener((observable, oldValue, newValue) -> amountTxt.setText(newValue.replaceAll("[^0-9]", "")));
    }

    private void onQuit(HMessage hMessage) {
        canSend = false;
        selectedHabboLabel.setBackground(new Background(new BackgroundFill(redColor, CornerRadii.EMPTY, Insets.EMPTY)));
        habboID = 0;
        habboNameLbl.setText("");
    }

    private void onGetSelectedBadges(HMessage hMessage) {
        habboID = hMessage.getPacket().readInteger();

        if(habboID <= 0)
            return;

        canSend = true;
        habboNameLbl.setText(""+habboID);
        selectedHabboLabel.setBackground(new Background(new BackgroundFill(greenColor, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void giveDucks(ActionEvent actionEvent) {
        try{
            amount = Integer.parseInt(amountTxt.getText());

        }catch (Exception e){
            System.out.println(e);
        }
        if(canSend & amount != 0){
            try{
                packetInfoSupport.sendToServer("GiveStarGemToUser", habboID, amount);
                openSuccesWindow();
            }catch (Exception e){

            }
        }
    }

    public void closeSuccesWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) continueButton.getScene().getWindow();
        stage.close();
    }

    private void openSuccesWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(StarDuckGifter.class.getClassLoader().getResource("succes.fxml"));
        Parent roott = loader.load();
        succesWindow = new Stage();
        succesWindow.setScene(new Scene(roott));
        succesWindow.show();
    }

    public void setAlwaysOnTop(ActionEvent actionEvent) {
        primaryStage.setAlwaysOnTop(alwaysOnTopChk.isSelected());
    }
}
