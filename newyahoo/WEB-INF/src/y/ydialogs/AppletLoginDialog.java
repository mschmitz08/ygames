package y.ydialogs;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.util.Timer;
import java.util.TimerTask;

import y.yutils.AbstractYahooGamesApplet;

public class AppletLoginDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    private static final int MODE_SIGN_IN = 0;
    private static final int MODE_REGISTER = 1;
    private static final int MODE_CHANGE_PASSWORD = 2;

    private static final Color FRAME_BG = new Color(22, 26, 38);
    private static final Color PANEL_BG = new Color(236, 224, 192);
    private static final Color FIELD_BG = new Color(255, 248, 224);
    private static final Color ACCENT = new Color(209, 124, 48);
    private static final Color INK = new Color(44, 32, 24);
    private static final Color SUBTLE = new Color(110, 86, 58);

    private Font bodyFont;
    private Font labelFont;
    private Font messageFont;
    private Font modeButtonFont;
    private Font actionButtonFont;

    protected AbstractYahooGamesApplet applet;
    protected Label lblMessage;
    protected Label lblField1;
    protected Label lblField2;
    protected Label lblField3;
    protected Label lblField4;
    protected TextField txtField1;
    protected TextField txtField2;
    protected TextField txtField3;
    protected TextField txtField4;
    protected Button btnPrimary;
    protected Button btnCancel;
    protected Button btnSignInMode;
    protected Button btnRegisterMode;
    protected Button btnChangePasswordMode;
    protected HeroCanvas heroCanvas;
    protected Panel row3;
    protected Panel row4;
    protected int mode;

    public AppletLoginDialog(AbstractYahooGamesApplet applet) {
        super(findOwnerFrame(applet), applet.uiText("dialog_title", "Game Login"), false);
        this.applet = applet;
        initializeFonts();

        setBackground(FRAME_BG);
        setLayout(new BorderLayout(10, 10));

        heroCanvas = new HeroCanvas(applet);
        add(heroCanvas, BorderLayout.NORTH);

        Panel body = new Panel(new BorderLayout(0, 10));
        body.setBackground(PANEL_BG);

        Panel modeButtons = new Panel(new GridLayout(1, 3, 6, 0));
        modeButtons.setBackground(PANEL_BG);
        btnSignInMode = new Button(controlText("sign_in", "Sign In"));
        btnRegisterMode = new Button(controlText("register", "Register"));
        btnChangePasswordMode = new Button(controlText("change_password", "Change Password"));
        styleModeButton(btnSignInMode);
        styleModeButton(btnRegisterMode);
        styleModeButton(btnChangePasswordMode);
        modeButtons.add(btnSignInMode);
        modeButtons.add(btnRegisterMode);
        modeButtons.add(btnChangePasswordMode);
        body.add(modeButtons, BorderLayout.NORTH);

        Panel center = new Panel(new BorderLayout(0, 8));
        center.setBackground(PANEL_BG);

        lblMessage = new Label(controlText("please_sign_in", "Please sign in"));
        lblMessage.setForeground(INK);
        lblMessage.setBackground(PANEL_BG);
        lblMessage.setFont(messageFont);
        center.add(lblMessage, BorderLayout.NORTH);

        Panel fields = new Panel(new GridLayout(4, 1, 0, 8));
        fields.setBackground(PANEL_BG);
        row3 = new Panel(new GridLayout(1, 2, 8, 0));
        row3.setBackground(PANEL_BG);
        row4 = new Panel(new GridLayout(1, 2, 8, 0));
        row4.setBackground(PANEL_BG);

        Panel row1 = new Panel(new GridLayout(1, 2, 8, 0));
        row1.setBackground(PANEL_BG);
        Panel row2 = new Panel(new GridLayout(1, 2, 8, 0));
        row2.setBackground(PANEL_BG);

        lblField1 = createFieldLabel(controlText("user_name", "User Name"));
        lblField2 = createFieldLabel(controlText("password", "Password"));
        lblField3 = createFieldLabel(controlText("confirm_password", "Confirm Password"));
        lblField4 = createFieldLabel(controlText("email", "Email"));

        txtField1 = createField(false);
        txtField2 = createField(true);
        txtField3 = createField(true);
        txtField4 = createField(false);

        row1.add(lblField1);
        row1.add(txtField1);
        row2.add(lblField2);
        row2.add(txtField2);
        row3.add(lblField3);
        row3.add(txtField3);
        row4.add(lblField4);
        row4.add(txtField4);

        fields.add(row1);
        fields.add(row2);
        fields.add(row3);
        fields.add(row4);
        center.add(fields, BorderLayout.CENTER);
        body.add(center, BorderLayout.CENTER);

        Panel buttons = new Panel(new GridLayout(1, 2, 8, 0));
        buttons.setBackground(PANEL_BG);
        btnPrimary = new Button(controlText("enter_room", "Enter Room"));
        btnCancel = new Button(controlText("cancel", "Cancel"));
        stylePrimaryButton(btnPrimary);
        styleSecondaryButton(btnCancel);
        buttons.add(btnPrimary);
        buttons.add(btnCancel);
        body.add(buttons, BorderLayout.SOUTH);

        add(wrapBody(body), BorderLayout.CENTER);

        pack();
        setResizable(false);
        setAlwaysOnTop(true);
        applyMode(MODE_SIGN_IN);
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setForeground(INK);
        label.setBackground(PANEL_BG);
        label.setFont(labelFont);
        return label;
    }

    private TextField createField(boolean password) {
        TextField field = new TextField(24);
        field.setBackground(FIELD_BG);
        field.setForeground(INK);
        field.setFont(bodyFont);
        if (password)
            field.setEchoChar('*');
        return field;
    }

    private Panel wrapBody(Panel body) {
        Panel wrapper = new Panel(new BorderLayout());
        wrapper.setBackground(FRAME_BG);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    private void styleModeButton(Button button) {
        button.setFont(modeButtonFont);
        button.setBackground(new Color(194, 180, 147));
        button.setForeground(INK);
    }

    private void stylePrimaryButton(Button button) {
        button.setFont(actionButtonFont);
        button.setBackground(ACCENT);
        button.setForeground(Color.white);
    }

    private void styleSecondaryButton(Button button) {
        button.setFont(actionButtonFont);
        button.setBackground(new Color(194, 180, 147));
        button.setForeground(INK);
    }

    private void initializeFonts() {
        String sample = buildFontSample();
        bodyFont = chooseUiFont(applet, sample, Font.PLAIN, 13);
        labelFont = chooseUiFont(applet, sample, Font.BOLD, 12);
        messageFont = chooseUiFont(applet, sample, Font.BOLD, 14);
        modeButtonFont = chooseUiFont(applet, sample, Font.BOLD, 11);
        actionButtonFont = chooseUiFont(applet, sample, Font.BOLD, 12);
    }

    private String buildFontSample() {
        StringBuffer sample = new StringBuffer();
        appendSample(sample, applet.uiText("dialog_title", "Game Login"));
        appendSample(sample, controlText("sign_in", "Sign In"));
        appendSample(sample, controlText("register", "Register"));
        appendSample(sample, controlText("change_password", "Change Password"));
        appendSample(sample, controlText("please_sign_in", "Please sign in"));
        appendSample(sample, controlText("user_name", "User Name"));
        appendSample(sample, controlText("password", "Password"));
        appendSample(sample, controlText("enter_room", "Enter Room"));
        return sample.toString();
    }

    private boolean useAsciiControlLabels() {
        String locale = applet == null || applet.intl_code == null ? "us"
                : applet.intl_code.toLowerCase();
        return locale.startsWith("ja") || locale.startsWith("zh_cn")
                || locale.startsWith("zh_tw") || locale.startsWith("ko");
    }

    private String controlText(String key, String fallback) {
        if (useAsciiControlLabels())
            return fallback;
        return applet.uiText(key, fallback);
    }

    private void appendSample(StringBuffer sample, String text) {
        if (text == null || text.length() == 0)
            return;
        if (sample.length() > 0)
            sample.append(' ');
        sample.append(text);
    }

    private static Font chooseUiFont(AbstractYahooGamesApplet applet,
            String sampleText, int style, int size) {
        String[] candidates = getFontCandidates(
                applet != null ? applet.intl_code : null);
        for (int i = 0; i < candidates.length; i++) {
            Font font = new Font(candidates[i], style, size);
            if (canDisplay(font, sampleText))
                return font;
        }
        return new Font("Dialog", style, size);
    }

    private static boolean canDisplay(Font font, String sampleText) {
        if (sampleText == null || sampleText.length() == 0)
            return true;
        return font.canDisplayUpTo(sampleText) == -1;
    }

    private static String[] getFontCandidates(String locale) {
        if (locale == null)
            locale = "us";
        locale = locale.toLowerCase();
        if (locale.startsWith("ja"))
            return new String[] { "Yu Gothic UI", "Meiryo", "MS UI Gothic",
                    "Dialog" };
        if (locale.startsWith("zh_cn"))
            return new String[] { "Microsoft YaHei UI", "Microsoft YaHei",
                    "SimSun", "Dialog" };
        if (locale.startsWith("zh_tw"))
            return new String[] { "Microsoft JhengHei UI",
                    "Microsoft JhengHei", "PMingLiU", "Dialog" };
        if (locale.startsWith("ko"))
            return new String[] { "Malgun Gothic", "Gulim", "Dotum",
                    "Dialog" };
        if (locale.startsWith("hi") || locale.startsWith("bn")
                || locale.startsWith("gu") || locale.startsWith("kn")
                || locale.startsWith("ml") || locale.startsWith("mr")
                || locale.startsWith("or") || locale.startsWith("pa")
                || locale.startsWith("ta") || locale.startsWith("te"))
            return new String[] { "Nirmala UI", "Mangal", "Kokila",
                    "Dialog" };
        return new String[] { "Segoe UI", "Tahoma", "Arial Unicode MS",
                "Dialog" };
    }

    protected static Frame findOwnerFrame(Applet applet) {
        Container current = applet;
        while (current != null) {
            if (current instanceof Frame)
                return (Frame) current;
            current = current.getParent();
        }
        return new Frame();
    }

    private void applyMode(int newMode) {
        mode = newMode;
        if (mode == MODE_SIGN_IN) {
            lblField1.setText(controlText("user_name", "User Name"));
            lblField2.setText(controlText("password", "Password"));
            lblField3.setText(controlText("confirm_password", "Confirm Password"));
            lblField4.setText(controlText("email", "Email"));
            row3.setVisible(false);
            row4.setVisible(false);
            txtField2.setEchoChar('*');
            txtField3.setEchoChar('*');
            txtField4.setEchoChar((char) 0);
            btnPrimary.setLabel(controlText("enter_room", "Enter Room"));
        }
        else if (mode == MODE_REGISTER) {
            lblField1.setText(controlText("user_name", "User Name"));
            lblField2.setText(controlText("password", "Password"));
            lblField3.setText(controlText("confirm_password", "Confirm Password"));
            lblField4.setText(controlText("invite_code", "Invite Code"));
            row3.setVisible(true);
            row4.setVisible(false);
            txtField2.setEchoChar('*');
            txtField3.setEchoChar('*');
            txtField4.setEchoChar((char) 0);
            btnPrimary.setLabel(controlText("create_account", "Create Account"));
            txtField3.setText("");
            txtField4.setText("");
        }
        else {
            lblField1.setText(controlText("user_name", "User Name"));
            lblField2.setText(controlText("current_password", "Current Password"));
            lblField3.setText(controlText("new_password", "New Password"));
            lblField4.setText(controlText("confirm_new_password", "Confirm New Password"));
            row3.setVisible(true);
            row4.setVisible(true);
            txtField2.setEchoChar('*');
            txtField3.setEchoChar('*');
            txtField4.setEchoChar('*');
            btnPrimary.setLabel(controlText("update_password", "Update Password"));
        }
        setTitle(applet.uiText("dialog_title", "Game Login"));
        refreshModeButtons();
        pack();
    }

    private void refreshModeButtons() {
        highlightModeButton(btnSignInMode, mode == MODE_SIGN_IN);
        highlightModeButton(btnRegisterMode, mode == MODE_REGISTER);
        highlightModeButton(btnChangePasswordMode,
                mode == MODE_CHANGE_PASSWORD);
    }

    private void highlightModeButton(Button button, boolean active) {
        if (active) {
            button.setBackground(ACCENT);
            button.setForeground(Color.white);
        }
        else {
            button.setBackground(new Color(194, 180, 147));
            button.setForeground(INK);
        }
    }

    private void submit() {
        if (mode == MODE_SIGN_IN) {
            applet.submitAppletLogin(txtField1.getText(), txtField2.getText());
        }
        else if (mode == MODE_REGISTER) {
            applet.submitAppletRegister(txtField1.getText(), txtField2.getText(),
                    txtField3.getText());
        }
        else {
            applet.submitAppletPasswordChange(txtField1.getText(),
                    txtField2.getText(), txtField3.getText(),
                    txtField4.getText());
        }
    }

    @Override
    public boolean action(Event event, Object obj) {
        if (event.target == btnPrimary || event.target == txtField1
                || event.target == txtField2 || event.target == txtField3
                || event.target == txtField4) {
            submit();
            return true;
        }
        if (event.target == btnSignInMode) {
            showSignIn();
            return true;
        }
        if (event.target == btnRegisterMode) {
            showRegister();
            return true;
        }
        if (event.target == btnChangePasswordMode) {
            showChangePassword();
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
        heroCanvas.stopAnimation();
        setVisible(false);
    }

    public void setBusy(boolean busy) {
        txtField1.setEnabled(!busy);
        txtField2.setEnabled(!busy);
        txtField3.setEnabled(!busy);
        txtField4.setEnabled(!busy);
        btnPrimary.setEnabled(!busy);
        btnSignInMode.setEnabled(!busy);
        btnRegisterMode.setEnabled(!busy);
        btnChangePasswordMode.setEnabled(!busy);
    }

    public void setMessage(String message) {
        lblMessage.setText(message != null ? message : controlText("please_sign_in", "Please sign in"));
    }

    public void setUsername(String username) {
        if (username == null)
            username = "";
        txtField1.setText(username);
    }

    public void showSignIn() {
        applyMode(MODE_SIGN_IN);
        txtField2.requestFocus();
    }

    public void showRegister() {
        applyMode(MODE_REGISTER);
        txtField1.requestFocus();
    }

    public void showChangePassword() {
        applyMode(MODE_CHANGE_PASSWORD);
        txtField1.requestFocus();
    }

    private void centerOverApplet() {
        try {
            Rectangle bounds = applet.getBounds();
            java.awt.Point location = applet.getLocationOnScreen();
            int x = location.x + Math.max(0, (bounds.width - getWidth()) / 2);
            int y = location.y + Math.max(0, (bounds.height - getHeight()) / 2);
            setLocation(x, y);
        }
        catch (Throwable t) {
            Frame owner = findOwnerFrame(applet);
            if (owner != null) {
                int x = owner.getX() + Math.max(0,
                        (owner.getWidth() - getWidth()) / 2);
                int y = owner.getY() + Math.max(0,
                        (owner.getHeight() - getHeight()) / 2);
                setLocation(x, y);
            }
        }
    }

    public void showDialog() {
        pack();
        centerOverApplet();
        heroCanvas.startAnimation();
        setVisible(true);
        toFront();
        requestFocus();
        txtField1.requestFocus();
    }

    @Override
    public Insets getInsets() {
        Insets insets = super.getInsets();
        return new Insets(insets.top + 10, insets.left + 10, insets.bottom + 10,
                insets.right + 10);
    }

    static class HeroCanvas extends Canvas {

        private static final long serialVersionUID = 1L;

        private final AbstractYahooGamesApplet applet;
        private transient Timer timer;
        private transient Image backBuffer;
        private int shimmerX;
        private int shimmerStep = 14;

        HeroCanvas(AbstractYahooGamesApplet applet) {
            this.applet = applet;
            setSize(360, 130);
            setBackground(FRAME_BG);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(360, 130);
        }

        void startAnimation() {
            stopAnimation();
            shimmerX = -80;
            timer = new Timer("AppletLoginHero", true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    shimmerX += shimmerStep;
                    if (shimmerX > getWidth() + 80)
                        shimmerX = -80;
                    repaint(0L);
                }
            }, 0L, 45L);
        }

        void stopAnimation() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        @Override
        public void paint(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0)
                return;
            if (backBuffer == null || backBuffer.getWidth(this) != width
                    || backBuffer.getHeight(this) != height)
                backBuffer = createImage(width, height);
            Graphics bufferGraphics = backBuffer.getGraphics();
            drawHero(bufferGraphics, width, height);
            g.drawImage(backBuffer, 0, 0, this);
            bufferGraphics.dispose();
        }

        private void drawHero(Graphics g, int width, int height) {
            g.setColor(FRAME_BG);
            g.fillRect(0, 0, width, height);

            g.setColor(new Color(49, 61, 90));
            g.fillRoundRect(12, 12, width - 24, height - 24, 28, 28);

            g.setColor(new Color(24, 29, 47));
            g.fillRoundRect(20, 20, width - 40, height - 40, 24, 24);

            g.setColor(new Color(76, 96, 143));
            g.fillRect(28, 28, width - 56, 10);

            g.setColor(ACCENT);
            g.fillRect(28, height - 40, width - 56, 6);

            g.setColor(new Color(255, 214, 150, 70));
            g.fillRect(shimmerX, 24, 54, height - 48);

            g.setColor(Color.white);
            g.setFont(chooseUiFont(applet, roomTitle(), Font.BOLD, 24));
            g.drawString("Y! Games Revival", 34, 64);

            g.setColor(new Color(232, 220, 198));
            g.setFont(chooseUiFont(applet, roomTitle(), Font.PLAIN, 13));
            g.drawString(roomTitle(), 36, 88);

            g.setColor(SUBTLE);
            g.setFont(chooseUiFont(applet,
                    applet.uiText("play_register_update_account",
                            "PLAY, REGISTER, OR UPDATE YOUR ACCOUNT"),
                    Font.BOLD, 11));
            g.drawString(applet.uiText("play_register_update_account",
                    "PLAY, REGISTER, OR UPDATE YOUR ACCOUNT"), 36, 108);

            String launcherVersion = applet.getParameter("launcher_version");
            if (launcherVersion != null && launcherVersion.length() > 0) {
                g.setColor(new Color(210, 201, 182));
                g.setFont(new Font("Dialog", Font.PLAIN, 10));
                g.drawString("v" + launcherVersion, width - 58, height - 18);
            }
        }

        private String roomTitle() {
            String room = applet.getParameter("room");
            if (room == null || room.length() == 0 || "undefined".equals(room))
                room = "the game room";
            room = room.replace('_', ' ');
            StringBuffer title = new StringBuffer();
            boolean cap = true;
            for (int i = 0; i < room.length(); i++) {
                char c = room.charAt(i);
                if (cap && Character.isLetter(c)) {
                    title.append(Character.toUpperCase(c));
                    cap = false;
                }
                else {
                    title.append(c);
                    cap = c == ' ';
                }
            }
            return applet.uiText("welcome_back_enter_room",
                    "Welcome back. Enter {0}.", title.toString());
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }
    }
}
