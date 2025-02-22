/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 7/23/2024
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggishared.common;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * <h2>GPanel</h2> {@code GPanel} is a custom {@code JPanel} object that handles
 * rendering of graphical elements using an internal Renderer, running on a
 * separate thread. It integrates with a {@code JFrame} to manage window
 * properties.
 * </p>
 * <hr>
 * <br>
 * <h3>Constructor</h3> {@code GPanel} will be upon costruction set with these
 * values:
 * <ul>
 * <li><b>FPS</b> - interval, at which will the renderer calculate new
 * frames</li>
 * <li><b>Window width & window height</b> - dimensions of the
 * {@code JFrame}</li>
 * <li><b>Resizable</b> - makes the window fixed size or resizable by user</li>
 * <li><b>App title</b> - text displayed on the {@code JFrame} as the app
 * title</li>
 * </ul>
 * The {@code JFrame} will be put in the middle of the user screen by default.
 * It will also have the default icon, that can be changed separatly by using
 * the {@code setIcon()} method. If the project is being packaged, change the
 * style the icon is being accesed. The app will be made visible, and the
 * rendering prosess will start.
 * </p>
 * <hr>
 * <h3>UI Elements</h3> This class supports adding renderable objects that are
 * drawn in a layered order based on their {@code z-index}. The higher the
 * index, the more on top they are. The object must implement the interface
 * {@code Renderable}. This is how the objects are added:
 *
 * <pre>
 * <code>
 * public void add(Renderable renderable);
 * </code>
 * </pre>
 *
 * </p>
 * <hr>
 * <h3>Rendering</h3> The rendering loop can be controlled with {@code start()}
 * and {@code stop()} methods.
 * </p>
 * The Renderer class inside GPanel controls the rendering loop, adjusting its
 * interval based on the provided frames-per-second value.
 * </p>
 * <hr>
 * <h3>Action listeners</h3> This class implements {@codeMouseListener} and
 * {@code MouseMotionListener} to handle mouse interaction events. This class
 * also includes a public interface {@code InteracableHandeler} that can be used
 * on classes that handle these events. Widgets that can be interacted with need
 * to implement the {@code Interactable} interface also contained in this class.
 * <hr>
 *
 * @author Matěj Šťastný aka
 *         <a href="https://github.com/kireiiiiiiii">@kireiiiiiiii</a>
 * @since 7/23/2024
 */
public class GPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {

    // Variables -----------------------------------------------------------------

    private final JFrame appFrame;
    private final InteractableHandler handler;
    private final RenderingEngine renderingEngine;
    private final ArrayList<Renderable> widgets;
    private boolean isRendering;
    private int[] viewportOffset;

    // Constructor ---------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param handeler               - {@code InteractableHandeler} object, that
     *                               will handle panel interaction.
     * @param windowWidth            - window width.
     * @param windowHeight           - window height.
     * @param resizable              - if the panel is resizable by the user.
     * @param fullscreen             - if the panel is fullscreen. Cannot be
     *                               changed.
     * @param appTitle               - title of the window (name of the app).
     * @param loadingBackgroundColor - color of the {@code JFrame} displayed before
     *                               the {@code JPanel} is initialized.
     */
    public GPanel(InteractableHandler handler, int windowWidth, int windowHeight, boolean resizable, String appTitle, Color loadingBackgroundColor) {
        this.renderingEngine = new RenderingEngine();
        this.widgets = new ArrayList<>();
        this.viewportOffset = new int[2];
        this.isRendering = false;
        this.handler = handler;

        this.appFrame = getDefaultJFrame(windowWidth, windowHeight, resizable, appTitle);
        this.appFrame.setBackground(loadingBackgroundColor);
        this.appFrame.add(this);

        setupListeners();
        startRendering();
    }

    /**
     * Constructor without loading color. Useful for {@code GPanel}s without
     * difficult initial calculations.
     *
     * @param handeler     - {@code InteractableHandeler} object, that will handle
     *                     panel interaction.
     * @param windowWidth  - window width.
     * @param windowHeight - window height.
     * @param resizable    - if the panel is resizable by the user.
     * @param fullscreen   - if the panel is fullscreen. Cannot be changed.
     * @param appTitle     - title of the window (name of the app).
     */
    public GPanel(InteractableHandler handeler, int windowWidth, int windowHeight, boolean resizable, String appTitle) {
        this(handeler, windowWidth, windowHeight, resizable, appTitle, Color.WHITE);
    }

    // Rendering -----------------------------------------------------------------

    public void startRendering() {
        if (!this.isRendering && (this.isRendering = true)) {
            this.renderingEngine.start();
        }
    }

    public void stopRendering() {
        if (this.isRendering && !(this.isRendering = false)) {
            this.renderingEngine.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        int[] size = { getWidth(), getHeight() };

        synchronized (this.widgets) {
            this.widgets.stream().filter(Renderable::isVisible).forEach(renderable -> renderable.render(g, size, this.viewportOffset, this.appFrame.getFocusCycleRootAncestor()));
        }
    }

    // Accesors -----------------------------------------------------------------

    /*
     * Size methods return the measurements of the owning {@JFrame} object. Methods
     * return zero, if owner wasn't initialized (is {@code null}).
     */

    @Override
    public int getWidth() {
        return this.appFrame == null ? 0 : this.appFrame.getWidth();
    }

    @Override
    public int getHeight() {
        return this.appFrame == null ? 0 : this.appFrame.getHeight();
    }

    @Override
    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public JFrame getAppFrame() {
        return this.appFrame;
    }

    public AtomicInteger getFps() {
        return this.renderingEngine.getFps();
    }

    public <T> ArrayList<T> getWidgetsOfClass(Class<T> targetClass) {
        synchronized (this.widgets) {
            ArrayList<T> list = new ArrayList<>();
            this.widgets.stream().filter(targetClass::isInstance).forEach(r -> list.add(targetClass.cast(r)));
            return list;
        }
    }

    public ArrayList<Renderable> getInteractables() {
        return getWidgetsByInterface(Interactable.class);
    }

    public ArrayList<Renderable> getTypables() {
        return getWidgetsByInterface(Typable.class);
    }

    public ArrayList<Renderable> getScrollables() {
        return getWidgetsByInterface(Scrollable.class);
    }

    // Modifiers ----------------------------------------------------------------

    /**
     * Sets the viewport offset. 0,0 is the standard position. This viewport value
     * will be passed as a param to the widgets render function.
     *
     * @param viewportOffset
     */
    public void setViewportOffset(int[] offset) {
        this.viewportOffset = offset;
    }

    /**
     * Clears the widgets list, and sets it to the one given as parameter.
     *
     * @param widgets - target widget list.
     */
    public void setWidgets(ArrayList<Renderable> widgets) {
        synchronized (this.widgets) {
            this.widgets.clear();
            this.add(widgets);
        }
    }

    public void add(Renderable renderable) {
        synchronized (this.widgets) {
            int value = renderable.getZIndex();
            int index = binarySearchInsertZIndex(value);
            this.widgets.add(index, renderable);
        }
    }

    public void add(List<Renderable> widgets) {
        synchronized (this.widgets) {
            widgets.forEach(w -> this.widgets.add(w));
        }
    }

    public boolean remove(Renderable renderable) {
        synchronized (this.widgets) {
            return this.widgets.remove(renderable);
        }
    }

    public <T> void removeWidgetsOfClass(Class<T> c) {
        synchronized (this.widgets) {
            this.widgets.removeIf(r -> c.isInstance(r));
        }
    }

    public void removeWidgetsWithTag(String tag) {
        synchronized (this.widgets) {
            this.widgets.removeIf(r -> r.getTags().contains(tag));
        }
    }

    /**
     * Sets the maximum FPS of the rendering engine. If set to 0, the FPS will be
     * unlimited.
     *
     * @param fps - target FPS.
     */
    public void setFpsCap(int fps) {
        this.renderingEngine.setFpsCap(fps);
    }

    /**
     * Changes the application icon. Works for MacOS dock icon too.
     *
     * @param path - path of the icon
     */
    public void setIcon(Image icon) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                Class<?> appClass = Class.forName("com.apple.eawt.Application");
                Object appInstance = appClass.getMethod("getApplication").invoke(null);
                appClass.getMethod("setDockIconImage", Image.class).invoke(appInstance, icon);
            } else {
                this.appFrame.setIconImage(icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OS specific icon setter, that sets a different icon depeending on the OS.
     *
     * @param winIcon   - icon for Window OS.
     * @param macIcon   - icon for MacOS.
     * @param linuxIcon - icon for Linux based OS.
     * @param other     - other not common OS.
     */
    public void setIconOSDependend(Image winIcon, Image macIcon, Image linuxIcon, Image other) {
        String os = System.getProperty("os.name").toLowerCase();
        Image icon;
        if (os.contains("win")) {
            icon = winIcon;
        } else if (os.contains("mac")) {
            icon = macIcon;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            icon = linuxIcon;
        } else {
            icon = other;
        }
        // Image icon = switch (os) { // TODO Change on update to above Java 14
        // case String osName when osName.contains("win") -> winIcon;
        // case String osName when osName.contains("mac") -> macIcon;
        // case String osName when osName.contains("nix"), String osName when
        // osName.contains("nux"), String osName when osName.contains("aix") ->
        // linuxIcon;
        // default -> other;
        // };
        setIcon(icon);
    }

    /**
     * Sets an action meant to be performed when the JPanel window is closed.
     *
     * @param operation - {@code Runnable} executed on window close.
     */
    public void setExitOperation(Runnable operation) {
        this.appFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.appFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                operation.run();
            }
        });
    }

    // Widget visibility --------------------------------------------------------

    /**
     * Sets the visibility of all widgets.
     */
    public void toggleWidgetsVisibility(boolean visible) {
        synchronized (this.widgets) {
            for (Renderable r : this.widgets) {
                // r.setVisibility(visible); // TODO new widget class
                if (visible) {
                    r.show();
                } else {
                    r.hide();
                }
            }
        }
    }

    /**
     * Sets the visibility of widgets with a specific tag.
     */
    public void toggleTaggedWidgetsVisibility(String tag, boolean visible) {
        synchronized (this.widgets) {
            for (Renderable r : this.widgets) {
                if (r.getTags().contains(tag)) {
                    // r.setVisibility(visible); // TODO new widget class
                    if (visible) {
                        r.show();
                    } else {
                        r.hide();
                    }
                }
            }
        }
    }

    // Private ------------------------------------------------------------------

    /**
     * @see GPanel#GPanel(InteractableHandler, int, int, boolean, String, Color)
     */
    private static JFrame getDefaultJFrame(int windowWidth, int windowHeight, boolean resizable, String appTitle) {
        JFrame frame = new JFrame();
        frame.setSize(windowWidth, windowHeight);
        frame.setResizable(resizable);
        frame.setTitle(appTitle);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }

    /**
     * @see GPanel#GPanel(InteractableHandler, int, int, boolean, String, Color)
     */
    private void setupListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);
        requestFocusInWindow();
    }

    /**
     * @see GPanel#getInteractables
     * @see GPanel#getTypables
     * @see GPanel#getScrollables
     */
    private <T> ArrayList<Renderable> getWidgetsByInterface(Class<T> targetInterface) {
        synchronized (this.widgets) {
            ArrayList<Renderable> list = new ArrayList<>();
            for (Renderable r : this.widgets) {
                if (targetInterface.isInstance(r)) {
                    list.add(r);
                }
            }
            return list;
        }
    }

    /**
     * @see GPanel#add(Renderable)
     */
    private int binarySearchInsertZIndex(int zIndex) {
        int low = 0;
        int high = this.widgets.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = this.widgets.get(mid).getZIndex();

            if (midVal < zIndex) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    // Render engine ------------------------------------------------------------

    /**
     * The RenderingEngine class is responsible for managing the rendering loop of
     * the GPanel. It runs in its own thread and continuously repaints the GPanel at
     * max framerate.
     */
    private class RenderingEngine implements Runnable {
        private final AtomicInteger currentFPS = new AtomicInteger(0);
        private boolean running = false;
        private long lastFpsTime = 0;
        private int targetFPS = 0;

        public void start() {
            running = true;
            Thread renderThread = new Thread(this, "Render Thread");
            renderThread.start();
        }

        public void stop() {
            running = false;
        }

        public void setFpsCap(int fps) {
            targetFPS = fps;
        }

        @Override
        public void run() {
            int frameCount = 0;
            lastFpsTime = System.nanoTime();

            while (running) {
                long startTime = System.nanoTime();
                render();
                frameCount++;

                long currentTime = System.nanoTime();
                if (currentTime - lastFpsTime >= 1_000_000_000) {
                    currentFPS.set(frameCount);
                    frameCount = 0;
                    lastFpsTime = currentTime;
                }

                if (targetFPS > 0) {
                    long frameTime = 1_000_000_000 / targetFPS;
                    long elapsedTime = System.nanoTime() - startTime;
                    long sleepTime = frameTime - elapsedTime;
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Thread.yield();
                }
            }
        }

        private void render() {
            SwingUtilities.invokeLater(() -> GPanel.this.repaint());
        }

        public AtomicInteger getFps() {
            return currentFPS;
        }
    }

    // Renderable abs class -----------------------------------------------------

    // Old interface, implement new one below in next commit
    public interface Renderable {

        public void render(Graphics2D g, int[] size, int[] viewportOffset, Container focusCycleRootAncestor);

        public int getZIndex();

        public boolean isVisible();

        public void hide();

        public void show();

        public ArrayList<String> getTags();

    }

    // /**
    // * Abstract base class for UI elements.
    // */
    // public abstract class Renderable {
    // private final AtomicBoolean visibility = new AtomicBoolean(true);
    // private final List<String> tags = new ArrayList<>();
    // private final int zIndex;

    // public Renderable(int zIndex, String... initialTags) {
    // this.zIndex = zIndex;
    // if (initialTags != null) {
    // tags.addAll(Arrays.asList(initialTags));
    // }
    // }

    // /**
    // * Renders the object using a {@code Graphics2D} object reference.
    // */
    // public abstract void render(Graphics2D g, int[] size, int[] origin, Container
    // focusCycleRootAncestor);

    // /**
    // * Returns the Z-Index of the object.
    // */
    // public int getZIndex() {
    // return zIndex;
    // }

    // /**
    // * Returns the visibility status.
    // */
    // public boolean isVisible() {
    // return visibility.get();
    // }

    // /**
    // * Hides the element (prevents rendering).
    // */
    // public void hide() {
    // visibility.set(false);
    // }

    // /**
    // * Shows the element (allows rendering).
    // */
    // public void show() {
    // visibility.set(true);
    // }

    // /**
    // * Returns the tags of the element.
    // */
    // public List<String> getTags() {
    // return tags;
    // }

    // /**
    // * Adds a new tag to the element.
    // */
    // public void addTag(String tag) {
    // tags.add(tag);
    // }
    // }

    // Widget input interfaces --------------------------------------------------

    /**
     * Interface for interactable UI elements.
     *
     */
    public interface Interactable {

        /**
         * Checks, if the button was clicked with the target mouse event.
         *
         * @param e - target {@code MouseEvent}.
         * @return {@code true} if interacted, and {@code false} if not.
         */
        public boolean interact(MouseEvent e);

    }

    /**
     * Interface for typable UI elements.
     *
     */
    public interface Typable {
        void type(KeyEvent k);
    }

    /**
     * Interface for scrollable UI elements.
     *
     */
    public interface Scrollable {
        void scroll(MouseWheelEvent e);
    }

    // User input forwaring -----------------------------------------------------

    /**
     * Interface for handling user input. Extends Java AWT listeners:
     * <ul>
     * <li>{@code MouseListener}</li>
     * <li>{@code MouseMotionListener}</li>
     * <li>{@code MouseWheelListener}</li>
     * <li>{@code KeyListener}</li>
     * </ul>
     */
    public interface InteractableHandler extends MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
        // No need to manually define methods, as they are inherited
    }

    private <T> void forwardEvent(Consumer<T> method, T event) {
        if (handler != null) {
            method.accept(event);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        forwardEvent(handler::mouseDragged, e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        forwardEvent(handler::mouseMoved, e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        forwardEvent(handler::mouseClicked, e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        forwardEvent(handler::mousePressed, e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        forwardEvent(handler::mouseReleased, e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        forwardEvent(handler::mouseEntered, e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        forwardEvent(handler::mouseExited, e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        forwardEvent(handler::mouseWheelMoved, e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        forwardEvent(handler::keyTyped, e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        forwardEvent(handler::keyPressed, e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        forwardEvent(handler::keyReleased, e);
    }

}
