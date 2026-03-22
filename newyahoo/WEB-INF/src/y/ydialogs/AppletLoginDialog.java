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
import java.awt.FontMetrics;
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

    private static final Color FRAME_BG = new Color(22, 26, 38);
    private static final Color PANEL_BG = new Color(236, 224, 192);
    private static final Color FIELD_BG = new Color(255, 248, 224);
    private static final Color ACCENT = new Color(209, 124, 48);
    private static final Color ACCENT_SHADOW = new Color(107, 54, 21);
    private static final Color INK = new Color(44, 32, 24);
    private static final Color SUBTLE = new Color(110, 86, 58);

    protected AbstractYahooGamesApplet applet;
    protected Label lblMessage;
    protected TextField txtUsername;
    protected TextField txtPassword;
    protected Button btnLogin;
    protected Button btnCancel;
    protected HeroCanvas heroCanvas;

    public AppletLoginDialog(AbstractYahooGamesApplet applet) {
        super(findOwnerFrame(applet), "Game Login", false);
        this.applet = applet;

        setBackground(FRAME_BG);
        setLayout(new BorderLayout(10, 10));

        heroCanvas = new HeroCanvas(applet);
        add(heroCanvas, BorderLayout.NORTH);

        Panel body = new Panel(new BorderLayout(0, 10));
        body.setBackground(PANEL_BG);

        lblMessage = new Label("Please sign in");
        lblMessage.setForeground(INK);
        lblMessage.setBackground(PANEL_BG);
        lblMessage.setFont(new Font("Dialog", Font.BOLD, 14));
        body.add(lblMessage, BorderLayout.NORTH);

        Panel fields = new Panel(new GridLayout(2, 2, 8, 8));
        fields.setBackground(PANEL_BG);
        fields.add(createFieldLabel("User Name"));
        txtUsername = new TextField(24);
        styleField(txtUsername);
        fields.add(txtUsername);
        fields.add(createFieldLabel("Password"));
        txtPassword = new TextField(24);
        txtPassword.setEchoChar('*');
        styleField(txtPassword);
        fields.add(txtPassword);
        body.add(fields, BorderLayout.CENTER);

        Panel buttons = new Panel(new GridLayout(1, 2, 8, 0));
        buttons.setBackground(PANEL_BG);
        btnLogin = new Button("Enter Room");
        btnCancel = new Button("Cancel");
        styleButton(btnLogin, true);
        styleButton(btnCancel, false);
        buttons.add(btnLogin);
        buttons.add(btnCancel);
        body.add(buttons, BorderLayout.SOUTH);

        add(wrapBody(body), BorderLayout.CENTER);

        pack();
        setResizable(false);
        setAlwaysOnTop(true);
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setForeground(INK);
        label.setBackground(PANEL_BG);
        label.setFont(new Font("Dialog", Font.BOLD, 12));
        return label;
    }

    private Panel wrapBody(Panel body) {
        Panel wrapper = new Panel(new BorderLayout());
        wrapper.setBackground(FRAME_BG);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    private void styleField(TextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(INK);
        field.setFont(new Font("Dialog", Font.PLAIN, 13));
    }

    private void styleButton(Button button, boolean primary) {
        button.setFont(new Font("Dialog", Font.BOLD, 12));
        if (primary) {
            button.setBackground(ACCENT);
            button.setForeground(Color.white);
        } else {
            button.setBackground(new Color(194, 180, 147));
            button.setForeground(INK);
        }
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

    private void submit() {
        applet.submitAppletLogin(txtUsername.getText(), txtPassword.getText());
    }

    @Override
    public boolean action(Event event, Object obj) {
        if (event.target == btnLogin || event.target == txtUsername
                || event.target == txtPassword) {
            submit();
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
                int x = owner.getX() + Math.max(0, (owner.getWidth() - getWidth()) / 2);
                int y = owner.getY() + Math.max(0, (owner.getHeight() - getHeight()) / 2);
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
        if (txtUsername.getText() != null && txtUsername.getText().length() > 0)
            txtPassword.requestFocus();
        else
            txtUsername.requestFocus();
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
            g.setFont(new Font("Dialog", Font.BOLD, 24));
            g.drawString("Y! Games Revival", 34, 64);

            g.setColor(new Color(232, 220, 198));
            g.setFont(new Font("Dialog", Font.PLAIN, 13));
            g.drawString(roomTitle(), 36, 88);

            g.setColor(SUBTLE);
            g.setFont(new Font("Dialog", Font.BOLD, 11));
            g.drawString("SIGN IN TO CONNECT", 36, 108);
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
                } else {
                    title.append(c);
                    cap = c == ' ';
                }
            }
            return "Welcome back. Enter " + title.toString() + ".";
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }
    }
}
