/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 7/23/2024
 * Github link: https://github.com/kireiiiiiiii
 */

package flaggi.shared.common;

import java.awt.BasicStroke;
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
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private final InteractableHandler handler;
    private final RenderingEngine renderingEngine;
    private final ArrayList<Renderable> widgets;
    private JFrame appFrame;
    private boolean isRendering;
    private int[] viewportOffset, contentSize;
    private double[] renderScale;

    // Constructor ---------------------------------------------------------------

    /**
     * Constructs a GPanel with the specified parameters.
     *
     * @param windowWidth  - window width.
     * @param windowHeight - window height.
     * @param resizable    - if the panel is resizable by the user.
     * @param fullscreen   - if the panel is fullscreen. Cannot be changed.
     * @param appTitle     - title of the window (name of the app).
     * @param handeler     - {@code InteractableHandeler} object, that will handle
     *                     panel interaction.
     */
    public GPanel(int windowWidth, int windowHeight, boolean resizable, String appTitle, InteractableHandler handler) {
        this(new Dimension(windowWidth, windowHeight), resizable, appTitle, handler);
        this.contentSize = new int[] { windowWidth, windowHeight };
        startRendering();
    }

    /**
     * Constructs a GPanel with the specified parameters.
     *
     * @param handler      - {@code InteractableHandler} object that will handle
     *                     panel user interaction.
     * @param windowWidth  - The width of the window.
     * @param windowHeight - The height of the window.
     * @param resizable    - If the panel is resizable by the user.
     * @param appTitle     - The title of the window (name of the app).
     * @param renderScale  - The scale for rendering.
     */
    public GPanel(int windowWidth, int windowHeight, int contentWidth, int contentHeight, boolean resizable, String appTitle, InteractableHandler handler) {
        this(new Dimension(windowWidth, windowHeight), resizable, appTitle, handler);
        this.contentSize = new int[] { contentWidth, contentHeight };
        startRendering();
    }

    private GPanel(Dimension size, boolean resizable, String appTitle, InteractableHandler handler) {
        this.renderingEngine = new RenderingEngine();
        this.widgets = new ArrayList<>();
        this.viewportOffset = new int[2];
        this.renderScale = new double[] { 1.0, 1.0 };
        this.isRendering = false;
        this.handler = handler;
        this.setPreferredSize(size);
        this.appFrame = getDefaultJFrame(this, resizable, appTitle, Color.WHITE);

        setupListeners();
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
        AffineTransform originalTransform = g.getTransform();
        int[] contentSize = scaleGraphicPane(g, this.contentSize[0], this.contentSize[1], false);

        synchronized (widgets) {
            widgets.stream().filter(Renderable::isVisible).forEach(r -> r.render(g, contentSize, viewportOffset, appFrame.getFocusCycleRootAncestor()));
        }

        g.setColor(Color.RED); // TODO DEBUG
        g.setStroke(new BasicStroke(10));
        g.drawRect(0, 0, contentSize[0], contentSize[1]);

        g.setTransform(originalTransform);
    }

    // Accesors -----------------------------------------------------------------cv

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
     * Sets the rendering scale, every element in the window will act according to
     * the scale, but the window will stay the same size.
     *
     * @param x - X scale
     * @param y - Y scale
     */
    public void setRenderScale(double x, double y) {
        this.renderScale[0] = x;
        this.renderScale[1] = y;
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

    public void add(Renderable... renderable) {
        synchronized (this.widgets) {
            for (Renderable r : renderable) {
                int value = r.getZIndex();
                int index = binarySearchInsertZIndex(value);
                this.widgets.add(index, r);
            }
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
            this.widgets.forEach(r -> r.setVisibility(visible));
        }
    }

    /**
     * Sets the visibility of widgets with a specific tag.
     */
    public void toggleTaggedWidgetsVisibility(String tag, boolean visible) {
        synchronized (this.widgets) {
            this.widgets.stream().filter(r -> r.getTags().contains(tag)).forEach(r -> r.setVisibility(visible));
        }
    }

    // Private ------------------------------------------------------------------

    /**
     * @see GPanel#GPanel(InteractableHandler, int, int, boolean, String, Color)
     */
    private static JFrame getDefaultJFrame(JPanel panel, boolean resizable, String appTitle, Color backgroundColor) {
        JFrame frame = new JFrame(appTitle);
        frame.setResizable(resizable);
        frame.setBackground(backgroundColor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        System.out.println(frame.getWidth() + " | " + frame.getHeight());
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
        this.requestFocusInWindow();
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

    /**
     * Scales a graphic pane to fit inside a the JFrame application window.
     *
     * @param g             - target {@code Graphics2D} object.
     * @param contentWidth  - width of the content.
     * @param contentHeight - height of the content.
     * @param fitFrame      - if the content should be fitted to the frame,
     *                      potentionally leaving empty space around the content, or
     *                      scaled to fit the frame.
     * @return the size of the content after scaling. Will remain the same if fit
     *         frame is false.
     */
    private int[] scaleGraphicPane(Graphics2D g, int contentWidth, int contentHeight, boolean fitFrame) {
        double scaleX = (double) this.getWidth() / contentWidth;
        double scaleY = (double) this.getHeight() / contentHeight;
        int[] contentSize = new int[] { contentWidth, contentHeight };

        if (fitFrame) {
            if (scaleX < scaleY) {
                contentSize[1] *= scaleX;
                g.scale(scaleX, scaleX);
            } else {
                contentSize[0] *= scaleY;
                g.scale(scaleY, scaleY);
            }
        } else {
            if (scaleX > scaleY) {
                contentSize[1] = (int) (this.getHeight() / scaleX);
                g.scale(scaleX, scaleX);
            } else {
                contentSize[0] = (int) (this.getWidth() / scaleY);
                g.scale(scaleY, scaleY);
            }
        }
        return contentSize;
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
            Thread renderThread = new Thread(this, GPanel.class.getSimpleName() + ": " + RenderingEngine.class.getSimpleName() + ": Render Thread");
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
                    long sleepTime = (1_000_000_000 / targetFPS) - (System.nanoTime() - startTime);
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
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

    /**
     * Abstract base class for UI elements.
     */
    public static abstract class Renderable {
        private final AtomicBoolean visibility = new AtomicBoolean(true);
        private final List<String> tags = new ArrayList<>();
        private int zIndex;

        public Renderable(int zIndex, String... initialTags) {
            this.zIndex = zIndex;
            if (initialTags != null) {
                tags.addAll(Arrays.asList(initialTags));
            }
        }

        public abstract void render(Graphics2D g, int[] size, int[] viewportOffset, Container focusCycleRootAncestor);

        public int getZIndex() {
            return this.zIndex;
        }

        public void setZIndex(int zIndex) {
            this.zIndex = zIndex;
        }

        public boolean isVisible() {
            return this.visibility.get();
        }

        public void setVisibility(boolean visibility) {
            this.visibility.set(visibility);
        }

        public List<String> getTags() {
            return this.tags;
        }

        public void addTag(String tag) {
            this.tags.add(tag);
        }

        public void removeTag(String tag) {
            if (tags.contains(tag)) {
                this.tags.remove(tag);
            }
        }
    }

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
     * Interface for handling user input. Extends java.awt.event listeners:
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
