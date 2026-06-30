import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Botão com cantos arredondados e efeito de destaque ao passar o mouse,
 * usado nas ações principais da aplicação (Calcular, Iniciar Animação,
 * Vista Padrão, Resetar...).
 */
public class BotaoArredondado extends JButton {

    private static final long serialVersionUID = 1L;

    private final Color corBase;
    private final Color corHover;
    private boolean mouseSobre = false;

    public BotaoArredondado(String texto, Color corBase, Color corHover) {
        super(texto);
        this.corBase = corBase;
        this.corHover = corHover;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(Paleta.FONTE_BOTAO);
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseSobre = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseSobre = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color cor = isEnabled() ? (mouseSobre ? corHover : corBase) : new Color(0xB7, 0xBC, 0xC6);
        RoundRectangle2D forma = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10);
        g2.setColor(cor);
        g2.fill(forma);
        g2.dispose();
        super.paintComponent(g);
    }
}
