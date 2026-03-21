package y.ydialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import y.yutils.AbstractYahooGamesApplet;

public class AppletLoginDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    protected AbstractYahooGamesApplet applet;
    protected Label lblMessage;
    protected TextField txtUsername;
    protected TextField txtPassword;
    protected Button btnLogin;
    protected Button btnCancel;

    public AppletLoginDialog(AbstractYahooGamesApplet applet) {
        super(findOwnerFrame(), "Sign In", false);
        this.applet = applet;

        setLayout(new BorderLayout(6, 6));

        lblMessage = new Label("Please sign in");
        add(lblMessage, BorderLayout.NORTH);

        Panel fields = new Panel(new GridLayout(2, 2, 4, 4));
        fields.add(new Label("User Name"));
        txtUsername = new TextField(24);
        fields.add(txtUsername);
        fields.add(new Label("Password"));
        txtPassword = new TextField(24);
        txtPassword.setEchoChar('*');
        fields.add(txtPassword);
        add(fields, BorderLayout.CENTER);

        Panel buttons = new Panel(new GridLayout(1, 2, 4, 4));
        btnLogin = new Button("Sign In");
        btnCancel = new Button("Cancel");
        buttons.add(btnLogin);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setResizable(false);
    }

    protected static Frame findOwnerFrame() {
        return new Frame();
    }

    @Override
    public boolean action(Event event, Object obj) {
        if (event.target == btnLogin) {
            applet.submitAppletLogin(txtUsername.getText(), txtPassword.getText());
            return true;
        }
        if (event.target == btnCancel) {
            applet.cancelAppletLogin();
            hideDialog();
            return true;
        }
        return false;
    }

    @Override
    public boolean handleEvent(Event event) {
        if (event.id == Event.WINDOW_DESTROY) {
            applet.cancelAppletLogin();
            hideDialog();
            return true;
        }
        return super.handleEvent(event);
    }

    public void hideDialog() {
        setVisible(false);
    }

    public void setBusy(boolean busy) {
        txtUsername.setEnabled(!busy);
        txtPassword.setEnabled(!busy);
        btnLogin.setEnabled(!busy);
    }

    public void setMessage(String message) {
        lblMessage.setText(message != null ? message : "Please sign in");
    }

    public void setUsername(String username) {
        if (username == null)
            username = "";
        txtUsername.setText(username);
        if (txtPassword.getText() == null || txtPassword.getText().length() == 0)
            txtPassword.requestFocus();
    }

    public void showDialog() {
        pack();
        setVisible(true);
        toFront();
        if (txtUsername.getText() != null && txtUsername.getText().length() > 0)
            txtPassword.requestFocus();
        else
            txtUsername.requestFocus();
    }
}
